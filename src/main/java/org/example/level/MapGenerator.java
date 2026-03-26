package org.example.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates level data procedurally based on a LevelConfig.
 */
public class MapGenerator {
    private static final Random random = new Random();

    /**
     * Generates a new level procedurally based on the provided configuration.
     * 
     * @param config The generation parameters (size, clusters, etc.).
     * @return A fully populated Level object.
     */
    public static Level generate(LevelConfig config) {
        Level level = new Level();
        level.name = config.name;
        level.width = config.width;
        level.height = config.height;
        level.tileSize = config.tileSize;
        level.data = new ArrayList<>();

        // 1. Initialize with Grass (0)
        for (int y = 0; y < level.height; y++) {
            List<Integer> row = new ArrayList<>();
            for (int x = 0; x < level.width; x++) {
                row.add(0);
            }
            level.data.add(row);
        }

        // 2. Add Border Walls (1)
        for (int x = 0; x < level.width; x++) {
            level.data.get(0).set(x, 1);
            level.data.get(level.height - 1).set(x, 1);
        }
        for (int y = 0; y < level.height; y++) {
            level.data.get(y).set(0, 1);
            level.data.get(y).set(level.width - 1, 1);
        }

        // 3. Generate Spirit Veins (3)
        for (int i = 0; i < config.veinCount; i++) {
            int cx = random.nextInt(level.width - 20) + 10;
            int cy = random.nextInt(level.height - 20) + 10;
            int size = random.nextInt(config.veinMaxSize - config.veinMinSize + 1) + config.veinMinSize;
            drawBlob(level, cx, cy, size, 3);
        }

        // 4. Generate Lakes (2)
        if (config.hasLakes) {
            for (int i = 0; i < config.lakeCount; i++) {
                int cx = random.nextInt(level.width - 30) + 15;
                int cy = random.nextInt(level.height - 30) + 15;
                drawBlob(level, cx, cy, config.lakeSize, 2);
            }
        }
        // 5. Scatter Terrain Variety (4) - Small rocks/grass tufts for visual feedback
        for (int y = 1; y < level.height - 1; y++) {
            for (int x = 1; x < level.width - 1; x++) {
                if (level.data.get(y).get(x) == 0 && random.nextDouble() < 0.05) {
                    level.data.get(y).set(x, 4);
                }
            }
        }

        return level;
    }

    /**
     * Draws a randomized "blob" of a certain tile type (used for lakes/veins).
     * 
     * @param level The level to draw into.
     * @param cx Center X coordinate.
     * @param cy Center Y coordinate.
     * @param size Maximum radius of the blob.
     * @param type The tile ID to use.
     */
    private static void drawBlob(Level level, int cx, int cy, int size, int type) {
        for (int y = cy - size; y <= cy + size; y++) {
            for (int x = cx - size; x <= cx + size; x++) {
                if (x > 0 && x < level.width - 1 && y > 0 && y < level.height - 1) {
                    double dist = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
                    if (dist < size * (0.7 + random.nextDouble() * 0.6)) {
                        level.data.get(y).set(x, type);
                    }
                }
            }
        }
    }
}
