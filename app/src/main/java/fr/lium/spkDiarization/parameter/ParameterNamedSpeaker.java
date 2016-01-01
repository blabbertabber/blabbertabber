package fr.lium.spkDiarization.parameter;

/**
 * The Class ParameterNamedSpeaker.
 */
public class ParameterNamedSpeaker extends ParameterBase implements Cloneable {

	/** The threshold decision. */
	private Double thresholdDecision;

	/**
	 * The Class ActionThresholdDecision.
	 */
	private class ActionThresholdDecision extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setThresholdDecision(Double.parseDouble(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return thresholdDecision.toString();
		}
	}

	/** The threshold transcription. */
	private Double thresholdTranscription;

	/**
	 * The Class ActionThresholdTranscription.
	 */
	private class ActionThresholdTranscription extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setThresholdTranscription(Double.parseDouble(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return thresholdTranscription.toString();
		}
	}

	/** The threshold audio. */
	private Double thresholdAudio;

	/**
	 * The Class ActionThresholdAudio.
	 */
	private class ActionThresholdAudio extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setThresholdAudio(Double.parseDouble(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return thresholdAudio.toString();
		}
	}

	/** The threshold video. */
	private Double thresholdVideo;

	/**
	 * The Class ActionThresholdVideo.
	 */
	private class ActionThresholdVideo extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setThresholdVideo(Double.parseDouble(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return thresholdVideo.toString();
		}
	}

	/** The threshold writing. */
	private Double thresholdWriting;

	/**
	 * The Class ActionThresholdWriting.
	 */
	private class ActionThresholdWriting extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setThresholdWriting(Double.parseDouble(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return thresholdWriting.toString();
		}
	}

	/** The name and gender list. */
	private String nameAndGenderList;

	/**
	 * The Class ActionNameAndGenderList.
	 */
	private class ActionNameAndGenderList extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setNameAndGenderList(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return nameAndGenderList;
		}
	}

	/** The dont check gender. */
	private Boolean dontCheckGender;

	/**
	 * The Class ActionDontCheckGender.
	 */
	private class ActionDontCheckGender extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setDontCheckGender(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return dontCheckGender.toString();
		}
	}

	/** The close list check. */
	private Boolean closeListCheck;

	/**
	 * The Class ActionCloseListCheck.
	 */
	private class ActionCloseListCheck extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setCloseListCheck(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return closeListCheck.toString();
		}
	}

	/** The belief functions. */
	private Boolean beliefFunctions;

	/**
	 * The Class ActionBeliefFunctions.
	 */
	private class ActionBeliefFunctions extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setBeliefFunctions(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return beliefFunctions.toString();
		}
	}

	/** The maximum. */
	private Boolean maximum;

	/**
	 * The Class ActionMaximum.
	 */
	private class ActionMaximum extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMaximum(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return maximum.toString();
		}
	}

	/** The hungarian. */
	private Boolean hungarian;

	/**
	 * The Class ActionHungarian.
	 */
	private class ActionHungarian extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setHungarian(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return hungarian.toString();
		}
	}

	/** The first name check. */
	private Boolean firstNameCheck;

	/**
	 * The Class ActionFirstNameCheck.
	 */
	private class ActionFirstNameCheck extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setFirstNameCheck(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return firstNameCheck.toString();
		}
	}

	/** The first name list. */
	private String firstNameList;

	/**
	 * The Class ActionFirstNameList.
	 */
	private class ActionFirstNameList extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setFirstNameList(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return firstNameList;
		}
	}

	/** The use audio. */
	private Boolean useAudio;

	/**
	 * The Class ActionUseAudio.
	 */
	private class ActionUseAudio extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setUseAudio(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return useAudio.toString();
		}
	}

	/** The use video. */
	private Boolean useVideo;

	/**
	 * The Class ActionUseVideo.
	 */
	private class ActionUseVideo extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setUseVideo(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return useVideo.toString();
		}
	}

	/** The use transcription. */
	private Boolean useTranscription;

	/**
	 * The Class ActionUseTranscription.
	 */
	private class ActionUseTranscription extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setUseTranscription(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return useTranscription.toString();
		}
	}

	/** The use writing. */
	private Boolean useWriting;

	/**
	 * The Class ActionUseWritting.
	 */
	private class ActionUseWritting extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setUseWriting(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return useWriting.toString();
		}
	}

	/** The SCT mask. */
	private String SCTMask;

	/**
	 * The Class ActionUseSCTMask.
	 */
	private class ActionUseSCTMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setSCTMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return SCTMask;
		}
	}

	/** The training. */
	private Boolean training;

	/**
	 * The Class ActionTraining.
	 */
	private class ActionTraining extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setTraining(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return training.toString();
		}
	}

	/**
	 * Instantiates a new parameter named speaker.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterNamedSpeaker(Parameter parameter) {
		super(parameter);
		setThresholdDecision(0.0);
		setThresholdTranscription(0.05);
		setThresholdVideo(0.0);
		setThresholdAudio(0.0);
		setThresholdWriting(0.0);

		setUseAudio(false);
		setUseVideo(false);
		setUseWriting(false);
		setUseTranscription(false);

		setNameAndGenderList("%s.lst");
		setSCTMask("%s.tree");
		dontCheckGender = false;
		closeListCheck = false;
		firstNameCheck = false;

		beliefFunctions = false;
		maximum = false;
		hungarian = false;

		training = false;

		addOption(new LongOptWithAction("nThresholdDecision", new ActionThresholdDecision(), "decision score threshold"));
		addOption(new LongOptWithAction("nThresholdTranscription", new ActionThresholdTranscription(), "transcription score threshold"));
		addOption(new LongOptWithAction("nThresholdAudio", new ActionThresholdAudio(), "audio score threshold"));
		addOption(new LongOptWithAction("nThresholdVideo", new ActionThresholdVideo(), "video score threshold"));
		addOption(new LongOptWithAction("nThresholdWriting", new ActionThresholdWriting(), "writing score threshold"));

		addOption(new LongOptWithAction("nUseTranscription", 0, new ActionUseTranscription(), "use speaker identification base on audio transcription"));
		addOption(new LongOptWithAction("nUseAudio", 0, new ActionUseAudio(), "use speaker identification base on audio model"));
		addOption(new LongOptWithAction("nUseVideo", 0, new ActionUseVideo(), "use speaker identification base on video model"));
		addOption(new LongOptWithAction("nUseWriting", 0, new ActionUseWritting(), "use speaker identification base on writing"));

		addOption(new LongOptWithAction("nNameAndGenderList", new ActionNameAndGenderList(), "list of name (full name or firstname) with gender information"));
		addOption(new LongOptWithAction("nDontCheckGender", 0, new ActionDontCheckGender(), "remove the gender check"));
		addOption(new LongOptWithAction("nCloseListCheck", 0, new ActionCloseListCheck(), "check the present of the speaker in --nNameAndGenderList list"));

		addOption(new LongOptWithAction("nBeliefFunctions", 0, new ActionBeliefFunctions(), ""));
		addOption(new LongOptWithAction("nMaximum", 0, new ActionMaximum(), ""));
		addOption(new LongOptWithAction("nHungarian", 0, new ActionHungarian(), ""));

		addOption(new LongOptWithAction("nFirstNameCheck", 0, new ActionFirstNameCheck(), "check the gender with a first name list"));
		addOption(new LongOptWithAction("nFirstNameList", new ActionFirstNameList(), "list of first name with gender information"));

		addOption(new LongOptWithAction("nIsTraining", 0, new ActionTraining(), "this a training corpus"));
		addOption(new LongOptWithAction("nLIA_SCTMask", new ActionUseSCTMask(), "Semantic Classification Tree Mask"));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterNamedSpeaker clone() throws CloneNotSupportedException {
		return (ParameterNamedSpeaker) super.clone();
	}

	/**
	 * Gets the threshold transcription.
	 * 
	 * @return the thresholdTranscription
	 */
	public double getThresholdTranscription() {
		return thresholdTranscription;
	}

	/**
	 * Sets the threshold transcription.
	 * 
	 * @param thresholdTranscription the thresholdTranscription to set
	 */
	public void setThresholdTranscription(double thresholdTranscription) {
		this.thresholdTranscription = thresholdTranscription;
	}

	/**
	 * Sets the threshold decision.
	 * 
	 * @param namedNEThr the new threshold decision
	 */
	public void setThresholdDecision(double namedNEThr) {
		thresholdDecision = namedNEThr;
	}

	/**
	 * Gets the name and gender list.
	 * 
	 * @return the name and gender list
	 */
	public String getNameAndGenderList() {
		return nameAndGenderList;
	}

	/**
	 * Sets the name and gender list.
	 * 
	 * @param ch the new name and gender list
	 */
	public void setNameAndGenderList(String ch) {
		this.nameAndGenderList = ch;
	}

	/**
	 * Gets the sCT mask.
	 * 
	 * @return the sCT mask
	 */
	public String getSCTMask() {
		return SCTMask;
	}

	/**
	 * Sets the sCT mask.
	 * 
	 * @param mask the new sCT mask
	 */
	public void setSCTMask(String mask) {
		SCTMask = mask;
	}

	/**
	 * Checks if is belief functions.
	 * 
	 * @return true, if is belief functions
	 */
	public boolean isBeliefFunctions() {
		return beliefFunctions;
	}

	/**
	 * Sets the belief functions.
	 * 
	 * @param beliefFunctions the new belief functions
	 */
	public void setBeliefFunctions(boolean beliefFunctions) {
		this.beliefFunctions = beliefFunctions;
	}

	/**
	 * Checks if is hungarian.
	 * 
	 * @return true, if is hungarian
	 */
	public boolean isHungarian() {
		return hungarian;
	}

	/**
	 * Sets the hungarian.
	 * 
	 * @param hungarian the new hungarian
	 */
	public void setHungarian(boolean hungarian) {
		this.hungarian = hungarian;
	}

	/**
	 * Checks if is maximum.
	 * 
	 * @return true, if is maximum
	 */
	public boolean isMaximum() {
		return maximum;
	}

	/**
	 * Sets the maximum.
	 * 
	 * @param maximum the new maximum
	 */
	public void setMaximum(boolean maximum) {
		this.maximum = maximum;
	}

	/**
	 * Checks if is dont check gender.
	 * 
	 * @return true, if is dont check gender
	 */
	public boolean isDontCheckGender() {
		return dontCheckGender;
	}

	/**
	 * Sets the dont check gender.
	 * 
	 * @param removeCheckGender the new dont check gender
	 */
	public void setDontCheckGender(boolean removeCheckGender) {
		this.dontCheckGender = removeCheckGender;
	}

	/**
	 * Checks if is first name check.
	 * 
	 * @return true, if is first name check
	 */
	public boolean isFirstNameCheck() {
		return firstNameCheck;
	}

	/**
	 * Sets the first name check.
	 * 
	 * @param firstNameCheck the new first name check
	 */
	public void setFirstNameCheck(boolean firstNameCheck) {
		this.firstNameCheck = firstNameCheck;
	}

	/**
	 * Gets the first name list.
	 * 
	 * @return the first name list
	 */
	public String getFirstNameList() {
		return firstNameList;
	}

	/**
	 * Sets the first name list.
	 * 
	 * @param ch the new first name list
	 */
	public void setFirstNameList(String ch) {
		this.firstNameList = ch;
	}

	/**
	 * Checks if is training.
	 * 
	 * @return the training
	 */
	public boolean isTraining() {
		return training;
	}

	/**
	 * Sets the training.
	 * 
	 * @param training the training to set
	 */
	public void setTraining(boolean training) {
		this.training = training;
	}

	/**
	 * Sets the close list check.
	 * 
	 * @param closeListCheck the closeListCheck to set
	 */
	public void setCloseListCheck(boolean closeListCheck) {
		this.closeListCheck = closeListCheck;
	}

	/**
	 * Checks if is close list check.
	 * 
	 * @return the closeListCheck
	 */
	public boolean isCloseListCheck() {
		return closeListCheck;
	}

	/**
	 * Gets the threshold audio.
	 * 
	 * @return the thresholdAudio
	 */
	public double getThresholdAudio() {
		return thresholdAudio;
	}

	/**
	 * Sets the threshold audio.
	 * 
	 * @param thresholdAudio the thresholdAudio to set
	 */
	public void setThresholdAudio(double thresholdAudio) {
		this.thresholdAudio = thresholdAudio;
	}

	/**
	 * Gets the threshold video.
	 * 
	 * @return the thresholdVideo
	 */
	public double getThresholdVideo() {
		return thresholdVideo;
	}

	/**
	 * Sets the threshold video.
	 * 
	 * @param thresholdVideo the thresholdVideo to set
	 */
	public void setThresholdVideo(double thresholdVideo) {
		this.thresholdVideo = thresholdVideo;
	}

	/**
	 * Gets the threshold writing.
	 * 
	 * @return the thresholdWriting
	 */
	public double getThresholdWriting() {
		return thresholdWriting;
	}

	/**
	 * Sets the threshold writing.
	 * 
	 * @param thresholdWriting the thresholdWriting to set
	 */
	public void setThresholdWriting(double thresholdWriting) {
		this.thresholdWriting = thresholdWriting;
	}

	/**
	 * Checks if is use audio.
	 * 
	 * @return the useAudio
	 */
	public boolean isUseAudio() {
		return useAudio;
	}

	/**
	 * Sets the use audio.
	 * 
	 * @param useAudio the useAudio to set
	 */
	public void setUseAudio(boolean useAudio) {
		this.useAudio = useAudio;
	}

	/**
	 * Checks if is use video.
	 * 
	 * @return the useVideo
	 */
	public boolean isUseVideo() {
		return useVideo;
	}

	/**
	 * Sets the use video.
	 * 
	 * @param useVideo the useVideo to set
	 */
	public void setUseVideo(boolean useVideo) {
		this.useVideo = useVideo;
	}

	/**
	 * Checks if is use transcription.
	 * 
	 * @return the useTranscription
	 */
	public boolean isUseTranscription() {
		return useTranscription;
	}

	/**
	 * Sets the use transcription.
	 * 
	 * @param useTranscription the useTranscription to set
	 */
	public void setUseTranscription(boolean useTranscription) {
		this.useTranscription = useTranscription;
	}

	/**
	 * Checks if is use writing.
	 * 
	 * @return the useWriting
	 */
	public boolean isUseWriting() {
		return useWriting;
	}

	/**
	 * Sets the use writing.
	 * 
	 * @param useWriting the useWriting to set
	 */
	public void setUseWriting(boolean useWriting) {
		this.useWriting = useWriting;
	}

	/**
	 * Gets the threshold decision.
	 * 
	 * @return the thresholdTranscription
	 */
	public double getThresholdDecision() {
		return thresholdDecision;
	}

}
