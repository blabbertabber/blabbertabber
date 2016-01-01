/**
 * 
 * <p>
 * ParameterFilter
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
 * The Class ParameterFilter.
 */
public class ParameterFilter extends ParameterBase implements Cloneable {

	/** The silence minimum length. */
	private Integer silenceMinimumLength;

	/**
	 * The Class ActionSilenceMinimumLength.
	 */
	private class ActionSilenceMinimumLength extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setSilenceMinimumLength(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return silenceMinimumLength.toString();
		}
	}

	/** The speech minimum length. */
	private Integer speechMinimumLength;

	/**
	 * The Class ActionSpeechMinimumLength.
	 */
	private class ActionSpeechMinimumLength extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setSpeechMinimumLength(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return speechMinimumLength.toString();
		}
	}

	/** The segment padding. */
	private Integer segmentPadding;

	/**
	 * The Class ActionSegmentPadding.
	 */
	private class ActionSegmentPadding extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setSegmentPadding(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return segmentPadding.toString();
		}
	}

	/**
	 * Instantiates a new parameter filter.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterFilter(Parameter parameter) {
		super(parameter);
		setSilenceMinimumLength(150);
		setSpeechMinimumLength(150);
		setSegmentPadding(25);
		addOption(new LongOptWithAction("fltSegMinLenSil", new ActionSilenceMinimumLength(), ""));
		addOption(new LongOptWithAction("fltSegMinLenSpeech", new ActionSpeechMinimumLength(), ""));
		addOption(new LongOptWithAction("fltSegPadding", new ActionSegmentPadding(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterFilter clone() throws CloneNotSupportedException { // TODO Auto-generated method stub
		return (ParameterFilter) super.clone();
	}

	/**
	 * Gets the silence minimum length.
	 * 
	 * @return the silence minimum length
	 */
	public int getSilenceMinimumLength() {
		return silenceMinimumLength;
	}

	/**
	 * Sets the silence minimum length.
	 * 
	 * @param segMinLenSil the new silence minimum length
	 */
	public void setSilenceMinimumLength(int segMinLenSil) {
		this.silenceMinimumLength = segMinLenSil;
	}

	/**
	 * Gets the speech minimum length.
	 * 
	 * @return the speech minimum length
	 */
	public int getSpeechMinimumLength() {
		return speechMinimumLength;
	}

	/**
	 * Sets the speech minimum length.
	 * 
	 * @param segMinLenSpeech the new speech minimum length
	 */
	public void setSpeechMinimumLength(int segMinLenSpeech) {
		this.speechMinimumLength = segMinLenSpeech;
	}

	/**
	 * Gets the segment padding.
	 * 
	 * @return the segment padding
	 */
	public int getSegmentPadding() {
		return segmentPadding;
	}

	/**
	 * Sets the segment padding.
	 * 
	 * @param segPadding the new segment padding
	 */
	public void setSegmentPadding(int segPadding) {
		this.segmentPadding = segPadding;
	}
}