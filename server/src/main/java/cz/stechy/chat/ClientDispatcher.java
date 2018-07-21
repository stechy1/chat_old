package cz.stechy.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientDispatcher extends Thread implements IClientDispatcher {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientDispatcher.class);

    private static final int SLEEP_TIME = 5000;

    private boolean interupt = false;
    private final Semaphore semaphore = new Semaphore(0);
    private final Queue<Client> waitingQueue = new ConcurrentLinkedQueue<>();
    private final Collection<Client> clientsToRemove = new ArrayList<>();
    private final int waitingQueueSize;

    /**
     * Vytvoří novou instanci třídy {@link ClientDispatcher}
     *
     * @param waitingQueueSize Velikost čekací fronty
     */
    public ClientDispatcher(int waitingQueueSize) {
        this.waitingQueueSize = waitingQueueSize;
    }

    @Override
    public void run() {
        LOGGER.info("Spouštím client dispatchera.");
        while(!interupt) {
            while(waitingQueue.isEmpty() && !interupt) {
                try {
                    LOGGER.info("Jdu spát na semaforu.");
                    semaphore.acquire();
                } catch (InterruptedException ignored) {}
            }

            if (interupt) {
                LOGGER.info("Přidávám všechny klienty na seznam pro ukončení spojení.");
                clientsToRemove.addAll(waitingQueue);
            } else {
                LOGGER.info("Posílám zprávu všem klientům.");
                final int count = waitingQueue.size();
                waitingQueue.forEach(client -> {
                    try {
                        client.writer.write(("count: " + count + "\n").getBytes());
                        client.writer.flush();
                    } catch (IOException e) {
                        LOGGER.info("Klient neudržel spojení, musím se ho zbavit.");
                        clientsToRemove.add(client);
                    }
                });
            }

            LOGGER.info("Zbavuji se všech klientů, kteří neudrželi spojení, nebo bylo potřeba spojení s nimi ukončit.");
            waitingQueue.removeAll(clientsToRemove);
            for (Client client : clientsToRemove) {
                client.close();
            }
            clientsToRemove.clear();

            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException ignored) {}
        }

        LOGGER.info("Client dispatcher končí.");
    }

    @Override
    public boolean hasClientInQueue() {
        return !waitingQueue.isEmpty();
    }

    @Override
    public Client getClientFromQueue() {
        return waitingQueue.poll();
    }

    @Override
    public boolean addClientToQueue(Client client) {
        if (waitingQueue.size() < waitingQueueSize) {
            waitingQueue.add(client);
            semaphore.release();
            return true;
        }

        return false;
    }

    @Override
    public void shutdown() {
        interupt = true;
        semaphore.release();
    }
}
