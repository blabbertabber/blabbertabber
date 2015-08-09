package com.blabbertabber.blabbertabber.tests;

import android.app.Application;
import android.test.ActivityInstrumentationTestCase2;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }
}

/*
public class Test1_SubmitTest extends ActivityInstrumentationTestCase2<ToDoManagerActivity> {

    private Solo solo;

    public Test1_SubmitTest() {
        super(ToDoManagerActivity.class);
    }

    protected void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    // Execute the SubmitTest
    public void testRun() {

    }
}
*/
