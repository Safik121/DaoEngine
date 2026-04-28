package org.example;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A centralized logging utility for the DaoEngine.
 * Supports configurable logging levels and writes to a 'latest.log' file.
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
    private static PrintWriter fileWriter;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    static {
        try {
            // Initialize file logging
            fileWriter = new PrintWriter(new FileWriter("latest.log", false));
            info("Log file 'latest.log' initialized.");
        } catch (Exception e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }

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
        log(Level.INFO, message, null);
    }

    public static void warning(String message) {
        log(Level.WARNING, message, null);
    }

    public static void error(String message) {
        log(Level.ERROR, message, null);
    }

    public static void error(String message, Throwable t) {
        log(Level.ERROR, message, t);
    }

    private static void log(Level level, String message, Throwable t) {
        String timestamp = LocalDateTime.now().format(formatter);
        String formattedMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        // Console Output
        if (currentLevel.rank >= level.rank) {
            if (level == Level.ERROR) {
                System.err.println(formattedMessage);
            } else {
                System.out.println(formattedMessage);
            }
        }

        // File Output
        if (fileWriter != null) {
            fileWriter.println(formattedMessage);
            if (t != null) {
                t.printStackTrace(fileWriter);
            }
            fileWriter.flush();
        }

        // Optional: Print stack trace to console for errors
        if (t != null && level == Level.ERROR) {
            t.printStackTrace();
        }
    }
}
