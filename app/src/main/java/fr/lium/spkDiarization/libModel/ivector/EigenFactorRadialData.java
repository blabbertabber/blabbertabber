package fr.lium.spkDiarization.libModel.ivector;

import java.io.Serializable;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libMatrix.MatrixRowVector;
import fr.lium.spkDiarization.libMatrix.MatrixSquare;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.gaussian.FullGaussian;

/**
 * The Class EigenFactorRadialData.
 */
public class EigenFactorRadialData implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(EigenFactorRadialData.class.getName());

	/** The mean and cov. */
	FullGaussian meanAndCov; // covariance not need but we keep it

	/** The t. */
	MatrixSquare t;

	/**
	 * Instantiates a new eigen factor radial data.
	 */
	public EigenFactorRadialData() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new eigen factor radial data.
	 * 
	 * @param meanAndCov the mean and cov
	 * @param t the t
	 */
	public EigenFactorRadialData(FullGaussian meanAndCov, MatrixSquare t) {
		super();
		this.meanAndCov = meanAndCov;
		this.t = t;
	}

	/**
	 * Gets the mean.
	 * 
	 * @return the mean
	 */
	protected MatrixRowVector getMean() {
		return meanAndCov.getMean();
	}

	/**
	 * Gets the covariance.
	 * 
	 * @return the covariance
	 */
	protected MatrixSymmetric getCovariance() {
		return meanAndCov.getCovariance();
	}

	/**
	 * Sets the mean and cov.
	 * 
	 * @param meanAndCov the new mean and cov
	 */
	protected void setMeanAndCov(FullGaussian meanAndCov) {
		this.meanAndCov = meanAndCov;
	}

	/**
	 * Gets the t.
	 * 
	 * @return the w
	 */
	protected MatrixSquare getT() {
		return t;
	}

	/**
	 * Sets the t.
	 * 
	 * @param t the new t
	 */
	protected void setT(MatrixSquare t) {
		this.t = t;
	}

	/**
	 * Debug.
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	protected void debug() throws DiarizationException {
		meanAndCov.debug(1);
		for (int i = 0; i < t.getSize(); i++) {
			String ch = "t (" + i + "): ";
			for (int j = 0; j < t.getSize(); j++) {
				ch += t.get(i, j) + " ";
			}
			logger.info(ch);
		}
	}
}
