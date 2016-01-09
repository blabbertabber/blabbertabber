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
package fr.lium.experimental.spkDiarization.system;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

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
import fr.lium.spkDiarization.programs.MSeg;
import fr.lium.spkDiarization.programs.MSegInit;
import fr.lium.spkDiarization.programs.MTrainEM;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.tools.SAdjSeg;

public class Meeting {

    public static FeatureSet loadFeature(Parameter param, ClusterSet clusters, String desc) throws IOException, DiarizationException {
        param.parameterInputFeature.setFeaturesDescription(desc);
        return MainTools.readFeatureSet(param, clusters);
    }

    public static void main(String[] args) {
        try {
            Parameter param = new Parameter();
//			param.parameterInputFeature.setFeaturesDescription("audio2sphinx,1:1:0:0:0:0,13,0:0:0:0");
            param.parameterInputFeature.setFeaturesDescription("htk,1:0:0:0:0:0,19,0:0:0:0");

            param.readParameters(args);
            if (args.length <= 1) {
                param.help = true;
            }
            if (param.trace) {
                param.printCmdLine(args);
            }
            info(param, "Meeting");

            if (param.nbShow > 0) {
                Meeting diarization = new Meeting();
                //diarization.baselineVersion(param);
                diarization.icsiVersion(param);
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

    public void icsiVersion(Parameter param) throws DiarizationException, Exception {
        ClusterSet clusterSetUEM = MainTools.readClusterSet(param);
        FeatureSet featureSet = loadFeature(param, clusterSetUEM, param.parameterInputFeature.getFeaturesDescString());
        clusterSetUEM.collapse();
        featureSet.setCurrentShow(param.show);
        int numberOfPart = 2;
        int numberOfInitialCluster = 16;
        String mask = param.parameterSegmentationOutputFile.getMask();
        //param.parameterDiarization.setSaveAllStep(true);

        int nbFeaturesInClusterSet = clusterSetUEM.getLength();

        int sizeOfAPart = nbFeaturesInClusterSet / (numberOfPart * numberOfInitialCluster);
        System.err.println(Math.exp(-1000) + " init: " + sizeOfAPart + " = " + nbFeaturesInClusterSet + " / (" + numberOfPart + "* " + numberOfInitialCluster + ")");
        TreeMap<Integer, Segment> map = clusterSetUEM.toFrames();
        Iterator<Integer> it = map.keySet().iterator();
        ClusterSet clusterSetInit = new ClusterSet();
        for (int i = 0; i < numberOfPart; i++) {
            for (int l = 0; l < numberOfInitialCluster; l++) {
                Cluster cluster = clusterSetInit.getOrCreateANewCluster("S" + l);
                for (int j = 0; j < sizeOfAPart; j++) {
                    int idx = it.next();
                    Segment segment = new Segment(map.get(idx).getShowName(), idx, 1, cluster);
                    cluster.addSegment(segment);
                }
            }
        }
        clusterSetInit.collapse();
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".i.seg");
            MainTools.writeClusterSet(param, clusterSetInit, false);
        }

        //ReSeg
        ClusterSet currentClusterSet = clusterSetInit;
        ArrayList<GMM> currentGMMSet = new ArrayList<GMM>();

        ClusterSet previousClusterSet = new ClusterSet();
        ArrayList<GMM> previousGMMSet = new ArrayList<GMM>();
        //param.trace = false;

        param.parameterModel.setKind("DIAG");
        param.parameterModel.setNumberOfComponents(5);
        param.parameterInitializationEM.setEMControl("1,5,0.0000001");
        param.parameterEM.setEMControl("1,5,0.000000001");
        param.parameterInitializationEM.setModelInitMethod("uniform");
        //param.parameterVarianceControl.setFlooring(0.01);
        double loopPenalty = -Math.log(0.9);
        double exitPenalty = -Math.log(0.1);
        param.parameterDecoder.setDecoderPenalty(exitPenalty + ":" + loopPenalty);
        param.parameterDecoder.setViterbiDurationConstraints("minimal,250");
        System.err.println("[ICSI] init model");
        MTrainInit.make(featureSet, currentClusterSet, previousGMMSet, param);
        System.err.println("[ICSI] train first model");
        MTrainEM.make(featureSet, currentClusterSet, previousGMMSet, currentGMMSet, param);

        int nbD = 0;
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".d." + nbD + ".seg");
            MainTools.writeClusterSet(param, currentClusterSet, false);
            nbD++;
        }
        boolean first = true;
        double maxScore = -Double.MAX_VALUE;
        int nbMerge = 0;
        do {
            if (param.parameterDiarization.isSaveAllStep()) {
                param.parameterSegmentationOutputFile.setMask(mask + ".m." + nbMerge + ".seg");
                MainTools.writeClusterSet(param, currentClusterSet, true);
                nbMerge++;
            }
            int n = 3;
            if (first) {
                n = 5;
                first = false;
            }
            while ((currentClusterSet.equals(previousClusterSet) == false) && (n > 0)) {
                previousClusterSet = currentClusterSet;
                System.err.println("[ICSI] decode");
                exitPenalty = -Math.log(0.1 / (double) currentGMMSet.size());

                param.parameterDecoder.setDecoderPenalty(exitPenalty + ":" + loopPenalty);
                currentClusterSet = MDecode.make(featureSet, clusterSetUEM, currentGMMSet, param);
//				currentClusterSet = MDecode.make(featureSet, previousClusterSet, currentGMMSet, param);

                previousGMMSet = currentGMMSet;
                currentGMMSet = new ArrayList<GMM>();
                System.err.println("[ICSI] train");
                ArrayList<GMM> tmpGMM = new ArrayList<GMM>();
                for (int i = 0; i < previousGMMSet.size(); i++) {
                    if (currentClusterSet.containsCluster(previousGMMSet.get(i).getName())) {
                        tmpGMM.add(previousGMMSet.get(i));
                    }
                }
                previousGMMSet = tmpGMM;
                MTrainEM.make(featureSet, currentClusterSet, previousGMMSet, currentGMMSet, param);
                System.err.println("** segment " + n);
                if (param.parameterDiarization.isSaveAllStep()) {
                    param.parameterSegmentationOutputFile.setMask(mask + ".d." + nbD + ".seg");
                    MainTools.writeClusterSet(param, currentClusterSet, false);
                    nbD++;
                }
                n--;
            }
            maxScore = -Double.MAX_VALUE;
            String maxClusterNameI = null;
            String maxClusterNameJ = null;
            int maxJ = 0;
            int maxI = 0;
            for (int i = 0; i < currentGMMSet.size() - 1; i++) {
                GMM gmmI = currentGMMSet.get(i);
                String clusterNameI = gmmI.getName();
                Cluster clusterI = currentClusterSet.getCluster(clusterNameI);
                for (int j = i + 1; j < currentGMMSet.size(); j++) {
                    GMM gmmJ = currentGMMSet.get(j);
                    String clusterNameJ = gmmJ.getName();
                    Cluster clusterJ = currentClusterSet.getCluster(clusterNameJ);
                    double score = Distance.GLR(gmmI, clusterI, gmmJ, clusterJ, featureSet, param);
                    System.err.println("distance : " + clusterNameI + "/" + clusterNameJ + " score:" + score + " " + i + "/" + j);
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
                System.err.println("---------------------------------");
                System.err.println("** merge : " + maxClusterNameI + " " + maxClusterNameJ + " score:" + maxScore + " " + maxI + "/" + maxJ);
                currentClusterSet.mergeCluster(maxClusterNameI, maxClusterNameJ);
                //currentClusterSet.removeCluster(maxClusterNameJ);
                GMM gmmI = currentGMMSet.get(maxI);
                GMM gmmJ = currentGMMSet.remove(maxJ);
                for (int i = 0; i < gmmJ.getNbOfComponents(); i++) {
                    gmmI.addComponent(gmmJ.getComponent(i));
                }
                gmmI.normWeights();
                for (int i = 0; i < currentGMMSet.size(); i++) {
                    GMM gmm = currentGMMSet.get(i);
                    System.err.println(" model : " + i + " " + gmm.getName() + " nb:" + gmm.getNbOfComponents());

                }
            }
        } while ((maxScore > 0.0) && (currentClusterSet.clusterGetSize() > 1));
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".m." + nbMerge + ".seg");
            MainTools.writeClusterSet(param, currentClusterSet, true);
            nbMerge++;
        }

        param.parameterSegmentationOutputFile.setMask(mask);
        MainTools.writeClusterSet(param, currentClusterSet, true);

    }

    public void baselineVersion(Parameter param) throws DiarizationException, Exception {
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
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".i.seg");
            MainTools.writeClusterSet(param, clustersSegInit, false);
        }

        // ** segmentation using gaussian with full covariance matrix and GLR metric
        param.parameterSegmentation.setMethod("GLR");
        param.parameterSegmentation.setModelWindowSize(200);
        param.parameterSegmentation.setMinimimWindowSize(150);
        param.parameterModel.setNumberOfComponents(1);
        param.parameterModel.setKind("FULL");
        ClusterSet clustersSeg = new ClusterSet();
        MSeg.make(features, clustersSegInit, clustersSeg, param);
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".s.seg");
            MainTools.writeClusterSet(param, clustersSeg, false);
        }

        // ** merge neighbour segment according a BIC metric
        param.parameterClustering.setMethod("l");
        param.parameterClustering.setThreshold(2);
        ClusterSet clustersLClust = MClust.make(features, clustersSeg, param, null);
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".l.seg");
            MainTools.writeClusterSet(param, clustersLClust, false);
        }

        // ** BIC bottom up hierarchical classification using gaussian with full covariance matrix
        param.parameterClustering.setMethod("h");
        param.parameterClustering.setThreshold(3);
        // param.parameterClustering.setThreshold(3.5);
        ClusterSet clustersHClust = MClust.make(features, clustersLClust, param, null);
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".h.seg");
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
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".d.seg");
            MainTools.writeClusterSet(param, clustersDClust, false);
        }

        // ** move the boundaries of the segment in low energy part of the signal
        ClusterSet clustersAdjClust = SAdjSeg.make(features, clustersDClust, param);
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".adj.seg");
            MainTools.writeClusterSet(param, clustersAdjClust, false);
        }

        // ** bottom up hierarchical classification using GMMs
        // ** one for each cluster, trained by MAP adaptation of a UBM composed of the fusion of 4x128GMM
        // ** the feature normalization use feature mapping technique, after the cluster frames are centered and reduced
        FeatureSet features2 = loadFeature(features, param, clustersAdjClust, FeatureFormat + ",1:3:2:0:0:0,13,1:1:300:4");
        URL ubmURL = getClass().getResource(dir + File.separator + "ubm.gmm");
        ArrayList<GMM> ubmVect = MainTools.readGMMContainer(ubmURL, param.parameterModel);
        GMM ubm = ubmVect.get(0);
        param.parameterClustering.setMethod("ce");
        param.parameterClustering.setThreshold(1.7);
        param.parameterEM.setEMControl("1,5,0.01");
        param.parameterTopGaussian.setScoreNTop(5);
        boolean saveAll = param.parameterDiarization.isSaveAllStep();
        param.parameterDiarization.setSaveAllStep(false);
        ClusterSet clustersCLR = MClust.make(features2, clustersAdjClust, param, ubm);
        param.parameterDiarization.setSaveAllStep(saveAll);

        param.parameterSegmentationOutputFile.setMask(mask + ".c.seg.toto2");
        if (param.parameterDiarization.isSaveAllStep()) {
            param.parameterSegmentationOutputFile.setMask(mask + ".c.seg");
            MainTools.writeClusterSet(param, clustersCLR, false);
        }
        param.parameterSegmentationOutputFile.setMask(mask);
        MainTools.writeClusterSet(param, clustersCLR, false);
    }

}
