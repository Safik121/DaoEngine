package org.example.logic;

import org.example.logic.event.GameEvent;
import org.example.state.PlayState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

/**
 * Integration Test using Mockito to verify interaction between 
 * the Quest system and the Sound subsystem.
 */
@DisplayName("Subsystem Interaction: Quest <-> Sound")
public class SoundSystemIntegrationTest {

    @Test
    @DisplayName("Verify quest_complete sound triggers on completion")
    public void testQuestCompleteSoundTrigger() {
        // --- 1. SETUP ---
        SoundManager mockSound = mock(SoundManager.class);
        PlayState mockState = mock(PlayState.class);
        QuestManager questManager = new QuestManager();
        
        when(mockState.getSoundManager()).thenReturn(mockSound);
        
        Quest quest = new Quest("q1", "Test", "Desc", Quest.ObjectiveType.KILL, "target", 1);
        questManager.addQuest(quest, null);

        // --- 2. ACTION ---
        // Triggering an event that completes the quest
        questManager.onGameEvent(GameEvent.ENTITY_DEATH, "target", 1, mockState);

        // --- 3. ASSERT (Interaction Verification) ---
        // Verify that playSfx was called with the correct ID
        verify(mockSound, atLeastOnce()).playSfx("quest_complete");
    }
}
