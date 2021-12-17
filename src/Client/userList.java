package Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class userList extends JFrame implements UserStatusListener{
    public JPanel content;
    public JButton chat;
    public JButton button2;
    public JButton logoff;
    private JList list1;
    public JPanel listPane;
    public JScrollPane js;
    public JTextField Status;
    public JTextField usernameTextField;
    private ChatClient client;
    private DefaultListModel<String>userListModel;
    private String username;

    public userList(ChatClient client, String username){
        logoff.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    client.logoff();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                dispose();
                System.exit(0);
            }
        });
        list1 = new JList();
        userListModel = new DefaultListModel<>();
        userListModel.addElement("Hello");
        userListModel.addElement("World");
        list1.setModel(userListModel);
        js.add(list1);
        this.username = username;
        this.usernameTextField.setText(this.username);
        this.client = client;
        this.setContentPane(content);
        this.pack();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }


    @Override
    public void online(String login) {
        System.out.println("Hellllllll");
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}
