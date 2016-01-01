/**
 * 
 * <p>
 * MDecode
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
 *          Viterbi decoding program
 * 
 */

package fr.lium.spkDiarization.programs;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libDecoder.FastDecoderWithDuration;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterDecoder;

/**
 * The Class MDecode.
 */
public class MDecode {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MDecode.class.getName());

	/**
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmList, Parameter parameter) throws Exception {
		String message = "Number of GMM=" + gmmList.size();
		FastDecoderWithDuration decoder = null;
		if (parameter.getParameterTopGaussian().getScoreNTop() > 0) {
			message += " (use top)";
			decoder = new FastDecoderWithDuration(parameter.getParameterTopGaussian().getScoreNTop(), gmmList.get(0), parameter.getParameterDecoder().isComputeLLhR(), parameter.getParameterDecoder().getShift());
		} else {
			decoder = new FastDecoderWithDuration(parameter.getParameterDecoder().getShift());
		}
		logger.info("fast decoding, " + message);
		decoder.setupHMM(gmmList, parameter);
		ClusterSet clusterSetToDecode = new ClusterSet();
		Cluster clusterToDecode = clusterSetToDecode.getOrCreateANewCluster("Init");
		TreeSet<Segment> segmentListToDecode = clusterSet.getSegments();

		for (Segment segment : segmentListToDecode) {
			clusterToDecode.addSegment(segment);
		}
		LinkedList<Integer> list = clusterSetToDecode.collapse(0);
		segmentListToDecode = clusterSetToDecode.getSegments();
		for (Segment segment : segmentListToDecode) {
			if (parameter.getParameterDecoder().getViterbiDurationConstraints().get(0) == ParameterDecoder.ViterbiDurationConstraint.VITERBI_JUMP_DURATION) {
				logger.fine("\t decoder.accumulation starting at " + segment.getStart() + " to " + segment.getLast()
						+ " with jump duration constraint");
				decoder.accumulate(featureSet, segment, list);
			} else {
				logger.fine("\t decoder.accumulation starting at " + segment.getStart() + " to " + segment.getLast());
				decoder.accumulate(featureSet, segment);
			}
		}
		logger.fine("\t decoder.get result");

		ClusterSet res = decoder.getClusterSet();
		res.collapse();
		return res;
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
			info(parameter, "MDecode");
			if (parameter.show.isEmpty() == false) {
				// Clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
				// clusters.debug();

				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);

				// Models
				GMMArrayList gmmList = MainTools.readGMMContainer(parameter);

				// Create the decoder
				ClusterSet clusterSetResult = make(featureSet, clusterSet, gmmList, parameter);
				// Seg outPut
				MainTools.writeClusterSet(parameter, clusterSetResult, false);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
	}

	/**
	 * Initialize the decoder.
	 * 
	 * @param parameter the parameter
	 * @param progam the progam
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */

	public static void info(Parameter parameter, String progam) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.finer(parameter.getSeparator2());
			logger.config("program name = " + progam);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterInputFeature().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationOutputFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			parameter.getParameterTopGaussian().logTopGaussian(); // sTop
			logger.config(parameter.getSeparator());
			parameter.getParameterDecoder().logAll();
		}
	}

}
