package fr.lium.spkDiarization.parameter;

/**
 * The Class ParameterILP.
 */
public class ParameterILP extends ParameterBase implements Cloneable {

	/** The threshold ilp. */
	Double thresholdILP = 100.0;

	/**
	 * The Class ActionThresholdILP.
	 */
	private class ActionThresholdILP extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setThresholdILP(Double.valueOf(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return thresholdILP.toString();
		}
	}

	/** The output problem mask. */
	String outputProblemMask;

	/**
	 * The Class ActionProblemMask.
	 */
	private class ActionProblemMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setProblemMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return outputProblemMask;
		}
	}

	/** The output solution mask. */
	String outputSolutionMask;

	/**
	 * The Class ActionSolutionMask.
	 */
	private class ActionSolutionMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setSolutionMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return outputSolutionMask;
		}
	}

	/** The glpsol program. */
	String glpsolProgram;

	/**
	 * The Class ActionGlpsolProgram.
	 */
	private class ActionGlpsolProgram extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setGlpsolProgram(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return glpsolProgram;
		}
	}

	/**
	 * Instantiates a new parameter ilp.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterILP(Parameter parameter) {
		super(parameter);
		setThresholdILP(100);
		setProblemMask("%s.ilp.problem.txt");
		setSolutionMask("%s.ilp.solution.txt");
		setGlpsolProgram("/usr/bin/glpsol");
		addOption(new LongOptWithAction("ilpOutputProblemMask", new ActionProblemMask(), "output text file of the ILP problem"));
		addOption(new LongOptWithAction("ilpOutputSolutionMask", new ActionSolutionMask(), "output text file of the ILP solution"));
		addOption(new LongOptWithAction("ilpGLPSolProgram", new ActionGlpsolProgram(), "path of the glpsol program"));
		addOption(new LongOptWithAction("ilpThr", new ActionThresholdILP(), "threshold on the distance"));
	}

	/**
	 * Gets the threshold ilp.
	 * 
	 * @return the thresholdILP
	 */
	public double getThresholdILP() {
		return thresholdILP;
	}

	/**
	 * Sets the threshold ilp.
	 * 
	 * @param thresholdILP the thresholdILP to set
	 */
	public void setThresholdILP(double thresholdILP) {
		this.thresholdILP = thresholdILP;
	}

	/**
	 * Gets the problem mask.
	 * 
	 * @return the mask
	 */
	public String getProblemMask() {
		return outputProblemMask;
	}

	/**
	 * Sets the problem mask.
	 * 
	 * @param mask the mask to set
	 */
	public void setProblemMask(String mask) {
		this.outputProblemMask = mask;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterILP clone() throws CloneNotSupportedException {
		return (ParameterILP) super.clone();
	}

	/**
	 * Gets the solution mask.
	 * 
	 * @return the outputSolutionMask
	 */
	public String getSolutionMask() {
		return outputSolutionMask;
	}

	/**
	 * Sets the solution mask.
	 * 
	 * @param outputSolutionMask the outputSolutionMask to set
	 */
	public void setSolutionMask(String outputSolutionMask) {
		this.outputSolutionMask = outputSolutionMask;
	}

	/**
	 * Gets the glpsol program.
	 * 
	 * @return the glpsolProgram
	 */
	public String getGlpsolProgram() {
		return glpsolProgram;
	}

	/**
	 * Sets the glpsol program.
	 * 
	 * @param glpsolProgram the glpsolProgram to set
	 */
	public void setGlpsolProgram(String glpsolProgram) {
		this.glpsolProgram = glpsolProgram;
	}

}
