/**
 * 
 * <p>
 * Diarization
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * 
 * 
 * 
 */

package fr.lium.spkDiarization.system;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.lib.libDiarizationError.ClusterSetResultList;
import fr.lium.spkDiarization.lib.libDiarizationError.DiarizationError;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libClusteringMethod.CLRHClustering;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterBNDiarization;
import fr.lium.spkDiarization.parameter.ParameterSegmentation;
import fr.lium.spkDiarization.programs.MClust;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MScore;
import fr.lium.spkDiarization.programs.MSeg;
import fr.lium.spkDiarization.programs.MSegInit;
import fr.lium.spkDiarization.programs.MTrainEM;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.tools.SAdjSeg;
import fr.lium.spkDiarization.tools.SFilter;
import fr.lium.spkDiarization.tools.SSplitSeg;

/**
 * The Class Diarization.
 */
public class Diarization extends Thread {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Diarization.class.getName());

	/** The h max. */
	double lMin = 2, lMax = 2, hMin = 3, hMax = 3;

	/** The d max. */
	double dMin = 250, dMax = 250;

	/** The c min. */
	double cMin = 1.7;

	/** The c max. */
	double cMax = 1.7;

	/** The mult. */
	double mult = 100;

	/** The diarization list. */
	static ArrayList<Diarization> diarizationList;

	/** The nb treated job. */
	static int nbTreatedJob = 0;

	/** The list of cluster set. */
	static ArrayList<ClusterSet> listOfClusterSet;

	/** The arguments. */
	static String[] arguments;

	/**
	 * Load feature.
	 * 
	 * @param parameter the parameter
	 * @param clusterSet the cluster set
	 * @param descriptor the descriptor
	 * @return the audio feature set
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static AudioFeatureSet loadFeature(Parameter parameter, ClusterSet clusterSet, String descriptor) throws IOException, DiarizationException {
		String oldDescriptor = parameter.getParameterInputFeature().getFeaturesDescriptorAsString();
		parameter.getParameterInputFeature().setFeaturesDescription(descriptor);
		AudioFeatureSet result = MainTools.readFeatureSet(parameter, clusterSet);
		parameter.getParameterInputFeature().setFeaturesDescription(oldDescriptor);
		return result;
	}

	/**
	 * Load feature.
	 * 
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @param clusterSet the cluster set
	 * @param descriptor the descriptor
	 * @return the audio feature set
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	private AudioFeatureSet loadFeature(AudioFeatureSet featureSet, Parameter parameter, ClusterSet clusterSet, String descriptor) throws IOException, DiarizationException {
		String oldDescriptor = parameter.getParameterInputFeature().getFeaturesDescriptorAsString();
		parameter.getParameterInputFeature().setFeaturesDescription(descriptor);
		AudioFeatureSet result = MainTools.readFeatureSet(parameter, clusterSet, featureSet);
		parameter.getParameterInputFeature().setFeaturesDescription(oldDescriptor);
		return result;
	}

	/**
	 * Initialize.
	 * 
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public ClusterSet initialize(Parameter parameter) throws DiarizationException, Exception {
		// ** get the first diarization
		logger.info("Initialize segmentation");
		ClusterSet clusterSet = null;
		if (parameter.getParameterDiarization().isLoadInputSegmentation()) {
			clusterSet = MainTools.readClusterSet(parameter);
			// seg IRIT
			// return clusterSet;
			// seg IRIT
		} else {
			clusterSet = new ClusterSet();
			Cluster cluster = clusterSet.createANewCluster("init");
			Segment segment = new Segment(parameter.show, 0, 1, cluster, parameter.getParameterSegmentationInputFile().getRate());
			cluster.addSegment(segment);
		}
		return clusterSet;
	}

	/**
	 * Sanity check.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws TransformerException the transformer exception
	 */
	public ClusterSet sanityCheck(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException, ParserConfigurationException, SAXException, TransformerException {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();

		ClusterSet clustersSegInit = new ClusterSet();
		MSegInit.make(featureSet, clusterSet, clustersSegInit, parameter);
		clustersSegInit.collapse();
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".i.seg");
			MainTools.writeClusterSet(parameter, clustersSegInit, false);
		}

		parameter.getParameterSegmentationOutputFile().setMask(mask);

		return clustersSegInit;
	}

	/**
	 * Segmentation.
	 * 
	 * @param method the method
	 * @param kind the kind
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet segmentation(String method, String kind, ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();

		String oldMethod = parameter.getParameterSegmentation().getMethodAsString();
		int oldNumberOfComponent = parameter.getParameterModel().getNumberOfComponents();
		String oldModelKind = parameter.getParameterModel().getModelKindAsString();

		parameter.getParameterSegmentation().setMethod(method);
		parameter.getParameterModel().setNumberOfComponents(1);
		parameter.getParameterModel().setModelKind(kind);
		ClusterSet clustersSeg = new ClusterSet();
		MSeg.make(featureSet, clusterSet, clustersSeg, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".s.seg");
			MainTools.writeClusterSet(parameter, clustersSeg, false);
		}
		if (SpkDiarizationLogger.DEBUG) clustersSeg.debug(10);
		parameter.getParameterSegmentation().setMethod(oldMethod);
		parameter.getParameterModel().setNumberOfComponents(oldNumberOfComponent);
		parameter.getParameterModel().setModelKind(oldModelKind);
		parameter.getParameterSegmentationOutputFile().setMask(mask);

		return clustersSeg;
	}

	/**
	 * Clustering linear.
	 * 
	 * @param threshold the threshold
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet clusteringLinear(double threshold, ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		String oldMethod = parameter.getParameterClustering().getMethodAsString();
		double oldThreshold = parameter.getParameterClustering().getThreshold();
		String oldModelKind = parameter.getParameterModel().getModelKindAsString();
		int oldNumberOfComponent = parameter.getParameterModel().getNumberOfComponents();

		parameter.getParameterModel().setModelKind("FULL");
		parameter.getParameterModel().setNumberOfComponents(1);
		parameter.getParameterClustering().setMethod("l");
		parameter.getParameterClustering().setThreshold(threshold);

		ClusterSet clustersLClust = MClust.make(featureSet, clusterSet, parameter, null);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".l.seg");
			MainTools.writeClusterSet(parameter, clustersLClust, false);
		}
		parameter.getParameterSegmentation().setMethod(oldMethod);
		parameter.getParameterModel().setNumberOfComponents(oldNumberOfComponent);
		parameter.getParameterModel().setModelKind(oldModelKind);
		parameter.getParameterClustering().setThreshold(oldThreshold);
		parameter.getParameterSegmentationOutputFile().setMask(mask);

		return clustersLClust;
	}

	/**
	 * Clustering.
	 * 
	 * @param threshold the threshold
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet clustering(double threshold, ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		String oldMethod = parameter.getParameterClustering().getMethodAsString();
		double oldThreshold = parameter.getParameterClustering().getThreshold();
		String oldModelKind = parameter.getParameterModel().getModelKindAsString();
		int oldNumberOfComponent = parameter.getParameterModel().getNumberOfComponents();

		// --- begin NEW v 1.14 / 4.16 / 4.18 / 4.20---
		parameter.getParameterClustering().setMethod("h");
		// parameter.getParameterClustering().setMethod("sr");
		// --- end NEW v 1.14 ---
		parameter.getParameterClustering().setThreshold(threshold);
		logger.finer("method:" + parameter.getParameterClustering().getMethod() + " thr:"
				+ parameter.getParameterClustering().getThreshold());
		parameter.getParameterModel().setModelKind("FULL");
		parameter.getParameterModel().setNumberOfComponents(1);
		ClusterSet clustersHClust = MClust.make(featureSet, clusterSet, parameter, null);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".h.seg");
			MainTools.writeClusterSet(parameter, clustersHClust, false);
		}

		parameter.getParameterSegmentation().setMethod(oldMethod);
		parameter.getParameterModel().setNumberOfComponents(oldNumberOfComponent);
		parameter.getParameterModel().setModelKind(oldModelKind);
		parameter.getParameterClustering().setThreshold(oldThreshold);
		parameter.getParameterSegmentationOutputFile().setMask(mask);

		return clustersHClust;
	}

	/**
	 * Decode.
	 * 
	 * @param nbComp the nb comp
	 * @param threshold the threshold
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet decode(int nbComp, double threshold, ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		String oldModelKind = parameter.getParameterModel().getModelKindAsString();
		int oldNumberOfComponent = parameter.getParameterModel().getNumberOfComponents();

		// ** Train GMM for each cluster.
		// ** GMM is a 8 component gaussian with diagonal covariance matrix
		// ** one GMM = one speaker = one cluster
		// ** initialization of the GMMs :
		// ** - same global covariance for each gaussian,
		// ** - 1/8 for the weight,
		// ** - means are initialized with the mean of 10 successive vectors taken
		parameter.getParameterModel().setModelKind("DIAG");
		parameter.getParameterModel().setNumberOfComponents(nbComp);
		GMMArrayList gmmInitVect = new GMMArrayList(clusterSet.clusterGetSize());
		MTrainInit.make(featureSet, clusterSet, gmmInitVect, parameter);
		// ** EM training of the initialized GMM
		GMMArrayList gmmVect = new GMMArrayList(clusterSet.clusterGetSize());
		MTrainEM.make(featureSet, clusterSet, gmmInitVect, gmmVect, parameter);

		// ** set the penalty to move from the state i to the state j, penalty to move from i to i is equal to 0
		parameter.getParameterDecoder().setDecoderPenalty(String.valueOf(threshold));
		// ** make Viterbi decoding using the 8-GMM set
		// ** one state = one GMM = one speaker = one cluster
		ClusterSet clustersDClust = MDecode.make(featureSet, clusterSet, gmmVect, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".d.seg");
			MainTools.writeClusterSet(parameter, clustersDClust, false);
		}
		// ** move the boundaries of the segment in low energy part of the signal
		ClusterSet clustersAdjClust = SAdjSeg.make(featureSet, clustersDClust, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".adj.seg");
			MainTools.writeClusterSet(parameter, clustersAdjClust, false);
		}

		parameter.getParameterSegmentationOutputFile().setMask(mask);
		parameter.getParameterModel().setNumberOfComponents(oldNumberOfComponent);
		parameter.getParameterModel().setModelKind(oldModelKind);
		return clustersAdjClust;
	}

	/**
	 * Speech.
	 * 
	 * @param threshold the threshold
	 * @param clustersSetBase the clusters set base
	 * @param clustersSegInit the clusters seg init
	 * @param clustersDClust the clusters d clust
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet speech(String threshold, ClusterSet clustersSetBase, ClusterSet clustersSegInit, ClusterSet clustersDClust, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		String oldDecoderPenalty = parameter.getParameterDecoder().getDecoderPenaltyAsString();

		// ** Reload MFCC, remove energy and add delta
		String FeatureFormat = "featureSetTransformation";
		AudioFeatureSet featureSet2 = loadFeature(featureSet, parameter, clustersSetBase, FeatureFormat
				+ ",1:3:2:0:0:0,13,0:0:0:0");
		String dir = "ester2";
		// ** load the model : 8 GMMs with 64 diagonal components
		InputStream pmsInputStream = getClass().getResourceAsStream(dir + "/sms.gmms");
		GMMArrayList pmsVect = MainTools.readGMMContainer(pmsInputStream, parameter.getParameterModel());
		// ** set penalties for the i to j states
		// ** 10 for the first and second model corresponding to boad/narrowband silence
		// ** 50 for the other jingle speech (f0 f2 f3 fx), jingle and music

		parameter.getParameterDecoder().setDecoderPenalty(threshold);
		ClusterSet clustersPMSClust = MDecode.make(featureSet2, clustersSegInit, pmsVect, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".sms.seg");
			MainTools.writeClusterSet(parameter, clustersPMSClust, false);
		}

		parameter.getParameterSegmentationOutputFile().setMask(mask);
		parameter.getParameterDecoder().setDecoderPenalty(oldDecoderPenalty);

		// ** Filter the segmentation adj acoording the sms segmentation
		// ** add 25 frames to all speech segments
		// ** remove silence part if silence segment is less than 25 frames
		// ** if a speech segment is less than 150 frames, it will be merge to the left or right closest segments

		int oldSegmentPadding = parameter.getParameterFilter().getSegmentPadding();
		int oldSilenceMinimumLength = parameter.getParameterFilter().getSilenceMinimumLength();
		int oldSpeechMinimumLength = parameter.getParameterFilter().getSpeechMinimumLength();
		String oldSegmentationFilterFile = parameter.getParameterSegmentationFilterFile().getClusterFilterName();
		parameter.getParameterFilter().setSegmentPadding(25);
		parameter.getParameterFilter().setSilenceMinimumLength(25);
		parameter.getParameterFilter().setSpeechMinimumLength(150);

		ClusterSet clustersFltClust = SFilter.make(clustersDClust, clustersPMSClust, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".flt.seg");
			MainTools.writeClusterSet(parameter, clustersFltClust, false);
			parameter.getParameterSegmentationOutputFile().setMask(mask);
		}

		// ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector
		InputStream silenceInputStream = getClass().getResourceAsStream(dir + "/s.gmms");
		GMMArrayList sVect = MainTools.readGMMContainer(silenceInputStream, parameter.getParameterModel());
		parameter.getParameterSegmentationFilterFile().setClusterFilterName("iS,iT,j");
		ClusterSet clustersSplitClust = SSplitSeg.make(featureSet2, clustersFltClust, sVect, clustersPMSClust, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".spl.seg");
			MainTools.writeClusterSet(parameter, clustersSplitClust, false);
			parameter.getParameterSegmentationOutputFile().setMask(mask);
		}

		parameter.getParameterSegmentationFilterFile().setClusterFilterName(oldSegmentationFilterFile);
		parameter.getParameterFilter().setSegmentPadding(oldSegmentPadding);
		parameter.getParameterFilter().setSilenceMinimumLength(oldSilenceMinimumLength);
		parameter.getParameterFilter().setSpeechMinimumLength(oldSpeechMinimumLength);

		return clustersSplitClust;
	}

	/**
	 * Gender.
	 * 
	 * @param clusterSetBase the cluster set base
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet gender(ClusterSet clusterSetBase, ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		boolean oldByCluster = parameter.getParameterScore().isByCluster();
		boolean oldGender = parameter.getParameterScore().isGender();

		String FeatureFormat = "featureSetTransformation";
		AudioFeatureSet featureSet2 = loadFeature(featureSet, parameter, clusterSet, FeatureFormat
				+ ",1:3:2:0:0:0,13,1:1:0:0");
		String dir = "ester2";
		InputStream genderInputStream = getClass().getResourceAsStream(dir + "/gender.gmms");
		GMMArrayList genderVector = MainTools.readGMMContainer(genderInputStream, parameter.getParameterModel());
		parameter.getParameterScore().setByCluster(true);
		parameter.getParameterScore().setGender(true);
		ClusterSet clustersGender = MScore.make(featureSet2, clusterSet, genderVector, null, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".g.seg");
			MainTools.writeClusterSet(parameter, clustersGender, false);
		}
		parameter.getParameterSegmentationOutputFile().setMask(mask);
		parameter.getParameterScore().setByCluster(oldByCluster);
		parameter.getParameterScore().setGender(oldGender);

		return clustersGender;
	}

	/**
	 * Speaker clustering.
	 * 
	 * @param referenceClusterSet the reference cluster set
	 * @param uemClusterSet the uem cluster set
	 * @param partialKey the partial key
	 * @param method the method
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @param showResult the show result
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet speakerClustering(ClusterSet referenceClusterSet, ClusterSet uemClusterSet, String partialKey, String method, ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter, ClusterSetResultList showResult) throws Exception {

		String oldSpeechDetectorMethod = parameter.getParameterInputFeature().getSpeechMethodAsString();
		double oldSpeechDetectorThreshold = parameter.getParameterInputFeature().getSpeechThreshold();
		String oldModelKind = parameter.getParameterModel().getModelKindAsString();
		int oldNumberOfComponent = parameter.getParameterModel().getNumberOfComponents();
		String oldMethod = parameter.getParameterClustering().getMethodAsString();
		double oldThreshold = parameter.getParameterClustering().getThreshold();

		String oldEMControl = parameter.getParameterEM().getEMControl();
		int oldNTop = parameter.getParameterTopGaussian().getScoreNTop();
		boolean saveAll = parameter.getParameterDiarization().isSaveAllStep();

		DiarizationError computeError = new DiarizationError(referenceClusterSet, uemClusterSet);
		double prevScore = cMin;
		int nbMerge = 0;
		parameter.getParameterInputFeature().setSpeechMethod("E");
		parameter.getParameterInputFeature().setSpeechThreshold(0.1);

		String FeatureFormat = "featureSetTransformation";
		String dir = "ester2";
		InputStream ubmInputStream = getClass().getResourceAsStream(dir + "/ubm.gmm");
		GMMArrayList ubmVect = MainTools.readGMMContainer(ubmInputStream, parameter.getParameterModel());
		GMM ubm = ubmVect.get(0);

		AudioFeatureSet featureSet2 = loadFeature(featureSet, parameter, clusterSet, FeatureFormat
				+ ",1:3:2:0:0:0,13,1:1:300:4");
		parameter.getParameterModel().setModelKind("DIAG");
		parameter.getParameterModel().setNumberOfComponents(ubm.getNbOfComponents());
		parameter.getParameterClustering().setMethod(method);
		parameter.getParameterClustering().setThreshold(cMax);
		parameter.getParameterEM().setEMControl("1,5,0.01");
		parameter.getParameterTopGaussian().setScoreNTop(5);
		// parameter.getParameterDiarization().setSaveAllStep(false);

		CLRHClustering clustering = new CLRHClustering(clusterSet, featureSet2, parameter, ubm);
		clustering.initialize();

		double score = clustering.getScoreOfCandidatesForMerging();
		double errorRate = 100;
		if (computeError.isUsed()) {
			showResult.setResult(prevScore, score, clustering.getClusterSet());
		}
		if (saveAll) {
			writeCLRClusterSet(clustering.getClusterSet(), nbMerge, parameter);
		}
		// prevScore = Math.max(score, prevScore);
		logger.fine("first " + parameter.show + " key=" + partialKey + " clrScore=" + score + " clrErrorRate="
				+ errorRate + " clrSize=" + clustering.getSize());
		while ((score < cMax) && (clustering.getSize() > 1)) {
			nbMerge++;
			if (computeError.isUsed()) {
				showResult.setResult(prevScore, score, clustering.getClusterSet());
				prevScore = Math.max(score, prevScore);
			}
			clustering.mergeCandidates();
			score = clustering.getScoreOfCandidatesForMerging();

			/*
			 * if (computeError.isUsed()) { error = computeError.scoreOfMatchedSpeakers(clustering.getClusterSet()); errorRate = error.getErrorRate(); logger.fine(parameter.show + " key=" + partialKey + " clrScore=" + score + " clrErrorRate=" +
			 * errorRate + " clrSize=" + clustering.getSize()+"/"+referenceClusterSet.clusterGetSize()); }
			 */
			if (saveAll) {
				writeCLRClusterSet(clustering.getClusterSet(), nbMerge, parameter);
			}
		}

		logger.finer(parameter.show + " key=" + partialKey + " show done ");
		if (computeError.isUsed()) {
			showResult.setResult(prevScore, score, clustering.getClusterSet());
			showResult.setResult(score, cMax, clustering.getClusterSet());
		}

		if (saveAll == true) {
			String mask = parameter.getParameterSegmentationOutputFile().getMask();
			parameter.getParameterSegmentationOutputFile().setMask(mask.replace(".seg", "") + ".c.seg");
			MainTools.writeClusterSet(parameter, clustering.getClusterSet(), false);
			parameter.getParameterSegmentationOutputFile().setMask(mask);
		}

		clustering.reset();

		parameter.getParameterModel().setNumberOfComponents(oldNumberOfComponent);
		parameter.getParameterModel().setModelKind(oldModelKind);
		parameter.getParameterClustering().setMethod(oldMethod);
		parameter.getParameterClustering().setThreshold(oldThreshold);
		parameter.getParameterEM().setEMControl(oldEMControl);
		parameter.getParameterTopGaussian().setScoreNTop(oldNTop);
		parameter.getParameterInputFeature().setSpeechMethod(oldSpeechDetectorMethod);
		parameter.getParameterInputFeature().setSpeechThreshold(oldSpeechDetectorThreshold);
		// parameter.getParameterDiarization().setSaveAllStep(oldSaveAll);

		return clustering.getClusterSet();
	}

	/**
	 * Write clr cluster set.
	 * 
	 * @param clusterSet the cluster set
	 * @param indexMerge the index merge
	 * @param parameter the parameter
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws DiarizationException the diarization exception
	 * @throws TransformerException the transformer exception
	 */
	public static void writeCLRClusterSet(ClusterSet clusterSet, int indexMerge, Parameter parameter) throws IOException, ParserConfigurationException, SAXException, DiarizationException, TransformerException {

		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		String mask2 = mask.replace(".seg", "");

		parameter.getParameterSegmentationOutputFile().setMask(mask2 + "."
				+ String.format("%3d", indexMerge).replace(" ", "_") + ".c.seg");
		MainTools.writeClusterSet(parameter, clusterSet, false);
		parameter.getParameterSegmentationOutputFile().setMask(mask);
	}

	/**
	 * Gets the next cluster set.
	 * 
	 * @return the next cluster set
	 */
	public synchronized ClusterSet getNextClusterSet() {

		int index = nbTreatedJob;
		nbTreatedJob++;
		if (index < listOfClusterSet.size()) {
			return listOfClusterSet.get(index);
		}
		return null;
	}

/*
 * public synchronized void sumResult(TreeMap<String, DiarizationResultList> showResult) throws DiarizationException { for (String key : showResult.keySet()) { DiarizationResultList values = showResult.get(key); if (corpusResult.containsKey(key)) {
 * corpusResult.get(key).addResultArray(values); } else { corpusResult.put(key, values); } } }
 */

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		ClusterSet clusterSet = getNextClusterSet();
		while (clusterSet != null) {
			Parameter parameter = getParameter(arguments);
			parameter.show = clusterSet.getShowNames().first();
			logger.finer("-------------------------------------------");
			logger.finer("--- " + parameter.show + " ---");
			logger.finer("-------------------------------------------");
			try {
				ester2Diarization(parameter, clusterSet);
				System.gc();
			} catch (DiarizationException e) {
				logger.log(Level.SEVERE, "Diarization error", e);
				e.printStackTrace();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception error", e);
				e.printStackTrace();
			}
			clusterSet = getNextClusterSet();
		}
	}

	/**
	 * Checks if is thread alive.
	 * 
	 * @return true, if is thread alive
	 */
	public boolean isThreadAlive() {
		for (Diarization diarization : diarizationList) {
			if (diarization.isAlive() == true) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Ester2 diarization corpus.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void ester2DiarizationCorpus(Parameter parameter) throws DiarizationException, Exception {
		// Parameter parameter = getParameter(arguments);

		ClusterSet fullClusterSet = initialize(parameter);
		listOfClusterSet = MainTools.splitHypotesis(fullClusterSet);

		int nbThread = parameter.getParameterDiarization().getThread();

		diarizationList = new ArrayList<Diarization>(nbThread);

		for (int i = 0; i < nbThread; i++) {
			diarizationList.add(new Diarization());
			diarizationList.get(i).start();
		}

		while (isThreadAlive() == true) {
			Thread.sleep(10000);
		}

	}

	/**
	 * Ester2 diarization.
	 * 
	 * @param parameter the parameter
	 * @param clusterSet the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void ester2Diarization(Parameter parameter, ClusterSet clusterSet) throws DiarizationException, Exception {

		TreeMap<String, ClusterSetResultList> result = new TreeMap<String, ClusterSetResultList>();

// double paramThr = parameter.getParameterClustering().getThreshold();
		lMin = parameter.getParameterDiarization().getThreshold("l");
		lMax = parameter.getParameterDiarization().getMaxThreshold("l");
		hMin = parameter.getParameterDiarization().getThreshold("h");
		hMax = parameter.getParameterDiarization().getMaxThreshold("h");
		dMin = parameter.getParameterDiarization().getThreshold("d");
		dMax = parameter.getParameterDiarization().getMaxThreshold("d");
		cMin = parameter.getParameterDiarization().getThreshold("c");
		cMax = parameter.getParameterDiarization().getMaxThreshold("c");

		String featureDesc = parameter.getParameterInputFeature().getFeaturesDescriptorAsString();

		AudioFeatureSet featureSet = null;
		ClusterSet clustersSegInit = null;
		ClusterSet clusterSetResult = null;

		if (parameter.getParameterDiarization().isLoadInputSegmentation() == false) {
			featureSet = loadFeature(parameter, clusterSet, featureDesc);
			featureSet.setCurrentShow(parameter.show);
			int nbFeatures = featureSet.getNumberOfFeatures();
			clusterSet.getFirstCluster().firstSegment().setLength(nbFeatures);
			clustersSegInit = sanityCheck(clusterSet, featureSet, parameter);
		} else {
			featureSet = loadFeature(parameter, clusterSet, featureDesc);
			featureSet.setCurrentShow(parameter.show);
			clustersSegInit = sanityCheck(clusterSet, featureSet, parameter);
			featureSet = loadFeature(parameter, clustersSegInit, featureDesc);
			featureSet.setCurrentShow(parameter.show);
		}

		// seg IRIT
		// ClusterSet clustersSegSave = clustersSegInit;
		// seg IRIT
		ClusterSet referenceClusterSet = MainTools.readTheSecondClusterSet(parameter);
		ClusterSet uemClusterSet = MainTools.readThe3rdClusterSet(parameter);

		if (parameter.getParameterDiarization().isLastStepOnly()) {
			String key = "l=" + lMin + " h=" + hMin + " d=" + dMin;
			ClusterSetResultList showResult = new ClusterSetResultList(cMin, cMax, mult);
			;
			if (parameter.getParameterDiarization().isCEClustering() == false) {
				logger.warning(" nothing to do isCEClustering == false");
			} else {
				clusterSetResult = speakerClustering(referenceClusterSet, uemClusterSet, key, "ce", clustersSegInit, featureSet, parameter, showResult);
			}
			result.put(key, showResult);
			if ((dMin == dMax) && (hMin == hMax) && (lMin == lMax)) {
				MainTools.writeClusterSet(parameter, clusterSetResult, false);
			}
		}

		int segmentationMethod = parameter.getParameterSegmentation().getMethod().ordinal();
		String segmentationMethodString = ParameterSegmentation.SegmentationMethodString[segmentationMethod];
		logger.info("--> segmentation method=" + segmentationMethodString);
		ClusterSet clustersSegSave = segmentation(segmentationMethodString, "FULL", clustersSegInit, featureSet, parameter);
		for (double l = lMin; l <= lMax; l += 0.5) {
			ClusterSet clustersSeg = clustersSegSave.clone();
			logger.finest("clustering l=" + l);
			ClusterSet clustersLClust = clusteringLinear(l, clustersSeg, featureSet, parameter);
			// ---- Begin NEW v 1.14 ---
			for (double h = hMin; h <= hMax; h += 0.5) {
				// for (double h = hMin; h <= hMax; h += 0.2) {
				// ---- end NEW v 1.14 ---
				// if (h > l) {
				ClusterSet clustersHClust = clustering(h, clustersLClust, featureSet, parameter);
				for (double d = dMin; d <= dMax; d += 50) {
					ClusterSet clustersDClust = decode(8, d, clustersHClust, featureSet, parameter);
					// double error = DiarizationError.scoreOfMatchedSpeakers(referenceClusterSet, clustersDClust);
					ClusterSet clustersSplitClust = speech("10,10,50", clusterSet, clustersSegInit, clustersDClust, featureSet, parameter);
					ClusterSet clustersGender = gender(clusterSet, clustersSplitClust, featureSet, parameter);

					String key = "l=" + l + " h=" + h + " d=" + d;
					ClusterSetResultList showResult = new ClusterSetResultList(cMin, cMax, mult);
					;
					if (parameter.getParameterDiarization().isCEClustering() == true) {
						clusterSetResult = speakerClustering(referenceClusterSet, uemClusterSet, key, "ce", clustersGender, featureSet, parameter, showResult);
					} else {
						clusterSetResult = clustersGender;
					}
				}
			}
		}
		// logger.info("value thr:"+dMin+" == "+dMax+") && ("+hMin+" == "+hMax+") && ("+lMin+" == "+lMax+") ");
		if ((dMin == dMax) && (hMin == hMax) && (lMin == lMax)) {
			MainTools.writeClusterSet(parameter, clusterSetResult, false);
		}
	}

	/**
	 * Gets the parameter.
	 * 
	 * @param args the args
	 * @return the parameter
	 */
	public static Parameter getParameter(String[] args) {
		Parameter parameter = new Parameter();
		parameter.getParameterInputFeature().setFeaturesDescription("audio2sphinx,1:1:0:0:0:0,13,0:0:0:0");
		parameter.readParameters(args);
		return parameter;
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			SpkDiarizationLogger.setup();
			arguments = args;
			Parameter parameter = getParameter(args);
			if (args.length <= 1) {
				parameter.help = true;
			}
			parameter.logCmdLine(args);
			info(parameter, "Diarization");

			if (parameter.show.isEmpty() == false) {
				Diarization diarization = new Diarization();
				if (parameter.getParameterDiarization().getSystem() == ParameterBNDiarization.SystemString[1]) {
					parameter.getParameterSegmentationSplit().setSegmentMaximumLength((10 * parameter.getParameterSegmentationInputFile().getRate()));
				}
// if (parameter.getParameterDiarization().getTuning() > 0) {
				logger.info("Diarization tuning");
				diarization.ester2DiarizationCorpus(parameter);
/*
 * } else { logger.info("Diarization BN"); diarization.ester2Version(parameter); }
 */
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "Diarization error", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOExecption error", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Execption error", e);
			e.printStackTrace();
		}

	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param programName the program name
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String programName) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("name = " + programName);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterInputFeature().logAll(); // fInMask
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationInputFile2().logAll(); // sInMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterDiarization().logAll();
			logger.config(parameter.getSeparator());
		}
	}
}
