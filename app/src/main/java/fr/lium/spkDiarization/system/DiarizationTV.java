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
import fr.lium.spkDiarization.lib.libDiarizationError.DiarizationError;
import fr.lium.spkDiarization.lib.libDiarizationError.DiarizationResult;
import fr.lium.spkDiarization.lib.libDiarizationError.DiarizationResultList;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libClusteringMethod.CLRHClustering;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterBNDiarization;
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
 * The Class DiarizationTV.
 */
public class DiarizationTV {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Diarization.class.getName());

	/** The h max. */
	double lMin = 1, lMax = 3, hMin = 2, hMax = 5;

	/** The d max. */
	double dMin = 100, dMax = 350;

	/** The c min. */
	double cMin = 0;

	/** The c max. */
	double cMax = 5.0;

	/** The mult. */
	double mult = 100;

	/** The diarization list. */
	static ArrayList<Diarization> diarizationList;

	/** The nb treated job. */
	static int nbTreatedJob = 0;

	/** The corpus result. */
	static TreeMap<String, DiarizationResultList> corpusResult;

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
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".i.seg");
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
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".s.seg");
			MainTools.writeClusterSet(parameter, clustersSeg, false);
		}

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
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".l.seg");
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
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".h.seg");
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
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".d.seg");
			MainTools.writeClusterSet(parameter, clustersDClust, false);
		}
		// ** move the boundaries of the segment in low energy part of the signal
		ClusterSet clustersAdjClust = SAdjSeg.make(featureSet, clustersDClust, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".adj.seg");
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
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".sms.seg");
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
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".flt.seg");
			MainTools.writeClusterSet(parameter, clustersFltClust, false);
			parameter.getParameterSegmentationOutputFile().setMask(mask);
		}

		// ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector
		InputStream silenceInputStream = getClass().getResourceAsStream(dir + "/s.gmms");
		GMMArrayList sVect = MainTools.readGMMContainer(silenceInputStream, parameter.getParameterModel());
		parameter.getParameterSegmentationFilterFile().setClusterFilterName("iS,iT,j");
		ClusterSet clustersSplitClust = SSplitSeg.make(featureSet2, clustersFltClust, sVect, clustersPMSClust, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".spl.seg");
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
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".g.seg");
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
	 * @param threshold the threshold
	 * @param method the method
	 * @param clusterSetBase the cluster set base
	 * @param clustersSet the clusters set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet speakerClustering(double threshold, String method, ClusterSet clusterSetBase, ClusterSet clustersSet, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		String oldMethod = parameter.getParameterClustering().getMethodAsString();
		double oldThreshold = parameter.getParameterClustering().getThreshold();
		String oldEMControl = parameter.getParameterEM().getEMControl();
		int oldNTop = parameter.getParameterTopGaussian().getScoreNTop();
		String oldSpeechDetectorMethod = parameter.getParameterInputFeature().getSpeechMethodAsString();
		double oldSpeechDetectorThreshold = parameter.getParameterInputFeature().getSpeechThreshold();

		// ** bottom up hierarchical classification using GMMs
		// ** one for each cluster, trained by MAP adaptation of a UBM composed of the fusion of 4x128GMM
		// ** the feature normalization use feature mapping technique, after the cluster frames are centered and reduced
		String dir = "ester2";
		InputStream ubmInputStream = getClass().getResourceAsStream(dir + "/ubm.gmm");
		GMMArrayList ubmVect = MainTools.readGMMContainer(ubmInputStream, parameter.getParameterModel());
		GMM ubm = ubmVect.get(0);
		// int nbCep = ubm.getDimension() + 1;
		String FeatureFormat = "featureSetTransformation";

		parameter.getParameterInputFeature().setSpeechMethod("E");
		parameter.getParameterInputFeature().setSpeechThreshold(0.1);

		AudioFeatureSet featureSet2 = loadFeature(featureSet, parameter, clustersSet, FeatureFormat
				+ ",1:3:2:0:0:0,13,1:1:300:4");
		parameter.getParameterClustering().setMethod(method);
		parameter.getParameterClustering().setThreshold(threshold);
		parameter.getParameterEM().setEMControl("1,5,0.01");
		parameter.getParameterTopGaussian().setScoreNTop(5);
		// ---- Begin NEW v 1.13 ---

// if (parameter.parameterSpeechDetector.useSpeechDetection() == true) {
// MSpeechDetector.EnergyThresholdMethod(clustersSet, featureSet, parameter);
// }
		// ---- End NEW v 1.13 ---

		boolean saveAll = parameter.getParameterDiarization().isSaveAllStep();
		parameter.getParameterDiarization().setSaveAllStep(false);
		ClusterSet clustersCLR = MClust.make(featureSet2, clustersSet, parameter, ubm);
		parameter.getParameterDiarization().setSaveAllStep(saveAll);

		parameter.getParameterSegmentationOutputFile().setMask(mask);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".c.seg");
			MainTools.writeClusterSet(parameter, clustersCLR, false);
		}
		parameter.getParameterSegmentationOutputFile().setMask(mask);
		parameter.getParameterClustering().setMethod(oldMethod);
		parameter.getParameterClustering().setThreshold(oldThreshold);
		parameter.getParameterEM().setEMControl(oldEMControl);
		parameter.getParameterTopGaussian().setScoreNTop(oldNTop);
		parameter.getParameterInputFeature().setSpeechMethod(oldSpeechDetectorMethod);
		parameter.getParameterInputFeature().setSpeechThreshold(oldSpeechDetectorThreshold);

		return clustersCLR;
	}

	/**
	 * Ester2 version.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void ester2Version(Parameter parameter) throws DiarizationException, Exception {

		// ** Caution this system is developed using Sphinx MFCC computed with legacy mode
		ClusterSet referenceClusterSet = null;
		if (!parameter.getParameterSegmentationInputFile2().getMask().equals("")) {
			referenceClusterSet = MainTools.readTheSecondClusterSet(parameter);
		}
		ClusterSet uemClusterSet = null;
		if (!parameter.getParameterSegmentationInputFile3().getMask().equals("")) {
			referenceClusterSet = MainTools.readThe3rdClusterSet(parameter);
		}
		ParameterBNDiarization parameterDiarization = parameter.getParameterDiarization();
		// ** mask for the output of the segmentation file

		ClusterSet clusterSet = initialize(parameter);

		// ** load the features, sphinx format (13 MFCC with C0) or compute it form a wave file
		AudioFeatureSet featureSet = loadFeature(parameter, clusterSet, parameter.getParameterInputFeature().getFeaturesDescriptorAsString());
		featureSet.setCurrentShow(parameter.show);
		int nbFeatures = featureSet.getNumberOfFeatures();
		if (parameter.getParameterDiarization().isLoadInputSegmentation() == false) {
			clusterSet.getFirstCluster().firstSegment().setLength(nbFeatures);
		}
		// clusterSet.debug(3);
		ClusterSet clustersSegInit = sanityCheck(clusterSet, featureSet, parameter);
		ClusterSet clustersSeg = segmentation("GLR", "FULL", clustersSegInit, featureSet, parameter);
		// Seg IRIT
		// ClusterSet clustersSegInit = clusterSet;
		// ClusterSet clustersSeg = clusterSet;
		// Seg IRIT
		ClusterSet clustersLClust = clusteringLinear(parameterDiarization.getThreshold("l"), clustersSeg, featureSet, parameter);
		ClusterSet clustersHClust = clustering(parameterDiarization.getThreshold("h"), clustersLClust, featureSet, parameter);
		// MainTools.writeClusterSet(parameter, clustersHClust, false);

		ClusterSet clustersDClust = decode(8, parameterDiarization.getThreshold("d"), clustersHClust, featureSet, parameter);
		ClusterSet clustersSplitClust = speech("10,10,50", clusterSet, clustersSegInit, clustersDClust, featureSet, parameter);
		ClusterSet clustersGender = gender(clusterSet, clustersSplitClust, featureSet, parameter);

		if (parameter.getParameterDiarization().isCEClustering()) {
			ClusterSet clustersCLR = speakerClustering(parameterDiarization.getThreshold("c"), "ce", clustersSegInit, clustersGender, featureSet, parameter);
			MainTools.writeClusterSet(parameter, clustersCLR, false);
			if (referenceClusterSet != null) {
				DiarizationError computeError = new DiarizationError(referenceClusterSet, uemClusterSet);
				computeError.scoreOfMatchedSpeakers(clustersCLR);
			}
		} else {
			MainTools.writeClusterSet(parameter, clustersGender, false);
		}
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

	/**
	 * Sum result.
	 * 
	 * @param showResult the show result
	 * @throws DiarizationException the diarization exception
	 */
	public synchronized void sumResult(TreeMap<String, DiarizationResultList> showResult) throws DiarizationException {
		for (String key : showResult.keySet()) {
			DiarizationResultList values = showResult.get(key);
			if (corpusResult.containsKey(key)) {
				corpusResult.get(key).addResultArray(values);
			} else {
				corpusResult.put(key, values);
			}
		}
	}

	/**
	 * Run.
	 */
	public void run() {
		ClusterSet clusterSet = getNextClusterSet();
		while (clusterSet != null) {
			Parameter parameter = getParameter(arguments);
			parameter.show = clusterSet.getShowNames().first();
			logger.finer("-------------------------------------------");
			logger.finer("--- " + parameter.show + " ---");
			logger.finer("-------------------------------------------");
			TreeMap<String, DiarizationResultList> showResult;
			try {
				showResult = tunEster2Diarization(parameter, clusterSet);
				sumResult(showResult);
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
	 * Tun ester2 corpus.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void tunEster2Corpus(Parameter parameter) throws DiarizationException, Exception {
		// Parameter parameter = getParameter(arguments);

		corpusResult = new TreeMap<String, DiarizationResultList>();
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

		logger.finer("**** ALL SHOWS ***");
		for (String key : corpusResult.keySet()) {
			DiarizationResultList values = corpusResult.get(key);
			values.log(key);
		}
	}

	/**
	 * Tun ester2 diarization.
	 * 
	 * @param parameter the parameter
	 * @param clusterSet the cluster set
	 * @return the tree map
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public TreeMap<String, DiarizationResultList> tunEster2Diarization(Parameter parameter, ClusterSet clusterSet) throws DiarizationException, Exception {

		TreeMap<String, DiarizationResultList> result = new TreeMap<String, DiarizationResultList>();

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
			DiarizationResultList values = null;
			if (parameter.getParameterDiarization().isCEClustering() == false) {
				logger.warning(" nothing to do isCEClustering == false");
			} else {
				values = tunEster2SpeakerCLRClustering(referenceClusterSet, uemClusterSet, key, "ce", clusterSet, clusterSet, featureSet, parameter);
			}
			result.put(key, values);
			return result;
		}

		ClusterSet clustersSegSave = segmentation("GLR", "FULL", clustersSegInit, featureSet, parameter);
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
					DiarizationResultList values = null;
					if (parameter.getParameterDiarization().isCEClustering() == false) {
						values = new DiarizationResultList(0, 0, 1);
						DiarizationError computeError = new DiarizationError(referenceClusterSet, uemClusterSet);
						DiarizationResult error = computeError.scoreOfMatchedSpeakers(clustersGender);
						values.setResult(0, 0, error);
						logger.finer(parameter.show + " key=" + key + " resultat du fichier");
						values.log("partial result: " + parameter.show + " " + key);
					} else {
						// V4.19 = CLUST_H_BIC_GMM_MAP
						// values = tunEster2SpeakerCLRClustering(referenceClusterSet, key, "bicgmmmap", clustersGender, clustersGender, featureSet, parameter);
						// V5.16 = ce_d
						// values = tunEster2SpeakerCLRClustering(referenceClusterSet, key, "ce_d", clustersGender, clustersGender, featureSet, parameter);
						values = tunEster2SpeakerCLRClustering(referenceClusterSet, uemClusterSet, key, "ce", clustersGender, clustersGender, featureSet, parameter);
					}
					if (result.containsKey(key)) {
						result.get(key).addResultArray(values);
					} else {
						result.put(key, values);
					}
				}
				// }
			}
		}
		return result;
	}

	/**
	 * Tun ester2 speaker clr clustering.
	 * 
	 * @param referenceClusterSet the reference cluster set
	 * @param uemClusterSet the uem cluster set
	 * @param partialKey the partial key
	 * @param method the method
	 * @param clusterSetBase the cluster set base
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the diarization result list
	 * @throws Exception the exception
	 */
	public DiarizationResultList tunEster2SpeakerCLRClustering(ClusterSet referenceClusterSet, ClusterSet uemClusterSet, String partialKey, String method, ClusterSet clusterSetBase, ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		String oldSpeechDetectorMethod = parameter.getParameterInputFeature().getSpeechMethodAsString();
		double oldSpeechDetectorThreshold = parameter.getParameterInputFeature().getSpeechThreshold();
		String oldModelKind = parameter.getParameterModel().getModelKindAsString();
		int oldNumberOfComponent = parameter.getParameterModel().getNumberOfComponents();
		String oldMethod = parameter.getParameterClustering().getMethodAsString();
		double oldThreshold = parameter.getParameterClustering().getThreshold();

		String oldEMControl = parameter.getParameterEM().getEMControl();
		int oldNTop = parameter.getParameterTopGaussian().getScoreNTop();
		boolean oldSaveAll = parameter.getParameterDiarization().isSaveAllStep();

		DiarizationResultList localResult = new DiarizationResultList(cMin, cMax, mult);
		DiarizationError computeError = new DiarizationError(referenceClusterSet, uemClusterSet);
		double prevScore = cMin;

		// ---- Begin NEW v 1.13 ---
		parameter.getParameterInputFeature().setSpeechMethod("E");
		parameter.getParameterInputFeature().setSpeechThreshold(0.1);

		// ---- End NEW v 1.13 ---
		String FeatureFormat = "featureSetTransformation";
		String dir = "ester2";
		InputStream ubmInputStream = getClass().getResourceAsStream(dir + "/ubm.gmm");
		GMMArrayList ubmVect = MainTools.readGMMContainer(ubmInputStream, parameter.getParameterModel());
		GMM ubm = ubmVect.get(0);
		/*
		 * int nbCep = 16; logger.info("---> nbCep:"+nbCep); FeatureSet featureSet2 = loadFeature(parameter, clusterSet, "audio16kHz2sphinx,1:3:2:0:0:0,"+nbCep+",1:1:300:4"); logger.info("---> nbCep:"+nbCep); //logger.fine("*** nbFeaturesNorm:" +
		 * clusterSetBase.getLength());
		 */
		// A tester

		AudioFeatureSet featureSet2 = loadFeature(featureSet, parameter, clusterSet, FeatureFormat
				+ ",1:3:2:0:0:0,13,1:1:300:4");
		// v5.14
		// FeatureSet featureSet2 = loadFeature(featureSet, parameter, clusterSet, FeatureFormat
		// + ",1:3:2:0:0:0,13,1:1:0:0");
		parameter.getParameterModel().setModelKind("DIAG");
		parameter.getParameterModel().setNumberOfComponents(ubm.getNbOfComponents());
		parameter.getParameterClustering().setMethod(method);
		// ---- Begin NEW v 1.19 ---
		// parameter.getParameterEM().setEMControl("1,1,0.01");
		// parameter.getParameterClustering().setThreshold(0);
		// ---- End NEW v 1.19 ---
		parameter.getParameterClustering().setThreshold(cMax);
		parameter.getParameterEM().setEMControl("1,5,0.01");
		parameter.getParameterTopGaussian().setScoreNTop(5);
		parameter.getParameterDiarization().setSaveAllStep(false);

		CLRHClustering clustering = new CLRHClustering(clusterSet, featureSet2, parameter, ubm);
		// int nbCluster = clusterSet.clusterGetSize();
		// logger.info("initialise clustering CLR clusterSet:"+clusterSet);
		clustering.initialize();

		double score = clustering.getScoreOfCandidatesForMerging();
		DiarizationResult error = computeError.scoreOfMatchedSpeakers(clustering.getClusterSet());
		double errorRate = error.getErrorRate();
		localResult.setResult(prevScore, score, error);
		// prevScore = Math.max(score, prevScore);
		logger.fine("first " + parameter.show + " key=" + partialKey + " clrScore=" + score + " clrErrorRate="
				+ errorRate + " clrSize=" + clustering.getSize() + "/" + referenceClusterSet.clusterGetSize());
		while ((score < cMax) && (clustering.getSize() > 1)) {
			localResult.setResult(prevScore, score, error);
			prevScore = Math.max(score, prevScore);
			clustering.mergeCandidates();

			// -- start V5.16 --
// logger.info("--> Decoding");
// ClusterSet decodeClusterSet = MDecode.make(featureSet2, clustering.getClusterSet(), clustering.getGmmList(), parameter);
// logger.info("--> Clustering");
// featureSet2 = loadFeature(featureSet, parameter, decodeClusterSet, FeatureFormat
// + ",1:3:2:0:0:0,13,1:1:300:4");
// clustering = new CLRHClustering(decodeClusterSet, featureSet2, parameter, ubm);
// clustering.initialize();
			// -- end V5.16 --

			score = clustering.getScoreOfCandidatesForMerging();
			error = computeError.scoreOfMatchedSpeakers(clustering.getClusterSet());
			errorRate = error.getErrorRate();
			// localResult.setResult(prevScore, score, error);
			// prevScore = Math.max(score, prevScore);
			logger.fine(parameter.show + " key=" + partialKey + " clrScore=" + score + " clrErrorRate=" + errorRate
					+ " clrSize=" + clustering.getSize() + "/" + referenceClusterSet.clusterGetSize());
		}
		localResult.setResult(prevScore, score, error);
		localResult.setResult(score, cMax, error);
		logger.finer(parameter.show + " key=" + partialKey + " resultat du fichier");
		localResult.log("partial result: " + parameter.show + " " + partialKey);

		clustering.reset();

		parameter.getParameterModel().setNumberOfComponents(oldNumberOfComponent);
		parameter.getParameterModel().setModelKind(oldModelKind);
		parameter.getParameterClustering().setMethod(oldMethod);
		parameter.getParameterClustering().setThreshold(oldThreshold);
		parameter.getParameterEM().setEMControl(oldEMControl);
		parameter.getParameterTopGaussian().setScoreNTop(oldNTop);
		parameter.getParameterInputFeature().setSpeechMethod(oldSpeechDetectorMethod);
		parameter.getParameterInputFeature().setSpeechThreshold(oldSpeechDetectorThreshold);
		parameter.getParameterDiarization().setSaveAllStep(oldSaveAll);

		return localResult;
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
				DiarizationTV diarizationTV = new DiarizationTV();
				if (parameter.getParameterDiarization().getSystem() == ParameterBNDiarization.SystemString[1]) {
					parameter.getParameterSegmentationSplit().setSegmentMaximumLength((10 * parameter.getParameterSegmentationInputFile().getRate()));
				}
				if (parameter.getParameterDiarization().getThread() > 0) {
					logger.info("Diarization tuning");
					diarizationTV.tunEster2Corpus(parameter);
				} else {
					logger.info("Diarization BN");
					diarizationTV.ester2Version(parameter);
				}
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
