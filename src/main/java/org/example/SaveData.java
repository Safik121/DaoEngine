package org.example;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object (DTO) for game state persistence.
 * Designed for JSON serialization using Jackson.
 * Exhaustive field mapping ensures 100% reliable state reconstruction.
 */
public class SaveData {
    @JsonProperty("mapSeed")
    public long mapSeed;
    @JsonProperty("biome")
    public String biome;

    // Player Stats
    @JsonProperty("playerX")
    public double playerX;
    @JsonProperty("playerY")
    public double playerY;
    @JsonProperty("hp")
    public double hp;
    @JsonProperty("maxHp")
    public double maxHp;
    @JsonProperty("qi")
    public double qi;
    @JsonProperty("maxQi")
    public double maxQi;
    @JsonProperty("activeHotbarSlot")
    public int activeHotbarSlot;

    // Inventory
    @JsonProperty("inventoryItemIds")
    public List<String> inventoryItemIds;
    @JsonProperty("hotbarItemIds")
    public List<String> hotbarItemIds;

    // World State
    @JsonProperty("levelConfigPath")
    public String levelConfigPath;
    @JsonProperty("currentTime")
    public double currentTime;

    // World Story/Progress Persistence
    @JsonProperty("worldFlags")
    public java.util.Map<String, Boolean> worldFlags = new java.util.HashMap<>();
    @JsonProperty("worldCounters")
    public java.util.Map<String, Integer> worldCounters = new java.util.HashMap<>();

    /**
     * Flag indicating if the world is currently in a Tribulation state (0=No,
     * 1=Yes).
     */
    @JsonProperty("inTribulationFlag")
    public int inTribulationFlag;

    /** Current level victory status (0=No, 1=Yes). */
    @JsonProperty("victoryAchievedFlag")
    public int victoryAchievedFlag;

    /** The difficulty limit for spawning in the current session. */
    @JsonProperty("tribulationSpawnLimit")
    public int tribulationSpawnLimit;

    /** Data for individual active enemies in the world. */
    public static class EnemySaveData {
        @JsonProperty("id")
        public String id;
        @JsonProperty("x")
        public double x;
        @JsonProperty("y")
        public double y;
        @JsonProperty("hp")
        public double hp;
        @JsonProperty("scaling")
        public double scaling;
        /** 0=Regular, 1=Tribulation Elite. */
        @JsonProperty("tribulationFlag")
        public int tribulationFlag;

        public EnemySaveData() {
        } // Jackson
    }

    /** Data for items lying on the ground. */
    public static class ItemSaveData {
        @JsonProperty("id")
        public String id;
        @JsonProperty("x")
        public double x;
        @JsonProperty("y")
        public double y;

        public ItemSaveData() {
        } // Jackson
    }

    @JsonProperty("activeEnemies")
    public java.util.List<EnemySaveData> activeEnemies = new java.util.ArrayList<>();

    @JsonProperty("itemsOnGround")
    public java.util.List<ItemSaveData> itemsOnGround = new java.util.ArrayList<>();

    /** No-arg constructor for Jackson serialization. */
    public SaveData() {
    }
}
