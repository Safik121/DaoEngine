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
    private static final ObjectMapper mapper = new ObjectMapper();

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
            spriteMap.putAll(loaded);
            System.out.println("Loaded " + spriteMap.size() + " sprite definitions.");
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
        SpriteMetadata meta = spriteMap.get(spriteId);
        if (meta == null)
            return null;

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
        if (meta.frames <= 1)
            return sheet;

        // Clip frame from horizontal sheet
        int frameW = meta.width;
        int frameH = meta.height;
        return new WritableImage(sheet.getPixelReader(), frameIndex * frameW, 0, frameW, frameH);
    }

    /**
     * POJO for JSON mapping of sprite metadata.
     */
    public static class SpriteMetadata {
        /** Path to the image file. */
        public String path;
        /** Total number of frames in the horizontal sheet. */
        public int frames = 1;
        /** Standard width of a single frame. */
        public int width = 32;
        /** Standard height of a single frame. */
        public int height = 32;
        /** Default duration for one frame in seconds. */
        public double frameDuration = 0.15;
    }
}
