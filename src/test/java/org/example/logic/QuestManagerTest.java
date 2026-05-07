package org.example.logic;

import org.example.state.PlayState;
import org.example.entity.Player;
import org.example.logic.event.GameEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuestManager Business Logic Tests (Mockito)")
public class QuestManagerTest {

    @Mock
    private PlayState mockState;

    @Mock
    private Player mockPlayer;

    @Mock
    private Quest mockQuest;
    
    @Mock
    private SoundManager mockSound;
    
    @Mock
    private org.example.item.Inventory mockInv;

    @Test
    @DisplayName("Successfully add quest and notify UI")
    public void testAddQuest() {
        QuestManager manager = new QuestManager();
        when(mockQuest.getId()).thenReturn("q1");
        when(mockQuest.getName()).thenReturn("Test Quest");
        
        manager.addQuest(mockQuest, mockState);
        
        assertTrue(manager.hasQuest("q1"));
        verify(mockQuest, atLeastOnce()).getId();
        // Verify that a notification was added to the UI
        verify(mockState, times(1)).addNotification(contains("New Quest"));
    }

    @Test
    @DisplayName("Complete quest via ENTITY_DEATH event and reward player")
    public void testRegisterProgressAndComplete() {
        QuestManager manager = new QuestManager();
        
        when(mockQuest.getId()).thenReturn("q1");
        when(mockQuest.getObjectiveType()).thenReturn(Quest.ObjectiveType.KILL);
        when(mockQuest.getTargetId()).thenReturn("enemy_01");
        when(mockQuest.addProgress(1)).thenReturn(true); // Quest completes with this
        when(mockQuest.getRewardQi()).thenReturn(10.0);
        when(mockQuest.getName()).thenReturn("Slayer Quest");
        when(mockState.getPlayer()).thenReturn(mockPlayer);
        lenient().when(mockState.getSoundManager()).thenReturn(mockSound);
        lenient().when(mockPlayer.getInventory()).thenReturn(mockInv);
        
        manager.addQuest(mockQuest, null);
        
        // Simulate enemy death event
        manager.onGameEvent(GameEvent.ENTITY_DEATH, "enemy_01", 1, mockState);
        
        assertTrue(manager.isQuestCompleted("q1"), "Quest should be in completed state.");
        verify(mockPlayer, times(1)).restoreQi(10.0);
        verify(mockQuest, atLeastOnce()).addProgress(1);
    }

    @Test
    @DisplayName("Unrelated event should not affect quest progress")
    public void testUnrelatedEvent() {
        QuestManager manager = new QuestManager();
        when(mockQuest.getId()).thenReturn("q1");
        when(mockQuest.getObjectiveType()).thenReturn(Quest.ObjectiveType.KILL);
        when(mockQuest.getTargetId()).thenReturn("target_01");
        
        manager.addQuest(mockQuest, null);
        
        // Event of different type (e.g., LEVEL_START instead of KILL)
        manager.onGameEvent(GameEvent.LEVEL_START, "any", 1, mockState);
        
        // Event for a different enemy
        manager.onGameEvent(GameEvent.ENTITY_DEATH, "wrong_enemy", 1, mockState);
        
        verify(mockQuest, never()).addProgress(anyInt());
    }

    @Test
    @DisplayName("Multiple quests tracking independently")
    public void testMultipleQuests() {
        QuestManager manager = new QuestManager();
        Quest q1 = mock(Quest.class);
        Quest q2 = mock(Quest.class);
        
        when(q1.getId()).thenReturn("q1");
        when(q1.getObjectiveType()).thenReturn(Quest.ObjectiveType.KILL);
        when(q1.getTargetId()).thenReturn("boss");
        
        when(q2.getId()).thenReturn("q2");
        when(q2.getObjectiveType()).thenReturn(Quest.ObjectiveType.COLLECT);
        when(q2.getTargetId()).thenReturn("item_herb");
        
        manager.addQuest(q1, null);
        manager.addQuest(q2, null);
        
        // Event for the first quest
        manager.onGameEvent(GameEvent.ENTITY_DEATH, "boss", 1, mockState);
        verify(q1, times(1)).addProgress(1);
        verify(q2, never()).addProgress(anyInt());
        
        // Event for the second quest
        manager.onGameEvent(GameEvent.ITEM_PICKUP, "item_herb", 1, mockState);
        verify(q2, times(1)).addProgress(1);
    }
}

