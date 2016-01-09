package fr.lium.spkDiarization.tools;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

public class MfccMlpConcat {
    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MfccMlpConcat");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = null;
                if (param.parameterSegmentationInputFile.getMask().equals("")) {
                    clusters = new ClusterSet();
                    Cluster cluster = clusters.createANewCluster("init");
                    Segment segment = new Segment(param.show, 0, Integer.MAX_VALUE, cluster);
                    cluster.addSegment(segment);

                } else {
                    // clusters
                    clusters = MainTools.readClusterSet(param);
                    clusters.collapse();
                }
                FeatureSet features = MainTools.readFeatureSet(param, clusters);
                features.setChangePositionOfEnergy(false);
                FeatureSet features2 = MainTools.readFeatureSet2(param, clusters);

                for (String showName : clusters.getShowNames()) {
                    // Features
                    features.setCurrentShow(showName);
                    features2.setCurrentShow(showName);
                    System.err.println("info: input1 num features = " + features.getNumberOfFeatures());
                    System.err.println("info: input2 num features = " + features2.getNumberOfFeatures());
                    if (Math.abs(features.getNumberOfFeatures() - features2.getNumberOfFeatures()) > 10) {
                        features.debug(2);
                        features2.debug(2);

                        throw new DiarizationException("MfccMlpConcat difference in number of features larger than 10: num1=" + features.getNumberOfFeatures() + " num2=" + features2.getNumberOfFeatures());
                    }
                    int numFeatures = Math.min(features.getNumberOfFeatures(), features2.getNumberOfFeatures());
                    FeatureSet featuresResult = new FeatureSet(numFeatures, param.parameterOutputFeature.getFeaturesDescription());

                    for (int i = 0; i < numFeatures; i++) {
                        float[] mfcc = features.getFeature(i);

                        float[] mlp = features2.getFeature(i);
                        float[] mfccMlp = new float[mfcc.length + mlp.length];

                        System.arraycopy(mfcc, 0, mfccMlp, 0, mfcc.length);
                        System.arraycopy(mlp, 0, mfccMlp, mfcc.length, mlp.length);
                        //System.err.println("mfcc[0]="+mfcc[0]+" mlp[0]="+mlp[0]+" mfccMlp[0]="+mfccMlp[0]+" mfccMlp[mfcc.length]="+mfccMlp[mfcc.length]);
                        featuresResult.addFrame(mfccMlp);
                    }
                    MainTools.writeFeatureSet(showName, param, featuresResult);
                    System.err.println("info: output num features = " + featuresResult.getNumberOfFeatures());
                }
            } else {
                System.err.println("error: no show found in segments file");
            }
        } catch (Exception e) {
            System.err.println("error \t exception " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void info(Parameter param, String prog) {
        if (param.help) {
            param.printSeparator2();
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterInputFeature.print();
            param.parameterInputFeature2.print();
            param.parameterOutputFeature.print();
            param.printSeparator();
            param.parameterSegmentationInputFile.printMask(); // sInMask
            param.parameterSegmentationInputFile.printEncodingFormat();

        }
    }

}
