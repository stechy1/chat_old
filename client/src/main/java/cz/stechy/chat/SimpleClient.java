package cz.stechy.chat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleClient {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClient.class);

    public static void main(String[] args) throws Exception{
        LOGGER.info("Spouštím klienta.");
        Socket socket = new Socket("localhost", 15378);
        LOGGER.info("Bylo navázané spojení.");
        Thread.sleep(1000);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        LOGGER.info("Odesílám zprávu.");
        writer.write("Hello from client.\n");
        writer.flush();
        LOGGER.info("Čtu zprávu.");
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        LOGGER.info(reader.readLine());
        LOGGER.info("Ukončuji spojení.");
        socket.close();
        LOGGER.info("Spojení bylo ukončeno. Klient končí.");
    }

}
