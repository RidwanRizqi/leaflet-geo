import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class DbFix {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/pbjt_assessment_db";
        String user = "postgres";
        String password = "1234";
        
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             PreparedStatement updateStmt = conn.prepareStatement("UPDATE pbjt_observation_history SET sample_transactions = ?::jsonb WHERE id = ?")) {
            
            ResultSet rs = stmt.executeQuery("SELECT id, sample_transactions FROM pbjt_observation_history");
            while (rs.next()) {
                long id = rs.getLong("id");
                String data = rs.getString("sample_transactions");
                if (data != null && data.startsWith("[") && !data.contains("{")) {
                    // It's an array of numbers like [25000, 32000]
                    String[] numbers = data.replace("[", "").replace("]", "").split(",");
                    StringBuilder newJson = new StringBuilder("[");
                    for (int i = 0; i < numbers.length; i++) {
                        String num = numbers[i].trim();
                        if (!num.isEmpty()) {
                            newJson.append("{\"amount\":").append(num).append(",\"notes\":\"Seed data\"}");
                            if (i < numbers.length - 1) {
                                newJson.append(",");
                            }
                        }
                    }
                    newJson.append("]");
                    
                    updateStmt.setString(1, newJson.toString());
                    updateStmt.setLong(2, id);
                    updateStmt.executeUpdate();
                    System.out.println("Updated ID: " + id);
                }
            }
            System.out.println("Finished updating seed data!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}