import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheck {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/pbjt_assessment_db";
        String user = "postgres";
        String password = "1234";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT id, sample_transactions FROM pbjt_observation_history");
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("id") + " - Data: " + rs.getString("sample_transactions"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}