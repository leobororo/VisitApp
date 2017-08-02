package com.leandrobororo.visitapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.*;

/**
 * Created by leandrobororo on 03/03/17.
 */

public class Visita implements Serializable {

    private static final String TEXTO_DATE = "Visita dia %s entre %02d:%02d e %02d:%02d horas";
    private static final String TEXTO_TIME_BOX = "Entre %02d:%02d e %02d:%02d horas";
    private static final String DATA = "%4d-%02d-%02d";
    private static final String HORA = "%02d:%02d:00";

    @SerializedName("nome_place")
    private String nomePlace;

    @SerializedName("endereco_place")
    private String enderecoPlace;

    @SerializedName("id_place")
    private String placeId;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("data_visita")
    private String dataVisita;

    @SerializedName("hora_inicio")
    private int horaInicioVisita;

    @SerializedName("minuto_inicio")
    private int minutoInicioVisita;

    @SerializedName("hora_fim")
    private int horaFimVisita;

    @SerializedName("minuto_fim")
    private int minutoFimVisita;

    @SerializedName("acompanhar_amigos")
    private boolean acompanharAmigos;

    @SerializedName("id_facebook")
    private String idFacebook;

    @SerializedName("objectId")
    private String objectId;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("createdAt")
    private String createdAt;

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getNomePlace() {
        return nomePlace;
    }

    public void setNomePlace(String nomePlace) {
        this.nomePlace = nomePlace;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDataVisita() {
        return dataVisita;
    }

    public void setDataVisita(String dataVisita) {
        this.dataVisita = dataVisita;
    }

    public int getHoraInicioVisita() {
        return horaInicioVisita;
    }

    public void setHoraInicioVisita(int horaInicioVisita) {
        this.horaInicioVisita = horaInicioVisita;
    }

    public int getMinutoInicioVisita() {
        return minutoInicioVisita;
    }

    public void setMinutoInicioVisita(int minutoInicioVisita) {
        this.minutoInicioVisita = minutoInicioVisita;
    }

    public int getHoraFimVisita() {
        return horaFimVisita;
    }

    public void setHoraFimVisita(int horaFimVisita) {
        this.horaFimVisita = horaFimVisita;
    }

    public int getMinutoFimVisita() {
        return minutoFimVisita;
    }

    public void setMinutoFimVisita(int minutoFimVisita) {
        this.minutoFimVisita = minutoFimVisita;
    }

    public boolean isAcompanharAmigos() {
        return acompanharAmigos;
    }

    public void setAcompanharAmigos(boolean acompanharAmigos) {
        this.acompanharAmigos = acompanharAmigos;
    }

    public String getIdFacebook() {
        return idFacebook;
    }

    public void setIdFacebook(String idFacebook) {
        this.idFacebook = idFacebook;
    }

    public String getEnderecoPlace() {
        return enderecoPlace;
    }

    public void setEnderecoPlace(String enderecoPlace) {
        this.enderecoPlace = enderecoPlace;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescricaoHorarioVisita() {
        return String.format(Locale.getDefault() ,TEXTO_TIME_BOX,
                horaInicioVisita,
                minutoInicioVisita,
                horaFimVisita,
                minutoFimVisita);
    }

    public String getDescricaoVisita() {
        return String.format(Locale.getDefault() ,TEXTO_DATE,
                dataVisita,
                horaInicioVisita,
                minutoInicioVisita,
                horaFimVisita,
                minutoFimVisita);
    }

    public CharSequence getDataVisitaFormatada() {
        return dataVisita;
    }

    public String obterDiaMesAnoStringDataVisita() {

        return String.format(DATA, Integer.parseInt(dataVisita.substring(6, 10)), Integer.parseInt(dataVisita.substring(3, 5)), Integer.parseInt(dataVisita.substring(0, 2)));
    }

    public String getHorarioMedioString() {
        int mediaHora = horaFimVisita + horaInicioVisita + (minutoFimVisita + minutoInicioVisita) / 60;
        int mediaMinutos = (minutoFimVisita + minutoInicioVisita)%60;

        return String.format(HORA, mediaHora / 2, mediaMinutos / 2 + ((int)(((double)mediaHora%2) / 2 * 60)));
    }

    public String getHorarioInicio() {
        return String.format(HORA, horaInicioVisita, minutoInicioVisita);
    }

    public String getHorarioFim() {
        return String.format(HORA, horaFimVisita, minutoFimVisita);
    }
}
