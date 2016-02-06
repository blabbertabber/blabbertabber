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
import java.util.concurrent.ThreadLocalRandom;

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
    private static final String TAG = "AudioEventProcessor";
    private static final int RECORDER_AUDIO_SOURCE = BestMicrophone.getBestMicrophone();
    private static final int RECORDER_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int NUM_FRAMES = RECORDER_SAMPLE_RATE_IN_HZ / 5;  // 5 updates/second
    // size of the buffer array needs to be NUM_FRAMES * 2;
    // 1 channel (mono), 2 bytes per sample (PCM 16-bit)
    private static final int RECORDER_BUFFER_SIZE_IN_BYTES = NUM_FRAMES * 1 * 2;
    private static AudioRecordAbstract audioRecordWrapper;
    private static String rawFilePathName;
    public int numSpeakers;
    private Context context;
    private OutputStream rawFileOutputStream;
    private LocalBroadcastManager mBroadcastManager;
    /// TODO remove these variables.
    private int speaker;
    private long nextSpeakerChange;

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
        nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
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
        Log.i(TAG, "onPeriodicNotification(AudioRecord recorder)   readSize: " + readSize);
        short maxAmplitude = 0;

        // if readSize is negative, it most likely means that we have an ERROR_INVALID_OPERATION (-3)
        if (readSize > 0) {
            byte[] rawAudio = new byte[readSize * 2];
            // Performance: we must copy the PCM data into an array of bytes so that
            // we can write the RawDataOutputStream in one shot; otherwise it can take
            // 2x longer than the sample time to write (i.e. we drop 1/2 the sound)
            // if we foolishly use writeShort() instead
            for (int i = 0; i < readSize; ++i) {
                // http://developer.android.com/reference/android/media/AudioFormat.html
                // "...when the short is stored in a ByteBuffer, it is native endian (as compared to the default Java big endian)."
                // However the following lines seem to work both on ARM (big endian) and x86_64 emulator (little endian)
                rawAudio[i * 2] = (byte) (buffer[i] >> 8);
                rawAudio[i * 2 + 1] = (byte) buffer[i];
                if (buffer[i] > maxAmplitude) {
                    maxAmplitude = buffer[i];
                }
            }
            Log.v(TAG, "onPeriodicNotification() readsize: " + readSize + " maxAmplitude " + maxAmplitude);
            // write data out to raw file
            try {
                getRawFileOutputStream().write(rawAudio, 0, readSize * 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
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
        // send max volume to RecordingActivity
        /// Refactor speakerId
        sendVolume(getSpeakerId(), maxAmplitude);
    }

    public void onPeriodicNotificationEmulator() {
        Log.i(TAG, "onPeriodicNotificationEmulator()");
        // send max volume to RecordingActivity
        /// Refactor speakerId
        sendVolume(getSpeakerId(), ThreadLocalRandom.current().nextInt(0, 32768));
    }

    @Override
    public void onMarkerReached(AudioRecord recorder) {
    }

    /**
     * Utility method to broadcast the recorded volume.
     *
     * @param id     The id of the speaker speaking.
     * @param volume The volume.  0-32767 inclusive.
     */
    /// TODO: remove references to id.
    /// TODO: should we change volume range to signed short?
    public void sendVolume(int id, int volume) {
        Intent intent = new Intent(RECORD_RESULT);
        intent.putExtra(RECORD_MESSAGE, new int[]{id, volume});
        mBroadcastManager.sendBroadcast(intent);
    }

    // Who is currently speaking?
    /// TODO: Remove this method.
    public int getSpeakerId() {
        if (System.currentTimeMillis() > nextSpeakerChange) {
            speaker = nextSpeaker();
            nextSpeakerChange = System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(0, 10_000);
        }
        Log.v(TAG, "getSpeaker(): " + speaker + " nextSpeakerChange: " + (nextSpeakerChange - System.currentTimeMillis()));
        return speaker;
    }

    // usually returns a speaker different than the current speaker, possibly a new speaker
    /// TODO: remove this method.
    private int nextSpeaker() {
        if (newSpeaker()) {
            int nextSpeaker = numSpeakers;
            numSpeakers += 1;
            return (nextSpeaker);
        }
        // I am quite proud of the following line's elegance
        int newSpeaker = (speaker + ThreadLocalRandom.current().nextInt(0, numSpeakers)) % numSpeakers;
        Log.i(TAG, "nextSpeaker(): " + newSpeaker);
        return newSpeaker;
    }

    // Are we adding a completely new speaker who hasn't spoken yet?
    /// TODO: remove this method.
    private boolean newSpeaker() {
        double p = ((TheSpeakers.MAX_SPEAKERS - numSpeakers) / (TheSpeakers.MAX_SPEAKERS - 1.0));
        Log.i(TAG, "newSpeaker(): " + p);
        return p > ThreadLocalRandom.current().nextDouble();
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
        Log.i(TAG, "run()   rc == AudioRecord.SUCCESS: " + (rc == AudioRecord.SUCCESS));
        if (rc != AudioRecord.SUCCESS) {
            Log.wtf(TAG, "run()   audioRecordWrapper.setPositionNotificationPeriod(..) failed!  It returned " + rc);
        }

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
