/**
 * 
 * <p>
 * DiagGaussian
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
 *          diagonal Gaussian
 * 
 */

package fr.lium.spkDiarization.libModel.gaussian;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixDiagonal;
import fr.lium.spkDiarization.libMatrix.MatrixRowVector;
import fr.lium.spkDiarization.parameter.ParameterMAP;
import org.ejml.data.DenseMatrix64F;

/**
 * The Class DiagGaussian.
 */
public class DiagGaussian extends Gaussian implements Cloneable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(DiagGaussian.class.getName());

	/**
	 * The Class Statistic Accumulator.
	 */
	public class Statistic implements Cloneable, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** number of features added in the statistic. */
		private int count;

		/** sum of occupation (likelihood) of accumulators, useful of EM. */
		private double zeroOrder;

		/** true if accumulators are allocated. */
		private int okAccumulator;

		/** accumulator for mean and covariance, sum of the features weighted by the likelihood. */
		private MatrixRowVector firstOrder;

		/** accumulator for covariance, sum of the square features weighted by the likelihood. */
		private MatrixRowVector secondOrder;

		/**
		 * Instantiates a new accumulator.
		 */
		public Statistic() {
			firstOrder = new MatrixRowVector();
			secondOrder = new MatrixRowVector();
		}

		/**
		 * Gets the occupation.
		 * 
		 * @return the weightAccumulator
		 */
		public double getZeroOrder() {
			return zeroOrder;
		}

		/**
		 * Gets the first order.
		 * 
		 * @return the meanAccumulator
		 */
		public MatrixRowVector getFirstOrder() {
			return firstOrder;
		}

		/**
		 * Gets the second order.
		 * 
		 * @return the covarianceAccumulator
		 */
		public MatrixRowVector getSecondOrder() {
			return secondOrder;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Statistic clone() {
			Statistic result = null;
			try {
				result = (Statistic) (super.clone());
			} catch (CloneNotSupportedException e) {
				logger.log(Level.SEVERE, "", e);
				e.printStackTrace();
			}
			if (firstOrder != null) {
				result.firstOrder = firstOrder.copy();
			}
			if (secondOrder != null) {
				result.secondOrder = secondOrder.copy();
			}
			return result;
		}

		/**
		 * Debug.
		 */
		public void debug() {
			logger.finer("Debug Accumulator.count=" + count);
			logger.finer("Debug Accumulator.occupation=" + zeroOrder);
			logger.finer("Debug Accumulator.okAcc=" + okAccumulator);
			/*
			 * if (statistic.firstOrder != null) { for (int i = 0; i < statistic.firstOrder.getSize(); i++) { logger.finer("Debug Acc.mean.data[" + i + "=" + firstOrder.get(i)); } }
			 */

		}
	}

	/** covariance matrix. */
	private MatrixDiagonal covariance;

	/** inverse covariance matrix. */
	private MatrixDiagonal invertCovariance;

	/** the accumulator instance. */
	protected Statistic statistic;

	/**
	 * Gets the statistic.
	 * 
	 * @return the accumulator
	 */
	public Statistic getStatistic() {
		return statistic;
	}

	/**
	 * Instantiates a new diag gaussian.
	 * 
	 * @param dimension the _dim
	 */
	public DiagGaussian(int dimension) {
		super(dimension, Gaussian.DIAG);
		// cov = new SCVector();
		// covInv = new SCVector();
		reset();
		statistic = new DiagGaussian.Statistic();
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#add(fr.lium.spkDiarization.lib.Gaussian, double)
	 */
	@Override
	public void statistic_add(Gaussian gaussian, double weight) throws DiarizationException {
		DiagGaussian diagGaussian = (DiagGaussian) gaussian;
		// check
		if (!(diagGaussian.gaussianKind == gaussianKind)) {
			throw new DiarizationException("Gauss::Acc: add() 1 error (kind)");
		}
		statistic.count += diagGaussian.statistic.count;
		statistic.zeroOrder += (diagGaussian.statistic.zeroOrder * weight);
		for (int j = 0; j < dimension; j++) {
			statistic.firstOrder.add(j, weight * diagGaussian.statistic.firstOrder.unsafe_get(j));
			statistic.secondOrder.add(j, weight * diagGaussian.statistic.secondOrder.unsafe_get(j));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#add(fr.lium.spkDiarization.lib.Gaussian, double)
	 */
	@Override
	public void statistic_remove(Gaussian gaussian, double weight) throws DiarizationException {
		DiagGaussian diagGaussian = (DiagGaussian) gaussian;
		// check
		if (!(diagGaussian.gaussianKind == gaussianKind)) {
			throw new DiarizationException("Gauss::Acc: remove() 1 error (kind)");
		}
		statistic.count -= diagGaussian.statistic.count;
		statistic.zeroOrder -= (diagGaussian.statistic.zeroOrder * weight);
		for (int j = 0; j < dimension; j++) {
			statistic.firstOrder.add(j, -1.0 * weight * diagGaussian.statistic.firstOrder.unsafe_get(j));
			statistic.secondOrder.add(j, -1.0 * weight * diagGaussian.statistic.secondOrder.unsafe_get(j));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(fr.lium.spkDiarization.lib.FeatureSet, int)
	 */
	@Override
	public void statistic_addFeature(AudioFeatureSet featureSet, int featureIndex) throws DiarizationException {
		statistic_addFeature(featureSet, featureIndex, 1.0);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(fr.lium.spkDiarization.lib.FeatureSet, int, double)
	 */
	@Override
	public void statistic_addFeature(AudioFeatureSet featureSet, int featureIndex, double weight) throws DiarizationException {
		statistic.count++;
		statistic.zeroOrder += weight;
		float[] frame = featureSet.getFeatureUnsafe(featureIndex);
		for (int j = 0; j < dimension; j++) {
			double v = weight * frame[j];

			statistic.firstOrder.add(j, v);
			statistic.secondOrder.add(j, v * frame[j]);
		}
		if (SpkDiarizationLogger.DEBUG)  logger.info("Frame "+featureIndex + ": " +frame[0] + " " + frame[1] + " " + frame[2] + " " + frame[3]);

	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(float[])
	 */
	@Override
	public void statistic_addFeature(float[] feature) throws DiarizationException {
		statistic_addFeature(feature, 1.0);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(float[], double)
	 */
	@Override
	public void statistic_addFeature(float[] feature, double weight) throws DiarizationException {
		statistic.count++;
		statistic.zeroOrder += weight;
		for (int j = 0; j < dimension; j++) {
			double v = weight * feature[j];
			statistic.firstOrder.add(j, v);
			statistic.secondOrder.add(j, v * feature[j]);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#clone()
	 */
	@Override
	public DiagGaussian clone() {
		DiagGaussian result = (DiagGaussian) (super.clone());
		if (covariance != null) {
			result.covariance = covariance.copy();
		}
		if (invertCovariance != null) {
			result.invertCovariance = invertCovariance.copy();
		}
		result.statistic = (statistic.clone());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#computeInvertCovariance()
	 */
	@Override
	public boolean computeInvertCovariance() throws DiarizationException {
		logDet = covariance.logDeterminant();
		invertCovariance = new MatrixDiagonal(covariance.getSize());
		boolean result = covariance.invert(invertCovariance);
		if (result == false) {
			okVariance = false;
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#computeLikelihoodConstant()
	 */
	@Override
	public void computeLikelihoodConstant() {
		likelihoodConstant = 1.0 / (Math.pow((2.0 * Math.PI), (0.5 * dimension)) * Math.pow(Math.exp(getLogDeterminant()), 0.5));
	}

	/**
	 * Debug.
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public void debug() throws DiarizationException {
		debug(0);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#debug(int)
	 */
	@Override
	public void debug(int level) throws DiarizationException {
		
		logger.finest("debug[GaussDiag] \t @=" + this);
		logger.finer("model Gauss<" + gaussianKind + "> dim=" + dimension + " count=" + count + " weight=" + weight
				+ " logDet=" + getLogDeterminant() + " cstL=" + likelihoodConstant);
		if (level > 0) {
			String message = "debug[GaussDiag] \t model mean = ";
			if (okModel == 0) {
				for (int i = 0; i < dimension; i++) {
					message += " " + getMean(i);
				}
			} else {
				message += "uninitialized";
			}
			logger.finer(message);
			if (level > 1) {
				message = "debug[GaussDiag] \t model " + ((gaussianKind == FULL) ? "FULL" : "DIAG") + " cov = ";
				if (okModel == 0) {
					for (int i = 0; i < dimension; i++) {
						message += " " + getCovariance(i, i) + "(" + getInvertCovariance(i, i) + ")";
					}
				} else {
					message += "uninitialized";
				}
				logger.finer(message);
				if (level > 2) {
					statistic_debug();
				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#debugAccumulator()
	 */
	@Override
	public void statistic_debug() {
		statistic.debug();
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#getAccumulatorCount()
	 */
	@Override
	public int statistic_getCount() {
		return statistic.count;
	}

	/*
	 * public double getAndAccumulateLogLikelihood(FeatureSet features, int featureIndex) throws DiarizationException { //System.out.println("trace[LOG lh Gauss Diag] \t "); double x, z; score.logLH = 0; float[] frame =
	 * features.getFeature(featureIndex); for (int i = 0; i < dim; i++) { x = frame[i] - mean.get(i); z = covariance.get(i, i); score.logLH += x * x / z + Math.log(Math.PI * 2.0 * z); } score.logLH *= -0.5; score.logLH += Math.log(weight); score.lh =
	 * Math.exp(score.logLH); score.sumLogLH += score.logLH; score.countLogLH++; //System.err.println(" : " + score.logLH); return score.logLH; }
	 */
	public double score_getAndAccumulate(float[] feature) throws DiarizationException {
		double tmp = 0.0;
                for (int j = 0; j < dimension; j++) {
			// System.err.print(" " + frame[j]+"/"+mean.get(j));
			double dmean = (feature[j] - mean.unsafe_get(j));
			tmp -= (0.5 * dmean * dmean * invertCovariance.unsafe_get(j));
		}
		return score.setScore(weight * likelihoodConstant * Math.exp(tmp));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libModel.gaussian.Model#score_getAndAccumulate(fr.lium.spkDiarization.libFeature.AudioFeatureSet, int, boolean)
	 */
	@Override
	public double score_getAndAccumulate(AudioFeatureSet featureSet, int featureIndex, boolean likelihoddOk) throws DiarizationException {
		double tmp = 0.0;
		likelihoddOk = true;
            //float[] feature = featureSet.getFeatureUnsafe(featureIndex);
            //if (SpkDiarizationLogger.DEBUG) logger.finest("feature size: " + featureSet.getFeatureUnsafe(featureIndex).length);
            //if (SpkDiarizationLogger.DEBUG) logger.finest("model size: " + dimension);
                for (int j = 0; j < dimension; j++) {
			// System.err.print(" " + frame[j]+"/"+mean.get(j));
			double dmean = (featureSet.getFeatureUnsafe(featureIndex)[j] - mean.unsafe_get(j));
			tmp -= (0.5 * dmean * dmean * invertCovariance.unsafe_get(j));
		}
		return score.setScore(weight * likelihoodConstant * Math.exp(tmp));
                //score.score = weight * likelihoodConstant * Math.exp(tmp);
		/*if (Double.isInfinite(score.score) || Double.isNaN(score.score) || (score.score == 0)) {
			score.score = Double.MIN_VALUE;
			likelihoddOk = false;
			logger.finest("GaussDiag : getAndAccumulateLikelihood lh=" + score.score + " featureIndex=" + featureIndex
					+ " show" + featureSet.getCurrentShowName() + "score=" + Math.log(score.score));
		}*/
		/*score.logScore = Math.log(score.score);
		score.sumLogScore += score.logScore;
		score.count++;
		//if (SpkDiarizationLogger.DEBUG) logger.finest(" : " + score.logScore);
		return score.score;*/
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
	 */
	@Override
	public double score_getAndAccumulate(AudioFeatureSet featureSet, int frameIndex) throws DiarizationException {
		boolean likelihoodOk = true;
		return score_getAndAccumulate(featureSet, frameIndex, likelihoodOk);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#getCovariance(int, int)
	 */
	@Override
	public double getCovariance(int i, int j) throws DiarizationException {
		if (i != j) {
			throw new DiarizationException("GaussDiag : getCov(long i, long j)");
		}
		return covariance.unsafe_get(i, i);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getEM()
	 */
	@Override
	public int setModel() throws DiarizationException {
		int res = 0;
		initialize();
		for (int j = 0; j < dimension; j++) {
			mean.unsafe_set(j, statistic.firstOrder.unsafe_get(j) / statistic.zeroOrder);
			covariance.unsafe_set(j, j, (statistic.secondOrder.unsafe_get(j) / statistic.zeroOrder)
					- (mean.unsafe_get(j) * mean.unsafe_get(j)));
			if (covariance.checkPositifValue(j) == false) {
				res = -1;
				okVariance = false;
			}
		}
		count = statistic.count;
		weight = statistic.zeroOrder / statistic.count;

		if (computeInvertCovariance() == false) {
			res = -2;
		}
		// logDet = covariance.logDeterminant();
		setGLR();
		computeLikelihoodConstant();

		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#getInvertCovariance(int, int)
	 */
	@Override
	public double getInvertCovariance(int i, int j) throws DiarizationException {
		if (i != j) {
			throw new DiarizationException("GaussDiag::getCovInv(long i, long j)");
		}
		return invertCovariance.unsafe_get(i, i);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#getMAP(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.parameter.ParameterMAP)
	 */
	@Override
	public int setAdaptedModel(Gaussian ubm, ParameterMAP parameterMAP) throws DiarizationException {
		int result = 0;
		double alpha = parameterMAP.getPrior();
		DiagGaussian diagonalUbm = (DiagGaussian) ubm;
		double prior = statistic.zeroOrder + alpha;
		if (parameterMAP.isMeanAdaptatation()) {
			for (int j = 0; j < dimension; j++) {
				mean.unsafe_set(j, (statistic.firstOrder.unsafe_get(j) + (alpha * diagonalUbm.mean.unsafe_get(j)))
						/ prior);
			}
		}
		if (parameterMAP.isCovarianceAdaptation()) {
			for (int j = 0; j < dimension; j++) {
				double mej = (statistic.firstOrder.unsafe_get(j) + (alpha * diagonalUbm.mean.unsafe_get(j))) / prior;
				double cj = ((statistic.secondOrder.unsafe_get(j) + (alpha * (diagonalUbm.covariance.unsafe_get(j, j) + (diagonalUbm.mean.unsafe_get(j) * diagonalUbm.mean.unsafe_get(j))))) / prior)
						- (mej * mej);
				covariance.unsafe_set(j, j, cj);
				if (covariance.checkPositifValue(j) == false) {
					result = -1;
					okVariance = false;
				}
			}
			computeInvertCovariance();
		}
		// logDet = covariance.logDeterminant();
		setGLR();
		computeLikelihoodConstant();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#getMAP_LIN(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.parameter.ParameterMAP)
	 */
	@Override
	public int setLinearAdaptedModel(Gaussian ubm, ParameterMAP parameterMAP) throws DiarizationException {
		int result = 0;
		double alpha = parameterMAP.getPrior();
		DiagGaussian diagonalUbm = (DiagGaussian) ubm;
		double beta = 1.0 - alpha;
		if (parameterMAP.isMeanAdaptatation()) {
			for (int j = 0; j < dimension; j++) {
				double value = statistic.firstOrder.unsafe_get(j) / statistic.zeroOrder;
				mean.unsafe_set(j, ((alpha * value) + (beta * diagonalUbm.mean.unsafe_get(j))));
			}
		}
		if (parameterMAP.isCovarianceAdaptation()) {
			for (int j = 0; j < dimension; j++) {
				double value = statistic.firstOrder.unsafe_get(j) / statistic.zeroOrder;
				double mej = (alpha * value) + (beta * diagonalUbm.mean.unsafe_get(j));

				double cj = ((alpha * statistic.secondOrder.unsafe_get(j)) + (beta * (diagonalUbm.covariance.unsafe_get(j, j) + (diagonalUbm.mean.unsafe_get(j) * diagonalUbm.mean.unsafe_get(j)))))
						- (mej * mej);
				covariance.unsafe_set(j, j, cj);
				if (covariance.checkPositifValue(j) == false) {
					result = -1;
					okVariance = false;
				}
			}
			computeInvertCovariance();
		}
		// logDet = covariance.logDeterminant();
		setGLR();
		computeLikelihoodConstant();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#init()
	 */
	@Override
	public void initialize() {
		covariance = new MatrixDiagonal(dimension);
		if (okModel != 0) {
			mean = new MatrixRowVector(dimension);
			okModel = 0;
		}
		okVariance = true;
		count = 0;
		weight = 0.0;
		likelihoodConstant = 0.0;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#initAccumulator()
	 */
	@Override
	public void statistic_initialize() {
		statistic = new DiagGaussian.Statistic();
		statistic.secondOrder = new MatrixRowVector(dimension);
		statistic.firstOrder = new MatrixRowVector(dimension);
		statistic.okAccumulator = 0;
		statistic.count = 0;
		statistic.zeroOrder = 0.0;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#merge(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.lib.Gaussian)
	 */
	@Override
	public void merge(Gaussian gaussian1, Gaussian gaussian2) throws DiarizationException {

		DiagGaussian diagonalGaussian1 = (DiagGaussian) gaussian1;
		DiagGaussian diagonalGaussian2 = (DiagGaussian) gaussian2;
		// Check
		if ((!(diagonalGaussian1.gaussianKind == diagonalGaussian2.gaussianKind))
				&& (!(diagonalGaussian1.gaussianKind == gaussianKind))) {
			throw new DiarizationException("Gauss::Acc: merge() error (kind)");
		}
		statistic.count = diagonalGaussian1.statistic.count + diagonalGaussian2.statistic.count;
		statistic.zeroOrder = diagonalGaussian1.statistic.zeroOrder + diagonalGaussian2.statistic.zeroOrder;
		for (int j = 0; j < dimension; j++) {
			statistic.firstOrder.unsafe_set(j, diagonalGaussian1.statistic.firstOrder.unsafe_get(j)
					+ diagonalGaussian2.statistic.firstOrder.unsafe_get(j));
			statistic.secondOrder.unsafe_set(j, diagonalGaussian1.statistic.secondOrder.unsafe_get(j)
					+ diagonalGaussian2.statistic.secondOrder.unsafe_get(j));
		}

	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#removeFeatureFromAccumulator(fr.lium.spkDiarization.lib.FeatureSet, int, double)
	 */
	@Override
	public void statistic_removeFeature(AudioFeatureSet featureSet, int featureIndex, double weight) throws DiarizationException {
		statistic.count--;
		statistic.zeroOrder -= weight;
		float[] frame = featureSet.getFeatureUnsafe(featureIndex);
		for (int j = 0; j < dimension; j++) {
			double v = weight * frame[j];
			statistic.firstOrder.unsafe_set(j, statistic.firstOrder.unsafe_get(j) - v);
			statistic.secondOrder.unsafe_set(j, statistic.secondOrder.unsafe_get(j) - (v * frame[j]));
		}
	}

	/**
	 * Reset.
	 */
	private void reset() {
		okModel = -1;
		mean = new MatrixRowVector();
		covariance = new MatrixDiagonal();
		invertCovariance = new MatrixDiagonal();
		okVariance = true;
		count = 0;
		weight = 0.0;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#resetAccumulator()
	 */
	@Override
	public void statistic_reset() {
		statistic = new DiagGaussian.Statistic();
		statistic.secondOrder = new MatrixRowVector();
		statistic.firstOrder = new MatrixRowVector();
		statistic.okAccumulator = -1;
		statistic.count = 0;
		statistic.zeroOrder = 0.0;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#setCovariance(int, int, double)
	 */
	@Override
	public void setCovariance(int i, int j, double v) throws DiarizationException {
		if (i != j) {
			throw new DiarizationException("GaussDiag::setCov(long i, long j)");
		}
		covariance.unsafe_set(i, i, v);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#setGLR()
	 */
	@Override
	public void setGLR() {
		score.GLR = 0.5 * count * getLogDeterminant();
	}

	/**
	 * @return the covariance
	 */
	public MatrixDiagonal getCovariance() {
		return covariance;
	}

	/**
	 * @return the invertCovariance
	 */
	public MatrixDiagonal getInvertCovariance() {
		return invertCovariance;
	}

}
