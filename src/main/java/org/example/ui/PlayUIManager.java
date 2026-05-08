package org.example.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.example.AssetRegistry;
import org.example.Input;
import org.example.SaveManager;
import org.example.entity.Enemy;
import org.example.entity.Player;
import org.example.item.Item;
import org.example.item.WorldItem;
import org.example.state.PlayState;
import org.example.ConfigManager;
import org.example.GameConfig;
import org.example.logic.CultivationRank;
import org.example.logic.Skill;
import org.example.logic.DialogueChoice;

/**
 * Dedicated manager for rendering all gameplay UI overlays.
 * Separates rendering (View) from game logic (Controller/Model).
 */
public class PlayUIManager {
    private static final Color GOLD = Color.web("#D4AF37");
    private static final Color DARK_INK = Color.web("#1A1A1A");

    public void render(GraphicsContext gc, PlayState state) {
        renderHUD(gc, state);
        renderMinimap(gc, state);

        if (state.isShowingFullMap())
            renderFullMap(gc, state);
        if (state.isInventoryOpen())
            drawInventory(gc, state);
        if (state.isQuestLogOpen())
            renderQuestLog(gc, state);
        if (state.isCultivationMenuOpen())
            renderCultivationMenu(gc, state);
        if (state.getDialogManager().isActive())
            renderDialogue(gc, state);

        if (state.isPaused())
            renderPauseMenu(gc, state);

        renderNotifications(gc, state);

        if (state.getCurrentMode() == PlayState.PlayMode.VICTORY)
            renderVictoryScreen(gc, state);
        if (state.getCurrentMode() == PlayState.PlayMode.GAMEOVER)
            renderGameOverScreen(gc, state);

        renderDraggedItem(gc, state);
    }

    private void renderHUD(GraphicsContext gc, PlayState state) {
        Player player = state.getPlayer();
        GameConfig.UIConfig ui = ConfigManager.getInstance().getConfig().ui;
        double w = state.getScreenWidth();

        // HP Bar
        gc.setFill(Color.web(ui.panelBgColor, ui.panelOpacity));
        gc.fillRect(20, 20, ui.barWidth, ui.barHeight);
        gc.setFill(Color.web(ui.hpColor));
        gc.fillRect(20, 20, (player.getHp() / player.getMaxHp()) * ui.barWidth, ui.barHeight);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(20, 20, ui.barWidth, ui.barHeight);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText((int) player.getHp() + " / " + (int) player.getMaxHp(), 20 + ui.barWidth + 10, 35);

        // Qi Bar
        double qiW = ui.barWidth / 2.0;
        double qiH = ui.barHeight * 0.75;
        gc.setFill(Color.web(ui.panelBgColor, ui.panelOpacity));
        gc.fillRect(20, 20 + ui.barHeight + 5, qiW, qiH);
        gc.setFill(Color.web(ui.qiColor));
        gc.fillRect(20, 20 + ui.barHeight + 5, (player.getQi() / player.getMaxQi()) * qiW, qiH);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(20, 20 + ui.barHeight + 5, qiW, qiH);

        gc.setFill(Color.web(ui.qiColor));
        gc.fillText((int) player.getQi() + " / " + (int) player.getMaxQi(), 30 + qiW,
                20 + ui.barHeight + 5 + (qiH * 0.8));

        // Weapon Cooldown Bar
        double attackRatio = player.getAttackCooldownRatio();
        if (attackRatio > 0) {
            double barY = 20 + ui.barHeight + 5 + qiH + 5;
            gc.setFill(Color.rgb(40, 40, 45, 0.7));
            gc.fillRect(20, barY, qiW, 4);
            gc.setFill(Color.ORANGERED);
            gc.fillRect(20, barY, (1.0 - attackRatio) * qiW, 4);
        }

        // Tribulation / Time
        if (state.isInTribulation()) {
            int remaining = state.countLivingTribulationEnemies();
            gc.setFill(remaining == 0 ? Color.GOLD : Color.web(ui.activeSlotHighlight));
            gc.fillText("Enemies Left: " + remaining, w - 150, 35);

            gc.setFill(Color.RED);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
            gc.fillText("TRIBULATION!", 20, 85);
        } else {
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            gc.fillText("Time: " + (int) state.getCurrentTime() + "s", 20, 85);
        }

        drawQuests(gc, state);
        drawHotbar(gc, state);
        drawActiveSkill(gc, state);
        drawInteractionPrompt(gc, state);
    }

    private void drawInteractionPrompt(GraphicsContext gc, PlayState state) {
        org.example.logic.Interactable best = state.getNearestInteractable();
        if (best == null || state.getDialogManager().isActive() || state.isInventoryOpen()) return;

        double w = state.getScreenWidth();
        double h = state.getScreenHeight();
        String prompt = best.getPrompt();

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        javafx.scene.text.Text text = new javafx.scene.text.Text(prompt);
        text.setFont(gc.getFont());
        double textWidth = text.getLayoutBounds().getWidth();

        double px = (w - textWidth) / 2.0;
        double py = h - 160;

        // Draw background for prompt
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(px - 10, py - 20, textWidth + 20, 30, 10, 10);

        gc.setFill(Color.GOLD);
        gc.fillText(prompt, px, py);
    }
    
    private void drawQuests(GraphicsContext gc, PlayState state) {
        java.util.List<org.example.logic.Quest> activeQuests = state.getQuestManager().getActiveQuests();
        if (activeQuests.isEmpty()) return;

        double startX = 20;
        double startY = 120; // Below Time/Tribulation

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.GOLD);
        gc.fillText("Active Quests:", startX, startY);
        
        startY += 20;
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        gc.setFill(Color.WHITE);
        
        for (org.example.logic.Quest q : activeQuests) {
            String text = "- " + q.getName() + " (" + q.getCurrentAmount() + "/" + q.getRequiredAmount() + ")";
            gc.fillText(text, startX, startY);
            startY += 15;
            
            gc.setFill(Color.LIGHTGRAY);
            gc.fillText("  " + q.getDescription(), startX, startY);
            startY += 20;
            gc.setFill(Color.WHITE);
        }
    }

    private void renderMinimap(GraphicsContext gc, PlayState state) {
        GameConfig.UIConfig ui = ConfigManager.getInstance().getConfig().ui;
        double w = state.getScreenWidth();
        double mapSize = 150, padding = 20, x = w - mapSize - padding, y = padding;

        gc.setFill(Color.web(ui.panelBgColor, ui.panelOpacity));
        gc.fillRect(x - 2, y - 2, mapSize + 4, mapSize + 4);
        gc.setStroke(Color.web(ui.activeSlotHighlight));
        gc.setLineWidth(2);
        gc.strokeRect(x - 2, y - 2, mapSize + 4, mapSize + 4);

        if (state.getMapCache() != null) {
            gc.drawImage(state.getMapCache(), x, y, mapSize, mapSize);
            double scale = mapSize / state.getCurrentLevel().width;
            int ts = state.getCurrentLevel().tileSize;

            gc.setFill(Color.GOLD);
            for (WorldItem wi : state.getItemsOnGround()) {
                double ix = x + (wi.getX() / ts) * scale;
                double iy = y + (wi.getY() / ts) * scale;
                gc.fillOval(ix - 1.25, iy - 1.25, 2.5, 2.5);
            }

            gc.setFill(Color.RED);
            for (Enemy e : state.getEnemies()) {
                double ex = x + (e.getX() / ts) * scale;
                double ey = y + (e.getY() / ts) * scale;
                gc.fillOval(ex - 1.5, ey - 1.5, 3, 3);
            }

            gc.setFill(Color.AQUAMARINE);
            for (org.example.entity.InteractableEntity ie : state.getCurrentLevel().interactables) {
                double ix = x + (ie.getX() / ts) * scale;
                double iy = y + (ie.getY() / ts) * scale;
                gc.fillOval(ix - 1.5, iy - 1.5, 3, 3);
            }

            if (state.getCurrentLevel().gate != null) {
                gc.setFill(Color.CYAN);
                double gx = x + (state.getCurrentLevel().gate.getX() / ts) * scale;
                double gy = y + (state.getCurrentLevel().gate.getY() / ts) * scale;
                gc.fillOval(gx - 2.5, gy - 2.5, 5, 5);
            }

            gc.setFill(Color.WHITE);
            double px = x + (state.getPlayer().getX() / ts) * scale;
            double py = y + (state.getPlayer().getY() / ts) * scale;
            gc.fillOval(px - 2, py - 2, 4, 4);
        }
    }

    private void drawHotbar(GraphicsContext gc, PlayState state) {
        GameConfig.UIConfig ui = ConfigManager.getInstance().getConfig().ui;
        double w = state.getScreenWidth();
        double h = state.getScreenHeight();
        double slotSize = 60, padding = 10, totalWidth = 5 * slotSize + 4 * padding;
        double startX = (w - totalWidth) / 2.0, startY = h - 85;

        // Adaptive Alpha
        double drawAlpha = ui.panelOpacity;
        double screenY = (state.getPlayer().getY() + 6) - state.getCameraY();
        if (screenY > h - 160)
            drawAlpha = ui.panelOpacity * 0.5;

        gc.setGlobalAlpha(drawAlpha);
        gc.setFill(Color.web(ui.panelBgColor));
        gc.fillRoundRect(startX - 15, startY - 15, totalWidth + 30, slotSize + 30, 15, 15);
        gc.setGlobalAlpha(1.0);

        for (int i = 0; i < 5; i++) {
            double sx = startX + i * (slotSize + padding);
            boolean isActive = (i == state.getPlayer().getActiveHotbarSlot());
            gc.setFill(isActive ? Color.web(ui.activeSlotHighlight).darker() : Color.web(ui.panelBgColor).brighter());
            gc.fillRect(sx, startY, slotSize, slotSize);
            gc.setStroke(isActive ? Color.web(ui.activeSlotHighlight) : Color.WHITE);
            gc.setLineWidth(isActive ? 3 : 1);
            gc.strokeRect(sx, startY, slotSize, slotSize);

            Item item = state.getPlayer().getInventory().getItemInHotbar(i);
            if (item != null)
                drawItemIcon(gc, sx, startY, slotSize, item, Color.SKYBLUE);

            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font("Arial", 12));
            gc.fillText(String.valueOf(i + 1), sx + 4, startY + 16);
        }
    }

    private void drawActiveSkill(GraphicsContext gc, PlayState state) {
        Player player = state.getPlayer();
        org.example.logic.Skill skill = player.getActiveSkill();
        GameConfig.UIConfig ui = ConfigManager.getInstance().getConfig().ui;

        double w = state.getScreenWidth();
        double h = state.getScreenHeight();
        double slotSize = 80;
        double padding = 20;
        double x = (w + (5 * 60 + 4 * 10)) / 2.0 + 30; // To the right of hotbar
        double y = h - 95;

        // Skill Border/Background
        gc.setGlobalAlpha(0.85);
        gc.setFill(DARK_INK);
        gc.fillRoundRect(x, y, slotSize, slotSize, 15, 15);
        gc.setStroke(GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, slotSize, slotSize, 15, 15);

        if (skill != null) {
            // Skill Icon (placeholder or text for now)
            gc.setFill(Color.web("#AEEEEE"));
            gc.setFont(Font.font("Serif", FontWeight.BOLD, 12));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText(skill.getName().toUpperCase(), x + slotSize / 2, y + slotSize / 2);

            // Qi Cost
            gc.setFill(GOLD);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.fillText("Qi: " + (int) skill.getQiCost(), x + slotSize / 2, y + slotSize - 10);

            // Cooldown Overlay (Sweep)
            double ratio = player.getSkillCooldownRatio();
            if (ratio > 0) {
                gc.setFill(Color.rgb(0, 0, 0, 0.7));
                gc.fillArc(x + 5, y + 5, slotSize - 10, slotSize - 10, 90, ratio * 360, javafx.scene.shape.ArcType.ROUND);
            }
        } else {
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("Arial", FontPosture.ITALIC, 10));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText("Empty", x + slotSize / 2, y + slotSize / 2);
        }

        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.setGlobalAlpha(1.0);

        // Label
        gc.setFill(Color.LIGHTGRAY);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("ACTIVE TECHNIQUE [RMB]", x, y - 10);
    }

    private void drawInventory(GraphicsContext gc, PlayState state) {
        double w = state.getScreenWidth();
        double h = state.getScreenHeight();

        gc.setFill(Color.color(0, 0, 0, 0.45));
        gc.fillRect(0, 0, w, h);

        double panelW = 800, panelH = 500;
        double panelX = (w - panelW) / 2, panelY = (h - panelH) / 2;

        gc.setFill(Color.rgb(25, 25, 30));
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.fillRect(panelX, panelY, panelW, panelH);
        gc.strokeRect(panelX, panelY, panelW, panelH);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        gc.fillText("INVENTORY", panelX + 40, panelY + 50);
        gc.fillText("CRAFTING", panelX + panelW - 300, panelY + 50);

        gc.setStroke(Color.rgb(100, 100, 100));
        gc.setLineWidth(1);
        gc.strokeLine(panelX + 460, panelY + 30, panelX + 460, panelY + panelH - 30);

        gc.setFill(Color.GRAY);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, FontPosture.ITALIC, 14));
        gc.fillText("Right-click consumables to use immediately.", panelX + 40, panelY + panelH - 20);

        double slotSize = 70, padding = 12;
        double startX = panelX + 40, startY = panelY + 80;
        double scrollY = state.getInventoryScrollY();

        // Clipping area for the inventory grid to prevent overlapping
        gc.save();
        gc.beginPath();
        gc.rect(startX - 5, startY - 5, (slotSize + padding) * 5 + 10, 350); // Visible area for 4-5 rows
        gc.clip();

        for (int i = 0; i < 30; i++) {
            double sx = startX + (i % 5) * (slotSize + padding);
            double sy = startY + (i / 5) * (slotSize + padding) - scrollY;
            drawSlot(gc, sx, sy, slotSize, state.getPlayer().getInventory().getItemInMain(i), Color.web("#444444"));
        }
        gc.restore();

        // Trash Icon
        double trashX = panelX + panelW - 100;
        double trashY = panelY + panelH - 80;
        gc.setFill(Color.rgb(60, 20, 20));
        gc.setStroke(Color.RED);
        gc.fillRoundRect(trashX, trashY, 60, 60, 10, 10);
        gc.strokeRoundRect(trashX, trashY, 60, 60, 10, 10);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.fillText("TRASH", trashX + 10, trashY + 35);

        double cX = panelX + 530, cY1 = panelY + 120, cY2 = panelY + 320, resX = panelX + 680, resY = panelY + 220;
        drawSlot(gc, cX, cY1, slotSize, state.getPlayer().getInventory().getCraftingInputs()[0], Color.web("#d4af37"));
        drawSlot(gc, cX, cY2, slotSize, state.getPlayer().getInventory().getCraftingInputs()[1], Color.web("#d4af37"));
        drawSlot(gc, resX, resY, slotSize + 15, state.getPlayer().getInventory().getCraftingResult(),
                Color.web("#2ecc71"));

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        gc.fillText("+", cX + 22, cY1 + slotSize + 55);
        gc.fillText("=", cX + slotSize + 30, resY + 55);
    }

    private void renderQuestLog(GraphicsContext gc, PlayState state) {
        double w = state.getScreenWidth();
        double h = state.getScreenHeight();

        // Background dim
        gc.setFill(Color.color(0, 0, 0, 0.6));
        gc.fillRect(0, 0, w, h);

        double panelW = 750, panelH = 550;
        double panelX = (w - panelW) / 2, panelY = (h - panelH) / 2;

        // Glass panel
        gc.setFill(Color.rgb(20, 20, 25, 0.9));
        gc.setStroke(GOLD);
        gc.setLineWidth(3);
        gc.fillRoundRect(panelX, panelY, panelW, panelH, 20, 20);
        gc.strokeRoundRect(panelX, panelY, panelW, panelH, 20, 20);

        // Header
        gc.setFill(GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 36));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText("JOURNAL OF ANCIENT PATHS", w / 2.0, panelY + 60);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);

        double startX = panelX + 50;
        double startY = panelY + 110;

        java.util.List<org.example.logic.Quest> activeQuests = state.getQuestManager().getActiveQuests();
        if (activeQuests.isEmpty()) {
            gc.setFill(Color.GRAY);
            gc.setFont(Font.font("Arial", FontPosture.ITALIC, 20));
            gc.fillText("Your journal is empty. Seek out those in need.", startX, startY + 50);
        } else {
            for (org.example.logic.Quest q : activeQuests) {
                // Quest Banner
                gc.setFill(Color.rgb(40, 40, 50));
                gc.fillRoundRect(startX - 10, startY - 10, panelW - 100, 100, 10, 10);
                gc.setStroke(Color.rgb(80, 80, 100));
                gc.setLineWidth(1);
                gc.strokeRoundRect(startX - 10, startY - 10, panelW - 100, 100, 10, 10);

                // Quest Title
                gc.setFill(Color.web("#AEEEEE"));
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                gc.fillText(q.getName(), startX, startY + 20);

                // Description
                gc.setFill(Color.LIGHTGRAY);
                gc.setFont(Font.font("Arial", 14));
                gc.fillText(q.getDescription(), startX, startY + 45, panelW - 150);

                // Progress Bar
                double barW = panelW - 300;
                double barH = 12;
                double barX = startX;
                double barY = startY + 65;
                
                gc.setFill(Color.BLACK);
                gc.fillRoundRect(barX, barY, barW, barH, 5, 5);
                gc.setFill(GOLD);
                gc.fillRoundRect(barX, barY, barW * q.getProgressPercentage(), barH, 5, 5);
                gc.setStroke(Color.WHITE);
                gc.setLineWidth(0.5);
                gc.strokeRoundRect(barX, barY, barW, barH, 5, 5);

                // Progress Text
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                gc.fillText(q.getCurrentAmount() + " / " + q.getRequiredAmount(), barX + barW + 15, barY + 10);

                startY += 120;
            }
        }

        // Footer info
        gc.setFill(Color.DARKGRAY);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText("Press [Q] or [ESC] to return to your journey.", panelX + 50, panelY + panelH - 25);
    }

    private void drawSlot(GraphicsContext gc, double x, double y, double size, Item item, Color borderColor) {
        gc.setFill(Color.rgb(40, 40, 45));
        gc.fillRect(x, y, size, size);
        gc.setStroke(borderColor);
        gc.setLineWidth((borderColor == Color.GOLD || borderColor.equals(Color.web("#2ecc71"))) ? 3 : 1);
        gc.strokeRect(x, y, size, size);
        if (item != null)
            drawItemIcon(gc, x, y, size, item, Color.ORANGE);
    }

    private void drawItemIcon(GraphicsContext gc, double x, double y, double size, Item item, Color fallbackColor) {
        // --- 1. Background / Slot Frame ---
        gc.setFill(Color.web("#1a1a1a", 0.8));
        gc.fillRoundRect(x, y, size, size, 8, 8);
        
        // --- 2. Render Texture with Rounded Corners ---
        if (item != null && item.getSpriteId() != null) {
            Image sprite = AssetRegistry.getSprite(item.getSpriteId(), item.getSpriteFrame());
            if (sprite != null) {
                gc.save();
                
                // Define rounded clipping path
                double pad = 2; // Subtract a small padding for the border
                double rx = x + pad;
                double ry = y + pad;
                double rs = size - (pad * 2);
                double rounded = 6;
                
                gc.beginPath();
                gc.moveTo(rx + rounded, ry);
                gc.lineTo(rx + rs - rounded, ry);
                gc.arcTo(rx + rs, ry, rx + rs, ry + rounded, rounded);
                gc.lineTo(rx + rs, ry + rs - rounded);
                gc.arcTo(rx + rs, ry + rs, rx + rs - rounded, ry + rs, rounded);
                gc.lineTo(rx + rounded, ry + rs);
                gc.arcTo(rx, ry + rs, rx, ry + rs - rounded, rounded);
                gc.lineTo(rx, ry + rounded);
                gc.arcTo(rx, ry, rx + rounded, ry, rounded);
                gc.closePath();
                gc.clip();

                // Draw the actual high-quality icon
                gc.drawImage(sprite, rx, ry, rs, rs);
                
                gc.restore();
                
                // --- 3. Premium Border Overlay ---
                gc.setStroke(Color.web("#d4af37", 0.5)); // Subtle gold
                gc.setLineWidth(1.5);
                gc.strokeRoundRect(rx, ry, rs, rs, rounded, rounded);
                
                return;
            }
        }
        
        // Fallback to stylized colored rectangle if no icon exists
        gc.setFill(fallbackColor);
        gc.setGlobalAlpha(0.6);
        gc.fillRoundRect(x + 5, y + 5, size - 10, size - 10, 5, 5);
        gc.setGlobalAlpha(1.0);
        gc.setStroke(fallbackColor.brighter());
        gc.strokeRoundRect(x + 5, y + 5, size - 10, size - 10, 5, 5);
    }

    private void renderFullMap(GraphicsContext gc, PlayState state) {
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();
        double mapSize = 600;
        double x = (w - mapSize) / 2.0;
        double y = (h - mapSize) / 2.0;

        gc.setFill(new Color(0, 0, 0, 0.45));
        gc.fillRect(0, 0, w, h);

        gc.setFill(new Color(0.1, 0.1, 0.1, 1.0));
        gc.fillRect(x - 10, y - 40, mapSize + 20, mapSize + 50);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(3);
        gc.strokeRect(x - 10, y - 40, mapSize + 20, mapSize + 50);

        gc.setFill(Color.GOLD);
        gc.setFont(Font.font("Arial", 24));
        gc.fillText("WORLD MAP ('M' to close)", x + 10, y - 10);

        if (state.getMapCache() != null) {
            gc.drawImage(state.getMapCache(), x, y, mapSize, mapSize);

            double scale = mapSize / state.getCurrentLevel().width;
            int ts = state.getCurrentLevel().tileSize;

            gc.setFill(Color.GOLD);
            for (WorldItem wi : state.getItemsOnGround()) {
                double ix = x + (wi.getX() / ts) * scale;
                double iy = y + (wi.getY() / ts) * scale;
                gc.fillOval(ix - 2, iy - 2, 4, 4);
            }

            gc.setFill(Color.RED);
            for (Enemy e : state.getEnemies()) {
                double ex = x + (e.getX() / ts) * scale;
                double ey = y + (e.getY() / ts) * scale;
                gc.fillOval(ex - 2, ey - 2, 4, 4);
            }

            gc.setFill(Color.AQUAMARINE);
            for (org.example.entity.InteractableEntity ie : state.getCurrentLevel().interactables) {
                double ix = x + (ie.getX() / ts) * scale;
                double iy = y + (ie.getY() / ts) * scale;
                gc.fillOval(ix - 3, iy - 3, 6, 6);
            }

            if (state.getCurrentLevel().gate != null) {
                gc.setFill(Color.CYAN);
                double gx = x + (state.getCurrentLevel().gate.getX() / ts) * scale;
                double gy = y + (state.getCurrentLevel().gate.getY() / ts) * scale;
                gc.fillOval(gx - 5, gy - 5, 10, 10);
            }

            gc.setFill(Color.WHITE);
            double px = x + (state.getPlayer().getX() / ts) * scale;
            double py = y + (state.getPlayer().getY() / ts) * scale;
            gc.fillOval(px - 4, py - 4, 8, 8);
        }
    }

    private void renderDialogue(GraphicsContext gc, PlayState state) {
        double width = 800, height = 180; // slightly taller to fit choices
        double x = (state.getScreenWidth() - width) / 2.0;
        double y = state.getScreenHeight() - height - 50;

        DialogManager dm = state.getDialogManager();
        if (dm.getCurrentNode() == null) return;

        double drawAlpha = 0.85;
        double screenY = (state.getPlayer().getY() + 6) - state.getCameraY();
        if (screenY > y - 20)
            drawAlpha = 0.4;

        gc.setGlobalAlpha(drawAlpha);
        gc.setFill(Color.BLACK);
        gc.fillRoundRect(x, y, width, height, 15, 15);
        gc.setStroke(Color.GOLD);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 15, 15);
        gc.setGlobalAlpha(1.0);

        // Name
        gc.setFill(Color.AQUAMARINE);
        gc.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        gc.fillText(dm.getActiveDialogue().getName(), x + 20, y + 35);

        // Main Text
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Inter", 16));
        
        // Split text into lines if too long (Simple wrapping)
        String fullText = dm.getCurrentNode().getText();
        gc.fillText(fullText, x + 20, y + 70, width - 40);

        // Render Choices
        java.util.List<org.example.logic.DialogueChoice> choices = dm.getCurrentNode().getChoices();
        if (!choices.isEmpty()) {
            double choiceY = y + 100;
            double mx = org.example.Input.getMouseX();
            double my = org.example.Input.getMouseY();
            
            gc.setFont(Font.font("Inter", FontWeight.BOLD, 14));
            for (int i = 0; i < choices.size(); i++) {
                // Hover Check (Synchronized with PlayState's detection)
                boolean hover = (mx >= x + 10 && mx <= x + width - 10 && my >= choiceY - 18 && my <= choiceY + 12);
                
                if (hover) {
                    gc.setFill(Color.rgb(255, 255, 255, 0.1));
                    gc.fillRoundRect(x + 15, choiceY - 18, width - 30, 25, 5, 5);
                    gc.setFill(Color.GOLD);
                } else {
                    gc.setFill(Color.web("#D4AF37", 0.8));
                }
                
                gc.fillText((i + 1) + ") ", x + 30, choiceY);
                gc.setFill(hover ? Color.WHITE : Color.LIGHTGRAY);
                gc.fillText(choices.get(i).getText(), x + 50, choiceY);
                choiceY += 25;
            }
        } else {
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font("Inter", 12));
            gc.fillText("[E] Continue...", x + width - 100, y + height - 15);
        }
    }

    public void renderPauseMenu(GraphicsContext gc, PlayState state) {
        double w = state.getScreenWidth(), h = state.getScreenHeight();
        gc.setFill(Color.rgb(0, 0, 0, 0.8));
        gc.fillRect(0, 0, w, h);

        double centerX = w / 2.0;
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

        if (state.getCurrentPauseState() == PlayState.PauseMenuState.MAIN) {
            gc.setFill(GOLD);
            gc.setFont(Font.font("Serif", FontWeight.BOLD, 48));
            gc.fillText("PAUSED", centerX, 180);

            drawMenuButton(gc, "RESUME", centerX - 150, 260, 300, 50, Color.DARKGREEN);
            drawMenuButton(gc, "BOOK OF KNOWLEDGE", centerX - 150, 320, 300, 50, Color.DARKBLUE);
            drawMenuButton(gc, "SAVE GAME", centerX - 150, 380, 300, 50, Color.rgb(60, 60, 80));
            drawMenuButton(gc, "LOAD GAME", centerX - 150, 440, 300, 50, Color.rgb(60, 60, 80));
            drawMenuButton(gc, "MAIN MENU", centerX - 150, 500, 300, 50, Color.DARKRED);
        } else {
            boolean isSave = (state.getCurrentPauseState() == PlayState.PauseMenuState.SAVE_SELECT);
            gc.setFill(GOLD);
            gc.setFont(Font.font("Serif", FontWeight.BOLD, 36));
            gc.fillText(isSave ? "SAVE SESSION" : "LOAD SESSION", centerX, 150);

            for (int i = 1; i <= 5; i++) {
                boolean exists = SaveManager.exists(i);
                Color color = exists ? Color.rgb(60, 100, 180) : Color.DARKGRAY;
                String label = "Slot " + i + (exists ? " [ ACTIVE ]" : " [ EMPTY ]");
                drawMenuButton(gc, label, centerX - 200, 210 + (i - 1) * 70, 400, 60, color);
            }
            drawMenuButton(gc, "BACK", centerX - 150, 600, 300, 60, Color.DARKRED);
        }
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    private void drawMenuButton(GraphicsContext gc, String text, double x, double y, double w, double h,
            Color baseColor) {
        double mx = Input.getMouseX(), my = Input.getMouseY();
        boolean hover = PlayState.isInside(mx, my, x, y, w, h);

        javafx.scene.image.Image normal = AssetRegistry.getSprite("ui_button_normal", 0);
        javafx.scene.image.Image hoverImg = AssetRegistry.getSprite("ui_button_hover", 0);

        if (hover && hoverImg != null) {
            gc.drawImage(hoverImg, x, y, w, h);
        } else if (normal != null) {
            gc.drawImage(normal, x, y, w, h);
        } else {
            // Procedural "Silk & Ink" style fallback
            gc.setFill(hover ? DARK_INK.deriveColor(0, 1, 1.5, 1) : DARK_INK);
            gc.fillRect(x, y, w, h);
            gc.setStroke(GOLD);
            gc.setLineWidth(hover ? 3 : 1);
            gc.strokeRect(x, y, w, h);
            
            if (hover) {
                gc.setGlobalAlpha(0.2);
                gc.setFill(GOLD);
                gc.fillRect(x, y, w, h);
                gc.setGlobalAlpha(1.0);
            }
        }

        // Label
        gc.setFill(hover ? Color.WHITE : GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 18));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(text, x + w / 2, y + h / 2 + 6);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    private void renderVictoryScreen(GraphicsContext gc, PlayState state) {
        renderOverlayMessage(gc, state, "LEVEL CLEARED!", "You survived the tribulation.", Color.GOLD);
    }

    private void renderGameOverScreen(GraphicsContext gc, PlayState state) {
        renderOverlayMessage(gc, state, "DEFEAT", "Your spirit fades away...", Color.DARKRED);
    }

    private void renderOverlayMessage(GraphicsContext gc, PlayState state, String title, String sub, Color color) {
        double w = state.getScreenWidth(), h = state.getScreenHeight();
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, w, h);

        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setFill(color);
        gc.setFont(Font.font("Inter", FontWeight.BOLD, 80));
        gc.fillText(title, w / 2.0, h / 2.0);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Inter", 24));
        gc.fillText(sub, w / 2.0, h / 2.0 + 60);

        gc.setFont(Font.font("Inter", 18));
        gc.fillText("Press 'SPACE' to Ascend to the Next Realm", w / 2.0, h / 2.0 + 120);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    public void renderCultivationMenu(GraphicsContext gc, PlayState state) {
        double w = state.getScreenWidth();
        double h = state.getScreenHeight();

        // Dark focus background
        gc.setFill(Color.rgb(10, 10, 15, 0.7));
        gc.fillRect(0, 0, w, h);

        double panelW = 600, panelH = 450;
        double x = (w - panelW) / 2, y = (h - panelH) / 2;

        // Silk Scroll / Paper Background
        gc.setFill(Color.rgb(30, 30, 40, 0.95));
        gc.setStroke(GOLD);
        gc.setLineWidth(4);
        gc.fillRoundRect(x, y, panelW, panelH, 30, 30);
        gc.strokeRoundRect(x, y, panelW, panelH, 30, 30);

        // Header
        gc.setFill(GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 36));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText("PATH OF ASCENSION", w / 2.0, y + 60);

        Player player = state.getPlayer();
        org.example.logic.CultivationRank current = player.getCultivationManager().getCurrentRank();
        org.example.logic.CultivationRank next = player.getCultivationManager().getNextRank();

        // Current Status
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        gc.setFont(Font.font("Serif", 20));
        gc.setFill(Color.AQUAMARINE);
        gc.fillText("Current Rank: " + current.getFullName(), x + 50, y + 110);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Serif", 16));
        gc.fillText("Spiritual Qi: " + (int)player.getQi() + " / " + (int)player.getMaxQi(), x + 50, y + 140);

        // Future Rank Preview
        if (next != null) {
            gc.setFill(GOLD);
            gc.setFont(Font.font("Serif", FontWeight.BOLD, 22));
            gc.fillText("NEXT BREAKTHROUGH: " + next.getFullName(), x + 50, y + 190);
            
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font("Serif", 18));
            gc.fillText("Required Qi: " + (int)next.getRequiredQiToBreakthrough(), x + 50, y + 220);
            
            gc.fillText("Anticipated Prowess Growth:", x + 50, y + 260);
            gc.setFont(Font.font("Serif", 16));
            gc.setFill(Color.web("#2ecc71"));
            gc.fillText("+ " + (int)next.getHpBonus() + " Vitality (HP)", x + 70, y + 290);
            gc.fillText("+ " + (int)next.getStrengthBonus() + " Internal Strength", x + 70, y + 315);
            gc.fillText("+ " + (int)next.getDefenseBonus() + " Fortitude", x + 70, y + 340);
        } else {
            gc.setFill(Color.AQUAMARINE);
            gc.setFont(Font.font("Serif", FontWeight.BOLD, 24));
            gc.fillText("YOU HAVE REACHED THE APEX", x + 50, y + 230);
        }

        // Breakthrough Button
        if (next != null) {
            boolean canAfford = player.getQi() >= next.getRequiredQiToBreakthrough();
            Color btnColor = canAfford ? Color.DARKGREEN : Color.rgb(80, 40, 40);
            drawMenuButton(gc, "BREAKTHROUGH", x + panelW/2 - 150, y + panelH - 80, 300, 50, btnColor);
        }
        
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    private void renderNotifications(GraphicsContext gc, PlayState state) {
        java.util.List<PlayState.Notification> notes = state.getNotifications();
        if (notes.isEmpty()) return;

        double w = state.getScreenWidth();
        double startY = 80;
        double spacing = 35;

        gc.setFont(Font.font("Serif", FontWeight.BOLD, 18));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

        for (int i = 0; i < notes.size(); i++) {
            PlayState.Notification n = notes.get(i);
            double alpha = Math.min(1.0, n.timer * 2.0); // Fade out in last 0.5s
            
            gc.setGlobalAlpha(alpha * 0.7);
            gc.setFill(Color.BLACK);
            double msgW = n.message.length() * 10 + 40;
            gc.fillRoundRect(w/2 - msgW/2, startY + i * spacing - 20, msgW, 30, 10, 10);
            
            gc.setGlobalAlpha(alpha);
            gc.setFill(GOLD);
            gc.fillText(n.message, w / 2, startY + i * spacing);
        }
        gc.setGlobalAlpha(1.0);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    private void renderDraggedItem(GraphicsContext gc, PlayState state) {
        Item item = state.getDraggedItem();
        if (item == null)
            return;

        double mx = Input.getMouseX(), my = Input.getMouseY(), s = 60;
        gc.setGlobalAlpha(0.8);
        gc.setFill(Color.ORANGE);
        gc.fillRect(mx - s / 2, my - s / 2, s, s);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(mx - s / 2, my - s / 2, s, s);
        gc.setGlobalAlpha(1.0);
    }
}
