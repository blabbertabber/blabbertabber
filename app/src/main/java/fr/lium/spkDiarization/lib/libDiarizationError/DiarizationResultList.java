package fr.lium.spkDiarization.lib.libDiarizationError;

import java.util.ArrayList;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;

/**
 * The Class DiarizationResultList.
 */
public class DiarizationResultList {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(DiarizationResultList.class.getName());

	/** The list. */
	private ArrayList<DiarizationResult> list;

	/** The precision. */
	private double precision;

	/** The min. */
	private int min;

	/** The max. */
	private int max;

	/**
	 * Instantiates a new diarization result list.
	 * 
	 * @param cMin the c min
	 * @param cMax the c max
	 * @param precision the precision
	 */
	public DiarizationResultList(double cMin, double cMax, double precision) {
		super();
		this.precision = precision;
		min = (int) Math.round(cMin * precision);
		max = (int) Math.round(cMax * precision);
		initialize();
	}

	/**
	 * Adds the result array.
	 * 
	 * @param diarizationResultList the diarization result list
	 * @throws DiarizationException the diarization exception
	 */
	public void addResultArray(DiarizationResultList diarizationResultList) throws DiarizationException {
		if (list.size() != diarizationResultList.list.size()) {
			throw new ArrayStoreException("size problem");
		}
		for (int i = 0; i < list.size(); i++) {
			list.get(i).addResult(diarizationResultList.list.get(i));
		}
	}

	/**
	 * Score2int.
	 * 
	 * @param score the score
	 * @return the int
	 */
	private int score2int(double score) {
		if (score == Double.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		long result = Math.round(score * precision);
		if (result > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if (result < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}
		return (int) result;
	}

	/**
	 * Sets the result.
	 * 
	 * @param previousScore the previous score
	 * @param score the score
	 * @param diarizationResult the diarization result
	 * @throws DiarizationException the diarization exception
	 */
	public void setResult(double previousScore, double score, DiarizationResult diarizationResult) throws DiarizationException {
		long endScore = score2int(score);
		long startScore = score2int(previousScore);
		int end = (int) Math.min(max, endScore);
		int start = (int) Math.max(min, startScore);

		logger.finer("put score previous=" + previousScore + " score=" + score + "(" + endScore + ") start/minStart="
				+ start + " / " + min + " / " + Math.max(min, startScore) + " end/maxEnd=" + end + " / " + max
				+ " array length=" + list.size());
		for (int i = start; i <= end; i++) {
			diarizationResult.setThreshold(getThershold(i - min));
			list.get(i - min).setResult(diarizationResult);
		}
	}

	/**
	 * Gets the thershold.
	 * 
	 * @param index the index
	 * @return the thershold
	 */
	private double getThershold(int index) {
		return (min + index) / precision;
	}

	/**
	 * Initialize.
	 */
	protected void initialize() {
		list = new ArrayList<DiarizationResult>();
		list.ensureCapacity((max - min) + 1);
		for (int i = min; i <= max; i++) {
			list.add(i - min, new DiarizationResult(getThershold(i - min), 0, 0, 0, 0, 0));
		}
	}

	/**
	 * Find minimum error.
	 * 
	 * @return the diarization result
	 */
	protected DiarizationResult findMinimumError() {
		int index = -1;
		int error = Integer.MAX_VALUE;
		for (int i = 0; i < list.size(); i++) {
			int currentError = list.get(i).getSumOfError();
			if (currentError < error) {
				error = currentError;
				index = i;
			}
		}
		if (index == -1) {
			return null;
		}
		return list.get(index);
	}

	/**
	 * Log.
	 * 
	 * @param key the key
	 */
	public void log(String key) {
		DiarizationResult minimumError = findMinimumError();
		if (minimumError != null) {
			for (int i = 0; i < list.size(); i++) {
				DiarizationResult currentError = list.get(i);
				String message = key + " thr= " + currentError.getThreshold();
				message += " spk= " + currentError.getSpeakerError();
				message += " fa= " + currentError.getFalseAlarmSpeakerError();
				message += " miss= " + currentError.getMissSpeakerError();
				message += " | refLen= " + currentError.getReferenceLength();
				message += " hypLen= " + currentError.getHypthesisLength();
				message += " | error= " + currentError.getSumOfError();
				message += String.format(" rate= %.3f", currentError.getErrorRate());
				if (minimumError.getSumOfError() == currentError.getSumOfError()) {
					message += " ** ";
				}
				logger.info(message);
			}
		}
	}

}
