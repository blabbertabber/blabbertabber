package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

/**
 * Created by cunnie on 10/3/15.
 */
public class DeviceRecorder extends Recorder {
    private MediaRecorder mRecorder;

    public DeviceRecorder(Context context) {
        super(context);
    }

    @Override
    protected void startRecording() {
        mRecorder = new MediaRecorder();
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
        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
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
}
