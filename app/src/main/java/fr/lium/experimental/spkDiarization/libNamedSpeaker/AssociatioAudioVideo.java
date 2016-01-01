package fr.lium.experimental.spkDiarization.libNamedSpeaker;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Logger;

import www.spatial.maine.edu.assignment.HungarianAlgorithm;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.libDiarizationError.DiarizationError;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;

/**
 * The Class AssociatioAudioVideo.
 */
public class AssociatioAudioVideo {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(AssociatioAudioVideo.class.getName());

	/**
	 * Match.
	 * 
	 * @param cluster1 the cluster1
	 * @param cluster2 the cluster2
	 * @return the double
	 */
	public static double match(Cluster cluster1, Cluster cluster2) {
		double sum = 0.0;
		for (Segment segment1 : cluster1) {
			for (Segment segment2 : cluster2) {
				sum += DiarizationError.match(segment1, segment2);
			}
		}
		return sum;
	}

	/**
	 * Video to array.
	 * 
	 * @param clusterSet the cluster set
	 * @throws DiarizationException the diarization exception
	 */
	static public void videoToArray(ClusterSet clusterSet) throws DiarizationException {
		TreeMap<Integer, Segment> speakerMap = clusterSet.getFeatureMap();
		logger.finest("----- 1 -------");
		TreeMap<Integer, AudioInfo> info = new TreeMap<Integer, AudioInfo>();
		ClusterSet videoClusterSet = clusterSet.getHeadClusterSet();

		for (String name : videoClusterSet) {
			Cluster cluster = videoClusterSet.getCluster(name);
			for (Segment head : cluster) {
				for (int i = head.getStart(); i < head.getLast(); i++) {
					if (info.get(i) == null) {
						info.put(i, new AudioInfo(1, name));
					} else {
						info.get(i).setNbHead(info.get(i).getNbHead() + 1);
					}
				}
			}
		}
		logger.finest("----- 2 -------");

		TreeMap<String, Integer> map = new TreeMap<String, Integer>();
		logger.finest("----- 3 -------");

		for (int speakerIndex : speakerMap.keySet()) {
			if (info.get(speakerIndex) != null) {
				if (info.get(speakerIndex).getNbHead() == 1) {
					String pair = speakerMap.get(speakerIndex).getCluster().getName() + "--"
							+ info.get(speakerIndex).getCentralHead();
					if (map.get(pair) == null) {
						map.put(pair, 1);
					} else {
						map.put(pair, map.get(pair) + 1);
					}
				}
			}
		}
		logger.finest("----- 4 -------");

		TreeMap<Integer, String> reverseMap = new TreeMap<Integer, String>();
		for (String pair : map.keySet()) {
			reverseMap.put(map.get(pair), pair);
		}
		logger.finest("----- 5 -------");

		ClusterSet videoClusterSet2 = clusterSet.getHeadClusterSet().clone();
		while (reverseMap.size() > 0) {
			Integer key = reverseMap.lastKey();
			if (key > 1000) {
				String pair = reverseMap.get(key);
				String keys[] = pair.split("--");
				if (keys[0].equals(keys[1]) == false) {
					logger.info("--> SET audio:" + keys[0] + " head:" + keys[1] + " score:" + key);
					videoClusterSet2.getCluster(keys[1]).setName(keys[0]);
				}
				reverseMap.remove(key);
			} else {
				break;
			}
		}

		clusterSet.setHeadClusterSet(videoClusterSet2);
	}

	/**
	 * Same name.
	 * 
	 * @param clusterSet the cluster set
	 */
	public static void sameName(ClusterSet clusterSet) {
		ClusterSet headClusterSet = clusterSet.getHeadClusterSet();

		for (String name : clusterSet) {
			if (headClusterSet.getCluster(name) != null) {
				headClusterSet.getCluster(name).setName(clusterSet.getCluster(name).getName());
			}
		}

		for (String name : headClusterSet) {
			if (clusterSet.getCluster(name) != null) {
				clusterSet.getCluster(name).setName(headClusterSet.getCluster(name).getName());
			}
		}
	}

	/**
	 * Assign speaker to head.
	 * 
	 * @param clusterSet the cluster set
	 * @return the int
	 */
	public static int assignSpeakerToHead(ClusterSet clusterSet) {
		int nbSet = 0;
		logger.finer("----- assignSpeakerToHead -------");
		// ClusterSet clusterSetResult = (ClusterSet) clusterSet.clone();

		ArrayList<String> speakerList = new ArrayList<String>();
		ArrayList<String> headList = new ArrayList<String>();

		for (String name : clusterSet) {
			speakerList.add(name);
		}

		ClusterSet headClusterSet = clusterSet.getHeadClusterSet();

		for (String name : headClusterSet) {
			headList.add(name);
// logger.finest("head list:"+name);
		}

		double[][] costMatrix = new double[speakerList.size()][headList.size()];

		// Start by assigning a great value to each entry (worst value)
		for (int i = 0; i < speakerList.size(); i++) {
			for (int j = 0; j < headList.size(); j++) {
				Cluster speakerCluster = clusterSet.getCluster(speakerList.get(i));
				Cluster headCluster = headClusterSet.getCluster(headList.get(j));
				costMatrix[i][j] = match(speakerCluster, headCluster);
			}
		}

		boolean transposed = false;
		if ((costMatrix.length > 0) && (costMatrix.length > costMatrix[0].length)) {
			logger.finest("Array transposed (because rows>columns).\n"); // Cols must be >= Rows.
			costMatrix = HungarianAlgorithm.transpose(costMatrix);
			transposed = true;
		}

		if (costMatrix.length > 0) {
			String sumType = "max";
			int[][] assignment = new int[costMatrix.length][2];
			assignment = HungarianAlgorithm.hgAlgorithm(costMatrix, sumType); // Call Hungarian algorithm.
			for (int[] element : assignment) {
				if (costMatrix[element[0]][element[1]] > 0) {
					Cluster speaker = null;
					Cluster head = null;

					if (transposed == false) {
						// speaker en 0
						speaker = clusterSet.getCluster(speakerList.get(element[0]));
						head = headClusterSet.getCluster(headList.get(element[1]));
						;
					} else {
						// speaker en 1
						speaker = clusterSet.getCluster(speakerList.get(element[1]));
						head = headClusterSet.getCluster(headList.get(element[0]));
						;
					}
					logger.info(String.format("SPK-HEAD HONG %s <-->%s = %.2f", speaker.getName(), head.getName(), costMatrix[element[0]][element[1]]).toString());
					logger.finest("trans:" + transposed + " ");

					if (speaker.getName().matches("S[0-9]+")) {
						if (head.getName().matches("C[0-9]+") == false) {
							speaker.setInformation("head_name", head.getName());
							speaker.setName(head.getName());
							speaker.setInformation("XMLSpeakerIdentity", head.getName());
							nbSet++;
						}

					} else {
						if (head.getName().matches("C[0-9]+")) {
							head.setInformation("speaker_name", speaker.getName());
							head.setName(speaker.getName());
							head.setInformation("XMLSpeakerIdentity", speaker.getName());
							nbSet++;
						}
					}

/*
 * String name = head.getName()+"--"+speaker.getName(); if (head.getName().equals(speaker.getName())) { name = speaker.getName(); } head.setName(name); speaker.setName(name);
 */
// head.getInformation().putAll(speaker.getInformation());
// logger.info(head.getInformations());
				}
			}
		}
		return nbSet;
		// return clusterSetResult;
	}

	/**
	 * Assign speaker to head2.
	 * 
	 * @param speakerClusterSet the speaker cluster set
	 */
	public static void assignSpeakerToHead2(ClusterSet speakerClusterSet) {

		TreeMap<String, Double> map = new TreeMap<String, Double>();

		ClusterSet headClusterSet = speakerClusterSet.getHeadClusterSet();

		// Start by assigning a great value to each entry (worst value)
		for (String speaker : speakerClusterSet) {
			for (String head : headClusterSet) {
				Cluster speakerCluster = speakerClusterSet.getCluster(speaker);
				Cluster headCluster = headClusterSet.getCluster(head);
				double value = match(speakerCluster, headCluster);
				String key = speaker + "--" + head;
				map.put(key, value);
			}
		}

		TreeMap<Double, String> reverseMap = new TreeMap<Double, String>();
		for (String pair : map.keySet()) {
			reverseMap.put(map.get(pair), pair);
		}

		while (reverseMap.size() > 0) {
			Double key = reverseMap.lastKey();
			if (key > 0.0) {
				String pair = reverseMap.get(key);
				String keys[] = pair.split("--");
				if (keys[0].equals(keys[1]) == false) {
					boolean anonymeSpeaker = keys[0].matches("S[0-9]+");
					boolean anonymeHead = keys[1].matches("C[0-9]+");

					if ((anonymeSpeaker == false) && (anonymeHead == true)) {
						headClusterSet.getCluster(keys[1]).setName(speakerClusterSet.getCluster(keys[0]).getName());
						logger.info("--> SET audio SPK:" + keys[0] + " head:" + keys[1] + " score:" + key);
					} else if ((anonymeSpeaker == true) && (anonymeHead == false)) {
						headClusterSet.getCluster(keys[0]).setName(speakerClusterSet.getCluster(keys[1]).getName());
						logger.info("--> SET head SPK:" + keys[0] + " head:" + keys[1] + " score:" + key);
					} else if ((anonymeSpeaker == true) && (anonymeHead == true)) {
						headClusterSet.getCluster(keys[1]).setName(speakerClusterSet.getCluster(keys[0]).getName());
						logger.info("--> SET audio2 SPK:" + keys[0] + " head:" + keys[1] + " score:" + key);
					} else {
						logger.info("--> SET not :" + keys[0] + " head:" + keys[1] + " score:" + key);
					}
				}
				reverseMap.remove(key);
			} else {
				break;
			}
		}

	}
}
