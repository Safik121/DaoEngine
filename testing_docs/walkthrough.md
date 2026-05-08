# DaoEngine Testing Walkthrough

This document provides a detailed explanation of the test suite implemented for the **DaoEngine** project. It covers the logic, values, and testing techniques used to ensure the quality and reliability of the engine.

## 1. Input Validation (SaveManager)
**File**: [SaveManagerInputTest.java](file:///a:/IDEA%20Projects/DaoEngine/src/test/java/org/example/SaveManagerInputTest.java)

### Techniques:
*   **Equivalence Partitioning (EC)**: Divided slots into valid {1, 2, 3, 4, 5} and invalid {0, 6, -1, 100}.
*   **Boundary Value Analysis (BVA)**: Specifically tested the edges: 1 (min), 5 (max), and 0/6 (invalid neighbors).
*   **Negative Testing**: Passing `null` to `save()` to ensure the system handles invalid data gracefully by throwing an `IllegalArgumentException`.

---

## 2. Unit Testing (RPG Logic)
**File**: [AttributeSetTest.java](file:///a:/IDEA%20Projects/DaoEngine/src/test/java/org/example/logic/AttributeSetTest.java)

### Key Scenarios:
*   **Minimum Damage**: Ensures that even if defense is higher than attack power, the entity takes at least 1 damage (prevents unintended invincibility).
*   **Stat Clamping**:
    *   HP cannot exceed `maxHp` during healing.
    *   HP cannot drop below 0 (no negative HP).
    *   Lowering `maxHp` automatically clips current `hp` to the new limit.
*   **Parameterized Tests**: Used `@CsvSource` to run multiple damage/defense combinations through a single test method for efficiency.

---

## 3. Mocking & Isolation (Mockito)
**File**: [QuestManagerTest.java](file:///a:/IDEA%20Projects/DaoEngine/src/test/java/org/example/logic/QuestManagerTest.java)

### Why Mockito?
*   **Isolation**: We test the `QuestManager` logic without starting the JavaFX renderer or loading real JSON configs.
*   **Behavior Verification**: We use `verify()` to check if the manager correctly sends notifications to the UI (e.g., `verify(mockState).addNotification(...)`).
*   **Stubbing**: We "stub" the `Quest` object to return specific values (like ID or Reward) to test how the manager processes them.

---

## 4. Integration & Process Testing (TDL 2)
These tests verify that different modules (Managers, Events, Entities) work together correctly.

### Process: Master Combat Lifecycle Chain
**File**: [CombatFlowIntegrationTest.java](file:///a:/IDEA%20Projects/DaoEngine/src/test/java/org/example/logic/CombatFlowIntegrationTest.java)
*   **Complete Diagram Mapping**: This single test method follows the **entire lethal path** (P5) of the Mermaid diagram from `test_scenarios.md`.
*   **Step-by-Step Assertions**:
    1.  **D1 (Hit)**: Verifies `SoundManager` and `ParticleManager` triggers.
    2.  **D2/D3 (Damage)**: Verifies the mathematical accuracy of `Crit Multiplier` combined with `Resistance Mitigation`.
    3.  **D4 (Effect)**: Verifies that the `StatusEffectManager` correctly receives the secondary effect.
    4.  **H (Attributes)**: Verifies the `AttributeSet` health reduction.
    5.  **D5/I (Death/Event)**: Verifies the entity's death flag and the subsequent triggering of the global `ENTITY_DEATH` event.
*   **Total Integration**: Demonstrates how a single action cascades through the entire engine stack in a synchronized flow.

### Process: Total Game State Integration
**File**: [QuestRewardFlowTest.java](file:///a:/IDEA%20Projects/DaoEngine/src/test/java/org/example/logic/QuestRewardFlowTest.java)
*   **Total Cycle**: Covers the entire loop from a world event to persistent storage.
*   **Flow**: `Event` -> `Quest Manager` -> `Inventory System` -> `Player Stats` -> `Save/Load Manager`.
*   Verifies that rewards (both **currency/Qi** and **inventory items**) given by the quest system are correctly propagated and successfully **saved to the disk** in a JSON slot.



### Other Integration Tests
*   **Quest Progress**: [QuestIntegrationTest.java](file:///a:/IDEA%20Projects/DaoEngine/src/test/java/org/example/logic/QuestIntegrationTest.java) – Base integration of events and quest progress.
*   **Combat Strike**: [CombatIntegrationTest.java](file:///a:/IDEA%20Projects/DaoEngine/src/test/java/org/example/logic/CombatIntegrationTest.java) – Multiple entity damage verification.


---

## 5. Persistence & Data Integrity
**File**: [SaveLoadIntegrationTest.java](file:///a:/IDEA%20Projects/DaoEngine/src/test/java/org/example/SaveLoadIntegrationTest.java)

*   **Round-trip Test**: Data is created -> Saved to disk -> Loaded back -> Compared.
*   Ensures that complex maps like `worldFlags` and floating-point values are preserved exactly during JSON serialization.

---

## Summary for Defense
*   **Risks covered**: Attribute calculation (R01), Save/Load reliability (R02), and Quest progression (R03).
*   **Architecture**: Tests follow the modular structure, using mocks where dependencies are too complex to initialize.
*   **Professionalism**: All comments and display names are in English, following industry standards.
