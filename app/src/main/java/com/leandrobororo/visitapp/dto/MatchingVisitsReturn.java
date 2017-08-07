package com.leandrobororo.visitapp.dto;

import com.leandrobororo.visitapp.model.Visit;

import java.io.Serializable;
import java.util.List;

/**
 * Created by leandrobororo on 30/07/17.
 */
public class MatchingVisitsReturn implements Serializable {

    private List<Visit> SameDayVisits;
    private List<Visit> SameTimeVisits;

    public List<Visit> getSameDayVisits() {
        return SameDayVisits;
    }

    public void setSameDayVisits(List<Visit> sameDayVisits) {
        SameDayVisits = sameDayVisits;
    }

    public List<Visit> getSameTimeVisits() {
        return SameTimeVisits;
    }

    public void setSameTimeVisits(List<Visit> sameTimeVisits) {
        SameTimeVisits = sameTimeVisits;
    }
}
