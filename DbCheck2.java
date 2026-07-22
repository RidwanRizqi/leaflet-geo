import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheck2 {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/pbjt_assessment_db";
        String user = "postgres";
        String password = "1234";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery("SELECT id, observations, menu_items FROM pbjt_assessments LIMIT 2");
            while (rs.next()) {
                System.out.println("ID: " + rs.getLong("id") + " - observations: " + rs.getString("observations"));
                System.out.println("ID: " + rs.getLong("id") + " - menu_items: " + rs.getString("menu_items"));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}