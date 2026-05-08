package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import org.example.AssetRegistry;
import org.example.ConfigManager;
import org.example.Input;
import org.example.logic.SoundManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The state representing the main menu of the game.
 * Supports interactive buttons, skinnable UI, and configurable game title.
 */
public class MenuState implements GameState {

    private String gameTitle = "DaoEngine";
    private final List<MenuButton> buttons = new ArrayList<>();
    private boolean startGameRequested = false;
    private boolean loadRequested = false;
    private boolean lexiconRequested = false;

    // UI Styles
    private static final Color GOLD = Color.web("#D4AF37");
    private static final Color DARK_INK = Color.web("#1A1A1A");

    public MenuState() {
        loadConfig();
        initButtons();
    }

    /**
     * Loads the game title from the global configuration file.
     */
    private void loadConfig() {
        try (InputStream is = getClass().getResourceAsStream("/game_config.json")) {
            if (is != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(is);
                if (root.has("gameTitle")) {
                    gameTitle = root.get("gameTitle").asText();
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load game_config.json, using default title.");
        }
    }

    /**
     * Creates and positions all interactive menu buttons.
     */
    private void initButtons() {
        double startY = 350;
        double spacing = 60;
        double btnWidth = 300;
        double btnHeight = 45;
        int width = ConfigManager.getInstance().getConfig().engine.width;
        double centerX = width / 2.0 - btnWidth / 2.0;

        buttons.add(new MenuButton("PLAY", centerX, startY, btnWidth, btnHeight, () -> {
            SoundManager.playSound("click");
            startGameRequested = true;
        }));
        buttons.add(new MenuButton("LOAD", centerX, startY + spacing, btnWidth, btnHeight, () -> {
            SoundManager.playSound("click");
            loadRequested = true;
        }));
        buttons.add(new MenuButton("BOOK OF KNOWLEDGE", centerX, startY + spacing * 2, btnWidth, btnHeight, () -> {
            SoundManager.playSound("click");
            lexiconRequested = true;
        }));
        buttons.add(new MenuButton("EXIT", centerX, startY + spacing * 3, btnWidth, btnHeight, () -> {
            SoundManager.playSound("click");
            System.exit(0);
        }));
    }

    @Override
    public void update(double deltaTime) {
        double mx = Input.getMouseX();
        double my = Input.getMouseY();
        boolean clicked = Input.isLmbPressed();

        for (MenuButton btn : buttons) {
            btn.update(mx, my, clicked);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        drawBackground(gc);
        drawTitle(gc);

        for (MenuButton btn : buttons) {
            btn.render(gc);
        }
    }

    /**
     * Renders the decorative menu background.
     */
    private void drawBackground(GraphicsContext gc) {
        int width = ConfigManager.getInstance().getConfig().engine.width;
        int height = ConfigManager.getInstance().getConfig().engine.height;
        Image bg = AssetRegistry.getSprite("ui_menu_bg", 0);
        if (bg != null) {
            gc.drawImage(bg, 0, 0, width, height);
        } else {
            // Procedural "Silk & Ink" Gradient
            LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, null,
                    new Stop(0, DARK_INK),
                    new Stop(1, Color.BLACK));
            gc.setFill(grad);
            gc.fillRect(0, 0, width, height);

            // Subtle "Qi" glow effect in center
            gc.setGlobalAlpha(0.1);
            gc.setFill(GOLD);
            gc.fillOval(width / 2.0 - 300, height / 2.0 - 300, 600, 600);
            gc.setGlobalAlpha(1.0);
        }
    }

    /**
     * Renders the game logo and title text.
     */
    private void drawTitle(GraphicsContext gc) {
        int width = ConfigManager.getInstance().getConfig().engine.width;
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

        // Shadow
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 62));
        gc.fillText(gameTitle, width / 2.0, 202);

        // Main Title
        gc.setFill(GOLD);
        gc.fillText(gameTitle, width / 2.0, 200);

        gc.setFont(Font.font("Serif", FontWeight.LIGHT, 20));
        gc.setFill(Color.LIGHTGRAY);
        gc.fillText("Path to Immortality", width / 2.0, 240);

        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT); // Reset
    }

    /** @return true if the user clicked PLAY. */
    public boolean isStartGameRequested() {
        return startGameRequested;
    }

    /** @param val State of play request. */
    public void setStartGameRequested(boolean val) {
        this.startGameRequested = val;
    }

    /** @return true if the user clicked LOAD. */
    public boolean isLoadRequested() {
        return loadRequested;
    }

    /** @param val State of load request. */
    public void setLoadRequested(boolean val) {
        this.loadRequested = val;
    }

    /** @return true if the user clicked LEXICON. */
    public boolean isLexiconRequested() {
        return lexiconRequested;
    }

    /** @param val State of lexicon request. */
    public void setLexiconRequested(boolean val) {
        this.lexiconRequested = val;
    }

    /**
     * Internal class to handle individual menu buttons.
     */
    private static class MenuButton {
        String label;
        double x, y, w, h;
        Runnable action;
        boolean hovered = false;
        boolean wasClicked = false;

        MenuButton(String label, double x, double y, double w, double h, Runnable action) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.action = action;
        }

        void update(double mx, double my, boolean lmb) {
            hovered = (mx >= x && mx <= x + w && my >= y && my <= y + h);

            if (hovered && lmb) {
                wasClicked = true;
            } else if (!lmb && wasClicked) {
                // Execute on release while hovered
                if (hovered)
                    action.run();
                wasClicked = false;
            }
        }

        void render(GraphicsContext gc) {
            Image normal = AssetRegistry.getSprite("ui_button_normal", 0);
            Image hover = AssetRegistry.getSprite("ui_button_hover", 0);

            if (hovered && hover != null) {
                gc.drawImage(hover, x, y, w, h);
            } else if (normal != null) {
                gc.drawImage(normal, x, y, w, h);
            } else {
                // Procedural Fallback
                gc.setStroke(GOLD);
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
            }

            // Label
            gc.setFill(hovered ? Color.WHITE : GOLD);
            gc.setFont(Font.font("Serif", FontWeight.BOLD, 18));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.fillText(label, x + w / 2, y + h / 2 + 6);
            gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
        }
    }
}