/**
 * 
 * <p>
 * ParameterDecoder
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
 */

package fr.lium.spkDiarization.parameter;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * The Class ParameterDecoder.
 */
public class ParameterDecoder extends ParameterBase implements Cloneable {

	/** The Constant ViterbiDurationConstraintString. */
	public final static String[] ViterbiDurationConstraintString = { "none", "minimal", "periodic", "fixed", "jump" };

	// Type of duration constraint for Viterbi.
	/**
	 * The Enum ViterbiDurationConstraint.
	 */
	static public enum ViterbiDurationConstraint {

		/** The viterbi no constraint. */
		VITERBI_NO_CONSTRAINT,
		/** The viterbi minimal duration. */
		VITERBI_MINIMAL_DURATION,
		/** The viterbi periodic duration. */
		VITERBI_PERIODIC_DURATION,
		/** The viterbi fixed duration. */
		VITERBI_FIXED_DURATION,
		/** The viterbi jump duration. */
		VITERBI_JUMP_DURATION
	};

	/** The decoder penality. */
	private String decoderPenality = "";

	/** The exit decoder penalty. */
	private ArrayList<Double> exitDecoderPenalty;

	/** The loop decoder penalty. */
	private ArrayList<Double> loopDecoderPenalty;

	/** The viterbi duration constraints. */
	private ArrayList<ViterbiDurationConstraint> viterbiDurationConstraints;

	/**
	 * The Class ActionDecoderPenality.
	 */
	private class ActionDecoderPenality extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setDecoderPenalty(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {

			String message = "--" + longOpt.getName() + " \t model penalties = ";

			for (int i = 0; i < getExitDecoderPenalty().size(); i++) {
				message += getExitDecoderPenalty().get(i) + ":";
				message += getLoopDecoderPenalty().get(i) + ", ";
			}
			logger.config(message + " [" + logger.getName() + "]");

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

	/** The viterbi duration constraint values string. */
	private String viterbiDurationConstraintValuesString = "";

	/** The viterbi duration constraint values. */
	private ArrayList<Integer> viterbiDurationConstraintValues;

	/**
	 * The Class ActionViterbiDurationConstraintValues.
	 */
	private class ActionViterbiDurationConstraintValues extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setViterbiDurationConstraints(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String message = "--" + longOpt.getName() + " \t duration constraints during decoding";
			message += formatStrigArray(ViterbiDurationConstraintString) + " = ";
			for (int i = 0; i < (getViterbiDurationConstraints().size() - 1); i++) {
				message += ViterbiDurationConstraintString[getViterbiDurationConstraints().get(i).ordinal()] + ",";
				if (getViterbiDurationConstraints().get(i) != ViterbiDurationConstraint.VITERBI_NO_CONSTRAINT) {
					message += getViterbiDurationConstraintValues().get(i) + ",";
				}
			}
			message += ViterbiDurationConstraintString[getViterbiDurationConstraints().get(getViterbiDurationConstraints().size() - 1).ordinal()];
			if (getViterbiDurationConstraints().get(getViterbiDurationConstraints().size() - 1) != ViterbiDurationConstraint.VITERBI_NO_CONSTRAINT) {
				message += "," + getViterbiDurationConstraintValues().get(getViterbiDurationConstraints().size() - 1);
			}
			logger.config(message + " [" + logger.getName() + "]");

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

	/** The compute l lh r. */
	private Boolean computeLLhR;

	/**
	 * The Class ActionComputeLLhR.
	 */
	private class ActionComputeLLhR extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setComputeLLhR(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return computeLLhR.toString();
		}
	}

	/** The shift. */
	private Integer shift;

	/**
	 * The Class ActionShift.
	 */
	private class ActionShift extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setShift(Integer.parseInt(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return shift.toString();
		}
	}

	/**
	 * Instantiates a new parameter decoder.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterDecoder(Parameter parameter) {
		super(parameter);
		setComputeLLhR(false);
		shift = 1;
		exitDecoderPenalty = new ArrayList<Double>();
		loopDecoderPenalty = new ArrayList<Double>();
		getExitDecoderPenalty().add(0.0);
		getLoopDecoderPenalty().add(0.0);
		viterbiDurationConstraints = new ArrayList<ViterbiDurationConstraint>();
		viterbiDurationConstraints.add(ViterbiDurationConstraint.VITERBI_NO_CONSTRAINT);
		viterbiDurationConstraintValues = new ArrayList<Integer>();
		getViterbiDurationConstraintValues().add(1);
		addOption(new LongOptWithAction("dPenality", new ActionDecoderPenality(), ""));
		addOption(new LongOptWithAction("dDurationConstraints", new ActionViterbiDurationConstraintValues(), ""));
		addOption(new LongOptWithAction("dComputeLLhR", 0, new ActionComputeLLhR(), "score is Log Likelihood Ratio"));
		addOption(new LongOptWithAction("dShift", new ActionShift(), "number of features for a jump between 2 states"));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterDecoder clone() throws CloneNotSupportedException {
		ParameterDecoder result = (ParameterDecoder) super.clone();
		result.exitDecoderPenalty = new ArrayList<Double>();
		result.exitDecoderPenalty.addAll(exitDecoderPenalty);
		result.loopDecoderPenalty = new ArrayList<Double>();
		result.loopDecoderPenalty.addAll(loopDecoderPenalty);
		result.viterbiDurationConstraints = new ArrayList<ViterbiDurationConstraint>();
		result.viterbiDurationConstraints.addAll(viterbiDurationConstraints);
		result.viterbiDurationConstraintValues = new ArrayList<Integer>();
		result.viterbiDurationConstraintValues.addAll(viterbiDurationConstraintValues);
		result.computeLLhR = computeLLhR;
		result.shift = shift;
		return (ParameterDecoder) super.clone();
	}

	/**
	 * Sets the shift.
	 * 
	 * @param parseInt the new shift
	 */
	public void setShift(int parseInt) {
		shift = parseInt;
	}

	/**
	 * Gets the shift.
	 * 
	 * @return the shift
	 */
	public int getShift() {
		return shift;
	}

	/**
	 * Gets the exit decoder penalty.
	 * 
	 * @return the exit decoder penalty
	 */
	public ArrayList<Double> getExitDecoderPenalty() {
		return exitDecoderPenalty;
	}

	/**
	 * Gets the loop decoder penalty.
	 * 
	 * @return the loop decoder penalty
	 */
	public ArrayList<Double> getLoopDecoderPenalty() {
		return loopDecoderPenalty;
	}

	/**
	 * Sets the decoder penalty.
	 * 
	 * @param ch the new decoder penalty
	 */
	public void setDecoderPenalty(String ch) {
		decoderPenality = ch;
		exitDecoderPenalty.clear();
		loopDecoderPenalty.clear();
		String[] listKey = ch.split(",");
		for (String key : listKey) {
			String[] listKey2 = key.split(":");
			getExitDecoderPenalty().add(Double.parseDouble(listKey2[0]));
			if (listKey2.length > 1) {
				getLoopDecoderPenalty().add(Double.parseDouble(listKey2[1]));
			} else {
				getLoopDecoderPenalty().add(0.0);
			}
		}
	}

	/**
	 * Gets the decoder penalty as string.
	 * 
	 * @return the decoder penalty as string
	 */
	public String getDecoderPenaltyAsString() {
		String ch = "";
		int i = 0;
		for (; i < (getExitDecoderPenalty().size() - 1); i++) {
			ch += getExitDecoderPenalty().get(i) + ":" + getLoopDecoderPenalty().get(i) + ",";
		}
		ch += getExitDecoderPenalty().get(i) + ":" + getLoopDecoderPenalty().get(i);
		return ch;
	}

	/**
	 * Gets the viterbi duration constraints.
	 * 
	 * @return the viterbi duration constraints
	 */
	public ArrayList<ViterbiDurationConstraint> getViterbiDurationConstraints() {
		return viterbiDurationConstraints;
	}

	/**
	 * Sets the viterbi duration constraints.
	 * 
	 * @param ch the new viterbi duration constraints
	 */
	public void setViterbiDurationConstraints(String ch) {
		viterbiDurationConstraintValuesString = ch;
		ArrayList<String> tmpCh = new ArrayList<String>();
		String limite = ",";
		StringTokenizer stok = new StringTokenizer(ch, limite);
		while (stok.hasMoreTokens()) {
			tmpCh.add(stok.nextToken());
		}
		if (tmpCh.size() > 0) {
			viterbiDurationConstraints.clear();
			setViterbiDurationConstraintValues(new ArrayList<Integer>());
			int durValue;
			for (int i = 0; i < tmpCh.size(); i++) {
				if (tmpCh.get(i).equals(ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_MINIMAL_DURATION.ordinal()])) {
					getViterbiDurationConstraints().add(ViterbiDurationConstraint.VITERBI_MINIMAL_DURATION);
					durValue = Integer.parseInt(tmpCh.get(++i));
				} else if (tmpCh.get(i).equals(ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_PERIODIC_DURATION.ordinal()])) {
					getViterbiDurationConstraints().add(ViterbiDurationConstraint.VITERBI_PERIODIC_DURATION);
					durValue = Integer.parseInt(tmpCh.get(++i));
				} else if (tmpCh.get(i).equals(ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_FIXED_DURATION.ordinal()])) {
					getViterbiDurationConstraints().add(ViterbiDurationConstraint.VITERBI_FIXED_DURATION);
					durValue = Integer.parseInt(tmpCh.get(++i));
				} else if (tmpCh.get(i).equals(ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_JUMP_DURATION.ordinal()])) {
					getViterbiDurationConstraints().add(ViterbiDurationConstraint.VITERBI_JUMP_DURATION);
					durValue = 1;
				} else {
					getViterbiDurationConstraints().add(ViterbiDurationConstraint.VITERBI_NO_CONSTRAINT);
					durValue = 1;
				}
				getViterbiDurationConstraintValues().add(durValue);
			}
		}
	}

	/**
	 * Gets the viterbi duration constraint values.
	 * 
	 * @return the viterbi duration constraint values
	 */
	public ArrayList<Integer> getViterbiDurationConstraintValues() {
		return viterbiDurationConstraintValues;
	}

	/**
	 * Sets the viterbi duration constraint values.
	 * 
	 * @param viterbiDurationConstraintValues the new viterbi duration constraint values
	 */
	public void setViterbiDurationConstraintValues(ArrayList<Integer> viterbiDurationConstraintValues) {
		this.viterbiDurationConstraintValues = viterbiDurationConstraintValues;
	}

	/**
	 * Sets the compute l lh r.
	 * 
	 * @param computeLLhR the new compute l lh r
	 */
	public void setComputeLLhR(boolean computeLLhR) {
		this.computeLLhR = computeLLhR;
	}

	/**
	 * Checks if is compute l lh r.
	 * 
	 * @return true, if is compute l lh r
	 */
	public boolean isComputeLLhR() {
		return computeLLhR;
	}
}