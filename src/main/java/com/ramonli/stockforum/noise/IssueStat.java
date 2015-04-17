package com.ramonli.stockforum.noise;

import java.util.Date;

public class IssueStat {
    private long id;
    private String shareCode;
    private Date issueDate;
    private int countOfIssue;
    private int countOfView;
    private int countOfResponse;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getShareCode() {
        return shareCode;
    }

    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public int getCountOfIssue() {
        return countOfIssue;
    }

    public void setCountOfIssue(int countOfIssue) {
        this.countOfIssue = countOfIssue;
    }

    public int getCountOfView() {
        return countOfView;
    }

    public void setCountOfView(int countOfView) {
        this.countOfView = countOfView;
    }

    public int getCountOfResponse() {
        return countOfResponse;
    }

    public void setCountOfResponse(int countOfResponse) {
        this.countOfResponse = countOfResponse;
    }

}
