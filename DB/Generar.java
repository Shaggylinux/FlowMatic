import org.mindrot.jbcrypt.BCrypt;

public class Generar {
    public String GenerarClave(String clave){
        return BCrypt.hashpw(clave, BCrypt.gensalt(10));
    }
}