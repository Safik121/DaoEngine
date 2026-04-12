package org.example.item;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.logic.Interactable;
import org.example.logic.event.GameEvent;
import org.example.state.PlayState;

/**
 * Represents an item that exists in the game world, waiting to be picked up.
 */
public class WorldItem implements Interactable {
    /** The underlying Item data. */
    private Item item;
    /** The world X coordinate of the item in pixels. */
    private double x;
    /** The world Y coordinate of the item in pixels. */
    private double y;
    /** The display size of the item icon. */
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
     * Renders the item in the world using its sprite if available.
     * 
     * @param gc The GraphicsContext to draw on.
     * @param camX Camera X offset.
     * @param camY Camera Y offset.
     */
    public void render(GraphicsContext gc, double camX, double camY) {
        String spriteId = item.getSpriteId();
        javafx.scene.image.Image sprite = (spriteId != null) ? org.example.AssetRegistry.getSprite(spriteId, 0) : null;

        if (sprite != null) {
            gc.drawImage(sprite, x - camX, y - camY, size, size);
        } else {
            // Fallback: Draw a small crate-like icon with an indicator color
            gc.setFill(Color.web("#d4af37")); // Golden border
            gc.fillRect(x - camX - 2, y - camY - 2, size + 4, size + 4);
            
            gc.setFill(Color.ORANGE);
            gc.fillRect(x - camX, y - camY, size, size);
        }
    }

    @Override
    public void onInteract(PlayState state) {
        if (state.getPlayer().getInventory().addItem(this.item)) {
            state.getItemsOnGround().remove(this);
            System.out.println("[Item] Picked up: " + item.getName());
            
            // Trigger Pickup Event
            state.getEventManager().triggerEvent(GameEvent.ITEM_PICKUP, item.getId(), 1, state);
        } else {
            System.out.println("[Inventory] Full! Cannot pick up " + item.getName());
        }
    }

    @Override
    public String getPrompt() {
        return "[E] Pick up " + item.getName();
    }

    @Override
    public double getInteractionRange() {
        return 150.0;
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

    /** @return The underlying Item object. */
    public Item getItem() { return item; }
    /** @return Current world X coordinate of the item. */
    public double getX() { return x; }
    /** @return Current world Y coordinate of the item. */
    public double getY() { return y; }
}
