package com.blabbertabber.blabbertabber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by brendancunnie on 2/8/16.
 */
public class SpeakersBuilder {
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
    }

    public SpeakersBuilder parseSegStream(InputStream in) throws IOException {
        Reader r = new BufferedReader(new InputStreamReader(in));
        StreamTokenizer st = new StreamTokenizer(r);
        int numChars;
        numChars = st.nextToken();
        st.nextToken();
        st.nextToken();
        long startTime = (long) st.nval;
        st.nextToken();
        long duration = (long) st.nval;
        st.nextToken();
        char gender = st.sval.charAt(0);
        st.nextToken();
        st.nextToken();
        st.nextToken();
        String name = st.sval;
        add(startTime, duration, name, gender);
        return this;
    }

    public SpeakersBuilder add(long startTime, long duration, String name, char gender) {
        Speaker speaker = speakerMap.get(name);
        if (speaker == null) {
            speaker = new Speaker(name, gender);
            speakerMap.put(name, speaker);
        }
        speaker.addTurn(startTime, duration);
        return this;
    }

    public Speaker[] build() {
        List<Speaker> speakers = new ArrayList<Speaker>(speakerMap.values());
        Collections.sort(speakers, Collections.reverseOrder());
        return colorize(speakers.toArray(new Speaker[0]));
    }

    private Speaker[] colorize(Speaker[] speakers) {
        for (int i = 0; i < speakers.length; i++) {
            speakers[i].setColor(speakerColors[i % speakerColors.length]);
        }
        return speakers;
    }
}
