package Utils;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket           socket;
    private final CommandDispatcher dispatcher;

    public ClientHandler(Socket socket, CommandDispatcher dispatcher) {
        this.socket     = socket;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        String clientAddr = socket.getRemoteSocketAddress().toString();
        System.out.println("[SERVER] Client connecté : " + clientAddr);

        try (
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream(),  "UTF-8"));
                PrintWriter    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)
        ) {
            String line;
            // Lire ligne par ligne — chaque ligne = une commande JSON complète
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                System.out.println("[SERVER] Reçu de " + clientAddr + " : " + line);

                String response = dispatcher.dispatch(line);

                System.out.println("[SERVER] Réponse → " + clientAddr + " : " + response);
                out.println(response);  // println ajoute '\n' → le client sait que la réponse est complète
            }

        } catch (IOException e) {
            System.out.println("[SERVER] Client déconnecté : " + clientAddr + " (" + e.getMessage() + ")");
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }

        System.out.println("[SERVER] Thread terminé pour " + clientAddr);
    }
}