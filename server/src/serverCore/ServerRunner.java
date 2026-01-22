package serverCore;

import auth.UserServiceXml;
import storage.CardStore;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerRunner implements Runnable {

    private final int port;
    private final UserServiceXml userService;
    private final CardStore store;

    private final AtomicInteger activeClients = new AtomicInteger(0);

    public ServerRunner(int port, UserServiceXml userService, CardStore store) {
        this.port = port;
        this.userService = userService;
        this.store = store;
    }

    public int getActiveClients() {
        return activeClients.get();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();

                activeClients.incrementAndGet();

                ClientHandler handler =
                        new ClientHandler(socket, userService, store, activeClients);
                handler.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
