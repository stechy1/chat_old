package cz.stechy.chat;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import cz.stechy.chat.cmd.IParameterFactory;
import cz.stechy.chat.cmd.IParameterProvider;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private final IParameterFactory parameterFactory;
    private final IServerThreadFactory serverThreadFactory;

    @Inject
    public Server(IParameterFactory parameterFactory, IServerThreadFactory serverThreadFactory) {
        this.parameterFactory = parameterFactory;
        this.serverThreadFactory = serverThreadFactory;
    }

    private void run(String[] args) throws IOException {
        final IParameterProvider parameters = parameterFactory.getParameters(args);
        final IServerThread serverThread = serverThreadFactory.getServerThread(parameters);

        LOGGER.info("Spouštím server...");

        // Spuštění vlákna serveru
        serverThread.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        LOGGER.info("Ukončuji server.");
        // Počkání na ukončení
        try {
            serverThread.join();
        } catch (InterruptedException ignored) {}

        LOGGER.info("Server byl ukončen.");
    }

    public static void main(String[] args) throws Exception {
        final Injector injector = Guice.createInjector(new ServerModule(), new PluginModule());
        final Server server = injector.getInstance(Server.class);
        server.run(args);
    }

}
