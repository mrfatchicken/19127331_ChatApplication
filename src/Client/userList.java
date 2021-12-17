package Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class userList extends JFrame implements UserStatusListener{
    public JPanel content;
    public JButton chat;
    public JButton logoff;
    private JList list1;
    public JPanel listPane;
    public JScrollPane js;
    public JTextField Status;
    public JTextField usernameTextField;
    private ChatClient client;
    private DefaultListModel<String> userListModel;
    private String username;

    public userList(ChatClient client, String username){
        JFrame jf = this;
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

        userListModel = new DefaultListModel<String>();
        list1 = new JList(userListModel);
        js = new JScrollPane(list1);
        listPane.add(js);
        this.username = username;
        this.usernameTextField.setText(this.username);

        this.client = client;
        this.client.addUserStatusListener(this);
        client.startMessageReader();
        this.setContentPane(content);
        this.pack();
        this.setVisible(true);
        System.out.println("GUI online set up");
    }


    @Override
    public void online(String login) {
        System.out.println("GUI online "+ login);
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}
