/**
 * 
 * <p>
 * MClust
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
 *          Hierarchical and linear clustering program based on CLR and BIC distances
 * 
 */

package fr.lium.spkDiarization.programs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libClusteringMethod.BICDClustering;
import fr.lium.spkDiarization.libClusteringMethod.BICHClustering;
import fr.lium.spkDiarization.libClusteringMethod.BICLClustering;
import fr.lium.spkDiarization.libClusteringMethod.CLRHClustering;
import fr.lium.spkDiarization.libClusteringMethod.ConnectedGraph;
import fr.lium.spkDiarization.libClusteringMethod.ExhaustiveClustering;
import fr.lium.spkDiarization.libClusteringMethod.HClustering;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixIO;
import fr.lium.spkDiarization.libMatrix.MatrixRectangular;
import fr.lium.spkDiarization.libMatrix.MatrixSquare;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialList;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialNormalizationFactory;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;
import fr.lium.spkDiarization.libModel.ivector.TotalVariability;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterClustering;
import fr.lium.spkDiarization.parameter.ParameterClustering.ClusteringMethod;
import fr.lium.spkDiarization.parameter.ParameterModelSetOutputFile;
import fr.lium.spkDiarization.programs.ivector.TrainIVectorOrTV;

/**
 * The Class MClust.
 */
public class MClust {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MClust.class.getName());

	/**
	 * save a step of the hierarchical clustering algorithm, clustering is duplicated form prevSuffix to suffix.
	 * 
	 * @param clustering the class of the hierarchical clustering
	 * @param previousSuffix save starting
	 * @param suffix save ending
	 * @param parameter root parameter class
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws DiarizationException the diarization exception
	 * @throws TransformerException the transformer exception
	 */
	public static void saveClustering(HClustering clustering, long previousSuffix, long suffix, Parameter parameter) throws IOException, ParserConfigurationException, SAXException, DiarizationException, TransformerException {
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			logger.info("--> save clustering : from " + previousSuffix + " to " + suffix);
			for (long i = previousSuffix; i < suffix; i++) {
				String segOutFilename = parameter.show + "." + String.valueOf(i);
				clustering.getClusterSet().write(segOutFilename, parameter.getParameterSegmentationOutputFile());
			}
		}
	}

	/**
	 * Save clustering.
	 * 
	 * @param clustering the clustering
	 * @param indexMerge the index merge
	 * @param parameter the parameter
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws DiarizationException the diarization exception
	 * @throws TransformerException the transformer exception
	 */
	public static void saveClustering(HClustering clustering, int indexMerge, Parameter parameter) throws IOException, ParserConfigurationException, SAXException, DiarizationException, TransformerException {
		// if (parameter.getParameterDiarization().isSaveAllStep()) {
		String segOutFilename = parameter.show + "-" + String.format("%3d", indexMerge).replace(" ", "_");
		logger.info("--> save clustering : " + segOutFilename);
		clustering.getClusterSet().write(segOutFilename, parameter.getParameterSegmentationOutputFile());
		// }
	}

	/**
	 * Bootum-up Hierarchical clustering based on GMMs, metric could be CE (Cross Entropy) or CLR (Cross Likelihood ratio).
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @param ubm the ubm
	 * @return Clusters
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws TransformerException the transformer exception
	 */
	public static ClusterSet gmmHAC(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter, GMM ubm) throws IOException, DiarizationException, ParserConfigurationException, SAXException, TransformerException {

		CLRHClustering clustering = new CLRHClustering(clusterSet, featureSet, parameter, ubm);
		int nbCluster = clusterSet.clusterGetSize();

		int nbMerge = 0;
		double clustThr = parameter.getParameterClustering().getThreshold();
		int nbMaxMerge = parameter.getParameterClustering().getMaximumOfMerge();
		int nbMinClust = parameter.getParameterClustering().getMinimumOfCluster();
		long suffix = -1000;
		int mult = 100;
		clustering.initialize(0, 0); // Ci = 0; Cj = 0;
		logScore(clustering, parameter);
		// saveClustering(clustering, nbMerge, parameter);
		saveClustering(clustering, suffix, suffix + 1, parameter);
		long previousSuffix = suffix;
		clustering.printDistance();

		double score = clustering.getScoreOfCandidatesForMerging();
		while (continuClustering(score, nbMerge, nbCluster, clusterSet, clustThr, nbMaxMerge, nbMinClust) == true) {
			nbMerge++;
			logger.finer("score = " + score + " ci = " + clustering.getIndexOfFirstCandidate() + "("
					+ clustering.getFirstCandidate().getName() + ")" + " cj = "
					+ clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")");
			suffix = Math.round(score * mult);
			if (suffix > previousSuffix) {
				saveClustering(clustering, previousSuffix, suffix, parameter);
				previousSuffix = suffix;
			}
// saveClustering(clustering, nbMerge, parameter);

			clustering.mergeCandidates();
			score = clustering.getScoreOfCandidatesForMerging();
			nbCluster = clustering.getClusterSet().clusterGetSize();
		}
		if (!parameter.getParameterModelSetOutputFile().getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
			MainTools.writeGMMContainer(parameter, clustering.getGmmList());
		}

		suffix = Math.round(clustThr * mult);
		saveClustering(clustering, previousSuffix, suffix + 1, parameter);
// saveClustering(clustering, nbMerge, parameter);
		return clustering.getClusterSet();
	}

	/**
	 * Bootum-up Hierarchical clustering based on GMMs, metric could be CE (Cross Entropy) or CLR (Cross Likelihood ratio) after each merge a decoding is performed.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @param ubm the ubm
	 * @return Clusters
	 * @throws Exception the exception
	 */
	public static ClusterSet cdclust(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter, GMM ubm) throws Exception {

		CLRHClustering clustering = new CLRHClustering(clusterSet, featureSet, parameter, ubm);
		int nbCluster = clusterSet.clusterGetSize();

		int nbMerge = 0;
		double clustThr = parameter.getParameterClustering().getThreshold();
		int nbMaxMerge = parameter.getParameterClustering().getMaximumOfMerge();
		int nbMinClust = parameter.getParameterClustering().getMinimumOfCluster();
		long suffix = -1000;
		int mult = 100;
		clustering.initialize(0, 0); // Ci = 0; Cj = 0;
		logScore(clustering, parameter);
		saveClustering(clustering, suffix, suffix + 1, parameter);
		long previousSuffix = suffix;

		double score = clustering.getScoreOfCandidatesForMerging();
		while (continuClustering(score, nbMerge, nbCluster, clusterSet, clustThr, nbMaxMerge, nbMinClust) == true) {
			nbMerge++;
			logger.finer("score = " + score + " ci = " + clustering.getIndexOfFirstCandidate() + "("
					+ clustering.getFirstCandidate().getName() + ")" + " cj = "
					+ clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")");
			suffix = Math.round(score * mult);
			if (suffix > previousSuffix) {
				saveClustering(clustering, previousSuffix, suffix, parameter);
				previousSuffix = suffix;
			}
			logger.info("--> Decoding");
			ClusterSet decodeClusterSet = MDecode.make(featureSet, clustering.getClusterSet(), clustering.getGmmList(), parameter);
			logger.info("--> Clustering");
			clustering = new CLRHClustering(decodeClusterSet, featureSet, parameter, ubm);
			clustering.initialize(0, 0); // Ci = 0; Cj = 0;

			clustering.mergeCandidates();

			score = clustering.getScoreOfCandidatesForMerging();
			nbCluster = clustering.getClusterSet().clusterGetSize();
		}
		if (!parameter.getParameterModelSetOutputFile().getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
			MainTools.writeGMMContainer(parameter, clustering.getGmmList());
		}
		suffix = Math.round(clustThr * mult);
		saveClustering(clustering, previousSuffix, suffix, parameter);
		return clustering.getClusterSet();
	}

	/**
	 * BIC Hierarchical clustering.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return Clusters
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterSet gaussianHAC(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		BICHClustering clustering = new BICHClustering(clusterSet.clone(), featureSet, parameter);
		int nbMerge = 0;
		int clustMaxMerge = parameter.getParameterClustering().getMaximumOfMerge();
		int clustMinSpk = parameter.getParameterClustering().getMinimumOfCluster();
		int nbCluster = clusterSet.clusterGetSize();

		double score = 0;
		clustering.initialize(0, 0); // Ci = 0; Cj = 0;
		score = clustering.getScoreOfCandidatesForMerging();
		// Double GD = clustering.getGDOfScoreOfClustering();

		while (continuClustering(score, nbMerge, nbCluster, clusterSet, 0.0, clustMaxMerge, clustMinSpk) == true) {
			logger.fine("merge = " + nbMerge + " score = " + score + " ci = " + clustering.getIndexOfFirstCandidate()
					+ "(" + clustering.getFirstCandidate().getName() + ")" + " cj = "
					+ clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")");
			clustering.mergeCandidates();
			score = clustering.getScoreOfCandidatesForMerging();
			/*
			 * Double newGD = clustering.getGDOfScoreOfClustering(); if (newGD > GD) { break; } GD = newGD;
			 */
			nbMerge++;
			nbCluster = clustering.getClusterSet().clusterGetSize();
		}
		if (!parameter.getParameterModelSetOutputFile().getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
			MainTools.writeGMMContainer(parameter, clustering.getGmmList());
		}
		return clustering.getClusterSet();
	}

	/**
	 * BIC Hierarchical clustering only diagonal merge.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return Clusters
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterSet gaussianHACDiagonal(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		BICDClustering clustering = new BICDClustering(clusterSet.clone(), featureSet, parameter);
		int nbMerge = 0;
		int clustMaxMerge = parameter.getParameterClustering().getMaximumOfMerge();
		int clustMinSpk = parameter.getParameterClustering().getMinimumOfCluster();
		int nbCluster = clusterSet.clusterGetSize();

		double score = 0;
		clustering.initialize(0, 0); // Ci = 0; Cj = 0;
		score = clustering.getScoreOfCandidatesForMerging();
		// Double GD = clustering.getGDOfScoreOfClustering();

		while (continuClustering(score, nbMerge, nbCluster, clusterSet, 0.0, clustMaxMerge, clustMinSpk) == true) {
			logger.fine("merge = " + nbMerge + " score = " + score + " ci = " + clustering.getIndexOfFirstCandidate()
					+ "(" + clustering.getFirstCandidate().getName() + ")" + " cj = "
					+ clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")");
			clustering.mergeCandidates();
			score = clustering.getScoreOfCandidatesForMerging();
			nbMerge++;
			nbCluster = clustering.getClusterSet().clusterGetSize();
		}
		if (!parameter.getParameterModelSetOutputFile().getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
			MainTools.writeGMMContainer(parameter, clustering.getGmmList());
		}
		return clustering.getClusterSet();
	}

	/**
	 * BIC linear left to right clustering.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return Clusters
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterSet gaussianHACRightToLeft(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		BICLClustering clustering = new BICLClustering(clusterSet.clone(), featureSet, parameter);
		double score = 0;
		clustering.initialize(0, 1); // Ci = 0; Cj = 1;
		score = clustering.getScoreOfCandidatesForMerging();
		while (score < Double.MAX_VALUE) {
			String message = "score = " + score + " ci = " + clustering.getIndexOfFirstCandidate() + "("
					+ clustering.getFirstCandidate().getName() + ")" + " cj = "
					+ clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")";
			if (score < 0.0) {
				logger.fine("\tmerge: " + message);
				clustering.mergeCandidates();
			} else {
				logger.fine("\tnext : " + message);
				clustering.incrementIndexOfFirstCandidate();
				clustering.incrementIndexOfSecondCandidate();
			}
			score = clustering.getScoreOfCandidatesForMerging();
		}
		if (!parameter.getParameterModelSetOutputFile().getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
			MainTools.writeGMMContainer(parameter, clustering.getGmmList());
		}
		return clustering.getClusterSet();
	}

	/**
	 * BIC linear right to left clustering.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return Clusters
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterSet gaussianHACLeftToRight(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		logger.warning("\t method need to be validate: problem of increment at each loop?");
		BICLClustering clustering = new BICLClustering(clusterSet.clone(), featureSet, parameter);
		double score = 0;
		int lastIndex = clustering.getIndexOfLastCandidate();
		clustering.initialize(lastIndex - 1, lastIndex);
		score = clustering.getScoreOfCandidatesForMerging();
		while (score < Double.MAX_VALUE) {
			String message = "score = " + score + " ci = " + clustering.getIndexOfFirstCandidate() + "("
					+ clustering.getFirstCandidate().getName() + ")" + " cj = "
					+ clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")";
			if (score < 0.0) {
				clustering.mergeCandidates();
				message = "merge " + message;
			}
			logger.fine("\t" + message);
			clustering.decrementIndexOfFirstCandidate();
			clustering.decrementIndexOfSecondCandidate();
			score = clustering.getScoreOfCandidatesForMerging();
		}
		if (!parameter.getParameterModelSetOutputFile().getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
			MainTools.writeGMMContainer(parameter, clustering.getGmmList());
		}
		return clustering.getClusterSet();
	}

	/**
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param parameter the parameter
	 * @param ubm the ubm
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterSet, Parameter parameter, GMM ubm) throws Exception {
		Date date = new Date();
		logger.info("Clustering: "
				+ ParameterClustering.ClustMethodString[parameter.getParameterClustering().getMethod().ordinal()]);
		ClusterSet clusterSetResult = new ClusterSet();
		logger.info("BEGIN CLUSTERING date: " + date.toString() + " time in ms:" + date.getTime());
		if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_BIC)) {
			clusterSetResult = MClust.gaussianHAC(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_ES_IV)) {
			clusterSetResult = MClust.iVectorExhaustiveSearch(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_ICR)) {
			clusterSetResult = MClust.gaussianHAC(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_GLR)) {
			clusterSetResult = MClust.gaussianHAC(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_GD)) {
			clusterSetResult = MClust.gaussianHAC(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_BIC_SR)) {
			clusterSetResult = MClust.gaussianHAC(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_L_BIC)) {
			clusterSetResult = MClust.gaussianHACRightToLeft(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_L_BIC_SR)) {
			clusterSetResult = MClust.gaussianHACRightToLeft(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_D_BIC)) {
			clusterSetResult = MClust.gaussianHACDiagonal(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_R_BIC)) {
			clusterSetResult = MClust.gaussianHACLeftToRight(clusterSet, featureSet, parameter);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_TScore)) {
			clusterSetResult = MClust.gmmHAC(clusterSet, featureSet, parameter, ubm);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_CLR)) {
			clusterSetResult = MClust.gmmHAC(clusterSet, featureSet, parameter, ubm);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_CE)) {
			clusterSetResult = MClust.gmmHAC(clusterSet, featureSet, parameter, ubm);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_CE_D)) {
			clusterSetResult = MClust.cdclust(clusterSet, featureSet, parameter, ubm);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_C_D)) {
			clusterSetResult = MClust.cdclust(clusterSet, featureSet, parameter, ubm);
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_GDGMM)) {
			clusterSetResult = MClust.gmmHAC(clusterSet, featureSet, parameter, ubm);
// } else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_BIC_GMM_EM)) {
// clusterSetResult = MClust.cclust(clusterSet, featureSet, parameter, ubm);
			logger.info("END CLUSTREING date: " + date.toString() + " time in ms:" + date.getTime());
		} else if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_BIC_GMM_MAP)) {
			clusterSetResult = MClust.gmmHAC(clusterSet, featureSet, parameter, ubm);
		} else {
			logger.severe("not implemented method");
			System.exit(-1);
		}
		return clusterSetResult;
	}

	/**
	 * I vector exhaustive search.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws SAXException the sAX exception
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws ClassNotFoundException the class not found exception
	 */
	public static ClusterSet iVectorExhaustiveSearch(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException, SAXException, ParserConfigurationException, ClassNotFoundException {
		ClusterSet clusterSetResult = new ClusterSet();
		featureSet.setCurrentShow(clusterSet.getFirstCluster().firstSegment().getShowName());
	
		Date date1 = new Date();
		IVectorArrayList iVectorList = TrainIVectorOrTV.make(clusterSet, featureSet, parameter);
		EigenFactorRadialList normalization = MainTools.readEigenFactorRadialNormalization(parameter);
		logger.info("number of iteration in normalisation: " + normalization.size());
		IVectorArrayList normalizedIVectorList = EigenFactorRadialNormalizationFactory.applied(iVectorList, normalization);
		MatrixSymmetric covarianceInvert = MainTools.readMahanalonisCovarianceMatrix(parameter).invert();

		Date date2 = new Date();

		MatrixSymmetric distance = new MatrixSymmetric(iVectorList.size());
		distance.fill(0.0);
		//TreeMap<Double, String> ds = new TreeMap<Double, String>();
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (int i = 0; i < distance.getSize(); i++) {
			//String ch = iVectorList.get(i).getName() + " " + i + " [ ";
			for (int j = i; j < distance.getSize(); j++) {
				double d = Distance.iVectorMahalanobis(normalizedIVectorList.get(i), normalizedIVectorList.get(j), covarianceInvert);
				if (d < min) min = d;
				if (d > max) max = d;
				distance.set(i, j, d);
				//ch += j + ": " + String.format("%8.6f", d) + ", ";
				//ds.put(d, i + "-" + j);
			}
		}
		logger.info("distance min:" + min + " max: "+max);
		/*String min = "";
		for (Double d : ds.keySet()) {
			min += " " + String.format("%f", d) + " (" + ds.get(d) + ") ";
		}
		logger.info("min distance: " + min);*/
		Date date3 = new Date();

		double threshold = parameter.getParameterClustering().getThreshold();
		ConnectedGraph connectedGraph = new ConnectedGraph(distance, threshold);
		int[] subGraph = connectedGraph.getSubGraph();
		int nbSubGraph = connectedGraph.getNbSubGraph();
		ArrayList<ArrayList<Integer>> subGraphList = new ArrayList<ArrayList<Integer>>(nbSubGraph);
		for (int i = 0; i < nbSubGraph; i++) {
			subGraphList.add(new ArrayList<Integer>());
		}
		for (int j = 0; j < iVectorList.size(); j++) {
			subGraphList.get(subGraph[j]).add(j);
		}
		
		logger.info("nb sub graph:" + nbSubGraph);
		for (int i = 0; i < nbSubGraph; i++) {
			logger.info("---------------");
			
			ArrayList<Integer> nodes = subGraphList.get(i);
			//ArrayList<Integer> nodes = new ArrayList<Integer>();
			String nodesString = "";
			for (int j = 0; j < iVectorList.size(); j++) {
				if (subGraph[j] == i) {
				//	nodes.add(j);
					nodesString += " " + j;
				}
			}
			logger.info("sub graph:" + i + " nodes : " + nodesString);

			ExhaustiveClustering exhaustiveClustering = new ExhaustiveClustering(distance, threshold, nodes);
			int[] partition = exhaustiveClustering.backtrack();

			String ch = "partition :";
/*			for (int c : partition) {
				ch += " " + c;
			}
			logger.info(ch);*/
			for (int c : nodes) {
				if (partition[c] == c) {
					ch += " " + c;
					String clusterName = iVectorList.get(c).getName();
					logger.info("add center: " + clusterName);
					Cluster cloneCluster = clusterSet.getCluster(clusterName).clone();
					clusterSetResult.addCluster(cloneCluster);
				} else {
					logger.info("c: "+c+" partition:"+partition[c]);
					String centerName = iVectorList.get(partition[c]).getName();
					String clusterName = iVectorList.get(c).getName();
					logger.info("center: "+centerName+" cluster:"+clusterName);
					Iterator<Segment> it = clusterSet.getCluster(clusterName).iterator();
					clusterSetResult.getCluster(centerName).addSegments(it);
				}
			}
			logger.info(ch);
			/*for (int c : nodes) {
				if (partition[c] != c) {
					String centerName = iVectorList.get(partition[c]).getName();
					String clusterName = iVectorList.get(c).getName();
					clusterSetResult.getCluster(centerName).addSegments(clusterSet.getCluster(clusterName).iterator());
				}
			}*/
		}
		Date date4 = new Date();
		long d = date2.getTime() - date1.getTime();
		logger.info("##--## ivector: "+d);
		 d = date3.getTime() - date2.getTime();
		logger.info("##--##distance: "+d);
		 d = date4.getTime() - date3.getTime();
		logger.info("##--##clustering: "+d);

		return clusterSetResult;
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "MClust");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);

				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);

				// methods
				GMMArrayList GMMList = MainTools.readGMMContainer(parameter);
				GMM ubm = null;
				if (GMMList != null) {
					ubm = GMMList.get(0);
				}
				ClusterSet clustersetResult = make(featureSet, clusterSet, parameter, ubm);

				MainTools.writeClusterSet(parameter, clustersetResult, false);

			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "error exception ", e);
			e.printStackTrace();
		}
	}

	/**
	 * Log score.
	 * 
	 * @param clustering the clustering
	 * @param parameter the parameter
	 */
	public static void logScore(CLRHClustering clustering, Parameter parameter) {
		MatrixSquare distances = clustering.getDistances(); // Matrix of distances.
		GMMArrayList models = clustering.getGmmList(); // List of models
		int size = distances.getSize();
		for (int i = 0; i < size; i++) {
			String spk1 = models.get(i).getName();
			for (int j = i + 1; j < size; j++) {
				String spk2 = models.get(j).getName();
				double score = distances.get(i, j);
				logger.finer("distance( " + spk1 + " , " + spk2 + " ) = " + score);
			}
		}

	}

	/**
	 * Continu clustering.
	 * 
	 * @param score the score
	 * @param nbMerge the nb merge
	 * @param nbCluster the nb cluster
	 * @param clusters the clusters
	 * @param clustThr the clust thr
	 * @param nbMaxMerge the nb max merge
	 * @param nbMinCluster the nb min cluster
	 * @return true, if successful
	 */
	public static boolean continuClustering(double score, int nbMerge, int nbCluster, ClusterSet clusters, double clustThr, int nbMaxMerge, int nbMinCluster) {

		if (score == Double.MAX_VALUE) {
			return false;
		}
		boolean res = ((score < clustThr) && (nbMerge < nbMaxMerge) && (nbCluster > nbMinCluster));
		if (SpkDiarizationLogger.DEBUG) {
                    logger.finer("\tstop result = " + res + " true=" + Boolean.TRUE);
                    logger.finer("\t\t Y|N thr = " + (score < clustThr));
                    logger.finer("\t\t Y|N nb merge = " + (nbMerge < nbMaxMerge));
                    logger.finer("\t\t Y|N nb cluster = " + (nbCluster > nbMinCluster));
                    logger.finer("\t\t score = " + score + " nbMerge=" + nbMerge + " nbCluster=" + nbCluster);
                    logger.finer("\t\t thr=" + clustThr + " nbMaxMerge=" + nbMaxMerge + " nbMinSpk=" + nbMinCluster);
                }
		return res;
	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param program the program
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config(" program name = " + program);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterInputFeature().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationOutputFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterDiarization().log("saveAllStep");
			logger.config(parameter.getSeparator());
			parameter.getParameterClustering().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterModelSetOutputFile().logAll(); // model
			// output
			logger.config(parameter.getSeparator());
			if (parameter.getParameterClustering().getMethod().equals(ClusteringMethod.CLUST_H_CLR)
					|| parameter.getParameterClustering().getMethod().equals(ClusteringMethod.CLUST_H_CE)) {
				parameter.getParameterModelSetInputFile().logAll(); // tInMask
				parameter.getParameterTopGaussian().logTopGaussian(); // sTop
				parameter.getParameterEM().logAll(); // emCtrl
				parameter.getParameterMAP().logAll(); // mapCtrl
				parameter.getParameterVarianceControl().logAll(); // varCtrl
			} else {
				parameter.getParameterModel().logAll(); // kind
			}
			
			if (parameter.getParameterClustering().getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_ES_IV)) {
				logger.config(parameter.getSeparator());
				parameter.getParameterNormlization().logAll();
				logger.config(parameter.getSeparator());
				//parameter.getParameterILP().logAll();
				//logger.config(parameter.getSeparator());
			}
			// logger.config(parameter.getSeparator());
			// parameter.getParameterNormlization().logAll();
		}
	}

}