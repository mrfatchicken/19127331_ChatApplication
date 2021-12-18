package Client;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class test {
    public static void main(String[] arg) throws Exception {
//        File file = new File("Serverfiles/test.txt");
//        FileInputStream fis = new FileInputStream(file);
//        BufferedInputStream bis = new BufferedInputStream(fis);
//
//        byte[] contents;
//        long fileLength = file.length();
//        System.out.println(fileLength);

        ChatClient c = new ChatClient("localhost", 8818);
        c.connect();
        c.login("test","123456");
        c.startMessageReader();
        //c.upload("admin","ClientFiles/boss.jpg");
        c.download("boss.jpg");
        c.logoff();

//        Path path = FileSystems.getDefault().getPath("ClientFiles/boss.jpg");
//        String filename = path.getFileName().toString();
//        System.out.println(filename);
    }
}
