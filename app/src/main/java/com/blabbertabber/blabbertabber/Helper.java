package com.blabbertabber.blabbertabber;

import android.util.Log;

/**
 * Useful helper functions
 */
public class Helper {
    private static final String TAG = "Helper";

    /**
     * Converts time to a string, e.g. "1:59:30.1"
     * or "3.6" or "5:33.2".
     * Rolled my own because JDK 7's DateFormat class seemed
     * to require some unnatural contortions. JDK 8 has a much
     * richer library.
     *
     * @param milliseconds Time in millseconds since start of meeting
     * @return String formatted time interval string in "H:MM:SS.m" format.
     */
    public static String timeToHMMSSm(long milliseconds) {
        Log.v(TAG, "timeToHMMSSm(" + milliseconds + ")");

        double seconds = (milliseconds % 60_000) / 1000.0;
        int minutes = (int) (milliseconds / 60_000) % 60;
        int hours = (int) (milliseconds / 3600_000);

        String hms;
        if (hours >= 1) {
            hms = String.format("%d:%02d:%04.1f", hours, minutes, seconds);
        } else if (minutes >= 1) {
            hms = String.format("%d:%04.1f", minutes, seconds);
        } else {
            hms = String.format("%1.1f", seconds);
        }
        return hms;
    }

    //      HMMSS → Hours Minutes Seconds, H:MM:SS

    /**
     * Converts time to a string, e.g. "1:59:30"
     * or "3.6" or "5:33".
     *
     * @param milliseconds Time in millseconds since start of meeting
     * @return String formatted time interval string in "H:MM:SS" format.
     */
    public static String timeToHMMSS(long milliseconds) {
        Log.v(TAG, "timeToHMMSS(" + milliseconds + ")");

        int seconds = (int) (milliseconds % 60_000) / 1000;
        int minutes = (int) (milliseconds / 60_000) % 60;
        int hours = (int) (milliseconds / 3600_000);

        String hms;
        if (hours >= 1) {
            hms = String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes >= 1) {
            hms = String.format("%d:%02d", minutes, seconds);
        } else {
            hms = String.format("%d", seconds);
        }
        return hms;
    }
}
