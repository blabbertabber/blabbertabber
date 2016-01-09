/**
 * <p>
 * Model
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
 * Abstract Gaussian model
 */

package fr.lium.spkDiarization.libModel;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libFeature.FeatureSet;

/**
 * The Class Model.
 */
public abstract class Model implements Cloneable {

    /** Gender of the model. */
    protected String gender;
    /** name of the model. */
    protected String name;
    /** Size of a feature. */
    protected int dim;
    /** Type of Gaussian. */
    protected int kind;
    /** Score accumulator. */
    protected Score score;
    /** Score : number of top Gaussians. */
    protected int nbTop;
    /** Score : vector of top Gaussians. */
    protected int[] top;

    /**
     * Instantiates a new model.
     */
    public Model() {
        this(0, Gaussian.DIAG);
    }

    /**
     * Instantiates a new model.
     *
     * @param featureDimension the feature dimension
     */
    public Model(int featureDimension) {
        this(featureDimension, Gaussian.DIAG);
    }

    /**
     * Instantiates a new model.
     *
     * @param featureDimension the feature dimension
     * @param gaussianKind the gaussian kind
     */
    public Model(int featureDimension, int gaussianKind) {
        resetScoreAccumulator();
        dim = featureDimension;
        kind = gaussianKind;
        // id = -1;
        name = "empty";
        gender = Cluster.genderStrings[0];
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        Model result = null;
        try {
            result = (Model) (super.clone());
        } catch (CloneNotSupportedException e) {
        }
        if (score != null) {
            result.score = (Score) (score.clone());
        } else {
            result.score = new Score();
        }
        if (top != null) {
            result.top = top.clone();
        } else {
            result.top = null;
        }
        return result;
    }

    /**
     * Debug.
     *
     * @param level the level
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract void debug(int level) throws DiarizationException;

    /**
     * Accumulator : print debug information.
     */
    public abstract void debugAccumulator();

    /**
     * Score: compute the #lh and #logLh of the \e features index \e i.
     *
     * @param features the features
     * @param i the index
     *
     * @return the likelihood #lh
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract double getAndAccumulateLikelihood(FeatureSet features, int i) throws DiarizationException;

    /**
     * Score: compute the #lh and #logLh of the \e features index \e i using the top Gaussians vector \e vTop.
     *
     * @param features the features
     * @param i the index
     * @param vTop the v top
     *
     * @return the likelihood #lh
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract double getAndAccumulateLikelihoodForComponentSubset(FeatureSet features, int i, int[] vTop) throws DiarizationException;

    public abstract double getAndAccumulateLikelihood(FeatureSet features, int frameIndex, boolean lhOk) throws DiarizationException;

    //public abstract double getAndAccumulateLogLikelihood(FeatureSet features, int featureIndex) throws DiarizationException;

    /**
     * Score: get the number of accumulated log-likelihood.
     *
     * @return the count log likelihood
     */
    public int getCountLogLikelihood() {
        return score.countLogLH;
    }

    /**
     * Get the dimension of the model, ie the size of a feature vector.
     *
     * @return the dimension of the vector
     */
    public int getDim() {
        return dim;
    }

    /**
     * Gets the eM.
     *
     * @return the eM
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract int setModelFromAccululator() throws DiarizationException;

    /**
     * Get the gender of the model.
     *
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * Set the gender of the model.
     *
     * @param newGender the new gender
     */
    public void setGender(String newGender) {
        gender = newGender;
    }

    /**
     * Score: get the GLR, ie Generalized Log-likelihood Ratio.
     *
     * @return the parial GLR
     */
    public double getPartialGLR() {
        return score.GLR;
    }

    /**
     * Get the type of Gaussian: FULL ou DIAG.
     *
     * @return the kind
     */
    public int getKind() {
        return kind;
    }

    /**
     * Set the type of Gaussian: FULL ou DIAG.
     *
     * @param newGaussianKind the new gaussian kind
     */
    void setKind(int newGaussianKind) {
        kind = newGaussianKind;
    }

    /**
     * Score: get the likelihood of the current features.
     *
     * @return the likelihood
     */
    public double getLikelihood() {
        return score.lh;
    }

    /**
     * Score: get the log-likelihood of the current features.
     *
     * @return the log likelihood
     */
    public double getLogLikelihood() {
        return score.logLH;
    }

    /**
     * Score: get the mean of accumulated log-likelihood.
     *
     * @return the mean log likelihood
     */
    public double getMeanLogLikelihood() {
        return score.sumLogLH / score.countLogLH;
    }

    /**
     * Get model name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set model name.
     *
     * @param newName the new name
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * Score: get the sum and count of accumulated log-likelihood.
     *
     * @return the sum and count the log likelihood
     */
    public double[] getSumAndCountLogLikelihood() {
        double[] res = new double[2];
        res[0] = score.sumLogLH;
        res[1] = score.countLogLH;
        return res;
    }

    /**
     * Score: get the sum of accumulated log-likelihood.
     *
     * @return the sum of the log likelihood
     */
    public double getSumLogLikelihood() {
        return score.sumLogLH;
    }

    /**
     * Score: get the top vector.
     *
     * @return the top gaussians
     */
    public int[] getTopGaussians() {
        return top;
    }

    /**
     * Model: allocated the mean.
     */
    public abstract void initModel();

    /**
     * Accumulator: initialize the accumulator.
     */
    public abstract void initStatisticAccumulator();

    /**
     * Score: initialize the score accumulator.
     */
    public void initScoreAccumulator() {
        resetScoreAccumulator();
    }

    /**
     * Accumulator: reset the accumulator.
     */
    public abstract void resetStatisticAccumulator();

    /**
     * Score: reset the score accumulator.
     */
    protected void resetScoreAccumulator() {
        score = new Model.Score();
        score.lh = 0.0;
        score.logLH = 0.0;
        score.sumLogLH = 0.0;
        score.countLogLH = 0;
        score.GLR = 0.0;

    }

    public double getAndAccumulateLikelihood(float[] feature) throws DiarizationException {
        // TODO Auto-generated method stub
        return 0;
    }

    public double LogAdd(double logA, double logB) {
        double result;
        if (logA < logB) {
            double tmp = logA;
            logA = logB;
            logB = tmp;
        }
        if ((logB - logA) <= -1000.0) {
            return logA;
        } else {
            result = logA + (float) (Math.log(1.0 + (double) (Math.exp((double) (logB - logA)))));
        }
        return result;
    }

    /**
     * The Class Score.
     */
    protected class Score implements Cloneable {

        /** Generalized likelihood ratio. */
        protected double GLR = 0.0;

        /** The likelihood. */
        protected double lh = 0.0; // !<

        /** The log #lh. */
        protected double logLH = 0.0; // !< log #lh

        /** sum of #logLh until reset() call. */
        protected double sumLogLH = 0.0; // !< sum of #logLh until reset() call

        /** Tount the number of #logLh added in #sumLogLh. */
        protected int countLogLH = 0; // !< c

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        @Override
        public Object clone() {
            Score result = null;
            try {
                result = (Score) (super.clone());
            } catch (CloneNotSupportedException e) {
            }
            return result;
        }
    }
}
