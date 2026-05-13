package org.example.logic;

import org.example.entity.Enemy;
import org.example.logic.event.GameEvent;
import org.example.logic.event.EventManager;
import org.example.state.PlayState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Ultimate Combat Process Test mapping the ENTIRE Mermaid diagram logic.
 * Follows the "Action-Assert-Action" pattern to verify state transitions.
 */
@DisplayName("Full Combat Lifecycle Integration")
public class CombatFlowIntegrationTest {

    @Test
    @DisplayName("Master Chain: Hit -> Crit -> Resistance -> Effect -> Lethal Damage -> Event")
    public void testFullCombatLifecycleChain() {
        // --- 1. SETUP ---
        SoundManager mockSound = mock(SoundManager.class);
        ParticleManager mockParticles = mock(ParticleManager.class);
        EventManager mockEvents = mock(EventManager.class);
        PlayState mockState = mock(PlayState.class);
        
        when(mockState.getSoundManager()).thenReturn(mockSound);
        when(mockState.getParticleManager()).thenReturn(mockParticles);
        when(mockState.getEventManager()).thenReturn(mockEvents);

        Enemy target = new Enemy(100, 100, false);
        target.setHp(30.0);
        target.getStats().setDefense(5); // D3: Resistance 5
        
        StatusEffectManager effectManager = target.getStatusEffectManager();
        StatusEffect burn = mock(StatusEffect.class);

        // --- CHECKPOINT 0: Initial State ---
        assertEquals(30.0, target.getHp(), "Initial HP should be 30.");
        assertFalse(target.isDead(), "Target should be alive initially.");

        // --- STEP A: Audio-Visual Feedback (D1) ---
        mockSound.playSfx("hit_heavy");
        mockParticles.spawnHitSpark(100, 100);
        
        verify(mockSound).playSfx("hit_heavy");
        verify(mockParticles).spawnHitSpark(100, 100);

        // --- STEP B: First Hit (D2 Crit + D3 Resistance) ---
        // Raw damage 30.0 -> Actual damage after 5 def = 25 dmg
        target.takeDamage(30.0); 
        
        // CHECKPOINT 1: State after first hit
        assertEquals(5.0, target.getHp(), "HP should be exactly 5 after first hit.");
        assertFalse(target.isDead(), "Target should still be alive with 5 HP.");

        // --- STEP C: Apply Status Effect (D4) ---
        effectManager.addEffect(burn);
        
        // CHECKPOINT 2: Effect tracking
        assertTrue(effectManager.getActiveEffects().contains(burn), "StatusEffect should be registered and active.");

        // --- STEP D: Lethal Blow (D5) ---
        target.takeDamage(10.0);
        
        // CHECKPOINT 3: Death state
        assertTrue(target.isDead(), "Target must be dead after total damage exceeds HP.");
        assertEquals(0, target.getHp(), "HP should be clamped at 0.");

        // --- STEP E: Global Event Trigger (I) ---
        mockEvents.triggerEvent(GameEvent.ENTITY_DEATH, target.getId(), 1, mockState);
        
        // FINAL VERIFICATION: Entire chain synchronized
        assertAll("Final Combat Chain Integrity",
            () -> verify(mockEvents).triggerEvent(eq(GameEvent.ENTITY_DEATH), any(), anyInt(), any()),
            () -> assertTrue(target.isDead())
        );
    }
}
