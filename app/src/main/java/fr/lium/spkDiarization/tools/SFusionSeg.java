/**
 * 
 * <p>
 * SFusionSeg
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
 *          Concat GMM model file in a GMM Vector and save it
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
import fr.lium.spkDiarization.lib.libDiarizationError.DiarizationError;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class SFusionSeg.
 */
public class SFusionSeg {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SFusionSeg.class.getName());

	/**
	 * Merge2.
	 * 
	 * @param clusterSet1 the cluster set1
	 * @param clusterSet2 the cluster set2
	 * @param pairNamesList the pair names list
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 */
	public static ClusterSet merge2(ClusterSet clusterSet1, ClusterSet clusterSet2, ArrayList<String> pairNamesList) throws DiarizationException {
		ClusterSet clusterSetResult = new ClusterSet();

		TreeMap<Integer, Segment> frameMap1 = clusterSet1.getFeatureMap();
		TreeMap<Integer, Segment> frameMap2 = clusterSet2.getFeatureMap();

		for (int i : frameMap1.keySet()) {
			Segment segment1 = frameMap1.get(i);
			Segment segment2 = frameMap2.get(i);
			Segment segment = frameMap1.get(i).clone();
			String name1 = segment1.getClusterName();
			String name2 = segment2.getClusterName();
			String name = name1 + ":" + name2;
			if (pairNamesList.contains(name)) {
				name = name1;
			}
			Cluster cluster = null;
			if (clusterSetResult.containsCluster(name) == true) {
				cluster = clusterSetResult.getCluster(name);
			} else {
				cluster = clusterSetResult.createANewCluster(name);
			}
			cluster.addSegment(segment);
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
			if (parameter.show.isEmpty() == false) { // clusters ClusterSet clusters1 =
				MainTools.readClusterSet(parameter);

				ClusterSet clusterSet1 = MainTools.readClusterSet(parameter);
				ClusterSet clusterSet2 = MainTools.readTheSecondClusterSet(parameter);
				DiarizationError computeError = new DiarizationError(clusterSet1, null);
				ArrayList<String> pairNameList = computeError.listOfMatchedSpeaker(clusterSet2);

				ClusterSet clusterSetResult = merge2(clusterSet1, clusterSet2, pairNameList);
				clusterSetResult.collapse();
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

			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationInputFile2().logAll();
			parameter.getParameterSegmentationOutputFile().logAll();
		}
	}

}
