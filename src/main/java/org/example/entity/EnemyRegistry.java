package org.example.entity;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for loading and managing enemy types from JSON.
 */
public class EnemyRegistry {
    /** Map of all loaded enemy configurations by ID. */
    private static Map<String, EnemyConfig> configs = new HashMap<>();
    /** Jackson object mapper for JSON parsing. */
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Loads the enemy configurations from the specified resource path.
     * 
     * @param resourcePath Path to the enemy_configs.json.
     */
    public static void loadConfigs(String resourcePath) {
        try {
            InputStream is = EnemyRegistry.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Enemy configuration file not found: " + resourcePath);
                return;
            }
            // Load a map of EnemyConfigs (ID -> Config)
            configs = mapper.readValue(is, new TypeReference<Map<String, EnemyConfig>>() {});
            System.out.println("Loaded " + configs.size() + " enemy configurations.");
        } catch (Exception e) {
            System.err.println("Fatal error loading EnemyRegistry data!");
            e.printStackTrace();
        }
    }

    /**
     * @param id The enemy ID from the registry.
     * @param x  Spawn X pixel.
     * @param y  Spawn Y pixel.
     * @param isTribulation Whether the enemy is elite.
     * @param scaling Constant multiplier for health and damage.
     * @return A newly created Enemy instance, or null if ID doesn't exist.
     */
    public static Enemy createEnemy(String id, double x, double y, boolean isTribulation, double scaling) {
        EnemyConfig config = configs.get(id);
        if (config == null) {
            System.err.println("Unknown enemy ID: " + id);
            return null;
        }

        // Apply scaling factor for Tribulation enemies
        double finalHp = config.hp * scaling;
        double finalDamage = config.damage * scaling;
        double finalSpeed = config.speed * (isTribulation ? 1.2 : 1.0); // Slight speed boost for elites

        Enemy enemy = new Enemy(x, y, isTribulation);
        enemy.setStats(config.id, config.name, finalHp, finalDamage, finalSpeed, config.size, config.color);
        return enemy;
    }

    /** @return Set of all registered enemy IDs. */
    public static Set<String> getAllIds() {
        return configs.keySet();
    }
}
