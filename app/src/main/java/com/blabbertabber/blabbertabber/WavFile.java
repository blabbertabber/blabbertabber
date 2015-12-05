package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by brendancunnie on 11/29/15.
 */
public class WavFile  {
    private static final String TAG = "WavFile";
    public static int DEFAULT_SAMPLE_RATE = 16_000;
    File wavFile;

    private WavFile() {
    }

/*
    public static WavFile of (InputStream input) {

    }
*/

    public static WavFile of (File rawFile) throws IOException {
        WavFile wavFile = new WavFile();
        String wavFilePath = convertFilenameFromRawToWav(rawFile.getPath());
        wavFile.rawToWave(rawFile, new File(wavFilePath));
        return wavFile;
    }

    public static WavFile of (String rawFilepathname) throws IOException {
        return of(new File(rawFilepathname));
    }

    // Returns an appropriately named .wav file path.
    // TODO: Consider making this private or moving to a different class.
    public static String convertFilenameFromRawToWav(String filename) {
        filename = filename.replaceFirst("\\.raw$", "");
        return filename + ".wav";
    }

    private void rawToWave(final File rawFile, final File waveFile) throws IOException {
        wavFile = waveFile;

        byte[] rawData = new byte[(int) rawFile.length()];
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(rawFile));
            input.read(rawData);
        } finally {
            if (input != null) {
                input.close();
            }
        }

        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF"); // chunk id
            writeInt(output, 36 + rawData.length); // chunk size
            writeString(output, "WAVE"); // format
            writeString(output, "fmt "); // subchunk 1 id
            writeInt(output, 16); // subchunk 1 size
            writeShort(output, (short) 1); // audio format (1 = PCM)
            writeShort(output, (short) 1); // number of channels
            writeInt(output, DEFAULT_SAMPLE_RATE); // sample rate
            writeInt(output, DEFAULT_SAMPLE_RATE * 2); // byte rate
            writeShort(output, (short) 2); // block align
            writeShort(output, (short) 16); // bits per sample
            writeString(output, "data"); // subchunk 2 id
            writeInt(output, rawData.length); // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            short[] shorts = new short[rawData.length / 2];
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
            ByteBuffer bytes = ByteBuffer.allocate(shorts.length * 2);
            for (short s : shorts) {
                bytes.putShort(s);
            }
            output.write(bytes.array());
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

}
