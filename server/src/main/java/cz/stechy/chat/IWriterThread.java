package cz.stechy.chat;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;

/**
 * Rozhraní definující metody pro odeslání zprávi příjemci
 */
public interface IWriterThread extends IThreadControl {

    /**
     * Odešle zprávu
     *
     * @param writer {@link BufferedWriter} Writer, pomocí kterého se zpráva odešle
     * @param message Zpráva, která se má odeslat
     */
    void sendMessage(BufferedOutputStream writer, String message);

}
