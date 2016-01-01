/**
 * 
 * <p>
 * MAPScoreNormalization
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          see, C. Fredouille, J.-F. Bonastre, T. Merlin, "Similarity Normalization Method based on World Model and a Posteriori Probability for Speaker Verification", Eurospeech 99, September 1999
 */

package fr.lium.spkDiarization.libModel;

import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.Distribution;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;

/**
 * The Class MAPScoreNormalization.
 */
public class MAPScoreNormalization {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MAPScoreNormalization.class.getName());

	/** The mean target. */
	private double meanTarget;

	/** The std traget. */
	private double stdTarget;

	/** The probability targer. */
	private double probabilityTarget;

	/** The mean non target. */
	private double meanNonTarget;

	/** The std non traget. */
	private double stdNonTarget;

	/** The probability non target. */
	private double probabilityNonTarget;

	/**
	 * Instantiates a new mAP score normalization.
	 * 
	 * @param meanTarget the mean target
	 * @param stdTraget the std traget
	 * @param probabilityTarger the probability target
	 * @param meanNonTarget the mean non target
	 * @param stdNonTraget the std non target
	 */
	public MAPScoreNormalization(double meanTarget, double stdTraget, double probabilityTarger, double meanNonTarget, double stdNonTraget) {
		super();
		this.meanTarget = meanTarget;
		this.stdTarget = stdTraget;
		this.probabilityTarget = probabilityTarger;
		this.meanNonTarget = meanNonTarget;
		this.stdNonTarget = stdNonTraget;
		this.probabilityNonTarget = 1.0 - probabilityTarger;
		if (SpkDiarizationLogger.DEBUG) logger.info(" mT=" + meanTarget + " sT=" + stdTraget + " paT=" + probabilityTarger + " mNT=" + meanNonTarget
				+ " sNT=" + stdNonTraget);
	}

	/**
	 * Gets the mean non target.
	 * 
	 * @return the mean non target
	 */
	public double getMeanNonTarget() {
		return meanNonTarget;
	}

	/**
	 * Gets the mean target.
	 * 
	 * @return the mean target
	 */
	public double getMeanTarget() {
		return meanTarget;
	}

	/**
	 * Gets the probability non target.
	 * 
	 * @return the probability non target
	 */
	public double getProbabilityNonTarget() {
		return probabilityNonTarget;
	}

	/**
	 * Gets the probability target.
	 * 
	 * @return the probability target
	 */
	public double getProbabilityTarget() {
		return probabilityTarget;
	}

	/**
	 * Gets the std non target.
	 * 
	 * @return the std non target
	 */
	public double getStdNonTraget() {
		return stdNonTarget;
	}

	/**
	 * Gets the std target.
	 * 
	 * @return the std target
	 */
	public double getStdTraget() {
		return stdTarget;
	}

	/**
	 * Normalize.
	 * 
	 * @param score the score
	 * 
	 * @return the double
	 */
	public double normalize(double score) {
		double target = Distribution.normalDistribution(score, meanTarget, stdTarget) * probabilityTarget;
		double nonTarget = Distribution.normalDistribution(score, meanNonTarget, stdNonTarget) * probabilityNonTarget;
		return target / (target + nonTarget);
	}

	/**
	 * Sets the mean non target.
	 * 
	 * @param meanNonTarget the new mean non target
	 */
	public void setMeanNonTarget(double meanNonTarget) {
		this.meanNonTarget = meanNonTarget;
	}

	/**
	 * Sets the mean target.
	 * 
	 * @param meanTarget the new mean target
	 */
	public void setMeanTarget(double meanTarget) {
		this.meanTarget = meanTarget;
	}

	/**
	 * Sets the probability non target.
	 * 
	 * @param probabilityNonTarger the new probability non target
	 */
	public void setProbabilityNonTarget(double probabilityNonTarger) {
		this.probabilityNonTarget = probabilityNonTarger;
	}

	/**
	 * Sets the probability target.
	 * 
	 * @param probabilityTarger the new probability target
	 */
	public void setProbabilityTarget(double probabilityTarger) {
		this.probabilityTarget = probabilityTarger;
	}

	/**
	 * Sets the standard deviation non target.
	 * 
	 * @param stdNonTraget the new standard deviation non target
	 */
	public void setStdNonTarget(double stdNonTraget) {
		this.stdNonTarget = stdNonTraget;
	}

	/**
	 * Sets the standard deviation target.
	 * 
	 * @param stdTraget the new standard target
	 */
	public void setStdTarget(double stdTraget) {
		this.stdTarget = stdTraget;
	}

}
