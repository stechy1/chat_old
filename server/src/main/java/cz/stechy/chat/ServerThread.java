package cz.stechy.chat;

import java.io.IOException;
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
        // TODO v budoucnu implementovat
    }

    @Override
    public void run() {
        // TODO nekonečná smyčka serverového vlákna
        LOGGER.info("Spuštěno vlákno serveru");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("Vlákno serveru bylo ukončeno");
    }
}
