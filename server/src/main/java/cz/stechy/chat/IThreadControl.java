package cz.stechy.chat;

/**
 * Rozhraní pro ovládání vlákna
 */
public interface IThreadControl {

    /**
     * Spustí vlákno
     */
    void start();

    /**
     * Spustí ukončovací sekvenci vlákna
     */
    void shutdown();

}
