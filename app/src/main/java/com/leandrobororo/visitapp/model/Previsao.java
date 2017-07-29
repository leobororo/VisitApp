package com.leandrobororo.visitapp.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static java.lang.Integer.*;

/**
 * Created by leandrobororo on 05/03/17.
 */
public class Previsao {

    private Integer dt;
    private Main main;
    private List<Weather> weather = null;
    private Rain rain;
    private String dt_txt;

    public Integer getDt() {
        return dt;
    }

    public void setDt(Integer dt) {
        this.dt = dt;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

    public String getDt_txt() {
        return dt_txt;
    }

    public void setDt_txt(String dt_txt) {
        this.dt_txt = dt_txt;
    }

    public String getTextoData() {
        return dt_txt.substring(0, 10);
    }

    public Date getDataPrevisao() {
        Calendar dataVisita = new GregorianCalendar();
        dataVisita.set(parseInt(dt_txt.substring(0, 4)), parseInt(dt_txt.substring(5, 7)), parseInt(dt_txt.substring(8, 10)));

        return dataVisita.getTime();
    }

    public String getStringDiaMesAnoPrevisao() {
        return getDt_txt().substring(0, 10);
    }
}
