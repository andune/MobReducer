/**
 * 
 */
package org.morganm.mobreducer.listener;

import javax.inject.Inject;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.morganm.mBukkitLib.Logger;
import org.morganm.mobreducer.Config;
import org.morganm.mobreducer.manager.EntityInfo;
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
    private final Config config;
    
    @Inject
    public EntityListener(MobManager manager, Logger log, Config config) {
        this.manager = manager;
        this.log = log;
        this.config = config;
    }
    
    @EventHandler(ignoreCancelled=true)
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
    
    @EventHandler(ignoreCancelled=true)
    public void onEntityDeath(EntityDeathEvent event) {
        if( event.getEntity() instanceof Player )
            return;
        
        EntityInfo info = manager.getEntityInfo(event.getEntity());
        
        // if not damaged by a player, don't drop anything
        if( config.isPlayerDamageRequiredForDrops() && !info.isPlayerDamaged() ) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if( event.getEntity() instanceof Player )   // do nothing if damaged entity is a player
            return;
        
        manager.interact(event.getEntity());
        
        log.info("EntityDamageByEntityEvent: cause=",event.getCause(),", damager=",event.getDamager());
        boolean isPlayerDamage = false;
        
        if( event.getDamager() instanceof Player ) {
            isPlayerDamage = true;
        }
        else if( event.getDamager() instanceof Arrow ) { 
            Arrow arrow = (Arrow) event.getDamager();
            log.info("EntityDamageByEntityEvent: shooter=",arrow.getShooter());
            if( arrow.getShooter() instanceof Player )
                isPlayerDamage = true;
        }
        
        if( isPlayerDamage )
            manager.playerDamage(event.getEntity());
    }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onPotionSplashEent(PotionSplashEvent event) {
        if( event.getEntity() instanceof Player )   // do nothing if damaged entity is a player
            return;
        
        manager.interact(event.getEntity());
        
        ThrownPotion potion = event.getPotion();
        if( potion != null ) {
            log.info("onPotionSplashEent: shooter=",potion.getShooter());
            if( potion.getShooter() instanceof Player ) {
                manager.playerDamage(event.getEntity());
            }
        }
    }
    
//    @EventHandler
//  public void onEntityDamage(EntityDamageEvent event) {
//        log.info("EntityDamageEvent: cause=",event.getCause());
//  }
    
    @EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
    public void onEntityTarget(EntityTargetEvent event) {
        if( event.getEntity() instanceof Player )   // do nothing if targeting entity is a player
            return;
        
        final Entity target = event.getTarget();
        
        // if the entity targeted a player, update it's interaction time
        if( target != null && target.getType() == EntityType.PLAYER ) {
            manager.interact(event.getEntity());
        }
    }
}
