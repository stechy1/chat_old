package cz.stechy.chat;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import cz.stechy.chat.plugins.IPlugin;
import cz.stechy.chat.plugins.Plugin;

public class PluginModule extends AbstractModule {

    @Override
    protected void configure() {
        Multibinder<IPlugin> pluginBinder = Multibinder.newSetBinder(binder(), IPlugin.class);

        for (Plugin plugin : Plugin.values()) {
            pluginBinder.addBinding().to(plugin.clazz).asEagerSingleton();
        }

        // TODO načíst externí pluginy
    }
}
