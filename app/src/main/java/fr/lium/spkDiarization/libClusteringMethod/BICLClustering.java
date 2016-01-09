/**
 * <p>
 * BICLClustering
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
import java.util.ArrayList;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

// TODO: Auto-generated Javadoc

/**
 * Linear hierarchical bottom-up clustering class
 *
 * The computation cost of a hierarchical clustering depends of the number of input segments. The computation cost could be reduced by applying first a
 * clustering that merged only adjacent segments S(i) and S(i+1) into S(i) from i=1 to i=K that satisfy Delta BIC(i, j) < 0. if Delta BIC(i, j) > 0 the next
 * candidates are S(i+1) and S(i+2) else the next candidates are the new S(i) and S(i+2).
 * <p>
 * This method could also performed form i=K to i=1.
 * <p>
 * This method is denoted linear clustering.
 */
public class BICLClustering extends BICHClustering {

    /**
     * Instantiates a new bICL clustering.
     *
     * @param _clusters the cluster set
     * @param _features the feature set
     * @param _param the list of parameters
     */
    public BICLClustering(ClusterSet _clusters, FeatureSet _features, Parameter _param) {
        super(_clusters, _features, _param);
        features = _features;
        param = _param;
        clusters = (ClusterSet) (_clusters.clone());
        clusterAndModelList = new ArrayList<ClusterAndGMM>();
        for (Segment segment : clusters.getSegments()) {
            ClusterAndGMM clusterAndGMM = new ClusterAndGMM();
            clusterAndGMM.setCluster(clusters.getCluster(segment.getClusterName()));
            clusterAndModelList.add(clusterAndGMM);
        }
        if (param.trace) System.err.println("clusterAndGMMList size:" + clusterAndModelList.size());
    }

    /**
     * Gets the index of the last feature of the cluster.
     *
     * @param ci the index of the cluster
     *
     * @return the index of the feature
     */
    public int getClusterEnd(int ci) {
        Cluster cluster = clusterAndModelList.get(ci).getCluster();
        int s = cluster.segmentsSize();
        if (s > 1) {
            if (param.trace)
                System.err.println("WARNING [BICLClustering::getClusterEnd] \t more than 1 segment in the cluster size=" + s);
        }
        return cluster.firstSegment().getLast();
    }

    /**
     * Gets the index of the first feature of the cluster.
     *
     * @param ci the index of the cluster
     *
     * @return the index of the feature
     */
    public int getClusterStart(int ci) {
        return clusterAndModelList.get(ci).getCluster().firstSegment().getStart();
    }

    /**
     * @see fr.lium.spkDiarization.libClusteringMethod.HClustering#getScoreOfCandidatesForMerging()
     */
    @Override
    public double getScoreOfCandidatesForMerging() throws DiarizationException {
        double min;
        if (cj >= clusterAndModelList.size()) {
            min = Double.MAX_VALUE;
        } else if (ci < 0) {
            min = Double.MAX_VALUE;
        } else {
            int end = getClusterEnd(ci);
            int start = getClusterStart(cj);
            if (start > (end + 1)) {
                if (param.trace)
                    System.err.println("WARNING [BICLClustering::getScoreOfCandidatesForMerging] \t there is a hole between segments");
                min = 1.0;
            } else {
                if (param.trace)
                    System.err.println("manage : start ci=" + getClusterStart(ci) + " start cj=" + getClusterStart(cj));
                min = computeDistance(ci, cj);
            }
        }
        return min;
    }

    /**
     * @see fr.lium.spkDiarization.libClusteringMethod.BICHClustering#init(int, int)
     */
    @Override
    public void init(int indexOfFirstMergeCandidate, int indexOfSecondMergeCandidate) throws DiarizationException, IOException {
        ci = indexOfFirstMergeCandidate;
        cj = indexOfSecondMergeCandidate;
        trainClusters();
    }

    /**
     * @see fr.lium.spkDiarization.libClusteringMethod.HClustering#mergeCandidates()
     */
    @Override
    public void mergeCandidates() throws DiarizationException {
        updateOrderOfCandidates();
        mergeClusters();
        clusterAndModelList.get(ci).getCluster().collapse();
        updateModels();
        updateClusterAndGMM();
    }

}
