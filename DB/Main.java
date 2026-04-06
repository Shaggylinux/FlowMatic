import javax.swing.JFrame;

import java.sql.*;

import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.*;

import org.mindrot.jbcrypt.BCrypt;

public class Main {
    static void saluda(){
            System.out.println("aasdfasdfasdf");
    }
    public static void main(String[] args){
    saluda();
    Conectar cas = new Conectar();
        cas.conectar("jdbc:postgresql://127.0.0.1:5432/gestion", "postgres", "12345");

        JFrame frame = new JFrame();
        JButton boton = new JButton("HOLA");
        JTextField texto1 = new JTextField("asdasdasd1");
        texto1.setBounds(25, 25, 100, 20);
        frame.add(texto1);

        boton.setBounds(50, 50, 20, 30);
        boton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("asdf");
            }
        });

        frame.add(boton);

        frame.setSize(900, 300);
        frame.setLayout(null);
        frame.setVisible(true);
    }
}
