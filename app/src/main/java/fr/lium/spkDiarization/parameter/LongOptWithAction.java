package fr.lium.spkDiarization.parameter;

import gnu.getopt.LongOpt;

import java.util.logging.Logger;

/**
 * The Class LongOptWithAction.
 */
public class LongOptWithAction extends LongOpt {

	/** The Constant logger. */
	@SuppressWarnings("unused")
	private final static Logger logger = Logger.getLogger(LongOptWithAction.class.getName());

	/** The action. */
	private LongOptAction action;

	/** The comment. */
	private String comment;

	/**
	 * Instantiates a new long opt with action.
	 * 
	 * @param name the name
	 * @param has_arg the has_arg
	 * @param action the action
	 * @param comment the comment
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public LongOptWithAction(String name, int has_arg, LongOptAction action, String comment) throws IllegalArgumentException {
		super(name, has_arg, null, Parameter.getNextOptionIndex());
		this.action = action;
		this.comment = comment;
	}

	/**
	 * Instantiates a new long opt with action.
	 * 
	 * @param name the name
	 * @param action the action
	 * @param comment the comment
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	public LongOptWithAction(String name, LongOptAction action, String comment) throws IllegalArgumentException {
		super(name, 1, null, Parameter.getNextOptionIndex());
		this.action = action;
		this.comment = comment;
	}

	/**
	 * Gets the comment.
	 * 
	 * @return the comment
	 */
	protected String getComment() {
		return comment;
	}

	/**
	 * Log.
	 * 
	 * @param logger the logger
	 */
	public void log(Logger logger) {
		if (action != null) {
			action.log(logger, this);
		}
	}

	/**
	 * Execute.
	 * 
	 * @param optarg the optarg
	 */
	public void execute(String optarg) {
		if (action != null) {
			action.execute(optarg);
		}
	}

}
