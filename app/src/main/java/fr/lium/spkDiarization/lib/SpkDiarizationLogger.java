package fr.lium.spkDiarization.lib;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Class SpkDiarizationLogger.
 */
public class SpkDiarizationLogger {
        public static boolean DEBUG = false;
        
	/** The logger. */
	private static Logger logger;

	/** The level. */
	private static Level level = Level.FINER;

	/** The console handler. */
	private static ConsoleHandler consoleHandler;

	/** The spk diarization formatter. */
	private static SpkDiarizationFormatter spkDiarizationFormatter;

	/**
	 * Setup.
	 */
	public static void setup() {
		// Create Logger
		logger = Logger.getLogger("");
		logger.removeHandler(logger.getHandlers()[0]);
		consoleHandler = new ConsoleHandler();
		spkDiarizationFormatter = new SpkDiarizationFormatter();
		logger.addHandler(consoleHandler);
		consoleHandler.setFormatter(spkDiarizationFormatter);
		logger.setLevel(Level.FINER);
		consoleHandler.setLevel(Level.FINER);
	}

	/**
	 * Sets the level.
	 * 
	 * @param levelString the new level
	 */
	public static void setLevel(String levelString) {
		if (levelString.contains("FINER")) {
			level = Level.FINER;
		} else if (levelString.contains("FINEST")) {
			level = Level.FINEST;
		} else if (levelString.contains("ALL")) {
			level = Level.ALL;
		} else if (levelString.contains("CONFIG")) {
			level = Level.CONFIG;
		} else if (levelString.contains("FINE")) {
			level = Level.FINE;
		} else if (levelString.contains("INFO")) {
			level = Level.INFO;
		} else if (levelString.contains("WARNING")) {
			level = Level.WARNING;
		} else if (levelString.contains("SEVERE")) {
			level = Level.SEVERE;
		}
		logger.setLevel(level);
		consoleHandler.setLevel(level);
		logger.config("ReferenceLoggerLevel:" + level.toString());

	}

}
