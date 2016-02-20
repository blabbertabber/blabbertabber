package com.blabbertabber.blabbertabber;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by brendancunnie on 2/8/16.
 */
public class SpeakersBuilder {
    private static final String TAG = "SpeakersBuilder";
    private static final int[] speakerColors = {
            0xb0ff6600,
            0xb0ffE600,
            0xb099ff00,
            0xb01aff00,
            0xb0ff001a,
            0xb0ff8b3d,
            0xb0ffaf7a,
            0xb000ff66,
            0xb0ff0099,
            0xb07acaff,
            0xb03db1ff,
            0xb000ffe6,
            0xb0e600ff,
            0xb06600ff,
            0xb0001aff,
            0xb00099ff};
    private HashMap<String, Speaker> speakerMap = new HashMap<String, Speaker>();

    public SpeakersBuilder() {
        Log.i(TAG, "SpeakersBuilder()");
    }

    public SpeakersBuilder parseSegStream(InputStream in) throws IOException {
        Log.i(TAG, "parseSegStream()");
        Reader r = new BufferedReader(new InputStreamReader(in));
        StreamTokenizer st = new StreamTokenizer(r);
        while (st.nextToken() != st.TT_EOF) { // show name
            st.nextToken(); // the channel number
            st.nextToken(); // the start of the segment (in features)
            // convert centiseconds to milliseconds
            long startTimeInMilliseconds = (long) st.nval * 10;
            st.nextToken(); // the length of the segment (in features)
            // convert centiseconds to milliseconds
            long durationInMilliseconds = (long) st.nval * 10;
            st.nextToken(); // the speaker gender (U=unknown, F=female, M=Male)
            char gender = st.sval.charAt(0);
            st.nextToken(); // the type of band (T=telephone, S=studio)
            st.nextToken(); // the type of environment (music, speech only, …)
            st.nextToken(); // the speaker label
            String name = st.sval;
            add(startTimeInMilliseconds, durationInMilliseconds, name, gender);
        }
        return this;
    }

    public SpeakersBuilder add(long startTimeInMilliseconds, long durationInMilliseconds, String name, char gender) {
        Log.i(TAG, "add() start: " + startTimeInMilliseconds + " duration: " + durationInMilliseconds +
                " name: " + name + " gender: " + gender);
        Speaker speaker = speakerMap.get(name);
        if (speaker == null) {
            speaker = new Speaker(name, gender);
            speakerMap.put(name, speaker);
        }
        speaker.addTurn(startTimeInMilliseconds, durationInMilliseconds);
        return this;
    }

    public ArrayList<Speaker> build() {
        Log.i(TAG, "build()");
        ArrayList<Speaker> speakers = new ArrayList<Speaker>(speakerMap.values());
        Collections.sort(speakers, Collections.reverseOrder());
        return colorize(speakers);
    }

    private ArrayList<Speaker> colorize(ArrayList<Speaker> speakers) {
        Log.i(TAG, "colorize()");
        for (int i = 0; i < speakers.size(); i++) {
            speakers.get(i).setColor(speakerColors[i % speakerColors.length]);
        }
        return speakers;
    }
}
