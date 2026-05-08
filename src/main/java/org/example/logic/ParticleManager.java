package org.example.logic;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages transient visual effects.
 */
public class ParticleManager {
    
    private static class Particle {
        double x, y, vx, vy, size, life, maxLife;
        Color color;

        Particle(double x, double y, double vx, double vy, double size, double life, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.life = life;
            this.maxLife = life;
            this.color = color;
        }
        
        void update(double dt) {
            this.x += this.vx * dt * 60.0;
            this.y += this.vy * dt * 60.0;
            this.life -= dt;
        }
        
        void render(GraphicsContext gc, double camX, double camY) {
            double alpha = Math.max(0, life / maxLife);
            gc.setGlobalAlpha(alpha);
            gc.setFill(color);
            gc.fillOval(x - camX - size/2, y - camY - size/2, size, size);
            gc.setGlobalAlpha(1.0);
        }
    }

    private List<Particle> particles = new ArrayList<>();

    /**
     * Updates all active particles and removes expired ones.
     * @param dt Delta time.
     */
    public void update(double dt) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(dt);
            if (p.life <= 0) {
                particles.remove(i);
            }
        }
    }

    /**
     * Renders all active particles.
     * @param gc Graphics context.
     * @param camX Camera X.
     * @param camY Camera Y.
     */
    public void render(GraphicsContext gc, double camX, double camY) {
        for (Particle p : particles) {
            p.render(gc, camX, camY);
        }
    }
    
    /**
     * Spawns yellow sparks at the location.
     * @param x World X.
     * @param y World Y.
     */
    public void spawnHitSpark(double x, double y) {
        for (int i = 0; i < 5; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = Math.random() * 3 + 1;
            particles.add(new Particle(x, y, Math.cos(angle)*speed, Math.sin(angle)*speed, 4, 0.3, Color.YELLOW));
        }
    }

    /**
     * Spawns cyan particles at the location.
     * @param x World X.
     * @param y World Y.
     */
    public void spawnQiBurst(double x, double y) {
        for (int i = 0; i < 15; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = Math.random() * 5 + 2;
            particles.add(new Particle(x, y, Math.cos(angle)*speed, Math.sin(angle)*speed, 6, 0.6, Color.CYAN));
        }
    }

    /**
     * Spawns intense golden particles for breakthroughs.
     * @param x World X.
     * @param y World Y.
     */
    public void spawnBreakthroughEffect(double x, double y) {
        for (int i = 0; i < 25; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = Math.random() * 6 + 1;
            particles.add(new Particle(x, y, Math.cos(angle)*speed, Math.sin(angle)*speed, 5, 1.2, Color.GOLD));
        }
    }
}
