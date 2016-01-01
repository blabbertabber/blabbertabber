/**
 * 
 * <p>
 * BICHClustering
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package fr.lium.spkDiarization.libClusteringMethod;

import java.io.IOException;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.Gaussian;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterClustering.ClusteringMethod;

// TODO: Auto-generated Javadoc
/**
 * BICHierarchical clustering class based on a BIC distance computed using Gaussian for cluster model.
 * <p>
 * The clustering is based upon a bottom-up hierarchical clustering. Each segment is associated to a cluster providing the initial set of clusters. The two closest clusters c_i and c_j are merged at each iteration until a stop criterion is met.
 * <p>
 * Various metric and stop criterion have been proposed in [Siu1991,Solomonoff1998,Chen1998a,Reynolds1998]. One of those is Delta BIC metric employed to select the clusters to group as well as to stop the merge process. The candidate clusters c_i and
 * c_j are selected according to:<br/>
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

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(BICHClustering.class.getName());

	/** The BIC penality constant. */
	protected double BICPenalityConstant; // Constant in BIC.

	/** The threshold. */
	protected double threshold; // Constant in BIC.

	/**
	 * Instantiates a new bICH clustering.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public BICHClustering(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		super(clusterSet, featureSet, parameter);
		key = "HBIC";
		threshold = parameter.getParameterClustering().getThreshold();
		BICPenalityConstant = Distance.BICGaussianConstant(parameter.getParameterModel().getModelKind(), featureSet.getFeatureSize(), threshold);
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
		Gaussian gaussianI = clusterAndGmmList.get(i).getGmm().getComponent(0);
		Gaussian gaussianJ = clusterAndGmmList.get(j).getGmm().getComponent(0);
		int minLength = parameter.getParameterClustering().getMinimumOfClusterLength();
		if ((gaussianI.getCount() > minLength) && (gaussianJ.getCount() > minLength)) {
			logger.warning("computeDistance to big = " + gaussianI.getCount() + " / " + gaussianJ.getCount() + " ("
					+ minLength + ") set to Double.Max");
			score = Double.MAX_VALUE;
		} else {
			if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_BIC) {
				score = Distance.BICLocal(gaussianI, gaussianJ, BICPenalityConstant);
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_L_BIC) {
				score = Distance.BICLocal(gaussianI, gaussianJ, BICPenalityConstant);
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_D_BIC) {
				score = Distance.BICLocal(gaussianI, gaussianJ, BICPenalityConstant);
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_R_BIC) {
				score = Distance.BICLocal(gaussianI, gaussianJ, BICPenalityConstant);
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_H2) {
				score = Distance.H2(gaussianI, gaussianJ) - threshold;
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_GD) {
				score = Distance.GD(gaussianI, gaussianJ) - threshold;
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_KL2) {
				score = Distance.KL2(gaussianI, gaussianJ) - threshold;
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_ICR) {
				score = Distance.ICR(gaussianI, gaussianJ, parameter.getParameterClustering().getThreshold());
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_GLR) {
				score = Distance.GLR(gaussianI, gaussianJ) - threshold;
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_H_BIC_SR) {
				score = Distance.BICSquareRoot(gaussianI, gaussianJ, BICPenalityConstant);
			} else if (parameter.getParameterClustering().getMethod() == ClusteringMethod.CLUST_L_BIC_SR) {
				score = Distance.BICSquareRoot(gaussianI, gaussianJ, BICPenalityConstant);
			} else {
				throw new DiarizationException("cluster distance don't exist");
			}
		}
		return score;
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
	public void initialize(int indexOfFirstMergeCandidate, int indexOfSecondMergeCandidate) throws DiarizationException, IOException {
		super.initialize(indexOfFirstMergeCandidate, indexOfSecondMergeCandidate);
		trainGmms();
		distances.fill(0.0);
		for (int i = 0; i < clusterAndGmmList.size(); i++) {
			for (int j = i + 1; j < clusterAndGmmList.size(); j++) {
				distances.set(i, j, computeDistance(i, j));
			}
		}
	}

	/**
	 * Train a cluster.
	 * 
	 * @param i the index
	 * @throws DiarizationException the sphinx clust exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	protected void trainGmm(int i) throws DiarizationException, IOException {
		if (SpkDiarizationLogger.DEBUG) logger.fine("train GMM");
		ClusterAndGMM clusterAndGMM = clusterAndGmmList.get(i);
		clusterAndGMM.setGmm(new GMM(1, featureSet.getFeatureSize(), parameter.getParameterModel().getModelKind()));
		Cluster cluster = clusterAndGMM.getCluster();
		Gaussian gaussian = clusterAndGMM.getGmm().getComponent(0);
		gaussian.statistic_initialize();
		gaussian.statistic_addFeatures(cluster, featureSet);
// length += gaussian.getAccumulatorCount();
		gaussian.setModel();
		clusterAndGMM.getGmm().setName(cluster.getName());
		if (SpkDiarizationLogger.DEBUG) logger.finer("train i=" + i + " name=" + clusterAndGMM.getGmm().getName());
	}

	/**
	 * Update gmms.
	 * 
	 * @throws DiarizationException the diarization exception
	 * @see fr.lium.spkDiarization.libClusteringMethod.HClustering#updateGmms()
	 */
	@Override
	protected void updateGmms() throws DiarizationException {
		Gaussian gi = clusterAndGmmList.get(ci).getGmm().getComponent(0);
		Gaussian gj = clusterAndGmmList.get(cj).getGmm().getComponent(0);
		gi.statistic_add(gj);
		gi.setModel();
	}

}
