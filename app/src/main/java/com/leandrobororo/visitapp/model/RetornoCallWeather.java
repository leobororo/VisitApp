package com.leandrobororo.visitapp.model;

import java.util.List;

/**
 * Created by leandrobororo on 05/03/17.
 */

public class RetornoCallWeather {

    private String cod;
    private Double message;
    private Integer cnt;
    private List<Previsao> list = null;

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

    public List<Previsao> getList() {
        return list;
    }

    public void setList(List<Previsao> previsao) {
        this.list = previsao;
    }
}
