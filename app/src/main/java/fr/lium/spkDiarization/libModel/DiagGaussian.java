/**
 * <p>
 * DiagGaussian
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
 * diagonal Gaussian
 */

package fr.lium.spkDiarization.libModel;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.DoubleVector;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.ParameterMAP;

/**
 * The Class DiagGaussian.
 */
public class DiagGaussian extends Gaussian implements Cloneable {

    /** covariance matrix*/
    private DoubleVector covariance;
    /** inverse covariance matrix*/
    private DoubleVector invertCovariance;
    /** the accumulator instance*/
    private Accumulator accumulator;

    /**
     * Instantiates a new diag gaussian.
     *
     * @param _dim the _dim
     */
    public DiagGaussian(int _dim) {
        super(_dim, Gaussian.DIAG);
        // cov = new SCVector();
        // covInv = new SCVector();
        reset();
        accumulator = new DiagGaussian.Accumulator();
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#add(fr.lium.spkDiarization.lib.Gaussian, double)
     */
    @Override
    public void add(Gaussian model, double w) throws DiarizationException {
        DiagGaussian gaussian = (DiagGaussian) model;
        // check
        if (!(gaussian.kind == kind)) {
            throw new DiarizationException("Gauss::Acc: add() 1 error (kind)");
        }
        accumulator.count += gaussian.accumulator.count;
        accumulator.weight += (gaussian.accumulator.weight * w);
        for (int j = 0; j < dim; j++) {
            accumulator.mean.increment(j, w * gaussian.accumulator.mean.get(j));
            accumulator.cov.increment(j, w * gaussian.accumulator.cov.get(j));
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
            accumulator.mean.increment(j, v);
            accumulator.cov.increment(j, v * frame[j]);
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(float[])
     */
    @Override
    public void addFeature(float[] frame) throws DiarizationException {
        addFeature(frame, 1.0);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(float[], double)
     */
    @Override
    public void addFeature(float[] frame, double _weight) throws DiarizationException {
// System.err.print("adFeat");
        accumulator.count++;
        accumulator.weight += _weight;
        for (int j = 0; j < dim; j++) {
            double v = _weight * frame[j];
            accumulator.mean.increment(j, v);
            accumulator.cov.increment(j, v * frame[j]);
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#clone()
     */
    @Override
    public Object clone() {
        DiagGaussian result = (DiagGaussian) (super.clone());
        result.covariance = (DoubleVector) (covariance.clone());
        result.invertCovariance = (DoubleVector) (invertCovariance.clone());
        result.accumulator = (Accumulator) (accumulator.clone());
        return result;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#computeInvertCovariance()
     */
    @Override
    public boolean computeInvertCovariance() throws DiarizationException {
        invertCovariance = (DoubleVector) (covariance.clone());
        boolean res = covariance.invert(invertCovariance);
        if (res == false) {
            okVar = false;
        }
        return res;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#computeLikelihoodConstant()
     */
    @Override
    public void computeLikelihoodConstant() {
        double det = covariance.getLogDetminant();
        likelihoodConstant = 1.0 / (Math.pow((2.0 * Math.PI), (0.5 * dim)) * Math.pow(Math.exp(det), 0.5));
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
        // System.out.println("debug[GaussDiag] \t @=" + this);
        System.out.print("model Gauss<" + kind + "> dim=" + dim);
        System.out.print(" count=" + count + " weight=" + weight);
        System.out.println(" logDet=" + covariance.getLogDetminant() + " cstL=" + likelihoodConstant);
        if (level > 0) {
            System.out.print("debug[GaussDiag] \t model mean = ");
            if (okModel == 0) {
                for (int i = 0; i < dim; i++) {
                    System.out.print(" " + getMean(i));
                }
            } else {
                System.out.print("uninitialized");
            }
            System.out.println();
            if (level > 1) {
                System.out.print("debug[GaussDiag] \t model " + ((kind == FULL) ? "FULL" : "DIAG") + " cov = ");
                if (okModel == 0) {
                    for (int i = 0; i < dim; i++) {
                        System.out.print(" " + getCovariance(i, i) + "(" + getInvertCovariance(i, i) + ")");
                    }
                } else {
                    System.out.print("uninitialized");
                }
                System.out.println();
                if (level > 2) {
                    debugAccumulator();
                }
            }

        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#debugAccumulator()
     */
    @Override
    public void debugAccumulator() {
        accumulator.debug();
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#getAccumulatorCount()
     */
    @Override
    public int getAccumulatorCount() {
        return accumulator.count;
    }

    @Override
    public double getAndAccumulateLikelihood(FeatureSet features, int featureIndex, boolean lhOk) throws DiarizationException {
        double tmp = 0.0;
        lhOk = true;
        float[] frame = features.getFeature(featureIndex);
        // System.out.println("debug[gauss] \t feature size" + frame.length);
        // System.out.println("debug[gauss] \t model size" + dim);
        for (int j = 0; j < dim; j++) {
            //System.err.print(" " + frame[j]+"/"+mean.get(j));
            double dmean = (frame[j] - mean.get(j));
            tmp -= (0.5 * dmean * dmean * invertCovariance.get(j, j));
        }
        score.lh = weight * likelihoodConstant * Math.exp(tmp);
        if (Double.isInfinite(score.lh) || Double.isNaN(score.lh) || (score.lh == 0)) {
            score.lh = Double.MIN_VALUE;
            lhOk = false;
            //System.err.println("GaussDiag : getAndAccumulateLikelihood lh="+score.lh+" featureIndex="+featureIndex+ " show"+features.getCurrentShowName()+ "score="+Math.log(score.lh));
            //throw new DiarizationException("GaussDiag : getAndAccumulateLikelihood lh="+score.lh);
        }
        score.logLH = Math.log(score.lh);
        score.sumLogLH += score.logLH;
        score.countLogLH++;
        //System.err.println(" : " + score.logLH);
        return score.lh;
    }

	/*public double getAndAccumulateLogLikelihood(FeatureSet features, int featureIndex) throws DiarizationException {
		//System.out.println("trace[LOG lh Gauss Diag] \t ");
		double x,  z;
		score.logLH = 0;
		float[] frame = features.getFeature(featureIndex);
		for (int i = 0; i < dim; i++) {
			x = frame[i] - mean.get(i);
			z = covariance.get(i, i);
			score.logLH += x * x / z + Math.log(Math.PI * 2.0 * z);
		}
		score.logLH *= -0.5;
		score.logLH += Math.log(weight);

		score.lh = Math.exp(score.logLH);
		score.sumLogLH += score.logLH;
		score.countLogLH++;
		//System.err.println(" : " + score.logLH);
		return score.logLH;
	}*/

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
     */
    @Override
    public double getAndAccumulateLikelihood(FeatureSet features, int frameIndex) throws DiarizationException {
        boolean lhOk = true;
        return getAndAccumulateLikelihood(features, frameIndex, lhOk);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#getCovariance(int, int)
     */
    @Override
    public double getCovariance(int i, int j) throws DiarizationException {
        if (i != j) {
            throw new DiarizationException("GaussDiag : getCov(long i, long j)");
        }
        return covariance.get(i);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getEM()
     */
    @Override
    public int setModelFromAccululator() throws DiarizationException {
        int res = 0;
        initModel();
        for (int j = 0; j < dim; j++) {
            mean.set(j, accumulator.mean.get(j) / accumulator.weight);
            covariance.set(j, (accumulator.cov.get(j) / accumulator.weight) - (mean.get(j) * mean.get(j)));
            if (covariance.checkPositifValue(j) == false) {
                res = -1;
                okVar = false;
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
    public double getInvertCovariance(int i, int j) throws DiarizationException {
        if (i != j) {
            throw new DiarizationException("GaussDiag::getCovInv(long i, long j)");
        }
        return invertCovariance.get(i, i);
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#getMAP(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.parameter.ParameterMAP)
     */
    @Override
    public int setAdaptedModelFromAccumulator(Gaussian world, ParameterMAP mapControl) throws DiarizationException {
        int res = 0;
        double alpha = mapControl.getPrior();
        DiagGaussian diagWorld = (DiagGaussian) world;
        double p = accumulator.weight + alpha;
        if (mapControl.isMeanAdaptatation()) {
            for (int j = 0; j < dim; j++) {
                mean.set(j, (accumulator.mean.get(j) + alpha * diagWorld.mean.get(j)) / p);
            }
        }
        if (mapControl.isCovarianceAdaptation()) {
            for (int j = 0; j < dim; j++) {
                double mej = (accumulator.mean.get(j) + alpha * diagWorld.mean.get(j)) / p;
                double cj = (accumulator.cov.get(j) + alpha * (diagWorld.covariance.get(j) + diagWorld.mean.get(j) * diagWorld.mean.get(j))) / p - (mej * mej);
                covariance.set(j, cj);
                if (covariance.checkPositifValue(j) == false) {
                    res = -1;
                    okVar = false;
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
                double value = accumulator.mean.get(j) / accumulator.weight;
                double mej = alpha * value + beta * diagWorld.mean.get(j);

                double cj = (alpha * accumulator.cov.get(j) + beta * (diagWorld.covariance.get(j) + diagWorld.mean.get(j) * diagWorld.mean.get(j)))
                        - (mej * mej);
                covariance.set(j, cj);
                if (covariance.checkPositifValue(j) == false) {
                    res = -1;
                    okVar = false;
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
        covariance = new DoubleVector(dim);
        if (okModel != 0) {
            mean = new DoubleVector(dim);
            okModel = 0;
        }
        okVar = true;
        count = 0;
        weight = 0.0;
        likelihoodConstant = 0.0;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#initAccumulator()
     */
    @Override
    public void initStatisticAccumulator() {
        accumulator = new DiagGaussian.Accumulator();
        accumulator.cov = new DoubleVector(dim);
        accumulator.mean = new DoubleVector(dim);
        accumulator.okAcc = 0;
        accumulator.count = 0;
        accumulator.weight = 0.0;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#merge(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.lib.Gaussian)
     */
    @Override
    public void merge(Gaussian model1, Gaussian model2) throws DiarizationException {

        DiagGaussian m1 = (DiagGaussian) model1;
        DiagGaussian m2 = (DiagGaussian) model2;
        // Check
        if ((!(m1.kind == m2.kind)) && (!(m1.kind == kind))) {
            throw new DiarizationException("Gauss::Acc: merge() error (kind)");
        }
        accumulator.count = m1.accumulator.count + m2.accumulator.count;
        accumulator.weight = m1.accumulator.weight + m2.accumulator.weight;
        for (int j = 0; j < dim; j++) {
            accumulator.mean.set(j, m1.accumulator.mean.get(j) + m2.accumulator.mean.get(j));
            accumulator.cov.set(j, m1.accumulator.cov.get(j) + m2.accumulator.cov.get(j));
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
            accumulator.cov.set(j, j, accumulator.cov.get(j, j) - v * frame[j]);
        }
    }

    /**
     * Reset.
     */
    private void reset() {
        okModel = -1;
        mean = new DoubleVector();
        covariance = new DoubleVector();
        invertCovariance = new DoubleVector();
        okVar = true;
        count = 0;
        weight = 0.0;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#resetAccumulator()
     */
    @Override
    public void resetStatisticAccumulator() {
        accumulator = new DiagGaussian.Accumulator();
        accumulator.cov = new DoubleVector();
        accumulator.mean = new DoubleVector();
        accumulator.okAcc = -1;
        accumulator.count = 0;
        accumulator.weight = 0.0;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Gaussian#setCovariance(int, int, double)
     */
    @Override
    public void setCovariance(int i, int j, double v) throws DiarizationException {
        if (i != j) {
            throw new DiarizationException("GaussDiag::setCov(long i, long j)");
        }
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
        /** cov accumulator, sum of the square features*/
        protected DoubleVector cov;
        /** number of features added in the statistic*/
        private int count;
        /** sum of weights of accumulators, useful of EM*/
        private double weight;
        /** true if accumulators are allocated*/
        private int okAcc;
        /** mean accumulator, sum of the features*/
        private DoubleVector mean;

        /**
         * Instantiates a new accumulator.
         */
        public Accumulator() {
            mean = new DoubleVector();
            cov = new DoubleVector();
        }

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
            result.cov = (DoubleVector) (cov.clone());
            return result;
        }

        public void debug() {
            System.out.println("Debug Acc.count=" + count);
            System.out.println("Debug Acc.weight=" + weight);
            System.out.println("Debug Acc.okAcc=" + okAcc);

            for (int i = 0; i < accumulator.mean.getDimension(); i++) {
                System.out.println("Debug Acc.mean.data[" + i + "=" + mean.get(i));
            }

        }
    }


}
