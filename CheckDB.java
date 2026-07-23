import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/pbjt_assessment_db", "postgres", "1234");
            Statement stmt = conn.createStatement();
            
            String sqlHotel = "CREATE TABLE IF NOT EXISTS hotel_accommodations (" +
                "id SERIAL PRIMARY KEY," +
                "simatda_id INTEGER," +
                "simatda_wp_id INTEGER," +
                "accommodation_type VARCHAR(50)," +
                "property_name VARCHAR(255)," +
                "owner_name VARCHAR(255)," +
                "owner_phone VARCHAR(50)," +
                "address TEXT," +
                "kelurahan VARCHAR(100)," +
                "kecamatan VARCHAR(100)," +
                "kabupaten VARCHAR(100)," +
                "latitude DECIMAL(10,8)," +
                "longitude DECIMAL(11,8)," +
                "total_rooms INTEGER," +
                "building_area DECIMAL(10,2)," +
                "land_area DECIMAL(10,2)," +
                "has_business_permit BOOLEAN," +
                "has_tax_registration BOOLEAN," +
                "formalization_status VARCHAR(50)," +
                "estimated_annual_revenue NUMERIC(15,2)," +
                "projected_annual_tax NUMERIC(15,2)," +
                "tax_rate DECIMAL(5,2) DEFAULT 0.10," +
                "willing_to_formalize BOOLEAN," +
                "status VARCHAR(50) DEFAULT 'ACTIVE'," +
                "is_closed BOOLEAN DEFAULT false," +
                "npwpd VARCHAR(50)," +
                "tax_object_id VARCHAR(50)," +
                "object_number VARCHAR(50)," +
                "photo_urls text[]," +
                "supporting_doc_url TEXT," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "synced_at TIMESTAMP" +
            ");";
            
            String sqlRealisasi = "CREATE TABLE IF NOT EXISTS hotel_realisasi (" +
                "id SERIAL PRIMARY KEY," +
                "hotel_id INTEGER REFERENCES hotel_accommodations(id)," +
                "simatda_objek_id INTEGER," +
                "tahun VARCHAR(4)," +
                "total_revenue NUMERIC(15,2)," +
                "total_tax NUMERIC(15,2)," +
                "total_payment NUMERIC(15,2)," +
                "transaction_count INTEGER," +
                "synced_at TIMESTAMP," +
                "UNIQUE (hotel_id, tahun)" +
            ");";
            
            stmt.executeUpdate(sqlHotel);
            System.out.println("Created table hotel_accommodations");
            
            stmt.executeUpdate(sqlRealisasi);
            System.out.println("Created table hotel_realisasi");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
