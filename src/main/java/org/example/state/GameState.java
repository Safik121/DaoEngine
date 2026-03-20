package org.example.state;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface representing a generic game state in the DaoEngine.
 * Each state must implement methods for updating logic and rendering to the
 * screen.
 */
public interface GameState {
    /**
     * Updates the internal logic of the game state.
     * This method is called once per frame in the game loop.
     */
    void update();

    /**
     * Renders the visual elements of the game state.
     * 
     * @param gc The GraphicsContext used for drawing on the canvas.
     */
    void render(GraphicsContext gc);
}