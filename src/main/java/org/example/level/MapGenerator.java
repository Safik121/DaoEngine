package org.example.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;

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

        // 4.5 Generate Rivers (2)
        if (config.hasRivers) {
            generateRivers(level, config);
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
        if (size <= 0) {
            if (cx >= 0 && cx < level.width && cy >= 0 && cy < level.height) {
                level.data.get(cy).set(cx, type);
            }
            return;
        }
        for (int y = cy - size; y <= cy + size; y++) {
            for (int x = cx - size; x <= cx + size; x++) {
                if (x >= 0 && x < level.width && y >= 0 && y < level.height) {
                    double dist = Math.sqrt(Math.pow(x - cx, 2) + Math.pow(y - cy, 2));
                    if (dist < size * (0.7 + random.nextDouble() * 0.6)) {
                        level.data.get(y).set(x, type);
                    }
                }
            }
        }
    }

    /**
     * Generates curved rivers using a randomized A* pathfinding algorithm.
     */
    private static void generateRivers(Level level, LevelConfig config) {
        for (int i = 0; i < config.riverCount; i++) {
            int startX, startY, endX, endY;
            // Pick points on opposite edges
            if (random.nextBoolean()) { // Top to Bottom
                startX = random.nextInt(level.width - 40) + 20;
                startY = 0;
                endX = random.nextInt(level.width - 40) + 20;
                endY = level.height - 1;
            } else { // Left to Right
                startX = 0;
                startY = random.nextInt(level.height - 40) + 20;
                endX = level.width - 1;
                endY = random.nextInt(level.height - 40) + 20;
            }

            List<int[]> path = findRiverPath(level, startX, startY, endX, endY);
            if (path != null) {
                for (int[] p : path) {
                    // Draw a small blob along the path for variable width
                    drawBlob(level, p[0], p[1], 1 + random.nextInt(2), 2);
                }
            }
        }
    }

    private static List<int[]> findRiverPath(Level level, int startX, int startY, int endX, int endY) {
        PriorityQueue<RiverNode> openSet = new PriorityQueue<>();
        Map<String, RiverNode> allNodes = new HashMap<>();

        RiverNode startNode = new RiverNode(startX, startY, 0, riverHeuristic(startX, startY, endX, endY), null);
        openSet.add(startNode);
        allNodes.put(startX + "," + startY, startNode);

        Set<String> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            RiverNode current = openSet.poll();
            if (current.x == endX && current.y == endY) {
                return reconstructRiverPath(current);
            }

            closedSet.add(current.x + "," + current.y);

            int[][] neighbors = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            for (int[] offset : neighbors) {
                int nx = current.x + offset[0];
                int ny = current.y + offset[1];

                if (nx < 0 || nx >= level.width || ny < 0 || ny >= level.height) continue;
                String key = nx + "," + ny;
                if (closedSet.contains(key)) continue;

                // Random jitter cost to force curves
                double jitter = random.nextDouble() * 15.0; 
                double moveCost = (Math.abs(offset[0]) + Math.abs(offset[1]) == 2 ? 1.414 : 1.0) + jitter;
                
                double tentativeG = current.g + moveCost;
                RiverNode neighbor = allNodes.get(key);

                if (neighbor == null || tentativeG < neighbor.g) {
                    if (neighbor == null) {
                        neighbor = new RiverNode(nx, ny, tentativeG, riverHeuristic(nx, ny, endX, endY), current);
                        allNodes.put(key, neighbor);
                        openSet.add(neighbor);
                    } else {
                        neighbor.g = tentativeG;
                        neighbor.f = tentativeG + neighbor.h;
                        neighbor.parent = current;
                        // Reprioritize by re-adding to PriorityQueue
                        openSet.remove(neighbor);
                        openSet.add(neighbor);
                    }
                }
            }
        }
        return null;
    }

    private static double riverHeuristic(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private static List<int[]> reconstructRiverPath(RiverNode node) {
        List<int[]> path = new ArrayList<>();
        RiverNode current = node;
        while (current != null) {
            path.add(0, new int[]{current.x, current.y});
            current = current.parent;
        }
        return path;
    }

    private static class RiverNode implements Comparable<RiverNode> {
        int x, y;
        double g, h, f;
        RiverNode parent;

        RiverNode(int x, int y, double g, double h, RiverNode parent) {
            this.x = x;
            this.y = y;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.parent = parent;
        }

        @Override
        public int compareTo(RiverNode o) {
            return Double.compare(this.f, o.f);
        }
    }
}
