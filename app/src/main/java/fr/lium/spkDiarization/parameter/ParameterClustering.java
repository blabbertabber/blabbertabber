/**
 * 
 * <p>
 * ParameterClustering
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
 *          not more use
 */

package fr.lium.spkDiarization.parameter;

import java.util.logging.Logger;

/**
 * The Class ParameterClustering.
 */
public class ParameterClustering extends ParameterBase implements Cloneable {

	// Type of clustering method.
	/**
	 * The Enum ClusteringMethod.
	 */
	static public enum ClusteringMethod {

		/** The clust l bic. */
		CLUST_L_BIC, // BIC linear left to right method, mono gaussian
		/** The clust h bic. */
		CLUST_H_BIC, // BIC hierarchical method, mono gaussian
		/** The clust h clr. */
		CLUST_H_CLR, // CLR method, multi gaussian
		/** The clust h ce. */
		CLUST_H_CE, // CE / NCLR method, see Solomonoff, multi gaussian
		/** The clust h icr. */
		CLUST_H_ICR, // ICR method, see Kyu J. Han
		/** The CLUS t_ h_ k l2. */
		CLUST_H_KL2, // Kulback Liebler
		/** The CLUS t_ h_ h2. */
		CLUST_H_H2, // Holister
		/** The clust h gd. */
		CLUST_H_GD, // Divergence gaussian, LIMSI
		/** The clust h gdgmm. */
		CLUST_H_GDGMM, // Mathieux Ben (IRISA), Divergence gaussian of LIMSI applied to a gmm (to check)
		/** The clust r bic. */
		CLUST_R_BIC, // BIC linear right to left method
		/** The CLUS t_ h_ t score. */
		CLUST_H_TScore,
		// IEEE 2009, Cluster criterion Fonctions in spectral subspace and their application in speaker clustering, T.H. Nguyen, H. Li, E.S. Chng
		/** The clust h glr. */
		CLUST_H_GLR, // GLR hierarchical method, mono gaussian
		/** The clust h bic sr. */
		CLUST_H_BIC_SR, // BIC hierarchical method, mono gaussian
		/** The clust h bic gmm em. */
		CLUST_H_BIC_GMM_EM,
		/** The clust h bic gmm map. */
		CLUST_H_BIC_GMM_MAP,
		/** The clust h ce d. */
		CLUST_H_CE_D, // CE / NCLR method, see Solomonoff, multi gaussian + decoding
		/** The clust d bic. */
		CLUST_D_BIC, // BIC diagonal only method, mono gaussian
		/** The clust h c d. */
		CLUST_H_C_D, // CLR method, see Solomonoff, multi gaussian + decoding
		/** The clust l bic sr. */
		CLUST_L_BIC_SR, // BIC hierarchical method, mono gaussian
		/** The clust es iv. */
		CLUST_ES_IV // Exhaustive search clustering using ivector
	};

	/** The Constant ClustMethodString. */
	final public static String[] ClustMethodString = { "l", "h", "c", "ce", "icr", "kl2", "h2", "gd", "gdgmm", "r",
			"t", "glr", "sr", "bicgmmem", "bicgmmmap", "ce_d", "d", "c_d", "l_sr", "es_iv" };

	/** The threshold. */
	private Double threshold; // Clustering threshold.

	/**
	 * The Class ActionThreshold.
	 */
	private class ActionThreshold extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setThreshold(Double.parseDouble(arg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return threshold.toString();
		}

	}

	/** The method. */
	ClusteringMethod method; // Clustering method.

	/**
	 * The Class ActionMethod.
	 */
	private class ActionMethod extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMethod(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String option = longOpt.getName();
			String comment = longOpt.getComment();

			String message = formatStrigArray(ClustMethodString);
			message += " = " + ClustMethodString[method.ordinal()] + "(" + method.ordinal() + ")";
			logger.config("--" + option + " \t " + comment + " " + message);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return null;
		}
	}

	/** The maximum of merge. */
	private Integer maximumOfMerge;

	/**
	 * The Class ActionMaximumOfMerge.
	 */
	private class ActionMaximumOfMerge extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMaximumOfMerge(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return maximumOfMerge.toString();
		}
	}

	/** The minmum of cluster. */
	private Integer minmumOfCluster;

	/**
	 * The Class ActionMinmumOfCluster.
	 */
	private class ActionMinmumOfCluster extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMinimumOfCluster(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return minmumOfCluster.toString();
		}
	}

	/** The minimum of cluster length. */
	private Integer minimumOfClusterLength;

	/**
	 * The Class ActionMinimumOfClusterLength.
	 */
	private class ActionMinimumOfClusterLength extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMinimumOfClusterLength(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return minimumOfClusterLength.toString();
		}
	}

	/**
	 * Instantiates a new parameter clustering.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterClustering(Parameter parameter) {
		super(parameter);
		threshold = 1.0;
		method = ClusteringMethod.CLUST_L_BIC;
		maximumOfMerge = Integer.MAX_VALUE;
		minmumOfCluster = 0;
		minimumOfClusterLength = Integer.MAX_VALUE;

		addOption(new LongOptWithAction("cThr", new ActionThreshold(), "clustering threshold"));
		addOption(new LongOptWithAction("cMethod", new ActionMethod(), "clsutering method"));
		addOption(new LongOptWithAction("cMaximumMerge", new ActionMaximumOfMerge(), "maximum of merge"));
		addOption(new LongOptWithAction("cMinimumOfCluster", new ActionMinmumOfCluster(), "minumum number of clustres at the end of the process"));
		addOption(new LongOptWithAction("cMinimumOfClusterLength", new ActionMinimumOfClusterLength(), "minumum length of each clsuter"));

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterClustering clone() throws CloneNotSupportedException {
		return (ParameterClustering) super.clone();
	}

	/**
	 * Gets the threshold.
	 * 
	 * @return the threshold
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * Sets the threshold.
	 * 
	 * @param clustThr the new threshold
	 */
	public void setThreshold(double clustThr) {
		this.threshold = clustThr;
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public ClusteringMethod getMethod() {
		return method;
	}

	/**
	 * Sets the method.
	 * 
	 * @param ch the new method
	 */
	public void setMethod(String ch) {
		for (ClusteringMethod value : ClusteringMethod.values()) {
			if (ch.equals(ClustMethodString[value.ordinal()])) {
				method = value;
				break;
			}
		}
	}

	/**
	 * Gets the maximum of merge.
	 * 
	 * @return the maximum of merge
	 */
	public int getMaximumOfMerge() {
		return maximumOfMerge;
	}

	/**
	 * Sets the maximum of merge.
	 * 
	 * @param clustNbMaxMerge the new maximum of merge
	 */
	public void setMaximumOfMerge(int clustNbMaxMerge) {
		this.maximumOfMerge = clustNbMaxMerge;
	}

	/**
	 * Gets the minimum of cluster.
	 * 
	 * @return the minimum of cluster
	 */
	public int getMinimumOfCluster() {
		return minmumOfCluster;
	}

	/**
	 * Sets the minimum of cluster.
	 * 
	 * @param clustNbMinClust the new minimum of cluster
	 */
	public void setMinimumOfCluster(int clustNbMinClust) {
		this.minmumOfCluster = clustNbMinClust;
	}

	/**
	 * Gets the minimum of cluster length.
	 * 
	 * @return the minimum of cluster length
	 */
	public int getMinimumOfClusterLength() {
		return minimumOfClusterLength;
	}

	/**
	 * Sets the minimum of cluster length.
	 * 
	 * @param clustMinLen the new minimum of cluster length
	 */
	public void setMinimumOfClusterLength(int clustMinLen) {
		this.minimumOfClusterLength = clustMinLen;
	}

	/**
	 * Gets the method as string.
	 * 
	 * @return the method as string
	 */
	public String getMethodAsString() {
		return ClustMethodString[method.ordinal()];
	}

}
