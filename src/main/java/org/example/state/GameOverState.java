package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.Input;
import org.example.ConfigManager;

/**
 * State displayed when the player dies.
 * Provides options to restart the game or return to the main menu.
 */
public class GameOverState implements GameState {

    private final int width;
    private final int height;

    private boolean tryAgainRequested = false;
    private boolean returnToMenuRequested = false;

    public GameOverState() {
        this.width = ConfigManager.getInstance().getConfig().engine.width;
        this.height = ConfigManager.getInstance().getConfig().engine.height;
    }

    @Override
    public void update(double deltaTime) {
        double mx = Input.getMouseX();
        double my = Input.getMouseY();
        boolean lmb = Input.isLmbPressed();

        // Button dimensions
        double btnWidth = 200;
        double btnHeight = 50;
        double centerX = (width - btnWidth) / 2.0;
        
        // Try Again Button
        if (isInside(mx, my, centerX, height / 2.0, btnWidth, btnHeight)) {
            if (lmb) tryAgainRequested = true;
        }

        // Back to Menu Button
        if (isInside(mx, my, centerX, height / 2.0 + 70, btnWidth, btnHeight)) {
            if (lmb) returnToMenuRequested = true;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // Dark overlay
        gc.setFill(Color.rgb(20, 0, 0, 0.8));
        gc.fillRect(0, 0, width, height);

        // "YOU DIED" Text
        gc.setFill(Color.RED);
        gc.setFont(Font.font("Cinzel", FontWeight.BOLD, 80));
        String deathMsg = "YOU DIED";
        gc.fillText(deathMsg, (width - calculateTextWidth(deathMsg, 80)) / 2.0, height / 3.0);

        gc.setFont(Font.font("Cinzel", FontWeight.NORMAL, 20));
        gc.setFill(Color.LIGHTGRAY);
        String subMsg = "Your soul returns to the cycle of reincarnation...";
        double subMsgWidth = calculateTextWidth(subMsg, 20);
        gc.fillText(subMsg, (width - subMsgWidth) / 2.0, height / 3.0 + 60);

        // Buttons
        drawButton(gc, "Try Again", height / 2.0);
        drawButton(gc, "Main Menu", height / 2.0 + 70);
    }

    private void drawButton(GraphicsContext gc, String text, double y) {
        double btnWidth = 200;
        double btnHeight = 50;
        double x = (width - btnWidth) / 2.0;

        boolean hover = isInside(Input.getMouseX(), Input.getMouseY(), x, y, btnWidth, btnHeight);

        // Shadow/Glow
        if (hover) {
            gc.setFill(Color.rgb(255, 0, 0, 0.3));
            gc.fillRect(x - 5, y - 5, btnWidth + 10, btnHeight + 10);
        }

        // Button Body
        gc.setFill(hover ? Color.rgb(60, 0, 0) : Color.rgb(40, 0, 0));
        gc.setStroke(hover ? Color.RED : Color.DARKRED);
        gc.setLineWidth(2);
        gc.fillRect(x, y, btnWidth, btnHeight);
        gc.strokeRect(x, y, btnWidth, btnHeight);

        // Text
        gc.setFill(hover ? Color.WHITE : Color.LIGHTGRAY);
        gc.setFont(Font.font("Cinzel", FontWeight.BOLD, 18));
        gc.fillText(text, x + (btnWidth - calculateTextWidth(text, 18)) / 2.0, y + 32);
    }

    private boolean isInside(double mx, double my, double x, double y, double w, double h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private double calculateTextWidth(String text, double fontSize) {
        // Refined approximation for Cinzel font (proportional width)
        return text.length() * (fontSize * 0.48);
    }

    /** @return true if the user clicked "Try Again". */
    public boolean isTryAgainRequested() { return tryAgainRequested; }
    /** @return true if the user clicked "Main Menu". */
    public boolean isReturnToMenuRequested() { return returnToMenuRequested; }
}
