package Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        this.setContentPane(content);
        this.pack();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.client = client;
        this.setVisible(true);
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
                    }
                    else if (Objects.equals(username, "") || Objects.equals(password, "")) {
                        Status.setText("Please enter username and password");
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
                try {
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
