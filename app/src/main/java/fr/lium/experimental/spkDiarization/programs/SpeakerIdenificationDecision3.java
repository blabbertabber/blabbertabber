package fr.lium.experimental.spkDiarization.programs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import www.spatial.maine.edu.assignment.HungarianAlgorithm;
import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerName;
import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerNameSet;
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
 * The Class SpeakerIdenificationDecision3.
 */
public class SpeakerIdenificationDecision3 {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SpeakerIdenificationDecision2.class.getName());

	/** The previous threshold. */
	public static double PREVIOUS_THRESHOLD = 0.05;

	/** The current threshold. */
	public static double CURRENT_THRESHOLD = 0.05;

	/** The next threshold. */
	public static double NEXT_THRESHOLD = 0.05;

	/** The parameter. */
	static Parameter parameter;

	/** The name and gender map. */
	static TargetNameMap nameAndGenderMap;

	/** The personne list. */
	static LinkedList<String> personneList;

	/** The first name and gender map. */
	static TargetNameMap firstNameAndGenderMap;

	/** The score keys. */
	static String scoreKeys[] = { "audio-score", "trans-score", "writing-score", "ocrSpk-score", "ocrHead-score",
			"video-score" };

	/** The name keys. */
	static String nameKeys[] = { "audio-name", "trans-name", "writing-name", "ocrSpk-name", "ocrHead-name",
			"video-name" };

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
			logger.config(parameter.getSeparator());
			parameter.getParameterNamedSpeaker().logAll();
		}
	}

	/**
	 * Gets the first name.
	 * 
	 * @param name the name
	 * @return the first name
	 */
	protected static String getFirstName(String name) {
		String first = new String(name);
		StringTokenizer tokenizer = new StringTokenizer(first, "_");
		if (tokenizer.hasMoreTokens()) {
			first = tokenizer.nextToken();
		}
		return first;
	}

	/**
	 * For each Solution of the SolutionsSet, put probabilities in the Cluster of the previous, current and next Turn for the target speaker.
	 * 
	 * @param probabilities the probabilities
	 * @param speakerName the name of the target speaker
	 * @param turnSet list of turn
	 * @param index index of the current turn in turns
	 * 
	 *            TODO make permutation of speaker name word (firstname/lastname - lastname/firstname)
	 * @return the string
	 */
	public static String putSpeakerName(SCTProbabilities probabilities, String speakerName, TurnSet turnSet, int index) {
		// SCTProbabilities probabilities = solution.getProbabilities();
		String normalizedSpeakerName = SpeakerNameUtils.normalizeSpeakerName(speakerName);
		String speakerGender;

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

		// logger.fine("normalized speaker name:" + normalizedSpeakerName + " gender is:" + speakerGender);

		Turn turn;
		double scorePrev = 0, scoreCurrent = 0, scoreNext = 0.0;
// boolean maximum = parameter.getParameterNamedSpeaker().isMaximum();

		// previous turn
		if ((index - 1) >= 0) {
			turn = turnSet.get(index - 1);
// String previousName = SpeakerNameUtils.normalizeSpeakerName(turn.getCluster().getName());
			scorePrev = probabilities.get(SpeakerNameUtils.PREVIOUS);
			if ((checkGender(turn, speakerGender) == true) && (scorePrev > PREVIOUS_THRESHOLD)) {
				logger.info("ACCEPT trans: " + turn.getCluster().getName() + " --> " + speakerName + " = " + scorePrev
						+ " previous= " + turn.first().getStart());
				addScore(turn, speakerName, scorePrev);
			}
		}

		// Current turn
		turn = turnSet.get(index);
// String currentName = SpeakerNameUtils.normalizeSpeakerName(turn.getCluster().getName());
		scoreCurrent = probabilities.get(SpeakerNameUtils.CURRENT);
		if ((checkGender(turn, speakerGender) == true) && (scoreCurrent > CURRENT_THRESHOLD)) {
			logger.info("ACCEPT trans: " + turn.getCluster().getName() + " --> " + speakerName + " = " + scoreCurrent
					+ " current= " + turn.first().getStart());
			addScore(turn, speakerName, scoreCurrent);
		}

		// newt turn
		if ((index + 1) < turnSet.size()) {
			turn = turnSet.get(index + 1);
			scoreNext = probabilities.get(SpeakerNameUtils.NEXT);
			String nextName = SpeakerNameUtils.normalizeSpeakerName(turn.getCluster().getName());

			if ((checkGender(turn, speakerGender) == true) && (scoreNext > NEXT_THRESHOLD)) {
				logger.info("ACCEPT trans: " + nextName + " --> " + speakerName + " = " + scoreNext + " next= "
						+ turn.first().getStart());
				addScore(turn, speakerName, scoreNext);
			}
		}
		speakerName = SpeakerNameUtils.normalizeSpeakerName(speakerName);

		return speakerName;
	}

	/**
	 * Put max speaker name.
	 * 
	 * @param probabilities the probabilities
	 * @param speakerName the speaker name
	 * @param turnSet the turn set
	 * @param index the index
	 * @return the string
	 */
	public static String putMaxSpeakerName(SCTProbabilities probabilities, String speakerName, TurnSet turnSet, int index) {
		// SCTProbabilities probabilities = solution.getProbabilities();
		String normalizedSpeakerName = SpeakerNameUtils.normalizeSpeakerName(speakerName);
		String speakerGender;

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

		Turn turn = turnSet.get(index);
		Turn turnMax = null;
		String label = SpeakerNameUtils.OTHER;

		double score = probabilities.get(SpeakerNameUtils.OTHER);
		double scoreMax = score;

		score = probabilities.get(SpeakerNameUtils.INSHOW);
		if (score > scoreMax) {
			scoreMax = score;
			label = SpeakerNameUtils.INSHOW;
		}

		score = probabilities.get(SpeakerNameUtils.CURRENT);
		if (((checkGender(turn, speakerGender) == true) && (score > CURRENT_THRESHOLD)) == false) {
			if (score > scoreMax) {
				scoreMax = score;
				turnMax = null;
				label = SpeakerNameUtils.CURRENT;
			}
		}

		// previous turn
		if ((index - 1) >= 0) {
			turn = turnSet.get(index - 1);
			score = probabilities.get(SpeakerNameUtils.PREVIOUS);
			if ((checkGender(turn, speakerGender) == true) && (score > PREVIOUS_THRESHOLD)) {
				if (score > scoreMax) {
					scoreMax = score;
					turnMax = turn;
					label = SpeakerNameUtils.PREVIOUS;
				}
			}
		}

		if ((index + 1) < turnSet.size()) {
			turn = turnSet.get(index + 1);
			score = probabilities.get(SpeakerNameUtils.NEXT);
			if ((checkGender(turn, speakerGender) == true) && (score > NEXT_THRESHOLD)) {
				if (score > scoreMax) {
					scoreMax = score;
					turnMax = turn;
					label = SpeakerNameUtils.NEXT;
				}
			}
		}

		if (turnMax != null) {
			logger.info("ACCEPT MAX trans: " + turnMax.getCluster().getName() + " --> " + speakerName + " = "
					+ scoreMax + " max= " + turnMax.first().getStart() + " label:" + label);
			addScore(turnMax, speakerName, scoreMax);
		}

		return speakerName;
	}

	/**
	 * Check coherence of genres between the target speaker name and the gender of the trun .
	 * 
	 * @param turn the turn
	 * @param speakerGender the speaker gender
	 * 
	 * @return true, if successful
	 */
	public static boolean checkGender(Turn turn, String speakerGender) {
		// logger.info("gender speaker:" + speakerGender + " check=" + parameter.getParameterNamedSpeaker().isDontCheckGender());
		if (parameter.getParameterNamedSpeaker().isDontCheckGender() == false) {
			// logger.info("gender check speaker:" + speakerGender + " turn=" + turn.getCluster().getGender());
			if ((turn.getCluster().getGender().equals(speakerGender) == false) && (speakerGender != null)
					&& !"U".equals(speakerGender)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Adds the score to the cluster attached to the turn.
	 * 
	 * @param turn the turn link to a cluster
	 * @param name the name of the target speaker
	 * @param value the score
	 */
	public static void addScore(Turn turn, String name, double value) {
		Cluster cluster = turn.getCluster();
		SpeakerName speakerName = cluster.getSpeakerName(name);
		speakerName.addScoreCluster(value);
	}

	/**
	 * Checks if is start with speaker name.
	 * 
	 * @param speakerName the speaker name
	 * @param list the list
	 * @return the string
	 */
	public static String isStartWithSpeakerName(String speakerName, LinkedList<String> list) {
		String partialName = SpeakerNameUtils.normalizeSpeakerName(speakerName) + "_";
		for (String name : list) {
			if (name.startsWith(partialName)) {
				return name;
			}
		}
		return null;
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
		logger.info("------ Use Audio ------");
		if (parameter.getParameterNamedSpeaker().isUseAudio()) {
			double thr = parameter.getParameterNamedSpeaker().getThresholdAudio();
			for (String name : clusterSet) {
				Cluster cluster = clusterSet.getCluster(name);
				ModelScores modelScores = cluster.getModelScores();
				for (String identity : modelScores.keySet()) {
					if (SpeakerNameUtils.checkSpeakerName(identity, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) == true) {
						if (checkGender(cluster, identity) == true) {
							double score = modelScores.get(identity);
							if (score > thr) {
								SpeakerName speakerName = cluster.getSpeakerName(SpeakerNameUtils.normalizeSpeakerName(identity));
								speakerName.addScoreCluster(score);

								logger.info("ACCEPT Audio name : " + cluster.getName() + " (" + cluster.getGender()
										+ ") --> " + identity + " = " + score + " (" + thr + ")");
							} else {
								logger.finest("REJECT Audio THR : " + cluster.getName() + " --> " + identity + " = "
										+ score + " (" + thr + ")");
							}
						} else {
							logger.finest("REJECT Audio GENDER : " + cluster.getName() + " (" + cluster.getGender()
									+ ") --> " + identity + " gender ");
						}
					} else {
						logger.finest("REJECT Audio LIST : " + cluster.getName() + " (" + cluster.getGender()
								+ ") --> " + identity + " gender ");
					}
				}
			}

		}
	}

	/**
	 * Put audio score max.
	 * 
	 * @param clusterSet the cluster set
	 */
	public static void putAudioScoreMax(ClusterSet clusterSet) {
		logger.info("------ Use Audio ------");
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		if (parameter.getParameterNamedSpeaker().isUseAudio()) {
			double thr = parameter.getParameterNamedSpeaker().getThresholdAudio();
			for (String name : clusterSet) {
				Cluster cluster = clusterSet.getCluster(name);
				ModelScores modelScores = cluster.getModelScores();
				double max = -Double.MAX_VALUE;
				String maxIdentity = "empty";
				for (String identity : modelScores.keySet()) {
					if (SpeakerNameUtils.checkSpeakerName(identity, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) == true) {
						if (checkGender(cluster, identity) == true) {
							double score = modelScores.get(identity);
							if ((score > thr) && (score > max)) {
								max = score;
								maxIdentity = identity;
							}
						}
					}
				}
				if (max > -Double.MAX_VALUE) {
					SpeakerName speakerName = cluster.getSpeakerName(SpeakerNameUtils.normalizeSpeakerName(maxIdentity));
					speakerName.addScoreCluster(max);
					logger.info("ACCEPT Audio MAX name max : " + cluster.getName() + ") --> " + maxIdentity + " = "
							+ max + " (" + thr + ")");
				}
			}
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
		if (parameter.getParameterNamedSpeaker().isUseVideo()) {
			double thr = parameter.getParameterNamedSpeaker().getThresholdVideo();
			for (String name : clusterSet) {
				Cluster cluster = clusterSet.getCluster(name);
				ModelScores modelScores = cluster.getModelScores();
				double max = -Double.MAX_VALUE;
				String maxIdentity = "empty";
				for (String identity : modelScores.keySet()) {
					if (SpeakerNameUtils.checkSpeakerName(identity, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) == true) {
						if (checkGender(cluster, identity) == true) {
							double score = modelScores.get(identity);
							if ((score > thr) && (score > max)) {
								max = score;
								maxIdentity = identity;
							}
						}
					}
				}
				if (max > -Double.MAX_VALUE) {
					SpeakerName speakerName = cluster.getSpeakerName(SpeakerNameUtils.normalizeSpeakerName(maxIdentity));
					speakerName.addScoreCluster(max);
					logger.info("ACCEPT Video name max : " + cluster.getName() + ") --> " + maxIdentity + " = " + max
							+ " (" + thr + ")");
				}
			}
		}
	}

	/**
	 * Put writing max.
	 * 
	 * @param audioClusterSet the audio cluster set
	 * @param thr the thr
	 */
	public static void putWritingMax(ClusterSet audioClusterSet, double thr) {
		// stratégie : si l'EN pers est co
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		logger.info("------ Use Writing ------");

		// double thr = parameter.getParameterNamedSpeaker().getThresholdWriting();
		if (parameter.getParameterNamedSpeaker().isUseWriting()) {
			Cluster writing = audioClusterSet.getWriting();

			for (Segment writingSegment : writing) {

				EntitySet entitySet = writingSegment.getTranscription().getEntitySet();
				for (Entity entity : entitySet) {
// if ((entity.isPerson() == true)){
					if ((entity.isPerson() == true) && (entity.start() == 0)) {
						String name = entity.getListOfWords();
						String person = SpeakerNameUtils.normalizeSpeakerName(name);
						if (SpeakerNameUtils.checkSpeakerName(person, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) == true) {
							int nbMatch = 0;
							double maxRate = 0;
							Segment matchSegment = null;
							for (Cluster cluster : audioClusterSet.getClusterVectorRepresentation()) {
								for (Segment segment : cluster) {
									int m = DiarizationError.match(writingSegment, segment);
									double rate = m / (double) writingSegment.getLength();
									if (rate >= thr) {
										nbMatch++;
									}
									if (rate >= maxRate) {
										maxRate = rate;
										matchSegment = segment;
									}
/*
 * if ((rate >= maxRate) && (rate >= thr)) { nbMatch++; maxRate = rate; matchHeadSegment = segment; }
 */
								}
							}
							if ((nbMatch == 1) && (maxRate > thr)) {
								Cluster cluster = matchSegment.getCluster();
// if(nbMatch > 0) {
								if (checkGender(cluster, person) == true) {
									SpeakerName speakerName = cluster.getSpeakerName(person);
									speakerName.addScoreCluster(thr);
// speakerName.addScoreCluster(thr);
									logger.info("ACCEPT WRITING name : " + cluster.getName() + " --> " + person + " = "
											+ maxRate);
								} else {
									logger.info("REJECT WRITING name : " + cluster.getName() + " --> " + person + " = "
											+ thr + " / gender");
								}
							} else {
								logger.info("REJECT WRITING name :  UNK --> " + person + " = " + thr + " / nbMatch: "
										+ nbMatch + " maxRate:" + maxRate);
							}
						} else {
							logger.info("REJECT WRITING name : " + person + " / list");
						}
					}
				}
			}
		}
	}

	/**
	 * Put writing.
	 * 
	 * @param audioClusterSet the audio cluster set
	 * @param thr the thr
	 */
	public static void putWriting(ClusterSet audioClusterSet, double thr) {
		// stratégie : si l'EN pers est co
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		logger.info("------ Use Writing ------");

		if (parameter.getParameterNamedSpeaker().isUseWriting()) {
			Cluster writing = audioClusterSet.getWriting();
			for (Segment writingSegment : writing) {
				EntitySet entitySet = writingSegment.getTranscription().getEntitySet();
				for (Entity entity : entitySet) {
					if ((entity.isPerson() == true) && (entity.start() == 0)) {
						String name = entity.getListOfWords();
						String person = SpeakerNameUtils.normalizeSpeakerName(name);
						if (SpeakerNameUtils.checkSpeakerName(person, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) == true) {
							int nbMatch = 0;
							double maxRate = 0;
							Segment matchSegment = null;
							for (Cluster cluster : audioClusterSet.getClusterVectorRepresentation()) {
								for (Segment segment : cluster) {
									int m = DiarizationError.match(writingSegment, segment);
									double rate = m / (double) writingSegment.getLength();
									if (rate >= thr) {
										nbMatch++;
									}
									if (rate >= maxRate) {
										maxRate = rate;
										matchSegment = segment;
									}
								}
							}
							if ((nbMatch == 1) && (maxRate > thr)) {
								Cluster cluster = matchSegment.getCluster();
								matchSegment.setInformation("OCR_name", person);
								matchSegment.setInformation("OCR_score", maxRate);

								SpeakerName speakerName = cluster.getSpeakerName(person);
								speakerName.addScoreCluster(maxRate);
								logger.info("ACCEPT WRITING name : " + cluster.getName() + " --> " + person + " = "
										+ maxRate);
							} else {
								logger.info("REJECT WRITING name :  UNK --> " + person + " = " + thr + " / nbMatch: "
										+ nbMatch + " maxRate:" + maxRate);
							}
						} else {
							logger.info("REJECT WRITING name : " + person + " / list");
						}
					}
				}
			}
		}
	}

	/**
	 * Writting split cluster.
	 * 
	 * @param clusterSet the cluster set
	 */
	public static void writtingSplitCluster(ClusterSet clusterSet) {
		ClusterSet clusterSetResult = new ClusterSet();
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			int nb = 0;
			for (Segment segment : cluster) {
				if (segment.getInformation("OCR_name").isEmpty() == false) {
					nb++;
				}
			}
			if (nb > 1) {
				String name = cluster.getName();
				logger.info("-->split :" + name);
				int idx = 1;
				boolean add = false;
				Cluster newCluster = null;
				TreeSet<Segment> set = new TreeSet<Segment>();
				for (Segment segment : cluster) {
					if (segment.getInformation("OCR_name").isEmpty() == false) {
						logger.info("\tnew cluster :" + name + "." + idx + " -- " + segment.getInformation("OCR_name")
								+ " " + segment.getInformation("OCR_score"));
						newCluster = clusterSetResult.createANewCluster(name + "." + idx);
						SpeakerName speakerName = newCluster.getSpeakerName(segment.getInformation("OCR_name"));
						speakerName.addScoreCluster(Double.valueOf(segment.getInformation("OCR_score")));
						cluster.getSpeakerNameSet().remove(segment.getInformation("OCR_name"));
						idx++;
						add = true;
					}
					if (add == true) {
						set.add(segment);
						newCluster.addSegment(segment);
					}
				}
				for (Segment segment : set) {
					cluster.removeSegment(segment);
				}
			}
		}
		for (Cluster cluster : clusterSetResult.clusterSetValue()) {
			clusterSet.getClusterMap().put(cluster.getName(), cluster);
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
		logger.info("------ Use Transcription ------");

		if (parameter.getParameterNamedSpeaker().isUseTranscription()) {

			personneList = new LinkedList<String>();

			TurnSet turns = clusterSet.getTurns();
			boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
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
							/*
							 * String ch = ""; for (String key : probabilities.keySet()) { ch += " "+key+":"+probabilities.get(key); }
							 */
							String normalizedName;
							// if (parameter.getParameterNamedSpeaker().isMaximum()) {
							// normalizedName = putSpeakerName(probabilities, speakerName, turns, i);
							// } else {
							normalizedName = putMaxSpeakerName(probabilities, speakerName, turns, i);
							// }
							personneList.addFirst(normalizedName);
						}
					}
				}
			}
		}
	}

	/**
	 * Decide maximum.
	 * 
	 * @param clusterSet the cluster set
	 * @param scoreKey the score key
	 * @param nameKey the name key
	 * @return the cluster set
	 */
	public static ClusterSet decideMaximum(ClusterSet clusterSet, String scoreKey, String nameKey) {
		logger.info("------  Maximum decision ------");
		ClusterSet clusterSetResult = clusterSet.clone();
		ClusterSet videoClusterSetResult = null;
		if (clusterSet.getHeadClusterSet() != null) {
			videoClusterSetResult = clusterSet.getHeadClusterSet().clone();
		}
		for (String name : clusterSet) {
			Cluster cluster = clusterSetResult.getCluster(name);
			SpeakerNameSet spkNameSet = cluster.getSpeakerNameSet();
			if (spkNameSet.size() == 0) {
				continue;
			}
			SpeakerName winer = spkNameSet.getMaxScore();
			if ((videoClusterSetResult != null) && (videoClusterSetResult.getCluster(name) != null)) {
				// videoClusterSetResult.getCluster(name).setName(winer.getName());

				videoClusterSetResult.getCluster(name).setInformation(nameKey, winer.getName());
				videoClusterSetResult.getCluster(name).setInformation(scoreKey, winer.getScore());
				logger.info(String.format("ASSIGN VIDEO MAX %s (%s) -->%s = %.2f", name, cluster.getGender(), winer.getName(), winer.getScore()).toString());
			}
			// --ici --
			// cluster.setName(winer.getName());

			cluster.setInformation(nameKey, winer.getName());
			cluster.setInformation(scoreKey, winer.getScore());
			logger.info(String.format("ASSIGN AUDIO MAX %s (%s) -->%s = %.2f", name, cluster.getGender(), winer.getName(), winer.getScore()).toString());
		}

		return clusterSetResult;
	}

	/**
	 * Assign the candidate speaker name to cluster. Use the Hungarian Algorithm
	 * 
	 * @param clusterSet the clusters
	 * @param scoreKey the score key
	 * @param nameKey the name key
	 * @return the a new cluster set
	 */
	public static ClusterSet decideHungarian(ClusterSet clusterSet, String scoreKey, String nameKey) {
		logger.info("------ decide hungrarian ------");

		ClusterSet clusterSetResult = clusterSet.clone();
		clusterSetResult.setHeadClusterSet(clusterSet.getHeadClusterSet().clone());
		TreeMap<String, Integer> clusterNameIndexMap = new TreeMap<String, Integer>();
		TreeMap<String, Integer> speakerNameIndexMap = new TreeMap<String, Integer>();
		TreeMap<Integer, String> reverseClusterNameIndexMap = new TreeMap<Integer, String>();
		TreeMap<Integer, String> reverseSpeakerNameIndexMap = new TreeMap<Integer, String>();
		// ClusterSet videoClusterSet = clusterSet.getHeadClusterSet();
		ClusterSet videoClusterSetResult = clusterSetResult.getHeadClusterSet();

		int clusterIndex = 0;
		int spkIndex = 0;

		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			SpeakerNameSet spkNameSet = cluster.getSpeakerNameSet();
			if (spkNameSet.size() == 0) {
				continue;
			}

			clusterNameIndexMap.put(name, clusterIndex);
			reverseClusterNameIndexMap.put(clusterIndex, name);

			Iterator<String> itr = spkNameSet.iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				SpeakerName spkName = spkNameSet.get(key);

				if (!speakerNameIndexMap.containsKey(spkName.getName())) {
					speakerNameIndexMap.put(spkName.getName(), spkIndex);
					reverseSpeakerNameIndexMap.put(spkIndex, spkName.getName());
					spkIndex++;
				}
			}
			clusterIndex++;
		}

		double[][] costMatrix = new double[clusterNameIndexMap.size()][speakerNameIndexMap.size()];

		// Start by assigning a great value to each entry (worst value)
		for (int i = 0; i < clusterNameIndexMap.size(); i++) {
			for (int j = 0; j < speakerNameIndexMap.size(); j++) {
				// costMatrix[i][j]=Float.MAX_VALUE;
				costMatrix[i][j] = 0;
			}
		}

		clusterIndex = 0;
		spkIndex = 0;

		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			SpeakerNameSet spkNameSet = cluster.getSpeakerNameSet();
			if (spkNameSet.size() == 0) {
				continue;
			}

			clusterIndex = clusterNameIndexMap.get(name);
			Iterator<String> itr = spkNameSet.iterator();
			while (itr.hasNext()) {
				String key = itr.next();
				SpeakerName spkName = spkNameSet.get(key);
				spkIndex = speakerNameIndexMap.get(spkName.getName());
				costMatrix[clusterIndex][spkIndex] = spkName.getScore();
			}
		}

		boolean transposed = false;

		if ((costMatrix.length > 0) && (costMatrix.length > costMatrix[0].length)) {
			logger.finest("Array transposed (because rows>columns).\n"); // Cols must be >= Rows.
			costMatrix = HungarianAlgorithm.transpose(costMatrix);
			transposed = true;
		}

		logger.finest("(Printing out only 2 decimals)");
		logger.finest("The matrix is:");

		String log = "";
		for (double[] element : costMatrix) {
			for (double element2 : element) {
				log += String.format("%.2f ", element2).toString();
			}
			logger.finest(log);
		}

		if (costMatrix.length > 0) {
			String sumType = "max";
			int[][] assignment = new int[costMatrix.length][2];
			assignment = HungarianAlgorithm.hgAlgorithm(costMatrix, sumType); // Call Hungarian algorithm.
			logger.finest("The winning assignment (" + sumType + " sum) is:\n");
			double sum = 0;
			for (int[] element : assignment) {
				// <COMMENT> to avoid printing the elements that make up the assignment
				String clusterName;
				String newName;
				if (!transposed) {
					clusterName = clusterSet.getCluster(reverseClusterNameIndexMap.get(element[0])).getName();
					newName = reverseSpeakerNameIndexMap.get(element[1]);
				} else {
					clusterName = clusterSet.getCluster(reverseClusterNameIndexMap.get(element[1])).getName();
					newName = reverseSpeakerNameIndexMap.get(element[0]);
				}
				sum = sum + costMatrix[element[0]][element[1]];

				if (costMatrix[element[0]][element[1]] > parameter.getParameterNamedSpeaker().getThresholdDecision()) {
					logger.info(String.format("ASSIGN HONG %s -->%s = %.2f", clusterName, newName, costMatrix[element[0]][element[1]]).toString());
					// clusterSetResult.getCluster(clusterName).setName(newName);
					clusterSetResult.getCluster(clusterName).setInformation(nameKey, newName);
					clusterSetResult.getCluster(clusterName).setInformation(scoreKey, costMatrix[element[0]][element[1]]);
					if ((videoClusterSetResult != null) && (videoClusterSetResult.getCluster(clusterName) != null)) {
						// videoClusterSetResult.getCluster(clusterName).setName(newName);
						videoClusterSetResult.getCluster(clusterName).setInformation(nameKey, newName);
						videoClusterSetResult.getCluster(clusterName).setInformation(scoreKey, costMatrix[element[0]][element[1]]);
					}
				}

			}
			logger.finest(String.format("\nThe %s is: %.2f\n", sumType, sum).toString());
		}

		return clusterSetResult;
	}

	/**
	 * Prints the float matrix.
	 * 
	 * @param matrix the matrix
	 */
	public static void printFloatMatrix(float[][] matrix) {
		String log = "";
		for (float[] element : matrix) {
			for (float element2 : element) {
				log += element2 + "\t";
			}
			logger.finest(log);
		}

	}

/*
 * Killer function ;-) It will go through each cluster, and compute each speaker score according to the belief function theory
 */
	/**
	 * Compute belief functions.
	 * 
	 * @param clusters the clusters
	 * @throws Exception the exception
	 */
	public static void computeBeliefFunctions(ClusterSet clusters) throws Exception {
		logger.info("------ compute Belief functions ------");
		for (String name : clusters) {
			Cluster cluster = clusters.getCluster(name);
			cluster.computeBeliefFunctions();
// cluster.debugSpeakerName();
		}
	}

	/**
	 * Prints the cluster name.
	 * 
	 * @param clusterSet the cluster set
	 */
	static public void printClusterName(ClusterSet clusterSet) {
		for (String name : clusterSet) {
			if (clusterSet.getCluster(name).segmentsSize() > 0) {
				logger.info("speaker cluster key:" + name + " name:" + clusterSet.getCluster(name).getName()
						+ " gender:" + clusterSet.getCluster(name).getGender());
			}
		}
		ClusterSet headClusterSet = clusterSet.getHeadClusterSet();
		if (headClusterSet != null) {
			for (String name : headClusterSet) {
				if (headClusterSet.getCluster(name).segmentsSize() > 0) {
					logger.info("head cluster key:" + name + " name:" + headClusterSet.getCluster(name).getName()
							+ " gender:" + headClusterSet.getCluster(name).getGender());
				}
			}
		}
	}

	/**
	 * Sets the anonymous.
	 * 
	 * @param clusterSet the new anonymous
	 */
	public static void setAnonymous(ClusterSet clusterSet) {
		logger.info("------ setAnonymous ------");
		for (String idx : clusterSet) {
			Cluster cluster = clusterSet.getCluster(idx);
			String name = cluster.getName();
			if (name.matches("S[0-9]+")) {
				String newName = name.replaceFirst("S", "speaker#");
				cluster.setName(newName);
				logger.info("SPEAKER remplace name: " + name + " with new Name:" + newName);
			}
		}
		ClusterSet headClusterSet = clusterSet.getHeadClusterSet();
		if (headClusterSet != null) {
			for (String idx : headClusterSet) {
				Cluster cluster = headClusterSet.getCluster(idx);
				String name = cluster.getName();
				if (name.matches("C[0-9]+")) {
					String newName = name.replaceFirst("C", "speaker#");
					cluster.setName(newName);
					logger.info("HEAD remplace name: " + name + " with new Name:" + newName);
				}
			}
		}
	}

	/**
	 * Selection.
	 * 
	 * @param clusterSet the cluster set
	 */
	public static void selection(ClusterSet clusterSet) {
		logger.info("------ selection ------");
		double thr = parameter.getParameterNamedSpeaker().getThresholdDecision();
		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			double max = -Double.MAX_VALUE;
			String maxName = cluster.getName();
			String nameKey = "";

			for (int i = 0; i < nameKeys.length; i++) {
				if (cluster.getInformation(nameKeys[i]) != "") {
					if (Double.valueOf(cluster.getInformation(scoreKeys[i])) > max) {
						max = Double.valueOf(cluster.getInformation(scoreKeys[i]));
						maxName = cluster.getInformation(nameKeys[i]);
						nameKey = nameKeys[i];
					}
				}
			}
			if (max > thr) {
				logger.info("SET cluster:" + name + " Name:" + maxName + " score:" + max + "(" + thr + ") key:"
						+ nameKey);
				cluster.setName(maxName);
			}
		}
	}

	/**
	 * Merge cluster set.
	 * 
	 * @param clusterSet the cluster set
	 */
	public static void mergeClusterSet(ClusterSet clusterSet) {
		ArrayList<String> array = new ArrayList<String>(clusterSet.getClusterMap().keySet());

		int i = 1;
		while (i < array.size()) {
			String nameP = array.get(i - 1);
			String name = array.get(i);

			Cluster clusterP = clusterSet.getCluster(nameP);
			Cluster cluster = clusterSet.getCluster(name);

			// logger.info("*** nameP: "+nameP+" ("+clusterP+") name:"+name+" ("+cluster+")");

			if (clusterP.getName().equals(cluster.getName())) {
				logger.info("*** merge i:" + i + " nameP: " + nameP + " (" + clusterP + ") name:" + name + " ("
						+ cluster + ")");
				clusterSet.mergeCluster(nameP, name);
				array.remove(i);
			} else {
				i++;
			}
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
			info(parameter, "SpeakerIdenificationDecision");
			if (parameter.show.isEmpty() == false) {

				PREVIOUS_THRESHOLD = CURRENT_THRESHOLD = NEXT_THRESHOLD = parameter.getParameterNamedSpeaker().getThresholdTranscription();

				ClusterSet audioClusterSet = MainTools.readClusterSet(parameter);

				audioClusterSet.collapse();
				audioClusterSet.getHeadClusterSet().collapse(5);
				// get the speaker name list
				nameAndGenderMap = SpeakerNameUtils.loadList(parameter.getParameterNamedSpeaker().getNameAndGenderList());

				firstNameAndGenderMap = null;
				if (parameter.getParameterNamedSpeaker().isFirstNameCheck()) {
					firstNameAndGenderMap = SpeakerNameUtils.loadList(parameter.getParameterNamedSpeaker().getFirstNameList());
				}

				logger.info("+++ AUDIO ++++++++++++++++++++++");
				// Trans
				putTranscriptionScore(audioClusterSet);
				putWriting(audioClusterSet, parameter.getParameterNamedSpeaker().getThresholdWriting());
				writtingSplitCluster(audioClusterSet);
				putAudioScore(audioClusterSet);
				computeBeliefFunctions(audioClusterSet);
				ClusterSet afterAudio = decideMaximum(audioClusterSet, scoreKeys[0], nameKeys[0]);

				// ClusterSet afterAudio = decideHungarian(audioClusterSet, scoreKeys[0], nameKeys[0]);

				logger.info("+++ DECISION ++++++++++++++++++++++");
				selection(afterAudio);

				logger.info("After Association =============================");
				printClusterName(afterAudio);
				MainTools.writeClusterSet(parameter, afterAudio, true);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "exception ", e);
			e.printStackTrace();
		}
	}

}
