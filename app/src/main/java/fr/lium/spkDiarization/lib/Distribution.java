/**
 * 
 * <p>
 * Distribution
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package fr.lium.spkDiarization.lib;

// import java.util.logging.Logger;

/**
 * The Class Distribution.
 */
public class Distribution {
	// private final static Logger logger = Logger.getLogger(Distribution.class.getName());

	/** The Constant NORMAL_INVERT_A1. */
	protected static final double NORMAL_INVERT_A1 = -3.969683028665376e+01;

	/** The Constant NORMAL_INVERT_A2. */
	protected static final double NORMAL_INVERT_A2 = 2.209460984245205e+02;

	/** The Constant NORMAL_INVERT_A3. */
	protected static final double NORMAL_INVERT_A3 = -2.759285104469687e+02;

	/** The Constant NORMAL_INVERT_A4. */
	protected static final double NORMAL_INVERT_A4 = 1.383577518672690e+02;

	/** The Constant NORMAL_INVERT_A5. */
	protected static final double NORMAL_INVERT_A5 = -3.066479806614716e+01;

	/** The Constant NORMAL_INVERT_A6. */
	protected static final double NORMAL_INVERT_A6 = 2.506628277459239e+00;

	/** The Constant NORMAL_INVERT_B1. */
	protected static final double NORMAL_INVERT_B1 = -5.447609879822406e+01;

	/** The Constant NORMAL_INVERT_B2. */
	protected static final double NORMAL_INVERT_B2 = 1.615858368580409e+02;

	/** The Constant NORMAL_INVERT_B3. */
	protected static final double NORMAL_INVERT_B3 = -1.556989798598866e+02;

	/** The Constant NORMAL_INVERT_B4. */
	protected static final double NORMAL_INVERT_B4 = 6.680131188771972e+01;

	/** The Constant NORMAL_INVERT_B5. */
	protected static final double NORMAL_INVERT_B5 = -1.328068155288572e+01;

	/** The Constant NORMAL_INVERT_C1. */
	protected static final double NORMAL_INVERT_C1 = -7.784894002430293e-03;

	/** The Constant NORMAL_INVERT_C2. */
	protected static final double NORMAL_INVERT_C2 = -3.223964580411365e-01;

	/** The Constant NORMAL_INVERT_C3. */
	protected static final double NORMAL_INVERT_C3 = -2.400758277161838e+00;

	/** The Constant NORMAL_INVERT_C4. */
	protected static final double NORMAL_INVERT_C4 = -2.549732539343734e+00;

	/** The Constant NORMAL_INVERT_C5. */
	protected static final double NORMAL_INVERT_C5 = 4.374664141464968e+00;

	/** The Constant NORMAL_INVERT_C6. */
	protected static final double NORMAL_INVERT_C6 = 2.938163982698783e+00;

	/** The Constant NORMAL_INVERT_D1. */
	protected static final double NORMAL_INVERT_D1 = 7.784695709041462e-03;

	/** The Constant NORMAL_INVERT_D2. */
	protected static final double NORMAL_INVERT_D2 = 3.224671290700398e-01;

	/** The Constant NORMAL_INVERT_D3. */
	protected static final double NORMAL_INVERT_D3 = 2.445134137142996e+00;

	/** The Constant NORMAL_INVERT_D4. */
	protected static final double NORMAL_INVERT_D4 = 3.754408661907416e+00;

	/** The Constant NORMAL_INVERT_P_LOW. */
	protected static final double NORMAL_INVERT_P_LOW = 0.02425;

	/** The Constant NORMAL_INVERT_P_HIGH. */
	protected static final double NORMAL_INVERT_P_HIGH = 1.0 - NORMAL_INVERT_P_LOW;

	/**
	 * Cumulative normal distribution.
	 * 
	 * @param x the x
	 * 
	 * @return the double
	 */
	private static double cumulativeNormalDistribution(double x) {
		double b1 = 0.319381530;
		double b2 = -0.356563782;
		double b3 = 1.781477937;
		double b4 = -1.821255978;
		double b5 = 1.330274429;
		double p = 0.2316419;
		double c = 0.39894228;

		if (x >= 0.0) {
			double t = 1.0 / (1.0 + (p * x));
			return (1.0 - (c * Math.exp((-x * x) / 2.0) * t * ((t * ((t * ((t * ((t * b5) + b4)) + b3)) + b2)) + b1)));
		} else {
			double t = 1.0 / (1.0 - (p * x));
			return (c * Math.exp((-x * x) / 2.0) * t * ((t * ((t * ((t * ((t * b5) + b4)) + b3)) + b2)) + b1));
		}

	}

	/**
	 * Normal pdf score.
	 * 
	 * @param x the x
	 * @param mean the mean
	 * @param std the variance
	 * 
	 * @return the double
	 */
	public static double normalDistribution(double x, double mean, double std) {
		return (1.0 / (Math.sqrt(std * 2.0 * Math.PI))) * Math.exp((-0.5 * Math.pow((x - mean), 2.0)) / std);
	}

	/**
	 * Cumulative normal distribution.
	 * 
	 * @param x the x
	 * @param mean the mean
	 * @param std the std
	 * @return the double
	 */
	public static double cumulativeNormalDistribution(double x, double mean, double std) {
		return cumulativeNormalDistribution((x - mean) / std);
	}

	/**
	 * Normal invert.
	 * 
	 * @param p the p
	 * 
	 * @return the double
	 * 
	 * @throws DiarizationException the diarization exception
	 */
	public static double normalInvert(double p) throws DiarizationException {
		double x = Double.NEGATIVE_INFINITY, q, r;
		if ((0 < p) && (p < NORMAL_INVERT_P_LOW)) {
			q = Math.sqrt(-2 * Math.log(p));
			x = ((((((((((NORMAL_INVERT_C1 * q) + NORMAL_INVERT_C2) * q) + NORMAL_INVERT_C3) * q) + NORMAL_INVERT_C4) * q) + NORMAL_INVERT_C5) * q) + NORMAL_INVERT_C6)
					/ ((((((((NORMAL_INVERT_D1 * q) + NORMAL_INVERT_D2) * q) + NORMAL_INVERT_D3) * q) + NORMAL_INVERT_D4) * q) + 1);
		} else if ((NORMAL_INVERT_P_LOW <= p) && (p <= NORMAL_INVERT_P_HIGH)) {
			q = p - 0.5;
			r = q * q;
			x = (((((((((((NORMAL_INVERT_A1 * r) + NORMAL_INVERT_A2) * r) + NORMAL_INVERT_A3) * r) + NORMAL_INVERT_A4) * r) + NORMAL_INVERT_A5) * r) + NORMAL_INVERT_A6) * q)
					/ ((((((((((NORMAL_INVERT_B1 * r) + NORMAL_INVERT_B2) * r) + NORMAL_INVERT_B3) * r) + NORMAL_INVERT_B4) * r) + NORMAL_INVERT_B5) * r) + 1);
		} else if ((NORMAL_INVERT_P_HIGH < p) && (p < 1)) {
			q = Math.sqrt(-2.0 * Math.log(1 - p));
			x = -((((((((((NORMAL_INVERT_C1 * q) + NORMAL_INVERT_C2) * q) + NORMAL_INVERT_C3) * q) + NORMAL_INVERT_C4) * q) + NORMAL_INVERT_C5) * q) + NORMAL_INVERT_C6)
					/ ((((((((NORMAL_INVERT_D1 * q) + NORMAL_INVERT_D2) * q) + NORMAL_INVERT_D3) * q) + NORMAL_INVERT_D4) * q) + 1);
		} else {
			throw new DiarizationException("FeatureNorm : normalInvert");
		}

		return x;
	}
}
