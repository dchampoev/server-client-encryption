package serverCore;

import auth.UserService;
import auth.UserServiceXml;
import storage.CardStore;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerMain {

    static void main() throws Exception {
        int port = 80;

        UserService userService = new UserServiceXml("users.xml");
        CardStore cardStore = new CardStore();

        AtomicInteger activeClients = new AtomicInteger(0);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port);

            while (true) {
                Socket client = serverSocket.accept();

                activeClients.incrementAndGet();

                new ClientHandler(client, userService, cardStore, activeClients).start();
            }
        }
    }
}
