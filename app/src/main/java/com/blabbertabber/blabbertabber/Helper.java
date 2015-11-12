package com.blabbertabber.blabbertabber;

import android.util.Log;

/**
 * Created by cunnie on 11/11/15.
 * <p/>
 * Useful helper functions
 */
public class Helper {
    private static final String TAG = "Helper";

    /*
     Converts time to a string, e.g. "1:59:30.1"
     or "3.6" or "5:33.2".
     Rolled my own because JDK 7's DateFormat class seemed
     to require some unnatural contortions. JDK 8 has a much
     richer library.
*/
    public static String timeToHMS(long milliseconds) {
        Log.i(TAG, "timeToHMS(" + milliseconds + ")");

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
}
