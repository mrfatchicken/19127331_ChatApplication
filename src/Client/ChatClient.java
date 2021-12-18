package Client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;


public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private InputStream serverIn;
    private OutputStream serverOut;
    private BufferedReader bufferedIn;
    private boolean runnable = true;
    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private ArrayList<FileInteract> fileListeners = new ArrayList<>();
    private String username;
    Thread t;

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String msgBody) {
                System.out.println("You got a message from " + fromLogin + " ===>" + msgBody);
            }
        });
        client.addFileInteractListener(new FileInteract() {
            @Override
            public void onFileNotice(String fromLogin, String filename) {
                System.out.println("You got a file from " + fromLogin + " named " + filename);
            }
        });
        if (!client.connect()) {
            System.err.println("Connect failed.");
        } else {
            Login login = new Login(client);
//            System.out.println("Connect successful");
//
//            if (client.login("guest", "guest")) {
//                System.out.println("Login successful");
//                client.msg("jim", "Hello World!");
//            } else {
//                System.err.println("Login failed");
//            }
            //client.startMessageReader();
            //client.logoff();
        }
    }

    public void msg(String sendTo, String msgBody) throws IOException {
        String cmd = "msg " + sendTo + " " + msgBody + "\n";
        serverOut.write(cmd.getBytes());
    }
    public boolean register(String login, String password) throws IOException {
        String cmd = "register " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line:" + response);

        if ("ok register".equalsIgnoreCase(response)) {
            return true;
        } else {
            return false;
        }
    }
    public boolean login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line:" + response);

        if ("ok login".equalsIgnoreCase(response)) {
            this.username = login;
            return true;
        } else {
            return false;
        }
    }

    public void logoff() throws IOException {
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }
    public void quit() throws Exception {
        String cmd = "quit\n";
        serverOut.write(cmd.getBytes());
    }
    public void upload(String login, String filename) throws IOException, InterruptedException {
        Socket socketUpload = new Socket(serverName, serverPort);
        OutputStream out = socketUpload.getOutputStream();
        InputStream in = socketUpload.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String cmd = "upload " +username + " "+ login + " "+ filename + "\n";
        out.write(cmd.getBytes());
        reader.readLine();
        File file = new File(filename);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] contents;
        long fileLength = file.length();
        out.write((fileLength + "\n").getBytes());
        reader.readLine();
        long current = 0;
        long start = System.nanoTime();
        while(current != fileLength){
            int size = 10000;
            if (fileLength - current > size){
                current += size;
            }
            else {
                size = (int) (fileLength - current);
                current = fileLength;
            }
            contents = new byte[size];
            bis.read(contents,0,size);
            out.write(contents);
            System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!\n");
        }
        out.flush();
        String sre  = "logoff\n";
        out.write(sre.getBytes());
        bis.close();
        fis.close();
    }
    public void download(String filename) throws IOException {
        Socket socketDownload = new Socket(serverName, serverPort);
        OutputStream out = socketDownload.getOutputStream();
        InputStream in = socketDownload.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String cmd = "download " + filename + "\n";
        out.write(cmd.getBytes());
        FileOutputStream fos = new FileOutputStream("ClientFiles/"+filename);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        long fileLength = Long.parseLong(reader.readLine());
        System.out.println(fileLength);
        out.write("ok\n".getBytes());
        byte[] contents;
        int bytesRead = 0;
        long current = 0;
        while (current != fileLength){
            int size  = 10000;
            if(fileLength -current >= size){
                current += size;
            }
            else {
                size = (int)(fileLength - current);
                current = fileLength;
            }
            contents = new byte[size];
            bytesRead = in.read(contents);
            bos.write(contents, 0 ,bytesRead);
        }
        bos.flush();
        String sre  = "logoff\n";
        out.write(sre.getBytes());
        bos.close();
        fos.close();
    }
    public void startMessageReader() {
        t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }

        };
        t.start();
    }

    public void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null && runnable) {
                System.out.println("Hello");
                String[] tokens = line.split(" ");
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = line.split(" ", 3);
                        handleMessage(tokensMsg);
                    } else if ("file".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = line.split(" ", 3);
                        handleFile(tokensMsg);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleFile(String[] tokensMsg) {
        String login = tokensMsg[1];
        String filename = tokensMsg[2];
        for(FileInteract listener: fileListeners){
            listener.onFileNotice(login, filename);
        }
    }

    public void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];

        for(MessageListener listener : messageListeners) {
            listener.onMessage(login, msgBody);
        }
    }

    public void handleOffline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    public void handleOnline(String[] tokens) {
        String login = tokens[1];
        for(UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName, serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
    public void addFileInteractListener(FileInteract listener) {
        fileListeners.add(listener);
    }

    public void removeFileInteractListener(FileInteract listener) {
        fileListeners.remove(listener);
    }

}
