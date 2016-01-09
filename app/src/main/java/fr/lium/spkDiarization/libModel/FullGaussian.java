/**
 * <p>
 * FullGaussian
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
 * Full Gaussian
 */

package fr.lium.spkDiarization.libModel;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.DoubleVector;
import fr.lium.spkDiarization.lib.SymmetricMatrix;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.ParameterMAP;

// TODO: Auto-generated Javadoc

/**
 * The Class for Gaussian with Full covariance matrix.
 */
public class FullGaussian extends Gaussian implements Cloneable {

    /** The covariance matrix. */
    protected SymmetricMatrix covariance;
    /** The invert covariance matrix. */
    protected SymmetricMatrix invertCovariance;
    /** The accumulator. */
    private Accumulator accumulator;
    /** The tmp log l full. */
    private DoubleVector tmpLogLFull;
    /** The tmp log l full2. */
    private DoubleVector tmpLogLFull2;

    /**
     * Instantiates a new full gaussian.
     *
     * @param _dim the _dim
     */
    public FullGaussian(int _dim) {
        super(_dim, Gaussian.FULL);
        covariance = new SymmetricMatrix();
        invertCovariance = new SymmetricMatrix();
        reset();
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#add(fr.lium.spkDiarization.lib.Gaussian, double)
     */
    @Override
    public void add(Gaussian _m1, double w) throws DiarizationException {
        FullGaussian m1 = (FullGaussian) _m1;
        // check
        if (m1.getKind() != getKind()) {
            throw new DiarizationException("Gauss::Acc: add() 1 error (kind)");
        }
        accumulator.count += m1.accumulator.count;
        accumulator.weight += (m1.accumulator.weight * w);
        for (int j = 0; j < dim; j++) {
            accumulator.mean.increment(j, w * m1.accumulator.mean.get(j));
            for (int k = j; k < dim; k++) {
                accumulator.cov.increment(j, k, w * m1.accumulator.cov.get(j, k));
            }
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(fr.lium.spkDiarization.lib.FeatureSet, int)
     */
    @Override
    public void addFeature(FeatureSet features, int i) throws DiarizationException {
        addFeature(features, i, 1.0);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(fr.lium.spkDiarization.lib.FeatureSet, int, double)
     */
    @Override
    public void addFeature(FeatureSet features, int i, double _weight) throws DiarizationException {
        accumulator.count++;
        accumulator.weight += _weight;
        float[] frame = features.getFeature(i);
        for (int j = 0; j < dim; j++) {
            double v = _weight * frame[j];
            accumulator.mean.set(j, accumulator.mean.get(j) + v);
            for (int k = j; k < dim; k++) {
                accumulator.cov.increment(j, k, v * frame[k]);
            }
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(float[], double)
     */
    @Override
    public void addFeature(float[] frame, double weight) throws DiarizationException {
        accumulator.count++;
        accumulator.weight += weight;
        for (int j = 0; j < dim; j++) {
            double v = weight * frame[j];
            accumulator.mean.set(j, accumulator.mean.get(j) + v);
            for (int k = j; k < dim; k++) {
                accumulator.cov.increment(j, k, v * frame[k]);
            }
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#clone()
     */
    @Override
    public Object clone() {
        FullGaussian result = (FullGaussian) (super.clone());
        result.covariance = (SymmetricMatrix) (covariance.clone());
        result.invertCovariance = (SymmetricMatrix) (invertCovariance.clone());
        result.accumulator = (Accumulator) (accumulator.clone());
        if (tmpLogLFull != null) {
            result.tmpLogLFull = (DoubleVector) (tmpLogLFull.clone());
        }
        if (tmpLogLFull2 != null) {
            result.tmpLogLFull2 = (DoubleVector) (tmpLogLFull2.clone());
        }
        return result;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#computeInvertCovariance()
     */
    @Override
    public boolean computeInvertCovariance() {
        invertCovariance = covariance;
        SymmetricMatrix tmp_invCov = null;
        boolean ok = true;
        try {
            tmp_invCov = covariance.invert(invertCovariance);
        } catch (DiarizationException mce) {
            okVar = false;
            ok = false;
        }
        if (ok) {
            invertCovariance = tmp_invCov;
        }
        return ok;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#computeLikelihoodConstant()
     */
    @Override
    public void computeLikelihoodConstant() {
        double det = covariance.getLogDetminant();
        likelihoodConstant = 1.0 / (Math.pow((2.0 * Math.PI), (0.5 * dim)) * Math.pow(Math.exp(det), 0.5));
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#debug(int)
     */
    @Override
    public void debug(int level) throws DiarizationException {
        // System.out.println("debug[GaussFull] \t @=" + this);
        System.out.print("model Gauss<" + kind + "> dim=" + dim);
        System.out.print(" count=" + count + " weight=" + weight);
        System.out.println(" logDet=" + covariance.getLogDetminant() + " cstL=" + likelihoodConstant);
        if (level > 0) {
            System.out.print("debug[GaussFull] \t model mean = ");
            if (okModel == 0) {
                for (int i = 0; i < dim; i++) {
                    System.out.print(" " + getMean(i));
                }
            } else {
                System.out.print("uninitalize");
            }
            System.out.println();
            if (level > 1) {
                System.out.print("debug[GaussFull] \t model " + ((kind == Gaussian.FULL) ? "FULL" : "DIAG") + " cov = ");
                if (okModel == 0) {
                    for (int i = 0; i < dim; i++) {
                        for (int j = 0; j < dim; j++) {
                            System.out.print(" " + getCovariance(i, j) + "(" + getInvertCovariance(i, j) + ")");
                        }
                        System.out.println();
                    }
                } else {
                    System.out.print("uninitalize");
                }
                System.out.println();
            }
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#debugAccumulator()
     */
    @Override
    public void debugAccumulator() {
        for (int i = 0; i < 24; i++) {
            System.out.println("debug.acc acc.mean(" + i + ")=" + accumulator.mean.get(i));
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#getAccumulatorCount()
     */
    @Override
    public int getAccumulatorCount() {
        return accumulator.count;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
     */
    @Override
    public double getAndAccumulateLikelihood(FeatureSet features, int featureIndex, boolean lhOk) throws DiarizationException {
        lhOk = true;
        double tmp = 0.0;
        float[] frame = features.getFeature(featureIndex);
        for (int j = 0; j < dim; j++) {
            tmpLogLFull.set(j, frame[j] - mean.get(j));
        }
        for (int j = 0; j < dim; j++) {
            tmpLogLFull2.set(j, 0.0);
            for (int k = 0; k < dim; k++) {
                tmpLogLFull2.increment(j, tmpLogLFull.get(k) * invertCovariance.get(k, j));
            }
            tmp += (tmpLogLFull.get(j) * tmpLogLFull2.get(j));
        }
        tmp *= (-0.5);
        score.lh = weight * likelihoodConstant * Math.exp(tmp);
        if (Double.isInfinite(score.lh) || Double.isNaN(score.lh) || (score.lh == 0)) {
            score.lh = Double.MIN_VALUE;
            lhOk = false;
            //throw new DiarizationException("GaussDiag : getAndAccumulateLikelihood lh="+score.lh);
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
    public double getAndAccumulateLikelihood(FeatureSet features, int featureIndex) throws DiarizationException {
        boolean lhOk = true;
        return getAndAccumulateLikelihood(features, featureIndex, lhOk);
    }

	/*public double getAndAccumulateLogLikelihood(FeatureSet features, int featureIndex) throws DiarizationException {
		getAndAccumulateLikelihood(features, featureIndex);
		return getLogLikelihood();
	
	}*/

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#getCovariance(int, int)
     */
    @Override
    public double getCovariance(int i, int j) throws DiarizationException {
        return covariance.get(i, j);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getEM()
     */
    @Override
    public int setModelFromAccululator() {
        int res = 0;
        initModel();
        for (int j = 0; j < dim; j++) {
            mean.set(j, accumulator.mean.get(j) / accumulator.weight);
        }
        for (int j = 0; j < dim; j++) {
            for (int k = j; k < dim; k++) {
                covariance.set(j, k, (accumulator.cov.get(j, k) / accumulator.weight) - (getMean(j) * getMean(k)));
                if (j == k) {
                    if (covariance.checkPositifValue(j, j) == false) {
                        res = -1;
                        okVar = false;
                    }
                }
            }
        }
        count = accumulator.count;
        weight = accumulator.weight / accumulator.count;
        if (computeInvertCovariance() == false) {
            res = -2;
        }
        setGLR();
        computeLikelihoodConstant();
        return res;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#getInvertCovariance(int, int)
     */
    @Override
    public double getInvertCovariance(int i, int j) {
        return invertCovariance.get(i, j);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#getMAP(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.parameter.ParameterMAP)
     */
    @Override
    public int setAdaptedModelFromAccumulator(Gaussian world, ParameterMAP mapControl) throws DiarizationException {
        int res = 0;
        double alpha = mapControl.getPrior();
        double p = accumulator.weight + alpha;
        if (mapControl.isMeanAdaptatation()) {
            for (int j = 0; j < dim; j++) {
                mean.set(j, accumulator.mean.get(j) + alpha * world.getMean(j) / p);
            }
        }
        if (mapControl.isCovarianceAdaptation()) {
            for (int j = 0; j < dim; j++) {
                for (int k = j; k < dim; k++) {
                    double mej = (accumulator.mean.get(j) + alpha * world.getMean(j)) / p;
                    double mek = (accumulator.mean.get(k) + alpha * world.getMean(k)) / p;
                    double cjk = (accumulator.cov.get(j, k) + alpha * (world.getCovariance(j, k) + world.getMean(j) * world.getMean(k))) / p - (mej * mek);
                    covariance.set(j, k, cjk);
                    if (j == k) {
                        if (covariance.checkPositifValue(j, j) == false) {
                            res = -1;
                            okVar = false;
                        }
                    }
                }
            }
            computeInvertCovariance();
        }
        setGLR();
        computeLikelihoodConstant();
        return res;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#getMAP_LIN(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.parameter.ParameterMAP)
     */
    @Override
    public int setLinearAdaptedModelFromAccumulator(Gaussian world, ParameterMAP mapControl) throws DiarizationException {
        int res = 0;
        double alpha = mapControl.getPrior();
        DiagGaussian diagWorld = (DiagGaussian) world;
        double beta = 1.0 - alpha;
        if (mapControl.isMeanAdaptatation()) {
            for (int j = 0; j < dim; j++) {
                double value = accumulator.mean.get(j) / accumulator.weight;
                mean.set(j, (alpha * value + beta * diagWorld.mean.get(j)));
            }
        }
        if (mapControl.isCovarianceAdaptation()) {
            for (int j = 0; j < dim; j++) {
                for (int k = j; k < dim; k++) {
                    double value = accumulator.mean.get(j) / accumulator.weight;
                    double mej = alpha * value + beta * diagWorld.mean.get(j);
                    value = accumulator.mean.get(k) / accumulator.weight;
                    double mek = alpha * value + beta * diagWorld.mean.get(k);
                    double cjk = (alpha * accumulator.cov.get(j, k) + beta * (world.getCovariance(j, k) + world.getMean(j) * world.getMean(k))) - (mej * mek);
                    covariance.set(j, k, cjk);
                    if (j == k) {
                        if (covariance.checkPositifValue(j, j) == false) {
                            res = -1;
                            okVar = false;
                        }
                    }
                }
            }
            computeInvertCovariance();
        }
        setGLR();
        computeLikelihoodConstant();
        return res;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#init()
     */
    @Override
    public void initModel() {
        covariance = new SymmetricMatrix(dim);
        if (okModel != 0) {
            mean = new DoubleVector(dim);
            okModel = 0;
        }
        count = 0;
        weight = 0.0;
        likelihoodConstant = 0.0;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#initAccumulator()
     */
    @Override
    public void initStatisticAccumulator() {
        accumulator = new FullGaussian.Accumulator();
        accumulator.cov = new SymmetricMatrix(dim);
        accumulator.mean = new DoubleVector(dim);
        accumulator.count = 0;
        accumulator.weight = 0.0;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#initScore()
     */
    @Override
    public void initScoreAccumulator() {
        super.initScoreAccumulator();
        tmpLogLFull = new DoubleVector(dim);
        tmpLogLFull2 = new DoubleVector(dim);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#merge(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.lib.Gaussian)
     */
    @Override
    public void merge(Gaussian _m1, Gaussian _m2) throws DiarizationException {
        FullGaussian m1 = (FullGaussian) _m1;
        FullGaussian m2 = (FullGaussian) _m2;
        // Check
        if (!(m1.kind == m2.kind) && (m1.kind == kind)) {
            throw new DiarizationException("Gauss::Acc: merge() error (kind)");
        }
        accumulator.count = m1.accumulator.count + m2.accumulator.count;
        accumulator.weight = m1.accumulator.weight + m2.accumulator.weight;
        for (int j = 0; j < dim; j++) {
            accumulator.mean.set(j, m1.accumulator.mean.get(j) + m2.accumulator.mean.get(j));
            for (int k = j; k < dim; k++) {
                accumulator.cov.set(j, k, m1.accumulator.cov.get(j, k) + m2.accumulator.cov.get(j, k));
            }
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#removeFeatureFromAccumulator(fr.lium.spkDiarization.lib.FeatureSet, int, double)
     */
    @Override
    public void removeFeatureFromAccumulator(FeatureSet features, int i, double _weight) throws DiarizationException {
        accumulator.count--;
        accumulator.weight -= _weight;
        float[] frame = features.getFeature(i);
        for (int j = 0; j < dim; j++) {
            double v = _weight * frame[j];
            accumulator.mean.set(j, accumulator.mean.get(j) - v);
            for (int k = j; k < dim; k++) {
                accumulator.cov.set(j, k, accumulator.cov.get(j, k) - v * frame[k]);
            }
        }
    }

    /**
     * Reset.
     */
    private void reset() {
        okModel = -1;
        mean = new DoubleVector();
        covariance = new SymmetricMatrix();
        invertCovariance = new SymmetricMatrix();
        count = 0;
        weight = 0.0;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#resetAccumulator()
     */
    @Override
    public void resetStatisticAccumulator() {
        accumulator.cov = new SymmetricMatrix();
        accumulator.mean = new DoubleVector();
        accumulator.count = 0;
        accumulator.weight = 0.0;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#resetScore()
     */
    @Override
    public void resetScoreAccumulator() {
        super.resetScoreAccumulator();
        if (tmpLogLFull != null) {
            tmpLogLFull = null;
        }
        if (tmpLogLFull2 != null) {
            tmpLogLFull2 = null;
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#setCovariance(int, int, double)
     */
    @Override
    public void setCovariance(int i, int j, double v) {
        covariance.set(i, j, v);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#setGLR()
     */
    @Override
    public void setGLR() {
        score.GLR = 0.5 * count * covariance.getLogDetminant();
    }

    /**
     * The Class Accumulator.
     */
    private class Accumulator implements Cloneable {

        /** The count, number of features added in the statistic. */
        private int count;
        /** The weight, sum of weights of accumulators, useful of EM. */
        private double weight;

        /** The mean, mean accumulator, sum of the features. */
        private DoubleVector mean;

        /** The cov, cov accumulator, sum of the square features. */
        private SymmetricMatrix cov;

        /* (non-Javadoc)
         * @see java.lang.Object#clone()
         */
        @Override
        public Object clone() {
            Accumulator result = null;
            try {
                result = (Accumulator) (super.clone());
            } catch (CloneNotSupportedException e) {
            }
            result.mean = (DoubleVector) (mean.clone());
            result.cov = (SymmetricMatrix) (cov.clone());
            return result;
        }
    }

}
