package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * The state representing the active gameplay level.
 * Handles the rendering and logic for the game's first level.
 */
public class PlayState implements GameState {

    /**
     * Updates the gameplay logic.
     * Future implementation will include timers and player movement.
     */
    @Override
    public void update() {
        // Placeholder for future Tribulation timer and player movement logic
    }

    /**
     * Renders the gameplay level.
     * Currently displays a dark green background and a placeholder message.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
    @Override
    public void render(GraphicsContext gc) {
        // Render game area with dark green background (representing grass)
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, 800, 600);

        gc.setFill(Color.WHITE);
        gc.setFont(new Font(30));
        gc.fillText("Gameplay will be here (Level 1)", 200, 300);
    }
}