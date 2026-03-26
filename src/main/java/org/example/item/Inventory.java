package org.example.item;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages a collection of items, divided into a main inventory and a hotbar.
 */
public class Inventory {
    private static final int MAIN_SLOTS = 25;
    private static final int HOTBAR_SLOTS = 5;

    private Item[] mainInventory;
    private Item[] hotbar;

    // Crafting system: 2 inputs, 1 result
    private Item[] craftingInputs;
    private Item craftingResult;

    public Inventory() {
        mainInventory = new Item[MAIN_SLOTS];
        hotbar = new Item[HOTBAR_SLOTS];
        craftingInputs = new Item[2];
        craftingResult = null;
    }

    /**
     * Attempts to add an item to the first available slot.
     * Checks hotbar first, then main inventory.
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

    public Item getItemInMain(int index) {
        if (index >= 0 && index < MAIN_SLOTS)
            return mainInventory[index];
        return null;
    }

    public Item getItemInHotbar(int index) {
        if (index >= 0 && index < HOTBAR_SLOTS)
            return hotbar[index];
        return null;
    }

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

        // If swapping involved crafting slots, update result
        if (sourceArr == craftingInputs || targetArr == craftingInputs) {
            updateCraftingResult();
        }
    }

    /**
     * Updates the crafting result based on current inputs.
     * TODO: Load these from a configuration file in the future.
     */
    private void updateCraftingResult() {
        Item item1 = craftingInputs[0];
        Item item2 = craftingInputs[1];

        if (item1 == null || item2 == null) {
            craftingResult = null;
            return;
        }

        // Example hardcoded recipe: Rusty Sword (weapon) + Qi Pill (consumable) =
        // Improved Qi Sword
        if ((item1.getId().equals("sword_01") && item2.getId().equals("pill_01")) ||
                (item1.getId().equals("pill_01") && item2.getId().equals("sword_01"))) {
            craftingResult = new Item("sword_02", "Improved Qi Sword", "A sword infused with concentrated Qi.",
                    Item.Type.WEAPON);
        } else {
            craftingResult = null;
        }
    }

    /**
     * Called when the player clicks on the crafting result.
     * Consumes inputs and provides the result.
     */
    public void consumeCraftingInputs() {
        craftingInputs[0] = null;
        craftingInputs[1] = null;
        craftingResult = null;
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
        return craftingResult;
    }

    /** @param item Programmatically set the crafting result (for testing). */
    public void setCraftingResult(Item item) {
        this.craftingResult = item;
    }
}
