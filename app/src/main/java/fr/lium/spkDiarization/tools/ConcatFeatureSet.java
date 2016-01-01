/**
 * 
 * <p>
 * SConcatFeatureSet
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
 *          Concatenation feature set describe in a segmentation file into a new feature set.
 * 
 */

package fr.lium.spkDiarization.tools;

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
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class to joint feature indexed by the input segmentation file.
 */
public class ConcatFeatureSet {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ConcatFeatureSet.class.getName());

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
			info(parameter, "SConcatFeatureSet");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet fullClusterSet = null;
				if (parameter.getParameterSegmentationInputFile().getMask().equals("")) {
					fullClusterSet = new ClusterSet();
					Cluster cluster = fullClusterSet.createANewCluster("init");
					Segment segment = new Segment(parameter.show, 0, 0, cluster, parameter.getParameterSegmentationInputFile().getRate());
					cluster.addSegment(segment);

				} else {
					// clusters
					fullClusterSet = MainTools.readClusterSet(parameter);
					fullClusterSet.collapse();
				}

				ArrayList<ClusterSet> listOfClusterSet = MainTools.splitHypotesis(fullClusterSet);
				AudioFeatureSet featureSetResult = new AudioFeatureSet(0, parameter.getParameterInputFeature().getFeaturesDescription());
				ClusterSet clusterSetResult = new ClusterSet();
				int resIdx = 0;

				for (ClusterSet clusterSet : listOfClusterSet) {
					String list = "";
					for (String name : clusterSet.getShowNames()) {
						list += name + "/";
					}
					logger.info("show: " + list);
					AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
					for (Cluster cluster : clusterSet.clusterSetValue()) {
						Cluster clusterResult = clusterSetResult.getOrCreateANewCluster(cluster.getName());
						for (Segment segment : cluster) {
							featureSet.setCurrentShow(segment.getShowName());
							int start = segment.getStart();
							int endSegment = start + segment.getLength();
							int end = Math.min(endSegment, featureSet.getNumberOfFeatures());
							Segment segmentResult = new Segment(parameter.show, resIdx, segment.getLength(), cluster, parameter.getParameterSegmentationInputFile().getRate());
							clusterResult.addSegment(segmentResult);
							for (int i = start; i < end; i++, resIdx++) {
								featureSetResult.addFeature(featureSet.getFeatureUnsafe(i));
							}
						}
					}
				}
				logger.fine("save");
				if (SpkDiarizationLogger.DEBUG) featureSetResult.debug();
				featureSetResult.setClusterSet(clusterSetResult);
				MainTools.writeFeatureSet(parameter, featureSetResult);
				MainTools.writeClusterSet(parameter, clusterSetResult, false);
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
	 * @param program the program name
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("Program name = " + program);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterInputFeature().logAll();
			parameter.getParameterOutputFeature().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationOutputFile().logAll();

		}
	}

}
