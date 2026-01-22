package serverUI.api;

import java.util.List;

public interface AdminUserService {
    List<UserInfo> listUsers();
    void addOrUpdateUser(String username, String password, java.util.EnumSet<Right> rights);
    boolean deleteUser(String username);
    void reload(); // load from XML (или от storage)
    void save();   // save to XML
}
