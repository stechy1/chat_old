package cz.stechy.chat.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloPlugin implements IPlugin {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloPlugin.class);

    @Override
    public String getName() {
        return "HelloPlugin";
    }

    @Override
    public void init() {
        LOGGER.info("Inicializace pluginu: {}", getName());
    }
}
