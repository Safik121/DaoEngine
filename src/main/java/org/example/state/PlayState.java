package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;

import org.example.Input;
import org.example.entity.Enemy;
import org.example.entity.Player;
import org.example.entity.Projectile;
import org.example.entity.LightningStrike;
import org.example.level.GameMap;
import org.example.level.Level;
import org.example.level.LevelLoader;
import org.example.level.LevelConfig;
import org.example.level.MapGenerator;
import org.example.item.Item;
import org.example.item.WorldItem;
import org.example.item.WeaponRegistry;
import org.example.item.WeaponConfig;
import org.example.item.ItemRegistry;
import org.example.entity.InteractableEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Game state where the actual gameplay takes place.
 * Handles level rendering, logic, and UI (HUD/Inventory/Crafting).
 * Optimized for 1024x768 resolution.
 */
public class PlayState implements GameState {

    /** The current level data being played. */
    private Level currentLevel;
    /** Spatial manager for the current level. */
    private GameMap gameMap;
    /** The player entity. */
    private Player player;
    /** List of all active enemies in the level. */
    private List<Enemy> enemies;

    /** Maximum time before a Tribulation starts (in seconds). */
    private double maxTime = 60.0;
    /** Remaining time before the next Tribulation phase. */
    private double currentTime = maxTime;
    /** Flag indicating if a Tribulation event is currently active. */
    private boolean inTribulation = false;
    /** Timer for periodic enemy spawning during Tribulation. */
    private double tribulationSpawnTimer = 0;
    /** Timer for periodic lightning strikes during Tribulation. */
    private double lightningTimer = 0;
    /** List of all active lightning strikes during Tribulation. */
    private List<LightningStrike> activeStrikes = new ArrayList<>();

    /** Flag indicating if the game logic is currently paused. */
    private boolean isPaused = false;
    /** Input buffer to detect ESC key releases. */
    private boolean escWasPressed = false;
    /** Toggle for the inventory/crafting UI overlay. */
    private boolean inventoryOpen = false;
    /** Input buffer to detect 'I' key releases. */
    private boolean inventoryWasPressed = false;
    /** Toggle for the full-screen world map overlay. */
    private boolean showFullMap = false;
    /** Input buffer to detect 'M' key releases. */
    private boolean mapWasPressed = false;
    /** Input buffer to detect 'E' key releases. */
    private boolean eWasPressed = false;

    /** The currently active NPC/Stele dialogue, if any. */
    private InteractableEntity activeDialogue = null;
    /** Current index of the line being displayed in the active dialogue. */
    private int dialogueIndex = 0;
    /** Flag to prevent combat input while in dialogue. */
    private boolean inDialogue = false;

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
    /** The item currently being dragged by the mouse. */
    private Item draggedItem = null;
    /** The inventory array from which the item was taken. */
    private Item[] sourceArr = null;
    /** The original index of the dragged item in its source array. */
    private int sourceIdx = -1;
    /** Input buffer for the Left Mouse Button. */
    private boolean lmbWasPressed = false;
    /** Input buffer for the Right Mouse Button. */
    private boolean rmbWasPressed = false;

    /** List of active projectiles in the world. */
    private List<Projectile> projectiles;
    /** List of items dropped on the ground in the game world. */
    private List<WorldItem> itemsOnGround;

    public PlayState() {
        // Load configurations from JSON
        WeaponRegistry.loadWeapons("/weapons/weapon_configs.json");
        ItemRegistry.loadData("/items/items.json", "/items/recipes.json");

        // Load configuration from JSON (Small level for faster testing)
        LevelConfig config = LevelLoader.loadConfig("/levels/level_small.json");
        currentLevel = MapGenerator.generate(config);

        gameMap = new GameMap(currentLevel);

        // Player starting position (middle of the map)
        player = new Player(config.width * config.tileSize / 2.0, config.height * config.tileSize / 2.0);

        maxTime = config.tribulationTime;
        currentTime = maxTime;

        enemies = new ArrayList<>();
        itemsOnGround = new ArrayList<>();
        projectiles = new ArrayList<>();
        activeStrikes = new ArrayList<>();
        spawnInitialEnemies();
        spawnInitialItems();
        addTestItems();

        generateMapCache();
    }

    /**
     * Updates the camera position to follow the player and stay within map
     * boundaries.
     */
    private void updateCamera() {
        // Center camera on player
        cameraX = player.getX() + 6 - screenWidth / 2.0;
        cameraY = player.getY() + 6 - screenHeight / 2.0;

        // Clamp camera to map bounds
        double mapWidthPx = currentLevel.width * currentLevel.tileSize;
        double mapHeightPx = currentLevel.height * currentLevel.tileSize;

        double padding = 100;
        if (cameraX < -padding)
            cameraX = -padding;
        if (cameraY < -padding)
            cameraY = -padding;
        if (cameraX > mapWidthPx - screenWidth + padding)
            cameraX = mapWidthPx - screenWidth + padding;
        if (cameraY > mapHeightPx - screenHeight + padding)
            cameraY = mapHeightPx - screenHeight + padding;
    }

    /**
     * Spawns initial items on the ground at random valid positions.
     */
    private void spawnInitialItems() {
        // Spawn some items on the ground for testing
        double[] pos1 = gameMap.getRandomFreePositionAwayFrom(16, player.getX(), player.getY(), 100);
        if (pos1 != null)
            itemsOnGround.add(new WorldItem(ItemRegistry.createItem("pill_qi_01"), pos1[0], pos1[1]));

        double[] pos2 = gameMap.getRandomFreePositionAwayFrom(16, player.getX(), player.getY(), 150);
        if (pos2 != null)
            itemsOnGround.add(new WorldItem(ItemRegistry.createItem("mat_hammer_01"), pos2[0], pos2[1]));
    }

    /**
     * Spawns initial enemies at random valid positions away from the player.
     */
    private void spawnInitialEnemies() {
        int enemyCount = 10; // Reduced for the small test map
        for (int i = 0; i < enemyCount; i++) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(12, player.getX(), player.getY(), 200);
            if (pos != null) {
                enemies.add(new Enemy(pos[0], pos[1], false));
            }
        }
    }

    /**
     * Adds initial starting items to the player's inventory for testing.
     */
    private void addTestItems() {
        if (player != null && player.getInventory() != null) {
            player.getInventory().addItem(ItemRegistry.createItem("sword_01"));
            player.getInventory().addItem(ItemRegistry.createItem("fireball_staff"));
            player.getInventory().addItem(ItemRegistry.createItem("divine_eyes"));
            player.getInventory().addItem(ItemRegistry.createItem("realm_token"));
        }
    }

    /**
     * Main update routine called every frame.
     * Manages input toggles, logic transitions, camera, and specific interaction
     * modes.
     * 
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    @Override
    public void update(double deltaTime) {
        handleToggles();

        if (isPaused)
            return;

        if (inventoryOpen) {
            handleInventoryInteraction();
        } else if (showFullMap) {
            // Full map interactions could go here
        }

        // --- Live Gameplay Updates ---
        // Even when Inventory or Map is open, the world continues to run!
        handleHotbarSelection();
        handleWorldInteraction();
        handleGameplayLogic(deltaTime);
        updateCamera();
    }

    /**
     * Handles interaction with objects in the game world (e.g., picking up items).
     * Now triggered by the 'E' key and uses proximity check for the nearest item.
     */
    private void handleWorldInteraction() {
        boolean ePressed = Input.isKeyPressed(KeyCode.E);
        if (ePressed && !eWasPressed) {
            // --- Interactable Interaction (NPCs/Steles) ---
            if (activeDialogue == null) {
                InteractableEntity bestInteractable = null;
                double minIdx = 60.0;
                for (InteractableEntity ie : currentLevel.interactables) {
                    double dist = Math.sqrt(
                            Math.pow(ie.getX() - player.getX(), 2) + Math.pow(ie.getY() - player.getY(), 2));
                    if (dist < minIdx) {
                        minIdx = dist;
                        bestInteractable = ie;
                    }
                }
                if (bestInteractable != null) {
                    activeDialogue = bestInteractable;
                    dialogueIndex = 0;
                    inDialogue = true;
                    eWasPressed = ePressed;
                    return; // Enter dialogue mode
                }
            } else {
                // Advance dialogue
                dialogueIndex++;
                if (dialogueIndex >= activeDialogue.getDialogueLines().size()) {
                    // Check for reward
                    if (activeDialogue.getRewardItem() != null && !activeDialogue.hasGivenReward()) {
                        if (player.getInventory().addItem(activeDialogue.getRewardItem())) {
                            System.out.println("Received reward from " + activeDialogue.getName());
                            activeDialogue.setHasGivenReward(true);
                        } else {
                            System.out.println("Inventory full! Could not receive reward.");
                        }
                    }
                    activeDialogue = null;
                    inDialogue = false;
                }
                eWasPressed = ePressed;
                return;
            }

            WorldItem bestItem = null;
            double minDist = 150.0; // Reach limit

            for (WorldItem wi : itemsOnGround) {
                double dist = Math.sqrt(Math.pow(wi.getX() - player.getX(), 2) + Math.pow(wi.getY() - player.getY(), 2));
                if (dist < minDist) {
                    minDist = dist;
                    bestItem = wi;
                }
            }

            if (bestItem != null) {
                if (player.getInventory().addItem(bestItem.getItem())) {
                    itemsOnGround.remove(bestItem);
                    System.out.println("Picked up: " + bestItem.getItem().getName());
                } else {
                    System.out.println("Inventory full!");
                }
            }

            // --- Gate of Realms Interaction ---
            if (currentLevel.gate != null) {
                double distG = Math.sqrt(
                        Math.pow(currentLevel.gate.getX() - player.getX(), 2)
                                + Math.pow(currentLevel.gate.getY() - player.getY(), 2));
                if (distG < 60) {
                    if (player.getInventory().hasItem("realm_token")) {
                        nextLevel();
                    } else {
                        System.out.println("You lack the required artifact to transcend!");
                    }
                }
            }
        }
        eWasPressed = ePressed;
    }

    private void handleToggles() {
        boolean escIsPressed = Input.isKeyPressed(KeyCode.ESCAPE);
        if (escIsPressed && !escWasPressed)
            isPaused = !isPaused;
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
        if (mapIsPressed && !mapWasPressed)
            showFullMap = !showFullMap;
        mapWasPressed = mapIsPressed;
    }

    private void handleHotbarSelection() {
        if (Input.isKeyPressed(KeyCode.DIGIT1))
            player.setActiveHotbarSlot(0);
        if (Input.isKeyPressed(KeyCode.DIGIT2))
            player.setActiveHotbarSlot(1);
        if (Input.isKeyPressed(KeyCode.DIGIT3))
            player.setActiveHotbarSlot(2);
        if (Input.isKeyPressed(KeyCode.DIGIT4))
            player.setActiveHotbarSlot(3);
        if (Input.isKeyPressed(KeyCode.DIGIT5))
            player.setActiveHotbarSlot(4);

        if (Input.isKeyPressed(KeyCode.F)) {
            int activeSlot = player.getActiveHotbarSlot();
            Item activeItem = player.getInventory().getItemInHotbar(activeSlot);
            if (activeItem != null) {
                // Apply effects to player
                activeItem.use(player);
                
                // If it's a consumable (like a pill), remove it after use
                if (activeItem.getType() == Item.Type.CONSUMABLE) {
                    player.getInventory().getHotbar()[activeSlot] = null;
                }
            }
        }

        handleCombatInput();
    }

    /**
     * Handles player combat input (Left Mouse Button to fire active weapon).
     */
    private void handleCombatInput() {
        if (inventoryOpen || showFullMap || isPaused || activeDialogue != null)
            return;

        if (Input.isLmbPressed() && player.canAttack()) {
            Item activeItem = player.getInventory().getItemInHotbar(player.getActiveHotbarSlot());
            if (activeItem != null && activeItem.getType() == Item.Type.WEAPON) {
                WeaponConfig wConfig = activeItem.getWeaponConfig();
                if (wConfig != null) {
                    // Check Qi cost
                    if (player.spendQi(wConfig.qiCost)) {
                        // Calculate angle to mouse
                        double mx = Input.getMouseX() + cameraX;
                        double my = Input.getMouseY() + cameraY;
                        double angle = Math.atan2(my - (player.getY() + 6), mx - (player.getX() + 6));

                        projectiles.add(new Projectile(player.getX() + 6, player.getY() + 6, angle, wConfig));
                        player.setAttackCooldown(wConfig.cooldown);
                    }
                }
            }
        }
    }


    /**
     * Transitions to the next level by regenerating the world.
     */
    private void nextLevel() {
        System.out.println("Transcending to the next realm...");
        
        // Reset state
        currentTime = maxTime;
        inTribulation = false;
        activeStrikes.clear();
        enemies.clear();
        projectiles.clear();
        itemsOnGround.clear();
        
        // Load configuration and regenerate (Using small level for testing)
        LevelConfig config = LevelLoader.loadConfig("/levels/level_small.json");
        currentLevel = MapGenerator.generate(config);
        gameMap = new GameMap(currentLevel);
        
        // Reposition player
        player.setX(currentLevel.width * currentLevel.tileSize / 2.0);
        player.setY(currentLevel.height * currentLevel.tileSize / 2.0);
        
        // Re-spawn initial entities and refresh cache
        spawnInitialEnemies();
        spawnInitialItems();
        generateMapCache();
    }

    /**
     * Core gameplay logic update.
     * Manages player/enemy updates, death checks, and Tribulation timers.
     * 
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    private void handleGameplayLogic(double deltaTime) {
        if (player.getHp() <= 0) {
            resetLevel();
            return;
        }

        player.update(currentLevel, deltaTime);
        for (Enemy enemy : enemies)
            enemy.update(gameMap, player, enemies, deltaTime);

        // Update Projectiles
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(gameMap, deltaTime);

            // Collision with enemies
            for (Enemy enemy : enemies) {
                if (p.checkCollision(enemy)) {
                    enemy.takeDamage(p.getDamage());
                    if (p.getType() != WeaponConfig.ProjectileType.BEAM) {
                        p.deactivate();
                        break;
                    }
                }
            }

            if (!p.isActive()) {
                projectiles.remove(i);
            }
        }

        // Clean up dead enemies
        enemies.removeIf(Enemy::isDead);

        if (!inTribulation) {
            currentTime -= deltaTime;
            if (currentTime <= 0)
                triggerTribulation();
        } else {
            tribulationSpawnTimer -= deltaTime;
            if (tribulationSpawnTimer <= 0) {
                spawnEnemyNearPlayer();
                tribulationSpawnTimer = 2.0;
            }

            lightningTimer -= deltaTime;
            if (lightningTimer <= 0) {
                // Target player's current position to force movement
                double lx = player.getX() + 6; // Center on player
                double ly = player.getY() + 6; 
                activeStrikes.add(new LightningStrike(lx, ly));
                lightningTimer = 1.0 + Math.random() * 2.0; // Slightly slower: 1.0 - 3.0s
            }
        }

        // Update Lightning Strikes
        for (int i = activeStrikes.size() - 1; i >= 0; i--) {
            LightningStrike strike = activeStrikes.get(i);
            strike.update(deltaTime);

            if (strike.isDealingDamage()) {
                // Damage player
                double distP = Math.sqrt(Math.pow(strike.getX() - player.getX(), 2) + Math.pow(strike.getY() - player.getY(), 2));
                if (distP < strike.getRadius()) {
                    player.takeDamage(20);
                }
                // Damage enemies
                for (Enemy e : enemies) {
                    double distE = Math.sqrt(Math.pow(strike.getX() - e.getX(), 2) + Math.pow(strike.getY() - e.getY(), 2));
                    if (distE < strike.getRadius()) {
                        e.takeDamage(40);
                    }
                }
                strike.markDamaged();
            }

            if (strike.isExpired()) {
                activeStrikes.remove(i);
            }
        }
    }

    /**
     * Spawns a single enemy at a random location away from the player.
     * Used during the Tribulation phase.
     */
    private void spawnEnemyNearPlayer() {
        double[] pos = gameMap.getRandomFreePositionAwayFrom(24, player.getX(), player.getY(), 150);
        if (pos != null)
            enemies.add(new Enemy(pos[0], pos[1], true));
    }

    /**
     * Activates the Tribulation phase, increasing difficulty and spawning dangerous
     * enemies.
     */
    private void triggerTribulation() {
        inTribulation = true;
        for (int i = 0; i < 2; i++) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(24, player.getX(), player.getY(), 250);
            if (pos != null)
                enemies.add(new Enemy(pos[0], pos[1], true));
        }
        tribulationSpawnTimer = 3.0;
    }

    /**
     * Completely resets the level by regenerating the map and resetting entity
     * states.
     * Triggered on player death.
     */
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

    /**
     * Generates a 1:1 pixel representation of the map for minimap rendering.
     * Colors each pixel based on the underlying tile type.
     */
    private void generateMapCache() {
        if (currentLevel == null || currentLevel.data == null)
            return;
        int w = currentLevel.width;
        int h = currentLevel.height;
        mapCache = new WritableImage(w, h);
        PixelWriter pw = mapCache.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int tile = currentLevel.data.get(y).get(x);
                Color c = Color.GREEN;
                if (tile == 2)
                    c = Color.BLUE; // Water (Wait, is it 1 or 2? Checking renderMap...)
                else if (tile == 3)
                    c = Color.CYAN; // Spirit Vein
                else if (tile == 4)
                    c = Color.FORESTGREEN; // Variety
                else if (tile == 1)
                    c = Color.DARKGRAY; // Wall/Obstacle
                else if (tile == 5)
                    c = Color.SADDLEBROWN; // Bridge
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

        // Projectiles
        for (Projectile p : projectiles)
            p.render(gc, cameraX, cameraY);

        // Lightning Strikes
        for (LightningStrike strike : activeStrikes)
            strike.render(gc, cameraX, cameraY);

        // Gate of Realms
        if (currentLevel.gate != null) {
            currentLevel.gate.update(0.016); // Simple fixed dt for animation
            currentLevel.gate.render(gc, cameraX, cameraY);
        }

        // Interactables (NPCs/Steles)
        for (InteractableEntity ie : currentLevel.interactables) {
            ie.render(gc, cameraX, cameraY);
            
            // Interaction visual prompt
            double dist = Math.sqrt(Math.pow(ie.getX() - player.getX(), 2) + Math.pow(ie.getY() - player.getY(), 2));
            if (activeDialogue == null && dist < 70) {
                gc.setFill(Color.GOLD);
                gc.setFont(new javafx.scene.text.Font("Arial Bold", 14));
                gc.fillText("[E]", ie.getX() - cameraX + 4, ie.getY() - cameraY - 8);
            }
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
        if (showFullMap)
            renderFullMap(gc);

        // Dialogue UI
        if (activeDialogue != null) {
            renderDialogue(gc);
        }

        if (isPaused) {
            gc.setFill(new Color(0, 0, 0, 0.5));
            gc.fillRect(0, 0, w, h);
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Arial", 48));
            gc.fillText("PAUSED", w / 2 - 100, h / 2);
        }
    }

    /**
     * Renders a small minimap in the top-right corner.
     * Shows the explored map area and active entities (Player/Enemies).
     * 
     * @param gc The GraphicsContext used for drawing.
     */
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

        if (mapCache == null)
            return;

        // Map Image
        gc.drawImage(mapCache, x, y, mapSize, mapSize);

        // Entities
        double scale = mapSize / currentLevel.width;
        int ts = currentLevel.tileSize;

        // Items
        gc.setFill(Color.GOLD);
        for (org.example.item.WorldItem wi : itemsOnGround) {
            double ix = x + (wi.getX() / ts) * scale;
            double iy = y + (wi.getY() / ts) * scale;
            gc.fillOval(ix - 1.25, iy - 1.25, 2.5, 2.5);
        }

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

    /**
     * Renders a full-screen interactive world map.
     * Dimens the background and provides higher detail than the minimap.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
    private void renderFullMap(GraphicsContext gc) {
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();

        double mapSize = 600;
        double x = (w - mapSize) / 2.0;
        double y = (h - mapSize) / 2.0;

        // Dim background (Live UI: more transparent)
        gc.setFill(new Color(0, 0, 0, 0.45));
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

        if (mapCache == null)
            return;

        // Map Image
        gc.drawImage(mapCache, x, y, mapSize, mapSize);

        // Entities
        double scale = mapSize / currentLevel.width;
        int ts = currentLevel.tileSize;

        // Items
        gc.setFill(Color.GOLD);
        for (org.example.item.WorldItem wi : itemsOnGround) {
            double ix = x + (wi.getX() / ts) * scale;
            double iy = y + (wi.getY() / ts) * scale;
            gc.fillOval(ix - 2, iy - 2, 4, 4);
        }

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

    /**
     * Renders the visible portion of the game map (Tiles).
     * Implements basic frustum culling for performance on large maps.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
    private void renderMap(GraphicsContext gc) {
        if (currentLevel == null || currentLevel.data == null)
            return;
        int tileSize = currentLevel.tileSize;

        // Frustum Culling: Only render visible tiles
        int startX = Math.max(0, (int) (cameraX / tileSize));
        int endX = Math.min(currentLevel.width, (int) ((cameraX + screenWidth) / tileSize) + 1);
        int startY = Math.max(0, (int) (cameraY / tileSize));
        int endY = Math.min(currentLevel.height, (int) ((cameraY + screenHeight) / tileSize) + 1);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int tileType = currentLevel.data.get(y).get(x);
                if (tileType == 1)
                    gc.setFill(Color.DARKGRAY);
                else if (tileType == 2)
                    gc.setFill(Color.BLUE);
                else if (tileType == 3)
                    gc.setFill(Color.MEDIUMPURPLE);
                else if (tileType == 4)
                    gc.setFill(Color.DARKGREEN.deriveColor(0, 1, 0.8, 1)); // Lighter green for variety
                else if (tileType == 5)
                    gc.setFill(Color.SADDLEBROWN); // Bridge
                else
                    gc.setFill(Color.DARKGREEN);

                gc.fillRect(x * tileSize - cameraX, y * tileSize - cameraY, tileSize, tileSize);
                // Optional: Grid lines for debugging (too heavy for 400x400?)
                // gc.setStroke(Color.BLACK);
                // gc.strokeRect(x * tileSize - cameraX, y * tileSize - cameraY, tileSize,
                // tileSize);
            }
        }
    }

    /**
     * Renders the Heads-Up Display (HUD).
     * Shows HP bars, Qi bars, and time/status labels.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
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
            gc.fillText("Time: " + (int) currentTime + "s", 20, 85);
        } else {
            gc.setFill(Color.RED);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 24));
            gc.fillText("TRIBULATION!", 20, 85);
        }
    }

    /**
     * Renders the active hotbar at the bottom of the screen.
     * 
     * @param gc The GraphicsContext used for drawing.
     * @param w  Canvas width.
     * @param h  Canvas height.
     */
    /**
     * Renders the hotbar HUD at the bottom of the screen.
     * Highlights the active slot and displays icons for assigned items.
     * 
     * @param gc The GraphicsContext used for drawing.
     * @param w  Canvas width.
     * @param h  Canvas height.
     */
    private void drawHotbar(GraphicsContext gc, double w, double h) {
        double slotSize = 60, padding = 10, totalWidth = 5 * slotSize + 4 * padding;
        double startX = (w - totalWidth) / 2.0, startY = h - 85;

        // Adaptive Alpha: If player is at the bottom, make UI extra transparent
        double drawAlpha = 0.8;
        double screenY = (player.getY() + 6) - cameraY;
        if (screenY > h - 160) drawAlpha = 0.35;

        gc.setGlobalAlpha(drawAlpha / 0.8 * 0.8); // Scale base alpha
        gc.setFill(Color.rgb(30, 30, 30, drawAlpha));
        gc.fillRoundRect(startX - 15, startY - 15, totalWidth + 30, slotSize + 30, 15, 15);
        gc.setGlobalAlpha(1.0);

        for (int i = 0; i < 5; i++) {
            double sx = startX + i * (slotSize + padding);
            gc.setFill(i == player.getActiveHotbarSlot() ? Color.rgb(80, 80, 120) : Color.rgb(40, 40, 50));
            gc.fillRect(sx, startY, slotSize, slotSize);
            gc.setStroke(i == player.getActiveHotbarSlot() ? Color.GOLD : Color.WHITE);
            gc.setLineWidth(i == player.getActiveHotbarSlot() ? 3 : 1);
            gc.strokeRect(sx, startY, slotSize, slotSize);

            Item item = player.getInventory().getItemInHotbar(i);
            if (item != null)
                drawItemIcon(gc, sx, startY, slotSize, item, Color.SKYBLUE);

            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(new javafx.scene.text.Font("Arial", 12));
            gc.fillText(String.valueOf(i + 1), sx + 4, startY + 16);
        }
    }

    /**
     * Renders the full inventory and crafting overlay.
     * 
     * @param gc The GraphicsContext used for drawing.
     * @param w  Canvas width.
     * @param h  Canvas height.
     */
    private void drawInventory(GraphicsContext gc, double w, double h) {
        // Live UI: more transparent background
        gc.setFill(Color.color(0, 0, 0, 0.45));
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

    /**
     * Renders an individual item slot in the inventory.
     * 
     * @param gc          The GraphicsContext used for drawing.
     * @param x           Slot X.
     * @param y           Slot Y.
     * @param size        Slot size.
     * @param item        Item in the slot (can be null).
     * @param borderColor Color of the slot border.
     */
    private void drawSlot(GraphicsContext gc, double x, double y, double size, Item item, Color borderColor) {
        gc.setFill(Color.rgb(40, 40, 45));
        gc.fillRect(x, y, size, size);
        gc.setStroke(borderColor);
        gc.setLineWidth(borderColor == Color.GOLD || borderColor.equals(Color.web("#2ecc71")) ? 3 : 1);
        gc.strokeRect(x, y, size, size);
        if (item != null)
            drawItemIcon(gc, x, y, size, item, Color.ORANGE);
    }

    /**
     * Helper to draw a simplified icon/box representing an item.
     * 
     * @param gc    The GraphicsContext used for drawing.
     * @param x     Icon X.
     * @param y     Icon Y.
     * @param size  Icon size.
     * @param item  Item to represent.
     * @param color Base color for the icon box.
     */
    private void drawItemIcon(GraphicsContext gc, double x, double y, double size, Item item, Color color) {
        gc.setFill(color);
        gc.fillRect(x + 5, y + 5, size - 10, size - 10);
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial Bold", 12));
        String name = item.getName();
        if (name.length() > 8)
            name = name.substring(0, 8);
        gc.fillText(name, x + 5, y + size - 12);
    }

    /**
     * Manages logic for inventory interactions (clicking, 시작 dragging).
     */
    /**
     * Handles logic for inventory interactions, including item dragging and
     * dropping.
     * Manages click detection for all UI elements (Grid, Hotbar, Crafting).
     */
    private void handleInventoryInteraction() {
        double mx = Input.getMouseX(), my = Input.getMouseY(), w = 1024, h = 768; // Hardcoded for logic consistency
        boolean lmbPressed = Input.isLmbPressed();

        double panelW = 800, panelH = 550, panelX = (w - panelW) / 2, panelY = (h - panelH) / 2;
        double slotSize = 70, padding = 12, startX = panelX + 40, startY = panelY + 80;

        if (lmbPressed && !lmbWasPressed && draggedItem == null) {
            // Main slots
            for (int i = 0; i < 25; i++) {
                double sx = startX + (i % 5) * (slotSize + padding), sy = startY + (i / 5) * (slotSize + padding);
                if (isInside(mx, my, sx, sy, slotSize)) {
                    draggedItem = player.getInventory().getMainInventory()[i];
                    if (draggedItem != null) {
                        sourceArr = player.getInventory().getMainInventory();
                        sourceIdx = i;
                        sourceArr[i] = null;
                    }
                    break;
                }
            }
            // Crafting
            if (draggedItem == null) {
                double cX = panelX + 30 + 500, cYs[] = { panelY + 120, panelY + 320 }; // Adjusted
                cX = panelX + 530;
                for (int i = 0; i < 2; i++) {
                    if (isInside(mx, my, cX, cYs[i], slotSize)) {
                        draggedItem = player.getInventory().getCraftingInputs()[i];
                        if (draggedItem != null) {
                            sourceArr = player.getInventory().getCraftingInputs();
                            sourceIdx = i;
                            sourceArr[i] = null;
                        }
                        break;
                    }
                }
            }
            // Result
            if (draggedItem == null) {
                if (isInside(mx, my, panelX + 680, panelY + 220, slotSize + 15)) {
                    draggedItem = player.getInventory().getCraftingResult();
                    if (draggedItem != null) {
                        player.getInventory().consumeCraftingInputs();
                        sourceArr = null;
                        sourceIdx = -1;
                    }
                }
            }
            // Hotbar (HUD only)
            if (draggedItem == null) {
                double hudS = 60, hudP = 10, hX = (w - (5 * hudS + 4 * hudP)) / 2, hY = h - 85;
                for (int i = 0; i < 5; i++) {
                    if (isInside(mx, my, hX + i * (hudS + hudP), hY, hudS)) {
                        draggedItem = player.getInventory().getHotbar()[i];
                        if (draggedItem != null) {
                            sourceArr = player.getInventory().getHotbar();
                            sourceIdx = i;
                            sourceArr[i] = null;
                        }
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

    /**
     * Manages logic for dropping a dragged item into a slot.
     * Performs boundary checks for all clickable UI elements.
     */
    private void handleDrop(double mx, double my, double w, double h, double px, double py, double pw, double ph,
            double ss, double pd, double sx, double sy) {
        boolean dropped = false;
        // Main
        for (int i = 0; i < 25; i++) {
            if (isInside(mx, my, sx + (i % 5) * (ss + pd), sy + (i / 5) * (ss + pd), ss)) {
                player.getInventory().swapSlots(new Item[] { draggedItem }, 0, player.getInventory().getMainInventory(),
                        i);
                dropped = true;
                break;
            }
        }
        // Crafting
        if (!dropped) {
            double cX = px + 530, cYs[] = { py + 120, py + 320 };
            for (int i = 0; i < 2; i++) {
                if (isInside(mx, my, cX, cYs[i], ss)) {
                    player.getInventory().swapSlots(new Item[] { draggedItem }, 0,
                            player.getInventory().getCraftingInputs(), i);
                    dropped = true;
                    break;
                }
            }
        }
        // HUD Hotbar
        if (!dropped) {
            double hudS = 60, hudP = 10, hX = (w - (5 * hudS + 4 * hudP)) / 2, hY = h - 85;
            for (int i = 0; i < 5; i++) {
                if (isInside(mx, my, hX + i * (hudS + hudP), hY, hudS)) {
                    player.getInventory().swapSlots(new Item[] { draggedItem }, 0, player.getInventory().getHotbar(),
                            i);
                    dropped = true;
                    break;
                }
            }
        }

        if (!dropped && sourceArr != null)
            sourceArr[sourceIdx] = draggedItem;
        else if (!dropped && sourceArr == null)
            player.getInventory().addItem(draggedItem);
        draggedItem = null;
        sourceArr = null;
        sourceIdx = -1;
    }

    /**
     * Utility to check if a point is within a square area.
     * 
     * @param mx Point X.
     * @param my Point Y.
     * @param x  Area X.
     * @param y  Area Y.
     * @param s  Area size.
     * @return true if (mx, my) is inside the area.
     */
    private boolean isInside(double mx, double my, double x, double y, double s) {
        return mx >= x && mx <= x + s && my >= y && my <= y + s;
    }

    /**
     * Renders the item box following the mouse cursor during a drag operation.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
    private void drawDraggedItem(GraphicsContext gc) {
        double mx = Input.getMouseX(), my = Input.getMouseY(), s = 60;
        gc.setGlobalAlpha(0.8);
        gc.setFill(Color.ORANGE);
        gc.fillRect(mx - s / 2, my - s / 2, s, s);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(mx - s / 2, my - s / 2, s, s);
        gc.setGlobalAlpha(1.0);
    }

    /**
     * Renders the dialogue box at the bottom of the screen.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
    private void renderDialogue(GraphicsContext gc) {
        double width = 800;
        double height = 150;
        double x = (screenWidth - width) / 2.0;
        double y = screenHeight - height - 50;

        // Adaptive Alpha for dialogue
        double drawAlpha = 0.85;
        double screenY = (player.getY() + 6) - cameraY;
        if (screenY > y - 20) drawAlpha = 0.4;

        // Box
        gc.setGlobalAlpha(drawAlpha);
        gc.setFill(Color.BLACK);
        gc.fillRoundRect(x, y, width, height, 15, 15);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 15, 15);
        gc.setGlobalAlpha(1.0);

        // Name
        gc.setFill(Color.AQUAMARINE);
        gc.setFont(new Font("Inter", 18));
        gc.fillText(activeDialogue.getName(), x + 20, y + 35);

        // Text
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Inter", 16));
        gc.fillText(activeDialogue.getDialogueLines().get(dialogueIndex), x + 20, y + 70);

        // Prompt
        gc.setFill(Color.GRAY);
        gc.setFont(new Font("Inter", 12));
        gc.fillText("[E] Continue", x + width - 100, y + height - 20);
    }
}