/**
 * <p>
 * CLRHClustering
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
import java.util.Iterator;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.GMMFactory;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterClustering.ClusteringMethod;

// TODO: Auto-generated Javadoc

/**
 * hierarchical bottom-up clustering class based on GMM distance (CLR, CE)
 *
 * The clustering is based upon a bottom-up hierarchical clustering. Each segment is associated to a cluster providing the initial set of clusters. The two
 * closest clusters Ci and Cj are merged at each iteration until a stop criterion is met.
 */
public class CLRHClustering extends HClustering {

    /** The length: Number of features in segmentation. */
    protected long length;

    /** The world model. */
    protected GMM worldModel;

    /** The mean of the scores. */
    protected double scoresMean;

    /** The std of the scores. */
    protected double scoreStd;

    /**
     * Instantiates a new cLRH clustering.
     *
     * @param _clusters the cluster set
     * @param _features the feature set
     * @param _param the list of parameters
     * @param ubm the Universal Background Model
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the sphinx clust exception
     */
    public CLRHClustering(ClusterSet _clusters, FeatureSet _features, Parameter _param, GMM ubm) throws IOException, DiarizationException {
        super(_clusters, _features, _param);
        scoresMean = 0.0;
        param = _param;
        worldModel = (GMM) ubm.clone();
        computeWorldModelScores();
        length = clusters.getLength();
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.HClustering#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        CLRHClustering result = (CLRHClustering) super.clone();
        result.length = length; // Number of features in segmentation.
        result.worldModel = (GMM) worldModel.clone();
        result.scoresMean = scoresMean;
        result.scoreStd = scoreStd;

        return result;
    }

    /**
     * Compute a BIC distance between \e i and \e j.
     *
     * @param i the i index
     * @param j the j index
     *
     * @return the distance
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the sphinx clust exception
     */
    @Override
    public double computeDistance(int i, int j) throws DiarizationException, IOException {
        double score = 0.0;
        ClusterAndGMM clusterAndGMMI = clusterAndModelList.get(i);
        GMM gmmI = clusterAndGMMI.getModel();
        Cluster clusterI = clusterAndGMMI.getCluster();

        ClusterAndGMM clusterAndGMMJ = clusterAndModelList.get(j);
        GMM gmmJ = clusterAndGMMJ.getModel();
        Cluster clusterJ = clusterAndGMMJ.getCluster();
        boolean top = (param.parameterTopGaussian.getScoreNTop() > 0);

        if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_CLR) {
            score = Distance.CLR(gmmI, gmmJ, worldModel, clusterI, clusterJ, clusterAndGMMI.getUBMScore(), clusterAndGMMJ.getUBMScore(), features, top);
        } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_CE) {
            score = Distance.CE(gmmI, gmmJ, clusterI, clusterJ, features, top);
        } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_GDGMM) {
            score = Distance.GDMAP(gmmI, gmmJ);
        } else if (param.parameterClustering.getMethod() == ClusteringMethod.CLUST_H_TScore) {
            score = Distance.TDist(gmmI, gmmJ, worldModel, clusterI, clusterJ, features, top, 1);
        } else {
            if (param.trace) System.out.println("trace[CLRHClust] \t MAX");
            score = Double.MAX_VALUE;
        }
        return score;
    }

    /**
     * Compute the world model scores.
     *
     * @throws DiarizationException the sphinx clust exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void computeWorldModelScores() throws DiarizationException, IOException {
        for (int i = 0; i < clusterAndModelList.size(); i++) {
            ClusterAndGMM clusterAndGMM = clusterAndModelList.get(i);
            Cluster cluster = clusterAndGMM.getCluster();
            double score;
            if (param.parameterTopGaussian.getScoreNTop() > 0) {
                score = Distance.getScoreSetTop(worldModel, param.parameterTopGaussian.getScoreNTop(), cluster.iterator(), features);
            } else {
                score = Distance.getScore(worldModel, cluster.iterator(), features);
            }
            clusterAndGMM.setUBMScore(score);
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.HClustering#getTDistScoreOfClustering()
     */
    @Override
    public double getTDistScoreOfClustering() throws DiarizationException, IOException {
        boolean ntop = param.parameterTopGaussian.getScoreNTop() > 0;
        return Distance.TDistStopCriterion(clusterAndModelList, worldModel, features, ntop, 30);
    }

    /**
     * Gets the log likelihood of clustering.
     *
     * @return the log likelihood of this clustering
     */
    public double getLogLikelihoodOfClustering() {
        double res = 0;
        for (int i = 0; i < clusterAndModelList.size(); i++) {
            res += clusterAndModelList.get(i).getModelScore();
        }
        return res / length;
    }

    /**
     * Gets the log likelihood ratio of clustering.
     *
     * @return the log likelihood ratio of this clustering
     */
    public double getLogLikelihoodRatioOfClustering() {
        double res = 0;
        double res2 = 0;
        for (int i = 0; i < clusterAndModelList.size(); i++) {
            res += clusterAndModelList.get(i).getModelScore();
            res2 += clusterAndModelList.get(i).getUBMScore();
        }
        return (res / length) - (res2 / length);
    }

    /**
     * Gets the world model.
     *
     * @return the world model
     */
    public GMM getWorldModel() {
        return worldModel;
    }

    /**
     * Initialize clustering. Train the model of each cluster and compute the distances between the clusters.
     *
     * @param indexOfFirstMergeCandidate the index of first merge candidate
     * @param indexOfSecondMergeCandidate the index of second merge candidate
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the sphinx clust exception
     */
    @Override
    public void init(int indexOfFirstMergeCandidate, int indexOfSecondMergeCandidate) throws DiarizationException, IOException {
        super.init(indexOfFirstMergeCandidate, indexOfSecondMergeCandidate);
        trainClusters();
        distances.init(0.0);
        double sum = 0.0;
        double sum2 = 0.0;
        int nb = 0;
        for (int i = 0; i < clusterAndModelList.size(); i++) {
            for (int j = i + 1; j < clusterAndModelList.size(); j++) {
                double score = computeDistance(i, j);
                sum += score;
                sum2 += score * score;
                nb++;
                distances.set(i, j, score);
            }
        }
    }

    /**
     * Initialize clustering. Train the model of each cluster and compute the distances between the clusters.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the sphinx clust exception
     */
    public void init() throws DiarizationException, IOException {
        init(0, 0);
    }

    /**
     * Merge two clusters and update model and distances.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the sphinx clust exception
     */
    @Override
    public void mergeCandidates() throws DiarizationException, IOException {
        updateOrderOfCandidates();
        mergeClusters();
        updateDistanceMatrixSize();
        updateClusterAndGMM();
        updateModels();
        updateWorldModelScores();
        updateDistances();
    }

    /**
     * Train a cluster.
     *
     * @param i the i
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the sphinx clust exception
     */
    @Override
    protected void trainCluster(int i) throws DiarizationException, IOException {
        ClusterAndGMM clusterAndGMM = clusterAndModelList.get(i);
        Cluster cluster = clusterAndGMM.getCluster();
        GMM init = (GMM) (worldModel.clone());
        // trainer MAP
        if (param.trace) {
            System.out.println("trace ------------------------------------");
            System.out.println("trace[CLRHClust] \t mTrainMAP cluster=" + cluster.getName());
        }
        clusterAndGMM.setModel(GMMFactory.getMAP(cluster, features, init, worldModel, param.parameterEM, param.parameterMAP, param.parameterVarianceControl,
                param.parameterTopGaussian, param.trace));
        if (param.trace)
            System.out.println("trace[hclustering] \t i=" + i + " name=" + clusterAndGMM.getModel().getName());
        clusterAndGMM.setModelScore(clusterAndGMM.getModel().getSumLogLikelihood());
        length += clusterAndGMM.getModel().getCountLogLikelihood();
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.HClustering#updateDistances()
     */
    @Override
    protected void updateDistances() throws DiarizationException, IOException {
        for (int i = 0; i < ci; i++) {
            distances.set(i, ci, computeDistance(i, ci));
        }
        for (int i = ci + 1; i < clusterAndModelList.size(); i++) {
            distances.set(ci, i, computeDistance(ci, i));
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.HClustering#updateModels()
     */
    @Override
    protected void updateModels() throws DiarizationException, IOException {
        ClusterAndGMM clusterAndGMM = clusterAndModelList.get(ci);
        Cluster cluster = clusterAndGMM.getCluster();

        GMM initilizationGMM = (GMM) (worldModel.clone());
        clusterAndGMM.setModel(GMMFactory.getMAP(cluster, features, initilizationGMM, worldModel, param.parameterEM, param.parameterMAP,
                param.parameterVarianceControl, param.parameterTopGaussian, param.trace));
        GMM gmm = clusterAndGMM.getModel();
        clusterAndGMM.setModelScore(gmm.getSumLogLikelihood());
    }

    /**
     * Update world model scores.
     *
     * @throws DiarizationException the sphinx clust exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void updateWorldModelScores() throws DiarizationException, IOException {
        ClusterAndGMM clusterAndGMM = clusterAndModelList.get(ci);
        Cluster cluster = clusterAndGMM.getCluster();
        Iterator<Segment> itSeg = cluster.iterator();
        clusterAndGMM.setUBMScore(Distance.getScore(worldModel, itSeg, features, (param.parameterTopGaussian.getScoreNTop() > 0)));
    }
}
