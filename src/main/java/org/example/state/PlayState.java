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
import org.example.level.LevelConfig;
import org.example.level.LevelLoader;
import org.example.level.MapGenerator;
import org.example.level.Biome;
import org.example.item.Item;
import org.example.item.WorldItem;
import org.example.item.WeaponRegistry;
import org.example.item.WeaponConfig;
import org.example.item.ItemRegistry;
import org.example.entity.EnemyRegistry;
import org.example.entity.InteractableEntity;
import org.example.AssetRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.example.SaveData;
import org.example.SaveManager;

/**
 * Game state where the actual gameplay takes place.
 * Handles level rendering, logic, and UI (HUD/Inventory/Crafting).
 * Optimized for 1024x768 resolution.
 */
public class PlayState implements GameState {

    /** The current level data being played. */
    private Level currentLevel;
    /** Current level configuration (Spawn rates, dimensions). */
    private LevelConfig currentLevelConfig;
    /** Spatial manager for the current level. */
    private GameMap gameMap;

    /** Current state of the game loop. */
    private enum PlayMode {
        PLAYING, VICTORY, GAMEOVER
    }

    private PlayMode currentMode = PlayMode.PLAYING;

    /** Current state of the Pause Menu. */
    private enum PauseMenuState {
        MAIN, SAVE_SELECT, LOAD_SELECT
    }

    private PauseMenuState currentPauseState = PauseMenuState.MAIN;

    /** The player entity. */
    private Player player;
    /** List of all active enemies in the level. */
    private List<Enemy> enemies;

    /** Maximum time before a Tribulation starts (in seconds). */
    private double maxTime = 60.0;
    /** Remaining time before the next Tribulation phase. */
    private double currentTime = maxTime;
    /** The random seed used to generate the current level. */
    private long currentMapSeed;
    /** Timer for map animations (e.g., water). */
    private double mapAnimationTimer = 0;
    /** Flag indicating if a Tribulation event is currently active. */
    private boolean inTribulation = false;
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
    /**
     * Whether the ultimate goal of the current level (e.g. survive Tribulation) was
     * met.
     */
    private boolean levelVictoryAchieved = false;
    /** Tracks the path of the last loaded level configuration. */
    private String lastConfigPath;

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
    /** Tracks active fire bursts that spawn projectiles over time. */
    private List<BurstTracker> pendingBursts = new ArrayList<>();

    public PlayState() {
        // Load configurations from JSON
        WeaponRegistry.loadWeapons("/weapons/weapon_configs.json");
        ItemRegistry.loadData("/items/items.json", "/items/recipes.json");
        EnemyRegistry.loadConfigs("/enemies/enemy_configs.json");
        AssetRegistry.loadAssets("/assets.json");

        // Load configuration from JSON
        lastConfigPath = "/levels/level_gen.json";
        currentLevelConfig = LevelLoader.loadConfig(lastConfigPath);
        currentLevel = MapGenerator.generate(currentLevelConfig);
        this.currentMapSeed = currentLevel.seed;

        gameMap = new GameMap(currentLevel);

        // Player starting position (middle of the map)
        player = new Player(currentLevelConfig.width * currentLevelConfig.tileSize / 2.0,
                currentLevelConfig.height * currentLevelConfig.tileSize / 2.0);

        maxTime = currentLevelConfig.tribulationTime;
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
        for (int i = 0; i < currentLevelConfig.initialEnemyCount; i++) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(12, player.getX(), player.getY(), 200);
            if (pos != null) {
                // Pick a random enemy from the pool
                String enemyId = currentLevelConfig.enemyPool
                        .get(new Random().nextInt(currentLevelConfig.enemyPool.size()));
                enemies.add(EnemyRegistry.createEnemy(enemyId, pos[0], pos[1], false, 1.0));
            }
        }
    }

    /**
     * Adds initial starting items to the player's inventory for testing.
     */
    private void addTestItems() {
        if (player != null && player.getInventory() != null) {
            // Set up specific crafting test items
            player.getInventory().addItem(ItemRegistry.createItem("sword_01"));
            player.getInventory().addItem(ItemRegistry.createItem("fire_essence_01"));
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
        if (currentMode == PlayMode.VICTORY || currentMode == PlayMode.GAMEOVER) {
            handleVictoryInputs();
            return;
        }

        handleToggles();

        if (isPaused) {
            handlePauseMenuInteraction();
            return;
        }

        if (inventoryOpen) {
            handleInventoryInteraction();
        }

        // Even when Inventory or Map is open, the world continues to run!
        handleHotbarSelection();
        handleWorldInteraction();
        handleGameplayLogic(deltaTime);
        updateCamera();

        mapAnimationTimer += deltaTime;
        if (mapAnimationTimer > 10.0)
            mapAnimationTimer -= 10.0;

        // --- Centralized Input State Update ---
        // Ensuring all handlers see correctly transitioned states for click detection.
        lmbWasPressed = Input.isLmbPressed();
        rmbWasPressed = Input.isRmbPressed();
    }

    /**
     * Handles keyboard shortcuts while in a victory or game-over state.
     */
    private void handleVictoryInputs() {
        if (Input.isKeyPressed(KeyCode.SPACE)) {
            nextLevel();
        }
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
                }
                eWasPressed = ePressed;
                return;
            }

            WorldItem bestItem = null;
            double minDist = 150.0; // Reach limit

            for (WorldItem wi : itemsOnGround) {
                double dist = Math
                        .sqrt(Math.pow(wi.getX() - player.getX(), 2) + Math.pow(wi.getY() - player.getY(), 2));
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
                    System.out.println("Entering the Gate of Realms...");
                    currentMode = PlayMode.VICTORY;
                }
            }
        }
        eWasPressed = ePressed;
    }

    private void handleToggles() {
        boolean escIsPressed = Input.isKeyPressed(KeyCode.ESCAPE);
        if (escIsPressed && !escWasPressed) {
            isPaused = !isPaused;
            if (isPaused) {
                currentPauseState = PauseMenuState.MAIN;
            }
        }
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

    /**
     * Processes mouse interactions with the Pause Menu buttons and slots.
     * This method is the primary input handler when the world is frozen via the
     * Pause (ESC) state.
     * It manages slot selection for Saving and Loading operations.
     */
    private void handlePauseMenuInteraction() {
        double mx = Input.getMouseX(), my = Input.getMouseY();
        boolean click = Input.isLmbPressed() && !lmbWasPressed;

        if (!click)
            return;

        double centerX = screenWidth / 2.0;

        if (currentPauseState == PauseMenuState.MAIN) {
            if (isInside(mx, my, centerX - 150, 280, 300, 60))
                isPaused = false;
            if (isInside(mx, my, centerX - 150, 360, 300, 60))
                currentPauseState = PauseMenuState.SAVE_SELECT;
            if (isInside(mx, my, centerX - 150, 440, 300, 60))
                currentPauseState = PauseMenuState.LOAD_SELECT;
        } else {
            // Back button
            if (isInside(mx, my, centerX - 75, 630, 150, 50))
                currentPauseState = PauseMenuState.MAIN;

            // Slot selection
            for (int i = 1; i <= 5; i++) {
                double sy = 210 + (i - 1) * 80;
                if (isInside(mx, my, centerX - 250, sy, 500, 70)) {
                    if (currentPauseState == PauseMenuState.SAVE_SELECT) {
                        performSave(i);
                        isPaused = false;
                    } else if (currentPauseState == PauseMenuState.LOAD_SELECT) {
                        if (SaveManager.exists(i)) {
                            performLoad(i);
                            isPaused = false;
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper to check if mouse is within a rectangle.
     */
    private boolean isInside(double mx, double my, double x, double y, double w, double h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private boolean isInside(double mx, double my, double x, double y, double s) {
        return isInside(mx, my, x, y, s, s);
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
     * Now supports spread and burst firing patterns.
     */
    private void handleCombatInput() {
        if (inventoryOpen || showFullMap || isPaused || activeDialogue != null)
            return;

        if (Input.isLmbPressed() && player.canAttack()) {
            Item activeItem = player.getInventory().getItemInHotbar(player.getActiveHotbarSlot());
            if (activeItem != null && activeItem.getType() == Item.Type.WEAPON) {
                WeaponConfig wConfig = activeItem.getWeaponConfig();
                if (wConfig != null) {
                    // Check Qi cost (Full burst cost upfront for simplicity)
                    if (player.spendQi(wConfig.qiCost)) {
                        // Calculate base angle to mouse
                        double mx = Input.getMouseX() + cameraX;
                        double my = Input.getMouseY() + cameraY;
                        double baseAngle = Math.atan2(my - (player.getY() + 6), mx - (player.getX() + 6));

                        if (wConfig.burstCount > 1) {
                            // Start a burst sequence
                            pendingBursts.add(new BurstTracker(wConfig, baseAngle));
                        } else {
                            // Handle single/spread shot
                            fireShot(wConfig, baseAngle);
                        }
                        player.setAttackCooldown(wConfig.cooldown);
                    }
                }
            }
        }
    }

    /**
     * Creates and adds projectiles to the world based on weapon configuration.
     * Handles spread-shot patterns.
     */
    private void fireShot(WeaponConfig config, double baseAngle) {
        if (config.projectileCount <= 1) {
            projectiles.add(new Projectile(player.getX() + 6, player.getY() + 6, baseAngle, config, player));
        } else {
            // Spread shot logic
            double startAngle = baseAngle - Math.toRadians(config.spreadAngle / 2.0);
            double angleStep = (config.projectileCount > 1)
                    ? Math.toRadians(config.spreadAngle) / (config.projectileCount - 1)
                    : 0;

            for (int i = 0; i < config.projectileCount; i++) {
                double currentAngle = startAngle + (angleStep * i);
                projectiles.add(new Projectile(player.getX() + 6, player.getY() + 6, currentAngle, config, player));
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
        currentMode = PlayMode.PLAYING;

        activeStrikes.clear();
        enemies.clear();
        projectiles.clear();
        itemsOnGround.clear();

        // Load configuration and regenerate - Alternate or randomize for variety
        String levelFile = Math.random() > 0.5 ? "/levels/level_gen.json" : "/levels/level_small.json";
        lastConfigPath = levelFile;
        currentLevelConfig = LevelLoader.loadConfig(levelFile);
        currentLevel = MapGenerator.generate(currentLevelConfig);
        gameMap = new GameMap(currentLevel);

        // Sync Timers
        maxTime = currentLevelConfig.tribulationTime;
        currentTime = maxTime;

        // Reposition player
        player.setX(currentLevel.width * currentLevel.tileSize / 2.0);
        player.setY(currentLevel.height * currentLevel.tileSize / 2.0);

        generateMapCache();
        spawnInitialEnemies();
        spawnInitialItems();
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

        // Update active bursts
        for (int i = pendingBursts.size() - 1; i >= 0; i--) {
            BurstTracker burst = pendingBursts.get(i);
            burst.timer -= deltaTime;
            if (burst.timer <= 0) {
                fireShot(burst.config, burst.angle);
                burst.shotsFired++;
                burst.timer = burst.config.burstDelay;
                if (burst.shotsFired >= burst.config.burstCount) {
                    pendingBursts.remove(i);
                }
            }
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
                    double finalDamage = p.getDamage();
                    // Scale continuous damage types by deltaTime for frame-rate independence
                    if (p.getType() == WeaponConfig.ProjectileType.BEAM ||
                            p.getType() == WeaponConfig.ProjectileType.AOE_ZONE) {
                        finalDamage *= deltaTime;
                    }

                    enemy.takeDamage(finalDamage);

                    if (p.getType() != WeaponConfig.ProjectileType.BEAM &&
                            p.getType() != WeaponConfig.ProjectileType.AOE_ZONE) {
                        p.deactivate();
                        break;
                    }
                }
            }

            if (!p.isActive()) {
                projectiles.remove(i);
            }
        }

        // Clean up dead enemies and track if any died this frame
        boolean enemyDied = enemies.removeIf(Enemy::isDead);

        // --- Event-Driven Victory Check (Survival) ---
        // Victory triggers if an enemy died, we are in Tribulation, and no Tribulation
        // enemies remain.
        if (enemyDied && !levelVictoryAchieved && inTribulation) {
            if (countLivingTribulationEnemies() == 0) {
                System.out.println("[EVENT] Final Tribulation enemy defeated! Victory Achieved.");
                levelVictoryAchieved = true;
                currentMode = PlayMode.VICTORY;
            }
        }

        if (!inTribulation) {
            currentTime -= deltaTime;
            if (currentTime <= 0)
                triggerTribulation();
        } else {
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
                double distP = Math
                        .sqrt(Math.pow(strike.getX() - player.getX(), 2) + Math.pow(strike.getY() - player.getY(), 2));
                if (distP < strike.getRadius()) {
                    player.takeDamage(20);
                }
                // Damage enemies
                for (Enemy e : enemies) {
                    double distE = Math
                            .sqrt(Math.pow(strike.getX() - e.getX(), 2) + Math.pow(strike.getY() - e.getY(), 2));
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
     * Activates the Tribulation phase, spawning the entire wave of enemies at once.
     */
    private void triggerTribulation() {
        inTribulation = true;

        // Spawn the entire Tribulation wave immediately
        for (int i = 0; i < currentLevelConfig.tribulationTotalEnemies; i++) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(24, player.getX(), player.getY(), 250);
            if (pos != null) {
                String enemyId = currentLevelConfig.enemyPool
                        .get(new Random().nextInt(currentLevelConfig.enemyPool.size()));
                enemies.add(EnemyRegistry.createEnemy(enemyId, pos[0], pos[1], true,
                        currentLevelConfig.tribulationScalingFactor));
            }
        }
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
        lastConfigPath = (lastConfigPath != null) ? lastConfigPath : "/levels/level_small.json";
        currentLevelConfig = LevelLoader.loadConfig(lastConfigPath);
        currentLevel = MapGenerator.generate(currentLevelConfig);
        gameMap = new GameMap(currentLevel);
        player = new Player(currentLevelConfig.width * currentLevelConfig.tileSize / 2.0,
                currentLevelConfig.height * currentLevelConfig.tileSize / 2.0);

        maxTime = currentLevelConfig.tribulationTime;
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
     * Counts how many "Tribulation" enemies are currently active in the world.
     * Used for the victory condition check and the HUD counter.
     * 
     * @return Number of living enemies with isTribulation == true.
     */
    private int countLivingTribulationEnemies() {
        int count = 0;
        for (Enemy e : enemies) {
            if (e.isTribulation())
                count++;
        }
        return count;
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
            renderPauseMenu(gc);
        }

        // Victory Overlay
        if (currentMode == PlayMode.VICTORY) {
            renderVictoryScreen(gc);
        }
    }

    /**
     * Renders a full-screen victory overlay.
     * Displays completion text and instructions for level transition.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
    private void renderVictoryScreen(GraphicsContext gc) {
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();

        // Dim background
        gc.setFill(new Color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, w, h);

        gc.setFill(Color.GOLD);
        gc.setFont(new Font("System", 60));
        gc.fillText("LEVEL COMPLETE!", w / 2.0 - 220, h / 2.0 - 50);

        gc.setFill(Color.WHITE);
        gc.setFont(new Font("System", 24));
        gc.fillText("You have survived the Heavenly Punishment", w / 2.0 - 210, h / 2.0 + 20);
        gc.fillText("Press SPACE to transcend to the next realm.", w / 2.0 - 215, h / 2.0 + 60);
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
                String spriteId = currentLevel.biome.getSpriteId(tileType);
                int frameIndex = 0;

                if (tileType == 2) {
                    frameIndex = (int) (mapAnimationTimer / 0.5) % 2;
                } else if (tileType == 3) {
                    // Pulsing animation for spirit veins
                    frameIndex = (int) (mapAnimationTimer / 0.2) % 4;
                }

                // Draw base grass first for decorative tiles (Variety) to avoid black
                // backgrounds
                if (tileType == 4 || tileType == 5) {
                    int baseType = 0; // Default to grass

                    if (tileType == 5) {
                        // For bridges, check neighbors (and further) to see if we are over water
                        // We check horizontally and vertically for the first non-bridge tile
                        int bx = x, by = y;
                        // Search left
                        while (bx > 0 && currentLevel.data.get(y).get(bx) == 5)
                            bx--;
                        if (currentLevel.data.get(y).get(bx) == 2)
                            baseType = 2;
                        if (baseType == 0) { // Search right
                            bx = x;
                            while (bx < currentLevel.width - 1 && currentLevel.data.get(y).get(bx) == 5)
                                bx++;
                            if (currentLevel.data.get(y).get(bx) == 2)
                                baseType = 2;
                        }
                        if (baseType == 0) { // Search up
                            bx = x;
                            by = y;
                            while (by > 0 && currentLevel.data.get(by).get(x) == 5)
                                by--;
                            if (currentLevel.data.get(by).get(x) == 2)
                                baseType = 2;
                        }
                        if (baseType == 0) { // Search down
                            by = y;
                            while (by < currentLevel.height - 1 && currentLevel.data.get(by).get(x) == 5)
                                by++;
                            if (currentLevel.data.get(by).get(x) == 2)
                                baseType = 2;
                        }
                    }

                    String baseId = currentLevel.biome.getSpriteId(baseType);
                    int bFrame = (baseType == 2) ? (int) (mapAnimationTimer / 0.5) % 2 : 0;
                    javafx.scene.image.Image base = AssetRegistry.getSprite(baseId, bFrame);
                    if (base != null) {
                        gc.drawImage(base, x * tileSize - cameraX, y * tileSize - cameraY, tileSize, tileSize);
                    }
                }

                javafx.scene.image.Image sprite = AssetRegistry.getSprite(spriteId, frameIndex);
                if (sprite != null) {
                    gc.drawImage(sprite, x * tileSize - cameraX, y * tileSize - cameraY, tileSize, tileSize);
                } else {
                    // Fallback to colors
                    if (tileType == 1)
                        gc.setFill(Color.DARKGRAY);
                    else if (tileType == 2)
                        gc.setFill(Color.BLUE);
                    else if (tileType == 3)
                        gc.setFill(Color.MEDIUMPURPLE);
                    else if (tileType == 5)
                        gc.setFill(Color.SADDLEBROWN);
                    else
                        gc.setFill(Color.DARKGREEN);
                    gc.fillRect(x * tileSize - cameraX, y * tileSize - cameraY, tileSize, tileSize);
                }
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

        // HP Counter
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial Bold", 12));
        gc.fillText((int) player.getHp() + " / " + (int) player.getMaxHp(), 330, 35);

        // Qi
        gc.setFill(Color.rgb(50, 50, 50, 0.7));
        gc.fillRect(20, 45, 150, 15);
        gc.setFill(Color.CYAN);
        gc.fillRect(20, 45, (player.getQi() / player.getMaxQi()) * 150, 15);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(20, 45, 150, 15);

        // Qi Counter
        gc.setFill(Color.CYAN);
        gc.fillText((int) player.getQi() + " / " + (int) player.getMaxQi(), 180, 57);

        // Enemies Left (Survival Path)
        if (inTribulation) {
            int remaining = countLivingTribulationEnemies();
            gc.setFill(remaining == 0 ? Color.GOLD : Color.ORANGE);
            gc.fillText("Enemies Left: " + remaining, w - 150, 35);
        }

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
        if (screenY > h - 160)
            drawAlpha = 0.35;

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

        // Usage Hint
        gc.setFill(Color.GRAY);
        gc.setFont(new javafx.scene.text.Font("Arial Italic", 14));
        gc.fillText("Right-click consumables to use immediately.", panelX + 40, panelY + panelH - 20);

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
        double mx = Input.getMouseX(), my = Input.getMouseY(), w = screenWidth;
        boolean lmbPressed = Input.isLmbPressed();
        boolean rmbPressed = Input.isRmbPressed();

        double panelW = 800, panelH = 550, panelX = (w - panelW) / 2, panelY = (screenHeight - panelH) / 2;
        double slotSize = 70, padding = 12, startX = panelX + 40, startY = panelY + 80;

        // -- Right Click to Use --
        if (rmbPressed && !rmbWasPressed && draggedItem == null) {
            // Main slots
            for (int i = 0; i < 25; i++) {
                double sx = startX + (i % 5) * (slotSize + padding), sy = startY + (i / 5) * (slotSize + padding);
                if (isInside(mx, my, sx, sy, slotSize)) {
                    Item item = player.getInventory().getMainInventory()[i];
                    if (item != null) {
                        item.use(player);
                        if (item.getType() == Item.Type.CONSUMABLE)
                            player.getInventory().getMainInventory()[i] = null;
                    }
                }
            }
            // Hotbar Slots
            double hudS = 60, hudP = 10, hX = (w - (5 * hudS + 4 * hudP)) / 2, hY = screenHeight - 85;
            for (int i = 0; i < 5; i++) {
                if (isInside(mx, my, hX + i * (hudS + hudP), hY, hudS)) {
                    Item item = player.getInventory().getHotbar()[i];
                    if (item != null) {
                        item.use(player);
                        if (item.getType() == Item.Type.CONSUMABLE)
                            player.getInventory().getHotbar()[i] = null;
                    }
                }
            }
        }

        // -- Left Click to Drag --
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
                double cX = panelX + 530;
                double[] cYs = { panelY + 120, panelY + 320 };
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
            // Hotbar
            if (draggedItem == null) {
                double hudS = 60, hudP = 10, hX = (w - (5 * hudS + 4 * hudP)) / 2, hY = screenHeight - 85;
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
            handleDrop(mx, my, w, screenHeight, panelX, panelY, panelW, panelH, slotSize, padding, startX, startY);
        }
    }

    /**
     * Manages logic for dropping a dragged item into a slot.
     * Performs boundary checks for all clickable UI elements.
     */
    private void handleDrop(double mx, double my, double w, double h, double px, double py, double pw, double ph,
            double ss, double pd, double sx, double sy) {
        boolean dropped = false;
        Item[] wrapper = new Item[] { draggedItem };

        // Main Inventory Slots
        for (int i = 0; i < 25; i++) {
            if (isInside(mx, my, sx + (i % 5) * (ss + pd), sy + (i / 5) * (ss + pd), ss)) {
                player.getInventory().swapSlots(wrapper, 0, player.getInventory().getMainInventory(), i);
                dropped = true;
                break;
            }
        }
        // Crafting Input Slots
        if (!dropped) {
            double cX = px + 530, cYs[] = { py + 120, py + 320 };
            for (int i = 0; i < 2; i++) {
                if (isInside(mx, my, cX, cYs[i], ss)) {
                    player.getInventory().swapSlots(wrapper, 0, player.getInventory().getCraftingInputs(), i);
                    dropped = true;
                    break;
                }
            }
        }
        // HUD Hotbar Slots
        if (!dropped) {
            double hudS = 60, hudP = 10, hX = (w - (5 * hudS + 4 * hudP)) / 2, hY = screenHeight - 85;
            for (int i = 0; i < 5; i++) {
                if (isInside(mx, my, hX + i * (hudS + hudP), hY, hudS)) {
                    player.getInventory().swapSlots(wrapper, 0, player.getInventory().getHotbar(), i);
                    dropped = true;
                    break;
                }
            }
        }

        if (dropped) {
            Item swappedOut = wrapper[0];
            if (swappedOut != null) {
                // If there was an item in the target slot, move it back to the source or
                // backpack
                if (sourceArr != null) {
                    sourceArr[sourceIdx] = swappedOut;
                } else {
                    player.getInventory().addItem(swappedOut); // From crafting result back to inventory
                }
            }
        } else {
            // Not dropped into any slot, return to original position
            if (sourceArr != null) {
                sourceArr[sourceIdx] = draggedItem;
            } else {
                player.getInventory().addItem(draggedItem);
            }
        }

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
        if (screenY > y - 20)
            drawAlpha = 0.4;

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
    }

    /**
     * Helper class to track successive shots in a rapid-fire burst.
     */
    private static class BurstTracker {
        final WeaponConfig config;
        final double angle;
        int shotsFired = 0;
        double timer = 0;

        BurstTracker(WeaponConfig config, double angle) {
            this.config = config;
            this.angle = angle;
        }
    }

    /**
     * Gathers all relevant game state and serializes it to a persistent JSON file.
     * Captures player stats, full inventory, world timer, and a detailed list of
     * all active entities (enemies and items on ground) to ensure 100% restoration.
     * 
     * @param slot The save slot to use (1-5).
     */
    private void performSave(int slot) {
        try {
            SaveData data = new SaveData();
            data.mapSeed = this.currentMapSeed;
            data.biome = currentLevel.biome.name();
            data.playerX = player.getX();
            data.playerY = player.getY();
            data.hp = player.getHp();
            data.maxHp = player.getMaxHp();
            data.qi = player.getQi();
            data.maxQi = player.getMaxQi();
            data.activeHotbarSlot = player.getActiveHotbarSlot();
            data.levelConfigPath = this.lastConfigPath;
            data.currentTime = this.currentTime;
            data.inTribulationFlag = this.inTribulation ? 1 : 0;
            data.victoryAchievedFlag = this.levelVictoryAchieved ? 1 : 0;
            data.tribulationSpawnLimit = this.currentLevelConfig.tribulationTotalEnemies;

            // Save Inventory
            java.util.List<String> invIds = new java.util.ArrayList<>();
            for (Item it : player.getInventory().getMainInventory()) {
                invIds.add(it != null ? it.getId() : null);
            }
            data.inventoryItemIds = invIds;

            java.util.List<String> hotIds = new java.util.ArrayList<>();
            for (Item it : player.getInventory().getHotbar()) {
                hotIds.add(it != null ? it.getId() : null);
            }
            data.hotbarItemIds = hotIds;

            System.out.println("[SAVE] Serializing " + (enemies != null ? enemies.size() : 0) + " enemies...");

            // Save Enemies
            if (enemies != null) {
                System.out.println("[SAVE] Found " + enemies.size() + " active enemies.");
                for (Enemy e : enemies) {
                    SaveData.EnemySaveData ed = new SaveData.EnemySaveData();
                    ed.id = e.getId();
                    ed.x = e.getX();
                    ed.y = e.getY();
                    ed.hp = e.getHP();
                    ed.tribulationFlag = e.isTribulation() ? 1 : 0;
                    ed.scaling = e.getScaling();
                    data.activeEnemies.add(ed);
                    System.out.println("  -> Saved: " + ed.id + " at (" + (int) ed.x + "," + (int) ed.y + ")");
                }
            }

            // Save Items on ground
            if (itemsOnGround != null) {
                for (WorldItem wi : itemsOnGround) {
                    SaveData.ItemSaveData isd = new SaveData.ItemSaveData();
                    isd.id = wi.getItem().getId();
                    isd.x = wi.getX();
                    isd.y = wi.getY();
                    data.itemsOnGround.add(isd);
                }
            }

            org.example.SaveManager.save(data, slot);
        } catch (java.io.IOException e) {
            System.err.println("CRITICAL: Save failed for slot " + slot);
            e.printStackTrace();
        }
    }

    /**
     * Loads game state from a specific slot and reconstructs the entire world.
     * Uses the saved Seed to regenerate the map, and then restores player stats,
     * inventory items, world progress timers, and actively reconstructs all
     * enemies and dropped items from their stored DTO snapshots.
     * 
     * @param slot The save slot to load (1-5).
     */
    private void performLoad(int slot) {
        SaveData data = org.example.SaveManager.load(slot);
        if (data == null)
            return;

        // Re-generate the EXACT SAME level using the seed
        if (data != null) {
            this.currentMapSeed = data.mapSeed;
            LevelConfig config = LevelLoader.loadConfig(data.levelConfigPath);
            if (config == null)
                return;
            this.currentLevelConfig = config;
            if (data.biome != null) {
                this.currentLevelConfig.biome = Biome.valueOf(data.biome);
            }
            this.currentLevel = MapGenerator.generate(this.currentLevelConfig, this.currentMapSeed);
        }
        this.gameMap = new GameMap(currentLevel);

        // Restore Player
        this.player = new Player(data.playerX, data.playerY);
        player.setStats(data.hp, data.maxHp, data.qi, data.maxQi);
        player.setActiveHotbarSlot(data.activeHotbarSlot);

        // Restore Inventory
        for (int i = 0; i < 25; i++) {
            String itemId = data.inventoryItemIds.get(i);
            player.getInventory().getMainInventory()[i] = (itemId != null) ? ItemRegistry.createItem(itemId) : null;
        }
        for (int i = 0; i < 5; i++) {
            String itemId = data.hotbarItemIds.get(i);
            player.getInventory().getHotbar()[i] = (itemId != null) ? ItemRegistry.createItem(itemId) : null;
        }

        // Restore World Progress
        this.currentTime = data.currentTime;
        this.inTribulation = (data.inTribulationFlag == 1);
        this.levelVictoryAchieved = (data.victoryAchievedFlag == 1);
        if (data.tribulationSpawnLimit > 0) {
            this.currentLevelConfig.tribulationTotalEnemies = data.tribulationSpawnLimit;
        }

        // Restore Enemies
        int restoredCount = 0;
        this.enemies.clear();
        if (data.activeEnemies != null) {
            System.out.println("[LOAD] Attempting to restore " + data.activeEnemies.size() + " enemies...");
            for (SaveData.EnemySaveData ed : data.activeEnemies) {
                boolean isT = (ed.tribulationFlag == 1);
                Enemy e = EnemyRegistry.createEnemy(ed.id, ed.x, ed.y, isT, ed.scaling);
                if (e != null) {
                    e.setHP(ed.hp);
                    this.enemies.add(e);
                    restoredCount++;
                    System.out.println("  -> Restored: " + e.getId() + " HP:" + (int) e.getHP());
                } else {
                    System.err.println("  !! Failed to restore enemy ID: " + ed.id);
                }
            }
        }
        System.out.println("[LOAD] Successfully restored " + restoredCount + " enemies to the world.");

        // Restore World Items
        this.itemsOnGround.clear();
        if (data.itemsOnGround != null) {
            for (SaveData.ItemSaveData id : data.itemsOnGround) {
                Item item = ItemRegistry.createItem(id.id);
                if (item != null) {
                    this.itemsOnGround.add(new WorldItem(item, id.x, id.y));
                }
            }
        }

        this.currentMode = PlayMode.PLAYING;
        this.isPaused = false;
        generateMapCache();
        System.out.println("Game loaded successfully from slot " + slot);
    }

    /**
     * Renders the interactive Pause Menu with main and sub-menu (slot selection)
     * states.
     */
    private void renderPauseMenu(GraphicsContext gc) {
        double w = screenWidth, h = screenHeight;
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, w, h);

        double centerX = w / 2.0;
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

        if (currentPauseState == PauseMenuState.MAIN) {
            gc.setFill(Color.WHITE);
            gc.setFont(new Font("Inter Bold", 48));
            gc.fillText("PAUSED", centerX, 180);

            drawMenuButton(gc, "RESUME", centerX - 150, 280, 300, 60, Color.DARKGREEN);
            drawMenuButton(gc, "SAVE GAME", centerX - 150, 360, 300, 60, Color.rgb(80, 80, 100));
            drawMenuButton(gc, "LOAD GAME", centerX - 150, 440, 300, 60, Color.rgb(80, 80, 100));
        } else {
            boolean isSave = (currentPauseState == PauseMenuState.SAVE_SELECT);
            gc.setFill(isSave ? Color.AQUAMARINE : Color.GOLD);
            gc.setFont(new Font("Inter Bold", 36));
            gc.fillText(isSave ? "SAVE SESSION" : "LOAD SESSION", centerX, 150);

            for (int i = 1; i <= 5; i++) {
                boolean exists = SaveManager.exists(i);
                double sy = 210 + (i - 1) * 80;
                Color baseColor = exists ? Color.rgb(60, 60, 80) : Color.rgb(40, 40, 40);
                String label = "SLOT " + i + (exists ? " (DATA FOUND)" : " (EMPTY)");
                drawMenuButton(gc, label, centerX - 250, sy, 500, 70, baseColor);
            }

            drawMenuButton(gc, "BACK", centerX - 75, 630, 150, 50, Color.rgb(150, 50, 50));
        }
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    /**
     * Visual utility to draw a button with hover state.
     */
    private void drawMenuButton(GraphicsContext gc, String text, double x, double y, double w, double h,
            Color baseColor) {
        double mx = Input.getMouseX(), my = Input.getMouseY();
        boolean hover = isInside(mx, my, x, y, w, h);

        gc.setFill(hover ? baseColor.brighter() : baseColor);
        gc.setStroke(hover ? Color.WHITE : Color.GRAY);
        gc.setLineWidth(hover ? 3 : 1);
        gc.fillRoundRect(x, y, w, h, 12, 12);
        gc.strokeRoundRect(x, y, w, h, 12, 12);

        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Inter Bold", 18));
        gc.fillText(text, x + w / 2.0, y + h / 2.0 + 7);
    }
}
