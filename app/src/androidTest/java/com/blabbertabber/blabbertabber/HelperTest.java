package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.core.StringContains;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Created by cunnie on 11/11/15.
 */

@RunWith(AndroidJUnit4.class)
public class HelperTest {
    private static final String TAG = "HelperTest";
    @Rule
    public final ExpectedException exception = ExpectedException.none();


    // HMMSS
    @Test
    public void testHMSZero_0Duration() {
        assertEquals("Zero-duration should return '0'", "0", Helper.timeToHMMSS(0));
    }

    @Test
    public void testHMSZero_1Duration() {
        assertEquals("50ms should return '0'", "0", Helper.timeToHMMSS(50));
    }

    @Test
    public void testHMSPoint1_0Duration() {
        assertEquals("51ms should return '0'", "0", Helper.timeToHMMSS(51));
    }

    @Test
    public void testHMSPoint1_1Duration() {
        assertEquals("149ms should return '0'", "0", Helper.timeToHMMSS(149));
    }

    @Test
    public void testHMSPoint59Duration() {
        assertEquals("59_900ms should return '59'", "59", Helper.timeToHMMSS(59_900));
    }

    @Test
    public void testHMSPoint1M_0Duration() {
        assertEquals("60_000ms should return '1:00'", "1:00", Helper.timeToHMMSS(60_000));
    }

    @Test
    public void testHMSPoint1M_1Duration() {
        assertEquals("60_001ms should return '1:00'", "1:00", Helper.timeToHMMSS(60_001));
    }

    @Test
    public void testHMSPoint1M_2Duration() {
        assertEquals("60_051ms should return '1:00'", "1:00", Helper.timeToHMMSS(60_051));
    }

    @Test
    public void testHMSPoint10M_0Duration() {
        assertEquals("599_949ms should return '9:59'", "9:59", Helper.timeToHMMSS(599_949));
    }

    @Test
    public void testHMSPoint10M_1Duration() {
        assertEquals("600_050ms should return '10:00'", "10:00", Helper.timeToHMMSS(600_050));
    }

    @Test
    public void testHMSPoint1H_0Duration() {
        assertEquals("3_599_949ms should return '59:59'", "59:59", Helper.timeToHMMSS(3_599_949));
    }

    @Test
    public void testHMSPoint1H_1Duration() {
        assertEquals("3_600_050ms should return '1:00:00'", "1:00:00", Helper.timeToHMMSS(3_600_050));
    }

    // HMMSSMinuteMandatory
    @Test
    public void testMinuteMandatoryHMSZero_0Duration() {
        assertEquals("Zero-duration should return '0:00'", "0:00", Helper.timeToHMMSSMinuteMandatory(0));
    }

    @Test
    public void testMinuteMandatoryHMSZero_1Duration() {
        assertEquals("50ms should return '0:00'", "0:00", Helper.timeToHMMSSMinuteMandatory(50));
    }

    @Test
    public void testMinuteMandatoryHMSPoint1_0Duration() {
        assertEquals("51ms should return '0:00'", "0:00", Helper.timeToHMMSSMinuteMandatory(51));
    }

    @Test
    public void testMinuteMandatoryHMSPoint1_1Duration() {
        assertEquals("149ms should return '0:00'", "0:00", Helper.timeToHMMSSMinuteMandatory(149));
    }

    @Test
    public void testMinuteMandatoryHMSPoint59Duration() {
        assertEquals("59_900ms should return '0:59'", "0:59", Helper.timeToHMMSSMinuteMandatory(59_900));
    }

    @Test
    public void testMinuteMandatoryHMSPoint1M_0Duration() {
        assertEquals("60_000ms should return '1:00'", "1:00", Helper.timeToHMMSSMinuteMandatory(60_000));
    }

    @Test
    public void testMinuteMandatoryHMSPoint1M_1Duration() {
        assertEquals("60_001ms should return '1:00'", "1:00", Helper.timeToHMMSSMinuteMandatory(60_001));
    }

    @Test
    public void testMinuteMandatoryHMSPoint1M_2Duration() {
        assertEquals("60_051ms should return '1:00'", "1:00", Helper.timeToHMMSSMinuteMandatory(60_051));
    }

    @Test
    public void testMinuteMandatoryHMSPoint10M_0Duration() {
        assertEquals("599_949ms should return '9:59'", "9:59", Helper.timeToHMMSSMinuteMandatory(599_949));
    }

    @Test
    public void testMinuteMandatoryHMSPoint10M_1Duration() {
        assertEquals("600_050ms should return '10:00'", "10:00", Helper.timeToHMMSSMinuteMandatory(600_050));
    }

    @Test
    public void testMinuteMandatoryHMSPoint1H_0Duration() {
        assertEquals("3_599_949ms should return '59:59'", "59:59", Helper.timeToHMMSSMinuteMandatory(3_599_949));
    }

    @Test
    public void testMinuteMandatoryHMSPoint1H_1Duration() {
        assertEquals("3_600_050ms should return '1:00:00'", "1:00:00", Helper.timeToHMMSSMinuteMandatory(3_600_050));
    }


    // HMMSSm
    @Test
    public void testZero_0Duration() {
        assertEquals("Zero-duration should return '0.0'", Helper.timeToHMMSSm(0), "0.0");
    }

    @Test
    public void testZero_1Duration() {
        assertEquals("50ms should return '0.0'", Helper.timeToHMMSSm(50), "0.0");
    }

    @Test
    public void testPoint1_0Duration() {
        assertEquals("51ms should return '0.1'", "0.1", Helper.timeToHMMSSm(51));
    }

    @Test
    public void testPoint1_1Duration() {
        assertEquals("149ms should return '0.1'", "0.1", Helper.timeToHMMSSm(149));
    }

    @Test
    public void testPoint59Duration() {
        assertEquals("59_900ms should return '59.9'", "59.9", Helper.timeToHMMSSm(59_900));
    }

    @Test
    public void testPoint1M_0Duration() {
        assertEquals("60_000ms should return '1:00.0'", "1:00.0", Helper.timeToHMMSSm(60_000));
    }

    @Test
    public void testPoint1M_1Duration() {
        assertEquals("60_001ms should return '1:00.0'", "1:00.0", Helper.timeToHMMSSm(60_001));
    }

    @Test
    public void testPoint1M_2Duration() {
        assertEquals("60_051ms should return '1:00.1'", "1:00.1", Helper.timeToHMMSSm(60_051));
    }

    @Test
    public void testPoint10M_0Duration() {
        assertEquals("599_949ms should return '9:59.9'", "9:59.9", Helper.timeToHMMSSm(599_949));
    }

    @Test
    public void testPoint10M_1Duration() {
        assertEquals("600_050ms should return '10:00.0'", "10:00.0", Helper.timeToHMMSSm(600_050));
    }

    @Test
    public void testPoint1H_0Duration() {
        assertEquals("3_599_949ms should return '59:59.9'", "59:59.9", Helper.timeToHMMSSm(3_599_949));
    }

    @Test
    public void testPoint1H_1Duration() {
        assertEquals("3_600_050ms should return '1:00:00.0'", "1:00:00.0", Helper.timeToHMMSSm(3_600_050));
    }

    // We only test error conditions; we cannot find a path that we can
    // successfully write to in our test environment
    // Once BlabberTabber has been run once
    // we can write to "/data/user/0/com.blabbertabber.blabbertabber/files/sphinx"
    @Test
    public void testCopyRawToFilesystem00() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("blah".getBytes(StandardCharsets.UTF_8));
        File file = new File("/some/nonexistent/directory/sphinx");

        exception.expect(IOException.class);
        exception.expectMessage(StringContains.containsString("ENOENT (No such file or directory)"));
        Helper.copyInputFileStreamToFilesystem(inputStream, file.getAbsolutePath());
    }

    @Test
    public void testCopyRawToFilesystem01() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("blah".getBytes(StandardCharsets.UTF_8));
        File file = new File("sphinx");

        exception.expect(IOException.class);
        exception.expectMessage(StringContains.containsString("open failed: EROFS (Read-only file system)"));
        Helper.copyInputFileStreamToFilesystem(inputStream, file.getAbsolutePath());
        assertEquals("the file is created", true, file.exists());
    }

    @Test
    public void testCopyRawToFilesystem02() throws Exception {
        InputStream inputStream = new ByteArrayInputStream("blah".getBytes(StandardCharsets.UTF_8));
        File file = new File("/data/local/tmp/sphinx");

        exception.expect(IOException.class);
        exception.expectMessage(StringContains.containsString("open failed: EACCES (Permission denied)"));
        Helper.copyInputFileStreamToFilesystem(inputStream, file.getAbsolutePath());
        assertEquals("the file is created", true, file.exists());
    }

    @Test
    public void testHowFastIsMyProcessor() {
        assertThat("My processor is fast enough", Helper.howFastIsMyProcessor(), greaterThan(2.0));
    }

    @Test
    public void testhowLongWasMeetingInSeconds00() {
        assertEquals("A 1-second meeting", 1.0, Helper.howLongWasMeetingInSeconds(32_000), 0.001);
    }

    @Test
    public void testhowLongWasMeetingInSeconds01() {
        assertEquals("A 2-second meeting", 2.0, Helper.howLongWasMeetingInSeconds(64_000), 0.001);
    }

    @Test
    public void testhowLongWillDiarizationTake00() {
        assertEquals("A 10-second meeting at 5x speed", 2.0, Helper.howLongWillDiarizationTake(10.0, 5.0), 0.001);
    }

    @Test
    public void testhowLongWillDiarizationTake01() {
        assertEquals("A 20-second meeting at 4x speed", 5.0, Helper.howLongWillDiarizationTake(20.0, 4.0), 0.001);
    }
}
