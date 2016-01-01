package fr.lium.spkDiarization.parameter;

import java.util.logging.Logger;

/**
 * The Class LongOptAction.
 */
public abstract class LongOptAction {

	/** The logger. */
	protected Logger logger = Logger.getLogger(LongOptAction.class.getName());

	/**
	 * Execute.
	 * 
	 * @param optarg the optarg
	 */
	public abstract void execute(String optarg);

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return "<empty>";
	}

	/**
	 * Log.
	 * 
	 * @param logger the logger
	 * @param longOpt the long opt
	 */
	public void log(Logger logger, LongOptWithAction longOpt) {
		String value = getValue();
		String option = longOpt.getName();
		String comment = longOpt.getComment();
		String loggerName = logger.getName().substring(logger.getName().lastIndexOf("."));
		logger.config("--" + option + " \t " + comment + " = " + value + " [" + loggerName + "]");
	}

	/**
	 * Format strig array.
	 * 
	 * @param list the list
	 * @return the string
	 */
	public static String formatStrigArray(String[] list) {
		String formatList = list[0];
		for (int i = 1; i < list.length; i++) {
			formatList += "," + list[i];
		}
		return "[" + formatList + "]";
	}
}
