/**
 * 
 */
package org.morganm.mobreducer;

import javax.inject.Inject;

import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.mBukkitLib.Debug;
import org.morganm.mBukkitLib.JarUtils;
import org.morganm.mBukkitLib.Logger;
import org.morganm.mBukkitLib.PermissionSystem;
import org.morganm.mobreducer.listener.EntityListener;
import org.morganm.mobreducer.manager.MobManager;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author morganm
 *
 */
public class MobReducer extends JavaPlugin {
    private final static int TICKS_ONE_MINUTE = 1200;   // how many ticks in a minute (20 * 60)
    
    // these dependencies are auto-injected by Guice
    private MobManager mobManager;
    private Debug debug;
    private Logger log;
    private PermissionSystem permSystem;
    private Config config;
    private EntityListener entityListener;
    
    private int buildNumber = -1;
    private boolean enableAborted = false;
    
	@Override
	public void onEnable() {
	    enableAborted = false;
        JarUtils jarUtil = new JarUtils(this, getLogger(), getFile());
        buildNumber = jarUtil.getBuildNumber();
        
        saveDefaultConfig();    // copy default config.yml into place if needed
        
        // build object graph using Guice dependency injection. This injects
        // all dependencies for us using the @Inject annotations
        Injector injector = Guice.createInjector(new MobReducerModule(this));
        injector.injectMembers(this);
        
        getConfig();    // make sure reloadConfig() is run
        if( enableAborted ) {
            log.severe("version "+getDescription().getVersion()+", build "+buildNumber+" enable aborted");
            return;
        }
        
        debug.setLogFileName("plugins/MobReducer/debug.log");
        debug.setDebug(getConfig().getBoolean("debug", false));
        debug.debug("DEBUG ENABLED");   // prints only if debug is enabled
        
        int ticks = TICKS_ONE_MINUTE;
        if( debug.isDebug() )
            ticks /= 4;         // run scheduled tasks more often when debugging
        
        getServer().getScheduler().scheduleSyncRepeatingTask(this, mobManager, ticks, ticks);
        permSystem.setupPermissions();
        getServer().getPluginManager().registerEvents(entityListener, this);
        
        log.info("version "+getDescription().getVersion()+", build "+buildNumber+" is enabled");
	}
	
	@Override
	public void reloadConfig() {
	    super.reloadConfig();
	    
	    if( config != null ) {
	        config.setSection(getConfig().getRoot());
	        if( !config.validate() ) {
	            log.severe("Config file validation failed, plugin shutting down");
	            
                enableAborted = true;   // abort enablement if we're just now being enabled
                
                // shutdown plugin if we were already enabled and we were
                // just reloaded with a bogus config
	            if( isEnabled() )
	                getServer().getPluginManager().disablePlugin(this);
	        }
	    }
	}
	
	@Override
	public void onDisable() {
        log.info("version "+getDescription().getVersion()+", build "+buildNumber+" is disabled");
	}
	
	@Inject
	public void setMobManager(MobManager mobManager) {
	    this.mobManager = mobManager;
	}
	
    @Inject
	public void setDebug(Debug debug) {
	    this.debug = debug;
	}
    
    @Inject
    public void setLogger(Logger log) {
        this.log = log;
    }
    
    @Inject
    public void setPermissionSystem(PermissionSystem permSystem) {
        this.permSystem = permSystem;
    }
    
    @Inject
    public void setConfig(Config config) {
        this.config = config;
    }
    
    @Inject
    public void setEntityListener(EntityListener entityListener) {
        this.entityListener = entityListener;
    }
}
