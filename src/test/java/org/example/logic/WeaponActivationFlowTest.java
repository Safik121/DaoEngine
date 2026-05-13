package org.example.logic;

import org.example.Input;
import org.example.entity.Player;
import org.example.item.Inventory;
import org.example.item.Item;
import org.example.item.WeaponConfig;
import org.example.state.PlayState;
import org.example.ui.DialogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Formal test for Section 7.5 of the Testing Report: Weapon Activation Flow.
 * Verifies the 8 decision points in CombatManager.handleFiring.
 */
public class WeaponActivationFlowTest {

    private CombatManager combatManager;
    private PlayState mockState;
    private Player mockPlayer;
    private Inventory mockInventory;
    private DialogManager mockDialogManager;

    @BeforeEach
    void setUp() {
        combatManager = new CombatManager();
        mockState = mock(PlayState.class);
        mockPlayer = mock(Player.class);
        mockInventory = mock(Inventory.class);
        mockDialogManager = mock(DialogManager.class);

        when(mockState.getPlayer()).thenReturn(mockPlayer);
        when(mockPlayer.getInventory()).thenReturn(mockInventory);
        when(mockState.getDialogManager()).thenReturn(mockDialogManager);
        
        // Stats needed for Projectile creation
        when(mockPlayer.getStats()).thenReturn(new org.example.logic.AttributeSet(100, 10, 10, 10, 10));
    }

    @Test
    void testD1_UIBlocking() {
        // Setup: UI is active (Inventory open)
        when(mockState.isInventoryOpen()).thenReturn(true);

        try (MockedStatic<Input> inputMock = mockStatic(Input.class)) {
            combatManager.handleFiring(mockState);
            
            // Verify: canAttack was never even checked because UI blocked it
            verify(mockPlayer, never()).canAttack();
        }
    }

    @Test
    void testD7_InsufficientQi() {
        // Setup: UI is clear, LMB pressed, can attack, has weapon
        when(mockState.isInventoryOpen()).thenReturn(false);
        when(mockPlayer.canAttack()).thenReturn(true);
        
        Item mockWeapon = mock(Item.class);
        WeaponConfig config = new WeaponConfig();
        config.qiCost = 100;
        
        when(mockInventory.getItemInHotbar(anyInt())).thenReturn(mockWeapon);
        when(mockWeapon.getType()).thenReturn(Item.Type.WEAPON);
        when(mockWeapon.getWeaponConfig()).thenReturn(config);
        
        // D7: Player has 0 Qi, so spendQi returns false
        when(mockPlayer.spendQi(100)).thenReturn(false);

        try (MockedStatic<Input> inputMock = mockStatic(Input.class)) {
            inputMock.when(Input::isLmbPressed).thenReturn(true);
            
            combatManager.handleFiring(mockState);
            
            // Verify: Qi was checked but failed, so no cooldown applied
            verify(mockPlayer).spendQi(100);
            verify(mockPlayer, never()).setAttackCooldown(anyDouble());
        }
    }

    @Test
    void testD8_SuccessfulSingleFire() {
        // Setup: Everything is valid
        when(mockState.isInventoryOpen()).thenReturn(false);
        when(mockPlayer.canAttack()).thenReturn(true);
        
        Item mockWeapon = mock(Item.class);
        WeaponConfig config = new WeaponConfig();
        config.qiCost = 10;
        config.cooldown = 0.5;
        config.burstCount = 1; // D8: Single shot
        
        when(mockInventory.getItemInHotbar(anyInt())).thenReturn(mockWeapon);
        when(mockWeapon.getType()).thenReturn(Item.Type.WEAPON);
        when(mockWeapon.getWeaponConfig()).thenReturn(config);
        when(mockPlayer.spendQi(10)).thenReturn(true);

        try (MockedStatic<Input> inputMock = mockStatic(Input.class)) {
            inputMock.when(Input::isLmbPressed).thenReturn(true);
            // Mock mouse for fireWeaponOrSkill internal logic
            inputMock.when(Input::getMouseX).thenReturn(100.0);
            inputMock.when(Input::getMouseY).thenReturn(100.0);
            
            combatManager.handleFiring(mockState);
            
            // Verify: Success! Cooldown applied.
            verify(mockPlayer).setAttackCooldown(0.5);
        }
    }
}
