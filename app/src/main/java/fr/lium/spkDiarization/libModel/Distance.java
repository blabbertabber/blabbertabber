/**
 * 
 * <p>
 * Distance
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
 *          Distance class, ie GLR, BIC, KL2, GD, CE, CLR for Gaussian or GMM
 * 
 */

package fr.lium.spkDiarization.libModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libClusteringMethod.ClusterAndGMM;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixRowVector;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.gaussian.DiagGaussian;
import fr.lium.spkDiarization.libModel.gaussian.FullGaussian;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMFactory;
import fr.lium.spkDiarization.libModel.gaussian.Gaussian;
import fr.lium.spkDiarization.libModel.ivector.IVector;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class Distance. A set of various distances, static methodes.
 */
public class Distance {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Distance.class.getName());

	/**
	 * Compute the Cosine distance between 2 i-vector (standard cosine or WCCN).
	 * 
	 * @param vector1 the vector1
	 * @param vector2 the vector2
	 * @return the cosine distance
	 */
	public static double iVectorCosine(IVector vector1, IVector vector2) {
		double somme = 0;
		double norm1 = 0;
		double norm2 = 0;

		for (int i = 0; i < vector1.getDimension(); i++) {
			somme += vector1.get(i) * vector2.get(i);
			norm1 += vector1.get(i) * vector1.get(i);
			norm2 += vector2.get(i) * vector2.get(i);
		}

		somme = somme / (Math.sqrt(norm1) * Math.sqrt(norm2));

		return somme;
	}

	/**
	 * Compute the euclidean distance between 2 i-vectors.
	 * 
	 * @param vector1 the vector1
	 * @param vector2 the vector2
	 * @return the euclidean distance
	 */
	public static double iVectorEuclidean(IVector vector1, IVector vector2) {
		// debug OK
		double somme = 0.0;
		for (int i = 0; i < vector1.getDimension(); i++) {
			double diff = vector1.get(i) - vector2.get(i);
			somme += diff * diff;
		}
		return Math.sqrt(somme);

	}

	/**
	 * Compute the Mahalanobis distance between 2 i-vetors (euclidean distance normalized by a covariance matrix).
	 * 
	 * @param vector1 the vector1
	 * @param vector2 the vector2
	 * @param w the w
	 * @return the Mahalanobis distance
	 * @throws DiarizationException the diarization exception
	 */
	public static double iVectorMahalanobis(IVector vector1, IVector vector2, MatrixSymmetric w) throws DiarizationException {
		// debug OK

		if (vector1 == vector2) {
			return 0.0;
		}

		double score = 0.0;
		int dimension = vector1.getDimension();
		MatrixRowVector tmpVectorDiff = new MatrixRowVector(dimension);
		MatrixRowVector tmpVectorSumProd = new MatrixRowVector(dimension);

		// String ch = "";
		for (int i = 0; i < dimension; i++) {
			tmpVectorDiff.set(i, vector1.get(i) - vector2.get(i));
			// ch += tmpVectorDiff.get(i)+" ";
		}
		// logger.info("diff: "+ch);

		for (int i = 0; i < dimension; i++) {
			tmpVectorSumProd.set(i, 0.0);
			for (int j = 0; j < dimension; j++) {
				tmpVectorSumProd.add(i, tmpVectorDiff.unsafe_get(j) * w.unsafe_get(j, i));
			}
			score += (tmpVectorDiff.unsafe_get(i) * tmpVectorSumProd.unsafe_get(i));
		}

		if (Double.isInfinite(score) || Double.isNaN(score) || (score <= 0)) {
			logger.warning("iVectorMahalanobis : score too small: d(" + vector1.getName() + ", "
					+ vector2.getName() + ")=" + score+ " --> set to 0.0");
			score = 0.0;
		}
		// logger.info("score:"+score);
		// return Math.sqrt(score);
		return score;
	}

	/**
	 * Gets the threshold.
	 * 
	 * @param cluster the cluster
	 * @param featureSet the feature set
	 * @param silenceThreshold the silence threshold
	 * @param energyIndex the energy index
	 * @return the threshold
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double getThreshold(Cluster cluster, AudioFeatureSet featureSet, double silenceThreshold, int energyIndex) throws DiarizationException, IOException {
		ArrayList<Double> energy = new ArrayList<Double>(cluster.segmentsSize());
		// get energy in a vector
		for (Segment segment : cluster) {
			featureSet.setCurrentShow(segment.getShowName());
			int start = segment.getStart();
			int endSegment = start + segment.getLength();
			int end = Math.min(endSegment, featureSet.getNumberOfFeatures());
			if (endSegment > end) {
				logger.warning("segment end upper to features end (" + "end of Seg=" + endSegment + " nb Features ="
						+ featureSet.getNumberOfFeatures() + ")" + " starting at =" + start + " len= "
						+ segment.getLength() + " in show = " + segment.getShowName());
			}
			for (int i = start; i < end; i++) {
				double value = featureSet.getFeatureUnsafe(i)[energyIndex];
				energy.add(value);
			}
		}
		// sort the energy
		Collections.sort(energy);
		// get thershold
		int indexOfThreshold = 0;
		if (silenceThreshold > 0.0) {
			indexOfThreshold = (int) Math.round(silenceThreshold * energy.size());
		}
		return energy.get(indexOfThreshold);
	}

	/**
	 * Get a BIC score for GMM using ICSI method.
	 * 
	 * @param gmmI the gmm i
	 * @param clusterI the cluster i
	 * @param gmmJ the gmm j
	 * @param clusterJ the cluster j
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double GLR_ICSI(GMM gmmI, Cluster clusterI, GMM gmmJ, Cluster clusterJ, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		Double scoreI = getScore(gmmI, clusterI.iterator(), featureSet);
		Double scoreJ = getScore(gmmJ, clusterJ.iterator(), featureSet);

		GMM gmmIJ = gmmI.clone();
		double weightI = (double) gmmI.getNbOfComponents()
				/ (double) (gmmI.getNbOfComponents() + gmmJ.getNbOfComponents());
		double weightJ = (double) gmmJ.getNbOfComponents()
				/ (double) (gmmI.getNbOfComponents() + gmmJ.getNbOfComponents());

		for (int k = 0; k < gmmJ.getNbOfComponents(); k++) {
			gmmIJ.addComponent(gmmJ.getComponent(k));
		}

		for (int k = 0; k < gmmIJ.getNbOfComponents(); k++) {
			Gaussian gaussian = gmmIJ.getComponent(k);
			double c = weightJ;
			if (k < gmmI.getNbOfComponents()) {
				c = weightI;
			}
			gaussian.setWeight(gaussian.getWeight() * c);
		}

		// gmmIJ.normWeights();
		Cluster clusterIJ = clusterI.clone();
		clusterIJ.addSegments(clusterJ.iterator());

		gmmIJ = GMMFactory.getEM(clusterIJ, featureSet, gmmIJ, gmmIJ.getNbOfComponents(), parameter.getParameterEM(), parameter.getParameterVarianceControl(), parameter.getParameterInputFeature().useSpeechDetection());
		Double scoreIJ = getScore(gmmIJ, clusterIJ.iterator(), featureSet);

		if (SpkDiarizationLogger.DEBUG) logger.fine(gmmI.getName() + "/" + gmmJ.getName() + " " + clusterI.getName() + "/" + clusterJ.getName() + " "
				+ (scoreIJ / clusterIJ.getLength()) + " - (" + (scoreI / clusterI.getLength()) + " + "
				+ (scoreJ / clusterJ.getLength()) + ") " + clusterIJ.getLength() + " DIM" + gmmI.getDimension() + " "
				+ weightI + " " + weightJ + " startI=" + clusterI.firstSegment().getStart() + " startJ="
				+ clusterJ.firstSegment().getStart());
		return scoreIJ - (scoreI + scoreJ);
	}

	/**
	 * Get a GLR score for GMM using EM.
	 * 
	 * @param gmmI the gmm i
	 * @param clusterI the cluster i
	 * @param gmmJ the gmm j
	 * @param clusterJ the cluster j
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double GLR_EM(GMM gmmI, Cluster clusterI, GMM gmmJ, Cluster clusterJ, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		Double scoreI = getScore(gmmI, clusterI.iterator(), featureSet);
		Double scoreJ = getScore(gmmJ, clusterJ.iterator(), featureSet);

		double weightI = (double) gmmI.getNbOfComponents()
				/ (double) (gmmI.getNbOfComponents() + gmmJ.getNbOfComponents());
		double weightJ = (double) gmmJ.getNbOfComponents()
				/ (double) (gmmI.getNbOfComponents() + gmmJ.getNbOfComponents());

		// gmmIJ.normWeights();
		Cluster clusterIJ = clusterI.clone();
		clusterIJ.addSegments(clusterJ.iterator());

		GMM gmmIJInit = GMMFactory.initializeGMM(gmmI.getName() + gmmJ.getName(), clusterIJ, featureSet, gmmI.getGaussianKind(), gmmI.getNbOfComponents(), parameter.getParameterInitializationEM().getModelInitMethod(), parameter.getParameterEM(), parameter.getParameterVarianceControl(), parameter.getParameterInputFeature().useSpeechDetection());
		GMM gmmIJ = GMMFactory.getEM(clusterIJ, featureSet, gmmIJInit, gmmIJInit.getNbOfComponents(), parameter.getParameterEM(), parameter.getParameterVarianceControl(), parameter.getParameterInputFeature().useSpeechDetection());
		Double scoreIJ = getScore(gmmIJ, clusterIJ.iterator(), featureSet);

		if (SpkDiarizationLogger.DEBUG) logger.fine(gmmI.getName() + "/" + gmmJ.getName() + " " + clusterI.getName() + "/" + clusterJ.getName() + " "
				+ (scoreIJ / clusterIJ.getLength()) + " - (" + (scoreI / clusterI.getLength()) + " + "
				+ (scoreJ / clusterJ.getLength()) + ") " + clusterIJ.getLength() + " DIM" + gmmI.getDimension() + " "
				+ weightI + " " + weightJ + " startI=" + clusterI.firstSegment().getStart() + " startJ="
				+ clusterJ.firstSegment().getStart());
		return -scoreIJ + (scoreI + scoreJ);
	}

	/**
	 * Get a BIC score for GMM using MAP method.
	 * 
	 * @param gmmI the gmm i
	 * @param clusterI the cluster i
	 * @param gmmJ the gmm j
	 * @param clusterJ the cluster j
	 * @param ubm the ubm
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double GLR_MAP(GMM gmmI, Cluster clusterI, GMM gmmJ, Cluster clusterJ, GMM ubm, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		boolean usedSpeech = parameter.getParameterInputFeature().useSpeechDetection();
		boolean useTop = parameter.getParameterTopGaussian().isUseTop();

		Double scoreI = getScore(gmmI, clusterI.iterator(), featureSet, useTop, usedSpeech);
		Double scoreJ = getScore(gmmJ, clusterJ.iterator(), featureSet, useTop, usedSpeech);

		// double weightI = (double) gmmI.getNbOfComponents()
		// / (double) (gmmI.getNbOfComponents() + gmmJ.getNbOfComponents());
		// double weightJ = (double) gmmJ.getNbOfComponents()
		// / (double) (gmmI.getNbOfComponents() + gmmJ.getNbOfComponents());

		// gmmIJ.normWeights();
		Cluster clusterIJ = clusterI.clone();
		clusterIJ.addSegments(clusterJ.iterator());

		GMM gmmIJ = null;

		if (parameter.getParameterEM().getMaximumIteration() == 1) {
			GMM initializationGmm = ubm.clone();
			gmmIJ = GMMFactory.getMAP(clusterIJ, featureSet, initializationGmm, ubm, parameter.getParameterEM(), parameter.getParameterMAP(), parameter.getParameterVarianceControl(), usedSpeech);
// logger.fine("MAP : merge acc");
// gmmIJ = (GMM) gmmI.clone();
// gmmIJ.add(gmmJ);
// gmmIJ.setAdaptedModelFromAccumulator(ubm, parameter.parameterMAP);
// gmmIJ.resetStatisticAccumulator();
		} else {
			GMM initializationGmm = ubm.clone();
			gmmIJ = GMMFactory.getMAP(clusterIJ, featureSet, initializationGmm, ubm, parameter.getParameterEM(), parameter.getParameterMAP(), parameter.getParameterVarianceControl(), usedSpeech);
		}
		gmmIJ.score_initialize();
		double scoreIJ = getScore(gmmIJ, clusterIJ.iterator(), featureSet, useTop, usedSpeech);

		if (SpkDiarizationLogger.DEBUG) logger.finer(gmmI.getName() + "/" + gmmJ.getName() + " " + clusterI.getName() + "/" + clusterJ.getName() + " "
				+ scoreIJ + " - (" + scoreI + " + " + scoreJ + ") " + clusterIJ.getLength() + " " + " startI="
				+ clusterI.firstSegment().getStart() + " startJ=" + clusterJ.firstSegment().getStart());
		return -scoreIJ + (scoreI + scoreJ);
	}

	/**
	 * Get a BIC score for Gaussians given a constant and the length.
	 * 
	 * @param gaussianI the gaussian i
	 * @param gaussianJ the gaussian j
	 * @param constant the BIC constant
	 * @param length the number of feature
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public static double BIC(Gaussian gaussianI, Gaussian gaussianJ, double constant, int length) throws DiarizationException {
		double v = Math.log(length);
		return (Distance.GLR(gaussianI, gaussianJ) - (constant * v));
	}

	/**
	 * Bic em.
	 * 
	 * @param gmmI the gmm i
	 * @param clusterI the cluster i
	 * @param gmmJ the gmm j
	 * @param clusterJ the cluster j
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double BIC_EM(GMM gmmI, Cluster clusterI, GMM gmmJ, Cluster clusterJ, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		int len = clusterI.getLength() + clusterJ.getLength();
		double cst = BICGMMConstant(gmmJ.getNbOfComponents(), gmmJ.getGaussianKind(), gmmJ.getDimension(), parameter.getParameterClustering().getThreshold());
		double v = Math.log(len);
		return (Distance.GLR_EM(gmmI, clusterI, gmmJ, clusterJ, featureSet, parameter) - (cst * v));
	}

	/**
	 * Bic map.
	 * 
	 * @param gmmI the gmm i
	 * @param clusterI the cluster i
	 * @param gmmJ the gmm j
	 * @param clusterJ the cluster j
	 * @param ubm the ubm
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double BIC_MAP(GMM gmmI, Cluster clusterI, GMM gmmJ, Cluster clusterJ, GMM ubm, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		int len = clusterI.getLength() + clusterJ.getLength();
		int nbComponent = gmmJ.getNbOfComponents();
		if (parameter.getParameterTopGaussian().isUseTop()) {
			nbComponent = parameter.getParameterTopGaussian().getScoreNTop();
		}

		double cst = BICGMMConstant(nbComponent, gmmJ.getGaussianKind(), gmmJ.getDimension(), parameter.getParameterClustering().getThreshold());
		double v = Math.log(len);
		return (Distance.GLR_MAP(gmmI, clusterI, gmmJ, clusterJ, ubm, featureSet, parameter) - (cst * v));
	}

	/**
	 * Get a BIC constant.
	 * 
	 * @param gaussianKind the kind of model
	 * @param featureSize the dimension of a feature vector
	 * @param alpha the control factor
	 * 
	 * @return the double
	 */
	public static double BICGaussianConstant(int gaussianKind, double featureSize, double alpha) {
		if (gaussianKind == Gaussian.FULL) {
			return 0.5 * alpha * (featureSize + (0.5 * ((featureSize + 1) * featureSize)));
		}
		return 0.5 * alpha * (featureSize + featureSize);
	}

	/**
	 * Get a BIC constant for GMM.
	 * 
	 * @param nbComponent the nb component
	 * @param gaussianKind the kind of model
	 * @param featureSize the dimension of a feature vector
	 * @param alpha the control factor
	 * @return the double
	 */
	public static double BICGMMConstant(int nbComponent, int gaussianKind, double featureSize, double alpha) {
		return nbComponent * BICGaussianConstant(gaussianKind, featureSize, alpha);
	}

	/**
	 * Get a BIC score for Gaussians using a length of the clusters.
	 * 
	 * @param gaussianI the gaussian i
	 * @param gaussianJ the gaussian j
	 * @param constant the BIC constant
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public static double BICSquareRoot(Gaussian gaussianI, Gaussian gaussianJ, double constant) throws DiarizationException {
		double nI = gaussianI.getCount();
		double nJ = gaussianJ.getCount();
		double nIJ = gaussianI.getCount() + gaussianJ.getCount();

		double featureSize = gaussianI.getDimension();
		// 0.5 * alpha * (featureSize + 0.5 * ((featureSize + 1) * featureSize))
		double alpha = constant / (0.5 * (featureSize + (0.5 * ((featureSize + 1) * featureSize))));

		double constantCovariance = 0.5 * alpha * (0.5 * ((featureSize + 1) * featureSize));
		double constantMean = 0.5 * alpha * featureSize;

		// double v = Math.sqrt(nJ) * Math.log(nJ) + Math.sqrt(nI) * Math.log(nI) - Math.sqrt(nIJ) * Math.log(nIJ);
		double vMean = ((Math.sqrt(nI) * Math.log(nI)) + (Math.sqrt(nJ) * Math.log(nJ)))
				- (Math.sqrt(nIJ) * Math.log(nIJ));
		double vCovariance = (Math.log(nI) + Math.log(nJ)) - Math.log(nIJ);
		// return Distance.GLR(gaussianI, gaussianJ) - constant * v;
		return Distance.GLR(gaussianI, gaussianJ) - (constantCovariance * vCovariance) - (constantMean * vMean);
	}

	/**
	 * BIC local.
	 * 
	 * @param gaussianI the gaussian i
	 * @param gaussianJ the gaussian j
	 * @param constant the constant
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 */
	public static double BICLocal(Gaussian gaussianI, Gaussian gaussianJ, double constant) throws DiarizationException {
		double v = Math.log((gaussianI.getCount() + gaussianJ.getCount()));
		return Distance.GLR(gaussianI, gaussianJ) - (constant * v);
	}

	/**
	 * T dist stop criterion.
	 * 
	 * @param GMMList the GMM list
	 * @param ubm the ubm model
	 * @param featureSet the feature set
	 * @param useTop the use top
	 * @param usedSpeech the used speech
	 * @param delay the delay
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double TDistStopCriterion(List<ClusterAndGMM> GMMList, GMM ubm, AudioFeatureSet featureSet, boolean useTop, boolean usedSpeech, int delay) throws DiarizationException, IOException {
		DiagGaussian accScoreInner = new DiagGaussian(1);
		DiagGaussian accScoreOuter = new DiagGaussian(1);

		accScoreInner.statistic_initialize();
		accScoreOuter.statistic_initialize();

		for (ClusterAndGMM clusterAndGMM : GMMList) {
			GMM gmm = clusterAndGMM.getGmm();

			for (ClusterAndGMM clusterAndGMM2 : GMMList) {
				Cluster cluster = clusterAndGMM2.getCluster();
				if (clusterAndGMM2 == clusterAndGMM) {
					getAccumulatorOfLogLikelihoodRatio(accScoreInner, gmm, ubm, cluster.iterator(), featureSet, useTop, usedSpeech, delay);
				} else {
					getAccumulatorOfLogLikelihoodRatio(accScoreOuter, gmm, ubm, cluster.iterator(), featureSet, useTop, usedSpeech, delay);
				}
			}

		}
		accScoreInner.setModel();
		accScoreOuter.setModel();

		return (-1.0 * (Math.abs(accScoreInner.getMean(0) - accScoreOuter.getMean(0))))
				/ (Math.sqrt((accScoreInner.getCovariance(0, 0) / accScoreInner.getCount())
						+ (accScoreOuter.getCovariance(0, 0) / accScoreOuter.getCount())));
	}

	/**
	 * T dist.
	 * 
	 * @param gmmI the gmm i
	 * @param gmmJ the gmm j
	 * @param ubm the ubm model
	 * @param clusterI the cluster i
	 * @param clusterJ the cluster j
	 * @param featureSet the features
	 * @param useTop the use top
	 * @param usedSpeech the used speech
	 * @param delay the delay
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double TDist(GMM gmmI, GMM gmmJ, GMM ubm, Cluster clusterI, Cluster clusterJ, AudioFeatureSet featureSet, boolean useTop, boolean usedSpeech, int delay) throws DiarizationException, IOException {
		DiagGaussian accScoreInner = new DiagGaussian(1);
		DiagGaussian accScoreOuter = new DiagGaussian(1);

		accScoreInner.statistic_initialize();
		accScoreOuter.statistic_initialize();

		getAccumulatorOfLogLikelihoodRatio(accScoreInner, gmmI, ubm, clusterI.iterator(), featureSet, useTop, usedSpeech, delay);
		getAccumulatorOfLogLikelihoodRatio(accScoreInner, gmmJ, ubm, clusterJ.iterator(), featureSet, useTop, usedSpeech, delay);

		getAccumulatorOfLogLikelihoodRatio(accScoreOuter, gmmJ, ubm, clusterI.iterator(), featureSet, useTop, usedSpeech, delay);
		getAccumulatorOfLogLikelihoodRatio(accScoreOuter, gmmI, ubm, clusterJ.iterator(), featureSet, useTop, usedSpeech, delay);

		accScoreInner.setModel();
		accScoreOuter.setModel();

		double count = Math.sqrt(accScoreInner.getCount() + accScoreOuter.getCount());
		return (-1.0 * (Math.abs(accScoreInner.getMean(0) - accScoreOuter.getMean(0)) * count))
				/ (Math.sqrt(accScoreInner.getCovariance(0, 0) + accScoreOuter.getCovariance(0, 0)));
	}

	/**
	 * Cross entropy / Normalized CLR distance.
	 * 
	 * @param gmmI the gmm i
	 * @param gmmJ the gmm j
	 * @param clusterI the cluster i
	 * @param clusterJ the cluster j
	 * @param featureSet the features
	 * @param useTop the use top
	 * @param usedSpeech the used speech
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double CE(GMM gmmI, GMM gmmJ, Cluster clusterI, Cluster clusterJ, AudioFeatureSet featureSet, boolean useTop, boolean usedSpeech) throws DiarizationException, IOException {
		double gmmI_segmentI = Distance.getScore(gmmI, clusterI.iterator(), featureSet, useTop, usedSpeech);
		double gmmJ_segmentI = Distance.getScore(gmmJ, clusterI.iterator(), featureSet, useTop, usedSpeech);
		double segmentICount = gmmI.score_getCount();
		gmmI.score_reset();
		gmmJ.score_reset();
		double gmmI_segmentJ = Distance.getScore(gmmI, clusterJ.iterator(), featureSet, useTop, usedSpeech);
		double gmmJ_segmentJ = Distance.getScore(gmmJ, clusterJ.iterator(), featureSet, useTop, usedSpeech);
		double segmentJCount = gmmJ.score_getCount();
		if (SpkDiarizationLogger.DEBUG) logger.finest("count I/J: " + segmentICount + "/" + segmentJCount + " len I/J: " + clusterI.getLength() + "/"
				+ clusterJ.getLength());
		return ((gmmJ_segmentJ - gmmI_segmentJ) / segmentJCount) + ((gmmI_segmentI - gmmJ_segmentI) / segmentICount);
	}

	/**
	 * Cross Likelihood Ratio distance.
	 * 
	 * @param gmmI the gmm i
	 * @param gmmJ the gmm j
	 * @param ubm the ubm
	 * @param clusterI the cluster i
	 * @param clusterJ the cluster j
	 * @param scoreUbmSI the score ub m_s i
	 * @param scoreUumSJ the score ub m_s j
	 * @param featureSet the features
	 * @param useTop the use top
	 * @param usedSpeech the used speech
	 * @return the double
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double CLR(GMM gmmI, GMM gmmJ, GMM ubm, Cluster clusterI, Cluster clusterJ, double scoreUbmSI, double scoreUumSJ, AudioFeatureSet featureSet, boolean useTop, boolean usedSpeech) throws DiarizationException, IOException {
		double score_gI_sJ = Distance.getScore(gmmI, clusterJ.iterator(), featureSet, useTop, usedSpeech);
		double score_gJ_sI = Distance.getScore(gmmJ, clusterI.iterator(), featureSet, useTop, usedSpeech);
		return ((scoreUumSJ - score_gI_sJ) / gmmI.score_getCount())
				+ ((scoreUbmSI - score_gJ_sI) / gmmJ.score_getCount());
	}

	/**
	 * Get a Gaussian Divergence score for diagonal gaussians (see LIMSI).
	 * 
	 * @param gaussian1 the gaussian 1
	 * @param gaussian2 the gaussian 2
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public static double GD(Gaussian gaussian1, Gaussian gaussian2) throws DiarizationException {
		double s = 0.0;
		int dim = gaussian1.getDimension();
		for (int j = 0; j < dim; j++) {
			double dmean = gaussian1.getMean(j) - gaussian2.getMean(j);
			double v = Math.sqrt(gaussian1.getCovariance(j, j)) * Math.sqrt(gaussian2.getCovariance(j, j));
			if (v < 0) {
				logger.warning("Warning[Distance] \t GD: variance problem");
				v = 1e-8;
			}
			s += (dmean * dmean) / v;
		}
		return s;
	}

	/**
	 * MAP Gaussian divergence. cf. Mathieux Ben thesis: MAP using KL distance
	 * 
	 * @param gmmI the gmm i
	 * @param gmmJ the gmm j
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public static double GDMAP(GMM gmmI, GMM gmmJ) throws DiarizationException {
		double score = 0.0;
		long nbComponents = gmmI.getNbOfComponents();
		for (int i = 0; i < nbComponents; i++) {
			DiagGaussian gaussI = (DiagGaussian) gmmI.getComponent(i);
			DiagGaussian gaussJ = (DiagGaussian) gmmJ.getComponent(i);
			score += gaussI.getWeight() * GD(gaussI, gaussJ);
		}
		return score;
	}

	/**
	 * Get the log-likelihood of a GMM over a list of segments.
	 * 
	 * @param gmm the gmm model
	 * @param iteratorSegmentation the segment iterator
	 * @param featureSet the features
	 * @return the score
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double getScore(GMM gmm, Iterator<Segment> iteratorSegmentation, AudioFeatureSet featureSet) throws DiarizationException, IOException {
		return Distance.getScore(gmm, iteratorSegmentation, featureSet, false, false);
	}

	/**
	 * Gets the score.
	 * 
	 * @param gmm the gmm model
	 * @param iteratorSegmentation the segment iterator
	 * @param featureSet the features
	 * @param useTop the use top
	 * @param usedSpeech the used speech
	 * @return the score
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double getScore(GMM gmm, Iterator<Segment> iteratorSegmentation, AudioFeatureSet featureSet, boolean useTop, boolean usedSpeech) throws DiarizationException, IOException {
		gmm.score_initialize();
		while (iteratorSegmentation.hasNext()) {
			Segment segment = iteratorSegmentation.next();
			int i = 0;
			int start = segment.getStart();
			int end = segment.getLength() + start;
			featureSet.setCurrentShow(segment.getShowName());
			ArrayList<int[]> top = segment.getTopGaussianList();
			for (int j = start; j < end; j++) {
				if (useTop) {
					if (useThisFeature(segment, j, usedSpeech)) {
						gmm.score_getAndAccumulateForComponentSubset(featureSet, j, top.get(i));
					}
					i++;
				} else {
					if (useThisFeature(segment, j, usedSpeech)) {
						gmm.score_getAndAccumulate(featureSet, j);
					}
				}
			}
		}
		return gmm.score_getSumLog();
	}

	/**
	 * Use this feature.
	 * 
	 * @param segment the segment
	 * @param index the index
	 * @param usedSpeech the used speech
	 * @return true, if successful
	 */
	public static boolean useThisFeature(Segment segment, int index, boolean usedSpeech) {
		if ((usedSpeech == true) && (segment.isSpeechFeature(index) == false)) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the accumulator of log likelihood ratio.
	 * 
	 * @param scoreAccumulator the score accumulator
	 * @param gmm the gmm model
	 * @param ubm the ubm model
	 * @param iteratorSegment the it seg
	 * @param featureSet the features
	 * @param useTop the use top
	 * @param usedSpeech the used speech
	 * @param delay the delay
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void getAccumulatorOfLogLikelihoodRatio(DiagGaussian scoreAccumulator, GMM gmm, GMM ubm, Iterator<Segment> iteratorSegment, AudioFeatureSet featureSet, boolean useTop, boolean usedSpeech, int delay) throws DiarizationException, IOException {
		gmm.score_initialize();
		ubm.score_initialize();
		float[] ratio = new float[1];
		while (iteratorSegment.hasNext()) {
			Segment segment = iteratorSegment.next();
			int i = 0;
			int start = segment.getStart();
			int end = segment.getLength() + start;
			featureSet.setCurrentShow(segment.getShowName());
			ArrayList<int[]> topGaussianList = segment.getTopGaussianList();
			boolean reste = false;
			for (int j = start; j < end; j++) {
				reste = true;
				if (useThisFeature(segment, j, usedSpeech)) {
					if (useTop) {
						gmm.score_getAndAccumulateForComponentSubset(featureSet, j, topGaussianList.get(i));
						ubm.score_getAndAccumulateForComponentSubset(featureSet, j, topGaussianList.get(i));
					} else {
						gmm.score_getAndAccumulate(featureSet, j);
						ubm.score_getAndAccumulate(featureSet, j);
					}
				}
				i++;
				if ((i % delay) == 0) {
					ratio[0] = (float) (gmm.score_getMeanLog() - ubm.score_getMeanLog());
					scoreAccumulator.statistic_addFeature(ratio);
					ubm.score_reset();
					gmm.score_reset();
					reste = false;
				}
			}
			if (reste == true) {
				ratio[0] = (float) (gmm.score_getMeanLog() - ubm.score_getMeanLog());
				scoreAccumulator.statistic_addFeature(ratio);
			}
		}
	}

	/**
	 * Gets the score set top.
	 * 
	 * @param gmm the model
	 * @param nbTop the number of top gaussian
	 * @param iteratorSegment the segment iterator
	 * @param featureSet the features
	 * @param usedSpeech the used speech
	 * @return the score set top
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static double getScoreSetTop(GMM gmm, int nbTop, Iterator<Segment> iteratorSegment, AudioFeatureSet featureSet, boolean usedSpeech) throws DiarizationException, IOException {
		gmm.score_initialize();
		while (iteratorSegment.hasNext()) {
			Segment segment = iteratorSegment.next();
			int start = segment.getStart();
			int end = segment.getLength() + start;
			featureSet.setCurrentShow(segment.getShowName());
			ArrayList<int[]> topGaussianList = segment.getTopGaussianList();
			topGaussianList.clear();
			for (int j = start; j < end; j++) {
				if (useThisFeature(segment, j, usedSpeech) == true) {
					gmm.score_getAndAccumulateAndFindTopComponents(featureSet, j, nbTop);
					topGaussianList.add(gmm.getTopGaussianVector());
				} else {
					topGaussianList.add(null);
				}
			}
		}
		return gmm.score_getSumLog();
	}

	/**
	 * Get a GLR score for Gaussians.
	 * 
	 * @param gaussianI the gaussian i
	 * @param gaussianJ the gaussian j
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public static double GLR(Gaussian gaussianI, Gaussian gaussianJ) throws DiarizationException {
		int dimension = gaussianJ.getDimension();
		int gaussianKind = gaussianJ.getGaussianKind();
		Gaussian gaussianIJ = null;
		if (gaussianKind == Gaussian.FULL) {
			gaussianIJ = new FullGaussian(dimension);
		} else {
			gaussianIJ = new DiagGaussian(dimension);
		}
		gaussianIJ.statistic_initialize();
		gaussianIJ.merge(gaussianI, gaussianJ);
		gaussianIJ.setModel();
		if (SpkDiarizationLogger.DEBUG) logger.finest("GLR: " + gaussianIJ.score_getPartialGLR() + " - " + gaussianI.score_getPartialGLR() + " - "
				+ gaussianJ.score_getPartialGLR());
		double res = gaussianIJ.score_getPartialGLR() - gaussianI.score_getPartialGLR()
				- gaussianJ.score_getPartialGLR();
		return res;
	}

	/**
	 * Get a Hotelling statistic score for diagonal Gaussians.
	 * 
	 * @param gaussianI the gaussian i
	 * @param gaussianJ the gaussian j
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public static double H2(Gaussian gaussianI, Gaussian gaussianJ) throws DiarizationException {
		double score = 0.0;
		int dimension = gaussianJ.getDimension();
		int gaussianKind = gaussianJ.getGaussianKind();
		Gaussian gaussianIJ = null;
		if (gaussianKind == Gaussian.FULL) {
			gaussianIJ = new FullGaussian(dimension);
		} else {
			gaussianIJ = new DiagGaussian(dimension);
		}
		gaussianIJ.statistic_initialize();
		gaussianIJ.merge(gaussianI, gaussianJ);
		gaussianIJ.setModel();
		for (int j = 0; j < dimension; j++) {
			double dmean = gaussianI.getMean(j) - gaussianJ.getMean(j);
			double v = gaussianIJ.getCovariance(j, j);
			if (v < 0) {
				logger.warning("H2: variance problem");
				v = 1e-8;
			}
			score += (dmean * dmean) / v;
		}
		return (score * gaussianIJ.statistic_getCount())
				/ (gaussianI.statistic_getCount() + gaussianJ.statistic_getCount());
	}

	/**
	 * ICR.
	 * 
	 * @param gaussianI the gaussian i
	 * @param gaussianJ the gaussian j
	 * @param constant the weighting constant
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public static double ICR(Gaussian gaussianI, Gaussian gaussianJ, double constant) throws DiarizationException {
		double v = 1.0 / ((gaussianI.getCount() + gaussianJ.getCount()));
		// return Distance.GLR(gi, gj) - cst * v; //correction du 30/11/2008
		return constant * v * Distance.GLR(gaussianI, gaussianJ);
	}

	/**
	 * Get a KL2 score for diagonal Gaussians.
	 * 
	 * @param gaussianI the gaussian 1
	 * @param gaussianJ the gaussian 2
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public static double KL2(Gaussian gaussianI, Gaussian gaussianJ) throws DiarizationException {
		double sscore = 0.0;
		int dimension = gaussianI.getDimension();
		for (int j = 0; j < dimension; j++) {
			double meanI = gaussianI.getMean(j);
			double meanJ = gaussianJ.getMean(j);
			double deltaMean = meanI - meanJ;
			double varianceI = gaussianI.getCovariance(j, j);
			double varianceJ = gaussianJ.getCovariance(j, j);
			sscore += 0.25 * ((((varianceI / varianceJ) + (varianceJ / varianceI)) + (deltaMean * deltaMean * ((1.0 / varianceI) + (1.0 / varianceJ)))) - 2.0);
		}
		// s /= (double) dim;
		return sscore;
	}

	/**
	 * Compute the Levenshtein distance thanks to Chas Emerick to Michael Gilleland for this implementation.
	 * <p>
	 * The difference between this impl. and the current is that, rather than creating and retaining a matrix of size s.length()+1 by t.length()+1, we maintain two single-dimensional arrays of length s.length()+1.
	 * <p>
	 * The first, d, is the 'current working' distance array that maintains the newest distance cost counts as we iterate through the characters of String s. Each time we increment the index of String t we are comparing, d is copied to p, the second
	 * int[]. Doing so allows us to retain the previous cost counts as required by the algorithm (taking the minimum of the cost count to the left, up one, and diagonally up and to the left of the current cost count being calculated). (Note that the
	 * arrays aren't really copied anymore, just switched...this is clearly much better than cloning an array or doing a System.arraycopy() each time through the outer loop.).
	 * <p>
	 * Effectively, the difference between the two implementations is this one does not cause an out of memory condition when calculating the LD over two very large strings.
	 * 
	 * @param string1 a string
	 * @param string2 a straing
	 * @return the distance
	 * @see <a href="http://www.merriampark.com/ldjava.htm">Levenshtein Distance Algorithm: Java Implementation, by Chas Emerick</a>
	 */
	public static int levenshteinDistance(String string1, String string2) {
		if ((string1 == null) || (string2 == null)) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		int lengthString1 = string1.length(); // length of s
		int lengthString2 = string2.length(); // length of t

		if (lengthString1 == 0) {
			return lengthString2;
		} else if (lengthString2 == 0) {
			return lengthString1;
		}

		int previousCostVector[] = new int[lengthString1 + 1]; // 'previous' cost array, horizontally
		int costVector[] = new int[lengthString1 + 1]; // cost array, horizontally
		int tmpCostVector[]; // place holder to assist in swapping

		char indexStrint2; // jth character of t
		int cost; // cost

		for (int i = 0; i <= lengthString1; i++) {
			previousCostVector[i] = i;
		}

		for (int j = 1; j <= lengthString2; j++) {
			indexStrint2 = string2.charAt(j - 1);
			costVector[0] = j;
			for (int i = 1; i <= lengthString1; i++) {
				cost = string1.charAt(i - 1) == indexStrint2 ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				costVector[i] = Math.min(Math.min(costVector[i - 1] + 1, previousCostVector[i] + 1), previousCostVector[i - 1]
						+ cost);
			}
			// copy current distance counts to 'previous row' distance counts
			tmpCostVector = previousCostVector;
			previousCostVector = costVector;
			costVector = tmpCostVector;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return previousCostVector[lengthString1];
	}

	/**
	 * Compute belief functions.
	 * 
	 * @param hash1 the hash1
	 * @param hash2 the hash2
	 * @return the hash map
	 */
	public static HashMap<String, Double> computeBeliefFunctions(HashMap<String, Double> hash1, HashMap<String, Double> hash2) {
		HashMap<String, Double> result = new HashMap<String, Double>();

		for (String key1 : hash1.keySet()) {
			Double score1 = hash1.get(key1);
			if (SpkDiarizationLogger.DEBUG) logger.finest(key1 + "-->" + score1);
			// foreach value of the hash1, we will compute the belief score
			for (String key2 : hash2.keySet()) {
				Double score2 = hash2.get(key2);
				// Same key ok to compute
				if (key1.equals(key2)) {
					Double oldScore;
					if (!result.containsKey(key1)) {
						oldScore = new Double(0);
					} else {
						oldScore = result.get(key1);
					}
					if (SpkDiarizationLogger.DEBUG) logger.finest("1 - Key1 (" + key1 + " " + score1 + ") / key2 (" + key2 + " " + score2 + ") -->"
							+ (score1 * score2));
					if (SpkDiarizationLogger.DEBUG) logger.finest("Adding " + (score1 * score2) + " to " + key1 + " : " + oldScore.doubleValue() + "\n");
					result.put(key1, oldScore.doubleValue() + (score1 * score2));
				} else if (key2.equals("_omega_")) {
					Double oldScore;
					if (!result.containsKey(key1)) {
						oldScore = new Double(0);
					} else {
						oldScore = result.get(key1);
					}
					if (SpkDiarizationLogger.DEBUG) logger.finest("2 - Key1 (" + key1 + " " + score1 + ") / key2 (" + key2 + " " + score2 + ") -->"
							+ (score1 * score2));
					if (SpkDiarizationLogger.DEBUG) logger.finest("Adding " + (score1 * score2) + " to " + key1 + " : " + oldScore.doubleValue() + "\n");

					result.put(key1, oldScore.doubleValue() + (score1 * score2));
				} else if (key1.equals("_omega_")) {
					Double oldScore;

					if (!result.containsKey(key2)) {
						oldScore = new Double(0);
					} else {
						oldScore = result.get(key2);
					}
					if (SpkDiarizationLogger.DEBUG) logger.finest("3 - Key1 (" + key1 + " " + score1 + ") / key2 (" + key2 + " " + score2 + ") -->"
							+ (score1 * score2));
					if (SpkDiarizationLogger.DEBUG) logger.finest("Adding " + (score1 * score2) + " to " + key2 + " : " + oldScore.doubleValue() + "\n");

					result.put(key2, oldScore.doubleValue() + (score1 * score2));
				}
			}
		}
		return result;
	}

}
