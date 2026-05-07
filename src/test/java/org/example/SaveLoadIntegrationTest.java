package org.example;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class SaveLoadIntegrationTest {

    @Test
    public void testSaveAndLoadConsistency() throws IOException {
        SaveData data = new SaveData();
        data.hp = 75.5;
        data.playerX = 120.0;
        data.worldFlags.put("test_flag", true);
        
        // Save to slot 5 (test slot)
        SaveManager.save(data, 5);
        
        // Try to load it immediately
        SaveData loaded = SaveManager.load(5);
        
        assertNotNull(loaded);
        assertEquals(75.5, loaded.hp);
        assertEquals(120.0, loaded.playerX);
        assertTrue(loaded.worldFlags.get("test_flag"));
        
        // Clean up after self so no files remain
        SaveManager.delete(5);
    }
}
