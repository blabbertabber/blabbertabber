package fr.lium.experimental.spkDiarization.programs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.GMMFactory;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.programs.MTrainMAP;

/**
 * The Class SegmentationPurify.
 */
public class SegmentationPurify {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SegmentationPurify.class.getName());

	/**
	 * Train speaker.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param speakersList the speakers list
	 * @param parameter the parameter
	 * @throws Exception the exception
	 */
	protected static void trainSpeaker(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList speakersList, Parameter parameter) throws Exception {
		speakersList.clear();
		parameter.getParameterEM().setMaximumIteration(1);
		GMMArrayList initVect = new GMMArrayList();
		GMMArrayList speakersListFull = new GMMArrayList();
		MTrainInit.make(featureSet, clusterSet, initVect, parameter);
		MTrainMAP.make(featureSet, clusterSet, initVect, speakersListFull, parameter, false);

		int size = speakersListFull.size();
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			int currentIndex = -1;
			GMM currentGMM = null;
			for (int j = 0; j < size; j++) {
				GMM gmm = speakersListFull.get(j);
				if (cluster.getName().equals(gmm.getName()) == true) {
					currentGMM = gmm;
					currentIndex = j;
					break;
				}
			}
			GMM saveCurrentGMM = currentGMM.clone();
			GMM localInitGMM = initVect.get(currentIndex);
			for (Segment segment : cluster) {
				featureSet.setCurrentShow(segment.getShowName());
				String localName = new String("local" + currentGMM.getName() + "-" + segment.getStart());
				logger.finest("--> new gmm: " + localName);
				Cluster localCluster = new Cluster(localName);
				localCluster.addSegment(segment);
				GMM localGMM = GMMFactory.getMAP(localCluster, featureSet, localInitGMM, localInitGMM, parameter.getParameterEM(), parameter.getParameterMAP(), parameter.getParameterVarianceControl(), parameter.getParameterInputFeature().useSpeechDetection(), false);

				logger.finest("count: " + currentGMM.getComponent(0).getCount() + " local:"
						+ localGMM.getComponent(0).getCount() + " int:" + localInitGMM.getComponent(0).getCount());

				for (int k = 0; k < currentGMM.getNbOfComponents(); k++) {
					currentGMM.getComponent(k).statistic_remove(localGMM.getComponent(k), 1);
				}
				logger.finest("count: " + currentGMM.getComponent(0).getCount());
				currentGMM.setAdaptedModel(localInitGMM, parameter.getParameterMAP());
				logger.info("add gmm: " + currentGMM.getName());
				speakersList.add(currentGMM);
				currentGMM = saveCurrentGMM.clone();
			}
		}
	}

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
		ClusterSet decodeClusterSet = MDecode.make(featureSet, decodeCluster, speakersList, parameter);
		ClusterSet resultClusterSet = new ClusterSet();
		TreeMap<Integer, Segment> featureMap = decodeClusterSet.getFeatureMap();

		for (Segment segment : clusterSet.getSegments()) {
			Segment seg = featureMap.get(segment.getStart() + (segment.getLength() / 2));
			// logger.finest("segment:"+segment.getStart());
			Cluster oldCluster = seg.getCluster();
			String name = oldCluster.getName();
			Cluster cluster = resultClusterSet.getOrCreateANewCluster(name);
			Segment newSegment = segment.clone();
			newSegment.getSpeechFeatureList().clear();
			cluster.addSegment(newSegment);

			cluster.setGender(oldCluster.getGender());
			cluster.setBandwidth(oldCluster.getBandwidth());
			cluster.setChannel(oldCluster.getChannel());
		}
		return resultClusterSet;
	}

	/**
	 * Purify.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet purify(AudioFeatureSet featureSet, ClusterSet clusterSet, Parameter parameter) throws Exception {
		GMMArrayList speakersList = new GMMArrayList();
		logger.info("resegmentation : train speakers");
		speakersList.clear();
		double saveSpeechThreshold = parameter.getParameterInputFeature().getSpeechThreshold();
		parameter.getParameterInputFeature().setSpeechThreshold(Double.MAX_VALUE);
		trainSpeaker(featureSet, clusterSet, speakersList, parameter);

		logger.info("resegmentation : decode ");
		ClusterSet resultClusterSet = decode(featureSet, clusterSet, speakersList, parameter);

		parameter.getParameterInputFeature().setSpeechThreshold(saveSpeechThreshold);
		return resultClusterSet;
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
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterSet, Parameter parameter) throws Exception {
		ClusterSet current = purify(featureSet, clusterSet, parameter);
		ClusterSet previous = new ClusterSet();

		while (current.equals(previous) == false) {
			previous = current;
			current = purify(featureSet, clusterSet, parameter);
		}

		return current;
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
			info(parameter, "ReSegmentation");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);

				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
				MainTools.readGMMForTopGaussian(parameter, featureSet);

				ClusterSet resultClusterSet = make(featureSet, clusterSet, parameter);

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
			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			parameter.getParameterTopGaussian().logTopGaussian(); // sTop
			logger.config(parameter.getSeparator());
			parameter.getParameterInitializationEM().logAll();
			parameter.getParameterEM().logAll(); // emCtl
			parameter.getParameterMAP().logAll(); // mapCtrl
			parameter.getParameterVarianceControl().logAll(); // varCtrl
			logger.config(parameter.getSeparator());
			parameter.getParameterDecoder().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterClustering().logAll(); // cThr
		}
	}

}
