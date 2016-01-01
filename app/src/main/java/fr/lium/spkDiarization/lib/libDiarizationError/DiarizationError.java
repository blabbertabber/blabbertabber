package fr.lium.spkDiarization.lib.libDiarizationError;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.Pair;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;

/**
 * The Class DiarizationError.
 */
public class DiarizationError {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(DiarizationError.class.getName());

	/** The is used. */
	protected boolean isUsed;

	/** The distance. */
	protected TreeMap<String, Integer> distance;

	/** The matched speaker. */
	ArrayList<String> matchedSpeaker;

	/** The reference cluster set. */
	ClusterSet referenceClusterSet;

	/** The uem cluster set. */
	ClusterSet uemClusterSet;

	/**
	 * Gets the matched speaker.
	 * 
	 * @return the matchedSpeaker
	 */
	public ArrayList<String> getMatchedSpeaker() {
		return matchedSpeaker;
	}

	/**
	 * Instantiates a new diarization error.
	 * 
	 * @param referenceClusterSet the reference cluster set
	 * @param uemClusterSet the uem cluster set
	 */
	public DiarizationError(ClusterSet referenceClusterSet, ClusterSet uemClusterSet) {
		super();
		isUsed = false;
		this.referenceClusterSet = referenceClusterSet;
		this.uemClusterSet = uemClusterSet;

		if (referenceClusterSet != null) {
			isUsed = true;
		}
		distance = new TreeMap<String, Integer>();
		matchedSpeaker = new ArrayList<String>();
	}

	/**
	 * Checks if is used.
	 * 
	 * @return the isUsed
	 */
	public boolean isUsed() {
		return isUsed;
	}

	/**
	 * Gets the reference cluster set.
	 * 
	 * @return the referenceClusterSet
	 */
	public ClusterSet getReferenceClusterSet() {
		return referenceClusterSet;
	}

	/**
	 * Match.
	 * 
	 * @param referenceSegment the reference segment
	 * @param hypothesisSegment the hypothesis segment
	 * @return the int
	 */
	static public int match(Segment referenceSegment, Segment hypothesisSegment) {
		int referenceEnd = referenceSegment.getLast();
		int referenceBegin = referenceSegment.getStart();
		int hypothesisEnd = hypothesisSegment.getLast();
		int hypothesisBegin = hypothesisSegment.getStart();

		// logger.info("match start:"+referenceBegin+"/"+hypothesisBegin+" end:"+referenceEnd+"/"+hypothesisEnd);
		if (referenceEnd <= hypothesisBegin) {
			return 0;
		}
		if (hypothesisEnd <= referenceBegin) {
			return 0;
		}
		int begin = referenceBegin;
		int end = referenceEnd;
		if (hypothesisBegin > begin) {
			begin = hypothesisBegin;
		}
		if (hypothesisEnd < referenceEnd) {
			end = hypothesisEnd;
		}
		return (end - begin) + 1;
	}

	/**
	 * Distance between speaker.
	 * 
	 * @param hypothesisClusterSet the hypothesis cluster set
	 * @return the int
	 * @throws DiarizationException the diarization exception
	 */
	protected int distanceBetweenSpeaker(ClusterSet hypothesisClusterSet) throws DiarizationException {
		distance = new TreeMap<String, Integer>();
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
					distance.put(referenceName + ":" + hypothesisName, score);
				}
			}
		}

		return sumOfscores;

	}

	/**
	 * Gets the max.
	 * 
	 * @return the max
	 */
	protected Pair<String, Integer> getMax() {
		int max = 0;
		String resultKey = "empty";

		for (String key : distance.keySet()) {
			int val = distance.get(key);
			if (val > max) {
				max = val;
				resultKey = key;
			}
		}
		ArrayList<String> keyToRemove = new ArrayList<String>();

		if (resultKey != "empty") {
			String[] subKey = resultKey.split(":");
			for (String key : distance.keySet()) {
				String[] subKey2 = key.split(":");
				if (subKey2[0].equals(subKey[0])) {
					keyToRemove.add(key);
					distance.put(key, -1);
				}
				if (subKey2[1].equals(subKey[1])) {
					keyToRemove.add(key);
					distance.put(key, -1);
				}
			}
			for (String key : keyToRemove) {
				distance.remove(key);
			}
		}
		return new Pair<String, Integer>(resultKey, max);
	}

	/**
	 * Select.
	 * 
	 * @return the int
	 */
	protected int select() {

		Pair<String, Integer> pair = new Pair<String, Integer>("", 0);

		matchedSpeaker = new ArrayList<String>();

		int matched = 0;

		while (pair.getFirst() != "empty") {
			pair = getMax();
			matched += pair.getSecond();
			if (pair.getFirst() != "empty") {
				matchedSpeaker.add(pair.getFirst());
			}
		}
		return matched;
	}

	/**
	 * List of matched speaker.
	 * 
	 * @param hypothesisClusterSet the hypothesis cluster set
	 * @return the array list
	 * @throws DiarizationException the diarization exception
	 */
	public ArrayList<String> listOfMatchedSpeaker(ClusterSet hypothesisClusterSet) throws DiarizationException {
		distanceBetweenSpeaker(hypothesisClusterSet);
		select();
		return getMatchedSpeaker();
	}

	/**
	 * Log matched speaker.
	 */
	public void logMatchedSpeaker() {
		for (String name : matchedSpeaker) {
			logger.finer("select: " + name);
		}
	}

/*
 * protected int overlap(ClusterSet referenceClusterSet){ int sum = 0; for (String referenceName1 : referenceClusterSet) { Cluster referenceCluster1 = referenceClusterSet.getCluster(referenceName1); for (String referenceName2 : referenceClusterSet) {
 * if (referenceName1.equals(referenceName2) == false) { Cluster referenceCluster2 = referenceClusterSet.getCluster(referenceName2); for(Segment referenceSegment1: referenceCluster1){ for(Segment referenceSegment2: referenceCluster2){ sum +=
 * match(referenceSegment1, referenceSegment2); } } } } } return sum; }
 */

	/**
	 * Find last.
	 * 
	 * @param clusterSet the cluster set
	 * @return the int
	 */
	int findLast(ClusterSet clusterSet) {
		int last = 0;
		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			for (Segment segment : cluster) {
				if (last < segment.getLast()) {
					last = segment.getLast();
				}
			}
		}
		return last;
	}

	/**
	 * Find first.
	 * 
	 * @param clusterSet the cluster set
	 * @return the int
	 */
	int findFirst(ClusterSet clusterSet) {
		int first = Integer.MAX_VALUE;
		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			for (Segment segment : cluster) {
				if (first > segment.getStart()) {
					first = segment.getStart();
				}
			}
		}
		return first;
	}

	/**
	 * Histogram.
	 * 
	 * @param clusterSet the cluster set
	 * @param last the last
	 * @return the array list
	 */
	ArrayList<Integer> histogram(ClusterSet clusterSet, int last) {
		ArrayList<Integer> hist = new ArrayList<Integer>(last + 1);
		for (int i = 0; i <= last; i++) {
			hist.add(0);
		}
		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			for (Segment segment : cluster) {
				for (int i = segment.getStart(); i <= segment.getLast(); i++) {
					hist.set(i, hist.get(i) + 1);
				}
			}
		}
		return hist;
	}

	/**
	 * Score of matched speakers.
	 * 
	 * @param hypothesisClusterSet the hypothesis cluster set
	 * @return the diarization result
	 * @throws DiarizationException the diarization exception
	 */
	public DiarizationResult scoreOfMatchedSpeakers(ClusterSet hypothesisClusterSet) throws DiarizationException {
		// int overlap = overlap(referenceClusterSet);
		int last = Math.max(findLast(referenceClusterSet), findLast(hypothesisClusterSet));
		ArrayList<Integer> refenceHistogram = histogram(referenceClusterSet, last);
		ArrayList<Integer> hypothesisHistogram = histogram(hypothesisClusterSet, last);
		int overlap = 0;
		for (int i = 0; i <= last; i++) {
			if ((refenceHistogram.get(i) > 0) && ((hypothesisHistogram.get(i) > 0))) {
				overlap += (refenceHistogram.get(i) - hypothesisHistogram.get(i));
			}
		}

		int score = distanceBetweenSpeaker(hypothesisClusterSet) - overlap;
		int matched = select();
		int hypLength = hypothesisClusterSet.getLength();
		if (uemClusterSet != null) {
			hypLength = uemClusterSet.getLength();
		}
		int refLength = referenceClusterSet.getLength();

		int spk = score - matched;
		int miss = refLength - score;
		int fa = hypLength - score;

		return new DiarizationResult(0.0, refLength, hypLength, spk, miss, fa);
	}

}
