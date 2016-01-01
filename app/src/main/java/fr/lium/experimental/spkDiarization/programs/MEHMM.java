package fr.lium.experimental.spkDiarization.programs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterEHMM;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.programs.MTrainMAP;

/**
 * The Class MEHMM.
 */
public class MEHMM {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MEHMM.class.getName());

	/** The Constant likelihoodWindowSize. */
	static final int likelihoodWindowSize = 300;

	/** The maximum segment. */
	static Segment maximumSegment;

	/** The maximum feature index. */
	static int maximumFeatureIndex;

	/**
	 * Train map speakers.
	 * 
	 * @param features the features
	 * @param clusters the clusters
	 * @param speakersList the speakers list
	 * @param param the param
	 * @throws Exception the exception
	 */
	public static void trainMAPSpeakers(AudioFeatureSet features, ClusterSet clusters, GMMArrayList speakersList, Parameter param) throws Exception {
		GMMArrayList initVect = new GMMArrayList();
		speakersList.clear();
		MTrainInit.make(features, clusters, initVect, param);
		MTrainMAP.make(features, clusters, initVect, speakersList, param, true);
	}

	/**
	 * Make n spk.
	 * 
	 * @param features the features
	 * @param clusters the clusters
	 * @param ubm the ubm
	 * @param param the param
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet makeNSpk(AudioFeatureSet features, ClusterSet clusters, GMM ubm, Parameter param) throws Exception {
		/*
		 * Integer nbOfSpeaker = 0; GMMArrayList speakersList = new GMMArrayList(); ClusterSet current = makeInitialClustering(clusters, nbOfSpeaker); trainSpeakers(features, current, speakersList, param); ClusterSet previous = new ClusterSet();
		 * while(){ getMaximumWindowzedLikelihood(features, clusters.getCluster("S0"), ubm); setNewSpeaker(features, current, ubm, "S"+nbOfSpeaker); nbOfSpeaker++; while(current.equals(previous) == false) { previous = current; trainSpeakers(features,
		 * previous, speakersList, param); current = MDecode.make(features, previous, speakersList, param); } } return current;
		 */
		return clusters;
	}

	/**
	 * Make2 spk.
	 * 
	 * @param features the features
	 * @param clusters the clusters
	 * @param ubm the ubm
	 * @param param the param
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make2Spk(AudioFeatureSet features, ClusterSet clusters, GMM ubm, Parameter param) throws Exception {
		Integer nbOfSpeaker = 0;
		GMMArrayList speakersList = new GMMArrayList();
		ClusterSet current = makeInitialClustering(clusters, nbOfSpeaker);

		trainMAPSpeakers(features, current, speakersList, param);

		GMM gmmS0 = speakersList.get(0);

		ClusterSet previous = new ClusterSet();

		getMaximumWindowzedLikelihood(features, current.getCluster("S" + nbOfSpeaker), ubm, gmmS0);
		nbOfSpeaker++;
		setNewSpeaker(features, current, ubm, "S" + nbOfSpeaker);

		while (current.equals(previous) == false) {
			previous = current;
			trainMAPSpeakers(features, previous, speakersList, param);
			current = MDecode.make(features, previous, speakersList, param);
		}

		return current;
	}

	/**
	 * Make re seg.
	 * 
	 * @param features the features
	 * @param clusters the clusters
	 * @param ubm the ubm
	 * @param param the param
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet makeReSeg(AudioFeatureSet features, ClusterSet clusters, GMM ubm, Parameter param) throws Exception {
		GMMArrayList speakersList = new GMMArrayList();
		ClusterSet current = clusters;

		trainMAPSpeakers(features, current, speakersList, param);

		ClusterSet previous = new ClusterSet();

		while (current.equals(previous) == false) {
			previous = current;
			trainMAPSpeakers(features, previous, speakersList, param);
			current = MDecode.make(features, previous, speakersList, param);
		}

		return current;
	}

	/**
	 * Make initial clustering.
	 * 
	 * @param clusters the clusters
	 * @param nbOfSpeaker the nb of speaker
	 * @return the cluster set
	 */
	public static ClusterSet makeInitialClustering(ClusterSet clusters, Integer nbOfSpeaker) {
		ClusterSet initialClusters = new ClusterSet();
		Cluster initalCluster = initialClusters.createANewCluster("S" + nbOfSpeaker);
		nbOfSpeaker++;
		for (Cluster cluster : clusters.clusterSetValue()) {
			for (Segment segment : cluster) {
				Segment initalSegment = segment.clone();
				initalCluster.addSegment(initalSegment);
			}
		}
		return initialClusters;
	}

	/**
	 * Gets the maximum windowzed likelihood.
	 * 
	 * @param features the features
	 * @param cluster the cluster
	 * @param ubm the ubm
	 * @param gmmS0 the gmm s0
	 * @return the maximum windowzed likelihood
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static int getMaximumWindowzedLikelihood(AudioFeatureSet features, Cluster cluster, GMM ubm, GMM gmmS0) throws DiarizationException, IOException {
		Double maximumLogLikelihood = -1.0 * Double.MAX_VALUE;
		maximumFeatureIndex = -1;
		maximumSegment = null;
		for (Segment segment : cluster) {
			features.setCurrentShow(segment.getShowName());
			int start = segment.getStart();
			int last = (segment.getLast() - likelihoodWindowSize) + 1;
			for (int i = start; i <= last; i++) {
				ubm.score_initialize();
				gmmS0.score_initialize();
				for (int j = 0; j < likelihoodWindowSize; j++) {
					ubm.score_getAndAccumulate(features, i + j);
					gmmS0.score_getAndAccumulate(features, i + j);

				}
				double ubmLlh = ubm.score_getMeanLog();
				double gmmS0Llh = gmmS0.score_getMeanLog();
				double llr = gmmS0Llh - ubmLlh;
				ubm.score_reset();
				gmmS0.score_reset();
				if (llr > maximumLogLikelihood) {
					logger.finer("index = " + i + " llr = " + llr);
					maximumLogLikelihood = llr;
					maximumFeatureIndex = i;
					maximumSegment = segment;
				}
			}
		}
		return maximumFeatureIndex;
	}

	/**
	 * Sets the new speaker.
	 * 
	 * @param features the features
	 * @param clusters the clusters
	 * @param ubm the ubm
	 * @param name the name
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void setNewSpeaker(AudioFeatureSet features, ClusterSet clusters, GMM ubm, String name) throws DiarizationException, IOException {
		Cluster clusterS0 = clusters.getCluster(maximumSegment.getClusterName());
		clusterS0.removeSegment(maximumSegment);

		Cluster clusterS1 = clusters.createANewCluster(name);

		if (maximumFeatureIndex != maximumSegment.getStart()) {
			Segment begin = maximumSegment.clone();
			begin.setLength(maximumFeatureIndex - begin.getStart());
			clusterS0.addSegment(begin);
		}

		Segment mid = maximumSegment.clone();
		mid.setStart(maximumFeatureIndex);
		mid.setLength(likelihoodWindowSize);
		clusterS1.addSegment(mid);

		if ((maximumFeatureIndex + likelihoodWindowSize) < maximumSegment.getLast()) {
			Segment end = maximumSegment.clone();
			end.setStart(maximumFeatureIndex + likelihoodWindowSize);
			end.setLength(maximumSegment.getLast() - end.getStart());
			clusterS0.addSegment(end);
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			Parameter param = MainTools.getParameters(args);
			info(param, "MEHMM");
			if (param.show.isEmpty() == false) {
				// Clusters
				ClusterSet clusters = MainTools.readClusterSet(param);
				// clusters.debug();

				// Features
				AudioFeatureSet features = MainTools.readFeatureSet(param, clusters);

				// Compute Model
				GMMArrayList initVect = MainTools.readGMMContainer(param);

				ClusterSet clustersRes = null;
				if (param.getParameterEHMM().getTypeEHMM() == ParameterEHMM.TypeEHMMList.ReSeg.ordinal()) {
					clustersRes = makeReSeg(features, clusters, initVect.get(0), param);
				} else if (param.getParameterEHMM().getTypeEHMM() == ParameterEHMM.TypeEHMMList.twoSpk.ordinal()) {
					clustersRes = make2Spk(features, clusters, initVect.get(0), param);
				} else {
					throw new DiarizationException("EHMM not implemented");
				}

				// Seg outPut
				MainTools.writeClusterSet(param, clustersRes, false);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "exception", e);
			e.printStackTrace();
		}
	}

	/**
	 * Info.
	 * 
	 * @param param the param
	 * @param prog the prog
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter param, String prog) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (param.help) {
			logger.config(param.getSeparator2());
			logger.config("Program name = " + prog);
			logger.config(param.getSeparator());
			param.logShow();

			param.getParameterInputFeature().logAll();
			logger.config(param.getSeparator());
			param.getParameterSegmentationInputFile().logAll();
			param.getParameterSegmentationOutputFile().logAll();
			logger.config(param.getSeparator());
			param.getParameterModelSetInputFile().logAll(); // tInMask
			param.getParameterTopGaussian().logTopGaussian(); // sTop
			logger.config(param.getSeparator());
			param.getParameterDecoder().logAll();
			logger.config(param.getSeparator());
			param.getParameterEM().logAll(); // emCtl
			param.getParameterMAP().logAll(); // mapCtrl
			param.getParameterVarianceControl().logAll(); // varCtrl
			logger.config(param.getSeparator());
			param.getParameterEHMM().logAll();
		}
	}

}
