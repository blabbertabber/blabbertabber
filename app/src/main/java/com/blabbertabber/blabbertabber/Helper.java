package com.blabbertabber.blabbertabber;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

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
//        Log.v(TAG, "timeToHMMSSm(" + milliseconds + ")");

        double seconds = (milliseconds % 60_000) / 1000.0;
        int minutes = (int) (milliseconds / 60_000) % 60;
        int hours = (int) (milliseconds / 3600_000);

        String hms;
        if (hours >= 1) {
            hms = String.format(Locale.getDefault(), "%d:%02d:%04.1f", hours, minutes, seconds);
        } else if (minutes >= 1) {
            hms = String.format(Locale.getDefault(), "%d:%04.1f", minutes, seconds);
        } else {
            hms = String.format(Locale.getDefault(), "%1.1f", seconds);
        }
        return hms;
    }

    //      HMMSS → Hours Minutes Seconds, H:MM:SS

    /**
     * Converts time to a string, e.g. "1:59:30"
     * or "3.6" or "5:33".
     *
     * @param milliseconds Time in milliseconds since start of meeting
     * @return String formatted time interval string in "H:MM:SS" format.
     */
    public static String timeToHMMSS(long milliseconds) {
        Log.v(TAG, "timeToHMMSS(" + milliseconds + ")");

        int seconds = (int) (milliseconds % 60_000) / 1000;
        int minutes = (int) (milliseconds / 60_000) % 60;
        int hours = (int) (milliseconds / 3600_000);

        String hms;
        if (hours >= 1) {
            hms = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        } else if (minutes >= 1) {
            hms = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        } else {
            hms = String.format(Locale.getDefault(), "%d", seconds);
        }
        return hms;
    }

    /**
     * Converts time to a string, with the minute and seconds always appearing.
     * E.g. "0:01" or "0:59" or 5:33" or "23:59:59".
     *
     * @param milliseconds Time in milliseconds since start of meeting
     * @return String formatted time interval string in "H:MM:SS" format.
     */
    public static String timeToHMMSSMinuteMandatory(long milliseconds) {
        Log.v(TAG, "timeToHMMSS(" + milliseconds + ")");

        int seconds = (int) (milliseconds % 60_000) / 1000;
        int minutes = (int) (milliseconds / 60_000) % 60;
        int hours = (int) (milliseconds / 3600_000);

        String hms = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
        if (hours >= 1) {
            hms = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        }
        return hms;
    }

    /**
     * Copies a file from res/raw to destination (typically context.getFilesDir())
     */
    public static void copyInputFileStreamToFilesystem(InputStream in, String outputFilePathName) throws IOException {
        Log.i(TAG, "copyInputFileStreamToFilesystem() outputFilePathName: " + outputFilePathName);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFilePathName));
        byte[] buffer = new byte[4096];
        int len = in.read(buffer);
        while (len != -1) {
            out.write(buffer, 0, len);
            len = in.read(buffer);
        }
        out.close();
    }

    /**
     * Calculates how fast a processor is. Result is the ratio of speech diarization to
     * to length of meeting, e.g. a Snapdragon 808 1.8 GHz hexa core 64-bit ARMv8-A takes
     * 36s to process a 300s meeting, which means its ratio is 8.33333 (i.e. 8.333 x faster
     * than real time).
     * <p/>
     * This number is fuzzy at best. For example, many processors are heterogenous (e.g
     * Snapdragon 808 has powerful ARM Cortex-A57 and weak ARM Cortex-A53 cores), so if
     * this benchmark is run on the fast core but later the processing is done on the slow
     * core, the progress bar will linger at 99% while the slow core trudges along. Throw
     * frequency-scaling into the mix, and you have a real crapshoot.
     * <p/>
     * (the value returned is used to display a progress bar)
     *
     * @return double
     */
    public static double howFastIsMyProcessor() {
        double goldenRatio = 65.0; // this has nothing to do with the Golden Ratio
        double junk = 1.0;

        // This test takes 533 - 555 ms to run on a Snapdragon 808
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 16_384; i++) {
            for (int j = 0; j < 1_024; j++) {
                junk = junk * 1.1;
            }
            for (int j = 0; j < 1_024; j++) {
                junk = junk / 1.1;
            }
        }
        long endTime = System.currentTimeMillis();

        long totalTime = endTime - startTime;
        return (double) totalTime / goldenRatio;
    }

    /**
     * Calculates the duration of a meeting based on the file's size in bytes
     *
     * @param fileSizeInBytes, typically new File(getFilesDir() + "/" + AudioEventProcessor.RECORDER_RAW_FILENAME).length()
     * @return double
     */
    public static double howLongWasMeetingInSeconds(long fileSizeInBytes) {
        double samplesPerSecond = AudioEventProcessor.RECORDER_SAMPLE_RATE_IN_HZ;
        double bytesPerSample = 2;
        return (double) fileSizeInBytes / (samplesPerSecond * bytesPerSample);
    }

    /**
     * Calculates how long diarization will take, in seconds.
     *
     * @param meetingLengthInSeconds double, meeting length in seconds
     * @param processorSpeed         double, processor speed, the ratio
     *                               of diarization speed to recording speed, typically set from howFastIsMyProcessor()
     * @return double, length in seconds
     */
    public static double howLongWillDiarizationTake(double meetingLengthInSeconds, double processorSpeed) {
        return meetingLengthInSeconds / processorSpeed;
    }
}
