/**
 * 
 * <p>
 * MScore
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
 *          Scoring program : log-likelihood
 * 
 */

package fr.lium.spkDiarization.programs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterScore;

/**
 * The Class MScore.
 */
public class MScore {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MScore.class.getName());

	/**
	 * Make.
	 * 
	 * @param featureSet the features
	 * @param clusterSet the clusters
	 * @param gmmList the gmm vector
	 * @param gmmTopList the gmm tops
	 * @param parameter the param
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */

	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmList, GMMArrayList gmmTopList, Parameter parameter) throws DiarizationException, IOException {
		logger.info("Compute Score");
		int size = gmmList.size();
		logger.finer("GMM size:" + size);
		ArrayList<String> genderString = new ArrayList<String>();
		ArrayList<String> bandwidthString = new ArrayList<String>();
		for (int i = 0; i < size; i++) {
			String gmmName = gmmList.get(i).getName();
			if (parameter.getParameterScore().isGender() == true) {
				if (gmmName.equals("MS")) {
					genderString.add(Cluster.genderStrings[1]);
					bandwidthString.add(Segment.bandwidthStrings[2]);
				} else if (gmmName.equals("FS")) {
					genderString.add(Cluster.genderStrings[2]);
					bandwidthString.add(Segment.bandwidthStrings[2]);
				} else if (gmmName.equals("MT")) {
					genderString.add(Cluster.genderStrings[1]);
					bandwidthString.add(Segment.bandwidthStrings[1]);
				} else if (gmmName.equals("FT")) {
					genderString.add(Cluster.genderStrings[2]);
					bandwidthString.add(Segment.bandwidthStrings[1]);
				} else {
					genderString.add(Cluster.genderStrings[0]);
					bandwidthString.add(Segment.bandwidthStrings[0]);
				}
			} else {
				genderString.add(Cluster.genderStrings[0]);
				bandwidthString.add(Segment.bandwidthStrings[0]);
			}
		}

		ClusterSet clusterSetResult = new ClusterSet();
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			double[] sumScoreVector = new double[size];
			int[] sumLenghtVector = new int[size];
			double ubmScore = 0.0;
			GMM gmmTop = null;
			if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
				gmmTop = gmmTopList.get(0);
			}
			Arrays.fill(sumScoreVector, 0.0);
			Arrays.fill(sumLenghtVector, 0);
			for (Segment currantSegment : cluster) {
				Segment segment = (currantSegment.clone());
				int end = segment.getStart() + segment.getLength();
				featureSet.setCurrentShow(segment.getShowName());
				double[] scoreVector = new double[size];
				double maxScore = 0.0;
				int idxMaxScore = 0;
				for (int i = 0; i < size; i++) {
					gmmList.get(i).score_initialize();
				}
				for (int start = segment.getStart(); start < end; start++) {
					for (int i = 0; i < size; i++) {
						GMM gmm = gmmList.get(i);
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

				if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
					ubmScore = gmmTop.score_getMeanLog();
					gmmTop.score_getSumLog();
					gmmTop.score_getCount();
					gmmTop.score_reset();
				}
				for (int i = 0; i < size; i++) {
					GMM gmm = gmmList.get(i);
					scoreVector[i] = gmm.score_getMeanLog();
					sumLenghtVector[i] += gmm.score_getCount();
					sumScoreVector[i] += gmm.score_getSumLog();
					if (i == 0) {
						maxScore = scoreVector[0];
						idxMaxScore = 0;
					} else {
						double value = scoreVector[i];
						if (maxScore < value) {
							maxScore = value;
							idxMaxScore = i;
						}
					}
					gmm.score_reset();
				}
				if (parameter.getParameterScore().isTNorm()) {
					double sumScore = 0;
					double sum2Score = 0;
					for (int i = 0; i < size; i++) {
						sumScore += scoreVector[i];
						sum2Score += (scoreVector[i] * scoreVector[i]);
					}
					for (int i = 0; i < size; i++) {
						double value = scoreVector[i];
						double mean = (sumScore - value) / (size - 1);
						double et = Math.sqrt(((sum2Score - (value * value)) / (size - 1)) - (mean * mean));
						scoreVector[i] = (value - mean) / et;
					}
				}
				if (parameter.getParameterScore().isGender() == true) {
					segment.setBandwidth(bandwidthString.get(idxMaxScore));
					segment.setInformation("segmentGender", genderString.get(idxMaxScore));
				}
				if (parameter.getParameterScore().isBySegment()) {
					for (int k = 0; k < size; k++) {
						double score = scoreVector[k];
						GMM gmm = gmmList.get(k);
						segment.setInformation("score:" + gmm.getName(), score);
						currantSegment.setInformation("score:" + gmm.getName(), score);
					}
					if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
						segment.setInformation("score:" + "UBM", ubmScore);
						currantSegment.setInformation("score:" + "UBM", ubmScore);
					}
				}
				String newName = cluster.getName();
				if (parameter.getParameterScore().isByCluster() == false) {
					if ((scoreVector[idxMaxScore] > parameter.getParameterSegmentation().getThreshold())
							&& (parameter.getParameterScore().getLabel() != ParameterScore.LabelType.LABEL_TYPE_NONE.ordinal())) {
						if (parameter.getParameterScore().getLabel() == ParameterScore.LabelType.LABEL_TYPE_ADD.ordinal()) {
							newName += "_";
							newName += gmmList.get(idxMaxScore).getName();
						} else {
							newName = gmmList.get(idxMaxScore).getName();
						}
					}

					Cluster temporaryCluster = clusterSetResult.getOrCreateANewCluster(newName);
					temporaryCluster.setGender(cluster.getGender());
					if (parameter.getParameterScore().isGender() == true) {
						temporaryCluster.setGender(genderString.get(idxMaxScore));
					}
					temporaryCluster.addSegment(segment);
				}
			}
			if (parameter.getParameterScore().isByCluster()) {
				for (int i = 0; i < size; i++) {
					sumScoreVector[i] /= sumLenghtVector[i];
				}
				if (parameter.getParameterScore().isTNorm()) {
					double sumScore = 0;
					double sum2Score = 0;
					for (int i = 0; i < size; i++) {
						sumScore += sumScoreVector[i];
						sum2Score += (sumScoreVector[i] * sumScoreVector[i]);
					}
					for (int i = 0; i < size; i++) {
						double value = sumScoreVector[i];
						double mean = (sumScore - value) / (size - 1);
						double et = Math.sqrt(((sum2Score - (value * value)) / (size - 1)) - (mean * mean));
						sumScoreVector[i] = (value - mean) / et;
					}
				}
				double maxScore = sumScoreVector[0];
				int idxMaxScore = 0;
				for (int i = 1; i < size; i++) {
					double s = sumScoreVector[i];
					if (maxScore < s) {
						maxScore = s;
						idxMaxScore = i;
					}
				}
				String newName = cluster.getName();
				if ((sumScoreVector[idxMaxScore] > parameter.getParameterSegmentation().getThreshold())
						&& (parameter.getParameterScore().getLabel() != ParameterScore.LabelType.LABEL_TYPE_NONE.ordinal())) {
					if (parameter.getParameterScore().getLabel() == ParameterScore.LabelType.LABEL_TYPE_ADD.ordinal()) {
						newName += "_";
						newName += gmmList.get(idxMaxScore).getName();
					} else {
						newName = gmmList.get(idxMaxScore).getName();
					}
					logger.finer("cluster name=" + cluster.getName() + " new_name=" + newName);
				}
				Cluster tempororaryCluster = clusterSetResult.getOrCreateANewCluster(newName);
				tempororaryCluster.setGender(cluster.getGender());
				if (parameter.getParameterScore().isGender() == true) {
					tempororaryCluster.setGender(genderString.get(idxMaxScore));
				}
				tempororaryCluster.setName(newName);
				for (Segment currantSegment : cluster) {
					Segment segment = (currantSegment.clone());
					if (parameter.getParameterScore().isGender() == true) {
						segment.setBandwidth(bandwidthString.get(idxMaxScore));
					}
					tempororaryCluster.addSegment(segment);
				}
				for (int k = 0; k < size; k++) {
					double score = sumScoreVector[k];
					GMM gmm = gmmList.get(k);
					logger.finer("clustername = " + newName + " name=" + gmm.getName() + " =" + score);
					tempororaryCluster.setInformation("score:" + gmm.getName(), score);
				}
				if (parameter.getParameterTopGaussian().getScoreNTop() >= 0) {
					// tempororaryCluster.putInformation("score:" + "length", ubmSumLen);
					// tempororaryCluster.putInformation("score:" + "UBM", ubmSumScore / ubmSumLen);
				}
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
			info(parameter, "MScore");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
				// FeatureSet featureSet2 = Diarization.loadFeature(parameter, clusterSetBase, parameter.getParameterInputFeature().getFeaturesDescription().getFeaturesFormat()
				// + ",1:1:0:0:0:0,13,0:0:0:0");
				// ClusterSet clusterSet = new ClusterSet();
				// MSegInit.make(featureSet2, clusterSetBase, clusterSet, parameter);
				// clusterSet.collapse();
				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
				// Top Gaussian model
				GMMArrayList gmmTopGaussianList = MainTools.readGMMForTopGaussian(parameter, featureSet);

				// Compute Model
				GMMArrayList gmmList = MainTools.readGMMContainer(parameter);

				ClusterSet clusterSetResult = make(featureSet, clusterSet, gmmList, gmmTopGaussianList, parameter);

				// Seg outPut
				MainTools.writeClusterSet(parameter, clusterSetResult, false);
			}
		} catch (DiarizationException e) {
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
			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			parameter.getParameterTopGaussian().logAll(); // sTop
			parameter.getParameterScore().logAll(); // sGender
			parameter.getParameterSegmentation().logAll(); // sThr
			logger.config(parameter.getSeparator());
		}
	}

}
