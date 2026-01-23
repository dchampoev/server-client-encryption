package serverCore;

import auth.UserServiceXml;
import storage.CardStore;

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerRunner implements Runnable {

    private final int port;
    private final UserServiceXml userService;
    private final CardStore store;

    private final AtomicInteger activeClients = new AtomicInteger(0);
    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();

    private volatile boolean running = true;
    private volatile ServerSocket serverSocket;

    public ServerRunner(int port, UserServiceXml userService, CardStore store) {
        this.port = port;
        this.userService = userService;
        this.store = store;
    }

    public int getActiveClients() { return activeClients.get(); }
    public boolean isRunning() { return running; }
    public int getPort() { return port; }

    public void stopServer() {
        running = false;

        // 1) stop accepting new clients
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}

        // 2) kick all connected clients with message
        for (Socket s : clients) {
            kickClient(s, "ERR SERVER STOPPED");
        }
        clients.clear();
    }

    private void kickClient(Socket s, String msg) {
        if (s == null) return;
        try {
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(msg);
        } catch (Exception ignored) {
        }
        try { s.close(); } catch (Exception ignored) {}
    }

    @Override
    public void run() {
        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;
            System.out.println("Server listening on port " + port);

            while (running) {
                Socket socket;
                try {
                    socket = ss.accept();
                } catch (Exception e) {
                    // when stopServer() closes ServerSocket, accept() throws -> exit loop
                    break;
                }

                clients.add(socket);
                activeClients.incrementAndGet();

                ClientHandler handler = new ClientHandler(socket, userService, store, activeClients, clients);
                handler.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            running = false;
            // safety: close any leftover
            for (Socket s : clients) kickClient(s, "ERR SERVER STOPPED");
            clients.clear();
        }
    }
}
