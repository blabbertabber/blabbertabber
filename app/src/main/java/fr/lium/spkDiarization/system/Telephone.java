/**
 * <p>
 * Diarization
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package fr.lium.spkDiarization.system;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.GMM;
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

public class Telephone {

    public static FeatureSet loadFeature(Parameter param, ClusterSet clusters, String desc) throws IOException, DiarizationException {
        param.parameterInputFeature.setFeaturesDescription(desc);
        return MainTools.readFeatureSet(param, clusters);
    }

    public static void main(String[] args) {
        try {
            Parameter param = new Parameter();
            param.parameterInputFeature.setFeaturesDescription("audio2sphinx,1:1:0:0:0:0,13,0:0:0:0");

            param.readParameters(args);
            if (args.length <= 1) {
                param.help = true;
            }
            if (param.trace) {
                param.printCmdLine(args);
            }
            info(param, "Diarization");

            if (param.nbShow > 0) {
                Telephone telephone = new Telephone();
                telephone.makeMedia(param);
            }
        } catch (DiarizationException e) {
            System.out.println("error \t exception " + e.getMessage());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void info(Parameter param, String prog) {
        if (param.help) {
            param.printSeparator2();
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterInputFeature.print(); // fInMask
            param.printSeparator();
            param.parameterSegmentationInputFile.print(); // sInMask
// param.parameterSegmentationInputFile.printEncodingFormat();
            param.parameterSegmentationOutputFile.print(); // sOutMask
            param.printSeparator();
            param.parameterDiarization.print();
            param.printSeparator();
        }
    }

    private FeatureSet loadFeature(FeatureSet features, Parameter param, ClusterSet clusters, String desc) throws IOException, DiarizationException {
        param.parameterInputFeature.setFeaturesDescription(desc);
        return MainTools.readFeatureSet(param, clusters, features);
    }

    public void makeOld(Parameter param) throws DiarizationException, Exception {
        // ** Caution this system is developed using Sphinx MFCC computed with legacy mode

        param.trace = true;
        param.help = true;
        String dir = "tel";
        // ** mask for the output of the segmentation file
        String mask = param.parameterSegmentationOutputFile.getMask();

        // ** get the first diarization
        ClusterSet clusters = null;
        if (param.parameterDiarization.isLoadInputSegmentation()) {
            clusters = MainTools.readClusterSet(param);
        } else {
            clusters = new ClusterSet();
            Cluster cluster = clusters.createANewCluster("init");
            Segment segment = new Segment(param.show, 0, 1, cluster);
            cluster.addSegment(segment);
        }

        // ** load the features, sphinx format (13 MFCC with C0) or compute it form a wave file
        FeatureSet features = loadFeature(param, clusters, param.parameterInputFeature.getFeaturesDescString());
        features.setCurrentShow(param.show);
        int nbFeatures = features.getNumberOfFeatures();
        String FeatureFormat = "featureSetTransformation";
        System.err.println("dim:" + features.getDim());
        System.err.println("dim:" + nbFeatures);
        if (param.parameterDiarization.isLoadInputSegmentation() == false) {
            clusters.getFirstCluster().firstSegment().setLength(nbFeatures);
        }

        // ** check the quality of the MFCC, remove similar consecutive MFCC of the featureSet
        ClusterSet clustersSegInit = new ClusterSet();
        MSegInit.make(features, clusters, clustersSegInit, param);
        clustersSegInit.collapse();
        param.parameterSegmentationOutputFile.setMask(mask + ".i.seg");
        if (param.parameterDiarization.isSaveAllStep()) {
            MainTools.writeClusterSet(param, clustersSegInit, false);
        }

        // ** segmentation using gaussian with full covariance matrix and GLR metric
        param.parameterSegmentation.setMethod("GLR");
        param.parameterModel.setNumberOfComponents(1);
        param.parameterModel.setKind("FULL");
        ClusterSet clustersSeg = new ClusterSet();
        MSeg.make(features, clustersSegInit, clustersSeg, param);
        param.parameterSegmentationOutputFile.setMask(mask + ".s.seg");
        if (param.parameterDiarization.isSaveAllStep()) {
            MainTools.writeClusterSet(param, clustersSeg, false);
        }

        // ** merge neighbour segment according a BIC metric
        param.parameterClustering.setMethod("l");
        param.parameterClustering.setThreshold(2);
        ClusterSet clustersLClust = MClust.make(features, clustersSeg, param, null);
        param.parameterSegmentationOutputFile.setMask(mask + ".l.seg");
        if (param.parameterDiarization.isSaveAllStep()) {
            MainTools.writeClusterSet(param, clustersLClust, false);
        }

        // ** BIC bottom up hierarchical classification using gaussian with full covariance matrix
        param.parameterClustering.setMethod("h");
        param.parameterClustering.setThreshold(6);
        param.parameterClustering.setMinimumOfCluster(2);
        ClusterSet clustersHClust = MClust.make(features, clustersLClust, param, null);
        param.parameterSegmentationOutputFile.setMask(mask + ".h.seg");
        if (param.parameterDiarization.isSaveAllStep()) {
            MainTools.writeClusterSet(param, clustersHClust, false);
        }

        // ** Train GMM for each cluster.
        // ** GMM is a 8 component gaussian with diagonal covariance matrix
        // ** one GMM = one speaker = one cluster
        // ** initialization of the GMMs :
        // ** - same global covariance for each gaussian,
        // ** - 1/8 for the weight,
        // ** - means are initialized with the mean of 10 successive vectors taken
        param.parameterModel.setKind("DIAG");
        param.parameterModel.setNumberOfComponents(8);
        ArrayList<GMM> gmmInitVect = new ArrayList<GMM>(clustersHClust.clusterGetSize());
        MTrainInit.make(features, clustersHClust, gmmInitVect, param);
        // ** EM training of the initialized GMM
        ArrayList<GMM> gmmVect = new ArrayList<GMM>(clustersHClust.clusterGetSize());
        MTrainEM.make(features, clustersHClust, gmmInitVect, gmmVect, param);

        // ** set the penalty to move from the state i to the state j, penalty to move from i to i is equal to 0
        param.parameterDecoder.setDecoderPenalty("250");
        // ** make Viterbi decoding using the 8-GMM set
        // ** one state = one GMM = one speaker = one cluster
        ClusterSet clustersDClust = MDecode.make(features, clustersHClust, gmmVect, param);
        param.parameterSegmentationOutputFile.setMask(mask + ".d.seg");
        if (param.parameterDiarization.isSaveAllStep()) {
            MainTools.writeClusterSet(param, clustersDClust, false);
        }

        // ** move the boundaries of the segment in low energy part of the signal
        ClusterSet clustersAdjClust = SAdjSeg.make(features, clustersDClust, param);
        param.parameterSegmentationOutputFile.setMask(mask + ".adj.seg");
        if (param.parameterDiarization.isSaveAllStep()) {
            MainTools.writeClusterSet(param, clustersAdjClust, false);
        }

		/*
        // ** Detection of speech, music, jingle using a Viterbi decoding
		// ** Reload MFCC, remove energy and add delta
		FeatureSet features2 = loadFeature(features, param, clusters, FeatureFormat + ",1:3:2:0:0:0,13,0:0:0:0");
		// ** load the model : 8 GMMs with 64 diagonal components
		URL pmsURL = getClass().getResource(dir + File.separator + "sms.gmms");
		ArrayList<GMM> pmsVect = MainTools.getInputGMMContainer(pmsURL, param.parameterModel);
		// ** set penalties for the i to j states
		// ** 10 for the first and second model corresponding to boad/narrowband silence
		// ** 50 for the other jingle speech (f0 f2 f3 fx), jingle and music
		param.parameterDecoder.setDecodePenalty("10,10,50");
		ClusterSet clustersPMSClust = MDecode.make(features2, clustersSegInit, pmsVect, param);
		param.parameterSegmentationOutputFile.setMask(mask + ".sms.seg");
		if (param.parameterDiarization.isSaveAllStep()) {
			MainTools.setOutputClusters(param, clustersPMSClust, false);
		}

		// ** Filter the segmentation adj acoording the sms segmentation
		// ** add 25 frames to all speech segments
		// ** remove silence part if silence segment is less than 25 frames
		// ** if a speech segment is less than 150 frames, it will be merge to the left or right closest segments
		param.parameterFilter.setSegmentPadding(25);
		param.parameterFilter.setSilenceMinimumLength(25);
		param.parameterFilter.setSpeechMinimumLength(150);
		ClusterSet clustersFltClust = SFilter.make(clustersAdjClust, clustersPMSClust, param);
		param.parameterSegmentationOutputFile.setMask(mask + ".flt.seg");
		if (param.parameterDiarization.isSaveAllStep()) {
			MainTools.setOutputClusters(param, clustersFltClust, false);
		}

		// ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector
		URL sURL = getClass().getResource(dir + File.separator + "s.gmms");
		ArrayList<GMM> sVect = MainTools.getInputGMMContainer(sURL, param.parameterModel);
		param.parameterSegmentationFilterFile.setClusterFilterName("iS,iT,j");
		ClusterSet clustersSplitClust = SSplitSeg.make(features2, clustersFltClust, sVect, clustersPMSClust, param);
		param.parameterSegmentationOutputFile.setMask(mask + ".spl.seg");
		if (param.parameterDiarization.isSaveAllStep()) {
			MainTools.setOutputClusters(param, clustersSplitClust, false);
		}*/

        // ** gender and band detection using 4 GMMs with 64 components with diagonal covariance matrix
        FeatureSet features2 = loadFeature(features, param, clusters, FeatureFormat + ",1:3:2:0:0:0,13,1:1:0:0");
        URL genderURL = getClass().getResource(dir + File.separator + "gender.gmms");
        ArrayList<GMM> genderVector = MainTools.readGMMContainer(genderURL, param.parameterModel);
        param.parameterScore.setByCluster(true);
        param.parameterScore.setGender(true);
        ClusterSet clustersGender = MScore.make(features2, clustersAdjClust, genderVector, null, param);
        param.parameterSegmentationOutputFile.setMask(mask + ".g.seg");
        if (param.parameterDiarization.isSaveAllStep()) {
            MainTools.writeClusterSet(param, clustersGender, false);
        }

        param.parameterSegmentationOutputFile.setMask(mask);
        MainTools.writeClusterSet(param, clustersGender, false);

		/*
		if (param.parameterDiarization.isCEClustering()) {
			// ** bottom up hierarchical classification using GMMs
			// ** one for each cluster, trained by MAP adaptation of a UBM composed of the fusion of 4x128GMM
			// ** the feature normalization use feature mapping technique, after the cluster frames are centered and reduced
			features2 = loadFeature(features, param, clustersGender, FeatureFormat + ",1:3:2:0:0:0,13,1:1:300:4");
			URL ubmURL = getClass().getResource(dir + File.separator + "ubm.gmm");
			ArrayList<GMM> ubmVect = MainTools.getInputGMMContainer(ubmURL, param.parameterModel);
			GMM ubm = ubmVect.get(0);
			param.parameterClustering.setMethod("ce");
			param.parameterClustering.setThreshold(1.7);
			param.parameterSegmentationOutputFile.setMask(mask + ".c.seg");
			param.parameterEM.setEMControl("1,5,0.01");
			param.parameterTopGaussian.setScoreNTop(5);
			ClusterSet clustersCLR = MClust.make(features2, clustersGender, param, ubm);
			if (param.parameterDiarization.isSaveAllStep()) {
				param.parameterSegmentationOutputFile.setMask(mask);
			}
			MainTools.setOutputClusters(param, clustersCLR, false);
			param.parameterSegmentationOutputFile.setMask(mask);
		} else {
		}*/
    }

    public void makeMedia(Parameter param) throws DiarizationException, Exception {
        // ** Caution this system is developed using Sphinx MFCC computed with legacy mode

        param.trace = true;
        param.help = true;
        String dir = "ester2";
        // ** mask for the output of the segmentation file
        String mask = param.parameterSegmentationOutputFile.getMask();

        // ** get the first diarization
        ClusterSet clusters = null;
        if (param.parameterDiarization.isLoadInputSegmentation()) {
            clusters = MainTools.readClusterSet(param);
        } else {
            clusters = new ClusterSet();
            Cluster cluster = clusters.createANewCluster("init");
            Segment segment = new Segment(param.show, 0, 1, cluster);
            cluster.addSegment(segment);
        }

        // ** load the features, sphinx format (13 MFCC with C0) or compute it form a wave file
        FeatureSet features = loadFeature(param, clusters, param.parameterInputFeature.getFeaturesDescString());

        features.setCurrentShow(param.show);
        int nbFeatures = features.getNumberOfFeatures();
        System.err.println("dim:" + features.getDim());
        System.err.println("dim:" + nbFeatures);
        if (param.parameterDiarization.isLoadInputSegmentation() == false) {
            clusters.getFirstCluster().firstSegment().setLength(nbFeatures);
        }

        // ** check the quality of the MFCC, remove similar consecutive MFCC of the featureSet
        ClusterSet clustersSegInit = new ClusterSet();
        MSegInit.make(features, clusters, clustersSegInit, param);
        clustersSegInit.collapse();
        param.parameterSegmentationOutputFile.setMask(mask + ".i.seg");
        if (param.parameterDiarization.isSaveAllStep()) {
            MainTools.writeClusterSet(param, clustersSegInit, false);
        }
        String FeatureFormat = "featureSetTransformation";
        FeatureSet featureSet2 = loadFeature(features, param, clustersSegInit, FeatureFormat + ",3:1:0:0:0:0,13,0:0:0:0");

        ClusterSet clusterSNS = new ClusterSet();
        Cluster clusterS = clusterSNS.createANewCluster("f2");
        Cluster clusterNS = clusterSNS.createANewCluster("iT");
        //param.parameterSegmentation.setSilenceThreshold(0.1);
        for (Cluster cluster : clustersSegInit.clusterSetValue()) {
            double thr1 = Distance.getEnergyThreshold(cluster, features, 0.1);
            double thr2 = Distance.getEnergyThreshold(cluster, features, 0.3);

            for (Segment segment : cluster) {
                features.setCurrentShow(segment.getShowName());
                for (int i = segment.getStart(); i <= segment.getLast(); i++) {
                    Segment newSegment = (Segment) segment.clone();
                    newSegment.setStart(i);
                    newSegment.setLength(1);
                    if (features.getFeature(i)[features.getIndexOfEnergy()] > thr2) {
                        clusterS.addSegment(newSegment);
                    }
                    if (features.getFeature(i)[features.getIndexOfEnergy()] < thr1) {
                        clusterNS.addSegment(newSegment);
                    }

                }
            }
        }
        clusterSNS.collapse();

        param.parameterSegmentationOutputFile.setMask(mask + ".sns_base.seg");
        if (param.parameterDiarization.isSaveAllStep()) {
            MainTools.writeClusterSet(param, clusterSNS, false);
        }

        ClusterSet previous = clustersSegInit;
        ClusterSet current = clusterSNS;
        int nb = 0;
        ArrayList<GMM> gmmVect = new ArrayList<GMM>();
        while (current.equals(previous) == false) {
            previous = current;
            param.parameterModel.setKind("DIAG");
            param.parameterModel.setNumberOfComponents(4);
            ArrayList<GMM> gmmInitVect = new ArrayList<GMM>(clusterSNS.clusterGetSize());
            MTrainInit.make(featureSet2, clusterSNS, gmmInitVect, param);
            // ** EM training of the initialized GMM
            gmmVect = new ArrayList<GMM>(clusterSNS.clusterGetSize());
            MTrainEM.make(featureSet2, clusterSNS, gmmInitVect, gmmVect, param);

            // ** set the penalty to move from the state i to the state j, penalty to move from i to i is equal to 0
            param.parameterDecoder.setDecoderPenalty("10");
            // ** make Viterbi decoding using the 8-GMM set
            // ** one state = one GMM = one speaker = one cluster
            current = MDecode.make(featureSet2, clustersSegInit, gmmVect, param);
            param.parameterSegmentationOutputFile.setMask(mask + ".sns_" + nb + ".seg");
            if (param.parameterDiarization.isSaveAllStep()) {
                MainTools.writeClusterSet(param, current, true);
            }
            nb++;
        }
        param.parameterFilter.setSegmentPadding(25);
        param.parameterFilter.setSilenceMinimumLength(10);
        param.parameterFilter.setSpeechMinimumLength(100);
        param.parameterSegmentationFilterFile.setClusterFilterName("iT");
        ClusterSet clustersFltClust = SFilter.make(clustersSegInit, current, param);
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".flt.seg");
            MainTools.writeClusterSet(param, clustersFltClust, false);
            param.parameterSegmentationOutputFile.setMask(mask);
        }


        // ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector
        param.parameterSegmentationFilterFile.setClusterFilterName("iT");
        ClusterSet clustersSplitClust = SSplitSeg.make(features, clustersFltClust, gmmVect, current, param);
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".spl.seg");
            MainTools.writeClusterSet(param, clustersSplitClust, false);
            param.parameterSegmentationOutputFile.setMask(mask);
        }

        FeatureSet featureSet3 = loadFeature(features, param, clustersSplitClust, FeatureFormat + ",1:3:2:0:0:0,13,1:1:0:0");
        mask = param.parameterSegmentationOutputFile.getMask();
        dir = "media";
        URL genderURL = getClass().getResource(dir + File.separator + "gender.gmms");
        ArrayList<GMM> genderVector = MainTools.readGMMContainer(genderURL, param.parameterModel);
        param.parameterScore.setByCluster(true);
        param.parameterScore.setGender(true);
        ClusterSet clustersGender = MScore.make(featureSet3, clustersSplitClust, genderVector, null, param);
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".g.seg");
            MainTools.writeClusterSet(param, clustersGender, false);
            param.parameterSegmentationOutputFile.setMask(mask);
        }
    }
}
