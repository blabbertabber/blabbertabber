/**
 * 
 * <p>
 * MTrainInit
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
 *          Program for the initialization of the GMMs
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
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.GMMFactory;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterInitializationEM.ModelInitializeMethod;

/**
 * The Class MTrainInit.
 */
public class MTrainInit {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MTrainInit.class.getName());

	/**
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 * @param parameter the parameter
	 * @throws Exception the exception
	 */
	public static void make(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmList, Parameter parameter) throws Exception {
		logger.info("Initialize models");

		// wld/UBM ?
		GMMArrayList ubmGmmList = new GMMArrayList();

		if (parameter.getParameterInitializationEM().getModelInitMethod().equals(ModelInitializeMethod.TRAININIT_COPY)) {
			ubmGmmList = MainTools.readGMMContainer(parameter);
			if (ubmGmmList.size() > 1) {
				throw new DiarizationException("error \t UBM input model is not unique ");
			}
		}

		// training

		int nbGmm = 0;
		for (String name : clusterSet) {
			Cluster cluster = clusterSet.getCluster(name);
			if (!parameter.getParameterInitializationEM().getModelInitMethod().equals(ModelInitializeMethod.TRAININIT_COPY)) {
				logger.fine("\t initialize cluster=" + cluster.getName());
				gmmList.add(GMMFactory.initializeGMM(name, cluster, featureSet, parameter.getParameterModel().getModelKind(), parameter.getParameterModel().getNumberOfComponents(), parameter.getParameterInitializationEM().getModelInitMethod(), parameter.getParameterEM(), parameter.getParameterVarianceControl(), parameter.getParameterInputFeature().useSpeechDetection()));
			} else {
				logger.fine("\t initialize(clone) cluster=" + cluster.getName());
				gmmList.add(ubmGmmList.get(0).clone());
				gmmList.get(nbGmm).setName(name);
			}
			nbGmm++;
		}
		for (int i = 0; i < gmmList.size(); i++) {
			logger.finest("resume : " + i + "=" + gmmList.get(i).getName() + "/" + gmmList.get(i).getName());
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
			info(parameter, "MTrainInit");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);

				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
				// Compute Model
				GMMArrayList gmmList = new GMMArrayList(clusterSet.clusterGetSize());

				make(featureSet, clusterSet, gmmList, parameter);

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

			parameter.getParameterInputFeature().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll();
			if (parameter.getParameterInitializationEM().getModelInitMethod().equals(ModelInitializeMethod.TRAININIT_COPY)) {
				parameter.getParameterModelSetInputFile().logAll(); // tInMask
			} else {
				parameter.getParameterModel().logAll(); // kind
			}
			parameter.getParameterModelSetOutputFile().logAll(); // tOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterInitializationEM().logAll(); // emInitMethod
			if (!((parameter.getParameterInitializationEM().getModelInitMethod().equals(ModelInitializeMethod.TRAININIT_COPY)) || (parameter.getParameterInitializationEM().getModelInitMethod().equals(ModelInitializeMethod.TRAININIT_UNIFORM)))) {
				logger.config(parameter.getSeparator());
				parameter.getParameterEM().logAll(); // emCtl
				parameter.getParameterVarianceControl().logAll(); // varCtrl
			}
			logger.config(parameter.getSeparator());
		}
	}

}