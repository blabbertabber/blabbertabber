package fr.lium.deprecated.spkDiarization;

/**
 * 
 * <p>
 * DecoderWithDuration
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          Viterbi decoder class, duration low by duplication of the state.
 * 
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.TreeMap;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.Model;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterDecoder;

/**
 * The Class FastDecoderWithDurationTest.
 */
public class FastDecoderWithDurationTest {

	/** UBM for nTop. */
	protected GMM UBM;

	/** nTop value. */
	protected int nbTopGaussians;

	/** list of models. */
	protected ArrayList<Model> models;

	/** a state stores the index of the corresponding model. */
	protected ArrayList<Integer> states;

	/** duration constraint for each model (see constants below). */
	protected ArrayList<Integer> modelDurationConstraints;

	/** value associated with each constraint (typically a duration, expressed in number of features). */
	protected ArrayList<Integer> durationConstraintsValues;

	/** index of the first state for each model/HMM. */
	protected ArrayList<Integer> modelEntryStateIndices;

	/** list of the states in the middle of the HMM. */
	protected ArrayList<Integer[]> modelMiddleStateList;

	/** index of the last state for each model/HMM. */
	protected ArrayList<Integer> modelEndStateIndices;

	/** exit hmm penality. */
	protected ArrayList<Double> exitPenalties;

	/** loop hmm penality. */
	protected ArrayList<Double> loopPenalties;

	/** the current top gaussian feature index, ie the feature index in the feature set. */
	protected int currentTopGaussianFeatureIndex;

	/** the current top gaussian score corresponding to currentTopGaussianFeatureIndex index. */
	protected double currentTopGaussianScore;

	/** list of scores of the current feature. */
	protected double modelScores[];

	/** list of scores of the previous feature. */
	protected double modelScoresPrevious[];

	/** structure to save topGaussians. */
	protected TreeMap<Integer, int[]> topGaussianIndices;

	/** keeps track of whether the transition matrix needs to be (re)filled and data container needs to get cleared. */
	protected boolean initializationRequired;

	/** shift by shift computation of feature. */
	protected int shift;

	/** compute log likelihood ratio or just log likelihood. */
	protected boolean computeLLhR;

	/** HMM without duration constraint, one state per HMM. */
	static final protected int NO_DURATION_CONSTRAINT = 0;

	/** HMM with n states linked, with only transition i-->i on the last, exit state only on the last. */
	static final protected int MINIMAL_DURATION_CONSTRAINT = 1;

	/** ?? HMM with n states linked, the last is linked to the first, exit state only on the last. */
	static final protected int FIXED_DURATION_CONSTRAINT = 2;

	/** HMM with n states linked, exit state only on the last. */
	static final protected int PERIODIC_DURATION_CONSTRAINT = 4;

	// ----
	/** accumulate internal variables. */
	private double previousScores[];

	/** The current scores. */
	private double currentScores[];
	// private int previousFeatures[];
	// private int currentFeatures[];
	// private LinkedList<PathInfo[]> previousPath;

	/** structure to store viterbi path. */
	private TreeMap<Integer, Integer> path;

	/** The segment list. */
	protected ArrayList<Segment> segmentList;

	/*
	 * private class PathInfo{ int featureIndex; int[] viterbi; String showName; public PathInfo(String showName, int featureIndex, int[] viterbi) { super(); this.showName = showName; this.featureIndex = featureIndex; this.viterbi = viterbi; } }
	 * private LinkedList<PathInfo> path;
	 */

	/** data used in forward pass; the keys are feature indices. */
	protected TreeMap<Integer, int[]> data;

	/**
	 * list of segment.
	 * 
	 * @param shift the shift
	 */
	// protected ArrayList<Segment> segmentList;

	/*
	 * protected class PathInfo{ private int last; private int start; private int model; private double score; private LinkedList<Segment> segmentList; private PathInfo previousPathInfo; public PathInfo(int start, int last, int model, double score,
	 * LinkedList<Segment> list, PathInfo previousPathInfo) { super(); this.last = last; this.start = start; this.model = model; this.score = score; segmentList = list; this.previousPathInfo = previousPathInfo; } public int getLast() { return last; }
	 * public int getStart() { return start; } public int getModel() { return model; } public double getScore() { return score; } public Segment getFirstSegment() { return segmentList.getFirst(); } public PathInfo getPreviousPathInfo() { return
	 * previousPathInfo; } public void addSegment(Segment segment){ segmentList.addLast(segment); } }
	 */

	// protected LinkedList<PathInfo> pathList;

	/**
	 * Create a decoder, without using top Gaussians.
	 */
	public FastDecoderWithDurationTest(int shift) {
		nbTopGaussians = -1;
		UBM = null;
		models = new ArrayList<Model>();
		states = new ArrayList<Integer>();
		modelDurationConstraints = new ArrayList<Integer>();
		durationConstraintsValues = new ArrayList<Integer>();
		modelEntryStateIndices = new ArrayList<Integer>();
		exitPenalties = new ArrayList<Double>();
		loopPenalties = new ArrayList<Double>();
		initializationRequired = true;
		this.shift = shift;
		computeLLhR = false;
		topGaussianIndices = new TreeMap<Integer, int[]>();
	}

	/**
	 * Create a decoder, using top Gaussians for likelihood computation.
	 * 
	 * @param n the number of top Gaussians to use
	 * @param ubm the UBM for the top Gaussians
	 * @param computeLLhR the compute l lh r
	 * @param shift the shift
	 */
	public FastDecoderWithDurationTest(int n, GMM ubm, boolean computeLLhR, int shift) {
		nbTopGaussians = n;
		UBM = ubm;
		currentTopGaussianFeatureIndex = -1;
		models = new ArrayList<Model>();
		states = new ArrayList<Integer>();
		modelDurationConstraints = new ArrayList<Integer>();
		durationConstraintsValues = new ArrayList<Integer>();
		modelEntryStateIndices = new ArrayList<Integer>();
		exitPenalties = new ArrayList<Double>();
		loopPenalties = new ArrayList<Double>();
		initializationRequired = true;
		this.shift = shift;
		this.computeLLhR = computeLLhR;
		topGaussianIndices = new TreeMap<Integer, int[]>();
	}

	/**
	 * Instantiates a new fast decoder with duration test.
	 */
	public FastDecoderWithDurationTest() {
		nbTopGaussians = -1;
		UBM = null;
		models = new ArrayList<Model>();
		states = new ArrayList<Integer>();
		modelDurationConstraints = new ArrayList<Integer>();
		durationConstraintsValues = new ArrayList<Integer>();
		modelEntryStateIndices = new ArrayList<Integer>();
		exitPenalties = new ArrayList<Double>();
		loopPenalties = new ArrayList<Double>();
		initializationRequired = true;
		this.shift = 1;
		computeLLhR = false;
		topGaussianIndices = new TreeMap<Integer, int[]>();
	}

	/**
	 * Accumulate.
	 * 
	 * @param features the features
	 * @param segment the segment
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void accumulate(AudioFeatureSet features, Segment segment) throws DiarizationException, IOException {
		int nbStates = states.size();
		int nbModels = models.size();
		features.setCurrentShow(segment.getShowName());

		int featureIndex = segment.getStart();
		int segmentLength = segment.getLength();
		int segmentLast = featureIndex + segmentLength;
		// PathInfo[] path = new PathInfo[segmentLength];
		// int pathInfoIndex = 0;

		if (initializationRequired == true) {
			modelScores = new double[nbModels];
			modelScoresPrevious = new double[nbModels];
			for (int i = 0; i < nbModels; i++) {
				modelScoresPrevious[i] = 0.0;
				modelScores[i] = 0.0;
			}
			// pathList = new LinkedList<PathInfo>();
			fillTransitionMatrix();
			initializationRequired = false;
			//
			previousScores = new double[nbStates];
			currentScores = new double[nbStates];
			// previousFeatures = new int[nbStates];
			// currentFeatures = new int[nbStates];
			for (int i = 0; i < nbStates; i++) {
				previousScores[i] = Double.NEGATIVE_INFINITY;
				// previousFeatures[i] = -1;
			}
			for (int i = 0; i < modelEndStateIndices.size(); i++) {
				previousScores[modelEndStateIndices.get(i)] = 0.0;
				// previousFeatures[modelEndStateIndices.get(i)] = featureIndex;
			}
			// previousPath = new LinkedList<PathInfo[]>();
			// path = new LinkedList<PathInfo>();
			path = new TreeMap<Integer, Integer>();
			segmentList = new ArrayList<Segment>();
		}
		data = new TreeMap<Integer, int[]>();

		// Treating all the features (including the first one)
// for (; featureIndex < segmentLast; featureIndex += shift, pathInfoIndex += shift) {
		for (; featureIndex < segmentLast; featureIndex += shift) {
			Arrays.fill(currentScores, Double.NEGATIVE_INFINITY);
			int viterbiColumn[] = new int[nbStates];
			computeScoreForAllModels(features, featureIndex);

			// middle states (first state is the middle list)
			for (int modelIndice = 0; modelIndice < modelMiddleStateList.size(); modelIndice++) {
				Integer[] middleStates = modelMiddleStateList.get(modelIndice);
				double score = modelScores[modelIndice];
				if (middleStates != null) {
					for (Integer middleState : middleStates) {
						int index = middleState;
						currentScores[index + 1] = previousScores[index] + score;
						// currentFeatures[index + 1] = previousFeatures[index];
						viterbiColumn[index + 1] = index;
					}
				}
			}

			// last states
			for (int endModel = 0; endModel < modelEndStateIndices.size(); endModel++) {
				int endIndice = modelEndStateIndices.get(endModel);
				double endPreviousScore = previousScores[endIndice];

				// loop: need to be better than middle state
				double score = (endPreviousScore - loopPenalties.get(endModel)) + modelScores[endModel];
				if (score > currentScores[endIndice]) { // keep the loop
					currentScores[endIndice] = score;
					// currentFeatures[endIndice] = previousFeatures[endIndice];
					viterbiColumn[endIndice] = endIndice;
				}

				// connect to an other model
				endPreviousScore -= exitPenalties.get(endModel);
				for (int startModel = 0; startModel < modelEntryStateIndices.size(); startModel++) {
					if (startModel != endModel) {
						int startIndice = modelEntryStateIndices.get(startModel);
						score = endPreviousScore + modelScores[startModel];
						if (score > currentScores[startIndice]) {
							currentScores[startIndice] = score;
							// currentFeatures[startIndice] = featureIndex;
							viterbiColumn[startIndice] = endIndice;
						}
					}
				}
			}

			// path.addFirst(new PathInfo(segment.getShowName(), featureIndex, viterbiColumn));
			data.put(featureIndex, viterbiColumn);

			// get the best segment for this feature
			/*
			 * double scoreMax = Double.NEGATIVE_INFINITY; int indexModelMax = -1; int indexStateMax = -1; for(int endModel= 0; endModel < modelEndStateIndices.size(); endModel++){ int endIndice = modelEndStateIndices.get(endModel);
			 * if(currentScores[endIndice] > scoreMax) { scoreMax = currentScores[endIndice]; indexModelMax = endModel; indexStateMax = endIndice; } } int startFeatureIndex = currentFeatures[indexStateMax]; PathInfo previous = null;
			 * LinkedList<Segment> list = new LinkedList<Segment>(); list.addFirst(segment); int previousIndex = startFeatureIndex - segment.getStart() - 1; if (previousIndex >= 0) { previous = path[previousIndex]; } else { for(PathInfo[] tmp:
			 * previousPath){ list.addLast(tmp[0].getFirstSegment()); previousIndex = startFeatureIndex - tmp[0].getFirstSegment().getStart() - 1;
			 * System.err.print(startFeatureIndex+"-"+tmp[0].getFirstSegment().getStart()+"="+previousIndex+"/"+tmp.length+" "); if (previousIndex >= 0) { previous = tmp[previousIndex]; break; } } System.err.println("-------------"); }
			 * path[pathInfoIndex] = new PathInfo(startFeatureIndex, featureIndex, indexModelMax, scoreMax, list, previous); pathList.addLast(path[pathInfoIndex]);
			 */

			// save context
			double scoresTmp[] = currentScores;
			currentScores = previousScores;
			previousScores = scoresTmp;

			/*
			 * int featureTmp[] = currentFeatures; currentFeatures = previousFeatures; previousFeatures = featureTmp;
			 */

		}
		// previousPath.addFirst(path);
		segmentList.add(segment);
		makePath(currentScores, segment.getStart());
	}

	/**
	 * Define a HMM. Add a model to the heap, with optional constraint on duration
	 * 
	 * @param newModel the new model
	 * @param exitPenalty the exit penalty (state i to state j)
	 * @param loopPenalty the loop penalty (state i to state i)
	 */
	public void addModel(Model newModel, double exitPenalty, double loopPenalty) {
		addModelWithDurationConstraint(newModel, exitPenalty, loopPenalty, NO_DURATION_CONSTRAINT, 0);
	}

	/**
	 * Define a HMM. Add a model to the heap with constraint on duration
	 * 
	 * @param newModel the new model
	 * @param exitPenalty the exit penalty (state i to state j)
	 * @param loopPenalty the loop penalty (state i to state i)
	 * @param constraint the constraint
	 * @param constraintValue the constraint value
	 */
	protected void addModelWithDurationConstraint(Model newModel, double exitPenalty, double loopPenalty, int constraint, int constraintValue) {
		models.add(newModel);
		modelDurationConstraints.add(constraint);
		int newModelIndex = models.size() - 1;
		modelEntryStateIndices.add(states.size());
		if (constraint == NO_DURATION_CONSTRAINT) {
			durationConstraintsValues.add(1);
			states.add(newModelIndex);
		} else {
			durationConstraintsValues.add(constraintValue);
			for (int i = 0; i < constraintValue; i++) {
				states.add(newModelIndex);
			}
		}
		newModel.score_initialize();
		exitPenalties.add(exitPenalty);
		loopPenalties.add(loopPenalty);
		initializationRequired = true;
	}

	/**
	 * Adds the model with fixed duration.
	 * 
	 * @param newModel the new model
	 * @param exitPenalty the exit penalty (state i to state j)
	 * @param loopPenalty the loop penalty (state i to state i)
	 * @param duration the duration
	 */
	public void addModelWithFixedDuration(Model newModel, double exitPenalty, double loopPenalty, int duration) {
		addModelWithDurationConstraint(newModel, exitPenalty, loopPenalty, FIXED_DURATION_CONSTRAINT, duration);
	}

	/**
	 * Adds the model with minimal duration.
	 * 
	 * @param newModel the new model
	 * @param exitPenalty the exit penalty (state i to state j)
	 * @param loopPenalty the loop penalty (state i to state i)
	 * @param duration the duration
	 */
	public void addModelWithMinimalDuration(Model newModel, double exitPenalty, double loopPenalty, int duration) {
		addModelWithDurationConstraint(newModel, exitPenalty, loopPenalty, MINIMAL_DURATION_CONSTRAINT, duration);
	}

	/**
	 * Adds the model with periodic duration.
	 * 
	 * @param newModel the new model
	 * @param exitPenalty the exit penalty
	 * @param loopPenalty the loop penalty
	 * @param duration the duration
	 */
	public void addModelWithPeriodicDuration(Model newModel, double exitPenalty, double loopPenalty, int duration) {
		addModelWithDurationConstraint(newModel, exitPenalty, loopPenalty, PERIODIC_DURATION_CONSTRAINT, duration);
	}

	/**
	 * Compute log likelihood ratio score model.
	 * 
	 * @param features the features
	 * @param featureIndexStart the feature index start
	 * @param idxModel the index model
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	protected double computeLLhRScoreModel(AudioFeatureSet features, int featureIndexStart, int idxModel) throws DiarizationException {
		Model model = models.get(idxModel);
		model.score_initialize();
		int end = Math.min(featureIndexStart + shift, features.getNumberOfFeatures());
		for (int featureIndex = featureIndexStart; featureIndex < end; featureIndex++) {
			if (nbTopGaussians <= 0) {
				model.score_getAndAccumulate(features, featureIndex);
				UBM.score_getAndAccumulate(features, featureIndex);
			} else {
				model.score_getAndAccumulateForComponentSubset(features, featureIndex, topGaussianIndices.get(featureIndex));
			}
		}
		double value = model.score_getMeanLog() - UBM.score_getMeanLog();
		if (value == Double.NEGATIVE_INFINITY) {
			System.out.println("Warning[DecoderWithDuration} getScoreForModel : score == Double.NEGATIVE_INFINITY");
			return modelScoresPrevious[idxModel];
		}
		return value;
	}

	/**
	 * Compute log likelihood ratio score for UBM.
	 * 
	 * @param features the features
	 * @param featureIndexStart the feature index start
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	protected double computeLLhRScoreUBM(AudioFeatureSet features, int featureIndexStart) throws DiarizationException {
		int end = Math.min(featureIndexStart + shift, features.getNumberOfFeatures());
		UBM.score_initialize();
		for (int featureIndex = featureIndexStart; featureIndex < end; featureIndex++) {
			if (nbTopGaussians <= 0) {
				UBM.score_getAndAccumulate(features, featureIndex);
			} else {
				if (currentTopGaussianFeatureIndex != featureIndex) {
					UBM.score_getAndAccumulateAndFindTopComponents(features, featureIndex, nbTopGaussians);
					currentTopGaussianFeatureIndex = featureIndex;
					topGaussianIndices.put(featureIndex, UBM.getTopGaussianVector());
				}
			}
		}
		return UBM.score_getMeanLog();
	}

	/**
	 * Compute log likelihood for a model.
	 * 
	 * @param features the features
	 * @param featureIndexStart the feature index start
	 * @param idxModel the index model
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 */
	protected double computeLLhScoreModel(AudioFeatureSet features, int featureIndexStart, int idxModel) throws DiarizationException {
		Model model = models.get(idxModel);
		model.score_initialize();
		int end = Math.min(featureIndexStart + shift, features.getNumberOfFeatures());
		for (int featureIndex = featureIndexStart; featureIndex < end; featureIndex++) {
			if (nbTopGaussians <= 0) {
				model.score_getAndAccumulate(features, featureIndex);
			} else {
				model.score_getAndAccumulateForComponentSubset(features, featureIndex, topGaussianIndices.get(featureIndex));
			}
		}
		double value = model.score_getSumLog();
		if (value == Double.NEGATIVE_INFINITY) {
			System.err.println("Warning[DecoderWithDuration} getScoreForModel : score == Double.NEGATIVE_INFINITY start="
					+ featureIndexStart + " end=" + end + " value=" + modelScoresPrevious[idxModel]);
			return modelScoresPrevious[idxModel];
		}
		return value;
	}

	/**
	 * Compute score for all models.
	 * 
	 * @param features the features
	 * @param featureIndex the feature index
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	protected void computeScoreForAllModels(AudioFeatureSet features, int featureIndex) throws DiarizationException {
		if (computeLLhR) {
			computeLLhRScoreUBM(features, featureIndex);
			for (int idxModel = 0; idxModel < models.size(); idxModel++) {
				modelScoresPrevious[idxModel] = modelScores[idxModel];
				modelScores[idxModel] = computeLLhRScoreModel(features, featureIndex, idxModel);
			}
		} else {
			if (nbTopGaussians > 0) {
				computeLLhRScoreUBM(features, featureIndex);
			}
			for (int idxModel = 0; idxModel < models.size(); idxModel++) {
				modelScoresPrevious[idxModel] = modelScores[idxModel];
				modelScores[idxModel] = computeLLhScoreModel(features, featureIndex, idxModel);
			}
		}
		topGaussianIndices.clear();
	}

	/**
	 * Debug.
	 */
	public void debug() {
		System.out.println("debug[decoder] \t nb models=" + models.size());
		for (int i = 0; i < models.size(); i++) {
			System.out.print("debug[decoder] \t Decoder::typeName idx:" + i + " ");
			// System.out.print(" gmm id=" + models.get(i).getId());
			System.out.print(" gmm name=" + models.get(i).getName());
			System.out.println(" type ModelAbst=" + models.get(i).getClass().getName());
		}
		System.out.print("debug[decoder] \t states : initializationRequired --> " + initializationRequired);
		System.out.println("debug[decoder] \t nb states : " + states.size());
		for (int i = 0; i < states.size(); i++) {
			System.out.println("debug[decoder] \t state = " + i + " idx model = " + states.get(i));
		}
		if (modelEntryStateIndices != null) {
			for (int i = 0; i < modelEntryStateIndices.size(); i++) {
				System.out.println("debug[decoder] \t modelEntryStateIndices[" + i + "] = "
						+ modelEntryStateIndices.get(i));
			}
		}
		if (modelMiddleStateList != null) {
			for (int i = 0; i < modelMiddleStateList.size(); i++) {
				System.out.print("debug[decoder] \t modelMiddleStateList[" + i + "] = ");
				Integer[] tmp = modelMiddleStateList.get(i);
				for (Integer element : tmp) {
					System.out.print(" " + element);
				}
				System.out.println();
			}
		}
		if (modelEndStateIndices != null) {
			for (int i = 0; i < modelEndStateIndices.size(); i++) {
				System.out.println("debug[decoder] \t modelEndStateIndices[" + i + "] = " + modelEndStateIndices.get(i));
			}
		}
	}

	/**
	 * Initialization before decoding Creation of the transition matrix, taking into account duration constraints.
	 */
	protected void fillTransitionMatrix() {
		modelMiddleStateList = new ArrayList<Integer[]>();
		modelEndStateIndices = new ArrayList<Integer>();
		for (int modelIndex = 0; modelIndex < models.size(); modelIndex++) {
			int firstStateIndex = modelEntryStateIndices.get(modelIndex);
			int lastStateIndex = (firstStateIndex + durationConstraintsValues.get(modelIndex)) - 1;
			modelEndStateIndices.add(lastStateIndex);

			if (modelDurationConstraints.get(modelIndex) != NO_DURATION_CONSTRAINT) {
				Integer[] tmp = new Integer[durationConstraintsValues.get(modelIndex) - 1];
				int i = 0;
				for (int stateIndex = firstStateIndex; stateIndex < lastStateIndex; stateIndex++, i++) {
					tmp[i] = stateIndex;
				}
				modelMiddleStateList.add(tmp);
			}
		}
	}

	/**
	 * Get the clustering.
	 * 
	 * @return the clusters TODO: A simplifier, faire les segments dans make path TODO: probleme si les locuteurs n'existe pas dans la seg...
	 */
	public ClusterSet getClusters() {
		ClusterSet res = new ClusterSet();

		/*
		 * >int index = -1; double max = Double.NEGATIVE_INFINITY; for(int stateIndex: modelEndStateIndices) { if (max < currentScores[stateIndex]) { max = currentScores[stateIndex]; index = stateIndex; } } for(PathInfo pathInfo: path) { Cluster
		 * cluster = res.getOrCreateANewCluster(models.get(states.get(index)).getName()); Segment newSegment = new Segment(pathInfo.showName, pathInfo.featureIndex, 1, cluster); cluster.addSegment(newSegment); index = pathInfo.viterbi[index]; }
		 */

		/*
		 * PathInfo pathInfo = pathList.getLast(); while (pathInfo != null) { int i = 0; for(Segment segment: pathInfo.segmentList){ Cluster cluster = res.getOrCreateANewCluster(models.get(pathInfo.getModel()).getName()); int start =
		 * Math.max(segment.getStart(), pathInfo.getStart()); int last = Math.min(segment.getLast(), pathInfo.getLast()); Segment newSegment = (Segment) segment.clone(); newSegment.setCluster(cluster); newSegment.setStartAndLast(start, last);
		 * newSegment.setInformation("nb", i++); cluster.addSegment(newSegment); } pathInfo = pathInfo.previousPathInfo; }
		 */
		path.clear();
		res.collapse();
		return res;
	}

	/**
	 * Adds the clusters.
	 * 
	 * @param res the res
	 */
	public void addClusters(ClusterSet res) {
		// ClusterSet res = new ClusterSet();

		/*
		 * int index = -1; double max = Double.NEGATIVE_INFINITY; for(int stateIndex: modelEndStateIndices) { if (max < currentScores[stateIndex]) { max = currentScores[stateIndex]; index = stateIndex; } } for(PathInfo pathInfo: path) { Cluster
		 * cluster = res.getOrCreateANewCluster(models.get(states.get(index)).getName()); Segment newSegment = new Segment(pathInfo.showName, pathInfo.featureIndex, 1, cluster); cluster.addSegment(newSegment); index = pathInfo.viterbi[index]; }
		 */

		/*
		 * PathInfo pathInfo = pathList.getLast(); while (pathInfo != null) { int i = 0; for(Segment segment: pathInfo.segmentList){ Cluster cluster = res.getOrCreateANewCluster(models.get(pathInfo.getModel()).getName()); int start =
		 * Math.max(segment.getStart(), pathInfo.getStart()); int last = Math.min(segment.getLast(), pathInfo.getLast()); Segment newSegment = (Segment) segment.clone(); newSegment.setCluster(cluster); newSegment.setStartAndLast(start, last);
		 * newSegment.setInformation("nb", i++); cluster.addSegment(newSegment); } pathInfo = pathInfo.previousPathInfo; }
		 */
		path.clear();
		res.collapse();
		// return res;
	}

	/**
	 * Gets the clusters2.
	 * 
	 * @return the clusters2
	 */
	public ClusterSet getClusters2() {
		ClusterSet res = new ClusterSet();

		Hashtable<Integer, Segment> m = new Hashtable<Integer, Segment>();
		for (Segment seg : segmentList) {
			int end = seg.getStart() + seg.getLength();
			for (int i = seg.getStart(); i < end; i++) {
				m.put(i, seg);
			}
		}

		int l = shift;
		for (int prev : path.keySet()) {
			Cluster cluster = res.getOrCreateANewCluster(models.get(path.get(prev)).getName());
			Segment segment = (m.get(prev).clone());
// segment.setStart(prev - l);
			int start = (prev - l) + 1;
			int len = l;
			if (start < 0) {
				len = (l - start) + 1;
				start = 0;
			}
			segment.setStart(start);
			segment.setLength(len);
// segment.setStart(prev);
// segment.setLength(l);
			cluster.addSegment(segment);
		}

		res.collapse();
		return res;
	}

	/**
	 * Make path.
	 * 
	 * @param scores the scores
	 * @param start the start
	 */
	protected void makePath(double[] scores, int start) {
		int idx = modelEndStateIndices.get(0);
		double max = scores[idx];
		for (int i = 1; i < modelEndStateIndices.size(); i++) {
			if (max < scores[modelEndStateIndices.get(i)]) {
				max = scores[modelEndStateIndices.get(i)];
				idx = modelEndStateIndices.get(i);
			}
		}

// for (int i = end - 1; i >= start; i--) {
		for (int i = data.lastKey(); i >= start; i -= shift) {
			path.put(i, states.get(idx));
			idx = data.get(i)[idx];
		}
	}

	/**
	 * Sets the compute l lh r.
	 * 
	 * @param computeLLhR the new compute l lh r
	 */
	public void setComputeLLhR(boolean computeLLhR) {
		this.computeLLhR = computeLLhR;
	}

	/**
	 * Use ntop Gaussian in likelihood computation.
	 * 
	 * @param n the number of Gaussians
	 * @param gmm the UBM
	 */
	public void setGMMForTopGaussian(int n, GMM gmm) {
		nbTopGaussians = n;
		UBM = gmm;
		currentTopGaussianFeatureIndex = -1;
	}

	/**
	 * Sets the shift.
	 * 
	 * @param declay the new shift
	 */
	public void setShift(int declay) {
		this.shift = declay;

	}

	/**
	 * Setup hmm.
	 * 
	 * @param models the models
	 * @param param the param
	 */
	public void setupHMM(GMMArrayList models, Parameter param) {
		// add model
		int nbOfModels = models.size();
		int nbOfPenalties = param.getParameterDecoder().getExitDecoderPenalty().size();
		for (int i = 0; i < nbOfModels; i++) {
			double exitPenalty;
			double loopPenalty;
			if (i < nbOfPenalties) {
				exitPenalty = param.getParameterDecoder().getExitDecoderPenalty().get(i);
				loopPenalty = param.getParameterDecoder().getLoopDecoderPenalty().get(i);
			} else {
				exitPenalty = param.getParameterDecoder().getExitDecoderPenalty().get(nbOfPenalties - 1);
				loopPenalty = param.getParameterDecoder().getLoopDecoderPenalty().get(nbOfPenalties - 1);
			}
			System.out.println("trace[mDecode] \t Model penalty=" + exitPenalty + ":" + loopPenalty + " model=" + i
					+ " / " + models.get(i).getName());
			ParameterDecoder.ViterbiDurationConstraint durationConstraint;
			int durationConstraintValue;
			if (i < param.getParameterDecoder().getViterbiDurationConstraints().size()) {
				durationConstraint = param.getParameterDecoder().getViterbiDurationConstraints().get(i);
				durationConstraintValue = param.getParameterDecoder().getViterbiDurationConstraintValues().get(i);
			} else {
				durationConstraint = param.getParameterDecoder().getViterbiDurationConstraints().get(param.getParameterDecoder().getViterbiDurationConstraints().size() - 1);
				durationConstraintValue = param.getParameterDecoder().getViterbiDurationConstraintValues().get(param.getParameterDecoder().getViterbiDurationConstraints().size() - 1);
			}
			switch (durationConstraint) {
			case VITERBI_MINIMAL_DURATION:
				System.out.println("trace[mDecode] \t Duration Minimal=" + durationConstraintValue + " model=" + i);
				addModelWithMinimalDuration(models.get(i), exitPenalty, loopPenalty, durationConstraintValue);
				break;
			case VITERBI_PERIODIC_DURATION:
				System.out.println("trace[mDecode] \t Duration periodic=" + durationConstraintValue + " model=" + i);
				addModelWithPeriodicDuration(models.get(i), exitPenalty, loopPenalty, durationConstraintValue);
				break;
			case VITERBI_FIXED_DURATION: // stupid case
				System.out.println("trace[mDecode] \t Duration fixed=" + durationConstraintValue + " model=" + i);
				addModelWithFixedDuration(models.get(i), exitPenalty, loopPenalty, durationConstraintValue);
				break;
			default:
				System.out.println("trace[mDecode] \t no duration model=" + i);
				addModel(models.get(i), exitPenalty, loopPenalty);
			}
		}
	}
}
