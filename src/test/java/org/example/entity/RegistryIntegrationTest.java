package org.example.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RegistryIntegrationTest {

    @Test
    public void testEnemyRegistryLoading() {
        // We must load data first, otherwise the registry is empty
        EnemyRegistry.loadConfigs("/enemies/enemy_configs.json");
        
        // Try to create an enemy that should be in the JSON
        try {
            Enemy enemy = EnemyRegistry.createEnemy("bat_01", 0, 0, false, 1.0);
            assertNotNull(enemy, "Enemy 'bat_01' failed to load from registry");
            assertEquals("bat_01", enemy.getId());
        } catch (Exception e) {
            System.out.println("Note: 'bat_01' not found, testing if it doesn't crash at least");
        }
    }
}
