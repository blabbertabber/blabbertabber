package fr.lium.experimental.spkDiarization.programs;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Entity;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.Turn;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.TurnSet;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.TargetNameMap;
import fr.lium.experimental.spkDiarization.libSCTree.SCTProbabilities;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.lib.libDiarizationError.DiarizationError;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libModel.ModelScores;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class FlatSegmentation.
 */
public class FlatSegmentation {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(FlatSegmentation.class.getName());

	/** The parameter. */
	static Parameter parameter;

	/** The name and gender map. */
	static TargetNameMap nameAndGenderMap;

	/** The personne list. */
	static LinkedList<String> personneList;

	/** The first name and gender map. */
	static TargetNameMap firstNameAndGenderMap;

	/**
	 * Adds the score to the cluster attached to the turn.
	 * 
	 * @param turn the turn link to a cluster
	 * @param name the name of the target speaker
	 * @param score the score
	 * @param pos the pos
	 */
	public static void addScore(Turn turn, String name, Double score, String pos) {
		String val = "(" + name + ", " + pos + ", " + String.format("%10.6f", score) + ") ";
		for (Segment segment : turn) {
			if (segment.getInformation("trans") == "") {
				segment.setInformation("trans", val);
			} else {
				segment.setInformation("trans", val + segment.getInformation("trans"));
			}
			if (segment.getCluster().getInformation("trans") == "") {
				segment.getCluster().setInformation("trans", val);
			} else {
				segment.getCluster().setInformation("trans", val + segment.getCluster().getInformation("trans"));
			}
		}
	}

	/**
	 * Put speaker name.
	 * 
	 * @param probabilities the probabilities
	 * @param speakerName the speaker name
	 * @param turnSet the turn set
	 * @param index the index
	 */
	public static void putSpeakerName(SCTProbabilities probabilities, String speakerName, TurnSet turnSet, int index) {

		Turn turn;
		double scorePrev = 0, scoreCurrent = 0, scoreNext = 0.0;

		logger.info(speakerName + " " + index);
		probabilities.debug();

		// previous turn
		if ((index - 1) >= 0) {
			turn = turnSet.get(index - 1);
			scorePrev = probabilities.get(SpeakerNameUtils.PREVIOUS);
			addScore(turn, speakerName, scorePrev, "Prev");
		}

		// Current turn
		turn = turnSet.get(index);
		scoreCurrent = probabilities.get(SpeakerNameUtils.CURRENT);
		addScore(turn, speakerName, scoreCurrent, "Curr");

		// newt turn
		if ((index + 1) < turnSet.size()) {
			turn = turnSet.get(index + 1);
			scoreNext = probabilities.get(SpeakerNameUtils.NEXT);
			addScore(turn, speakerName, scoreNext, "Next");
		}
	}

	/**
	 * Put transcription score.
	 * 
	 * @param clusterSet the cluster set
	 * @throws CloneNotSupportedException the clone not supported exception
	 * @throws DiarizationException the diarization exception
	 */
	public static void putTranscriptionScore(ClusterSet clusterSet) throws CloneNotSupportedException, DiarizationException {
		logger.info("------ Transcription ------");
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		TurnSet turns = clusterSet.getTurns();
		for (int i = 0; i < turns.size(); i++) {
			Turn currentTurn = turns.get(i);
			LinkSet linkSet = currentTurn.getCollapsedLinkSet();
			boolean startTurn = true;
			boolean endTurn = true;
			SpeakerNameUtils.makeLinkSetForSCT(linkSet, startTurn, endTurn);
			for (int index = 0; index < linkSet.size(); index++) {
				Link link = linkSet.getLink(index);
				if (link.haveEntity(EntitySet.TypePersonne) == true) {
					SCTProbabilities probabilities = link.getEntity().getScore("SCT");
					String speakerName = link.getWord();
					if (SpeakerNameUtils.checkSpeakerName(speakerName, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) == true) {
						putSpeakerName(probabilities, speakerName, turns, i);
					}
				}
			}
		}
	}

	/**
	 * Print the available options.
	 * 
	 * @param parameter is all the parameters
	 * @param program name of this program
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("Program name = " + program);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			parameter.logShow();
			logger.config(parameter.getSeparator());
			parameter.getParameterNamedSpeaker().logAll();
		}
	}

	/**
	 * Put writing.
	 * 
	 * @param clusterSet the cluster set
	 */
	public static void putWriting(ClusterSet clusterSet) {
		logger.info("------ Writing ------");
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();

		for (Segment writingSegment : clusterSet.getWriting()) {
			EntitySet entitySet = writingSegment.getTranscription().getEntitySet();
			LinkSet linkSet = writingSegment.getTranscription().getLinkSet();
			writingSegment.setInformation("txt", linkSet.toString());

			// logger.info("-----");
			// linkSet.debug();
			// entitySet.debug();
			logger.info("-----");
			for (Entity entity : entitySet) {
				if (entity.isPerson() == true) {
					String name = entity.getListOfWords();
					int next = entity.last() + 1;
					String start = "EN_not_start_seg";
					String CRLF = "no_crlf";
					String function = "no_function";
					if (entity.start() == 0) {
						start = "en_start_seg";
					}
					if (next < linkSet.size()) {
						// logger.info("written crf: "+name+" "+next+" "+linkSet.getLink(next).getWord());
						if ((linkSet.getLink(next).find("CRLF") == true)
								|| (linkSet.getLink(next).find("crlf") == true)) {
							CRLF = "crlf";
							next++;
						}
					}
					if (next < linkSet.size()) {
						// logger.info("written func: "+name+" "+next+" "+linkSet.getLink(next).getWord()+" "+linkSet.getLink(next).haveEntity());
						if (linkSet.getLink(next).haveEntity()) {
							logger.info("written next is an entity: " + linkSet.getLink(next).getEntity().getType());
						}
						if (linkSet.getLink(next).haveEntity(EntitySet.TypeEster2Fonction)) {
							function = "function";
						}
					}
					String person = SpeakerNameUtils.normalizeSpeakerName(name);
					String inList = (SpeakerNameUtils.checkSpeakerName(person, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) ? "target"
							: "not_target");

					String info = "(" + person + ", " + CRLF + ", " + function + ", " + inList + ")";
					writingSegment.setInformation("ocr", info);
					for (String clusterName : clusterSet) {
						Cluster cluster = clusterSet.getCluster(clusterName);
						for (Segment segment : cluster) {
							int m = DiarizationError.match(writingSegment, segment);
							double rate = m / (double) writingSegment.getLength();
							if (rate > 0) {
								String gender = (checkGender(cluster, person) ? "gender_match" : "gender_not_match");
								String val = "(" + name + ", " + start + ", " + CRLF + ", " + function + ", "
										+ String.format("%10.6f", rate) + ", " + inList + ", " + gender + ") ";
								// String valSeg =new String(val);
								if (cluster.getInformation("ocr").isEmpty()) {
									cluster.setInformation("ocr", val);
									logger.info("written cluster first:" + clusterName + " --> "
											+ cluster.getInformation("ocr"));
								} else {
									cluster.setInformation("ocr", val + cluster.getInformation("ocr"));
									logger.info("written cluster add:" + clusterName + " --> "
											+ cluster.getInformation("ocr"));
								}
								if (segment.getInformation("ocr").isEmpty()) {
									segment.setInformation("ocr", val);
									logger.info("written seg first:" + clusterName + "/" + segment.getStart() + " --> "
											+ segment.getInformation("ocr"));
								} else {
									segment.setInformation("ocr", val + segment.getInformation("ocr"));
									logger.info("written seg add:" + clusterName + "/" + segment.getStart() + " --> "
											+ segment.getInformation("ocr"));
								}
							}
						}
					}
				} else {
					logger.info("written: \t\tentity " + entity.getId() + " " + entity.getType() + " "
							+ entity.getListOfWords() + " " + entity.start() + " " + entity.last());

				}
			}
		}
	}

	/**
	 * Check gender.
	 * 
	 * @param cluster the cluster
	 * @param speakerName the speaker name
	 * @return true, if successful
	 */
	public static boolean checkGender(Cluster cluster, String speakerName) {
		String speakerGender = "U";
		String normalizedSpeakerName = SpeakerNameUtils.normalizeSpeakerName(speakerName);
		if (parameter.getParameterNamedSpeaker().isFirstNameCheck()) {
			StringTokenizer tokenizer = new StringTokenizer(normalizedSpeakerName, "_");
			if (tokenizer.hasMoreTokens()) {
				normalizedSpeakerName = tokenizer.nextToken();
			}
			speakerGender = firstNameAndGenderMap.get(normalizedSpeakerName);
			logger.finest("normalized speaker name: " + normalizedSpeakerName + ", speakerGender firstname checked: "
					+ speakerGender);

		} else {
			speakerGender = nameAndGenderMap.get(normalizedSpeakerName);
			logger.finest("normalized speaker name: " + normalizedSpeakerName + ", speakerGender name checked: "
					+ speakerGender);
		}

		// logger.info("gender speaker:" + speakerGender + " check=" + parameter.getParameterNamedSpeaker().isDontCheckGender());
		if (parameter.getParameterNamedSpeaker().isDontCheckGender() == false) {
			// logger.info("gender check speaker:" + speakerGender + " turn=" + turn.getCluster().getGender());
			if ((cluster.getGender().equals(speakerGender) == false) && (speakerGender != null)
					&& !"U".equals(speakerGender)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Put audio score.
	 * 
	 * @param clusterSet the cluster set
	 */
	public static void putAudioScore(ClusterSet clusterSet) {
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		double thr = parameter.getParameterNamedSpeaker().getThresholdAudio();
		logger.info("------ Audio ------  " + thr);
		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			String spkId = "";
			ModelScores modelScores = cluster.getModelScores();
			for (String identity : modelScores.keySet()) {
				boolean inList = SpeakerNameUtils.checkSpeakerName(identity, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap);
				boolean gender = checkGender(cluster, identity) == true;
				double score = modelScores.get(identity);
				if (score >= thr) {
					spkId += "(" + SpeakerNameUtils.normalizeSpeakerName(identity) + ", "
							+ String.format("%10.6f", score) + ", " + (gender ? "gender_match" : "gender_not_match")
							+ ", " + (inList ? "target" : "not_target") + ") ";
				}
				// logger.info("put Audio name : " + cluster.getName() + " (" + cluster.getGender()
				// + ") --> " + identity + " = " + score );
			}
			cluster.setInformation("spkId", spkId);
		}

	}

	/**
	 * Put video score.
	 * 
	 * @param clusterSet the cluster set
	 */
	public static void putVideoScore(ClusterSet clusterSet) {
		logger.info("------ Use Video ------");
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			String faceId = "";
			ModelScores modelScores = cluster.getModelScores();
			for (String identity : modelScores.keySet()) {
				boolean inList = SpeakerNameUtils.checkSpeakerName(identity, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap);
				double score = modelScores.get(identity);
				faceId += "(" + SpeakerNameUtils.normalizeSpeakerName(identity) + ", " + String.format("%10.6f", score)
						+ ", " + inList + ") ";
				// logger.info("put video name : " + cluster.getName() + " (" + cluster.getGender()
				// + ") --> " + identity + " = " + score );

			}
			cluster.setInformation("faceId", faceId);
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			SpkDiarizationLogger.setup();
			parameter = MainTools.getParameters(args);
			info(parameter, "FlatSegmentation");
			if (parameter.show.isEmpty() == false) {
				nameAndGenderMap = SpeakerNameUtils.loadList(parameter.getParameterNamedSpeaker().getNameAndGenderList());

				firstNameAndGenderMap = null;
				if (parameter.getParameterNamedSpeaker().isFirstNameCheck()) {
					firstNameAndGenderMap = SpeakerNameUtils.loadList(parameter.getParameterNamedSpeaker().getFirstNameList());
				}

				ClusterSet audioClusterSet = MainTools.readClusterSet(parameter);
				// audioClusterSet.debug(100);
/*
 * logger.info("----------------"); logger.info("---debug -------------"); audioClusterSet.getWriting().debug(2); logger.info("----------------");
 */

				putTranscriptionScore(audioClusterSet);
				putWriting(audioClusterSet);
				putAudioScore(audioClusterSet);

				audioClusterSet.getHeadClusterSet().setWriting(audioClusterSet.getWriting());
				putVideoScore(audioClusterSet.getHeadClusterSet());
				putWriting(audioClusterSet.getHeadClusterSet());

				MainTools.writeClusterSet(parameter, audioClusterSet, true);

			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "exception ", e);
			e.printStackTrace();
		}

	}

	/**
	 * Main old.
	 * 
	 * @param args the args
	 * @throws Exception the exception
	 */
	public static void mainOld(String[] args) throws Exception {
		try {
			SpkDiarizationLogger.setup();
			parameter = MainTools.getParameters(args);
			info(parameter, "FlatSegmentation");
			if (parameter.show.isEmpty() == false) {
				nameAndGenderMap = SpeakerNameUtils.loadList(parameter.getParameterNamedSpeaker().getNameAndGenderList());

				firstNameAndGenderMap = null;
				if (parameter.getParameterNamedSpeaker().isFirstNameCheck()) {
					firstNameAndGenderMap = SpeakerNameUtils.loadList(parameter.getParameterNamedSpeaker().getFirstNameList());
				}

				ClusterSet audioClusterSet = MainTools.readClusterSet(parameter);
				// audioClusterSet.debug(100);
/*
 * logger.info("----------------"); logger.info("---debug -------------"); audioClusterSet.getWriting().debug(2); logger.info("----------------");
 */

				putTranscriptionScore(audioClusterSet);
// putWriting(audioClusterSet);
				putAudioScore(audioClusterSet);
				putVideoScore(audioClusterSet.getHeadClusterSet());

				// ClusterSet writtingClusterSet = new ClusterSet();
				// writtingClusterSet.getClusterMap().put("writting", audioClusterSet.getWriting());

				TreeMap<Integer, Segment> mapAudio = audioClusterSet.getFeatureMap(true);
				TreeMap<Integer, Segment> mapVideo = audioClusterSet.getHeadClusterSet().getFeatureMap(true);
				// TreeMap<Integer,Segment> mapWritting = writtingClusterSet.getFeatureMap(true);

				ClusterSet clusterSetResult = new ClusterSet();
				clusterSetResult.setWriting(audioClusterSet.getWriting());

// int first = Math.min(mapWritting.firstKey(), Math.min(mapAudio.firstKey(), mapVideo.firstKey()));
// int last = Math.max(mapWritting.lastKey(), Math.max(mapAudio.lastKey(), mapVideo.lastKey()));
				int first = Math.min(mapAudio.firstKey(), mapVideo.firstKey());
				int last = Math.max(mapAudio.lastKey(), mapVideo.lastKey());

				for (int i = first; i <= last; i++) {
					String spk = "empty";
					String spkGender = "empty";
					String face = "empty";
					String ocr = "";
					String trans = "";
					String spkId = "";
					String faceId = "";

					if (mapAudio.containsKey(i)) {
						Segment segmentAudio = mapAudio.get(i);
						spk = segmentAudio.getClusterName();
						spkGender = segmentAudio.getCluster().getGender();
						trans = segmentAudio.getInformation("trans");
						Cluster cluster = audioClusterSet.getCluster(spk);
						spkId = cluster.getInformation("spkId");

					}
					if (mapVideo.containsKey(i)) {
						face = mapVideo.get(i).getClusterName();
						Cluster cluster = audioClusterSet.getHeadClusterSet().getCluster(face);
						faceId = cluster.getInformation("faceId");
					}
// if (mapWritting.containsKey(i)) {
// ocr = mapWritting.get(i).getInformation("ocr");
// }

					String key = spk + "##" + face;

					Cluster clusterResult = clusterSetResult.getOrCreateANewCluster(key);
					clusterResult.setInformation("spkId", spkId);
					clusterResult.setInformation("faceId", faceId);
					Segment segment = new Segment(parameter.show, i, 1, clusterResult, parameter.getParameterSegmentationInputFile().getRate());
					clusterResult.setInformation("spk", spk);
					// segment.setInformation("spkGender", spkGender);
					clusterResult.setGender(spkGender);
					clusterResult.setInformation("face", face);
					if (!ocr.isEmpty()) {
						segment.setInformation("ocr", ocr);
					}
					if (!trans.isEmpty()) {
						segment.setInformation("trans", trans);
					}

					clusterResult.addSegment(segment);

					/*
					 * String ch = i+" / "+segment.getStart()+" "+segment.getLast()+" / "; ch += segment.getInformation("spk")+" "+segment.getInformation("spkGender")+" / "; ch += segment.getInformation("face")+" / "; ch +=
					 * segment.getInformation("trans")+" / "; ch += segment.getInformation("ocr")+" / "; System.err.println(ch);
					 */
				}

				clusterSetResult.collapse();
				// clusterSetResult.debug(5);

				putWriting(clusterSetResult);

				MainTools.writeClusterSet(parameter, clusterSetResult, true);

			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "exception ", e);
			e.printStackTrace();
		}
	}

}
