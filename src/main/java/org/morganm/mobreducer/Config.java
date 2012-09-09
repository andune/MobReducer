/**
 * 
 */
package org.morganm.mobreducer;

import javax.inject.Inject;

import org.bukkit.configuration.ConfigurationSection;

/**
 * @author morganm
 *
 */
public class Config {
    private static final String MONSTER_BASE = "monster.";
    private static final String ANIMALS_BASE = "animals.";
    
    private ConfigurationSection section;
    
    /** Class accepts a ConfigurationSection as opposed to calling
     * plugin.getConfig() or using a FileConfiguration object. This provides
     * maximum flexibility for testing purposes.
     * 
     * @param section
     */
    @Inject
    public Config(ConfigurationSection section) {
        this.section = section;
    }
    
    /** Used to change the config section in use, most commonly used when
     * the config is reloaded and a new root section is created.
     * 
     * @param section
     */
    public void setSection(ConfigurationSection section) {
        this.section = section;
    }
    
    /** The time (in seconds) that a monster must be doing nothing before
     * we consider it "idle" (and therefore eligible for purge).
     * 
     * @return
     */
    public int getMonsterIdleAge() {
        return section.getInt(MONSTER_BASE+"idleAge");
    }
    
    /** The square radius around a player that mobs are never considered
     * idle and despawned. This prevents mobs from respawning in the
     * same or immediately nearby chunks.
     * 
     * @return
     */
    public int getMonsterPlayerChunkRadius() {
        return section.getInt(MONSTER_BASE+"playerChunkRadius");
    }

    /** Chunks per segment: value is squared to represent square size.
     * Examples:
     *   1 = 1 chunk
     *   2 = 4 chunks (2x2 square)
     *   3 = 9 chunks (3x3 square)
     * 
     * @return
     */
    public int getAnimalChunkSegmentSize() {
        return section.getInt(ANIMALS_BASE+"chunkSegmentSize");
    }
    
    /** Maximum animals allowed per segment before they aren't allowed
     * to spawn anymore.
     * 
     * @return
     */
    public int getAnimalMaxPerSegment() {
        return section.getInt(ANIMALS_BASE+"maxPerSegment");
    }
}
