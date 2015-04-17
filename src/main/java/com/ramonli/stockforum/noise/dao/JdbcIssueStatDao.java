package com.ramonli.stockforum.noise.dao;

import com.ramonli.stockforum.noise.IssueStat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class JdbcIssueStatDao implements IssueStatDao {
    private Connection conn;

    public JdbcIssueStatDao(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(IssueStat stat) {
        List stats = new LinkedList<IssueStat>();
        this.insert(stats);
    }

    @Override
    public void insert(List<IssueStat> stats) {
        try {
            PreparedStatement ps = conn.prepareStatement("insert into forum_stat(share_code,issue_date,"
                    + "count_of_issue,count_of_view,count_of_response) values(?,?,?,?,?)");
            for (IssueStat stat : stats) {
                ps.setString(1, stat.getShareCode());
                ps.setDate(2, new java.sql.Date(stat.getIssueDate().getTime()));
                ps.setInt(3, stat.getCountOfIssue());
                ps.setInt(4, stat.getCountOfView());
                ps.setInt(5, stat.getCountOfResponse());
                ps.addBatch();
            }

            ps.executeBatch();
            ps.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IssueStat findLatest(String shareCode) {
        IssueStat issueStat = null;
        try {
            PreparedStatement ps = conn.prepareStatement("select id,issue_date,count_of_issue,"
                    + "count_of_view,count_of_response from forum_stat where share_code=? order by issue_date desc");
            ps.setString(1, shareCode);
            // we only need the 1st row.
            ps.setMaxRows(1);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                issueStat = new IssueStat();
                issueStat.setId(rs.getLong(1));
                issueStat.setShareCode(shareCode);
                issueStat.setIssueDate(new Date(rs.getDate(2).getTime()));
                issueStat.setCountOfIssue(rs.getInt(3));
                issueStat.setCountOfView(rs.getInt(4));
                issueStat.setCountOfResponse(rs.getInt(5));
            }

            rs.close();
            ps.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return issueStat;
    }
}
