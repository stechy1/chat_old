package cz.stechy.chat.plugins;

public enum Plugin {
    ;

    public Class<? extends IPlugin> clazz;

    Plugin(Class<? extends IPlugin> clazz) {
        this.clazz = clazz;
    }
}
