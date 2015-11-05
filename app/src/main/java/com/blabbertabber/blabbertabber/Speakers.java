package com.blabbertabber.blabbertabber;

/**
 * Created by cunnie on 11/4/15.
 */
public class Speakers {
    public static final int MAX_SPEAKERS = 16;
    private Speaker[] mSpeakers = new Speaker[Speakers.MAX_SPEAKERS];

    public Speakers() {
        // initialization is a slog
        for (int i = 0; i < MAX_SPEAKERS; i++) {
            mSpeakers[i] = new Speaker();
        }
        mSpeakers[0].setViewID(R.id.speaker_0);
        mSpeakers[1].setViewID(R.id.speaker_1);
        mSpeakers[2].setViewID(R.id.speaker_2);
        mSpeakers[3].setViewID(R.id.speaker_3);
        mSpeakers[4].setViewID(R.id.speaker_4);
        mSpeakers[5].setViewID(R.id.speaker_5);
        mSpeakers[6].setViewID(R.id.speaker_6);
        mSpeakers[7].setViewID(R.id.speaker_7);
        mSpeakers[8].setViewID(R.id.speaker_8);
        mSpeakers[9].setViewID(R.id.speaker_9);
        mSpeakers[10].setViewID(R.id.speaker_10);
        mSpeakers[11].setViewID(R.id.speaker_11);
        mSpeakers[12].setViewID(R.id.speaker_12);
        mSpeakers[13].setViewID(R.id.speaker_13);
        mSpeakers[14].setViewID(R.id.speaker_14);
        mSpeakers[15].setViewID(R.id.speaker_15);
        // colors
        mSpeakers[0].setColor(0xff6600);
        mSpeakers[1].setColor(0xff6600);
        mSpeakers[2].setColor(0xff6600);
        mSpeakers[3].setColor(0xff6600);
        mSpeakers[4].setColor(0xff6600);
        mSpeakers[5].setColor(0xff6600);
        mSpeakers[6].setColor(0xff6600);
        mSpeakers[7].setColor(0xff6600);
        mSpeakers[8].setColor(0xff6600);
        mSpeakers[9].setColor(0xff6600);
        mSpeakers[10].setColor(0xff6600);
        mSpeakers[11].setColor(0xff6600);
        mSpeakers[12].setColor(0xff6600);
        mSpeakers[13].setColor(0xff6600);
        mSpeakers[14].setColor(0xff6600);
        mSpeakers[15].setColor(0xff6600);
    }
}
