/**
 * 
 */
package org.morganm.mobreducer;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/** Class that manages active mobs on the server, tracking information
 * about them for use by the rest of the plugin.
 * 
 * @author morganm
 *
 */
public class MobManager implements Listener {
	HashMap<String, ChunkInfo> chunks;

	public void onEntitySpawn(CreatureSpawnEvent event) {
		Entity entity = event.getEntity();
		Chunk chunk = event.getLocation().getChunk();
		String chunkKey = getChunkKey(chunk);
	}
	
	private String getChunkKey(Chunk chunk) {
		return chunk.getWorld().getName()+","+chunk.getX()+","+chunk.getZ();
	}
	
	private class ChunkInfo {
		World world;
		int x;
		int z;
		Set<Entity> entities;
		
		public boolean isChunkLoaded() {
			return world.isChunkLoaded(x, z);
		}
	}
}

