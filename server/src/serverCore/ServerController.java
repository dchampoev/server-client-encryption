package serverCore;

import auth.UserServiceXml;
import storage.CardStore;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerController {

    private final int port;
    private final UserServiceXml userService;
    private final CardStore store;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger activeClients = new AtomicInteger(0);

    private Thread thread;
    private volatile ServerSocket serverSocket;

    public ServerController(int port, UserServiceXml userService, CardStore store) {
        this.port = port;
        this.userService = userService;
        this.store = store;
    }

    public int getPort() { return port; }
    public boolean isRunning() { return running.get(); }
    public int getActiveClients() { return activeClients.get(); }

    public synchronized void start() {
        if (running.get()) return;

        running.set(true);
        thread = new Thread(this::runLoop, "ServerRunner");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop() {
        if (!running.get()) return;

        running.set(false);
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (Exception ignored) {}
    }

    private void runLoop() {
        try (ServerSocket ss = new ServerSocket(port)) {
            serverSocket = ss;
            System.out.println("Server listening on port " + port);

            while (running.get()) {
                Socket s;
                try {
                    s = ss.accept();
                } catch (Exception e) {
                    break;
                }

                activeClients.incrementAndGet();
                ClientHandler handler = new ClientHandler(s, userService, store, activeClients);
                handler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverSocket = null;
            running.set(false);
        }
    }
}
