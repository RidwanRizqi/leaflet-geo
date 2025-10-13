package com.example.leaflet_geo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Kecamatan Boundary with GeoJSON geometry
 * Converts WKB hex string from BPRD API to GeoJSON format for Leaflet
 */
public class KecamatanBoundaryDTO {
    
    private String id;
    
    @JsonProperty("kd_kec")
    private String kdKec;
    
    private String nama;
    
    private String color;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    // GeoJSON geometry object (not WKB hex string)
    private Object geojson;
    
    // Original WKB hex string (kept for reference, not sent to frontend)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String geom;
    
    public KecamatanBoundaryDTO() {
    }
    
    public KecamatanBoundaryDTO(String id, String kdKec, String nama, String color, Boolean isActive, String geom) {
        this.id = id;
        this.kdKec = kdKec;
        this.nama = nama;
        this.color = color;
        this.isActive = isActive;
        this.geom = geom;
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

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

    @Override
    public String toString() {
        return "KecamatanBoundaryDTO{" +
                "id='" + id + '\'' +
                ", kdKec='" + kdKec + '\'' +
                ", nama='" + nama + '\'' +
                ", color='" + color + '\'' +
                ", isActive=" + isActive +
                ", hasGeojson=" + (geojson != null) +
                '}';
    }
}
