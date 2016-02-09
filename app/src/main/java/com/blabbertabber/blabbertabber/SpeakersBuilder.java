package com.blabbertabber.blabbertabber;

import java.util.HashMap;

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
        return speakerMap.values().toArray(new Speaker[0]);
    }
}
