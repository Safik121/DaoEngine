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

    /** The time in seconds until the Heavenly Punishment (Tribulation) begins. */
    public double tribulationTime = 60.0;
}
