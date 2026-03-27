package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.example.Input;
import org.example.entity.Enemy;
import org.example.entity.Player;
import org.example.level.GameMap;
import org.example.level.Level;
import org.example.level.LevelLoader;
import org.example.item.Item;
import org.example.item.WorldItem;
import javafx.scene.input.MouseButton;

import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import org.example.level.LevelConfig;
import org.example.level.MapGenerator;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;

/**
 * Game state where the actual gameplay takes place.
 * Handles level rendering, logic, and UI (HUD/Inventory/Crafting).
 * Optimized for 1024x768 resolution.
 */
public class PlayState implements GameState {

    private Level currentLevel;
    private GameMap gameMap;
    private Player player;
    private List<Enemy> enemies;

    private double maxTime = 60.0;
    private double currentTime = maxTime;
    private boolean inTribulation = false;
    private double tribulationSpawnTimer = 0;

    private boolean isPaused = false;
    private boolean escWasPressed = false;
    private boolean inventoryOpen = false;
    private boolean inventoryWasPressed = false;
    private boolean showFullMap = false;
    private boolean mapWasPressed = false;

    /** Cached image of the map background for performance. */
    private WritableImage mapCache;

    // --- CAMERA SYSTEM ---
    /** Current X offset of the camera (world space to screen space). */
    private double cameraX = 0;
    /** Current Y offset of the camera (world space to screen space). */
    private double cameraY = 0;
    /** Game window width in pixels. */
    private final int screenWidth = 1024;
    /** Game window height in pixels. */
    private final int screenHeight = 768;

    // --- DRAG & DROP STATE ---
    private Item draggedItem = null;
    private Item[] sourceArr = null;
    private int sourceIdx = -1;
    private boolean lmbWasPressed = false;
    private boolean rmbWasPressed = false;

    private List<WorldItem> itemsOnGround;

    public PlayState() {
        // Load configuration from JSON
        LevelConfig config = LevelLoader.loadConfig("/levels/level_gen.json");
        currentLevel = MapGenerator.generate(config);
        
        gameMap = new GameMap(currentLevel);

        // Player starting position (middle of the large map for testing)
        player = new Player(config.width * config.tileSize / 2.0, config.height * config.tileSize / 2.0);

        maxTime = config.tribulationTime;
        currentTime = maxTime;

        enemies = new ArrayList<>();
        itemsOnGround = new ArrayList<>();
        spawnInitialEnemies();
        spawnInitialItems();
        addTestItems();

        generateMapCache();
    }

    /**
     * Updates the camera position to follow the player and stay within map boundaries.
     */
    private void updateCamera() {
        // Center camera on player
        cameraX = player.getX() + 6 - screenWidth / 2.0;
        cameraY = player.getY() + 6 - screenHeight / 2.0;

        // Clamp camera to map bounds
        double mapWidthPx = currentLevel.width * currentLevel.tileSize;
        double mapHeightPx = currentLevel.height * currentLevel.tileSize;

        if (cameraX < 0) cameraX = 0;
        if (cameraY < 0) cameraY = 0;
        if (cameraX > mapWidthPx - screenWidth) cameraX = mapWidthPx - screenWidth;
        if (cameraY > mapHeightPx - screenHeight) cameraY = mapHeightPx - screenHeight;
    }

    private void spawnInitialItems() {
        // Spawn some items on the ground for testing
        double[] pos1 = gameMap.getRandomFreePositionAwayFrom(16, player.getX(), player.getY(), 100);
        if (pos1 != null) itemsOnGround.add(new WorldItem(new Item("pill_qi_01", "Spirit Pill", "Ancient Qi recovery pill.", Item.Type.CONSUMABLE), pos1[0], pos1[1]));
        
        double[] pos2 = gameMap.getRandomFreePositionAwayFrom(16, player.getX(), player.getY(), 150);
        if (pos2 != null) itemsOnGround.add(new WorldItem(new Item("mat_hammer_01", "Rusty Hammer", "Crafting material.", Item.Type.CRAFTING), pos2[0], pos2[1]));
    }

    private void spawnInitialEnemies() {
        for (int i = 0; i < 50; i++) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(12, player.getX(), player.getY(), 200);
            if (pos != null) {
                enemies.add(new Enemy(pos[0], pos[1], false));
            }
        }
    }

    private void addTestItems() {
        if (player != null && player.getInventory() != null) {
            player.getInventory().addItem(new Item("sword_01", "Rusty Flying Sword", "A weathered cultivation tool.", Item.Type.WEAPON));
            player.getInventory().addItem(new Item("pill_01", "Qi Pill", "Restores a small amount of Qi.", Item.Type.CONSUMABLE));
        }
    }

    @Override
    public void update() {
        handleToggles();
        if (isPaused) return;

        if (inventoryOpen) {
            handleInventoryInteraction();
            return;
        }

        handleHotbarSelection();
        handleGameplayLogic();
        handleWorldInteraction();
        updateCamera();
    }

    private void handleWorldInteraction() {
        boolean rmbPressed = Input.isRmbPressed();
        if (rmbPressed && !rmbWasPressed) {
            double mx = Input.getMouseX();
            double my = Input.getMouseY();
            
            WorldItem toPick = null;
            for (WorldItem wi : itemsOnGround) {
                // Adjust mouse coordinates for camera offset when checking world items
                double worldMx = mx + cameraX;
                double worldMy = my + cameraY;

                if (wi.isClicked(worldMx, worldMy)) {
                    // Check distance (optional, but let's allow "infinite" reach for now or small limit)
                    double dx = wi.getX() - player.getX();
                    double dy = wi.getY() - player.getY();
                    if (Math.sqrt(dx*dx + dy*dy) < 150) { // Reach limit
                        toPick = wi;
                        break;
                    }
                }
            }
            
            if (toPick != null) {
                if (player.getInventory().addItem(toPick.getItem())) {
                    itemsOnGround.remove(toPick);
                    System.out.println("Picked up: " + toPick.getItem().getName());
                } else {
                    System.out.println("Inventory full!");
                }
            }
        }
        rmbWasPressed = rmbPressed;
    }

    private void handleToggles() {
        boolean escIsPressed = Input.isKeyPressed(KeyCode.ESCAPE);
        if (escIsPressed && !escWasPressed) isPaused = !isPaused;
        escWasPressed = escIsPressed;

        boolean invIsPressed = Input.isKeyPressed(KeyCode.I);
        if (invIsPressed && !inventoryWasPressed) {
            inventoryOpen = !inventoryOpen;
            if (!inventoryOpen && draggedItem != null && sourceArr != null) {
                sourceArr[sourceIdx] = draggedItem;
                draggedItem = null;
            }
        }
        inventoryWasPressed = invIsPressed;

        boolean mapIsPressed = Input.isKeyPressed(KeyCode.M);
        if (mapIsPressed && !mapWasPressed) showFullMap = !showFullMap;
        mapWasPressed = mapIsPressed;
    }

    private void handleHotbarSelection() {
        if (Input.isKeyPressed(KeyCode.DIGIT1)) player.setActiveHotbarSlot(0);
        if (Input.isKeyPressed(KeyCode.DIGIT2)) player.setActiveHotbarSlot(1);
        if (Input.isKeyPressed(KeyCode.DIGIT3)) player.setActiveHotbarSlot(2);
        if (Input.isKeyPressed(KeyCode.DIGIT4)) player.setActiveHotbarSlot(3);
        if (Input.isKeyPressed(KeyCode.DIGIT5)) player.setActiveHotbarSlot(4);

        if (Input.isKeyPressed(KeyCode.E)) {
            Item activeItem = player.getInventory().getItemInHotbar(player.getActiveHotbarSlot());
            if (activeItem != null) activeItem.use();
        }
    }

    private void handleGameplayLogic() {
        if (player.getHp() <= 0) {
            resetLevel();
            return;
        }

        player.update(currentLevel);
        for (Enemy enemy : enemies) enemy.update(gameMap, player, enemies);

        if (!inTribulation) {
            currentTime -= 1.0 / 60.0;
            if (currentTime <= 0) triggerTribulation();
        } else {
            tribulationSpawnTimer -= 1.0 / 60.0;
            if (tribulationSpawnTimer <= 0) {
                double[] pos = gameMap.getRandomFreePositionAwayFrom(24, player.getX(), player.getY(), 150);
                if (pos != null) enemies.add(new Enemy(pos[0], pos[1], true));
                tribulationSpawnTimer = 3.0;
            }
        }
    }

    private void triggerTribulation() {
        inTribulation = true;
        for (int i = 0; i < 2; i++) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(24, player.getX(), player.getY(), 250);
            if (pos != null) enemies.add(new Enemy(pos[0], pos[1], true));
        }
        tribulationSpawnTimer = 3.0;
    }

    private void resetLevel() {
        inTribulation = false;
        currentTime = maxTime;
        isPaused = false;
        // Load configuration from JSON
        LevelConfig config = LevelLoader.loadConfig("/levels/level_gen.json");
        currentLevel = MapGenerator.generate(config);
        gameMap = new GameMap(currentLevel);
        player = new Player(config.width * config.tileSize / 2.0, config.height * config.tileSize / 2.0);
        
        maxTime = config.tribulationTime;
        currentTime = maxTime;
        
        enemies.clear();
        spawnInitialEnemies();
        addTestItems();

        generateMapCache();
    }

    private void generateMapCache() {
        if (currentLevel == null || currentLevel.data == null) return;
        int w = currentLevel.width;
        int h = currentLevel.height;
        mapCache = new WritableImage(w, h);
        PixelWriter pw = mapCache.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int tile = currentLevel.data.get(y).get(x);
                Color c = Color.GREEN;
                if (tile == 2) c = Color.BLUE; // Water (Wait, is it 1 or 2? Checking renderMap...)
                else if (tile == 3) c = Color.CYAN; // Spirit Vein
                else if (tile == 4) c = Color.FORESTGREEN; // Variety
                else if (tile == 1) c = Color.DARKGRAY; // Wall/Obstacle
                pw.setColor(x, y, c);
            }
        }
    }

    /**
     * Main render loop for the game world.
     */
    @Override
    public void render(GraphicsContext gc) {
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();

        // Clear background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);

        renderMap(gc);
        
        // Render world entities with camera offset
        for (WorldItem wi : itemsOnGround) {
            wi.render(gc, cameraX, cameraY);
        }
        
        for (Enemy enemy : enemies) {
            enemy.render(gc, cameraX, cameraY);
        }
        
        player.render(gc, cameraX, cameraY);

        // --- HUD / UI ---
        renderHUD(gc);
        drawHotbar(gc, w, h);
        if (inventoryOpen) {
            drawInventory(gc, w, h);
        }
        
        if (draggedItem != null) {
            drawDraggedItem(gc);
        }
        // Render Maps
        renderMinimap(gc);
        if (showFullMap) renderFullMap(gc);

        if (isPaused) {
            gc.setFill(new Color(0, 0, 0, 0.5));
            gc.fillRect(0, 0, w, h);
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial", 48));
            gc.fillText("PAUSED", w / 2 - 100, h / 2);
        }
    }

    private void renderMinimap(GraphicsContext gc) {
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();
        
        double mapSize = 150;
        double padding = 20;
        double x = w - mapSize - padding;
        double y = padding;

        // Background / Border
        gc.setFill(new Color(0, 0, 0, 0.7));
        gc.fillRect(x - 2, y - 2, mapSize + 4, mapSize + 4);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(x - 2, y - 2, mapSize + 4, mapSize + 4);

        if (mapCache == null) return;

        // Map Image
        gc.drawImage(mapCache, x, y, mapSize, mapSize);

        // Entities
        double scale = mapSize / currentLevel.width;
        int ts = currentLevel.tileSize;
        
        // Enemies
        gc.setFill(Color.RED);
        for (Enemy e : enemies) {
            double ex = x + (e.getX() / ts) * scale;
            double ey = y + (e.getY() / ts) * scale;
            gc.fillOval(ex - 1.5, ey - 1.5, 3, 3);
        }

        // Player
        gc.setFill(Color.WHITE);
        double px = x + (player.getX() / ts) * scale;
        double py = y + (player.getY() / ts) * scale;
        gc.fillOval(px - 2, py - 2, 4, 4);
    }

    private void renderFullMap(GraphicsContext gc) {
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();

        double mapSize = 600;
        double x = (w - mapSize) / 2.0;
        double y = (h - mapSize) / 2.0;

        // Dim background
        gc.setFill(new Color(0, 0, 0, 0.8));
        gc.fillRect(0, 0, w, h);

        // Border
        gc.setFill(new Color(0.1, 0.1, 0.1, 1.0));
        gc.fillRect(x - 10, y - 40, mapSize + 20, mapSize + 50);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.strokeRect(x - 10, y - 40, mapSize + 20, mapSize + 50);

        // Title
        gc.setFill(Color.GOLD);
        gc.setFont(new Font("Arial", 24));
        gc.fillText("WORLD MAP ('M' to close)", x + 10, y - 10);

        if (mapCache == null) return;

        // Map Image
        gc.drawImage(mapCache, x, y, mapSize, mapSize);

        // Entities
        double scale = mapSize / currentLevel.width;
        int ts = currentLevel.tileSize;
        
        // Enemies
        gc.setFill(Color.RED);
        for (Enemy e : enemies) {
            double ex = x + (e.getX() / ts) * scale;
            double ey = y + (e.getY() / ts) * scale;
            gc.fillOval(ex - 2, ey - 2, 4, 4);
        }

        // Player
        gc.setFill(Color.WHITE);
        double px = x + (player.getX() / ts) * scale;
        double py = y + (player.getY() / ts) * scale;
        gc.fillOval(px - 4, py - 4, 8, 8);
    }

    private void renderMap(GraphicsContext gc) {
        if (currentLevel == null || currentLevel.data == null) return;
        int tileSize = currentLevel.tileSize;
        
        // Frustum Culling: Only render visible tiles
        int startX = Math.max(0, (int) (cameraX / tileSize));
        int endX = Math.min(currentLevel.width, (int) ((cameraX + screenWidth) / tileSize) + 1);
        int startY = Math.max(0, (int) (cameraY / tileSize));
        int endY = Math.min(currentLevel.height, (int) ((cameraY + screenHeight) / tileSize) + 1);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int tileType = currentLevel.data.get(y).get(x);
                if (tileType == 1) gc.setFill(Color.DARKGRAY);
                else if (tileType == 2) gc.setFill(Color.BLUE);
                else if (tileType == 3) gc.setFill(Color.MEDIUMPURPLE);
                else if (tileType == 4) gc.setFill(Color.DARKGREEN.deriveColor(0, 1, 0.8, 1)); // Lighter green for variety
                else gc.setFill(Color.DARKGREEN);
                
                gc.fillRect(x * tileSize - cameraX, y * tileSize - cameraY, tileSize, tileSize);
                // Optional: Grid lines for debugging (too heavy for 400x400?)
                // gc.setStroke(Color.BLACK);
                // gc.strokeRect(x * tileSize - cameraX, y * tileSize - cameraY, tileSize, tileSize);
            }
        }
    }

    private void renderHUD(GraphicsContext gc) {
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();
        // HP
        gc.setFill(Color.rgb(50, 50, 50, 0.7));
        gc.fillRect(20, 20, 300, 20);
        gc.setFill(Color.RED);
        gc.fillRect(20, 20, (player.getHp() / player.getMaxHp()) * 300, 20);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(20, 20, 300, 20);

        // Qi
        gc.setFill(Color.rgb(50, 50, 50, 0.7));
        gc.fillRect(20, 45, 150, 15);
        gc.setFill(Color.CYAN);
        gc.fillRect(20, 45, (player.getQi() / player.getMaxQi()) * 150, 15);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(20, 45, 150, 15);

        if (!inTribulation) {
            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 14));
            gc.fillText("Time: " + (int)currentTime + "s", 20, 85);
        } else {
            gc.setFill(Color.RED);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 24));
            gc.fillText("TRIBULATION!", 20, 85);
        }

        if (isPaused) {
            gc.setFill(Color.color(0, 0, 0, 0.6));
            gc.fillRect(0, 0, w, h);
            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 80));
            gc.fillText("PAUSED", w/2 - 160, h/2);
        }
    }

    private void drawHotbar(GraphicsContext gc, double w, double h) {
        double slotSize = 60, padding = 10, totalWidth = 5 * slotSize + 4 * padding;
        double startX = (w - totalWidth) / 2.0, startY = h - 85;

        gc.setFill(Color.rgb(30, 30, 30, 0.8));
        gc.fillRoundRect(startX - 15, startY - 15, totalWidth + 30, slotSize + 30, 15, 15);

        for (int i = 0; i < 5; i++) {
            double sx = startX + i * (slotSize + padding);
            gc.setFill(i == player.getActiveHotbarSlot() ? Color.rgb(80, 80, 120) : Color.rgb(40, 40, 50));
            gc.fillRect(sx, startY, slotSize, slotSize);
            gc.setStroke(i == player.getActiveHotbarSlot() ? Color.GOLD : Color.WHITE);
            gc.setLineWidth(i == player.getActiveHotbarSlot() ? 3 : 1);
            gc.strokeRect(sx, startY, slotSize, slotSize);

            Item item = player.getInventory().getItemInHotbar(i);
            if (item != null) drawItemIcon(gc, sx, startY, slotSize, item, Color.SKYBLUE);
            
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(new javafx.scene.text.Font("Arial", 12));
            gc.fillText(String.valueOf(i + 1), sx + 4, startY + 16);
        }
    }

    private void drawInventory(GraphicsContext gc, double w, double h) {
        gc.setFill(Color.color(0, 0, 0, 0.75));
        gc.fillRect(0, 0, w, h);

        double panelW = 800, panelH = 500; // Reduced height
        double panelX = (w - panelW) / 2, panelY = (h - panelH) / 2;

        gc.setFill(Color.rgb(25, 25, 30));
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.fillRect(panelX, panelY, panelW, panelH);
        gc.strokeRect(panelX, panelY, panelW, panelH);

        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial Bold", 28));
        gc.fillText("INVENTORY", panelX + 40, panelY + 50);
        gc.fillText("CRAFTING", panelX + panelW - 300, panelY + 50);

        gc.setStroke(Color.rgb(100, 100, 100));
        gc.setLineWidth(1);
        gc.strokeLine(panelX + 460, panelY + 30, panelX + 460, panelY + panelH - 30);

        double slotSize = 70, padding = 12;
        double startX = panelX + 40, startY = panelY + 80;

        // 5x5 Main Grid
        for (int i = 0; i < 25; i++) {
            double sx = startX + (i % 5) * (slotSize + padding);
            double sy = startY + (i / 5) * (slotSize + padding);
            drawSlot(gc, sx, sy, slotSize, player.getInventory().getItemInMain(i), Color.web("#444444"));
        }

        // Crafting Section
        double cX = panelX + 530, cY1 = panelY + 120, cY2 = panelY + 320, resX = panelX + 680, resY = panelY + 220;
        drawSlot(gc, cX, cY1, slotSize, player.getInventory().getCraftingInputs()[0], Color.web("#d4af37"));
        drawSlot(gc, cX, cY2, slotSize, player.getInventory().getCraftingInputs()[1], Color.web("#d4af37"));
        drawSlot(gc, resX, resY, slotSize + 15, player.getInventory().getCraftingResult(), Color.web("#2ecc71"));

        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial Bold", 40));
        gc.fillText("+", cX + 22, cY1 + slotSize + 55);
        gc.fillText("=", cX + slotSize + 30, resY + 55);
    }

    private void drawSlot(GraphicsContext gc, double x, double y, double size, Item item, Color borderColor) {
        gc.setFill(Color.rgb(40, 40, 45));
        gc.fillRect(x, y, size, size);
        gc.setStroke(borderColor);
        gc.setLineWidth(borderColor == Color.GOLD || borderColor.equals(Color.web("#2ecc71")) ? 3 : 1);
        gc.strokeRect(x, y, size, size);
        if (item != null) drawItemIcon(gc, x, y, size, item, Color.ORANGE);
    }

    private void drawItemIcon(GraphicsContext gc, double x, double y, double size, Item item, Color color) {
        gc.setFill(color);
        gc.fillRect(x + 5, y + 5, size - 10, size - 10);
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial Bold", 12));
        String name = item.getName();
        if (name.length() > 8) name = name.substring(0, 8);
        gc.fillText(name, x + 5, y + size - 12);
    }

    private void handleInventoryInteraction() {
        double mx = Input.getMouseX(), my = Input.getMouseY(), w = 1024, h = 768; // Hardcoded for logic consistency
        boolean lmbPressed = Input.isLmbPressed();
        
        double panelW = 800, panelH = 550, panelX = (w - panelW)/2, panelY = (h - panelH)/2;
        double slotSize = 70, padding = 12, startX = panelX + 40, startY = panelY + 80;

        if (lmbPressed && !lmbWasPressed && draggedItem == null) {
            // Main slots
            for (int i = 0; i < 25; i++) {
                double sx = startX + (i % 5)* (slotSize + padding), sy = startY + (i / 5)* (slotSize + padding);
                if (isInside(mx, my, sx, sy, slotSize)) { 
                    draggedItem = player.getInventory().getMainInventory()[i];
                    if (draggedItem != null) { sourceArr = player.getInventory().getMainInventory(); sourceIdx = i; sourceArr[i] = null; }
                    break;
                }
            }
            // Crafting
            if (draggedItem == null) {
                double cX = panelX + 30 + 500, cYs[] = {panelY + 120, panelY + 320}; // Adjusted
                cX = panelX + 530;
                for (int i = 0; i < 2; i++) {
                    if (isInside(mx, my, cX, cYs[i], slotSize)) {
                        draggedItem = player.getInventory().getCraftingInputs()[i];
                        if (draggedItem != null) { sourceArr = player.getInventory().getCraftingInputs(); sourceIdx = i; sourceArr[i] = null; }
                        break;
                    }
                }
            }
            // Result
            if (draggedItem == null) {
                if (isInside(mx, my, panelX + 680, panelY + 220, slotSize + 15)) {
                    draggedItem = player.getInventory().getCraftingResult();
                    if (draggedItem != null) { player.getInventory().consumeCraftingInputs(); sourceArr = null; sourceIdx = -1; }
                }
            }
            // Hotbar (HUD only)
            if (draggedItem == null) {
                double hudS = 60, hudP = 10, hX = (w - (5*hudS + 4*hudP))/2, hY = h - 85;
                for (int i = 0; i < 5; i++) {
                    if (isInside(mx, my, hX + i * (hudS + hudP), hY, hudS)) {
                        draggedItem = player.getInventory().getHotbar()[i];
                        if (draggedItem != null) { sourceArr = player.getInventory().getHotbar(); sourceIdx = i; sourceArr[i] = null; }
                        break;
                    }
                }
            }
        }

        if (!lmbPressed && lmbWasPressed && draggedItem != null) {
            handleDrop(mx, my, w, h, panelX, panelY, panelW, panelH, slotSize, padding, startX, startY);
        }
        lmbWasPressed = lmbPressed;
    }

    private void handleDrop(double mx, double my, double w, double h, double px, double py, double pw, double ph, double ss, double pd, double sx, double sy) {
        boolean dropped = false;
        // Main
        for (int i = 0; i < 25; i++) {
            if (isInside(mx, my, sx + (i % 5)* (ss + pd), sy + (i / 5)* (ss + pd), ss)) {
                player.getInventory().swapSlots(new Item[]{draggedItem}, 0, player.getInventory().getMainInventory(), i);
                dropped = true; break;
            }
        }
        // Crafting
        if (!dropped) {
            double cX = px + 530, cYs[] = {py + 120, py + 320};
            for (int i = 0; i < 2; i++) {
                if (isInside(mx, my, cX, cYs[i], ss)) {
                    player.getInventory().swapSlots(new Item[]{draggedItem}, 0, player.getInventory().getCraftingInputs(), i);
                    dropped = true; break;
                }
            }
        }
        // HUD Hotbar
        if (!dropped) {
            double hudS = 60, hudP = 10, hX = (w - (5*hudS + 4*hudP))/2, hY = h - 85;
            for (int i = 0; i < 5; i++) {
                if (isInside(mx, my, hX + i*(hudS+hudP), hY, hudS)) {
                    player.getInventory().swapSlots(new Item[]{draggedItem}, 0, player.getInventory().getHotbar(), i);
                    dropped = true; break;
                }
            }
        }

        if (!dropped && sourceArr != null) sourceArr[sourceIdx] = draggedItem;
        else if (!dropped && sourceArr == null) player.getInventory().addItem(draggedItem);
        draggedItem = null; sourceArr = null; sourceIdx = -1;
    }

    private boolean isInside(double mx, double my, double x, double y, double s) {
        return mx >= x && mx <= x + s && my >= y && my <= y + s;
    }

    private void drawDraggedItem(GraphicsContext gc) {
        double mx = Input.getMouseX(), my = Input.getMouseY(), s = 60;
        gc.setGlobalAlpha(0.8);
        gc.setFill(Color.ORANGE);
        gc.fillRect(mx - s/2, my - s/2, s, s);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(mx - s/2, my - s/2, s, s);
        gc.setGlobalAlpha(1.0);
    }
}