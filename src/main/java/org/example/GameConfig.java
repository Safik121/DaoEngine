package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * POJO structure for the global game_config.json.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameConfig {
    public EngineConfig engine;
    public PlayerConfig player;
    public UIConfig ui;
    public BalanceConfig balance;

    public static class EngineConfig {
        public int width;
        public int height;
        public String title;
        public int fps = 60;
        public boolean fullscreen = false;
        public String loggingLevel = "INFO";
    }

    public static class PlayerConfig {
        public double initialMaxHp;
        public double initialMaxQi;
        public double baseSpeed;
        public double pickupRange;
        public double size;
        public java.util.List<String> startingItems;
    }

    public static class UIConfig {
        public String hpColor;
        public String qiColor;
        public String panelBgColor;
        public double panelOpacity;
        public double barWidth;
        public double barHeight;
        public String dialogueBoxColor;
        public String activeSlotHighlight;
    }

    public static class BalanceConfig {
        public double meditationHpRate;
        public double meditationQiRate;
        public double spiritVeinQiRate;
        public double lightningIntervalMin;
        public double lightningIntervalMax;
        public double lightningPlayerDamage;
        public double lightningEnemyDamage;
    }
}
