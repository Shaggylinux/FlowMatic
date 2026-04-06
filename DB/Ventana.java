import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Ventana {
    private JFrame frame = new JFrame("Registro de Administrador - SENA");
    
    private JLabel lblUser = new JLabel("Usuario:");
    private JLabel lblApe  = new JLabel("Apellido:");
    private JLabel lblEma  = new JLabel("Email:");
    private JLabel lblTel  = new JLabel("Teléfono:");
    private JLabel lblCla  = new JLabel("Clave:");

    private JTextField use = new JTextField();
    private JTextField ape = new JTextField();
    private JTextField ema = new JTextField();
    private JTextField tel = new JTextField();
    private JPasswordField cla = new JPasswordField();
    
    private JButton Aceptar = new JButton("Registrar Administrador");
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

        Aceptar.setBounds(fieldX, 260, width, 40);
        Aceptar.setBackground(new Color(34, 139, 34));
        Aceptar.setForeground(Color.WHITE);
        Aceptar.setFocusPainted(false);
    }

    public String GetUse() { return use.getText().trim(); }
    public String GetApe() { return ape.getText().trim(); }
    public String GetEma() { return ema.getText().trim(); }
    public String GetTel() { return tel.getText().trim(); }
    public String GetCla() { return new String(cla.getPassword()); }

    public void ventana() {
        Aceptar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(GetUse().isEmpty() || GetCla().isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Usuario y Clave son obligatorios");
                    return;
                }
                query.EnviarQuery(GetUse(), GetApe(), GetEma(), GetTel(), GetCla());
                JOptionPane.showMessageDialog(frame, "¡Admin registrado con éxito!");
            }
        });

        frame.add(lblUser); frame.add(use);
        frame.add(lblApe);  frame.add(ape);
        frame.add(lblEma);  frame.add(ema);
        frame.add(lblTel);  frame.add(tel);
        frame.add(lblCla);  frame.add(cla);
        frame.add(Aceptar);

        frame.setLayout(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}