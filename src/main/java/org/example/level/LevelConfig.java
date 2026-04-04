package org.example.level;

/**
 * Configuration for procedural level generation.
 */
public class LevelConfig {
    /** The name of the generated realm. */
    public String name = "Generated Realm";
    /** The width of the level in tiles. */
    public int width = 400;
    /** The height of the level in tiles. */
    public int height = 400;
    /** The size of each tile in pixels. */
    public int tileSize = 32;
    /** The environmental theme for generated levels. */
    public Biome biome = Biome.FOREST;

    // Spirit Vein Settings
    /** The number of spirit vein clusters to generate. */
    public int veinCount = 6;
    /** The minimum size of a spirit vein cluster. */
    public int veinMinSize = 3;
    /** The maximum size of a spirit vein cluster. */
    public int veinMaxSize = 12;

    // Water Settings
    /** Whether the generator should create lakes. */
    public boolean hasLakes = true;
    /** The number of lakes to generate. */
    public int lakeCount = 5;
    /** The average size of a lake. */
    public int lakeSize = 15;
    
    /** Whether the generator should create rivers. */
    public boolean hasRivers = true;
    /** The number of rivers to generate. */
    public int riverCount = 2;
    /** Minimum bridges per river. */
    public int bridgeMin = 1;
    /** Maximum bridges per river. */
    public int bridgeMax = 3;
    /** Minimum width of a bridge (along the river). */
    public int bridgeMinWidth = 2;
    /** Maximum width of a bridge (along the river). */
    public int bridgeMaxWidth = 4;

    /** The time in seconds until the Heavenly Punishment (Tribulation) begins. */
    public double tribulationTime = 60.0;
    
    // --- Enemy Spawning Configuration ---
    /** The number of enemies to spawn at the start of the level. */
    public int initialEnemyCount = 10;
    /** Total number of enemies that will spawn in one wave when the Tribulation phase begins. */
    public int tribulationTotalEnemies = 20;
    /** Multiplier for enemy HP and Damage during Tribulation. */
    public double tribulationScalingFactor = 2.0;
    /** List of enemy IDs that are allowed to spawn in this level. */
    public java.util.List<String> enemyPool = java.util.Arrays.asList("bat_01", "zombie_01");
}
