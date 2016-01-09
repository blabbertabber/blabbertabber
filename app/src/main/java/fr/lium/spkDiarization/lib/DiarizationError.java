package fr.lium.spkDiarization.lib;

import java.util.ArrayList;
import java.util.TreeMap;

import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;

public class DiarizationError {
    protected static int match(Segment reference, Segment hypothesis) {
        int rend = reference.getLast();
        int rbegin = reference.getStart();
        int hend = hypothesis.getLast();
        int hbegin = hypothesis.getStart();
        if (rend <= hbegin) {
            return 0;
        }
        if (hend <= rbegin) {
            return 0;
        }
        int begin = rbegin;
        int end = rend;
        if (hbegin > begin) begin = hbegin;
        if (hend < rend) end = hend;
        return end - begin + 1;
    }

    protected static int distanceBetweenSpeaker(ClusterSet referenceClusterSet, ClusterSet hypothesisClusterSet, TreeMap<String, Integer> listOfSpeakers) throws DiarizationException {
        //TreeMap<String, Integer> result = new TreeMap<String, Integer>();
        int sumOfscores = 0;
        for (String referenceName : referenceClusterSet) {
            Cluster referenceCluster = referenceClusterSet.getCluster(referenceName);
            for (String hypothesisName : hypothesisClusterSet) {
                Cluster hypothesisCluster = hypothesisClusterSet.getCluster(hypothesisName);
                int score = 0;
                for (Segment referenceSegment : referenceCluster) {
                    for (Segment hypothesisSegment : hypothesisCluster) {
                        int value = match(referenceSegment, hypothesisSegment);
                        score += value;
                        sumOfscores += value;
                    }
                }
                if (score > 0) {
                    listOfSpeakers.put(referenceName + ":" + hypothesisName, score);
                }
            }
        }

		/*int sum = 0;
        for(Integer val: listOfSpeakers.values()) {
			sum += val;
		}
		System.err.println("commun : "+sum);*/

        return sumOfscores;
		
		/*TreeMap<String, Integer> res = new TreeMap<String, Integer>();
		TreeMap<Integer, Segment> segMap2 = hypothesisClusterSet.toFrames();

		for (String name1 : referenceClusterSet) {
			Cluster cluster1 = referenceClusterSet.getCluster(name1);
			for (Segment segment1 : cluster1) {
				for (int i = segment1.getStart(); i <= segment1.getLast(); i++) {
					String name2 = segMap2.get(i).getClusterName();
					String idx = name1 + ":" + name2;
					if (res.containsKey(idx)) {
						res.put(idx, res.get(idx) + 1);
					} else {
						res.put(idx, 1);
					}
				}
			}
		}

		return res;*/
    }

    protected static Pair<String, Integer> getMax(TreeMap<String, Integer> distance) {
        int max = 0;
        String res = "empty";

        for (String key : distance.keySet()) {
            int val = distance.get(key);
            if (val > max) {
                max = val;
                res = key;
            }
        }
        //System.err.println("getMax: "+res+" "+max);
        ArrayList<String> toRemove = new ArrayList<String>();

        if (res != "empty") {
            String[] subKey = res.split(":");
            for (String key : distance.keySet()) {
                if (key.contains(subKey[0])) {
                    toRemove.add(key);
                    distance.put(key, -1);
                }
                if (key.contains(subKey[1])) {
                    toRemove.add(key);
                    distance.put(key, -1);
                }
            }
            for (String key : toRemove) {
                //System.err.println("\tgetMax: remove"+key);
                distance.remove(key);
            }
        }
        return new Pair<String, Integer>(res, max);
    }

    protected static Pair<ArrayList<String>, Integer> select(TreeMap<String, Integer> distance) {
        Pair<String, Integer> val = new Pair<String, Integer>("", 0);
        ArrayList<String> list = new ArrayList<String>();
        int sumOfScores = 0;

        while (val.fst != "empty") {
            val = getMax(distance);
            sumOfScores += val.snd;
            if (val.fst != "empty") {
                double v = (double) val.snd / 100.0;
                System.err.println("select: " + val.fst + " " + v);

                list.add(val.fst);
            }
        }

        return new Pair<ArrayList<String>, Integer>(list, sumOfScores);
    }

    public static ArrayList<String> listOfMatchedSpeakers(ClusterSet referenceClusterSet, ClusterSet hypothesisClusterSet) throws DiarizationException {
        TreeMap<String, Integer> distance = new TreeMap<String, Integer>();
        distanceBetweenSpeaker(referenceClusterSet, hypothesisClusterSet, distance);
        Pair<ArrayList<String>, Integer> result = select(distance);
        return result.fst;
    }

    public static double scoreOfMatchedSpeakers(ClusterSet referenceClusterSet, ClusterSet hypothesisClusterSet) throws DiarizationException {
        //System.err.println("distanceBetweenSpeaker");
        TreeMap<String, Integer> distance = new TreeMap<String, Integer>();
        int score = distanceBetweenSpeaker(referenceClusterSet, hypothesisClusterSet, distance);
        Pair<ArrayList<String>, Integer> result = select(distance);

        double hypLength = hypothesisClusterSet.getLength();
        double refLength = referenceClusterSet.getLength();
        System.err.println("**** hyp:" + hypLength / 100.0 + " ref:" + refLength / 100.0 + " score:" + score / 100.0 + " match:" + result.snd / 100.0);

        double FASpk = (hypLength - (double) score);
        double MissSpeechAndSpkErr = refLength - (double) result.snd;
        System.err.println("**** FA:" + FASpk + " Miss:" + MissSpeechAndSpkErr);
        return FASpk + MissSpeechAndSpkErr;
    }

}
