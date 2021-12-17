package Server;

import java.util.Objects;

public class User {
    private String username;
    private String password;

    public User(String userame, String password) {
        this.username = userame;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    public boolean validation(String user, String pass){
        return Objects.equals(this.username, user) && Objects.equals(this.password, pass);
    }
    public boolean isUserName(String user){
        return Objects.equals(this.username, user);
    }
    @Override
    public String toString() {
        return username+","+password;
    }
}
