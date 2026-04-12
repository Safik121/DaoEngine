package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.ConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * A stylized loading screen that transitions between the Menu and the active Game.
 * Uses cultivation-themed text to enhance immersion.
 */
public class LoadingState implements GameState {

    private final GameState targetState;
    private double progress = 0;
    private final double loadingDuration = 2.5; // seconds
    private double elapsedTime = 0;
    private boolean finished = false;

    private String currentMessage = "Initialising Heavens...";
    private final List<String> messages = new ArrayList<>();

    private final int width;
    private final int height;

    private static final Color GOLD = Color.web("#D4AF37");
    private static final Color DARK_INK = Color.web("#1A1A1A");

    public LoadingState(GameState targetState) {
        this.targetState = targetState;
        this.width = ConfigManager.getInstance().getConfig().engine.width;
        this.height = ConfigManager.getInstance().getConfig().engine.height;

        messages.add("Gathering Natural Essence...");
        messages.add("Stabilizing Qi Gates...");
        messages.add("Opening the Spirit Meridian...");
        messages.add("Seeking the Heavenly Dao...");
        messages.add("Forging the Golden Core...");
        messages.add("Entering the Immortal Realm...");
    }

    @Override
    public void update(double deltaTime) {
        elapsedTime += deltaTime;
        progress = Math.min(1.0, elapsedTime / loadingDuration);

        // Update message based on progress
        int msgIndex = (int) (progress * (messages.size() - 1));
        if (msgIndex < messages.size()) {
            currentMessage = messages.get(msgIndex);
        }

        if (progress >= 1.0) {
            finished = true;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        drawBackground(gc);
        drawProgressBar(gc);
        drawLoadingText(gc);
    }

    private void drawBackground(GraphicsContext gc) {
        LinearGradient grad = new LinearGradient(0, 0, 0, 1, true, null,
                new Stop(0, DARK_INK),
                new Stop(1, Color.BLACK)
        );
        gc.setFill(grad);
        gc.fillRect(0, 0, width, height);

        // Border
        gc.setStroke(GOLD);
        gc.setLineWidth(2);
        gc.strokeRect(10, 10, width - 20, height - 20);
    }

    private void drawProgressBar(GraphicsContext gc) {
        double barW = 400;
        double barH = 10;
        double x = (width - barW) / 2.0;
        double y = height / 2.0 + 50;

        // Track
        gc.setFill(Color.web("#333333"));
        gc.fillRoundRect(x, y, barW, barH, 5, 5);

        // Progress
        gc.setFill(GOLD);
        gc.fillRoundRect(x, y, barW * progress, barH, 5, 5);

        // Glow
        gc.setStroke(GOLD);
        gc.setGlobalAlpha(0.3);
        gc.strokeRoundRect(x - 2, y - 2, barW + 4, barH + 4, 7, 7);
        gc.setGlobalAlpha(1.0);
    }

    private void drawLoadingText(GraphicsContext gc) {
        gc.setFill(GOLD);
        gc.setFont(Font.font("Serif", FontWeight.BOLD, 28));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(currentMessage, width / 2.0, height / 2.0);

        gc.setFont(Font.font("Serif", 14));
        gc.setFill(Color.web("#808080"));
        gc.fillText("Ascension in progress: " + (int)(progress * 100) + "%", width / 2.0, height / 2.0 + 85);
        
        gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
    }

    public boolean isFinished() {
        return finished;
    }

    public GameState getTargetState() {
        return targetState;
    }
}
