package serverUI.api;

import java.util.EnumSet;

public class UserInfo {
    public final String username;
    public final EnumSet<Right> rights;

    public UserInfo(String username, EnumSet<Right> rights) {
        this.username = username;
        this.rights = rights.clone();
    }
}
