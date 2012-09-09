/**
 * 
 */
package org.morganm.mobreducer;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

/**
 * @author morganm
 *
 */
public class Util {
    /** Return a specific key to a chunk that can be referenced even if the Chunk
     * is unloaded and no longer a valid reference.
     * 
     * @param chunk
     * @return
     */
    public String getChunkKey(Chunk chunk) {
        return chunk.getWorld().getName()+","+chunk.getX()+","+chunk.getZ();
    }
    /** Return a specific key to a location at the block level (ie. ignores
     * decimal precision and pitch/yaw). 
     * 
     * @param chunk
     * @return
     */
    public String getLocationKey(Location l) {
        return l.getWorld().getName()+","+l.getBlockX()+","+l.getBlockY()+","+l.getBlockZ();
    }
    
    /** 
     * 
     * @param entity
     * @return true if entity is a monster, false if not
     */
    public boolean isMonster(final Entity entity) {
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
    public boolean isAnimal(final Entity entity) {
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
    public boolean isVillageEntity(final Entity entity) {
           switch(entity.getType()) {
           case VILLAGER:
           case IRON_GOLEM:
               return true;
           default:
               return false;
           }
    }
}
