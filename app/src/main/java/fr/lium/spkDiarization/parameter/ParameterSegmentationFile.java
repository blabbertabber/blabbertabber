/**
 * 
 * <p>
 * ParameterSegmentationFile
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

import java.nio.charset.Charset;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * The Class ParameterSegmentationFile.
 */
public abstract class ParameterSegmentationFile extends ParameterBase implements Cloneable {
	// Type of segmentation file.
	/**
	 * The Enum SegmentationFormat.
	 */
	public enum SegmentationFormat {

		/** The file seg. */
		FILE_SEG,
		/** The file bck. */
		FILE_BCK,
		/** The file ctl. */
		FILE_CTL,
		/** The file sausage. */
		FILE_SAUSAGE,
		/** The file xml epac. */
		FILE_XML_EPAC,
		/** The file xml media. */
		FILE_XML_MEDIA,
		/** The file xml repere. */
		FILE_XML_REPERE,
		/** The file eger hyp. */
		FILE_EGER_HYP
	};

	/** The Segmentation format string. */
	public static String[] SegmentationFormatString = { "seg", "bck", "ctl", "saus.seg", "seg.xml", "media.xml",
			"repere.xml", "eger.hyp" };

	/** The Segmentation encoding string. */
	public static String[] SegmentationEncodingString = { "ISO-8859-1", "UTF8" };

	/** The type. */
	protected String type;

	/** The format encoding. */
	protected String formatEncoding;

	/** The format. */
	protected SegmentationFormat format;

	/** The encoding. */
	protected Charset encoding;

	/**
	 * The Class ActionFormatEncoding.
	 */
	protected class ActionFormatEncoding extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setFormatEncoding(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String formatList = formatStrigArray(SegmentationFormatString);
			String encodingList = formatStrigArray(SegmentationEncodingString);

			logger.config("--" + longOpt.getName() + " \t " + type + " segmentation file format = "
					+ SegmentationFormatString[format.ordinal()] + "," + encoding.name() + " (" + formatList + ", "
					+ encodingList + ")" + " [" + logger.getName() + "]");
		}
	}

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
			logger.config("--" + longOpt.getName() + " \t " + type + " segmentation file mask = " + getMask() + " ["
					+ logger.getName() + "]");
		}
	}

	/** The rate. */
	protected Integer rate; // feature rate in ms

	/**
	 * The Class ActionRate.
	 */
	protected class ActionRate extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setRate(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t " + type + " segmentation file rate = " + getRate() + " ["
					+ logger.getName() + "]");
		}
	}

	/**
	 * Instantiates a new parameter segmentation file.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterSegmentationFile(Parameter parameter) {
		super(parameter);
		setFormat(SegmentationFormat.FILE_SEG);
		encoding = Parameter.DefaultCharset;
		setMask("");
		type = "seg";
		setRate(100);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterSegmentationFile clone() throws CloneNotSupportedException {
		return (ParameterSegmentationFile) super.clone();
	}

	/**
	 * Gets the format.
	 * 
	 * @return the format
	 */
	public SegmentationFormat getFormat() {
		return format;
	}

	/**
	 * Sets the format.
	 * 
	 * @param format the new format
	 */
	private void setFormat(SegmentationFormat format) {
		this.format = format;
	}

	/**
	 * Gets the encoding.
	 * 
	 * @return the encoding
	 */
	public Charset getEncoding() {
		return encoding;
	}

	/**
	 * Sets the encoding.
	 * 
	 * @param encoding the new encoding
	 */
	private void setEncoding(String encoding) {
		this.encoding = Charset.forName(encoding);
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
	 * Sets the mask.
	 * 
	 * @param mask the new mask
	 */
	public void setMask(String mask) {
		this.mask = mask;
	}

	/**
	 * Sets the format encoding.
	 * 
	 * @param optarg the new format encoding
	 */
	public void setFormatEncoding(String optarg) {
		formatEncoding = optarg;
		StringTokenizer stok = new StringTokenizer(optarg, ",");
		int cpt = 0;
		while (stok.hasMoreTokens()) {
			String ch = stok.nextToken();
			cpt++;
			if (cpt == 1) {
				for (SegmentationFormat num : SegmentationFormat.values()) {
					if (ch.equals(SegmentationFormatString[num.ordinal()])) {
						setFormat(num);
					}
				}
			}
			if (cpt == 2) {
				setEncoding(ch);
			}
		}

	}

	/**
	 * Gets the rate.
	 * 
	 * @return the rate
	 */
	public int getRate() {
		return rate;
	}

	/**
	 * Sets the rate.
	 * 
	 * @param rate the rate to set
	 */
	public void setRate(int rate) {
		this.rate = rate;
	}

}