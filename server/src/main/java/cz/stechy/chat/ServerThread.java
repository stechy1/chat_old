package cz.stechy.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vlákno serveru
 */
class ServerThread extends Thread implements IServerThread {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerThread.class);

    // Číslo portu
    private final int port;
    // Maximální počet klientů
    private final int maxClients;
    // Threadpool s vlákny pro jednotlivé klienty
    private final Executor pool;

    // Indikátor, zda-li vlákno běží, nebo ne
    private boolean running = false;

    /**
     * Vytvoří novou instanci vlákna serveru
     *
     * @param port Číslo portu
     * @param maxClients Maximální počet připojených klientů
     * @param waitingQueueSize Velikost čekací fronty
     * @throws IOException Pokud se nepodaří vlákno vytvořit
     */
    ServerThread(int port, int maxClients, int waitingQueueSize) throws IOException {
        super("ServerThread");
        this.port = port;
        this.maxClients = maxClients;
        pool = Executors.newFixedThreadPool(maxClients);
    }

    @Override
    public void shutdown() {
        running = false;
    }

    @Override
    public synchronized void start() {
        running = true;
        super.start();
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Každých 5 vteřin dojde k vyjímce SocketTimeoutException
            // To proto, že metoda serverSocket.accept() je blokující
            // a my bychom neměli šanci činnost vlákna ukončit
            serverSocket.setSoTimeout(5000);
            LOGGER.info(String.format("Server naslouchá na portu: %d.", serverSocket.getLocalPort()));
            // Nové vlákno serveru
            while (running) {
                try {
                    final Socket socket = serverSocket.accept();
                    LOGGER.info("Server přijal nové spojení.");

                    // TODO zpracovat nové spojení
                } catch (SocketTimeoutException ignored) {}
            }

        } catch (IOException e) {
            LOGGER.error("Chyba v server socketu.", e);
        }
    }
}
