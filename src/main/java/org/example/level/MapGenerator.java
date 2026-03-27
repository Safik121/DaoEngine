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
     * 
     * @param level The level to draw into.
     * @param config The level configuration containing river and bridge parameters.
     */
    private static void generateRivers(Level level, LevelConfig config) {
        List<List<int[]>> allPaths = new ArrayList<>();
        List<Boolean> verticalFlags = new ArrayList<>();
        List<boolean[]> inLakes = new ArrayList<>();
        java.util.Set<String> bridgeCenters = new java.util.HashSet<>();

        for (int i = 0; i < config.riverCount; i++) {
            int startX, startY, endX, endY;
            boolean isOverallVertical;
            if (random.nextBoolean()) {
                startX = random.nextInt(level.width - 40) + 20;
                startY = 0;
                endX = random.nextInt(level.width - 40) + 20;
                endY = level.height - 1;
                isOverallVertical = true;
            } else {
                startX = 0;
                startY = random.nextInt(level.height - 40) + 20;
                endX = level.width - 1;
                endY = random.nextInt(level.height - 40) + 20;
                isOverallVertical = false;
            }

            List<int[]> path = findRiverPath(level, startX, startY, endX, endY);
            if (path != null) {
                allPaths.add(path);
                verticalFlags.add(isOverallVertical);
                
                boolean[] inLake = new boolean[path.size()];
                for (int j = 0; j < path.size(); j++) {
                    int[] p = path.get(j);
                    if (p[0] >= 0 && p[0] < level.width && p[1] >= 0 && p[1] < level.height) {
                        inLake[j] = (level.data.get(p[1]).get(p[0]) == 2);
                    }
                }
                inLakes.add(inLake);
            }
        }

        // 1. Draw all rivers using a temporary tile type (6) to distinguish from lakes (2)
        for (List<int[]> path : allPaths) {
            for (int[] p : path) {
                drawRiverBlob(level, p[0], p[1], 1 + random.nextInt(2), 6);
            }
        }

        // 2. Draw all bridges
        for (int i = 0; i < allPaths.size(); i++) {
            List<int[]> path = allPaths.get(i);
            boolean isOverallVertical = verticalFlags.get(i);
            boolean[] inLake = inLakes.get(i);

            int numBridges = config.bridgeMin + random.nextInt(config.bridgeMax - config.bridgeMin + 1);
            int attempts = 0;
            int bridgesPlaced = 0;
            while (bridgesPlaced < numBridges && attempts < 50) {
                attempts++;
                if (path.size() < 40) break;
                int idx = 20 + random.nextInt(path.size() - 40);
                
                if (idx < 0 || idx >= path.size() || inLake[idx]) continue;
                
                // Safe distance from lakes (check a window around idx along the path)
                boolean nearLake = false;
                for (int win = -10; win <= 10; win++) {
                    int wIdx = idx + win;
                    if (wIdx >= 0 && wIdx < inLake.length && inLake[wIdx]) {
                        nearLake = true;
                        break;
                    }
                }
                if (nearLake) continue;
                
                int[] p = path.get(idx);
                
                // Prevent bridges from being too close together (on any river)
                boolean tooClose = false;
                for (String center : bridgeCenters) {
                    String[] parts = center.split(",");
                    int cx = Integer.parseInt(parts[0]);
                    int cy = Integer.parseInt(parts[1]);
                    if (Math.hypot(p[0] - cx, p[1] - cy) < 15) {
                        tooClose = true;
                        break;
                    }
                }
                if (tooClose) continue;

                int bThickness = config.bridgeMinWidth + random.nextInt(config.bridgeMaxWidth - config.bridgeMinWidth + 1);
                int estRiverWidth = 5; 
                int span = (3 * estRiverWidth + 2) / 2;
                
                // If river is overall vertical, bridge is horizontal. If horizontal, bridge is vertical.
                if (drawBridgeSafe(level, p[0], p[1], bThickness, span, !isOverallVertical)) {
                    bridgeCenters.add(p[0] + "," + p[1]);
                    bridgesPlaced++;
                }
            }
        }

        // 3. Convert all temporary river tiles (6) back to water (2)
        for (int y = 0; y < level.height; y++) {
            List<Integer> row = level.data.get(y);
            for (int x = 0; x < level.width; x++) {
                if (row.get(x) == 6) {
                    row.set(x, 2);
                }
            }
        }
    }

    /**
     * Draws a solid bridge ONLY if it doesn't overlap with a lake (type 2).
     * 
     * @param level The level to draw into.
     * @param x Center X coordinate of the bridge.
     * @param y Center Y coordinate of the bridge.
     * @param thickness Thickness of the bridge in tiles.
     * @param span Half-length of the bridge span.
     * @param orientationVertical True for North-South orientation, false for East-West.
     * @return True if the bridge was successfully drawn, false if it was skipped due to lake overlap.
     */
    private static boolean drawBridgeSafe(Level level, int x, int y, int thickness, int span, boolean orientationVertical) {
        // First check for lakes with a 1-pixel safety margin to account for jagged boundaries
        int margin = 1;
        if (orientationVertical) {
            for (int ty = y - span - margin; ty <= y + span + margin; ty++) {
                for (int tx = x - (thickness - 1) / 2 - margin; tx <= x + thickness / 2 + margin; tx++) {
                    if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                        if (level.data.get(ty).get(tx) == 2) return false;
                    }
                }
            }
        } else {
            for (int tx = x - span - margin; tx <= x + span + margin; tx++) {
                for (int ty = y - (thickness - 1) / 2 - margin; ty <= y + thickness / 2 + margin; ty++) {
                    if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                        if (level.data.get(ty).get(tx) == 2) return false;
                    }
                }
            }
        }

        // No lakes found, draw the bridge
        if (orientationVertical) {
            for (int ty = y - span; ty <= y + span; ty++) {
                for (int tx = x - (thickness - 1) / 2; tx <= x + thickness / 2; tx++) {
                    if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                        level.data.get(ty).set(tx, 5);
                    }
                }
            }
        } else {
            for (int tx = x - span; tx <= x + span; tx++) {
                for (int ty = y - (thickness - 1) / 2; ty <= y + thickness / 2; ty++) {
                    if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                        level.data.get(ty).set(tx, 5);
                    }
                }
            }
        }
        return true;
    }

    /**
     * Specialized blob drawer for rivers that does not overwrite lake tiles (2).
     * 
     * @param level The level to draw into.
     * @param cx Center X coordinate.
     * @param cy Center Y coordinate.
     * @param size Radius of the blob.
     * @param type The tile ID to use.
     */
    private static void drawRiverBlob(Level level, int cx, int cy, int size, int type) {
        for (int ty = cy - size; ty <= cy + size; ty++) {
            for (int tx = cx - size; tx <= cx + size; tx++) {
                if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                    double distSq = Math.pow(tx - cx, 2) + Math.pow(ty - cy, 2);
                    if (distSq <= size * size) {
                        // Only set if not already a lake (2)
                        if (level.data.get(ty).get(tx) != 2) {
                            level.data.get(ty).set(tx, type);
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds a curved path for a river using randomized A* pathfinding.
     * 
     * @param level The level for bounds checking.
     * @param startX Starting X coordinate.
     * @param startY Starting Y coordinate.
     * @param endX Ending X coordinate.
     * @param endY Ending Y coordinate.
     * @return A list of [x, y] coordinates forming the path, or null if no path found.
     */
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

    /**
     * Calculates the Euclidean heuristic for river pathfinding.
     * 
     * @param x1 Start X.
     * @param y1 Start Y.
     * @param x2 End X.
     * @param y2 End Y.
     * @return The distance between points.
     */
    private static double riverHeuristic(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    /**
     * Reconstructs the path from the end node back to the start.
     * 
     * @param node The end node of the path.
     * @return A list of coordinates from start to end.
     */
    private static List<int[]> reconstructRiverPath(RiverNode node) {
        List<int[]> path = new ArrayList<>();
        RiverNode current = node;
        while (current != null) {
            path.add(0, new int[]{current.x, current.y});
            current = current.parent;
        }
        return path;
    }

    /**
     * Internal node class for the A* river pathfinding algorithm.
     */
    private static class RiverNode implements Comparable<RiverNode> {
        /** X coordinate on the grid. */
        int x;
        /** Y coordinate on the grid. */
        int y;
        /** G-cost: actual cost from the start node. */
        double g;
        /** H-cost: estimated cost to the end node. */
        double h;
        /** F-cost: total estimated cost (G + H). */
        double f;
        /** Reference to the parent node for path reconstruction. */
        RiverNode parent;

        /**
         * Creates a new river node.
         * 
         * @param x X coordinate.
         * @param y Y coordinate.
         * @param g Start cost.
         * @param h Heuristic cost.
         * @param parent Parent node.
         */
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
