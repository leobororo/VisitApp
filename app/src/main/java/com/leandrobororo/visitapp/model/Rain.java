package com.leandrobororo.visitapp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by leandrobororo on 05/03/17.
 */
public class Rain {

    @SerializedName("3h")
    private Double _3h;

    public Double get3h() {
        return _3h;
    }

    public void set3h(Double _3h) {
        this._3h = _3h;
    }

}
