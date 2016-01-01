/**
 * 
 * <p>
 * ParameterScore
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
 * The Class ParameterScore.
 */
public class ParameterScore extends ParameterBase implements Cloneable {

	/** The Constant LabelTypeString. */
	public final static String[] LabelTypeString = { "none", "add", "remplace" };

	// Type of duration constraint for Viterbi.
	/**
	 * The Enum LabelType.
	 */
	public enum LabelType {

		/** The label type none. */
		LABEL_TYPE_NONE,
		/** The label type add. */
		LABEL_TYPE_ADD,
		/** The label type replace. */
		LABEL_TYPE_REPLACE
	};

	/** The score threshold. */
	private Double scoreThreshold;

	/**
	 * The Class ActionScoreThreshold.
	 */
	private class ActionScoreThreshold extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setScoreThreshold(Double.valueOf(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return scoreThreshold.toString();
		}
	}

	/** The gender. */
	private Boolean gender;

	/**
	 * The Class ActionGender.
	 */
	private class ActionGender extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setGender(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return gender.toString();
		}
	}

	/** The T norm. */
	private Boolean TNorm;

	/**
	 * The Class ActionTNorm.
	 */
	private class ActionTNorm extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setTNorm(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return TNorm.toString();
		}
	}

	/** The Z norm. */
	private Boolean ZNorm;

	/**
	 * The Class ActionZNorm.
	 */
	private class ActionZNorm extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setZNorm(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return ZNorm.toString();
		}
	}

	/** The LL ratio. */
	private Boolean LLRatio;

	/**
	 * The Class ActionLLRatio.
	 */
	private class ActionLLRatio extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setLLRatio(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return LLRatio.toString();
		}
	}

	/** The by cluster. */
	private Boolean byCluster;

	/**
	 * The Class ActionByCluster.
	 */
	private class ActionByCluster extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setByCluster(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return byCluster.toString();
		}
	}

	/** The by segment. */
	private Boolean bySegment;

	/**
	 * The Class ActionBySegment.
	 */
	private class ActionBySegment extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setBySegment(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return bySegment.toString();
		}
	}

	/** The label format. */
	private Integer labelFormat;

	/**
	 * The Class ActionLabelFormat.
	 */
	private class ActionLabelFormat extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setLabel(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String ch = formatStrigArray(LabelTypeString);
			logger.config("--" + longOpt.getName() + " " + ch + " = " + LabelTypeString[labelFormat] + " ["
					+ logger.getName() + "]");
		}

	}

	/**
	 * The Class ActionMAPNorm.
	 */
	private class ActionMAPNorm extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMapNorm(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " [meanTarget:stdTarget:meanNonTarget:stdNonTarget]= "
					+ meanTarget + ":" + stdTarget + "," + meanNonTarget + ":" + stdNonTarget + ","
					+ probabilityAPrioriTarget + " [" + logger.getName() + "]");

		}
	}

	/** The map norm. */
	private String mapNorm;

	/** The mean target. */
	private double meanTarget;

	/** The mean non target. */
	private double meanNonTarget;

	/** The probability a priori target. */
	private double probabilityAPrioriTarget;

	/** The std target. */
	private double stdTarget;

	/** The std non target. */
	private double stdNonTarget;

	/** The model list. */
	private String modelList;

	/**
	 * The Class ActionModelList.
	 */
	private class ActionModelList extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setModelList(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return modelList;
		}
	}

	/**
	 * Instantiates a new parameter score.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterScore(Parameter parameter) {
		super(parameter);
		setLabel(LabelTypeString[LabelType.LABEL_TYPE_NONE.ordinal()]);
		setGender(false);
		setTNorm(false);
		setZNorm(false);
		setLLRatio(false);
		setByCluster(false);
		setBySegment(false);
		setScoreThreshold(-Double.MAX_VALUE);
		setMapNorm("0:1,0:1,0.1");
		meanTarget = 0.0;
		meanNonTarget = 0.0;
		stdTarget = 1.0;
		stdNonTarget = 1.0;
		probabilityAPrioriTarget = 0.1;
		modelList = "";
		addOption(new LongOptWithAction("sScoreThr", new ActionScoreThreshold(), ""));
		addOption(new LongOptWithAction("sGender", 0, new ActionGender(), ""));
		addOption(new LongOptWithAction("sByCluster", 0, new ActionByCluster(), ""));
		addOption(new LongOptWithAction("sTNorm", 0, new ActionTNorm(), ""));
		addOption(new LongOptWithAction("sMAPNorm", new ActionMAPNorm(), ""));
		addOption(new LongOptWithAction("sZNorm", 0, new ActionZNorm(), ""));
		addOption(new LongOptWithAction("sLLRatio", 0, new ActionLLRatio(), ""));
		addOption(new LongOptWithAction("sBySegment", 0, new ActionBySegment(), ""));
		addOption(new LongOptWithAction("sSetLabel", new ActionLabelFormat(), ""));
		addOption(new LongOptWithAction("sModelList", new ActionModelList(), ""));

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterScore clone() throws CloneNotSupportedException {
		return (ParameterScore) super.clone();
	}

	/**
	 * Checks if is z norm.
	 * 
	 * @return the zNorm
	 */
	public boolean isZNorm() {
		return ZNorm;
	}

	/**
	 * Sets the z norm.
	 * 
	 * @param zNorm the zNorm to set
	 */
	public void setZNorm(boolean zNorm) {
		ZNorm = zNorm;
	}

	/**
	 * Checks if is lL ratio.
	 * 
	 * @return the lLRatio
	 */
	public boolean isLLRatio() {
		return LLRatio;
	}

	/**
	 * Sets the lL ratio.
	 * 
	 * @param lLRatio the lLRatio to set
	 */
	public void setLLRatio(boolean lLRatio) {
		LLRatio = lLRatio;
	}

	/**
	 * Sets the label.
	 * 
	 * @param value the new label
	 */
	public void setLabel(String value) {
		for (LabelType num : LabelType.values()) {
			if (value.equals(LabelTypeString[num.ordinal()])) {
				labelFormat = num.ordinal();
			}
		}
	}

	/**
	 * Gets the label.
	 * 
	 * @return the label
	 */
	public int getLabel() {
		return labelFormat;
	}

	/**
	 * Checks if is gender.
	 * 
	 * @return true, if is gender
	 */
	public boolean isGender() {
		return gender;
	}

	/**
	 * Sets the gender.
	 * 
	 * @param scoreGender the new gender
	 */
	public void setGender(boolean scoreGender) {
		this.gender = scoreGender;
	}

	/**
	 * Checks if is t norm.
	 * 
	 * @return true, if is t norm
	 */
	public boolean isTNorm() {
		return TNorm;
	}

	/**
	 * Sets the t norm.
	 * 
	 * @param scoreTNorm the new t norm
	 */
	public void setTNorm(boolean scoreTNorm) {
		this.TNorm = scoreTNorm;
	}

	/**
	 * Checks if is by cluster.
	 * 
	 * @return true, if is by cluster
	 */
	public boolean isByCluster() {
		return byCluster;
	}

	/**
	 * Sets the by cluster.
	 * 
	 * @param scoreByCluster the new by cluster
	 */
	public void setByCluster(boolean scoreByCluster) {
		this.byCluster = scoreByCluster;
	}

	/**
	 * Checks if is by segment.
	 * 
	 * @return true, if is by segment
	 */
	public boolean isBySegment() {
		return bySegment;
	}

	/**
	 * Gets the score threshold.
	 * 
	 * @return the scoreThreshold
	 */
	public double getScoreThreshold() {
		return scoreThreshold;
	}

	/**
	 * Sets the score threshold.
	 * 
	 * @param scoreThreshold the scoreThreshold to set
	 */
	public void setScoreThreshold(double scoreThreshold) {
		this.scoreThreshold = scoreThreshold;
	}

	/**
	 * Sets the by segment.
	 * 
	 * @param scoreBySegment the new by segment
	 */
	public void setBySegment(boolean scoreBySegment) {
		this.bySegment = scoreBySegment;
	}

	/**
	 * Gets the mean target.
	 * 
	 * @return the meanTarget
	 */
	public double getMeanTarget() {
		return meanTarget;
	}

	/**
	 * Gets the std target.
	 * 
	 * @return the stdTarget
	 */
	public double getStdTarget() {
		return stdTarget;
	}

	/**
	 * Gets the mean non target.
	 * 
	 * @return the meanNonTarget
	 */
	public double getMeanNonTarget() {
		return meanNonTarget;
	}

	/**
	 * Gets the std non target.
	 * 
	 * @return the stdNonTarget
	 */
	public double getStdNonTarget() {
		return stdNonTarget;
	}

	/**
	 * Gets the probability a priori target.
	 * 
	 * @return the probabilityAPrioriTarget
	 */
	public double getProbabilityAPrioriTarget() {
		return probabilityAPrioriTarget;
	}

	/**
	 * Sets the map norm.
	 * 
	 * @param mapNorm the mapNorm to set
	 */
	public void setMapNorm(String mapNorm) {
		this.mapNorm = mapNorm;
		String list[] = mapNorm.split("[:,]");
		meanTarget = Double.parseDouble(list[0]);
		stdTarget = Double.parseDouble(list[1]);
		meanNonTarget = Double.parseDouble(list[2]);
		stdNonTarget = Double.parseDouble(list[3]);
		stdNonTarget = Double.parseDouble(list[3]);
		probabilityAPrioriTarget = Double.parseDouble(list[4]);
	}

	/**
	 * Checks if is map norm.
	 * 
	 * @return true, if is map norm
	 */
	public boolean isMapNorm() {
		return !((meanTarget == 0.0) && (stdTarget == 1.0) && (meanNonTarget == 0.0) && (stdNonTarget == 1.0));
	}

	/**
	 * Gets the model list.
	 * 
	 * @return the modelList
	 */
	public String getModelList() {
		return modelList;
	}

	/**
	 * Sets the model list.
	 * 
	 * @param modelList the modelList to set
	 */
	public void setModelList(String modelList) {
		this.modelList = modelList;
	}
}