package com.leandrobororo.visitapp.model;

import java.io.Serializable;

/**
 * Created by leandrobororo on 05/03/17.
 */
public class Friend implements Serializable {

    private String name;
    private Visit visit;

    public String getId() {
        return visit.getIdFacebook();
    }

    public void setId(String id) {
        visit.setIdFacebook(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        return obj instanceof Friend && ((Friend) obj).getId() == visit.getIdFacebook();
    }
}
