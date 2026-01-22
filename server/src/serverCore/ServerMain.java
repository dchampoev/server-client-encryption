package serverCore;

import auth.UserService;
import auth.UserServiceXml;
import serverCore.ClientHandler;
import storage.CardStore;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    static void main() throws Exception {
        int port = 80;

        UserService userService= new UserServiceXml("users.xml");
        CardStore cardStore = new CardStore();

        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Server listening on port "+port);

            while(true){
                Socket client = serverSocket.accept();
                new ClientHandler(client,userService,cardStore).start();
            }
        }
    }
}
