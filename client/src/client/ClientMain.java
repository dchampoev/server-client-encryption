package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {
    private static final String HOST = "localhost";
    private static final int PORT = 80;

    static void main() {
        try(
                Socket socket = new Socket(HOST,PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
                Scanner scanner = new Scanner(System.in)
                ){
            System.out.println(in.readLine());

            //LOGIN
            System.out.print("Username: ");
            String user =  scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            out.println("LOGIN "+user+" "+password);
            String resp = readResponseOrExit(in);
            if (resp == null) return;

            System.out.println(resp);

            if (!resp.startsWith("OK")) {
                return;
            }

            //COMMAND LOOP
            while(true){
                System.out.print("> ");
                String cmd =  scanner.nextLine().trim();
                if(cmd.isEmpty()) continue;

                out.println(cmd);
                String serverResp = readResponseOrExit(in);
                if (serverResp == null) break;

                System.out.println(serverResp);

                if(cmd.equalsIgnoreCase("QUIT")){
                    break;
                }
            }
        } catch(IOException e) {
            System.err.println("Client error: "+e.getMessage());
        }
    }
    private static String readResponseOrExit(BufferedReader in) throws IOException {
        String resp = in.readLine();

        if (resp == null) {
            System.out.println("Server disconnected.");
            return null;
        }

        if (resp.startsWith("ERR SERVER STOPPED")) {
            System.out.println("Server stopped.");
            return null;
        }

        return resp;
    }
}
