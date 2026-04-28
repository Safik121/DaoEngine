package org.example.item;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of items, divided into a main inventory and a hotbar.
 */
public class Inventory {
    /** Number of slots in the main inventory grid. */
    private static final int MAIN_SLOTS = 25;
    /** Number of slots in the quick-access hotbar. */
    private static final int HOTBAR_SLOTS = 5;

    /** Array holding items in the main inventory. */
    private Item[] mainInventory;
    /** Array holding items in the hotbar. */
    private Item[] hotbar;

    /** Current input items for the crafting station. */
    private Item[] craftingInputs;

    /**
     * Initializes a new inventory with empty slots and no crafting result.
     */
    public Inventory() {
        mainInventory = new Item[MAIN_SLOTS];
        hotbar = new Item[HOTBAR_SLOTS];
        craftingInputs = new Item[2];
    }

    /**
     * Attempts to add an item to the first available slot.
     * Checks hotbar first, then main inventory.
     * 
     * @param item The item to add.
     * @return True if the item was added, false if the inventory is full.
     */
    public boolean addItem(Item item) {
        for (int i = 0; i < HOTBAR_SLOTS; i++) {
            if (hotbar[i] == null) {
                hotbar[i] = item;
                return true;
            }
        }
        for (int i = 0; i < MAIN_SLOTS; i++) {
            if (mainInventory[i] == null) {
                mainInventory[i] = item;
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves an item from the main inventory.
     * 
     * @param index Slot index.
     * @return The item at the index, or null if empty/out of bounds.
     */
    public Item getItemInMain(int index) {
        if (index >= 0 && index < MAIN_SLOTS)
            return mainInventory[index];
        return null;
    }

    /**
     * Retrieves an item from the hotbar.
     * 
     * @param index Hotbar index.
     * @return The item at the index, or null if empty/out of bounds.
     */
    public Item getItemInHotbar(int index) {
        if (index >= 0 && index < HOTBAR_SLOTS)
            return hotbar[index];
        return null;
    }

    /**
     * @return The total number of slots in the main inventory.
     */
    public int getMainSlotsCount() {
        return MAIN_SLOTS;
    }

    /**
     * @return The total number of slots in the hotbar.
     */
    public int getHotbarSlotsCount() {
        return HOTBAR_SLOTS;
    }

    /**
     * Swaps items between two slot arrays (including main inventory, hotbar, or crafting).
     * 
     * @param sourceArr Array containing the source item.
     * @param sourceIdx Index in the source array.
     * @param targetArr Array to move the item to.
     * @param targetIdx Index in the target array.
     */
    public void swapSlots(Item[] sourceArr, int sourceIdx, Item[] targetArr, int targetIdx) {
        Item temp = targetArr[targetIdx];
        targetArr[targetIdx] = sourceArr[sourceIdx];
        sourceArr[sourceIdx] = temp;
    }


    /**
     * Called when the player clicks on the crafting result.
     * Consumes inputs.
     */
    public void consumeCraftingInputs() {
        craftingInputs[0] = null;
        craftingInputs[1] = null;
    }

    /** @return The main inventory slot array. */
    public Item[] getMainInventory() {
        return mainInventory;
    }

    /** @return The hotbar slot array. */
    public Item[] getHotbar() {
        return hotbar;
    }

    /** @return The two crafting input slots. */
    public Item[] getCraftingInputs() {
        return craftingInputs;
    }

    /** @return The currently crafted item result, if any. */
    public Item getCraftingResult() {
        Item item1 = craftingInputs[0];
        Item item2 = craftingInputs[1];

        if (item1 == null || item2 == null) return null;

        // Re-evaluate on the fly to avoid "ghost results" when materials are removed
        String resultId = ItemRegistry.getRecipeResult(item1.getId(), item2.getId());
        if (resultId != null) {
            return ItemRegistry.createItem(resultId);
        }
        return null;
    }


    /**
     * Checks if the inventory contains an item with the given ID.
     * 
     * @param id The item ID to search for.
     * @return true if the item exists in main inventory or hotbar.
     */
    public boolean hasItem(String id) {
        return hasItem(id, 1);
    }

    /**
     * Consumes one instance of the given item ID if present.
     * @param id The item ID.
     * @return true if consumed, false if not found.
     */
    public boolean consumeItem(String id) {
        return removeItem(id, 1);
    }

    /**
     * Checks if the inventory contains a specific amount of an item ID.
     */
    public boolean hasItem(String id, int amount) {
        int count = 0;
        for (Item item : hotbar) {
            if (item != null && item.getId().equals(id)) count++;
        }
        for (Item item : mainInventory) {
            if (item != null && item.getId().equals(id)) count++;
        }
        return count >= amount;
    }

    /**
     * Removes a specific amount of items with the given ID.
     * Only performs removal if the full amount is available.
     */
    public boolean removeItem(String id, int amount) {
        if (!hasItem(id, amount)) return false;

        int leftToRemove = amount;
        
        // Start with hotbar
        for (int i = 0; i < HOTBAR_SLOTS && leftToRemove > 0; i++) {
            if (hotbar[i] != null && hotbar[i].getId().equals(id)) {
                hotbar[i] = null;
                leftToRemove--;
            }
        }
        
        // Then main inventory
        for (int i = 0; i < MAIN_SLOTS && leftToRemove > 0; i++) {
            if (mainInventory[i] != null && mainInventory[i].getId().equals(id)) {
                mainInventory[i] = null;
                leftToRemove--;
            }
        }
        
        return true;
    }
}
