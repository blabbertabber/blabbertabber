package fr.lium.spkDiarization.parameter;

/**
 * The Class ParameterModelSetInputFile2.
 */
public class ParameterModelSetInputFile2 extends ParameterModelSet implements Cloneable {

	/**
	 * Instantiates a new parameter model set input file2.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterModelSetInputFile2(Parameter parameter) {
		super(parameter);
		type = "Input";
		addOption(new LongOptWithAction("t" + type + "Mask2", new ActionMask(), ""));
		addOption(new LongOptWithAction("t" + type + "ModelType2", new ActionFormat(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.parameter.ParameterModelSet#clone()
	 */
	@Override
	protected ParameterModelSetInputFile2 clone() throws CloneNotSupportedException {
		return (ParameterModelSetInputFile2) super.clone();
	}

}
