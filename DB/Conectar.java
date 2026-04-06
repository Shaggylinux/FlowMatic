import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Conectar {
    // private String url  = "jdbc:postgresql://127.0.0.1:5432/gestion";
    // private String user = "postgres";
    // private String pass = "12345";

    private String url;
    private String user;
    private String pass;

    Conectar(){
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    public void conectar(String url, String user, String pass){
        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            if (conn != null) {
                System.out.println("Connected to the database!");
                ResultSet rs = conn.createStatement().executeQuery("insert into test(nombre, apellido) values ('Aa','Bb')");
                rs.next();
                System.out.println(rs.getString("nombre"));
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.err.format("SQL State: %s\n%s", e.getSQLState(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
