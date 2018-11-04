package com.blabbertabber.blabbertabber;

import android.media.MediaRecorder;
import android.util.Log;

/**
 * Determines the best Microphone (MediaRecorder.AudioSource.XXX) to use
 */

public class BestMicrophone {
    private static final String TAG = "BestMicrophone()";
    // Different mics work better for different models
    // e.g. Nexus 5 == MIC; Nexus 6 == VOICE_RECOGNITION
    // http://stackoverflow.com/questions/1995439/get-android-phone-model-programmatically
    //
    // NEXUS 6            MediaRecorder.AudioSource.
    //
    // kinda works:       CAMCORDER
    //                    VOICE_RECOGNITION
    // terrible:          DEFAULT
    //                    MIC
    //                    VOICE_COMMUNICATION
    // RuntimeException:  VOICE_UPLINK
    //                    REMOTE_SUBMIX
    //                    VOICE_CALL
    //                    VOICE_DOWNLINK

    public static int getBestMicrophone() {
        return getBestMicrophone(new BuildMODEL().model());
    }

    // Determines the best Media.AudioSource microphone for the specific Android model on which
    // BlabberTabber is running.
    // @param buildModel The model of the android on which BlabberTabber is running.
    // @return The best microphone/AudioSource (e.g. MediaRecord.AudioSource.DEFAULT)
    public static int getBestMicrophone(String buildModelModel) {
        Log.v(TAG, "getBestMicrophone() Build.MODEL == " + buildModelModel);
        switch (buildModelModel) {
            case "Nexus 4":
            case "Nexus 5":
                return (MediaRecorder.AudioSource.MIC);
            case "Nexus 6":
                return (MediaRecorder.AudioSource.VOICE_RECOGNITION);
            case "Nexus 5X":
            default:
                Log.v(TAG, "getBestMicrophone() FIXME new model: " + buildModelModel);
                return (MediaRecorder.AudioSource.DEFAULT);
        }

    }
}
