package com.blabbertabber.blabbertabber;

/**
 * Created by cunnie on 12/21/15.
 */
public class Timer {
    private long startTime;
    private long elapsedTime;
    private boolean started = false;
    private boolean running;

    public Timer() {
        reset();
    }

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

    public void reset() {
        started = false;
        running = false;
        startTime = 0;
        elapsedTime = 0;
    }

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

    public void stop() {
        if (running) {
            running = false;
            elapsedTime = System.currentTimeMillis() - startTime;
        }
    }
}
