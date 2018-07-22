package cz.stechy.chat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Třída reprezentuje připojeného klienta a zprostředkovává komunikaci s klientem
 */
class Client implements Runnable {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final Socket socket;
    private final InputStream inputStream;
    private final IWriterThread writerThread;
    final BufferedOutputStream writer;

    private ConnectionClosedListener connectionClosedListener;

    Client(Socket socket, IWriterThread writerThread) throws IOException {
        this.socket = socket;
        this.writerThread = writerThread;
        inputStream = socket.getInputStream();
        writer = new BufferedOutputStream(socket.getOutputStream());
        LOGGER.info("Byl vytvořen nový klient.");
    }

    /**
     * Uzavře spojení s klientem
     */
    void close() {
        try {
            LOGGER.info("Uzavírám socket.");
            socket.close();
            LOGGER.info("Socket byl úspěšně uzavřen.");
        } catch (IOException e) {
            LOGGER.error("Socket se nepodařilo uzavřít!", e);
        }
    }

    /**
     * Odešle klientovi zprávu
     *
     * @param message Zpráva, která se má odeslat
     */
    public void sendMessage(String message) {
        writerThread.sendMessage(writer, message);
    }

    @Override
    public void run() {
        LOGGER.info("Spouštím nekonečnou smyčku pro komunikaci s klientem.");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            LOGGER.info("InputStream byl úspěšně vytvořen.");
            String received;
            while ((received = reader.readLine()) != null) {
                LOGGER.info(String.format("Bylo přijato: '%s'", received));
                sendMessage(received);
            }
        } catch (EOFException |SocketException e) {
            LOGGER.info("Klient ukončil spojení.");
        } catch (IOException e) {
            LOGGER.warn("Nastala neočekávaná vyjímka.", e);
        } catch (Exception e) {
            LOGGER.error("Neznámá chyba.", e);
        } finally {
            LOGGER.info("Volám connectionClosedListener.");
            if (connectionClosedListener != null) {
                connectionClosedListener.onConnectionClosed();
            }
            close();
        }
    }

    /**
     * Nastaví listener na ztrátu spojení s klientem
     *
     * @param connectionClosedListener {@link ConnectionClosedListener}
     */
    public void setConnectionClosedListener(ConnectionClosedListener connectionClosedListener) {
        this.connectionClosedListener = connectionClosedListener;
    }

    /**
     * Rozhraní obsahující metodu, která se zavolá v případě, že se ukončí spojení s klientem
     */
    @FunctionalInterface
    interface ConnectionClosedListener {

        /**
         * Metoda se zavolá v případě, že se ukončí spojení s klientem
         */
        void onConnectionClosed();
    }
}
