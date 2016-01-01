package fr.lium.spkDiarization.libClusteringMethod;

import java.io.IOException;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class BICDClustering.
 */
public class BICDClustering extends BICHClustering {

	/** The logger. */
	final Logger logger = Logger.getLogger(BICDClustering.class.getName());

	/**
	 * Instantiates a new bICD clustering.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public BICDClustering(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		super(clusterSet, featureSet, parameter);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libClusteringMethod.BICHClustering#initialize(int, int)
	 */
	@Override
	public void initialize(int indexOfFirstMergeCandidate, int indexOfSecondMergeCandidate) throws DiarizationException, IOException {
		ci = indexOfFirstMergeCandidate;
		cj = indexOfSecondMergeCandidate;
		trainGmms();
		distances.fill(-Double.MAX_VALUE);
		for (int i = 0; i < (clusterAndGmmList.size() - 1); i++) {
			int j = i + 1;
			distances.set(i, j, computeDistance(i, j));
		}

	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.libClusteringMethod.HClustering#getScoreOfCandidatesForMerging()
	 */
	@Override
	public double getScoreOfCandidatesForMerging() throws DiarizationException {
		// Double min = Double.MAX_VALUE;
		scoreOfMerge = Double.MAX_VALUE;
		ci = -1;
		cj = -1;
		int size = clusterSet.clusterGetSize();

		if (size > 1) {
			ci = -1;
			cj = -1;
			for (int i = 0; i < (size - 1); i++) {
				int j = i + 1;
				if (distances.get(i, j) < scoreOfMerge) {
					ci = i;
					cj = j;
					scoreOfMerge = distances.get(i, j);
				}
			}
		}
		return scoreOfMerge;
	}

}
