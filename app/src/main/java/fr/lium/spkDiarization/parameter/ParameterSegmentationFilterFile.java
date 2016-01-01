/**
 * 
 * <p>
 * ParameterSegmentationFilterFile
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

/**
 * The Class ParameterSegmentationFilterFile.
 */
public class ParameterSegmentationFilterFile extends ParameterSegmentationFile implements Cloneable {

	/** The cluster filter name. */
	private String clusterFilterName; // list of cluster name

	/**
	 * The Class ActionClusterFilterName.
	 */
	private class ActionClusterFilterName extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setClusterFilterName(optarg);
		}
	}

	/**
	 * Instantiates a new parameter segmentation filter file.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterSegmentationFilterFile(Parameter parameter) {
		super(parameter);
		// TODO Auto-generated constructor stub
		setMask("%s.flt.seg");
		clusterFilterName = "j";
		type = "Filter";
		addOption(new LongOptWithAction("s" + type + "Mask", new ActionMask(), ""));
		addOption(new LongOptWithAction("s" + type + "Format", new ActionFormatEncoding(), ""));
		addOption(new LongOptWithAction("s" + type + "Rate", new ActionRate(), ""));
		addOption(new LongOptWithAction("s" + type + "ClusterName", new ActionClusterFilterName(), "name of the filterCluster"));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterSegmentationFilterFile clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (ParameterSegmentationFilterFile) super.clone();
	}

	/**
	 * Sets the cluster filter name.
	 * 
	 * @param clusterFilterName the new cluster filter name
	 */
	public void setClusterFilterName(String clusterFilterName) {
		this.clusterFilterName = clusterFilterName;
	}

	/**
	 * Gets the cluster filter name.
	 * 
	 * @return the cluster filter name
	 */
	public String getClusterFilterName() {
		return clusterFilterName;
	}

}
