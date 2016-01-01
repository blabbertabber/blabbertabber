/**
 * 
 * <p>
 * BICLClustering
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
import java.util.ArrayList;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

// TODO: Auto-generated Javadoc
/**
 * Linear hierarchical bottom-up clustering class
 * 
 * The computation cost of a hierarchical clustering depends of the number of input segments. The computation cost could be reduced by applying first a clustering that merged only adjacent segments S(i) and S(i+1) into S(i) from i=1 to i=K that
 * satisfy Delta BIC(i, j) < 0. if Delta BIC(i, j) > 0 the next candidates are S(i+1) and S(i+2) else the next candidates are the new S(i) and S(i+2).
 * <p>
 * This method could also performed form i=K to i=1.
 * <p>
 * This method is denoted linear clustering.
 */
public class BICLClustering extends BICHClustering {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(BICLClustering.class.getName());

	/**
	 * Instantiates a new bICL clustering.
	 * 
	 * @param clusterSet the cluster set
	 * @param featureSet the feature set
	 * @param parameter the list of parameters
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public BICLClustering(ClusterSet clusterSet, AudioFeatureSet featureSet, Parameter parameter) throws DiarizationException, IOException {
		super(clusterSet, featureSet, parameter);
		key = "LBIC";
		this.featureSet = featureSet;
		this.parameter = parameter;
		this.clusterSet = (clusterSet.clone());
		this.clusterAndGmmList = new ArrayList<ClusterAndGMM>();
		for (Segment segment : this.clusterSet.getSegments()) {
			ClusterAndGMM clusterAndGMM = new ClusterAndGMM();
			clusterAndGMM.setCluster(this.clusterSet.getCluster(segment.getClusterName()));
			clusterAndGmmList.add(clusterAndGMM);
		}
		if (SpkDiarizationLogger.DEBUG) logger.finer("clusterAndGMMList size:" + clusterAndGmmList.size());
	}

	/**
	 * Gets the index of the last feature of the cluster.
	 * 
	 * @param index the index of the cluster
	 * 
	 * @return the index of the feature
	 */
	public int getClusterEnd(int index) {
		Cluster cluster = clusterAndGmmList.get(index).getCluster();
		int s = cluster.segmentsSize();
		if (s > 1) {
			logger.warning("more than 1 segment in the cluster size=" + s);
		}
		return cluster.firstSegment().getLast();
	}

	/**
	 * Gets the index of the first feature of the cluster.
	 * 
	 * @param index the index of the cluster
	 * 
	 * @return the index of the feature
	 */
	public int getClusterStart(int index) {
		return clusterAndGmmList.get(index).getCluster().firstSegment().getStart();
	}

	/**
	 * Gets the score of candidates for merging.
	 * 
	 * @return the score of candidates for merging
	 * @throws DiarizationException the diarization exception
	 * @see fr.lium.spkDiarization.libClusteringMethod.HClustering#getScoreOfCandidatesForMerging()
	 */
	@Override
	public double getScoreOfCandidatesForMerging() throws DiarizationException {
		double min;
		if (cj >= clusterAndGmmList.size()) {
			min = Double.MAX_VALUE;
		} else if (ci < 0) {
			min = Double.MAX_VALUE;
		} else {
			int end = getClusterEnd(ci);
			int start = getClusterStart(cj);
			if (start > (end + 1)) {
				logger.warning("there is a hole between segments ");
				min = 1.0;
			} else {
				if (SpkDiarizationLogger.DEBUG) logger.finer("manage : start " + ci + "=" + getClusterStart(ci) + " start " + cj + "="
						+ getClusterStart(cj));
				min = computeDistance(ci, cj);
			}
		}
		return min;
	}

	/**
	 * Initialize.
	 * 
	 * @param indexOfFirstMergeCandidate the index of first merge candidate
	 * @param indexOfSecondMergeCandidate the index of second merge candidate
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @see fr.lium.spkDiarization.libClusteringMethod.BICHClustering#initialize(int, int)
	 */
	@Override
	public void initialize(int indexOfFirstMergeCandidate, int indexOfSecondMergeCandidate) throws DiarizationException, IOException {
		ci = indexOfFirstMergeCandidate;
		cj = indexOfSecondMergeCandidate;
		trainGmms();
	}

	/**
	 * Merge candidates.
	 * 
	 * @throws DiarizationException the diarization exception
	 * @see fr.lium.spkDiarization.libClusteringMethod.HClustering#mergeCandidates()
	 */
	@Override
	public void mergeCandidates() throws DiarizationException {
		updateOrderOfCandidates();
		mergeClusters();
		clusterAndGmmList.get(ci).getCluster().collapse();
		updateGmms();
		updateClusterAndGMM();
	}

}
