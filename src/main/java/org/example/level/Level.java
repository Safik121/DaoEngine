package org.example.level;

import java.util.List;
import org.example.entity.GateOfRealms;
import org.example.entity.InteractableEntity;
import java.util.ArrayList;

/**
 * Represents a game level in the DaoEngine.
 * Contains metadata and the grid data for the world layout.
 */
public class Level {
    /** The name of the level. */
    public String name;
    /** The width of the level in tiles. */
    public int width;
    /** The height of the level in tiles. */
    public int height;
    /** The size of each tile in pixels. */
    public int tileSize;
    /** The environmental theme of this level instance. */
    public Biome biome = Biome.FOREST;
    /** The configuration used to generate this level. */
    public LevelConfig config;
    /**
     * The grid data representing the world layout:
     * 0 = Grass/Free
     * 1 = Wall/Border
     * 2 = Water/Lake/River
     * 3 = Spirit Vein (Qi Regen)
     * 4 = Terrain Variation (Rocks/Decoration)
     * 5 = Bridge
     */
    public List<List<Integer>> data;
    /** The gate to the next realm. */
    public GateOfRealms gate;
    /** List of interactive entities (NPCs, Steles). */
    public List<InteractableEntity> interactables = new ArrayList<>();
    /** The random seed used to generate this level. */
    public long seed;
}