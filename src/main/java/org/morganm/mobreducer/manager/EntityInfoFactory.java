/**
 * 
 */
package org.morganm.mobreducer.manager;

import org.bukkit.entity.Entity;

/**
 * @author morganm
 *
 */
public interface EntityInfoFactory {
    public EntityInfo create(Entity entity);
}
