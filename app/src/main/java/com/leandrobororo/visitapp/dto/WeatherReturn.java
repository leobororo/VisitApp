package com.leandrobororo.visitapp.dto;

import com.leandrobororo.visitapp.model.Forecast;

import java.util.List;

/**
 * Created by leandrobororo on 05/03/17.
 */

public class WeatherReturn {

    private String cod;
    private Double message;
    private Integer cnt;
    private List<Forecast> list = null;

    public String getCod() {
        return cod;
    }

    public void setCod(String cod) {
        this.cod = cod;
    }

    public Double getMessage() {
        return message;
    }

    public void setMessage(Double message) {
        this.message = message;
    }

    public Integer getCnt() {
        return cnt;
    }

    public void setCnt(Integer cnt) {
        this.cnt = cnt;
    }

    public List<Forecast> getList() {
        return list;
    }

    public void setList(List<Forecast> forecast) {
        this.list = forecast;
    }
}
