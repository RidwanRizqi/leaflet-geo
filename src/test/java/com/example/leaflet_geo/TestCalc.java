package com.example.leaflet_geo;

import com.example.leaflet_geo.dto.AssessmentRequestDTO;
import com.example.leaflet_geo.service.PbjtCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.test.context.SpringBootTest;

public class TestCalc {
    public static void main(String[] args) throws Exception {
        String json = """
        {"businessId":"02.00247.06.005","businessName":"Rumah Makan","businessType":"RESTAURANT","seatingCapacity":10,"buildingArea":100,"operatingHoursStart":"04:35","operatingHoursEnd":"11:35","assessmentDate":"2026-02-08","openingDaysPerMonth":25,"menuItems":[{"name":"makan","price":10000,"category":"FOOD"},{"name":"minum","price":5000,"category":"BEVERAGE"}],"address":"Sukosari, Kel. SUKOSARI, Kec. KUNIR, Kab. LUMAJANG","kelurahan":"SUKOSARI","kecamatan":"K U N I R","kabupaten":"Lumajang","latitude":-8.22469458,"longitude":113.24131167,"roadType":"ARTERI","nearSchool":true,"nearOffice":false,"nearMarket":false,"surveyorId":"1","photoUrls":["/uploads/pbjt-images/ae711028-8e59-484f-9137-8bb168e61807.jpeg"],"observations":[]}
        """;
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        AssessmentRequestDTO req = mapper.readValue(json, AssessmentRequestDTO.class);
        System.out.println("MenuItems Size: " + (req.getMenuItems() != null ? req.getMenuItems().size() : "NULL"));
        
        PbjtCalculationService calc = new PbjtCalculationService();
        var res = calc.calculate(req);
        System.out.println("MenuBasedRevenue: " + res.getMonthlyRevenueMenuBased());
    }
}

