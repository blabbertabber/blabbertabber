package com.blabbertabber.blabbertabber;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Created by cunnie on 11/18/15.
 */

@RunWith(AndroidJUnit4.class)
public class WavFileTest {

    @Test
    public void returnsCorrectWavFilename() {
        String rawFileName = "/dir1/dir2/file.raw";
        String wavFileName = WavFile.convertFilenameFromRawToWav(rawFileName);
        assertEquals("The .raw extension is replaced with the .wav extension.", "/dir1/dir2/file.wav", wavFileName);
    }

    @Test
    public void returnsAppendedWavExtension() {
        String fileName = "/dir1/dir2/file.txt";
        String wavFileName = WavFile.convertFilenameFromRawToWav(fileName);
        assertEquals("The .wav extension is added if the original file's extension is not .raw", "/dir1/dir2/file.txt.wav", wavFileName);
    }

    @Test
    public void returnsAppendedWavExtensionEvenWhenAlmostRaw() {
        String fileName = "/dir1/dir2/file.Xraw";
        String wavFileName = WavFile.convertFilenameFromRawToWav(fileName);
        assertEquals("The .wav extension is added even if the original file's extension is raw", "/dir1/dir2/file.Xraw.wav", wavFileName);
    }
}
