package com.blabbertabber.blabbertabber;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by brendancunnie on 1/16/16.
 */
public class AudioEventProcessor implements Runnable, AudioRecord.OnRecordPositionUpdateListener {
    public static final String RECORD_STATUS = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_STATUS";
    public static final String RECORD_RESULT = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_RESULT";
    public static final String RECORD_STATUS_MESSAGE = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_STATUS_MESSAGE";
    public static final String RECORD_MESSAGE = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_MESSAGE";
    public static final int UNKNOWN_STATUS = -1;
    public static final int MICROPHONE_UNAVAILABLE = -2;
    public static final int CANT_WRITE_MEETING_FILE = -3;
    public static final String RECORDER_FILENAME_NO_EXTENSION = "meeting";
    public static final String RECORDER_RAW_FILENAME = RECORDER_FILENAME_NO_EXTENSION + ".raw";
    // http://developer.android.com/reference/android/media/AudioRecord.html
    // "44100Hz is currently the only rate that is guaranteed to work on all devices"
    // 16k samples/sec * 2 bytes/sample = 32kB/sec == 115.2 MB/hour
    public static final int RECORDER_SAMPLE_RATE_IN_HZ = 16_000;
    public static final int UPDATES_PER_SECOND = 5;
    public static final int NUM_FRAMES = RECORDER_SAMPLE_RATE_IN_HZ / UPDATES_PER_SECOND;
    private static final String TAG = "AudioEventProcessor";
    private static final int RECORDER_AUDIO_SOURCE = BestMicrophone.getBestMicrophone();
    private static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    // size of the buffer array needs to be NUM_FRAMES * 2;
    // 1 channel (mono), 2 bytes per sample (PCM 16-bit)
    private static final int RECORDER_BUFFER_SIZE_IN_BYTES = NUM_FRAMES * 1 * 2;
    private static AudioRecordAbstract audioRecordWrapper;
    private static String rawFilePathName;
    public int numSpeakers;
    private Context context;
    private OutputStream rawFileOutputStream;
    private LocalBroadcastManager mBroadcastManager;

    public AudioEventProcessor(Context context) {
        Log.i(TAG, "AudioEventProcessor(Context context)   context: " + context);
        this.context = context;
        File sharedDir = context.getFilesDir();
        rawFilePathName = sharedDir.getAbsolutePath() + "/" + "meeting.raw";
        try {
            rawFileOutputStream = context.openFileOutput("meeting.raw", Context.MODE_WORLD_WRITEABLE);
        } catch (FileNotFoundException e) {
            Log.wtf(TAG, "AudioEventProcessor()   context.openFileOutput(\"meeting.raw\", Context.MODE_WORLD_WRITEABLE) threw FileNotFoundException.");
            e.printStackTrace();
        }
        mBroadcastManager = LocalBroadcastManager.getInstance(context);
        /// TODO remove
        numSpeakers = 1;
    }

    public static String getRawFilePathName() {
        return rawFilePathName;
    }

    public synchronized static void newMeetingFile() {
        Log.i(TAG, "newMeetingFile()");
        File rawFile = new File(getRawFilePathName());
        if (rawFile.exists()) {
            rawFile.delete();
        }
    }

    private OutputStream getRawFileOutputStream() {
        if (rawFileOutputStream == null) {
            // open rawFileOutputStream
            try {
                rawFileOutputStream = context.openFileOutput("meeting.raw", Context.MODE_WORLD_WRITEABLE);
            } catch (FileNotFoundException e) {
                Log.wtf(TAG, "AudioEventProcessor()   context.openFileOutput(\"meeting.raw\", Context.MODE_WORLD_WRITEABLE) threw FileNotFoundException.");
                e.printStackTrace();
            }
        }
        return rawFileOutputStream;
    }

    @Override
    public void onPeriodicNotification(AudioRecord recorder) {
        Log.i(TAG, "onPeriodicNotification(AudioRecord recorder)");
        short buffer[] = new short[NUM_FRAMES];
        int readSize = recorder.read(buffer, 0, NUM_FRAMES);
        if (readSize > 0) {
            Log.i(TAG, "onPeriodicNotification(AudioRecord recorder)   readSize: " + readSize);
            onPeriodicNotification(buffer);
        } else {
            // if readSize is negative, it most likely means that we have an ERROR_INVALID_OPERATION (-3)
            Log.i(TAG, "onPeriodicNotification() NEGATIVE readsize: " + readSize);
            switch (readSize) {
                case AudioRecord.ERROR_BAD_VALUE:
                    Log.wtf(TAG, "onPeriodicNotification(..)   readSize == AudioRecord.ERROR_BAD_VALUE.  Denotes a failure due to the use of an invalid value.");
                    break;
                case AudioRecord.ERROR_INVALID_OPERATION:
                    Log.wtf(TAG, "onPeriodicNotification(..)   readSize == AudioRecord.ERROR_INVALID_OPERATION.  Denotes a failure due to the improper use of a method.");
                    break;
            }
        }
    }

    public void onPeriodicNotification(short[] buffer) {
        Log.i(TAG, "onPeriodicNotification()");
        short maxAmplitude = writeRawAndReturnMaxAmplitude(buffer);
        Log.v(TAG, "onPeriodicNotification(buffer) readsize: " + buffer.length + " maxAmplitude " + maxAmplitude);
        sendVolume(maxAmplitude);
    }

    private short writeRawAndReturnMaxAmplitude(short[] buffer) {
        short maxAmplitude = 0;
        byte[] rawAudio = new byte[buffer.length * 2];
        // Performance: we must copy the PCM data into an array of bytes so that
        // we can write the RawDataOutputStream in one shot; otherwise it can take
        // 2x longer than the sample time to write (i.e. we drop 1/2 the sound)
        // if we foolishly use writeShort() instead
        for (int i = 0; i < buffer.length; ++i) {
            // http://developer.android.com/reference/android/media/AudioFormat.html
            // "...when the short is stored in a ByteBuffer, it is native endian (as compared to the default Java big endian)."
            // However the following lines seem to work both on ARM (big endian) and x86_64 emulator (little endian)
            rawAudio[i * 2] = (byte) (buffer[i] >> 8);
            rawAudio[i * 2 + 1] = (byte) buffer[i];
            if (buffer[i] > maxAmplitude) {
                maxAmplitude = buffer[i];
            }
        }
        // write data out to raw file
        try {
            getRawFileOutputStream().write(rawAudio, 0, buffer.length * 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return maxAmplitude;
    }

    @Override
    public void onMarkerReached(AudioRecord recorder) {
    }

    /**
     * Utility method to broadcast the recorded volume.
     *
     * @param volume The volume.  0-32767 inclusive.
     */
    /// TODO: remove references to id.
    /// TODO: should we change volume range to signed short?
    public void sendVolume(int volume) {
        Intent intent = new Intent(RECORD_RESULT);
        intent.putExtra(RECORD_MESSAGE, new int[]{volume});
        mBroadcastManager.sendBroadcast(intent);
    }

    private AudioRecordAbstract createAudioRecord(int recorderAudioSource, int recorderSampleRateInHz, int recorderChannelConfig, int recorderAudioFormat, int recorderBufferSizeInBytes) {
        Log.i(TAG, "createAudioRecord()");
        // emulator crashes if attempts to use the actual microphone, so we simulate microphone in EmulatorRecorder
        return ("goldfish".equals(Build.HARDWARE) || "ranchu".equals(Build.HARDWARE)) ?
                new AudioRecordEmulator(recorderAudioSource, recorderSampleRateInHz, recorderChannelConfig, recorderAudioFormat, recorderBufferSizeInBytes) :
                AudioRecordReal.getInstance(recorderAudioSource, recorderSampleRateInHz, recorderChannelConfig, recorderAudioFormat, recorderBufferSizeInBytes);
    }

    @Override
    public void run() {
        Log.i(TAG, "run() STARTING Thread ID " + Thread.currentThread().getId());

        if (audioRecordWrapper != null) {
            audioRecordWrapper.stopAndRelease();
            audioRecordWrapper = null;
        }

        audioRecordWrapper = createAudioRecord(RECORDER_AUDIO_SOURCE, RECORDER_SAMPLE_RATE_IN_HZ,
                RECORDER_CHANNEL_CONFIG, RECORDER_AUDIO_FORMAT, RECORDER_BUFFER_SIZE_IN_BYTES);
        audioRecordWrapper.setRecordPositionUpdateListener(this);
        int rc = audioRecordWrapper.setPositionNotificationPeriod(NUM_FRAMES);
        Log.i(TAG, "run()   rc == AudioRecord.SUCCESS: " + (rc == AudioRecord.SUCCESS)
                + " audioRecordWrapper: " + audioRecordWrapper);
        if (rc != AudioRecord.SUCCESS) {
            Log.wtf(TAG, "run()   audioRecordWrapper.setPositionNotificationPeriod(..) failed!  It returned " + rc);
        }

        Log.i(TAG, "run() audioRecordWrapper: " + audioRecordWrapper);
        audioRecordWrapper.startRecording();

        boolean oldRecordingServiceRecording = RecordingService.recording;
        Log.i(TAG, "run()   RecordingService.recording: " + RecordingService.recording + "   oldRecordingServiceRecording: " + oldRecordingServiceRecording);
        while (true) {
            if (RecordingService.reset) {
                // close and re-open the meeting.wav file.
                try {
                    rawFileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                rawFileOutputStream = null;
                getRawFileOutputStream();
                RecordingService.reset = false;
            }
            if (RecordingService.recording != oldRecordingServiceRecording) {
                Log.i(TAG, "run()   RecordingService.recording: " + RecordingService.recording + "   oldRecordingServiceRecording: " + oldRecordingServiceRecording);
                if (!RecordingService.recording) {
                    // We're not recording, stop
                    audioRecordWrapper.stopAndRelease();
                } else {
                    Log.i(TAG, "run()   About to call audioRecordWrapper.startRecording()");
                    audioRecordWrapper.startRecording();
                }
                oldRecordingServiceRecording = RecordingService.recording;
            }

            try {
                Thread.currentThread().sleep(200);
            } catch (InterruptedException e) {
                Log.i(TAG, "run()   Huh.  InterruptedException thrown while sleep()ing.");
                e.printStackTrace();
            }
        }
    }
}
