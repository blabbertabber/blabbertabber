package com.blabbertabber.blabbertabber;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Records audio to a file; is meant to run as a thread, call by RecordingService
 * uses AudioEventProcessor to handle the audio.
 */
public class AudioEventProcessor implements Runnable {
    public static final String RECORD_STATUS = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_STATUS";
    public static final String RECORD_RESULT = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_RESULT";
    public static final String RECORD_STATUS_MESSAGE = "com.blabbertabber.blabbertabber.AudioEventProcessor.RECORD_STATUS_MESSAGE";
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
    private static final int RECORDER_BUFFER_SIZE_IN_BYTES = NUM_FRAMES * 2;
    private static final int RECORDER_NOTIFICATION_ID = 19937;   // Unique id for notifications
    private static AudioRecordAbstract audioRecordWrapper;
    private static String rawFilePathName;
    NotificationManager mNotificationManager;
    private Context context;
    private OutputStream rawFileOutputStream;

    public AudioEventProcessor(Context context) {
        Log.i(TAG, "AudioEventProcessor(Context context)   context: " + context);
        this.context = context;
        File sharedDir = context.getFilesDir();
        rawFilePathName = sharedDir.getAbsolutePath() + "/" + "meeting.raw";
        try {
            rawFileOutputStream = context.openFileOutput("meeting.raw", Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.wtf(TAG, "AudioEventProcessor()   context.openFileOutput(\"meeting.raw\", Context.MODE_WORLD_WRITEABLE) threw FileNotFoundException.");
            e.printStackTrace();
        }
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static String getRawFilePathName() {
        return rawFilePathName;
    }

    public synchronized static void newMeetingFile() {
        Log.i(TAG, "newMeetingFile()");
        File rawFile = new File(getRawFilePathName());
        if (rawFile.exists()) {
            if (!rawFile.delete()) {
                Log.e(TAG, "newMeetingFile() failed to delete " + rawFile.getAbsolutePath());
            }
        }
    }

    private OutputStream getRawFileOutputStream() {
        if (rawFileOutputStream == null) {
            // open rawFileOutputStream
            try {
                rawFileOutputStream = context.openFileOutput("meeting.raw", Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                Log.wtf(TAG, "AudioEventProcessor()   context.openFileOutput(\"meeting.raw\", Context.MODE_WORLD_WRITEABLE) threw FileNotFoundException.");
                e.printStackTrace();
            }
        }
        return rawFileOutputStream;
    }

    private void writeRaw(short[] buffer) {
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
        }
        // write data out to raw file
        try {
            getRawFileOutputStream().write(rawAudio, 0, buffer.length * 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Log.i(TAG, "run() audioRecordWrapper: " + audioRecordWrapper);
        audioRecordWrapper.startRecording();
        notificationBar(true);

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
                    notificationBar(false);
                } else {
                    Log.i(TAG, "run()   About to call audioRecordWrapper.startRecording()");
                    audioRecordWrapper.startRecording();
                    notificationBar(true);
                }
                oldRecordingServiceRecording = RecordingService.recording;
            }
            if (RecordingService.recording) {
                short[] buffer = new short[NUM_FRAMES];
                int readSize = audioRecordWrapper.read(buffer, 0, NUM_FRAMES);
                if (readSize > 0) {
                    Log.v(TAG, "run() readSize: " + readSize);
                    writeRaw(buffer);
                } else {
                    // if readSize is negative, it most likely means that we have an ERROR_INVALID_OPERATION (-3)
                    Log.v(TAG, "run() NEGATIVE readsize: " + readSize);
                    switch (readSize) {
                        case AudioRecord.ERROR_BAD_VALUE:
                            Log.wtf(TAG, "run()   readSize == AudioRecord.ERROR_BAD_VALUE.  Denotes a failure due to the use of an invalid value.");
                            break;
                        case AudioRecord.ERROR_INVALID_OPERATION:
                            Log.wtf(TAG, "run(..)   readSize == AudioRecord.ERROR_INVALID_OPERATION.  Denotes a failure due to the improper use of a method.");
                            break;
                    }
                }
            } else {
                // sleep while not recording rather than spinning like crazy
                try {
                    Thread.sleep(1000 / AudioEventProcessor.UPDATES_PER_SECOND);
                } catch (InterruptedException e) {
                    Log.i(TAG, "run()   Huh.  InterruptedException thrown while sleep()ing.");
                    e.printStackTrace();
                }
            }
        }
    }

    private void notificationBar(boolean show) {
        if (show) {
            Notification.Builder mBuilder =
                    new Notification.Builder(context)
                            .setSmallIcon(R.drawable.black_mic)
                            .setContentTitle(context.getString(R.string.app_name))
                            .setContentText(context.getString(R.string.recording_service));
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, RecordingActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(RecordingActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);

            // mId allows you to update the notification later on.
            mNotificationManager.notify(RECORDER_NOTIFICATION_ID, mBuilder.build());
        } else {
            mNotificationManager.cancelAll();
        }
    }
}
