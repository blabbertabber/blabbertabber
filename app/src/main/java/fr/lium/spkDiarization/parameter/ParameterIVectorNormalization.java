package fr.lium.spkDiarization.parameter;

/**
 * The Class ParameterIVectorNormalization.
 */
public class ParameterIVectorNormalization extends ParameterBase implements Cloneable {

	/** The number of interation. */
	protected Integer numberOfInteration;

	/**
	 * The Class ActionNumberOfInteration.
	 */
	private class ActionNumberOfInteration extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {

			return numberOfInteration.toString();
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setNumberOfInteration(Integer.parseInt(optarg));
		}
	}

	/** The eigen factor radial mask. */
	protected String eigenFactorRadialMask;

	/**
	 * The Class ActionMaskInterSessionCompensation.
	 */
	protected class ActionMaskInterSessionCompensation extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setEigenFactorRadialMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return eigenFactorRadialMask;
		}
	}

	/** The WCCN mask. */
	protected String WCCNMask;

	/**
	 * The Class ActionWCCN.
	 */
	protected class ActionWCCN extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setWCCNMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return WCCNMask;
		}
	}

	/** The mahanalobis covariance mask. */
	protected String mahanalobisCovarianceMask;

	/** The key mahanalobis covariance mask. */
	protected static String keyMahanalobisCovarianceMask = "nMahanalobisCovarianceMask";

	/**
	 * The Class ActionMahanalobisCovarianceMask.
	 */
	protected class ActionMahanalobisCovarianceMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMahanalobisCovarianceMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return mahanalobisCovarianceMask;
		}
	}

	/**
	 * Instantiates a new parameter i vector normalization.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterIVectorNormalization(Parameter parameter) {
		super(parameter);
		numberOfInteration = 0;
		eigenFactorRadialMask = "%s.isc.xml";
		WCCNMask = "wccn.cov.xml";
		mahanalobisCovarianceMask = "mahanalobis.cov.mat";
		addOption(new LongOptWithAction("nEFRNbIt", new ActionNumberOfInteration(), "number of iteration in Eigen Factor Radial normalization"));
		addOption(new LongOptWithAction("nEFRMask", new ActionMaskInterSessionCompensation(), "mask for Eigen Factor Radial normalizations xml file name"));
		addOption(new LongOptWithAction("nWCCNMask", new ActionWCCN(), "mask for WCCN matrix xml file name"));
		addOption(new LongOptWithAction(keyMahanalobisCovarianceMask, new ActionMahanalobisCovarianceMask(), "mask for mahanalobis matrix xml file name"));
	}

	/**
	 * Gets the eigen factor radial mask.
	 * 
	 * @return the eigen factor radial mask
	 */
	public String getEigenFactorRadialMask() {
		return eigenFactorRadialMask;
	}

	/**
	 * Sets the eigen factor radial mask.
	 * 
	 * @param outputMask the new eigen factor radial mask
	 */
	public void setEigenFactorRadialMask(String outputMask) {
		this.eigenFactorRadialMask = outputMask;
	}

	/**
	 * Gets the number of interation.
	 * 
	 * @return the numberOfInteration
	 */
	public int getNumberOfInteration() {
		return numberOfInteration;
	}

	/**
	 * Sets the number of interation.
	 * 
	 * @param numberOfInteration the numberOfInteration to set
	 */
	public void setNumberOfInteration(int numberOfInteration) {
		this.numberOfInteration = numberOfInteration;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterIVectorNormalization clone() throws CloneNotSupportedException {
		return (ParameterIVectorNormalization) super.clone();
	}

	/**
	 * Gets the wCCN mask.
	 * 
	 * @return the maskCovariance
	 */
	public String getWCCNMask() {
		return WCCNMask;
	}

	/**
	 * Sets the wCCN mask.
	 * 
	 * @param maskCovariance the maskCovariance to set
	 */
	public void setWCCNMask(String maskCovariance) {
		this.WCCNMask = maskCovariance;
	}

	/**
	 * Gets the mahanalobis covariance mask.
	 * 
	 * @return the mahanalobisCovarianceMask
	 */
	public String getMahanalobisCovarianceMask() {
		return mahanalobisCovarianceMask;
	}

	/**
	 * Sets the mahanalobis covariance mask.
	 * 
	 * @param mahanalobisCovarianceMask the mahanalobisCovarianceMask to set
	 */
	public void setMahanalobisCovarianceMask(String mahanalobisCovarianceMask) {
		this.mahanalobisCovarianceMask = mahanalobisCovarianceMask;
	}

	/**
	 * Sets the number of interation.
	 * 
	 * @param numberOfInteration the numberOfInteration to set
	 */
	public void setNumberOfInteration(Integer numberOfInteration) {
		this.numberOfInteration = numberOfInteration;
	}

	/**
	 * Log mahanalobis covariance mask.
	 */
	public void logMahanalobisCovarianceMask() {
		log(keyMahanalobisCovarianceMask);
	}
}
