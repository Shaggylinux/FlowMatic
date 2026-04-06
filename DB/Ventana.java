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
    private JTextField cla  = new JTextField();
    private JTextField claV = new JTextField();
    
    private JButton Aceptar = new JButton("Registrar");
    private Conectar query = new Conectar();
    
    Ventana(int x, int y) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        frame.setSize(x, y);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        int labelX = 50;
        int fieldX = 150;
        int width  = 200;
        int height = 30;

        lblUser.setBounds(labelX, 40, 100, height);
        use.setBounds(fieldX, 40, width, height);

        lblApe.setBounds(labelX, 80, 100, height);
        ape.setBounds(fieldX, 80, width, height);

        lblEma.setBounds(labelX, 120, 100, height);
        ema.setBounds(fieldX, 120, width, height);

        lblTel.setBounds(labelX, 160, 100, height);
        tel.setBounds(fieldX, 160, width, height);

        lblCla.setBounds(labelX, 200, 100, height);
        cla.setBounds(fieldX, 200, width, height);

        lblClaV.setBounds(labelX, 240, 100, height);
        claV.setBounds(fieldX, 240, width, height);


        Aceptar.setBounds(fieldX, 300, width, 40);
        Aceptar.setBackground(new Color(34, 139, 34));
        Aceptar.setForeground(Color.WHITE);
        Aceptar.setFocusPainted(false);
    }

    public String GetUse(){return use.getText();}
    public String GetApe(){return ape.getText();}
    public String GetEma(){return ema.getText();}
    public String GetTel(){return tel.getText();}
    public String GetCla(){return cla.getText();}

    public void ventana() {
        Aceptar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(GetUse().isEmpty() || GetTel().isEmpty() || GetEma().isEmpty() || GetUse().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Todos los campos son requeridos.");
                    return;
                }

                if (cla.getText().length() >= 8 && claV.getText().length() >= 8){
                    if (cla.getText().equals(claV.getText())){
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