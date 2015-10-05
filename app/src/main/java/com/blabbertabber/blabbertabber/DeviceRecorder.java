package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import java.io.IOException;

/**
 * Created by cunnie on 10/3/15.
 *
 * Class that works with a REAL microphone (non-emulator)
 */
public class DeviceRecorder extends Recorder {
    private MediaRecorder mRecorder;

    public DeviceRecorder(Context context) {
        super(context);
    }

    @Override
    protected void startRecording() {
        Log.i(TAG, "startRecording()");
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(getBestMicrophone());
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile("/dev/null");
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        mRecorder.start();

    }

    @Override
    protected void stopRecording() {
        Log.i(TAG, "stopRecording()");
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    @Override
    public int getSpeakerVolume() {
        int volume = mRecorder.getMaxAmplitude();
        volume = volume * 100 / 32768;
        Log.i(TAG, "volume is " + volume);
        return volume;
    }

    // Different mics work better for different models
    // e.g. Nexus 5 == MIC; Nexus 6 == VOICE_RECOGNITION
    // http://stackoverflow.com/questions/1995439/get-android-phone-model-programmatically
    //                    NEXUS 6 MediaRecorder.AudioSource.
    // kinda works:       CAMCORDER
    //                    VOICE_RECOGNITION
    // terrible:          DEFAULT
    //                    MIC
    //                    VOICE_COMMUNICATION
    // RuntimeException:  VOICE_UPLINK
    //                    REMOTE_SUBMIX
    //                    VOICE_CALL
    //                    VOICE_DOWNLINK
    private int getBestMicrophone() {
        String model = Build.MODEL;
        Log.i(TAG, "getBestMicrophone() Build.MODEL == " + model);
        switch (model) {
            case "Nexus 4":  // Nexus 4
            case "Nexus 5":  // Nexus 5
                return (MediaRecorder.AudioSource.MIC);
            case "Nexus 6":
                return (MediaRecorder.AudioSource.VOICE_RECOGNITION);
            default:
                Log.wtf(TAG, "getBestMicrophone() FIXME new model: " + model);
                return (MediaRecorder.AudioSource.MIC);
        }

    }
}
