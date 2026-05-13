package org.example.logic;

import org.example.SaveData;
import org.example.SaveManager;
import org.example.entity.Player;
import org.example.item.Item;
import org.example.item.MaterialItem;
import org.example.logic.event.EventManager;
import org.example.logic.event.GameEvent;
import org.example.state.PlayState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.ArrayList;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Ultimate Integration Test following the iterative Action-Assert pattern.
 * Verifies continuous state changes from game event to persistent disk storage.
 */
@DisplayName("Ultimate Quest & Persistence Flow")
public class QuestRewardFlowTest {

    @Test
    @DisplayName("Process: Progress -> Completion -> Reward -> Save")
    public void testFullQuestLifecycleToPersistence() throws IOException {
        // --- 1. SETUP ---
        EventManager eventManager = new EventManager();
        QuestManager questManager = new QuestManager();
        eventManager.subscribe(GameEvent.ENTITY_DEATH, questManager);

        Player player = new Player(0, 0);
        player.setMaxQi(1000); // Allow high rewards
        player.setQi(0); // Ensure starting state is 0 Qi

        Quest quest = new Quest("q_ultimate", "The Final Step", "Desc", 
                              Quest.ObjectiveType.KILL, "boss_id", 1);
        quest.setRewardQi(500.0);
        questManager.addQuest(quest, null);

        PlayState mockState = mock(PlayState.class);
        SoundManager mockSound = mock(SoundManager.class);
        when(mockState.getPlayer()).thenReturn(player);
        when(mockState.getSoundManager()).thenReturn(mockSound);

        // CHECKPOINT 0: Initial State
        assertEquals(0, player.getQi(), "Player should start with 0 Qi.");
        assertFalse(quest.isCompleted(), "Quest should start as incomplete.");

        // --- STEP A: Trigger Quest Completion ---
        eventManager.triggerEvent(GameEvent.ENTITY_DEATH, "boss_id", 1, mockState);
        
        // CHECKPOINT 1: Memory State after Event
        assertTrue(quest.isCompleted(), "Quest should be completed after the event.");
        assertEquals(500.0, player.getQi(), "Qi reward should be added to the player in memory.");

        // --- STEP B: Inventory Interaction ---
        Item rewardItem = new MaterialItem("legendary_sword", "Dao Breaker", "Ancient relic", Item.Type.MISC);
        player.getInventory().addItem(rewardItem);
        
        // CHECKPOINT 2: Inventory state
        assertTrue(player.getInventory().hasItem("legendary_sword"), "Reward item should be present in player's inventory.");

        // --- STEP C: Persistence (Saving to Disk) ---
        SaveData data = new SaveData();
        data.qi = player.getQi();
        data.inventoryItemIds = new ArrayList<>();
        data.inventoryItemIds.add(rewardItem.getId());
        data.completedQuestIds.add(quest.getId());
        
        SaveManager.save(data, 1);
        
        // --- STEP D: Loading & Verification ---
        SaveData loaded = SaveManager.load(1);
        
        // FINAL CHECKPOINT: Disk Persistence
        assertNotNull(loaded, "Save file should be valid on disk.");
        assertAll("Disk Data Integrity",
            () -> assertEquals(500.0, loaded.qi, "Qi value on disk must match the rewarded amount."),
            () -> assertTrue(loaded.inventoryItemIds.contains("legendary_sword"), "Inventory on disk must contain the reward item."),
            () -> assertTrue(loaded.completedQuestIds.contains("q_ultimate"), "Quest completion record must be preserved on disk.")
        );

        // Cleanup
        SaveManager.delete(1);
    }
}
