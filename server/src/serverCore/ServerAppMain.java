package serverCore;

import auth.UserServiceXml;
import admin.AdminUserServiceImpl;
import storage.CardStore;
import serverUI.AdminApp;
import serverUI.api.AdminUserService;
import serverUI.api.ExportService;

public class ServerAppMain {

    public static void main(String[] args) {
        int port = 80;

        UserServiceXml users = new UserServiceXml("users.xml");
        CardStore store = new CardStore();

        ServerController controller = new ServerController(80, users, store);

        serverUI.api.AdminUserService adminSvc = new admin.AdminUserServiceImpl(users);
        serverUI.api.ServerControlService controlSvc = new admin.ServerControlServiceImpl(controller);
        serverUI.api.LogService logSvc = new admin.LogServiceImpl("server.log");
        serverUI.api.ExportService exportSvc = new admin.ExportServiceImpl(store);

        serverUI.AdminApp.setAdminService(adminSvc);
        serverUI.AdminApp.setServerControl(controlSvc);
        serverUI.AdminApp.setLogService(logSvc);
        serverUI.AdminApp.setExportService(exportSvc);

        javafx.application.Application.launch(serverUI.AdminApp.class, args);
    }
}
