package com.blabbertabber.blabbertabber;

import android.view.View;

/**
 * Created by cunnie on 11/4/15.
 * <p/>
 * A singleton array of Speakers with attributes
 * such as ViewID, color, visibility, etc...
 */
public class TheSpeakers {
    public static final int MAX_SPEAKERS = 16;
    public static TheSpeakers singleton;
    public Speaker[] speakers = new Speaker[TheSpeakers.MAX_SPEAKERS];

    protected TheSpeakers() {
        // initialization is a slog
        for (int i = 0; i < MAX_SPEAKERS; i++) {
            speakers[i] = new Speaker();
            speakers[i].setVisible(View.INVISIBLE);
        }
        speakers[0].setViewID(R.id.speaker_0);
        speakers[1].setViewID(R.id.speaker_1);
        speakers[2].setViewID(R.id.speaker_2);
        speakers[3].setViewID(R.id.speaker_3);
        speakers[4].setViewID(R.id.speaker_4);
        speakers[5].setViewID(R.id.speaker_5);
        speakers[6].setViewID(R.id.speaker_6);
        speakers[7].setViewID(R.id.speaker_7);
        speakers[8].setViewID(R.id.speaker_8);
        speakers[9].setViewID(R.id.speaker_9);
        speakers[10].setViewID(R.id.speaker_10);
        speakers[11].setViewID(R.id.speaker_11);
        speakers[12].setViewID(R.id.speaker_12);
        speakers[13].setViewID(R.id.speaker_13);
        speakers[14].setViewID(R.id.speaker_14);
        speakers[15].setViewID(R.id.speaker_15);
        // colors
        speakers[0].setColor(0xff6600);
        speakers[1].setColor(0xffE600);
        speakers[2].setColor(0x99ff00);
        speakers[3].setColor(0x1aff00);
        speakers[4].setColor(0xff001a);
        speakers[5].setColor(0xff8b3d);
        speakers[6].setColor(0xffaf7a);
        speakers[7].setColor(0x00ff66);
        speakers[8].setColor(0xff0099);
        speakers[9].setColor(0x7acaff);
        speakers[10].setColor(0x3db1ff);
        speakers[11].setColor(0x00ffe6);
        speakers[12].setColor(0xe600ff);
        speakers[13].setColor(0x6600ff);
        speakers[14].setColor(0x001aff);
        speakers[15].setColor(0x0099ff);
    }

    public synchronized static TheSpeakers getInstance() {
        if (singleton == null) {
            singleton = new TheSpeakers();
        }
        return singleton;
    }
}
