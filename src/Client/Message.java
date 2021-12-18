package Client;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class Message extends JFrame implements MessageListener {
    private JPanel content;
    private JTextField messageField;
    private JButton sendFile;
    private JButton sendButton;
    private JPanel messagePane;
    private JPanel sideBar;
    private JButton fileInteractButton;
    private String target;
    private String username;
    private ChatClient client;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JScrollPane js = new JScrollPane(messageList);
    private FileInteraction fileInteraction;

    public Message(ChatClient client, String username, String target) {
        this.client = client;
        this.username = username;
        this.target = target;
        JFrame jf = this;
        client.addMessageListener(this);

        messagePane.add(js);
        this.setContentPane(content);
        this.pack();
        this.setTitle("CHAT WITH: " + target + "    | YOU: " + username);
        this.setVisible(false);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        fileInteraction = new FileInteraction(client,target,username);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    String text = messageField.getText();
                    client.msg(target,text);
                    listModel.addElement("You: "+ text);
                    messageField.setText("");
                }
                catch (Exception err){
                    err.printStackTrace();
                }
            }
        });

        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    String text = messageField.getText();
                    client.msg(target,text);
                    listModel.addElement("You: "+ text);
                    messageField.setText("");
                }
                catch (Exception err){
                    err.printStackTrace();
                }
            }
        });
        fileInteractButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileInteraction.setVisible(true);
            }
        });
        sendFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileDialog = new JFileChooser(System.getProperty("user.dir"));
                fileDialog.setDialogType(JFileChooser.OPEN_DIALOG);
                fileDialog.setDialogTitle("Choose file");
                int returnVal = fileDialog.showOpenDialog(jf);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    java.io.File file = fileDialog.getSelectedFile();
                    String filename = file.getPath();
                    try {
                        client.upload(target, filename);
                        String text = "You: sent file " + file.getName() + " to " + target;
                        String sending = username + " sent file " + file.getName() + " to you. Please check Files to download.";
                        client.msg(target,sending);
                        listModel.addElement(text);
                    } catch (IOException | InterruptedException ex) {
                        ex.printStackTrace();
                    }

                } else {
                    System.out.println("Not choose file");
                }
            }
        });
    }

    public String getTarget() {
        return target;
    }


    @Override
    public void onMessage(String fromLogin, String msgBody) {
        System.out.println(fromLogin + " : " + msgBody);
        if (target.equalsIgnoreCase(fromLogin)) {
            String line = fromLogin + ": " + msgBody;
            listModel.addElement(line);
        }
    }
}
