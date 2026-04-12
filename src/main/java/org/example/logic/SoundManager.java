package org.example.logic;

/**
 * Stub manager for audio to make integration easy once JavaFX Media is fully set up.
 */
public class SoundManager {
    
    private boolean isMuted = false;

    public void playSfx(String soundId) {
        if (isMuted) return;
        // In a real implementation:
        // AudioClip clip = new AudioClip(getClass().getResource("/sounds/" + soundId + ".wav").toExternalForm());
        // clip.play();
        System.out.println("[SFX] Playing: " + soundId);
    }
    
    public void playBgm(String trackId) {
        if (isMuted) return;
        System.out.println("[BGM] Playing track: " + trackId);
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }
}
