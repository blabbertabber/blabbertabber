package com.blabbertabber.blabbertabber;

/**
 * Simple Timer class to track the length of meetings, and to pause when appropriate
 */
public class Timer {
    private long startTime;
    private long elapsedTime;
    private boolean started = false;
    private boolean running = false;

    public Timer() {
        reset();
    }

    public Timer(long elapsedTimeMillis) {
        reset();
        elapsedTime = elapsedTimeMillis;
        started = true;
    }

    /**
     * How much time has elapsed
     *
     * @return long the number of milliseconds elapsed, e.g. 5000 => 5 seconds
     */
    public long time() {
        if (started) {
            if (running) {
                return System.currentTimeMillis() - startTime;
            } else {
                return elapsedTime;
            }
        } else {
            return 0; // we haven't started yet
        }
    }

    /**
     * resets the timer. Time elapsed is back to 0; the timer is not running.
     */
    public void reset() {
        started = false;
        running = false;
        startTime = 0;
        elapsedTime = 0;
    }

    /**
     * begins the timer (clicking the start button on a stopwatch)
     */
    public void start() {
        if (!started) {
            started = true;
            running = true;
            startTime = System.currentTimeMillis();
        } else {
            if (!running) {
                running = true;
                startTime = System.currentTimeMillis() - elapsedTime;
            }
        }
    }

    /**
     * pauses the timer. The timer will no longer increment the elapsed amount of time
     * but won't reset it either. call start() to resume the incrementing of elapsed time.
     */
    public void stop() {
        if (running) {
            running = false;
            elapsedTime = System.currentTimeMillis() - startTime;
        }
    }
}
