package org.example.logic;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.example.GameLogger;

/**
 * An independent timer running on a separate thread to manage the countdown
 * until a Tribulation Phase starts.
 * Complies with the Vision Document requirement for an asynchronous timer.
 */
public class TribulationTimer {

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "TribulationTimerThread");
        t.setDaemon(true);
        return t;
    });

    private double remainingSeconds;
    private final AtomicBoolean active = new AtomicBoolean(false);
    private Runnable onExpireCallback;

    public TribulationTimer(double durationSeconds, Runnable onExpire) {
        this.remainingSeconds = durationSeconds;
        this.onExpireCallback = onExpire;
    }

    /**
     * Starts the countdown in a background thread.
     */
    public void start() {
        if (active.getAndSet(true)) return;
        
        GameLogger.info("Starting independent Tribulation timer: " + remainingSeconds + "s");
        scheduler.scheduleAtFixedRate(() -> {
            if (!active.get()) return;

            remainingSeconds -= 1.0;
            if (remainingSeconds <= 0) {
                stop();
                if (onExpireCallback != null) {
                    onExpireCallback.run();
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * Pauses the timer logic.
     */
    public void pause() {
        active.set(false);
    }

    /**
     * Resumes the timer logic.
     */
    public void resume() {
        active.set(true);
    }

    /**
     * Resets the timer to a new duration.
     * Starts the timer if it wasn't already running.
     */
    public void reset(double durationSeconds) {
        this.remainingSeconds = durationSeconds;
        start();
    }

    /**
     * Stops the timer and shuts down the thread pool.
     */
    public void stop() {
        active.set(false);
        scheduler.shutdown();
    }

    public double getRemainingSeconds() {
        return Math.max(0, remainingSeconds);
    }
}
