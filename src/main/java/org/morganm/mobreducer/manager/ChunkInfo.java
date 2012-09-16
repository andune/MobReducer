package org.morganm.mobreducer.manager;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.morganm.mobreducer.Util;

import com.google.inject.assistedinject.Assisted;

/** Class for keeping track of chunk metadata without actually storing the chunks object.
 * This is important since chunk objects load and unload all the time and two chunk
 * objects representing the same chunk (loaded at different times) aren't guaranteed
 * to be .equal() to each other.
 * 
 * @author morganm
 *
 */
public class ChunkInfo {
    private final Util util;
    private final World world;
    private final int x;
    private final int z;
    
	/* A bukkit tick is 50ms (or longer). Rather than schedule a tick counter
	 * to run on every tick, we just use this fact to cache data for no more than
	 * 50ms, thus guaranteeing we reset the cache on every tick (>50ms since last call)
	 * and never use stale data (never use data past 50ms). 
	 * 
	 */
	private long lastCacheTick;
	private final Set<Entity> cachedAnimals = new HashSet<Entity>();

	@Inject
	public ChunkInfo(@Assisted Chunk chunk, Util util) {
        this.world = chunk.getWorld();
	    this.x = chunk.getX();
	    this.z = chunk.getZ();
	    this.util = util;
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