/**
 * 
 * <p>
 * GMM
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
 *          GMM is a components of full or diagonal Gaussians.
 * 
 *          Memory allocation is done by the call of init()
 * 
 */

package fr.lium.spkDiarization.libModel.gaussian;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.parameter.ParameterMAP;

/**
 * The Class GMM.
 */
public class GMM extends Model implements Cloneable, Iterable<Gaussian> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(GMM.class.getName());

	/**
	 * The Class EntryTop.
	 */
	private class EntryTop implements Comparable<EntryTop>, Serializable {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;

		/** The score. */
		public double score;

		/** The index. */
		public int index;

		/*
		 * (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
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

	/** The key written in the gmm file. */
	protected static String key = "mClust1";

	/** The components. */
	protected GaussianList componentList; // vector of components

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
	 * @param gaussianKind the kind
	 */
	public GMM(int nbComponents, int dimension, int gaussianKind) {
		super(dimension, gaussianKind);
		if (nbComponents > 0) {
			componentList = new GaussianList(nbComponents);
			double weight = 1.0 / nbComponents;
			for (int i = 0; i < nbComponents; i++) {
				addNewComponent(weight);
			}
		} else {
			componentList = new GaussianList(0);
		}
		score_reset();
	}

	/**
	 * Statistic_add.
	 * 
	 * @param gmm the gmm
	 * @throws DiarizationException the diarization exception
	 */
	public void statistic_add(GMM gmm) throws DiarizationException {
		if (gmm.dimension != dimension) {
			throw new DiarizationException("GMM::Acc: add() 1 error (dimension)");
		}
		if (gmm.getNbOfComponents() != getNbOfComponents()) {
			throw new DiarizationException("GMM::Acc: add() 1 error (number of components)");
		}
		for (int i = 0; i < getNbOfComponents(); i++) {
			getComponent(i).statistic_add(gmm.getComponent(i));
		}
	}

	/**
	 * Copy a Gaussian.
	 * 
	 * @param gaussian the gaussian
	 * 
	 * @return the gaussian
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public Gaussian addComponent(Gaussian gaussian) throws DiarizationException {
		componentList.add((gaussian.clone()));
		return componentList.get(componentList.size() - 1);
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
		Gaussian gaussian = null;
		if (gaussianKind == Gaussian.FULL) {
			gaussian = new FullGaussian(dimension);
		} else {
			gaussian = new DiagGaussian(dimension);
		}
		componentList.add(gaussian);
		gaussian.setWeight(weight);
		return (componentList.get(componentList.size() - 1));
	}

	/**
	 * Generates a clone of this model, by doing a deep copy (the Gaussians in the clone are copies of, not references to, the Gaussians in the current model).
	 * 
	 * @return the object
	 */
	@Override
	public GMM clone() {
		GMM gmmResult = (GMM) (super.clone());
		gmmResult.componentList = new GaussianList(componentList.size());
		for (Gaussian gaussian : componentList) {
			gmmResult.componentList.add((gaussian.clone()));
		}
		return gmmResult;
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
		logger.fine("debug[gmm] \t model type = GMM<" + gaussianKind + "> dim = " + dimension + " vect.size()="
				+ componentList.size());
		logger.fine(" name=" + name + " gender=" + gender);
		for (Gaussian gaussian : componentList) {
			gaussian.debug(level);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#debugAccumulator()
	 */
	@Override
	public void statistic_debug() {
		for (Gaussian gaussian : componentList) {
			gaussian.statistic_debug();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
	 */
	@Override
	public double score_getAndAccumulate(AudioFeatureSet features, int featureIndex, boolean likelihoodOk) throws DiarizationException {
		score.score = 0.0;
		likelihoodOk = true;
		if (SpkDiarizationLogger.DEBUG) logger.finest("size = " + componentList.size());
		float[] feature = features.getFeatureUnsafe(featureIndex);
		for (Gaussian gaussian : componentList) {
                        double likelihood = gaussian.score_getAndAccumulate(feature);
                        //double likelihood = gaussian.score_getAndAccumulate(features, featureIndex);
			if (likelihood == Double.MIN_VALUE) {
				likelihoodOk = false;
			}
			score.score += likelihood;
		}
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
	public double score_getAndAccumulate(float[] feature) throws DiarizationException {
		score.score = 0.0;
		if (SpkDiarizationLogger.DEBUG) logger.finest("size = " + componentList.size());
		for (Gaussian gaussian : componentList) {
			double v = gaussian.score_getAndAccumulate(feature);
			score.score += v;
		}
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
	public double score_getAndAccumulate(AudioFeatureSet featureSet, int featureIndex) throws DiarizationException {
		return score_getAndAccumulate(featureSet.getFeatureUnsafe(featureIndex));
		/*score.score = 0.0;
		if (SpkDiarizationLogger.DEBUG) logger.finest("size = " + componentList.size());
		for (Gaussian gaussian : componentList) {
			double v = gaussian.score_getAndAccumulate(featureSet, featureIndex);
			score.score += v;
		}
		score.logScore = Math.log(score.score);
		score.sumLogScore += score.logScore;
		score.count++;
		return score.score;*/
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihood(fr.lium.spkDiarization.lib.FeatureSet, int)
	 */
	/*
	 * public double getAndAccumulateLogLikelihood(FeatureSet features, int frameIndex) throws DiarizationException { score.lh = 0.0; score.logLH = -1000; //System.out.println("trace[LOG lh GMM] \t size = " + components.size()); for (int i = 0; i <
	 * components.size(); i++) { double v = components.get(i).getAndAccumulateLogLikelihood(features, frameIndex); score.lh += components.get(i).getLikelihood(); score.logLH = LogAdd(score.logLH, v); } //score.logLH = Math.log(score.lh);
	 * score.sumLogLH += score.logLH; score.countLogLH++; return score.logLH; }
	 */

	/**
	 * Gets the and accumulate likelihood and find top components.
	 * 
	 * @param featureSet the features
	 * @param featureIndex the feature index
	 * @param nbOfTopComponents the number of top components
	 * 
	 * @return the and accumulate likelihood and find top components
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public double score_getAndAccumulateAndFindTopComponents(AudioFeatureSet featureSet, int featureIndex, int nbOfTopComponents) throws DiarizationException {
		topGaussian = new int[nbOfTopComponents];
		EntryTop[] sortGaussian = new EntryTop[componentList.size()];
		float[] feature = featureSet.getFeatureUnsafe(featureIndex);
		
		for (int i = 0; i < componentList.size(); i++) {
			sortGaussian[i] = new EntryTop();
			double tmpLikelihood = componentList.get(i).score_getAndAccumulate(feature);
			sortGaussian[i].score = tmpLikelihood;
			sortGaussian[i].index = i;
		}
		Arrays.sort(sortGaussian);
		score.score = 0.0;
		for (int i = 0; i < nbOfTopComponents; i++) {
			EntryTop likelihood = sortGaussian[i];
			score.score += likelihood.score;
			topGaussian[i] = likelihood.index;
		}
		score.logScore = Math.log(score.score);
		score.sumLogScore += score.logScore;
		score.count++;
		return score.score;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihoodForComponentSubset(fr.lium.spkDiarization.lib.FeatureSet, int, int[])
	 */
	@Override
	public double score_getAndAccumulateForComponentSubset(AudioFeatureSet featureSet, int featureIndex, int[] componentIndices) throws DiarizationException {
		float[] feature = featureSet.getFeatureUnsafe(featureIndex);
		score.score = 0;
		for (int componentIndice : componentIndices) {
			double likelihood = componentList.get(componentIndice).score_getAndAccumulate(feature);
			score.score += likelihood;
		}
		score.logScore = Math.log(score.score);
		score.sumLogScore += score.logScore;
		score.count++;
		return score.score;
	}

	/**
	 * Gets the and accumulate likelihood for component subset.
	 * 
	 * @param featureSet the features
	 * @param featureIndex the frame index
	 * @param componentIndices the component indices
	 * @param nbTop the number of top gaussian
	 * 
	 * @return the and accumulate likelihood for component subset
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public double score_getAndAccumulateForComponentSubset(AudioFeatureSet featureSet, int featureIndex, int[] componentIndices, int nbTop) throws DiarizationException {
		float[] feature = featureSet.getFeatureUnsafe(featureIndex);
		score.score = 0.0;
		for (int i = 0; i < nbTop; i++) {
			double likelihood = componentList.get(componentIndices[i]).score_getAndAccumulate(feature);
			score.score += likelihood;
		}
		score.logScore = Math.log(score.score);
		score.sumLogScore += score.logScore;
		score.count++;
		return score.score;
	}

	/**
	 * Get a Gaussian by index \e idx.
	 * 
	 * @param index the index
	 * 
	 * @return the component
	 */
	public Gaussian getComponent(int index) {
		return componentList.get(index);
	}

	/**
	 * Get the components of Gaussians.
	 * 
	 * @return the components
	 */
	public GaussianList getComponents() {
		return componentList;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#getEM()
	 */
	@Override
	public int setModel() throws DiarizationException {
		int res = 0;
		// initialize();
		for (Gaussian gaussian : componentList) {
			res += gaussian.setModel();
		}
		if (res != 0) {
			res = -1;
		}
		return res;
	}

	/**
	 * Accumulator: compute model from accumulator, MAP mode.
	 * 
	 * @param ubm the universal background model
	 * @param parameterMAP the map control parameter
	 * @return the int
	 * @throws DiarizationException the diarization exception
	 */
	public int setAdaptedModel(GMM ubm, ParameterMAP parameterMAP) throws DiarizationException {
		int res = 0;
		if (parameterMAP.getMethod() == ParameterMAP.MAPMethod.MAP_LIN) {
			for (int i = 0; i < componentList.size(); i++) {
				res += componentList.get(i).setLinearAdaptedModel(ubm.getComponent(i), parameterMAP);
			}
		} else {
			for (int i = 0; i < componentList.size(); i++) {
				res += componentList.get(i).setAdaptedModel(ubm.getComponent(i), parameterMAP);
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
		return componentList.size();
	}

	/**
	 * Initialize the model.
	 */
	@Override
	public void initialize() {
		for (Gaussian gaussian : componentList) {
			gaussian.initialize();
		}
	}

	/**
	 * initialize the accumulator.
	 */
	@Override
	public void statistic_initialize() {
		for (Gaussian gaussian : componentList) {
			gaussian.statistic_initialize();
		}
	}

	/**
	 * initialize the score.
	 */
	@Override
	public void score_initialize() {
		super.score_initialize();
		for (Gaussian gaussian : componentList) {
			gaussian.score_initialize();
		}
	}

	/**
	 * Normalize the weights.
	 */
	public void normalizeWeights() {
		double sum = 0;
		for (int i = 0; i < componentList.size(); i++) {
			sum += componentList.get(i).getWeight();
		}
		if (SpkDiarizationLogger.DEBUG) logger.finest("normWeights count:" + sum);
		for (Gaussian gaussian : componentList) {
			gaussian.setWeight(gaussian.getWeight() / sum);
		}
	}

	/**
	 * Replaces the content of this model with the content of the argument, by doing a deep copy (the Gaussians of the original model are copied, not just referenced).
	 * 
	 * @param gmm the gmm
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	void replaceWithGMM(GMM gmm) throws DiarizationException {
		// componentList.clear();
		gaussianKind = gmm.gaussianKind;
		dimension = gmm.dimension;
		// id = _gmm.id;
		gender = gmm.gender;
		name = gmm.name;
		componentList = new GaussianList(gmm.componentList.size());
		for (Gaussian src : gmm.componentList) {
			componentList.add((src.clone()));
		}
		score = gmm.score;
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#resetAccumulator()
	 */
	@Override
	public void statistic_reset() {
		for (Gaussian gaussian : componentList) {
			gaussian.statistic_reset();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.Model#resetScore()
	 */
	@Override
	public void score_reset() {
		if (componentList != null) {
			super.score_reset();
			for (Gaussian gaussian : componentList) {
				gaussian.score_reset();
			}
		}
	}

	/**
	 * Sort components.
	 */
	public void sortComponents() {
		Collections.sort(this.componentList);
	}

	/**
	 * Normalize the weights.
	 * 
	 * @param c the c
	 */
	public void updateCount(int c) {
		for (Gaussian gaussian : componentList) {
			gaussian.updateCount(c);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Gaussian> iterator() {

		return componentList.iterator();
	}

}
