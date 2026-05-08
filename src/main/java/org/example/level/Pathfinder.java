package org.example.level;

import java.util.*;

/**
 * Utility class implementing the A* pathfinding algorithm for a grid.
 * Used for navigating enemies towards the player through obstacles.
 */
public class Pathfinder {

    /**
     * Internal representation of a node for the A* algorithm.
     */
    private static class Node implements Comparable<Node> {
        int x, y;
        int gScore; // Cost from start to this node
        int fScore; // Estimated total cost (g + h)
        Node parent;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
            this.gScore = Integer.MAX_VALUE;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fScore, other.fScore);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    /**
     * Finds a path from starting tile to target tile using A*.
     * 
     * @param map GameMap for collision checks (walls).
     * @param startX Starting X coordinate in the grid.
     * @param startY Starting Y coordinate in the grid.
     * @param targetX Target X coordinate in the grid.
     * @param targetY Target Y coordinate in the grid.
     * @return List of tile coordinates [x, y] representing the path, or null if no path found.
     */
    public static List<int[]> findPath(GameMap map, int startX, int startY, int targetX, int targetY) {
        // If target is in a wall, no path exists
        if (map.isSolid(targetX, targetY)) return null;

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<String, Node> allNodes = new HashMap<>();

        Node startNode = new Node(startX, startY);
        startNode.gScore = 0;
        startNode.fScore = heuristic(startX, startY, targetX, targetY);
        
        openSet.add(startNode);
        allNodes.put(startX + "," + startY, startNode);

        Set<String> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = current.x + "," + current.y;

            // Target reached
            if (current.x == targetX && current.y == targetY) {
                return reconstructPath(current);
            }

            closedSet.add(currentKey);

            // Check 8 neighbors (cardinal + diagonal)
            int[][] neighbors = {
                {0, 1, 10}, {0, -1, 10}, {1, 0, 10}, {-1, 0, 10}, // Cardinal (cost 10)
                {1, 1, 14}, {1, -1, 14}, {-1, 1, 14}, {-1, -1, 14} // Diagonal (cost 14 ~ sqrt(2)*10)
            };
            for (int[] offset : neighbors) {
                int nx = current.x + offset[0];
                int ny = current.y + offset[1];
                int moveCost = offset[2];
                String neighborKey = nx + "," + ny;

                if (map.isSolid(nx, ny) || closedSet.contains(neighborKey)) {
                    continue;
                }

                // Prevent "corner cutting" - don't move diagonally if both adjacent cardinal walls are solid
                if (Math.abs(offset[0]) == 1 && Math.abs(offset[1]) == 1) {
                    if (map.isSolid(current.x + offset[0], current.y) && map.isSolid(current.x, current.y + offset[1])) {
                        continue;
                    }
                }

                int tentativeGScore = current.gScore + moveCost;
                Node neighbor = allNodes.getOrDefault(neighborKey, new Node(nx, ny));

                if (tentativeGScore < neighbor.gScore || !openSet.contains(neighbor)) {
                    neighbor.parent = current;
                    neighbor.gScore = tentativeGScore;
                    neighbor.fScore = neighbor.gScore + heuristic(nx, ny, targetX, targetY) * 10;
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                        allNodes.put(neighborKey, neighbor);
                    }
                }
            }
        }

        return null; // Path not found
    }

    /**
     * Heuristic function for distance estimation between two grid points.
     * Calculates Euclidean distance multiplied by 10 for integer processing.
     * 
     * @param x1 Start X.
     * @param y1 Start Y.
     * @param x2 Target X.
     * @param y2 Target Y.
     * @return Estimated cost to target.
     */
    private static int heuristic(int x1, int y1, int x2, int y2) {
        return (int) (Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2)) * 10);
    }

    /**
     * Reconstructs the calculated path from target node back to start.
     * 
     * @param node The target node from which to backtrack.
     * @return List of tile coordinates [x, y] in order from start to target.
     */
    private static List<int[]> reconstructPath(Node node) {
        List<int[]> path = new ArrayList<>();
        Node current = node;
        while (current != null) {
            path.add(0, new int[]{current.x, current.y});
            current = current.parent;
        }
        // Remove start node (we are already there)
        if (!path.isEmpty()) path.remove(0);
        return path;
    }
}
