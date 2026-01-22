package admin;

import auth.Right;
import auth.User;
import auth.UserServiceXml;
import serverUI.api.AdminUserService;
import serverUI.api.UserInfo;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class AdminUserServiceImpl implements AdminUserService {

    private final UserServiceXml userService;

    public AdminUserServiceImpl(UserServiceXml userService) {
        this.userService = userService;
    }

    @Override
    public synchronized List<UserInfo> listUsers() {
        List<UserInfo> result = new ArrayList<>();
        for (User u : userService.listUsers()) {
            result.add(new UserInfo(u.getUsername(), mapRightsToUi(u.getRights())));
        }
        return result;
    }

    @Override
    public synchronized void addOrUpdateUser(String username, String password, EnumSet<serverUI.api.Right> rights) {
        userService.addOrUpdateUser(new User(username, password, mapRightsToServer(rights)));
    }

    @Override
    public synchronized boolean deleteUser(String username) {
        return userService.deleteUser(username);
    }

    @Override
    public synchronized void reload() {
        userService.load();
    }

    @Override
    public synchronized void save() {
        userService.save();
    }

    private static EnumSet<serverUI.api.Right> mapRightsToUi(EnumSet<Right> rights) {
        EnumSet<serverUI.api.Right> r = EnumSet.noneOf(serverUI.api.Right.class);
        if (rights.contains(Right.ENCRYPT)) r.add(serverUI.api.Right.ENCRYPT);
        if (rights.contains(Right.DECRYPT)) r.add(serverUI.api.Right.DECRYPT);
        if (rights.contains(Right.ADMIN))   r.add(serverUI.api.Right.ADMIN);
        return r;
    }

    private static EnumSet<Right> mapRightsToServer(EnumSet<serverUI.api.Right> rights) {
        EnumSet<Right> r = EnumSet.noneOf(Right.class);
        if (rights.contains(serverUI.api.Right.ENCRYPT)) r.add(Right.ENCRYPT);
        if (rights.contains(serverUI.api.Right.DECRYPT)) r.add(Right.DECRYPT);
        if (rights.contains(serverUI.api.Right.ADMIN))   r.add(Right.ADMIN);
        return r;
    }
}
