/**
 * 
 * <p>
 * ClusterAndGMM
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package fr.lium.spkDiarization.libClusteringMethod;

import java.util.logging.Logger;

import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libModel.gaussian.GMM;

/**
 * Container to store a Cluster and GMM, useful in clustering classes.
 */
public class ClusterAndGMM {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ClusterAndGMM.class.getName());

	/** The cluster. */
	private Cluster cluster;

	/** The gmm associated to the cluster. */
	private GMM gmm;

	/** The UBM likelihood score. */
	private double UBMScore;

	/** The model likelihood score. */
	private double gmmScore;

	/**
	 * Debug.
	 */
	public void debug() {
		int end = cluster.getLength() + cluster.firstSegment().getStart();
		logger.finer("clusterName:" + cluster.getName() + " gmmName:" + gmm.getName() + " cluster Start/len/end/nb:"
				+ cluster.firstSegment().getStart() + "/" + cluster.getLength() + "/" + end + "/"
				+ cluster.segmentsSize());
	}

	/**
	 * Instantiates a new cluster and gmm.
	 */
	public ClusterAndGMM() {
		super();
		UBMScore = 0.0;
		gmmScore = 0.0;
	}

	/**
	 * Instantiates a new cluster and gmm.
	 * 
	 * @param cluster the cluster
	 * @param gmm the gmm
	 */
	public ClusterAndGMM(Cluster cluster, GMM gmm) {
		super();
		UBMScore = 0.0;
		gmmScore = 0.0;
		this.cluster = cluster;
		this.gmm = gmm;
	}

	/**
	 * Instantiates a new cluster and gmm.
	 * 
	 * @param cluster the cluster
	 * @param gmm the gmm
	 * @param score the score
	 */
	public ClusterAndGMM(Cluster cluster, GMM gmm, double score) {
		super();
		UBMScore = 0.0;
		gmmScore = score;
		this.cluster = cluster;
		this.gmm = gmm;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		ClusterAndGMM result = (ClusterAndGMM) super.clone();
		result.cluster = cluster.clone();
		result.gmm = gmm.clone();
		result.UBMScore = UBMScore;
		result.gmmScore = gmmScore;
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
	 * Gets the model.
	 * 
	 * @return the model
	 */
	public GMM getGmm() {
		return gmm;
	}

	/**
	 * Gets the model likelihood score.
	 * 
	 * @return the model score
	 */
	public double getGmmScore() {
		return gmmScore;
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
	 * Sets the cluster.
	 * 
	 * @param cluster the new cluster
	 */
	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * Sets the model.
	 * 
	 * @param gmm the new model
	 */
	public void setGmm(GMM gmm) {
		this.gmm = gmm;
	}

	/**
	 * Sets the model likelihood score.
	 * 
	 * @param gmmScore the new model score
	 */
	public void setGmmScore(double gmmScore) {
		this.gmmScore = gmmScore;
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
