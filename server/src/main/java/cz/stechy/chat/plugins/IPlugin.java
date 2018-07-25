package cz.stechy.chat.plugins;

/**
 * Rozhraní definující plugin
 */
public interface IPlugin {

    /**
     * Vrátí název pluginu
     *
     * @return Název pluginu
     */
    String getName();

    /**
     * Inicializace pluginu
     * Zde by se měl plugin inicializovat, ne v konstruktoru
     */
    void init();

}
