package org.morganm.mobreducer.manager;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.morganm.mobreducer.Util;

import com.google.inject.assistedinject.Assisted;

/** Class to track meta information about an Entity and specifically to
 * be hash key/value safe even as entities are loaded/unloaded as chunks
 * come and go.
 * 
 */
public class EntityInfo {
    private final UUID uuid;
    private final Util util;
    // WeakReference serves as a cache to the entity object. Might be null if
    // the entity has been unloaded or removed.
    private WeakReference<Entity> entityRef;
    private String worldName;
    private long lastInteractEvent;
    private String currentChunkKey;       // our last known chunk position
//    private Location spawnLocation;
    private String spawnChunkKey;
    
    @Inject
    public EntityInfo(@Assisted Entity entity, Util util) {
        this.entityRef = new WeakReference<Entity>(entity);
        this.uuid = entity.getUniqueId();
        this.util = util;
        
        /* Though not documented explicitly one way or another, as best I can tell
         * an entity will always have a location when being created. It's possible
         * it could be null if the entity has been removed, but this constructor
         * is only called as entities are being created. So rather than write a
         * bunch of code here and elsewhere in this class to protect against it
         * possibly being null, I'll go with the assumption that it is never null
         * until I see an NPE that proves otherwise.
         */
        final Location l = entity.getLocation();
        this.worldName = l.getWorld().getName();
        this.currentChunkKey = util.getChunkKey(l.getChunk());
        this.spawnChunkKey = currentChunkKey;
        
        this.lastInteractEvent = System.currentTimeMillis();
    }
    
    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }
    
    /**
     * 
     * @return the entity this EntityInfo represents. Can be null if the backing entity
     * is currently unloaded (because the Chunk it is in has been unloaded by Bukkit).
     */
    public Entity getEntity() {
        Entity entity = entityRef.get();   // use WeakReference object if set
        
        if( entity == null ) {
            World world = getWorld();
            
            // possible world was deleted by MultiVerse since this entity was created
            if( world != null ) {
                List<Entity> entities = world.getEntities();
                for(Entity e : entities) {
                    if( e.getUniqueId().equals(uuid) ) {
                        entity = e;
                        entityRef = new WeakReference<Entity>(e);
                        break;
                    }
                }
            }
        }
        
        return entity;
    }
    
    public long getLastInteractEventTime() {
        return lastInteractEvent;
    }
    public void setLastInteractEventTime(long time) {
        this.lastInteractEvent = time;
    }
    
    public String getCurrentChunkKey() {
        Entity entity = getEntity();
        
        // make sure chunkKey and world is current
        if( entity != null ) {
            worldName = entity.getLocation().getWorld().getName();
            currentChunkKey = util.getChunkKey(entity.getLocation().getChunk());
        }
        
        return currentChunkKey;
    }
    public void setCurrentChunkKey(String chunkKey) {
        this.currentChunkKey = chunkKey;
    }
    
    public String getSpawnChunkKey() {
        return spawnChunkKey;
    }
}