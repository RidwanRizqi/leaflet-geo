"""
Generate SQL UPDATE statements for pbjt_assessments with coordinates from kelurahan centroids
Each business gets a unique coordinate with random offset
"""
import json
import random
import math

# Load kelurahan centroids from previous script
with open('kelurahan_centroids.json', 'r') as f:
    centroids = json.load(f)

# Business data from seeder (business_id -> kelurahan)
businesses = [
    ('SIM-0001', 'KEDUNGJAJANG'),
    ('SIM-0002', 'TOMPOKERSAN'),
    ('SIM-0003', 'JATISARI'),
    ('SIM-0004', 'WONOREJO'),
    ('SIM-0005', 'LABRUK LOR'),
    ('SIM-0006', 'JOGOTRUNAN'),
    ('SIM-0007', 'DITOTRUNAN'),
    ('SIM-0008', 'JOGOYUDAN'),
    ('SIM-0009', 'SUMBERSUKO'),
    ('SIM-0010', 'TOMPOKERSAN'),
    ('SIM-0011', 'KEBONSARI'),
    ('SIM-0012', 'TOMPOKERSAN'),
    ('SIM-0013', 'KLAKAH'),
    ('SIM-0014', 'JOGOTRUNAN'),
    ('SIM-0015', 'JOGOTRUNAN'),
    ('SIM-0016', 'CITRODIWANGSAN'),
    ('SIM-0017', 'PANDANWANGI'),
    ('SIM-0018', 'KALIBENDO'),
    ('SIM-0019', 'ROGOTRUNAN'),
    ('SIM-0020', 'BONDOYUDO'),
    ('SIM-0021', 'WONOREJO'),
    ('SIM-0022', 'KEPUHARJO'),
    ('SIM-0023', 'TOMPOKERSAN'),
    ('SIM-0024', 'KEPUHARJO'),
    ('SIM-0025', 'SUMBEREJO'),
    ('SIM-0026', 'ROGOTRUNAN'),
    ('SIM-0027', 'PANDANWANGI'),
    ('SIM-0028', 'KEPUHARJO'),
    ('SIM-0029', 'GUCIALIT'),
    ('SIM-0030', 'CITRODIWANGSAN'),
    ('SIM-0031', 'BONDOYUDO'),
    ('SIM-0032', 'TOMPOKERSAN'),
    ('SIM-0033', 'TOMPOKERSAN'),
    ('SIM-0034', 'KUTORENON'),
    ('SIM-0035', 'LABRUK LOR'),
    ('SIM-0036', 'TEMPEH LOR'),
    ('SIM-0037', 'KEPUHARJO'),
    ('SIM-0038', 'YOSOWILANGUN LOR'),
    ('SIM-0039', 'KEBONSARI'),
    ('SIM-0040', 'DITOTRUNAN'),
    ('SIM-0041', 'KUTORENON'),
    ('SIM-0042', 'KEPUHARJO'),
    ('SIM-0043', 'KUTORENON'),
    ('SIM-0044', 'KANDANGTEPUS'),
    ('SIM-0045', 'KEPUHARJO'),
    ('SIM-0046', 'JOGOTRUNAN'),
    ('SIM-0047', 'JOGOYUDAN'),
    ('SIM-0048', 'TOMPOKERSAN'),
    ('SIM-0049', 'TOMPOKERSAN'),
    ('SIM-0050', 'JOGOYUDAN'),
    ('SIM-0051', 'PENANGGAL'),
    ('SIM-0052', 'SENDURO'),
    ('SIM-0053', 'PASIRIAN'),
    ('SIM-0054', 'DITOTRUNAN'),
    ('SIM-0055', 'PASIRIAN'),
    ('SIM-0056', 'TOMPOKERSAN'),
    ('SIM-0057', 'TUKUM'),
    ('SIM-0058', 'SENDURO'),
    ('SIM-0059', 'JOGOTRUNAN'),
    ('SIM-0060', 'SUMBER WULUH'),
    ('SIM-0061', 'JATISARI'),
    ('SIM-0062', 'CITRODIWANGSAN'),
    ('SIM-0063', 'TEMPURSARI'),
    ('SIM-0064', 'JOGOYUDAN'),
    ('SIM-0065', 'YOSOWILANGUN KIDUL'),
    ('SIM-0066', 'KRATON'),
    ('SIM-0067', 'TAMANAYU'),
    ('SIM-0068', 'DITOTRUNAN'),
    ('SIM-0069', 'CITRODIWANGSAN'),
    ('SIM-0070', 'JOGOTRUNAN'),
    ('SIM-0071', 'DARUNGAN'),
    ('SIM-0072', 'PAGOWAN'),
    ('SIM-0073', 'KLANTING'),
    ('SIM-0074', 'WONOGRIYO'),
    ('SIM-0075', 'BADES'),
    ('SIM-0076', 'TEMPEH TENGAH'),
    ('SIM-0077', 'CITRODIWANGSAN'),
    ('SIM-0078', 'PRONOJIWO'),
    ('SIM-0079', 'SELOKBESUKI'),
    ('SIM-0080', 'CITRODIWANGSAN'),
    ('SIM-0081', 'NGUTER'),
    ('SIM-0082', 'SELOKBESUKI'),
    ('SIM-0083', 'KARANGSARI'),
    ('SIM-0084', 'DOROGOWOK'),
    ('SIM-0085', 'WONOKERTO'),
    ('SIM-0086', 'CITRODIWANGSAN'),
    ('SIM-0087', 'CANDIPURO'),
    ('SIM-0088', 'TOMPOKERSAN'),
    ('SIM-0089', 'SUMBERSUKO'),
    ('SIM-0090', 'RANDUAGUNG'),
    ('SIM-0091', 'KRAI'),
    ('SIM-0092', 'ROGOTRUNAN'),
    ('SIM-0093', 'PANDAN ARUM'),
    ('SIM-0094', 'JOGOYUDAN'),
    ('SIM-0095', 'DAWUHAN LOR'),
    ('SIM-0096', 'KARANGLO'),
    ('SIM-0097', 'KUNIR LOR'),
    ('SIM-0098', 'KARANGREJO'),
    ('SIM-0099', 'DADAPAN'),
    ('SIM-0100', 'RANDUAGUNG'),
]

# Fallback coordinates for unmatched kelurahan (Lumajang city center)
FALLBACK_COORDS = {
    'KEDUNGJAJANG': {'lat': -8.1425, 'lon': 113.1950},  # Near Lumajang
    'WONOREJO': {'lat': -8.1380, 'lon': 113.1880},      # Near Kedungjajang
    'SUMBERSUKO': {'lat': -8.0950, 'lon': 113.1720},    # Near Sukodono
    'PAGOWAN': {'lat': -8.0450, 'lon': 113.2500},       # Near Pasrujambe
}

def add_random_offset(lat, lon, offset_meters=200):
    """Add random offset to coordinates (in meters)"""
    offset_lat = (random.uniform(-offset_meters, offset_meters) / 111000)
    offset_lon = (random.uniform(-offset_meters, offset_meters) / (111000 * math.cos(math.radians(lat))))
    return (lat + offset_lat, lon + offset_lon)

def get_coords(kelurahan):
    """Get coordinates for a kelurahan with random offset"""
    if kelurahan in centroids:
        lat = centroids[kelurahan]['lat']
        lon = centroids[kelurahan]['lon']
    elif kelurahan in FALLBACK_COORDS:
        lat = FALLBACK_COORDS[kelurahan]['lat']
        lon = FALLBACK_COORDS[kelurahan]['lon']
    else:
        # Default to Lumajang center
        lat = -8.1350
        lon = 113.2200
    
    return add_random_offset(lat, lon)

# Generate SQL
print("-- PBJT Assessment Coordinate Updates")
print("-- Generated from kelurahan polygon centroids with random offset")
print("-- Each business gets unique coordinates within its kelurahan")
print()
print("BEGIN;")
print()

for business_id, kelurahan in businesses:
    lat, lon = get_coords(kelurahan)
    print(f"UPDATE pbjt_assessments SET latitude = {lat:.8f}, longitude = {lon:.8f} WHERE business_id = '{business_id}';")

print()
print("COMMIT;")
print()
print(f"-- Total: {len(businesses)} records updated")
