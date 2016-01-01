/**
 * 
 * <p>
 * ParameterSegmentationSplit
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
 * The Class ParameterSegmentationSplit.
 */
public class ParameterSegmentationSplit extends ParameterBase implements Cloneable {

	/** The segment maximum length. */
	private Integer segmentMaximumLength;

	/**
	 * The Class ActionSegmentMaximumLength.
	 */
	private class ActionSegmentMaximumLength extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setSegmentMaximumLength(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return segmentMaximumLength.toString();
		}
	}

	/** The segment minimum length. */
	private Integer segmentMinimumLength;

	/**
	 * The Class ActionSegmentMinimumLength.
	 */
	private class ActionSegmentMinimumLength extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setSegmentMinimumLength(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return segmentMinimumLength.toString();
		}
	}

	/**
	 * Instantiates a new parameter segmentation split.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterSegmentationSplit(Parameter parameter) {
		super(parameter);
		setSegmentMaximumLength(2000);
		setSegmentMinimumLength(200);
		addOption(new LongOptWithAction("sSegMaxLen", new ActionSegmentMaximumLength(), ""));
		addOption(new LongOptWithAction("sSegMinLen", new ActionSegmentMinimumLength(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterSegmentationSplit clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (ParameterSegmentationSplit) super.clone();
	}

	/**
	 * Gets the segment maximum length.
	 * 
	 * @return the segment maximum length
	 */
	public int getSegmentMaximumLength() {
		return segmentMaximumLength;
	}

	/**
	 * Sets the segment maximum length.
	 * 
	 * @param segMaxLen the new segment maximum length
	 */
	public void setSegmentMaximumLength(int segMaxLen) {
		this.segmentMaximumLength = segMaxLen;
	}

	/**
	 * Gets the segment minimum length.
	 * 
	 * @return the segment minimum length
	 */
	public int getSegmentMinimumLength() {
		return segmentMinimumLength;
	}

	/**
	 * Sets the segment minimum length.
	 * 
	 * @param segMinLen the new segment minimum length
	 */
	public void setSegmentMinimumLength(int segMinLen) {
		this.segmentMinimumLength = segMinLen;
	}

}
