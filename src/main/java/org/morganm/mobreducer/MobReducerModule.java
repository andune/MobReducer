/**
 * 
 */
package org.morganm.mobreducer;

import javax.inject.Inject;

import org.bukkit.plugin.Plugin;
import org.morganm.mBukkitLib.Debug;
import org.morganm.mBukkitLib.Logger;
import org.morganm.mBukkitLib.LoggerImpl;
import org.morganm.mBukkitLib.PermissionSystem;
import org.morganm.mobreducer.manager.ChunkInfo;
import org.morganm.mobreducer.manager.ChunkInfoFactory;
import org.morganm.mobreducer.manager.EntityInfo;
import org.morganm.mobreducer.manager.EntityInfoFactory;
import org.morganm.mobreducer.manager.MobManager;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/** This module tells Guice how to wire together all dependencies
 * for the plugin.
 * 
 * @author morganm
 *
 */
public class MobReducerModule extends AbstractModule {
    private final Plugin plugin;
    
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
        bind(Config.class)
            .toProvider(ConfigProvider.class)
            .in(Scopes.SINGLETON);
        bind(MobManager.class)
            .in(Scopes.SINGLETON);
        
        install(new FactoryModuleBuilder()
            .implement(ChunkInfo.class, ChunkInfo.class)
            .build(ChunkInfoFactory.class)
        );
        install(new FactoryModuleBuilder()
            .implement(EntityInfo.class, EntityInfo.class)
            .build(EntityInfoFactory.class)
        );
    }

    @Provides
    protected Plugin providePlugin() {
        return plugin;
    }
    
    public static class ConfigProvider implements Provider<Config> {
        private static Config config;
        private final Logger log;
        private final Plugin plugin;
        
        @Inject
        public ConfigProvider(Plugin plugin, Logger log) {
            this.plugin = plugin;
            this.log = log;
        }

        public Config get() {
            if( config == null )
                config = new Config(plugin.getConfig().getRoot(), log);
            return config;
        }
    }
}
