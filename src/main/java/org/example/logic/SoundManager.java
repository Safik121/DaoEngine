package org.example.logic;

import org.example.GameLogger;

/**
 * Stub manager for audio to make integration easy once JavaFX Media is fully set up.
 */
public class SoundManager {
    
    private boolean isMuted = false;

    /**
     * Triggers a one-shot sound effect.
     * @param soundId Resource name without extension.
     */
    public void playSfx(String soundId) {
        if (isMuted) return;
        // In a real implementation:
        // AudioClip clip = new AudioClip(getClass().getResource("/sounds/" + soundId + ".wav").toExternalForm());
        // clip.play();
        GameLogger.info("[SFX] Playing: " + soundId);
    }
    
    /**
     * Starts looping background music.
     * @param trackId Resource name without extension.
     */
    public void playBgm(String trackId) {
        if (isMuted) return;
        GameLogger.info("[BGM] Playing track: " + trackId);
    }

    /** @param muted Whether to silence all audio. */
    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }
}
