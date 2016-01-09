/**
 * <p>
 * GMM
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
 * GMM is a components of full or diagonal Gaussians.
 * <p/>
 * Memory allocation is done by the call of init()
 */

package fr.lium.spkDiarization.libModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.ParameterMAP;

/**
 * The Class GMM.
 */
public class GMM extends Model implements Cloneable {

    /** The key written in the gmm file. */
    protected static String key = "mClust1";
    /** The components. */
    protected ArrayList<Gaussian> components; // vector of components

    /**
     * Instantiates a new gMM.
     */
    public GMM() {
        this(0, 0, Gaussian.DIAG);
    }

    /**
     * Instantiates a new GMM.
     *
     * @param nbComponents the nb components
     */
    public GMM(int nbComponents) {
        this(nbComponents, 0, Gaussian.DIAG);
    }

    /**
     * Instantiates a new GMM.
     *
     * @param nbComponents the nb components
     * @param _dim the _dim
     */
    public GMM(int nbComponents, int _dim) {
        this(nbComponents, _dim, Gaussian.DIAG);
    }

    /**
     * Instantiates a new GMM.
     *
     * @param nbComponents the nb components
     * @param dimension the dimension
     * @param kind the kind
     */
    public GMM(int nbComponents, int dimension, int kind) {
        super(dimension, kind);
        if (nbComponents > 0) {
            components = new ArrayList<Gaussian>(nbComponents);
            double w = 1.0 / nbComponents;
            for (int i = 0; i < nbComponents; i++) {
                addNewComponent(w);
            }
        } else {
            components = new ArrayList<Gaussian>(0);
        }
        resetScoreAccumulator();
    }

    /**
     * Copy a Gaussian.
     *
     * @param g the g
     *
     * @return the gaussian
     *
     * @throws DiarizationException the diarization exception
     */
    public Gaussian addComponent(Gaussian g) throws DiarizationException {
        components.add((Gaussian) (g.clone()));
        return components.get(components.size() - 1);
    }

    /**
     * Create a Gaussian.
     *
     * @return the gaussian
     */
    public Gaussian addNewComponent() {
        return addNewComponent(1.0);
    }

    /**
     * Create a Gaussian.
     *
     * @param weight the weight
     *
     * @return the gaussian
     */
    private Gaussian addNewComponent(double weight) {
        Gaussian g = null;
        if (kind == Gaussian.FULL) {
            g = new FullGaussian(dim);
        } else {
            g = new DiagGaussian(dim);
        }
        components.add(g);
        g.setWeight(weight);
        return (components.get(components.size() - 1));
    }

    /**
     * Generates a clone of this model, by doing a deep copy (the Gaussians in the clone are copies of, not references to, the Gaussians in the current model).
     *
     * @return the object
     */
    @Override
    public Object clone() {
        GMM result = (GMM) (super.clone());
        result.components = new ArrayList<Gaussian>(components.size());
        for (int i = 0; i < components.size(); i++) {
            result.components.add((Gaussian) (components.get(i).clone()));
        }
        return result;
    }

    /**
     * Debug.
     *
     * @throws DiarizationException the diarization exception
     */
    public void debug() throws DiarizationException {
        debug(0);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#debug(int)
     */
    @Override
    public void debug(int level) throws DiarizationException {
        // System.out.println("debug[gmm] \t @=" + this);
        System.out.print("debug[gmm] \t model type = GMM<" + kind + "> dim = " + dim);
        System.out.println(" vect.size()=" + components.size());
        // System.out.print(" id=" + id + " name=" + name);
        System.out.print(" name=" + name);
        System.out.println(" gender=" + gender);
        if (components.size() > 0) {
            for (int i = 0; i < components.size(); i++) {
                components.get(i).debug(level);
            }
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#debugAccumulator()
     */
    @Override
    public void debugAccumulator() {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).debugAccumulator();
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
     */
    @Override
    public double getAndAccumulateLikelihood(FeatureSet features, int frameIndex, boolean lhOk) throws DiarizationException {
        score.lh = 0.0;
        lhOk = true;
        // System.out.println("trace[lh] \t size = " + components.size());
        for (int i = 0; i < components.size(); i++) {
            double v = components.get(i).getAndAccumulateLikelihood(features, frameIndex);
            if (v == Double.MIN_VALUE) {
                lhOk = false;
            }
            score.lh += v;
        }
        score.logLH = Math.log(score.lh);
        score.sumLogLH += score.logLH;
        score.countLogLH++;
        return score.lh;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
     */
    @Override
    public double getAndAccumulateLikelihood(float[] feature) throws DiarizationException {
        score.lh = 0.0;
        // System.out.println("trace[lh] \t size = " + components.size());
        for (int i = 0; i < components.size(); i++) {
            double v = components.get(i).getAndAccumulateLikelihood(feature);
            score.lh += v;
        }
        score.logLH = Math.log(score.lh);
        score.sumLogLH += score.logLH;
        score.countLogLH++;
        return score.lh;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
     */
    @Override
    public double getAndAccumulateLikelihood(FeatureSet features, int frameIndex) throws DiarizationException {
        score.lh = 0.0;
        // System.out.println("trace[lh] \t size = " + components.size());
        for (int i = 0; i < components.size(); i++) {
            double v = components.get(i).getAndAccumulateLikelihood(features, frameIndex);
            score.lh += v;
        }
        score.logLH = Math.log(score.lh);
        score.sumLogLH += score.logLH;
        score.countLogLH++;
        return score.lh;
    }

    /**
     * Gets the and accumulate likelihood and find top components.
     *
     * @param features the features
     * @param frameIndex the frame index
     * @param nbOfTopComponents the number of top components
     *
     * @return the and accumulate likelihood and find top components
     *
     * @throws DiarizationException the diarization exception
     */
    public double getAndAccumulateLikelihoodAndFindTopComponents(FeatureSet features, int frameIndex, int nbOfTopComponents) throws DiarizationException {
        top = new int[nbOfTopComponents];
        EntryTop[] sortGauss = new EntryTop[components.size()];

        for (int i = 0; i < components.size(); i++) {
            sortGauss[i] = new EntryTop();
            double tmp = components.get(i).getAndAccumulateLikelihood(features, frameIndex);
            sortGauss[i].score = tmp;
            sortGauss[i].index = i;
        }
        Arrays.sort(sortGauss);
        score.lh = 0.0;
        for (int i = 0; i < nbOfTopComponents; i++) {
            EntryTop v = sortGauss[i];
            score.lh += v.score;
            top[i] = v.index;
        }
        score.logLH = Math.log(score.lh);
        score.sumLogLH += score.logLH;
        score.countLogLH++;
        return score.lh;
    }

	/* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
	 */
	/*public double getAndAccumulateLogLikelihood(FeatureSet features, int frameIndex) throws DiarizationException {
		
		score.lh = 0.0;
		score.logLH = -1000;
		//System.out.println("trace[LOG lh GMM] \t size = " + components.size());
		for (int i = 0; i < components.size(); i++) {
			double v = components.get(i).getAndAccumulateLogLikelihood(features, frameIndex);
			score.lh += components.get(i).getLikelihood();
			score.logLH = LogAdd(score.logLH, v);
		}
		//score.logLH = Math.log(score.lh);
		score.sumLogLH += score.logLH;
		score.countLogLH++;
		return score.logLH;
	}*/

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihoodForComponentSubset(fr.lium.spkDiarization.lib.FeatureSet, int, int[])
     */
    @Override
    public double getAndAccumulateLikelihoodForComponentSubset(FeatureSet features, int frameIndex, int[] componentIndices) throws DiarizationException {
        score.lh = 0;
        for (int componentIndice : componentIndices) {
            double v = components.get(componentIndice).getAndAccumulateLikelihood(features, frameIndex);
            score.lh += v;
        }
        score.logLH = Math.log(score.lh);
        score.sumLogLH += score.logLH;
        score.countLogLH++;
        return score.lh;
    }

    /**
     * Gets the and accumulate likelihood for component subset.
     *
     * @param features the features
     * @param frameIndex the frame index
     * @param componentIndices the component indices
     * @param nbTop the number of top gaussian
     *
     * @return the and accumulate likelihood for component subset
     *
     * @throws DiarizationException the diarization exception
     */
    public double getAndAccumulateLikelihoodForComponentSubset(FeatureSet features, int frameIndex, int[] componentIndices, int nbTop)
            throws DiarizationException {
        score.lh = 0.0;
        for (int i = 0; i < nbTop; i++) {
            double v = components.get(componentIndices[i]).getAndAccumulateLikelihood(features, frameIndex);
            score.lh += v;
        }
        score.logLH = Math.log(score.lh);
        score.sumLogLH += score.logLH;
        score.countLogLH++;
        return score.lh;
    }

    /**
     * Get a Gaussian by index \e idx.
     *
     * @param idx the idx
     *
     * @return the component
     */
    public Gaussian getComponent(int idx) {
        return components.get(idx);
    }

    /**
     * Get the components of Gaussians.
     *
     * @return the components
     */
    public ArrayList<Gaussian> getComponents() {
        return components;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getEM()
     */
    @Override
    public int setModelFromAccululator() throws DiarizationException {
        int res = 0;
        initModel();
        for (int i = 0; i < components.size(); i++) {
            res += components.get(i).setModelFromAccululator();
        }
        if (res != 0) {
            res = -1;
        }
        return res;
    }

    /**
     * Accumulator: compute model from accumulator, MAP mode.
     *
     * @param wld the wld
     * @param mapControl the map control parameter
     *
     * @throws DiarizationException the diarization exception
     */
    public int setAdaptedModelFromAccumulator(GMM wld, ParameterMAP mapControl) throws DiarizationException {
        int res = 0;
        if (mapControl.getMethod() == ParameterMAP.MAPMethod.MAP_LIN) {
            for (int i = 0; i < components.size(); i++) {
                res += components.get(i).setLinearAdaptedModelFromAccumulator(wld.getComponent(i), mapControl);
            }
        } else {
            for (int i = 0; i < components.size(); i++) {
                res += components.get(i).setAdaptedModelFromAccumulator(wld.getComponent(i), mapControl);
            }
        }
        return res;
    }

    /**
     * Get the number of components.
     *
     * @return the number of components
     */
    public int getNbOfComponents() {
        return components.size();
    }

    /**
     * Initialize the model.
     */
    @Override
    public void initModel() {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).initModel();
        }
    }

    /**
     * initialize the accumulator.
     */
    @Override
    public void initStatisticAccumulator() {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).initStatisticAccumulator();
        }
    }

    /**
     * initialize the score.
     */
    @Override
    public void initScoreAccumulator() {
        super.initScoreAccumulator();
        for (int i = 0; i < components.size(); i++) {
            components.get(i).initScoreAccumulator();
        }
    }

    /**
     * Normalize the weights.
     */
    public void normWeights() {
        double s = 0;
        for (int i = 0; i < components.size(); i++) {
            s += components.get(i).getWeight();
        }
        //System.out.println("trace[gmm] \t normWeights count:" + s);
        for (int i = 0; i < components.size(); i++) {
            components.get(i).setWeight(components.get(i).getWeight() / s);
        }
    }

    /**
     * Replaces the content of this model with the content of the argument, by doing a deep copy (the Gaussians of the original model are copied, not just
     * referenced).
     *
     * @param _gmm the gmm
     *
     * @throws DiarizationException the diarization exception
     */
    void replaceWithGMM(GMM _gmm) throws DiarizationException {
        components.clear();
        kind = _gmm.kind;
        dim = _gmm.dim;
        // id = _gmm.id;
        gender = _gmm.gender;
        name = _gmm.name;
        components = new ArrayList<Gaussian>(_gmm.components.size());
        for (int i = 0; i < _gmm.components.size(); i++) {
            components.add((Gaussian) (_gmm.components.get(i).clone()));
        }
        score = _gmm.score;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#resetAccumulator()
     */
    @Override
    public void resetStatisticAccumulator() {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).resetStatisticAccumulator();
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#resetScore()
     */
    @Override
    public void resetScoreAccumulator() {
        if (components != null) {
            super.resetScoreAccumulator();
            for (int i = 0; i < components.size(); i++) {
                components.get(i).resetScoreAccumulator();
            }
        }
    }

    /**
     * Sort components.
     */
    public void sortComponents() {
        Collections.sort(this.components);
    }

    /**
     * Normalize the weights.
     *
     * @param c the c
     */
    public void updateCount(int c) {
        for (int i = 0; i < components.size(); i++) {
            components.get(i).updateCount(c);
        }
    }

    /**
     * The Class EntryTop.
     */
    private class EntryTop implements Comparable<EntryTop> {

        /** The score. */
        public double score;

        /** The index. */
        public int index;

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(EntryTop arg0) {
            if (score > arg0.score) {
                return -1;
            } else {
                if (score == arg0.score) {
                    return 0;
                }
            }
            return 1;
        }
    }


}
