package fr.lium.spkDiarization.parameter;

/**
 * The Class ParameterSegmentationInputFile2.
 */
public class ParameterSegmentationInputFile2 extends ParameterSegmentationFile implements Cloneable {

	/**
	 * Instantiates a new parameter segmentation input file2.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterSegmentationInputFile2(Parameter parameter) {
		super(parameter);
		type = "Input2";
		addOption(new LongOptWithAction("s" + type + "Mask", new ActionMask(), ""));
		addOption(new LongOptWithAction("s" + type + "Format", new ActionFormatEncoding(), ""));
		addOption(new LongOptWithAction("s" + type + "Rate", new ActionRate(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.parameter.ParameterSegmentationFile#clone()
	 */
	@Override
	protected ParameterSegmentationInputFile2 clone() throws CloneNotSupportedException {
		return (ParameterSegmentationInputFile2) super.clone();
	}

}
