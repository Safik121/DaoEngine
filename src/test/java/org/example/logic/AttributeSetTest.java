package org.example.logic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AttributeSet class, which manages base RPG stats.
 */
@DisplayName("AttributeSet RPG Mechanics Tests")
public class AttributeSetTest {

    @ParameterizedTest(name = "Damage={0}, Defense={1} => ExpectedHP={2}")
    @CsvSource({
        "15, 10, 95",   // Classic: 15 dmg - 10 def = 5 damage
        "5, 10, 99",    // Damage less than defense -> minimum damage is always 1
        "10, 10, 99",   // Dmg = def -> min 1 again
        "120, 0, 0",    // Overkill (HP must not go negative)
        "-10, 10, 99"   // Negative dmg should be handled as minimum hit (1)
    })
    @DisplayName("Boundary value analysis for damage calculation")
    public void testTakeDamageBoundaries(double amount, double defense, double expectedHp) {
        AttributeSet stats = new AttributeSet(100, 10, 10, defense, 10);
        stats.takeDamage(amount);
        assertEquals(expectedHp, stats.getHp(), "HP calculation mismatch for dmg=" + amount + " and def=" + defense);
    }

    @Test
    @DisplayName("Heal logic including max HP constraints")
    public void testHeal() {
        AttributeSet stats = new AttributeSet(100, 10, 10, 10, 10);
        stats.setHp(50);
        
        stats.heal(30);
        assertEquals(80, stats.getHp(), "Heal should add HP.");
        
        // Heal above maxHP
        stats.heal(100);
        assertEquals(100, stats.getHp(), "HP must not exceed maxHP.");
        
        // Negative heal (should do nothing)
        stats.heal(-50);
        assertEquals(100, stats.getHp(), "Negative heal should not change HP (or should be ignored).");
    }

    @Test
    @DisplayName("Testing attribute addition methods (Growth)")
    public void testAttributeGrowth() {
        AttributeSet stats = new AttributeSet(100, 10, 10, 10, 10);
        
        stats.addMaxHp(50);
        assertEquals(150, stats.getMaxHp(), "MaxHP should increase.");
        
        stats.addStrength(5);
        assertEquals(15, stats.getStrength(), "Strength should increase.");
        
        stats.addDefense(5);
        assertEquals(15, stats.getDefense(), "Defense should increase.");
        
        stats.addSpirit(5);
        assertEquals(15, stats.getSpirit(), "Spirit should increase.");
    }

    @Test
    @DisplayName("Stat clamping during setHp and setMaxHp")
    public void testClamping() {
        AttributeSet stats = new AttributeSet(100, 10, 10, 10, 10);
        
        // HP clamp to 0
        stats.setHp(-50);
        assertEquals(0, stats.getHp(), "HP should be at least 0.");
        
        // HP clamp to maxHP
        stats.setHp(200);
        assertEquals(100, stats.getHp(), "HP should be at most maxHP.");
        
        // Changing maxHP downwards should clip current HP
        stats.setHp(100);
        stats.setMaxHp(80);
        assertEquals(80, stats.getHp(), "Current HP should be clipped according to new maxHP.");
    }
}
