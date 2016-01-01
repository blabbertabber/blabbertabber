package fr.lium.spkDiarization.lib.libDiarizationError;

// import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;

/**
 * The Class DiarizationResult.
 */
public class DiarizationResult {
	// private final static Logger logger = Logger.getLogger(DiarizationResult.class.getName());

	/** The reference length. */
	protected int referenceLength;

	/** The hypthesis length. */
	protected int hypthesisLength;

	/** The speaker error. */
	protected int speakerError;

	/** The miss speaker error. */
	protected int missSpeakerError;

	/** The false alarm speaker error. */
	protected int falseAlarmSpeakerError;

	/** The threshold. */
	protected double threshold;

	/**
	 * Instantiates a new diarization result.
	 * 
	 * @param threshold the threshold
	 * @param referenceLength the reference length
	 * @param hypothesisLength the hypothesis length
	 * @param speakerError the speaker error
	 * @param missSpeakerError the miss speaker error
	 * @param falseAlarmSpeakerError the false alarm speaker error
	 */
	public DiarizationResult(double threshold, int referenceLength, int hypothesisLength, int speakerError, int missSpeakerError, int falseAlarmSpeakerError) {
		super();
		this.threshold = threshold;
		this.referenceLength = referenceLength;
		this.hypthesisLength = hypothesisLength;
		this.speakerError = speakerError;
		this.missSpeakerError = missSpeakerError;
		this.falseAlarmSpeakerError = falseAlarmSpeakerError;
	}

	/**
	 * Adds the result.
	 * 
	 * @param diarizationResult the diarization result
	 * @throws DiarizationException the diarization exception
	 */
	public void addResult(DiarizationResult diarizationResult) throws DiarizationException {

		if (diarizationResult.threshold != threshold) {
			throw new DiarizationException("DiarizationResult: addResult() threshold problem : "
					+ diarizationResult.threshold + " / " + threshold);
		}
		referenceLength += diarizationResult.referenceLength;
		hypthesisLength += diarizationResult.hypthesisLength;
		speakerError += diarizationResult.speakerError;
		missSpeakerError += diarizationResult.missSpeakerError;
		falseAlarmSpeakerError += diarizationResult.falseAlarmSpeakerError;
	}

	/**
	 * Sets the result.
	 * 
	 * @param diarizationResult the new result
	 * @throws DiarizationException the diarization exception
	 */
	public void setResult(DiarizationResult diarizationResult) throws DiarizationException {

		if (diarizationResult.threshold != threshold) {
			throw new DiarizationException("threshold problem : " + diarizationResult.threshold + " / " + threshold);
		}
		referenceLength = diarizationResult.referenceLength;
		hypthesisLength = diarizationResult.hypthesisLength;
		speakerError = diarizationResult.speakerError;
		missSpeakerError = diarizationResult.missSpeakerError;
		falseAlarmSpeakerError = diarizationResult.falseAlarmSpeakerError;
	}

	/**
	 * Gets the threshold.
	 * 
	 * @return the threshold
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * Sets the threshold.
	 * 
	 * @param threshold the threshold to set
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	/**
	 * Gets the reference length.
	 * 
	 * @return the refLength
	 */
	public int getReferenceLength() {
		return referenceLength;
	}

	/**
	 * Sets the reference length.
	 * 
	 * @param refLength the refLength to set
	 */
	public void setReferenceLength(int refLength) {
		this.referenceLength = refLength;
	}

	/**
	 * Gets the hypthesis length.
	 * 
	 * @return the hypLength
	 */
	public int getHypthesisLength() {
		return hypthesisLength;
	}

	/**
	 * Sets the hypthesis length.
	 * 
	 * @param hypLength the hypLength to set
	 */
	public void setHypthesisLength(int hypLength) {
		this.hypthesisLength = hypLength;
	}

	/**
	 * Gets the speaker error.
	 * 
	 * @return the spk
	 */
	public int getSpeakerError() {
		return speakerError;
	}

	/**
	 * Sets the speaker error.
	 * 
	 * @param spk the spk to set
	 */
	public void setSpeakerError(int spk) {
		this.speakerError = spk;
	}

	/**
	 * Gets the miss speaker error.
	 * 
	 * @return the miss
	 */
	public int getMissSpeakerError() {
		return missSpeakerError;
	}

	/**
	 * Sets the miss speaker error.
	 * 
	 * @param miss the miss to set
	 */
	public void setMissSpeakerError(int miss) {
		this.missSpeakerError = miss;
	}

	/**
	 * Gets the false alarm speaker error.
	 * 
	 * @return the fa
	 */
	public int getFalseAlarmSpeakerError() {
		return falseAlarmSpeakerError;
	}

	/**
	 * Sets the false alarm speaker error.
	 * 
	 * @param fa the fa to set
	 */
	public void setFalseAlarmSpeakerError(int fa) {
		this.falseAlarmSpeakerError = fa;
	}

	/**
	 * Gets the error rate.
	 * 
	 * @return the error rate
	 */
	public double getErrorRate() {
		if (referenceLength == 0) {
			return Double.MAX_VALUE;
		}
		return ((speakerError + missSpeakerError + falseAlarmSpeakerError) / (double) (referenceLength)) * 100.0;
	}

	/**
	 * Gets the sum of error.
	 * 
	 * @return the sum of error
	 */
	public int getSumOfError() {
		if (referenceLength == 0) {
			return Integer.MAX_VALUE;
		}
		return (speakerError + missSpeakerError + falseAlarmSpeakerError);
	}

}
