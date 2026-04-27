package org.example.level;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;

/**
 * Utility class for loading game levels from resources.
 * Uses Jackson's ObjectMapper for JSON-to-object mapping.
 */
public class LevelLoader {

    /**
     * Loads a level from the specified JSON file path.
     * 
     * @param filePath The path to the level JSON file in the resources folder (e.g., "/levels/level1.json").
     * @return The loaded Level object, or null if an error occurred.
     */
    public static Level loadLevel(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            // Access the resources folder for the JSON file
            InputStream is = LevelLoader.class.getResourceAsStream(filePath);
            if (is == null) {
                throw new RuntimeException("Level file not found: " + filePath);
            }
            // Jackson magic: automatically maps JSON to the Level class
            return mapper.readValue(is, Level.class);
        } catch (Exception e) {
            System.err.println("Error while loading level!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads a procedural generation configuration from the specified JSON file path.
     * 
     * @param filePath The path to the config JSON file in the resources folder.
     * @return The loaded LevelConfig object or a new default one if an error occurred.
     */
    public static LevelConfig loadConfig(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            InputStream is = LevelLoader.class.getResourceAsStream(filePath);
            if (is == null) {
                throw new RuntimeException("Config file not found: " + filePath);
            }
            return mapper.readValue(is, LevelConfig.class);
        } catch (Exception e) {
            System.err.println("Error while loading level config: " + filePath);
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

    public static WorldManifest loadManifest(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
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