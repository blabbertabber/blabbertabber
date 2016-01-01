/**
 * 
 * <p>
 * MTrainEM
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
 *          EM trainer program for the GMMs
 * 
 */

package fr.lium.spkDiarization.programs;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.GMMFactory;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class MTrainMAP.
 */
public class MTrainMAP {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MTrainMAP.class.getName());

	/**
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param initializationGmmList the initialization gmm list
	 * @param gmmList the gmm list
	 * @param parameter the parameter
	 * @param resetAccumulator the reset accumulator
	 * @throws Exception the exception
	 */
	public static void make(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList initializationGmmList, GMMArrayList gmmList, Parameter parameter, boolean resetAccumulator) throws Exception {
		logger.info("Train models using MAP");

		if (initializationGmmList.size() != clusterSet.clusterGetSize()) {
			throw new DiarizationException("error \t initial model number is not good ");
		}
		for (int i = 0; i < initializationGmmList.size(); i++) {
			logger.finer("info : " + i + "=" + initializationGmmList.get(i).getName());
		}

		boolean useSpeechDetector = (parameter.getParameterInputFeature().getSpeechThreshold() > 0);

		for (int i = 0; i < initializationGmmList.size(); i++) {
			GMM initializationGmm = initializationGmmList.get(i);
			GMM ubmGmm = initializationGmmList.get(i);
			String name = initializationGmm.getName();
			Cluster cluster = clusterSet.getCluster(name);
			if (cluster == null) {
				for (String clusterName : clusterSet) {
					logger.fine("cluster/gmm: " + clusterName + "/" + name + " levenshtein="
							+ Distance.levenshteinDistance(clusterName, name));
					byte tabC[] = clusterName.getBytes();
					byte tabN[] = name.getBytes();
					for (int j = 0; j < Math.min(tabC.length, tabN.length); j++) {
						logger.fine("cluster/gmm char " + j + " = " + tabC[j] + " " + tabN[j]);
					}
				}

				throw new DiarizationException("error \t can't find cluster for model " + name);
			}
			logger.fine("\t train MAP cluster=" + cluster.getName() + " size=" + cluster.getLength());

			gmmList.add(GMMFactory.getMAP(cluster, featureSet, initializationGmm, ubmGmm, parameter.getParameterEM(), parameter.getParameterMAP(), parameter.getParameterVarianceControl(), useSpeechDetector, resetAccumulator));
		}
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
			info(parameter, "MTrainMAP");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);

				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);

				// Compute Model
				GMMArrayList initializationGmmList = MainTools.readGMMContainer(parameter);
				GMMArrayList gmmList = new GMMArrayList();

				make(featureSet, clusterSet, initializationGmmList, gmmList, parameter, true);

				MainTools.writeGMMContainer(parameter, gmmList);
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
			logger.config(parameter.getSeparator());
			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			parameter.getParameterModelSetOutputFile().logAll(); // tOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterEM().logAll(); // emCtl
			parameter.getParameterMAP().logAll(); // mapCtrl
			parameter.getParameterVarianceControl().logAll(); // varCtrl
			logger.config(parameter.getSeparator());
		}
	}

}
