package serverUI.api;

public interface ServerControlService {
    void start();
    void stop();
    boolean isRunning();
    int getPort();
    int getActiveClients();
}
