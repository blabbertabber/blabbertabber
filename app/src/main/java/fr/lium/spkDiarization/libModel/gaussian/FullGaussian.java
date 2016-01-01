/**
 * 
 * <p>
 * FullGaussian
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
 *          Full Gaussian
 * 
 */

package fr.lium.spkDiarization.libModel.gaussian;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixRowVector;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.parameter.ParameterMAP;

// TODO: Auto-generated Javadoc
/**
 * The Class for Gaussian with Full covariance matrix.
 */
public class FullGaussian extends Gaussian implements Cloneable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(FullGaussian.class.getName());

	/**
	 * The Class Accumulator.
	 */
	protected class Statistic implements Cloneable, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
		/** The count, number of features added in the statistic. */
		private int count;
		/** The weight, sum of weights of accumulators, useful of EM. */
		private double occupation;

		/** The mean, mean accumulator, sum of the features. */
		private MatrixRowVector firstOrder;

		/** The covariance, covariance accumulator, sum of the square features. */
		private MatrixSymmetric secondOrder;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Statistic clone() {
			Statistic statisticResult = null;
			try {
				statisticResult = (Statistic) (super.clone());
			} catch (CloneNotSupportedException e) {
				logger.log(Level.SEVERE, "", e);
				e.printStackTrace();
			}
			if (secondOrder != null) {
				statisticResult.firstOrder = firstOrder.copy();
			}
			if (secondOrder != null) {
				statisticResult.secondOrder = secondOrder.copy();
			}
			return statisticResult;
		}
	}

	/** The covariance matrix. */
	protected MatrixSymmetric covariance;

	/** The invert covariance matrix. */
	protected MatrixSymmetric invertCovariance;

	/** The accumulator. */
	private Statistic statistic;

	/** The tmp log l full. */
	private MatrixRowVector tmpLogScoreVector;

	/** The tmp log l full2. */
	private MatrixRowVector tmpLogScoreVector2;

	/**
	 * Instantiates a new full gaussian.
	 * 
	 * @param dimension the _dim
	 */
	public FullGaussian(int dimension) {
		super(dimension, Gaussian.FULL);
		covariance = new MatrixSymmetric();
		invertCovariance = new MatrixSymmetric();
		reset();
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#add(fr.lium.spkDiarization.lib.Gaussian, double)
	 */
	@Override
	public void statistic_add(Gaussian gaussian, double weight) throws DiarizationException {
		FullGaussian gaussianFull = (FullGaussian) gaussian;
		// check
		if (gaussianFull.getGaussianKind() != getGaussianKind()) {
			throw new DiarizationException("Gauss::Acc: add() 1 error (kind)");
		}
		statistic.count += gaussianFull.statistic.count;
		statistic.occupation += (gaussianFull.statistic.occupation * weight);
		for (int j = 0; j < dimension; j++) {
			statistic.firstOrder.add(j, weight * gaussianFull.statistic.firstOrder.get(j));
			for (int k = j; k < dimension; k++) {
				statistic.secondOrder.add(j, k, weight * gaussianFull.statistic.secondOrder.get(j, k));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(fr.lium.spkDiarization.lib.FeatureSet, int)
	 */
	@Override
	public void statistic_addFeature(AudioFeatureSet featureSet, int i) throws DiarizationException {
		statistic_addFeature(featureSet, i, 1.0);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(fr.lium.spkDiarization.lib.FeatureSet, int, double)
	 */
	@Override
	public void statistic_addFeature(AudioFeatureSet featureSet, int index, double weight) throws DiarizationException {
		statistic.count++;
		statistic.occupation += weight;
		float[] frame = featureSet.getFeatureUnsafe(index);
		for (int j = 0; j < dimension; j++) {
			double v = weight * frame[j];
			statistic.firstOrder.set(j, statistic.firstOrder.get(j) + v);
			for (int k = j; k < dimension; k++) {
				statistic.secondOrder.add(j, k, v * frame[k]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#addFeature(float[], double)
	 */
	@Override
	public void statistic_addFeature(float[] feature, double weight) throws DiarizationException {
		statistic.count++;
		statistic.occupation += weight;
		for (int j = 0; j < dimension; j++) {
			double v = weight * feature[j];
			statistic.firstOrder.set(j, statistic.firstOrder.get(j) + v);
			for (int k = j; k < dimension; k++) {
				statistic.secondOrder.add(j, k, v * feature[k]);
			}
		}
	}

	/**
	 * Statistic_add feature.
	 * 
	 * @param feature the feature
	 * @param weight the weight
	 * @throws DiarizationException the diarization exception
	 */
	public void statistic_addFeature(MatrixRowVector feature, double weight) throws DiarizationException {
		statistic.count++;
		statistic.occupation += weight;
		for (int j = 0; j < dimension; j++) {
			double v = weight * feature.get(j);
			statistic.firstOrder.set(j, statistic.firstOrder.get(j) + v);
			for (int k = j; k < dimension; k++) {
				statistic.secondOrder.add(j, k, v * feature.get(k));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#clone()
	 */
	@Override
	public FullGaussian clone() {
		FullGaussian result = (FullGaussian) (super.clone());
		if (covariance != null) {
			result.covariance = covariance.copy();
		}
		if (invertCovariance != null) {
			result.invertCovariance = invertCovariance.copy();
		}
		result.statistic = (statistic.clone());
		if (tmpLogScoreVector != null) {
			result.tmpLogScoreVector = tmpLogScoreVector.copy();
		}
		if (tmpLogScoreVector2 != null) {
			result.tmpLogScoreVector2 = tmpLogScoreVector2.copy();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#computeInvertCovariance()
	 */
	@Override
	public boolean computeInvertCovariance() {
		try {
			logDet = covariance.logDeterminant();
		} catch (DiarizationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		invertCovariance = covariance.invert();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#computeLikelihoodConstant()
	 */
	@Override
	public void computeLikelihoodConstant() {
		likelihoodConstant = 1.0 / (Math.pow((2.0 * Math.PI), (0.5 * dimension)) * Math.pow(Math.exp(getLogDeterminant()), 0.5));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#debug(int)
	 */
	@Override
	public void debug(int level) throws DiarizationException {
		logger.finest("@=" + this);
		logger.finer("model Gauss<" + gaussianKind + "> dim=" + dimension + " count=" + count + " weight=" + weight
				+ " logDet=" + getLogDeterminant() + " cstL=" + likelihoodConstant);
		if (level > 0) {
			String message = "model mean = ";
			if (okModel == 0) {
				for (int i = 0; i < dimension; i++) {
					message += " " + String.format("%f", getMean(i));
				}
			} else {
				message += "uninitalize";
			}
			logger.finer(message);
			if (level > 1) {
				message = "model " + ((gaussianKind == Gaussian.FULL) ? "FULL" : "DIAG") + " cov = ";
				if (okModel == 0) {
					for (int i = 0; i < dimension; i++) {
						for (int j = 0; j < dimension; j++) {
							message += " " + String.format("%f", getCovariance(i, j)) + " ";
							// if (invertCovariance != null) message += "(" + String.format("%f", getInvertCovariance(i, j)) + ")";
						}
					}
				} else {
					message += "uninitalize";
				}
				logger.finer(message);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#debugAccumulator()
	 */
	@Override
	public void statistic_debug() {
		for (int i = 0; i < 24; i++) {
			logger.finer("acc.mean(" + i + ")=" + statistic.firstOrder.get(i));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#getAccumulatorCount()
	 */
	@Override
	public int statistic_getCount() {
		return statistic.count;
	}

	public double score_getAndAccumulate(float[] frame) throws DiarizationException {
		double tmp = 0.0;
		for (int j = 0; j < dimension; j++) {
			tmpLogScoreVector.set(j, frame[j] - mean.get(j));
		}
		for (int j = 0; j < dimension; j++) {
			tmpLogScoreVector2.set(j, 0.0);
			for (int k = 0; k < dimension; k++) {
				tmpLogScoreVector2.add(j, tmpLogScoreVector.get(k) * invertCovariance.get(k, j));
			}
			tmp += (tmpLogScoreVector.get(j) * tmpLogScoreVector2.get(j));
		}
		tmp *= (-0.5);
		score.score = weight * likelihoodConstant * Math.exp(tmp);
		/*if (Double.isInfinite(score.score) || Double.isNaN(score.score) || (score.score == 0)) {
			score.score = Double.MIN_VALUE;
			// throw new DiarizationException("GaussDiag : getAndAccumulateLikelihood lh="+score.lh);
		}*/
		score.logScore = Math.log(score.score);
		score.sumLogScore += score.logScore;
		score.count++;
		return score.score;
	}
	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
	 */
	@Override
	public double score_getAndAccumulate(AudioFeatureSet featureSet, int featureIndex, boolean likelihoodOk) throws DiarizationException {
		likelihoodOk = true;
		double tmp = 0.0;
		float[] frame = featureSet.getFeatureUnsafe(featureIndex);
		for (int j = 0; j < dimension; j++) {
			tmpLogScoreVector.set(j, frame[j] - mean.get(j));
		}
		for (int j = 0; j < dimension; j++) {
			tmpLogScoreVector2.set(j, 0.0);
			for (int k = 0; k < dimension; k++) {
				tmpLogScoreVector2.add(j, tmpLogScoreVector.get(k) * invertCovariance.get(k, j));
			}
			tmp += (tmpLogScoreVector.get(j) * tmpLogScoreVector2.get(j));
		}
		tmp *= (-0.5);
		score.score = weight * likelihoodConstant * Math.exp(tmp);
		if (Double.isInfinite(score.score) || Double.isNaN(score.score) || (score.score == 0)) {
			score.score = Double.MIN_VALUE;
			likelihoodOk = false;
			// throw new DiarizationException("GaussDiag : getAndAccumulateLikelihood lh="+score.lh);
		}
		score.logScore = Math.log(score.score);
		score.sumLogScore += score.logScore;
		score.count++;
		return score.score;
	}

	/*
	 * public double getAndAccumulateLogLikelihood(FeatureSet features, int featureIndex) throws DiarizationException { getAndAccumulateLikelihood(features, featureIndex); return getLogLikelihood(); }
	 */

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
	 */
	@Override
	public double score_getAndAccumulate(AudioFeatureSet featureSet, int featureIndex) throws DiarizationException {
		boolean likelihoodOk = true;
		return score_getAndAccumulate(featureSet, featureIndex, likelihoodOk);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#getCovariance(int, int)
	 */
	@Override
	public double getCovariance(int i, int j) throws DiarizationException {
		return covariance.get(i, j);
	}

	/**
	 * Statistic_set mean and covariance.
	 * 
	 * @return the int
	 */
	public int statistic_setMeanAndCovariance() {
		int result = 0;
		initialize();
		for (int j = 0; j < dimension; j++) {
			mean.set(j, statistic.firstOrder.get(j) / statistic.occupation);
		}
		for (int j = 0; j < dimension; j++) {
			for (int k = j; k < dimension; k++) {
				covariance.set(j, k, (statistic.secondOrder.get(j, k) / statistic.occupation)
						- (getMean(j) * getMean(k)));
				if (j == k) {
					if (covariance.checkPositifValue(j, j) == false) {
						result = -1;
						okVariance = false;
					}
				}
			}
		}
		count = statistic.count;
		weight = statistic.occupation / statistic.count;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getEM()
	 */
	@Override
	public int setModel() {
		int result = statistic_setMeanAndCovariance();
		if (computeInvertCovariance() == false) {
			result = -2;
		}
		// logDet = covariance.logDeterminant();
		setGLR();
		computeLikelihoodConstant();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#getInvertCovariance(int, int)
	 */
	@Override
	public double getInvertCovariance(int i, int j) {
		return invertCovariance.get(i, j);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#getMAP(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.parameter.ParameterMAP)
	 */
	@Override
	public int setAdaptedModel(Gaussian ubm, ParameterMAP parameterMAP) throws DiarizationException {
		int result = 0;
		double alpha = parameterMAP.getPrior();
		double prior = statistic.occupation + alpha;
		if (parameterMAP.isMeanAdaptatation()) {
			for (int j = 0; j < dimension; j++) {
				mean.set(j, statistic.firstOrder.get(j) + ((alpha * ubm.getMean(j)) / prior));
			}
		}
		if (parameterMAP.isCovarianceAdaptation()) {
			for (int j = 0; j < dimension; j++) {
				for (int k = j; k < dimension; k++) {
					double mej = (statistic.firstOrder.get(j) + (alpha * ubm.getMean(j))) / prior;
					double mek = (statistic.firstOrder.get(k) + (alpha * ubm.getMean(k))) / prior;
					double cjk = ((statistic.secondOrder.get(j, k) + (alpha * (ubm.getCovariance(j, k) + (ubm.getMean(j) * ubm.getMean(k))))) / prior)
							- (mej * mek);
					covariance.set(j, k, cjk);
					if (j == k) {
						if (covariance.checkPositifValue(j, j) == false) {
							result = -1;
							okVariance = false;
						}
					}
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
				double value = statistic.firstOrder.get(j) / statistic.occupation;
				mean.set(j, ((alpha * value) + (beta * diagonalUbm.mean.get(j))));
			}
		}
		if (parameterMAP.isCovarianceAdaptation()) {
			for (int j = 0; j < dimension; j++) {
				for (int k = j; k < dimension; k++) {
					double value = statistic.firstOrder.get(j) / statistic.occupation;
					double mej = (alpha * value) + (beta * diagonalUbm.mean.get(j));
					value = statistic.firstOrder.get(k) / statistic.occupation;
					double mek = (alpha * value) + (beta * diagonalUbm.mean.get(k));
					double cjk = ((alpha * statistic.secondOrder.get(j, k)) + (beta * (ubm.getCovariance(j, k) + (ubm.getMean(j) * ubm.getMean(k)))))
							- (mej * mek);
					covariance.set(j, k, cjk);
					if (j == k) {
						if (covariance.checkPositifValue(j, j) == false) {
							result = -1;
							okVariance = false;
						}
					}
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
		covariance = new MatrixSymmetric(dimension);
		if (okModel != 0) {
			mean = new MatrixRowVector(dimension);
			okModel = 0;
		}
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
		statistic = new FullGaussian.Statistic();
		statistic.secondOrder = new MatrixSymmetric(dimension);
		statistic.firstOrder = new MatrixRowVector(dimension);
		statistic.count = 0;
		statistic.occupation = 0.0;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#initScore()
	 */
	@Override
	public void score_initialize() {
		super.score_initialize();
		tmpLogScoreVector = new MatrixRowVector(dimension);
		tmpLogScoreVector2 = new MatrixRowVector(dimension);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#merge(fr.lium.spkDiarization.lib.Gaussian, fr.lium.spkDiarization.lib.Gaussian)
	 */
	@Override
	public void merge(Gaussian gaussian1, Gaussian gaussian2) throws DiarizationException {
		FullGaussian gaussianFull1 = (FullGaussian) gaussian1;
		FullGaussian gaussianFull2 = (FullGaussian) gaussian2;
		// Check
		if (!(gaussianFull1.gaussianKind == gaussianFull2.gaussianKind) && (gaussianFull1.gaussianKind == gaussianKind)) {
			throw new DiarizationException("Gauss::Acc: merge() error (kind)");
		}
		statistic.count = gaussianFull1.statistic.count + gaussianFull2.statistic.count;
		statistic.occupation = gaussianFull1.statistic.occupation + gaussianFull2.statistic.occupation;
		for (int j = 0; j < dimension; j++) {
			statistic.firstOrder.set(j, gaussianFull1.statistic.firstOrder.get(j)
					+ gaussianFull2.statistic.firstOrder.get(j));
			for (int k = j; k < dimension; k++) {
				statistic.secondOrder.set(j, k, gaussianFull1.statistic.secondOrder.get(j, k)
						+ gaussianFull2.statistic.secondOrder.get(j, k));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#removeFeatureFromAccumulator(fr.lium.spkDiarization.lib.FeatureSet, int, double)
	 */
	@Override
	public void statistic_removeFeature(AudioFeatureSet featureSet, int featureIndex, double weight) throws DiarizationException {
		statistic.count--;
		statistic.occupation -= weight;
		float[] frame = featureSet.getFeatureUnsafe(featureIndex);
		for (int j = 0; j < dimension; j++) {
			double v = weight * frame[j];
			statistic.firstOrder.set(j, statistic.firstOrder.get(j) - v);
			for (int k = j; k < dimension; k++) {
				statistic.secondOrder.set(j, k, statistic.secondOrder.get(j, k) - (v * frame[k]));
			}
		}
	}

	/**
	 * Reset.
	 */
	private void reset() {
		okModel = -1;
		mean = new MatrixRowVector();
		covariance = new MatrixSymmetric();
		invertCovariance = new MatrixSymmetric();
		count = 0;
		weight = 0.0;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#resetAccumulator()
	 */
	@Override
	public void statistic_reset() {
		statistic.secondOrder = new MatrixSymmetric();
		statistic.firstOrder = new MatrixRowVector();
		statistic.count = 0;
		statistic.occupation = 0.0;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#resetScore()
	 */
	@Override
	public void score_reset() {
		super.score_reset();
		if (tmpLogScoreVector != null) {
			tmpLogScoreVector = null;
		}
		if (tmpLogScoreVector2 != null) {
			tmpLogScoreVector2 = null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#setCovariance(int, int, double)
	 */
	@Override
	public void setCovariance(int i, int j, double v) {
		covariance.set(i, j, v);
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Gaussian#setGLR()
	 */
	@Override
	public void setGLR() {
// logger.info("logDet: " + getLogDeterminant()+" count:"+count+" logdet cov"+Math.log(covariance.determinant())+" log det incov:"+Math.log(invertCovariance.determinant())+" chol:"+covariance.choleskyDet());
		score.GLR = 0.5 * count * getLogDeterminant();
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libModel.gaussian.Gaussian#statistic_remove(fr.lium.spkDiarization.libModel.gaussian.Gaussian, double)
	 */
	@Override
	public void statistic_remove(Gaussian gaussian, double weight) throws DiarizationException {
		FullGaussian gaussianFull = (FullGaussian) gaussian;
		// check
		if (gaussianFull.getGaussianKind() != getGaussianKind()) {
			throw new DiarizationException("Gauss::Acc: remove() 1 error (kind)");
		}
		statistic.count -= gaussianFull.statistic.count;
		statistic.occupation -= (gaussianFull.statistic.occupation * weight);
		for (int j = 0; j < dimension; j++) {
			statistic.firstOrder.add(j, -1.0 * weight * gaussianFull.statistic.firstOrder.get(j));
			for (int k = j; k < dimension; k++) {
				statistic.secondOrder.add(j, k, -1.0 * weight * gaussianFull.statistic.secondOrder.get(j, k));
			}
		}

	}

	/**
	 * @return the covariance
	 */
	public MatrixSymmetric getCovariance() {
		return covariance;
	}

	/**
	 * @return the invertCovariance
	 */
	public MatrixSymmetric getInvertCovariance() {
		return invertCovariance;
	}

	/**
	 * @return the statistic
	 */
	public Statistic getStatistic() {
		return statistic;
	}

}
