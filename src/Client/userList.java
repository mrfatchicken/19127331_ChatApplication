package Client;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

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
    private ArrayList<Message> messageArrayList = new ArrayList<Message>();

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
        this.setTitle(username);
        this.pack();
        this.setVisible(true);
        System.out.println("GUI online set up");
        chat.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(list1.getSelectedValue() != null){
                    String target = (String) list1.getSelectedValue();
                    boolean flag = true;
                    for (Message item: messageArrayList){
                        if (Objects.equals(item.getTarget(), target)){
                            item.setVisible(true);
                            flag = false;
                        }
                    }
                    if (flag) {
                        Message message = new Message(client, username, target);
                        message.setVisible(true);
                        messageArrayList.add(message);
                    }
                }
            }
        });
        list1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1){
                    if(list1.getSelectedValue() != null){
                        String target = (String) list1.getSelectedValue();
                        boolean flag = true;
                        for (Message item: messageArrayList){
                            if (Objects.equals(item.getTarget(), target)){
                                item.setVisible(true);
                                flag = false;
                            }
                        }
                        if (flag) {
                            Message message = new Message(client, username, target);
                            message.setVisible(true);
                            messageArrayList.add(message);
                        }
                    }
                }
            }
        });
    }


    @Override
    public void online(String login) {
        System.out.println("GUI online "+ login);
        messageArrayList.add(new Message(client, username, login));
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
        messageArrayList.removeIf(item -> Objects.equals(item.getTarget(), login));
    }
}
