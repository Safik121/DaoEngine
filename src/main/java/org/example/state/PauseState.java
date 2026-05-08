package org.example.state;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.GameLogger;
import org.example.Input;
import org.example.SaveManager;
import org.example.logic.SoundManager;

/**
 * A dedicated game state for the Pause Menu.
 * Complies with the Vision Document requirement for engine-managed states.
 */
public class PauseState implements GameState {

    private final PlayState backgroundState;
    private boolean returnToMenuRequested = false;
    private boolean resumeRequested = false;
    private boolean lexiconRequested = false;
    
    // Input state tracking to prevent "flash" transitions
    private boolean escWasPressed = true; 
    private boolean lmbWasPressed = true;

    /**
     * @param playState The active gameplay session to freeze in the background.
     */
    public PauseState(PlayState playState) {
        this.backgroundState = playState;
        GameLogger.info("Entered PauseState.");
    }

    @Override
    public void update(double deltaTime) {
        boolean escPressed = Input.isKeyPressed(javafx.scene.input.KeyCode.ESCAPE);
        if (escPressed && !escWasPressed) {
             resumeRequested = true;
        }
        escWasPressed = escPressed;

        if (resumeRequested) return;

        handleMenuInteraction();
    }

    private void handleMenuInteraction() {
        boolean lmbPressed = Input.isLmbPressed();
        if (!lmbPressed || lmbWasPressed) {
            lmbWasPressed = lmbPressed;
            return;
        }
        lmbWasPressed = lmbPressed;

        double mx = Input.getMouseX(), my = Input.getMouseY();
        double centerX = backgroundState.getScreenWidth() / 2.0;

        if (backgroundState.getCurrentPauseState() == PlayState.PauseMenuState.MAIN) {
            if (PlayState.isInside(mx, my, centerX - 150, 260, 300, 50)) { resumeRequested = true; SoundManager.playSound("click"); }
            if (PlayState.isInside(mx, my, centerX - 150, 320, 300, 50)) { lexiconRequested = true; SoundManager.playSound("click"); }
            if (PlayState.isInside(mx, my, centerX - 150, 380, 300, 50)) { backgroundState.setCurrentPauseState(PlayState.PauseMenuState.SAVE_SELECT); SoundManager.playSound("click"); }
            if (PlayState.isInside(mx, my, centerX - 150, 440, 300, 50)) { backgroundState.setCurrentPauseState(PlayState.PauseMenuState.LOAD_SELECT); SoundManager.playSound("click"); }
            if (PlayState.isInside(mx, my, centerX - 150, 500, 300, 50)) { returnToMenuRequested = true; SoundManager.playSound("click"); }
        } else {
            // Save/Load slot selection
            for (int i = 1; i <= 5; i++) {
                if (PlayState.isInside(mx, my, centerX - 200, 210 + (i - 1) * 70, 400, 60)) {
                    if (backgroundState.getCurrentPauseState() == PlayState.PauseMenuState.SAVE_SELECT) {
                        backgroundState.performSave(i);
                        backgroundState.setCurrentPauseState(PlayState.PauseMenuState.MAIN);
                    } else {
                        if (SaveManager.exists(i)) {
                            backgroundState.performLoad(i);
                            resumeRequested = true; // Resume after loading
                        }
                        backgroundState.setCurrentPauseState(PlayState.PauseMenuState.MAIN);
                    }
                }
            }
            if (PlayState.isInside(mx, my, centerX - 150, 600, 300, 60)) {
                backgroundState.setCurrentPauseState(PlayState.PauseMenuState.MAIN);
                SoundManager.playSound("click");
            }
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        // 1. Render the game world behind the menu
        backgroundState.render(gc);

        // 2. Apply a darkening overlay
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        // 3. The actual Pause Menu rendering is currently in PlayUIManager.
        // We will trigger it via the reference.
        backgroundState.getUiManager().renderPauseMenu(gc, backgroundState);
    }

    /** @return true if the game should resume. */
    public boolean isResumeRequested() {
        return resumeRequested;
    }

    /** @param resumeRequested New state of resume request. */
    public void setResumeRequested(boolean resumeRequested) {
        this.resumeRequested = resumeRequested;
    }

    /** @return true if returning to title screen. */
    public boolean isReturnToMenuRequested() {
        return returnToMenuRequested;
    }

    /** @return The frozen gameplay session. */
    public PlayState getBackgroundState() {
        return backgroundState;
    }

    /** @return true if the lexicon was opened. */
    public boolean isLexiconRequested() {
        return lexiconRequested;
    }

    /** @param lexiconRequested New state of lexicon request. */
    public void setLexiconRequested(boolean lexiconRequested) {
        this.lexiconRequested = lexiconRequested;
    }
}
