import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class UpdateDB {
    public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/avvj_cart";
        String USER = "root";
        String PASS = "Akira@8055";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(URL, USER, PASS);
            Statement stmt = con.createStatement();
            
            try {
                stmt.execute("ALTER TABLE cart ADD COLUMN size VARCHAR(10) DEFAULT ''");
                System.out.println("Size column added to cart table.");
            } catch (Exception e) {
                System.out.println("Error adding to cart (may already exist): " + e.getMessage());
            }

            try {
                stmt.execute("ALTER TABLE order_items ADD COLUMN size VARCHAR(10) DEFAULT ''");
                System.out.println("Size column added to order_items table.");
            } catch (Exception e) {
                System.out.println("Error adding to order_items (may already exist): " + e.getMessage());
            }

            con.close();
            System.out.println("Database update complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
