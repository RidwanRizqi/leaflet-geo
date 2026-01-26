"""
Script to generate accurate coordinates from BPRD boundary GeoJSON
Calculates centroids for each kelurahan polygon
"""
import requests
import json
from statistics import mean
import random

# Kecamatan code mapping (from BPRD API)
KECAMATAN_CODES = {
    'TEMPURSARI': '010',
    'PRONOJIWO': '020', 
    'CANDIPURO': '030',
    'PASIRIAN': '040',
    'TEMPEH': '050',
    'LUMAJANG': '060',
    'YOSOWILANGUN': '070',
    'KUNIR': '080',
    'TEKUNG': '090',
    'SUKODONO': '150',
    'SENDURO': '120',
    'GUCIALIT': '130',
    'PASRUJAMBE': '110',
    'KLAKAH': '190',
    'RANDUAGUNG': '180',
    'KEDUNGJAJANG': '160',
    'SUMBERSUKO': '210',
    'ROWOKANGKUNG': '140',
    'JATIROTO': '170',
    'PADANG': '200',
}

# All kelurahan from seeder, grouped by kecamatan
kelurahan_kecamatan = {
    # From seeder data
    'KEDUNGJAJANG': 'KEDUNGJAJANG',
    'TOMPOKERSAN': 'LUMAJANG',
    'JATISARI': 'TEMPEH',
    'WONOREJO': 'KEDUNGJAJANG',
    'LABRUK LOR': 'LUMAJANG',
    'JOGOTRUNAN': 'LUMAJANG',
    'DITOTRUNAN': 'LUMAJANG',
    'JOGOYUDAN': 'LUMAJANG',
    'SUMBERSUKO': 'SUMBERSUKO',
    'KEBONSARI': 'YOSOWILANGUN',
    'KLAKAH': 'KLAKAH',
    'CITRODIWANGSAN': 'LUMAJANG',
    'PANDANWANGI': 'TEMPEH',
    'KALIBENDO': 'PASIRIAN',
    'ROGOTRUNAN': 'LUMAJANG',
    'BONDOYUDO': 'SUKODONO',
    'KEPUHARJO': 'LUMAJANG',
    'SUMBEREJO': 'SUKODONO',
    'GUCIALIT': 'GUCIALIT',
    'KUTORENON': 'SUKODONO',
    'KANDANGTEPUS': 'SENDURO',
    'TEMPEH LOR': 'TEMPEH',
    'YOSOWILANGUN LOR': 'YOSOWILANGUN',
    'PENANGGAL': 'CANDIPURO',
    'SENDURO': 'SENDURO',
    'PASIRIAN': 'PASIRIAN',
    'SUMBER WULUH': 'CANDIPURO',
    'TEMPURSARI': 'TEMPURSARI',
    'DARUNGAN': 'YOSOWILANGUN',
    'PAGOWAN': 'PASRUJAMBE',
    'KLANTING': 'SUKODONO',
    'WONOGRIYO': 'TEKUNG',
    'BADES': 'PASIRIAN',
    'TEMPEH TENGAH': 'TEMPEH',
    'PRONOJIWO': 'PRONOJIWO',
    'SELOKBESUKI': 'SUKODONO',
    'KARANGSARI': 'SUKODONO',
    'DOROGOWOK': 'KUNIR',
    'WONOKERTO': 'TEKUNG',
    'CANDIPURO': 'CANDIPURO',
    'RANDUAGUNG': 'RANDUAGUNG',
    'KRAI': 'YOSOWILANGUN',
    'PANDAN ARUM': 'TEMPEH',
    'DAWUHAN LOR': 'SUKODONO',
    'KARANGLO': 'KUNIR',
    'KUNIR LOR': 'KUNIR',
    'KARANGREJO': 'YOSOWILANGUN',
    'DADAPAN': 'GUCIALIT',
    'YOSOWILANGUN KIDUL': 'YOSOWILANGUN',
    'KRATON': 'YOSOWILANGUN',
    'TAMANAYU': 'PRONOJIWO',
    'NGUTER': 'PASIRIAN',
    'TUKUM': 'TEKUNG',
}

def calculate_polygon_centroid(coordinates):
    """Calculate centroid of a polygon using average of all points"""
    try:
        if not coordinates or len(coordinates) == 0:
            return None
            
        # Handle MultiPolygon - take first polygon
        if isinstance(coordinates[0][0][0], list):
            coordinates = coordinates[0]
        
        # Flatten to get all points
        all_lons = []
        all_lats = []
        
        for ring in coordinates:
            for point in ring:
                if len(point) >= 2:
                    all_lons.append(point[0])
                    all_lats.append(point[1])
        
        if not all_lons or not all_lats:
            return None
            
        centroid_lon = mean(all_lons)
        centroid_lat = mean(all_lats)
        
        return (centroid_lat, centroid_lon)
    except Exception as e:
        print(f"Error calculating centroid: {e}")
        return None

def add_random_offset(lat, lon, offset_meters=500):
    """Add random offset to coordinates (in meters) - larger spread"""
    # 1 degree lat ~ 111km, 1 degree lon ~ 111km * cos(lat)
    import math
    # Use larger offset range (300-700m) for better spread
    actual_offset = random.uniform(300, 700)
    angle = random.uniform(0, 2 * math.pi)  # Random direction
    offset_lat = (actual_offset * math.cos(angle) / 111000)
    offset_lon = (actual_offset * math.sin(angle) / (111000 * math.cos(math.radians(lat))))
    return (lat + offset_lat, lon + offset_lon)

def fetch_all_kelurahan():
    """Fetch all kelurahan boundaries from BPRD API"""
    results = {}
    
    for kec_name, kd_kec in KECAMATAN_CODES.items():
        print(f"\nFetching kelurahan for {kec_name} (kd_kec: {kd_kec})...")
        try:
            url = f"http://localhost:8080/api/bprd/kelurahan?kd_kec={kd_kec}"
            response = requests.get(url, timeout=30)
            if response.status_code == 200:
                data = response.json()
                # Handle wrapped response {value: [...], Count: N}
                items = data.get('value', data) if isinstance(data, dict) else data
                for item in items:
                    # Use 'nama' field instead of 'nm_kel'
                    kel_name = item.get('nama', '').upper().strip()
                    geojson = item.get('geojson')
                    if kel_name and geojson and 'coordinates' in geojson:
                        centroid = calculate_polygon_centroid(geojson['coordinates'])
                        if centroid:
                            results[kel_name] = {
                                'latitude': centroid[0],
                                'longitude': centroid[1],
                                'kecamatan': kec_name
                            }
                            print(f"  ✓ {kel_name}")
        except Exception as e:
            print(f"  Error: {e}")
    
    return results

def main():
    print("=" * 80)
    print("Generating Accurate Coordinates from BPRD Boundaries")
    print("=" * 80)
    
    # Fetch all kelurahan centroids from API
    all_centroids = fetch_all_kelurahan()
    print(f"\nTotal kelurahan centroids fetched: {len(all_centroids)}")
    
    # Match with our seeder kelurahan
    matched = {}
    not_matched = []
    
    for kel_name in kelurahan_kecamatan.keys():
        # Try exact match
        if kel_name in all_centroids:
            matched[kel_name] = all_centroids[kel_name]
        else:
            # Try fuzzy match
            found = False
            for api_name in all_centroids.keys():
                if kel_name.replace(' ', '') == api_name.replace(' ', ''):
                    matched[kel_name] = all_centroids[api_name]
                    found = True
                    break
            if not found:
                not_matched.append(kel_name)
    
    print(f"\nMatched: {len(matched)}/{len(kelurahan_kecamatan)}")
    if not_matched:
        print(f"Not matched: {not_matched}")
    
    # Generate SQL UPDATE statements
    print("\n" + "=" * 80)
    print("-- SQL UPDATE Statements")
    print("-- Copy and run this in PostgreSQL")
    print("=" * 80)
    print()
    
    # Count businesses per kelurahan to add offset
    kel_counts = {}
    
    for kel_name, coords in matched.items():
        if kel_name not in kel_counts:
            kel_counts[kel_name] = 0
        kel_counts[kel_name] += 1
        
        # Add random offset so markers don't overlap
        offset_lat, offset_lon = add_random_offset(coords['latitude'], coords['longitude'])
        
        print(f"-- {kel_name} ({coords['kecamatan']})")
        print(f"UPDATE pbjt_assessments SET latitude = {offset_lat:.8f}, longitude = {offset_lon:.8f}")
        print(f"WHERE UPPER(kelurahan) = '{kel_name}' AND business_id LIKE 'SIM-%' AND latitude = 0;")
        print()
    
    # Generate per-record updates with different offsets
    print("\n" + "=" * 80)
    print("-- Per-record UPDATE with unique coordinates")
    print("=" * 80)
    print()
    
    # Read seeder to get business_ids for each kelurahan
    business_coords = []
    for kel_name, coords in matched.items():
        # Generate unique coordinate for each business in this kelurahan
        offset_lat, offset_lon = add_random_offset(coords['latitude'], coords['longitude'])
        business_coords.append({
            'kelurahan': kel_name,
            'lat': offset_lat,
            'lon': offset_lon
        })
    
    # Print summary
    print("\n" + "=" * 80)
    print(f"Total kelurahan matched: {len(matched)}/{len(kelurahan_kecamatan)}")
    print("=" * 80)
    
    # Save to JSON for reference
    with open('kelurahan_centroids.json', 'w') as f:
        json.dump({k: {'lat': v['latitude'], 'lon': v['longitude'], 'kec': v['kecamatan']} 
                   for k, v in matched.items()}, f, indent=2)
    print("\nSaved to: kelurahan_centroids.json")

if __name__ == '__main__':
    main()
