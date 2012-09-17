/**
 * 
 */
package org.morganm.mobreducer;

import javax.inject.Inject;

import org.bukkit.configuration.ConfigurationSection;
import org.morganm.mBukkitLib.Logger;

/**
 * @author morganm
 *
 */
public class Config {
    private static final String MONSTER_BASE = "monsters.";
    private static final String ANIMALS_BASE = "animals.";
    
    private ConfigurationSection section;
    private Logger log;
    
    /** Class accepts a ConfigurationSection as opposed to calling
     * plugin.getConfig() or using a FileConfiguration object. This provides
     * maximum flexibility for testing purposes.
     * 
     * @param section
     */
    @Inject
    public Config(ConfigurationSection section, Logger log) {
        this.section = section;
        this.log = log;
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

    private static final String ANIMAL_SEGMENT_SIZE = ANIMALS_BASE+"chunkSegmentSize";
    /** Chunks per segment: cocentric squares from chunk being measured.
     * Examples:
     *   0 = 1 chunk 
     *   1 = 9 chunks (3x3 square, ie. tic-tac-toe)
     *   2 = 25 chunks (5x5 square)
     * 
     * @return
     */
    public int getAnimalChunkSegmentSize() {
        return section.getInt(ANIMAL_SEGMENT_SIZE);
    }
    
    /** Maximum animals allowed per segment before they aren't allowed
     * to spawn anymore.
     * 
     * @return
     */
    public int getAnimalMaxPerSegment() {
        return section.getInt(ANIMALS_BASE+"maxPerSegment");
    }
    
    public boolean isPlayerDamageRequiredForDrops() {
        return section.getBoolean("playerDamageRequiredForDrops");
    }

    /** Do validations to warn admin if there are any funky settings.
     * 
     * @return true if validations pass enough that the plugin should run, false if not
     */
    public boolean validate() {
        boolean ret = true;
        
        // we run through all checks, not just the first one. This way the admin gets
        // to see all errors at once that they need to fix.
        
        if( getAnimalChunkSegmentSize() < 0 || getAnimalChunkSegmentSize() > 2 ) {
            log.severe("Invalid config value for "+ANIMAL_SEGMENT_SIZE+": "+getAnimalChunkSegmentSize()
                    +" [value must be between 0 and 2]");
            ret = false;
        }
        
        return ret;
    }
}
