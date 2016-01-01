/**
 * 
 * <p>
 * ParameterTopGaussian
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

import fr.lium.spkDiarization.parameter.ParameterModelSet.ModelFormat;

/**
 * The Class ParameterTopGaussian.
 */
public class ParameterTopGaussian extends ParameterBase implements Cloneable {

	/** The n top string. */
	private String nTopString = "";

	/**
	 * The Class ActionScoreNTopGMMMask.
	 */
	private class ActionScoreNTopGMMMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setTopGaussian(arg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t use top Gaussians (ntop,modelMask,modelFormat) = "
					+ nTopString + " [" + logger.getName() + "]");
			logger.config("\t nb = " + getScoreNTop());
			logger.config("\t model = " + getScoreNTopGMMMask());
			logger.config("\t format = " + getFormatToString() + "(" + getFormat() + ")");
		}
	}

	/** The score n top gmm mask. */
	private String scoreNTopGMMMask;

	/** The score n top. */
	private int scoreNTop;

	/** The format. */
	private int format;

	/**
	 * Instantiates a new parameter top gaussian.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterTopGaussian(Parameter parameter) {
		super(parameter);
		setScoreNTop(-1);
		setScoreNTopGMMMask("");
		setFormat(ParameterModelSet.ModelFormatString[0]);
		addOption(new LongOptWithAction("sTop", new ActionScoreNTopGMMMask(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterTopGaussian clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (ParameterTopGaussian) super.clone();
	}

	/**
	 * Gets the score n top gmm mask.
	 * 
	 * @return the score n top gmm mask
	 */
	public String getScoreNTopGMMMask() {
		return scoreNTopGMMMask;
	}

	/**
	 * Sets the score n top gmm mask.
	 * 
	 * @param scoreNTopGMMMask the new score n top gmm mask
	 */
	protected void setScoreNTopGMMMask(String scoreNTopGMMMask) {
		this.scoreNTopGMMMask = scoreNTopGMMMask;
	}

	/**
	 * Gets the score n top.
	 * 
	 * @return the score n top
	 */
	public int getScoreNTop() {
		return scoreNTop;
	}

	/**
	 * Sets the score n top.
	 * 
	 * @param scoreNTop the new score n top
	 */
	public void setScoreNTop(int scoreNTop) {
		this.scoreNTop = scoreNTop;
	}

	/**
	 * Sets the top gaussian.
	 * 
	 * @param optarg the new top gaussian
	 */
	public void setTopGaussian(String optarg) {
		nTopString = optarg;
		String values[] = optarg.split(",");
		setScoreNTop(Integer.parseInt(values[0]));
		setScoreNTopGMMMask(values[1]);

		if (values.length > 2) {
			setFormat(values[2]);
		}
	}

	/**
	 * Sets the format.
	 * 
	 * @param value the new format
	 */
	public void setFormat(String value) {
		for (ModelFormat num : ModelFormat.values()) {
			// logger.finest("***" + value + "=" + LabelTypeString[num.ordinal()]);
			if (value.equals(ParameterModelSet.ModelFormatString[num.ordinal()])) {
				format = num.ordinal();
			}
		}
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
	 * Gets the format to string.
	 * 
	 * @return the format to string
	 */
	public String getFormatToString() {
		return ParameterModelSet.ModelFormatString[format];
	}

	/**
	 * Checks if is use top.
	 * 
	 * @return true, if is use top
	 */
	public boolean isUseTop() {
		return (getScoreNTop() > 0);
	}

	/**
	 * Log top gaussian.
	 */
	public void logTopGaussian() {
	}
}