package com.blabbertabber.blabbertabber;

import android.os.Build;

/**
 * Created by cunnie on 10/6/15.
 * <p/>
 * This is a class that wraps "Build.MODEL", which returns the type of phone we're running
 * on.
 * <p/>
 * It's split out to its own class to enable testing via mock().
 */
public class BuildMODEL {
    public String model() {
        return Build.MODEL;
    }
}
