/**
 * 
 * <p>
 * MSegInit
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
 *          Initial segmentation program
 * 
 */

package fr.lium.spkDiarization.programs;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
import fr.lium.spkDiarization.libModel.gaussian.Gaussian;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class MSegInit.
 */
public class MSegInit {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MSegInit.class.getName());

	/**
	 * Check cluster set.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterSet checkClusterSet(AudioFeatureSet featureSet, ClusterSet clusterSet, Parameter parameter) throws DiarizationException, IOException {
		ClusterSet clusterSetResult = new ClusterSet();
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			Cluster clusterResult = clusterSetResult.getOrCreateANewCluster(cluster.getName());
			for (Segment segment : cluster) {
				featureSet.setCurrentShow(segment.getShowName());
				int startSegment = segment.getStart();
				int lengthSegment = segment.getLength();
				int endTmp = startSegment + lengthSegment;
				int nbFeatures = featureSet.getNumberOfFeatures();
				int endSegment = Math.min(endTmp, nbFeatures);
				logger.finer("check segment : " + startSegment + " " + endSegment);

				if (endTmp > endSegment) {
					logger.warning("segment end after features end");
					lengthSegment = (nbFeatures - startSegment) + 1;
				}
				Segment newSegment = segment.clone();
				newSegment.setLength(lengthSegment);
				clusterResult.addSegment(newSegment);
			}
		}
		return clusterSetResult;
	}

	/**
	 * Detect equal features.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param clusterSetResult the cluster set result
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void detectEqualFeatures(AudioFeatureSet featureSet, ClusterSet clusterSet, ClusterSet clusterSetResult, Parameter parameter) throws DiarizationException, IOException {
		// String initialClusterName = new String("S0");
		// Cluster resultCluster = clustersResult.createANewCluster(initialClusterName);
		float rate = parameter.getParameterSegmentationInputFile().getRate();

		for (Cluster cluster : clusterSet.clusterSetValue()) {
			Cluster clusterResult = clusterSetResult.getOrCreateANewCluster(cluster.getName());
			for (Segment segment : cluster) {
				featureSet.setCurrentShow(segment.getShowName());
				int startSegment = segment.getStart();
				int endTmp = startSegment + segment.getLength();
				int endSegment = Math.min(endTmp, featureSet.getNumberOfFeatures());
				logger.finer("check segment : " + startSegment + " " + endSegment);

				if (endTmp > endSegment) {
					logger.warning("segment end after features end");
				}
				boolean equal = false;
				for (int i = startSegment + 1; i < endSegment; i++) {
					if (featureSet.compareFreatures(i - 1, i)) {
						logger.warning("two consecutive features are the same, index = " + i);
						equal = true;
					} else {
						if (equal == true) {
							equal = false;
						} else {
							Segment oneFrameSegment;
							if (i == (startSegment + 1)) {
								oneFrameSegment = new Segment(segment.getShowName(), i - 1, 1, clusterResult, rate);
								clusterResult.addSegment(oneFrameSegment);
							}
							oneFrameSegment = new Segment(segment.getShowName(), i, 1, clusterResult, rate);
							clusterResult.addSegment(oneFrameSegment);
						}
					}

				}
			}
		}
	}

	/**
	 * Detect likelihood problem.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param clusterSetResult the cluster set result
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void detectLikelihoodProblem(AudioFeatureSet featureSet, ClusterSet clusterSet, ClusterSet clusterSetResult, Parameter parameter) throws DiarizationException, IOException {
		// String initialClusterName = new String("S0");
		// Cluster resultCluster = clustersResult.createANewCluster(initialClusterName);

		int dim = featureSet.getFeatureSize();
		GMM gmm = new GMM(1, dim, Gaussian.DIAG);
		Gaussian gaussian = gmm.getComponent(0);

		/*
		 * ClusterSet clusterByFile = new ClusterSet(); for(Cluster cluster : clusters.clusterSetValue()) { for(Segment segment : cluster) { String show = segment.getShowName(); Cluster fileCluster = clusterByFile.getCluster(show); if (fileCluster ==
		 * null) { fileCluster = clusterByFile.createANewCluster(show); } fileCluster.addSegment(segment); } }
		 */

		for (Cluster cluster : clusterSet.clusterSetValue()) {
			Cluster resultCluster = clusterSetResult.getOrCreateANewCluster(cluster.getName());
			gaussian.statistic_initialize();
			gaussian.statistic_addFeatures(cluster, featureSet);
			gaussian.setModel();

			for (Segment segment : cluster) {
				featureSet.setCurrentShow(segment.getShowName());
				int startSegment = segment.getStart();
				int endTmp = startSegment + segment.getLength();
				int endSegment = Math.min(endTmp, featureSet.getNumberOfFeatures());
				logger.finer("check segment : " + startSegment + " " + endSegment);

				if (endTmp > endSegment) {
					logger.warning("segment end after features end");
				}
				for (int i = startSegment; i < endSegment; i++) {
					double score = gaussian.score_getAndAccumulate(featureSet, i);
					if (score == Double.MIN_VALUE) {
						logger.warning("freature get a tiny likelihood, remove feature index = " + i);

					} else {
						resultCluster.addSegment(new Segment(segment.getShowName(), i, 1, resultCluster, segment.getRate()));
					}
				}
			}
		}
	}

	/**
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param clusterSetResult the cluster set result
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void make(AudioFeatureSet featureSet, ClusterSet clusterSet, ClusterSet clusterSetResult, Parameter parameter) throws DiarizationException, IOException {
		logger.info("Initialization of the segmentation");
		ClusterSet clusterSetResultTmp = new ClusterSet();
		detectEqualFeatures(featureSet, clusterSet, clusterSetResultTmp, parameter);
		clusterSetResultTmp.collapse();
		detectLikelihoodProblem(featureSet, clusterSetResultTmp, clusterSetResult, parameter);
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws DiarizationException, Exception {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "MSegInit");
			if (parameter.show.isEmpty() == false) {
				ClusterSet clusterSet = null;
				Segment segment = null;
				if (parameter.getParameterSegmentationInputFile().getMask().equals("")) {
					clusterSet = new ClusterSet();
					Cluster cluster = clusterSet.createANewCluster("init");
					segment = new Segment(parameter.show, 0, 0, cluster, parameter.getParameterSegmentationInputFile().getRate());
					cluster.addSegment(segment);

				} else {
					// clusters
					clusterSet = MainTools.readClusterSet(parameter);
				}

				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
				if (parameter.getParameterSegmentationInputFile().getMask().equals("")) {
					featureSet.setCurrentShow(segment.getShowName());
					segment.setLength(featureSet.getNumberOfFeatures());
				}

				ClusterSet clusterSetResult = new ClusterSet();

				make(featureSet, clusterSet, clusterSetResult, parameter);

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
	 * @param program the program
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("program name = " + program);
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