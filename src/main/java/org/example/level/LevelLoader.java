package org.example.level;

import org.example.GameLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

/**
 * Utility class for loading game levels from resources.
 * Uses Jackson's ObjectMapper for JSON-to-object mapping.
 */
public class LevelLoader {
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Loads a procedural generation configuration from the specified JSON file path.
     * 
     * @param filePath The path to the config JSON file in the resources folder.
     * @return The loaded LevelConfig object or a new default one if an error occurred.
     */
    public static LevelConfig loadConfig(String filePath) {
        try {
            InputStream is = LevelLoader.class.getResourceAsStream(filePath);
            if (is == null) {
                throw new RuntimeException("Config file not found: " + filePath);
            }
            return mapper.readValue(is, LevelConfig.class);
        } catch (Exception e) {
            GameLogger.error("Error while loading level config: " + filePath);
            e.printStackTrace();
            return new LevelConfig(); // Fallback to defaults
        }
    }

    /**
     * DTO for world_manifest.json
     */
    public static class WorldManifest {
        public java.util.List<String> maps;
    }

    /**
     * Loads the list of available maps from a world manifest JSON.
     * @param filePath Path to the manifest.
     * @return The manifest DTO or null.
     */
    public static WorldManifest loadManifest(String filePath) {
        try {
            InputStream is = LevelLoader.class.getResourceAsStream(filePath);
            if (is == null) return null;
            return mapper.readValue(is, WorldManifest.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}