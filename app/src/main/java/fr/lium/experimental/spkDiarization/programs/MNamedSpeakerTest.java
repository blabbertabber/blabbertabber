/**
 * 
 * <p>
 * SNamedSpeaker2
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
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import www.spatial.maine.edu.assignment.HungarianAlgorithm;
import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerName;
import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerNameSet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.Turn;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.TurnSet;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.TargetNameMap;
import fr.lium.experimental.spkDiarization.libSCTree.SCT;
import fr.lium.experimental.spkDiarization.libSCTree.SCTProbabilities;
import fr.lium.experimental.spkDiarization.libSCTree.SCTSolution;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.programs.Identification;

/**
 * The Class MNamedSpeakerTest.
 * 
 * @author Meignier, Jousse
 */
public class MNamedSpeakerTest {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MNamedSpeakerTest.class.getName());

	/** The Constant PREVIOUS_THRESHOLD. */
	public static final double PREVIOUS_THRESHOLD = 0.05;

	/** The Constant CURRENT_THRESHOLD. */
	public static final double CURRENT_THRESHOLD = 0.05;

	/** The Constant NEXT_THRESHOLD. */
	public static final double NEXT_THRESHOLD = 0.05;
	// public static final double PREVIOUS_THRESHOLD = 0.09;
	// public static final double CURRENT_THRESHOLD = 0.2;
	// public static final double NEXT_THRESHOLD = 0.2;
	/** The parameter. */
	static Parameter parameter;

	/** The name and gender map. */
	static TargetNameMap nameAndGenderMap;

	/** The first name and gender map. */
	static TargetNameMap firstNameAndGenderMap;

	/** The next false. */
	static int nextFalse = 0;

	/** The next total. */
	static int nextTotal = 0;

	/** The previous false. */
	static int previousFalse = 0;

	/** The previous total. */
	static int previousTotal = 0;

	/** The current false. */
	static int currentFalse = 0;

	/** The current total. */
	static int currentTotal = 0;

	/** The other false. */
	static int otherFalse = 0;

	/** The other total. */
	static int otherTotal = 0;

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
			logger.config(parameter.getSeparator());
			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			parameter.getParameterTopGaussian().logTopGaussian(); // sTop
			logger.config(parameter.getSeparator());
			parameter.getParameterScore().logAll();
		}
	}

	/**
	 * For each Solution of the SolutionsSet, put probabilities in the Cluster of the previous, current and next Turn for the target speaker.
	 * 
	 * @param solution a SCT solution
	 * @param speakerName the name of the target speaker
	 * @param turnSet list of turn
	 * @param index index of the current turn in turns
	 * 
	 *            TODO make permutation of speaker name word (firstname/lastname - lastname/firstname)
	 */
	public static void putSpeakerName(SCTSolution solution, String speakerName, TurnSet turnSet, int index) {
		SCTProbabilities probabilities = solution.getProbabilities();
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
		boolean maximum = parameter.getParameterNamedSpeaker().isMaximum();

		String nextName = "";
		String previousName = "";
		String currentName = "";

		String maxKey = probabilities.getMaxKey();

		// previous turn
		if ((index - 1) >= 0) {
			turn = turnSet.get(index - 1);
			previousName = SpeakerNameUtils.normalizeSpeakerName(turn.getCluster().getName());
			scorePrev = probabilities.get(SpeakerNameUtils.PREVIOUS);
			if ((checkGender(turn, speakerGender) == true)
					&& ((!maximum && (scorePrev > PREVIOUS_THRESHOLD)) || (maximum && maxKey.equals(SpeakerNameUtils.PREVIOUS)))) {
				logger.finest("SCT cluster: " + turn.getCluster().getName() + " speaker name:" + speakerName
						+ " add score: " + scorePrev + " PUT ON PREVIOUS " + turn.first().getStart());
				addScore(turn, speakerName, scorePrev);
			}
		}

		// Current turn
		turn = turnSet.get(index);
		// float turnStart = turn.first().getStartInSecond();
		// float turnEnd = turn.last().getStartInSecond() + turn.last().getLengthInSecond();
		// String gender = turn.first().getCluster().getGender();
		currentName = SpeakerNameUtils.normalizeSpeakerName(turn.getCluster().getName());
		scoreCurrent = probabilities.get(SpeakerNameUtils.CURRENT);
		if ((checkGender(turn, speakerGender) == true)
				&& ((!maximum && (scoreCurrent > CURRENT_THRESHOLD)) || (maximum && maxKey.equals(SpeakerNameUtils.CURRENT)))) {
			logger.finest("SCT cluster: " + turn.getCluster().getName() + " speaker name:" + speakerName
					+ " add score: " + scoreCurrent + " PUT ON CURRENT " + turn.first().getStart());
			addScore(turn, speakerName, scoreCurrent);
		}

		// newt turn
		if ((index + 1) < turnSet.size()) {
			turn = turnSet.get(index + 1);
			scoreNext = probabilities.get(SpeakerNameUtils.NEXT);
			nextName = SpeakerNameUtils.normalizeSpeakerName(turn.getCluster().getName());

			// if ((checkGender(turn, speakerGender) == true) && (scoreNext > NEXT_THRESHOLD)) {
			if ((checkGender(turn, speakerGender) == true)
					&& ((!maximum && (scoreNext > NEXT_THRESHOLD)) || (maximum && maxKey.equals(SpeakerNameUtils.NEXT)))) {
				logger.finest("SCT cluster: " + turn.getCluster().getName() + " speaker name:" + speakerName
						+ " add score: " + scoreNext + " PUT ON NEXT " + turn.first().getStart());
				addScore(turn, speakerName, scoreNext);
			}
		}
		speakerName = SpeakerNameUtils.normalizeSpeakerName(speakerName);

		if (maxKey.equals(SpeakerNameUtils.NEXT)) {
			if (!speakerName.equals(nextName)) {
				nextFalse++;
			}
			nextTotal++;
		}

		if (maxKey.equals(SpeakerNameUtils.CURRENT)) {
			if (!speakerName.equals(currentName)) {
				currentFalse++;
			}
			currentTotal++;
		}

		if (maxKey.equals(SpeakerNameUtils.PREVIOUS)) {
			if (!speakerName.equals(previousName)) {
				previousFalse++;
			}
			previousTotal++;
		}

		if (maxKey.equals(SpeakerNameUtils.OTHER)) {
			if (speakerName.equals(nextName) || speakerName.equals(previousName) || speakerName.equals(currentName)) {
				otherFalse++;
			}
			otherTotal++;
		}

		// logger.info("DETECTED :" + SpeakerNameUtils.normalizeSpeakerName(speakerName) + ", PREVIOUS previous score: "
		// + scorePrev);
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

		// Keeping the old way (just summing the score)
		speakerName.incrementScoreCluster(value);

		// Adding the new way, will keep trace of each score
		speakerName.addScoreCluster(value);
	}

	/**
	 * Test the SCT over each segment containing a linkSet and an entity. The result of the SCT (speaker name and probability) are stored in the clusters of the previous, current or next turn.
	 * 
	 * @param clusterSet the cluster set
	 * @param sct the sct
	 * @param targetSpeakerNameMap the target speaker name map
	 * @throws CloneNotSupportedException the clone not supported exception
	 * @throws DiarizationException TODO manager open and close speaker list
	 */
	public static void computeSCTSCore(ClusterSet clusterSet, SCT sct, TargetNameMap targetSpeakerNameMap) throws CloneNotSupportedException, DiarizationException {
		TurnSet turns = clusterSet.getTurns();
		boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		for (int i = 0; i < turns.size(); i++) {
			// logger.info("+++++++++++++++++++++++++++++++++++++++++++++++");
			Turn currentTurn = turns.get(i);
			LinkSet linkSet = currentTurn.getCollapsedLinkSet();
			// linkSet.debug();
			boolean startTurn = true;
			boolean endTurn = true;
			SpeakerNameUtils.makeLinkSetForSCT(linkSet, startTurn, endTurn);
			// logger.info("*" + currentTurn.first().getStartInSecond() + "/" + currentTurn.last().getLastInSecond() + "/"
			// + currentTurn.getCluster().getName() + "***********************************************");
			for (int index = 0; index < linkSet.size(); index++) {
				Link link = linkSet.getLink(index);
				if (link.haveEntity(EntitySet.TypePersonne) == true) {
					LinkSet linkSetForTest = SpeakerNameUtils.reduceLinkSetForSCT(linkSet, index, 5, startTurn, endTurn, true);
					// logger.info(currentTurn.first().getStart() + " -------------------------------------------------");
					// linkSetForTest.debug();
					String speakerName = link.getWord();
					if (SpeakerNameUtils.checkSpeakerName(speakerName, isCloseListCheck, nameAndGenderMap, firstNameAndGenderMap) == true) {
						// logger.info("test speaker:" + speakerName);
						SCTSolution solution = sct.test(linkSetForTest);
						String ch = "";
						for (Double v : solution.getProbabilities().values()) {
							ch += " " + v;
						}

						logger.info("@@ trun:" + i + "/" + currentTurn.get(0).getStartInSecond() + " name:"
								+ speakerName + " proba:" + ch);
						// logger.info("put solution speaker:" + speakerName);
						putSpeakerName(solution, speakerName, turns, i);
					}
				}
			}

		}
	}

	/**
	 * Assign the candidate speaker name to cluster. Just sort the cluster according to the max
	 * 
	 * @param clusterSet the clusters
	 * @param clusterSetResult the cluster set result
	 * @return the a new cluster set
	 */
	public static ClusterSet decideMaximumFirst(ClusterSet clusterSet, ClusterSet clusterSetResult) {

		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			// logger.info("decide: Cluster = " + cluster.getName() + " ");
			if (SpkDiarizationLogger.DEBUG) cluster.getSpeakerNameSet().debug();
		}

		SpeakerName max = new SpeakerName("");
		int size = clusterSet.clusterGetSize();
		for (int i = 0; i < size; i++) {
			Cluster cluster = getMaxSpeakerName(clusterSet, max);
			if (cluster == null) {
				break;
			}
			String newName = SpeakerNameUtils.normalizeSpeakerName(max.getName().replace(' ', '_').toLowerCase());
			int dist = Distance.levenshteinDistance(cluster.getName(), max.getName());
			logger.info("decide: Cluster = " + cluster.getName() + " --> " + newName + " lenvenshtein=" + dist
					+ " score=" + max.getScore() + " || ");
			if (SpkDiarizationLogger.DEBUG) cluster.getSpeakerNameSet().debug();
			clusterSetResult.getCluster(cluster.getName()).setName(newName);
			clusterSet.removeCluster(cluster.getName());
			for (String name : clusterSet) {
				clusterSet.getCluster(name).RemoveSpeakerName(max.getName());
			}
		}

		String unk = "unk";
		clusterSetResult.createANewCluster(unk);
		for (String name : clusterSet) {
			// logger.info("decide: create unk");
			clusterSetResult.mergeCluster(unk, name);
		}

		return clusterSetResult;
	}

	/**
	 * Assign the candidate speaker name to cluster. Use the Hungarian Algorithm
	 * 
	 * @param clusterSet the clusters
	 * @param clusterSetResult the cluster set result
	 * @return the a new cluster set
	 */
	public static ClusterSet decideHungarian(ClusterSet clusterSet, ClusterSet clusterSetResult) {

		logger.finest("Enter decideHungarian");
		TreeMap<Cluster, Integer> clusterIndexMap = new TreeMap<Cluster, Integer>();
		TreeMap<String, Integer> speakerNameIndexMap = new TreeMap<String, Integer>();
		TreeMap<Integer, Cluster> reverseClusterIndexMap = new TreeMap<Integer, Cluster>();
		TreeMap<Integer, String> reverseSpeakerNameIndexMap = new TreeMap<Integer, String>();

		int clusterIndex = 0;
		int spkIndex = 0;

		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			SpeakerNameSet spkNameSet = cluster.getSpeakerNameSet();
			if (spkNameSet.size() == 0) {
				continue;
			}

			clusterIndexMap.put(cluster, clusterIndex);
			reverseClusterIndexMap.put(clusterIndex, cluster);

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

		double[][] costMatrix = new double[clusterIndexMap.size()][speakerNameIndexMap.size()];

		// Start by assigning a great value to each entry (worst value)
		for (int i = 0; i < clusterIndexMap.size(); i++) {
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

			clusterIndex = clusterIndexMap.get(cluster);
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
				Cluster cluster;
				String newName;
				if (!transposed) {
					cluster = reverseClusterIndexMap.get(element[0]);
					newName = reverseSpeakerNameIndexMap.get(element[1]);
				} else {
					cluster = reverseClusterIndexMap.get(element[1]);
					newName = reverseSpeakerNameIndexMap.get(element[0]);
				}
				logger.info(String.format("array(%d,%s %s=>%d,%s %s) = %.2f", (element[0]), cluster.getName(), cluster.getGender(), (element[1]), newName, nameAndGenderMap.get(newName), costMatrix[element[0]][element[1]]).toString());
				sum = sum + costMatrix[element[0]][element[1]];

				if (costMatrix[element[0]][element[1]] > 0) {
					clusterSetResult.getCluster(cluster.getName()).setName(newName);
					clusterSet.removeCluster(cluster.getName());
				}

				for (String name : clusterSet) {
					clusterSet.getCluster(name).RemoveSpeakerName(newName);
				}
				// </COMMENT>
			}

			logger.finest(String.format("\nThe %s is: %.2f\n", sumType, sum).toString());
			// HungarianAlgorithm.printTime((endTime - startTime) / 1000000000.0);
		}

		String unk = "unk";
		clusterSetResult.createANewCluster(unk);
		for (String name : clusterSet) {
			// logger.info("decide: create unk");
			clusterSetResult.mergeCluster(unk, name);
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

	/**
	 * Gets the maximum score of the SpeakerNameSet instance of each cluster instance stored in clusters.
	 * 
	 * @param clusterSet the clusters in with the cluster with the maximum score is searched
	 * @param max the max score SpeakerName instance, this is an output
	 * 
	 * @return the cluster with the maximum score
	 */
	public static Cluster getMaxSpeakerName(ClusterSet clusterSet, SpeakerName max) {
		Cluster maxCluster = null;
		max.set("", Double.NEGATIVE_INFINITY, 1.0);
		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			SpeakerName tmp = cluster.getMaxSpeakerName();
			if (tmp != null) {
				if (max.getScore() < tmp.getScore()) {
					max.set(tmp.getName(), tmp.getScore(), 1.0);
					maxCluster = cluster;
				}
			}
		}
		return maxCluster;
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
		for (String name : clusters) {
			Cluster cluster = clusters.getCluster(name);
			cluster.computeBeliefFunctions();
			cluster.debugSpeakerName();
		}
	}

	/*
	 * Is called to normalized each summed score It computes the score of a speaker for a cluster using normalization
	 */
	/**
	 * Sets the sum score.
	 * 
	 * @param clusters the new sum score
	 */
	public static void setSumScore(ClusterSet clusters) {
	}

	/*
	 * Is called to normalized each summed score It computes the score of a speaker for a cluster using normalization
	 */
	/**
	 * Sets the score.
	 * 
	 * @param clusterSet the new score
	 */
	public static void setScore(ClusterSet clusterSet) {

		// Normalize the speakers scores
		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			cluster.computeNormalizedScore();
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
			info(parameter, "MNamedSpeakerTest");
			if (parameter.show.isEmpty() == false) {
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
				ClusterSet clusterSetSave = clusterSet.clone();
				clusterSet.collapse();
				// get the speaker name list
				nameAndGenderMap = null;
				nameAndGenderMap = SpeakerNameUtils.loadList(parameter.getParameterNamedSpeaker().getNameAndGenderList());

				firstNameAndGenderMap = null;

				if (parameter.getParameterNamedSpeaker().isFirstNameCheck()) {
					firstNameAndGenderMap = SpeakerNameUtils.loadList(parameter.getParameterNamedSpeaker().getFirstNameList());
				}

				SCT sct = new SCT(SpeakerNameUtils.getNbOfLabel());
				sct.read(parameter.show, parameter.getParameterNamedSpeaker().getSCTMask());
				computeSCTSCore(clusterSet, sct, nameAndGenderMap);

				if (parameter.getParameterNamedSpeaker().isUseAudio()) {
					// Features
					AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
					// Top Gaussian model
					GMMArrayList gmmTopGaussianList = MainTools.readGMMForTopGaussian(parameter, featureSet);
					GMMArrayList gmmList = MainTools.readGMMContainer(parameter);
					clusterSet = Identification.make(featureSet, clusterSet, gmmList, gmmTopGaussianList, parameter);
				}

				if (parameter.getParameterNamedSpeaker().isBeliefFunctions()) {
					// Put the code for the belief functions
					computeBeliefFunctions(clusterSet);
				} else if (parameter.getParameterNamedSpeaker().isMaximum()) {
					// Nothing
				} else { // decide on score accumulation
					for (String name : clusterSet) {
						Cluster cluster = clusterSet.getCluster(name);
						if (SpkDiarizationLogger.DEBUG) cluster.getSpeakerNameSet().debug();
					}
					setScore(clusterSet);
				}

				if (parameter.getParameterNamedSpeaker().isHungarian()) {
					clusterSet = decideHungarian(clusterSet, clusterSetSave);
				} else {
					clusterSet = decideMaximumFirst(clusterSet, clusterSetSave);
				}

				// printStat();
				MainTools.writeClusterSet(parameter, clusterSetSave, true);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "exception ", e);
			e.printStackTrace();
		}
	}

	/**
	 * Prints the stat.
	 */
	protected static void printStat() {
		logger.info("Hungarian " + parameter.getParameterNamedSpeaker().isHungarian());
		logger.info("Maximum " + parameter.getParameterNamedSpeaker().isMaximum());
		logger.info("+++++++++++++++++++++++++++++++++++++++++++++++");
		logger.info("SCT STATS " + parameter.show);
		logger.info("SCT STATS Next false : " + nextFalse + " / " + nextTotal);
		logger.info("SCT STATS Previous false : " + previousFalse + " / " + previousTotal);
		logger.info("SCT STATS Current false : " + currentFalse + " / " + currentTotal);
		logger.info("SCT STATS Other false : " + otherFalse + " / " + otherTotal);
	}

}
