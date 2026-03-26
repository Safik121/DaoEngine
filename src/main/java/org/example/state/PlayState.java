package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.example.Input;
import org.example.entity.Player;
import org.example.level.Level;
import org.example.level.LevelLoader;

/**
 * The game state where the actual gameplay takes place.
 * It handles level rendering and gameplay logic updates.
 */
public class PlayState implements GameState {

    private Level currentLevel;
    /** The player entity in the current session. */
    private Player player;

    /** The maximum time allowed to complete the level (in seconds). */
    private double maxTime = 60.0;
    /** The current remaining time for the level. */
    private double currentTime = maxTime;

    /** Flag indicating if the Heavenly Tribulation survival phase has started. */
    private boolean inTribulation = false;

    /** Flags for the pause system. */
    private boolean isPaused = false;
    private boolean escWasPressed = false;

    /**
     * Initializes the play state and loads the initial level.
     */
    public PlayState() {
        // Load the level immediately when the game starts
        currentLevel = LevelLoader.loadLevel("/levels/level1.json");
        // Initialize the player at the starting position (offset by 4 if needed, or 36)
        player = new Player(36, 36);
    }

    /**
     * Updates the gameplay logic, including player movement, collisions, and timer.
     */
    @Override
    public void update() {
        // --- 1. PAUSE LOGIC (Single-fire mechanism) ---
        boolean escIsPressed = Input.isKeyPressed(KeyCode.ESCAPE);

        // Toggle pause only on the exact frame the key is pressed down
        if (escIsPressed && !escWasPressed) {
            isPaused = !isPaused;
        }
        escWasPressed = escIsPressed; // Save state for the next frame

        // If the game is paused, stop all entity and timer updates
        if (isPaused) {
            return;
        }

        // --- 2. GAMEPLAY LOGIC (Runs only when not paused) ---
        if (player != null && currentLevel != null) {
            player.update(currentLevel);
        }

        // Countdown logic runs only if we are not yet in the Tribulation phase
        if (!inTribulation) {
            if (currentTime > 0) {
                currentTime -= 1.0 / 60.0;
            } else {
                currentTime = 0;
                triggerTribulation(); // Time's up, survival phase begins!
            }
        } else {
            // TODO: Here we will later update enemy spawners for the Tribulation phase
        }
    }

    /**
     * Triggers the Heavenly Tribulation event when the timer reaches zero.
     * Transitions the game into the survival phase.
     */
    private void triggerTribulation() {
        inTribulation = true;
        System.out.println("THE HEAVENLY TRIBULATION HAS DESCENDED! SURVIVE!");
    }

    /**
     * Renders the game world and entities using the provided GraphicsContext.
     * * @param gc The GraphicsContext used for drawing.
     */
    @Override
    public void render(GraphicsContext gc) {
        // Clear the screen with a black background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);

        // Render the level if it was successfully loaded
        if (currentLevel != null && currentLevel.data != null) {
            int tileSize = currentLevel.tileSize;

            // Iterate through the grid (rows and columns) to draw tiles
            for (int y = 0; y < currentLevel.data.size(); y++) {
                var row = currentLevel.data.get(y);
                for (int x = 0; x < row.size(); x++) {
                    int tileType = row.get(x);

                    // 1 = Wall (Gray), 0 = Path/Grass (Green)
                    if (tileType == 1) {
                        gc.setFill(Color.DARKGRAY);
                    } else {
                        gc.setFill(Color.DARKGREEN);
                    }

                    // Draw the tile
                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);

                    // Draw a thin black border around the tile for the grid effect
                    gc.setStroke(Color.BLACK);
                    gc.strokeRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }
        }

        // Render the player on top of the level
        if (player != null) {
            player.render(gc);
        }

        // Render the HUD (Heads-Up Display)
        drawHUD(gc);
    }

    /**
     * Draws the HUD elements, including the tribulation timer bar, status messages, and pause screen.
     * * @param gc The GraphicsContext used for drawing.
     */
    private void drawHUD(GraphicsContext gc) {
        // Only draw the timer bar if Tribulation hasn't started yet
        if (!inTribulation) {
            // Background for the timer bar (dark gray with alpha)
            gc.setFill(Color.rgb(50, 50, 50, 0.7));
            gc.fillRect(10, 10, 200, 20);

            // The actual time bar (indicates remaining time)
            double timeBarWidth = (currentTime / maxTime) * 200;
            gc.setFill(Color.ORANGERED);
            gc.fillRect(10, 10, timeBarWidth, 20);

            // Border around the bar
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeRect(10, 10, 200, 20);

            // Text displaying the level name and current countdown status
            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 14));
            gc.fillText(currentLevel.name + " | Time to Tribulation: " + (int)currentTime + "s", 10, 50);
        } else {
            // Tribulation Active UI
            // Light red tint over the whole screen to indicate danger
            gc.setFill(Color.color(1, 0, 0, 0.2));
            gc.fillRect(0, 0, 800, 600);

            gc.setFill(Color.RED);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 24));
            gc.fillText("TRIBULATION ACTIVE: SURVIVE!", 10, 30);
        }

        // Draw Pause Screen Overlay
        if (isPaused) {
            gc.setFill(Color.color(0, 0, 0, 0.6)); // Semi-transparent black
            gc.fillRect(0, 0, 800, 600);

            gc.setFill(Color.WHITE);
            gc.setFont(new javafx.scene.text.Font("Arial Bold", 60));
            gc.fillText("PAUSED", 280, 300);
        }
    }
}