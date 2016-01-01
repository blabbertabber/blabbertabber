package fr.lium.spkDiarization.parameter;

/**
 * The Class ParameterTotalVariability.
 */
public class ParameterTotalVariability extends ParameterBase implements Cloneable {

	/** The train total variability matrix. */
	Boolean trainTotalVariabilityMatrix;

	/**
	 * The Class ActionTrainTotalVariabilityMatrix.
	 */
	private class ActionTrainTotalVariabilityMatrix extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return trainTotalVariabilityMatrix.toString();
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setTrainTotalVariabilityMatrix(Boolean.parseBoolean(optarg));
		}
	}

	/** The total variability matrix size. */
	Integer totalVariabilityMatrixSize;

	/**
	 * The Class ActionTotalVariabilityMatrixSize.
	 */
	private class ActionTotalVariabilityMatrixSize extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return totalVariabilityMatrixSize.toString();
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setTotalVariabilityMatrixSize(Integer.parseInt(optarg));
		}
	}

	/** The total variability matrix mask. */
	String totalVariabilityMatrixMask;

	/**
	 * The Class ActionTotalVariabilityMatrixMask.
	 */
	protected class ActionTotalVariabilityMatrixMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setTotalVariabilityMatrixMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return totalVariabilityMatrixMask;
		}
	}

	/** The partial total variability matrix mask. */
	String partialTotalVariabilityMatrixMask;

	/**
	 * The Class ActionPartialTotalVariabilityMatrixMask.
	 */
	protected class ActionPartialTotalVariabilityMatrixMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setPartialTotalVariabilityMatrixMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return partialTotalVariabilityMatrixMask;
		}
	}

	/** The initial TV matrix mask. */
	String initialTotalVariabilityMatrixMask;

	/**
	 * The Class ActionIntialTotalVariabilityMatrixMask.
	 */
	protected class ActionIntialTotalVariabilityMatrixMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setInitialTotalVariabilityMatrixMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return initialTotalVariabilityMatrixMask;
		}
	}

	/** The zero order statistic mask. */
	String zeroOrderStatisticMask;

	/**
	 * The Class ActionZeroOrderStatisticMask.
	 */
	protected class ActionZeroOrderStatisticMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setZeroOrderStatisticMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return zeroOrderStatisticMask;
		}
	}

	/** The first order statistic mask. */
	String firstOrderStatisticMask;

	/**
	 * The Class ActionFirstOrderStatisticMask.
	 */
	protected class ActionFirstOrderStatisticMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setFirstOrderStatisticMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return firstOrderStatisticMask;
		}
	}

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

	/**
	 * Instantiates a new parameter total variability.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterTotalVariability(Parameter parameter) {
		super(parameter);
		partialTotalVariabilityMatrixMask = "";
		totalVariabilityMatrixMask = "totalVariability.mat";
		initialTotalVariabilityMatrixMask = "";
		zeroOrderStatisticMask = "";
		firstOrderStatisticMask = "";
		numberOfInteration = 0;
		trainTotalVariabilityMatrix = false;
		totalVariabilityMatrixSize = 0;
		addOption(new LongOptWithAction("tvNbIt", new ActionNumberOfInteration(), "number of iteration during the training"));
		addOption(new LongOptWithAction("tvTrainTotalVariabilityMatrix", new ActionTrainTotalVariabilityMatrix(), "train total variability matrix before i-vector training"));
		addOption(new LongOptWithAction("tvSize", new ActionTotalVariabilityMatrixSize(), "the size of an i-vector"));
		addOption(new LongOptWithAction("tvPartialTotalVariabilityMatrixMask", new ActionPartialTotalVariabilityMatrixMask(), "the mask of partial Total Variability Matrix (inital, + one per iteration)"));
		addOption(new LongOptWithAction("tvTotalVariabilityMatrixMask", new ActionTotalVariabilityMatrixMask(), "the mask of partial Total Variability Matrix"));
		addOption(new LongOptWithAction("tvIntialTotalVariabilityMatrixMask", new ActionIntialTotalVariabilityMatrixMask(), ""));
		addOption(new LongOptWithAction("tvZeroOrderStatisticMask", new ActionZeroOrderStatisticMask(), ""));
		addOption(new LongOptWithAction("tvFirstOrderStatisticMask", new ActionFirstOrderStatisticMask(), ""));
	}

	/**
	 * Gets the number of interation.
	 * 
	 * @return the numberOfInteration
	 */
	public Integer getNumberOfInteration() {
		return numberOfInteration;
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
	 * Gets the total variability matrix size.
	 * 
	 * @return the iVectorSize
	 */
	public int getTotalVariabilityMatrixSize() {
		return totalVariabilityMatrixSize;
	}

	/**
	 * Sets the total variability matrix size.
	 * 
	 * @param iVectorSize the iVectorSize to set
	 */
	public void setTotalVariabilityMatrixSize(int iVectorSize) {
		this.totalVariabilityMatrixSize = iVectorSize;
	}

	/**
	 * Gets the total variability matrix mask.
	 * 
	 * @return the totalVariabilityMatrixMask
	 */
	public String getTotalVariabilityMatrixMask() {
		return totalVariabilityMatrixMask;
	}

	/**
	 * Sets the total variability matrix mask.
	 * 
	 * @param totalVariabilityMatrixMask the totalVariabilityMatrixMask to set
	 */
	public void setTotalVariabilityMatrixMask(String totalVariabilityMatrixMask) {
		this.totalVariabilityMatrixMask = totalVariabilityMatrixMask;
	}

	/**
	 * Gets the partial total variability matrix mask.
	 * 
	 * @return the partialTotalVariabilityMatrixMask
	 */
	public String getPartialTotalVariabilityMatrixMask() {
		return partialTotalVariabilityMatrixMask;
	}

	/**
	 * Sets the partial total variability matrix mask.
	 * 
	 * @param partialTotalVariabilityMatrixMask the partialTotalVariabilityMatrixMask to set
	 */
	public void setPartialTotalVariabilityMatrixMask(String partialTotalVariabilityMatrixMask) {
		this.partialTotalVariabilityMatrixMask = partialTotalVariabilityMatrixMask;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterTotalVariability clone() throws CloneNotSupportedException {
		return (ParameterTotalVariability) super.clone();
	}

	/**
	 * Gets the zero order statistic mask.
	 * 
	 * @return the zeroOrderStatisticMask
	 */
	public String getZeroOrderStatisticMask() {
		return zeroOrderStatisticMask;
	}

	/**
	 * Sets the zero order statistic mask.
	 * 
	 * @param zeroOrderStatisticMask the zeroOrderStatisticMask to set
	 */
	public void setZeroOrderStatisticMask(String zeroOrderStatisticMask) {
		this.zeroOrderStatisticMask = zeroOrderStatisticMask;
	}

	/**
	 * Gets the first order statistic mask.
	 * 
	 * @return the firstOrderStatisticMask
	 */
	public String getFirstOrderStatisticMask() {
		return firstOrderStatisticMask;
	}

	/**
	 * Sets the first order statistic mask.
	 * 
	 * @param firstOrderStatisticMask the firstOrderStatisticMask to set
	 */
	public void setFirstOrderStatisticMask(String firstOrderStatisticMask) {
		this.firstOrderStatisticMask = firstOrderStatisticMask;
	}

	/**
	 * Sets the total variability matrix size.
	 * 
	 * @param totalVariabilityMatrixSize the totalVariabilityMatrixSize to set
	 */
	public void setTotalVariabilityMatrixSize(Integer totalVariabilityMatrixSize) {
		this.totalVariabilityMatrixSize = totalVariabilityMatrixSize;
	}

	/**
	 * Gets the train total variability matrix.
	 * 
	 * @return the trainTotalVariabilityMatrix
	 */
	public Boolean getTrainTotalVariabilityMatrix() {
		return trainTotalVariabilityMatrix;
	}

	/**
	 * Sets the train total variability matrix.
	 * 
	 * @param trainTotalVariabilityMatrix the trainTotalVariabilityMatrix to set
	 */
	public void setTrainTotalVariabilityMatrix(Boolean trainTotalVariabilityMatrix) {
		this.trainTotalVariabilityMatrix = trainTotalVariabilityMatrix;
	}

	/**
	 * @return the initialTotalVariabilityMatrixMask
	 */
	public String getInitialTotalVariabilityMatrixMask() {
		return initialTotalVariabilityMatrixMask;
	}

	/**
	 * @param initialTotalVariabilityMatrixMask the initialTotalVariabilityMatrixMask to set
	 */
	public void setInitialTotalVariabilityMatrixMask(String initialTotalVariabilityMatrixMask) {
		this.initialTotalVariabilityMatrixMask = initialTotalVariabilityMatrixMask;
	}

}
