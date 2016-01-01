/**
 * 
 * <p>
 * CLRHClustering
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
 */

package fr.lium.spkDiarization.libClusteringMethod;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMFactory;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterClustering;
import fr.lium.spkDiarization.parameter.ParameterClustering.ClusteringMethod;

// TODO: Auto-generated Javadoc
/**
 * hierarchical bottom-up clustering class based on GMM distance (CLR, CE)
 * 
 * The clustering is based upon a bottom-up hierarchical clustering. Each segment is associated to a cluster providing the initial set of clusters. The two closest clusters Ci and Cj are merged at each iteration until a stop criterion is met.
 */
public class CLRHClustering extends HClustering {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(CLRHClustering.class.getName());

	/** The length: Number of features in segmentation. */
	protected long length;

	/** The world model. */
	protected GMM ubmGmm;

	/** The mean of the scores. */
	protected double scoresMean;

	/** The std of the scores. */
	protected double scoreStd;

	/** The use top gaussian. */
	protected boolean useTopGaussian;

	/** The use speech detector. */
	protected boolean useSpeechDetector;

	/** The reset accumulator. */
	protected boolean resetAccumulator;

	/**
	 * Instantiates a new cLRH clustering.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the list of parameters
	 * @param ubm the Universal Background Model
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the sphinx clust exception
	 */
	public CLRHClustering(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter, GMM ubm) throws IOException, DiarizationException {
		super(clusterSet, featureSet, parameter);
		key = "HCLR";
		scoresMean = 0.0;
		this.parameter = parameter;
		ubmGmm = ubm.clone();
		computeUbmScores();
		length = this.clusterSet.getLength();
		useTopGaussian = parameter.getParameterTopGaussian().isUseTop();
		useSpeechDetector = parameter.getParameterInputFeature().useSpeechDetection();
		resetAccumulator = true;

		if (parameter.getParameterEM().getMaximumIteration() == 1) {
			logger.info("Don't reset accumulator max it EM=" + parameter.getParameterEM().getMaximumIteration());
			resetAccumulator = false;
		} else {
			if (SpkDiarizationLogger.DEBUG) logger.info("reset accumulator max it EM=" + parameter.getParameterEM().getMaximumIteration());
		}
		/*
		 * if ((parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_BIC_GMM_MAP) && (parameter.getParameterEM().getMaximumIteration() == 1)) { // resetAccumulator = false; resetAccumulator = true; }
		 */

	}

	/**
	 * Reset.
	 */
	public void reset() {
		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			clusterAndGmmList.get(i).getGmm().statistic_reset();
		}
		clusterAndGmmList.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.HClustering#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		CLRHClustering result = (CLRHClustering) super.clone();
		result.length = length; // Number of features in segmentation.
		result.ubmGmm = ubmGmm.clone();
		result.scoresMean = scoresMean;
		result.scoreStd = scoreStd;
		result.useSpeechDetector = useSpeechDetector;
		result.useTopGaussian = useTopGaussian;
		return result;
	}

	/**
	 * Compute a BIC distance between \e i and \e j.
	 * 
	 * @param i the i index
	 * @param j the j index
	 * @return the distance
	 * @throws DiarizationException the sphinx clust exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public double computeDistance(int i, int j) throws DiarizationException, IOException {
		double score = 0.0;
		ClusterAndGMM clusterAndGMMI = clusterAndGmmList.get(i);
		GMM gmmI = clusterAndGMMI.getGmm();
		Cluster clusterI = clusterAndGMMI.getCluster();

		ClusterAndGMM clusterAndGMMJ = clusterAndGmmList.get(j);
		GMM gmmJ = clusterAndGMMJ.getGmm();
		Cluster clusterJ = clusterAndGMMJ.getCluster();
		if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_CLR) {
			score = Distance.CLR(gmmI, gmmJ, ubmGmm, clusterI, clusterJ, clusterAndGMMI.getUBMScore(), clusterAndGMMJ.getUBMScore(), featureSet, useTopGaussian, useSpeechDetector);
		} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_CE) {
			score = Distance.CE(gmmI, gmmJ, clusterI, clusterJ, featureSet, useTopGaussian, useSpeechDetector);
		} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_CE_D) {
			score = Distance.CE(gmmI, gmmJ, clusterI, clusterJ, featureSet, useTopGaussian, useSpeechDetector);
		} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_C_D) {
			score = Distance.CLR(gmmI, gmmJ, ubmGmm, clusterI, clusterJ, clusterAndGMMI.getUBMScore(), clusterAndGMMJ.getUBMScore(), featureSet, useTopGaussian, useSpeechDetector);
		} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_GDGMM) {
			score = Distance.GDMAP(gmmI, gmmJ);
		} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_TScore) {
			score = Distance.TDist(gmmI, gmmJ, ubmGmm, clusterI, clusterJ, featureSet, useTopGaussian, useSpeechDetector, 1);
			// } else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_BIC_GMM_EM) {
			// score = Distance.BIC_EM(gmmI, clusterI, gmmJ, clusterJ, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_BIC_GMM_MAP) {
			score = Distance.BIC_MAP(gmmI, clusterI, gmmJ, clusterJ, ubmGmm, featureSet, parameter);
			if (SpkDiarizationLogger.DEBUG) logger.info("\t\t distance " + i + "-" + j + ": " + score);
		} else {
			logger.warning("distance ("
					+ ParameterClustering.ClustMethodString[parameter.getParameterClustering().getMethod().ordinal()]
					+ ") not implemented, value set to Double.MAX_VALUE");
			score = Double.MAX_VALUE;
		}
		return score;
	}

	/**
	 * Compute the world model scores.
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void computeUbmScores() throws DiarizationException, IOException {
		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			ClusterAndGMM clusterAndGMM = clusterAndGmmList.get(i);
			Cluster cluster = clusterAndGMM.getCluster();
			double score;
			if (parameter.getParameterTopGaussian().getScoreNTop() > 0) {
				score = Distance.getScoreSetTop(ubmGmm, parameter.getParameterTopGaussian().getScoreNTop(), cluster.iterator(), featureSet, useSpeechDetector);
			} else {
				score = Distance.getScore(ubmGmm, cluster.iterator(), featureSet, false, useSpeechDetector);
			}
			clusterAndGMM.setUBMScore(score);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.HClustering#getTDistScoreOfClustering()
	 */
	@Override
	public double getTDistScoreOfClustering() throws DiarizationException, IOException {
		return Distance.TDistStopCriterion(clusterAndGmmList, ubmGmm, featureSet, useTopGaussian, useSpeechDetector, 30);
	}

	/**
	 * Gets the log likelihood of clustering.
	 * 
	 * @return the log likelihood of this clustering
	 */
	public double getLogLikelihoodOfClustering() {
		double res = 0;
		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			res += clusterAndGmmList.get(i).getGmmScore();
		}
		return res / length;
	}

	/**
	 * Gets the log likelihood ratio of clustering.
	 * 
	 * @return the log likelihood ratio of this clustering
	 */
	public double getLogLikelihoodRatioOfClustering() {
		double res = 0;
		double res2 = 0;
		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			res += clusterAndGmmList.get(i).getGmmScore();
			res2 += clusterAndGmmList.get(i).getUBMScore();
		}
		return (res / length) - (res2 / length);
	}

	/**
	 * Gets the world model.
	 * 
	 * @return the world model
	 */
	public GMM getUbm() {
		return ubmGmm;
	}

	/**
	 * Initialize clustering. Train the model of each cluster and compute the distances between the clusters.
	 * 
	 * @param indexOfFirstMergeCandidate the index of first merge candidate
	 * @param indexOfSecondMergeCandidate the index of second merge candidate
	 * @throws DiarizationException the sphinx clust exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void initialize(int indexOfFirstMergeCandidate, int indexOfSecondMergeCandidate) throws DiarizationException, IOException {
		Date date = new Date();
		super.initialize(indexOfFirstMergeCandidate, indexOfSecondMergeCandidate);
		trainGmms();
		if (SpkDiarizationLogger.DEBUG) logger.info("INITIALIZE CLUSTERING - AFTER TRAIN MODEL date: " + date.toString() + " time in ms:"
				+ date.getTime());

		distances.fill(0.0);
		if (SpkDiarizationLogger.DEBUG) logger.fine("\tCompute distance");
		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			for (int j = i + 1; j < clusterAndGmmList.size(); j++) {
				double score = computeDistance(i, j);
				distances.set(i, j, score);
			}
		}
		if (SpkDiarizationLogger.DEBUG) logger.info("INITIALIZE CLUSTERING - AFTER COMPUTE DISTANCE date: " + date.toString() + " time in ms:"
				+ date.getTime());
	}

	/**
	 * Merge two clusters and update model and distances.
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public void mergeCandidates() throws DiarizationException, IOException {
		Date date = new Date();
		if (SpkDiarizationLogger.DEBUG) logger.info("CLUSTERING - BEGIN MERGE date: " + date.toString() + " time in ms:" + date.getTime());
		updateOrderOfCandidates();
		mergeClustersAndAddInformation();
		updateDistanceMatrixSize();
		updateGmms();
		updateClusterAndGMM();
		if (parameter.getParameterClustering().getMethod() != ClusteringMethod.CLUST_H_CE) {
			updateUbmScores();
		}
		updateDistances();
		if (SpkDiarizationLogger.DEBUG) logger.info("CLUSTERING - END MERGE date: " + date.toString() + " time in ms:" + date.getTime());
	}

	/**
	 * Train a cluster.
	 * 
	 * @param i the i
	 * @throws DiarizationException the sphinx clust exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void trainGmm(int i) throws DiarizationException, IOException {
		ClusterAndGMM clusterAndGMM = clusterAndGmmList.get(i);
		Cluster cluster = clusterAndGMM.getCluster();
		GMM init = (ubmGmm.clone());
		// trainer MAP
		if (SpkDiarizationLogger.DEBUG) logger.finer("mTrainMAP cluster=" + cluster.getName());
		clusterAndGMM.setGmm(GMMFactory.getMAP(cluster, featureSet, init, ubmGmm, parameter.getParameterEM(), parameter.getParameterMAP(), parameter.getParameterVarianceControl(), useSpeechDetector, resetAccumulator));
		if (SpkDiarizationLogger.DEBUG) logger.finer("i=" + i + " name=" + clusterAndGMM.getGmm().getName());
		clusterAndGMM.setGmmScore(clusterAndGMM.getGmm().score_getSumLog());
		length += clusterAndGMM.getGmm().score_getCount();
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.HClustering#updateDistances()
	 */
	@Override
	protected void updateDistances() throws DiarizationException, IOException {
		for (int i = 0; i < ci; i++) {
			distances.set(i, ci, computeDistance(i, ci));
		}
		for (int i = ci + 1; i < clusterAndGmmList.size(); i++) {
			distances.set(ci, i, computeDistance(ci, i));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.lib.HClustering#updateModels()
	 */
	@Override
	protected void updateGmms() throws DiarizationException, IOException {
		ClusterAndGMM clusterAndGMM = clusterAndGmmList.get(ci);
		Cluster cluster = clusterAndGMM.getCluster();
		if (resetAccumulator == false) {
			if (SpkDiarizationLogger.DEBUG) logger.fine("use accumulator ci:" + ci + " cj:" + cj);
			GMM gmm = clusterAndGMM.getGmm();
			if (SpkDiarizationLogger.DEBUG) for (ClusterAndGMM g : clusterAndGmmList) {
				g.debug();
			}

			ClusterAndGMM clusterAndGMMJ = clusterAndGmmList.get(cj);
			gmm.statistic_add(clusterAndGMMJ.getGmm());
			gmm.setAdaptedModel(ubmGmm, parameter.getParameterMAP());
		} else {
			if (SpkDiarizationLogger.DEBUG) logger.fine("compute accumulator");
			GMM initilizationGMM = (ubmGmm.clone());
			clusterAndGMM.setGmm(GMMFactory.getMAP(cluster, featureSet, initilizationGMM, ubmGmm, parameter.getParameterEM(), parameter.getParameterMAP(), parameter.getParameterVarianceControl(), useSpeechDetector, resetAccumulator));
			GMM gmm = clusterAndGMM.getGmm();
			clusterAndGMM.setGmmScore(gmm.score_getSumLog());
		}
	}

	/**
	 * Update world model scores.
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void updateUbmScores() throws DiarizationException, IOException {
		ClusterAndGMM clusterAndGMM = clusterAndGmmList.get(ci);
		Cluster cluster = clusterAndGMM.getCluster();
		Iterator<Segment> itSeg = cluster.iterator();
		clusterAndGMM.setUBMScore(Distance.getScore(ubmGmm, itSeg, featureSet, useTopGaussian, useSpeechDetector));
	}
}
