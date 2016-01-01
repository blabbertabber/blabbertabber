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

package fr.lium.spkDiarization.libDecoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
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
 * The Class DecoderWithDuration.
 */
public class DecoderWithDuration {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(DecoderWithDuration.class.getName());

	/** UBM for nTop. */
	protected GMM ubm;

	/** nTop value. */
	protected int nbTopGaussians;

	/** list of models. */
	protected ArrayList<Model> modelList;

	/** a state stores the index of the corresponding model. */
	protected ArrayList<Integer> stateList;

	/** duration constraint for each model (see constants below). */
	protected ArrayList<Integer> modelDurationConstraintList;

	/** value associated with each constraint (typically a duration, expressed in number of features). */
	protected ArrayList<Integer> durationConstraintValueList;

	/** index of the first state for each model. */
	protected ArrayList<Integer> modelEntryStateIndiceList;

	/** for each state, a list of the states from which transition to this state is possible. */
	protected ArrayList<Integer> validPreviousStates[];

	/** for each state, a list of the states from which transition to this state is possible. */
	protected ArrayList<Integer> validNextStates[];

	/** list of the states from which transition to the exit state is possible. */
	protected ArrayList<Integer> validLastStates;

	/** transition matrix. */
	protected double transitions[][];

	/** The transitions previous next. */
	protected ArrayList<double[]> transitionsPreviousNext;

	/** exit hmm penality. */
	protected ArrayList<Double> exitPenaltyList;

	/** loop hmm penality. */
	protected ArrayList<Double> loopPenaltyList;

	/** the current top gaussian feature index, ie the feature index in the feature set. */
	protected int currentTopGaussianFeatureIndex;

	/** the current top gaussian score corresponding to currentTopGaussianFeatureIndex index. */
	protected double currentTopGaussianScore;

	/** data used in forward pass; the keys are feature indices. */
	protected TreeMap<Integer, int[]> data;

	/** list of segment. */
	protected ArrayList<Segment> segmentList;

	/** list of scores of the current feature. */
	protected double modelScores[];

	/** list of scores of the previous feature. */
	protected double modelScoresPrevious[];

	/** structure to save topGaussians. */
	protected TreeMap<Integer, int[]> topGaussianIndices;

	/** keeps track of whether the transition matrix needs to be (re)filled and data container needs to get cleared. */
	protected boolean initializationRequired;

	/** used to detect whether a segment passed to accumulate() is part of the same show as the previous segment. */
	protected String showNameForPreviousSegment;

	/** structure to store viterbi path. */
	protected TreeMap<Integer, Integer> path;

	/** shift by shift computation of feature. */
	protected int shift;

	/** compute log likelihood ratio or just log likelihood. */
	protected boolean computeLogLikelihoodRatio;

	/** HMM without duration constraint, one state per HMM. */
	static final protected int NO_DURATION_CONSTRAINT = 0;

	/** HMM with n states linked, with only transition i-->i on the last, exit state only on the last. */
	static final protected int MINIMAL_DURATION_CONSTRAINT = 1;

	/** ?? HMM with n states linked, the last is linked to the first, exit state only on the last. */
	static final protected int FIXED_DURATION_CONSTRAINT = 2;

	/** ?? HMM with n states linked, the last is linked to the first, exit state only on the last. */
	static final protected int PERIODIC_DURATION_CONSTRAINT = 4;

	/** The previous scores. */
	protected double previousScores[];

	/** The next scores. */
	protected double nextScores[];

	/**
	 * Create a decoder, without using top Gaussians.
	 * 
	 * @param shift the shift
	 */
	public DecoderWithDuration(int shift) {
// features = (FeatureSet) (featureSet.clone());
		nbTopGaussians = -1;
		ubm = null;
		modelList = new ArrayList<Model>();
		stateList = new ArrayList<Integer>();
		modelDurationConstraintList = new ArrayList<Integer>();
		durationConstraintValueList = new ArrayList<Integer>();
		modelEntryStateIndiceList = new ArrayList<Integer>();
		exitPenaltyList = new ArrayList<Double>();
		loopPenaltyList = new ArrayList<Double>();
		initializationRequired = true;
		this.shift = shift;
		computeLogLikelihoodRatio = false;
		topGaussianIndices = new TreeMap<Integer, int[]>();
	}

	/**
	 * Create a decoder, using top Gaussians for likelihood computation.
	 * 
	 * @param n the number of top Gaussians to use
	 * @param ubm the UBM for the top Gaussians
	 * @param computeLogLikelihoodRatio the compute log likelihood ratio
	 * @param shift the shift
	 */
	public DecoderWithDuration(int n, GMM ubm, boolean computeLogLikelihoodRatio, int shift) {
// features = (FeatureSet) (featureSet.clone());
		nbTopGaussians = n;
		this.ubm = ubm;
		currentTopGaussianFeatureIndex = -1;
		modelList = new ArrayList<Model>();
		stateList = new ArrayList<Integer>();
		modelDurationConstraintList = new ArrayList<Integer>();
		durationConstraintValueList = new ArrayList<Integer>();
		modelEntryStateIndiceList = new ArrayList<Integer>();
		exitPenaltyList = new ArrayList<Double>();
		loopPenaltyList = new ArrayList<Double>();
		initializationRequired = true;
		this.shift = shift;
		this.computeLogLikelihoodRatio = computeLogLikelihoodRatio;
		topGaussianIndices = new TreeMap<Integer, int[]>();
	}

	/**
	 * Instantiates a new decoder with duration.
	 */
	public DecoderWithDuration() {
		// features = (FeatureSet) (featureSet.clone());
		nbTopGaussians = -1;
		ubm = null;
		modelList = new ArrayList<Model>();
		stateList = new ArrayList<Integer>();
		modelDurationConstraintList = new ArrayList<Integer>();
		durationConstraintValueList = new ArrayList<Integer>();
		modelEntryStateIndiceList = new ArrayList<Integer>();
		exitPenaltyList = new ArrayList<Double>();
		loopPenaltyList = new ArrayList<Double>();
		initializationRequired = true;
		this.shift = 1;
		computeLogLikelihoodRatio = false;
		topGaussianIndices = new TreeMap<Integer, int[]>();
	}

	/**
	 * Forward pass for a segment
	 * 
	 * Probabilities computed at each frame.
	 * 
	 * @param featureSet the features
	 * @param segment the segment
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
/*
 * public void accumulate(FeatureSet features, Segment segment) throws DiarizationException, IOException { //debug(); int nbStates = states.size(); data = new TreeMap<Integer, int[]>(); if (initializationRequired == true) { previousScores = new
 * double[nbStates]; nextScores = new double[nbStates]; path = new TreeMap<Integer, Integer>(); modelScores = new double[models.size()]; modelScoresPrevious = new double[models.size()]; for (int i = 0; i < models.size(); i++) { modelScoresPrevious[i]
 * = 0.0; modelScores[i] = 0.0; } segmentList = new ArrayList<Segment>(); showNameForPreviousSegment = FeatureSet.UNKNOWN_SHOW; fillTransitionMatrix(); // Treating the case of the first feature frame if this segment is the // first one for (int i =
 * 0; i < nbStates; i++) { previousScores[i] = Double.NEGATIVE_INFINITY; } for (int i = 0; i < validLastStates.size(); i++) { previousScores[validLastStates.get(i)] = 0.0; } initializationRequired = false; } segmentList.add(segment);
 * features.setCurrentShow(segment.getShowName()); int featureIndex = segment.getStart(); int last = featureIndex + segment.getLength(); showNameForPreviousSegment = segment.getShowName(); // Treating all the features (including the first one) for (;
 * featureIndex < last; featureIndex += shift) { int viterbiColumn[] = new int[nbStates]; computeScoreForAllModels(features, featureIndex); for (int currentStateIndex = 0; currentStateIndex < nbStates; currentStateIndex++) { double currentStateScore
 * = modelScores[states.get(currentStateIndex)]; double maxScore = Double.NEGATIVE_INFINITY; int indexOfMaxScore = -1; for (int i = 0; i < validPreviousStates[currentStateIndex].size(); i++) { int previousStateIndex =
 * validPreviousStates[currentStateIndex].get(i); double tmpScore = previousScores[previousStateIndex] + transitions[previousStateIndex][currentStateIndex] + currentStateScore; if (tmpScore > maxScore) { maxScore = tmpScore; indexOfMaxScore =
 * previousStateIndex; } } nextScores[currentStateIndex] = maxScore; viterbiColumn[currentStateIndex] = indexOfMaxScore; } double scoresTmp[] = nextScores; nextScores = previousScores; previousScores = scoresTmp; data.put(featureIndex,
 * viterbiColumn); // checkUniqueSolution(featureIndex, viterbiColumn); } // Saving the scores for use at the beginning of the next segment makePath(previousScores, segment.getStart(), last); data.clear(); }
 */

// double previousScores[];
// double nextScores[];

	public void accumulate(AudioFeatureSet featureSet, Segment segment) throws DiarizationException, IOException {
		int nbStates = stateList.size();
		featureSet.setCurrentShow(segment.getShowName());

		int featureIndex = segment.getStart();
		int last = featureIndex + segment.getLength();
		data = new TreeMap<Integer, int[]>();

		if (initializationRequired == true) {
			path = new TreeMap<Integer, Integer>();
			modelScores = new double[modelList.size()];
			modelScoresPrevious = new double[modelList.size()];
			for (int i = 0; i < modelList.size(); i++) {
				modelScoresPrevious[i] = 0.0;
				modelScores[i] = 0.0;
			}
			segmentList = new ArrayList<Segment>();
			showNameForPreviousSegment = AudioFeatureSet.UNKNOWN_SHOW;
			fillTransitionMatrix();
			initializationRequired = false;
			//
			previousScores = new double[nbStates];
			nextScores = new double[nbStates];
			for (int i = 0; i < nbStates; i++) {
				previousScores[i] = Double.NEGATIVE_INFINITY;
			}
			for (int i = 0; i < validLastStates.size(); i++) {
				previousScores[validLastStates.get(i)] = 0.0;
			}
		}
		segmentList.add(segment);
		showNameForPreviousSegment = segment.getShowName();

		// Treating all the features (including the first one)
// System.err.println("*** forward pass");
		for (; featureIndex < last; featureIndex += shift) {
			int viterbiColumn[] = new int[nbStates];
			computeScoreForAllModels(featureSet, featureIndex);

			for (int currentStateIndex = 0; currentStateIndex < nbStates; currentStateIndex++) {
				double currentStateScore = modelScores[stateList.get(currentStateIndex)];
				double maxScore = Double.NEGATIVE_INFINITY;
				int indexOfMaxScore = -1;
				for (int previousStateIndex : validPreviousStates[currentStateIndex]) {
					double tmpScore = previousScores[previousStateIndex]
							+ transitions[previousStateIndex][currentStateIndex] + currentStateScore;
					if (tmpScore > maxScore) {
						maxScore = tmpScore;
						indexOfMaxScore = previousStateIndex;
					}
				}
				nextScores[currentStateIndex] = maxScore;
				viterbiColumn[currentStateIndex] = indexOfMaxScore;

			}
			double scoresTmp[] = nextScores;
			nextScores = previousScores;
			previousScores = scoresTmp;
			data.put(featureIndex, viterbiColumn);
			// checkUniqueSolution(featureIndex, viterbiColumn);
		}
		// Saving the scores for use at the beginning of the next segment
		makePath(previousScores, segment.getStart(), last);
		data.clear();
	}

	/**
	 * Accumulate2.
	 * 
	 * @param featureSet the feature set
	 * @param segment the segment
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void accumulate2(AudioFeatureSet featureSet, Segment segment) throws DiarizationException, IOException {
		int nbStates = stateList.size();
		featureSet.setCurrentShow(segment.getShowName());

		int featureIndex = segment.getStart();
		int last = featureIndex + segment.getLength();
		data = new TreeMap<Integer, int[]>();

		if (initializationRequired == true) {
			path = new TreeMap<Integer, Integer>();
			modelScores = new double[modelList.size()];
			modelScoresPrevious = new double[modelList.size()];
			for (int i = 0; i < modelList.size(); i++) {
				modelScoresPrevious[i] = 0.0;
				modelScores[i] = 0.0;
			}
			segmentList = new ArrayList<Segment>();
			showNameForPreviousSegment = AudioFeatureSet.UNKNOWN_SHOW;
			fillTransitionMatrix();
			initializationRequired = false;
			//
			previousScores = new double[nbStates];
			nextScores = new double[nbStates];
			for (int i = 0; i < nbStates; i++) {
				previousScores[i] = Double.NEGATIVE_INFINITY;
			}
			for (int i = 0; i < validLastStates.size(); i++) {
				previousScores[validLastStates.get(i)] = 0.0;
			}
		}
		segmentList.add(segment);
// debug();
		showNameForPreviousSegment = segment.getShowName();
		// Treating all the features (including the first one)
		for (; featureIndex < last; featureIndex += shift) {
			Arrays.fill(nextScores, Double.NEGATIVE_INFINITY);
			int viterbiColumn[] = new int[nbStates];
			computeScoreForAllModels(featureSet, featureIndex);

			double scoresTmp[] = nextScores;
			nextScores = previousScores;
			previousScores = scoresTmp;
			data.put(featureIndex, viterbiColumn);
			// checkUniqueSolution(featureIndex, viterbiColumn);
		}
		// Saving the scores for use at the beginning of the next segment
		makePath(previousScores, segment.getStart(), last);
		data.clear();
	}

	/**
	 * Check unique solution.
	 * 
	 * @param index the index
	 * @param viterbiColumn the viterbi column
	 * @return true, if successful
	 */
	@SuppressWarnings("unused")
	private boolean checkUniqueSolution(int index, int viterbiColumn[]) {
		int ref = viterbiColumn[0];
		for (int i = 1; i < viterbiColumn.length; i++) {
			if (ref != viterbiColumn[i]) {
				return false;
			}
		}
		if (SpkDiarizationLogger.DEBUG) logger.finer("UniqueSollution at featureIndex : " + index);
		return true;
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
		modelList.add(newModel);
		modelDurationConstraintList.add(constraint);
		int newModelIndex = modelList.size() - 1;
		modelEntryStateIndiceList.add(stateList.size());
		if (constraint == NO_DURATION_CONSTRAINT) {
			durationConstraintValueList.add(1);
			stateList.add(newModelIndex);
		} else {
			durationConstraintValueList.add(constraintValue);
			for (int i = 0; i < constraintValue; i++) {
				stateList.add(newModelIndex);
			}
		}
		newModel.score_initialize();
		exitPenaltyList.add(exitPenalty);
		loopPenaltyList.add(loopPenalty);
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
	 * @param featureSet the features
	 * @param featureIndexStart the feature index start
	 * @param idxModel the index model
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	protected double computeLogLikelihoodRatioModel(AudioFeatureSet featureSet, int featureIndexStart, int idxModel) throws DiarizationException {
		Model model = modelList.get(idxModel);
		model.score_initialize();
		int end = Math.min(featureIndexStart + shift, featureSet.getNumberOfFeatures());
		for (int featureIndex = featureIndexStart; featureIndex < end; featureIndex++) {
			if (nbTopGaussians <= 0) {
				model.score_getAndAccumulate(featureSet, featureIndex);
				ubm.score_getAndAccumulate(featureSet, featureIndex);
			} else {
				model.score_getAndAccumulateForComponentSubset(featureSet, featureIndex, topGaussianIndices.get(featureIndex));
			}
		}
		double value = model.score_getMeanLog() - ubm.score_getMeanLog();
		if (value == Double.NEGATIVE_INFINITY) {
			logger.warning("score == Double.NEGATIVE_INFINITY");
			return modelScoresPrevious[idxModel];
		}
		return value;
	}

	/**
	 * Compute log likelihood ratio score for UBM.
	 * 
	 * @param featureSet the features
	 * @param featureIndexStart the feature index start
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	protected double computeLogLikelihoodRatioUbm(AudioFeatureSet featureSet, int featureIndexStart) throws DiarizationException {
		int end = Math.min(featureIndexStart + shift, featureSet.getNumberOfFeatures());
		ubm.score_initialize();
		for (int featureIndex = featureIndexStart; featureIndex < end; featureIndex++) {
			if (nbTopGaussians <= 0) {
				ubm.score_getAndAccumulate(featureSet, featureIndex);
			} else {
				if (currentTopGaussianFeatureIndex != featureIndex) {
					ubm.score_getAndAccumulateAndFindTopComponents(featureSet, featureIndex, nbTopGaussians);
					currentTopGaussianFeatureIndex = featureIndex;
					topGaussianIndices.put(featureIndex, ubm.getTopGaussianVector());
				}
			}
		}
		return ubm.score_getMeanLog();
	}

	/**
	 * Compute log likelihood for a model.
	 * 
	 * @param featureSet the features
	 * @param featureIndexStart the feature index start
	 * @param idxModel the index model
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 */
	protected double computeLogLikelihoodModel(AudioFeatureSet featureSet, int featureIndexStart, int idxModel) throws DiarizationException {
		Model model = modelList.get(idxModel);
		model.score_initialize();
		int end = Math.min(featureIndexStart + shift, featureSet.getNumberOfFeatures());
		for (int featureIndex = featureIndexStart; featureIndex < end; featureIndex++) {
			if (nbTopGaussians <= 0) {
				model.score_getAndAccumulate(featureSet, featureIndex);
			} else {
				model.score_getAndAccumulateForComponentSubset(featureSet, featureIndex, topGaussianIndices.get(featureIndex));
			}
		}
		double value = model.score_getSumLog();
		if (value == Double.NEGATIVE_INFINITY) {
			logger.warning("score == Double.NEGATIVE_INFINITY start=" + featureIndexStart + " end=" + end + " value="
					+ modelScoresPrevious[idxModel]);
			return modelScoresPrevious[idxModel];
		}
		return value;
	}

	/**
	 * Compute score for all models.
	 * 
	 * @param featureSet the features
	 * @param featureIndex the feature index
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	protected void computeScoreForAllModels(AudioFeatureSet featureSet, int featureIndex) throws DiarizationException {
		if (computeLogLikelihoodRatio) {
			computeLogLikelihoodRatioUbm(featureSet, featureIndex);
			for (int idxModel = 0; idxModel < modelList.size(); idxModel++) {
				modelScoresPrevious[idxModel] = modelScores[idxModel];
				modelScores[idxModel] = computeLogLikelihoodRatioModel(featureSet, featureIndex, idxModel);
			}
		} else {
			if (nbTopGaussians > 0) {
				computeLogLikelihoodRatioUbm(featureSet, featureIndex);
			}
			for (int idxModel = 0; idxModel < modelList.size(); idxModel++) {
				modelScoresPrevious[idxModel] = modelScores[idxModel];
				modelScores[idxModel] = computeLogLikelihoodModel(featureSet, featureIndex, idxModel);
			}
		}
		topGaussianIndices.clear();
	}

	/**
	 * Debug.
	 */
	public void debug() {
		logger.finer("nb models=" + modelList.size());
		for (int i = 0; i < modelList.size(); i++) {
			logger.finer("Decoder::typeName idx:" + i + " " + " gmm name=" + modelList.get(i).getName()
					+ " type ModelAbst=" + modelList.get(i).getClass().getName());
		}
		if (initializationRequired == false) {
			for (int i = 0; i < stateList.size(); i++) {
				// System.out.print("debug[decoder] transition ligne i="+i+"\t");
				for (int j = 0; j < stateList.size(); j++) {
					// System.out.print(transitions[i][j] + " ");
				}
				// System.out.println();
			}
		} else {
			logger.finer("states : initializationRequired");
		}
		System.out.println("debug[decoder] \t nb states : " + stateList.size());
		for (int i = 0; i < stateList.size(); i++) {
			logger.finer("state = " + i + " idx model = " + stateList.get(i));
		}
		if (modelEntryStateIndiceList != null) {
			for (int i = 0; i < modelEntryStateIndiceList.size(); i++) {
				logger.finer("modelEntryStateIndices[" + i + "] = " + modelEntryStateIndiceList.get(i));
			}
		}
		if (validLastStates != null) {
			for (int i = 0; i < validLastStates.size(); i++) {
				logger.finer("validLastStates[" + i + "] = " + validLastStates.get(i));
			}
		}
		if (validPreviousStates != null) {
			for (int i = 0; i < validPreviousStates.length; i++) {
				String message = "debug[decoder] \t validPreviousStates[" + i + "] = ";
				for (int j = 0; j < validPreviousStates[i].size(); j++) {
					message += validPreviousStates[i].get(j) + " ";
				}
				logger.finer(message);
			}
		}
	}

	/**
	 * Initialization before decoding Creation of the transition matrix, taking into account duration constraints.
	 */
	@SuppressWarnings("unchecked")
	protected void fillTransitionMatrix() {
		transitions = new double[stateList.size()][stateList.size()];
		transitionsPreviousNext = new ArrayList<double[]>(stateList.size());
		for (int i = 0; i < stateList.size(); i++) {
			double[] tmp = new double[stateList.size()];
			Arrays.fill(tmp, Double.NEGATIVE_INFINITY);
			transitionsPreviousNext.add(tmp);
			for (int j = 0; j < stateList.size(); j++) {
				transitions[i][j] = Double.NEGATIVE_INFINITY;
			}
		}
		validLastStates = new ArrayList<Integer>();
		int firstStateIndex;
		for (int modelIndex = 0; modelIndex < modelList.size(); modelIndex++) {
			firstStateIndex = modelEntryStateIndiceList.get(modelIndex);
			// Setting the transitions between states of this model
			int lastStateIndex = (firstStateIndex + durationConstraintValueList.get(modelIndex)) - 1;
			switch (modelDurationConstraintList.get(modelIndex)) {
			case MINIMAL_DURATION_CONSTRAINT:
				for (int stateIndex = firstStateIndex; stateIndex < lastStateIndex; stateIndex++) {
					transitions[stateIndex][stateIndex + 1] = 0.0;
					transitionsPreviousNext.get(stateIndex)[stateIndex + 1] = 0.0;

				}
				break;
			case FIXED_DURATION_CONSTRAINT: // stupid case
				for (int stateIndex = firstStateIndex; stateIndex < lastStateIndex; stateIndex++) {
					transitions[stateIndex][stateIndex + 1] = 0.0;
					transitionsPreviousNext.get(stateIndex)[stateIndex + 1] = 0.0;
					validLastStates.add(stateIndex);
				}
				break;
			case PERIODIC_DURATION_CONSTRAINT:
				for (int stateIndex = firstStateIndex; stateIndex < lastStateIndex; stateIndex++) {
					transitions[stateIndex][stateIndex + 1] = 0.0;
					transitionsPreviousNext.get(stateIndex)[stateIndex + 1] = 0.0;
					validLastStates.add(stateIndex);
				}
				break;
			}

			// Setting penalties related to staying on this model: loop, loop
			// back to first state, forced exit, etc.
			switch (modelDurationConstraintList.get(modelIndex)) {
			case NO_DURATION_CONSTRAINT:
				transitions[lastStateIndex][lastStateIndex] = -loopPenaltyList.get(modelIndex);
				transitionsPreviousNext.get(lastStateIndex)[lastStateIndex] = -loopPenaltyList.get(modelIndex);
				break;
			case MINIMAL_DURATION_CONSTRAINT:
				transitions[lastStateIndex][lastStateIndex] = -loopPenaltyList.get(modelIndex);
				transitionsPreviousNext.get(lastStateIndex)[lastStateIndex] = -loopPenaltyList.get(modelIndex);
				break;
			case FIXED_DURATION_CONSTRAINT: // stupid case
				transitions[lastStateIndex][lastStateIndex] = Double.NEGATIVE_INFINITY;
				transitionsPreviousNext.get(lastStateIndex)[lastStateIndex] = Double.NEGATIVE_INFINITY;
				break;
			case PERIODIC_DURATION_CONSTRAINT:
				transitions[lastStateIndex][lastStateIndex] = Double.NEGATIVE_INFINITY;
				transitionsPreviousNext.get(lastStateIndex)[lastStateIndex] = Double.NEGATIVE_INFINITY;
				transitions[lastStateIndex][firstStateIndex] = 0.0;
				transitionsPreviousNext.get(lastStateIndex)[firstStateIndex] = 0.0;
				break;
			}
			validLastStates.add(lastStateIndex);

			// Setting penalties related to switching to another model (from the
			// last state of this model)
			int otherModelFirstStateIndex;
			for (int otherModelIndex = 0; otherModelIndex < modelList.size(); otherModelIndex++) {
				otherModelFirstStateIndex = modelEntryStateIndiceList.get(otherModelIndex);
				if (otherModelIndex != modelIndex) {
					transitions[lastStateIndex][otherModelFirstStateIndex] = -exitPenaltyList.get(modelIndex);
					transitionsPreviousNext.get(lastStateIndex)[otherModelFirstStateIndex] = -exitPenaltyList.get(modelIndex);
				}
			}
		}

		// Setting up the lists of possible previous states
		validPreviousStates = new ArrayList[stateList.size()];
		validNextStates = new ArrayList[stateList.size()];
		for (int stateIndex = 0; stateIndex < stateList.size(); stateIndex++) {
			validPreviousStates[stateIndex] = new ArrayList<Integer>();
			validNextStates[stateIndex] = new ArrayList<Integer>();
		}

		for (int stateIndex = 0; stateIndex < stateList.size(); stateIndex++) {
			for (int otherStateIndex = 0; otherStateIndex < stateList.size(); otherStateIndex++) {
				if (!Double.isInfinite(transitions[otherStateIndex][stateIndex])) {
					validPreviousStates[stateIndex].add(otherStateIndex);
					validNextStates[otherStateIndex].add(stateIndex);
				}
			}
		}

	}

	/**
	 * Get the clustering.
	 * 
	 * @return the clusters TODO: A simplifier, faire les segments dans make path TODO: probleme si les locuteurs n'existe pas dans la seg...
	 */
	public ClusterSet getClusterSet() {
		ClusterSet clusterSetResult = new ClusterSet();

		Hashtable<Integer, Segment> m = new Hashtable<Integer, Segment>();
		for (Segment seg : segmentList) {
			int end = seg.getStart() + seg.getLength();
			for (int i = seg.getStart(); i < end; i++) {
				m.put(i, seg);
			}
		}

		int l = shift;
		for (int prev : path.keySet()) {
			Cluster cluster = clusterSetResult.getOrCreateANewCluster(modelList.get(path.get(prev)).getName());
			Segment segment = (m.get(prev).clone());
			int start = (prev - l) + 1;
			int len = l;
			if (start < 0) {
				len = (l - start) + 1;
				start = 0;
			}
			segment.setStart(start);
			segment.setLength(len);
			cluster.addSegment(segment);
		}

		clusterSetResult.collapse();
		return clusterSetResult;
	}

	/**
	 * Compute the path: backward pass.
	 * 
	 * @param scores the scores
	 * @param start the start
	 * @param end the end
	 */
	protected void makePath(double[] scores, int start, int end) {
		int idx = validLastStates.get(0);
		double max = scores[idx];
		for (int i = 1; i < validLastStates.size(); i++) {
			if (max < scores[validLastStates.get(i)]) {
				max = scores[validLastStates.get(i)];
				idx = validLastStates.get(i);
			}
		}

// for (int i = end - 1; i >= start; i--) {
		for (int i = data.lastKey(); i >= start; i -= shift) {
			path.put(i, stateList.get(idx));
			idx = data.get(i)[idx];
		}
	}

	/**
	 * Sets the compute l lh r.
	 * 
	 * @param computeLLhR the new compute l lh r
	 */
	public void setComputeLLhR(boolean computeLLhR) {
		this.computeLogLikelihoodRatio = computeLLhR;
	}

	/**
	 * Use ntop Gaussian in likelihood computation.
	 * 
	 * @param n the number of Gaussians
	 * @param ubm the UBM
	 */
	public void setUbmForTopGaussian(int n, GMM ubm) {
		nbTopGaussians = n;
		this.ubm = ubm;
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
	 * @param modelList the model list
	 * @param parameter the parameter
	 */
	public void setupHMM(GMMArrayList modelList, Parameter parameter) {
		// add model
		int nbOfModels = modelList.size();
		int nbOfPenalties = parameter.getParameterDecoder().getExitDecoderPenalty().size();
		for (int i = 0; i < nbOfModels; i++) {
			double exitPenalty;
			double loopPenalty;
			if (i < nbOfPenalties) {
				exitPenalty = parameter.getParameterDecoder().getExitDecoderPenalty().get(i);
				loopPenalty = parameter.getParameterDecoder().getLoopDecoderPenalty().get(i);
			} else {
				exitPenalty = parameter.getParameterDecoder().getExitDecoderPenalty().get(nbOfPenalties - 1);
				loopPenalty = parameter.getParameterDecoder().getLoopDecoderPenalty().get(nbOfPenalties - 1);
			}
			logger.finer("\t Model penalty=" + exitPenalty + ":" + loopPenalty + " model=" + i + " / "
					+ modelList.get(i).getName());
			ParameterDecoder.ViterbiDurationConstraint durationConstraint;
			int durationConstraintValue;
			if (i < parameter.getParameterDecoder().getViterbiDurationConstraints().size()) {
				durationConstraint = parameter.getParameterDecoder().getViterbiDurationConstraints().get(i);
				durationConstraintValue = parameter.getParameterDecoder().getViterbiDurationConstraintValues().get(i);
			} else {
				durationConstraint = parameter.getParameterDecoder().getViterbiDurationConstraints().get(parameter.getParameterDecoder().getViterbiDurationConstraints().size() - 1);
				durationConstraintValue = parameter.getParameterDecoder().getViterbiDurationConstraintValues().get(parameter.getParameterDecoder().getViterbiDurationConstraints().size() - 1);
			}
			switch (durationConstraint) {
			case VITERBI_MINIMAL_DURATION:
				logger.finer("Duration Minimal=" + durationConstraintValue + " model=" + i);

				addModelWithMinimalDuration(modelList.get(i), exitPenalty, loopPenalty, durationConstraintValue);
				break;
			case VITERBI_PERIODIC_DURATION:
				logger.finer("Duration periodic=" + durationConstraintValue + " model=" + i);

				addModelWithPeriodicDuration(modelList.get(i), exitPenalty, loopPenalty, durationConstraintValue);
				break;
			case VITERBI_FIXED_DURATION: // stupid case
				logger.finer("Duration fixed=" + durationConstraintValue + " model=" + i);

				addModelWithFixedDuration(modelList.get(i), exitPenalty, loopPenalty, durationConstraintValue);
				break;
			default:
				logger.finest(" \t no duration model=" + i);

				addModel(modelList.get(i), exitPenalty, loopPenalty);
			}
		}
	}

}
