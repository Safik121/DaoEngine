package org.example;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for loading and managing textures and animations.
 * Provides a unified way to retrieve sprites for characters, tiles, and
 * effects.
 */
public class AssetRegistry {
    private static final Map<String, SpriteMetadata> spriteMap = new HashMap<>();
    private static final Map<String, Image> imageCache = new HashMap<>();
    /** Cached arrays of pre-clipped frames for multi-frame sprites. */
    private static final Map<String, Image[]> frameCache = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Loads the asset manifest from JSON.
     * 
     * @param manifestPath Path to the assets.json file.
     */
    public static void loadAssets(String manifestPath) {
        try {
            InputStream is = AssetRegistry.class.getResourceAsStream(manifestPath);
            if (is == null) {
                System.err.println("Asset manifest not found: " + manifestPath);
                return;
            }
            Map<String, SpriteMetadata> loaded = mapper.readValue(is, new TypeReference<Map<String, SpriteMetadata>>() {
            });
            
            // Post-process to ensure frames count matches paths length if paths are present
            for (SpriteMetadata meta : loaded.values()) {
                if (meta.paths != null && meta.paths.length > 0) {
                    meta.frames = meta.paths.length;
                }
            }
            
            spriteMap.putAll(loaded);
            GameLogger.info("Loaded " + spriteMap.size() + " sprite definitions.");
        } catch (Exception e) {
            System.err.println("Error loading AssetRegistry manifest!");
            e.printStackTrace();
        }
    }

    /**
     * Gets a specific image or frame. Caches images on first load.
     * 
     * @param spriteId   Unique ID from assets.json (e.g. "player_walk").
     * @param frameIndex The index of the frame in the sprite sheet.
     * @return The Image object (or a sub-image for clipping).
     */
    public static Image getSprite(String spriteId, int frameIndex) {
        // Optimized: Check frame cache first
        Image[] frames = frameCache.get(spriteId);
        if (frames != null && frameIndex >= 0 && frameIndex < frames.length) {
            return frames[frameIndex];
        }

        SpriteMetadata meta = spriteMap.get(spriteId);
        if (meta == null)
            return null;

        // --- Multi-File Mode ---
        // If 'paths' is provided in JSON, we prioritize loading individual images as frames.
        if (meta.paths != null && meta.paths.length > 0) {
            Image[] cachedFrames = new Image[meta.paths.length];
            for (int i = 0; i < meta.paths.length; i++) {
                String framePath = meta.paths[i];
                cachedFrames[i] = imageCache.computeIfAbsent(framePath, p -> {
                    InputStream is = AssetRegistry.class.getResourceAsStream(p);
                    if (is == null) {
                        System.err.println("Frame file not found: " + p);
                        return null;
                    }
                    return new Image(is);
                });
            }
            frameCache.put(spriteId, cachedFrames);
            return (frameIndex >= 0 && frameIndex < cachedFrames.length) ? cachedFrames[frameIndex] : null;
        }

        // --- Sprite Sheet Mode ---
        // Fallback to loading a single large image and cutting it into grid frames.
        Image sheet = imageCache.computeIfAbsent(meta.path, path -> {
            InputStream is = AssetRegistry.class.getResourceAsStream(path);
            if (is == null) {
                System.err.println("Texture file not found: " + path);
                return null;
            }
            return new Image(is);
        });

        if (sheet == null)
            return null;

        // If single frame, return sheet and cache it as a 1-element array
        if (meta.frames <= 1) {
            Image single = sheet;
            frameCache.put(spriteId, new Image[] { single });
            return single;
        }

        // Lazy-populate all frames into cache for multi-frame sprites
        Image[] cachedFrames = new Image[meta.frames];
        int frameW = meta.width;
        int frameH = meta.height;
        PixelReader reader = sheet.getPixelReader();

        // Use explicit column count if provided, otherwise calculate from width
        int colsInSheet = (meta.cols > 0) ? meta.cols : (int) (sheet.getWidth() / frameW);
        if (colsInSheet <= 0) colsInSheet = 1;

        for (int i = 0; i < meta.frames; i++) {
            int row = i / colsInSheet;
            int col = i % colsInSheet;
            int startX = col * frameW;
            int startY = row * frameH;
            
            // Boundary checks
            if (startY + frameH > sheet.getHeight() || startX + frameW > sheet.getWidth()) break;
            
            cachedFrames[i] = new WritableImage(reader, startX, startY, frameW, frameH);
        }
        frameCache.put(spriteId, cachedFrames);

        return (frameIndex >= 0 && frameIndex < cachedFrames.length) ? cachedFrames[frameIndex] : null;
    }

    /**
     * Helper to get the total number of frames for a specific sprite ID.
     */
    public static int getFrameCount(String spriteId) {
        SpriteMetadata meta = spriteMap.get(spriteId);
        if (meta == null) return 1;
        if (meta.paths != null && meta.paths.length > 0) return meta.paths.length;
        return meta.frames;
    }

    /**
     * POJO for JSON mapping of sprite metadata.
     */
    public static class SpriteMetadata {
        /** Path to a single sprite sheet image (Legacy mode). */
        public String path;
        /** Array of paths to individual frame images (Universal mode). */
        public String[] paths;
        /** Total number of frames (extracted from sheet or paths list). */
        public int frames = 1;
        /** Nominal width of a single frame (used for clipping sheets). */
        public int width = 32;
        /** Nominal height of a single frame (used for clipping sheets). */
        public int height = 32;
        /** Number of columns in a sprite sheet grid (0 = auto-calculate). */
        public int cols = 0;
        /** How long to display each frame during animation (seconds). */
        public double frameDuration = 0.15;
    }
}
