package Server;



import java.io.*;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jim on 4/18/17.
 */
public class ServerWorker extends Thread {

    private final Socket clientSocket;
    private final Server server;
    private String login = "null";
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();
    private UserList list;
    private InputStream inputStream;
    private BufferedReader reader;

    public ServerWorker(Server server, Socket clientSocket, UserList list) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.list = list;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        this.inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        this.reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( (line = reader.readLine()) != null) {
            String[] tokens = line.split(" ");
            System.out.println(login + " : " + line);

            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if ("logoff".equals(cmd)) {
                    handleLogoff();
                    break;
                } else if ("quit".equalsIgnoreCase(cmd)) {
                    handleQuit();
                } else if ("login".equalsIgnoreCase(cmd)) {
                    handleLogin(outputStream, tokens);
                } else if ("msg".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = line.split(" ", 3);
                    handleMessage(tokensMsg);
                } else if ("join".equalsIgnoreCase(cmd)) {
                    handleJoin(tokens);
                } else if ("leave".equalsIgnoreCase(cmd)) {
                    handleLeave(tokens);
                } else if ("register".equalsIgnoreCase(cmd)) {
                    handleRegister(tokens);
                } else if ("download".equalsIgnoreCase(cmd)) {
                    handleDownload(tokens);
                } else if ("upload".equalsIgnoreCase(cmd)) {
                    handleUpload(tokens);
                } else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }

        clientSocket.close();
    }
    private void handleUpload(String[] tokens) throws IOException {
        String owner = tokens[1];
        String sendTo = tokens[2];
        Path path = FileSystems.getDefault().getPath(tokens[3]);
        String filename = path.getFileName().toString();

        outputStream.write("ok\n".getBytes());
        FileOutputStream fos = new FileOutputStream("ServerFiles/"+filename);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        long fileLength = Long.parseLong(reader.readLine());
        System.out.println(filename + " size: " + fileLength);

        outputStream.write("ok\n".getBytes());

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
            bytesRead = inputStream.read(contents);
            bos.write(contents, 0 ,bytesRead);
        }
        bos.flush();
        bos.close();
        fos.close();
        boolean isTopic = sendTo.charAt(0) == '#';
        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "file " + sendTo + ":" + owner + " " + filename + "\n";
                    worker.send(outMsg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "file " + owner + " " + filename + "\n";
                    worker.send(outMsg);
                }
            }
        }

    }
    private void handleDownload(String[] tokens) throws IOException{
        String filename = tokens[1];
        File file = new File("ServerFiles/"+filename);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        byte[] contents;
        long fileLength = file.length();
        outputStream.write((fileLength + "\n").getBytes());
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
            outputStream.write(contents);
            System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!\n");

        }
        outputStream.flush();
        bis.close();
        fis.close();
    }


    private void handleRegister(String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String username = tokens[1];
            String password = tokens[2];
            if(list.addUser(username,password)){
                list.saveData();
                String msg = "ok register\n";
                outputStream.write(msg.getBytes());
            }
            else {
                String msg = "username has been taken\n";
                outputStream.write(msg.getBytes());
            }
        }
        else {
            String msg = "error register\n";
            outputStream.write(msg.getBytes());
            System.err.println("Register failed");
        }

    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    // format: "msg" "login" body...
    // format: "msg" "#topic" body...
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerWorker> workerList = server.getWorkerList();
        for(ServerWorker worker : workerList) {
            if (isTopic) {
                if (worker.isMemberOfTopic(sendTo)) {
                    String outMsg = "msg " + sendTo + ":" + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            } else {
                if (sendTo.equalsIgnoreCase(worker.getLogin())) {
                    String outMsg = "msg " + login + " " + body + "\n";
                    worker.send(outMsg);
                }
            }
        }
    }

    private void handleLogoff() throws IOException {
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();

        // send other online users current user's status
        if (login != "null") {
            String onlineMsg = "offline " + login + "\n";
            for (ServerWorker worker : workerList) {
                if (!login.equals(worker.getLogin())) {
                    worker.send(onlineMsg);
                }
            }
        }
        clientSocket.close();
    }
    private void handleQuit() throws IOException{
        server.removeWorker(this);
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if (list.validation(login, password)) {
                String msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in succesfully: " + login);

                List<ServerWorker> workerList = server.getWorkerList();
                System.out.println(login + " online");
                // send current user all other online logins
                for(ServerWorker worker : workerList) {
                    if (worker.getLogin() != "null") {
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current user's status
                String onlineMsg = "online " + login + "\n";
                for(ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }
        }
    }

    private void send(String msg) throws IOException {
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
