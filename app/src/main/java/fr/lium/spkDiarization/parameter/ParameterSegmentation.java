/**
 * 
 * <p>
 * ParameterSegmentation
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
 * The Class ParameterSegmentation.
 */
public class ParameterSegmentation extends ParameterBase implements Cloneable {
	// Type of distance for segmentation.
	/**
	 * The Enum SegmentationMethod.
	 */
	public enum SegmentationMethod {

		/** The seg bic. */
		SEG_BIC,
		/** The seg glr. */
		SEG_GLR,
		/** The SE g_ k l2. */
		SEG_KL2,
		/** The seg gd. */
		SEG_GD,
		/** The SE g_ h2. */
		SEG_H2,
		/** The seg energy. */
		SEG_ENERGY,
		/** The seg gaussian. */
		SEG_GAUSSIAN,
		/** The seg glr it. */
		SEG_GLR_IT

	};

	/** The Constant SegmentationMethodString. */
	public final static String[] SegmentationMethodString = { "BIC", "GLR", "KL2", "GD", "H2", "E", "GAUSSIAN",
			"GLR_IT" };

	/** The model window size. */
	private Integer modelWindowSize; // Size of a window in segmentation, ie number of feature for the learning of a model.

	/**
	 * The Class ActionModelWindowSize.
	 */
	private class ActionModelWindowSize extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setModelWindowSize(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return modelWindowSize.toString();
		}
	}

	/** The minimim window size. */
	private Integer minimimWindowSize; // Minimum size of segment.

	/**
	 * The Class ActionMinimimWindowSize.
	 */
	private class ActionMinimimWindowSize extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMinimimWindowSize(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return minimimWindowSize.toString();
		}
	}

	/** The threshold. */
	private Double threshold; // Segmentation threshold.

	/**
	 * The Class ActionThreshold.
	 */
	private class ActionThreshold extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setThreshold(Long.parseLong(optarg));
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
	protected SegmentationMethod method; // Segmentation method.

	/**
	 * The Class ActionSegmentationMethod.
	 */
	private class ActionSegmentationMethod extends LongOptAction {

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
			logger.config("--" + longOpt.getName() + " \t segmentation similarity "
					+ formatStrigArray(SegmentationMethodString) + " = "
					+ SegmentationMethodString[getMethod().ordinal()] + "(" + getMethod().ordinal() + ")" + " ["
					+ logger.getName() + "]");
		}
	}

	// private SegSilMethod segSilMethod; // Silence segmentation method.
	/** The recursion. */
	private Boolean recursion;

	/**
	 * The Class ActionRecursion.
	 */
	private class ActionRecursion extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setRecursion(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return recursion.toString();
		}
	}

	//
	/**
	 * Instantiates a new parameter segmentation.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterSegmentation(Parameter parameter) {
		super(parameter);
		setModelWindowSize(250);
		setMinimimWindowSize(250);
		setThreshold(Integer.MIN_VALUE);
		method = SegmentationMethod.SEG_GLR;
		setRecursion(false);
		addOption(new LongOptWithAction("sModelWindowSize", new ActionModelWindowSize(), "seg 1/2 window size (in features)"));
		addOption(new LongOptWithAction("sMinimumWindowSize", new ActionMinimimWindowSize(), "seg min size segment (in features)"));
		addOption(new LongOptWithAction("sThr", new ActionThreshold(), "segmentation threshold"));
		addOption(new LongOptWithAction("sMethod", new ActionSegmentationMethod(), ""));
		addOption(new LongOptWithAction("sRecursion", 0, new ActionRecursion(), "segmentation make by a recursion fonction"));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterSegmentation clone() throws CloneNotSupportedException {
		return (ParameterSegmentation) super.clone();
	}

	/**
	 * Sets the model window size.
	 * 
	 * @param segWSize the new model window size
	 */
	public void setModelWindowSize(int segWSize) {
		this.modelWindowSize = segWSize;
	}

	/**
	 * Gets the model window size.
	 * 
	 * @return the model window size
	 */
	public int getModelWindowSize() {

		return modelWindowSize;
	}

	/**
	 * Sets the minimim window size.
	 * 
	 * @param segMinWSize the new minimim window size
	 */
	public void setMinimimWindowSize(int segMinWSize) {
		this.minimimWindowSize = segMinWSize;
	}

	/**
	 * Gets the minimim window size.
	 * 
	 * @return the minimim window size
	 */
	public int getMinimimWindowSize() {
		return minimimWindowSize;
	}

	/**
	 * Sets the threshold.
	 * 
	 * @param segThr the new threshold
	 */
	public void setThreshold(double segThr) {
		this.threshold = segThr;
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
	 * Sets the method.
	 * 
	 * @param ch the new method
	 */
	public void setMethod(String ch) {
		for (SegmentationMethod num : SegmentationMethod.values()) {
			if (ch.equals(SegmentationMethodString[num.ordinal()])) {
				method = num;
			}
		}
	}

	/**
	 * Gets the method.
	 * 
	 * @return the method
	 */
	public SegmentationMethod getMethod() {
		return method;
	}

	/**
	 * Gets the method as string.
	 * 
	 * @return the method as string
	 */
	public String getMethodAsString() {
		return SegmentationMethodString[getMethod().ordinal()];
	}

	/**
	 * Sets the recursion.
	 * 
	 * @param segRecursion the new recursion
	 */
	public void setRecursion(boolean segRecursion) {
		this.recursion = segRecursion;
	}

	/**
	 * Checks if is recursion.
	 * 
	 * @return true, if is recursion
	 */
	public boolean isRecursion() {
		return recursion;
	}

}
