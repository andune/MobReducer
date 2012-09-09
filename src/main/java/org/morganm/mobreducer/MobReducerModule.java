/**
 * 
 */
package org.morganm.mobreducer;

import org.bukkit.plugin.Plugin;
import org.morganm.mBukkitLib.Debug;
import org.morganm.mBukkitLib.Logger;
import org.morganm.mBukkitLib.LoggerImpl;
import org.morganm.mBukkitLib.PermissionSystem;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;

/** This module tells Guice how to wire together all dependencies
 * for the plugin.
 * 
 * @author morganm
 *
 */
public class MobReducerModule extends AbstractModule {
    private final Plugin plugin;
    private Config config;
    
    public MobReducerModule(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    protected void configure() {
        bind(Logger.class)
            .to(LoggerImpl.class)
            .in(Scopes.SINGLETON);
        bind(Debug.class)
            .in(Scopes.SINGLETON);
        bind(PermissionSystem.class)
            .in(Scopes.SINGLETON);
        bind(Util.class)
            .in(Scopes.SINGLETON);
    }

    @Provides
    protected Plugin providePlugin() {
        return plugin;
    }
    
    @Provides
    protected Config provideConfig() {
        if( config == null )
            config = new Config(plugin.getConfig().getRoot());
        return config;
    }
}
