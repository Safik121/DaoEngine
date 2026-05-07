package org.example.logic;

import org.example.logic.event.EventManager;
import org.example.logic.event.GameEvent;
import org.example.state.PlayState;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class QuestIntegrationTest {

    @Test
    public void testQuestProgressThroughEvents() {
        EventManager eventManager = new EventManager();
        QuestManager questManager = new QuestManager();
        
        // Subscribe QuestManager to events
        eventManager.subscribe(GameEvent.ENTITY_DEATH, questManager);
        
        // New quest
        Quest quest = new Quest("q1", "Kill Slimes", "Desc", Quest.ObjectiveType.KILL, "slime_01", 2);
        questManager.addQuest(quest, null);
        
        // Mock state and its sub-managers to prevent NullPointerException
        PlayState mockState = mock(PlayState.class);
        org.example.logic.SoundManager mockSound = mock(org.example.logic.SoundManager.class);
        org.example.entity.Player mockPlayer = mock(org.example.entity.Player.class);
        org.example.item.Inventory mockInv = mock(org.example.item.Inventory.class);
        
        lenient().when(mockState.getSoundManager()).thenReturn(mockSound);
        lenient().when(mockState.getPlayer()).thenReturn(mockPlayer);
        lenient().when(mockPlayer.getInventory()).thenReturn(mockInv);
        
        // Simulate first death
        eventManager.triggerEvent(GameEvent.ENTITY_DEATH, "slime_01", 1, mockState);
        
        assertEquals(1, quest.getCurrentAmount());
        assertFalse(quest.isCompleted());
        
        // Second death -> quest finished
        eventManager.triggerEvent(GameEvent.ENTITY_DEATH, "slime_01", 1, mockState);
        
        assertEquals(2, quest.getCurrentAmount());
        assertTrue(quest.isCompleted());
    }
}
