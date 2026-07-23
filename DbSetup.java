import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DbSetup {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/pbjt_assessment_db";
        String user = "postgres";
        String password = "1234";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            System.out.println("Connected to the database!");
            
            String sqlContent = new String(Files.readAllBytes(Paths.get("src/main/resources/sql/pbjt_assessment_setup.sql")));
            System.out.println("Loaded SQL script. Executing...");
            
            stmt.execute(sqlContent);
            System.out.println("Setup script executed successfully!");
            
            System.out.println("Executing seeder script...");
            String seedContent = new String(Files.readAllBytes(Paths.get("src/main/resources/sql/pbjt_assessment_seed_data.sql")));
            stmt.execute(seedContent);
            System.out.println("Seeder script executed successfully!");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}