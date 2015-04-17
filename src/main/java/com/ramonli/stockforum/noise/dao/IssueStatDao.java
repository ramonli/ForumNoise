package com.ramonli.stockforum.noise.dao;

import com.ramonli.stockforum.noise.IssueStat;

import java.util.List;

public interface IssueStatDao {
    
    void insert(IssueStat stat);
    
    void insert(List<IssueStat> stats);
    
    IssueStat findLatest(String shareCode);
}
