/**
 * <p>
 * ClusterAndGMM
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

package fr.lium.spkDiarization.libClusteringMethod;

import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libModel.GMM;

/**
 * Container to store a Cluster and GMM, useful in clustering classes.
 */
public class ClusterAndGMM {

    /** The cluster. */
    private Cluster cluster;

    /** The gmm associated to the cluster. */
    private GMM model;

    /** The UBM likelihood score. */
    private double UBMScore;

    /** The model likelihood score. */
    private double modelScore;

    /**
     * Instantiates a new cluster and gmm.
     */
    public ClusterAndGMM() {
        super();
        UBMScore = 0.0;
        modelScore = 0.0;
    }

    public ClusterAndGMM(Cluster cluster, GMM gmm) {
        super();
        UBMScore = 0.0;
        modelScore = 0.0;
        this.cluster = cluster;
        this.model = gmm;
    }

    public ClusterAndGMM(Cluster cluster, GMM gmm, double score) {
        super();
        UBMScore = 0.0;
        modelScore = score;
        this.cluster = cluster;
        this.model = gmm;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        ClusterAndGMM result = (ClusterAndGMM) super.clone();
        result.cluster = (Cluster) cluster.clone();
        result.model = (GMM) model.clone();
        result.UBMScore = UBMScore;
        result.modelScore = modelScore;
        return result;
    }

    /**
     * Gets the cluster.
     *
     * @return the cluster
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * Sets the cluster.
     *
     * @param cluster the new cluster
     */
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    /**
     * Gets the model.
     *
     * @return the model
     */
    public GMM getModel() {
        return model;
    }

    /**
     * Sets the model.
     *
     * @param model the new model
     */
    public void setModel(GMM model) {
        this.model = model;
    }

    /**
     * Gets the model likelihood score.
     *
     * @return the model score
     */
    public double getModelScore() {
        return modelScore;
    }

    /**
     * Sets the model likelihood score.
     *
     * @param modelScore the new model score
     */
    public void setModelScore(double modelScore) {
        this.modelScore = modelScore;
    }

    /**
     * Gets the UBM likelihood score.
     *
     * @return the uBM score
     */
    public double getUBMScore() {
        return UBMScore;
    }

    /**
     * Sets the UBM likelihood score.
     *
     * @param score the new uBM score
     */
    public void setUBMScore(double score) {
        UBMScore = score;
    }
}
