import Utils.ClientHandler;
import Utils.CommandDispatcher;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT        = 9090;
    private static final int THREAD_POOL = 20;

    public static void main(String[] args) {

        CommandDispatcher dispatcher = new CommandDispatcher();
        ExecutorService   pool       = Executors.newFixedThreadPool(THREAD_POOL);


        System.out.println("    JSON-SOCKET SERVER ");
        System.out.println("  Port     : " + PORT );
        System.out.println("  Threads  : " + THREAD_POOL );

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("[SERVER] En attente de connexions...\n");

            // Shutdown hook : ferme proprement le pool sur Ctrl+C
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[SERVER] Arrêt en cours...");
                pool.shutdown();
                System.out.println("[SERVER] Arrêté.");
            }));

            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket, dispatcher));
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Erreur fatale : " + e.getMessage());
        }
    }
}