/**
 * 
 * <p>
 * ParameterModelSet
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
 * The Class ParameterModelSet.
 */
public abstract class ParameterModelSet extends ParameterBase implements Cloneable {

	/** The Model format string. */
	public static String[] ModelFormatString = { "lium", "alize", "alizeXML", "iv_txt" };

	/**
	 * The Enum ModelFormat.
	 */
	public enum ModelFormat {

		/** The lium. */
		LIUM,
		/** The alize. */
		ALIZE,
		/** The alizexml. */
		ALIZEXML,
		/** The iv txt. */
		IV_TXT
	};

	/** The mask. */
	protected String mask;

	/**
	 * The Class ActionMask.
	 */
	protected class ActionMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t " + type + " model mask = " + getMask());
		}
	}

	/** The format. */
	protected int format;

	/**
	 * The Class ActionFormat.
	 */
	protected class ActionFormat extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setFormat(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t " + type + " model format "
					+ formatStrigArray(ModelFormatString) + " = " + getFormat());
		}
	}

	/** The type. */
	protected String type;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterModelSet clone() throws CloneNotSupportedException {
		return (ParameterModelSet) super.clone();
	}

	/**
	 * Instantiates a new parameter model set.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterModelSet(Parameter parameter) {
		super(parameter);
		mask = "%s.gmm";
		format = 0;
	}

	/**
	 * Gets the mask.
	 * 
	 * @return the mask
	 */
	public String getMask() {
		return mask;
	}

	/**
	 * Gets the format.
	 * 
	 * @return the format
	 */
	public int getFormat() {
		return format;
	}

	/**
	 * Sets the mask.
	 * 
	 * @param modelOutputMask the new mask
	 */
	public void setMask(String modelOutputMask) {
		this.mask = modelOutputMask;
	}

	/**
	 * Sets the format.
	 * 
	 * @param value the new format
	 */
	public void setFormat(String value) {
		for (ModelFormat num : ModelFormat.values()) {
			if (value.equals(ModelFormatString[num.ordinal()])) {
				format = num.ordinal();
			}
		}
	}

}
