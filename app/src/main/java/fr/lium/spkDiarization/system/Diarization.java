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

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;

import edu.thesis.xml.transform.TransformerException;
import fr.lium.spkDiarization.lib.DiarizationError;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libClusteringMethod.CLRHClustering;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterBNDiarization;
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

public class Diarization {
    double lMin = 1, lMax = 3, hMin = 2, hMax = 5;
    double dMin = 100, dMax = 350;
    double cMax = 5.0;
    double mult = 100;

    TreeMap<String, Integer> result;

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
                Diarization diarization = new Diarization();
                if (param.parameterDiarization.getSystem() == ParameterBNDiarization.SystemString[1]) {
                    param.parameterSegmentationSplit.setSegmentMaximumLength(10 * 100);
                }
                if (param.parameterDiarization.isTuning() == true) {
                    diarization.tunEster2Version(param);
                } else {
                    diarization.ester2Version(param);
                }
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
            param.parameterSegmentationInputFile2.print(); // sInMask
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

    public ClusterSet initialize(Parameter parameter) throws DiarizationException, Exception {
        // ** get the first diarization
        ClusterSet clusterSet = null;
        if (parameter.parameterDiarization.isLoadInputSegmentation()) {
            clusterSet = MainTools.readClusterSet(parameter);
        } else {
            clusterSet = new ClusterSet();
            Cluster cluster = clusterSet.createANewCluster("init");
            Segment segment = new Segment(parameter.show, 0, 1, cluster);
            cluster.addSegment(segment);
        }
        return clusterSet;
    }

    public ClusterSet sanityCheck(ClusterSet clusterSet, FeatureSet features, Parameter parameter) throws DiarizationException, IOException, ParserConfigurationException, SAXException, TransformerException {
        String mask = parameter.parameterSegmentationOutputFile.getMask();
        ClusterSet clustersSegInit = new ClusterSet();
        MSegInit.make(features, clusterSet, clustersSegInit, parameter);
        clustersSegInit.collapse();
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".i.seg");
            MainTools.writeClusterSet(parameter, clustersSegInit, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }
        return clustersSegInit;
    }

    public ClusterSet segmentation(String methode, String kind, ClusterSet clusterSet, FeatureSet featureSet, Parameter parameter) throws Exception {
        String mask = parameter.parameterSegmentationOutputFile.getMask();
        parameter.parameterSegmentation.setMethod(methode);
        parameter.parameterModel.setNumberOfComponents(1);
        parameter.parameterModel.setKind(kind);
        ClusterSet clustersSeg = new ClusterSet();
        MSeg.make(featureSet, clusterSet, clustersSeg, parameter);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".s.seg");
            MainTools.writeClusterSet(parameter, clustersSeg, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }
        return clustersSeg;
    }

    public ClusterSet clusteringLinear(double threshold, ClusterSet clusterSet, FeatureSet featureSet, Parameter parameter) throws Exception {
        String mask = parameter.parameterSegmentationOutputFile.getMask();
        parameter.parameterClustering.setMethod("l");
        parameter.parameterModel.setKind("FULL");
        parameter.parameterModel.setNumberOfComponents(1);
        parameter.parameterClustering.setThreshold(threshold);
        ClusterSet clustersLClust = MClust.make(featureSet, clusterSet, parameter, null);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".l.seg");
            MainTools.writeClusterSet(parameter, clustersLClust, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }
        return clustersLClust;
    }

    public ClusterSet clustering(double threshold, ClusterSet clusterSet, FeatureSet featureSet, Parameter parameter) throws Exception {
        String mask = parameter.parameterSegmentationOutputFile.getMask();
        parameter.parameterClustering.setMethod("h");
        parameter.parameterClustering.setThreshold(threshold);
        parameter.parameterModel.setKind("FULL");
        parameter.parameterModel.setNumberOfComponents(1);
        ClusterSet clustersHClust = MClust.make(featureSet, clusterSet, parameter, null);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".h.seg");
            MainTools.writeClusterSet(parameter, clustersHClust, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }
        return clustersHClust;
    }

    public ClusterSet decode(int nbComp, double threshold, ClusterSet clusterSet, FeatureSet featureSet, Parameter parameter) throws Exception {
        String mask = parameter.parameterSegmentationOutputFile.getMask();
        // ** Train GMM for each cluster.
        // ** GMM is a 8 component gaussian with diagonal covariance matrix
        // ** one GMM = one speaker = one cluster
        // ** initialization of the GMMs :
        // ** - same global covariance for each gaussian,
        // ** - 1/8 for the weight,
        // ** - means are initialized with the mean of 10 successive vectors taken
        parameter.parameterModel.setKind("DIAG");
        parameter.parameterModel.setNumberOfComponents(nbComp);
        ArrayList<GMM> gmmInitVect = new ArrayList<GMM>(clusterSet.clusterGetSize());
        MTrainInit.make(featureSet, clusterSet, gmmInitVect, parameter);
        // ** EM training of the initialized GMM
        ArrayList<GMM> gmmVect = new ArrayList<GMM>(clusterSet.clusterGetSize());
        MTrainEM.make(featureSet, clusterSet, gmmInitVect, gmmVect, parameter);

        // ** set the penalty to move from the state i to the state j, penalty to move from i to i is equal to 0
        parameter.parameterDecoder.setDecoderPenalty(String.valueOf(threshold));
        // ** make Viterbi decoding using the 8-GMM set
        // ** one state = one GMM = one speaker = one cluster
        ClusterSet clustersDClust = MDecode.make(featureSet, clusterSet, gmmVect, parameter);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".d.seg");
            MainTools.writeClusterSet(parameter, clustersDClust, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }
        // ** move the boundaries of the segment in low energy part of the signal
        ClusterSet clustersAdjClust = SAdjSeg.make(featureSet, clustersDClust, parameter);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".adj.seg");
            MainTools.writeClusterSet(parameter, clustersAdjClust, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }

        return clustersAdjClust;
    }

    public ClusterSet speech(String threshold, ClusterSet clustersSetBase, ClusterSet clustersSegInit, ClusterSet clustersDClust, FeatureSet featureSet, Parameter parameter) throws Exception {
        // ** Reload MFCC, remove energy and add delta
        String FeatureFormat = "featureSetTransformation";
        FeatureSet featureSet2 = loadFeature(featureSet, parameter, clustersSetBase, FeatureFormat + ",1:3:2:0:0:0,13,0:0:0:0");
        String mask = parameter.parameterSegmentationOutputFile.getMask();
        String dir = "ester2";
        // ** load the model : 8 GMMs with 64 diagonal components
        URL pmsURL = getClass().getResource(dir + File.separator + "sms.gmms");
        ArrayList<GMM> pmsVect = MainTools.readGMMContainer(pmsURL, parameter.parameterModel);
        // ** set penalties for the i to j states
        // ** 10 for the first and second model corresponding to boad/narrowband silence
        // ** 50 for the other jingle speech (f0 f2 f3 fx), jingle and music
        parameter.parameterDecoder.setDecoderPenalty(threshold);
        ClusterSet clustersPMSClust = MDecode.make(featureSet2, clustersSegInit, pmsVect, parameter);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".sms.seg");
            MainTools.writeClusterSet(parameter, clustersPMSClust, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }

        // ** Filter the segmentation adj acoording the sms segmentation
        // ** add 25 frames to all speech segments
        // ** remove silence part if silence segment is less than 25 frames
        // ** if a speech segment is less than 150 frames, it will be merge to the left or right closest segments

		/*ClusterSet clustersAdjClust = SAdjSeg.make(featureSet, clustersDClust, parameter);
        if (parameter.parameterDiarization.isSaveAllStep()) {
			parameter.parameterSegmentationOutputFile.setMask(mask+".adj.seg");
			MainTools.writeClusterSet(parameter, clustersAdjClust, false);
			parameter.parameterSegmentationOutputFile.setMask(mask);
		} //warring : -0.1 de DER
		 */

        parameter.parameterFilter.setSegmentPadding(25);
        parameter.parameterFilter.setSilenceMinimumLength(25);
        parameter.parameterFilter.setSpeechMinimumLength(150);
        ClusterSet clustersFltClust = SFilter.make(clustersDClust, clustersPMSClust, parameter);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".flt.seg");
            MainTools.writeClusterSet(parameter, clustersFltClust, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }

        // ** segments of more than 20s are split according of silence present in the pms or using a gmm silence detector
        URL sURL = getClass().getResource(dir + File.separator + "s.gmms");
        ArrayList<GMM> sVect = MainTools.readGMMContainer(sURL, parameter.parameterModel);
        parameter.parameterSegmentationFilterFile.setClusterFilterName("iS,iT,j");
        ClusterSet clustersSplitClust = SSplitSeg.make(featureSet2, clustersFltClust, sVect, clustersPMSClust, parameter);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".spl.seg");
            MainTools.writeClusterSet(parameter, clustersSplitClust, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }
        return clustersSplitClust;
    }

    public ClusterSet gender(ClusterSet clusterSetBase, ClusterSet clusterSet, FeatureSet featureSet, Parameter parameter) throws Exception {
        String FeatureFormat = "featureSetTransformation";
        FeatureSet featureSet2 = loadFeature(featureSet, parameter, clusterSet, FeatureFormat + ",1:3:2:0:0:0,13,1:1:0:0");
        String mask = parameter.parameterSegmentationOutputFile.getMask();
        String dir = "ester2";
        URL genderURL = getClass().getResource(dir + File.separator + "gender.gmms");
        ArrayList<GMM> genderVector = MainTools.readGMMContainer(genderURL, parameter.parameterModel);
        parameter.parameterScore.setByCluster(true);
        parameter.parameterScore.setGender(true);
        ClusterSet clustersGender = MScore.make(featureSet2, clusterSet, genderVector, null, parameter);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".g.seg");
            MainTools.writeClusterSet(parameter, clustersGender, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }
        return clustersGender;
    }

    public ClusterSet speakerClustering(double threshold, String method, ClusterSet clusterSetBase, ClusterSet clustersSet, FeatureSet featureSet, Parameter parameter) throws Exception {
        // ** bottom up hierarchical classification using GMMs
        // ** one for each cluster, trained by MAP adaptation of a UBM composed of the fusion of 4x128GMM
        // ** the feature normalization use feature mapping technique, after the cluster frames are centered and reduced
        String mask = parameter.parameterSegmentationOutputFile.getMask();
        String FeatureFormat = "featureSetTransformation";
        String dir = "ester2";
        FeatureSet featureSet2 = loadFeature(featureSet, parameter, clustersSet, FeatureFormat + ",1:3:2:0:0:0,13,1:1:300:4");
        URL ubmURL = getClass().getResource(dir + File.separator + "ubm.gmm");
        ArrayList<GMM> ubmVect = MainTools.readGMMContainer(ubmURL, parameter.parameterModel);
        GMM ubm = ubmVect.get(0);
        parameter.parameterClustering.setMethod(method);
        parameter.parameterClustering.setThreshold(threshold);
        parameter.parameterEM.setEMControl("1,5,0.01");
        parameter.parameterTopGaussian.setScoreNTop(5);
        boolean saveAll = parameter.parameterDiarization.isSaveAllStep();
        parameter.parameterDiarization.setSaveAllStep(false);
        ClusterSet clustersCLR = MClust.make(featureSet2, clustersSet, parameter, ubm);
        parameter.parameterDiarization.setSaveAllStep(saveAll);

        parameter.parameterSegmentationOutputFile.setMask(mask);
        if (parameter.parameterDiarization.isSaveAllStep()) {
            parameter.parameterSegmentationOutputFile.setMask(mask + ".c.seg");
            MainTools.writeClusterSet(parameter, clustersCLR, false);
            parameter.parameterSegmentationOutputFile.setMask(mask);
        }
        parameter.parameterTopGaussian.setScoreNTop(-1);
        parameter.parameterEM.setEMControl("3,10,0.01");
        return clustersCLR;
    }

    public void ester2Version(Parameter parameter) throws DiarizationException, Exception {
        // ** Caution this system is developed using Sphinx MFCC computed with legacy mode
        //String mask = parameter.parameterSegmentationOutputFile.getMask();
        ClusterSet referenceClusterSet = null;
        double lenReferenceClusterSet = 0.0;
        if (!parameter.parameterSegmentationInputFile2.getMask().equals("")) {
            referenceClusterSet = MainTools.readTheSecondClusterSet(parameter);
            lenReferenceClusterSet = referenceClusterSet.getLength();
        }
        //parameter.trace = true;
        //parameter.help = true;
        ParameterBNDiarization parameterDiarization = parameter.parameterDiarization;
        // ** mask for the output of the segmentation file
//		String mask = parameter.parameterSegmentationOutputFile.getMask();

        ClusterSet clusterSet = initialize(parameter);

        // ** load the features, sphinx format (13 MFCC with C0) or compute it form a wave file
        FeatureSet featureSet = loadFeature(parameter, clusterSet, parameter.parameterInputFeature.getFeaturesDescString());
        featureSet.setCurrentShow(parameter.show);
        int nbFeatures = featureSet.getNumberOfFeatures();
        if (parameter.parameterDiarization.isLoadInputSegmentation() == false) {
            clusterSet.getFirstCluster().firstSegment().setLength(nbFeatures);
        }

        ClusterSet clustersSegInit = sanityCheck(clusterSet, featureSet, parameter);
        ClusterSet clustersSeg = segmentation("GLR", "FULL", clustersSegInit, featureSet, parameter);
        ClusterSet clustersLClust = clusteringLinear(parameterDiarization.getThreshold("l"), clustersSeg, featureSet, parameter);
        ClusterSet clustersHClust = clustering(parameterDiarization.getThreshold("h"), clustersLClust, featureSet, parameter);
        ClusterSet clustersDClust = decode(8, parameterDiarization.getThreshold("d"), clustersHClust, featureSet, parameter);
        ClusterSet clustersSplitClust = speech("10,10,50", clusterSet, clustersSegInit, clustersDClust, featureSet, parameter);
        ClusterSet clustersGender = gender(clusterSet, clustersSplitClust, featureSet, parameter);

        if (parameter.parameterDiarization.isCEClustering()) {
            ClusterSet clustersCLR = speakerClustering(parameterDiarization.getThreshold("c"), "ce", clusterSet, clustersGender, featureSet, parameter);
            MainTools.writeClusterSet(parameter, clustersCLR, false);
            if (referenceClusterSet != null) {
                double error = DiarizationError.scoreOfMatchedSpeakers(referenceClusterSet, clustersCLR);
                double rate = error / lenReferenceClusterSet;
                System.err.println("*** Error=" + error + " len=" + lenReferenceClusterSet + " rate=" + rate);
            }
        } else {
            MainTools.writeClusterSet(parameter, clustersGender, false);
        }

    }

    protected TreeMap<String, ClusterSet> splitHypotesis(ClusterSet fullClusterSet) {
        TreeMap<String, ClusterSet> listOfClusterSet = new TreeMap<String, ClusterSet>();

        for (String showName : fullClusterSet.getShowNames()) {
            System.err.println("showName=" + showName);
            ClusterSet clusterSet = new ClusterSet();
            for (Segment segment : fullClusterSet.getSegments()) {
                if (segment.getShowName().equals(showName) == true) {
                    Cluster cluster = clusterSet.getOrCreateANewCluster(segment.getClusterName());
                    cluster.addSegment(segment);
                }
            }
            listOfClusterSet.put(showName, clusterSet);
        }

        return listOfClusterSet;
    }

    public void tunEster2Version(Parameter parameter) throws DiarizationException, Exception {

        parameter.trace = false;
        parameter.help = true;
        ParameterBNDiarization parameterDiarization = parameter.parameterDiarization;

//		double paramThr = parameter.parameterClustering.getThreshold();
        lMin = parameterDiarization.getThreshold("l");
        lMax = parameterDiarization.getMaxThreshold("l");
        hMin = parameterDiarization.getThreshold("h");
        hMax = parameterDiarization.getMaxThreshold("h");
        dMin = parameterDiarization.getThreshold("d");
        dMax = parameterDiarization.getMaxThreshold("d");
        //cMin = parameterDiarization.getThreshold("c");
        cMax = parameterDiarization.getMaxThreshold("c");
        ClusterSet referenceClusterSet = MainTools.readTheSecondClusterSet(parameter);
        TreeMap<String, ArrayList<Double>> result = new TreeMap<String, ArrayList<Double>>();

        ClusterSet fullClusterSet = initialize(parameter);

        TreeMap<String, ClusterSet> listOfClusterSet = splitHypotesis(fullClusterSet);

        for (String showName : listOfClusterSet.keySet()) {
            ClusterSet clusterSet = (ClusterSet) listOfClusterSet.get(showName);
            parameter.show = showName;
            // ** load the features, sphinx format (13 MFCC with C0) or compute it form a wave file
            FeatureSet featureSet = loadFeature(parameter, clusterSet, parameter.parameterInputFeature.getFeaturesDescString());
            featureSet.setCurrentShow(parameter.show);
            int nbFeatures = featureSet.getNumberOfFeatures();
            if (parameter.parameterDiarization.isLoadInputSegmentation() == false) {
                clusterSet.getFirstCluster().firstSegment().setLength(nbFeatures);
            }
            ClusterSet clustersSegInit = sanityCheck(clusterSet, featureSet, parameter);
            ClusterSet clustersSegSave = segmentation("GLR", "FULL", clustersSegInit, featureSet, parameter);

            for (double l = lMin; l <= lMax; l += 0.5) {
                ClusterSet clustersSeg = (ClusterSet) clustersSegSave.clone();
                //System.err.println("clustering l="+l);
                ClusterSet clustersLClust = clusteringLinear(l, clustersSeg, featureSet, parameter);
                for (double h = hMin; h <= hMax; h += 0.5) {
                    if (h > l) {
                        ClusterSet clustersHClust = clustering(h, clustersLClust, featureSet, parameter);
                        for (double d = dMin; d <= dMax; d += 50) {
                            ClusterSet clustersDClust = decode(8, d, clustersHClust, featureSet, parameter);
                            //double error = DiarizationError.scoreOfMatchedSpeakers(referenceClusterSet, clustersDClust);
                            ClusterSet clustersSplitClust = speech("10,10,50", clusterSet, clustersSegInit, clustersDClust, featureSet, parameter);
                            ClusterSet clustersGender = gender(clusterSet, clustersSplitClust, featureSet, parameter);

                            String key = "l=" + l + " h=" + h + " d=" + d;
                            ArrayList<Double> values = tunSpeakerClustering(referenceClusterSet, key, "ce", clusterSet, clustersGender, featureSet, parameter);
                            if (result.containsKey(key)) {
                                ArrayList<Double> values2 = result.get(key);
                                for (int i = 0; i < values2.size(); i++) {
                                    values2.set(i, values2.get(i) + values.get(i));
                                }
                            } else {
                                result.put(key, values);
                            }
                        }
                    }
                }
            }
        }

        double min = Double.MAX_VALUE;
        for (String key : result.keySet()) {
            ArrayList<Double> values = result.get(key);
            for (int i = 0; i < values.size(); i++) {
                double val = values.get(i);
                //		double thr = (double)(i - Math.round(cMax*mult))/mult;
                //		System.err.println("[result] key="+key+" thr="+thr+" score="+val);
                if (val < min) {
                    min = val;
                }
            }
        }
        for (String key : result.keySet()) {
            ArrayList<Double> values = result.get(key);
            for (int i = 0; i < values.size(); i++) {
                double val = values.get(i);
                double thr = (double) (i - Math.round(cMax * mult)) / mult;
                System.err.print("[result] key=" + key + " thr=" + thr + " score=" + val);
                if (val == min) {
                    System.err.print(" *");
                }
                System.err.println();
            }
        }
        //System.err.println("key="+maxKey+" thr="+maxThreshold+" score="+max);
    }

    protected void putLocalResult(ArrayList<Double> localResult, double prevScore, double score, double value) {
        long dec = Math.round(cMax * mult);
        long end = Math.min(dec, Math.round(score * mult));
        long start = Math.max(-dec, Math.round(prevScore * mult));
        ;
        //System.err.println("pscore="+prevScore+" score="+score+" start="+start+" end="+end+" dec="+dec);
        for (long i = start; i <= end; i++) {
            localResult.set((int) (dec + i), value);
        }
    }

    protected ArrayList<Double> initLocalResult() {
        ArrayList<Double> localResult = new ArrayList<Double>();
        int max = (int) Math.round(cMax * mult);
        localResult.ensureCapacity(max * 2 + 1);
        for (int i = 0; i < max * 2 + 1; i++) {
            localResult.add(i, Double.MAX_VALUE);
        }
        //putLocalResult(localResult, -threshold, threshold, 0.0);
        return localResult;
    }

    public ArrayList<Double> tunSpeakerClustering(ClusterSet referenceClusterSet, String partialKey, String method, ClusterSet clusterSetBase, ClusterSet clusterSet, FeatureSet featureSet, Parameter parameter) throws IOException, DiarizationException,
            ParserConfigurationException, SAXException, TransformerException {
        ArrayList<Double> localResult = initLocalResult();
        String FeatureFormat = "featureSetTransformation";
        String dir = "ester2";
        FeatureSet featureSet2 = loadFeature(featureSet, parameter, clusterSetBase, FeatureFormat + ",1:3:2:0:0:0,13,1:1:300:4");
        URL ubmURL = getClass().getResource(dir + File.separator + "ubm.gmm");
        ArrayList<GMM> ubmVect = MainTools.readGMMContainer(ubmURL, parameter.parameterModel);
        GMM ubm = ubmVect.get(0);
        parameter.parameterModel.setKind("DIAG");
        parameter.parameterClustering.setMethod(method);
        parameter.parameterClustering.setThreshold(cMax);
        parameter.parameterEM.setEMControl("1,5,0.01");
        parameter.parameterTopGaussian.setScoreNTop(5);
        parameter.parameterDiarization.setSaveAllStep(false);
        //parameter.trace = true;
        CLRHClustering clustering = new CLRHClustering(clusterSet, featureSet2, parameter, ubm);
        int nbCluster = clusterSet.clusterGetSize();

        double prevScore = -cMax;
        //System.err.println(parameter.show+" init");
        clustering.init();

        //System.err.println(parameter.show+" get score");
        double score = clustering.getScoreOfCandidatesForMerging();
        double error = DiarizationError.scoreOfMatchedSpeakers(referenceClusterSet, clustering.getClusterSet());
        //System.err.println(parameter.show+" init score");
        putLocalResult(localResult, prevScore, score, error);
        prevScore = score;
        //System.err.println(parameter.show+" clr ="+score);
        while ((score < cMax) && (nbCluster > 1)) {
            clustering.mergeCandidates();
            score = clustering.getScoreOfCandidatesForMerging();
            error = DiarizationError.scoreOfMatchedSpeakers(referenceClusterSet, clustering.getClusterSet());
            putLocalResult(localResult, prevScore, score, error);
            prevScore = score;
            System.err.println(parameter.show + " key=" + partialKey + " clrScore=" + score + " clrError=" + error + " clrSize=" + clustering.getSize());
        }
        parameter.parameterTopGaussian.setScoreNTop(-1);
        parameter.parameterEM.setEMControl("3,10,0.01");
        return localResult;
    }
}
