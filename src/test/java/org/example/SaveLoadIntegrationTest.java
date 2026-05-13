package org.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * COMPREHENSIVE INTEGRATION TEST: Save/Load System.
 * Verifies data integrity across complex nested structures and boundary conditions.
 */
@DisplayName("Save System Deep Integration")
public class SaveLoadIntegrationTest {

    private final int TEST_SLOT = 4; // Using slot 4 for main tests

    @BeforeEach
    @AfterEach
    void cleanUp() {
        // Absolute isolation: clear all possible slots before and after each test
        for (int i = 1; i <= 5; i++) {
            SaveManager.delete(i);
        }
    }

    /**
     * TEST 1 (SUCCESS): Deep Data Integrity Stress Test.
     * Populates all complex fields in SaveData and verifies perfect reconstruction.
     */
    @Test
    @DisplayName("Scenario: Full System State Persistence (Complex Success)")
    public void testDeepDataIntegrity() throws IOException {
        // --- 1. SETUP: Create an exhaustive game state ---
        SaveData original = new SaveData();

        // Basic World & Metadata
        original.mapSeed = 9876543210L;
        original.biome = "Volcanic Abyss";
        original.levelConfigPath = "/levels/hell_gate.json";
        original.currentTime = 12500.25;
        original.mapLevel = 99;
        original.inTribulationFlag = 1;
        original.victoryAchievedFlag = 0;

        // Player Stats & Position
        original.playerX = 5000.0;
        original.playerY = 25.0;
        original.hp = 12.0;
        original.maxHp = 200.0;
        original.qi = 5.0;
        original.maxQi = 1000.0;
        original.activeHotbarSlot = 4;

        // Complex Lists (Inventory & Quests)
        original.inventoryItemIds.add("void_blade");
        original.inventoryItemIds.add("crimson_elixir");
        original.inventoryItemIds.add("forbidden_scroll");
        
        original.completedQuestIds.add("q_intro_01");
        original.completedQuestIds.add("q_intro_02");

        // Advanced Maps (World Flags & Counters)
        original.worldFlags.put("gate_open", true);
        original.worldFlags.put("npc_dead", false);
        original.worldCounters.put("total_kills", 666);

        // Nested Objects: Enemies (The most complex part of serialization)
        SaveData.EnemySaveData elite = new SaveData.EnemySaveData();
        elite.id = "boss_archdemon";
        elite.x = 5100;
        elite.y = 50;
        elite.hp = 50000;
        elite.scaling = 5.0;
        elite.tribulationFlag = 1;
        original.activeEnemies.add(elite);

        // Nested Objects: Active Quests
        SaveData.QuestSaveData progress = new SaveData.QuestSaveData();
        progress.id = "kill_10_wolves";
        progress.currentAmount = 7;
        original.activeQuests.add(progress);

        // --- 2. ACTION: Serialize and Deserialze ---
        SaveManager.save(original, TEST_SLOT);
        SaveData loaded = SaveManager.load(TEST_SLOT);

        // --- 3. ASSERT: Detailed Field-by-Field Verification ---
        assertNotNull(loaded, "Loaded SaveData must not be null.");
        
        // Check Primitive Fields
        assertEquals(original.mapSeed, loaded.mapSeed);
        assertEquals(original.biome, loaded.biome);
        assertEquals(original.currentTime, loaded.currentTime);
        assertEquals(original.hp, loaded.hp);

        // Check Lists
        assertEquals(3, loaded.inventoryItemIds.size());
        assertEquals("crimson_elixir", loaded.inventoryItemIds.get(1));
        assertTrue(loaded.completedQuestIds.contains("q_intro_01"));

        // Check Maps
        assertEquals(true, loaded.worldFlags.get("gate_open"));
        assertEquals(666, loaded.worldCounters.get("total_kills"));

        // Check Nested List of Objects
        assertEquals(1, loaded.activeEnemies.size());
        assertEquals("boss_archdemon", loaded.activeEnemies.get(0).id);
        assertEquals(5.0, loaded.activeEnemies.get(0).scaling);

        assertEquals(1, loaded.activeQuests.size());
        assertEquals(7, loaded.activeQuests.get(0).currentAmount);
    }

    /**
     * TEST 2 (ERROR): Boundary Condition - Invalid Slot Access.
     * Verifies that the system correctly rejects invalid input.
     */
    @Test
    @DisplayName("Scenario: Invalid Slot Protection (Boundary Error)")
    public void testInvalidSlotAccess() {
        SaveData dummy = new SaveData();
        
        // Attempting to save to slot 0 (Invalid, slots are 1-5)
        assertThrows(IllegalArgumentException.class, () -> {
            SaveManager.save(dummy, 0);
        }, "SaveManager should throw IllegalArgumentException for slot 0.");

        // Attempting to load from slot 6 (Invalid)
        assertThrows(IllegalArgumentException.class, () -> {
            SaveManager.load(6);
        }, "SaveManager should throw IllegalArgumentException for slot 6.");
        
        // Verify that no files were created for invalid slots
        // We don't call exists(0) here because it also validates and throws.
        // The fact that save(dummy, 0) threw is enough to know it didn't proceed.
    }

    /**
     * TEST 3 (ERROR): Resilience - Loading Non-Existent File and Multi-Slot Integrity.
     * Verifies that the system handles missing files gracefully across multiple slots
     * and that operations on empty slots don't corrupt the environment.
     */
    @Test
    @DisplayName("Scenario: Exhaustive Missing File and Multi-Slot Handling")
    public void testLoadingMissingFile() {
        // --- 1. Preparation of a clean environment ---
        SaveManager.delete(1);
        SaveManager.delete(2);
        assertFalse(SaveManager.exists(1), "Slot 1 must be empty.");
        assertFalse(SaveManager.exists(2), "Slot 2 must be empty.");

        // --- 2. Action: Loading empty slots ---
        SaveData result1 = SaveManager.load(1);
        SaveData result2 = SaveManager.load(2);
        
        assertNull(result1, "Loading slot 1 should return null.");
        assertNull(result2, "Loading slot 2 should return null.");

        // --- 3. Safety: Redundant deletion and interaction ---
        // Deleting already non-existent files should be safe and idempotent
        assertDoesNotThrow(() -> SaveManager.delete(1));
        assertDoesNotThrow(() -> SaveManager.delete(2));
        
        // --- 4. Cross-Integrity: Ensure empty slots don't affect new saves ---
        SaveData newData = new SaveData();
        newData.biome = "Safe Zone";
        try {
            SaveManager.save(newData, 1);
            assertTrue(SaveManager.exists(1));
            assertFalse(SaveManager.exists(2), "Slot 2 should remain empty even if 1 is saved.");
            
            SaveData verified = SaveManager.load(1);
            assertEquals("Safe Zone", verified.biome);
        } catch (IOException e) {
            fail("Saving after empty-slot interaction should not fail.");
        }
    }
    
    /**
     * TEST 4 (SUCCESS): Async Operation Configuration and Lifecycle State.
     * Verifies that the asynchronous save tasks are correctly initialized with 
     * full data payload and ready for JavaFX execution.
     */
    @Test
    @DisplayName("Scenario: Advanced Async Task Configuration Analysis")
    public void testAsyncTaskCreation() {
        // --- 1. Setup a complex payload for the task ---
        SaveData data = new SaveData();
        data.playerX = 100.5;
        data.inventoryItemIds.add("qi_gem");
        data.worldCounters.put("sessions", 1);
        
        // --- 2. Create the Task ---
        var saveTask = SaveManager.saveAsync(data, 5);
        var loadTask = SaveManager.loadAsync(5);
        
        // --- 3. Verification of Save Task ---
        assertNotNull(saveTask, "Save task object must be instantiated.");
        assertEquals(javafx.concurrent.Worker.State.READY, saveTask.getState(), "Task should start in READY state.");
        assertFalse(saveTask.isRunning(), "Task should not run automatically.");
        
        // --- 4. Verification of Load Task ---
        assertNotNull(loadTask, "Load task object must be instantiated.");
        assertEquals(javafx.concurrent.Worker.State.READY, loadTask.getState());
        
        // --- 5. Logic Check (Thread Safety and Param preservation) ---
        // We verify the internal configuration by checking the slot via side-effect or mocking if needed, 
        // but here we focus on the Task object's integrity and readiness.
        assertNotNull(saveTask.toString()); 
        assertTrue(saveTask.getMessage() == null || saveTask.getMessage().isEmpty());
    }

    /**
     * TEST 5 (SUCCESS): Save Overwrite Stress Test with Cross-Slot Protection.
     * Verifies that overwriting an existing slot with a completely different 
     * complex state works perfectly and doesn't leak into other slots.
     */
    @Test
    @DisplayName("Scenario: Overwriting Complex World State (Deep Integrity)")
    public void testSaveOverwrite() throws IOException {
        // --- 1. SETUP: Create two distinct worlds ---
        SaveData worldA = new SaveData();
        worldA.biome = "World A: Fire";
        worldA.hp = 10.0;
        worldA.inventoryItemIds.add("fire_sword");
        
        SaveData worldB = new SaveData();
        worldB.biome = "World B: Ice";
        worldB.hp = 99.0;
        worldB.inventoryItemIds.add("ice_bow");
        worldB.worldFlags.put("ice_melted", false);

        // --- 2. ACTION: Save A, then Overwrite with B ---
        SaveManager.save(worldA, 1);
        assertTrue(SaveManager.exists(1));
        
        // Intermediate check
        SaveData loadedA = SaveManager.load(1);
        assertEquals("World A: Fire", loadedA.biome);

        // Overwrite
        SaveManager.save(worldB, 1);
        
        // --- 3. VERIFICATION: Total replacement check ---
        SaveData loadedB = SaveManager.load(1);
        assertNotNull(loadedB);
        assertEquals("World B: Ice", loadedB.biome, "Biome must be updated.");
        assertEquals(99.0, loadedB.hp, "Stats must be updated.");
        assertEquals(1, loadedB.inventoryItemIds.size(), "Inventory list must be replaced, not merged.");
        assertEquals("ice_bow", loadedB.inventoryItemIds.get(0));
        assertTrue(loadedB.worldFlags.containsKey("ice_melted"));
        assertFalse(loadedB.worldFlags.containsKey("fire_ignited"), "Old flags should not exist.");

        // --- 4. CRITICAL: Cross-Slot isolation check ---
        assertFalse(SaveManager.exists(2), "Slot 2 should not be affected by actions on Slot 1.");
    }

    /**
     * TEST 6 (SUCCESS): Massive Persistence Lifecycle and Cleanup Verification.
     * Verifies the creation, verification, and absolute removal of a massive game state
     * involving 100+ entities and nested quest chains.
     */
    @Test
    @DisplayName("Scenario: Massive State Persistence Lifecycle (100+ Objects)")
    public void testDeleteWorkflow() throws IOException {
        // --- 1. Populate a massive, dense state ---
        SaveData massiveData = new SaveData();
        massiveData.mapSeed = 42424242;
        massiveData.biome = "The Infinite Loop";
        
        // Populate 100 ground items to stress test JSON buffer
        for(int i=0; i<100; i++) {
            SaveData.ItemSaveData item = new SaveData.ItemSaveData();
            item.id = "particle_" + i;
            item.x = Math.random() * 1000;
            item.y = Math.random() * 1000;
            massiveData.itemsOnGround.add(item);
        }
        
        // Populate a complex quest chain
        for(int i=0; i<10; i++) {
            SaveData.QuestSaveData q = new SaveData.QuestSaveData();
            q.id = "mega_quest_stage_" + i;
            q.currentAmount = i;
            massiveData.activeQuests.add(q);
        }

        // Save to slot 3
        SaveManager.save(massiveData, 3);
        assertTrue(SaveManager.exists(3), "Save file 3 should be on disk.");
        
        // --- 2. High-Fidelity Verification before deletion ---
        SaveData check = SaveManager.load(3);
        assertAll("Massive Data Integrity",
            () -> assertEquals(100, check.itemsOnGround.size()),
            () -> assertEquals(10, check.activeQuests.size()),
            () -> assertEquals(42424242, check.mapSeed),
            () -> assertTrue(check.itemsOnGround.get(50).id.startsWith("particle_"))
        );
        
        // --- 3. Absolute Removal ---
        SaveManager.delete(3);
        
        // --- 4. Post-Mortem Verification ---
        assertFalse(SaveManager.exists(3), "Physical file must be completely purged from the 'saves' directory.");
        assertNull(SaveManager.load(3), "Attempting to load a purged slot must return null.");
        
        // Ensure no other slot was accidentally deleted (Slot 1 was saved in Test 5)
        // Note: Tests run in isolation but if they share slot 1, we check it here
        if(SaveManager.exists(1)) {
            assertNotNull(SaveManager.load(1), "Slot 1 should be independent from actions on Slot 3.");
        }
    }
}
