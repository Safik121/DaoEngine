package org.example.logic;

import org.example.ConfigManager;
import org.example.entity.LightningStrike;
import org.example.entity.Player;
import org.example.entity.Enemy;
import org.example.state.PlayState;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CombatIntegrationTest {

    @Test
    public void testStrikeDamageApplication() throws Exception {
        CombatManager manager = new CombatManager();
        PlayState mockState = mock(PlayState.class);
        
        // Prepare player and enemy (positions set so it hits both)
        Player player = new Player(100, 100);
        player.setHp(100);
        
        Enemy enemy = new Enemy(105, 105, false);
        enemy.setHp(50);
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(enemy);
        
        when(mockState.getPlayer()).thenReturn(player);
        when(mockState.getEnemies()).thenReturn(enemies);
        
        // Strike at 102, 102 (within range of both)
        LightningStrike strike = new LightningStrike(102, 102);
        
        // This method is private, so we call it via reflection
        Method method = CombatManager.class.getDeclaredMethod("handleStrikeDamage", PlayState.class, LightningStrike.class);
        method.setAccessible(true);
        
        method.invoke(manager, mockState, strike);
        
        // Check if HP decreased (defense mitigation might affect this slightly)
        assertTrue(player.getHp() < 100, "Player should have taken damage");
        assertTrue(enemy.getHp() < 50, "Enemy should have taken damage");
    }
}
