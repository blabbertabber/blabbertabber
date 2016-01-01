/**
 * 
 * <p>
 * SFilter2
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
 *          filter a segmentation file by another one
 * 
 */

package fr.lium.spkDiarization.tools;

import java.lang.reflect.InvocationTargetException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class SFilter2.
 */
public class SFilter2 {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SFilter2.class.getName());

	/**
	 * Adds the frame.
	 * 
	 * @param start the start
	 * @param length the length
	 * @param segmentMap the segment map
	 * @param clusterSetResult the cluster set result
	 */
	public static void addFrame(int start, int length, TreeMap<Integer, Segment> segmentMap, ClusterSet clusterSetResult) {
		for (int i = start; i < (start + length); i++) {
			if (segmentMap.containsKey(i)) {
				String name = segmentMap.get(i).getClusterName();
				Cluster cluster = clusterSetResult.getOrCreateANewCluster(name);
				// A verifier rajout du getCluster()
				cluster.setGender(segmentMap.get(i).getCluster().getGender());
				cluster.addSegment(segmentMap.get(i));
			} else {
				logger.warning("Feature not found=" + i);
			}
		}
	}

	/**
	 * Filter.
	 * 
	 * @param segmentMap the segment map
	 * @param filterClusterSet the filter cluster set
	 * @param parameter the parameter
	 * @return the cluster set
	 */
	public static ClusterSet filter(TreeMap<Integer, Segment> segmentMap, ClusterSet filterClusterSet, Parameter parameter) {
		ClusterSet clusterSetResult = new ClusterSet();
		int pad = parameter.getParameterFilter().getSegmentPadding();
		String indexFliter = parameter.getParameterSegmentationFilterFile().getClusterFilterName();
		Cluster cluster = filterClusterSet.getCluster(indexFliter);
		// cluster.debug(0);
		for (Segment segment : cluster) {
			int startFilter = segment.getStart();
			int lengthFilter = segment.getLength();
			int endFilter = (startFilter + lengthFilter);
			if (segmentMap.containsKey(startFilter - 1) == true) {
				startFilter += pad;
			} else {
				logger.finer("no start padding");
			}
			if (segmentMap.containsKey(endFilter + 1) == true) {
				endFilter -= pad;
			} else {
				logger.finer("no end padding");
			}

			if (lengthFilter > parameter.getParameterFilter().getSilenceMinimumLength()) {
				logger.finer("eti = " + indexFliter + " start = " + startFilter + " len = " + lengthFilter + " end = "
						+ endFilter);

				for (int i = startFilter; i < endFilter; i++) {
					segmentMap.remove(i);
				}
			}
		}
		for (int idx : segmentMap.keySet()) {
			addFrame(idx, 1, segmentMap, clusterSetResult);
		}
		clusterSetResult.collapse();

		for (Cluster clusterResult : clusterSetResult.clusterSetValue()) {
			for (Segment segmentResult : clusterResult) {
				if (segmentResult.getLength() <= parameter.getParameterFilter().getSpeechMinimumLength()) {
					segmentResult.setLength(0);
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
			info(parameter, "SFilter2");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
				clusterSet.collapse();
				ClusterSet filterClusterSet = new ClusterSet();
				filterClusterSet.read(parameter.show, parameter.getParameterSegmentationFilterFile());
				filterClusterSet.collapse(5);
				TreeMap<Integer, Segment> segmentMap = clusterSet.getFeatureMap();
				ClusterSet clusterSetResult = filter(segmentMap, filterClusterSet, parameter);

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
	 * @param program the program
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
			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationOutputFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationFilterFile().logAll();
			parameter.getParameterSegmentationFilterFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterFilter().logAll();
		}
	}
}
