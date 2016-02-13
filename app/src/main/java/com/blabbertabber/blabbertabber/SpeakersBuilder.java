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
    HashMap<String, Speaker> speakerMap = new HashMap<String, Speaker>();

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
        return speakers.toArray(new Speaker[0]);
    }
}
