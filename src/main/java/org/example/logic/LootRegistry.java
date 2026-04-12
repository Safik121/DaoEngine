package org.example.logic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class LootRegistry {

    public static class LootEntry {
        public String itemId;
        public double chance;
    }

    private static Map<String, List<LootEntry>> lootTables = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Random random = new Random();

    public static void loadConfigs(String resourcePath) {
        try {
            InputStream is = LootRegistry.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Loot configuration file not found: " + resourcePath);
                return;
            }
            Map<String, List<LootEntry>> loaded = mapper.readValue(is, new TypeReference<Map<String, List<LootEntry>>>() {});
            lootTables = loaded;
            System.out.println("Loaded " + lootTables.size() + " loot tables.");
        } catch (Exception e) {
            System.err.println("Fatal error loading LootRegistry data!");
            e.printStackTrace();
        }
    }

    public static List<String> rollLoot(String enemyId) {
        List<String> drops = new ArrayList<>();
        List<LootEntry> table = lootTables.get(enemyId);
        if (table == null) return drops;

        for (LootEntry entry : table) {
            if (random.nextDouble() <= entry.chance) {
                drops.add(entry.itemId);
            }
        }
        return drops;
    }
}
