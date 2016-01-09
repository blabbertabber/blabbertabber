package fr.lium.example.spkDiarization;

import java.util.ArrayList;
import java.util.TreeSet;

import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libClusteringMethod.BICHClustering;
import fr.lium.spkDiarization.libDecoder.DecoderWithDuration;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.Gaussian;
import fr.lium.spkDiarization.parameter.Parameter;

public class ClusterSetTest {

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws Exception the exception
     */
    public static void main(String[] args) throws Exception {

        // Get all parameters from the command line.
        // MainTools is a class with static method to manage command line.
        Parameter parameters = MainTools.getParameters(args);

        // Read a clusterSet file.
        // The name of the file is defined in parameters instance.
        // MainTools permits to read and write easily containers.
        ClusterSet clusterSet = MainTools.readClusterSet(parameters);

        //Iterate over Cluster
        for (Cluster cluster : clusterSet.clusterSetValue()) {
            //Iterate over segment of the segment
            for (Segment segment : cluster) {
                segment.debug(0);
            }
        }

        // Write the clusterSet in a file.
        // The name of the file is defined in parameters instance.
        MainTools.writeClusterSet(parameters, clusterSet);
    }

    public static void featureSet(Parameter parameters) throws Exception {

        // Read a clusterSet file.
        ClusterSet clusterSet = MainTools.readClusterSet(parameters);

        // Read a FeatureSet file
        FeatureSet featureSet = MainTools.readFeatureSet(parameters, clusterSet);

        //Iterate over Cluster
        for (Cluster cluster : clusterSet.clusterSetValue()) {
            //Iterate over segment of the segment
            for (Segment segment : cluster) {
                // Set the show name in featureSet.
                // If the current show name id different from the previous
                // the features of the current show name are read (or computed).
                featureSet.setCurrentShow(segment.getShowName());
                // Iterate over feature of the segment
                for (int i = 0; i < segment.getLength(); i++) {
                    // Get a feature, ie a array of float
                    float[] feature = featureSet.getFeature(segment.getStart() + i);
                    //alternative:
                    //  featureSet.getFeature(segment.getShowName(), segment.getStart()+i);
                    for (int j = 0; j < feature.length; j++) {
                        //...
                    }
                }
            }
        }

    }

    public ClusterSet clustering(ClusterSet clusterSet, FeatureSet featureSet, Parameter parameters) throws Exception {

        // Get an instance of the BIC clustering class
        BICHClustering clustering = new BICHClustering(clusterSet, featureSet, parameters);
        // set the thresold
        double threshold = 0.0;
        //Initialize the clustering: train models, compute scores between clusters
        clustering.init(); // Ci = 0; Cj = 0;
        // get the score of the two candidate for merging, ie the score of the two "close" clusters
        double score = clustering.getScoreOfCandidatesForMerging();

        while (score > threshold) {
            //Merge the two candidates available by methods
            // clustering.getFirstCandidate() and clustering.getSecondCandidate()
            clustering.mergeCandidates();
            // get the score of the two next candidate for merging
            score = clustering.getScoreOfCandidatesForMerging();
        }
        //get the result
        return clustering.getClusterSet();

    }

    public void training(Cluster cluster, FeatureSet featureSet, GMM gmm, Parameter parameters) throws Exception {
        // Initialize the statistic and score accumulator
        gmm.initStatisticAccumulator();
        gmm.initScoreAccumulator();

        for (Segment segment : cluster) {
            featureSet.setCurrentShow(segment.getShowName());
            // Iterate over feature of the segment
            for (int i = 0; i < segment.getLength(); i++) {
                float[] feature = featureSet.getFeature(segment.getStart() + i);
                // Get the likelihood of the feature (1)
                double lhGMM = gmm.getAndAccumulateLikelihood(feature);
                for (int j = 0; j < gmm.getNbOfComponents(); j++) {
                    // read the likelihood of the component j computed in (1)
                    double lhGaussian = gmm.getComponent(j).getLikelihood();
                    //add weighted feature in the accumulator
                    gmm.getComponent(j).addFeature(feature, lhGaussian / lhGMM);
                }
            }
        }
        // compute a EM iteration of the model from the statistic
        gmm.setModelFromAccululator();
        // Reset the statistic and score accumulator
        gmm.resetStatisticAccumulator();
        gmm.resetScoreAccumulator();
    }

    public void trainingGaussian(Cluster cluster, FeatureSet featureSet, Gaussian gaussian, Parameter parameters) throws Exception {
        // Initialize the statistic and score accumulator
        gaussian.initStatisticAccumulator();
        gaussian.initScoreAccumulator();

        for (Segment segment : cluster) {
            featureSet.setCurrentShow(segment.getShowName());
            // Iterate over feature of the segment
            for (int i = 0; i < segment.getLength(); i++) {
                float[] feature = featureSet.getFeature(segment.getStart() + i);
                //add feature in statistic accumulator
                gaussian.addFeature(feature);
            }
        }
        // compute the model from the statistic
        gaussian.setModelFromAccululator();
        // Reset the statistic and score accumulator
        gaussian.resetStatisticAccumulator();
    }


    public ClusterSet decoding(ClusterSet clusterSet, FeatureSet featureSet, ArrayList<GMM> modelSet, Parameter parameters) throws Exception {
        // Create a decoder
        DecoderWithDuration decoder = new DecoderWithDuration();
        // Setup the HMM states according the GMMs in modelSet and parameters
        decoder.setupHMM(modelSet, parameters);

        // Get the list of segment in the clusterSet
        TreeSet<Segment> segmentList = clusterSet.getSegments();

        // Iterate over segment
        for (Segment segment : segmentList) {
            // accumulate decoder statistic, forward pass
            decoder.accumulate(featureSet, segment);
        }
        // make the backward pass and return the diarization
        return decoder.getClusters(clusterSet);
    }
}
