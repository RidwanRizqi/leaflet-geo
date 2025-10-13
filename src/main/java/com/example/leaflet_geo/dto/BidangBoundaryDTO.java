package com.example.leaflet_geo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for bidang boundary data from BPRD API
 * Backend converts WKB hex string to GeoJSON for frontend
 */
public class BidangBoundaryDTO {
    private String id;
    
    @JsonProperty("kd_prop")
    private String kdProp;
    
    @JsonProperty("kd_dati2")
    private String kdDati2;
    
    @JsonProperty("kd_kec")
    private String kdKec;
    
    @JsonProperty("kd_kel")
    private String kdKel;
    
    @JsonProperty("kd_blok")
    private String kdBlok;
    
    @JsonProperty("no_urut")
    private String noUrut;
    
    @JsonProperty("kd_jns_op")
    private String kdJnsOp;
    
    private String nop;
    
    private Object geojson; // GeoJSON geometry object for frontend
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String geom; // WKB hex string from BPRD (internal only)
    
    @JsonProperty("is_active")
    private Boolean isActive;

    // Constructors
    public BidangBoundaryDTO() {}

    public BidangBoundaryDTO(String id, String kdProp, String kdDati2, String kdKec, 
                             String kdKel, String kdBlok, String noUrut, String kdJnsOp,
                             String nop, String geom, Boolean isActive) {
        this.id = id;
        this.kdProp = kdProp;
        this.kdDati2 = kdDati2;
        this.kdKec = kdKec;
        this.kdKel = kdKel;
        this.kdBlok = kdBlok;
        this.noUrut = noUrut;
        this.kdJnsOp = kdJnsOp;
        this.nop = nop;
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

    public String getKdProp() {
        return kdProp;
    }

    public void setKdProp(String kdProp) {
        this.kdProp = kdProp;
    }

    public String getKdDati2() {
        return kdDati2;
    }

    public void setKdDati2(String kdDati2) {
        this.kdDati2 = kdDati2;
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

    public String getNoUrut() {
        return noUrut;
    }

    public void setNoUrut(String noUrut) {
        this.noUrut = noUrut;
    }

    public String getKdJnsOp() {
        return kdJnsOp;
    }

    public void setKdJnsOp(String kdJnsOp) {
        this.kdJnsOp = kdJnsOp;
    }

    public String getNop() {
        return nop;
    }

    public void setNop(String nop) {
        this.nop = nop;
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
        return "BidangBoundaryDTO{" +
                "id='" + id + '\'' +
                ", nop='" + nop + '\'' +
                ", kdBlok='" + kdBlok + '\'' +
                ", noUrut='" + noUrut + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
