/**
 * <p>
 * Decoder
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
 */

package fr.lium.spkDiarization.libDecoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.TreeSet;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.DoubleVector;
import fr.lium.spkDiarization.lib.SquareMatrix;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.Model;

/**
 * Viterbi decoder class, standard exponential duration low. One HMM is one state.
 * @deprecated
 */
public class Decoder {

    /** The feature set instance. */
    protected FeatureSet features;

    /** UBM for nTop*/
    protected GMM UBM;

    /** nTop value*/
    protected int nbTopGaussians;

    /** list of models*/
    protected ArrayList<Model> models;

    /** HMM state list*/
    protected ArrayList<Integer> states;

    /** model lenghts for each HMM ?*/
    protected ArrayList<Integer> modelLengths;

    /** transition matrix*/
    protected SquareMatrix transition;

    /** List of penalty ??*/
    protected ArrayList<Double> penalty;

    /** the current top gaussian feature index, ie the feature index in the feature set*/
    protected int currentTopGaussianFeatureIndex;

    /** data used in forward pass*/
    protected TreeMap<Integer, ViterbiColumnStates> data;

    /** list of segment ?*/
    protected ArrayList<Segment> segmentList;

    /** scores for each states of the current feature index */
    protected DoubleVector scores;

    /** scores for each states of the current feature index */
    protected DoubleVector modelScores;

    /** structure to save top Gaussians*/
    protected TreeMap<Integer, int[]> topGaussianIndices;

    /** shift factor, process order: 0, shift, 2*shift features*/
    protected int shift;

    /** compute or not the log likelihood ratio*/
    protected boolean computeLLhR;

    /**
     * Instantiates a new decoder.
     *
     * @param _features the _features
     */
    public Decoder(FeatureSet _features) {
        features = (FeatureSet) (_features.clone());
        nbTopGaussians = -1;
        UBM = null;
        models = new ArrayList<Model>();
        states = new ArrayList<Integer>();
        modelLengths = new ArrayList<Integer>();
        transition = new SquareMatrix();
        penalty = new ArrayList<Double>();
        data = new TreeMap<Integer, ViterbiColumnStates>();
        segmentList = new ArrayList<Segment>();
        scores = new DoubleVector();
        modelScores = new DoubleVector();
        shift = 1;
        computeLLhR = false;
        topGaussianIndices = new TreeMap<Integer, int[]>();
    }

    /**
     * Instantiates a new decoder.
     *
     * @param featureSet the feature set
     * @param n the n
     * @param ubm the ubm
     */
    public Decoder(FeatureSet featureSet, int n, GMM ubm) {
        features = (FeatureSet) (featureSet.clone());
        nbTopGaussians = n;
        UBM = ubm;
        currentTopGaussianFeatureIndex = -1;
        models = new ArrayList<Model>();
        states = new ArrayList<Integer>();
        modelLengths = new ArrayList<Integer>();
        transition = new SquareMatrix();
        penalty = new ArrayList<Double>();
        data = new TreeMap<Integer, ViterbiColumnStates>();
        segmentList = new ArrayList<Segment>();
        scores = new DoubleVector();
        modelScores = new DoubleVector();
        topGaussianIndices = new TreeMap<Integer, int[]>();
    }

    /**
     * Forward pass for a segment
     *
     * Probabilities computed at each frame.
     *
     * @param segment the segment
     * @param init the init
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public void accumulate(Segment segment, TreeSet<Integer> init) throws DiarizationException, IOException {
        segmentList.add(segment);
        int begin = segment.getStart();
        int end = segment.getLength() + begin;
        features.setCurrentShow(segment.getShowName());
        int nbStates = states.size();

        scores.resize(nbStates);
        scores.init(Double.NEGATIVE_INFINITY);
        DoubleVector scores2 = new DoubleVector(nbStates);// ???:Teva:20080114 Pourquoi
        // un new ici si on est
        // cense utiliser scores2
        // pour recuperer des
        // resultats du precedent
        // appel de accumulate ?
        modelScores.resize(models.size());

        DoubleVector previousScores = scores;
        DoubleVector nextScores = scores2;
        int featureIndex = begin;
        if (featureIndex < end) {
            boolean first = true;
            ViterbiColumnStates lastData = null;
            if (!data.isEmpty()) {
                lastData = data.get(data.lastKey());
            }

            if (data.get(featureIndex - 1) != lastData) {
                first = false;
            }
            for (Integer m : init) {
                if (first) {
                    double v = transition.get(m, m);
                    previousScores.set(m, v);
                } else {
                    previousScores.set(m, scores2.get(m));// !!!:Teva:20080114
                    // Tant que scores2
                    // est initialise au
                    // debut de
                    // accumulate, ceci
                    // ne sert qu'a
                    // recuperer null.
                }
            }
            featureIndex += shift;
        }
        for (; featureIndex < end; featureIndex += shift) {
            ViterbiColumnStates vc = new ViterbiColumnStates(nbStates, -1);
            computeScoreForAllModels(featureIndex);
            for (int nextStateIndex = 0; nextStateIndex < nbStates; nextStateIndex++) {
                double nextStateScore = getScoreOfModel(nextStateIndex);
                double maxScore = previousScores.get(0) + transition.get(0, nextStateIndex) + nextStateScore;
                int indexOfMaxScore = 0;
                for (int previousStateIndex = 1; previousStateIndex < nbStates; previousStateIndex++) {
                    double tmpScore = previousScores.get(previousStateIndex) + transition.get(previousStateIndex, nextStateIndex) + nextStateScore;
                    if (tmpScore > maxScore) {
                        maxScore = tmpScore;
                        indexOfMaxScore = previousStateIndex;
                    }
                }
                nextScores.set(nextStateIndex, maxScore);
                vc.set(nextStateIndex, indexOfMaxScore);
            }
            DoubleVector scoresTmp = nextScores;
            nextScores = previousScores;
            previousScores = scoresTmp;
            data.put(featureIndex, vc);
        }
        for (int m = 0; m < nbStates; m++) {
            scores.set(m, previousScores.get(m));
        }
    }

    /**
     * Define a HMM. Add a model to the heap
     *
     * @param g the model
     * @param length the length
     * @param p the penalty
     */
    public void add(Model g, int length, double p) {
        models.add(g);
        modelLengths.add(length);
        int s = models.size();
        for (int i = 0; i < length; i++) {
            states.add(s - 1);
        }
        g.initScoreAccumulator();
        penalty.add(p);
    }

    /**
     * Compute log likelihood ratio score for the given model.
     *
     * @param featureIndexStart the feature index start
     * @param idxModel the index model
     *
     * @return the LLhR
     *
     * @throws DiarizationException the diarization exception
     */
    protected double computeLLhRScoreModel(int featureIndexStart, int idxModel) throws DiarizationException {
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
        return model.getMeanLogLikelihood() - UBM.getMeanLogLikelihood();
    }

    /**
     * Compute log likelihood ratio score for the ubm.
     *
     * @param featureIndexStart the feature index start
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    protected double computeLLhRScoreUBM(int featureIndexStart) throws DiarizationException {
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
     * Compute log likelihood score for the model.
     *
     * @param featureIndexStart the feature index start
     * @param idxModel the idx model
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    protected double computeLLhScoreModel(int featureIndexStart, int idxModel) throws DiarizationException {
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
        return model.getSumLogLikelihood();
    }

    /**
     * Compute the scores of all models.
     *
     * @param featureIndex the feature index
     *
     * @throws DiarizationException the diarization exception
     */
    protected void computeScoreForAllModels(int featureIndex) throws DiarizationException {
        if (computeLLhR) {
            computeLLhRScoreUBM(featureIndex);
            for (int idxModel = 0; idxModel < models.size(); idxModel++) {
                modelScores.set(idxModel, computeLLhRScoreModel(featureIndex, idxModel));
            }
        } else {
            if (nbTopGaussians > 0) {
                computeLLhRScoreUBM(featureIndex);
            }
            for (int idxModel = 0; idxModel < models.size(); idxModel++) {
                modelScores.set(idxModel, computeLLhScoreModel(featureIndex, idxModel));
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
        for (int i = 0; i < states.size(); i++) {
            System.out.print("debug[decoder] \t");
            for (int j = 0; j < states.size(); j++) {
                System.out.print(transition.get(i, j) + " ");
            }
            System.out.println();
        }
        System.out.println("debug[decoder] \t nb states : " + states.size());
        for (int i = 0; i < states.size(); i++) {
            System.out.println("debug[decoder] \t state = " + i + " idx model = " + states.get(i));
        }
    }

    /**
     * Get the clustering
     * TODO A simplifier, faire les segments dans make path.
     *
     * @param clusters the clusters
     *
     * @return the clusters
     */
    public ClusterSet getClusters(ClusterSet clusters) {
        TreeMap<Integer, Integer> path = new TreeMap<Integer, Integer>();
        makePath(path);
        ClusterSet res = new ClusterSet();

        Hashtable<Integer, Segment> m = new Hashtable<Integer, Segment>();
        for (Segment seg : segmentList) {
            int end = seg.getStart() + seg.getLength();
            for (int i = seg.getStart(); i < end; i++) {
                m.put(i, seg);
            }
        }

        int l = shift;
        for (Integer prev : path.keySet()) {
            Cluster cluster = res.getOrCreateANewCluster(models.get(path.get(prev)).getName());
            Segment segment = (Segment) (m.get(prev).clone());
            segment.setStart(prev - l + 1);
            segment.setLength(l);
            cluster.addSegment(segment);
        }
        res.collapse();
        return res;
    }

    /**
     * Gets the score of the model.
     *
     * @param idx the index
     *
     * @return the score model
     */
    protected double getScoreOfModel(int idx) {
        return modelScores.get(states.get(idx));
    }

    /**
     * Define a HMM. Initialization before a decoding
     *
     * Clear all container
     */
    public void init() {
        for (ViterbiColumnStates vect : data.values()) {
            vect.clear();
        }
        /*
		 * Iterator<ViterbiCol> enumData = data.values().iterator(); while (enumData.hasNext()) { enumData.next().clear(); }
		 */
        data.clear();
        segmentList.clear();
    }

    /**
     * Compute the path: backward pass
     * TODO ne fonctionne pas avec des segments pas contigus 1 -transformer SegmentList en cluster 2- faire un collpase 3.
     *
     * @param path the path
     */
    protected void makePath(TreeMap<Integer, Integer> path) {
        double max = scores.get(0);
        int idx = 0;
        for (int i = 1; i < states.size(); i++) {
            if (max < scores.get(i)) {
                max = scores.get(i);
                idx = i;
            }
        }

        Integer[] dataTab = new Integer[data.size()];
        data.keySet().toArray(dataTab);
        int i = dataTab.length - 1;
        for (i = dataTab.length - 1; i >= 0; i--) {
            path.put(dataTab[i], states.get(idx));
            idx = data.get(dataTab[i]).get(idx);
        }
        data.clear();
    }

    /**
     * Make penalty.
     */
    public void makePenalty() {
        transition.resize(states.size());
        transition.init(Double.NEGATIVE_INFINITY);
        int start = 0;
        int end = 0;
        for (int i = 0; i < models.size(); i++) {
            end = start + modelLengths.get(i) - 1;
            // HMM
            for (int j = start; j < end; j++) {
                transition.set(j, j + 1, 0.0);
            }
            transition.set(end, end, 0.0);
            int startOther = 0;
            for (int j = 0; j < models.size(); j++) {
                if (i != j) {
                    transition.set(start, startOther, -penalty.get(i));
                }
                startOther += modelLengths.get(j);
            }
            start = end + 1;
        }
    }

    /**
     * Sets the compute log likelihood ratio.
     *
     * @param computeLLhR the new compute log likelihood ratio
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
// System.out.println("trace[decoder] \t setNTop");
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
}
