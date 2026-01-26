"""
Script untuk generate kd_kec dan kd_kel dari SISMIOP Oracle
dan patch ke seeder pbjt_assessments
"""

import oracledb
import psycopg2
import json
from typing import Dict, List

# Oracle SISMIOP Connection
ORACLE_CONFIG = {
    'user': 'system',
    'password': 'admin',
    'dsn': 'localhost:1521/FREEPDB1'
}

# PostgreSQL PBJT Connection
POSTGRES_CONFIG = {
    'host': 'localhost',
    'database': 'pbjt_assessment_db',
    'user': 'postgres',
    'password': 'root'
}

def fetch_kecamatan_mapping() -> Dict[str, str]:
    """Fetch kecamatan mapping dari Oracle SISMIOP"""
    print("🔍 Fetching kecamatan data from Oracle SISMIOP...")
    
    connection = oracledb.connect(**ORACLE_CONFIG)
    cursor = connection.cursor()
    
    # Query untuk get kecamatan Lumajang
    query = """
        SELECT KD_KECAMATAN, NM_KECAMATAN
        FROM REF_KECAMATAN
        WHERE KD_PROPINSI = '35' 
          AND KD_DATI2 = '08'
        ORDER BY KD_KECAMATAN
    """
    
    cursor.execute(query)
    rows = cursor.fetchall()
    
    # Create mapping: NM_KECAMATAN (uppercase) -> KD_KECAMATAN
    kec_mapping = {}
    for kd_kec, nm_kec in rows:
        kec_mapping[nm_kec.upper()] = kd_kec
        print(f"  ✓ {nm_kec} -> {kd_kec}")
    
    cursor.close()
    connection.close()
    
    return kec_mapping

def fetch_kelurahan_mapping() -> Dict[str, Dict]:
    """Fetch kelurahan mapping dari Oracle SISMIOP"""
    print("\n🔍 Fetching kelurahan data from Oracle SISMIOP...")
    
    connection = oracledb.connect(**ORACLE_CONFIG)
    cursor = connection.cursor()
    
    # Query untuk get kelurahan Lumajang
    query = """
        SELECT KD_KECAMATAN, KD_KELURAHAN, NM_KELURAHAN
        FROM REF_KELURAHAN
        WHERE KD_PROPINSI = '35' 
          AND KD_DATI2 = '08'
        ORDER BY KD_KECAMATAN, KD_KELURAHAN
    """
    
    cursor.execute(query)
    rows = cursor.fetchall()
    
    # Create mapping: NM_KELURAHAN (uppercase) -> {KD_KECAMATAN, KD_KELURAHAN}
    kel_mapping = {}
    for kd_kec, kd_kel, nm_kel in rows:
        key = nm_kel.upper()
        kel_mapping[key] = {
            'kd_kec': kd_kec,
            'kd_kel': kd_kel,
            'nm_kel': nm_kel
        }
        print(f"  ✓ {nm_kel} (Kec: {kd_kec}) -> Kel: {kd_kel}")
    
    cursor.close()
    connection.close()
    
    return kel_mapping

def update_pbjt_assessments_with_codes():
    """Update existing pbjt_assessments dengan kd_kec dan kd_kel"""
    print("\n📝 Updating pbjt_assessments with kd_kec and kd_kel...")
    
    # Fetch mapping from Oracle
    kec_mapping = fetch_kecamatan_mapping()
    kel_mapping = fetch_kelurahan_mapping()
    
    # Connect to PostgreSQL
    conn = psycopg2.connect(**POSTGRES_CONFIG)
    cursor = conn.cursor()
    
    # Get all assessments
    cursor.execute("""
        SELECT id, kecamatan, kelurahan, kd_kec
        FROM pbjt_assessments
        WHERE kecamatan IS NOT NULL
    """)
    
    assessments = cursor.fetchall()
    updated_count = 0
    not_found = []
    
    for assessment_id, kecamatan, kelurahan, existing_kd_kec in assessments:
        kecamatan_upper = kecamatan.upper() if kecamatan else None
        kelurahan_upper = kelurahan.upper() if kelurahan else None
        
        # Get kd_kec from mapping
        kd_kec = kec_mapping.get(kecamatan_upper)
        
        # Get kd_kel from mapping
        kel_data = kel_mapping.get(kelurahan_upper)
        kd_kel = kel_data['kd_kel'] if kel_data else None
        
        if kd_kec and kd_kel:
            # Update record
            cursor.execute("""
                UPDATE pbjt_assessments
                SET kd_kec = %s
                WHERE id = %s
            """, (kd_kec, assessment_id))
            
            updated_count += 1
            print(f"  ✓ Updated ID {assessment_id}: {kecamatan} ({kd_kec}), {kelurahan} ({kd_kel})")
        else:
            not_found.append({
                'id': assessment_id,
                'kecamatan': kecamatan,
                'kelurahan': kelurahan
            })
            print(f"  ⚠️ Not found mapping for ID {assessment_id}: {kecamatan}, {kelurahan}")
    
    conn.commit()
    cursor.close()
    conn.close()
    
    print(f"\n✅ Updated {updated_count} records")
    if not_found:
        print(f"⚠️ {len(not_found)} records not found in SISMIOP mapping:")
        for item in not_found:
            print(f"   - ID {item['id']}: {item['kecamatan']}, {item['kelurahan']}")

def generate_updated_seeder():
    """Generate SQL seeder baru dengan kd_kec dan kd_kel"""
    print("\n📄 Generating updated seeder SQL...")
    
    # Fetch mapping from Oracle
    kec_mapping = fetch_kecamatan_mapping()
    kel_mapping = fetch_kelurahan_mapping()
    
    # Save mapping to JSON for reference
    with open('kecamatan_kelurahan_mapping.json', 'w', encoding='utf-8') as f:
        mapping_data = {
            'kecamatan': {k: v for k, v in kec_mapping.items()},
            'kelurahan': {k: {
                'kd_kec': v['kd_kec'],
                'kd_kel': v['kd_kel']
            } for k, v in kel_mapping.items()}
        }
        json.dump(mapping_data, f, indent=2, ensure_ascii=False)
    
    print(f"✅ Mapping saved to kecamatan_kelurahan_mapping.json")
    print(f"   - {len(kec_mapping)} kecamatan")
    print(f"   - {len(kel_mapping)} kelurahan")

if __name__ == "__main__":
    print("=" * 60)
    print("PBJT Assessment - Patch Seeder with SISMIOP Codes")
    print("=" * 60)
    
    try:
        # Step 1: Generate mapping file
        generate_updated_seeder()
        
        # Step 2: Update existing records
        update_pbjt_assessments_with_codes()
        
        print("\n" + "=" * 60)
        print("✅ Process completed successfully!")
        print("=" * 60)
        
    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()
