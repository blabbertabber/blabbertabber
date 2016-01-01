package fr.lium.spkDiarization.parameter;

import java.util.logging.Logger;

/**
 * The Class ParameterEHMM.
 */
public class ParameterEHMM extends ParameterBase implements Cloneable {
	// Type of EHMM.
	/**
	 * The Enum TypeEHMMList.
	 */
	public static enum TypeEHMMList {

		/** The Re seg. */
		ReSeg,
		/** The two spk. */
		twoSpk,
		/** The n spk. */
		nSpk
	};

	/** The Type ehmm string. */
	public static String[] TypeEHMMString = { "reSeg", "2Spk", "nSpk" };

	/** The type ehmm. */
	private int typeEHMM; // Minimum of iteration of EM algorithm.

	/**
	 * The Class ActionTypeEHMM.
	 */
	private class ActionTypeEHMM extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setTypeEHMM(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("info[--" + longOpt.getName() + " \t number of speakers " + formatStrigArray(TypeEHMMString)
					+ " = " + TypeEHMMString[getTypeEHMM()] + " / " + getTypeEHMM() + " [" + logger.getName() + "]");
		}
	}

	/**
	 * Instantiates a new parameter ehmm.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterEHMM(Parameter parameter) {
		super(parameter);
		setTypeEHMM(TypeEHMMString[TypeEHMMList.ReSeg.ordinal()]);
		addOption(new LongOptWithAction("typeEHMM", new ActionTypeEHMM(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterEHMM clone() throws CloneNotSupportedException {
		return (ParameterEHMM) super.clone();
	}

	/**
	 * Sets the type ehmm.
	 * 
	 * @param optarg the new type ehmm
	 */
	private void setTypeEHMM(String optarg) {
		if (optarg.equals(TypeEHMMString[TypeEHMMList.ReSeg.ordinal()])) {
			typeEHMM = TypeEHMMList.ReSeg.ordinal();
		} else if (optarg.equals(TypeEHMMString[TypeEHMMList.twoSpk.ordinal()])) {
			typeEHMM = TypeEHMMList.twoSpk.ordinal();
		} else if (optarg.equals(TypeEHMMString[TypeEHMMList.nSpk.ordinal()])) {
			typeEHMM = TypeEHMMList.nSpk.ordinal();
		}
	}

	/**
	 * Sets the type ehmm.
	 * 
	 * @param typeEHMM the typeEHMM to set
	 */
	public void setTypeEHMM(int typeEHMM) {
		this.typeEHMM = typeEHMM;
	}

	/**
	 * Gets the type ehmm.
	 * 
	 * @return the typeEHMM
	 */
	public int getTypeEHMM() {
		return typeEHMM;
	}

}
