package org.example.render;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.example.AssetRegistry;
import org.example.entity.Enemy;
import org.example.entity.InteractableEntity;
import org.example.entity.LightningStrike;
import org.example.entity.Projectile;
import org.example.item.WorldItem;
import org.example.state.PlayState;

/**
 * Handles all in-world rendering logic, including the map tiles,
 * entities (Player, Enemies, Items), and combat effects.
 */
public class WorldRenderer {

    public void render(GraphicsContext gc, PlayState state) {
        // 0. Clear background
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, state.getScreenWidth(), state.getScreenHeight());

        // 1. Render Map
        renderMap(gc, state);

        // 2. Render World Items
        for (WorldItem wi : state.getItemsOnGround()) {
            wi.render(gc, state.getCameraX(), state.getCameraY());
        }

        // 3. Render Enemies
        for (Enemy enemy : state.getEnemies()) {
            enemy.render(gc, state.getCameraX(), state.getCameraY());
        }

        // 4. Render Projectiles
        for (Projectile p : state.getProjectiles()) {
            p.render(gc, state.getCameraX(), state.getCameraY());
        }

        // 5. Render Lightning Strikes
        for (LightningStrike strike : state.getActiveStrikes()) {
            strike.render(gc, state.getCameraX(), state.getCameraY());
        }

        // 6. Render Gate of Realms
        if (state.getCurrentLevel().gate != null) {
            // Note: Update logic normally belongs in update(), but we preserve visual parity
            state.getCurrentLevel().gate.render(gc, state.getCameraX(), state.getCameraY());
        }

        // 7. Render Interactables (NPCs/Steles)
        for (InteractableEntity ie : state.getCurrentLevel().interactables) {
            ie.render(gc, state.getCameraX(), state.getCameraY());

            // Interaction visual prompt
            double dist = Math.sqrt(Math.pow(ie.getX() - state.getPlayer().getX(), 2) + Math.pow(ie.getY() - state.getPlayer().getY(), 2));
            if (!state.getDialogManager().isActive() && dist < 70) {
                gc.setFill(Color.GOLD);
                gc.setFont(new Font("Arial Bold", 14));
                gc.fillText("[E]", ie.getX() - state.getCameraX() + 4, ie.getY() - state.getCameraY() - 8);
            }
        }

        // 8. Render Player
        state.getPlayer().render(gc, state.getCameraX(), state.getCameraY());

        // 9. Render Lighting & Atmosphere
        renderLighting(gc, state);
    }
    
    private void renderLighting(GraphicsContext gc, PlayState state) {
        double w = state.getScreenWidth();
        double h = state.getScreenHeight();
        double camX = state.getCameraX();
        double camY = state.getCameraY();
        
        // Calculate ambient light level
        // Tribulation = dark, Normal = slightly dim
        double ambientLight = state.isInTribulation() ? 0.8 : 0.4;
        
        // Subtract ambient light based on active lightning strikes
        for (LightningStrike strike : state.getActiveStrikes()) {
            if (strike.isDealingDamage()) {
                ambientLight = 0.0; // Flash full brightness
                break;
            }
        }

        // Draw global darkness
        gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER);
        gc.setFill(Color.rgb(0, 0, 10, ambientLight));
        gc.fillRect(0, 0, w, h);

        // We can't easily punch holes with standard GraphicsContext commands without affecting the underlying layers.
        // A simple trick to simulate soft lighting around the player:
        // Draw a radial gradient centered on the player with BlendMode.ADD
        gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.ADD);
        
        double playerCX = state.getPlayer().getX() + state.getPlayer().getSize() / 2 - camX;
        double playerCY = state.getPlayer().getY() + state.getPlayer().getSize() / 2 - camY;
        
        javafx.scene.paint.RadialGradient playerLight = new javafx.scene.paint.RadialGradient(
            0, 0, playerCX, playerCY, 200, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
            new javafx.scene.paint.Stop(0, Color.rgb(255, 255, 200, 0.4)),
            new javafx.scene.paint.Stop(1, Color.TRANSPARENT)
        );
        gc.setFill(playerLight);
        gc.fillOval(playerCX - 200, playerCY - 200, 400, 400);

        // Projectile light sources
        for (Projectile p : state.getProjectiles()) {
            double px = p.getX() - camX;
            double py = p.getY() - camY;
            javafx.scene.paint.RadialGradient pLight = new javafx.scene.paint.RadialGradient(
                0, 0, px, py, 60, false, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new javafx.scene.paint.Stop(0, Color.rgb(255, 100, 0, 0.6)),
                new javafx.scene.paint.Stop(1, Color.TRANSPARENT)
            );
            gc.setFill(pLight);
            gc.fillOval(px - 60, py - 60, 120, 120);
        }

        gc.setGlobalBlendMode(javafx.scene.effect.BlendMode.SRC_OVER); // Reset
    }

    private void renderMap(GraphicsContext gc, PlayState state) {
        if (state.getCurrentLevel() == null || state.getCurrentLevel().data == null) return;
        
        int tileSize = state.getCurrentLevel().tileSize;
        double cameraX = state.getCameraX();
        double cameraY = state.getCameraY();
        int screenWidth = state.getScreenWidth();
        int screenHeight = state.getScreenHeight();

        // Frustum Culling: Only render visible tiles
        int startX = Math.max(0, (int) (cameraX / tileSize));
        int endX = Math.min(state.getCurrentLevel().width, (int) ((cameraX + screenWidth) / tileSize) + 1);
        int startY = Math.max(0, (int) (cameraY / tileSize));
        int endY = Math.min(state.getCurrentLevel().height, (int) ((cameraY + screenHeight) / tileSize) + 1);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                int tileType = state.getCurrentLevel().data.get(y).get(x);
                String spriteId = state.getCurrentLevel().biome.getSpriteId(tileType);
                int frameIndex = 0;

                if (tileType == 2) {
                    frameIndex = (int) (state.getMapAnimationTimer() / 0.5) % 2;
                } else if (tileType == 3) {
                    frameIndex = (int) (state.getMapAnimationTimer() / 0.2) % 4;
                }

                // Draw base decoration
                if (tileType == 4 || tileType == 5) {
                    int baseType = 0; 

                    if (tileType == 5) {
                        baseType = getBridgeBaseTileType(state, x, y);
                    }

                    String baseId = state.getCurrentLevel().biome.getSpriteId(baseType);
                    int bFrame = (baseType == 2) ? (int) (state.getMapAnimationTimer() / 0.5) % 2 : 0;
                    javafx.scene.image.Image base = AssetRegistry.getSprite(baseId, bFrame);
                    if (base != null) {
                        gc.drawImage(base, x * tileSize - cameraX, y * tileSize - cameraY, tileSize, tileSize);
                    }
                }

                javafx.scene.image.Image sprite = AssetRegistry.getSprite(spriteId, frameIndex);
                if (sprite != null) {
                    gc.drawImage(sprite, x * tileSize - cameraX, y * tileSize - cameraY, tileSize, tileSize);
                } else {
                    renderFallbackColor(gc, tileType, x * tileSize - cameraX, y * tileSize - cameraY, tileSize);
                }
            }
        }
    }

    private int getBridgeBaseTileType(PlayState state, int x, int y) {
        int width = state.getCurrentLevel().width;
        int height = state.getCurrentLevel().height;
        
        int bx = x, by = y;
        // Search left
        while (bx > 0 && state.getCurrentLevel().data.get(y).get(bx) == 5) bx--;
        if (state.getCurrentLevel().data.get(y).get(bx) == 2) return 2;

        // Search right
        bx = x;
        while (bx < width - 1 && state.getCurrentLevel().data.get(y).get(bx) == 5) bx++;
        if (state.getCurrentLevel().data.get(y).get(bx) == 2) return 2;

        // Search up
        bx = x; by = y;
        while (by > 0 && state.getCurrentLevel().data.get(by).get(x) == 5) by--;
        if (state.getCurrentLevel().data.get(by).get(x) == 2) return 2;

        // Search down
        by = y;
        while (by < height - 1 && state.getCurrentLevel().data.get(by).get(x) == 5) by++;
        if (state.getCurrentLevel().data.get(by).get(x) == 2) return 2;

        return 0;
    }

    private void renderFallbackColor(GraphicsContext gc, int tileType, double tx, double ty, int size) {
        if (tileType == 1) gc.setFill(Color.DARKGRAY);
        else if (tileType == 2) gc.setFill(Color.BLUE);
        else if (tileType == 3) gc.setFill(Color.MEDIUMPURPLE);
        else if (tileType == 5) gc.setFill(Color.SADDLEBROWN);
        else gc.setFill(Color.DARKGREEN);
        gc.fillRect(tx, ty, size, size);
    }
}
