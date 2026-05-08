package org.example.logic;

import org.example.GameLogger;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Registry that holds the entire progression path of cultivation realms and ranks.
 */
public class CultivationRegistry {
    private static List<CultivationRank> fullProgressionPath = new ArrayList<>();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static class RealmTemplate {
        public String realmName;
        public String description;
        public List<StageTemplate> stages;
    }

    public static class StageTemplate {
        public int tier;
        public double qi;
        public double hpBonus;
        public double strBonus;
        public double defBonus;
        public double spiBonus;
        public String requiredItemId;
        public Integer requiredItemCount;
    }

    /**
     * Loads cultivation data from a JSON resource.
     * @param resourcePath Path to cultivation_configs.json.
     */
    public static void loadConfigs(String resourcePath) {
        try {
            InputStream is = CultivationRegistry.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Cultivation configuration not found: " + resourcePath);
                return;
            }

            List<RealmTemplate> templates = mapper.readValue(is, new TypeReference<List<RealmTemplate>>() {});
            fullProgressionPath.clear();

            for (RealmTemplate realm : templates) {
                for (StageTemplate stage : realm.stages) {
                    fullProgressionPath.add(new CultivationRank(
                        realm.realmName,
                        stage.tier,
                        stage.qi,
                        stage.hpBonus,
                        stage.strBonus,
                        stage.defBonus,
                        stage.spiBonus,
                        realm.description,
                        stage.requiredItemId,
                        stage.requiredItemCount != null ? stage.requiredItemCount : 0
                    ));
                }
            }
            GameLogger.info("Loaded " + fullProgressionPath.size() + " cultivation stages.");
        } catch (Exception e) {
            System.err.println("Fatal error loading CultivationRegistry data!");
            e.printStackTrace();
        }
    }

    /** @return Defensive copy of the full cultivation path. */
    public static List<CultivationRank> getFullProgressionPath() {
        return new ArrayList<>(fullProgressionPath);
    }
}
