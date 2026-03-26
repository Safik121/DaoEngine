package org.example.item;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents an item that exists in the game world, waiting to be picked up.
 */
public class WorldItem {
    private Item item;
    private double x, y;
    private double size = 16.0;

    /**
     * Constructs a new item in the game world.
     * 
     * @param item The underlying Item data.
     * @param x The world X coordinate.
     * @param y The world Y coordinate.
     */
    public WorldItem(Item item, double x, double y) {
        this.item = item;
        this.x = x;
        this.y = y;
    }

    /**
     * Renders the item in the world with a golden border and orange fill.
     * 
     * @param gc The GraphicsContext to draw on.
     */
    public void render(GraphicsContext gc) {
        // Draw a small crate-like icon with an indicator color
        gc.setFill(Color.web("#d4af37")); // Golden border
        gc.fillRect(x - 2, y - 2, size + 4, size + 4);
        
        gc.setFill(Color.ORANGE);
        gc.fillRect(x, y, size, size);
        
        // Tooltip or label placeholder
        gc.setFill(Color.WHITE);
        gc.setFont(new javafx.scene.text.Font("Arial", 8));
        // Optional: gc.fillText(item.getName(), x, y - 5);
    }

    /**
     * Checks if the item was clicked by the player (includes a small margin).
     * 
     * @param mx Mouse X coordinate.
     * @param my Mouse Y coordinate.
     * @return True if clicked.
     */
    public boolean isClicked(double mx, double my) {
        return mx >= x - 5 && mx <= x + size + 5 && my >= y - 5 && my <= y + size + 5;
    }

    public Item getItem() { return item; }
    public double getX() { return x; }
    public double getY() { return y; }
}
