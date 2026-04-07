import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Ventana {
    private JFrame frame = new JFrame("Registro de Administrador");
    
    private JLabel lblUser  = new JLabel("Usuario :");
    private JLabel lblApe   = new JLabel("Apellido :");
    private JLabel lblEma   = new JLabel("Email :");
    private JLabel lblTel   = new JLabel("Teléfono :");
    private JLabel lblCla   = new JLabel("Clave :");
    private JLabel lblClaV  = new JLabel("Verificar clave :");

    private JTextField use  = new JTextField();
    private JTextField ape  = new JTextField();
    private JTextField ema  = new JTextField();
    private JTextField tel  = new JTextField();
    private JPasswordField cla  = new JPasswordField();
    private JPasswordField claV = new JPasswordField();
    
    private JButton Aceptar = new JButton("Registrar");
    private Conectar query = new Conectar();
    
Ventana(int x, int y) {
    try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {}

    frame.setSize(x, y);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);
    frame.setLayout(null);

    frame.getContentPane().setBackground(new Color(244, 246, 248));

    Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
    Font fieldFont = new Font("Segoe UI", Font.PLAIN, 13);

    int labelX = 60;
    int fieldX = 180;
    int width  = 220;
    int height = 30;

    JLabel[] labels = {lblUser, lblApe, lblEma, lblTel, lblCla, lblClaV};
    int yPos = 40;

    for (JLabel lbl : labels) {
        lbl.setFont(labelFont);
        lbl.setForeground(new Color(51, 51, 51));
        lbl.setBounds(labelX, yPos, 120, height);
        yPos += 40;
    }

    JTextField[] fields = {use, ape, ema, tel, cla, claV};
    yPos = 40;

    for (JTextField txt : fields) {
        txt.setFont(fieldFont);
        txt.setBounds(fieldX, yPos, width, height);
        txt.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        yPos += 40;
    }

    Aceptar.setBounds(fieldX, 300, width, 40);
    Aceptar.setBackground(new Color(15, 118, 110));
    Aceptar.setForeground(Color.WHITE);
    Aceptar.setFont(new Font("Segoe UI", Font.BOLD, 14));
    Aceptar.setFocusPainted(false);
    Aceptar.setBorder(BorderFactory.createEmptyBorder());
}

    public String GetUse(){return use.getText();}
    public String GetApe(){return ape.getText();}
    public String GetEma(){return ema.getText();}
    public String GetTel(){return tel.getText();}
    public String GetCla(){return new String(cla.getPassword());}

    public void ventana() {
        Aceptar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(GetUse().isEmpty() || GetTel().isEmpty() || GetEma().isEmpty() || GetUse().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Todos los campos son requeridos.");
                    return;
                }
                if (GetCla().length() >= 8 && new String(claV.getPassword()).length() >= 8){
                    if (GetCla().equals(new String(claV.getPassword()))){
                        query.EnviarQuery(GetUse(), GetApe(), GetEma(), GetTel(), GetCla());
                    JOptionPane.showMessageDialog(frame, "Administrador registrado exitosamente.");
                } else {
                    JOptionPane.showMessageDialog(frame, "Las claves ingresadas *NO* son iguales.");
                    return;
                }
            } else {
                JOptionPane.showMessageDialog(frame, "La clave ingresada debe ser mayor de 8 digitos.");
            }
            }
        });

        frame.add(lblUser);  frame.add(use);
        frame.add(lblApe);   frame.add(ape);
        frame.add(lblEma);   frame.add(ema);
        frame.add(lblTel);   frame.add(tel);
        frame.add(lblCla);   frame.add(cla);
        frame.add(lblClaV);  frame.add(claV);
        frame.add(Aceptar);

        frame.setLayout(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}