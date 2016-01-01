package fr.lium.experimental.spkDiarization.programs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.Gaussian;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MSeg;
import fr.lium.spkDiarization.programs.MTrainEM;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.programs.MTrainMAP;

/**
 * The Class SegmentationMeeting.
 */
public class SegmentationMeeting {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SegmentationMeeting.class.getName());

	/**
	 * Segmentation.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet segmentation(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();

		ClusterSet clustersSeg = new ClusterSet();
		MSeg.make(featureSet, clusterSet, clustersSeg, parameter);

		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".s.seg");
			MainTools.writeClusterSet(parameter, clustersSeg, false);
		}

		return clustersSeg;
	}

	/**
	 * Train ubm.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param parameter the parameter
	 * @return the gmm
	 * @throws Exception the exception
	 */
	protected static GMM trainUBM(AudioFeatureSet featureSet, ClusterSet clusterSet, Parameter parameter) throws Exception {
		GMMArrayList speakersList = new GMMArrayList();
		parameter.getParameterInitializationEM().setModelInitMethod("uniform");
		parameter.getParameterEM().setMaximumIteration(20);
		GMMArrayList initVect = new GMMArrayList();

		ClusterSet clusterSetUBM = new ClusterSet();
		Cluster clusterUBM = clusterSetUBM.createANewCluster("UBM");
		for (Segment segment : clusterSet.getSegments()) {
			clusterUBM.addSegment(segment.clone());
		}

		MTrainInit.make(featureSet, clusterSetUBM, initVect, parameter);
		MTrainEM.make(featureSet, clusterSetUBM, initVect, speakersList, parameter);

		return speakersList.get(0);
	}

	/**
	 * Train speaker.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param ubm the ubm
	 * @param parameter the parameter
	 * @return the gMM array list
	 * @throws Exception the exception
	 */
	protected static GMMArrayList trainSpeaker(AudioFeatureSet featureSet, ClusterSet clusterSet, GMM ubm, Parameter parameter) throws Exception {
		GMMArrayList speakersList = new GMMArrayList();
// parameter.getParameterEM().setMaximumIteration(10);
		parameter.getParameterModel().setKind(Gaussian.DIAG);
// parameter.getParameterInitializationEM().setModelInitMethod("uniform");
		parameter.getParameterInitializationEM().setModelInitMethod("copy");
		GMMArrayList initVect = new GMMArrayList();
		int nbGmm = 0;
		for (String name : clusterSet) {
			initVect.add(ubm.clone());
			initVect.get(nbGmm).setName(name);
			nbGmm++;
		}
		MTrainMAP.make(featureSet, clusterSet, initVect, speakersList, parameter, true);
// MTrainEM.make(featureSet, clusterSet, initVect, speakersList, parameter);

		return speakersList;
	}

/*
 * private static ClusterSet assignSegmentToSpeaker(FeatureSet featureSet, ClusterSet clusterSet, GMMArrayList speakersList, Parameter parameter) throws DiarizationException, IOException { ClusterSet clusterSetResult = new ClusterSet(); int nbAssign
 * = 0; for (Segment segment: clusterSet.getSegments()) { String show = segment.getShowName(); int start = segment.getStart(); int len = segment.getLength(); featureSet.setCurrentShow(show); String name = "enmpty"; double maxValue =
 * -Double.MAX_VALUE; for (GMM gmm : speakersList) { gmm.initializeScoreAccumulator(); for (int j = start; j <= segment.getLast(); j++) { gmm.getAndAccumulateLikelihood(featureSet, j); } if (gmm.getMeanLogLikelihood() > maxValue) { name =
 * gmm.getName(); maxValue = gmm.getMeanLogLikelihood(); } } if (!segment.getClusterName().equals(name)) { nbAssign++; } logger.info("assign start: "+segment.getStartInSecond()+ " "+segment.getClusterName()+" --> "+name+ " llh: "+maxValue); Cluster
 * cluster = clusterSetResult.getOrCreateANewCluster(name); cluster.addSegment(new Segment(show, start, len, cluster, segment.getRate())); } logger.info("--> assign nb: "+nbAssign); return clusterSetResult; }
 */

	/**
	 * Decode.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param speakersList the speakers list
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	protected static ClusterSet decode(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList speakersList, Parameter parameter) throws Exception {
		logger.info("segment data");
		ClusterSet decodeCluster = clusterSet.clone();
		parameter.getParameterDecoder().setDecoderPenalty("50");
		parameter.getParameterDecoder().setViterbiDurationConstraints("minimal,50");

		ClusterSet decodeClusterSet = MDecode.make(featureSet, decodeCluster, speakersList, parameter);

		return decodeClusterSet;
	}

	/**
	 * Make part.
	 * 
	 * @param part the part
	 * @param minNbGauss the min nb gauss
	 * @param maxNbGauss the max nb gauss
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet makePart(int part, int minNbGauss, int maxNbGauss, ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws Exception {
		// segment
		ClusterSet clusterSetClustering = affectation(clusterSet, part);

		parameter.getParameterModel().setKind(Gaussian.DIAG);

		// assign
		ClusterSet current = clusterSetClustering;
		for (int nbGauss = minNbGauss; nbGauss <= maxNbGauss; nbGauss *= 2) {
			logger.info(parameter.getSeparator());
			logger.info(" --> iteration: " + nbGauss);
			parameter.getParameterModel().setNumberOfComponents(nbGauss);
			GMM ubm = trainUBM(featureSet, current, parameter);
// GMM ubm = null;
			ClusterSet previous = new ClusterSet();
			while ((current.equals(previous) == false)) {
				previous = current;
				GMMArrayList speakersList = trainSpeaker(featureSet, previous, ubm, parameter);
				// current = assignSegmentToSpeaker(featureSet, previous, speakersList, parameter);
				current = decode(featureSet, previous, speakersList, parameter);
			}
			// saveClusterSet(current, nbGauss, parameter);
		}

		return current;
	}

	/**
	 * Make.
	 * 
	 * @param clusterSet the cluster set
	 * @param clusterSetResult the cluster set result
	 * @param featureSet the feature set
	 * @param parameterGlobal the parameter global
	 * @throws Exception the exception
	 */
	public static void make(ClusterSet clusterSet, ClusterSet clusterSetResult, AudioFeatureSet featureSet, Parameter parameterGlobal) throws Exception {
		// ClusterSet clusterSetInitial = sanityCheck(clusterSet, featureSet, parameter);
		Parameter parameter = parameterGlobal.clone();

		String saveMethod = parameter.getParameterSegmentation().getMethodAsString();
		parameter.getParameterSegmentation().setMethod("GLR");
		ClusterSet clusterSetSegmentation = segmentation(clusterSet, featureSet, parameter);
		parameter.getParameterSegmentation().setMethod(saveMethod);

		int nbFeaturesInClusterSet = clusterSetSegmentation.getLength();
		int partSize = 15 * 60 * 100;
		int nbPart = (int) Math.round(((double) nbFeaturesInClusterSet / (double) partSize) + 0.5);
		int size = nbFeaturesInClusterSet / nbPart;
		logger.info("nbPart: " + nbPart + " partSize: " + partSize + " size: " + size);
		ClusterSet clusterSetPart = new ClusterSet();
		// clusterSetResult = new ClusterSet();
		int duration = 0;
		int part = 1;
		for (Segment segment : clusterSetSegmentation.getSegments()) {
			clusterSetPart.getOrCreateANewCluster(segment.getCluster().getName()).addSegment(segment.clone());
			duration += segment.getLength();
			if (duration >= size) {
				ClusterSet partialResul = makePart(part, 1, 5, clusterSetPart, featureSet, parameter);
				clusterSetResult.addVector(partialResul.getSegmentVectorRepresentation());
				clusterSetPart = new ClusterSet();
				part++;
				duration = 0;
			}
		}
		if (clusterSetPart.getLength() > 0) {
			ClusterSet partialResul = makePart(part, 1, 4, clusterSetPart, featureSet, parameter);
			clusterSetResult.addVector(partialResul.getSegmentVectorRepresentation());
		}
		// return clusterSetResult;
		// return makePart(part, 4, 8, clusterSetResult, featureSet, parameter);
	}

	/**
	 * Save cluster set.
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
	public static void saveClusterSet(ClusterSet clusterSet, int indexMerge, Parameter parameter) throws IOException, ParserConfigurationException, SAXException, DiarizationException, TransformerException {
		// if (parameter.getParameterDiarization().isSaveAllStep()) {
		String segOutFilename = parameter.show + "-" + String.format("%3d", indexMerge).replace(" ", "_");
		logger.info("--> save clustering : " + segOutFilename);
		clusterSet.write(segOutFilename, parameter.getParameterSegmentationOutputFile());
		// }
	}

	/**
	 * Affectation.
	 * 
	 * @param clusterSet the cluster set
	 * @param part the part
	 * @return the cluster set
	 */
	private static ClusterSet affectation(ClusterSet clusterSet, int part) {
		ClusterSet clusterSetResult = new ClusterSet();
		int spk = 0;
		for (Segment segment : clusterSet.getSegments()) {
			clusterSetResult.getOrCreateANewCluster("P" + part + "S" + spk).addSegment(segment.clone());
			spk++;
			if (spk >= 20) {
				spk = 0;
			}
		}

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
			info(parameter, "Segmentation Sing");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);

				ClusterSet resultClusterSet = new ClusterSet();
				make(clusterSet, resultClusterSet, featureSet, parameter);

				MainTools.writeClusterSet(parameter, resultClusterSet, true);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param progam the progam
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String progam) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("Program name = " + progam);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterInputFeature().logAll(); // fInMask
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterEM().logAll(); // emCtl
			parameter.getParameterMAP().logAll(); // mapCtrl
			parameter.getParameterVarianceControl().logAll(); // varCtrl
			logger.config(parameter.getSeparator());
			parameter.getParameterDecoder().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentation().logAll();
		}
	}

}
