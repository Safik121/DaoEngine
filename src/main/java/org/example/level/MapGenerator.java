package org.example.level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import org.example.entity.GateOfRealms;
import org.example.entity.InteractableEntity;
import org.example.item.ItemRegistry;

/**
 * Generates level data procedurally based on a LevelConfig.
 */
public class MapGenerator {
    /**
     * Generates a new level procedurally with a random seed.
     * 
     * @param config The generation parameters.
     * @return A fully populated Level object.
     */
    public static Level generate(LevelConfig config) {
        return generate(config, new Random().nextLong());
    }

    /**
     * Generates a new level procedurally using a specific seed.
     * 
     * @param config The generation parameters.
     * @param seed   The specific seed for reproduction.
     * @return A fully populated Level object.
     */
    public static Level generate(LevelConfig config, long seed) {
        Random random = new Random(seed);
        Level level = new Level();
        level.seed = seed;
        level.name = config.name;
        level.width = config.width;
        level.height = config.height;
        level.tileSize = config.tileSize;
        level.biome = config.biome;
        level.config = config;
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
            drawBlob(level, cx, cy, size, 3, random);
        }

        // 4. Generate Lakes (2)
        if (config.hasLakes) {
            int lakeCount = config.lakeCount * (level.biome == Biome.ICE ? 2 : 1);
            for (int i = 0; i < lakeCount; i++) {
                int cx = random.nextInt(level.width - 30) + 15;
                int cy = random.nextInt(level.height - 30) + 15;
                drawBlob(level, cx, cy, config.lakeSize, 2, random);
            }
        }

        // 4.5 Generate Rivers (2)
        if (config.hasRivers) {
            int riverCount = config.riverCount * (level.biome == Biome.ICE ? 2 : 1);
            // Temporary adjustment for manual river generation loop
            for (int i = 0; i < riverCount; i++) {
                generateRivers(level, config, random);
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

        // 6. Spawn Gate of Realms (Far from center)
        int centerThreshold = Math.min(level.width, level.height) / 3;
        int gx = 0, gy = 0;
        boolean found = false;

        for (int attempt = 0; attempt < 200; attempt++) {
            gx = random.nextInt(level.width - 10) + 5;
            gy = random.nextInt(level.height - 10) + 5;

            double distFromCenter = Math
                    .sqrt(Math.pow(gx - level.width / 2.0, 2) + Math.pow(gy - level.height / 2.0, 2));
            if (level.data.get(gy).get(gx) == 0 && distFromCenter > centerThreshold) {
                found = true;
                break;
            }
        }

        if (found) {
            level.gate = new GateOfRealms(gx * level.tileSize, gy * level.tileSize);
        }

        // 7. Spawn NPCs and Steles
        spawnInteractables(level, random);

        return level;
    }

    /**
     * Spawns NPCs and Steles based on the LevelConfig definitions.
     */
    private static void spawnInteractables(Level level, Random random) {
        if (level.config == null || level.config.interactables == null) return;

        for (InteractableConfig ic : level.config.interactables) {
            for (int i = 0; i < ic.count; i++) {
                double[] pos = findRandomGrass(level, random);
                if (pos != null) {
                    InteractableEntity entity = new InteractableEntity(pos[0], pos[1], ic.name, ic.type);
                    if (ic.dialogueTreeId != null) {
                        entity.setDialogueTreeId(ic.dialogueTreeId);
                    }
                    if (ic.rewardItemId != null && !ic.rewardItemId.isEmpty()) {
                        entity.setRewardItem(ItemRegistry.createItem(ic.rewardItemId));
                    }
                    if (ic.giveQuestId != null && !ic.giveQuestId.isEmpty()) {
                        entity.setGiveQuestId(ic.giveQuestId);
                    }
                    level.interactables.add(entity);
                }
            }
        }
    }

    private static double[] findRandomGrass(Level level, Random random) {
        for (int attempt = 0; attempt < 100; attempt++) {
            int rx = random.nextInt(level.width);
            int ry = random.nextInt(level.height);
            if (level.data.get(ry).get(rx) == 0) {
                return new double[] { rx * level.tileSize, ry * level.tileSize };
            }
        }
        return null;
    }

    private static void drawBlob(Level level, int cx, int cy, int size, int type, Random random) {
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

    private static void generateRivers(Level level, LevelConfig config, Random random) {
        List<List<int[]>> allPaths = new ArrayList<>();
        List<Boolean> verticalFlags = new ArrayList<>();
        List<boolean[]> inLakes = new ArrayList<>();
        Set<String> bridgeCenters = new HashSet<>();

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

            List<int[]> path = findRiverPath(level, startX, startY, endX, endY, random);
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

        for (List<int[]> path : allPaths) {
            for (int[] p : path) {
                drawRiverBlob(level, p[0], p[1], 1 + random.nextInt(2), 6);
            }
        }

        for (int i = 0; i < allPaths.size(); i++) {
            List<int[]> path = allPaths.get(i);
            boolean isOverallVertical = verticalFlags.get(i);
            boolean[] inLake = inLakes.get(i);

            int numBridges = config.bridgeMin + random.nextInt(config.bridgeMax - config.bridgeMin + 1);
            int attempts = 0;
            int bridgesPlaced = 0;
            while (bridgesPlaced < numBridges && attempts < 50) {
                attempts++;
                if (path.size() < 40)
                    break;
                int idx = 20 + random.nextInt(path.size() - 40);
                if (idx < 0 || idx >= path.size() || inLake[idx])
                    continue;

                boolean nearLake = false;
                for (int win = -10; win <= 10; win++) {
                    int wIdx = idx + win;
                    if (wIdx >= 0 && wIdx < inLake.length && inLake[wIdx]) {
                        nearLake = true;
                        break;
                    }
                }
                if (nearLake)
                    continue;

                int[] p = path.get(idx);
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
                if (tooClose)
                    continue;

                int bThickness = config.bridgeMinWidth
                        + random.nextInt(config.bridgeMaxWidth - config.bridgeMinWidth + 1);
                int span = 8;
                if (drawBridgeSafe(level, p[0], p[1], bThickness, span, !isOverallVertical)) {
                    bridgeCenters.add(p[0] + "," + p[1]);
                    bridgesPlaced++;
                }
            }
        }

        for (int y = 0; y < level.height; y++) {
            List<Integer> row = level.data.get(y);
            for (int x = 0; x < level.width; x++) {
                if (row.get(x) == 6) {
                    row.set(x, 2);
                }
            }
        }
    }

    private static boolean drawBridgeSafe(Level level, int x, int y, int thickness, int span,
            boolean orientationVertical) {
        int margin = 1;
        if (orientationVertical) {
            for (int ty = y - span - margin; ty <= y + span + margin; ty++) {
                for (int tx = x - (thickness - 1) / 2 - margin; tx <= x + thickness / 2 + margin; tx++) {
                    if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                        if (level.data.get(ty).get(tx) == 2)
                            return false;
                    }
                }
            }
        } else {
            for (int tx = x - span - margin; tx <= x + span + margin; tx++) {
                for (int ty = y - (thickness - 1) / 2 - margin; ty <= y + thickness / 2 + margin; ty++) {
                    if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                        if (level.data.get(ty).get(tx) == 2)
                            return false;
                    }
                }
            }
        }

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

    private static void drawRiverBlob(Level level, int cx, int cy, int size, int type) {
        for (int ty = cy - size; ty <= cy + size; ty++) {
            for (int tx = cx - size; tx <= cx + size; tx++) {
                if (tx >= 0 && tx < level.width && ty >= 0 && ty < level.height) {
                    double distSq = Math.pow(tx - cx, 2) + Math.pow(ty - cy, 2);
                    if (distSq <= size * size) {
                        int current = level.data.get(ty).get(tx);
                        // Protect Water (2) and Bridges (5) from being overwritten by new rivers
                        if (current != 2 && current != 5) {
                            level.data.get(ty).set(tx, type);
                        }
                    }
                }
            }
        }
    }

    private static List<int[]> findRiverPath(Level level, int startX, int startY, int endX, int endY, Random random) {
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

            int[][] neighbors = { { 0, 1 }, { 0, -1 }, { 1, 0 }, { -1, 0 }, { 1, 1 }, { 1, -1 }, { -1, 1 },
                    { -1, -1 } };
            for (int[] offset : neighbors) {
                int nx = current.x + offset[0];
                int ny = current.y + offset[1];

                if (nx < 0 || nx >= level.width || ny < 0 || ny >= level.height)
                    continue;
                String key = nx + "," + ny;
                if (closedSet.contains(key))
                    continue;

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
            path.add(0, new int[] { current.x, current.y });
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
