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
     * 
     * @param deltaTime Time elapsed since the last frame in seconds.
     */
    void update(double deltaTime);

    /**
     * Renders the visual elements of the game state.
     * 
     * @param gc The GraphicsContext used for drawing on the canvas.
     */
    void render(GraphicsContext gc);
}