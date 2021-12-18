package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;

public class Login extends JFrame{
    private JPanel content;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton login;
    private JButton register;
    private JTextArea Status;
    private ChatClient client;
    public Login(ChatClient client){
        JFrame jf = this;
        this.setContentPane(content);
        this.pack();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.client = client;
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                if (JOptionPane.showConfirmDialog(jf,
                        "Are you sure you want to close this window?", "Close Window?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    try {
                        client.logoff();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.exit(0);
                }
            }
        });
        login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = String.valueOf(passwordField.getPassword());
                try {
                    if(client.login(username, password)){
                        System.out.println("Login ok");
                        setVisible(false);
                        userList listPane = new userList(client,username);
                        return;
                    }
                    else if (Objects.equals(username, "") || Objects.equals(password, "")) {
                        Status.setText("Please enter username and password");
                    }
                    else if (username.charAt(0) == '#'){
                        Status.setText("This username is not allowed");
                    }
                    else {
                        Status.setText("Wrong username or password");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        register.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = String.valueOf(passwordField.getPassword());
                if (Objects.equals(username, "null")){
                    Status.setText("Username has been taken");
                    return;
                }
                try {
                    if (username.charAt(0) == '#'){
                        Status.setText("This username is not allowed");
                        return;
                    }
                    if(client.register(username, password)){
                        System.out.println("Register ok");
                        if(client.login(username, password)){
                            userList listPane = new userList(client,username);
                        }
                        setVisible(false);
                    }
                    else if (Objects.equals(username, "") || Objects.equals(password, "")) {
                        Status.setText("Please enter username and password");
                    }
                    else {
                        Status.setText("Username has been taken");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
    public static void main(String[] arg){

    }

}
