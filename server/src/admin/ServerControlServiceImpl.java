package admin;

import serverCore.ServerController;
import serverUI.api.ServerControlService;

public class ServerControlServiceImpl implements ServerControlService {

    private final ServerController controller;

    public ServerControlServiceImpl(ServerController controller) {
        this.controller = controller;
    }

    @Override public void start() { controller.start(); }
    @Override public void stop() { controller.stop(); }
    @Override public boolean isRunning() { return controller.isRunning(); }
    @Override public int getPort() { return controller.getPort(); }
    @Override public int getActiveClients() { return controller.getActiveClients(); }
}
