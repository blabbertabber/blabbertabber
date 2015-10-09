package com.blabbertabber.blabbertabber;

import android.media.MediaRecorder;

/**
 * Created by cunnie on 10/8/15.
 * http://www.javaworld.com/article/2073352/core-java/simply-singleton.html
 */

public class MediaRecorderSingleton extends MediaRecorder {
    public final static MediaRecorderSingleton INSTANCE = (MediaRecorderSingleton) new MediaRecorder();
    private MediaRecorderSingleton() {
    }
}