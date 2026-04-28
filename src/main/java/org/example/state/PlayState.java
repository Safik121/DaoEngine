package org.example.state;

import org.example.GameLogger;
import org.example.ui.PlayUIManager;
import org.example.ui.DialogManager;
import org.example.render.WorldRenderer;
import org.example.logic.CombatManager;
import org.example.logic.TribulationTimer;
import org.example.ConfigManager;
import org.example.Input;
import org.example.SaveData;
import org.example.entity.Enemy;
import org.example.entity.Player;
import org.example.entity.Projectile;
import org.example.entity.LightningStrike;
import org.example.entity.EnemyRegistry;
import org.example.logic.CultivationManager;
import org.example.logic.CultivationRank;
import org.example.logic.Skill;
import org.example.item.Item;
import org.example.item.WorldItem;
import org.example.item.ItemRegistry;
import org.example.level.GameMap;
import org.example.level.Level;
import org.example.level.LevelConfig;
import org.example.level.LevelLoader;
import org.example.level.MapGenerator;
import org.example.level.Biome;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    public enum PlayMode {
        PLAYING, VICTORY, GAMEOVER
    }

    private PlayMode currentMode = PlayMode.PLAYING;

    /** Current state of the Pause Menu. */
    public enum PauseMenuState {
        MAIN, SAVE_SELECT, LOAD_SELECT
    }

    private PauseMenuState currentPauseState = PauseMenuState.MAIN;

    /** The player entity. */
    private Player player;
    /** List of all active enemies in the level. */
    private List<Enemy> enemies;

    /** Maximum time before a Tribulation starts (in seconds). */
    private double maxTime = 60.0;
    /** Independent background timer for Tribulation. */
    private TribulationTimer tribulationTimer;
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
    /** Toggle for the quest log overlay. */
    private boolean questLogOpen = false;
    /** Input buffer to detect 'Q' key releases. */
    private boolean questLogWasPressed = false;
    /** Toggle for the full-screen world map overlay. */
    private boolean showFullMap = false;
    /** Input buffer to detect 'M' key releases. */
    private boolean mapWasPressed = false;
    /** Input buffer to detect 'E' key releases. */
    private boolean eWasPressed = false;
    /** Input buffer to detect 'B' key releases. */
    private boolean bWasPressed = false;
    /** Input buffer to detect 'C' key releases. */
    private boolean cWasPressed = false;

    /** The currently active NPC/Stele dialogue, if any. */
    private DialogManager dialogManager;
    /** Whether the ultimate goal of the current level was met. */
    private boolean levelVictoryAchieved = false;
    /** Whether the cultivation/meditation menu is currently open. */
    private boolean cultivationMenuOpen = false;

    /** Simple notification tracking. */
    public static class Notification {
        public String message;
        public double timer;

        public Notification(String m, double t) {
            this.message = m;
            this.timer = t;
        }
    }

    private List<Notification> notifications = new ArrayList<>();

    /** Dynamic list of levels loaded from world_manifest.json. */
    private java.util.List<String> worldManifest = new java.util.ArrayList<>();
    private int currentLevelIndex = 0;

    /** Path to the last used level configuration for resets/saves. */
    private String lastConfigPath = "/levels/world/map1.json";

    /** Current map level (1-20) for scaling difficulty and rewards. */
    private int mapLevel = 1;

    /** Cached image of the map background for performance. */
    private WritableImage mapCache;

    // --- CAMERA SYSTEM ---
    /** Current X offset of the camera (world space to screen space). */
    private double cameraX = 0;
    /** Current Y offset of the camera (world space to screen space). */
    private double cameraY = 0;
    /** Game window width in pixels. */
    private final int screenWidth = ConfigManager.getInstance().getConfig().engine.width;
    /** Game window height in pixels. */
    private final int screenHeight = ConfigManager.getInstance().getConfig().engine.height;

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

    /** @return true if the cultivation menu is active. */
    public boolean isCultivationMenuOpen() {
        return cultivationMenuOpen;
    }

    /** @return List of projectiles in the air. */
    private List<Projectile> projectiles;
    /** List of items dropped on the ground in the game world. */
    private List<WorldItem> itemsOnGround;
    /** Tracks active fire bursts. */
    private List<CombatManager.BurstTracker> pendingBursts = new ArrayList<>();

    private org.example.logic.ParticleManager particleManager;
    private org.example.logic.SoundManager soundManager;
    private org.example.logic.QuestManager questManager;
    private org.example.logic.event.EventManager eventManager;

    /** Manager for all UI overlays (HUD, Inventory, etc). */
    private PlayUIManager uiManager;
    /** Manager for world and entity rendering. */
    private WorldRenderer worldRenderer;
    /** Manager for combat systems, projectiles, and hazards. */
    private CombatManager combatManager;

    private boolean pauseRequested = false;

    private void initManagers() {
        this.worldRenderer = new WorldRenderer();
        this.combatManager = new CombatManager();
        this.particleManager = new org.example.logic.ParticleManager();
        this.soundManager = new org.example.logic.SoundManager();
        this.questManager = new org.example.logic.QuestManager();
        this.eventManager = new org.example.logic.event.EventManager();

        // Register Event Listeners
        this.eventManager.subscribe(org.example.logic.event.GameEvent.ENTITY_DEATH, this.questManager);
        this.eventManager.subscribe(org.example.logic.event.GameEvent.ITEM_PICKUP, this.questManager);
        
        uiManager = new PlayUIManager();
        dialogManager = new DialogManager();

        // Load World Manifest (Always needed for transitions)
        LevelLoader.WorldManifest manifest = LevelLoader.loadManifest("/levels/world/world_manifest.json");
        if (manifest != null) {
            this.worldManifest = manifest.maps;
        }
    }

    public PlayState() {
        initManagers();

        // Load initial level from manifest
        if (!worldManifest.isEmpty()) {
            lastConfigPath = "/levels/world/" + worldManifest.get(0);
        } else {
            lastConfigPath = "/levels/world/map1.json";
        }
        
        currentLevelConfig = LevelLoader.loadConfig(lastConfigPath);
        currentLevel = MapGenerator.generate(currentLevelConfig);
        this.currentMapSeed = currentLevel.seed;

        gameMap = new GameMap(currentLevel);

        // Player starting position (middle of the map)
        player = new Player(currentLevelConfig.width * currentLevelConfig.tileSize / 2.0,
                currentLevelConfig.height * currentLevelConfig.tileSize / 2.0);

        maxTime = currentLevelConfig.tribulationTime;
        tribulationTimer = new TribulationTimer(maxTime, () -> combatManager.triggerTribulation(this));
        tribulationTimer.start();
        enemies = new ArrayList<>();
        itemsOnGround = new ArrayList<>();
        projectiles = new ArrayList<>();
        activeStrikes = new ArrayList<>();

        spawnInitialEnemies();
        spawnInitialItems();
        addStartingItems();

        generateMapCache();
    }

    /**
     * Specialized constructor to restore a game from SaveData.
     * Skips randomization and uses fixed seed and entity positions.
     */
    public PlayState(SaveData data) {
        initManagers();

        // Restore Level
        lastConfigPath = data.levelConfigPath;
        if (lastConfigPath == null)
            lastConfigPath = "/levels/world/map1.json";
        currentLevelConfig = LevelLoader.loadConfig(lastConfigPath);
        currentLevel = MapGenerator.generate(currentLevelConfig, data.mapSeed);
        this.currentMapSeed = data.mapSeed;
        
        this.mapLevel = data.mapLevel;
        this.currentLevelIndex = data.currentLevelIndex;

        gameMap = new GameMap(currentLevel);

        // Restore Player
        player = new Player(data.playerX, data.playerY);
        player.setStats(data.hp, data.maxHp, data.qi, data.maxQi);
        player.setActiveHotbarSlot(data.activeHotbarSlot);

        // Restore Inventory (Exact Slots)
        if (data.inventoryItemIds != null) {
            org.example.item.Item[] main = player.getInventory().getMainInventory();
            for (int i = 0; i < Math.min(main.length, data.inventoryItemIds.size()); i++) {
                String id = data.inventoryItemIds.get(i);
                main[i] = (id != null) ? org.example.item.ItemRegistry.createItem(id) : null;
            }
        }
        if (data.hotbarItemIds != null) {
            org.example.item.Item[] hot = player.getInventory().getHotbar();
            for (int i = 0; i < Math.min(hot.length, data.hotbarItemIds.size()); i++) {
                String id = data.hotbarItemIds.get(i);
                hot[i] = (id != null) ? org.example.item.ItemRegistry.createItem(id) : null;
            }
        }

        // Restore World Timers
        maxTime = currentLevelConfig.tribulationTime;
        tribulationTimer = new TribulationTimer(data.currentTime, () -> combatManager.triggerTribulation(this));
        if (data.inTribulationFlag == 1) {
            inTribulation = true;
        } else {
            tribulationTimer.start();
        }

        // Restore Entities
        enemies = new ArrayList<>();
        for (SaveData.EnemySaveData eData : data.activeEnemies) {
            enemies.add(
                    EnemyRegistry.createEnemy(eData.id, eData.x, eData.y, eData.tribulationFlag == 1, eData.scaling));
            // Note: Currently EnemyRegistry.createEnemy doesn't take current HP, but we
            // could add it if needed.
        }

        itemsOnGround = new ArrayList<>();
        for (SaveData.ItemSaveData iData : data.itemsOnGround) {
            itemsOnGround.add(new WorldItem(ItemRegistry.createItem(iData.id), iData.x, iData.y));
        }

        // Restore Cultivation
        player.getCultivationManager().setCurrentRankIndex(data.cultivationIndex);

        // Restore Active Skill
        if (data.activeSkillId != null) {
            org.example.logic.Skill skill = org.example.logic.SkillRegistry.getSkill(data.activeSkillId);
            if (skill != null) {
                player.setActiveSkill(skill);
            }
        }

        // Restore Quests
        if (data.activeQuests != null) {
            for (SaveData.QuestSaveData qsd : data.activeQuests) {
                org.example.logic.Quest q = org.example.logic.QuestRegistry.createQuest(qsd.id);
                if (q != null) {
                    q.setCurrentAmount(qsd.currentAmount);
                    questManager.addQuest(q, this);
                }
            }
        }
        if (data.completedQuestIds != null) {
            for (String qid : data.completedQuestIds) {
                org.example.logic.Quest q = org.example.logic.QuestRegistry.createQuest(qid);
                if (q != null) {
                    questManager.getCompletedQuests().add(q);
                }
            }
        }

        activeStrikes = new ArrayList<>();
        projectiles = new ArrayList<>();

        generateMapCache();
        uiManager = new PlayUIManager();
        dialogManager = new DialogManager();

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
     * Spawns initial items on the ground based on the LevelConfig definitions.
     */
    private void spawnInitialItems() {
        if (currentLevelConfig.initialWorldItems == null)
            return;

        for (String itemId : currentLevelConfig.initialWorldItems) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(16, player.getX(), player.getY(), 150);
            if (pos != null) {
                itemsOnGround.add(new org.example.item.WorldItem(org.example.item.ItemRegistry.createItem(itemId),
                        pos[0], pos[1]));
            }
        }
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
                // Scaling factor based on map level: 1.3^ (level - 1)
                double scaleFactor = Math.pow(1.3, mapLevel - 1);
                enemies.add(EnemyRegistry.createEnemy(enemyId, pos[0], pos[1], false, scaleFactor));
            }
        }
    }

    /**
     * Adds initial starting items to the player's inventory as defined in
     * game_config.json.
     */
    private void addStartingItems() {
        if (player != null && player.getInventory() != null) {
            java.util.List<String> startingItems = org.example.ConfigManager.getInstance()
                    .getConfig().player.startingItems;
            if (startingItems != null) {
                for (String itemId : startingItems) {
                    player.getInventory().addItem(org.example.item.ItemRegistry.createItem(itemId));
                }
            }
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

        // --- World & Combat Logic ---
        // We handle these BEFORE UI so that a click closing a UI element 
        // doesn't immediately trigger a world action in the same frame.
        handleHotbarSelection();
        handleWorldInteraction();

        // --- UI Interactions ---
        if (inventoryOpen) {
            handleInventoryInteraction();
        }

        if (dialogManager.isActive()) {
            handleDialogueInteraction();
        }

        if (cultivationMenuOpen) {
            handleCultivationInteraction();
            player.setMeditating(true);
        } else {
            player.setMeditating(false);
        }
        handleGameplayLogic(deltaTime);
        updateCamera();

        mapAnimationTimer += deltaTime;
        if (mapAnimationTimer > 10.0)
            mapAnimationTimer -= 10.0;

        // --- Update Notifications ---
        for (int i = notifications.size() - 1; i >= 0; i--) {
            notifications.get(i).timer -= deltaTime;
            if (notifications.get(i).timer <= 0)
                notifications.remove(i);
        }

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
     * Handles dialogue progression.
     */
    private void handleDialogueInteraction() {
        boolean ePressed = Input.isKeyPressed(KeyCode.E);
        boolean lmbPressed = Input.isLmbPressed();
        double mx = Input.getMouseX();
        double my = Input.getMouseY();

        if (ePressed && !eWasPressed) {
            dialogManager.advance(this);
            eWasPressed = ePressed;
            return;
        }

        // Choice selection via number keys or mouse click
        java.util.List<org.example.logic.DialogueChoice> choices = (dialogManager.getCurrentNode() != null)
                ? dialogManager.getCurrentNode().getChoices()
                : null;

        if (choices != null && !choices.isEmpty()) {
            // Number keys
            for (int i = 1; i <= Math.min(9, choices.size()); i++) {
                KeyCode code = KeyCode.valueOf("DIGIT" + i);
                if (Input.isKeyPressed(code)) {
                    dialogManager.selectChoice(i - 1, this);
                    break;
                }
            }

            // Mouse click support
            if (lmbPressed && !lmbWasPressed) {
                double width = 800;
                double x = (screenWidth - width) / 2.0;
                double y = screenHeight - 180 - 50;
                double choiceY = y + 100;

                for (int i = 0; i < choices.size(); i++) {
                    // Check if mouse is over this choice line (expanded area)
                    if (mx >= x + 10 && mx <= x + width - 10 && my >= choiceY - 18 && my <= choiceY + 12) {
                        GameLogger.info("[DIALOGUE] Selected choice index: " + i + " (" + choices.get(i).getText() + ")");
                        dialogManager.selectChoice(i, this);
                        break;
                    }
                    choiceY += 25;
                }
            }
        }
    }

    /**
     * Handles interaction with objects in the game world (e.g., picking up items).
     * Now triggered by the 'E' key and uses proximity check for the nearest item.
     */
    private void handleWorldInteraction() {
        if (dialogManager.isActive())
            return;

        boolean ePressed = Input.isKeyPressed(KeyCode.E);
        if (ePressed && !eWasPressed) {
            org.example.logic.Interactable best = null;
            double minDist = Double.MAX_VALUE;

            // Collect all potential interactables in the world
            java.util.List<org.example.logic.Interactable> all = new java.util.ArrayList<>();
            all.addAll(currentLevel.interactables);
            all.addAll(itemsOnGround);
            if (currentLevel.gate != null)
                all.add(currentLevel.gate);

            for (org.example.logic.Interactable inter : all) {
                double dist = Math
                        .sqrt(Math.pow(inter.getX() - player.getX(), 2) + Math.pow(inter.getY() - player.getY(), 2));
                if (dist < inter.getInteractionRange() && dist < minDist) {
                    minDist = dist;
                    best = inter;
                }
            }

            if (best != null) {
                best.onInteract(this);
            }
        }
        eWasPressed = ePressed;
    }

    public void setVictory() {
        this.currentMode = PlayMode.VICTORY;
    }

    private void handleToggles() {
        boolean escPressed = Input.isKeyPressed(KeyCode.ESCAPE);
        if (escPressed && !escWasPressed) {
            pauseRequested = true;
        }
        escWasPressed = escPressed;

        boolean invIsPressed = Input.isKeyPressed(KeyCode.I);
        if (invIsPressed && !inventoryWasPressed) {
            inventoryOpen = !inventoryOpen;
            if (!inventoryOpen && draggedItem != null && sourceArr != null) {
                sourceArr[sourceIdx] = draggedItem;
                draggedItem = null;
            }
        }
        inventoryWasPressed = invIsPressed;

        boolean qIsPressed = Input.isKeyPressed(KeyCode.Q);
        if (qIsPressed && !questLogWasPressed) {
            questLogOpen = !questLogOpen;
        }
        questLogWasPressed = qIsPressed;

        boolean mapIsPressed = Input.isKeyPressed(KeyCode.M);
        if (mapIsPressed && !mapWasPressed)
            showFullMap = !showFullMap;
        mapWasPressed = mapIsPressed;

        // --- CULTIVATION MENU TOGGLE ---
        if (Input.isKeyPressed(KeyCode.C)) {
            if (!cWasPressed) {
                cultivationMenuOpen = !cultivationMenuOpen;
                // Game should pause while cultivating? Usually yes in solo RPGs.
                // We'll keep it simple for now and just show the menu.
            }
            cWasPressed = true;
        } else {
            cWasPressed = false;
        }

        // --- PAUSE MENU TOGGLE ---
        boolean bIsPressed = Input.isKeyPressed(KeyCode.B);
        if (bIsPressed && !bWasPressed) {
            org.example.logic.CultivationManager cm = player.getCultivationManager();
            if (cm.attemptBreakthrough(player)) {
                System.out.println("BREAKTHROUGH SUCCESSFUL! New Rank: " + cm.getCurrentRank().getFullName());
                particleManager.spawnQiBurst(player.getX() + player.getSize() / 2,
                        player.getY() + player.getSize() / 2);
                soundManager.playSfx("breakthrough");
            } else {
                if (cm.getNextRank() == null) {
                    System.out.println("Maximum Realm Achieved!");
                } else {
                    System.out.println("Insufficient Qi for breakthrough. Required: "
                            + cm.getNextRank().getRequiredQiToBreakthrough() + ", current: " + player.getQi());
                }
            }
        }
        bWasPressed = bIsPressed;
    }

    /**
     * Helper to check if mouse is within a rectangle.
     */
    public static boolean isInside(double mx, double my, double x, double y, double w, double h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public boolean isInside(double mx, double my, double x, double y, double s) {
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

        // Use item in hotbar (F key or clicking while not a weapon)
        // Ensure we don't trigger this if a dialogue is active or other UI is blocking
        if (dialogManager.isActive() || inventoryOpen || cultivationMenuOpen || questLogOpen) return;

        boolean lmbPressed = Input.isLmbPressed();
        if (Input.isKeyPressed(KeyCode.F) || (lmbPressed && !lmbWasPressed)) {
            int activeSlot = player.getActiveHotbarSlot();
            Item activeItem = player.getInventory().getItemInHotbar(activeSlot);

            if (activeItem != null && activeItem.getType() != Item.Type.WEAPON) {
                activeItem.use(player, this);

                // If it's a consumable, remove it
                if (activeItem.getType() == Item.Type.CONSUMABLE) {
                    player.getInventory().getHotbar()[activeSlot] = null;
                }
                // (SKILL_BOOK is removed in DialogManager upon confirmation)
            }
        }

        combatManager.handleFiring(this);
    }

    /**
     * Transitions to the next level by regenerating the world.
     */
    private void nextLevel() {
        System.out.println("Transcending to the next realm...");

        // Reset state
        inTribulation = false;
        currentMode = PlayMode.PLAYING;

        activeStrikes.clear();
        enemies.clear();
        projectiles.clear();
        itemsOnGround.clear();

        // Load configuration and regenerate - Sequential progression from manifest
        currentLevelIndex++;
        if (currentLevelIndex >= worldManifest.size()) {
            // Loop back to start but keep mapLevel scaling!
            currentLevelIndex = 0;
        }

        // Increase world level for scaling
        mapLevel++;

        String levelFile = "/levels/world/" + worldManifest.get(currentLevelIndex);
        lastConfigPath = levelFile;
        currentLevelConfig = LevelLoader.loadConfig(levelFile);
        currentLevel = MapGenerator.generate(currentLevelConfig);
        gameMap = new GameMap(currentLevel);

        // Sync Timers
        tribulationTimer.reset(currentLevelConfig.tribulationTime);

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

        combatManager.update(this, deltaTime);
        particleManager.update(deltaTime);

        player.update(currentLevel, deltaTime);
        for (Enemy enemy : enemies)
            enemy.update(gameMap, player, enemies, deltaTime, projectiles);

        // Clean up dead enemies and track if any died this frame
        boolean enemyDied = false;
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (enemy.isDead()) {
                enemyDied = true;

                eventManager.triggerEvent(org.example.logic.event.GameEvent.ENTITY_DEATH, enemy.getId(), 1, this);

                // --- Qi Absorption System (Refinement) ---
                // Defeating enemies now permanently increases Max Qi
                // Balanced to scale with rank requirements (approx 2.2x per map level)
                double maxQiGain = 5.0 * Math.pow(2.2, mapLevel - 1);
                if (enemy.isTribulation())
                    maxQiGain *= 10.0; // Tribulation kills are massive boosts

                player.setMaxQi(player.getMaxQi() + maxQiGain);
                player.restoreQi(maxQiGain * 2.0); // Refund enough for next attack
                addNotification(String.format("+%.0f Max Qi Refined", maxQiGain));

                // Roll for loot
                java.util.List<String> drops = org.example.logic.LootRegistry.rollLoot(enemy.getId());
                for (String itemId : drops) {
                    itemsOnGround.add(new WorldItem(org.example.item.ItemRegistry.createItem(itemId), enemy.getX(),
                            enemy.getY()));
                }

                enemies.remove(i);
            }
        }

        // --- Event-Driven Victory Check (Survival) ---
        // Victory triggers if an enemy died, we are in Tribulation, and no Tribulation
        // enemies remain.
        if (enemyDied && !levelVictoryAchieved && inTribulation) {
            if (countLivingTribulationEnemies() == 0) {
                GameLogger.info("[EVENT] Final Tribulation enemy defeated! Victory Achieved.");
                levelVictoryAchieved = true;
                currentMode = PlayMode.VICTORY;
            }
        }

        if (!inTribulation) {
            // Background thread is already counting down
        }

        // --- Gate of Realms animation update ---
        if (currentLevel.gate != null) {
            currentLevel.gate.update(deltaTime);
        }
    }

    /**
     * Completely resets the level by regenerating the map and resetting entity
     * states.
     * Triggered on player death.
     */
    private void resetLevel() {
        inTribulation = false;
        isPaused = false;
        // Load configuration from JSON
        lastConfigPath = (lastConfigPath != null) ? lastConfigPath : "/levels/level_small.json";
        currentLevelConfig = LevelLoader.loadConfig(lastConfigPath);
        currentLevel = MapGenerator.generate(currentLevelConfig);
        gameMap = new GameMap(currentLevel);
        player = new Player(currentLevelConfig.width * currentLevelConfig.tileSize / 2.0,
                currentLevelConfig.height * currentLevelConfig.tileSize / 2.0);

        tribulationTimer.reset(currentLevelConfig.tribulationTime);

        enemies.clear();
        spawnInitialEnemies();
        addStartingItems();

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
        // Use the dedicated World Renderer for entities and tiles
        worldRenderer.render(gc, this);

        // Use the dedicated UI Manager for overlays
        uiManager.render(gc, this);
    }

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
                        item.use(player, this);
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
                        item.use(player, this);
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

    public void addNotification(String message) {
        notifications.add(new Notification(message, 3.5)); // Display for 3.5 seconds
        if (notifications.size() > 5)
            notifications.remove(0); // Max 5 at once
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    private void handleCultivationInteraction() {
        double mx = Input.getMouseX(), my = Input.getMouseY(), w = screenWidth;
        boolean lmbPressed = Input.isLmbPressed();

        double panelW = 600, panelH = 450;
        double x = (w - panelW) / 2, y = (screenHeight - panelH) / 2;

        if (lmbPressed && !lmbWasPressed) {
            org.example.logic.CultivationManager cm = player.getCultivationManager();
            if (cm.getNextRank() != null) {
                // Button position: x + panelW/2 - 150, y + panelH - 80, 300, 50
                if (isInside(mx, my, x + panelW / 2 - 150, y + panelH - 80, 300, 50)) {
                    boolean success = cm.attemptBreakthrough(player);
                    if (success) {
                        GameLogger.info("突破! Breakthrough successful!");
                        soundManager.playSfx("level_up");
                        // Spawn golden particles around player
                        particleManager.spawnBreakthroughEffect(player.getX() + player.getSize() / 2,
                                player.getY() + player.getSize() / 2);
                    }
                }
            }
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
     * Gathers all relevant game state and serializes it to a persistent JSON file.
     * Captures player stats, full inventory, world timer, and a detailed list of
     * all active entities (enemies and items on ground) to ensure 100% restoration.
     * 
     * @param slot The save slot to use (1-5).
     */
    public void performSave(int slot) {
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
            data.currentTime = tribulationTimer.getRemainingSeconds();
            data.mapLevel = this.mapLevel;
            data.currentLevelIndex = this.currentLevelIndex;
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

            GameLogger.info("[SAVE] Serializing " + (enemies != null ? enemies.size() : 0) + " enemies...");

            // Save Enemies
            if (enemies != null) {
                GameLogger.info("[SAVE] Found " + enemies.size() + " active enemies.");
                for (Enemy e : enemies) {
                    SaveData.EnemySaveData ed = new SaveData.EnemySaveData();
                    ed.id = e.getId();
                    ed.x = e.getX();
                    ed.y = e.getY();
                    ed.hp = e.getHp();
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

            // Save World State Persistence
            data.worldFlags = org.example.logic.WorldState.getInstance().getFlags();
            data.worldCounters = org.example.logic.WorldState.getInstance().getCounters();

            // Save Cultivation
            data.cultivationIndex = player.getCultivationManager().getCurrentRankIndex();

            // Save Active Skill
            if (player.getActiveSkill() != null) {
                data.activeSkillId = player.getActiveSkill().getId();
            }

            // Save Quests
            for (org.example.logic.Quest q : questManager.getActiveQuests()) {
                SaveData.QuestSaveData qsd = new SaveData.QuestSaveData();
                qsd.id = q.getId();
                qsd.currentAmount = q.getCurrentAmount();
                data.activeQuests.add(qsd);
            }

            // Collect completed quest IDs (need to add getter to QuestManager)
            for (org.example.logic.Quest q : questManager.getCompletedQuests()) {
                data.completedQuestIds.add(q.getId());
            }

            org.example.SaveManager.save(data, slot);
        } catch (java.io.IOException e) {
            System.err.println("CRITICAL: Save failed for slot " + slot);
            e.printStackTrace();
        }
    }

    /**
     * Loads game state from a specific JSON save file and restores all systems.
     * Uses performLoadHelper to avoid duplication between new game and load.
     * 
     * @param slot The save slot to load (1-5).
     */
    public void performLoad(int slot) {
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
        this.tribulationTimer.reset(data.currentTime);
        this.inTribulation = (data.inTribulationFlag == 1);
        this.levelVictoryAchieved = (data.victoryAchievedFlag == 1);
        if (data.tribulationSpawnLimit > 0) {
            this.currentLevelConfig.tribulationTotalEnemies = data.tribulationSpawnLimit;
        }

        // Restore World State Persistence
        if (data.worldFlags != null) {
            org.example.logic.WorldState.getInstance().setFlags(data.worldFlags);
        }
        if (data.worldCounters != null) {
            org.example.logic.WorldState.getInstance().setCounters(data.worldCounters);
        }

        // Restore Enemies
        int restoredCount = 0;
        this.enemies.clear();
        if (data.activeEnemies != null) {
            GameLogger.info("[LOAD] Attempting to restore " + data.activeEnemies.size() + " enemies...");
            for (SaveData.EnemySaveData ed : data.activeEnemies) {
                boolean isT = (ed.tribulationFlag == 1);
                Enemy e = EnemyRegistry.createEnemy(ed.id, ed.x, ed.y, isT, ed.scaling);
                if (e != null) {
                    e.setHp(ed.hp);
                    this.enemies.add(e);
                    restoredCount++;
                    System.out.println("  -> Restored: " + e.getId() + " HP:" + (int) e.getHp());
                } else {
                    System.err.println("  !! Failed to restore enemy ID: " + ed.id);
                }
            }
        }
        GameLogger.info("[LOAD] Successfully restored " + restoredCount + " enemies to the world.");

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

        // Restore Cultivation
        player.getCultivationManager().setCurrentRankIndex(data.cultivationIndex);

        // Restore Active Skill
        if (data.activeSkillId != null) {
            org.example.logic.Skill skill = org.example.logic.SkillRegistry.getSkill(data.activeSkillId);
            if (skill != null) {
                player.setActiveSkill(skill);
            }
        }

        // Restore Quests
        if (data.activeQuests != null) {
            for (SaveData.QuestSaveData qsd : data.activeQuests) {
                org.example.logic.Quest q = org.example.logic.QuestRegistry.createQuest(qsd.id);
                if (q != null) {
                    q.setCurrentAmount(qsd.currentAmount);
                    questManager.addQuest(q, this);
                }
            }
        }
        if (data.completedQuestIds != null) {
            for (String qid : data.completedQuestIds) {
                org.example.logic.Quest q = org.example.logic.QuestRegistry.createQuest(qid);
                if (q != null) {
                    questManager.addCompletedQuest(q);
                }
            }
        }

        this.currentMode = PlayMode.PLAYING;
        this.isPaused = false;
        generateMapCache();
        GameLogger.info("Game loaded successfully from slot " + slot);
    }

    /**
     * Heals the player by a specific amount, up to max HP.
     */
    public void heal(double amount) {
        if (player.getHp() + amount > player.getMaxHp()) {
            player.setStats(player.getMaxHp(), player.getMaxHp(), player.getQi(), player.getMaxQi());
        } else {
            player.setStats(player.getHp() + amount, player.getMaxHp(), player.getQi(), player.getMaxQi());
        }
    }

    /**
     * Restores the player's Qi by a specific amount, up to max Qi.
     */
    public void restoreQi(double amount) {
        if (player.getQi() + amount > player.getMaxQi()) {
            player.setStats(player.getHp(), player.getMaxHp(), player.getMaxQi(), player.getMaxQi());
        } else {
            player.setStats(player.getHp(), player.getMaxHp(), player.getQi() + amount, player.getMaxQi());
        }
    }

    // --- GETTERS FOR UI & REFACTORING ---
    public CombatManager getCombatManager() {
        return combatManager;
    }

    public org.example.logic.ParticleManager getParticleManager() {
        return particleManager;
    }

    public org.example.logic.SoundManager getSoundManager() {
        return soundManager;
    }

    public org.example.logic.event.EventManager getEventManager() {
        return eventManager;
    }

    public org.example.logic.QuestManager getQuestManager() {
        return questManager;
    }

    public Player getPlayer() {
        return player;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public List<WorldItem> getItemsOnGround() {
        return itemsOnGround;
    }

    public boolean isInTribulation() {
        return inTribulation;
    }

    public double getCurrentTime() {
        return tribulationTimer.getRemainingSeconds();
    }

    public PauseMenuState getCurrentPauseState() {
        return currentPauseState;
    }

    public void setCurrentPauseState(PauseMenuState state) {
        this.currentPauseState = state;
    }

    public PlayUIManager getUiManager() {
        return uiManager;
    }

    public DialogManager getDialogManager() {
        return dialogManager;
    }

    public boolean isPauseRequested() {
        return pauseRequested;
    }

    public void setPauseRequested(boolean pauseRequested) {
        this.pauseRequested = pauseRequested;
    }

    public boolean isLevelVictoryAchieved() {
        return levelVictoryAchieved;
    }

    public boolean isInventoryOpen() {
        return inventoryOpen;
    }

    public boolean isQuestLogOpen() {
        return questLogOpen;
    }

    public Item getDraggedItem() {
        return draggedItem;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public double getCameraX() {
        return cameraX;
    }

    public double getCameraY() {
        return cameraY;
    }

    public Level getCurrentLevel() {
        return currentLevel;
    }

    public double getMapAnimationTimer() {
        return mapAnimationTimer;
    }

    public WritableImage getMapCache() {
        return mapCache;
    }

    public PlayMode getCurrentMode() {
        return currentMode;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isShowingFullMap() {
        return showFullMap;
    }

    public List<Projectile> getProjectiles() {
        return projectiles;
    }

    public List<LightningStrike> getActiveStrikes() {
        return activeStrikes;
    }

    public List<CombatManager.BurstTracker> getPendingBursts() {
        return pendingBursts;
    }

    public LevelConfig getCurrentLevelConfig() {
        return currentLevelConfig;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public double getLightningTimer() {
        return lightningTimer;
    }

    public void setLightningTimer(double timer) {
        this.lightningTimer = timer;
    }

    public String getLastConfigPath() {
        return lastConfigPath;
    }

    public long getCurrentMapSeed() {
        return currentMapSeed;
    }

    public double getMaxTime() {
        return maxTime;
    }

    public void setInTribulation(boolean val) {
        this.inTribulation = val;
    }

    public int countLivingTribulationEnemies() {
        int count = 0;
        if (enemies != null) {
            for (Enemy e : enemies) {
                if (e.isTribulation() && e.getHp() > 0)
                    count++;
            }
        }
        return count;
    }
}
