package Client;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class FileInteraction extends JFrame implements FileInteract {
    private JPanel content;
    private JButton download;
    private JTextPane status;
    private JPanel fileListPane;
    private ChatClient client;
    private String target;
    private String username;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> fileList = new JList<>(listModel);
    private JScrollPane js = new JScrollPane(fileList);
    public FileInteraction(ChatClient client,String target,String username){
        this.client = client;
        this.username = username;
        this.target = target;
        client.addFileInteractListener(this);

        fileListPane.add(js);
        this.setContentPane(content);
        this.pack();
        this.setTitle("Files");
        this.setVisible(false);
        this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        download.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(fileList.getSelectedValue() != null){
                    String filename = (String) fileList.getSelectedValue();
                    try {
                        client.download(filename);
                        status.setText("Your " + filename + " is downloaded. Please check ClientFiles folder.");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onFileNotice(String fromLogin, String filename) {
        if (target.equalsIgnoreCase(fromLogin)) {
            listModel.addElement(filename);
        }
    }
}
