package com.example.leaflet_geo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for blok boundary data from BPRD API
 * Backend converts WKB hex string to GeoJSON for frontend
 */
public class BlokBoundaryDTO {
    private String id;
    
    @JsonProperty("kd_kec")
    private String kdKec;
    
    @JsonProperty("kd_kel")
    private String kdKel;
    
    @JsonProperty("kd_blok")
    private String kdBlok;
    
    private Object geojson; // GeoJSON geometry object for frontend
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String geom; // WKB hex string from BPRD (internal only)
    
    @JsonProperty("is_active")
    private Boolean isActive;

    // Constructors
    public BlokBoundaryDTO() {}

    public BlokBoundaryDTO(String id, String kdKec, String kdKel, String kdBlok, String geom, Boolean isActive) {
        this.id = id;
        this.kdKec = kdKec;
        this.kdKel = kdKel;
        this.kdBlok = kdBlok;
        this.geom = geom;
        this.isActive = isActive;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKdKec() {
        return kdKec;
    }

    public void setKdKec(String kdKec) {
        this.kdKec = kdKec;
    }

    public String getKdKel() {
        return kdKel;
    }

    public void setKdKel(String kdKel) {
        this.kdKel = kdKel;
    }

    public String getKdBlok() {
        return kdBlok;
    }

    public void setKdBlok(String kdBlok) {
        this.kdBlok = kdBlok;
    }

    public Object getGeojson() {
        return geojson;
    }

    public void setGeojson(Object geojson) {
        this.geojson = geojson;
    }

    public String getGeom() {
        return geom;
    }

    public void setGeom(String geom) {
        this.geom = geom;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public String toString() {
        return "BlokBoundaryDTO{" +
                "id='" + id + '\'' +
                ", kdKec='" + kdKec + '\'' +
                ", kdKel='" + kdKel + '\'' +
                ", kdBlok='" + kdBlok + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}