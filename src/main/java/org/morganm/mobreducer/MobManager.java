/**
 * 
 */
package org.morganm.mobreducer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
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
import org.morganm.mBukkitLib.Logger;

/** Class that manages active mobs on the server, tracking information
 * about them for use by the rest of the plugin.
 * 
 * @author morganm
 *
 */
public class MobManager implements Listener, Runnable {
    // 10 minutes, in millis. Move to config
    private static final long IDLE_LIMIT = 30000;
    // testing only, to replaced by config options later
    private static final int CHUNK_ENTITY_SIZE_LIMIT = 30;
    
	private final HashMap<String, ChunkInfo> chunks = new HashMap<String, ChunkInfo>(100);
	private final HashMap<Integer, EntityInfo> entities = new HashMap<Integer, EntityInfo>(500);
	private final Logger log;
	private int entitySpawnCounter=0;  // debug counter
	
	@Inject
	public MobManager(Logger log) {
	    this.log = log;
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
	}
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent event) {
	    interact(event.getEntity());
	}
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
	    if( isAnimal(entity) ) {
	        Location l = entity.getLocation();
	        if( l != null ) {
	            ChunkInfo chunkInfo = getChunkInfo(l.getChunk());
	            if( chunkInfo.entities.size() > CHUNK_ENTITY_SIZE_LIMIT ) {
	                log.info("refusing entity spawn due to chunk size limits for entity ", entity);
	                return false;
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
		return chunk.getWorld().getName()+","+chunk.getX()+","+chunk.getZ();
	}
	
	/** Return ChunkInfo object. Will create one if one doesn't already exist.
	 * 
	 * @param chunkKey
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
	    EntityInfo entityInfo = entities.get(entity.getEntityId());
	    if( entityInfo == null ) {
	        entityInfo = new EntityInfo(entity);
	        entities.put(entity.getEntityId(), entityInfo);
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
	            (entityInfo.chunkKey != null && !entityInfo.chunkKey.equals(currentChunkKey)) ) {
	        // remove 
	        ChunkInfo chunkInfo = chunks.get(entityInfo.chunkKey);
	        if( chunkInfo != null ) {
	            chunkInfo.entities.remove(entity);
	        }
	    }
	    
	    // if there is a current position, update new position
	    if( currentChunkKey != null ) {
	        // update entity->chunk mapping
            entityInfo.chunkKey = currentChunkKey;
            
            // update chunk->entities set
	        ChunkInfo chunkInfo = getChunkInfo(currentLocation.getChunk());
	        chunkInfo.entities.add(entity);
	    }
	}
	
	private void cleanupEntity(final Entity entity) {
	    EntityInfo entityInfo = getEntityInfo(entity);
	    entities.remove(entity.getEntityId());
	    chunks.remove(entityInfo.chunkKey);
	}
	
	/** 
	 * 
	 * @param entity
	 * @return true if entity is a monster, false if not
	 */
	private boolean isMonster(final Entity entity) {
	    switch(entity.getType()) {
	    case BLAZE:
	    case CAVE_SPIDER:
	    case CREEPER:
	    case ENDER_DRAGON:
	    case ENDERMAN:
	    case GHAST:
	    case GIANT:
	    case MAGMA_CUBE:
	    case PIG_ZOMBIE:
	    case SKELETON:
	    case SLIME:
	    case SILVERFISH:
	    case SPIDER:
	    case ZOMBIE:
	        return true;
	    default:
	        return false;
	    }
	}
	
	/**
	 * 
	 * @param entity
	 * @return true if entity is an animal, false if not
	 */
	private boolean isAnimal(final Entity entity) {
       switch(entity.getType()) {
       case CHICKEN:
       case COW:
       case MUSHROOM_COW:
       case OCELOT:
       case PIG:
       case SHEEP:
       case SNOWMAN:
       case SQUID:
       case WOLF:
           return true;
       default:
           return false;
       }
	}
	
    /**
     * 
     * @param entity
     * @return true if entity is a village entity (villager or golem), false if not
     */
	private boolean isVillageEntity(final Entity entity) {
	       switch(entity.getType()) {
	       case VILLAGER:
	       case IRON_GOLEM:
	           return true;
	       default:
	           return false;
	       }
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
                
                // is the player within 1 chunk distance?
                if( playerChunkX >= chunkX-1 && playerChunkX <= chunkX+1
                        && playerChunkZ >= chunkZ-1 && playerChunkZ <= chunkZ+1 ) {
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
	    if( !isMonster(entity) )   // only monsters can be purged
	        return false;
	    
	    EntityInfo entityInfo = getEntityInfo(entity);
	    long timeSinceLastInteract = System.currentTimeMillis() - entityInfo.lastInteractEvent;
	    if( timeSinceLastInteract > IDLE_LIMIT ) {
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
	
	private class EntityInfo {
	    final int entityId;
	    long lastInteractEvent;
	    String chunkKey;       // our last known chunk position
	    
	    public EntityInfo(Entity entity) {
	        this.entityId = entity.getEntityId();
	        this.lastInteractEvent = System.currentTimeMillis();
	        Location l = entity.getLocation();
	        if( l != null )
	            chunkKey = getChunkKey(l.getChunk());
	    }
	}
	
	private class ChunkInfo {
		final World world;
		final int x;
		final int z;
		final Set<Entity> entities;

		public ChunkInfo(Chunk chunk) {
		    world = chunk.getWorld();
		    x = chunk.getX();
		    z = chunk.getZ();
		    entities = new HashSet<Entity>();
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
	}
}

