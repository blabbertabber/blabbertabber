/**
 * 
 * <p>
 * Diarization
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
 * 
 * 
 * 
 */
package fr.lium.experimental.spkDiarization.system;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.programs.MClust;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MSeg;
import fr.lium.spkDiarization.programs.MSegInit;
import fr.lium.spkDiarization.programs.MTrainEM;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.tools.SAdjSeg;
import fr.lium.spkDiarization.tools.SFilter;

/**
 * The Class Meeting.
 */
public class Meeting {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Meeting.class.getName());

	/**
	 * Load feature.
	 * 
	 * @param param the param
	 * @param clusters the clusters
	 * @param desc the desc
	 * @return the audio feature set
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static AudioFeatureSet loadFeature(Parameter param, ClusterSet clusters, String desc) throws IOException, DiarizationException {
		param.getParameterInputFeature().setFeaturesDescription(desc);
		return MainTools.readFeatureSet(param, clusters);
	}

	/**
	 * Load feature.
	 * 
	 * @param features the features
	 * @param param the param
	 * @param clusters the clusters
	 * @param desc the desc
	 * @return the audio feature set
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	private AudioFeatureSet loadFeature(AudioFeatureSet features, Parameter param, ClusterSet clusters, String desc) throws IOException, DiarizationException {
		param.getParameterInputFeature().setFeaturesDescription(desc);
		return MainTools.readFeatureSet(param, clusters, features);
	}

	/**
	 * Inits the.
	 * 
	 * @param param the param
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public ClusterSet init(Parameter param) throws DiarizationException, Exception {
		// ** mask for the output of the segmentation file
		String mask = param.getParameterSegmentationOutputFile().getMask();

		// ** get the first diarization
		ClusterSet clusters = null;
		clusters = new ClusterSet();
		Cluster clusterInit = clusters.createANewCluster("init");
		Segment segmentInit = new Segment(param.show, 0, 1, clusterInit, param.getParameterSegmentationInputFile().getRate());
		clusterInit.addSegment(segmentInit);

		// ** load the features, sphinx format (13 MFCC with C0) or compute it form a wave file
		AudioFeatureSet features = loadFeature(param, clusters, param.getParameterInputFeature().getFeaturesDescriptorAsString());
		features.debug();
		features.setCurrentShow(param.show);
		int nbFeatures = features.getNumberOfFeatures();
		logger.info("dim:" + features.getFeatureSize());
		logger.info("dim:" + nbFeatures);
		if (param.getParameterDiarization().isLoadInputSegmentation() == false) {
			clusters.getFirstCluster().firstSegment().setLength(nbFeatures);
		}

		// ** check the quality of the MFCC, remove similar consecutive MFCC of the featureSet
		ClusterSet clustersSegInit = new ClusterSet();
		MSegInit.make(features, clusters, clustersSegInit, param);
		clustersSegInit.collapse();
		param.getParameterSegmentationOutputFile().setMask(mask + ".i.seg");
		if (param.getParameterDiarization().isSaveAllStep()) {
			MainTools.writeClusterSet(param, clustersSegInit, false);
		}

		ClusterSet clusterSNS = new ClusterSet();
		Cluster clusterS = clusterSNS.createANewCluster("f0");
		Cluster clusterNS = clusterSNS.createANewCluster("iS");
		// param.getParameterSegmentation().setSilenceThreshold(0.1);
		for (Cluster cluster : clustersSegInit.clusterSetValue()) {
			double thr1 = Distance.getThreshold(cluster, features, 0.1, 0);
			double thr2 = Distance.getThreshold(cluster, features, 0.6, 0);

			for (Segment segment : cluster) {
				features.setCurrentShow(segment.getShowName());
				for (int i = segment.getStart(); i <= segment.getLast(); i++) {
					Segment newSegment = segment.clone();
					newSegment.setStart(i);
					newSegment.setLength(1);
					if (features.getFeatureUnsafe(i)[0] > thr2) {
						clusterS.addSegment(newSegment);
					}
					if (features.getFeatureUnsafe(i)[0] < thr1) {
						clusterNS.addSegment(newSegment);
					}

				}
			}
		}
		clusterSNS.collapse();
		ClusterSet previous = clustersSegInit;
		ClusterSet current = clusterSNS;
		int nb = 0;
		GMMArrayList gmmVect = new GMMArrayList();
		while (current.equals(previous) == false) {
			previous = current;
			param.getParameterModel().setModelKind("DIAG");
			param.getParameterModel().setNumberOfComponents(4);
			GMMArrayList gmmInitVect = new GMMArrayList(clusterSNS.clusterGetSize());
			MTrainInit.make(features, clusterSNS, gmmInitVect, param);
			// ** EM training of the initialized GMM
			gmmVect = new GMMArrayList(clusterSNS.clusterGetSize());
			MTrainEM.make(features, clusterSNS, gmmInitVect, gmmVect, param);

			// ** set the penalty to move from the state i to the state j, penalty to move from i to i is equal to 0
			param.getParameterDecoder().setDecoderPenalty("10");
			// ** make Viterbi decoding using the 8-GMM set
			// ** one state = one GMM = one speaker = one cluster
			current = MDecode.make(features, clustersSegInit, gmmVect, param);
			param.getParameterSegmentationOutputFile().setMask(mask + ".sns_" + nb + ".seg");
			if (param.getParameterDiarization().isSaveAllStep()) {
				MainTools.writeClusterSet(param, current, true);
			}
			nb++;
			break;
		}
		param.getParameterFilter().setSegmentPadding(25);
		param.getParameterFilter().setSilenceMinimumLength(50);
		param.getParameterFilter().setSpeechMinimumLength(10);
		param.getParameterSegmentationFilterFile().setClusterFilterName("iS");
		ClusterSet clustersFltClust = SFilter.make(clustersSegInit, current, param);
		if (param.getParameterDiarization().isSaveAllStep()) {
			param.getParameterSegmentationOutputFile().setMask(mask + ".flt.seg");
			MainTools.writeClusterSet(param, clustersFltClust, false);
			param.getParameterSegmentationOutputFile().setMask(mask);
		}

		return clustersFltClust;

	}

	/**
	 * Sanity check.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws TransformerException the transformer exception
	 */
	public ClusterSet sanityCheck(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException, ParserConfigurationException, SAXException, TransformerException {
		String mask = parameter.getParameterSegmentationOutputFile().getMask();

		ClusterSet clustersSegInit = new ClusterSet();
		MSegInit.make(featureSet, clusterSet, clustersSegInit, parameter);
		clustersSegInit.collapse();
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".i.seg");
			MainTools.writeClusterSet(parameter, clustersSegInit, false);
		}

		parameter.getParameterSegmentationOutputFile().setMask(mask);

		return clustersSegInit;
	}

	/**
	 * Icsi version.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void icsiVersion(Parameter parameter) throws DiarizationException, Exception {
		// ClusterSet clusterSetUEM = init(param);
		ClusterSet clusterSetBase = MainTools.readClusterSet(parameter);
		AudioFeatureSet featureSet = loadFeature(parameter, clusterSetBase, parameter.getParameterInputFeature().getFeaturesDescriptorAsString());

		ClusterSet clusterSetUEM = sanityCheck(clusterSetBase, featureSet, parameter);
		clusterSetUEM.collapse();
		featureSet.setClusterSet(clusterSetUEM);
		featureSet.setCurrentShow(parameter.show);
		// featureSet.debug(2);

		// pour 10 minutes
		int numberOfPart = 2;
		int numberOfInitialCluster = 16;
		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		// param.getParameterDiarization().setSaveAllStep(true);

		int nbFeaturesInClusterSet = clusterSetUEM.getLength();
		if (nbFeaturesInClusterSet > (15 * 60 * 100)) {
			numberOfPart = nbFeaturesInClusterSet / (60 * 100 * 5);
			numberOfInitialCluster = numberOfPart * 8;
		}

		float duration = nbFeaturesInClusterSet / (60 * 100);
		int sizeOfAPart = nbFeaturesInClusterSet / (numberOfPart * numberOfInitialCluster);
		logger.info(" ---> nb feature: " + duration + " minutes, nb of part: " + numberOfPart
				+ ", nb of Initial cluster: " + numberOfInitialCluster);
		TreeMap<Integer, Segment> map = clusterSetUEM.getFeatureMap();
		Iterator<Integer> it = map.keySet().iterator();
		ClusterSet clusterSetInit = new ClusterSet();
		for (int i = 0; i < numberOfPart; i++) {
			for (int l = 0; l < numberOfInitialCluster; l++) {
				Cluster cluster = clusterSetInit.getOrCreateANewCluster("S" + l);
				for (int j = 0; j < sizeOfAPart; j++) {
					int idx = it.next();
					Segment segment = new Segment(map.get(idx).getShowName(), idx, 1, cluster, parameter.getParameterSegmentationInputFile().getRate());
					cluster.addSegment(segment);
				}
			}
		}
		clusterSetInit.collapse();
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".i2.seg");
			MainTools.writeClusterSet(parameter, clusterSetInit, false);
		}

		// ReSeg
		ClusterSet currentClusterSet = clusterSetInit;
		GMMArrayList currentGMMSet = new GMMArrayList();

		ClusterSet previousClusterSet = new ClusterSet();
		GMMArrayList previousGMMSet = new GMMArrayList();

		parameter.getParameterModel().setModelKind("DIAG");
		parameter.getParameterModel().setNumberOfComponents(5);
		parameter.getParameterInitializationEM().setEMControl("1,5,0.0");
		parameter.getParameterEM().setEMControl("1,5,0.0");
		parameter.getParameterInitializationEM().setModelInitMethod("uniform");
		// param.getParameterVarianceControl().setFlooring(0.01);
		double loopPenalty = -Math.log(0.9);
		double exitPenalty = -Math.log(0.1);
// double loopPenalty = 0.0;
// double exitPenalty = 0.0;
// param.getParameterDecoder().setShift(30);
		parameter.getParameterDecoder().setDecoderPenalty(exitPenalty + ":" + loopPenalty);
		parameter.getParameterDecoder().setViterbiDurationConstraints("minimal,250");
		logger.info("[ICSI] init model");
		MTrainInit.make(featureSet, currentClusterSet, previousGMMSet, parameter);

		logger.info("[ICSI] train first model");
		MTrainEM.make(featureSet, currentClusterSet, previousGMMSet, currentGMMSet, parameter);

		int nbD = 0;
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".d." + nbD + ".seg");
			MainTools.writeClusterSet(parameter, currentClusterSet, false);
			nbD++;
		}
		boolean first = true;
		double maxScore = -Double.MAX_VALUE;
		int nbMerge = 0;
		do {
			if (parameter.getParameterDiarization().isSaveAllStep()) {
				parameter.getParameterSegmentationOutputFile().setMask(mask + ".m." + nbMerge + ".seg");
				MainTools.writeClusterSet(parameter, currentClusterSet, true);
				nbMerge++;
			}
			int n = 3;
			if (first) {
				n = 5;
				first = false;
			}
			nbD = 0;
			while ((currentClusterSet.equals(previousClusterSet) == false) && (n > 0)) {
				previousClusterSet = currentClusterSet;
				logger.info("[ICSI] decode");
				exitPenalty = -Math.log(0.1 / currentGMMSet.size());

				parameter.getParameterDecoder().setDecoderPenalty(exitPenalty + ":" + loopPenalty);
				currentClusterSet = MDecode.make(featureSet, clusterSetUEM, currentGMMSet, parameter);
// currentClusterSet = MDecode.make(featureSet, previousClusterSet, currentGMMSet, param);
				logger.finer("** segment " + n);
				if (parameter.getParameterDiarization().isSaveAllStep()) {
					parameter.getParameterSegmentationOutputFile().setMask(mask + ".m." + nbMerge + ".d." + nbD
							+ ".seg");
					MainTools.writeClusterSet(parameter, currentClusterSet, false);
					nbD++;
				}

				previousGMMSet = currentGMMSet;
				currentGMMSet = new GMMArrayList();
				logger.info("[ICSI] train");
				GMMArrayList tmpGMM = new GMMArrayList();
				for (int i = 0; i < previousGMMSet.size(); i++) {
					boolean add = true;
					if (currentClusterSet.containsCluster(previousGMMSet.get(i).getName()) == false) {
						add = false;
					} else {
						GMM gmm = previousGMMSet.get(i);
						Cluster cluster = currentClusterSet.getCluster(gmm.getName());
						if (cluster.getLength() <= (gmm.getNbOfComponents() * 30)) {
							add = false;
							currentClusterSet.removeCluster(gmm.getName());
							logger.finer("*** remove model");
						}
					}

					if (add == true) {
						tmpGMM.add(previousGMMSet.get(i));
					}
				}
				previousGMMSet = tmpGMM;
				MTrainEM.make(featureSet, currentClusterSet, previousGMMSet, currentGMMSet, parameter);
				n--;
			}
			maxScore = -Double.MAX_VALUE;
			String maxClusterNameI = null;
			String maxClusterNameJ = null;
			int maxJ = 0;
			int maxI = 0;
			for (int i = 0; i < (currentGMMSet.size() - 1); i++) {
				GMM gmmI = currentGMMSet.get(i);
				String clusterNameI = gmmI.getName();
				Cluster clusterI = currentClusterSet.getCluster(clusterNameI);
				for (int j = i + 1; j < currentGMMSet.size(); j++) {
					GMM gmmJ = currentGMMSet.get(j);
					String clusterNameJ = gmmJ.getName();
					Cluster clusterJ = currentClusterSet.getCluster(clusterNameJ);
					double score = Distance.GLR_ICSI(gmmI, clusterI, gmmJ, clusterJ, featureSet, parameter);
					logger.info("distance : " + clusterNameI + "/" + clusterNameJ + " score:" + score + " " + i + "/"
							+ j);
					if (score > maxScore) {
						maxScore = score;
						maxClusterNameI = clusterNameI;
						maxClusterNameJ = clusterNameJ;
						maxJ = j;
						maxI = i;
					}
				}
			}
			if (maxScore > 0.0) {
				logger.info("---------------------------------");
				logger.info("Merge : " + nbMerge + " " + maxClusterNameI + " " + maxClusterNameJ + " score:" + maxScore
						+ " " + maxI + "/" + maxJ);
				currentClusterSet.mergeCluster(maxClusterNameI, maxClusterNameJ);
				// currentClusterSet.removeCluster(maxClusterNameJ);
				GMM gmmI = currentGMMSet.get(maxI);
				GMM gmmJ = currentGMMSet.remove(maxJ);
				for (int i = 0; i < gmmJ.getNbOfComponents(); i++) {
					gmmI.addComponent(gmmJ.getComponent(i));
				}
				gmmI.normalizeWeights();
				for (int i = 0; i < currentGMMSet.size(); i++) {
					GMM gmm = currentGMMSet.get(i);
					logger.info("Model : " + i + " " + gmm.getName() + " nb:" + gmm.getNbOfComponents()
							+ " nb features:" + gmm.score_getCount());

				}
			}
		} while ((maxScore > 0.0) && (currentClusterSet.clusterGetSize() > 1));
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".m." + nbMerge + ".seg");
			MainTools.writeClusterSet(parameter, currentClusterSet, true);
			nbMerge++;
		}

		parameter.getParameterSegmentationOutputFile().setMask(mask);
		MainTools.writeClusterSet(parameter, currentClusterSet, true);

	}

	/**
	 * Icsi init.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void icsiInit(Parameter parameter) throws DiarizationException, Exception {
		// ClusterSet clusterSetUEM = init(param);
		ClusterSet clusterSetBase = MainTools.readClusterSet(parameter);
		AudioFeatureSet featureSet = loadFeature(parameter, clusterSetBase, parameter.getParameterInputFeature().getFeaturesDescriptorAsString());

		ClusterSet clusterSetUEM = sanityCheck(clusterSetBase, featureSet, parameter);
		clusterSetUEM.collapse();
		featureSet.setClusterSet(clusterSetUEM);
		featureSet.setCurrentShow(parameter.show);
		// featureSet.debug(2);

		// pour 10 minutes
		int numberOfPart = 2;
		int numberOfInitialCluster = 16;
		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		// param.getParameterDiarization().setSaveAllStep(true);

		int nbFeaturesInClusterSet = clusterSetUEM.getLength();
		if (nbFeaturesInClusterSet > (15 * 60 * 100)) {
			numberOfPart = nbFeaturesInClusterSet / (60 * 100 * 5);
			numberOfInitialCluster = numberOfPart * 8;
		}

		float duration = nbFeaturesInClusterSet / (60 * 100);
		int sizeOfAPart = nbFeaturesInClusterSet / (numberOfPart * numberOfInitialCluster);
		logger.info(" ---> nb feature: " + duration + " minutes, nb of part: " + numberOfPart
				+ ", nb of Initial cluster: " + numberOfInitialCluster);
		TreeMap<Integer, Segment> map = clusterSetUEM.getFeatureMap();
		Iterator<Integer> it = map.keySet().iterator();
		ClusterSet clusterSetInit = new ClusterSet();
		for (int i = 0; i < numberOfPart; i++) {
			for (int l = 0; l < numberOfInitialCluster; l++) {
				Cluster cluster = clusterSetInit.getOrCreateANewCluster("S" + l);
				for (int j = 0; j < sizeOfAPart; j++) {
					int idx = it.next();
					Segment segment = new Segment(map.get(idx).getShowName(), idx, 1, cluster, parameter.getParameterSegmentationInputFile().getRate());
					cluster.addSegment(segment);
				}
			}
		}
		clusterSetInit.collapse();
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".i2.seg");
			MainTools.writeClusterSet(parameter, clusterSetInit, false);
		}

		// ReSeg
		ClusterSet currentClusterSet = clusterSetInit;
		GMMArrayList currentGMMSet = new GMMArrayList();

		ClusterSet previousClusterSet = new ClusterSet();
		GMMArrayList previousGMMSet = new GMMArrayList();

		parameter.getParameterModel().setModelKind("DIAG");
		parameter.getParameterModel().setNumberOfComponents(5);
		parameter.getParameterInitializationEM().setEMControl("1,5,0.0");
		parameter.getParameterEM().setEMControl("1,5,0.0");
		parameter.getParameterInitializationEM().setModelInitMethod("uniform");
		// param.getParameterVarianceControl().setFlooring(0.01);
		double loopPenalty = -Math.log(0.9);
		double exitPenalty = -Math.log(0.1);
// double loopPenalty = 0.0;
// double exitPenalty = 0.0;
// param.getParameterDecoder().setShift(30);
		parameter.getParameterDecoder().setDecoderPenalty(exitPenalty + ":" + loopPenalty);
// - parameter.getParameterDecoder().setViterbiDurationConstraints("minimal,250");
		parameter.getParameterDecoder().setViterbiDurationConstraints("minimal,75");
		logger.info("[ICSI] init model");
		MTrainInit.make(featureSet, currentClusterSet, previousGMMSet, parameter);

		logger.info("[ICSI] train first model");
		MTrainEM.make(featureSet, currentClusterSet, previousGMMSet, currentGMMSet, parameter);

		int nbD = 0;
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".d." + nbD + ".seg");
			MainTools.writeClusterSet(parameter, currentClusterSet, false);
			nbD++;
		}
		int nbMerge = 0;
		int n = 20;
		while ((currentClusterSet.equals(previousClusterSet) == false) && (n > 0)) {
			previousClusterSet = currentClusterSet;
			logger.info("[ICSI] decode");
			exitPenalty = -Math.log(0.1 / currentGMMSet.size());

			parameter.getParameterDecoder().setDecoderPenalty(exitPenalty + ":" + loopPenalty);
			currentClusterSet = MDecode.make(featureSet, clusterSetUEM, currentGMMSet, parameter);
// currentClusterSet = MDecode.make(featureSet, previousClusterSet, currentGMMSet, param);
			logger.finer("** segment " + n);
			if (parameter.getParameterDiarization().isSaveAllStep()) {
				parameter.getParameterSegmentationOutputFile().setMask(mask + ".m." + nbMerge + ".d." + nbD + ".seg");
				MainTools.writeClusterSet(parameter, currentClusterSet, false);
				nbD++;
			}

			previousGMMSet = currentGMMSet;
			currentGMMSet = new GMMArrayList();
			logger.info("[ICSI] train");
			GMMArrayList tmpGMM = new GMMArrayList();
			for (int i = 0; i < previousGMMSet.size(); i++) {
				boolean add = true;
				if (currentClusterSet.containsCluster(previousGMMSet.get(i).getName()) == false) {
					add = false;
				} else {
					GMM gmm = previousGMMSet.get(i);
					Cluster cluster = currentClusterSet.getCluster(gmm.getName());
					if (cluster.getLength() <= (gmm.getNbOfComponents() * 30)) {
						add = false;
						currentClusterSet.removeCluster(gmm.getName());
						logger.finer("*** remove model");
					}
				}

				if (add == true) {
					tmpGMM.add(previousGMMSet.get(i));
				}
			}
			previousGMMSet = tmpGMM;
			MTrainEM.make(featureSet, currentClusterSet, previousGMMSet, currentGMMSet, parameter);
			n--;
		}

		parameter.getParameterSegmentationOutputFile().setMask(mask);
		MainTools.writeClusterSet(parameter, currentClusterSet, true);

	}

	/**
	 * Icsi init2.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void icsiInit2(Parameter parameter) throws DiarizationException, Exception {
		// ClusterSet clusterSetUEM = init(param);
		ClusterSet clusterSetBase = MainTools.readClusterSet(parameter);
		AudioFeatureSet featureSet = loadFeature(parameter, clusterSetBase, parameter.getParameterInputFeature().getFeaturesDescriptorAsString());

		ClusterSet clusterSetUEM = sanityCheck(clusterSetBase, featureSet, parameter);
		clusterSetUEM.collapse();
		featureSet.setClusterSet(clusterSetUEM);
		featureSet.setCurrentShow(parameter.show);
		// featureSet.debug(2);

		// pour 10 minutes
		int numberOfPart = 2;
		int numberOfInitialCluster = 16;
		String mask = parameter.getParameterSegmentationOutputFile().getMask();
		// param.getParameterDiarization().setSaveAllStep(true);

		ClusterSet clusterSetResult = new ClusterSet();

		int nbUEM = 0;

		for (Segment segmentUEM : clusterSetUEM.getSegmentVectorRepresentation()) {
			ClusterSet clusterSetDecode = new ClusterSet();
			Cluster clusterDecode = clusterSetDecode.createANewCluster("init");
			clusterDecode.addSegment(segmentUEM.clone());
			nbUEM++;
			int nbFeaturesInSegmentUEM = segmentUEM.getLength();
			int nb10mBlock = (int) Math.ceil(nbFeaturesInSegmentUEM / (15.0 * 60 * 100));
			int sizeBlock = nbFeaturesInSegmentUEM / nb10mBlock;

// TreeMap<Integer, Segment> map = clusterSetUEM.getFeatureMap();
// Iterator<Integer> it = map.keySet().iterator();

			int idx = segmentUEM.getStart();

			for (int block = 0; block < nb10mBlock; block++) {
				int sizeOfAPart = sizeBlock / (numberOfPart * numberOfInitialCluster);
				ClusterSet clusterSetInit = new ClusterSet();
				for (int i = 0; i < numberOfPart; i++) {
					for (int l = 0; l < numberOfInitialCluster; l++) {
						Cluster cluster = clusterSetInit.getOrCreateANewCluster("S_" + nbUEM + "_" + block + "_" + l);
						for (int j = 0; (j < sizeOfAPart) && (idx < (segmentUEM.getLength() + segmentUEM.getStart())); j++) {
							Segment segment = new Segment(segmentUEM.getShowName(), idx, 1, cluster, parameter.getParameterSegmentationInputFile().getRate());
							cluster.addSegment(segment);
							idx++;
						}
					}
				}
				clusterSetInit.collapse();
				if (parameter.getParameterDiarization().isSaveAllStep()) {
					parameter.getParameterSegmentationOutputFile().setMask(mask + ".i." + nbUEM + "_" + block + ".seg");
					MainTools.writeClusterSet(parameter, clusterSetInit, false);
				}

				// ReSeg
				ClusterSet currentClusterSet = clusterSetInit;
				GMMArrayList currentGMMSet = new GMMArrayList();

				ClusterSet previousClusterSet = new ClusterSet();
				GMMArrayList previousGMMSet = new GMMArrayList();

				parameter.getParameterModel().setModelKind("DIAG");
				parameter.getParameterModel().setNumberOfComponents(5);
				parameter.getParameterInitializationEM().setEMControl("1,5,0.0");
				parameter.getParameterEM().setEMControl("1,5,0.0");
				parameter.getParameterInitializationEM().setModelInitMethod("uniform");
				double loopPenalty = -Math.log(0.9);
				double exitPenalty = -Math.log(0.1);
				parameter.getParameterDecoder().setDecoderPenalty(exitPenalty + ":" + loopPenalty);
				parameter.getParameterDecoder().setViterbiDurationConstraints("minimal,150");
				logger.info("[ICSI] init model");
				MTrainInit.make(featureSet, currentClusterSet, previousGMMSet, parameter);

				logger.info("[ICSI] train first model");
				MTrainEM.make(featureSet, currentClusterSet, previousGMMSet, currentGMMSet, parameter);

				int nbD = 0;
				int n = 50;
				while ((currentClusterSet.equals(previousClusterSet) == false) && (n > 0)) {
					previousClusterSet = currentClusterSet;
					logger.info("[ICSI] decode");
					exitPenalty = -Math.log(0.1 / currentGMMSet.size());

					parameter.getParameterDecoder().setDecoderPenalty(exitPenalty + ":" + loopPenalty);
					currentClusterSet = MDecode.make(featureSet, clusterSetDecode, currentGMMSet, parameter);
					logger.finer("** segment " + n);
					if (parameter.getParameterDiarization().isSaveAllStep()) {
						parameter.getParameterSegmentationOutputFile().setMask(mask + ".m." + nbUEM + "_" + block
								+ ".d." + nbD + ".seg");
						MainTools.writeClusterSet(parameter, currentClusterSet, false);
						nbD++;
					}

					previousGMMSet = currentGMMSet;
					currentGMMSet = new GMMArrayList();
					logger.info("[ICSI] train");
					GMMArrayList tmpGMM = new GMMArrayList();
					for (int i = 0; i < previousGMMSet.size(); i++) {
						boolean add = true;
						if (currentClusterSet.containsCluster(previousGMMSet.get(i).getName()) == false) {
							add = false;
						} else {
							GMM gmm = previousGMMSet.get(i);
							Cluster cluster = currentClusterSet.getCluster(gmm.getName());
							if (cluster.getLength() <= (gmm.getNbOfComponents() * 30)) {
								add = false;
								currentClusterSet.removeCluster(gmm.getName());
								logger.finer("*** remove model");
							}
						}

						if (add == true) {
							tmpGMM.add(previousGMMSet.get(i));
						}
					}
					previousGMMSet = tmpGMM;
					MTrainEM.make(featureSet, currentClusterSet, previousGMMSet, currentGMMSet, parameter);
					n--;
				}
				clusterSetResult.addVector(currentClusterSet.getSegmentVectorRepresentation());
			}
		}
		parameter.getParameterSegmentationOutputFile().setMask(mask);
		MainTools.writeClusterSet(parameter, clusterSetResult, true);

	}

	/**
	 * Baseline version.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void baselineVersion(Parameter parameter) throws DiarizationException, Exception {
		// ** Caution this system is developed using Sphinx MFCC computed with legacy mode

		String dir = "ester2";
		// ** mask for the output of the segmentation file
		String mask = parameter.getParameterSegmentationOutputFile().getMask();

		// ** get the first diarization
		ClusterSet clusters = null;
		if (parameter.getParameterDiarization().isLoadInputSegmentation()) {
			clusters = MainTools.readClusterSet(parameter);
		} else {
			clusters = new ClusterSet();
			Cluster cluster = clusters.createANewCluster("init");
			Segment segment = new Segment(parameter.show, 0, 1, cluster, parameter.getParameterSegmentationInputFile().getRate());
			cluster.addSegment(segment);
		}

		// ** load the features, sphinx format (13 MFCC with C0) or compute it form a wave file
		AudioFeatureSet features = loadFeature(parameter, clusters, parameter.getParameterInputFeature().getFeaturesDescriptorAsString());
		features.setCurrentShow(parameter.show);
		int nbFeatures = features.getNumberOfFeatures();
		String FeatureFormat = "featureSetTransformation";
		logger.info("dim:" + features.getFeatureSize());
		logger.info("dim:" + nbFeatures);
		if (parameter.getParameterDiarization().isLoadInputSegmentation() == false) {
			clusters.getFirstCluster().firstSegment().setLength(nbFeatures);
		}

		// ** check the quality of the MFCC, remove similar consecutive MFCC of the featureSet
		ClusterSet clustersSegInit = new ClusterSet();
		MSegInit.make(features, clusters, clustersSegInit, parameter);
		clustersSegInit.collapse();
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".i.seg");
			MainTools.writeClusterSet(parameter, clustersSegInit, false);
		}

		// ** segmentation using gaussian with full covariance matrix and GLR metric
		parameter.getParameterSegmentation().setMethod("GLR");
		parameter.getParameterSegmentation().setModelWindowSize(200);
		parameter.getParameterSegmentation().setMinimimWindowSize(150);
		parameter.getParameterModel().setNumberOfComponents(1);
		parameter.getParameterModel().setModelKind("FULL");
		ClusterSet clustersSeg = new ClusterSet();
		MSeg.make(features, clustersSegInit, clustersSeg, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".s.seg");
			MainTools.writeClusterSet(parameter, clustersSeg, false);
		}

		// ** merge neighbour segment according a BIC metric
		parameter.getParameterClustering().setMethod("l");
		parameter.getParameterClustering().setThreshold(2);
		ClusterSet clustersLClust = MClust.make(features, clustersSeg, parameter, null);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".l.seg");
			MainTools.writeClusterSet(parameter, clustersLClust, false);
		}

		// ** BIC bottom up hierarchical classification using gaussian with full covariance matrix
		parameter.getParameterClustering().setMethod("h");
		parameter.getParameterClustering().setThreshold(3);
		// param.getParameterClustering().setThreshold(3.5);
		ClusterSet clustersHClust = MClust.make(features, clustersLClust, parameter, null);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".h.seg");
			MainTools.writeClusterSet(parameter, clustersHClust, false);
		}

		// ** Train GMM for each cluster.
		// ** GMM is a 8 component gaussian with diagonal covariance matrix
		// ** one GMM = one speaker = one cluster
		// ** initialization of the GMMs :
		// ** - same global covariance for each gaussian,
		// ** - 1/8 for the weight,
		// ** - means are initialized with the mean of 10 successive vectors taken
		parameter.getParameterModel().setModelKind("DIAG");
		parameter.getParameterModel().setNumberOfComponents(8);
		GMMArrayList gmmInitVect = new GMMArrayList(clustersHClust.clusterGetSize());
		MTrainInit.make(features, clustersHClust, gmmInitVect, parameter);
		// ** EM training of the initialized GMM
		GMMArrayList gmmVect = new GMMArrayList(clustersHClust.clusterGetSize());
		MTrainEM.make(features, clustersHClust, gmmInitVect, gmmVect, parameter);

		// ** set the penalty to move from the state i to the state j, penalty to move from i to i is equal to 0
		parameter.getParameterDecoder().setDecoderPenalty("250");
		// ** make Viterbi decoding using the 8-GMM set
		// ** one state = one GMM = one speaker = one cluster
		ClusterSet clustersDClust = MDecode.make(features, clustersHClust, gmmVect, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".d.seg");
			MainTools.writeClusterSet(parameter, clustersDClust, false);
		}

		// ** move the boundaries of the segment in low energy part of the signal
		ClusterSet clustersAdjClust = SAdjSeg.make(features, clustersDClust, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".adj.seg");
			MainTools.writeClusterSet(parameter, clustersAdjClust, false);
		}

		// ** bottom up hierarchical classification using GMMs
		// ** one for each cluster, trained by MAP adaptation of a UBM composed of the fusion of 4x128GMM
		// ** the feature normalization use feature mapping technique, after the cluster frames are centered and reduced
		AudioFeatureSet features2 = loadFeature(features, parameter, clustersAdjClust, FeatureFormat
				+ ",1:3:2:0:0:0,13,1:1:300:4");
		InputStream ubmInputStream = getClass().getResourceAsStream(dir + "/ubm.gmm");
		GMMArrayList ubmVect = MainTools.readGMMContainer(ubmInputStream, parameter.getParameterModel());
		GMM ubm = ubmVect.get(0);
		parameter.getParameterClustering().setMethod("ce");
		parameter.getParameterClustering().setThreshold(1.7);
		parameter.getParameterEM().setEMControl("1,5,0.01");
		parameter.getParameterTopGaussian().setScoreNTop(5);
		boolean saveAll = parameter.getParameterDiarization().isSaveAllStep();
		parameter.getParameterDiarization().setSaveAllStep(false);
		ClusterSet clustersCLR = MClust.make(features2, clustersAdjClust, parameter, ubm);
		parameter.getParameterDiarization().setSaveAllStep(saveAll);

		parameter.getParameterSegmentationOutputFile().setMask(mask + ".c.seg.toto2");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".c.seg");
			MainTools.writeClusterSet(parameter, clustersCLR, false);
		}
		parameter.getParameterSegmentationOutputFile().setMask(mask);
		MainTools.writeClusterSet(parameter, clustersCLR, false);
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = new Parameter();
			parameter.getParameterInputFeature().setFeaturesDescription("audio16kHz2sphinx,1:1:0:0:0:0,13,0:0:0:0");
			parameter.readParameters(args);
			if (args.length <= 1) {
				parameter.help = true;
			}
			parameter.logCmdLine(args);
			info(parameter, "Meeting");

			// parametre icsi
			// param.getParameterInputFeature().setFeaturesDescription("htk,1:0:0:0:0:0,19,0:0:0:0");

			if (parameter.show.isEmpty() == false) {
				Meeting diarization = new Meeting();
				// diarization.baselineVersion(param);
// - diarization.icsiVersion(parameter);
				diarization.icsiInit2(parameter);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}

	}

	/**
	 * Info.
	 * 
	 * @param param the param
	 * @param prog the prog
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter param, String prog) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		// if (param.help) {
		logger.config(param.getSeparator2());
		logger.config("Program name = " + prog);
		logger.config(param.getSeparator());
		param.logShow();

		param.getParameterInputFeature().logAll(); // fInMask
		logger.config(param.getSeparator());
		param.getParameterSegmentationInputFile().logAll(); // sInMask
		param.getParameterSegmentationOutputFile().logAll(); // sOutMask
		logger.config(param.getSeparator());
		param.getParameterDiarization().logAll();
		logger.config(param.getSeparator());
		// }
	}

}
