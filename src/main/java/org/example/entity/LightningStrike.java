package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Represents a single Tribulation Lightning strike.
 * Manages its own timing, visual stages, and damage state.
 */
public class LightningStrike {
    public enum Stage {
        WARNING, STRIKE, FADE
    }

    private double x, y;
    private double timer;
    private Stage stage;
    private boolean damaged = false;

    private static final double WARNING_DURATION = 0.6;
    private static final double STRIKE_DURATION = 0.1;
    private static final double FADE_DURATION = 0.2;

    public LightningStrike(double x, double y) {
        this.x = x;
        this.y = y;
        this.timer = WARNING_DURATION;
        this.stage = Stage.WARNING;
    }

    public void update(double dt) {
        timer -= dt;
        if (timer <= 0) {
            if (stage == Stage.WARNING) {
                stage = Stage.STRIKE;
                timer = STRIKE_DURATION;
            } else if (stage == Stage.STRIKE) {
                stage = Stage.FADE;
                timer = FADE_DURATION;
            } else {
                stage = null; // Mark for removal
            }
        }
    }

    public void render(GraphicsContext gc, double cameraX, double cameraY) {
        double sx = x - cameraX;
        double sy = y - cameraY;

        if (stage == Stage.WARNING) {
            gc.setStroke(new Color(1, 1, 1, 0.3));
            gc.setLineWidth(1);
            gc.strokeOval(sx - 40, sy - 20, 80, 40);
            gc.strokeLine(sx, 0, sx, sy);
        } else if (stage == Stage.STRIKE) {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(6);
            gc.strokeLine(sx, 0, sx, sy);
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(3);
            gc.strokeLine(sx, 0, sx, sy);
        } else if (stage == Stage.FADE) {
            double alpha = timer / FADE_DURATION;
            gc.setStroke(new Color(1, 0.8, 0, alpha));
            gc.setLineWidth(3);
            gc.strokeLine(sx, 0, sx, sy);
        }
    }

    public boolean isExpired() {
        return stage == null;
    }

    public boolean isDealingDamage() {
        return stage == Stage.STRIKE && !damaged;
    }

    public void markDamaged() {
        this.damaged = true;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getRadius() {
        return 50.0;
    } // Increased damage radius
}
