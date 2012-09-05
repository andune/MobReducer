/**
 * 
 */
package org.morganm.mobreducer;

import java.io.File;

import javax.inject.Inject;

import org.bukkit.plugin.java.JavaPlugin;
import org.morganm.mBukkitLib.Debug;
import org.morganm.mBukkitLib.JarUtils;
import org.morganm.mBukkitLib.Logger;
import org.morganm.mBukkitLib.PermissionSystem;

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
    
    private int buildNumber = -1;
    
	@Override
	public void onEnable() {
        JarUtils jarUtil = new JarUtils(this, getLogger(), getFile());
        // copy default config.yml into place if needed
        jarUtil.copyConfigFromJar("config.yml", new File(getDataFolder(), "config.yml"));
        buildNumber = jarUtil.getBuildNumber();
        
        // build object graph using Guice dependency injection. This injects
        // all dependencies for us using the @Inject annotations
        Injector injector = Guice.createInjector(new MobReducerModule(this));
        injector.injectMembers(this);
        
		getServer().getScheduler().scheduleSyncRepeatingTask(this, mobManager, TICKS_ONE_MINUTE, TICKS_ONE_MINUTE);
        debug.setLogFileName("plugins/MobReducer/debug.log");
        debug.setDebug(getConfig().getBoolean("debug", false));
        
        permSystem.setupPermissions();
        
        getServer().getPluginManager().registerEvents(mobManager, this);
        
        log.info("version "+getDescription().getVersion()+", build "+buildNumber+" is enabled");
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
}
