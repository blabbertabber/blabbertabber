/**
 * 
 * <p>
 * SFilter
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
import java.util.ArrayList;
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
 * The Class SFilter.
 */
public class SFilter {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SFilter.class.getName());

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
				if (i < segmentMap.lastKey()) {
					logger.warning("feature not found=" + i);
				}
			}
		}

	}

	/**
	 * Filter.
	 * 
	 * @param segmentMap the segment map
	 * @param clusterSetFilter the cluster set filter
	 * @param parameter the parameter
	 * @return the cluster set
	 */
	public static ClusterSet filter(TreeMap<Integer, Segment> segmentMap, ClusterSet clusterSetFilter, Parameter parameter) {
		ClusterSet clusterResult = new ClusterSet();
		String listOfFilter = "," + parameter.getParameterSegmentationFilterFile().getClusterFilterName() + ",";
		int pad = parameter.getParameterFilter().getSegmentPadding();
		for (Cluster clusterFilter : clusterSetFilter.clusterSetValue()) {
			String label = clusterFilter.getName();
			for (Segment segmentFilter : clusterFilter) {
				int startFilter = segmentFilter.getStart();
				int lengthFilter = segmentFilter.getLength();
				if (listOfFilter.contains("," + label + ",")) {
					if (lengthFilter < parameter.getParameterFilter().getSilenceMinimumLength()) {
						addFrame(startFilter, lengthFilter, segmentMap, clusterResult);
					} else {
						if (pad > 0) {
							addFrame(startFilter, pad, segmentMap, clusterResult);
							int s2 = (startFilter + lengthFilter) - pad;
							addFrame(s2, pad, segmentMap, clusterResult);
						}
					}
				} else {
					addFrame(startFilter, lengthFilter, segmentMap, clusterResult);
				}
			}
		}
		clusterResult.collapse();
		return clusterResult;
	}

	/**
	 * Make.
	 * 
	 * @param clusterset the clusterset
	 * @param filterClusterSet the filter cluster set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 */
	public static ClusterSet make(ClusterSet clusterset, ClusterSet filterClusterSet, Parameter parameter) throws DiarizationException {
		logger.info("Filter segmentation using: "
				+ parameter.getParameterSegmentationFilterFile().getClusterFilterName());
		// --- remove non speech segment
		TreeMap<Integer, Segment> segmentMap = clusterset.getFeatureMap();
		ClusterSet clusterSetResult = filter(segmentMap, filterClusterSet, parameter);

		// --- rename small speech segment ---
		ArrayList<Segment> segmentList = clusterSetResult.getSegmentVectorRepresentation();
		SFilter.removeSmall(segmentList, parameter);

		ClusterSet clusterSetResult2 = new ClusterSet();
		clusterSetResult2.addVector(segmentList);
		return clusterSetResult2;
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
			info(parameter, "SFilter");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);

				ClusterSet filterClusterSet = new ClusterSet();
				filterClusterSet.read(parameter.show, parameter.getParameterSegmentationFilterFile());

				ClusterSet clusterResult = make(clusterSet, filterClusterSet, parameter);

				MainTools.writeClusterSet(parameter, clusterResult, true);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
	}

	/**
	 * Removes the small.
	 * 
	 * @param segmentList the segment list
	 * @param parameter the parameter
	 */
	public static void removeSmall(ArrayList<Segment> segmentList, Parameter parameter) {
		int previous = -1;
		int current = 0;
		int next = 1;
		logger.finer("remove segment less than param.segMinLenSpeech="
				+ parameter.getParameterFilter().getSpeechMinimumLength());

		int size = segmentList.size();
		while (current < size) {
			if (segmentList.get(current).getLength() > parameter.getParameterFilter().getSpeechMinimumLength()) {
				previous++;
				current++;
				next++;
			} else {
				int delayPrevious = 10;
				int delayNext = 10;
				if (previous >= 0) {
					delayPrevious = segmentList.get(current).getStart()
							- (segmentList.get(previous).getStart() + segmentList.get(previous).getLength());
				}
				if (next < size) {
					delayNext = segmentList.get(next).getStart()
							- (segmentList.get(current).getStart() + segmentList.get(current).getLength());
				}
				if ((delayPrevious <= 0) && (delayNext <= 0)) {
					if (segmentList.get(previous).getLength() < segmentList.get(next).getLength()) {
						segmentList.get(previous).setLength(segmentList.get(previous).getLength()
								+ segmentList.get(current).getLength());
						segmentList.remove(current);
					} else {
						segmentList.get(current).setLength(segmentList.get(next).getLength()
								+ segmentList.get(current).getLength());
						segmentList.get(current).setCluster(segmentList.get(next).getCluster());
						segmentList.remove(next);
					}
				} else if (delayPrevious <= 0) {
					segmentList.get(previous).setLength(segmentList.get(previous).getLength()
							+ segmentList.get(current).getLength());
					segmentList.remove(current);
				} else if (delayNext <= 0) {
					segmentList.get(current).setLength(segmentList.get(next).getLength()
							+ segmentList.get(current).getLength());
					segmentList.get(current).setCluster(segmentList.get(next).getCluster());
					segmentList.remove(next);
				} else {
					segmentList.remove(current);
				}
			}
			size = segmentList.size();
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
			parameter.getSeparator2();
			logger.config("Program name = " + progam);
			logger.config(parameter.getSeparator2());
			parameter.logShow();

			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationFilterFile().logAll(); // sInFltMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationFilterFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterFilter().logAll();
		}
	}

}
