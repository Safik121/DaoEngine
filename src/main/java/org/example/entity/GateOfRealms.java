package org.example.entity;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import org.example.logic.Interactable;
import org.example.state.PlayState;

/**
 * Represents the Gate of Realms, used for level transitions.
 * The player must interact with this while holding a required artifact.
 */
public class GateOfRealms implements Interactable {
    private double x, y;
    private double animationTimer = 0;

    public GateOfRealms(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void update(double dt) {
        animationTimer += dt;
    }

    public void render(GraphicsContext gc, double cameraX, double cameraY) {
        double sx = x - cameraX;
        double sy = y - cameraY;

        // Draw a glowing portal effect
        double pulse = 1.0 + Math.sin(animationTimer * 3) * 0.1;
        double radius = 40 * pulse;

        RadialGradient gradient = new RadialGradient(
            0, 0, sx + 6, sy + 6, radius, false, CycleMethod.NO_CYCLE,
            new Stop(0, Color.CYAN),
            new Stop(0.6, Color.BLUE),
            new Stop(1, Color.TRANSPARENT)
        );

        gc.setFill(gradient);
        gc.fillOval(sx + 6 - radius, sy + 6 - radius, radius * 2, radius * 2);

        // Core of the portal
        gc.setFill(Color.WHITE);
        gc.fillOval(sx + 6 - 5, sy + 6 - 5, 10, 10);
        
        // Label
        gc.setFill(Color.WHITE);
        gc.fillText("Gate of Realms", sx - 20, sy - radius - 5);
    }

    @Override
    public void onInteract(PlayState state) {
        if (state.getPlayer().getInventory().consumeItem("realm_token")) {
            System.out.println("[Gate] The Realm Token resonates! Entering the Gate of Realms...");
            state.setVictory();
        } else {
            System.out.println("[Gate] The gate remains sealed. You sense it requires a 'Realm Token'.");
        }
    }

    @Override
    public String getPrompt() {
        return "[E] Enter the Gate of Realms";
    }

    @Override
    public double getInteractionRange() {
        return 60.0;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
