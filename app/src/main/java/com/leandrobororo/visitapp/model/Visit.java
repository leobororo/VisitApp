package com.leandrobororo.visitapp.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.*;

/**
 * Created by leandrobororo on 03/03/17.
 */
public class Visit implements Serializable {

    private static final String TEXTO_DATE = "Visita dia %s entre %02d:%02d e %02d:%02d horas";

    @SerializedName("nome_place")
    private String placeName;

    @SerializedName("endereco_place")
    private String placeAddress;

    @SerializedName("id_place")
    private String placeId;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("data_visita")
    private String visitDate;

    @SerializedName("hora_inicio")
    private int startHour;

    @SerializedName("minuto_inicio")
    private int startMinute;

    @SerializedName("hora_fim")
    private int endHour;

    @SerializedName("minuto_fim")
    private int endMinute;

    @SerializedName("acompanhar_amigos")
    private boolean followUpFriends;

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

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
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

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setEndMinute(int endMinute) {
        this.endMinute = endMinute;
    }

    public boolean isFollowUpFriends() {
        return followUpFriends;
    }

    public void setFollowUpFriends(boolean followUpFriends) {
        this.followUpFriends = followUpFriends;
    }

    public String getIdFacebook() {
        return idFacebook;
    }

    public void setIdFacebook(String idFacebook) {
        this.idFacebook = idFacebook;
    }

    public String getPlaceAddress() {
        return placeAddress;
    }

    public void setPlaceAddress(String placeAddress) {
        this.placeAddress = placeAddress;
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

    public String getDescricaoVisita() {
        return String.format(Locale.getDefault() ,TEXTO_DATE,
                visitDate,
                startHour,
                startMinute,
                endHour,
                endMinute);
    }
}
