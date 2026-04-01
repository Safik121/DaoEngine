package org.example.item;

/**
 * Data structure representing a crafting recipe from JSON.
 * Links two input item IDs to one result item ID.
 */
public class RecipeConfig {
    /** ID of the first required material. */
    public String input1;
    /** ID of the second required material. */
    public String input2;
    /** ID of the resulting item produced by the combination. */
    public String result;
}
