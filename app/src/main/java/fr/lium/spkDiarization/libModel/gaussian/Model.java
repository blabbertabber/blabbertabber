/**
 * 
 * <p>
 * Model
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
 *          Abstract Gaussian model
 * 
 */

package fr.lium.spkDiarization.libModel.gaussian;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;

/**
 * The Class Model.
 */
public abstract class Model implements Cloneable, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Model.class.getName());

	/**
	 * The Class Score.
	 */
	protected class Score implements Cloneable, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** Generalized likelihood ratio. */
		protected double GLR = 0.0;

		/** The likelihood. */
		protected double score = 0.0; // !<

		/** The log #Score. */
		protected double logScore = 0.0; // !< log #lh

		/** sum of #logScore until reset() call. */
		protected double sumLogScore = 0.0; // !< sum of #logLh until reset() call

		/** Tount the number of #logScore added in #sumLogScore. */
		protected int count = 0; // !< c

                double setScore(double d) {
                    score = d;
                    logScore = Math.log(d);
                    sumLogScore += logScore;
                    count++;
                    return d;
                }
                
		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#clone()
		 */
		@Override
		public Object clone() {
			Score result = null;
			try {
				result = (Score) (super.clone());
			} catch (CloneNotSupportedException e) {
				logger.log(Level.SEVERE, "", e);
				e.printStackTrace();
			}
			return result;
		}
	}

	/** Gender of the model. */
	protected String gender;

	/** name of the model. */
	protected String name;

	/** Size of a feature. */
	protected int dimension;

	/** Type of Gaussian. */
	protected int gaussianKind;

	/** Score accumulator. */
	protected Score score;

	/** Score : number of top Gaussians. */
	protected int nbTopGaussian;

	/** Score : vector of top Gaussians. */
	protected int[] topGaussian;

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
		score_reset();
		dimension = featureDimension;
		this.gaussianKind = gaussianKind;
		// id = -1;
		name = "empty";
		gender = Cluster.genderStrings[0];
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Model clone() {
		Model result = null;
		try {
			result = (Model) (super.clone());
		} catch (CloneNotSupportedException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
		if (score != null) {
			result.score = (Score) (score.clone());
		} else {
			result.score = new Score();
		}
		if (topGaussian != null) {
			result.topGaussian = topGaussian.clone();
		} else {
			result.topGaussian = null;
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
	public abstract void statistic_debug();

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
	public abstract double score_getAndAccumulate(AudioFeatureSet features, int i) throws DiarizationException;

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
	public abstract double score_getAndAccumulateForComponentSubset(AudioFeatureSet features, int i, int[] vTop) throws DiarizationException;

	// public abstract double getAndAccumulateLogLikelihood(FeatureSet features, int featureIndex) throws DiarizationException;

	/**
	 * Score_get and accumulate.
	 * 
	 * @param features the features
	 * @param frameIndex the frame index
	 * @param lhOk the lh ok
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 */
	public abstract double score_getAndAccumulate(AudioFeatureSet features, int frameIndex, boolean lhOk) throws DiarizationException;

	/**
	 * Score: get the number of accumulated log-likelihood.
	 * 
	 * @return the count log likelihood
	 */
	public int score_getCount() {
		return score.count;
	}

	/**
	 * Get the dimension of the model, ie the size of a feature vector.
	 * 
	 * @return the dimension of the vector
	 */
	public int getDimension() {
		return dimension;
	}

	/**
	 * Gets the model.
	 * 
	 * @return the model computed
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract int setModel() throws DiarizationException;

	/**
	 * Get the gender of the model.
	 * 
	 * @return the gender
	 */
	public String getGender() {
		return gender;
	}

	/**
	 * Score: get the GLR, ie Generalized Log-likelihood Ratio.
	 * 
	 * @return the parial GLR
	 */
	public double score_getPartialGLR() {
		return score.GLR;
	}

	/**
	 * Get the type of Gaussian: FULL ou DIAG.
	 * 
	 * @return the kind
	 */
	public int getGaussianKind() {
		return gaussianKind;
	}

	/**
	 * Score: get the likelihood of the current features.
	 * 
	 * @return the likelihood
	 */
	public double score_getScore() {
		return score.score;
	}

	/**
	 * Score: get the log-likelihood of the current features.
	 * 
	 * @return the log likelihood
	 */
	public double score_getLogScore() {
		return score.logScore;
	}

	/**
	 * Score: get the mean of accumulated log-likelihood.
	 * 
	 * @return the mean log likelihood
	 */
	public double score_getMeanLog() {
		return score.sumLogScore / score.count;
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
	 * Score: get the sum and count of accumulated log-likelihood.
	 * 
	 * @return the sum and count the log likelihood
	 */
	public double[] score_getSumAndCount() {
		double[] res = new double[2];
		res[0] = score.sumLogScore;
		res[1] = score.count;
		return res;
	}

	/**
	 * Score: get the sum of accumulated log-likelihood.
	 * 
	 * @return the sum of the log likelihood
	 */
	public double score_getSumLog() {
		return score.sumLogScore;
	}

	/**
	 * Score: get the top vector.
	 * 
	 * @return the top gaussians
	 */
	public int[] getTopGaussianVector() {
		return topGaussian;
	}

	/**
	 * Model: allocated the mean.
	 */
	public abstract void initialize();

	/**
	 * Accumulator: initialize the accumulator.
	 */
	public abstract void statistic_initialize();

	/**
	 * Score: initialize the score accumulator.
	 */
	public void score_initialize() {
		score_reset();
	}

	/**
	 * Accumulator: reset the accumulator.
	 */
	public abstract void statistic_reset();

	/**
	 * Score: reset the score accumulator.
	 */
	protected void score_reset() {
		score = new Model.Score();
		score.score = 0.0;
		score.logScore = 0.0;
		score.sumLogScore = 0.0;
		score.count = 0;
		score.GLR = 0.0;

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
	 * Set the type of Gaussian: FULL ou DIAG.
	 * 
	 * @param newGaussianKind the new gaussian kind
	 */
	void setKind(int newGaussianKind) {
		gaussianKind = newGaussianKind;
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
	 * Score_get and accumulate.
	 * 
	 * @param feature the feature
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 */
	public double score_getAndAccumulate(float[] feature) throws DiarizationException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * Log add.
	 * 
	 * @param logA the log a
	 * @param logB the log b
	 * @return the double
	 */
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
			result = logA + (float) (Math.log(1.0 + (Math.exp((logB - logA)))));
		}
		return result;
	}
}
