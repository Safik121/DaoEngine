package org.example.logic;

import org.example.GameLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for loading and caching quest templates from JSON.
 */
public class QuestRegistry {
    private static Map<String, QuestConfig> questTemplates = new HashMap<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static class QuestConfig {
        public String id;
        public String name;
        public String description;
        public Quest.ObjectiveType objectiveType;
        public String targetId;
        public int requiredAmount;
        public java.util.List<String> rewardItemIds;
        public double rewardQi;
        public String rewardSkillId;
    }

    public static void loadQuests(String resourcePath) {
        try {
            InputStream is = QuestRegistry.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Quest configuration file not found: " + resourcePath);
                return;
            }
            questTemplates = mapper.readValue(is, new TypeReference<Map<String, QuestConfig>>() {});
            org.example.GameLogger.info("Loaded " + questTemplates.size() + " quests.");
        } catch (Exception e) {
            System.err.println("Fatal error loading QuestRegistry data!");
            e.printStackTrace();
        }
    }

    public static Quest createQuest(String id) {
        QuestConfig config = questTemplates.get(id);
        if (config == null) return null;

        Quest q = new Quest(config.id, config.name, config.description, config.objectiveType, config.targetId, config.requiredAmount);
        q.setRewardQi(config.rewardQi);
        if (config.rewardItemIds != null) {
            for (String itemId : config.rewardItemIds) {
                q.addRewardItem(itemId);
            }
        }
        q.setRewardSkillId(config.rewardSkillId);
        return q;
    }
}
