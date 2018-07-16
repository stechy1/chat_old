package cz.stechy.chat;

interface IServerThread extends IThreadControl {

    void join() throws InterruptedException;

}
