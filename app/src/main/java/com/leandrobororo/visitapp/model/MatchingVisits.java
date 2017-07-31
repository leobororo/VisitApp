package com.leandrobororo.visitapp.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by leandrobororo on 30/07/17.
 */

public class MatchingVisits implements Serializable {

    private List<Visita> SameDayVisits;
    private List<Visita> SameTimeVisits;

    public List<Visita> getSameDayVisits() {
        return SameDayVisits;
    }

    public void setSameDayVisits(List<Visita> sameDayVisits) {
        SameDayVisits = sameDayVisits;
    }

    public List<Visita> getSameTimeVisits() {
        return SameTimeVisits;
    }

    public void setSameTimeVisits(List<Visita> sameTimeVisits) {
        SameTimeVisits = sameTimeVisits;
    }
}
