/*
 * <p>HClustering</p>
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0 Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms. THIS SOFTWARE IS PROVIDED BY THE
 * "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package fr.lium.spkDiarization.libClusteringMethod;

import java.io.IOException;
import java.util.ArrayList;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SquareMatrix;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.DiagGaussian;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * Abstract class for hierarchical bottom-up Clustering;
 * <p/>
 * 2 methods are provided to perform a clustering:
 * <ul>
 * <li>to select the next candidates: the call of method {@link #getScoreOfCandidatesForMerging()} returns the score of the next couple of clusters to merge.</li>
 * <li>the merge are perform by the call of the {@link #mergeCandidates()} method ; the couples of clusters are merge and models, distances are updated. then
 * the end of the clustering could be control out side of the class.</li>
 * </ul>
 */
public abstract class HClustering {

    /**
     * The features.
     */
    protected FeatureSet features;

    /**
     * Inner cluster set.
     */
    protected ClusterSet clusters;

    /**
     * Matrix of distances.
     */
    protected SquareMatrix distances;

    /**
     * The param.
     */
    protected Parameter param;

    /**
     * List of couples (cluster and model).
     */
    protected ArrayList<ClusterAndGMM> clusterAndModelList;

    /**
     * Indices of the two candidates for the next merging.
     */
    protected int ci, cj;

    /**
     * Instantiates a new h clustering.
     *
     * @param _clusters the cluster set
     * @param _features the feature set.
     * @param _param    the parameter
     */
    public HClustering(ClusterSet _clusters, FeatureSet _features, Parameter _param) {
        features = _features;
        param = _param;
        clusters = (ClusterSet) (_clusters.clone());
        int size = clusters.clusterGetSize();
        distances = new SquareMatrix(size);
        clusterAndModelList = new ArrayList<ClusterAndGMM>(size);

        for (Cluster cluster : clusters.clusterSetValue()) {
            ClusterAndGMM clusterAndGMM = new ClusterAndGMM();
            clusterAndGMM.setCluster(cluster);
            clusterAndModelList.add(clusterAndGMM);
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
        result.features = features;
        result.clusters = (ClusterSet) clusters.clone();
        result.distances = (SquareMatrix) distances.clone();
        result.param = param;
        for (int i = 0; i < clusterAndModelList.size(); i++) {
            ClusterAndGMM val = (ClusterAndGMM) clusterAndModelList.get(i).clone();
            result.clusterAndModelList.add(val);
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
     * @return the double
     * @throws DiarizationException the diarization exception
     * @throws IOException          Signals that an I/O exception has occurred.
     */
    protected abstract double computeDistance(int i, int j) throws DiarizationException, IOException;

    /**
     * Direct access to a cluster through its index.
     *
     * @param index the index
     * @return the cluster
     */
    public Cluster getCluster(int index) {
        return clusterAndModelList.get(index).getCluster();
    }

    /**
     * Get the name of a cluster, given its index. Kept for backward-compatibility only; it's better to use getCluster(index).getName()
     *
     * @param index the index
     * @return the cluster name
     */
    public String getClusterName(int index) {
        return clusterAndModelList.get(index).getCluster().getName();
    }

    /**
     * Access to the inner clusters.
     *
     * @return the cluster set
     */
    public ClusterSet getClusterSet() {
        return clusters;
    }

    /**
     * Gets the distances.
     *
     * @return the distances
     */
    public SquareMatrix getDistances() {
        return distances;
    }

    /**
     * Get the cluster with index Ci.
     *
     * @return the first candidate
     */
    public Cluster getFirstCandidate() {
        return clusterAndModelList.get(ci).getCluster();
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
     * @return the model
     */
    public GMM getModel(int i) {
        return clusterAndModelList.get(i).getModel();
    }

    /**
     * Get the a model list.
     *
     * @return the models
     */
    public ArrayList<GMM> getModels() {
        ArrayList<GMM> models = new ArrayList<GMM>();

        for (int i = 0; i < clusterAndModelList.size(); i++) {
            models.add(clusterAndModelList.get(i).getModel());
        }
        return models;
    }

    /**
     * Gets the gaussian divergence of score of clustering.
     *
     * @return the gD of score of clustering
     * @throws DiarizationException the diarization exception
     * @throws IOException          Signals that an I/O exception has occurred.
     */
    public double getGDOfScoreOfClustering() throws DiarizationException, IOException {
        DiagGaussian inter = new DiagGaussian(1);
        DiagGaussian intra = new DiagGaussian(1);
        inter.initStatisticAccumulator();
        intra.initStatisticAccumulator();
        float tab[] = new float[1];
        int size = clusters.clusterGetSize();

        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                tab[0] = (float) distances.get(i, j);
                intra.addFeature(tab);
            }
        }
        for (int i = 0; i < size; i++) {
            tab[0] = (float) computeDistance(i, i);
            inter.addFeature(tab);
        }

        inter.setModelFromAccululator();
        intra.setModelFromAccululator();
        return Distance.GD(inter, intra);
    }

    /**
     * Gets the t distance score of clustering.
     *
     * @return the t distance score of clustering
     * @throws DiarizationException the diarization exception
     * @throws IOException          Signals that an I/O exception has occurred.
     */
    public double getTDistScoreOfClustering() throws DiarizationException, IOException {
        throw new DiarizationException("getTDistScoreOfClustering not implemented");
    }

    /**
     * Find the next candidates for merging.
     *
     * @return the score of candidates for merging
     * @throws DiarizationException the diarization exception
     */
    public double getScoreOfCandidatesForMerging() throws DiarizationException {
        Double min = Double.MAX_VALUE;
        ci = -1;
        cj = -1;
        int size = clusters.clusterGetSize();
        if (size > 1) {
            ci = -1;
            cj = -1;
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    if (distances.get(i, j) < min) {
                        ci = i;
                        cj = j;
                        min = distances.get(i, j);
                    }
                }
            }
        }
        return min;
    }

    /**
     * Get the cluster with index Cj.
     *
     * @return the second candidate
     */
    public Cluster getSecondCandidate() {
        return clusterAndModelList.get(cj).getCluster();
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
     * Initialize the clustering method.
     *
     * @param indexOfFirstCandidate  the index of first candidate
     * @param indexOfSecondCandidate the index of second candidate
     * @throws IOException          Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public void init(int indexOfFirstCandidate, int indexOfSecondCandidate) throws DiarizationException, IOException {
        ci = indexOfFirstCandidate;
        cj = indexOfSecondCandidate;
    }

    /**
     * Merge the two candidate clusters Ci and Cj, update model and distances...
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException          Signals that an I/O exception has occurred.
     */
    public void mergeCandidates() throws DiarizationException, IOException {
        updateOrderOfCandidates();
        mergeClusters();
        updateDistanceMatrixSize();
        updateModels();
        updateClusterAndGMM();
        updateDistances();
    }

    /**
     * Merge the two candidate clusters Ci and Cj. The resulting cluster is at the index previously occupied by the first candidate.
     */
    protected void mergeClusters() {
        clusters.mergeCluster(clusterAndModelList.get(ci).getCluster().getName(), clusterAndModelList.get(cj).getCluster().getName());
    }

    /**
     * Train the i cluster.
     *
     * @param i the i
     * @throws IOException          Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    protected abstract void trainCluster(int i) throws DiarizationException, IOException;

    /**
     * Train all cluster models.
     *
     * @throws IOException          Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     * @see #trainCluster(int)
     */
    protected void trainClusters() throws DiarizationException, IOException {
        for (int i = 0; i < clusterAndModelList.size(); i++) {
            trainCluster(i);
        }
    }

    /**
     * Remove Cj index in #clusterAndModelList.
     */
    protected void updateClusterAndGMM() {
        clusterAndModelList.remove(cj);
    }

    /**
     * Resize the distance matrix.
     */
    protected void updateDistanceMatrixSize() {
        for (int i = cj + 1; i < clusterAndModelList.size(); i++) {
            for (int j = 0; j < clusterAndModelList.size(); j++) {
                distances.set(i - 1, j, distances.get(i, j));
            }
        }
        for (int i = cj + 1; i < clusterAndModelList.size(); i++) {
            for (int j = 0; j < clusterAndModelList.size(); j++) {
                distances.set(j, i - 1, distances.get(j, i));
            }
        }
        for (int i = 0; i < clusterAndModelList.size(); i++) {
            distances.set(i, clusterAndModelList.size() - 1, Double.MAX_VALUE);
            distances.set(clusterAndModelList.size() - 1, i, Double.MAX_VALUE);
        }
    }

    /**
     * Compute new distances for the cluster Ci (after a merge).
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException          Signals that an I/O exception has occurred.
     */
    protected void updateDistances() throws DiarizationException, IOException {
        for (int i = 0; i < ci; i++) {
            distances.set(i, ci, computeDistance(i, ci));
        }
        for (int i = ci + 1; i < clusterAndModelList.size(); i++) {
            distances.set(ci, i, computeDistance(ci, i));
        }
    }

    /**
     * Compute the resulting merged model and remove Cj model.
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException          Signals that an I/O exception has occurred.
     */
    protected abstract void updateModels() throws DiarizationException, IOException;

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
        return clusterAndModelList.size();
    }

    /**
     * return the index of the last candidate.
     *
     * @return the index of last candidate
     */
    public int getIndexOfLastCandidate() {
        return clusterAndModelList.size() - 1;
    }

}
