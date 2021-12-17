package Client;

import javax.swing.*;

public class Message extends JFrame implements MessageListener {
    private JPanel content;
    private JTextField messageField;
    private JButton sendFile;
    private JButton sendButton;
    private JPanel messagePane;
    private JPanel sideBar;
    private String target;
    private String username;
    private ChatClient client;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);

    public String getTarget() {
        return target;
    }

    @Override
    public void onMessage(String fromLogin, String msgBody) {
        if (target.equalsIgnoreCase(fromLogin)) {
            String line = fromLogin + ": " + msgBody;
            listModel.addElement(line);
        }
    }
}
