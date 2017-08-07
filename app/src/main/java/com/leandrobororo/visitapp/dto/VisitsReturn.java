package com.leandrobororo.visitapp.dto;

import com.leandrobororo.visitapp.model.Visit;

import java.util.List;

/**
 * Created by leandrobororo on 05/03/17.
 */
public class VisitsReturn {

    private List<Visit> results = null;

    public List<Visit> getResults() {
        return results;
    }

    public void setResults(List<Visit> results) {
        this.results = results;
    }
}
