/**
 * 
 * <p>
 * MSegSil
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          Silence segmentation
 * 
 */

package fr.lium.spkDiarization.programs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.Gaussian;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterAudioFeature.SpeechDetectorMethod;

/**
 * The Class MSpeechDetector.
 */
public class MSpeechDetector {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MSpeechDetector.class.getName());

	/**
	 * Bi gaussien thresholded method.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @param threshold the threshold
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet BiGaussienThresholdedMethod(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter, double threshold) throws Exception {
		String FeatureFormat = parameter.getParameterInputFeature().getFeaturesDescriptorAsString();
		parameter.getParameterInputFeature().setFeaturesDescription("featureSetTransformation,3:1:0:0:0:0,13,0:0:0:0");
		AudioFeatureSet featureSet2 = MainTools.readFeatureSet(parameter, clusterSet);

		ClusterSet clusterSetResult = (clusterSet.clone());
		// int engergyIndex = featureSet.getIndexOfEnergy();
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			double thrNS = Distance.getThreshold(cluster, featureSet, 0.1, featureSet.getIndexOfEnergy());
			double thrS = Distance.getThreshold(cluster, featureSet, 0.9, featureSet.getIndexOfEnergy());

			ClusterSet clusterSNS = new ClusterSet();
			Cluster clusterS = clusterSNS.createANewCluster("S");
			Cluster clusterNS = clusterSNS.createANewCluster("NS");

			for (Segment segment : cluster) {
				featureSet.setCurrentShow(segment.getShowName());
				for (int i = segment.getStart(); i <= segment.getLast(); i++) {
					Segment newSegment = segment.clone();
					newSegment.setStart(i);
					newSegment.setLength(1);
					if (featureSet.getFeatureUnsafe(i)[featureSet.getIndexOfEnergy()] > thrS) {
						clusterS.addSegment(newSegment);
					}
					if (featureSet.getFeatureUnsafe(i)[featureSet.getIndexOfEnergy()] < thrNS) {
						clusterNS.addSegment(newSegment);
					}

				}
			}
			clusterSNS.collapse();

			GMMArrayList initizationGmmList = new GMMArrayList(clusterSNS.clusterGetSize());
			GMMArrayList gmmList = new GMMArrayList(clusterSNS.clusterGetSize());
			MTrainInit.make(featureSet2, clusterSNS, initizationGmmList, parameter);
			// ** EM training of the initialized GMM
			MTrainEM.make(featureSet2, clusterSNS, initizationGmmList, gmmList, parameter);
			Gaussian gaussian = gmmList.get(0).getComponent(0);
			if (gmmList.get(0).getComponent(0).getMean(0) < gmmList.get(0).getComponent(1).getMean(0)) {
				gaussian = gmmList.get(0).getComponent(1);
			}
			double thr = gaussian.getMean(0) - Math.sqrt(gaussian.getCovariance(0, 0) * threshold);

			if ((threshold > 0.0)) {
				logger.finer("cluster : " + cluster.getName() + " thr = " + thr);
			}
			Cluster clusterResult = clusterSetResult.getCluster(cluster.getName());
			clusterResult.clearSegments();
			for (Segment segTmp : cluster) {
				int start = segTmp.getStart();
				int endSegment = start + segTmp.getLength();
				int end = Math.min(endSegment, featureSet.getNumberOfFeatures());
				featureSet.setCurrentShow(segTmp.getShowName());
				ArrayList<Boolean> speechFeatureList = segTmp.getSpeechFeatureList();
				speechFeatureList.clear();
				for (int i = start; i < end; i++) {
					double value = featureSet.getFeatureUnsafe(i)[featureSet.getIndexOfEnergy()];
					if (value > thr) {
						Segment seg = (segTmp.clone());
						seg.setStart(i);
						seg.setLength(1);
						clusterResult.addSegment(seg);
						speechFeatureList.add(true);
					} else {
						speechFeatureList.add(false);
					}
				}
			}
		}
		parameter.getParameterInputFeature().setFeaturesDescription(FeatureFormat);
		return clusterSetResult;
	}

	/**
	 * Energy threshold method.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @param threshold the threshold
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterSet EnergyThresholdMethod(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter, double threshold) throws DiarizationException, IOException {
		// clusters result
		ClusterSet clusterSetResult = (clusterSet.clone());

		// int staticDimension = features.getStaticDimension();
		// int engergyIndex = featureSet.getIndexOfEnergy();
		// compute mean and std
		for (Cluster cluster : clusterSet.clusterSetValue()) {

			featureSet.setCurrentShow(cluster.firstSegment().getShowName());
			double thr = Distance.getThreshold(cluster, featureSet, threshold, featureSet.getIndexOfEnergy());
			Cluster clusterResult = clusterSetResult.getCluster(cluster.getName());
			clusterResult.clearSegments();

			if ((threshold > 0.0)) {
				logger.finer("cluster : " + cluster.getName() + " thr = " + thr);
			}
			// remove frames that have energy under the thershold
			for (Segment segTmp : cluster) {
				featureSet.setCurrentShow(segTmp.getShowName());
				int start = segTmp.getStart();
				int endSegment = start + segTmp.getLength();
				int end = Math.min(endSegment, featureSet.getNumberOfFeatures());
				ArrayList<Boolean> speechFeatureList = segTmp.getSpeechFeatureList();
				speechFeatureList.clear();
				speechFeatureList.ensureCapacity(segTmp.getLength() + 1);
				logger.finer("speech detector cluster:" + cluster.getName() + " start:" + segTmp.getStart() + " len:"
						+ segTmp.getLength() + " last:" + segTmp.getLast());
				for (int i = start; i < end; i++) {
					double value = featureSet.getFeatureUnsafe(i)[featureSet.getIndexOfEnergy()];
					if (value > thr) {
						Segment seg = (segTmp.clone());
						seg.setStart(i);
						seg.setLength(1);
						clusterResult.addSegment(seg);
						speechFeatureList.add(true);
					} else {
						speechFeatureList.add(false);
					}
				}

			}
			clusterResult.collapse();
		}
		return clusterSetResult;
	}

	/**
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param parameter the parameter
	 * @param threshold the threshold
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterSet, Parameter parameter, double threshold) throws Exception {
		logger.info("Speech detection");
		ClusterSet clusterSetResult = null;
		if (parameter.getParameterInputFeature().getSpeechMethod() == SpeechDetectorMethod.SPEECH_ON_ENERGY) {
			clusterSetResult = EnergyThresholdMethod(clusterSet, featureSet, parameter, threshold);
		} else if (parameter.getParameterInputFeature().getSpeechMethod() == SpeechDetectorMethod.SPEECH_ON_BIGAUSSIAN) {
			clusterSetResult = BiGaussienThresholdedMethod(clusterSet, featureSet, parameter, threshold);
		} else {
			return clusterSet;
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
			info(parameter, "MSpeechDetector");
			if (!parameter.getParameterInputFeature().getFeaturesDescription().getEnergyPresence()) {
				logger.finer("Energy is not available in features");
				System.exit(-1);
			}
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
				// Features
				double threshold = parameter.getParameterInputFeature().getSpeechThreshold();
				parameter.getParameterInputFeature().setSpeechThreshold(0.0);
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
				ClusterSet clusterSetResult = make(featureSet, clusterSet, parameter, threshold);
				MainTools.writeClusterSet(parameter, clusterSetResult, true);
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
	 * @param progam the progam
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String progam) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("program name = " + progam);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterInputFeature().logAll(); // fInMask
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
		}
	}

}
