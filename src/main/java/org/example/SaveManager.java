package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;

/**
 * Manages saving and loading game state to 5 dedicated JSON slots.
 */
public class SaveManager {
    private static final String SAVE_DIR = "saves";
    /** 
     * Jackson ObjectMapper configured for indented output and lenient deserialization. 
     * Lenient mode (FAIL_ON_UNKNOWN_PROPERTIES=false) ensures that saves made in 
     * older versions remain loadable even if the SaveData structure evolves.
     */
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Saves game state to a specific slot (1-5).
     * 
     * @param data The game state to preserve.
     * @param slot The slot index (1-based).
     * @throws IOException If file I/O fails.
     */
    public static void save(SaveData data, int slot) throws IOException {
        validateSlot(slot);
        File file = new File(SAVE_DIR, "save_slot_" + slot + ".json");
        mapper.writeValue(file, data);
        GameLogger.info("Saved game state to slot " + slot);
    }

    /**
     * Loads game state from a specific slot (1-5).
     * 
     * @param slot The slot index (1-based).
     * @return The loaded SaveData, or null if loading fails.
     */
    public static SaveData load(int slot) {
        validateSlot(slot);
        File file = new File(SAVE_DIR, "save_slot_" + slot + ".json");
        if (!file.exists()) return null;

        try {
            return mapper.readValue(file, SaveData.class);
        } catch (IOException e) {
            System.err.println("Failed to load save slot " + slot);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Deletes a specific save slot.
     * 
     * @param slot The slot to clear.
     */
    public static void delete(int slot) {
        validateSlot(slot);
        File file = new File(SAVE_DIR, "save_slot_" + slot + ".json");
        if (file.exists()) {
            file.delete();
            GameLogger.info("Deleted save slot " + slot);
        }
    }

    /**
     * Checks if a save file exists for the given slot.
     * 
     * @param slot The slot to check.
     * @return true if a save file is present.
     */
    public static boolean exists(int slot) {
        validateSlot(slot);
        return new File(SAVE_DIR, "save_slot_" + slot + ".json").exists();
    }

    /** Helper to ensure slot is within 1-5. */
    private static void validateSlot(int slot) {
        if (slot < 1 || slot > 5) {
            throw new IllegalArgumentException("Save slot must be between 1 and 5.");
        }
    }
}
