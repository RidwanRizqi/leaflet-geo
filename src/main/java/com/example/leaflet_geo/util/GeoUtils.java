package com.example.leaflet_geo.util;

public class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    /**
     * Menghitung jarak antara dua koordinat geografis menggunakan Haversine Formula.
     *
     * @param lat1 Latitude titik 1
     * @param lon1 Longitude titik 1
     * @param lat2 Latitude titik 2
     * @param lon2 Longitude titik 2
     * @return Jarak dalam satuan meter
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Memeriksa apakah suatu koordinat berada di dalam radius toleransi dari koordinat kantor.
     */
    public static boolean isWithinRadius(double userLat, double userLon,
                                          double officeLat, double officeLon,
                                          double radiusMeters) {
        return calculateDistance(userLat, userLon, officeLat, officeLon) <= radiusMeters;
    }
}
