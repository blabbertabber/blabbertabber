package fr.lium.spkDiarization.parameter;

import java.util.logging.Logger;

/**
 * The Class ParameterVideoInputFeature.
 */
public class ParameterVideoInputFeature extends ParameterBase implements Cloneable {

	/**
	 * The Enum VideoFeatureFormat.
	 */
	public enum VideoFeatureFormat {

		/** The image. */
		IMAGE,
		/** The video. */
		VIDEO
	};

	/** The Video feature format string. */
	public static String[] VideoFeatureFormatString = { "image", "video" };

	/** The video format. */
	private VideoFeatureFormat videoFormat;

	/**
	 * The Class ActionVideoFeatureFormat.
	 */
	private class ActionVideoFeatureFormat extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setVideoFormat(arg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String option = longOpt.getName();
			String comment = longOpt.getComment();
			logger.config("--" + option + " \t " + comment + " " + formatStrigArray(VideoFeatureFormatString) + " = "
					+ VideoFeatureFormatString[videoFormat.ordinal()] + " [" + logger.getName() + "]");
		}

	}

	// ----
	/** The video mask. */
	private String videoMask;

	/**
	 * The Class ActionVideoMask.
	 */
	private class ActionVideoMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setVideoMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return videoMask;
		}
	}

	// ----

	/**
	 * Instantiates a new parameter video input feature.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterVideoInputFeature(Parameter parameter) {
		super(parameter);
		setVideoMask("%s.avi");
		addOption(new LongOptWithAction("videoMask", new ActionVideoMask(), "video mask"));

		setVideoFormat(VideoFeatureFormat.VIDEO);
		String comment = "video format " + ArrayToSting(VideoFeatureFormatString);
		addOption(new LongOptWithAction("videoFormat", new ActionVideoFeatureFormat(), comment));

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterVideoInputFeature clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (ParameterVideoInputFeature) super.clone();
	}

	/**
	 * Gets the video format.
	 * 
	 * @return the format
	 */
	public VideoFeatureFormat getVideoFormat() {
		return videoFormat;
	}

	/**
	 * Gets the video mask.
	 * 
	 * @return the mask
	 */
	public String getVideoMask() {
		return videoMask;
	}

	/**
	 * Sets the video format.
	 * 
	 * @param optarg the new video format
	 */
	private void setVideoFormat(String optarg) {
		if (optarg.equals(VideoFeatureFormatString[VideoFeatureFormat.IMAGE.ordinal()])) {
			setVideoFormat(VideoFeatureFormat.IMAGE);
		} else if (optarg.equals(VideoFeatureFormatString[VideoFeatureFormat.VIDEO.ordinal()])) {
			setVideoFormat(VideoFeatureFormat.VIDEO);
		}
	}

	/**
	 * Sets the video format.
	 * 
	 * @param videoFormat the videoFormat to set
	 */
	protected void setVideoFormat(VideoFeatureFormat videoFormat) {
		this.videoFormat = videoFormat;
	}

	/**
	 * Sets the video mask.
	 * 
	 * @param optarg the new video mask
	 */
	private void setVideoMask(String optarg) {
		videoMask = optarg;
	}

}
