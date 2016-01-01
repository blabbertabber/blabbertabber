package fr.lium.experimental.spkDiarization.programs;

import java.io.IOException;
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
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.GMMFactory;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterInitializationEM.ModelInitializeMethod;
import fr.lium.spkDiarization.programs.MClust;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MTrainEM;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.programs.MTrainMAP;

/**
 * The Class ReSegmentation.
 */
public class ReSegmentation {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ReSegmentation.class.getName());

	/**
	 * Purify cluster set.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmTopGaussianList the gmm top gaussian list
	 * @param parameter the parameter
	 * @param speakersList the speakers list
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected static void purifyClusterSet(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmTopGaussianList, Parameter parameter, GMMArrayList speakersList) throws DiarizationException, IOException {
		int size = speakersList.size();
		GMM gmmTop = gmmTopGaussianList.get(0);
		logger.info("score segment");
		ArrayList<Integer> listOfBestMatch = new ArrayList<Integer>(size);
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			int currentIndex = -1;
			GMM currentGMM = null;
			for (int j = 0; j < size; j++) {
				listOfBestMatch.add(0);
			}
			for (int j = 0; j < size; j++) {
				GMM gmm = speakersList.get(j);
				if (cluster.getName().equals(gmm.getName()) == true) {
					currentGMM = gmm;
					currentIndex = j;
					break;
				}
			}
			GMM saveCurrentGMM = currentGMM.clone();
			for (Segment segment : cluster) {
				featureSet.setCurrentShow(segment.getShowName());
				String localName = new String("local" + currentGMM.getName() + "-" + segment.getStart());
				logger.finest("--> new gmm: " + localName);

				Cluster localCluster = new Cluster(localName);
				localCluster.addSegment(segment);
				GMM localInitGMM = gmmTop.clone();
				localInitGMM.setName(localName);
				GMM localGMM = GMMFactory.getMAP(localCluster, featureSet, localInitGMM, gmmTop, parameter.getParameterEM(), parameter.getParameterMAP(), parameter.getParameterVarianceControl(), parameter.getParameterInputFeature().useSpeechDetection(), false);

				logger.finest("count: " + currentGMM.getComponent(0).getCount() + " local:"
						+ localGMM.getComponent(0).getCount() + " int:" + localInitGMM.getComponent(0).getCount());

				for (int k = 0; k < currentGMM.getNbOfComponents(); k++) {
					// localGMM.getComponent(k).debug(9);
					currentGMM.getComponent(k).statistic_remove(localGMM.getComponent(k), 1);
				}
				logger.finest("count: " + currentGMM.getComponent(0).getCount());

				currentGMM.setAdaptedModel(gmmTop, parameter.getParameterMAP());
				speakersList.set(currentIndex, currentGMM);
				for (int i = 0; i < size; i++) {
					speakersList.get(i).score_initialize();
				}
				int end = Math.min(segment.getLast(), featureSet.getNumberOfFeatures() - 1);
				// ArrayList<int[]> topGaussianList = segment.getTopGaussianList();
				// topGaussianList.clear();
				int nTop = 0;
				for (int start = segment.getStart(); start <= end; start++, nTop++) {
					for (int i = 0; i < size; i++) {
						GMM gmm = speakersList.get(i);
						if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
							/*
							 * if (i == 0) { gmmTop.getAndAccumulateLikelihoodAndFindTopComponents(featureSet, start, parameter.getParameterTopGaussian().getScoreNTop()); topGaussianList.add(gmm.getTopGaussianVector()); }
							 * gmm.getAndAccumulateLikelihoodForComponentSubset(featureSet, start, gmmTop.getTopGaussianVector());
							 */
							gmm.score_getAndAccumulateForComponentSubset(featureSet, start, segment.getTopGaussianList().get(nTop));
						} else {
							gmm.score_getAndAccumulate(featureSet, start);
						}
					}
				}
				double max = -Double.MAX_VALUE;
				String maxModel = "";
				int maxId = -1;
				for (int i = 0; i < size; i++) {
					GMM gmm = speakersList.get(i);
					if (gmm.score_getMeanLog() > max) {
						maxId = i;
						max = gmm.score_getMeanLog();
						maxModel = gmm.getName();
					}
				}
				boolean v = true;
				if (cluster.getName().equals(maxModel) == false) {
					logger.finest("--> remove segment : " + maxId);
					listOfBestMatch.set(maxId, listOfBestMatch.get(maxId) + 1);
					v = false;
				}
				int idx = 0;
				for (int start = segment.getStart(); start <= end; start++) {
					segment.setSpeechFeature(idx, v);
					idx++;
				}
				currentGMM = saveCurrentGMM.clone();
				speakersList.set(currentIndex, currentGMM);
			}
			logger.info("--> for " + cluster.getName() + " : ");
			int sum = 0;
			for (int i = 0; i < size; i++) {
				int nb = listOfBestMatch.get(i);
				float r = (float) nb / (float) cluster.segmentsSize();
				if (nb > 0) {

					logger.info("\t\t" + speakersList.get(i).getName() + " this model remove segments : " + nb + " // "
							+ r);
				}
				sum += nb;
			}

			float r = (float) sum / (float) cluster.segmentsSize();
			logger.info("\t\t" + cluster.getName() + " in this model," + sum + " segments are removed (" + r + ")");
			listOfBestMatch.clear();
		}
	}

	/**
	 * Train speaker.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param speakersList the speakers list
	 * @param parameter the parameter
	 * @param remove the remove
	 * @throws Exception the exception
	 */
	protected static void trainSpeaker(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList speakersList, Parameter parameter, boolean remove) throws Exception {
		speakersList.clear();
		// parameter.getParameterEM().setMaximumIteration(1);
		ClusterSet trainCluster = new ClusterSet();
		int min = parameter.getParameterClustering().getMinimumOfClusterLength();
		for (String clusterName : clusterSet) {
			Cluster cluster = clusterSet.getCluster(clusterName);
			if ((cluster.getLength() > min) || (remove == false)) {
				trainCluster.getClusterMap().put(clusterName, cluster);
				logger.info("accept " + clusterName + " = " + cluster.getLength() + " / min: " + min);
			} else {
				logger.info("reject " + clusterName + " = " + cluster.getLength() + " / min: " + min);
			}
		}

		GMMArrayList initVect = new GMMArrayList();
		MTrainInit.make(featureSet, clusterSet, initVect, parameter);
		if (parameter.getParameterInitializationEM().getModelInitMethod() == ModelInitializeMethod.TRAININIT_COPY) {
			MTrainMAP.make(featureSet, clusterSet, initVect, speakersList, parameter, true);
		} else {
			MTrainEM.make(featureSet, clusterSet, initVect, speakersList, parameter);
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
	 * Resegmentation.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmTopGaussianList the gmm top gaussian list
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet resegmentation(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmTopGaussianList, Parameter parameter) throws Exception {
		GMMArrayList speakersList = new GMMArrayList();
// float rate = parameter.getParameterSegmentationInputFile().getRate();
		logger.info("resegmentation : train speakers");

		trainSpeaker(featureSet, clusterSet, speakersList, parameter, false);
		logger.info("resegmentation : purify");

		purifyClusterSet(featureSet, clusterSet, gmmTopGaussianList, parameter, speakersList);

		logger.info("resegmentation : train purified speakers");
		speakersList.clear();
		double saveSpeechThreshold = parameter.getParameterInputFeature().getSpeechThreshold();
		parameter.getParameterInputFeature().setSpeechThreshold(Double.MAX_VALUE);
		trainSpeaker(featureSet, clusterSet, speakersList, parameter, true);
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
	 * Merge cluster.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmTopGaussianList the gmm top gaussian list
	 * @param parameter the parameter
	 * @param nbMerge the nb merge
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	public static boolean mergeCluster(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmTopGaussianList, Parameter parameter, int nbMerge) throws Exception {
		GMMArrayList speakersList = new GMMArrayList();
		double clustThr = parameter.getParameterClustering().getThreshold();
		int nbMaxMerge = parameter.getParameterClustering().getMaximumOfMerge();
		int nbMinClust = parameter.getParameterClustering().getMinimumOfCluster();

		/*
		 * logger.info("mergeCluster : train speakers"); trainSpeaker( featureSet, clusterSet, speakersList, parameter, false); logger.info("mergeCluster : purify"); purifyClusterSet(featureSet, clusterSet, gmmTopGaussianList, parameter,
		 * speakersList);
		 */
		double saveSpeechThreshold = parameter.getParameterInputFeature().getSpeechThreshold();
		parameter.getParameterInputFeature().setSpeechThreshold(-Double.MAX_VALUE);

		logger.info("mergeCluster : train purified speakers");
		trainSpeaker(featureSet, clusterSet, speakersList, parameter, true);

		boolean useTop = parameter.getParameterTopGaussian().isUseTop();
		boolean usedSpeech = parameter.getParameterInputFeature().useSpeechDetection();

		logger.info("resegmentation : merge ");
		double minScore = Double.MAX_VALUE;
		String minClusterNameI = null;
		String minClusterNameJ = null;
		int minJ = 0;
		int minI = 0;
		for (int i = 0; i < (speakersList.size() - 1); i++) {
			GMM gmmI = speakersList.get(i);
			String clusterNameI = gmmI.getName();
			Cluster clusterI = clusterSet.getCluster(clusterNameI);
			for (int j = i + 1; j < speakersList.size(); j++) {
				GMM gmmJ = speakersList.get(j);
				String clusterNameJ = gmmJ.getName();
				Cluster clusterJ = clusterSet.getCluster(clusterNameJ);
				double score = Distance.CE(gmmI, gmmJ, clusterI, clusterJ, featureSet, useTop, usedSpeech);
				logger.info("distance : " + clusterNameI + "/" + clusterNameJ + " score:" + score + " " + i + "/" + j);
				if (score < minScore) {
					minScore = score;
					minClusterNameI = clusterNameI;
					minClusterNameJ = clusterNameJ;
					minJ = j;
					minI = i;
				}
			}
		}
		int nbCluster = clusterSet.clusterGetSize();

		boolean needMerge = MClust.continuClustering(minScore, nbMerge, nbCluster, clusterSet, clustThr, nbMaxMerge, nbMinClust);
		logger.info("---------------------------------");
		logger.info("Merge: " + needMerge + " ( " + minClusterNameI + ", " + minClusterNameJ + ") score:" + minScore
				+ " (" + minI + ", " + minJ + ")");
		if (needMerge == true) {
			clusterSet.mergeCluster(minClusterNameI, minClusterNameJ);
		}
		parameter.getParameterInputFeature().setSpeechThreshold(saveSpeechThreshold);
		return needMerge;
	}

	/**
	 * Sets the top gaussian.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmTopGaussianList the gmm top gaussian list
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private static void setTopGaussian(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmTopGaussianList, Parameter parameter) throws DiarizationException, IOException {
		if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
			GMM gmmTop = gmmTopGaussianList.get(0);
			logger.info("set Top");
			for (Cluster cluster : clusterSet.clusterSetValue()) {
				for (Segment segment : cluster) {
					featureSet.setCurrentShow(segment.getShowName());
					int end = Math.min(segment.getLast(), featureSet.getNumberOfFeatures() - 1);
					ArrayList<int[]> topGaussianList = segment.getTopGaussianList();
					topGaussianList.clear();
					for (int start = segment.getStart(); start <= end; start++) {
						gmmTop.score_getAndAccumulateAndFindTopComponents(featureSet, start, parameter.getParameterTopGaussian().getScoreNTop());
						topGaussianList.add(gmmTop.getTopGaussianVector());
					}
				}
			}
		}
	}

	/**
	 * Make3.
	 * 
	 * @param featureSet the feature set
	 * @param previousClusterSet the previous cluster set
	 * @param gmmTopGaussianList the gmm top gaussian list
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make3(AudioFeatureSet featureSet, ClusterSet previousClusterSet, GMMArrayList gmmTopGaussianList, Parameter parameter) throws Exception {
		setTopGaussian(featureSet, previousClusterSet, gmmTopGaussianList, parameter);

		ClusterSet currentClusterSet = resegmentation(featureSet, previousClusterSet, gmmTopGaussianList, parameter);
		int maxMerge = 100;
		int nb = 0;

		while (!((currentClusterSet.equals(previousClusterSet) == true) || (nb >= maxMerge))) {
			logger.info("--> iteration : " + nb);

			boolean mergeDone = mergeCluster(featureSet, currentClusterSet, gmmTopGaussianList, parameter, nb);
			if (mergeDone == false) {
				break;
			}
			previousClusterSet = currentClusterSet;
			currentClusterSet = resegmentation(featureSet, previousClusterSet, gmmTopGaussianList, parameter);
			nb++;
			ClusterSet save = currentClusterSet.clone();
			save.collapse();
			saveClusterSet(save, nb, parameter);
		}
		logger.info("--> fin iteration : " + nb + " equal : " + currentClusterSet.equals(previousClusterSet));
		return currentClusterSet;

	}

	/**
	 * Make2.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmTopGaussianList the gmm top gaussian list
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make2(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmTopGaussianList, Parameter parameter) throws Exception {
		GMMArrayList speakersList = new GMMArrayList();
		GMMArrayList initVect = new GMMArrayList();
		MTrainInit.make(featureSet, clusterSet, initVect, parameter);
		MTrainMAP.make(featureSet, clusterSet, initVect, speakersList, parameter, true);
		int size = speakersList.size();
// float rate = parameter.getParameterSegmentationInputFile().getRate();
		GMM gmmTop = gmmTopGaussianList.get(0);

		logger.info("score segment");
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			for (Segment segment : cluster) {
				featureSet.setCurrentShow(segment.getShowName());

				for (int i = 0; i < size; i++) {
					speakersList.get(i).score_initialize();
				}
				int end = Math.min(segment.getLast(), featureSet.getNumberOfFeatures() - 1);
				for (int start = segment.getStart(); start <= end; start++) {
					for (int i = 0; i < size; i++) {
						GMM gmm = speakersList.get(i);
						if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
							if (i == 0) {
								gmmTop.score_getAndAccumulateAndFindTopComponents(featureSet, start, parameter.getParameterTopGaussian().getScoreNTop());
							}
							gmm.score_getAndAccumulateForComponentSubset(featureSet, start, gmmTop.getTopGaussianVector());
						} else {
							gmm.score_getAndAccumulate(featureSet, start);
						}
					}
				}
				double max = -Double.MAX_VALUE;
				String maxModel = "";
				for (int i = 0; i < size; i++) {
					GMM gmm = speakersList.get(i);
					if (gmm.score_getMeanLog() > max) {
						max = gmm.score_getMeanLog();
						maxModel = gmm.getName();
					}
				}
				boolean v = true;
				if (cluster.getName().equals(maxModel) == false) {
					v = false;
				}
				int idx = 0;
				for (int start = segment.getStart(); start <= end; start++) {
					segment.setSpeechFeature(idx, v);
					idx++;
				}
			}
		}

		logger.info("train speakers");
		// initVect.clear();
		speakersList.clear();
		parameter.getParameterInputFeature().setSpeechThreshold(Double.MAX_VALUE);
		// MTrainInit.make(featureSet, clusterSet, initVect, parameter);
		MTrainMAP.make(featureSet, clusterSet, initVect, speakersList, parameter, true);

		logger.info("train speakers");
		initVect.clear();
		speakersList.clear();
		parameter.getParameterInputFeature().setSpeechThreshold(Double.MAX_VALUE);
		MTrainInit.make(featureSet, clusterSet, initVect, parameter);
		MTrainMAP.make(featureSet, clusterSet, initVect, speakersList, parameter, true);

		logger.info("segment data");
		ClusterSet decodeCluster = clusterSet.clone();
		// decodeCluster.collapse();
		ClusterSet resultClusterSet = MDecode.make(featureSet, decodeCluster, speakersList, parameter);
		return resultClusterSet;

		/*
		 * logger.info("identify segment"); ClusterSet resultClusterSet = new ClusterSet(); for (Cluster cluster : clusterSet.clusterSetValue()) { for (Segment segment : cluster) { featureSet.setCurrentShow(segment.getShowName()); for (int i = 0; i <
		 * size; i++) { speakersList.get(i).initializeScoreAccumulator(); } int end = Math.min(segment.getLast(), featureSet.getNumberOfFeatures() - 1); for (int start = segment.getStart(); start <= end; start++) { for (int i = 0; i < size; i++) {
		 * GMM gmm = speakersList.get(i); if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) { if (i == 0) { gmmTop.getAndAccumulateLikelihoodAndFindTopComponents(featureSet, start, parameter.getParameterTopGaussian().getScoreNTop()); }
		 * gmm.getAndAccumulateLikelihoodForComponentSubset(featureSet, start, gmmTop.getTopGaussianVector()); } else { gmm.getAndAccumulateLikelihood(featureSet, start); } } } double max = -Double.MAX_VALUE; GMM maxModel = null; for (int i = 0; i <
		 * size; i++) { GMM gmm = speakersList.get(i); if (gmm.getMeanLogLikelihood() > max) { max = gmm.getMeanLogLikelihood(); maxModel = gmm; } } Cluster resultCluster = resultClusterSet.getOrCreateANewCluster(maxModel.getName());
		 * resultCluster.setGender(maxModel.getGender()); Segment resultSegment = (Segment) segment.clone(); resultCluster.addSegment(resultSegment); } } //logger.info("segment data"); ClusterSet decodeCluster = (ClusterSet) clusterSet.clone(); while
		 * (decodeCluster.clusterGetSize() > 1) { ArrayList<Cluster> lst = decodeCluster.getClusterVectorRepresentation(); decodeCluster.mergeCluster(lst.get(0).getName(), lst.get(1).getName()); } //decodeCluster.collapse(); ClusterSet
		 * resultClusterSet = MDecode.make(featureSet, decodeCluster, speakersList, parameter); return resultClusterSet;
		 */
	}

	/**
	 * Write cluster set.
	 * 
	 * @param clusterSet the cluster set
	 * @param index the index
	 * @param parameter the parameter
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws DiarizationException the diarization exception
	 * @throws TransformerException the transformer exception
	 */
	public static void writeClusterSet(ClusterSet clusterSet, int index, Parameter parameter) throws IOException, ParserConfigurationException, SAXException, DiarizationException, TransformerException {

		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		String mask2 = mask.replace(".seg", "");

		parameter.getParameterSegmentationOutputFile().setMask(mask2 + "." + index + ".seg");
		logger.info("save: " + parameter.getParameterSegmentationOutputFile().getMask().replace("%s", parameter.show));

		MainTools.writeClusterSet(parameter, clusterSet, true);
		parameter.getParameterSegmentationOutputFile().setMask(mask);
	}

	/**
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmTopGaussianList the gmm top gaussian list
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmTopGaussianList, Parameter parameter) throws Exception {
		ClusterSet current = clusterSet;
		ClusterSet previous = new ClusterSet();
		int nb = 1;
		while (previous.equals(current) == false) {
			previous = current;
			logger.info("Train " + nb);
			GMMArrayList speakersList = new GMMArrayList();
			trainSpeaker(featureSet, previous, speakersList, parameter, (nb == 1));
			logger.info("Decode " + nb);
			current = decode(featureSet, clusterSet, speakersList, parameter);
			writeClusterSet(clusterSet, nb, parameter);
			nb++;
			if (nb > 20) {
				break;
			}
		}
		return current;
	}

	/**
	 * Make4.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmTopGaussianList the gmm top gaussian list
	 * @param parameterGlobal the parameter global
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make4(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmTopGaussianList, Parameter parameterGlobal) throws Exception {
		Parameter parameter = parameterGlobal.clone();

		logger.info("train baseline speakers");
		GMMArrayList speakersList = new GMMArrayList();
		GMMArrayList initVect = new GMMArrayList();
		MTrainInit.make(featureSet, clusterSet, initVect, parameter);
		MTrainMAP.make(featureSet, clusterSet, initVect, speakersList, parameter, true);
		int size = speakersList.size();
		float rate = parameter.getParameterSegmentationInputFile().getRate();
		GMM gmmTop = gmmTopGaussianList.get(0);

		ClusterSet initClusterSet = new ClusterSet();
		logger.info("Cut the segment");
		int d = 100;
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			Cluster initCluster = initClusterSet.createANewCluster(cluster.getName());
			initCluster.setGender(cluster.getGender());
			for (Segment segment : cluster) {
				for (int start = segment.getStart(); start <= (segment.getLast() - d); start += d) {
					Segment shortSegment = new Segment(segment.getShowName(), start, d, initCluster, rate);
					initCluster.addSegment(shortSegment);
				}
			}
		}

		logger.info("score segment");
		for (Cluster cluster : initClusterSet.clusterSetValue()) {
			for (Segment segment : cluster) {
				featureSet.setCurrentShow(segment.getShowName());

				for (int i = 0; i < size; i++) {
					speakersList.get(i).score_initialize();
				}
				int end = Math.min(segment.getLast(), featureSet.getNumberOfFeatures() - 1);
				for (int start = segment.getStart(); start <= end; start++) {
					for (int i = 0; i < size; i++) {
						GMM gmm = speakersList.get(i);
						if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
							if (i == 0) {
								gmmTop.score_getAndAccumulateAndFindTopComponents(featureSet, start, parameter.getParameterTopGaussian().getScoreNTop());
							}
							gmm.score_getAndAccumulateForComponentSubset(featureSet, start, gmmTop.getTopGaussianVector());
						} else {
							gmm.score_getAndAccumulate(featureSet, start);
						}
					}
				}
				double max = -Double.MAX_VALUE;
				String maxModel = "";
				for (int i = 0; i < size; i++) {
					GMM gmm = speakersList.get(i);
					if (gmm.score_getMeanLog() > max) {
						max = gmm.score_getMeanLog();
						maxModel = gmm.getName();
					}
				}
				boolean v = true;
				if (cluster.getName().equals(maxModel) == false) {
					v = false;
				}
				int idx = 0;
				for (int start = segment.getStart(); start <= end; start++) {
					segment.setSpeechFeature(idx, v);
					idx++;
				}
			}
		}

		logger.info("train speakers");
		initVect.clear();
		speakersList.clear();
		parameter.getParameterInputFeature().setSpeechThreshold(Double.MAX_VALUE);
		MTrainInit.make(featureSet, initClusterSet, initVect, parameter);
		MTrainMAP.make(featureSet, initClusterSet, initVect, speakersList, parameter, true);

		logger.info("segment data");
		ClusterSet decodeCluster = clusterSet.clone();
		decodeCluster.collapse();
		ClusterSet resultClusterSet = MDecode.make(featureSet, decodeCluster, speakersList, parameter);
		return resultClusterSet;
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
				// Top Gaussian model
				GMMArrayList gmmTopGaussianList = MainTools.readGMMForTopGaussian(parameter, featureSet);

// ClusterSet resultClusterSet = make3(featureSet, clusterSet, gmmTopGaussianList, parameter);
				ClusterSet resultClusterSet = make(featureSet, clusterSet, gmmTopGaussianList, parameter);

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
			parameter.getParameterDecoder().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterEM().logAll(); // emCtl
			parameter.getParameterMAP().logAll(); // mapCtrl
			parameter.getParameterVarianceControl().logAll(); // varCtrl
			logger.config(parameter.getSeparator());
			if (parameter.getParameterInitializationEM().getModelInitMethod().equals(ModelInitializeMethod.TRAININIT_COPY)) {
				parameter.getParameterModelSetInputFile().logAll(); // tInMask
			} else {
				parameter.getParameterModel().logAll(); // kind
			}
			logger.config(parameter.getSeparator());
			parameter.getParameterDecoder().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterClustering().logAll(); // cThr
		}
	}

}
