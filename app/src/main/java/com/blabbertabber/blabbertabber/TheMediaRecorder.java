package com.blabbertabber.blabbertabber;

import android.media.MediaRecorder;
import android.util.Log;

/**
 * Created by Cunnie on 10/8/15.
 * http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
 */

// BUGS: uses 'static' which makes testing much more difficult, for example we don't test that
//    release() calls MediaRecorder.reset() instead of MediaRecorder.release();

public class TheMediaRecorder extends MediaRecorder {
    private static final String TAG = "TheMediaRecorder";
    public static TheMediaRecorder singleton;

    protected TheMediaRecorder() {
        // Exists only to defeat instantiation.
        super();
    }

    public synchronized static TheMediaRecorder getInstance() {
        if (singleton == null) {
            singleton = new TheMediaRecorder();
        }
        return singleton;
    }

    @Override
    public void setAudioSource(int audioSource) {
        // Our application has a race condition: .setAudioSource() may occur 84 - 86ms BEFORE
        // .release(), which generates an IllegalStateException.
        // To guard against that we attempt .setAudioSource 5 x at 50ms intervals until we succeed,
        // then give up
        for (int i = 0; i < 5; i++) {
            try {
                super.setAudioSource(audioSource);
                return;
            } catch (IllegalStateException e) {
                Log.wtf(TAG, "MediaRecorder.setAudioSource() IllegalStateException, retrying in 50ms");
                // placate Android Studio who always wants to wrap sleep() in try/catch
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        throw new IllegalStateException(TAG + " setAudioSource() wasn't able to succeed");
    }

    // TODO: Figure out if we want to call release() if the activity is paused
    // "In particular, whenever an Activity of an application is paused (its onPause() method is
    // called), or stopped (its onStop() method is called), this method should be invoked to release
    // the MediaRecorder object"
    // http://developer.android.com/reference/android/media/MediaRecorder.html#release%28%29

    @Override
    public void release() {
        // We don't want to release the object once we have it because that would render our
        // Singleton useless; instead we reset() it which will allow new calls to the object
        reset();
    }

    @Override
    public void stop() {
        // TODO: (when we record to a file instead of /dev/null)
        // "clean up the output file (delete the output file, for instance), since the output
        // file is not properly constructed when this happens."
        try {
            super.stop();
        } catch (RuntimeException e) {
            Log.wtf(TAG, "MediaRecorder.stop() Runtime Exception");
        }
    }
}
