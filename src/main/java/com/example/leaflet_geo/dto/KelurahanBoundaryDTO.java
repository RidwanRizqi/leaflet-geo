package com.example.leaflet_geo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KelurahanBoundaryDTO {
    private String id;
    
    @JsonProperty("kd_kec")
    private String kdKec;
    
    @JsonProperty("kd_kel")
    private String kdKel;
    
    private String nama;
    
    // GeoJSON geometry object for frontend
    private Object geojson;
    
    // WKB hex string (internal use only, not sent to frontend)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String geom;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    public KelurahanBoundaryDTO() {}
    
    public KelurahanBoundaryDTO(String id, String kdKec, String kdKel, String nama, Boolean isActive, String geom) {
        this.id = id;
        this.kdKec = kdKec;
        this.kdKel = kdKel;
        this.nama = nama;
        this.isActive = isActive;
        this.geom = geom;
    }
    
    // Getters and setters
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
    
    public String getNama() {
        return nama;
    }
    
    public void setNama(String nama) {
        this.nama = nama;
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
}
