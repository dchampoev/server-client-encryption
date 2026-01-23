package serverCore;

import cipher.CardCipher;
import luhn.LuhnValidator;
import auth.Right;
import auth.User;
import auth.UserService;
import storage.CardStore;
import log.ServerLogger;

import java.io.*;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler extends Thread {
    private static final Logger LOG = ServerLogger.get();
    private final Socket socket;
    private final UserService userService;
    private final CardStore store;
    private final java.util.concurrent.atomic.AtomicInteger activeClients;
    private final Set<Socket> clients;

    public ClientHandler(Socket socket, UserService userService, CardStore store,
                         java.util.concurrent.atomic.AtomicInteger activeClients, Set<Socket> clients) {
        this.socket = socket;
        this.userService = userService;
        this.store = store;
        this.activeClients = activeClients;
        this.clients = clients;
    }
    @Override
    public void run() {
        try(socket;
            BufferedReader in= new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true)){

            out.println("OK CONNECTED");

            //1) Login
            String line = in.readLine();
            if(line==null) return;

            String[] parts = split(line);
            if(parts.length!=3 || !parts[0].equalsIgnoreCase("LOGIN")) {
                LOG.warning("Protocol error (LOGIN required) from " + socket.getRemoteSocketAddress());
                out.println("ERR AUTH LOGIN_REQUIRED");
                return;
            }

            User user = userService.authenticate(parts[1],parts[2]);
            if(user==null) {
                LOG.warning("Login failed for user=" + parts[1] +
                        " from " + socket.getRemoteSocketAddress());
                out.println("ERR AUTH INVALID_CREDENTIALS");
                return;
            }
            LOG.info("Login attempt: user=" + parts[1] + " from " + socket.getRemoteSocketAddress());
            out.println("OK AUTH");

            //2) Commands loop
            while((line = in.readLine())!=null) {
                line = line.trim();
                if(line.isEmpty()) continue;

                parts = split(line);
                String cmd = parts[0].toUpperCase();

                if(cmd.equals("QUIT")) {
                    out.println("OK BYE");
                    return;
                }

                switch(cmd) {
                    case "ENC" -> handleEncrypt(parts,user,out);
                    case "DEC" -> handleDecrypt(parts,user,out);
                    default -> {
                        LOG.warning("Unknown command '" + cmd+ "' from user="+user.getUsername());
                        out.println("ERR CMD UNKNOWN_COMMAND");
                    }
                }
            }
        } catch (Exception e){
            LOG.log(Level.SEVERE, "Client handler error", e);
        } finally {
            if(clients!=null) clients.remove(socket);
            if (activeClients != null) activeClients.decrementAndGet();
        }
    }
    private void handleEncrypt(String[] parts,User user,PrintWriter out){
        if(!user.has(Right.ENCRYPT)) {
            LOG.warning("Encrypt denied (no right) user=" + user.getUsername());
            out.println("ERR ACL NO_ENCRYPT_RIGHT");
            return;
        }
        if(parts.length!=2){
            out.println("ERR ENC BAD_FORMAT");
            return;
        }

        String card = parts[1].trim();
        if(!LuhnValidator.isValid(card)){
            LOG.warning("Invalid card '" + card + "' from user=" + user.getUsername());
            out.println("ERR ENC INVALID_CARD");
            return;
        }

        CardStore.CardRecord rec = store.getOrCreateRecord(card);
        int attempt = store.nextAttemptOrFail(rec);
        if(attempt<0){
            LOG.warning("Too many encrypt attempts for card " + mask(card));
            out.println("ERR ENC TOO_MANY_ATTEMPTS");
            return;
        }

        String crypt = CardCipher.encrypt(card,attempt,rec.baseShift);
        store.saveCryptogram(crypt,card,attempt,rec.baseShift);

        LOG.info("Encrypted card for user=" + user.getUsername() +
                " crypt=" + crypt);

        out.println("OK ENC "+crypt);
    }
    private void handleDecrypt(String[] parts,User user,PrintWriter out){
        if(!user.has(Right.DECRYPT)) {
            LOG.warning("Decrypt denied (no right) user=" + user.getUsername());
            out.println("ERR ACL NO_DECRYPT_RIGHT");
            return;
        }
        if(parts.length!=2){
            out.println("ERR DEC BAD_FORMAT");
            return;
        }
        String crypt = parts[1].trim();
        if(crypt.length()!=16){
            out.println("ERR DEC INVALID_CRYPTOGRAM");
            return;
        }

        CardStore.CryptInfo info = store.findByCryptogram(crypt);
        if(info==null){
            LOG.warning("Decrypt failed (not found) crypt=" + crypt +
                    " user=" + user.getUsername());
            out.println("ERR DEC NOT_FOUND");
            return;
        }

        String card = CardCipher.decrypt(crypt,info.attempts,info.baseShift);
        out.println("OK DECRYPT "+card);
    }

    private String[] split(String line){
        return line.trim().split("\\s+");
    }
    private static String mask(String card) {
        return "************" + card.substring(12);
    }
}
