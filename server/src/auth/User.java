package auth;

import java.util.EnumSet;

public class User {
    private final String username;
    private final String password;
    private final EnumSet<Right> rights;

    public User(String username, String password, EnumSet<Right> rights) {
        this.username = username;
        this.password = password;
        this.rights = rights.clone();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public EnumSet<Right> getRights() {
        return rights.clone();
    }

    public boolean has(Right right) {
        return rights.contains(right);
    }
}
