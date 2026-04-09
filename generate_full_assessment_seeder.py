import mysql.connector
import datetime
import os

# Konfigurasi Koneksi Database SIMATDA (Source)
# Kredensial diambil dari GenerateRealisasiSeeder.java
DB_CONFIG = {
    'host': '192.178.10.112',
    'port': 3306,
    'user': 'polinema',
    'password': 'P0l1n3m4@bprd',
    'database': 'simpatda_lumajang'
}

OUTPUT_FILE = 'pbjt_assessments_full_seeder.sql'

def escape_sql_string(val):
    if val is None:
        return ''
    # Escape single quotes for SQL
    return str(val).replace("'", "''").strip()

def generate_seeder():
    print(f"Connecting to SIMATDA ({DB_CONFIG['host']})...")
    
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)
        
        # Ambil data Objek Pajak (Jenis Objek = 2 -> Restoran)
        query = """
            SELECT 
                t_npwpdwp,
                t_namawp,
                t_idobjek,
                t_nop,
                t_namaobjek,
                t_alamatlengkapobjek,
                t_objektutup
            FROM view_wpobjek
            WHERE t_jenisobjek = 2 
              AND t_nop IS NOT NULL 
              AND t_nop != ''
              AND LENGTH(TRIM(t_nop)) > 5
            ORDER BY t_tgldaftarobjek DESC
        """
        
        print("Executing query...")
        cursor.execute(query)
        rows = cursor.fetchall()
        print(f"Found {len(rows)} records.")
        
        with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
            f.write("-- ================================================================\n")
            f.write(f"-- PBJT ASSESSMENTS SEEDER (Source: SIMATDA, Jenis Objek 2)\n")
            f.write(f"-- Generated on: {datetime.datetime.now()}\n")
            f.write("-- ================================================================\n\n")
            
            for row in rows:
                # FIXED: business_id sekarang menggunakan NOP agar sama persis
                nop = escape_sql_string(row['t_nop'])
                business_id = nop 
                
                business_name = escape_sql_string(row['t_namaobjek'])
                tax_object_id = str(row['t_idobjek'])
                npwpd = escape_sql_string(row['t_npwpdwp']) # Ambil NPWPD
                
                # Gunakan alamat lengkap, jika kosong gunakan strip
                raw_address = row['t_alamatlengkapobjek'] if row['t_alamatlengkapobjek'] else '-'
                address = escape_sql_string(raw_address)
                
                # Status bisnis berdasarkan t_objektutup (Asumsi: 1=Tutup, 0=Buka)
                # Sesuaikan logic ini jika t_objektutup memiliki makna lain
                # Biasanya field 'tutup' kalau true/1 berarti INACTIVE
                status = 'INACTIVE' if row['t_objektutup'] else 'ACTIVE'
                
                # Gunakan capacity 1 agar lolos constraint valid_capacity
                
                sql = (
                    "INSERT INTO pbjt_assessments ("
                    "business_id, business_name, assessment_date, building_area, seating_capacity, "
                    "business_type, tax_rate, inflation_rate, confidence_score, confidence_level, "
                    "address, tax_object_id, tax_object_number, npwpd, status, created_at, updated_at"
                    ") VALUES ("
                    f"'{business_id}', "
                    f"'{business_name}', "
                    "CURRENT_DATE, "
                    "1, 1, "            # FIXED: Area=1, Capacity=1 (Constraint Fix)
                    "'RESTAURANT', "
                    "0.10, 0.03, "
                    "80, 'MEDIUM', "
                    f"'{address}', "
                    f"'{tax_object_id}', "
                    f"'{nop}', "
                    f"'{npwpd}', "
                    f"'{status}', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP"
                    ") ON CONFLICT (business_id) DO NOTHING;\n"
                )
                
                f.write(sql)
                
        print(f"Successfully generated {OUTPUT_FILE}")
        
    except mysql.connector.Error as err:
        print(f"Error: {err}")
    finally:
        if 'conn' in locals() and conn.is_connected():
            cursor.close()
            conn.close()
            print("Connection closed.")

if __name__ == "__main__":
    generate_seeder()
