package serverCore;

import auth.UserServiceXml;
import storage.CardStore;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerController {

    private final int port;
    private final UserServiceXml userService;
    private final CardStore store;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger activeClients = new AtomicInteger(0);

    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();

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

        // 1) stop accept loop
        running.set(false);
        try {
            if (serverSocket != null) serverSocket.close(); // breaks accept()
        } catch (Exception ignored) {}

        // 2) kick all clients
        kickAllClients();
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

                clients.add(s);
                activeClients.incrementAndGet();

                ClientHandler handler =
                        new ClientHandler(s, userService, store, activeClients, clients);
                handler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            serverSocket = null;
            running.set(false);

            // safety: if server exits, also kick leftovers
            kickAllClients();
        }
    }

    private void kickAllClients() {
        for (Socket s : clients) {
            kickClient(s);
        }
        clients.clear();
    }

    private void kickClient(Socket s) {
        if (s == null) return;
        try {
            new PrintWriter(s.getOutputStream(), true).println("ERR SERVER STOPPED");
        } catch (Exception ignored) {
        }
        try { s.close(); } catch (Exception ignored) {}
    }
}