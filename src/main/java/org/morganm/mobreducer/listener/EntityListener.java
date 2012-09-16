/**
 * 
 */
package org.morganm.mobreducer.listener;

import javax.inject.Inject;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.morganm.mBukkitLib.Logger;
import org.morganm.mobreducer.manager.MobManager;

/** Class to listen for Bukkit entity events and record interesting details
 * or respond to events as appropriate.
 * 
 * @author morganm
 *
 */
public class EntityListener implements Listener {
    private final Logger log;
    private final MobManager manager;
    
    @Inject
    public EntityListener(MobManager manager, Logger log) {
        this.manager = manager;
        this.log = log;
    }
    
    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if( !manager.canSpawn(entity) ) {
            event.setCancelled(true);
        }
        else {
            manager.updatePosition(entity);
        }
        
        // spawning is considered an interaction. This makes sure that entities being
        // loaded due to ChunkLoad don't get despawned immediately at the next idle
        // check because maybe their last update time was from when the Chunk was
        // last loaded (possibly a long time ago).
        manager.interact(entity);
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        manager.interact(event.getEntity());
        log.info("EntityDamageByEntityEvent: cause=",event.getCause(),", damager=",event.getDamager());
        if( event.getDamager() instanceof Arrow ) { 
            Arrow arrow = (Arrow) event.getDamager();
            log.info("EntityDamageByEntityEvent: shooter=",arrow.getShooter());
        }
    }
    
    @EventHandler
    public void onPotionSplashEent(PotionSplashEvent event) {
        manager.interact(event.getEntity());
        log.info("onPotionSplashEent: shooter=",event.getPotion().getShooter());
    }
    
//    @EventHandler
//  public void onEntityDamage(EntityDamageEvent event) {
//        log.info("EntityDamageEvent: cause=",event.getCause());
//  }
    
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        final Entity target = event.getTarget();
        
        // if the entity targeted a player, update it's interaction time
        if( target != null && target.getType() == EntityType.PLAYER ) {
            manager.interact(event.getEntity());
        }
    }
}
