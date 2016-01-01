package fr.lium.spkDiarization.tools;

/**
 * 
 * <p>
 * SIterativeSegmentation
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          Merge the labels of two segmentation at the frame level
 * 
 */
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
 * The Class to merge 2 segmentation.The overlap segments are merge and the new name correspond to "<name1>:<name2>"
 */
public class MergeSeg {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MergeSeg.class.getName());

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
			info(parameter, "sMerge");
			if (parameter.show.isEmpty() == false) {
				// clusters
				logger.info("Merge segmentation");
				ClusterSet clusterSet1 = MainTools.readClusterSet(parameter);
				ClusterSet clusterSet2 = MainTools.readTheSecondClusterSet(parameter);

				TreeMap<Integer, Segment> segmentMap1 = clusterSet1.getFeatureMap();
				TreeMap<Integer, Segment> segmentMap2 = clusterSet2.getFeatureMap();
				ClusterSet clusterSetResult = new ClusterSet();

				int size = Math.max(segmentMap1.lastKey(), segmentMap2.lastKey());
				logger.info("size=" + size);

				for (int i = 0; i < size; i++) {

					Segment newSegment = null;
					String newName = "empty";
					String gender = Cluster.genderStrings[0];
					if (segmentMap1.containsKey(i) && segmentMap2.containsKey(i)) {
						Segment segment1 = segmentMap1.get(i);
						Segment segment2 = segmentMap2.get(i);

						if (segment2.getCluster().getGender().equals(segment1.getCluster().getGender()) == true) {
							gender = segment1.getCluster().getGender();
						}
						newName = segment1.getCluster().getName();
						newName += ":";
						newName += segment2.getCluster().getName();
						newSegment = segment1.clone();
					} else {
						if (segmentMap1.containsKey(i) == true) {
							Segment segMap = segmentMap1.get(i);
							gender = segMap.getCluster().getGender();
							newName = segMap.getCluster().getName();
							newName += ":UNK";
							newSegment = segMap.clone();
						} else {
							if (segmentMap2.containsKey(i) == true) {
								Segment segMap2 = segmentMap2.get(i);
								gender = segMap2.getCluster().getGender();
								newName = "UNK:";
								newName += segMap2.getCluster().getName();
								newSegment = segMap2.clone();
							}
						}
					}
					// logger.info(i+"="+newName+info);

					if (newSegment != null) {
						Cluster cluster = null;
						if (clusterSetResult.containsCluster(newName)) {
							cluster = clusterSetResult.getCluster(newName);
						} else {
							cluster = clusterSetResult.createANewCluster(newName);
							cluster.setGender(gender);
						}
						cluster.addSegment(newSegment);
					}
				}

				MainTools.writeClusterSet(parameter, clusterSetResult, true);
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

			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationInputFile2().logAll();
			parameter.getParameterSegmentationOutputFile().logAll();
		}
	}
}
