/**
 * 
 */
package org.morganm.mobreducer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.morganm.mBukkitLib.Logger;

/** Class that manages active mobs on the server, tracking information
 * about them for use by the rest of the plugin.
 * 
 * @author morganm
 *
 */
public class MobManager implements Listener, Runnable {
	private final HashMap<String, ChunkInfo> chunks = new HashMap<String, ChunkInfo>(100);
	private final HashMap<UUID, EntityInfo> entities = new HashMap<UUID, EntityInfo>(500);
	private final Logger log;
	private final Util util;
	private final Config config;
	private int entitySpawnCounter=0;  // debug counter
	
	@Inject
	public MobManager(Logger log, Util util, Config config) {
	    this.log = log;
	    this.util = util;
	    this.config = config;
	}

	@EventHandler
	public void onEntitySpawn(CreatureSpawnEvent event) {
		Entity entity = event.getEntity();
		if( !canSpawn(entity) ) {
		    event.setCancelled(true);
		}
		else {
		    updatePosition(entity);
		}
		
		// spawning is considered an interaction. This makes sure that entities being
		// loaded due to ChunkLoad don't get despawned immediately at the next idle
		// check because maybe their last update time was from when the Chunk was
		// last loaded (possibly a long time ago).
        interact(entity);
	}
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
	    interact(event.getEntity());
	    log.info("EntityDamageByEntityEvent: cause=",event.getCause(),", damager=",event.getDamager());
	    if( event.getDamager() instanceof Arrow ) { 
	        Arrow arrow = (Arrow) event.getDamager();
	        log.info("EntityDamageByEntityEvent: shooter=",arrow.getShooter());
	    }
	}
    @EventHandler
    public void onPotionSplashEent(PotionSplashEvent event) {
        interact(event.getEntity());
        log.info("onPotionSplashEent: shooter=",event.getPotion().getShooter());
    }
//    @EventHandler
//	public void onEntityDamage(EntityDamageEvent event) {
//        log.info("EntityDamageEvent: cause=",event.getCause());
//	}
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
	    final Entity target = event.getTarget();
	    
	    // if the entity targeted a player, update it's interaction time
	    if( target != null && target.getType() == EntityType.PLAYER ) {
	        interact(event.getEntity());
	    }
	}
	
	/** Method to determine whether we will allow a given entity to spawn
	 * or not.
	 * 
	 * @param entity
	 * @return
	 */
	private boolean canSpawn(Entity entity) {
	    // curently only animals are limited
	    if( util.isAnimal(entity) ) {
	        Location l = entity.getLocation();
	        if( l != null ) {
	            final int chunkX = l.getChunk().getX();
	            final int chunkZ = l.getChunk().getZ();
	            final int segmentSize = config.getAnimalChunkSegmentSize();
	            final int maxPerSegment = config.getAnimalMaxPerSegment();

	            int count = 0;
	            // iterate through all chunks in the segment and count mobs
	            // to be sure we can spawn this animal
	            for(int x=chunkX-segmentSize; x <= chunkX+segmentSize; x++) {
	                for(int z=chunkZ-segmentSize; z <= chunkZ+segmentSize; z++) {
	                    ChunkInfo chunkInfo = getChunkInfo(l.getChunk());
	                    count += chunkInfo.getAnimals().size();
	                    
	                    // don't loop any longer than we have to, if we're over the
	                    // limit, stop looping and return the status
	                    if( count > maxPerSegment ) {
	                        log.debug("refusing entity spawn due to chunk size limits for entity ", entity);
	                        return false;
	                    }
	                }
	            }
	        }
	    }
	    
	    entitySpawnCounter++;
	    return true;
	}
	
	/** To be called when an entity has an "interaction" that keeps it active. Being
	 * active means it won't be purged.
	 * 
	 * @param entity
	 */
	public void interact(final Entity entity) {
	    EntityInfo entityInfo = getEntityInfo(entity);
	    entityInfo.lastInteractEvent = System.currentTimeMillis();
	}
	
	private String getChunkKey(Chunk chunk) {
	    return util.getChunkKey(chunk);
	}
	
	/** Return ChunkInfo object. Will create one if one doesn't already exist.
	 * 
	 * @param currentChunkKey
	 * @return
	 */
	private ChunkInfo getChunkInfo(final Chunk chunk) {
	    final String chunkKey = getChunkKey(chunk);
	    ChunkInfo chunkInfo = chunks.get(chunkKey);
	    if( chunkInfo == null ) {
	        chunkInfo = new ChunkInfo(chunk);
	        chunks.put(chunkKey, chunkInfo);
	    }
	    return chunkInfo;
	}
	
	private EntityInfo getEntityInfo(final Entity entity) {
	    EntityInfo entityInfo = entities.get(entity.getUniqueId());
	    if( entityInfo == null ) {
	        entityInfo = new EntityInfo(entity);
	        entities.put(entity.getUniqueId(), entityInfo);
	    }
	    return entityInfo;
	}
	
	private void updatePosition(Entity entity) {
	    EntityInfo entityInfo = getEntityInfo(entity);
	    
	    String currentChunkKey = null;
	    Location currentLocation = entity.getLocation();
	    if( currentLocation != null )
	        currentChunkKey = getChunkKey(currentLocation.getChunk());
	    
	    // if position has changed, delete old position
	    if( currentChunkKey == null ||
	            (entityInfo.currentChunkKey != null && !entityInfo.currentChunkKey.equals(currentChunkKey)) ) {
	        // remove 
//	        ChunkInfo chunkInfo = chunks.get(entityInfo.currentChunkKey);
//	        if( chunkInfo != null ) {
//	            chunkInfo.entities.remove(entity);
//	        }
	    }
	    
	    // if there is a current position, update new position
	    if( currentChunkKey != null ) {
	        // update entity->chunk mapping
            entityInfo.currentChunkKey = currentChunkKey;
            
            // update chunk->entities set
//	        ChunkInfo chunkInfo = getChunkInfo(currentLocation.getChunk());
//	        chunkInfo.entities.add(entity);
	    }
	}
	
	private void cleanupEntity(final Entity entity) {
	    EntityInfo entityInfo = getEntityInfo(entity);
	    entities.remove(entity.getUniqueId());
	    chunks.remove(entityInfo.currentChunkKey);
	}
	
	/** Check if a player is nearby the given entity. This checks for a player in
	 * the current chunk or in any of the 8 adjacent chunks (think of a tic-tac-toe
	 * grid, with current chunk being the middle grid).
	 * 
	 * @param entity
	 * @return
	 */
	private boolean playerIsNearby(final Entity entity) {
	    if( !entity.isValid() )
	        return false;
	    
	    final Location location = entity.getLocation();
	    if( location == null )
	        return false;
	    
	    final World world = location.getWorld();
	    final int chunkX = location.getChunk().getX();
	    final int chunkZ = location.getChunk().getZ();
	    
	    for(Player p : world.getPlayers()) {
	        Location playerLocation = p.getLocation();
	        if( playerLocation != null ) {
	            int playerChunkX = p.getLocation().getChunk().getX();
                int playerChunkZ = p.getLocation().getChunk().getZ();
                
                int distance = config.getMonsterPlayerChunkRadius();
                
                // is the player within the configured chunk distance?
                if( playerChunkX >= chunkX-distance && playerChunkX <= chunkX+distance
                        && playerChunkZ >= chunkZ-distance && playerChunkZ <= chunkZ+distance ) {
                    return true;
                }
	        }
	    }
	    
	    // if we get here, no players are nearby
	    return false;
	}
	
	/** Check if an entity should be purged based on entity type and activity.
	 * 
	 * @param entity
	 * @return
	 */
	private boolean shouldPurge(final Entity entity) {
	    if( !util.isMonster(entity) )   // only monsters can be purged
	        return false;
	    
	    EntityInfo entityInfo = getEntityInfo(entity);
	    long timeSinceLastInteract = System.currentTimeMillis() - entityInfo.lastInteractEvent;
	    if( timeSinceLastInteract > (config.getMonsterIdleAge() * 1000) ) {
	        if( entity instanceof Creature ) {
	            Creature creature = (Creature) entity;
	            
	            // don't purge if we're currently targeting a player
	            LivingEntity target = creature.getTarget();
	            if( target instanceof Player ) {
	                log.debug("shouldPurge is false. entity is targeting player. timeSinceLastInteract=",timeSinceLastInteract/1000,"s, entity=",entity);
	                return false;
	            }

	            if( playerIsNearby(entity) ) {
                    log.debug("shouldPurge is false. player is nearby. timeSinceLastInteract=",timeSinceLastInteract/1000,"s, entity=",entity);
                    return false;
	            }
	        }
	        log.debug("shouldPurge is true. timeSinceLastInteract=",timeSinceLastInteract/1000,"s, entity=",entity);
	        return true;
	    }
	    else {
            log.debug("shouldPurge is false. timeSinceLastInteract=",timeSinceLastInteract/1000,"s, entity=",entity);
	        return false;
	    }
	}
	
	/** We run on a regular schedule to update entity positions.
	 * Possibly chunk into smaller segments for separate runs if performance
	 * becomes an issue. Lots of work to keep track of place/state between runs
	 * so for now use simple algorithm and keep it lightweight so we get through
	 * it quickly.
	 */
	public void run() {
	    List<World> worlds = Bukkit.getWorlds();
	    for(World world : worlds) {
	        List<Entity> entities = world.getEntities();
	        for(Entity entity : entities) {
	            if( !entity.isValid() ) {
	                cleanupEntity(entity);
	                continue;
	            }
	            
	            if( shouldPurge(entity) ) {
	                entity.remove();
	                cleanupEntity(entity);
	            }
	            else
	                updatePosition(entity);
	        }
	    }
	    
	    log.debug("Entities spawned since last reset=",entitySpawnCounter);
	    entitySpawnCounter=0;
	}
	
	/** Class to track meta information about an Entity and specifically to
	 * be hash key/value safe even as entities are loaded/unloaded as chunks
	 * come and go.
	 * 
	 */
	private class EntityInfo {
	    final UUID uuid;
	    // WeakReference serves as a cache to the entity object. Might be null if
	    // the entity has been unloaded or removed.
	    WeakReference<Entity> entityRef;
	    String worldName;
	    long lastInteractEvent;
	    private String currentChunkKey;       // our last known chunk position
	    Location spawnLocation;
	    String spawnChunkKey;
	    
	    public EntityInfo(Entity entity) {
	        this.entityRef = new WeakReference<Entity>(entity);
	        this.uuid = entity.getUniqueId();
	        
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
            this.currentChunkKey = getChunkKey(l.getChunk());
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
	    
	    public String getCurrentChunkKey() {
	        Entity entity = getEntity();
	        
	        // make sure chunkKey and world is current
	        if( entity != null ) {
	            worldName = entity.getLocation().getWorld().getName();
	            currentChunkKey = util.getChunkKey(entity.getLocation().getChunk());
	        }
	        
	        return currentChunkKey;
	    }
	    
	    public String getSpawnChunkKey() {
	        return spawnChunkKey;
	    }
	}
	
	/** Class for keeping track of chunk metadata without actually storing the chunks object.
	 * This is important since chunk objects load and unload all the time and two chunk
	 * objects representing the same chunk (loaded at different times) aren't guaranteed
	 * to be .equal() to each other.
	 * 
	 * @author morganm
	 *
	 */
	private class ChunkInfo {
		final World world;
		final int x;
		final int z;
		/* A bukkit tick is 50ms (or longer). Rather than schedule a tick counter
		 * to run on every tick, we just use this fact to cache data for no more than
		 * 50ms, thus guaranteeing we reset the cache on every tick (>50ms since last call)
		 * and never use stale data (never use data past 50ms). 
		 * 
		 */
		private long lastCacheTick;
		private final Set<Entity> cachedAnimals = new HashSet<Entity>();

		public ChunkInfo(Chunk chunk) {
		    world = chunk.getWorld();
		    x = chunk.getX();
		    z = chunk.getZ();
		}
		public boolean isChunkLoaded() {
			return world.isChunkLoaded(x, z);
		}
		public Chunk getChunk() {
		    if( isChunkLoaded() )
		        return world.getChunkAt(x, z);
		    else
		        return null;
		}
		public Entity[] getEntities() {
		    return getChunk().getEntities();
		}
		
		/** Return the animals that are in the current chunk. The result is
		 * cached per-tick (since entities won't move between successive calls
		 * in the same tick to this method), so it is efficient to call
		 * repeatedly.
		 * 
		 * @return
		 */
		public Set<Entity> getAnimals() {
		    // cache still valid?
		    if( System.currentTimeMillis() < lastCacheTick+50 ) {
		        return cachedAnimals;
		    }
		    // cache no longer valid, reload cache and reset timer
		    else {
		        lastCacheTick=System.currentTimeMillis();
		        cachedAnimals.clear();
		        Entity[] entities = getEntities();
		        for(int i=0; i < entities.length; i++) {
		            if( util.isAnimal(entities[i]) )
		                cachedAnimals.add(entities[i]);
		        }
		    }
		    
		    return cachedAnimals;
		}
	}
}
