import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

public class Conectar {
    private String url  = "jdbc:postgresql://127.0.0.1:5432/gestion";
    private String user = "postgres";
    private String pass = "12345";
    private Connection conn;

    Conectar(){
        try{
            this.conn = DriverManager.getConnection(url, user, pass);
            if (this.conn != null){
                System.out.println("Si conecto XD");
            } else {
                System.out.println("No conecto :C");
            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void EnviarQuery(String use, String ape, String ema, String tel, String cla){
        Generar g = new Generar();
        String sql = "INSERT INTO usuarios(username, apellido, email, telefono, clave, rol) VALUES (?, ?, ?, ?, ?, ?)";
        try ( PreparedStatement algo = this.conn.prepareStatement(sql)){
            algo.setString(1, use);
            algo.setString(2, ape);
            algo.setString(3, ema);
            algo.setString(4, tel);
            algo.setString(5, g.GenerarClave(cla));
            algo.setString(6, "ROL_ADMINISTRADOR");
            algo.executeUpdate();
        } catch (SQLException e){
            System.out.println(e);
        }
    }
}
