package org.example.item;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Global registry and management system for weapon configurations.
 * Loads weapon data from JSON resources on startup and provides centralized access
 * to weapon properties identified by their unique string IDs.
 */
public class WeaponRegistry {
    /** Internal cache mapping weapon IDs to their loaded configurations. */
    private static Map<String, WeaponConfig> weapons = new HashMap<>();
    /** Jackson mapper instance used for JSON deserialization. */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Initializes the registry by loading weapon configurations from the classpath.
     * This method parses the JSON structure into a Map of WeaponConfig objects.
     * 
     * @param filePath The absolute resource path to the weapon configuration JSON.
     */
    public static void loadWeapons(String filePath) {
        try {
            InputStream is = WeaponRegistry.class.getResourceAsStream(filePath);
            if (is == null) {
                System.err.println("Weapon config file not found: " + filePath);
                return;
            }
            // Load map of WeaponConfigs indexed by their ID (e.g., "sword_01")
            weapons = mapper.readValue(is, new TypeReference<Map<String, WeaponConfig>>() {});
            System.out.println("Loaded " + weapons.size() + " weapon configurations.");
        } catch (Exception e) {
            System.err.println("Error loading weapon configurations: " + filePath);
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a specialized weapon configuration for the provided weapon ID.
     * 
     * @param id The unique string identifier of the weapon data.
     * @return The WeaponConfig associated with the ID, or null if it doesn't exist.
     */
    public static WeaponConfig getWeaponConfig(String id) {
        return weapons.get(id);
    }
}
