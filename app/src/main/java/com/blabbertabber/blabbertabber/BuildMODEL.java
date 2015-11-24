package com.blabbertabber.blabbertabber;

import android.os.Build;

/**
 * Created by cunnie on 10/6/15.
 * <p/>
 * This is a class that wraps "Build.MODEL", which returns a String of the model phone we're running
 * on, e.g. "Nexus 5X".
 * <p/>
 * It's split out to its own class to enable testing via mock().
 * <p/>
 * Keep in mind that Mockito can't handle static methods, so avoid making its method static
 * unless you're ready to add JMock or somesuch.
 */
public class BuildMODEL {
    public String model() {
        return Build.MODEL;
    }
}