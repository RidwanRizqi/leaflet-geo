package com.example.leaflet_geo.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.io.WKBWriter;

import java.util.List;
import java.util.Map;

/**
 * Utility class for converting GeoJSON to WKB (Well-Known Binary) hex strings
 */
public class GeoJsonToWkbConverter {

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private static final WKBWriter wkbWriter = new WKBWriter(2, true); // 2D with SRID

    /**
     * Convert GeoJSON geometry object to WKB hex string
     * 
     * @param geoJson GeoJSON geometry as Map (from JSON parsing)
     * @return WKB hex string
     */
    public static String convertGeoJsonToWkbHex(Map<String, Object> geoJson) {
        try {
            if (geoJson == null) {
                return null;
            }

            String type = (String) geoJson.get("type");
            Object coordinates = geoJson.get("coordinates");

            if (type == null || coordinates == null) {
                System.err.println("❌ GeoJSON missing type or coordinates");
                return null;
            }

            Geometry geometry = parseGeoJsonGeometry(type, coordinates);
            if (geometry == null) {
                return null;
            }

            byte[] wkbBytes = wkbWriter.write(geometry);
            return bytesToHex(wkbBytes);

        } catch (Exception e) {
            System.err.println("❌ Error converting GeoJSON to WKB: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert GeoJSON string to WKB hex string
     */
    public static String convertGeoJsonStringToWkbHex(String geoJsonString) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> geoJson = mapper.readValue(geoJsonString, Map.class);
            return convertGeoJsonToWkbHex(geoJson);
        } catch (Exception e) {
            System.err.println("❌ Error parsing GeoJSON string: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Geometry parseGeoJsonGeometry(String type, Object coordinates) {
        switch (type) {
            case "Point":
                return parsePoint((List<Number>) coordinates);

            case "LineString":
                return parseLineString((List<List<Number>>) coordinates);

            case "Polygon":
                return parsePolygon((List<List<List<Number>>>) coordinates);

            case "MultiPoint":
                return parseMultiPoint((List<List<Number>>) coordinates);

            case "MultiLineString":
                return parseMultiLineString((List<List<List<Number>>>) coordinates);

            case "MultiPolygon":
                return parseMultiPolygon((List<List<List<List<Number>>>>) coordinates);

            default:
                System.err.println("⚠️ Unsupported geometry type: " + type);
                return null;
        }
    }

    private static Geometry parsePoint(List<Number> coords) {
        return geometryFactory.createPoint(new Coordinate(
                coords.get(0).doubleValue(),
                coords.get(1).doubleValue()));
    }

    private static Geometry parseLineString(List<List<Number>> coords) {
        Coordinate[] coordinates = coords.stream()
                .map(c -> new Coordinate(c.get(0).doubleValue(), c.get(1).doubleValue()))
                .toArray(Coordinate[]::new);
        return geometryFactory.createLineString(coordinates);
    }

    private static Geometry parsePolygon(List<List<List<Number>>> coords) {
        // Exterior ring
        List<List<Number>> exteriorCoords = coords.get(0);
        Coordinate[] shellCoords = exteriorCoords.stream()
                .map(c -> new Coordinate(c.get(0).doubleValue(), c.get(1).doubleValue()))
                .toArray(Coordinate[]::new);

        org.locationtech.jts.geom.LinearRing shell = geometryFactory.createLinearRing(shellCoords);

        // Interior rings (holes)
        org.locationtech.jts.geom.LinearRing[] holes = new org.locationtech.jts.geom.LinearRing[coords.size() - 1];
        for (int i = 1; i < coords.size(); i++) {
            Coordinate[] holeCoords = coords.get(i).stream()
                    .map(c -> new Coordinate(c.get(0).doubleValue(), c.get(1).doubleValue()))
                    .toArray(Coordinate[]::new);
            holes[i - 1] = geometryFactory.createLinearRing(holeCoords);
        }

        return geometryFactory.createPolygon(shell, holes);
    }

    private static Geometry parseMultiPoint(List<List<Number>> coords) {
        org.locationtech.jts.geom.Point[] points = coords.stream()
                .map(c -> geometryFactory.createPoint(new Coordinate(c.get(0).doubleValue(), c.get(1).doubleValue())))
                .toArray(org.locationtech.jts.geom.Point[]::new);
        return geometryFactory.createMultiPoint(points);
    }

    private static Geometry parseMultiLineString(List<List<List<Number>>> coords) {
        org.locationtech.jts.geom.LineString[] lineStrings = coords.stream()
                .map(lineCoords -> {
                    Coordinate[] coordinates = lineCoords.stream()
                            .map(c -> new Coordinate(c.get(0).doubleValue(), c.get(1).doubleValue()))
                            .toArray(Coordinate[]::new);
                    return geometryFactory.createLineString(coordinates);
                })
                .toArray(org.locationtech.jts.geom.LineString[]::new);
        return geometryFactory.createMultiLineString(lineStrings);
    }

    private static Geometry parseMultiPolygon(List<List<List<List<Number>>>> coords) {
        org.locationtech.jts.geom.Polygon[] polygons = coords.stream()
                .map(polyCoords -> (org.locationtech.jts.geom.Polygon) parsePolygon(polyCoords))
                .toArray(org.locationtech.jts.geom.Polygon[]::new);
        return geometryFactory.createMultiPolygon(polygons);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
