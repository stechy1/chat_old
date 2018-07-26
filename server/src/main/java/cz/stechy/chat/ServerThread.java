package cz.stechy.chat;

import cz.stechy.chat.net.message.IMessage;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vlákno serveru
 */
class ServerThread extends Thread implements IServerThread {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerThread.class);

    // Seznam klientů, se kterými server aktivně komunikuje
    private final List<Client> clients = new ArrayList<>();
    private final IWriterThread writerThread = new WriterThread();
    // Číslo portu
    private final int port;
    // Maximální počet klientů
    private final int maxClients;
    // Threadpool s vlákny pro jednotlivé klienty
    private final ExecutorService pool;
    // Client dispatcher
    private final IClientDispatcher clientDispatcher;

    // Indikátor, zda-li vlákno běží, nebo ne
    private boolean running = false;

    /**
     * Vytvoří novou instanci vlákna serveru
     *
     * @param port Číslo portu
     * @param maxClients Maximální počet připojených klientů
     * @param clientDispatcher {@link IClientDispatcher} starající se o klienty v čekací frontě
     * @throws IOException Pokud se nepodaří vlákno vytvořit
     */
    ServerThread(int port, int maxClients, IClientDispatcher clientDispatcher) throws IOException {
        super("ServerThread");
        this.port = port;
        this.maxClients = maxClients;
        this.clientDispatcher = clientDispatcher;
        pool = Executors.newFixedThreadPool(maxClients);
    }

    private synchronized void insertClientToListOrQueue(Client client) {
        if (clients.size() < maxClients) {
            clients.add(client);
            client.setConnectionClosedListener(() -> {
                clients.remove(client);
                LOGGER.info("Počet připojených klientů: {}.", clients.size());
                if (clientDispatcher.hasClientInQueue()) {
                    LOGGER.info("V čekací listině se našel klient, který by rád komunikoval.");
                    this.insertClientToListOrQueue(clientDispatcher.getClientFromQueue());
                }
            });
            pool.submit(client);
        } else {
            if (clientDispatcher.addClientToQueue(client)) {
                LOGGER.info("Přidávám klienta na čekací listinu.");
            } else {
                LOGGER.warn("Odpojuji klienta od serveru. Je připojeno příliš mnoho uživatelů.");
                client.close();
            }
        }
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
        clientDispatcher.start();
        writerThread.start();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            // Každých 5 vteřin dojde k vyjímce SocketTimeoutException
            // To proto, že metoda serverSocket.accept() je blokující
            // a my bychom neměli šanci činnost vlákna ukončit
            serverSocket.setSoTimeout(5000);
            LOGGER
                .info(String.format("Server naslouchá na portu: %d.", serverSocket.getLocalPort()));
            // Nové vlákno serveru
            while (running) {
                try {
                    final Socket socket = serverSocket.accept();
                    LOGGER.info("Server přijal nové spojení.");

                    final Client client = new Client(socket, writerThread, this::receiveListener);
                    insertClientToListOrQueue(client);
                } catch (SocketTimeoutException ignored) {
                }
            }

        } catch (IOException e) {
            LOGGER.error("Chyba v server socketu.", e);
        }

        LOGGER.info("Ukončuji client dispatcher.");
        clientDispatcher.shutdown();
        try {
            clientDispatcher.join();
        } catch (InterruptedException ignored) {}

        LOGGER.info("Ukončuji writer thread.");
        writerThread.shutdown();
        try {
            writerThread.join();
        } catch (InterruptedException ignored) {}
    }

    private void receiveListener(IMessage message, Client client) {
        switch (message.getType()) {
            case HELLO:
                client.sendMessage(message);
                break;
            default:
                System.out.println("Byla přijata zpráva neznámého typu: " + message.getType());
        }
    }
}
