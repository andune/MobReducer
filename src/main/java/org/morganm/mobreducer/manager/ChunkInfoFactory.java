/**
 * 
 */
package org.morganm.mobreducer.manager;

import org.bukkit.Chunk;

/**
 * @author morganm
 *
 */
public interface ChunkInfoFactory {
    public ChunkInfo create(Chunk chunk);
}
