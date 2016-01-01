package fr.lium.spkDiarization.libFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.ParameterAudioFeature.SpeechDetectorMethod;

/**
 * The Class AudioSpeechDetection.
 */
public class AudioSpeechDetection {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(AudioSpeechDetection.class.getName());

	/**
	 * Bi gaussien thresholded method.
	 * 
	 * @param featureSet the feature set
	 * @return the cluster set
	 */
	protected static ClusterSet BiGaussienThresholdedMethod(AudioFeatureSet featureSet) {
		/*
		 * String FeatureFormat = parameterFeature.getFeaturesDescriptorAsString(); double EThr = parameterFeature.getSpeechThreshold(); parameterFeature.setSpeechThreshold(0);
		 * parameterFeature.setFeaturesDescription("featureSetTransformation,3:1:0:0:0:0,13,0:0:0:0"); //FeatureSet featureSet2 = MainTools.readFeatureSet(parameter, clusterSet, featureSet); GMMArrayList initizationGmmList = new GMMArrayList(); //
		 * GMMArrayList gmmList = new GMMArrayList(); ClusterSet clusterSetResult = new ClusterSet(); // int engergyIndex = featureSet.getIndexOfEnergy(); for (Cluster cluster : clusterSet.clusterSetValue()) { double thrNS =
		 * Distance.getEnergyThreshold(cluster, featureSet, 0.1); // double thrNS2 = Distance.getEnergyThreshold(cluster, featureSet, 0.1); double thrS = Distance.getEnergyThreshold(cluster, featureSet, 0.9); // double thrS2 =
		 * Distance.getEnergyThreshold(cluster, featureSet, 0.9); String name = cluster.getName(); Gaussian gaussian = new DiagGaussian(1); gaussian.initializeStatisticAccumulator(); gaussian.addFeaturesFromSegments(cluster.iterator(), featureSet);
		 * gaussian.setModelFromAccululator(); //gaussian.debug(4); GMM init = new GMM(0, 1); init.addComponent(gaussian); init.addComponent(gaussian); init.getComponent(0).setMean(0, thrNS); init.getComponent(1).setMean(0, thrS);
		 * initizationGmmList.add(init); //logger.info("comp="+init.getNbOfComponents()+" // dim="+init.getDimension()); //logger.info("Mean 0:"+init.getComponent(0).getMean(0)); //logger.info("Mean 1:"+init.getComponent(1).getMean(0));
		 * init.setName(name); //init.debug(4); //parameter.getParameterEM().setMaximumIteration(1); GMM gmm = GMMFactory.getEM(cluster, featureSet, init, 2, parameter.parameterEM, parameter.parameterVarianceControl, false); //gmmList.add(gmm);
		 * //logger.info("NS :"+thrNS+ " NS2: "+thrNS2); //logger.info("S :"+thrS+ " S2: "+thrS2); //logger.info("Mean :"+gaussian.getMean(0)+ " cov:"+gaussian.getCovariance(0, 0)); //logger.info("init 0:"+init.getComponent(0).getMean(0)+
		 * " cov:"+init.getComponent(0).getCovariance(0, 0)); //logger.info("init 1:"+init.getComponent(1).getMean(0)+ " cov:"+init.getComponent(1).getCovariance(0, 0)); //logger.info("Mean 0:"+gmm.getComponent(0).getMean(0)+
		 * " cov:"+gmm.getComponent(0).getCovariance(0, 0)); //logger.info("Mean 1:"+gmm.getComponent(1).getMean(0)+ " cov:"+gmm.getComponent(1).getCovariance(0, 0)); gaussian = gmm.getComponent(0); if (gmm.getComponent(0).getMean(0) <
		 * gmm.getComponent(1).getMean(0)) { gaussian = gmm.getComponent(1); } double thr = gaussian.getMean(0) - Math.sqrt(gaussian.getCovariance(0, 0) * EThr); logger.info("cluster : " + cluster.getName() + " thr = " + thr); Cluster clusterResult =
		 * clusterSetResult.createANewCluster(name); for (Segment segTmp : cluster) { featureSet.setCurrentShow(segTmp.getShowName()); int start = segTmp.getStart(); int endSegment = start + segTmp.getLength(); int end = Math.min(endSegment,
		 * featureSet.getNumberOfFeatures()); ArrayList<Boolean> speechFeatureList = segTmp.getSpeechFeatureList(); speechFeatureList.clear(); logger.finer("speech detector cluster:" + cluster.getName() + " start:" + segTmp.getStart() + " len:" +
		 * segTmp.getLength() + " last:" + segTmp.getLast()); for (int i = start; i < end; i++) { double value = featureSet.getFeature(i)[featureSet.getIndexOfEnergy()]; if (value > thr) { Segment seg = (Segment) (segTmp.clone()); seg.setStart(i);
		 * seg.setLength(1); clusterResult.addSegment(seg); speechFeatureList.add(true); } else { speechFeatureList.add(false); } } } clusterResult.collapse(); } parameter.getParameterInputFeature().setSpeechThreshold(EThr);
		 * parameter.getParameterInputFeature().setFeaturesDescription(FeatureFormat); return clusterSetResult;
		 */
		return null;
	}

	/**
	 * Energy threshold method.
	 * 
	 * @param featureSet the feature set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected static void EnergyThresholdMethod(AudioFeatureSet featureSet) throws DiarizationException, IOException {
		// clusters result
		ClusterSet clusterSet = featureSet.clusterSet;
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			double thr = getEnergyThreshold(cluster, featureSet);
			for (Segment segment : cluster) {
				if (featureSet.getCurrentShowName().compareTo(segment.getShowName()) == 0) {
					int start = segment.getStart();
					int endSegment = start + segment.getLength();
					int end = Math.min(endSegment, featureSet.getNumberOfFeatures());
					ArrayList<Boolean> speechFeatureList = segment.getSpeechFeatureList();
					speechFeatureList.clear();
					int nbFreaturesRemoved = 0;
					for (int i = start; i < end; i++) {
						double value = featureSet.getFeatureUnsafe(i)[featureSet.getIndexOfEnergy()];
						if (value > thr) {
							speechFeatureList.add(true);
						} else {
							speechFeatureList.add(false);
							nbFreaturesRemoved++;
						}
					}
					if (SpkDiarizationLogger.DEBUG) logger.finer("speech detector cluster:" + cluster.getName() + " start:" + segment.getStart()
							+ " len:" + segment.getLength() + " last:" + segment.getLast() + " remove: "
							+ nbFreaturesRemoved + " thr:" + featureSet.speechThreshold);
				}
			}
		}
	}

	/**
	 * Speech detection.
	 * 
	 * @param featureSet the feature set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void speechDetection(AudioFeatureSet featureSet) throws DiarizationException, IOException {
		if (SpkDiarizationLogger.DEBUG) logger.info("Speech detection");
		if (featureSet.speechMethod == SpeechDetectorMethod.SPEECH_ON_ENERGY) {
			EnergyThresholdMethod(featureSet);
		} else if (featureSet.speechMethod == SpeechDetectorMethod.SPEECH_ON_BIGAUSSIAN) {
			BiGaussienThresholdedMethod(featureSet);
		}
	}

	/**
	 * Gets the energy threshold.
	 * 
	 * @param cluster the cluster
	 * @param featureSet the feature set
	 * @return the energy threshold
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static double getEnergyThreshold(Cluster cluster, AudioFeatureSet featureSet) throws DiarizationException, IOException {
		ArrayList<Double> energy = new ArrayList<Double>(0);
		// get energy in a vector
		for (Segment segment : cluster) {
			if (featureSet.getCurrentShowName().compareTo(segment.getShowName()) == 0) {
				//logger.finest("index of energy:" + featureSet.currentFileDesc.getIndexOfEnergy());
				int start = segment.getStart();
				int endSegment = start + segment.getLength();
				for (int i = start; i < endSegment; i++) {
					double value = featureSet.getFeatureUnsafe(i)[featureSet.currentFileDesc.getIndexOfEnergy()];
					energy.add(value);
				}
			}
		}

		if (energy.size() <= 0) {
			return -Double.MAX_VALUE;
		}
		// sort the energy
		Collections.sort(energy);
		// get thershold
		int indexOfThreshold = -1;
		if (featureSet.speechThreshold > 0.0) {
			indexOfThreshold = (int) Math.round(featureSet.speechThreshold * energy.size());
		}

		return energy.get(indexOfThreshold);
	}

}
