package org.example.item;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global registry for items and crafting recipes.
 * Loads all static item data and valid combinations from JSON resources.
 */
public class ItemRegistry {
    private static Map<String, ItemConfig> items = new HashMap<>();
    private static List<RecipeConfig> recipesList = new ArrayList<>();
    private static Map<String, String> recipes = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Loads item and recipe data from the specified JSON paths.
     * 
     * @param itemsPath   Resource path to items.json
     * @param recipesPath Resource path to recipes.json
     */
    public static void loadData(String itemsPath, String recipesPath) {
        try {
            // Load Items
            InputStream isItems = ItemRegistry.class.getResourceAsStream(itemsPath);
            if (isItems != null) {
                List<ItemConfig> itemList = mapper.readValue(isItems, new TypeReference<List<ItemConfig>>() {});
                for (ItemConfig config : itemList) {
                    items.put(config.id, config);
                }
                System.out.println("Loaded " + items.size() + " items from JSON.");
            }

            // Load Recipes
            InputStream isRecipes = ItemRegistry.class.getResourceAsStream(recipesPath);
            if (isRecipes != null) {
                List<RecipeConfig> recipeList = mapper.readValue(isRecipes, new TypeReference<List<RecipeConfig>>() {});
                recipesList = recipeList;
                for (RecipeConfig recipe : recipeList) {
                    // Store as sorted pair string for easy lookup: "itemA+itemB" -> result
                    String key = getRecipeKey(recipe.input1, recipe.input2);
                    recipes.put(key, recipe.result);
                }
                System.out.println("Loaded " + recipes.size() + " crafting recipes from JSON.");
            }
        } catch (Exception e) {
            System.err.println("Fatal error loading ItemRegistry data!");
            e.printStackTrace();
        }
    }

    /**
     * Creates a fresh Item instance based on the registry configuration.
     * 
     * @param id The unique item identifier.
     * @return A new Item object, or null if ID not found.
     */
    public static Item createItem(String id) {
        ItemConfig config = items.get(id);
        if (config == null) return null;

        Item item;
        switch (config.type) {
            case WEAPON:
                item = new WeaponItem(config.id, config.name, config.description);
                break;
            case CONSUMABLE:
                item = new ConsumableItem(config.id, config.name, config.description);
                break;
            case CRAFTING:
            case MISC:
            default:
                item = new MaterialItem(config.id, config.name, config.description, config.type);
                break;
        }
        item.setHpRestore(config.hpRestore);
        item.setQiRestore(config.qiRestore);
        item.setMaxHpBoost(config.maxHpBoost);
        item.setMaxQiBoost(config.maxQiBoost);
        item.setWeaponConfigId(config.weaponConfigId);
        item.setSpriteId(config.spriteId);
        
        return item;
    }

    /**
     * Checks if two items can be combined into a new item.
     * 
     * @param id1 ID of the first item.
     * @param id2 ID of the second item.
     * @return ID of the resulting item, or null if no recipe exists.
     */
    public static String getRecipeResult(String id1, String id2) {
        return recipes.get(getRecipeKey(id1, id2));
    }

    /**
     * Helper to generate a consistent recipe key (alphabetical order).
     */
    private static String getRecipeKey(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + "+" + id2;
        } else {
            return id2 + "+" + id1;
        }
    }

    /** @return List of all loaded recipe configurations. */
    public static List<RecipeConfig> getAllRecipes() {
        return recipesList;
    }

    /** @return Map of all loaded item configurations. */
    public static Map<String, ItemConfig> getAllItems() {
        return items;
    }
}
