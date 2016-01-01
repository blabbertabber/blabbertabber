package fr.lium.spkDiarization.parameter;

/**
 * The Class ParameterSegmentationInputFile3.
 */
public class ParameterSegmentationInputFile3 extends ParameterSegmentationFile implements Cloneable {

	/**
	 * Instantiates a new parameter segmentation input file3.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterSegmentationInputFile3(Parameter parameter) {
		super(parameter);
		type = "Input3";
		addOption(new LongOptWithAction("s" + type + "Mask", new ActionMask(), ""));
		addOption(new LongOptWithAction("s" + type + "Format", new ActionFormatEncoding(), ""));
		addOption(new LongOptWithAction("s" + type + "Rate", new ActionRate(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.parameter.ParameterSegmentationFile#clone()
	 */
	@Override
	protected ParameterSegmentationInputFile3 clone() throws CloneNotSupportedException {
		return (ParameterSegmentationInputFile3) super.clone();
	}

}
