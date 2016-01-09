/**
 * <p>
 * DecoderWithDuration
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 * <p/>
 * Viterbi decoder class, duration low by duplication of the state.
 */

package fr.lium.spkDiarization.libDecoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.Model;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterDecoder;

public class DecoderWithDuration {
    /** HMM without duration constraint, one state per HMM*/
    static final protected int NO_DURATION_CONSTRAINT = 0;
    /** HMM with n states linked, with only transition i-->i on the last, exit state only on the last*/
    static final protected int MINIMAL_DURATION_CONSTRAINT = 1;
    /** ?? HMM with n states linked, the last is linked to the first, exit state only on the last*/
    static final protected int FIXED_DURATION_CONSTRAINT = 2;
    /** ?? HMM with n states linked, the last is linked to the first, exit state only on the last*/
    static final protected int PERIODIC_DURATION_CONSTRAINT = 4;
    /** UBM for nTop*/
    protected GMM UBM;
    /** nTop value*/
    protected int nbTopGaussians;
    /** list of models*/
    protected ArrayList<Model> models;
    /** a state stores the index of the corresponding model*/
    protected ArrayList<Integer> states;
    /** duration constraint for each model (see constants below)*/
    protected ArrayList<Integer> modelDurationConstraints;
    /** value associated with each constraint (typically a duration, expressed in number of features)*/
    protected ArrayList<Integer> durationConstraintsValues;
    /** index of the first state for each model*/
    protected ArrayList<Integer> modelEntryStateIndices;
    /** for each state, a list of the states from which transition to this state is possible*/
    protected ArrayList<Integer> validPreviousStates[];
    /** list of the states from which transition to the exit state is possible*/
    protected ArrayList<Integer> validLastStates;
    /** transition matrix*/
    protected double transitions[][];
    /** exit hmm penality*/
    protected ArrayList<Double> exitPenalties;
    /** loop hmm penality*/
    protected ArrayList<Double> loopPenalties;
    /** the current top gaussian feature index, ie the feature index in the feature set */
    protected int currentTopGaussianFeatureIndex;
    /** the current top gaussian score corresponding to currentTopGaussianFeatureIndex index */
    protected double currentTopGaussianScore;
    /** data used in forward pass; the keys are feature indices*/
    protected TreeMap<Integer, int[]> data;
    /** list of segment */
    protected ArrayList<Segment> segmentList;
    /** list of scores of the current feature */
    protected double modelScores[];
    /** list of scores of the previous feature */
    protected double modelScoresPrevious[];
    /** structure to save topGaussians*/
    protected TreeMap<Integer, int[]> topGaussianIndices;
    /** keeps track of whether the transition matrix needs to be (re)filled and data container needs to get cleared*/
    protected boolean initializationRequired;
    /** used to detect whether a segment passed to accumulate() is part of the same show as the previous segment*/
    protected String showNameForPreviousSegment;
    /** structure to store viterbi path*/
    protected TreeMap<Integer, Integer> path;
    /** shift by shift computation of feature*/
    protected int shift;
    /** compute log likelihood ratio or just log likelihood*/
    protected boolean computeLLhR;
    protected double previousScores[];
    protected double nextScores[];

    /**
     * Create a decoder, without using top Gaussians.
     */
    public DecoderWithDuration(int shift) {
// features = (FeatureSet) (featureSet.clone());
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
     */
    public DecoderWithDuration(int n, GMM ubm, boolean computeLLhR, int shift) {
// features = (FeatureSet) (featureSet.clone());
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

    public DecoderWithDuration() {
        // features = (FeatureSet) (featureSet.clone());
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
     * Forward pass for a segment
     *
     * Probabilities computed at each frame.
     *
     * @param features the features
     * @param segment the segment
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
/*	public void accumulate(FeatureSet features, Segment segment) throws DiarizationException, IOException {
        //debug();
		int nbStates = states.size();
		data = new TreeMap<Integer, int[]>();
		if (initializationRequired == true) {
			previousScores = new double[nbStates];
			nextScores = new double[nbStates];
			path = new TreeMap<Integer, Integer>();
			modelScores = new double[models.size()];
			modelScoresPrevious = new double[models.size()];
			for (int i = 0; i < models.size(); i++) {
				modelScoresPrevious[i] = 0.0;
				modelScores[i] = 0.0;
			}
			segmentList = new ArrayList<Segment>();
			showNameForPreviousSegment = FeatureSet.UNKNOWN_SHOW;
			fillTransitionMatrix();

			// Treating the case of the first feature frame if this segment is the
			// first one
			for (int i = 0; i < nbStates; i++) {
				previousScores[i] = Double.NEGATIVE_INFINITY;
			}
			for (int i = 0; i < validLastStates.size(); i++) {
				previousScores[validLastStates.get(i)] = 0.0;
			}
			
			initializationRequired = false;
		}
		segmentList.add(segment);
		features.setCurrentShow(segment.getShowName());

		int featureIndex = segment.getStart();
		int last = featureIndex + segment.getLength();
		showNameForPreviousSegment = segment.getShowName();
		// Treating all the features (including the first one)

		for (; featureIndex < last; featureIndex += shift) {
			int viterbiColumn[] = new int[nbStates];
			computeScoreForAllModels(features, featureIndex);
			for (int currentStateIndex = 0; currentStateIndex < nbStates; currentStateIndex++) {
				double currentStateScore = modelScores[states.get(currentStateIndex)];

				double maxScore = Double.NEGATIVE_INFINITY;
				int indexOfMaxScore = -1;
				for (int i = 0; i < validPreviousStates[currentStateIndex].size(); i++) {
					int previousStateIndex = validPreviousStates[currentStateIndex].get(i);
					double tmpScore = previousScores[previousStateIndex] + transitions[previousStateIndex][currentStateIndex] + currentStateScore;
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
			//	checkUniqueSolution(featureIndex, viterbiColumn);
		}
		// Saving the scores for use at the beginning of the next segment
		makePath(previousScores, segment.getStart(), last);
		data.clear();
	}*/
    public void accumulate(FeatureSet features, Segment segment) throws DiarizationException, IOException {
        //debug();
        int nbStates = states.size();
        data = new TreeMap<Integer, int[]>();
        if (initializationRequired == true) {
            path = new TreeMap<Integer, Integer>();
            modelScores = new double[models.size()];
            modelScoresPrevious = new double[models.size()];
            for (int i = 0; i < models.size(); i++) {
                modelScoresPrevious[i] = 0.0;
                modelScores[i] = 0.0;
            }
            segmentList = new ArrayList<Segment>();
            showNameForPreviousSegment = FeatureSet.UNKNOWN_SHOW;
            fillTransitionMatrix();
            initializationRequired = false;
        }

        double previousScores[] = new double[nbStates];
        double nextScores[] = new double[nbStates];

        segmentList.add(segment);
        features.setCurrentShow(segment.getShowName());

        int featureIndex = segment.getStart();
        int last = featureIndex + segment.getLength();
        // Treating the case of the first feature frame if this segment is the
        // first one
        for (int i = 0; i < nbStates; i++) {
            previousScores[i] = Double.NEGATIVE_INFINITY;
        }
        for (int i = 0; i < validLastStates.size(); i++) {
            previousScores[validLastStates.get(i)] = 0.0;
        }
        showNameForPreviousSegment = segment.getShowName();
        // Treating all the features (including the first one)

        for (; featureIndex < last; featureIndex += shift) {
            int viterbiColumn[] = new int[nbStates];
            computeScoreForAllModels(features, featureIndex);
            for (int currentStateIndex = 0; currentStateIndex < nbStates; currentStateIndex++) {
                double currentStateScore = modelScores[states.get(currentStateIndex)];

                double maxScore = Double.NEGATIVE_INFINITY;
                int indexOfMaxScore = -1;
                for (int i = 0; i < validPreviousStates[currentStateIndex].size(); i++) {
                    int previousStateIndex = validPreviousStates[currentStateIndex].get(i);
                    double tmpScore = previousScores[previousStateIndex] + transitions[previousStateIndex][currentStateIndex] + currentStateScore;
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
            //	checkUniqueSolution(featureIndex, viterbiColumn);
        }
        // Saving the scores for use at the beginning of the next segment
        makePath(previousScores, segment.getStart(), last);
        data.clear();
    }

    @SuppressWarnings("unused")
    private boolean checkUniqueSolution(int index, int viterbiColumn[]) {
        int ref = viterbiColumn[0];
        for (int i = 1; i < viterbiColumn.length; i++) {
            if (ref != viterbiColumn[i]) {
                return false;
            }
        }
        System.err.println("*** UniqueSollution at featureIndex : " + index);
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
        newModel.initScoreAccumulator();
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
    protected double computeLLhRScoreModel(FeatureSet features, int featureIndexStart, int idxModel) throws DiarizationException {
        Model model = models.get(idxModel);
        model.initScoreAccumulator();
        int end = Math.min(featureIndexStart + shift, features.getNumberOfFeatures());
        for (int featureIndex = featureIndexStart; featureIndex < end; featureIndex++) {
            if (nbTopGaussians <= 0) {
                model.getAndAccumulateLikelihood(features, featureIndex);
                UBM.getAndAccumulateLikelihood(features, featureIndex);
            } else {
                model.getAndAccumulateLikelihoodForComponentSubset(features, featureIndex, topGaussianIndices.get(featureIndex));
            }
        }
        double value = model.getMeanLogLikelihood() - UBM.getMeanLogLikelihood();
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
    protected double computeLLhRScoreUBM(FeatureSet features, int featureIndexStart) throws DiarizationException {
        int end = Math.min(featureIndexStart + shift, features.getNumberOfFeatures());
        UBM.initScoreAccumulator();
        for (int featureIndex = featureIndexStart; featureIndex < end; featureIndex++) {
            if (nbTopGaussians <= 0) {
                UBM.getAndAccumulateLikelihood(features, featureIndex);
            } else {
                if (currentTopGaussianFeatureIndex != featureIndex) {
                    UBM.getAndAccumulateLikelihoodAndFindTopComponents(features, featureIndex, nbTopGaussians);
                    currentTopGaussianFeatureIndex = featureIndex;
                    topGaussianIndices.put(featureIndex, UBM.getTopGaussians());
                }
            }
        }
        return UBM.getMeanLogLikelihood();
    }

    /**
     * Compute log likelihood for a model
     *
     * @param features the features
     * @param featureIndexStart the feature index start
     * @param idxModel the index model
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    protected double computeLLhScoreModel(FeatureSet features, int featureIndexStart, int idxModel) throws DiarizationException {
        Model model = models.get(idxModel);
        model.initScoreAccumulator();
        int end = Math.min(featureIndexStart + shift, features.getNumberOfFeatures());
        for (int featureIndex = featureIndexStart; featureIndex < end; featureIndex++) {
            if (nbTopGaussians <= 0) {
                model.getAndAccumulateLikelihood(features, featureIndex);
            } else {
                model.getAndAccumulateLikelihoodForComponentSubset(features, featureIndex, topGaussianIndices.get(featureIndex));
            }
        }
        double value = model.getSumLogLikelihood();
        if (value == Double.NEGATIVE_INFINITY) {
            System.err.println("Warning[DecoderWithDuration} getScoreForModel : score == Double.NEGATIVE_INFINITY start=" + featureIndexStart + " end=" + end + " value=" + modelScoresPrevious[idxModel]);
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
    protected void computeScoreForAllModels(FeatureSet features, int featureIndex) throws DiarizationException {
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
        if (initializationRequired == false) {
            for (int i = 0; i < states.size(); i++) {
                //		System.out.print("debug[decoder] transition ligne i="+i+"\t");
                for (int j = 0; j < states.size(); j++) {
                    //		System.out.print(transitions[i][j] + " ");
                }
                //	System.out.println();
            }
        } else {
            System.out.print("debug[decoder] \t states : initializationRequired");
        }
        System.out.println("debug[decoder] \t nb states : " + states.size());
        for (int i = 0; i < states.size(); i++) {
            System.out.println("debug[decoder] \t state = " + i + " idx model = " + states.get(i));
        }
        if (modelEntryStateIndices != null) {
            for (int i = 0; i < modelEntryStateIndices.size(); i++) {
                System.out.println("debug[decoder] \t modelEntryStateIndices[" + i + "] = " + modelEntryStateIndices.get(i));
            }
        }
        if (validLastStates != null) {
            for (int i = 0; i < validLastStates.size(); i++) {
                System.out.println("debug[decoder] \t validLastStates[" + i + "] = " + validLastStates.get(i));
            }
        }
        if (validPreviousStates != null) {
            for (int i = 0; i < validPreviousStates.length; i++) {
                System.out.print("debug[decoder] \t validPreviousStates[" + i + "] = ");
                for (int j = 0; j < validPreviousStates[i].size(); j++) {
                    System.out.print(validPreviousStates[i].get(j) + " ");
                }
                System.out.println();
            }
        }
    }

    /**
     * Initialization before decoding Creation of the transition matrix, taking into account duration constraints.
     */
    @SuppressWarnings("unchecked")
    protected void fillTransitionMatrix() {
        transitions = new double[states.size()][states.size()];
        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < states.size(); j++) {
                transitions[i][j] = Double.NEGATIVE_INFINITY;
            }
        }
        validLastStates = new ArrayList<Integer>();
        int firstStateIndex;
        for (int modelIndex = 0; modelIndex < models.size(); modelIndex++) {
            firstStateIndex = modelEntryStateIndices.get(modelIndex);
            // Setting the transitions between states of this model
            int lastStateIndex = firstStateIndex + durationConstraintsValues.get(modelIndex) - 1;
            switch (modelDurationConstraints.get(modelIndex)) {
                case MINIMAL_DURATION_CONSTRAINT:
                    for (int stateIndex = firstStateIndex; stateIndex < lastStateIndex; stateIndex++) {
                        transitions[stateIndex][stateIndex + 1] = 0.0;
                    }
                    break;
                case FIXED_DURATION_CONSTRAINT: //stupid case
                    for (int stateIndex = firstStateIndex; stateIndex < lastStateIndex; stateIndex++) {
                        transitions[stateIndex][stateIndex + 1] = 0.0;
                        validLastStates.add(stateIndex);
                    }
                    break;
                case PERIODIC_DURATION_CONSTRAINT:
                    for (int stateIndex = firstStateIndex; stateIndex < lastStateIndex; stateIndex++) {
                        transitions[stateIndex][stateIndex + 1] = 0.0;
                        validLastStates.add(stateIndex);
                    }
                    break;
            }

            // Setting penalties related to staying on this model: loop, loop
            // back to first state, forced exit, etc.
            switch (modelDurationConstraints.get(modelIndex)) {
                case NO_DURATION_CONSTRAINT:
                    transitions[lastStateIndex][lastStateIndex] = 0.0;
                    break;
                case MINIMAL_DURATION_CONSTRAINT:
                    transitions[lastStateIndex][lastStateIndex] = Math.log(0.9);
                    break;
                case FIXED_DURATION_CONSTRAINT: //stupid case
                    transitions[lastStateIndex][lastStateIndex] = Double.NEGATIVE_INFINITY;
                    break;
                case PERIODIC_DURATION_CONSTRAINT:
                    transitions[lastStateIndex][lastStateIndex] = Double.NEGATIVE_INFINITY;
                    transitions[lastStateIndex][firstStateIndex] = 0.0;
                    break;
            }
            validLastStates.add(lastStateIndex);

            // Setting penalties related to switching to another model (from the
            // last state of this model)
            int otherModelFirstStateIndex;
            for (int otherModelIndex = 0; otherModelIndex < models.size(); otherModelIndex++) {
                otherModelFirstStateIndex = modelEntryStateIndices.get(otherModelIndex);
                if (otherModelIndex != modelIndex) {
                    transitions[lastStateIndex][otherModelFirstStateIndex] = -exitPenalties.get(modelIndex);
                }
            }
            // Setting penalties related to loop to model (from the
            // last state of this model)
            transitions[lastStateIndex][lastStateIndex] = -loopPenalties.get(modelIndex);
        }

        // Setting up the lists of possible previous states
        validPreviousStates = new ArrayList[states.size()];
        for (int stateIndex = 0; stateIndex < states.size(); stateIndex++) {
            validPreviousStates[stateIndex] = new ArrayList<Integer>();
            for (int otherStateIndex = 0; otherStateIndex < states.size(); otherStateIndex++) {
                if (!Double.isInfinite(transitions[otherStateIndex][stateIndex])) {
                    validPreviousStates[stateIndex].add(otherStateIndex);
                }
            }
        }
    }

    /**
     * Get the clustering.
     *
     * @param clusters the clusters
     *
     * @return the clusters
     * TODO: A simplifier, faire les segments dans make path
     * TODO: probleme si les locuteurs n'existe pas dans la seg...
     */
    public ClusterSet getClusters(ClusterSet clusters) {
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
            Segment segment = (Segment) (m.get(prev).clone());
//			segment.setStart(prev - l); 
            segment.setStart(prev - l + 1);
            segment.setLength(l);
//			segment.setStart(prev);
//			segment.setLength(l);
            cluster.addSegment(segment);
        }

        res.collapse();
        return res;
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

        for (int i = end - 1; i >= start; i--) {
            //System.err.println("previous state:"+idx+ " i:" + i +" (start:" + start + " end:" + end + ")");
            path.put(i, states.get(idx));
            idx = data.get(i)[idx];
			/*if (idx == -1) {

				System.err.println("length:"+data.get(i).length+" ");
				for(int j=0; j < data.get(i).length; j++){
					System.err.print("j:"+j+" value:"+data.get(i)[j]+" / ");
				}
				System.err.println();
			}*/
        }
    }

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

    public void setupHMM(ArrayList<GMM> models, Parameter param) {
        // add model
        int nbOfModels = models.size();
        int nbOfPenalties = param.parameterDecoder.getExitDecoderPenalty().size();
        for (int i = 0; i < nbOfModels; i++) {
            double exitPenalty;
            double loopPenalty;
            if (i < nbOfPenalties) {
                exitPenalty = param.parameterDecoder.getExitDecoderPenalty().get(i);
                loopPenalty = param.parameterDecoder.getLoopDecoderPenalty().get(i);
            } else {
                exitPenalty = param.parameterDecoder.getExitDecoderPenalty().get(nbOfPenalties - 1);
                loopPenalty = param.parameterDecoder.getLoopDecoderPenalty().get(nbOfPenalties - 1);
            }
            if (param.trace) {
                System.out.println("trace[mDecode] \t Model penalty=" + exitPenalty + ":" + loopPenalty + " model=" + i + " / " + models.get(i).getName());
            }
            ParameterDecoder.ViterbiDurationConstraint durationConstraint;
            int durationConstraintValue;
            if (i < param.parameterDecoder.getViterbiDurationConstraints().size()) {
                durationConstraint = param.parameterDecoder.getViterbiDurationConstraints().get(i);
                durationConstraintValue = param.parameterDecoder.getViterbiDurationConstraintValues().get(i);
            } else {
                durationConstraint = param.parameterDecoder.getViterbiDurationConstraints().get(
                        param.parameterDecoder.getViterbiDurationConstraints().size() - 1);
                durationConstraintValue = param.parameterDecoder.getViterbiDurationConstraintValues().get(
                        param.parameterDecoder.getViterbiDurationConstraints().size() - 1);
            }
            switch (durationConstraint) {
                case VITERBI_MINIMAL_DURATION:
                    if (param.trace) {
                        System.out.println("trace[mDecode] \t Duration Minimal=" + durationConstraintValue + " model=" + i);
                    }
                    addModelWithMinimalDuration(models.get(i), exitPenalty, loopPenalty, durationConstraintValue);
                    break;
                case VITERBI_PERIODIC_DURATION:
                    if (param.trace) {
                        System.out.println("trace[mDecode] \t Duration periodic=" + durationConstraintValue + " model=" + i);
                    }
                    addModelWithPeriodicDuration(models.get(i), exitPenalty, loopPenalty, durationConstraintValue);
                    break;
                case VITERBI_FIXED_DURATION: //stupid case
                    if (param.trace) {
                        System.out.println("trace[mDecode] \t Duration fixed=" + durationConstraintValue + " model=" + i);
                    }
                    addModelWithFixedDuration(models.get(i), exitPenalty, loopPenalty, durationConstraintValue);
                    break;
                default:
                    if (param.trace) {
                        System.out.println("trace[mDecode] \t no duration model=" + i);
                    }
                    addModel(models.get(i), exitPenalty, loopPenalty);
            }
        }
    }

}
