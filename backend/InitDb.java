import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class InitDb {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asset_db", "root", "admin");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS notice (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "title VARCHAR(255) NOT NULL," +
                "content TEXT NOT NULL," +
                "author_id INT NOT NULL," +
                "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP NULL," +
                "FOREIGN KEY (author_id) REFERENCES users(id)" +
                ")");
            System.out.println("Notice table created successfully.");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
