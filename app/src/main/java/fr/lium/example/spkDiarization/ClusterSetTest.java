package fr.lium.example.spkDiarization;

import java.util.TreeSet;

import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libClusteringMethod.BICHClustering;
import fr.lium.spkDiarization.libDecoder.DecoderWithDuration;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.Gaussian;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class ClusterSetTest.
 */
public class ClusterSetTest {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * 
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

		// Iterate over Cluster
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			// Iterate over segment of the segment
			for (Segment segment : cluster) {
				segment.debug(0);
			}
		}

		// Write the clusterSet in a file.
		// The name of the file is defined in parameters instance.
		MainTools.writeClusterSet(parameters, clusterSet);
	}

	/**
	 * Feature set.
	 * 
	 * @param parameters the parameters
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unused")
	public static void featureSet(Parameter parameters) throws Exception {

		// Read a clusterSet file.
		ClusterSet clusterSet = MainTools.readClusterSet(parameters);

		// Read a FeatureSet file
		AudioFeatureSet featureSet = MainTools.readFeatureSet(parameters, clusterSet);

		// Iterate over Cluster
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			// Iterate over segment of the segment
			for (Segment segment : cluster) {
				// Set the show name in featureSet.
				// If the current show name id different from the previous
				// the features of the current show name are read (or computed).
				featureSet.setCurrentShow(segment.getShowName());
				// Iterate over feature of the segment
				for (int i = 0; i < segment.getLength(); i++) {
					// Get a feature, ie a array of float
					float[] feature = featureSet.getFeatureUnsafe(segment.getStart() + i);
					// alternative:
					// featureSet.getFeature(segment.getShowName(), segment.getStart()+i);
					for (float element : feature) {
						// ...
					}
				}
			}
		}

	}

	/**
	 * Clustering.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameters the parameters
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet clustering(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameters) throws Exception {

		// Get an instance of the BIC clustering class
		BICHClustering clustering = new BICHClustering(clusterSet, featureSet, parameters);
		// set the thresold
		double threshold = 0.0;
		// Initialize the clustering: train models, compute scores between clusters
		clustering.initialize(); // Ci = 0; Cj = 0;
		// get the score of the two candidate for merging, ie the score of the two "close" clusters
		double score = clustering.getScoreOfCandidatesForMerging();

		while (score > threshold) {
			// Merge the two candidates available by methods
			// clustering.getFirstCandidate() and clustering.getSecondCandidate()
			clustering.mergeCandidates();
			// get the score of the two next candidate for merging
			score = clustering.getScoreOfCandidatesForMerging();
		}
		// get the result
		return clustering.getClusterSet();

	}

	/**
	 * Training.
	 * 
	 * @param cluster the cluster
	 * @param featureSet the feature set
	 * @param gmm the gmm
	 * @param parameters the parameters
	 * @throws Exception the exception
	 */
	public void training(Cluster cluster, AudioFeatureSet featureSet, GMM gmm, Parameter parameters) throws Exception {
		// Initialize the statistic and score accumulator
		gmm.statistic_initialize();
		gmm.score_initialize();

		for (Segment segment : cluster) {
			featureSet.setCurrentShow(segment.getShowName());
			// Iterate over feature of the segment
			for (int i = 0; i < segment.getLength(); i++) {
				float[] feature = featureSet.getFeatureUnsafe(segment.getStart() + i);
				// Get the likelihood of the feature (1)
				double lhGMM = gmm.score_getAndAccumulate(feature);
				for (int j = 0; j < gmm.getNbOfComponents(); j++) {
					// read the likelihood of the component j computed in (1)
					double lhGaussian = gmm.getComponent(j).score_getScore();
					// add weighted feature in the accumulator
					gmm.getComponent(j).statistic_addFeature(feature, lhGaussian / lhGMM);
				}
			}
		}
		// compute a EM iteration of the model from the statistic
		gmm.setModel();
		// Reset the statistic and score accumulator
		gmm.statistic_reset();
		gmm.score_reset();
	}

	/**
	 * Training gaussian.
	 * 
	 * @param cluster the cluster
	 * @param featureSet the feature set
	 * @param gaussian the gaussian
	 * @param parameters the parameters
	 * @throws Exception the exception
	 */
	public void trainingGaussian(Cluster cluster, AudioFeatureSet featureSet, Gaussian gaussian, Parameter parameters) throws Exception {
		// Initialize the statistic and score accumulator
		gaussian.statistic_initialize();
		gaussian.score_initialize();

		for (Segment segment : cluster) {
			featureSet.setCurrentShow(segment.getShowName());
			// Iterate over feature of the segment
			for (int i = 0; i < segment.getLength(); i++) {
				float[] feature = featureSet.getFeatureUnsafe(segment.getStart() + i);
				// add feature in statistic accumulator
				gaussian.statistic_addFeature(feature);
			}
		}
		// compute the model from the statistic
		gaussian.setModel();
		// Reset the statistic and score accumulator
		gaussian.statistic_reset();
	}

	/**
	 * Decoding.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param modelSet the model set
	 * @param parameters the parameters
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	public ClusterSet decoding(ClusterSet clusterSet, AudioFeatureSet featureSet, GMMArrayList modelSet, Parameter parameters) throws Exception {
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
		return decoder.getClusterSet();
	}
}
