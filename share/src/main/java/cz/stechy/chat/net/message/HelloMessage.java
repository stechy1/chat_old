package cz.stechy.chat.net.message;

public class HelloMessage implements IMessage {

    private final String data;

    public HelloMessage(String data) {
        this.data = data;
    }

    @Override
    public MessageType getType() {
        return MessageType.HELLO;
    }

    @Override
    public Object getData() {
        return data;
    }
}
