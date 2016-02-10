package com.blabbertabber.blabbertabber;

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
