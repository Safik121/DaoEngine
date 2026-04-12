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

    public BaseEntity(double x, double y, double size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    // Abstract render method ensuring all entities can be drawn
    public abstract void render(GraphicsContext gc, double cameraX, double cameraY);

    // Getters and Setters
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double getSize() { return size; }
    public void setSize(double size) { this.size = size; }
}
