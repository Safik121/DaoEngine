package org.example.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages spatial information about the game level, such as free tiles
 * and safe spawning locations. Analyzes level data to provide utility methods
 * for entity placement.
 */
public class GameMap {
    /** Reference to the underlying level data. */
    private final Level level;
    /** List of all free tiles (grass) on the map. */
    private final List<int[]> freeTiles;
    /** Random generator for spawning logic. */
    private final Random random;

    /**
     * Constructs a GameMap for the given level.
     * @param level The level to analyze.
     */
    public GameMap(Level level) {
        this.level = level;
        this.freeTiles = new ArrayList<>();
        this.random = new Random();
        initializeFreeTiles();
    }

    /**
     * Checks if a tile at the given coordinates is solid (a wall).
     * 
     * @param x Tile X coordinate.
     * @param y Tile Y coordinate.
     * @return true if solid or out of bounds, false otherwise.
     */
    public boolean isSolid(int x, int y) {
        if (level == null || level.data == null) return true;
        if (x < 0 || x >= level.width || y < 0 || y >= level.height) return true;
        return level.data.get(y).get(x) == 1;
    }

    /**
     * Returns the size of a single tile in pixels.
     */
    public int getTileSize() {
        return level.tileSize;
    }

    /**
     * Initializes the list of free tiles based on the level data.
     */
    private void initializeFreeTiles() {
        if (level == null || level.data == null) return;

        for (int y = 0; y < level.data.size(); y++) {
            List<Integer> row = level.data.get(y);
            for (int x = 0; x < row.size(); x++) {
                if (row.get(x) == 0) { // 0 = Grass/Free tile
                    freeTiles.add(new int[]{x, y});
                }
            }
        }
    }

    /**
     * Picks a random free tile and returns its pixel coordinates.
     * 
     * @param entitySize Size of the entity (for centering).
     * @return Array [pixelX, pixelY] or null if no free tiles exist.
     */
    public double[] getRandomFreePosition(double entitySize) {
        if (freeTiles.isEmpty()) return null;

        int[] tile = freeTiles.get(random.nextInt(freeTiles.size()));

        // Calculate pixel coordinates (centered in the tile)
        double px = tile[0] * level.tileSize + (level.tileSize - entitySize) / 2.0;
        double py = tile[1] * level.tileSize + (level.tileSize - entitySize) / 2.0;

        return new double[]{px, py};
    }

    /**
     * Picks a random free tile that is at least a certain distance away from a target.
     * 
     * @param entitySize Size of the entity.
     * @param targetX Pixel X coordinate of the target (e.g., player).
     * @param targetY Pixel Y coordinate of the target (e.g., player).
     * @param minDistance Minimum distance in pixels.
     * @return Array [pixelX, pixelY] or null.
     */
    public double[] getRandomFreePositionAwayFrom(double entitySize, double targetX, double targetY, double minDistance) {
        List<int[]> candidates = new ArrayList<>();
        
        for (int[] tile : freeTiles) {
            double px = tile[0] * level.tileSize + (level.tileSize - entitySize) / 2.0;
            double py = tile[1] * level.tileSize + (level.tileSize - entitySize) / 2.0;
            
            double dx = px - targetX;
            double dy = py - targetY;
            double distSq = dx * dx + dy * dy;
            
            if (distSq >= minDistance * minDistance) {
                candidates.add(tile);
            }
        }

        if (candidates.isEmpty()) {
            return getRandomFreePosition(entitySize); // Fallback to any tile if too crowded
        }

        int[] tile = candidates.get(random.nextInt(candidates.size()));
        double px = tile[0] * level.tileSize + (level.tileSize - entitySize) / 2.0;
        double py = tile[1] * level.tileSize + (level.tileSize - entitySize) / 2.0;

        return new double[]{px, py};
    }
}
