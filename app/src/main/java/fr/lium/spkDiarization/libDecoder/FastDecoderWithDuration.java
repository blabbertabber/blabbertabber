package fr.lium.spkDiarization.libDecoder;

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
import java.util.List;
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
 * The Class FastDecoderWithDuration.
 */
public class FastDecoderWithDuration {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(FastDecoderWithDuration.class.getName());

	/** UBM for nTop. */
	protected GMM ubm;

	/** nTop value. */
	protected int nbTopGaussians;

	/*--- HMM ---*/
	/** list of models. */
	protected ArrayList<Model> modelList;

	/** a state stores the index of the corresponding model. */
	protected ArrayList<Integer> stateList;

	/** duration constraint for each model (see constants below). */
	protected ArrayList<Integer> modelDurationConstraintList;

	/** value associated with each constraint (typically a duration, expressed in number of features). */
	protected ArrayList<Integer> durationConstraintValueList;

	/** index of the first state for each model/HMM. */
	protected ArrayList<Integer> modelEntryStateIndiceList;

	/** list of the states in the middle of the HMM. */
	protected ArrayList<Integer[]> modelMiddleStateList;

	/** index of the last state for each model/HMM. */
	protected ArrayList<Integer> modelEndStateIndiceList;

	/** exit hmm penality. */
	protected ArrayList<Double> exitPenaltyList;

	/** loop hmm penality. */
	protected ArrayList<Double> loopPenaltyList;

	/** HMM without duration constraint, one state per HMM. */
	static final protected int NO_DURATION_CONSTRAINT = 0;

	/** HMM with n states linked, with only transition i-->i on the last, exit state only on the last. */
	static final protected int MINIMAL_DURATION_CONSTRAINT = 1;

	/** ?? HMM with n states linked, the last is linked to the first, exit state only on the last. */
	static final protected int FIXED_DURATION_CONSTRAINT = 2;

	/** ?? HMM with n states linked, the last is linked to the first, exit state only on the last. */
	static final protected int PERIODIC_DURATION_CONSTRAINT = 4;

	/*--- Likelihood ---*/
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

	/** compute log likelihood ratio or just log likelihood. */
	protected boolean computeLogLikelihoodRatio;

	/*--- Decoding ---*/
	/** list of segment. */
	protected ArrayList<Segment> segmentList;

	/** keeps track of whether the transition matrix needs to be (re)filled and data container needs to get cleared. */
	protected boolean initializationRequired;

	/** shift by shift computation of feature. */
	protected int shift;

	// Scores of the previous feature
	/** The previous scores. */
	protected double previousScores[];

	// Scores of the current feature
	/** The current scores. */
	protected double currentScores[];

	/** The previous features. */
	private int previousFeatures[];

	/** The current features. */
	private int currentFeatures[];

	// Diarization result
	/** The result cluster set. */
	ClusterSet resultClusterSet;

	// Information of a potential segment
	/**
	 * The Class PotentialSegment.
	 */
	private class PotentialSegment {

		/** The last feature index. */
		int lastFeatureIndex;

		/** The start feature index. */
		int startFeatureIndex;

		/** The previous index. */
		int previousIndex;

		/** The model. */
		int model;
		// int[] viterbi;
		/** The show name. */
		String showName;

		/**
		 * Instantiates a new potential segment.
		 * 
		 * @param showName the show name
		 * @param startFeatureIndex the start feature index
		 * @param lastFeatureIndex the last feature index
		 * @param previousIndex the previous index
		 * @param model the model
		 */
		public PotentialSegment(String showName, int startFeatureIndex, int lastFeatureIndex, int previousIndex, int model) {
			super();
			this.showName = showName;
			this.startFeatureIndex = startFeatureIndex;
			this.lastFeatureIndex = lastFeatureIndex;
			this.previousIndex = previousIndex;
			this.model = model;
		}

		/**
		 * Debug.
		 */
		@SuppressWarnings("unused")
		public void debug() {
			logger.finer(showName + " previous=" + previousIndex + " start=" + startFeatureIndex + " last="
					+ lastFeatureIndex + " model=" + model);
		}
	}

	// list of potential segment, one by feature;
	/** The path. */
	private ArrayList<PotentialSegment> path;

	// Current index in path
	/** The index path. */
	int indexPath;

	/**
	 * Create a decoder, without using top Gaussians.
	 * 
	 * @param shift the shift
	 */
	public FastDecoderWithDuration(int shift) {
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
		segmentList = new ArrayList<Segment>();
	}

	/**
	 * Create a decoder, using top Gaussians for likelihood computation.
	 * 
	 * @param n the number of top Gaussians to use
	 * @param ubm the UBM for the top Gaussians
	 * @param computeLogLikelihoodRatio the compute l lh r
	 * @param shift the shift
	 */
	public FastDecoderWithDuration(int n, GMM ubm, boolean computeLogLikelihoodRatio, int shift) {
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
		segmentList = new ArrayList<Segment>();
	}

	/**
	 * Instantiates a new fast decoder with duration.
	 */
	public FastDecoderWithDuration() {
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
		segmentList = new ArrayList<Segment>();
	}

	/**
	 * Initialize.
	 * 
	 * @param nbStates the nb states
	 * @param nbModels the nb models
	 */
	protected void initialize(int nbStates, int nbModels) {
		if (initializationRequired == true) {
			resultClusterSet = new ClusterSet();
			modelScores = new double[nbModels];
			modelScoresPrevious = new double[nbModels];
			for (int i = 0; i < nbModels; i++) {
				modelScoresPrevious[i] = 0.0;
				modelScores[i] = 0.0;
			}
			fillTransitionMatrix();
			//
			previousScores = new double[nbStates];
			currentScores = new double[nbStates];
			previousFeatures = new int[nbStates];
			currentFeatures = new int[nbStates];
			initializationRequired = false;
		}
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
	public void accumulate(AudioFeatureSet featureSet, Segment segment) throws DiarizationException, IOException {
		int nbStates = stateList.size();
		int nbModels = modelList.size();
		featureSet.setCurrentShow(segment.getShowName());
		int startFeatureIndex = segment.getStart();

		int featureIndex = startFeatureIndex;
		int segmentLength = segment.getLength();
		int segmentLast = featureIndex + segmentLength;
		// data = new TreeMap<Integer, int[]> ();
		initialize(nbStates, nbModels);
		for (int i = 0; i < nbStates; i++) {
			previousScores[i] = Double.NEGATIVE_INFINITY;
		}

		for (int i = 0; i < modelDurationConstraintList.size(); i++) {
			int diff = modelDurationConstraintList.get(i) - segmentLength;
			if (diff <= 0) {
				previousScores[modelEndStateIndiceList.get(i)] = 0.0;
			} else {
				previousScores[modelMiddleStateList.get(i)[diff]] = 0.0;
			}
		}

		indexPath = 0;
		for (int i = 0; i < nbStates; i++) {
			previousFeatures[i] = -1;
		}
		path = new ArrayList<PotentialSegment>(segmentLength);
		segmentList.add(segment);
		// Treating all the features (including the first one)
		for (; featureIndex < segmentLast; featureIndex += shift, indexPath++) {
			// logger.info("decoder index :"+featureIndex+ " shift:"+shift+ " indexpath:"+indexPath);
			Arrays.fill(currentScores, Double.NEGATIVE_INFINITY);
			int viterbiColumn[] = new int[nbStates];
			computeScoreForAllModels(featureSet, featureIndex);

			// middle states (first state is the middle list)
			for (int modelIndice = 0; modelIndice < modelMiddleStateList.size(); modelIndice++) {
				Integer[] middleStates = modelMiddleStateList.get(modelIndice);
				double score = modelScores[modelIndice];
				if (middleStates != null) {
					for (Integer middleState : middleStates) {
						int middleIndice = middleState;
						currentScores[middleIndice + 1] = previousScores[middleIndice] + score;
						currentFeatures[middleIndice + 1] = previousFeatures[middleIndice];
						viterbiColumn[middleIndice + 1] = middleIndice;
					}
				}
			}

			// last states
			for (int endModel = 0; endModel < modelEndStateIndiceList.size(); endModel++) {
				int endIndice = modelEndStateIndiceList.get(endModel);
				double endPreviousScore = previousScores[endIndice];

				// loop: need to be better than middle state
				double score = (endPreviousScore - loopPenaltyList.get(endModel)) + modelScores[endModel];
				if (score > currentScores[endIndice]) { // keep the loop
					currentScores[endIndice] = score;
					currentFeatures[endIndice] = previousFeatures[endIndice];
					viterbiColumn[endIndice] = endIndice;
				}

				// connect to an other model
				endPreviousScore -= exitPenaltyList.get(endModel);
				for (int startModel = 0; startModel < modelEntryStateIndiceList.size(); startModel++) {
					if ((startModel != endModel) || (featureIndex == startFeatureIndex)) {
						int startIndice = modelEntryStateIndiceList.get(startModel);
						score = endPreviousScore + modelScores[startModel];
						if (score > currentScores[startIndice]) {
							currentScores[startIndice] = score;
							currentFeatures[startIndice] = indexPath;
							viterbiColumn[startIndice] = endIndice;
						}
					}
				}
			}

			double scoreMax = Double.NEGATIVE_INFINITY;
			int indexModelMax = -1;
			int indexStateMax = -1;

			for (int endModel = 0; endModel < modelEndStateIndiceList.size(); endModel++) {
				int endIndice = modelEndStateIndiceList.get(endModel);
				if (currentScores[endIndice] > scoreMax) {
					scoreMax = currentScores[endIndice];
					indexModelMax = endModel;
					indexStateMax = endIndice;
				}
			}

			int previous = currentFeatures[indexStateMax];
			if (previous >= 0) {
				if (previous == indexPath) {
					// à vérifier
// logger.info("cas 1");
					startFeatureIndex = featureIndex;
					previous = previous - 1;
// previous = previous - oldShift;
				} else {
// logger.info("cas 2");
					startFeatureIndex = path.get(previous).lastFeatureIndex;
				}
			} else {
// logger.info("cas 3");
				startFeatureIndex = segment.getStart();
			}
			PotentialSegment info = new PotentialSegment(segment.getShowName(), startFeatureIndex, featureIndex, previous, indexModelMax);
// logger.info("path t_start: "+startFeatureIndex+" t_last:"+featureIndex+" i_prev:"+previous+" i_cur:"+indexPath+" model:"+indexModelMax);
			path.add(info);
// logger.info("--------");

			// save context
			double scoresTmp[] = currentScores;
			currentScores = previousScores;
			previousScores = scoresTmp;

			int featureTmp[] = currentFeatures;
			currentFeatures = previousFeatures;
			previousFeatures = featureTmp;
		}
		makePath(segment);
		path = null;
	}

	/**
	 * Forward pass for a segment
	 * 
	 * Probabilities computed at each frame.
	 * 
	 * @param featureSet the features
	 * @param segment the segment
	 * @param list the list
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void accumulate(AudioFeatureSet featureSet, Segment segment, List<Integer> list) throws DiarizationException, IOException {
		int nbStates = stateList.size();
		int nbModels = modelList.size();
		featureSet.setCurrentShow(segment.getShowName());
		int startFeatureIndex = segment.getStart();

		int featureIndex = startFeatureIndex;
		int segmentLength = segment.getLength();
		int segmentLast = featureIndex + segmentLength;
		// data = new TreeMap<Integer, int[]> ();
		initialize(nbStates, nbModels);
		for (int i = 0; i < nbStates; i++) {
			previousScores[i] = Double.NEGATIVE_INFINITY;
		}

		for (int i = 0; i < modelDurationConstraintList.size(); i++) {
			int diff = modelDurationConstraintList.get(i) - segmentLength;
			if (diff <= 0) {
				previousScores[modelEndStateIndiceList.get(i)] = 0.0;
			} else {
				previousScores[modelMiddleStateList.get(i)[diff]] = 0.0;
			}
		}

		indexPath = 0;
		for (int i = 0; i < nbStates; i++) {
			previousFeatures[i] = -1;
// logger.info("start prev score "+i+": "+previousScores[i]);
		}
		path = new ArrayList<PotentialSegment>(segmentLength);
		segmentList.add(segment);
		int oldShift = 0;
		// Treating all the features (including the first one)
		for (; featureIndex < segmentLast; featureIndex += shift, indexPath++) {
			shift = list.remove(0);
// logger.info("decoder index :"+featureIndex+ " shift:"+shift+ " indexpath:"+indexPath);
			Arrays.fill(currentScores, Double.NEGATIVE_INFINITY);
			int viterbiColumn[] = new int[nbStates];
			computeScoreForAllModels(featureSet, featureIndex);

			// middle states (first state is the middle list)
			for (int modelIndice = 0; modelIndice < modelMiddleStateList.size(); modelIndice++) {
				Integer[] middleStates = modelMiddleStateList.get(modelIndice);
				double score = modelScores[modelIndice];
				if (middleStates != null) {
					for (Integer middleState : middleStates) {
						int middleIndice = middleState;
						currentScores[middleIndice + 1] = previousScores[middleIndice] + score;
						currentFeatures[middleIndice + 1] = previousFeatures[middleIndice];
						viterbiColumn[middleIndice + 1] = middleIndice;
					}
				}
			}

			// last states
			for (int endModel = 0; endModel < modelEndStateIndiceList.size(); endModel++) {
				int endIndice = modelEndStateIndiceList.get(endModel);
				double endPreviousScore = previousScores[endIndice];
// logger.info("index :"+endModel+ " loop: "+loopPenaltyList.get(endModel)+ " exit: "+exitPenaltyList.get(endModel));
				// loop: need to be better than middle state
				double score = (endPreviousScore - loopPenaltyList.get(endModel)) + modelScores[endModel];
				if (score > currentScores[endIndice]) { // keep the loop
					currentScores[endIndice] = score;
					currentFeatures[endIndice] = previousFeatures[endIndice];
					viterbiColumn[endIndice] = endIndice;
				}

				// connect to an other model
				endPreviousScore -= exitPenaltyList.get(endModel);
				for (int startModel = 0; startModel < modelEntryStateIndiceList.size(); startModel++) {
					if ((startModel != endModel) || (featureIndex == startFeatureIndex)) {
						int startIndice = modelEntryStateIndiceList.get(startModel);
						score = endPreviousScore + modelScores[startModel];
						if (score > currentScores[startIndice]) {
							currentScores[startIndice] = score;
							currentFeatures[startIndice] = indexPath;
							viterbiColumn[startIndice] = endIndice;
						}
					}
				}
			}

			double scoreMax = Double.NEGATIVE_INFINITY;
			int indexModelMax = -1;
			int indexStateMax = -1;

			for (int endModel = 0; endModel < modelEndStateIndiceList.size(); endModel++) {
				int endIndice = modelEndStateIndiceList.get(endModel);
// logger.info("cur score "+endIndice+": "+currentScores[endIndice]);
				if (currentScores[endIndice] > scoreMax) {
					scoreMax = currentScores[endIndice];
					indexModelMax = endModel;
					indexStateMax = endIndice;
				}
			}

			int previous = currentFeatures[indexStateMax];
			if (previous >= 0) {
				if (previous == indexPath) {
					// à vérifier
// logger.info("cas 1");
					startFeatureIndex = (featureIndex - oldShift) + 1;
					previous = previous - 1;
					// previous = previous - oldShift;
				} else {
// logger.info("cas 2");
					startFeatureIndex = path.get(previous).lastFeatureIndex;
				}
			} else {
// logger.info("cas 3");
				startFeatureIndex = segment.getStart();
			}
			PotentialSegment info = new PotentialSegment(segment.getShowName(), startFeatureIndex, featureIndex, previous, indexModelMax);
// logger.info("path t_start: "+startFeatureIndex+" t_last:"+featureIndex+" i_prev:"+previous+" i_cur:"+indexPath+" model:"+indexModelMax);
			path.add(info);
// logger.info("--------");

			// save context
			double scoresTmp[] = currentScores;
			currentScores = previousScores;
			previousScores = scoresTmp;

			int featureTmp[] = currentFeatures;
			currentFeatures = previousFeatures;
			previousFeatures = featureTmp;
			oldShift = shift;
		}
		// list.add(0, shift);
		if (SpkDiarizationLogger.DEBUG) logger.info("**seg start: " + startFeatureIndex + " end: " + segmentLast + " len: " + segmentLength
				+ " lenList: " + featureIndex);

		makePath(segment);
		path = null;
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
	protected double computeLogLikelihoodRatioUBM(AudioFeatureSet featureSet, int featureIndexStart) throws DiarizationException {
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
				if (topGaussianIndices.get(featureIndex) == null) {
					logger.warning("topgaussian NULL, index:" + featureIndex + " nbTop: " + nbTopGaussians);
				}
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
			computeLogLikelihoodRatioUBM(featureSet, featureIndex);
			for (int idxModel = 0; idxModel < modelList.size(); idxModel++) {
				modelScoresPrevious[idxModel] = modelScores[idxModel];
				modelScores[idxModel] = computeLogLikelihoodRatioModel(featureSet, featureIndex, idxModel);
			}
		} else {
			if (nbTopGaussians > 0) {
				if (SpkDiarizationLogger.DEBUG)  logger.info("compute topgaussian index:"+featureIndex);
				computeLogLikelihoodRatioUBM(featureSet, featureIndex);
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
		logger.finer("states : initializationRequired --> " + initializationRequired);
		logger.finer("nb states : " + stateList.size());
		for (int i = 0; i < stateList.size(); i++) {
			logger.finer("\t state = " + i + " idx model = " + stateList.get(i));
		}
		if (modelEntryStateIndiceList != null) {
			for (int i = 0; i < modelEntryStateIndiceList.size(); i++) {
				logger.finer("modelEntryStateIndices[" + i + "] = " + modelEntryStateIndiceList.get(i));
			}
		}
		if (modelMiddleStateList != null) {
			for (int i = 0; i < modelMiddleStateList.size(); i++) {
				String message = "modelMiddleStateList[" + i + "] = ";
				Integer[] tmp = modelMiddleStateList.get(i);
				for (Integer element : tmp) {
					message += " " + element;
				}
				logger.finer(message);
			}
		}
		if (modelEndStateIndiceList != null) {
			for (int i = 0; i < modelEndStateIndiceList.size(); i++) {
				logger.finer("modelEndStateIndices[" + i + "] = " + modelEndStateIndiceList.get(i));
			}
		}
	}

	/**
	 * Initialization before decoding Creation of the transition matrix, taking into account duration constraints.
	 */
	protected void fillTransitionMatrix() {
		modelMiddleStateList = new ArrayList<Integer[]>();
		modelEndStateIndiceList = new ArrayList<Integer>();
		for (int modelIndex = 0; modelIndex < modelList.size(); modelIndex++) {
			int firstStateIndex = modelEntryStateIndiceList.get(modelIndex);
			int lastStateIndex = (firstStateIndex + durationConstraintValueList.get(modelIndex)) - 1;
			modelEndStateIndiceList.add(lastStateIndex);

			if (modelDurationConstraintList.get(modelIndex) != NO_DURATION_CONSTRAINT) {
				Integer[] tmp = new Integer[durationConstraintValueList.get(modelIndex) - 1];
				int i = 0;
				for (int stateIndex = firstStateIndex; stateIndex < lastStateIndex; stateIndex++, i++) {
					tmp[i] = stateIndex;
				}
				modelMiddleStateList.add(tmp);
			}
		}
	}

	/**
	 * Adds the segment.
	 * 
	 * @param showName the show name
	 * @param start the start
	 * @param last the last
	 * @param indexModel the index model
	 * @param info the info
	 * @param rate the rate
	 */
	protected void addSegment(String showName, int start, int last, int indexModel, String info, float rate) {
		Cluster cluster = resultClusterSet.getOrCreateANewCluster(modelList.get(indexModel).getName());
		Segment newSegment = new Segment(showName, 0, 1, cluster, rate);
		newSegment.setStartAndLast(start, last);
		newSegment.setInformation("initial", info);
		//newSegment.debug(1);
		cluster.addSegment(newSegment);
	}

	/**
	 * Make path.
	 * 
	 * @param segment the segment
	 */
	public void makePath(Segment segment) {
		int i = path.size() - 1;
		PotentialSegment info = path.get(i);
		addSegment(info.showName, info.startFeatureIndex, segment.getLast(), info.model, Integer.toString(segment.getStart()), segment.getRate());
		i = info.previousIndex;
		while (i >= 0) {
			info = path.get(i);
			addSegment(info.showName, info.startFeatureIndex, info.lastFeatureIndex - 1, info.model, Integer.toString(segment.getStart()), segment.getRate());
			i = info.previousIndex;
		}
		path.clear();
	}

	/**
	 * Gets the cluster set.
	 * 
	 * @return the cluster set
	 */
	public ClusterSet getClusterSet() {
		resultClusterSet.collapse();
		return resultClusterSet;
	}

	/**
	 * Sets the compute log likelihood ratio.
	 * 
	 * @param computeLogLikelihoodRatio the new compute log likelihood ratio
	 */
	public void setComputeLogLikelihoodRatio(boolean computeLogLikelihoodRatio) {
		this.computeLogLikelihoodRatio = computeLogLikelihoodRatio;
	}

	/**
	 * Use ntop Gaussian in likelihood computation.
	 * 
	 * @param n the number of Gaussians
	 * @param gmm the UBM
	 */
	public void setGMMForTopGaussian(int n, GMM gmm) {
		nbTopGaussians = n;
		ubm = gmm;
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
			if (SpkDiarizationLogger.DEBUG) logger.finer("Model penalty=" + exitPenalty + ":" + loopPenalty + " model=" + i + " / "
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
				if (SpkDiarizationLogger.DEBUG) logger.finer("Duration Minimal=" + durationConstraintValue + " model=" + i);
				addModelWithMinimalDuration(modelList.get(i), exitPenalty, loopPenalty, durationConstraintValue);
				break;
			case VITERBI_PERIODIC_DURATION:
				if (SpkDiarizationLogger.DEBUG) logger.finer("Duration periodic=" + durationConstraintValue + " model=" + i);
				addModelWithPeriodicDuration(modelList.get(i), exitPenalty, loopPenalty, durationConstraintValue);
				break;
			case VITERBI_FIXED_DURATION: // stupid case
				if (SpkDiarizationLogger.DEBUG) logger.finer("Duration fixed=" + durationConstraintValue + " model=" + i);
				addModelWithFixedDuration(modelList.get(i), exitPenalty, loopPenalty, durationConstraintValue);
				break;
			default:
				addModel(modelList.get(i), exitPenalty, loopPenalty);
			}
		}
	}
}
