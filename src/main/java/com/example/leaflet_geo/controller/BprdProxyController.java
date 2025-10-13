package com.example.leaflet_geo.controller;

import com.example.leaflet_geo.dto.KecamatanBoundaryDTO;
import com.example.leaflet_geo.dto.KelurahanBoundaryDTO;
import com.example.leaflet_geo.dto.BlokBoundaryDTO;
import com.example.leaflet_geo.dto.BidangBoundaryDTO;
import com.example.leaflet_geo.util.WkbToGeoJsonConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bprd")
@CrossOrigin(origins = "*")
public class BprdProxyController {
    
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String cachedToken = null;
    private final String BPRD_BASE_URL = "https://bprd.lumajangkab.go.id:1151/api";
    
    public BprdProxyController() {
        System.out.println("🏗️ BprdProxyController initialized!");
    }    /**
     * Simple endpoint that handles login and get boundaries in one call
     * Converts WKB hex string to GeoJSON for Leaflet consumption
     */
    @GetMapping("/boundaries")
    public ResponseEntity<?> getBoundaries() {
        System.out.println("🗺️ Boundaries request received");
        
        try {
            // Step 1: Login if no token
            if (cachedToken == null) {
                System.out.println("🔑 No token found, logging in...");
                
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", "user");
                credentials.put("password", "user");
                
                HttpHeaders loginHeaders = new HttpHeaders();
                loginHeaders.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(credentials, loginHeaders);
                
                ResponseEntity<String> loginResponse = restTemplate.exchange(
                    BPRD_BASE_URL + "/user/login",
                    HttpMethod.POST,
                    loginRequest,
                    String.class
                );
                
                if (loginResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> loginResult = objectMapper.readValue(loginResponse.getBody(), Map.class);
                    Map<String, Object> user = (Map<String, Object>) loginResult.get("user");
                    
                    if (user != null && user.get("token") != null) {
                        cachedToken = (String) user.get("token");
                        System.out.println("✅ Login successful, token: " + cachedToken.substring(0, 10) + "...");
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Login failed - no token received"));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Login failed with status: " + loginResponse.getStatusCode()));
                }
            }
            
            // Step 2: Get boundaries
            System.out.println("🗺️ Getting boundaries with token...");
            
            HttpHeaders boundariesHeaders = new HttpHeaders();
            boundariesHeaders.set("Authorization", "Bearer " + cachedToken);
            boundariesHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> boundariesRequest = new HttpEntity<>(boundariesHeaders);
            
            ResponseEntity<String> boundariesResponse = restTemplate.exchange(
                BPRD_BASE_URL + "/kecamatan/list?option=false",
                HttpMethod.GET,
                boundariesRequest,
                String.class
            );
            
            if (boundariesResponse.getStatusCode() == HttpStatus.OK) {
                // Parse response as array of boundary objects
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rawBoundaries = objectMapper.readValue(
                    boundariesResponse.getBody(), 
                    List.class
                );
                
                System.out.println("📡 Received " + rawBoundaries.size() + " raw boundaries from BPRD API");
                
                // Convert WKB to GeoJSON for each boundary
                List<KecamatanBoundaryDTO> convertedBoundaries = new ArrayList<>();
                int successCount = 0;
                int failCount = 0;
                
                for (Map<String, Object> rawBoundary : rawBoundaries) {
                    try {
                        String id = (String) rawBoundary.get("id");
                        String kdKec = (String) rawBoundary.get("kd_kec");
                        String nama = (String) rawBoundary.get("nama");
                        String color = (String) rawBoundary.get("color");
                        Boolean isActive = (Boolean) rawBoundary.get("is_active");
                        String geomWkbHex = (String) rawBoundary.get("geom");
                        
                        // Create DTO
                        KecamatanBoundaryDTO dto = new KecamatanBoundaryDTO(
                            id, kdKec, nama, color, isActive, geomWkbHex
                        );
                        
                        // Convert WKB hex to GeoJSON
                        if (geomWkbHex != null && !geomWkbHex.isEmpty()) {
                            Map<String, Object> geoJson = WkbToGeoJsonConverter.convertWkbHexToGeoJson(geomWkbHex);
                            
                            if (geoJson != null) {
                                dto.setGeojson(geoJson);
                                successCount++;
                                System.out.println("✅ Converted " + nama + " to GeoJSON");
                            } else {
                                failCount++;
                                System.err.println("⚠️ Failed to convert " + nama + " - setting null GeoJSON");
                                dto.setGeojson(null);
                            }
                        } else {
                            failCount++;
                            System.err.println("⚠️ No geometry data for " + nama);
                        }
                        
                        convertedBoundaries.add(dto);
                        
                    } catch (Exception e) {
                        failCount++;
                        System.err.println("❌ Error processing boundary: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                System.out.println("📊 Conversion complete: " + successCount + " success, " + failCount + " failed");
                System.out.println("✅ Successfully processed " + convertedBoundaries.size() + " boundaries");
                
                return ResponseEntity.ok(convertedBoundaries);
            } else {
                // Token might be expired, clear it
                cachedToken = null;
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch boundaries: " + boundariesResponse.getStatusCode()));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            
            // Clear token on error and try to return useful info
            cachedToken = null;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        System.out.println("🧪 Test endpoint hit");
        return ResponseEntity.ok(Map.of(
            "message", "Controller is working!",
            "timestamp", System.currentTimeMillis(),
            "hasToken", cachedToken != null
        ));
    }

    /**
     * Get kelurahan boundaries for a specific kecamatan
     * Converts WKB hex string to GeoJSON for Leaflet consumption
     */
    @GetMapping("/kelurahan")
    public ResponseEntity<?> getKelurahanBoundaries(@RequestParam("kd_kec") String kdKec) {
        System.out.println("🏘️ Kelurahan boundaries request received for kecamatan: " + kdKec);
        
        try {
            // Step 1: Login if no token
            if (cachedToken == null) {
                System.out.println("🔑 No token found, logging in...");
                
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", "user");
                credentials.put("password", "user");
                
                HttpHeaders loginHeaders = new HttpHeaders();
                loginHeaders.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(credentials, loginHeaders);
                
                ResponseEntity<String> loginResponse = restTemplate.exchange(
                    BPRD_BASE_URL + "/user/login",
                    HttpMethod.POST,
                    loginRequest,
                    String.class
                );
                
                if (loginResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> loginResult = objectMapper.readValue(loginResponse.getBody(), Map.class);
                    Map<String, Object> user = (Map<String, Object>) loginResult.get("user");
                    
                    if (user != null && user.get("token") != null) {
                        cachedToken = (String) user.get("token");
                        System.out.println("✅ Login successful for kelurahan request");
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Login failed - no token received"));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Login failed with status: " + loginResponse.getStatusCode()));
                }
            }
            
            // Step 2: Get kelurahan boundaries
            System.out.println("🏘️ Getting kelurahan boundaries for kecamatan " + kdKec + " with token...");
            
            HttpHeaders boundariesHeaders = new HttpHeaders();
            boundariesHeaders.set("Authorization", "Bearer " + cachedToken);
            boundariesHeaders.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> boundariesRequest = new HttpEntity<>(boundariesHeaders);
            
            String url = BPRD_BASE_URL + "/kelurahan/list?kd_kec=" + kdKec + "&option=false";
            System.out.println("🌐 Requesting: " + url);
            
            ResponseEntity<String> boundariesResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                boundariesRequest,
                String.class
            );
            
            if (boundariesResponse.getStatusCode() == HttpStatus.OK) {
                // Parse response as array of boundary objects
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> rawBoundaries = objectMapper.readValue(
                    boundariesResponse.getBody(), 
                    List.class
                );
                
                System.out.println("📡 Received " + rawBoundaries.size() + " kelurahan boundaries from BPRD API");
                
                // Convert WKB to GeoJSON for each boundary
                List<KelurahanBoundaryDTO> convertedBoundaries = new ArrayList<>();
                int successCount = 0;
                int failCount = 0;
                
                for (Map<String, Object> rawBoundary : rawBoundaries) {
                    try {
                        String id = (String) rawBoundary.get("id");
                        String kecId = (String) rawBoundary.get("kd_kec");
                        String kelId = (String) rawBoundary.get("kd_kel");
                        String nama = (String) rawBoundary.get("nama");
                        Boolean isActive = (Boolean) rawBoundary.get("is_active");
                        String geomWkbHex = (String) rawBoundary.get("geom");
                        
                        // Create DTO
                        KelurahanBoundaryDTO dto = new KelurahanBoundaryDTO(
                            id, kecId, kelId, nama, isActive, geomWkbHex
                        );
                        
                        // Convert WKB hex to GeoJSON
                        if (geomWkbHex != null && !geomWkbHex.isEmpty()) {
                            Map<String, Object> geoJson = WkbToGeoJsonConverter.convertWkbHexToGeoJson(geomWkbHex);
                            
                            if (geoJson != null) {
                                dto.setGeojson(geoJson);
                                successCount++;
                                System.out.println("✅ Converted kelurahan " + nama + " to GeoJSON");
                            } else {
                                failCount++;
                                System.err.println("⚠️ Failed to convert kelurahan " + nama + " - setting null GeoJSON");
                                dto.setGeojson(null);
                            }
                        } else {
                            failCount++;
                            System.err.println("⚠️ No geometry data for kelurahan " + nama);
                        }
                        
                        convertedBoundaries.add(dto);
                        
                    } catch (Exception e) {
                        failCount++;
                        System.err.println("❌ Error processing kelurahan boundary: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                System.out.println("📊 Kelurahan conversion complete: " + successCount + " success, " + failCount + " failed");
                System.out.println("✅ Successfully processed " + convertedBoundaries.size() + " kelurahan boundaries");
                
                return ResponseEntity.ok(convertedBoundaries);
            } else {
                // Token might be expired, clear it
                cachedToken = null;
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch kelurahan boundaries: " + boundariesResponse.getStatusCode()));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error fetching kelurahan boundaries: " + e.getMessage());
            e.printStackTrace();
            
            // Clear token on error
            cachedToken = null;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    /**
     * Get blok boundaries for specific kecamatan and kelurahan from BPRD API
     * 
     * @param kdKec Kode Kecamatan
     * @param kdKel Kode Kelurahan
     * @return List of blok boundaries with WKB converted to GeoJSON
     */
    @GetMapping("/blok")
    public ResponseEntity<?> getBlokBoundaries(
            @RequestParam("kd_kec") String kdKec,
            @RequestParam("kd_kel") String kdKel) {
        
        System.out.println("🏗️ Getting blok boundaries for kd_kec=" + kdKec + ", kd_kel=" + kdKel);
        
        try {
            // Step 1: Login if no token
            if (cachedToken == null) {
                System.out.println("� No token found, logging in...");
                
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", "user");
                credentials.put("password", "user");
                
                HttpHeaders loginHeaders = new HttpHeaders();
                loginHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(credentials, loginHeaders);
                
                String loginUrl = BPRD_BASE_URL + "/auth/signin";
                System.out.println("📡 Calling login API: " + loginUrl);
                
                ResponseEntity<Map> loginResponse = restTemplate.exchange(
                    loginUrl,
                    HttpMethod.POST,
                    loginRequest,
                    Map.class
                );
                
                if (loginResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> responseBody = loginResponse.getBody();
                    if (responseBody != null && responseBody.containsKey("token")) {
                        cachedToken = (String) responseBody.get("token");
                        System.out.println("✅ Login successful! Token cached.");
                    } else {
                        System.err.println("❌ Login response missing token");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Authentication failed - no token in response"));
                    }
                } else {
                    System.err.println("❌ Login failed with status: " + loginResponse.getStatusCode());
                    return ResponseEntity.status(loginResponse.getStatusCode())
                        .body(Map.of("error", "Authentication failed"));
                }
            }

            if (cachedToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Failed to authenticate with BPRD API"));
            }

            // Call BPRD blok API
            String blokUrl = BPRD_BASE_URL + "/blok/list?kd_kec=" + kdKec + "&kd_kel=" + kdKel;
            System.out.println("📡 Calling BPRD blok API: " + blokUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + cachedToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(
                blokUrl,
                HttpMethod.GET,
                entity,
                List.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                System.err.println("❌ BPRD blok API returned status: " + response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "BPRD API error: " + response.getStatusCode()));
            }

            // Parse response
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawBlokBoundaries = (List<Map<String, Object>>) response.getBody();

            if (rawBlokBoundaries == null || rawBlokBoundaries.isEmpty()) {
                System.out.println("⚠️ No blok boundaries found for kd_kec=" + kdKec + ", kd_kel=" + kdKel);
                return ResponseEntity.ok(new ArrayList<>());
            }

            System.out.println("📡 Received " + rawBlokBoundaries.size() + " raw blok boundaries from BPRD API");

            // Convert WKB to GeoJSON for each blok boundary
            List<BlokBoundaryDTO> convertedBlokBoundaries = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (Map<String, Object> rawBlokBoundary : rawBlokBoundaries) {
                try {
                    String id = (String) rawBlokBoundary.get("id");
                    String kdKecResp = (String) rawBlokBoundary.get("kd_kec");
                    String kdKelResp = (String) rawBlokBoundary.get("kd_kel");
                    String kdBlok = (String) rawBlokBoundary.get("kd_blok");
                    Boolean isActive = (Boolean) rawBlokBoundary.get("is_active");
                    String geomWkbHex = (String) rawBlokBoundary.get("geom");

                    // Create DTO
                    BlokBoundaryDTO dto = new BlokBoundaryDTO(
                        id, kdKecResp, kdKelResp, kdBlok, geomWkbHex, isActive);

                    // Convert WKB hex to GeoJSON
                    if (geomWkbHex != null && !geomWkbHex.trim().isEmpty()) {
                        Map<String, Object> geoJson = WkbToGeoJsonConverter.convertWkbHexToGeoJson(geomWkbHex);
                        dto.setGeojson(geoJson);
                        successCount++;
                        System.out.println("✅ Converted blok " + kdBlok + " geometry successfully");
                    } else {
                        System.out.println("⚠️ Empty geometry for blok " + kdBlok);
                        failCount++;
                    }

                    convertedBlokBoundaries.add(dto);

                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Failed to process blok boundary: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("🏗️ Blok boundary conversion complete: " + successCount + " success, " + failCount + " failed");
            return ResponseEntity.ok(convertedBlokBoundaries);

        } catch (Exception e) {
            System.err.println("❌ Error getting blok boundaries: " + e.getMessage());
            e.printStackTrace();
            
            // Clear token on error
            cachedToken = null;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    /**
     * Get bidang boundaries for specific kecamatan, kelurahan, and blok from BPRD API
     * 
     * @param kdKec Kode Kecamatan
     * @param kdKel Kode Kelurahan
     * @param kdBlok Kode Blok
     * @return List of bidang boundaries with WKB converted to GeoJSON
     */
    @GetMapping("/bidang")
    public ResponseEntity<?> getBidangBoundaries(
            @RequestParam("kd_kec") String kdKec,
            @RequestParam("kd_kel") String kdKel,
            @RequestParam("kd_blok") String kdBlok) {
        
        System.out.println("📦 Getting bidang boundaries for kd_kec=" + kdKec + ", kd_kel=" + kdKel + ", kd_blok=" + kdBlok);
        
        try {
            // Step 1: Login if no token
            if (cachedToken == null) {
                System.out.println("🔑 No token found, logging in...");
                
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", "user");
                credentials.put("password", "user");
                
                HttpHeaders loginHeaders = new HttpHeaders();
                loginHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(credentials, loginHeaders);
                
                String loginUrl = BPRD_BASE_URL + "/auth/signin";
                System.out.println("📡 Calling login API: " + loginUrl);
                
                ResponseEntity<Map> loginResponse = restTemplate.exchange(
                    loginUrl,
                    HttpMethod.POST,
                    loginRequest,
                    Map.class
                );
                
                if (loginResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> responseBody = loginResponse.getBody();
                    if (responseBody != null && responseBody.containsKey("token")) {
                        cachedToken = (String) responseBody.get("token");
                        System.out.println("✅ Login successful! Token cached.");
                    } else {
                        System.err.println("❌ Login response missing token");
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Authentication failed - no token in response"));
                    }
                } else {
                    System.err.println("❌ Login failed with status: " + loginResponse.getStatusCode());
                    return ResponseEntity.status(loginResponse.getStatusCode())
                        .body(Map.of("error", "Authentication failed"));
                }
            }

            if (cachedToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Failed to authenticate with BPRD API"));
            }

            // Call BPRD bidang API
            String bidangUrl = BPRD_BASE_URL + "/bidang/list?kd_kec=" + kdKec + "&kd_kel=" + kdKel + "&kd_blok=" + kdBlok;
            System.out.println("📡 Calling BPRD bidang API: " + bidangUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + cachedToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<List> response = restTemplate.exchange(
                bidangUrl,
                HttpMethod.GET,
                entity,
                List.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                System.err.println("❌ BPRD bidang API returned status: " + response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "BPRD API error: " + response.getStatusCode()));
            }

            // Parse response
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rawBidangBoundaries = (List<Map<String, Object>>) response.getBody();

            if (rawBidangBoundaries == null || rawBidangBoundaries.isEmpty()) {
                System.out.println("⚠️ No bidang boundaries found for kd_kec=" + kdKec + ", kd_kel=" + kdKel + ", kd_blok=" + kdBlok);
                return ResponseEntity.ok(new ArrayList<>());
            }

            System.out.println("📡 Received " + rawBidangBoundaries.size() + " raw bidang boundaries from BPRD API");

            // Convert WKB to GeoJSON for each bidang boundary
            List<BidangBoundaryDTO> convertedBidangBoundaries = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;

            for (Map<String, Object> rawBidangBoundary : rawBidangBoundaries) {
                try {
                    // Debug: Log available keys in first boundary
                    if (successCount == 0) {
                        System.out.println("🔍 Available keys in raw bidang boundary: " + rawBidangBoundary.keySet());
                        System.out.println("📋 Sample raw bidang boundary: " + rawBidangBoundary);
                    }
                    
                    String id = (String) rawBidangBoundary.get("id");
                    String kdProp = (String) rawBidangBoundary.get("kd_prop");
                    String kdDati2 = (String) rawBidangBoundary.get("kd_dati2");
                    String kdKecResp = (String) rawBidangBoundary.get("kd_kec");
                    String kdKelResp = (String) rawBidangBoundary.get("kd_kel");
                    String kdBlokResp = (String) rawBidangBoundary.get("kd_blok");
                    String noUrut = (String) rawBidangBoundary.get("no_urut");
                    String kdJnsOp = (String) rawBidangBoundary.get("kd_jns_op");
                    String nop = (String) rawBidangBoundary.get("nop");
                    
                    // Debug: Log no_urut extraction for first few records
                    if (successCount < 3) {
                        System.out.println("🔍 Extracted no_urut: '" + noUrut + "' from NOP: " + nop);
                    }
                    Boolean isActive = (Boolean) rawBidangBoundary.get("is_active");
                    String geomWkbHex = (String) rawBidangBoundary.get("geom");

                    // Create DTO
                    BidangBoundaryDTO dto = new BidangBoundaryDTO(
                        id, kdProp, kdDati2, kdKecResp, kdKelResp, kdBlokResp, 
                        noUrut, kdJnsOp, nop, geomWkbHex, isActive);

                    // Convert WKB hex to GeoJSON
                    if (geomWkbHex != null && !geomWkbHex.trim().isEmpty()) {
                        Map<String, Object> geoJson = WkbToGeoJsonConverter.convertWkbHexToGeoJson(geomWkbHex);
                        dto.setGeojson(geoJson);
                        successCount++;
                        System.out.println("✅ Converted bidang " + nop + " geometry successfully");
                    } else {
                        System.out.println("⚠️ Empty geometry for bidang " + nop);
                        failCount++;
                    }

                    convertedBidangBoundaries.add(dto);

                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ Failed to process bidang boundary: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("📦 Bidang boundary conversion complete: " + successCount + " success, " + failCount + " failed");
            return ResponseEntity.ok(convertedBidangBoundaries);

        } catch (Exception e) {
            System.err.println("❌ Error getting bidang boundaries: " + e.getMessage());
            e.printStackTrace();
            
            // Clear token on error
            cachedToken = null;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    /**
     * Get bidang detail (infonop) from BPRD API
     */
    @GetMapping("/infonop")
    public ResponseEntity<?> getBidangDetail(
            @RequestParam String id,
            @RequestParam String kd_prop, 
            @RequestParam String kd_dati2,
            @RequestParam String kd_kec,
            @RequestParam String kd_kel,
            @RequestParam String kd_blok,
            @RequestParam String no_urut,
            @RequestParam String kd_jns_op
    ) {
        try {
            System.out.println("🏠 Getting bidang detail from BPRD API...");
            System.out.println("Parameters: id=" + id + ", kd_prop=" + kd_prop + ", kd_dati2=" + kd_dati2 + 
                             ", kd_kec=" + kd_kec + ", kd_kel=" + kd_kel + ", kd_blok=" + kd_blok + 
                             ", no_urut=" + no_urut + ", kd_jns_op=" + kd_jns_op);

            // Step 1: Ensure we have a valid token (login if needed)
            if (cachedToken == null) {
                System.out.println("🔐 No cached token found, performing login...");
                
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", "user");
                credentials.put("password", "user");
                
                HttpHeaders loginHeaders = new HttpHeaders();
                loginHeaders.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(credentials, loginHeaders);
                
                ResponseEntity<String> loginResponse = restTemplate.exchange(
                    BPRD_BASE_URL + "/user/login",
                    HttpMethod.POST,
                    loginRequest,
                    String.class
                );
                
                if (loginResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> loginResult = objectMapper.readValue(loginResponse.getBody(), Map.class);
                    Map<String, Object> user = (Map<String, Object>) loginResult.get("user");
                    
                    if (user != null && user.get("token") != null) {
                        cachedToken = (String) user.get("token");
                        System.out.println("✅ Login successful for bidang detail request");
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Login failed - no token received"));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Login failed with status: " + loginResponse.getStatusCode()));
                }
            }

            // Build the infonop API URL
            String infonopUrl = BPRD_BASE_URL + "/map/infonop" +
                "?id=" + id +
                "&kd_prop=" + kd_prop +
                "&kd_dati2=" + kd_dati2 +
                "&kd_kec=" + kd_kec +
                "&kd_kel=" + kd_kel +
                "&kd_blok=" + kd_blok +
                "&no_urut=" + no_urut +
                "&kd_jns_op=" + kd_jns_op;

            System.out.println("📡 Calling BPRD infonop API: " + infonopUrl);

            // Set up headers with authentication
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + cachedToken);
            headers.set("Content-Type", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Make the API call
            ResponseEntity<Map> response = restTemplate.exchange(
                infonopUrl,
                HttpMethod.GET,
                entity,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("✅ Successfully got bidang detail from BPRD API");
                return ResponseEntity.ok(response.getBody());
            } else {
                System.err.println("❌ Failed to get bidang detail: " + response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "Failed to get bidang detail from BPRD API"));
            }

        } catch (Exception e) {
            System.err.println("❌ Error getting bidang detail: " + e.getMessage());
            e.printStackTrace();
            
            // Clear token on error
            cachedToken = null;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    /**
     * Get tematik data from BPRD API
     */
    @PostMapping("/tematik")
    public ResponseEntity<?> getTematikData(@RequestBody Map<String, Object> tematikRequest) {
        try {
            System.out.println("🎨 Getting tematik data from BPRD API...");
            System.out.println("Tematik Request: " + tematikRequest);

            // Step 1: Ensure we have a valid token (login if needed)
            if (cachedToken == null) {
                System.out.println("🔐 No cached token found, performing login...");
                
                Map<String, String> credentials = new HashMap<>();
                credentials.put("username", "user");
                credentials.put("password", "user");
                
                HttpHeaders loginHeaders = new HttpHeaders();
                loginHeaders.setContentType(MediaType.APPLICATION_JSON);
                
                HttpEntity<Map<String, String>> loginRequest = new HttpEntity<>(credentials, loginHeaders);
                
                ResponseEntity<String> loginResponse = restTemplate.exchange(
                    BPRD_BASE_URL + "/user/login",
                    HttpMethod.POST,
                    loginRequest,
                    String.class
                );
                
                if (loginResponse.getStatusCode() == HttpStatus.OK) {
                    Map<String, Object> loginResult = objectMapper.readValue(loginResponse.getBody(), Map.class);
                    Map<String, Object> user = (Map<String, Object>) loginResult.get("user");
                    
                    if (user != null && user.get("token") != null) {
                        cachedToken = (String) user.get("token");
                        System.out.println("✅ Login successful for tematik request");
                    } else {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body(Map.of("error", "Login failed - no token received"));
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Login failed with status: " + loginResponse.getStatusCode()));
                }
            }

            // Step 2: Call BPRD tematik API
            String tematikUrl = BPRD_BASE_URL + "/map/tematik";
            System.out.println("📡 Calling BPRD tematik API: " + tematikUrl);

            // Set up headers with authentication
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + cachedToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(tematikRequest, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                tematikUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Parse the tematik response
                @SuppressWarnings("unchecked")
                Map<String, Object> tematikResponse = objectMapper.readValue(response.getBody(), Map.class);
                
                System.out.println("✅ Successfully got tematik data from BPRD API");
                
                // Process and convert WKB geometries to GeoJSON
                Map<String, Object> processedResponse = processTematikResponse(tematikResponse);
                
                return ResponseEntity.ok(processedResponse);
            } else {
                System.err.println("❌ Failed to get tematik data: " + response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode())
                    .body(Map.of("error", "Failed to get tematik data from BPRD API"));
            }

        } catch (Exception e) {
            System.err.println("❌ Error getting tematik data: " + e.getMessage());
            e.printStackTrace();
            
            // Clear token on error
            cachedToken = null;
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Exception: " + e.getMessage()));
        }
    }

    /**
     * Process tematik response and convert WKB geometries to GeoJSON
     */
    private Map<String, Object> processTematikResponse(Map<String, Object> tematikResponse) {
        try {
            System.out.println("🔄 Processing tematik response with WKB conversion...");
            
            Map<String, Object> processedResponse = new HashMap<>(tematikResponse);
            
            // Get the layer data
            @SuppressWarnings("unchecked")
            Map<String, Object> layerMap = (Map<String, Object>) tematikResponse.get("layer");
            
            if (layerMap != null) {
                Map<String, Object> processedLayers = new HashMap<>();
                
                for (Map.Entry<String, Object> layerEntry : layerMap.entrySet()) {
                    String layerKey = layerEntry.getKey();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> layerData = (Map<String, Object>) layerEntry.getValue();
                    
                    // Copy layer metadata
                    Map<String, Object> processedLayer = new HashMap<>();
                    processedLayer.put("label", layerData.get("label"));
                    processedLayer.put("color", layerData.get("color"));
                    
                    // Process the data array
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> dataList = (List<Map<String, Object>>) layerData.get("data");
                    
                    if (dataList != null) {
                        List<Map<String, Object>> processedDataList = new ArrayList<>();
                        
                        for (Map<String, Object> bidangData : dataList) {
                            Map<String, Object> processedBidang = new HashMap<>(bidangData);
                            
                            // Convert WKB geometry to GeoJSON
                            String geomWkbHex = (String) bidangData.get("geom");
                            if (geomWkbHex != null && !geomWkbHex.trim().isEmpty()) {
                                try {
                                    Map<String, Object> geoJson = WkbToGeoJsonConverter.convertWkbHexToGeoJson(geomWkbHex);
                                    processedBidang.put("geojson", geoJson);
                                } catch (Exception e) {
                                    System.err.println("⚠️ Failed to convert geometry for bidang " + bidangData.get("nop"));
                                    processedBidang.put("geojson", null);
                                }
                            }
                            
                            processedDataList.add(processedBidang);
                        }
                        
                        processedLayer.put("data", processedDataList);
                    }
                    
                    processedLayers.put(layerKey, processedLayer);
                }
                
                processedResponse.put("layer", processedLayers);
            }
            
            System.out.println("✅ Tematik response processed successfully");
            return processedResponse;
            
        } catch (Exception e) {
            System.err.println("❌ Error processing tematik response: " + e.getMessage());
            e.printStackTrace();
            return tematikResponse; // Return original if processing fails
        }
    }
}