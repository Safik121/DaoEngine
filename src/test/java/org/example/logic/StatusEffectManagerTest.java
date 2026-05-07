package org.example.logic;

import org.example.entity.LivingEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StatusEffectManager Interaction Tests")
public class StatusEffectManagerTest {

    @Mock
    private LivingEntity mockOwner;

    @Mock
    private StatusEffect mockEffect;

    @Test
    @DisplayName("Adding effect should trigger onApply and store it")
    public void testAddEffectTriggersApply() {
        StatusEffectManager manager = new StatusEffectManager(mockOwner);
        
        manager.addEffect(mockEffect);
        
        // Verify that onApply was called immediately after adding
        verify(mockEffect, times(1)).onApply(mockOwner);
        assertTrue(manager.getActiveEffects().contains(mockEffect));
    }

    @Test
    @DisplayName("Updating manager should tick effects and remove expired ones")
    public void testUpdateRemovesExpiredEffects() {
        StatusEffectManager manager = new StatusEffectManager(mockOwner);
        manager.addEffect(mockEffect);
        
        // Mock onTick to return false (as if the effect expired)
        when(mockEffect.onTick(eq(mockOwner), anyDouble())).thenReturn(false);
        
        manager.update(0.1);
        
        // onTick and onRemove must be called
        verify(mockEffect).onTick(eq(mockOwner), eq(0.1));
        verify(mockEffect).onRemove(mockOwner);
        assertTrue(manager.getActiveEffects().isEmpty(), "Effect list should be empty.");
    }

    @Test
    @DisplayName("Manager should handle multiple effects independently")
    public void testMultipleEffects() {
        StatusEffectManager manager = new StatusEffectManager(mockOwner);
        StatusEffect e1 = mock(StatusEffect.class);
        StatusEffect e2 = mock(StatusEffect.class);
        
        when(e1.onTick(any(), anyDouble())).thenReturn(true); // e1 remains
        when(e2.onTick(any(), anyDouble())).thenReturn(false); // e2 expires
        
        manager.addEffect(e1);
        manager.addEffect(e2);
        
        manager.update(1.0);
        
        assertTrue(manager.getActiveEffects().contains(e1));
        assertFalse(manager.getActiveEffects().contains(e2));
        verify(e2).onRemove(mockOwner);
    }

    @Test
    @DisplayName("Clearing effects should trigger onRemove for all")
    public void testClearEffects() {
        StatusEffectManager manager = new StatusEffectManager(mockOwner);
        manager.addEffect(mockEffect);
        
        manager.clear();
        
        verify(mockEffect).onRemove(mockOwner);
        assertTrue(manager.getActiveEffects().isEmpty());
    }
}


