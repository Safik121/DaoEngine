package org.example.level;

import java.util.List;

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
    /** The grid data representing the world layout (0 = grass, 1 = wall, 2 = water, 3 = spirit vein). */
    public List<List<Integer>> data;
}