package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
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

    /**
     * Initializes the play state and loads the initial level.
     */
    public PlayState() {
        // Load the level immediately when the game starts
        currentLevel = LevelLoader.loadLevel("/levels/level1.json");
        // Initialize the player at the starting position
        player = new Player(32, 32);
    }

    /**
     * Updates the gameplay logic.
     */
    @Override
    public void update() {
        // Future home for the Tribulation timer logic
    }

    /**
     * Renders the game world and entities using the provided GraphicsContext.
     * 
     * @param gc The GraphicsContext used for drawing.
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
    }
}