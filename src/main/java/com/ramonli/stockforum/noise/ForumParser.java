package com.ramonli.stockforum.noise;

import com.ramonli.stockforum.DateUtils;
import com.ramonli.stockforum.noise.dao.IssueStatDao;
import com.ramonli.stockforum.noise.dao.JdbcIssueStatDao;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class ForumParser {
    private String baseUrl = "http://guba.eastmoney.com/list,";

    /**
     * The parser will parse the forum issues since {@code latestExist} to yesterday. If no {@code latestExist}
     * found(the forum of given share hasn't been parsed before, {@code latestExist} will be set to the same day of
     * yesterday of latest year.
     * <p/>
     * For example today is 2015-04-16, if {@code latestExist} is 2015-02-01, then all issues from 2015-02-02 to
     * 2015-04-15 will be parsed. If no {@code latestExist} found, then issues from 2014-04-15 to 2015-04-15 will be
     * parsed.
     * 
     * @param shareCodes
     *            The share codes which will be parsed.
     * @return all available issues.The key of map will be share code, and list will be daily statistics of that issue.
     */
    public void parse(String... shareCodes) throws Exception {
        Connection conn = this.getConnection();
        try {
            for (String shareCode : shareCodes) {
                Map<String, IssueStat> dailyStat = parserSingleShare(conn, shareCode);
                IssueStatDao dao = new JdbcIssueStatDao(conn);
                dao.insert(new LinkedList(dailyStat.values()));
                conn.commit();
                System.out.println("[" + shareCode + "] Done!");
            }
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    protected Map<String, IssueStat> parserSingleShare(Connection conn, String shareCode) {
        Map<String, IssueStat> dailyMap = new LinkedHashMap<String, IssueStat>();
        Date[] dateRange = this.determineDateRange(conn, shareCode);
        if (dateRange == null) {
            System.out.println("[" + shareCode
                    + "] No issues will be parsed, as the latest statistics has already existed.");
            return dailyMap;
        }
        System.out.println("[" + shareCode + "] will try to parse issues during " + dateRange[1] + " and "
                + dateRange[1] + ".");

        // the max page number is 499
        for (int i = 0; i < 499; i++) {
            String realUrl = baseUrl + shareCode + ",f" + (i == 0 ? "" : ("_" + (i + 1))) + ".html";
            Document doc = null;
            try {
                System.out.println("[" + shareCode + "] Start to parse html page: " + realUrl);
                doc = Jsoup.connect(realUrl).get();
            } catch (IOException e) {
                System.out.println("[WARN] " + e.getMessage());
                return dailyMap;
            }
            Elements newsHeadlines = doc.select("#articlelistnew .articleh");
            // Elements newsHeadlines = doc.select("#articlelistnew > div.articleh > span.16");
            Iterator<Element> eleIterator = newsHeadlines.iterator();
            while (eleIterator.hasNext()) {
                // System.out.println(eleIterator.next().html());
                Element ele = eleIterator.next();

                // filter those public issues
                Element titleEle = ele.child(2);
                if (titleEle.child(0).html().equalsIgnoreCase("话题")) {
                    continue;
                }

                int viewOfIssue = Integer.parseInt(ele.child(0).html().trim());
                int countOfResponse = Integer.parseInt(ele.child(1).html().trim());
                String issueDay = ele.child(4).html().trim();
                // append year
                issueDay = this.determineIssueDate(issueDay, dateRange[0]);
                Date issueDate = DateUtils.parse(issueDay, "yyyy-MM-dd");
                if (DateUtils.isSameDay(issueDate, new Date())) {
                    continue;
                }

                IssueStat statOfDaily = dailyMap.get(issueDay);
                if (statOfDaily == null) {
                    if (dateRange[0].before(issueDate) || issueDate.before(dateRange[1])) {
                        System.out.println("[" + shareCode + "] has reached the end date " + dateRange[1]);
                        // no need to handle
                        return dailyMap;
                    }
                    System.out.println("[" + shareCode + "] Found issue date: " + issueDay);
                    statOfDaily = new IssueStat();
                    statOfDaily.setShareCode(shareCode);
                    statOfDaily.setIssueDate(issueDate);
                    statOfDaily.setCountOfIssue(1);
                    statOfDaily.setCountOfResponse(countOfResponse);
                    statOfDaily.setCountOfView(viewOfIssue);
                    dailyMap.put(issueDay, statOfDaily);
                } else {
                    statOfDaily.setCountOfIssue(statOfDaily.getCountOfIssue() + 1);
                    statOfDaily.setCountOfResponse(statOfDaily.getCountOfResponse() + countOfResponse);
                    statOfDaily.setCountOfView(statOfDaily.getCountOfView() + viewOfIssue);
                }
            }
        }
        return dailyMap;
    }

    protected String determineIssueDate(String issueDay, Date beginDate) {
        // append year, as there is no year in issue date
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String realIssueDay = year + "-" + issueDay;
        if (DateUtils.parse(realIssueDay, "yyyy-MM-dd").after(new Date())) {
            // the parser will parse issues of max of last year
            // calculate last year
            Calendar tmpCal = Calendar.getInstance();
            tmpCal.setTime(DateUtils.addYear(beginDate, -1));
            realIssueDay = tmpCal.get(Calendar.YEAR) + "-" + issueDay;
        }
        return realIssueDay;
    }

    /**
     * Determine the date range in which the issues should be parsed.
     * 
     * @param conn
     *            Database connection.
     * @param shareCode
     *            The code of share.
     * @return a date array, the 1st is begin date, and the 2nd is end date, or null will be returned, if no any issues
     *         will be parsed.
     */
    protected Date[] determineDateRange(Connection conn, String shareCode) {
        Date[] dateRange = new Date[2];
        // yesterday
        Date beginDate = DateUtils.addDay(new Date(), -1);
        Date endDate = DateUtils.addDay(DateUtils.addYear(new Date(), -1), 1);

        IssueStatDao dao = new JdbcIssueStatDao(conn);
        IssueStat stat = dao.findLatest(shareCode);
        if (stat == null) {
            endDate = DateUtils.addDay(DateUtils.addYear(new Date(), -1), 1);
        } else {
            Date tmpEndDate = DateUtils.addDay(stat.getIssueDate(), 1);
            if (tmpEndDate.after(endDate)) {
                endDate = tmpEndDate;
            }
        }

        if (!endDate.before(beginDate)) {
            return null;
        }

        dateRange[0] = beginDate;
        dateRange[1] = endDate;
        return dateRange;
    }

    protected Connection getConnection() throws Exception {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:~/forumstat", "sa", "");
        conn.setAutoCommit(false);
        return conn;
    }

    public static void main(String[] args) throws Exception {
        ForumParser main = new ForumParser();
        // Date[] dateRange = main.determineDateRange(main.getConnection(), "600405");
        // System.out.println(dateRange[0]);
        // System.out.println(dateRange[1]);
        //
        // dateRange = main.determineDateRange(main.getConnection(), "002243");
        // System.out.println(dateRange[0]);
        // System.out.println(dateRange[1]);

        String realDay = main.determineIssueDate("03-01", DateUtils.addDay(new Date(), -1));
        System.out.println(realDay);
    }
}
