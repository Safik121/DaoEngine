package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.InputStream;

/**
 * Singleton manager to load and provide access to the global GameConfig.
 * Ensures data-driven values are available to all components.
 */
public class ConfigManager {
    private static ConfigManager instance;
    private GameConfig config;
    private static final ObjectMapper mapper = new ObjectMapper();

    private ConfigManager() {
        loadConfig("/game_config.json");
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfig(String path) {
        try {
            InputStream is = ConfigManager.class.getResourceAsStream(path);
            if (is != null) {
                config = mapper.readValue(is, GameConfig.class);
                System.out.println("Loaded global game config: " + config.engine.title);
            } else {
                System.err.println("Could not find global game config at: " + path);
                createFallbackConfig();
            }
        } catch (Exception e) {
            System.err.println("Error loading global game config!");
            e.printStackTrace();
            createFallbackConfig();
        }
    }

    private void createFallbackConfig() {
        config = new GameConfig();
        config.engine = new GameConfig.EngineConfig();
        config.engine.width = 1024;
        config.engine.height = 768;
        config.engine.title = "DaoEngine (Fallback)";
        config.engine.fps = 60;

        config.player = new GameConfig.PlayerConfig();
        config.player.initialMaxHp = 100.0;
        config.player.initialMaxQi = 100.0;
        config.player.baseSpeed = 3.0;

        config.ui = new GameConfig.UIConfig();
        config.ui.hpColor = "#FF0000";
        config.ui.qiColor = "#00FFFF";
    }

    /**
     * Persists the current configuration back to the JSON file on disk.
     * Note: In a development environment, this targets the source resources
     * directory.
     */
    public void saveConfig() {
        try {
            // Target the actual source file if running in an IDE environment
            File sourceFile = new File("src/main/resources/game_config.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(sourceFile, config);
            System.out.println("Persistent config saved to: " + sourceFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to save persistent config!");
            e.printStackTrace();
        }
    }

    public GameConfig getConfig() {
        return config;
    }
}
