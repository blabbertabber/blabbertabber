package fr.lium.spkDiarization.programs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import www.spatial.maine.edu.assignment.HungarianAlgorithm;
import fr.lium.experimental.spkDiarization.libClusteringData.speakerName.SpeakerName;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;
import fr.lium.spkDiarization.lib.CityBlockPair;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.lib.StringListFileIO;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.MAPScoreNormalization;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterScore;

/**
 * The Class Identification.
 */
public class Identification {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Identification.class.getName());

	/** The cluster and model scores. */
	static double clusterAndModelScores[][];

	/** The list model. */
	static ArrayList<String> listModel;

	/**
	 * Prints the score.
	 * 
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 */
	@SuppressWarnings("unused")
	private static void printScore(ClusterSet clusterSet, GMMArrayList gmmList) {
		int clusterIndex = 0;
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				if (clusterAndModelScores[clusterIndex][modelIndex] > Double.NEGATIVE_INFINITY) {
					logger.info(cluster.getName() + " -- " + gmmList.get(modelIndex).getName() + "="
							+ clusterAndModelScores[clusterIndex][modelIndex]);
				}
			}
			clusterIndex++;
		}
	}

	/**
	 * Z normalization.
	 * 
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 * @throws DiarizationException the diarization exception
	 */
	private static void ZNormalization(ClusterSet clusterSet, GMMArrayList gmmList) throws DiarizationException {
		for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
			GMM normalization = new GMM(clusterSet.clusterGetSize(), 1);
			normalization.statistic_initialize();
			int clusterIndex = 0;
			float value[] = new float[1];
			for (@SuppressWarnings("unused")
			Cluster cluster : clusterSet.clusterSetValue()) {
				int j = 0;
				for (Cluster cluster2 : clusterSet.clusterSetValue()) {
					if ((j != clusterIndex) && (clusterAndModelScores[j][modelIndex] > Double.NEGATIVE_INFINITY)) {
						value[0] = (float) clusterAndModelScores[j][modelIndex];
						int cluster2Length = cluster2.getLength();
						// logger.info("value:"+value[0]+" len="+cluster2Length);
						normalization.getComponent(clusterIndex).statistic_addFeature(value, cluster2Length);
						// normalization.getComponent(clusterIndex).addFeature(value);
					}
					j++;
				}
				normalization.getComponent(clusterIndex).setModel();
				clusterIndex++;
			}
			clusterIndex = 0;
			for (@SuppressWarnings("unused")
			Cluster cluster : clusterSet.clusterSetValue()) {
				double mean = normalization.getComponent(clusterIndex).getMean(0);
				double std = Math.sqrt(normalization.getComponent(clusterIndex).getCovariance(0, 0));
				double unorm = clusterAndModelScores[clusterIndex][modelIndex];
				clusterAndModelScores[clusterIndex][modelIndex] = (clusterAndModelScores[clusterIndex][modelIndex] - mean)
						/ std;
				logger.info(gmmList.get(modelIndex).getName() + " --> zmean:" + mean + " zcov:" + std + " score:"
						+ unorm + " --> " + clusterAndModelScores[clusterIndex][modelIndex]);
				clusterIndex++;
			}
		}
	}

	/**
	 * T normalization.
	 * 
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 * @throws DiarizationException the diarization exception
	 */
	private static void TNormalization(ClusterSet clusterSet, GMMArrayList gmmList) throws DiarizationException {
		int clusterIndex = 0;
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			GMM normalization = new GMM(gmmList.size(), 1);
			normalization.statistic_initialize();
			float value[] = new float[1];
			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				for (int modelIndex2 = 0; modelIndex2 < gmmList.size(); modelIndex2++) {
					// logger.info("TNORM add value: "+ clusterAndModelScores[clusterIndex][modelIndex2]+" c="+clusterIndex+" g="+modelIndex2);
					if ((modelIndex != modelIndex2)
							&& (clusterAndModelScores[clusterIndex][modelIndex2] > Double.NEGATIVE_INFINITY)) {
						value[0] = (float) clusterAndModelScores[clusterIndex][modelIndex2];
						normalization.getComponent(modelIndex).statistic_addFeature(value);
					}
				}
				normalization.getComponent(modelIndex).setModel();
			}
			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				double mean = normalization.getComponent(modelIndex).getMean(0);
				double std = Math.sqrt(normalization.getComponent(modelIndex).getCovariance(0, 0));

				clusterAndModelScores[clusterIndex][modelIndex] = (clusterAndModelScores[clusterIndex][modelIndex] - mean)
						/ std;
				/*
				 * logger.info("TNORM "+cluster.getName()+"/"+gmmList.get(modelIndex).getName()+"--> tmean:"+mean+ " tcov:"+std+" score norm:"+clusterAndModelScores[clusterIndex][modelIndex]+ " uunorm:"+s+
				 * " nb:"+normalization.getComponent(modelIndex).getAccumulatorCount());
				 */
			}
			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				double v = clusterAndModelScores[clusterIndex][modelIndex];
				if (v > Double.NEGATIVE_INFINITY) {
					cluster.getInformation().put("TNormScore:" + gmmList.get(modelIndex).getName(), v);
				}
			}
			clusterIndex++;
		}
	}

	/**
	 * City block.
	 * 
	 * @param clusterSet the cluster set
	 * @param nbModel the nb model
	 * @param modelIndex the model index
	 * @return the array list
	 */
	private static ArrayList<Integer> cityBlock(ClusterSet clusterSet, int nbModel, int modelIndex) {

		ArrayList<CityBlockPair> list = new ArrayList<CityBlockPair>(nbModel);
		for (int modelIndex2 = 0; modelIndex2 < nbModel; modelIndex2++) {
			list.set(modelIndex2, new CityBlockPair(modelIndex2, Double.MAX_VALUE));
		}

		for (int modelIndex2 = 0; modelIndex2 < nbModel; modelIndex2++) {
			int clusterIndex = 0;
			boolean first = true;
			for (@SuppressWarnings("unused")
			Cluster cluster : clusterSet.clusterSetValue()) {
				double score = clusterAndModelScores[clusterIndex][modelIndex];
				double score2 = clusterAndModelScores[clusterIndex][modelIndex2];
				double diff = 0.0;
				if ((modelIndex != modelIndex2) && (score > Double.NEGATIVE_INFINITY)) {
					diff = Math.abs(score - score2);
					if (first == true) {
						list.get(modelIndex2).setSecond(diff);
						first = false;
					} else {
						list.get(modelIndex2).setSecond(list.get(modelIndex2).getSecond() + diff);
					}
				}
				clusterIndex++;
			}
		}
		Collections.sort(list);
		ArrayList<Integer> result = new ArrayList<Integer>();
		for (int i = 0; i < 50; i++) {
			if (list.get(i).getSecond() < Double.MAX_VALUE) {
				result.add(list.get(i).getFirst());
			}
		}

		return result;
	}

	/**
	 * AT normalization.
	 * 
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 * @throws DiarizationException the diarization exception
	 */
	@SuppressWarnings("unused")
	private static void ATNormalization(ClusterSet clusterSet, GMMArrayList gmmList) throws DiarizationException {
		int clusterIndex = 0;
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			GMM normalization = new GMM(gmmList.size(), 1);
			normalization.statistic_initialize();
			float value[] = new float[1];

			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				ArrayList<Integer> list = cityBlock(clusterSet, gmmList.size(), modelIndex);

				for (int modelIndex2 : list) {
					if ((modelIndex != modelIndex2)
							&& (clusterAndModelScores[clusterIndex][modelIndex2] > Double.NEGATIVE_INFINITY)) {
						value[0] = (float) clusterAndModelScores[clusterIndex][modelIndex2];
						normalization.getComponent(modelIndex).statistic_addFeature(value);
					}
				}
				normalization.getComponent(modelIndex).setModel();
			}
			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				for (int modelIndex2 = 0; modelIndex2 < gmmList.size(); modelIndex2++) {
					if ((modelIndex != modelIndex2)
							&& (clusterAndModelScores[clusterIndex][modelIndex2] > Double.NEGATIVE_INFINITY)) {
						value[0] = (float) clusterAndModelScores[clusterIndex][modelIndex2];
						normalization.getComponent(modelIndex).statistic_addFeature(value);
					}
				}
				normalization.getComponent(modelIndex).setModel();
			}
			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				double mean = normalization.getComponent(modelIndex).getMean(0);
				double std = Math.sqrt(normalization.getComponent(modelIndex).getCovariance(0, 0));
				// double s = clusterAndModelScores[clusterIndex][modelIndex];
				clusterAndModelScores[clusterIndex][modelIndex] = (clusterAndModelScores[clusterIndex][modelIndex] - mean)
						/ std;
				// logger.info("TNORM "+cluster.getName()+"/"+gmmList.get(modelIndex).getName()+"--> tmean:"+mean+
				// " tcov:"+std+" score norm:"+clusterAndModelScores[clusterIndex][modelIndex]+ " uunorm:"+s+
				// " nb:"+normalization.getComponent(modelIndex).getAccumulatorCount());
			}
			clusterIndex++;
		}
	}

	/**
	 * Map normalization.
	 * 
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 * @param parameterScore the parameter score
	 * @return the cluster set
	 */
	protected static ClusterSet mapNormalization(ClusterSet clusterSet, GMMArrayList gmmList, ParameterScore parameterScore) {
		double meanTarget = parameterScore.getMeanTarget();
		double meanNonTarget = parameterScore.getMeanNonTarget();
		double stdTarget = parameterScore.getStdTarget();
		double stdNonTarget = parameterScore.getStdNonTarget();
		double proba = parameterScore.getProbabilityAPrioriTarget();

		MAPScoreNormalization normalize = new MAPScoreNormalization(meanTarget, stdTarget, proba, meanNonTarget, stdNonTarget);
		int clusterIndex = 0;
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				if (clusterAndModelScores[clusterIndex][modelIndex] > Double.NEGATIVE_INFINITY) {
					Double score1 = clusterAndModelScores[clusterIndex][modelIndex];
					clusterAndModelScores[clusterIndex][modelIndex] = normalize.normalize(clusterAndModelScores[clusterIndex][modelIndex]);
					Double score2 = clusterAndModelScores[clusterIndex][modelIndex];
					logger.info("MAP before Norm: " + score1 + " after Norm:" + score2);

				}
			}

			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				double v = clusterAndModelScores[clusterIndex][modelIndex];
				if (v > Double.NEGATIVE_INFINITY) {
					cluster.getInformation().put("MAPscore:" + gmmList.get(modelIndex).getName(), v);
				}
			}

			double maxScore = Double.NEGATIVE_INFINITY;
			int idxMaxScore = -1;
			for (int i = 1; i < gmmList.size(); i++) {
				double s = clusterAndModelScores[clusterIndex][i];
				if (maxScore < s) {
					maxScore = s;
					idxMaxScore = i;
				}
			}
			if (maxScore > parameterScore.getScoreThreshold()) {
				String newName = gmmList.get(idxMaxScore).getName();
				logger.info("IDENT cluster:" + cluster.getName() + " GMM: " + newName + " add score: " + maxScore
						+ "Thr:" + parameterScore.getScoreThreshold());
				addScore(cluster, newName, maxScore);
				if (parameterScore.getLabel() == ParameterScore.LabelType.LABEL_TYPE_ADD.ordinal()) {
					newName += "_";
					newName += gmmList.get(idxMaxScore).getName();
				} else {
					newName = gmmList.get(idxMaxScore).getName();
				}
				cluster.setName(newName);

			}
			clusterIndex++;
		}
		return clusterSet;

	}

	/**
	 * Make.
	 * 
	 * @param featureSet the features
	 * @param clusterSet the clusters
	 * @param gmmList the gmm vector
	 * @param gmmTopList the gmm tops
	 * @param parameter the param
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmList, GMMArrayList gmmTopList, Parameter parameter) throws Exception {
		logger.info("Compute Score");
		int size = gmmList.size();
		logger.finer("GMM size:" + size);

		clusterAndModelScores = new double[clusterSet.clusterSetValue().size()][gmmList.size()];

		int clusterIndex = 0;
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			String gender = cluster.getGender();
			double[] sumScoreVector = new double[size];
			int[] sumLenghtVector = new int[size];
			double ubmSumScore = 0.0;
			int ubmSumLen = 0;
			GMM gmmTop = null;
			if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
				gmmTop = gmmTopList.get(0);
			}
			Arrays.fill(sumScoreVector, 0.0);
			Arrays.fill(sumLenghtVector, 0);

			for (Segment currentSegment : cluster) {
				double[] scoreVector = new double[size];
				double maxScore = 0.0;
				gmmList.accumulateLikelihood(featureSet, currentSegment, gmmTop, parameter.getParameterTopGaussian(), gender);

				if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
					gmmTop.score_getMeanLog();
					ubmSumScore += gmmTop.score_getSumLog();
					ubmSumLen += gmmTop.score_getCount();
					gmmTop.score_reset();
				}
				for (int i = 0; i < size; i++) {
					GMM gmm = gmmList.get(i);
					scoreVector[i] = gmm.score_getMeanLog();
					sumLenghtVector[i] += gmm.score_getCount();
					sumScoreVector[i] += gmm.score_getSumLog();
					if (i == 0) {
						maxScore = scoreVector[0];
					} else {
						double value = scoreVector[i];
						if (maxScore < value) {
							maxScore = value;
						}
					}
					gmm.score_reset();
				}

			}

			if (gmmTop != null) {
				gmmTop.score_getMeanLog();

				ubmSumScore += gmmTop.score_getSumLog();
				ubmSumLen += gmmTop.score_getCount();

				gmmTop.score_reset();
			}

			for (int i = 0; i < size; i++) {
				logger.info("score brute model " + i + ": " + sumScoreVector[i] + " len: " + sumLenghtVector[i]);
				sumScoreVector[i] /= sumLenghtVector[i];
				if (parameter.getParameterScore().isLLRatio()) {
					sumScoreVector[i] -= (ubmSumScore / ubmSumLen);
				}
				clusterAndModelScores[clusterIndex][i] = sumScoreVector[i];
			}

			/*
			 * for(int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) { double v = Double.NEGATIVE_INFINITY; int counter = 0; //logger.info("idx="+modelIndex+ " count="+gmmList.get(modelIndex).getCountLogLikelihood()+
			 * " llk="+gmmList.get(modelIndex).getMeanLogLikelihood()); if (gmmList.get(modelIndex).getCountLogLikelihood() > 0) { v = gmmList.get(modelIndex).getSumLogLikelihood(); counter = gmmList.get(modelIndex).getCountLogLikelihood(); v = v /
			 * counter; if (parameter.getParameterScore().isLLRatio()) { v -= ubmScore; } } clusterAndModelScores[clusterIndex][modelIndex] = v; }
			 */

			cluster.getInformation().put("score:length", cluster.getLength());
			if (parameter.getParameterScore().isLLRatio()) {
				// cluster.getInformation().put("score:UBM", 0.00);
			} else {
				// cluster.getInformation().put("score:UBM", ubmSumScore / ubmSumLen);
			}

			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				double v = clusterAndModelScores[clusterIndex][modelIndex];
				logger.info("score " + gmmList.get(modelIndex).getName() + ": " + v);
				if (v > Double.NEGATIVE_INFINITY) {
					cluster.getInformation().put("score:" + gmmList.get(modelIndex).getName(), v);
				}
			}

			gmmList.resetScoreAccumulator();
			clusterIndex++;
		}
		// logger.info("--------------------------");
		// logger.info("--No NORM-----------------");
		// printScore(clusterSet, gmmList);

		if (parameter.getParameterScore().isTNorm()) {
			// logger.info("--------------------------");
			// logger.info("--TNORM-------------------");
			TNormalization(clusterSet, gmmList);
			// printScore(clusterSet, gmmList);
		}

		if (parameter.getParameterScore().isZNorm()) {
			// logger.info("--------------------------");
			// logger.info("--ZNORM-------------------");
			ZNormalization(clusterSet, gmmList);
			// printScore(clusterSet, gmmList);
		}

		if (parameter.getParameterScore().isMapNorm() == true) {
			// logger.info("--------------------------");
			// logger.info("--MAP---------------------");
			clusterSet = mapNormalization(clusterSet, gmmList, parameter.getParameterScore());
			// printScore(clusterSet, gmmList);
		}

// return clusterSet;
		return selectionMax(clusterSet, gmmList, parameter.getParameterScore());
// return selectionHungarian(clusterSet, gmmList, parameter.parameterScore);
	}

	/**
	 * Selection hungarian.
	 * 
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 * @param parameterScore the parameter score
	 * @return the cluster set
	 */
	@SuppressWarnings("unused")
	private static ClusterSet selectionHungarian(ClusterSet clusterSet, GMMArrayList gmmList, ParameterScore parameterScore) {
		boolean transposed = false;
		// ClusterSet clusterSetResult = (ClusterSet) clusterSet.clone();
		ClusterSet clusterSetResult = clusterSet;

		if ((clusterAndModelScores.length > 0) && (clusterAndModelScores.length > clusterAndModelScores[0].length)) {
			logger.finest("Array transposed (because rows>columns)."); // Cols must be >= Rows.
			clusterAndModelScores = HungarianAlgorithm.transpose(clusterAndModelScores);
			transposed = true;
		}

		ArrayList<Cluster> clusterList = clusterSetResult.getClusterVectorRepresentation();

		if (clusterAndModelScores.length > 0) {
			String sumType = "max";
			int[][] assignment = new int[clusterAndModelScores.length][2];
			assignment = HungarianAlgorithm.hgAlgorithm(clusterAndModelScores, sumType); // Call Hungarian algorithm.
			// double sum = 0;
			for (int[] element : assignment) {
				// <COMMENT> to avoid printing the elements that make up the assignment
				int idxCluster = -1;
				int idxGMM = -1;
				if (!transposed) {
					idxCluster = element[0];
					idxGMM = element[1];
				} else {
					idxCluster = element[1];
					idxGMM = element[0];
				}
				Cluster cluster = clusterList.get(idxCluster);
				String newName = gmmList.get(idxGMM).getName();

				logger.info(String.format("array(%d,%s %s=>%d,%s %s) = %.2f ", idxCluster, cluster.getName(), cluster.getGender(), idxGMM, newName, gmmList.get(idxGMM).getGender(), clusterAndModelScores[element[0]][element[1]]).toString());
				// sum = sum + clusterAndModelScores[assignment[i][0]][assignment[i][1]];

				if (clusterAndModelScores[element[0]][element[1]] > parameterScore.getScoreThreshold()) {
// addScore(cluster, newName, clusterAndModelScores[assignment[i][0]][assignment[i][1]]);
					if (parameterScore.getLabel() == ParameterScore.LabelType.LABEL_TYPE_ADD.ordinal()) {
						cluster.setName(cluster.getName() + "#_#" + newName);
					} else if (parameterScore.getLabel() == ParameterScore.LabelType.LABEL_TYPE_REPLACE.ordinal()) {
						cluster.setName(newName);
					}
				}
			}
		}
		return clusterSetResult;
	}

	/**
	 * Adds the score.
	 * 
	 * @param cluster the cluster
	 * @param GMMName the gMM name
	 * @param value the value
	 */
	private static void addScore(Cluster cluster, String GMMName, double value) {

		String name = SpeakerNameUtils.normalizeSpeakerName(GMMName.split("-")[0].toLowerCase());
		SpeakerName speakerName = cluster.getSpeakerName(name);

		// Keeping the old way (just summing the score)
		speakerName.incrementScoreCluster(value);

		// Adding the new way, will keep trace of each score
		speakerName.addScoreCluster(value);

		logger.info("ADDIDENT name: " + name + " score:" + value + " gmm:" + GMMName + " cluster:" + cluster.getName());
	}

	/**
	 * Checks if is target model.
	 * 
	 * @param name the name
	 * @return true, if is target model
	 */
	private static boolean isTargetModel(String name) {
		// if (name.equals("martin_luther_king")) return false;

		if (listModel.size() == 0) {
			return true;
		}
		return listModel.contains(name);
	}

	/**
	 * Selection max.
	 * 
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 * @param parameterScore the parameter score
	 * @return the cluster set
	 */
	private static ClusterSet selectionMax(ClusterSet clusterSet, GMMArrayList gmmList, ParameterScore parameterScore) {
		// ClusterSet clusterSetResult = (ClusterSet) clusterSet.clone();
		ClusterSet clusterSetResult = clusterSet;
		int clusterIndex = 0;
		for (Cluster cluster : clusterSetResult.clusterSetValue()) {
			int indexMax = -1;
			double max = -Double.MAX_VALUE;
			for (int modelIndex = 0; modelIndex < gmmList.size(); modelIndex++) {
				if (clusterAndModelScores[clusterIndex][modelIndex] > max) {

					if (isTargetModel(gmmList.get(modelIndex).getName()) == true) {
						max = clusterAndModelScores[clusterIndex][modelIndex];
						indexMax = modelIndex;
					}
				}
			}
			String newName = cluster.getName();
			if (max > parameterScore.getScoreThreshold()) {
				if (parameterScore.getLabel() == ParameterScore.LabelType.LABEL_TYPE_ADD.ordinal()) {
					newName += "#_#";
					newName += gmmList.get(indexMax).getName();
				} else if (parameterScore.getLabel() == ParameterScore.LabelType.LABEL_TYPE_REPLACE.ordinal()) {
					newName = gmmList.get(indexMax).getName();
				}
				logger.finer("cluster name=" + cluster.getName() + " new_name=" + newName);

				cluster.getInformation().put("max", gmmList.get(indexMax).getName() + "=" + String.format("%10f", max));
				addScore(cluster, newName, max);
				cluster.setName(newName);
				clusterIndex++;
			}
		}
		return clusterSetResult;
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "Identification");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
				// Top Gaussian model
				GMMArrayList gmmTopGaussianList = MainTools.readGMMForTopGaussian(parameter, featureSet);

				// Compute Model
				GMMArrayList gmmList = MainTools.readGMMContainer(parameter);
				// gmmList.debug(1);
				if (parameter.getParameterScore().getModelList().isEmpty()) {
					listModel = new ArrayList<String>();
				} else {
					listModel = StringListFileIO.read(parameter.getParameterScore().getModelList(), false);
				}

				ClusterSet clusterSetResult = make(featureSet, clusterSet, gmmList, gmmTopGaussianList, parameter);

				// Seg outPut
				MainTools.writeClusterSet(parameter, clusterSetResult, false);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error \t exception ", e);
			e.printStackTrace();
		}

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
			logger.config("info[program] \t name = " + program);
			parameter.getSeparator();
			parameter.logShow();

			parameter.getParameterInputFeature().logAll(); // fInMask
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			parameter.getParameterTopGaussian().logTopGaussian(); // sTop
			parameter.getParameterScore().logAll();

			logger.config(parameter.getSeparator());
		}
	}

}
