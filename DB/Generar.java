import org.mindrot.jbcrypt.BCrypt;

public class Generar {
    public String GenerarClave(String clave){
        String pass = BCrypt.hashpw(clave, BCrypt.gensalt());
        return pass;
    }
}