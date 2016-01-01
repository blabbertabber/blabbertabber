/**
 * 
 * <p>
 * SFusionSegWithClassification
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
import java.util.Iterator;
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
 * The Class SFusionSegWithClassification.
 */
public class SFusionSegWithClassification {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SFusionSegWithClassification.class.getName());

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
			info(parameter, "SFusionSegWithClassification");
			if (parameter.show.isEmpty() == false) {
				logger.info("use segFltInputMask for classification");
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);

				ClusterSet classificationClusterSet = new ClusterSet();
				classificationClusterSet.read(parameter.show, parameter.getParameterSegmentationFilterFile());
				TreeMap<Integer, Segment> classificationFrameMap = classificationClusterSet.getFeatureMap();

				ClusterSet clusterSetResult = classificationClusterSet.clone();
				for (Cluster cluster : clusterSetResult.clusterSetValue()) {
					cluster.clearSegments();
				}

				String nameUnk = new String("UNK");
				for (Cluster cluster : clusterSet.clusterSetValue()) {
					for (Segment segment : cluster) {
						TreeMap<String, Integer> count = new TreeMap<String, Integer>();
						Iterator<Cluster> itCluster2 = classificationClusterSet.clusterSetValueIterator();
						while (itCluster2.hasNext()) {
							count.put(itCluster2.next().getName(), 0);
						}

						int unknown = 0;
						for (int i = segment.getStart(); i <= segment.getLast(); i++) {
							if (classificationFrameMap.containsKey(i)) {
								String name = classificationFrameMap.get(i).getClusterName();
								int val = count.get(name) + 1;
								count.put(name, val);
							} else {
								unknown++;
							}
						}
						String idxMax = "UNK";
						Iterator<String> itCount = count.keySet().iterator();
						while (itCount.hasNext()) {
							if (count.get(itCount.next()) > count.get(idxMax)) {
								idxMax = itCount.next();
							}
						}
						if (count.get(idxMax) < unknown) {
							logger.warning("more unknow, get UNK ");
							clusterSetResult.getOrCreateANewCluster(nameUnk);
						}
						// ajouter les segments
						Segment newSegment = segment.clone();
						clusterSetResult.getCluster(idxMax).addSegment(newSegment);
					}
				}

				// --- remove non speech segment

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

			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
		}
	}
}
