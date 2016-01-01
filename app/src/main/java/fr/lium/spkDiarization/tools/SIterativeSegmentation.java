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
 *          Iterative train speakers and segmentation the signal by Viterbi decoding
 * 
 */
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterInitializationEM;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.programs.MTrainMAP;

/**
 * The Class SIterativeSegmentation.
 */
public class SIterativeSegmentation {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SIterativeSegmentation.class.getName());

	/**
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param filterClusterSEt the filter cluster s et
	 * @param ubmList the ubm list
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterSet, ClusterSet filterClusterSEt, GMMArrayList ubmList, Parameter parameter) throws Exception {
		logger.info("Iteratice Segmentation");
		ClusterSet currentClusterSet = clusterSet;
		ClusterSet oldClusterSet = null;
		GMMArrayList speakerList = new GMMArrayList(currentClusterSet.clusterGetSize());
		GMMArrayList oldSpeakerList = new GMMArrayList(currentClusterSet.clusterGetSize());

		parameter.getParameterModel().setModelKind("DIAG");
		parameter.getParameterInitializationEM().setModelInitMethod(ParameterInitializationEM.TrainInitMethodString[3]);

		int i = 0;
		while ((oldClusterSet == null) || (oldClusterSet.equals(currentClusterSet) == false)) {

			logger.info("iteration idx=" + i);

			for (String name : currentClusterSet) {
				boolean compute = true;
				Cluster cluster = currentClusterSet.getCluster(name);
				if (cluster.getLength() > 50) {
					if (oldClusterSet != null) {
						Cluster oldCluster = oldClusterSet.getCluster(name);
						if (oldCluster != null) {
							if (oldCluster.equals(cluster)) {
								compute = false;
							}
						}
					}
					if (compute == false) {
						logger.fine(" copy gmm :" + name);
						for (GMM gmm : oldSpeakerList) {
							if (gmm.getName() == name) {
								speakerList.add(gmm);
								break;
							}
						}
					} else {
						GMMArrayList gmmInitializationList = new GMMArrayList(1);
						GMMArrayList gmmList = new GMMArrayList(1);
						ClusterSet clusterSetLocal = new ClusterSet();
						clusterSetLocal.putCluster(name, cluster);
						MTrainInit.make(featureSet, clusterSetLocal, gmmInitializationList, parameter);
						MTrainMAP.make(featureSet, clusterSetLocal, gmmInitializationList, gmmList, parameter, true);
						speakerList.add(gmmList.get(0));
					}
				}
			}

			ClusterSet newClusters = MDecode.make(featureSet, filterClusterSEt, speakerList, parameter);

			oldClusterSet = currentClusterSet;
			oldSpeakerList = speakerList;
			currentClusterSet = newClusters;
			i++;
		}

		return currentClusterSet;
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
			info(parameter, "SIterativeSegmentation");
			if (parameter.show.isEmpty() == false) {
				// Clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
// clusters.debug();
				ArrayList<String> toRemove = new ArrayList<String>();

				for (String name : clusterSet) {
					Cluster cluster = clusterSet.getCluster(name);
					int length = cluster.getLength();
					if (length < 50) {
						logger.fine("\tremove cluster : " + name + " len = " + length);
						toRemove.add(name);
					}
				}
				for (String name : toRemove) {
					clusterSet.removeCluster(name);
				}

				ClusterSet filterClusterSet = new ClusterSet();
				filterClusterSet.read(parameter.show, parameter.getParameterSegmentationFilterFile());

				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);

				// Models
				GMMArrayList gmmList = MainTools.readGMMContainer(parameter);

				ClusterSet clusterSetResult = make(featureSet, clusterSet, filterClusterSet, gmmList, parameter);
				// Seg outPut
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
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationFilterFile().logAll(); // sInFltMask
			parameter.getParameterSegmentationOutputFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			parameter.getParameterTopGaussian().logTopGaussian(); // sTop
			logger.config(parameter.getSeparator());
			parameter.getParameterEM().logAll(); // emCtl
			parameter.getParameterMAP().logAll(); // mapCtrl
			parameter.getParameterVarianceControl().logAll(); // varCtrl
			logger.config(parameter.getSeparator());
			parameter.getParameterDecoder().logAll();
		}
	}
}
