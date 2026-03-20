package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * The state representing the main menu of the game.
 * It displays the game title and instructions to start the game.
 */
public class MenuState implements GameState {

    /**
     * Updates the menu state.
     * Currently, no logic updates are needed for the menu.
     */
    @Override
    public void update() {
        // No logic updates for now
    }

    /**
     * Renders the main menu UI.
     * Draws the background, game title, and start instructions.
     * 
     * @param gc The GraphicsContext used for drawing.
     */
    @Override
    public void render(GraphicsContext gc) {
        // Draw black background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 800, 600);

        // Draw game title
        gc.setFill(Color.ORANGE);
        gc.setFont(new Font(40));
        gc.fillText("DaoEngine: Path to Immortality", 150, 200);

        // Draw start instruction
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(20));
        gc.fillText("Press ENTER to start the game", 250, 300);
    }
}