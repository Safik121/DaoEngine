package org.example.entity;

import javafx.scene.canvas.GraphicsContext;

/**
 * Abstract base class for all logical entities in the game world.
 * Defines position, size, and requires rendering functionality.
 */
public abstract class BaseEntity {
    protected double x;
    protected double y;
    protected double size;

    /**
     * @param x Initial X coordinate in world space.
     * @param y Initial Y coordinate in world space.
     * @param size Nominal collision/render radius.
     */
    public BaseEntity(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    // Abstract render method ensuring all entities can be drawn
    public abstract void render(GraphicsContext gc, double cameraX, double cameraY);

    // Getters and Setters
    /** @return World X coordinate. */
    public double getX() { return x; }
    /** @param x New world X coordinate. */
    public void setX(double x) { this.x = x; }

    /** @return World Y coordinate. */
    public double getY() { return y; }
    /** @param y New world Y coordinate. */
    public void setY(double y) { this.y = y; }

    /** @return Size of the entity. */
    public double getSize() { return size; }
    /** @param size New size of the entity. */
    public void setSize(double size) { this.size = size; }
}
