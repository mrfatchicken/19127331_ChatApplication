package Server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class UserList {
    ArrayList<User> userArrayList = new ArrayList<User>();

    public UserList() {
        this.getData();
    }
    private void printAllUser(){
        for (User user : userArrayList){
            System.out.println(user);
        }
    }
    private void getData() {
        try (BufferedReader in = new BufferedReader(new FileReader("userDB.txt"))) {
            String str;
            while((str=in.readLine())!=null && str.length()!=0){
                String[] temp =  str.split(",");
                User newUser = new User(temp[0], temp[1]);
                userArrayList.add(newUser);
            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public boolean validation(String username,String password){
        for (User user : userArrayList){
            if(user.validation(username, password)) return true;
        }
        return false;
    }
    public boolean addUser(String username, String password){
        for(User item: userArrayList){
            if (item.isUserName(username)) return false;
        }
        User newUser = new User(username,password);
        userArrayList.add(newUser);
        return true;
    }
    public void saveData(){
        FileWriter fileWrite;
        try{
            fileWrite = new FileWriter("userDB.txt");
            System.out.println("Writing user info. ");
            for(User item: userArrayList){
                fileWrite.write(item.toString()+"\n");
            }
            System.out.println("Complete.");
            fileWrite.close();
        } catch (IOException e) {
            System.out.println(e);;
        }
    }
    public static void main(String arg[]){
        UserList l = new UserList();
        l.printAllUser();
        System.out.println(l.addUser("anh","hoanganh"));
        l.saveData();
    }
}
