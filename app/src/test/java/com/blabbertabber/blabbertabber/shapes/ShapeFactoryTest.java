package com.blabbertabber.blabbertabber.shapes;

import com.blabbertabber.blabbertabber.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.security.InvalidParameterException;

import math.geom2d.Box2D;

/**
 * Created by cunnie on 5/18/16.
 */

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ShapeFactoryTest {

    @Test(expected = InvalidParameterException.class)
    public void testZeroAreaEnclosingBoxThrowsException() {
        ShapeFactory.makeLines(new Box2D(0, 0, 0, 0));
    }
}
