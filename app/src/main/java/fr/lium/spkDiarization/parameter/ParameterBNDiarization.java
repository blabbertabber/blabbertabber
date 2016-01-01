package fr.lium.spkDiarization.parameter;

import java.util.logging.Logger;

/**
 * The Class ParameterBNDiarization.
 */
public class ParameterBNDiarization extends ParameterBase implements Cloneable {

	/** The CE clustering. */
	private Boolean CEClustering;

	/**
	 * The Class ActionCEClustering.
	 */
	private class ActionCEClustering extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setCEClustering(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return CEClustering.toString();
		}
	}

	/** The thread. */
	private Integer thread;

	/**
	 * The Class ActionThread.
	 */
	private class ActionThread extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setThread(arg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return thread.toString();
		}
	}

	/** The save all step. */
	private Boolean saveAllStep;

	/**
	 * The Class ActionSaveAllStep.
	 */
	private class ActionSaveAllStep extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setSaveAllStep(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return saveAllStep.toString();
		}
	}

	/** The last step only. */
	private Boolean lastStepOnly;

	/**
	 * The Class ActionLastStepOnly.
	 */
	private class ActionLastStepOnly extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setLastStepOnly(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return lastStepOnly.toString();
		}
	}

	/** The load input segmentation. */
	private Boolean loadInputSegmentation;

	/**
	 * The Class ActionLoadInputSegmentation.
	 */
	private class ActionLoadInputSegmentation extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setLoadInputSegmentation(true);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return loadInputSegmentation.toString();
		}
	}

	/** The system. */
	private String system;

	/**
	 * The Class ActionSystem.
	 */
	private class ActionSystem extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setSystem(arg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return system.toString();
		}
	}

	/** The thresholds string. */
	private String thresholdsString;

	/**
	 * The Class ActionThresholdsString.
	 */
	private class ActionThresholdsString extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			setThresholds(arg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String option = longOpt.getName();

			String ch = "";
			int i = 0;
			if (thresholds[i] == thresholdsMax[i]) {
				ch = thresholdsKey[i] + "=" + Double.toString(thresholds[i]);
			} else {
				ch = thresholdsKey[i] + "=" + Double.toString(thresholds[i]) + ":" + Double.toString(thresholdsMax[i]);
			}
			for (i = 1; i < thresholds.length; i++) {
				if (thresholds[i] == thresholdsMax[i]) {
					ch = ch + ", " + thresholdsKey[i] + "=" + Double.toString(thresholds[i]);
				} else {
					ch = ch + ", " + thresholdsKey[i] + "=" + Double.toString(thresholds[i]) + ":"
							+ Double.toString(thresholdsMax[i]);
				}
			}
			logger.config("--" + option + " \t " + ch + " [" + logger.getName() + "]");

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

	/** The Constant thresholdsKey. */
	static final private String[] thresholdsKey = { "l", "h", "d", "c" };

	/** The thresholds. */
	private double[] thresholds = { 2.0, 3.0, 250.0, 1.7 };

	/** The thresholds max. */
	private double[] thresholdsMax = { 2.0, 3.0, 250.0, 1.7 };

	/** The Constant SystemString. */
	public final static String[] SystemString = { "baseline", "10s", "tv" };

	/**
	 * Instantiates a new parameter bn diarization.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterBNDiarization(Parameter parameter) {
		super(parameter);
		CEClustering = false;
		saveAllStep = false;
		lastStepOnly = false;
		loadInputSegmentation = false;
		thread = 1;
		system = SystemString[0];

		addOption(new LongOptWithAction("system", new ActionSystem(), "selection the diarization module"));
		addOption(new LongOptWithAction("thresholds", new ActionThresholdsString(), "thresholds of all step"));
		addOption(new LongOptWithAction("doCEClustering", 0, new ActionCEClustering(), "make the CLR/NCLR clustering"));
		addOption(new LongOptWithAction("nbThread", new ActionThread(), "number of shows process in parallele"));
		addOption(new LongOptWithAction("saveAllStep", 0, new ActionSaveAllStep(), "save all intermediate diarization"));
		addOption(new LongOptWithAction("lastStepOnly", 0, new ActionLastStepOnly(), "need an input diarization"));
		addOption(new LongOptWithAction("loadInputSegmentation", 0, new ActionLoadInputSegmentation(), "load an input diarization"));
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterBNDiarization clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return (ParameterBNDiarization) super.clone();
	}

	/**
	 * Gets the threshold.
	 * 
	 * @param key the key
	 * @return the threshold
	 */
	public double getThreshold(String key) {
		for (int i = 0; i < thresholdsKey.length; i++) {
			if (key.equals(thresholdsKey[i])) {
				return thresholds[i];
			}
		}
		return Double.NaN;
	}

	/**
	 * Gets the max threshold.
	 * 
	 * @param key the key
	 * @return the max threshold
	 */
	public double getMaxThreshold(String key) {
		for (int i = 0; i < thresholdsKey.length; i++) {
			if (key.equals(thresholdsKey[i])) {
				return thresholdsMax[i];
			}
		}
		return Double.NaN;
	}

	/**
	 * Sets the thresholds.
	 * 
	 * @param optarg the new thresholds
	 */
	private void setThresholds(String optarg) {
		thresholdsString = optarg;
		String[] tab = optarg.split(",");
		for (int i = 0; i < thresholds.length; i++) {
			if (i < tab.length) {
				double min = Double.NaN;
				double max = Double.NaN;
				if (tab[i].contains(":")) {
					String minmax[] = tab[i].split(":");
					min = Double.valueOf(minmax[0]);
					max = Double.valueOf(minmax[1]);
				} else {
					min = Double.valueOf(tab[i]);
					max = min;
				}
				thresholds[i] = min;
				thresholdsMax[i] = max;
			}
		}
	}

	/**
	 * Sets the thread.
	 * 
	 * @param value the new thread
	 */
	private void setThread(String value) {
		thread = Integer.parseInt(value);
	}

	/**
	 * Gets the thread.
	 * 
	 * @return the thread
	 */
	public int getThread() {
		return thread;
	}

	/**
	 * Checks if is cE clustering.
	 * 
	 * @return true, if is cE clustering
	 */
	public boolean isCEClustering() {
		return CEClustering;
	}

	/**
	 * Checks if is load input segmentation.
	 * 
	 * @return true, if is load input segmentation
	 */
	public boolean isLoadInputSegmentation() {
		return loadInputSegmentation;
	}

	/**
	 * Sets the system.
	 * 
	 * @param system the new system
	 */
	public void setSystem(String system) {
		this.system = SystemString[0];
		if (system.equals(SystemString[1])) {
			this.system = system;
		}
	}

	/**
	 * Sets the cE clustering.
	 * 
	 * @param cEClustering the new cE clustering
	 */
	public void setCEClustering(boolean cEClustering) {
		CEClustering = cEClustering;
	}

	/**
	 * Sets the load input segmentation.
	 * 
	 * @param b the new load input segmentation
	 */
	private void setLoadInputSegmentation(boolean b) {
		loadInputSegmentation = b;
	}

	/**
	 * Gets the system.
	 * 
	 * @return the system
	 */
	public String getSystem() {
		return system;
	}

	/**
	 * Checks if is save all step.
	 * 
	 * @return true, if is save all step
	 */
	public boolean isSaveAllStep() {
		return saveAllStep;
	}

	/**
	 * Sets the save all step.
	 * 
	 * @param saveAllStep the new save all step
	 */
	public void setSaveAllStep(boolean saveAllStep) {
		this.saveAllStep = saveAllStep;
	}

	/**
	 * Checks if is last step only.
	 * 
	 * @return the lastStepOnly
	 */
	public boolean isLastStepOnly() {
		return lastStepOnly;
	}

	/**
	 * Sets the last step only.
	 * 
	 * @param lastStepOnly the lastStepOnly to set
	 */
	public void setLastStepOnly(boolean lastStepOnly) {
		this.lastStepOnly = lastStepOnly;
	}

}
