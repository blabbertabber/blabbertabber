/**
 * <p>
 * BICHClustering
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
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
package fr.lium.spkDiarization.libClusteringMethod;

import java.io.IOException;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.Gaussian;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterClustering.ClusteringMethod;

// TODO: Auto-generated Javadoc

/**
 * BICHierarchical clustering class based on a BIC distance computed using Gaussian for cluster model.
 * <p>
 * The clustering is based upon a bottom-up hierarchical clustering. Each segment is associated to a cluster providing the initial set of clusters. The two
 * closest clusters c_i and c_j are merged at each iteration until a stop criterion is met.
 * <p>
 * Various metric and stop criterion have been proposed in [Siu1991,Solomonoff1998,Chen1998a,Reynolds1998]. One of those is Delta BIC metric employed to select
 * the clusters to group as well as to stop the merge process. The candidate clusters c_i and c_j are selected according to:<br/>
 * min (i > j, i in {1, ..., K}) Delta BIC(i, j)<br/>
 * with K the number of cluster.
 * <p>
 * Moreover the merge process is stopped when Delta BIC(i, j) > 0. Two kinds of Delta BIC metrics differing by the penalty factor are proposed:
 * <ul>
 * <li>the local Delta BIC penalty factor Pl with Ni and Nj respectively the sum of segment length in Ci and Cj:<br/>
 * Pl = 1/2 (d + d(d+1)/2) log(N1 + N2)</li>
 * <li>or the global BIC penalty factor Pg with N the length of the whole signal:<br/>
 * Pg = 1/2 (d + d(d+1)/2) log(N)</li>
 * <p>
 * The LIMSI proved that local BIC penalty factor gives better result in [Barras2004] and this is confirmed in [Tranter2004].
 */
public class BICHClustering extends HClustering {

    /** The BIC penality constant. */
    protected double BICPenalityConstant; // Constant in BIC.
    protected double threshold; // Constant in BIC.

    /**
     * Instantiates a new bICH clustering.
     *
     * @param _clusters the cluster set
     * @param _features the feature set
     * @param parameters the list of parameters
     */
    public BICHClustering(ClusterSet _clusters, FeatureSet _features, Parameter parameters) {
        super(_clusters, _features, parameters);
        threshold = parameters.parameterClustering.getThreshold();
        BICPenalityConstant = Distance.BICConstant(parameters.parameterModel.getKind(), _features.getDim(), threshold);
    }

    /**
     * Compute a BIC distance between \e i and \e j.
     *
     * @param i the i index
     * @param j the j index
     *
     * @return the distance value
     *
     * @throws DiarizationException the sphinx clust exception
     */
    @Override
    protected double computeDistance(int i, int j) throws DiarizationException {
        double score = 0.0;
        Gaussian gi = clusterAndModelList.get(i).getModel().getComponent(0);
        Gaussian gj = clusterAndModelList.get(j).getModel().getComponent(0);
        int minLen = param.parameterClustering.getMinimumOfClusterLength();
        if ((gi.getCount() > minLen) && (gj.getCount() > minLen)) {
            if (param.trace) {
                System.out.println("trace[BICHClustering::computeDistance] \t to big = " + gi.getCount() + " / " + gj.getCount() + " (" + minLen + ")");
            }
            score = Double.MAX_VALUE;
        } else {
            if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_BIC) {
                score = Distance.BICLocal(gi, gj, BICPenalityConstant);
            } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_L_BIC) {
                score = Distance.BICLocal(gi, gj, BICPenalityConstant);
            } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_R_BIC) {
                score = Distance.BICLocal(gi, gj, BICPenalityConstant);
            } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_H2) {
                score = Distance.H2(gi, gj) - threshold;
            } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_GD) {
                score = Distance.GD(gi, gj) - threshold;
            } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_KL2) {
                score = Distance.KL2(gi, gj) - threshold;
            } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_ICR) {
                score = Distance.ICR(gi, gj, param.parameterClustering.getThreshold());
            } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_GLR) {
                score = Distance.GLR(gi, gj) - threshold;
            } else {
                throw new DiarizationException("[BICHClustering::computeDistance] \t cluster method don't exist");
            }
        }
        return score;
    }

    public void init() throws DiarizationException, IOException {
        init(0, 0);
    }

    /**
     * Initialize clustering. Train the model of each cluster and compute the distances between the clusters.
     *
     * @param indexOfFirstMergeCandidate the index of first merged candidate
     * @param indexOfSecondMergeCandidate the index of second merged candidate
     *
     * @throws DiarizationException the sphinx clust exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Override
    public void init(int indexOfFirstMergeCandidate, int indexOfSecondMergeCandidate) throws DiarizationException, IOException {
        super.init(indexOfFirstMergeCandidate, indexOfSecondMergeCandidate);
        trainClusters();
        distances.init(0.0);
        for (int i = 0; i < clusterAndModelList.size(); i++) {
            for (int j = i + 1; j < clusterAndModelList.size(); j++) {
                distances.set(i, j, computeDistance(i, j));
            }
        }
    }

    /**
     * Train a cluster.
     *
     * @param i the index
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the sphinx clust exception
     */
    @Override
    protected void trainCluster(int i) throws DiarizationException, IOException {
        ClusterAndGMM clusterAndGMM = clusterAndModelList.get(i);
        clusterAndGMM.setModel(new GMM(1, features.getDim(), param.parameterModel.getKind()));
        Cluster cluster = clusterAndGMM.getCluster();
        Gaussian gaussian = clusterAndGMM.getModel().getComponent(0);
        gaussian.initStatisticAccumulator();
        gaussian.addFeaturesFromSegments(cluster.iterator(), features);
// length += gaussian.getAccumulatorCount();
        gaussian.setModelFromAccululator();
        clusterAndGMM.getModel().setName(cluster.getName());
        if (param.trace) {
            System.out.println("trace[BICHClustering::train] \t i=" + i + " name=" + clusterAndGMM.getModel().getName());
        }
    }

    /**
     * @see fr.lium.spkDiarization.libClusteringMethod.HClustering#updateModels()
     */
    @Override
    protected void updateModels() throws DiarizationException {
        Gaussian gi = clusterAndModelList.get(ci).getModel().getComponent(0);
        Gaussian gj = clusterAndModelList.get(cj).getModel().getComponent(0);
        gi.add(gj);
        gi.setModelFromAccululator();
    }
}
