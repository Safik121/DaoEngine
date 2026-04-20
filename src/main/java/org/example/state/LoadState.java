package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.SaveData;
import org.example.SaveManager;
import org.example.Input;
import org.example.AssetRegistry;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;

/**
 * State for selecting a save slot to load.
 */
public class LoadState implements GameState {

    private final List<LoadButton> buttons = new ArrayList<>();
    private SaveData selectedSave = null;
    private boolean returnToMenuRequested = false;

    // UI Styles
    private static final Color GOLD = Color.web("#D4AF37");
    private static final Color DARK_INK = Color.web("#1A1A1A");

    public LoadState() {
        initButtons();
    }

    private void initButtons() {
        double startY = 200;
        double spacing = 80;
        double btnWidth = 400;
        double btnHeight = 60;
        double centerX = 1024 / 2.0 - btnWidth / 2.0;

        for (int i = 1; i <= 5; i++) {
            final int slot = i;
            boolean exists = SaveManager.exists(slot);
            String label = "Slot " + slot + (exists ? " - [ RESUME ]" : " - [ EMPTY ]");
            
            buttons.add(new LoadButton(label, centerX, startY + (i - 1) * spacing, btnWidth, btnHeight, exists, () -> {
                if (exists) {
                    selectedSave = SaveManager.load(slot);
                }
            }));
        }

        buttons.add(new LoadButton("BACK TO MENU", 50, 680, 200, 40, true, () -> returnToMenuRequested = true));
    }

    @Override
    public void update(double deltaTime) {
        double mx = Input.getMouseX();
        double my = Input.getMouseY();
        boolean clicked = Input.isLmbPressed();

        for (LoadButton btn : buttons) {
            btn.update(mx, my, clicked);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        drawBackground(gc);
        drawTitle(gc);

        for (LoadButton btn : buttons) {
            btn.render(gc);
        }
    }

    private void drawBackground(GraphicsContext gc) {
        Image bg = AssetRegistry.getSprite("ui_load_bg", 0);
        if (bg != null) {
            gc.drawImage(bg, 0, 0, 1024, 768);
        } else {
            // Procedural "Silk & Ink" Gradient (Matches MenuState)
            LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, null,
                new Stop(0, DARK_INK),
                new Stop(1, Color.BLACK)
            );
            gc.setFill(grad);
            gc.fillRect(0, 0, 1024, 768);
        }
    }

    private void drawTitle(GraphicsContext gc) {
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setFill(GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 48));
        gc.fillText("Select Journey", 512, 100);
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    public SaveData getSelectedSave() {
        return selectedSave;
    }

    public boolean isReturnToMenuRequested() {
        return returnToMenuRequested;
    }

    private static class LoadButton {
        String label;
        double x, y, w, h;
        Runnable action;
        boolean hovered = false;
        boolean wasClicked = false;
        boolean enabled;

        LoadButton(String label, double x, double y, double w, double h, boolean enabled, Runnable action) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.enabled = enabled;
            this.action = action;
        }

        void update(double mx, double my, boolean lmb) {
            if (!enabled && !label.contains("BACK")) {
                hovered = false;
                return;
            }
            hovered = (mx >= x && mx <= x + w && my >= y && my <= y + h);
            
            if (hovered && lmb) {
                wasClicked = true;
            } else if (!lmb && wasClicked) {
                if (hovered) action.run();
                wasClicked = false;
            }
        }

        void render(GraphicsContext gc) {
            // Stylized Frame
            gc.setStroke(enabled ? GOLD : Color.DARKGRAY);
            gc.setLineWidth(hovered ? 3 : 1);
            gc.setFill(hovered ? Color.web("#333333") : Color.web("#222222"));
            
            gc.fillRect(x, y, w, h);
            gc.strokeRect(x, y, w, h);

            if (hovered) {
                gc.setGlobalAlpha(0.2);
                gc.setFill(GOLD);
                gc.fillRect(x, y, w, h);
                gc.setGlobalAlpha(1.0);
            }

            // Label
            gc.setFill(enabled ? (hovered ? Color.WHITE : GOLD) : Color.GRAY);
            gc.setFont(Font.font("Serif", FontWeight.BOLD, 22));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText(label, x + w / 2, y + h / 2 + 8);
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        }
    }
}
