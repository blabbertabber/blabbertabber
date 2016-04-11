package com.blabbertabber.blabbertabber;

/**
 * Abstraction of AudioRecord
 */
public abstract class AudioRecordAbstract {

    public AudioRecordAbstract() {
    }

    protected abstract void stopAndRelease();     // stops the recording, closes file, cannot resume after stop().

    public abstract void startRecording();

    public abstract int read(short[] audioData, int offsetInShorts, int sizeInShorts);
}
