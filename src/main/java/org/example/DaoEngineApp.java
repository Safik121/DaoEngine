package org.example;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import org.example.state.*;
import org.example.item.ItemRegistry;
import org.example.item.WeaponRegistry;
import org.example.entity.EnemyRegistry;
import org.example.logic.CultivationRegistry;
import org.example.logic.SkillRegistry;
import org.example.logic.LootRegistry;
import org.example.logic.QuestRegistry;
import org.example.logic.DialogueRegistry;
import org.example.logic.SoundManager;

/**
 * The main application class for DaoEngine: Path to Immortality.
 * This class initializes the JavaFX environment, sets up the game loop,
 * and manages transitions between different game states.
 */
public class DaoEngineApp extends Application {

    /** Current game window width. */
    private static int width;
    /** Current game window height. */
    private static int height;

    private static Stage mainStage;
    private static Canvas mainCanvas;
    private static GraphicsContext mainGC;

    /** NanoTime of the previous frame for delta time calculation. */
    private long lastNanoTime = 0;
    /** Current active game state. */
    private GameState currentState;
    private GameState lastStateBeforeLexicon = null;

    /**
     * Initializes the JavaFX stage, scene, and canvas.
     * Sets up the initial game state and the input event handlers.
     * 
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        GameConfig config = ConfigManager.getInstance().getConfig();
        width = config.engine.width;
        height = config.engine.height;

        primaryStage.setTitle(config.engine.title);

        Group root = new Group();
        Scene scene = new Scene(root, width, height);
        mainCanvas = new Canvas(width, height);
        root.getChildren().add(mainCanvas);
        mainGC = mainCanvas.getGraphicsContext2D();

        // 0. Initialize Game Registries and Logger
        String logLevel = config.engine.loggingLevel;
        if (getParameters() != null && getParameters().getNamed().containsKey("log")) {
            logLevel = getParameters().getNamed().get("log");
        }
        GameLogger.initialize(logLevel);
        initRegistries();

        // 1. Initial game state is the Main Menu
        currentState = new MenuState();

        // 2. Keyboard Event Handling
        scene.setOnKeyPressed(event -> {
            Input.addKey(event.getCode()); // Register the key as active

            // State transition to game on ENTER
            if (event.getCode() == KeyCode.ENTER && currentState instanceof MenuState) {
                currentState = new PlayState();
            }
        });

        scene.setOnKeyReleased(event -> {
            Input.removeKey(event.getCode()); // Remove the key when released
        });

        // 3. Mouse Event Handling
        scene.setOnMouseMoved(event -> {
            Input.setMousePosition(event.getX(), event.getY());
        });

        scene.setOnMouseDragged(event -> {
            Input.setMousePosition(event.getX(), event.getY());
        });

        scene.setOnMousePressed(event -> {
            Input.setMouseButton(event.getButton(), true);
        });

        scene.setOnMouseReleased(event -> Input.setMouseButton(event.getButton(), false));

        scene.setOnScroll(event -> Input.addScroll(event.getDeltaY()));

        // 4. Main Game Loop using AnimationTimer
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long currentNanoTime) {
                if (lastNanoTime == 0) {
                    lastNanoTime = currentNanoTime;
                    return;
                }

                double deltaTime = (currentNanoTime - lastNanoTime) / 1_000_000_000.0;
                lastNanoTime = currentNanoTime;

                // Cap delta time to avoid huge jumps (e.g., during window move or freeze)
                if (deltaTime > 0.1)
                    deltaTime = 0.1;

                update(deltaTime);
                render(mainGC);
            }
        };
        gameLoop.start();

        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setFullScreen(config.engine.fullscreen);
        primaryStage.show();
    }

    /**
     * Updates the current game state's logic based on the active state.
     * Manages transitions between states like Menu, Play, Pause, and Game Over.
     * 
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    private void update(double deltaTime) {
        if (currentState == null) return;

        currentState.update(deltaTime);

        // --- STATE TRANSITION LOGIC ---
        if (currentState instanceof MenuState menu) {
            handleMenuTransitions(menu);
        } else if (currentState instanceof LoadState load) {
            handleLoadTransitions(load);
        } else if (currentState instanceof LexiconState lexicon) {
            handleLexiconTransitions(lexicon);
        } else if (currentState instanceof PlayState play) {
            handlePlayTransitions(play);
        } else if (currentState instanceof PauseState pause) {
            handlePauseTransitions(pause);
        } else if (currentState instanceof LoadingState loading) {
            if (loading.isFinished()) {
                currentState = loading.getTargetState();
            }
        } else if (currentState instanceof GameOverState gameOver) {
            handleGameOverTransitions(gameOver);
        }
    }

    /** @param menu The menu state to handle. */
    private void handleMenuTransitions(MenuState menu) {
        if (menu.isStartGameRequested()) {
            currentState = new LoadingState(new PlayState());
        } else if (menu.isLoadRequested()) {
            currentState = new LoadState();
        } else if (menu.isLexiconRequested()) {
            menu.setLexiconRequested(false);
            lastStateBeforeLexicon = currentState;
            currentState = new LexiconState();
        }
    }

    /** @param load The loading/save selection state to handle. */
    private void handleLoadTransitions(LoadState load) {
        if (load.getSelectedSave() != null) {
            currentState = new LoadingState(new PlayState(load.getSelectedSave()));
        } else if (load.isReturnToMenuRequested()) {
            currentState = new MenuState();
        }
    }

    /** @param lexicon The lexicon state to handle. */
    private void handleLexiconTransitions(LexiconState lexicon) {
        if (lexicon.isReturnRequested()) {
            currentState = (lastStateBeforeLexicon != null) ? lastStateBeforeLexicon : new MenuState();
            lastStateBeforeLexicon = null;
        }
    }

    /** @param play The active gameplay state to handle. */
    private void handlePlayTransitions(PlayState play) {
        if (play.isGameOverRequested()) {
            currentState = new GameOverState();
        } else if (play.isNextLevelRequested()) {
            currentState = new LoadingState(new PlayState(play.getNextLevelTransitionData()));
        } else if (play.isPauseRequested()) {
            play.setPauseRequested(false);
            currentState = new PauseState(play);
        }
    }

    /** @param pause The pause menu state to handle. */
    private void handlePauseTransitions(PauseState pause) {
        if (pause.isResumeRequested()) {
            pause.setResumeRequested(false);
            currentState = pause.getBackgroundState();
        } else if (pause.isReturnToMenuRequested()) {
            currentState = new MenuState();
        } else if (pause.isLexiconRequested()) {
            pause.setLexiconRequested(false);
            lastStateBeforeLexicon = currentState;
            currentState = new LexiconState();
        }
    }

    /** @param gameOver The game over state to handle. */
    private void handleGameOverTransitions(GameOverState gameOver) {
        if (gameOver.isTryAgainRequested()) {
            currentState = new LoadingState(new PlayState());
        } else if (gameOver.isReturnToMenuRequested()) {
            currentState = new MenuState();
        }
    }

    /**
     * Dynamically updates the game resolution by resizing the stage and canvas.
     * 
     * @param w New width.
     * @param h New height.
     */
    public static void updateResolution(int w, int h) {
        width = w;
        height = h;
        if (mainCanvas != null) {
            mainCanvas.setWidth(w);
            mainCanvas.setHeight(h);
        }
        if (mainStage != null) {
            mainStage.setWidth(w);
            mainStage.setHeight(h + 28); // Account for title bar height estimate
            if (!mainStage.isFullScreen()) {
                mainStage.centerOnScreen();
            }
        }
    }

    /**
     * Toggles between fullscreen and windowed mode.
     * 
     * @param fs true for fullscreen, false for windowed.
     */
    public static void updateWindowMode(boolean fs) {
        if (mainStage != null) {
            mainStage.setFullScreen(fs);
        }
    }

    /**
     * Renders the current game state's visual elements.
     * 
     * @param gc The GraphicsContext to render to.
     */
    private void render(GraphicsContext gc) {
        // Delegate render call to the active state
        if (currentState != null) {
            currentState.render(gc);
        }
    }

    /**
     * Initializes all game registries (items, enemies, quests, etc.) from JSON.
     */
    private void initRegistries() {
        WeaponRegistry.loadWeapons("/weapons/weapon_configs.json");
        ItemRegistry.loadData("/items/items.json", "/items/recipes.json");
        EnemyRegistry.loadConfigs("/enemies/enemy_configs.json");
        AssetRegistry.loadAssets("/assets.json");
        LootRegistry.loadConfigs("/levels/loot_tables.json");
        QuestRegistry.loadQuests("/levels/quests.json");
        DialogueRegistry.loadDialogues("/levels/dialogues.json");
        CultivationRegistry.loadConfigs("/levels/cultivation.json");
        SkillRegistry.loadSkills("/levels/skills.json");

        // Load Sounds
        SoundManager.loadSound("click", "/sounds/click_effect.mp3");
        SoundManager.loadSound("inventory", "/sounds/inventory_effect.mp3");
        SoundManager.loadSound("pickup", "/sounds/item_pickup.mp3");
        SoundManager.loadSound("sword_shot", "/sounds/sword_shot.mp3");
        SoundManager.loadSound("fireball_shot", "/sounds/fireball_shot.mp3");
        SoundManager.loadSound("magic_missile", "/sounds/magic_missile.mp3");
        SoundManager.loadSound("lightning", "/sounds/lightning.mp3");
        SoundManager.loadSound("beam", "/sounds/beam.mp3");
        SoundManager.loadSound("aoe_zone", "/sounds/aoe_zone.mp3");
        SoundManager.loadSound("enemy_hit", "/sounds/enemy_hit.mp3");
        SoundManager.loadSound("player_hit", "/sounds/player_hit.mp3");
        SoundManager.loadSound("thunder", "/sounds/thunder.mp3");
        SoundManager.loadSound("breakthrough", "/sounds/breaktrough.mp3");
        SoundManager.loadSound("death", "/sounds/death.mp3");
        SoundManager.loadSound("meditation", "/sounds/meditation.mp3");
        SoundManager.loadSound("quest_complete", "/sounds/quest_complete.mp3");
    }

    /**
     * Main entry point for the application.
     * 
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}