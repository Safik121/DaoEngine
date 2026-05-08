package org.example.logic;
 
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.example.GameLogger;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
 
/**
 * Manages the loading and playback of sound effects throughout the game.
 * Supports both instance-based and static access for convenience.
 */
public class SoundManager {
    private static final Map<String, AudioClip> sounds = new HashMap<>();
    private static boolean isMuted = false;
    private static double volume = 0.5;
    
    private static MediaPlayer bgmPlayer;
    private static String currentBgmPath;
 
    /**
     * Loads a sound effect from the resources folder.
     * 
     * @param id The unique identifier for the sound (e.g., "click").
     * @param path The resource path to the audio file.
     */
    public static void loadSound(String id, String path) {
        try {
            URL resource = SoundManager.class.getResource(path);
            if (resource == null) {
                GameLogger.error("Sound file not found: " + path);
                return;
            }
            AudioClip clip = new AudioClip(resource.toExternalForm());
            sounds.put(id, clip);
        } catch (Exception e) {
            GameLogger.error("Failed to load sound: " + path);
            e.printStackTrace();
        }
    }
 
    /**
     * Plays a previously loaded sound effect.
     * 
     * @param id The identifier of the sound to play.
     */
    public static void playSound(String id) {
        if (isMuted) return;
        AudioClip clip = sounds.get(id);
        if (clip != null) {
            clip.play(volume);
        } else {
            // Fallback for names that might not have extension in the ID
            GameLogger.warning("Attempted to play unknown sound: " + id);
        }
    }
 
    /**
     * Immediately stops a playing sound effect.
     * @param id The identifier of the sound to stop.
     */
    public static void stopSound(String id) {
        AudioClip clip = sounds.get(id);
        if (clip != null) {
            clip.stop();
        }
    }
 
    /**
     * Triggers a one-shot sound effect (Instance wrapper for playSound).
     * @param soundId Resource name or ID.
     */
    public void playSfx(String soundId) {
        playSound(soundId);
    }
    
    /**
     * Starts looping background music.
     * @param path Resource path (e.g., "/sounds/background_1.mp3").
     */
    public static void playBgm(String path) {
        if (isMuted) return;
        if (path.equals(currentBgmPath) && bgmPlayer != null) return; // Already playing
        
        try {
            stopBgm(); // Stop previous if any
            
            URL resource = SoundManager.class.getResource(path);
            if (resource == null) {
                GameLogger.error("BGM file not found: " + path);
                return;
            }
            
            Media media = new Media(resource.toExternalForm());
            bgmPlayer = new MediaPlayer(media);
            bgmPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            bgmPlayer.setVolume(volume * 0.7); // Music should be slightly quieter than SFX
            bgmPlayer.play();
            
            currentBgmPath = path;
            GameLogger.info("[BGM] Playing track: " + path);
        } catch (Exception e) {
            GameLogger.error("Failed to play BGM: " + path);
            e.printStackTrace();
        }
    }
 
    /**
     * Stops current background music.
     */
    public static void stopBgm() {
        if (bgmPlayer != null) {
            bgmPlayer.stop();
            bgmPlayer = null;
            currentBgmPath = null;
        }
    }
 
    /** @param muted Whether to silence all audio. */
    public void setMuted(boolean muted) {
        isMuted = muted;
        if (muted) stopBgm();
    }
 
    /** @return true if audio is muted. */
    public static boolean isMuted() {
        return isMuted;
    }
 
    /** @param v Volume level (0.0 to 1.0). */
    public static void setVolume(double v) {
        volume = Math.max(0, Math.min(1.0, v));
        if (bgmPlayer != null) {
            bgmPlayer.setVolume(volume * 0.7);
        }
    }
}
