package com.example.leaflet_geo.util;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.io.WKBReader;

import java.util.*;

/**
 * Utility class for converting WKB (Well-Known Binary) hex strings to GeoJSON
 */
public class WkbToGeoJsonConverter {

    private static final WKBReader wkbReader = new WKBReader();

    /**
     * Convert WKB hex string to GeoJSON geometry object
     * 
     * @param wkbHex WKB hex string (e.g., "0106000020E6100000...")
     * @return GeoJSON geometry as Map (can be serialized to JSON)
     */
    public static Map<String, Object> convertWkbHexToGeoJson(String wkbHex) {
        try {
            if (wkbHex == null || wkbHex.trim().isEmpty()) {
                System.err.println("❌ WKB hex string is null or empty");
                return null;
            }

            // Convert hex string to byte array
            byte[] wkbBytes = hexStringToByteArray(wkbHex);
            
            // Parse WKB to JTS Geometry
            Geometry geometry = wkbReader.read(wkbBytes);
            
            if (geometry == null) {
                System.err.println("❌ Failed to parse WKB to geometry");
                return null;
            }

            // Convert JTS Geometry to GeoJSON manually
            Map<String, Object> geoJsonGeometry = jtsGeometryToGeoJson(geometry);
            
            return geoJsonGeometry;
            
        } catch (Exception e) {
            System.err.println("❌ Error converting WKB to GeoJSON: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert JTS Geometry to GeoJSON geometry object
     */
    private static Map<String, Object> jtsGeometryToGeoJson(Geometry geometry) {
        Map<String, Object> geoJson = new HashMap<>();
        
        String geometryType = geometry.getGeometryType();
        
        switch (geometryType) {
            case "Point":
                geoJson.put("type", "Point");
                geoJson.put("coordinates", coordinateToArray(geometry.getCoordinate()));
                break;
                
            case "LineString":
                geoJson.put("type", "LineString");
                geoJson.put("coordinates", coordinatesToArray(geometry.getCoordinates()));
                break;
                
            case "Polygon":
                geoJson.put("type", "Polygon");
                geoJson.put("coordinates", polygonToArray(geometry));
                break;
                
            case "MultiPoint":
                geoJson.put("type", "MultiPoint");
                List<double[]> multiPointCoords = new ArrayList<>();
                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                    multiPointCoords.add(coordinateToArray(geometry.getGeometryN(i).getCoordinate()));
                }
                geoJson.put("coordinates", multiPointCoords);
                break;
                
            case "MultiLineString":
                geoJson.put("type", "MultiLineString");
                List<List<double[]>> multiLineCoords = new ArrayList<>();
                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                    multiLineCoords.add(coordinatesToArray(geometry.getGeometryN(i).getCoordinates()));
                }
                geoJson.put("coordinates", multiLineCoords);
                break;
                
            case "MultiPolygon":
                geoJson.put("type", "MultiPolygon");
                List<List<List<double[]>>> multiPolygonCoords = new ArrayList<>();
                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                    multiPolygonCoords.add(polygonToArray(geometry.getGeometryN(i)));
                }
                geoJson.put("coordinates", multiPolygonCoords);
                break;
                
            case "GeometryCollection":
                geoJson.put("type", "GeometryCollection");
                List<Map<String, Object>> geometries = new ArrayList<>();
                for (int i = 0; i < geometry.getNumGeometries(); i++) {
                    geometries.add(jtsGeometryToGeoJson(geometry.getGeometryN(i)));
                }
                geoJson.put("geometries", geometries);
                break;
                
            default:
                System.err.println("⚠️ Unsupported geometry type: " + geometryType);
                break;
        }
        
        return geoJson;
    }

    /**
     * Convert single coordinate to array [x, y]
     */
    private static double[] coordinateToArray(Coordinate coord) {
        return new double[] { coord.x, coord.y };
    }

    /**
     * Convert coordinates to array of [x, y] arrays
     */
    private static List<double[]> coordinatesToArray(Coordinate[] coords) {
        List<double[]> result = new ArrayList<>();
        for (Coordinate coord : coords) {
            result.add(coordinateToArray(coord));
        }
        return result;
    }

    /**
     * Convert polygon geometry to array structure
     */
    private static List<List<double[]>> polygonToArray(Geometry polygon) {
        List<List<double[]>> result = new ArrayList<>();
        
        // Exterior ring
        result.add(coordinatesToArray(polygon.getCoordinates()));
        
        // Interior rings (holes) - for Polygon with holes
        if (polygon.getNumGeometries() > 1) {
            for (int i = 1; i < polygon.getNumGeometries(); i++) {
                result.add(coordinatesToArray(polygon.getGeometryN(i).getCoordinates()));
            }
        }
        
        return result;
    }

    /**
     * Convert hex string to byte array
     */
    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
