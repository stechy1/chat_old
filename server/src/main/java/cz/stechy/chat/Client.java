package cz.stechy.chat;

import cz.stechy.chat.net.message.IMessage;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private final IMessageReceiveListener messageReceiveListener;
    final ObjectOutputStream writer;

    private ConnectionClosedListener connectionClosedListener;

    Client(Socket socket, IWriterThread writerThread,
        IMessageReceiveListener messageReceiveListener) throws IOException {
        this.socket = socket;
        this.writerThread = writerThread;
        inputStream = socket.getInputStream();
        writer = new ObjectOutputStream(socket.getOutputStream());
        this.messageReceiveListener = messageReceiveListener;
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
    public void sendMessage(IMessage message) {
        writerThread.sendMessage(writer, message);
    }

    @Override
    public void run() {
        LOGGER.info("Spouštím nekonečnou smyčku pro komunikaci s klientem.");
        try (ObjectInputStream reader = new ObjectInputStream(inputStream)) {
            LOGGER.info("InputStream byl úspěšně vytvořen.");
            IMessage received;
            while ((received = (IMessage) reader.readObject()) != null) {
                LOGGER.info(String.format("Bylo přijato: '%s'", received));
                sendMessage(received);
                messageReceiveListener.onMessageReceived(received, this);
            }
        } catch (EOFException |SocketException e) {
            LOGGER.info("Klient ukončil spojení.");
        } catch (IOException e) {
            LOGGER.warn("Nastala neočekávaná vyjímka.", e);
        } catch (ClassNotFoundException e) {
            // Nikdy by nemělo nastat
            LOGGER.error("Nebyla nalezena třída.", e);
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

    /**
     * Rozhraní obsahující metodu, která se zavolá vždy, když klient pošle zprávu
     */
    @FunctionalInterface
    interface IMessageReceiveListener {

        /**
         * Metoda se zavolá pokaždé, když server obdrží zprávu od klienta
         *
         * @param message {@link IMessage} Zpráva
         * @param client {@link Client} Klient, který zprávu poslal
         */
        void onMessageReceived(IMessage message, Client client);
    }
}
