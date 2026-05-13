package org.example.entity;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.example.logic.SkillRegistry;
import org.example.logic.Skill;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MASTER RESOURCE INTEGRITY TEST.
 * This test verifies that all JSON-based registries (Enemies, Skills, etc.) 
 * correctly parse resource files and instantiate game objects with perfect fidelity.
 */
@DisplayName("Resource Registries Integration (JSON Mapping)")
public class RegistryIntegrationTest {

    @BeforeAll
    static void init() {
        // Load all core registries from project resources
        EnemyRegistry.loadConfigs("/enemies/enemy_configs.json");
        SkillRegistry.loadSkills("/levels/skills.json");
    }

    @Test
    @DisplayName("EnemyRegistry: Verify Deep Attribute Mapping")
    public void testEnemyRegistryIntegrity() {
        // 1. Verify "bat_01" (Fast, low HP)
        Enemy bat = EnemyRegistry.createEnemy("bat_01", 100, 100, false, 1.0);
        assertNotNull(bat, "Enemy 'bat_01' should exist in JSON");
        assertEquals(20.0, bat.getStats().getMaxHp(), "HP for bat_01 must be 20.0 (from JSON)");
        assertEquals(1.5, bat.getStats().getSpeed(), "Speed for bat_01 must be 1.5 (from JSON)");

        // 2. Verify "guard_01" (Elite, high HP)
        Enemy guard = EnemyRegistry.createEnemy("guard_01", 200, 200, true, 1.0);
        assertNotNull(guard);
        assertEquals(120.0, guard.getStats().getMaxHp(), "HP for guard_01 must be 120.0");
        assertTrue(guard.isTribulation(), "Elite flag should be preserved");
    }

    @Test
    @DisplayName("EnemyRegistry: Scaling Mechanics Integrity")
    public void testEnemyScaling() {
        // Verify that scaling factor (2.0x) correctly modifies the base stats
        double scalingFactor = 2.0;
        Enemy scaledBat = EnemyRegistry.createEnemy("bat_01", 0, 0, false, scalingFactor);
        
        assertNotNull(scaledBat);
        // Base HP 20.0 * 2.0 scaling = 40.0
        assertEquals(40.0, scaledBat.getStats().getMaxHp(), "Scaled HP should be exactly 40.0 (20 * 2.0)");
    }

    @Test
    @DisplayName("SkillRegistry: Verify Technique Mapping")
    public void testSkillRegistryIntegrity() {
        // Verify core technique loading
        Skill fieryPalm = SkillRegistry.getSkill("fiery_palm");
        
        assertNotNull(fieryPalm, "Skill 'fiery_palm' should be loaded from skills.json");
        assertFalse(fieryPalm.getName().isEmpty(), "Skill name should not be empty");
        assertTrue(fieryPalm.getQiCost() > 0, "Skill should have a positive Qi cost");
    }

    @Test
    @DisplayName("Robustness: Handling Invalid IDs")
    public void testInvalidIdHandling() {
        // The registry should handle unknown IDs gracefully (usually by returning null)
        Enemy unknown = EnemyRegistry.createEnemy("non_existent_mob_999", 0, 0, false, 1.0);
        assertNull(unknown, "Creating a non-existent enemy should return null, not crash.");
        
        Skill unknownSkill = SkillRegistry.getSkill("super_mega_forbidden_technique");
        assertNull(unknownSkill, "Loading a non-existent skill should return null.");
    }
}
