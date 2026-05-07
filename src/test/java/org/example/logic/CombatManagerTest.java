package org.example.logic;

import org.example.entity.LivingEntity;
import org.example.entity.Projectile;
import org.example.item.WeaponConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CombatManagerTest {

    @Mock
    private LivingEntity mockTarget;

    @Mock
    private Projectile mockProjectile;

    @Test
    public void testApplyDamageCallsTarget() throws Exception {
        CombatManager manager = new CombatManager();
        
        when(mockProjectile.getDamage()).thenReturn(20.0);
        when(mockProjectile.getType()).thenReturn(WeaponConfig.ProjectileType.FIREBALL);
        
        // applyDamage is private, so we call it via reflection
        Method method = CombatManager.class.getDeclaredMethod("applyDamage", Projectile.class, LivingEntity.class, double.class);
        method.setAccessible(true);
        
        method.invoke(manager, mockProjectile, mockTarget, 0.016);
        
        // Verify that damage was indeed transferred to target
        verify(mockTarget).takeDamage(20.0);
    }
}
