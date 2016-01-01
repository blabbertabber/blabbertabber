/**
 * 
 * <p>
 * SpeakerIndentification
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */
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
import fr.lium.experimental.spkDiarization.libNamedSpeaker.AssociatioAudioVideo;
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
 * The Class SpeakerIdenificationDecision13.
 * 
 * @author Meignier, Jousse
 * 
 *         test : cumuler tous les scores avant de prendre une décision meilleur système au 9 janvier 2012
 */
public class SpeakerIdenificationDecision13 {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SpeakerIdenificationDecision13.class.getName());

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
	static TreeSet<String> personneList;

	/** The first name and gender map. */
	static TargetNameMap firstNameAndGenderMap;

	/** The score keys. */
	static String scoreKeys[] = { "audio-score", "trans-score", "writing_audio-score", "writing_video-score",
			"head-score", "video-score" };

	/** The name keys. */
	static String nameKeys[] = { "audio-name", "trans-name", "writing_audio-name", "writing_video-name", "head-name",
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

		Turn turn;
		double scorePrev = 0, scoreCurrent = 0, scoreNext = 0.0;

		// previous turn
		if ((index - 1) >= 0) {
			turn = turnSet.get(index - 1);
			scorePrev = probabilities.get(SpeakerNameUtils.PREVIOUS);
			if ((checkGender(turn, speakerGender) == true) && (scorePrev > PREVIOUS_THRESHOLD)) {
				logger.info("ACCEPT trans: " + turn.getCluster().getName() + " --> " + speakerName + " = " + scorePrev
						+ " previous= " + turn.first().getStart());
				addScore(turn, speakerName, scorePrev);
			}
		}

		// Current turn
		turn = turnSet.get(index);
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

		if (SpeakerNameUtils.getNbOfLabel() > 4) {
			score = probabilities.get(SpeakerNameUtils.INSHOW);
			if (score > scoreMax) {
				scoreMax = score;
				label = SpeakerNameUtils.INSHOW;
			}
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
		speakerName.incrementScoreCluster(value);
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
								String person = SpeakerNameUtils.normalizeSpeakerName(identity);
								SpeakerName speakerName = cluster.getSpeakerName(person);
								speakerName.addScoreCluster(score);
								speakerName.incrementScoreCluster(score);
								personneList.add(person);

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
					speakerName.incrementScoreCluster(max);
					logger.info("ACCEPT Audio MAX name max : " + cluster.getName() + ") --> " + maxIdentity + " = "
							+ max + " (" + thr + ")");
				}
			}
		}
	}

	/**
	 * Filter video score.
	 * 
	 * @param modelScores the model scores
	 * @return the model scores
	 */
	public static ModelScores filterVideoScore(ModelScores modelScores) {
		ModelScores filteredModelScores = new ModelScores();

		for (String identity : personneList) {
			String id = SpeakerNameUtils.normalizeSpeakerName(identity);
			if (modelScores.containsKey(id) == true) {
				logger.info("found name: " + id + " score: " + modelScores.get(id));
				filteredModelScores.put(id, modelScores.get(id));
				// } else {
				// logger.info("not found name: "+id+" score: "+modelScores.get(id));

			}
		}
		return filteredModelScores;
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
				ModelScores modelScores = filterVideoScore(cluster.getModelScores());

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
					speakerName.incrementScoreCluster(max);
					logger.info("ACCEPT Video name max : " + cluster.getName() + ") --> " + maxIdentity + " = " + max
							+ " (" + thr + ")");
				} else {
					logger.info("REJECT Video name max : " + cluster.getName() + ") --> " + maxIdentity + " = " + max
							+ " (" + thr + ")");

				}
			}
		} else {
			logger.info("vidéo not use");
		}
	}

/*
 * public static void putHeadScore(ClusterSet clusterSet) { logger.info("------ Use Video ------"); if (parameter.getParameterNamedSpeaker().isUseVideo()) { for (String name : clusterSet) { Cluster cluster = clusterSet.getCluster(name); // try to get
 * the audio cluster // score is put in audio if the video cluster name is equal to a audio cluster name // in this case, we suppose that head and speaker is link ModelScores modelScores = cluster.getModelScores(); for (String identity :
 * modelScores.keySet()) { double score = modelScores.get(identity); if (score > parameter.getParameterNamedSpeaker().getThresholdVideo()) { SpeakerName speakerName = cluster.getSpeakerName(SpeakerNameUtils.normalizeSpeakerName(identity));
 * speakerName.addScoreCluster(score); } } } } }
 */
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

		// double thr = parameter.getParameterNamedSpeaker().getThresholdWriting();
		if (parameter.getParameterNamedSpeaker().isUseWriting()) {
			Cluster writing = audioClusterSet.getWriting();

			for (Segment writingSegment : writing) {
				writingSegment.setInformation("use", "no");

				EntitySet entitySet = writingSegment.getTranscription().getEntitySet();
				for (Entity entity : entitySet) {
					if ((entity.isPerson() == true) && (entity.start() == 0)) {
						String name = entity.getListOfWords();
						String person = SpeakerNameUtils.normalizeSpeakerName(name);
						if (SpeakerNameUtils.checkSpeakerName(person, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) == true) {
							personneList.add(person);
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
								SpeakerName speakerName = cluster.getSpeakerName(person);
								speakerName.addScoreCluster(thr);
								speakerName.incrementScoreCluster(thr);
								writingSegment.setInformation("use", "yes");
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
	 * Put writing2.
	 * 
	 * @param audioClusterSet the audio cluster set
	 * @param thr the thr
	 */
	public static void putWriting2(ClusterSet audioClusterSet, double thr) {
		// stratégie : si l'EN pers est co
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		logger.info("------ Use Writing2 ------");

		// double thr = parameter.getParameterNamedSpeaker().getThresholdWriting();
		if (parameter.getParameterNamedSpeaker().isUseWriting()) {
			Cluster writing = audioClusterSet.getWriting();

			for (Segment writingSegment : writing) {
				if (writingSegment.getInformation("use").equals("no") == true) {
					EntitySet entitySet = writingSegment.getTranscription().getEntitySet();
					for (Entity entity : entitySet) {
						if ((entity.isPerson() == true) && (entity.start() == 0)) {
							String name = entity.getListOfWords();
							String person = SpeakerNameUtils.normalizeSpeakerName(name);
							if ((audioClusterSet.getCluster(person) == null)
									&& (SpeakerNameUtils.checkSpeakerName(person, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) == true)) {
								int nbMatch = 0;
								double maxRate = 0;
								Segment matchSegment = null;

								for (Cluster cluster : audioClusterSet.getHeadClusterSet().getClusterVectorRepresentation()) {
									if (cluster.getInformation("XMLSpeakerIdentity").isEmpty() == true) {
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
								}
								if ((nbMatch == 1) && (maxRate > thr)) {
									Cluster cluster = matchSegment.getCluster();
									SpeakerName speakerName = cluster.getSpeakerName(person);
									speakerName.addScoreCluster(thr);
									speakerName.incrementScoreCluster(thr);
									logger.info("ACCEPT WRITING2 name : " + cluster.getName() + " --> " + person
											+ " = " + maxRate);
								} else {
									logger.info("REJECT WRITING2 name :  UNK --> " + person + " = " + thr
											+ " / nbMatch: " + nbMatch + " maxRate:" + maxRate);
								}
							} else {
								logger.info("REJECT WRITING2 name : " + person + " / list");
							}
						}
					}
				}
			}
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
							personneList.add(SpeakerNameUtils.normalizeSpeakerName(normalizedName));
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
			logger.fine("Cluster: " + name);
			cluster.debugSpeakerName();
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
					logger.info("selection cluster:" + name + " Name:" + cluster.getInformation(nameKeys[i])
							+ " score:" + cluster.getInformation(scoreKeys[i]) + "(" + thr + ") key:" + nameKeys[i]);
					if (Double.valueOf(cluster.getInformation(scoreKeys[i])) > max) {
						max = Double.valueOf(cluster.getInformation(scoreKeys[i]));
						maxName = cluster.getInformation(nameKeys[i]);
						nameKey = nameKeys[i];
					}
				}
			}
			if (max > thr) {
				logger.info("\tSET cluster:" + name + " Name:" + maxName + " score:" + max + "(" + thr + ") key:"
						+ nameKey);
				cluster.setInformation("old_name", cluster.getName());
				cluster.setName(maxName);
				cluster.setInformation("XMLSpeakerIdentity", maxName);

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
	 * Compute normalized score.
	 * 
	 * @param clusters the clusters
	 * @throws Exception the exception
	 */
	public static void computeNormalizedScore(ClusterSet clusters) throws Exception {
		logger.info("------ compute Sum ------");
		for (String name : clusters) {
			Cluster cluster = clusters.getCluster(name);

			cluster.computeNormalizedScore();
			logger.fine("Cluster: " + name);
			cluster.debugSpeakerName();
		}
	}

	/**
	 * Compute mean score.
	 * 
	 * @param clusters the clusters
	 * @throws Exception the exception
	 */
	public static void computeMeanScore(ClusterSet clusters) throws Exception {
		logger.info("------ compute Sum ------");
		for (String name : clusters) {
			Cluster cluster = clusters.getCluster(name);

			cluster.computeMeanScore();
			logger.fine("Cluster: " + name);
			cluster.debugSpeakerName();
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
			personneList = new TreeSet<String>();
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
				putAudioScoreMax(audioClusterSet);
				computeBeliefFunctions(audioClusterSet);
				// computeNormalizedScore(audioClusterSet);
				ClusterSet afterAudio = decideMaximum(audioClusterSet, scoreKeys[0], nameKeys[0]);

				logger.info("+++ VIDEO ++++++++++++++++++++++");

				parameter.getParameterNamedSpeaker().setDontCheckGender(true);
				ClusterSet videoClusterSet = afterAudio.getHeadClusterSet();
				videoClusterSet.setWriting(afterAudio.getWriting());

				// putTranscriptionScore(videoClusterSet);
				putWriting(videoClusterSet, parameter.getParameterNamedSpeaker().getThresholdVideo());
				// putVideoScore(videoClusterSet);
				computeBeliefFunctions(videoClusterSet);
				videoClusterSet = decideMaximum(videoClusterSet, scoreKeys[0], nameKeys[0]);

				logger.info("+++ DECISION ++++++++++++++++++++++");
				selection(afterAudio);
				selection(videoClusterSet);
				afterAudio.setHeadClusterSet(videoClusterSet);

				AssociatioAudioVideo.assignSpeakerToHead(afterAudio);

				/*
				 * logger.info("+++ Passe 2 ++++++++++++++++++++++"); for (String name : afterAudio.getHeadClusterSet()) { afterAudio.getHeadClusterSet().getCluster(name).clearSpeakerNameSet(); } putWriting2(afterAudio,
				 * parameter.getParameterNamedSpeaker().getThresholdWriting()); computeBeliefFunctions(afterAudio.getHeadClusterSet()); ClusterSet videoClusterSet = decideMaximum(afterAudio.getHeadClusterSet(), scoreKeys[0], nameKeys[0]);
				 * selection(videoClusterSet); afterAudio.setHeadClusterSet(videoClusterSet);
				 */

				logger.info("After Association =============================");
				setAnonymous(afterAudio);
				printClusterName(afterAudio);
				MainTools.writeClusterSet(parameter, afterAudio, true);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "exception ", e);
			e.printStackTrace();
		}
	}

}
