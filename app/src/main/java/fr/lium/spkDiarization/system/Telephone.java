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

package fr.lium.spkDiarization.system;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.programs.MClust;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MScore;
import fr.lium.spkDiarization.programs.MSeg;
import fr.lium.spkDiarization.programs.MSegInit;
import fr.lium.spkDiarization.programs.MTrainEM;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.tools.SAdjSeg;
import fr.lium.spkDiarization.tools.SFilter;
import fr.lium.spkDiarization.tools.SSplitSeg;

/**
 * The Class Telephone.
 */
public class Telephone {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Telephone.class.getName());

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
	 * Make old.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void makeOld(Parameter parameter) throws DiarizationException, Exception {
		// ** Caution this system is developed using Sphinx MFCC computed with legacy mode

		String dir = "tel";
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
		logger.finest("dim:" + features.getFeatureSize());
		logger.finest("dim:" + nbFeatures);
		if (parameter.getParameterDiarization().isLoadInputSegmentation() == false) {
			clusters.getFirstCluster().firstSegment().setLength(nbFeatures);
		}

		// ** check the quality of the MFCC, remove similar consecutive MFCC of the featureSet
		ClusterSet clustersSegInit = new ClusterSet();
		MSegInit.make(features, clusters, clustersSegInit, parameter);
		clustersSegInit.collapse();
		parameter.getParameterSegmentationOutputFile().setMask(mask + ".i.seg");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			MainTools.writeClusterSet(parameter, clustersSegInit, false);
		}

		// ** segmentation using gaussian with full covariance matrix and GLR metric
		parameter.getParameterSegmentation().setMethod("GLR");
		parameter.getParameterModel().setNumberOfComponents(1);
		parameter.getParameterModel().setModelKind("FULL");
		ClusterSet clustersSeg = new ClusterSet();
		MSeg.make(features, clustersSegInit, clustersSeg, parameter);
		parameter.getParameterSegmentationOutputFile().setMask(mask + ".s.seg");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			MainTools.writeClusterSet(parameter, clustersSeg, false);
		}

		// ** merge neighbour segment according a BIC metric
		parameter.getParameterClustering().setMethod("l");
		parameter.getParameterClustering().setThreshold(2);
		ClusterSet clustersLClust = MClust.make(features, clustersSeg, parameter, null);
		parameter.getParameterSegmentationOutputFile().setMask(mask + ".l.seg");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			MainTools.writeClusterSet(parameter, clustersLClust, false);
		}

		// ** BIC bottom up hierarchical classification using gaussian with full covariance matrix
		parameter.getParameterClustering().setMethod("h");
		parameter.getParameterClustering().setThreshold(6);
		parameter.getParameterClustering().setMinimumOfCluster(2);
		ClusterSet clustersHClust = MClust.make(features, clustersLClust, parameter, null);
		parameter.getParameterSegmentationOutputFile().setMask(mask + ".h.seg");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
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
		parameter.getParameterSegmentationOutputFile().setMask(mask + ".d.seg");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			MainTools.writeClusterSet(parameter, clustersDClust, false);
		}

		// ** move the boundaries of the segment in low energy part of the signal
		ClusterSet clustersAdjClust = SAdjSeg.make(features, clustersDClust, parameter);
		parameter.getParameterSegmentationOutputFile().setMask(mask + ".adj.seg");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			MainTools.writeClusterSet(parameter, clustersAdjClust, false);
		}

		/*
		 * // ** Detection of speech, music, jingle using a Viterbi decoding // ** Reload MFCC, remove energy and add delta FeatureSet features2 = loadFeature(features, param, clusters, FeatureFormat + ",1:3:2:0:0:0,13,0:0:0:0"); // ** load the model
		 * : 8 GMMs with 64 diagonal components URL pmsURL = getClass().getResource(dir + File.separator + "sms.gmms"); GMMArrayList pmsVect = MainTools.getInputGMMContainer(pmsURL, param.parameterModel); // ** set penalties for the i to j states //
		 * ** 10 for the first and second model corresponding to boad/narrowband silence // ** 50 for the other jingle speech (f0 f2 f3 fx), jingle and music param.getParameterDecoder().setDecodePenalty("10,10,50"); ClusterSet clustersPMSClust =
		 * MDecode.make(features2, clustersSegInit, pmsVect, param); param.getParameterSegmentationOutputFile().setMask(mask + ".sms.seg"); if (param.getParameterDiarization().isSaveAllStep()) { MainTools.setOutputClusters(param, clustersPMSClust,
		 * false); } // ** Filter the segmentation adj acoording the sms segmentation // ** add 25 frames to all speech segments // ** remove silence part if silence segment is less than 25 frames // ** if a speech segment is less than 150 frames, it
		 * will be merge to the left or right closest segments param.getParameterFilter().setSegmentPadding(25); param.getParameterFilter().setSilenceMinimumLength(25); param.getParameterFilter().setSpeechMinimumLength(150); ClusterSet
		 * clustersFltClust = SFilter.make(clustersAdjClust, clustersPMSClust, param); param.getParameterSegmentationOutputFile().setMask(mask + ".flt.seg"); if (param.getParameterDiarization().isSaveAllStep()) { MainTools.setOutputClusters(param,
		 * clustersFltClust, false); } // ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector URL sURL = getClass().getResource(dir + File.separator + "s.gmms"); GMMArrayList sVect =
		 * MainTools.getInputGMMContainer(sURL, param.parameterModel); param.getParameterSegmentationFilterFile().setClusterFilterName("iS,iT,j"); ClusterSet clustersSplitClust = SSplitSeg.make(features2, clustersFltClust, sVect, clustersPMSClust,
		 * param); param.getParameterSegmentationOutputFile().setMask(mask + ".spl.seg"); if (param.getParameterDiarization().isSaveAllStep()) { MainTools.setOutputClusters(param, clustersSplitClust, false); }
		 */

		// ** gender and band detection using 4 GMMs with 64 components with diagonal covariance matrix
		AudioFeatureSet features2 = loadFeature(features, parameter, clusters, FeatureFormat
				+ ",1:3:2:0:0:0,13,1:1:0:0");
		InputStream genderURL = getClass().getResourceAsStream(dir + "/gender.gmms");
		GMMArrayList genderVector = MainTools.readGMMContainer(genderURL, parameter.getParameterModel());
		parameter.getParameterScore().setByCluster(true);
		parameter.getParameterScore().setGender(true);
		ClusterSet clustersGender = MScore.make(features2, clustersAdjClust, genderVector, null, parameter);
		parameter.getParameterSegmentationOutputFile().setMask(mask + ".g.seg");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			MainTools.writeClusterSet(parameter, clustersGender, false);
		}

		parameter.getParameterSegmentationOutputFile().setMask(mask);
		MainTools.writeClusterSet(parameter, clustersGender, false);

		/*
		 * if (param.getParameterDiarization().isCEClustering()) { // ** bottom up hierarchical classification using GMMs // ** one for each cluster, trained by MAP adaptation of a UBM composed of the fusion of 4x128GMM // ** the feature
		 * normalization use feature mapping technique, after the cluster frames are centered and reduced features2 = loadFeature(features, param, clustersGender, FeatureFormat + ",1:3:2:0:0:0,13,1:1:300:4"); URL ubmURL = getClass().getResource(dir +
		 * File.separator + "ubm.gmm"); GMMArrayList ubmVect = MainTools.getInputGMMContainer(ubmURL, param.parameterModel); GMM ubm = ubmVect.get(0); param.getParameterClustering().setMethod("ce"); param.getParameterClustering().setThreshold(1.7);
		 * param.getParameterSegmentationOutputFile().setMask(mask + ".c.seg"); param.getParameterEM().setEMControl("1,5,0.01"); param.getParameterTopGaussian().setScoreNTop(5); ClusterSet clustersCLR = MClust.make(features2, clustersGender, param,
		 * ubm); if (param.getParameterDiarization().isSaveAllStep()) { param.getParameterSegmentationOutputFile().setMask(mask); } MainTools.setOutputClusters(param, clustersCLR, false); param.getParameterSegmentationOutputFile().setMask(mask); }
		 * else { }
		 */
	}

	/**
	 * Make media.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void makeMedia(Parameter parameter) throws DiarizationException, Exception {
		// ** Caution this system is developed using Sphinx MFCC computed with legacy mode

		parameter.help = true;
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
		logger.finest("dim:" + features.getFeatureSize());
		logger.finest("dim:" + nbFeatures);
		if (parameter.getParameterDiarization().isLoadInputSegmentation() == false) {
			clusters.getFirstCluster().firstSegment().setLength(nbFeatures);
		}

		// ** check the quality of the MFCC, remove similar consecutive MFCC of the featureSet
		ClusterSet clustersSegInit = new ClusterSet();
		MSegInit.make(features, clusters, clustersSegInit, parameter);
		clustersSegInit.collapse();
		parameter.getParameterSegmentationOutputFile().setMask(mask + ".i.seg");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			MainTools.writeClusterSet(parameter, clustersSegInit, false);
		}
		String FeatureFormat = "featureSetTransformation";
		AudioFeatureSet featureSet2 = loadFeature(features, parameter, clustersSegInit, FeatureFormat
				+ ",3:1:0:0:0:0,13,0:0:0:0");

		ClusterSet clusterSNS = new ClusterSet();
		Cluster clusterS = clusterSNS.createANewCluster("f2");
		Cluster clusterNS = clusterSNS.createANewCluster("iT");
		// param.getParameterSegmentation().setSilenceThreshold(0.1);
		for (Cluster cluster : clustersSegInit.clusterSetValue()) {
			double thr1 = Distance.getThreshold(cluster, features, 0.1, features.getIndexOfEnergy());
			double thr2 = Distance.getThreshold(cluster, features, 0.3, features.getIndexOfEnergy());

			for (Segment segment : cluster) {
				features.setCurrentShow(segment.getShowName());
				for (int i = segment.getStart(); i <= segment.getLast(); i++) {
					Segment newSegment = segment.clone();
					newSegment.setStart(i);
					newSegment.setLength(1);
					if (features.getFeatureUnsafe(i)[features.getIndexOfEnergy()] > thr2) {
						clusterS.addSegment(newSegment);
					}
					if (features.getFeatureUnsafe(i)[features.getIndexOfEnergy()] < thr1) {
						clusterNS.addSegment(newSegment);
					}

				}
			}
		}
		clusterSNS.collapse();

		parameter.getParameterSegmentationOutputFile().setMask(mask + ".sns_base.seg");
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			MainTools.writeClusterSet(parameter, clusterSNS, false);
		}

		ClusterSet previous = clustersSegInit;
		ClusterSet current = clusterSNS;
		int nb = 0;
		GMMArrayList gmmVect = new GMMArrayList();
		while (current.equals(previous) == false) {
			previous = current;
			parameter.getParameterModel().setModelKind("DIAG");
			parameter.getParameterModel().setNumberOfComponents(4);
			GMMArrayList gmmInitVect = new GMMArrayList(clusterSNS.clusterGetSize());
			MTrainInit.make(featureSet2, clusterSNS, gmmInitVect, parameter);
			// ** EM training of the initialized GMM
			gmmVect = new GMMArrayList(clusterSNS.clusterGetSize());
			MTrainEM.make(featureSet2, clusterSNS, gmmInitVect, gmmVect, parameter);

			// ** set the penalty to move from the state i to the state j, penalty to move from i to i is equal to 0
			parameter.getParameterDecoder().setDecoderPenalty("10");
			// ** make Viterbi decoding using the 8-GMM set
			// ** one state = one GMM = one speaker = one cluster
			current = MDecode.make(featureSet2, clustersSegInit, gmmVect, parameter);
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".sns_" + nb + ".seg");
			if (parameter.getParameterDiarization().isSaveAllStep()) {
				MainTools.writeClusterSet(parameter, current, true);
			}
			nb++;
		}
		parameter.getParameterFilter().setSegmentPadding(25);
		parameter.getParameterFilter().setSilenceMinimumLength(10);
		parameter.getParameterFilter().setSpeechMinimumLength(100);
		parameter.getParameterSegmentationFilterFile().setClusterFilterName("iT");
		ClusterSet clustersFltClust = SFilter.make(clustersSegInit, current, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".flt.seg");
			MainTools.writeClusterSet(parameter, clustersFltClust, false);
			parameter.getParameterSegmentationOutputFile().setMask(mask);
		}

		// ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector
		parameter.getParameterSegmentationFilterFile().setClusterFilterName("iT");
		ClusterSet clustersSplitClust = SSplitSeg.make(features, clustersFltClust, gmmVect, current, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".spl.seg");
			MainTools.writeClusterSet(parameter, clustersSplitClust, false);
			parameter.getParameterSegmentationOutputFile().setMask(mask);
		}

		AudioFeatureSet featureSet3 = loadFeature(features, parameter, clustersSplitClust, FeatureFormat
				+ ",1:3:2:0:0:0,13,1:1:0:0");
		mask = parameter.getParameterSegmentationOutputFile().getMask();
		dir = "media";
		InputStream genderInputStream = getClass().getResourceAsStream(dir + "/gender.gmms");
		GMMArrayList genderVector = MainTools.readGMMContainer(genderInputStream, parameter.getParameterModel());
		parameter.getParameterScore().setByCluster(true);
		parameter.getParameterScore().setGender(true);
		ClusterSet clustersGender = MScore.make(featureSet3, clustersSplitClust, genderVector, null, parameter);
		if (parameter.getParameterDiarization().isSaveAllStep()) {
			parameter.getParameterSegmentationOutputFile().setMask(mask + ".g.seg");
			MainTools.writeClusterSet(parameter, clustersGender, false);
			parameter.getParameterSegmentationOutputFile().setMask(mask);
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			Parameter parameter = new Parameter();
			parameter.getParameterInputFeature().setFeaturesDescription("audio2sphinx,1:1:0:0:0:0,13,0:0:0:0");

			parameter.readParameters(args);
			if (args.length <= 1) {
				parameter.help = true;
			}
			parameter.logCmdLine(args);
			info(parameter, "Diarization");

			if (parameter.show.isEmpty() == false) {
				Telephone telephone = new Telephone();
				telephone.makeMedia(parameter);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "Diarization error", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IO error", e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error", e);
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

			parameter.getParameterInputFeature().logAll(); // fInMask
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
// param.getParameterSegmentationInputFile().printEncodingFormat();
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterDiarization().logAll();
			logger.config(parameter.getSeparator());
		}
	}
}
