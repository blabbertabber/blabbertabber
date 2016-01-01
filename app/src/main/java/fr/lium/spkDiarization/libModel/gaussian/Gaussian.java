/**
 * 
 * <p>
 * Gaussian
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

import java.io.IOException;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixRowVector;
import fr.lium.spkDiarization.parameter.ParameterMAP;

/**
 * The Class Gaussian.
 */
public abstract class Gaussian extends Model implements Comparable<Gaussian>, Cloneable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant FULL. */
	public static final int FULL = 0;

	/** The Constant DIAG. */
	public static final int DIAG = 1;

	/** The ok model, true if model allocated (true dimension). */
	protected int okModel; //

	/** The ok var: all variances are > 0. */
	protected boolean okVariance; //

	/** The mean: mean vector <i>mu</i>. */
	protected MatrixRowVector mean; //

	/** The weight. of the Gaussian, set to 1 for mono Gaussian */
	protected double weight;
	/** The count: number of features used to compute the Gaussian. */
	protected int count;

	/** The likelihood constant: preset of constant for likelihood. */
	protected double likelihoodConstant; //

	/** The log det. */
	protected double logDet = Double.NaN;

	/**
	 * The Constructor.
	 * 
	 * @param dimension gaussian dimension
	 * @param gaussianKind gaussian kind (FULL or DIAG)
	 */
	public Gaussian(int dimension, int gaussianKind) {
		super(dimension, gaussianKind);
		mean = new MatrixRowVector();
	}

	/**
	 * Accumulator: add the statistic accumulator \e m1.
	 * 
	 * @param gaussian the gaussian
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public void statistic_add(Gaussian gaussian) throws DiarizationException {
		statistic_add(gaussian, 1.0);
	}

	/**
	 * Accumulator: adding the statistic accumulator \e m1 with weight.
	 * 
	 * @param gaussian the gaussian
	 * @param weight the weight
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract void statistic_add(Gaussian gaussian, double weight) throws DiarizationException;

	/**
	 * Accumulator: add feature of index \e i.
	 * 
	 * @param featureSet the features
	 * @param featureIndex the frame index
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public void statistic_addFeature(AudioFeatureSet featureSet, int featureIndex) throws DiarizationException {
		statistic_addFeature(featureSet, featureIndex, 1.0);
	}

	/**
	 * Adds the feature.
	 * 
	 * @param frame the frame
	 * @param weight the weight
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract void statistic_addFeature(float[] frame, double weight) throws DiarizationException;

	/**
	 * Adds the feature.
	 * 
	 * @param feature the feature
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public void statistic_addFeature(float[] feature) throws DiarizationException {
		statistic_addFeature(feature, weight);
	}

	/**
	 * Accumulator: add the weighted feature of index \e i.
	 * 
	 * @param featureSet the features
	 * @param featureIndex the feature index
	 * @param weight the weight
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract void statistic_addFeature(AudioFeatureSet featureSet, int featureIndex, double weight) throws DiarizationException;

	/**
	 * Adds the features from segments.
	 * 
	 * @param cluster the cluster
	 * @param featureSet the features
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void statistic_addFeatures(Cluster cluster, AudioFeatureSet featureSet) throws DiarizationException, IOException {
		statistic_addFeatures(cluster, featureSet, 1.0);
	}

	/**
	 * Accumulator: add the weighted feature contained in the segment set.
	 * 
	 * @param cluster the cluster
	 * @param featureSet the features
	 * @param weight the weight
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void statistic_addFeatures(Cluster cluster, AudioFeatureSet featureSet, double weight) throws DiarizationException, IOException {
		for (Segment segment : cluster) {
			statistic_addFeatures(segment, featureSet, weight);
		}
	}

	/**
	 * Statistic_add features.
	 * 
	 * @param segment the segment
	 * @param featureSet the feature set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void statistic_addFeatures(Segment segment, AudioFeatureSet featureSet) throws DiarizationException, IOException {
		statistic_addFeatures(segment, featureSet, 1.0);
	}

	/**
	 * Statistic_add features.
	 * 
	 * @param segment the segment
	 * @param featureSet the feature set
	 * @param weight the weight
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void statistic_addFeatures(Segment segment, AudioFeatureSet featureSet, double weight) throws DiarizationException, IOException {
		featureSet.setCurrentShow(segment.getShowName());
		for (int i : segment) {
			statistic_addFeature(featureSet, i, weight);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#clone()
	 */
	@Override
	public Gaussian clone() {
		Gaussian result = (Gaussian) (super.clone());
		result.mean = mean.copy();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Gaussian gaussian) {
		if (weight > gaussian.getWeight()) {
			return -1;
		} else {
			return (weight < gaussian.getWeight()) ? 1 : 0;
		}
	}

	/**
	 * Model: set the invert of the covariance matrix.
	 * 
	 * @return true, if compute invert covariance matrix
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract boolean computeInvertCovariance() throws DiarizationException;

	/**
	 * Compute the preset of constant for likelihood computation.
	 */
	public abstract void computeLikelihoodConstant();

	/**
	 * Accumulator: get the number of features in the accumulator.
	 * 
	 * @return the accumulator count
	 */
	public abstract int statistic_getCount();

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihoodForComponentSubset(fr.lium.spkDiarization.lib.FeatureSet, int, int[])
	 */
	@Override
	public double score_getAndAccumulateForComponentSubset(AudioFeatureSet featureSet, int index, int[] topVector) throws DiarizationException {
		return score_getAndAccumulate(featureSet, index);
	}

	/**
	 * Model: get the number of features in the accumulator.
	 * 
	 * @return the count
	 */
	public int getCount() {
		return count;
	}

	/**
	 * Model: get the \e i, \e j covariance value.
	 * 
	 * @param i the i index
	 * @param j the j index
	 * 
	 * @return the covariance
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract double getCovariance(int i, int j) throws DiarizationException;

	/**
	 * Model: get the \e i, \e j invert value of the covariance.
	 * 
	 * @param i the i index
	 * @param j the j index
	 * 
	 * @return the invert covariance
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract double getInvertCovariance(int i, int j) throws DiarizationException;

	/**
	 * Gets the mAP.
	 * 
	 * @param ubm the universal background model
	 * @param parameterMAP the map control
	 * 
	 * @return the mAP
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract int setAdaptedModel(Gaussian ubm, ParameterMAP parameterMAP) throws DiarizationException;

	/**
	 * Accumulator : get the model from the accumulator, MAP mode.
	 * 
	 * @param ubm the universal background model
	 * @param parameterMAP the map control
	 * 
	 * @return the value of the ??
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract int setLinearAdaptedModel(Gaussian ubm, ParameterMAP parameterMAP) throws DiarizationException;

	/**
	 * Model: get the \e i mean value.
	 * 
	 * @param i the i index
	 * 
	 * @return the mean value at position i
	 */
	public double getMean(int i) {
		return mean.get(i);
	}

	/**
	 * Model: get the weight of features in the accumulator.
	 * 
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Accumulator: Merge the statistics accumulators \e m1 and \e m2 Useful to compute the cluster \f$xy\f$ resulting of the merge of \f$x\f$ and \f$y\f$ clusters Warning The accumulator statistics are initialized before the merge.
	 * 
	 * @param gaussian1 the gaussian 1
	 * @param gaussian2 the gaussian 2
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract void merge(Gaussian gaussian1, Gaussian gaussian2) throws DiarizationException;

	/**
	 * Accumulator: subtract from the accumulator the \e i feature.
	 * 
	 * @param featureSet the features
	 * @param featureIndex index of the feature
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public void statistic_removeFeature(AudioFeatureSet featureSet, int featureIndex) throws DiarizationException {
		statistic_removeFeature(featureSet, featureIndex, 1.0);
	}

	/**
	 * Accumulator: subtract the weighted feature of index \e i.
	 * 
	 * @param featureSet the features
	 * @param featureIndex the frame index
	 * @param weight the weight
	 * @throws DiarizationException the diarization exception
	 */
	public abstract void statistic_removeFeature(AudioFeatureSet featureSet, int featureIndex, double weight) throws DiarizationException;

	/**
	 * Accumulator : set the number of features in the accumulator.
	 * 
	 * @param c the c
	 */
	protected void setCount(int c) {
		count = c;
	}

	/**
	 * Model: set \e v to the \e i, \e j invert value of the covariance matrix.
	 * 
	 * @param i the i
	 * @param j the j
	 * @param v the v
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public abstract void setCovariance(int i, int j, double v) throws DiarizationException;

	/**
	 * Compute the #GLR score.
	 */
	public abstract void setGLR();

	/**
	 * Model: set \e v to the \e i mean value.
	 * 
	 * @param i the i
	 * @param v the v
	 */
	public void setMean(int i, double v) {
		mean.set(i, v);
	}

	/**
	 * Model/Acc : set the weight of features in the accumulator.
	 * 
	 * @param w the w
	 */
	public void setWeight(double w) {
		weight = w;
	}

	/**
	 * update the count.
	 * 
	 * @param c the c
	 */

	public void updateCount(int c) {
		count = c;
		setGLR();
	}

	/**
	 * Statistic_remove.
	 * 
	 * @param gaussian the gaussian
	 * @param weight the weight
	 * @throws DiarizationException the diarization exception
	 */
	public abstract void statistic_remove(Gaussian gaussian, double weight) throws DiarizationException;

	/**
	 * Gets the log determinant.
	 * 
	 * @return the log determinant
	 */
	protected double getLogDeterminant() {
		return logDet;
	}

	/**
	 * @return the mean
	 */
	public MatrixRowVector getMean() {
		return mean;
	}

    void sccore_initialize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
