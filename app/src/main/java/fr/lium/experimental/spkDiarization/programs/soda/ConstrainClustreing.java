package fr.lium.experimental.spkDiarization.programs.soda;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerName;
import fr.lium.experimental.spkDiarization.programs.SpeakerIdenificationDecision12;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.lib.libDiarizationError.DiarizationError;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libClusteringMethod.BICHClustering;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterModelSetOutputFile;

public class ConstrainClustreing {
	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SpeakerIdenificationDecision12.class.getName());
	/** The parameter. */
	static Parameter parameter;

	
	public static double getScoreOfCandidatesForMergingWithConstraint(BICHClustering clustering) throws DiarizationException {
		
		double score = clustering.getScoreOfCandidatesForMerging();
		Cluster ci = clustering.getFirstCandidate();
		Cluster cj = clustering.getSecondCandidate();
		
		int si = ci.getSpeakerNameSet().size();
		int sj = cj.getSpeakerNameSet().size();
		logger.info("identity size of "+ci.getName()+": "+si+" size of "+cj.getName()+": "+sj);
		
		if ((si == 0) || (sj == 0)) {
			return score;
		} else {
			for(String name: ci.getSpeakerNameSet()) {
				logger.info("test name: "+name);
				if (! cj.getSpeakerNameSet().contains(name)) {
					logger.info("\tname "+name+" not found set to MAX_VALUE");
					clustering.getDistances().set(clustering.getIndexOfFirstCandidate(), clustering.getIndexOfSecondCandidate(), Double.MAX_VALUE);
					logger.info("\tci: "+ci.getName());
					ci.getSpeakerNameSet().debug();
					logger.info("\tcj: "+cj.getName());
					cj.getSpeakerNameSet().debug();
				} else {
					logger.info("\tname "+name+" found - merge ok");
					return score;
				}
			}
		}
		
		return Double.MAX_VALUE;
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
	public static boolean continuClustering(double score, int nbMerge, int nbCluster, ClusterSet clusters, double clustThr, int nbMaxMerge, int nbMinCluster, int nbTest) {

		if (score == Double.MAX_VALUE) {
			logger.finer("\tscore unavailable");
			if (nbTest > 0) return true;
			return false;
		}
		boolean res = ((score < clustThr) && (nbMerge < nbMaxMerge) && (nbCluster > nbMinCluster) && (nbTest > 0));
		//logger.finer("\tstop result = " + res + " true=" + Boolean.TRUE);
		if (nbTest < 0) logger.finer("\t\tnbTest = " + (nbTest >= 0));
		if (score >= clustThr) logger.finer("\t\t thr = " + (score < clustThr));
		if (nbMerge >= nbMaxMerge) logger.finer("\t\t nb merge = " + (nbMerge < nbMaxMerge));
		if (nbCluster < nbMinCluster) logger.finer("\t\t nb cluster = " + (nbCluster > nbMinCluster));
		//logger.finer("\t\t score = " + score + " nbMerge=" + nbMerge + " nbCluster=" + nbCluster);
		//logger.finer("\t\t thr=" + clustThr + " nbMaxMerge=" + nbMaxMerge + " nbMinSpk=" + nbMinCluster);
		return res;
	}

	public static ClusterSet gaussianHAC(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		BICHClustering clustering = new BICHClustering(clusterSet.clone(), featureSet, parameter);
		int nbMerge = 0;
		int nbTest = clusterSet.clusterGetSize() - 1;
		int clustMaxMerge = parameter.getParameterClustering().getMaximumOfMerge();
		int clustMinSpk = parameter.getParameterClustering().getMinimumOfCluster();
		int nbCluster = clusterSet.clusterGetSize();

		clusterSet.debug(0);
		
		double score = 0;
		clustering.initialize(0, 0);

		score = getScoreOfCandidatesForMergingWithConstraint(clustering);
		nbTest--;
		
		while (continuClustering(score, nbMerge, nbCluster, clusterSet, 0.0, clustMaxMerge, clustMinSpk, nbTest) == true) {
			logger.fine("merge = " + nbMerge + " score = " + score + " ci = " + clustering.getIndexOfFirstCandidate()
					+ "(" + clustering.getFirstCandidate().getName() + ")" + " cj = "
					+ clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")");
			if (score != Double.MAX_VALUE) {
				clustering.mergeCandidates();
				nbMerge++;
			}
			score = getScoreOfCandidatesForMergingWithConstraint(clustering);
			nbTest--;
			nbCluster = clustering.getClusterSet().clusterGetSize();
		}
		//clustering.getClusterSet().debug(0);
		if (!parameter.getParameterModelSetOutputFile().getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
			MainTools.writeGMMContainer(parameter, clustering.getGmmList());
		}
		return clustering.getClusterSet();
	}

	public static void putWritingInCluster(ClusterSet audioClusterSet, ClusterSet writtenClusterSet, double thr) {
		//boolean isCloseListCheck = parameter.getParameterNamedSpeaker().isCloseListCheck();
		logger.info("------ Use Writing ------");

		for(String name: writtenClusterSet) {
			for (Segment writingSegment : writtenClusterSet.getCluster(name)) {
				double maxRate = 0;
				Cluster matchCluster = null;
				for (Cluster cluster : audioClusterSet.getClusterVectorRepresentation()) {
					double rate = 0;
					for (Segment segment : cluster) {
						rate += DiarizationError.match(writingSegment, segment);
					}
					rate /= writingSegment.getLength();
					if ((rate >= thr) && (rate >= maxRate)) {
						maxRate = rate;
						matchCluster = cluster;
					}
				}
				if (matchCluster != null) {
					SpeakerName speakerName = matchCluster.getSpeakerName(name);
					speakerName.addScoreCluster(maxRate);
					//speakerName.incrementScoreCluster(maxRate);
					logger.info("found: "+name+" rate: "+maxRate+ " add in "+matchCluster.getName());
				} else {
					logger.info("no found: "+name+" // no cluster");
				}

			}
		}
	}

	public static void renameCluster(ClusterSet clusterSet) {
		for(Cluster cluster: clusterSet.getClusterMap().values()) {
			logger.info("------------------------");
			logger.info("name: "+cluster.getName());
			cluster.getSpeakerNameSet().debug();
			if (cluster.getSpeakerNameSet().size() == 1) {
				String name = cluster.getMaxSpeakerName().getName();
				cluster.setName(name);
			}
		}
	}

	/**
	 * Print the available options.
	 * 
	 * @param parameter is all the parameters
	 * @param program name of this program
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
			parameter.getParameterSegmentationInputFile2().logAll();
			parameter.getParameterSegmentationOutputFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterClustering().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterModelSetOutputFile().logAll(); // model
			parameter.getParameterModel().logAll(); // kind
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
			SpkDiarizationLogger.setup();
			parameter = MainTools.getParameters(args);
			info(parameter, "SpeakerIdenificationDecision");
			if (parameter.show.isEmpty() == false) {

				//audio seg
				ClusterSet audioClusterSet = MainTools.readClusterSet(parameter);
				ClusterSet writtenClusterSet = MainTools.readTheSecondClusterSet(parameter);
				writtenClusterSet.collapse(10);
				writtenClusterSet.debug(2);
				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, audioClusterSet);
				putWritingInCluster(audioClusterSet, writtenClusterSet, 0.8);
				ClusterSet resultClusterSet = gaussianHAC(audioClusterSet, featureSet, parameter);
				renameCluster(resultClusterSet);
				MainTools.writeClusterSet(parameter, resultClusterSet, false);

			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "exception ", e);
			e.printStackTrace();
		}
	}

}
