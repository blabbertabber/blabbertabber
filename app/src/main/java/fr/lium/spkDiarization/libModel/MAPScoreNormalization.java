/**
 * <p>
 * MAPScoreNormalization
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 * <p/>
 * see, C. Fredouille, J.-F. Bonastre, T. Merlin, "Similarity Normalization Method based on World Model and a Posteriori Probability for Speaker
 * Verification", Eurospeech 99, September 1999
 */

package fr.lium.spkDiarization.libModel;

/**
 * The Class MAPScoreNormalization.
 */
public class MAPScoreNormalization {

    /** The mean target. */
    private double meanTarget;

    /** The std traget. */
    private double stdTraget;

    /** The probability targer. */
    private double probabilityTarger;

    /** The mean non target. */
    private double meanNonTarget;

    /** The std non traget. */
    private double stdNonTraget;

    /** The probability non targer. */
    private double probabilityNonTarger;

    /**
     * Instantiates a new mAP score normalization.
     *
     * @param meanTarget the mean target
     * @param stdTraget the std traget
     * @param probabilityTarger the probability target
     * @param meanNonTarget the mean non target
     * @param stdNonTraget the std non target
     * @param probabilityNonTarger the probability non target
     */
    public MAPScoreNormalization(double meanTarget, double stdTraget, double probabilityTarger, double meanNonTarget, double stdNonTraget,
                                 double probabilityNonTarger) {
        super();
        this.meanTarget = meanTarget;
        this.stdTraget = stdTraget;
        this.probabilityTarger = probabilityTarger;
        this.meanNonTarget = meanNonTarget;
        this.stdNonTraget = stdNonTraget;
        this.probabilityNonTarger = probabilityNonTarger;
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
     * Sets the mean non target.
     *
     * @param meanNonTarget the new mean non target
     */
    public void setMeanNonTarget(double meanNonTarget) {
        this.meanNonTarget = meanNonTarget;
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
     * Sets the mean target.
     *
     * @param meanTarget the new mean target
     */
    public void setMeanTarget(double meanTarget) {
        this.meanTarget = meanTarget;
    }

    /**
     * Gets the probability non target.
     *
     * @return the probability non target
     */
    public double getProbabilityNonTarger() {
        return probabilityNonTarger;
    }

    /**
     * Sets the probability non target.
     *
     * @param probabilityNonTarger the new probability non target
     */
    public void setProbabilityNonTarger(double probabilityNonTarger) {
        this.probabilityNonTarger = probabilityNonTarger;
    }

    /**
     * Gets the probability target.
     *
     * @return the probability target
     */
    public double getProbabilityTarger() {
        return probabilityTarger;
    }

    /**
     * Sets the probability target.
     *
     * @param probabilityTarger the new probability target
     */
    public void setProbabilityTarger(double probabilityTarger) {
        this.probabilityTarger = probabilityTarger;
    }

    /**
     * Gets the std non target.
     *
     * @return the std non target
     */
    public double getStdNonTraget() {
        return stdNonTraget;
    }

    /**
     * Sets the standard deviation non target.
     *
     * @param stdNonTraget the new standard deviation non target
     */
    public void setStdNonTraget(double stdNonTraget) {
        this.stdNonTraget = stdNonTraget;
    }

    /**
     * Gets the std target.
     *
     * @return the std target
     */
    public double getStdTraget() {
        return stdTraget;
    }

    /**
     * Sets the standard deviation target.
     *
     * @param stdTraget the new standard target
     */
    public void setStdTraget(double stdTraget) {
        this.stdTraget = stdTraget;
    }

    /**
     * Normalize.
     *
     * @param score the score
     *
     * @return the double
     */
    public double normalize(double score) {
        double target = normalPDFScore(score, meanTarget, stdTraget) * probabilityTarger;
        double nonTarget = normalPDFScore(score, meanNonTarget, stdNonTraget) * probabilityNonTarger;
        return target / (target + nonTarget);
    }

    /**
     * Normal pdf score.
     *
     * @param x the x
     * @param mean the mean
     * @param std the standard deviation
     *
     * @return the double
     */
    private double normalPDFScore(double x, double mean, double std) {
        return 1.0 / (std * Math.sqrt(2 * Math.PI)) * Math.exp(-0.5) * Math.pow(((x - mean) / std), 2.0);
    }

}
