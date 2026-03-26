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

import java.util.ArrayList;
import java.util.List;

/**
 * Game state where the actual gameplay takes place.
 * Handles level rendering, logic for players and enemies, and game time.
 */
public class PlayState implements GameState {

    /** Currently loaded level. */
    private Level currentLevel;
    /** Helper map for spatial queries (spawning, collisions). */
    private GameMap gameMap;
    /** Player instance. */
    private Player player;

    /** List of all enemies currently on the map. */
    private List<Enemy> enemies;

    /** Max time before the Tribulation begins. */
    private double maxTime = 60.0;
    /** Current remaining time. */
    private double currentTime = maxTime;

    /** Whether the Tribulation (aggressive enemy waves) is active. */
    private boolean inTribulation = false;
    /** Timer for spawning additional waves during Tribulation. */
    private double tribulationSpawnTimer = 0;

    /** Pause state of the game. */
    private boolean isPaused = false;
    /** Helper for Esc key edge detection. */
    private boolean escWasPressed = false;

    /**
     * Initializes the game state, loads the level, and spawns the player and initial enemies.
     */
    public PlayState() {
        currentLevel = LevelLoader.loadLevel("/levels/level1.json");
        gameMap = new GameMap(currentLevel);

        // Spawn player at a safe position
        double[] playerPos = gameMap.getRandomFreePosition(12); // Player size is 12
        if (playerPos != null) {
            player = new Player(playerPos[0], playerPos[1]);
        } else {
            player = new Player(36, 36); // Fallback
        }

        enemies = new ArrayList<>();

        // Spawn initial regular enemies at a safe distance from the player
        for (int i = 0; i < 3; i++) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(24, player.getX(), player.getY(), 200);
            if (pos != null) {
                enemies.add(new Enemy(pos[0], pos[1], false));
            }
        }
    }

    /**
     * Updates game logic: movement, collisions, time, and entity states.
     */
    @Override
    public void update() {
        // --- 1. PAUSE LOGIC ---
        boolean escIsPressed = Input.isKeyPressed(KeyCode.ESCAPE);
        if (escIsPressed && !escWasPressed) {
            isPaused = !isPaused;
        }
        escWasPressed = escIsPressed;

        if (isPaused) {
            return;
        }

        // --- 2. DEATH LOGIC ---
        if (player != null && player.getHp() <= 0) {
            System.out.println("GAME OVER! Restarting level...");
            resetLevel();
            return;
        }

        // --- 3. GAMEPLAY LOGIC ---
        if (player != null && currentLevel != null) {
            player.update(currentLevel);
        }

        // Update all enemies
        for (Enemy enemy : enemies) {
            enemy.update(gameMap, player);
        }

        // Time management and Tribulation trigger
        if (!inTribulation) {
            if (currentTime > 0) {
                currentTime -= 1.0 / 60.0;
            } else {
                currentTime = 0;
                triggerTribulation();
            }
        } else {
            // Tribulation spawning: Every 3 seconds, add a new aggressive enemy
            tribulationSpawnTimer -= 1.0 / 60.0;
            if (tribulationSpawnTimer <= 0) {
                double[] pos = gameMap.getRandomFreePositionAwayFrom(24, player.getX(), player.getY(), 150);
                if (pos != null) {
                    enemies.add(new Enemy(pos[0], pos[1], true));
                }
                tribulationSpawnTimer = 3.0; // Reset timer
            }
        }
    }

    /**
     * Triggers the Tribulation (Heavenly Punishment) mode.
     */
    private void triggerTribulation() {
        inTribulation = true;
        System.out.println("THE HEAVENLY TRIBULATION HAS DESCENDED! SURVIVE!");

        // Immediate wave: two elite enemies
        for (int i = 0; i < 2; i++) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(24, player.getX(), player.getY(), 250);
            if (pos != null) {
                enemies.add(new Enemy(pos[0], pos[1], true));
            }
        }

        tribulationSpawnTimer = 3.0;
    }

    /**
     * Resets the level to its initial state (including player stats).
     */
    private void resetLevel() {
        inTribulation = false;
        currentTime = maxTime;
        isPaused = false;
        
        double[] playerPos = gameMap.getRandomFreePosition(12);
        if (playerPos != null) {
            player = new Player(playerPos[0], playerPos[1]);
        }
        
        enemies.clear();
        for (int i = 0; i < 3; i++) {
            double[] pos = gameMap.getRandomFreePositionAwayFrom(24, player.getX(), player.getY(), 200);
            if (pos != null) {
                enemies.add(new Enemy(pos[0], pos[1], false));
            }
        }
    }

    /**
     * Renders the current game state.
     */
    @Override
    public void render(GraphicsContext gc) {
        // Background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);

        // Map rendering
        if (currentLevel != null && currentLevel.data != null) {
            int tileSize = currentLevel.tileSize;

            for (int y = 0; y < currentLevel.data.size(); y++) {
                var row = currentLevel.data.get(y);
                for (int x = 0; x < row.size(); x++) {
                    int tileType = row.get(x);

                    if (tileType == 1) {
                        gc.setFill(Color.DARKGRAY); // Wall
                    } else {
                        gc.setFill(Color.DARKGREEN); // Grass
                    }

                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }

        // Entity rendering
        for (Enemy enemy : enemies) {
            enemy.render(gc);
        }

        if (player != null) {
            player.render(gc);
        }

        // UI bars
        drawHUD(gc);
    }

    /**
     * Renders the HUD (Heads-Up Display) with HP, Qi, and Time bars.
     */
    private void drawHUD(GraphicsContext gc) {
        // 1. Health Bar (Red)
        gc.setFill(Color.rgb(50, 50, 50, 0.7));
        gc.fillRect(10, 10, 200, 15);
        double hpWidth = (player.getHp() / player.getMaxHp()) * 200;
        gc.setFill(Color.RED);
        gc.fillRect(10, 10, hpWidth, 15);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(10, 10, 200, 15);

        // 2. Qi Bar (Cyan/Blue)
        gc.setFill(Color.rgb(50, 50, 50, 0.7));
        gc.fillRect(10, 30, 100, 10);
        double qiWidth = (player.getQi() / player.getMaxQi()) * 100;
        gc.setFill(Color.CYAN);
        gc.fillRect(10, 30, qiWidth, 10);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(10, 30, 100, 10);

        // 3. Time / Tribulation UI
        if (!inTribulation) {
            gc.setFill(Color.rgb(50, 50, 50, 0.7));
            gc.fillRect(10, 50, 200, 10);

            double timeBarWidth = (currentTime / maxTime) * 200;
            gc.setFill(Color.ORANGERED);
            gc.fillRect(10, 50, timeBarWidth, 10);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeRect(10, 50, 200, 10);

            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 12));
            gc.fillText("Level: " + currentLevel.name + " | Path to Immortality", 10, 75);
            gc.fillText("Time to Tribulation: " + (int)currentTime + "s", 10, 90);
        } else {
            // Visual effect during Tribulation
            gc.setFill(Color.color(1, 0, 0, 0.15));
            gc.fillRect(0, 0, 800, 600);

            gc.setFill(Color.RED);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 24));
            gc.fillText("TRIBULATION ACTIVE: SURVIVE!", 10, 30);
        }

        // Pause screen
        if (isPaused) {
            gc.setFill(Color.color(0, 0, 0, 0.6));
            gc.fillRect(0, 0, 800, 600);

            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 60));
            gc.fillText("PAUSED", 280, 300);
        }
    }
}