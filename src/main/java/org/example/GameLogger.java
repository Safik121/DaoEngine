package org.example;

/**
 * A centralized logging utility for the DaoEngine.
 * Supports configurable logging levels to filter output.
 */
public class GameLogger {

    public enum Level {
        NONE(0),
        ERROR(1),
        WARNING(2),
        INFO(3);

        final int rank;
        Level(int rank) { this.rank = rank; }
    }

    private static Level currentLevel = Level.INFO;

    /**
     * Initializes the logger with a specific level.
     * @param levelStr The level name (e.g. "INFO", "ERROR").
     */
    public static void initialize(String levelStr) {
        try {
            currentLevel = Level.valueOf(levelStr.toUpperCase());
            info("Logger initialized with level: " + currentLevel);
        } catch (Exception e) {
            currentLevel = Level.INFO;
            warning("Invalid logging level '" + levelStr + "'. Defaulting to INFO.");
        }
    }

    public static void info(String message) {
        if (currentLevel.rank >= Level.INFO.rank) {
            System.out.println("[INFO] " + message);
        }
    }

    public static void warning(String message) {
        if (currentLevel.rank >= Level.WARNING.rank) {
            System.out.println("[WARNING] " + message);
        }
    }

    public static void error(String message) {
        if (currentLevel.rank >= Level.ERROR.rank) {
            System.err.println("[ERROR] " + message);
        }
    }

    public static void error(String message, Throwable t) {
        if (currentLevel.rank >= Level.ERROR.rank) {
            System.err.println("[ERROR] " + message);
            t.printStackTrace();
        }
    }
}
