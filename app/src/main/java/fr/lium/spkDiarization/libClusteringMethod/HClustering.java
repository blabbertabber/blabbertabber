/*
 * <p>HClustering</p>
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0 Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms. THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fr.lium.spkDiarization.libClusteringMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixSquare;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.DiagGaussian;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * Abstract class for hierarchical bottom-up Clustering;
 * <p>
 * 2 methods are provided to perform a clustering:
 * <ul>
 * <li>to select the next candidates: the call of method {@link #getScoreOfCandidatesForMerging()} returns the score of the next couple of clusters to merge.</li>
 * <li>the merge are perform by the call of the {@link #mergeCandidates()} method ; the couples of clusters are merge and models, distances are updated. then the end of the clustering could be control out side of the class.</li>
 * </ul>
 */
public abstract class HClustering {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(HClustering.class.getName());

	/** The features. */
	protected AudioFeatureSet featureSet;

	/** Inner cluster set. */
	protected ClusterSet clusterSet;

	/** Matrix of distances. */
	protected MatrixSquare distances;

	/** The param. */
	protected Parameter parameter;

	/** List of couples (cluster and model). */
	protected List<ClusterAndGMM> clusterAndGmmList;

	/** Indices of the two candidates for the next merging. */
	protected int ci, cj;

	/** The nb merge. */
	protected int nbMerge;

	/** The score of merge. */
	protected double scoreOfMerge;

	/** The key. */
	protected String key = "EMPTY";

	/**
	 * Instantiates a new h clustering.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set.
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public HClustering(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		this.featureSet = featureSet;
		this.parameter = parameter;
		this.clusterSet = (clusterSet.clone());
// this.clusterSet = clusterSet;
		if (featureSet.getClusterSet() == clusterSet) {
			featureSet.setClusterSet(this.clusterSet);
		}
		int size = this.clusterSet.clusterGetSize();
		distances = new MatrixSquare(size);
		clusterAndGmmList = new ArrayList<ClusterAndGMM>(size);

		for (Cluster cluster : this.clusterSet.clusterSetValue()) {
			ClusterAndGMM clusterAndGMM = new ClusterAndGMM();
			clusterAndGMM.setCluster(cluster);
			clusterAndGmmList.add(clusterAndGMM);
		}
		ci = cj = -1;

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		HClustering result = (HClustering) super.clone();
		result.featureSet = featureSet;
		result.clusterSet = clusterSet.clone();
		result.distances = distances.copy();
		result.parameter = parameter;
		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			ClusterAndGMM val = (ClusterAndGMM) clusterAndGmmList.get(i).clone();
			result.clusterAndGmmList.add(val);
		}
		result.ci = ci;
		result.cj = cj;

		return result;
	}

	/**
	 * Compute distance of the model #i and #j.
	 * 
	 * @param i the i index
	 * @param j the j index
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract double computeDistance(int i, int j) throws DiarizationException, IOException;

	/**
	 * Direct access to a cluster through its index.
	 * 
	 * @param index the index
	 * 
	 * @return the cluster
	 */
	public Cluster getCluster(int index) {
		return clusterAndGmmList.get(index).getCluster();
	}

	/**
	 * Get the name of a cluster, given its index. Kept for backward-compatibility only; it's better to use getCluster(index).getName()
	 * 
	 * @param index the index
	 * 
	 * @return the cluster name
	 */
	public String getClusterName(int index) {
		return clusterAndGmmList.get(index).getCluster().getName();
	}

	/**
	 * Access to the inner clusters.
	 * 
	 * @return the cluster set
	 */
	public ClusterSet getClusterSet() {
		return clusterSet;
	}

	/**
	 * Gets the distances.
	 * 
	 * @return the distances
	 */
	public MatrixSquare getDistances() {
		return distances;
	}

	/**
	 * Get the cluster with index Ci.
	 * 
	 * @return the first candidate
	 */
	public Cluster getFirstCandidate() {
		return clusterAndGmmList.get(ci).getCluster();
	}

	/**
	 * Get the value of first candidate, ie Ci.
	 * 
	 * @return the index of first candidate
	 */
	public int getIndexOfFirstCandidate() {
		return ci;
	}

	/**
	 * Get the value of the second candidate, ie Cj.
	 * 
	 * @return the index of second candidate
	 */
	public int getIndexOfSecondCandidate() {
		return cj;
	}

	/**
	 * Get the i rank model.
	 * 
	 * @param i the i
	 * 
	 * @return the model
	 */
	public GMM getGmm(int i) {
		return clusterAndGmmList.get(i).getGmm();
	}

	/**
	 * Get the a model list.
	 * 
	 * @return the models
	 */
	public GMMArrayList getGmmList() {
		GMMArrayList models = new GMMArrayList();

		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			models.add(clusterAndGmmList.get(i).getGmm());
		}
		return models;
	}

	/**
	 * Gets the gaussian divergence of score of clustering.
	 * 
	 * @return the gD of score of clustering
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double getGDOfScoreOfClustering() throws DiarizationException, IOException {
		DiagGaussian inter = new DiagGaussian(1);
		DiagGaussian intra = new DiagGaussian(1);
		inter.statistic_initialize();
		intra.statistic_initialize();
		float tab[] = new float[1];
		int size = clusterSet.clusterGetSize();

		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				tab[0] = (float) distances.get(i, j);
				intra.statistic_addFeature(tab);
			}
		}
		for (int i = 0; i < size; i++) {
			tab[0] = (float) computeDistance(i, i);
			inter.statistic_addFeature(tab);
		}

		inter.setModel();
		intra.setModel();
		return Distance.GD(inter, intra);
	}

	/**
	 * Gets the t distance score of clustering.
	 * 
	 * @return the t distance score of clustering
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public double getTDistScoreOfClustering() throws DiarizationException, IOException {
		throw new DiarizationException("getTDistScoreOfClustering not implemented");
	}

	/**
	 * Find the next candidates for merging.
	 * 
	 * @return the score of candidates for merging
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public double getScoreOfCandidatesForMerging() throws DiarizationException {
		// Double min = Double.MAX_VALUE;
		scoreOfMerge = Double.MAX_VALUE;
		ci = -1;
		cj = -1;
		int size = clusterSet.clusterGetSize();
		if (size > 1) {
			ci = -1;
			cj = -1;
			for (int i = 0; i < size; i++) {
				for (int j = i + 1; j < size; j++) {
					if (distances.get(i, j) < scoreOfMerge) {
						ci = i;
						cj = j;
						scoreOfMerge = distances.get(i, j);
					}
				}
			}
		}
		return scoreOfMerge;
	}

	/**
	 * Print the matrix of scores.
	 * 
	 * @param prefix the prefix
	 * @return the string
	 */
	public String printScoreMatrix(String prefix) {
		String ch = "";
		int size = clusterSet.clusterGetSize();
		if (size > 1) {
			for (int i = 0; i < size; i++) {
				ch += prefix + " " + clusterAndGmmList.get(cj).getCluster().getName() + " =";
				for (int j = 0; j < i; j++) {
					ch += " " + distances.get(i, j);
				}
				ch += "\n";
			}
		}
		return ch;
	}

	/**
	 * Get the cluster with index Cj.
	 * 
	 * @return the second candidate
	 */
	public Cluster getSecondCandidate() {
		return clusterAndGmmList.get(cj).getCluster();
	}

	/**
	 * Increment by 1 the value of Ci.
	 */
	public void incrementIndexOfFirstCandidate() {
		ci++;
	}

	/**
	 * Increment by 1 the value of Cj.
	 */
	public void incrementIndexOfSecondCandidate() {
		cj++;
	}

	/**
	 * Decrement by 1 the value of Ci.
	 */
	public void decrementIndexOfFirstCandidate() {
		ci--;
	}

	/**
	 * Decrement by 1 the value of Cj.
	 */
	public void decrementIndexOfSecondCandidate() {
		cj--;
	}

	/**
	 * Initialize clustering. Train the model of each cluster and compute the distances between the clusters.
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void initialize() throws DiarizationException, IOException {
		initialize(0, 0);
	}

	/**
	 * Initialize the clustering method.
	 * 
	 * @param indexOfFirstCandidate the index of first candidate
	 * @param indexOfSecondCandidate the index of second candidate
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void initialize(int indexOfFirstCandidate, int indexOfSecondCandidate) throws DiarizationException, IOException {
		ci = indexOfFirstCandidate;
		cj = indexOfSecondCandidate;
	}

	/**
	 * Merge the two candidate clusters Ci and Cj, update model and distances...
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void mergeCandidates() throws DiarizationException, IOException {
		updateOrderOfCandidates();
		mergeClusters();
		updateDistanceMatrixSize();
		updateGmms();
		updateClusterAndGMM();
		updateDistances();
	}

	/**
	 * Merge the two candidate clusters Ci and Cj. The resulting cluster is at the index previously occupied by the first candidate.
	 */
	protected void mergeClusters() {
		clusterSet.mergeCluster(clusterAndGmmList.get(ci).getCluster().getName(), clusterAndGmmList.get(cj).getCluster().getName());
	}

	/**
	 * Merge the two candidate clusters Ci and Cj. The resulting cluster is at the index previously occupied by the first candidate.
	 */
	protected void mergeClustersAndAddInformation() {
		clusterSet.mergeClusterAndAddInformation(key, clusterAndGmmList.get(ci).getCluster().getName(), clusterAndGmmList.get(cj).getCluster().getName(), nbMerge, scoreOfMerge);
		nbMerge++;
	}

	/**
	 * Train the i cluster.
	 * 
	 * @param i the i
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract void trainGmm(int i) throws DiarizationException, IOException;

	/**
	 * Train all cluster models.
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @see #trainGmm(int)
	 */
	protected void trainGmms() throws DiarizationException, IOException {
		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			trainGmm(i);
		}
	}

	/**
	 * Remove Cj index in #clusterAndModelList.
	 */
	protected void updateClusterAndGMM() {
		clusterAndGmmList.remove(cj);
	}

	/**
	 * Prints the distance.
	 */
	public void printDistance() {
		String show = parameter.show;
		int size = clusterAndGmmList.size();
		if (size > 1) {
			for (int i = 0; i < size; i++) {
				String nameI = clusterAndGmmList.get(i).getCluster().getName();
				for (int j = i + 1; j < size; j++) {
					String nameJ = clusterAndGmmList.get(j).getCluster().getName();

					logger.info(show + " distance ( " + nameI + " , " + nameJ + " ) = " + distances.get(i, j));
				}
			}
		}
	}

	/**
	 * Resize the distance matrix.
	 */
	protected void updateDistanceMatrixSize() {
		for (int i = cj + 1; i < clusterAndGmmList.size(); i++) {
			for (int j = 0; j < clusterAndGmmList.size(); j++) {
				distances.set(i - 1, j, distances.get(i, j));
			}
		}
		for (int i = cj + 1; i < clusterAndGmmList.size(); i++) {
			for (int j = 0; j < clusterAndGmmList.size(); j++) {
				distances.set(j, i - 1, distances.get(j, i));
			}
		}
		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			distances.set(i, clusterAndGmmList.size() - 1, Double.MAX_VALUE);
			distances.set(clusterAndGmmList.size() - 1, i, Double.MAX_VALUE);
		}
	}

	/**
	 * Compute new distances for the cluster Ci (after a merge).
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void updateDistances() throws DiarizationException, IOException {
		for (int i = 0; i < ci; i++) {
			distances.set(i, ci, computeDistance(i, ci));
		}
		for (int i = ci + 1; i < clusterAndGmmList.size(); i++) {
			distances.set(ci, i, computeDistance(ci, i));
		}
	}

	/**
	 * Compute the resulting merged model and remove Cj model.
	 * 
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected abstract void updateGmms() throws DiarizationException, IOException;

	/**
	 * Swap Ci and Cj if Ci > Cj.
	 */
	protected void updateOrderOfCandidates() {
		if (ci > cj) {
			int tmp = ci;
			ci = cj;
			cj = tmp;
		}
	}

	/**
	 * return the number of models.
	 * 
	 * @return the size
	 */
	public int getSize() {
		return clusterAndGmmList.size();
	}

	/**
	 * return the index of the last candidate.
	 * 
	 * @return the index of last candidate
	 */
	public int getIndexOfLastCandidate() {
		return clusterAndGmmList.size() - 1;
	}

}
