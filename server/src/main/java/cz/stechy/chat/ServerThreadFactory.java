package cz.stechy.chat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import cz.stechy.chat.cmd.CmdParser;
import cz.stechy.chat.cmd.IParameterProvider;
import java.io.IOException;

/**
 * Továrna serverového vlákna
 */
@Singleton
class ServerThreadFactory implements IServerThreadFactory {

    // Výchozí hodnota portu
    private static final int DEFAULT_SERVER_PORT = 15378;
    // Výchozí maximální počet klientů
    private static final int DEFAULT_MAX_CLIENTS = 1;
    // Výchozí velikost čekací fronty
    private static final int DEFAULT_WAITING_QUEUE_SIZE = 1;

    private final IClientDispatcherFactory clientDispatcherFactory;

    @Inject
    ServerThreadFactory(IClientDispatcherFactory clientDispatcherFactory) {
        this.clientDispatcherFactory = clientDispatcherFactory;
    }

    @Override
    public IServerThread getServerThread(IParameterProvider parameters) throws IOException {
        final int port = parameters.getInteger(CmdParser.PORT, DEFAULT_SERVER_PORT);
        final int maxClients = parameters.getInteger(CmdParser.CLIENTS, DEFAULT_MAX_CLIENTS);
        final int waitingQueueSize = parameters.getInteger(CmdParser.MAX_WAITING_QUEUE, DEFAULT_WAITING_QUEUE_SIZE);

        return new ServerThread(port, maxClients, clientDispatcherFactory.getClientDispatcher(waitingQueueSize));
    }
}
