/**
 * 
 */
package org.morganm.mobreducer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/** Class that tracks heuristics to identify mob grinders and either
 * disable or tune them down.
 * 
 * @author morganm
 *
 */
public class AntiGrinder implements Listener {
    // LinkedHashMaps used to utilize LRU functionality to trim eldest hash entries
    private final Map<Entity, String> spawnChunks = new LinkedHashMap<Entity, String>(500);
    private final Map<String, Integer> deathLocation = new LinkedHashMap<String, Integer>(500);
    private final Map<String, Integer> deathChunks = new LinkedHashMap<String, Integer>(500);
    
    private final Map<String, Integer> spawnerChunks = new HashMap<String, Integer>(10);
    private final Util util;
    
    @Inject
    public AntiGrinder(Util util) {
        this.util = util;
    }
    
    @EventHandler
    public void onEntitySpawn(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        if( !util.isMonster(entity) )
            return;
        final String chunkKey = util.getChunkKey(entity.getLocation().getChunk());
        spawnChunks.put(entity, chunkKey);
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if( !util.isMonster(entity) )
            return;
        
        // TODO: update to track lists of entities rather than just a number
        
        final String chunkKey = util.getChunkKey(entity.getLocation().getChunk());
        int count = deathChunks.get(chunkKey);
        deathChunks.put(chunkKey, Integer.valueOf(count+1)); 
        
        final String locationKey = util.getLocationKey(entity.getLocation());
        count = deathLocation.get(locationKey);
        deathLocation.put(locationKey, Integer.valueOf(count+1)); 
    }
}
