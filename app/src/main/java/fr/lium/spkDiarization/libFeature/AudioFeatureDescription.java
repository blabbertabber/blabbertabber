/**
 * 
 * <p>
 * FeatureDesc
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
 * 
 */

package fr.lium.spkDiarization.libFeature;

import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.parameter.ParameterAudioFeature;

/**
 * The Class FeatureDescription.
 */
public class AudioFeatureDescription implements Cloneable {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(AudioFeatureDescription.class.getName());

	/** The Constant STATIC_COEFF_MASK. */
	public static final byte STATIC_COEFF_MASK = 1;

	/** The Constant ENERGY_MASK. */
	public static final byte ENERGY_MASK = 2;

	/** The Constant DELTA_COEFF_MASK. */
	public static final byte DELTA_COEFF_MASK = 4;

	/** The Constant DELTAENERGY_MASK. */
	public static final byte DELTAENERGY_MASK = 8;

	/** The Constant DOUBLEDELTA_COEFF_MASK. */
	public static final byte DOUBLEDELTA_COEFF_MASK = 16;

	/** The Constant DOUBLEDELTAENERGY_MASK. */
	public static final byte DOUBLEDELTAENERGY_MASK = 32;

	/** The Constant NORM_BY_SEGMENT. */
	public static final int NORM_BY_SEGMENT = 0;

	/** The Constant NORM_BY_CLUSTER. */
	public static final int NORM_BY_CLUSTER = 1;

	/** The Constant NORM_BY_SLIDING. */
	public static final int NORM_BY_SLIDING = 2;

	/** The Constant NORM_BY_WARPING. */
	public static final int NORM_BY_WARPING = 3;

	/** The Constant NORM_BY_WARPING_AND_CR. */
	public static final int NORM_BY_WARPING_AND_CR = 4;

	/** The Constant NORM_BY_WARPING_AND_CRByCluster. */
	public static final int NORM_BY_WARPING_AND_CRByCluster = 6;

	/** The Constant NORM_BY_CR_AND_MAPPING. */
	public static final int NORM_BY_CR_AND_MAPPING = 5;

	/** The Constant NORMALIZE_METHOD_STR. */
	public static final String[] NORMALIZE_METHOD_STR = { "segment", "cluster", "sliding", "warping", "warping&CR",
			"cr&mapping", "warping&CRByCluster" };

	/** Dimension of feature vector in file. */
	protected int featureSize;

	/** The present parts. */
	protected byte presentParts;

	/** The needed parts. */
	protected byte neededParts;

	/** The centered. */
	protected boolean centered;

	/** The reduced. */
	protected boolean reduced;

	/** The window size. */
	protected int windowSize;

	/** The normalization method. */
	protected int normalizationMethod;

	/** Feature file format. */
	private int featuresFormat;

	/** The delta format. */
	private int deltaFormat;

	/**
	 * Instantiates a new feature description.
	 */
	public AudioFeatureDescription() {
		presentParts = 0;
		neededParts = 0;
		centered = false;
		reduced = false;
		featureSize = 0;
		windowSize = 0;
		setStaticCoeffPresence(true);
		setEnergyPresence(true);
		setDeltaCoeffPresence(false);
		setDoubleDeltaCoeffPresence(false);
		setDeltaEnergyPresence(false);
		setDoubleDeltaEnergyPresence(false);
		setFeatureSize(13);
		setCentered(false);
		setReduced(false);
		setNormalizationWindowSize(0);
		setNormalizationMethod(AudioFeatureDescription.NORM_BY_SEGMENT);
		setFeaturesFormat(AudioFeatureSet.SPHINX);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		AudioFeatureDescription result = null;
		try {
			result = (AudioFeatureDescription) (super.clone());
		} catch (CloneNotSupportedException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * Gets the base size. Number of coefficients in the static, delta, or double-delta parts (not counting energy)
	 * 
	 * @return the base size
	 */
	public int getBaseSize() {
		int baseSize = featureSize;
		if (getEnergyPresence()) {
			baseSize--;
		}
		if (getDeltaEnergyPresence()) {
			baseSize--;
		}
		if (getDoubleDeltaEnergyPresence()) {
			baseSize--;
		}
		int nbComponents = 0;
		if (getStaticCoeffPresence()) {
			nbComponents++;
		}
		if (getDeltaCoeffPresence()) {
			nbComponents++;
		}
		if (getDoubleDeltaCoeffPresence()) {
			nbComponents++;
		}
		if (nbComponents == 0) {
			return 0;
		} else {
			return baseSize / nbComponents;
		}
	}

	/**
	 * Indicates whether the mean of the distribution of the features was moved to 0.
	 * 
	 * return true if the mean is 0, false otherwise
	 * 
	 * @return the centered
	 */
	public boolean getCentered() {
		return centered;
	}

	/**
	 * Gets the delta coefficient must be computed.
	 * 
	 * @return the delta coefficient must be computed
	 */
	public boolean getDeltaCoeffMustBeComputed() {
		return ((presentParts & DELTA_COEFF_MASK) == 0) && ((neededParts & DELTA_COEFF_MASK) != 0);
	}

	/**
	 * Gets the delta coefficient must be deleted.
	 * 
	 * @return the delta coefficient must be deleted
	 */
	public boolean getDeltaCoeffMustBeDeleted() {
		return ((presentParts & DELTA_COEFF_MASK) != 0) && ((neededParts & DELTA_COEFF_MASK) == 0);
	}

	/**
	 * Gets the delta coefficient needed.
	 * 
	 * @return the delta coefficient needed
	 */
	public boolean getDeltaCoeffNeeded() {
		return (neededParts & DELTA_COEFF_MASK) != 0;
	}

	/**
	 * Gets the delta coefficient presence.
	 * 
	 * @return the delta coefficient presence
	 */
	public boolean getDeltaCoeffPresence() {
		return (presentParts & DELTA_COEFF_MASK) != 0;
	}

	/**
	 * Gets the delta energy must be computed.
	 * 
	 * @return the delta energy must be computed
	 */
	public boolean getDeltaEnergyMustBeComputed() {
		return ((presentParts & DELTAENERGY_MASK) == 0) && ((neededParts & DELTAENERGY_MASK) != 0);
	}

	/**
	 * Gets the delta energy must be deleted.
	 * 
	 * @return the delta energy must be deleted
	 */
	public boolean getDeltaEnergyMustBeDeleted() {
		return ((presentParts & DELTAENERGY_MASK) != 0) && ((neededParts & DELTAENERGY_MASK) == 0);
	}

	/**
	 * Gets the delta energy needed.
	 * 
	 * @return the delta energy needed
	 */
	public boolean getDeltaEnergyNeeded() {
		return (neededParts & DELTAENERGY_MASK) != 0;
	}

	/**
	 * Gets the delta energy presence.
	 * 
	 * @return the delta energy presence
	 */
	public boolean getDeltaEnergyPresence() {
		return (presentParts & DELTAENERGY_MASK) != 0;
	}

	/**
	 * Gets the double delta coefficient must be computed.
	 * 
	 * @return the double delta coefficient must be computed
	 */
	public boolean getDoubleDeltaCoeffMustBeComputed() {
		return ((presentParts & DOUBLEDELTA_COEFF_MASK) == 0) && ((neededParts & DOUBLEDELTA_COEFF_MASK) != 0);
	}

	/**
	 * Gets the double delta coefficient must be deleted.
	 * 
	 * @return the double delta coefficient must be deleted
	 */
	public boolean getDoubleDeltaCoeffMustBeDeleted() {
		return ((presentParts & DOUBLEDELTA_COEFF_MASK) != 0) && ((neededParts & DOUBLEDELTA_COEFF_MASK) == 0);
	}

	/**
	 * Gets the double delta coefficient needed.
	 * 
	 * @return the double delta coefficient needed
	 */
	public boolean getDoubleDeltaCoeffNeeded() {
		return (neededParts & DOUBLEDELTA_COEFF_MASK) != 0;
	}

	/**
	 * Gets the double delta coefficient presence.
	 * 
	 * @return the double delta coefficient presence
	 */
	public boolean getDoubleDeltaCoeffPresence() {
		return (presentParts & DOUBLEDELTA_COEFF_MASK) != 0;
	}

	/**
	 * Gets the double delta energy must be computed.
	 * 
	 * @return the double delta energy must be computed
	 */
	public boolean getDoubleDeltaEnergyMustBeComputed() {
		return ((presentParts & DOUBLEDELTAENERGY_MASK) == 0) && ((neededParts & DOUBLEDELTAENERGY_MASK) != 0);
	}

	/**
	 * Gets the double delta energy must be deleted.
	 * 
	 * @return the double delta energy must be deleted
	 */
	public boolean getDoubleDeltaEnergyMustBeDeleted() {
		return ((presentParts & DOUBLEDELTAENERGY_MASK) != 0) && ((neededParts & DOUBLEDELTAENERGY_MASK) == 0);
	}

	/**
	 * Gets the double delta energy needed.
	 * 
	 * @return the double delta energy needed
	 */
	public boolean getDoubleDeltaEnergyNeeded() {
		return (neededParts & DOUBLEDELTAENERGY_MASK) != 0;
	}

	/* Delta-delta energy */
	/**
	 * Gets the double delta energy presence.
	 * 
	 * @return the double delta energy presence
	 */
	public boolean getDoubleDeltaEnergyPresence() {
		return (presentParts & DOUBLEDELTAENERGY_MASK) != 0;
	}

	/**
	 * Gets the energy must be deleted.
	 * 
	 * @return the energy must be deleted
	 */
	public boolean getEnergyMustBeDeleted() {
		return ((presentParts & ENERGY_MASK) != 0) && ((neededParts & ENERGY_MASK) == 0);
	}

	/**
	 * Gets the energy needed.
	 * 
	 * @return the energy needed
	 */
	public boolean getEnergyNeeded() {
		return (neededParts & ENERGY_MASK) != 0;
	}

	/**
	 * Gets the energy presence.
	 * 
	 * @return the energy presence
	 */
	public boolean getEnergyPresence() {
		return (presentParts & ENERGY_MASK) != 0;
	}

	/**
	 * Gets the index of delta energy.
	 * 
	 * @return the index of delta energy
	 */
	public int getIndexOfDeltaEnergy() {
		if (!getDeltaEnergyPresence()) {
			return -1;
		}
		int result = 0;
		if (getStaticCoeffPresence()) {
			result += getBaseSize();
		}
		if (getEnergyPresence()) {
			result += 1;
		}
		if (getDeltaCoeffPresence()) {
			result += getBaseSize();
		}
		return result;
	}

	/**
	 * Gets the index of double delta energy.
	 * 
	 * @return the index of double delta energy
	 */
	public int getIndexOfDoubleDeltaEnergy() {
		if (!getDoubleDeltaEnergyPresence()) {
			return -1;
		}
		int result = 0;
		if (getStaticCoeffPresence()) {
			result += getBaseSize();
		}
		if (getEnergyPresence()) {
			result += 1;
		}
		if (getDeltaCoeffPresence()) {
			result += getBaseSize();
		}
		if (getDeltaEnergyPresence()) {
			result += 1;
		}
		if (getDoubleDeltaCoeffPresence()) {
			result += getBaseSize();
		}
		return result;
	}

	/**
	 * Gets the index of energy.
	 * 
	 * @return the index of energy
	 */
	public int getIndexOfEnergy() {
		if (!getEnergyPresence()) {
			return -1;
		}
		if (getStaticCoeffPresence()) {
			return getBaseSize();
		} else {
			return 0;
		}
	}

	/**
	 * Gets the index of first delta coefficient.
	 * 
	 * @return the index of first delta coefficient
	 */
	public int getIndexOfFirstDeltaCoeff() {
		if (!getDeltaCoeffPresence()) {
			return -1;
		}
		int result = 0;
		if (getStaticCoeffPresence()) {
			result += getBaseSize();
		}
		if (getEnergyPresence()) {
			result += 1;
		}
		return result;
	}

	/**
	 * Gets the index of first double delta coefficient.
	 * 
	 * @return the index of first double delta coefficient
	 */
	public int getIndexOfFirstDoubleDeltaCoeff() {
		if (!getDoubleDeltaCoeffPresence()) {
			return -1;
		}
		int result = 0;
		if (getStaticCoeffPresence()) {
			result += getBaseSize();
		}
		if (getEnergyPresence()) {
			result += 1;
		}
		if (getDeltaCoeffPresence()) {
			result += getBaseSize();
		}
		if (getDeltaEnergyPresence()) {
			result += 1;
		}
		return result;
	}

	/**
	 * Gets the index of first static coefficient.
	 * 
	 * @return the index of first static coefficient
	 */
	public int getIndexOfFirstStaticCoeff() {
		if (!getStaticCoeffPresence()) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * Gets the normalization method.
	 * 
	 * @return the normalization method
	 */
	public int getNormalizationMethod() {
		return normalizationMethod;
	}

	/**
	 * Gives the window size for feature normalization. If 0, normalization is applied at the global level (segment, cluster, or file). If greater than 0, normalization is applied on a sliding window of this size.
	 * 
	 * @return the size of the normalization window, as a number of frames
	 */
	public int getNormalizationWindowSize() {
		return windowSize;
	}

	/**
	 * Indicates whether the variance of the distribution of the features was normalized to 1.
	 * 
	 * @return true if the variance is 1, false otherwise
	 */
	public boolean getReduced() {
		return reduced;
	}

	/**
	 * Gets the static coefficient must be deleted.
	 * 
	 * @return the static coefficient must be deleted
	 */
	public boolean getStaticCoeffMustBeDeleted() {
		return ((presentParts & STATIC_COEFF_MASK) != 0) && ((neededParts & STATIC_COEFF_MASK) == 0);
	}

	/**
	 * Gets the static coefficient needed.
	 * 
	 * @return the static coefficient needed
	 */
	public boolean getStaticCoeffNeeded() {
		return (neededParts & STATIC_COEFF_MASK) != 0;
	}

	/* Static coefficients */
	/**
	 * Gets the static coefficient presence.
	 * 
	 * @return the static coefficient presence
	 */
	public boolean getStaticCoeffPresence() {
		return (presentParts & STATIC_COEFF_MASK) != 0;
	}

	/**
	 * Gets the trimmed feature descriptor.
	 * 
	 * @return the feature description obtained after computing the missing parts and suppressing the unneeded parts.
	 */
	public AudioFeatureDescription getTrimmedFeatureDesc() {
		AudioFeatureDescription result = (AudioFeatureDescription) (clone());
		if (getDoubleDeltaEnergyMustBeDeleted()) {
			result.setDoubleDeltaEnergyPresence(false);
		} else if (getDoubleDeltaEnergyMustBeComputed()) {
			result.setDoubleDeltaEnergyPresence(true);
		}
		if (getDoubleDeltaCoeffMustBeDeleted()) {
			result.setDoubleDeltaCoeffPresence(false);
		} else if (getDoubleDeltaCoeffMustBeComputed()) {
			result.setDoubleDeltaCoeffPresence(true);
		}
		if (getDeltaEnergyMustBeDeleted()) {
			result.setDeltaEnergyPresence(false);
		} else if (getDeltaEnergyMustBeComputed()) {
			result.setDeltaEnergyPresence(true);
		}
		if (getDeltaCoeffMustBeDeleted()) {
			result.setDeltaCoeffPresence(false);
		} else if (getDeltaCoeffMustBeComputed()) {
			result.setDeltaCoeffPresence(true);
		}
		if (getEnergyMustBeDeleted()) {
			result.setEnergyPresence(false);
		}
		if (getStaticCoeffMustBeDeleted()) {
			result.setStaticCoeffPresence(false);
		}
		return result;
	}

	/**
	 * Gives the size of the feature vectors in the feature file.
	 * 
	 * @return true if the mean is 0, false otherwise
	 * 
	 */
	public int getFeatureSize() {
		return featureSize;
	}

	/**
	 * Sets the centered.
	 * 
	 * @param newValue the new centered
	 */
	public void setCentered(boolean newValue) {
		centered = newValue;
	}

	/**
	 * Sets the delta coefficient needed.
	 * 
	 * @param newValue the new delta coefficient needed
	 */
	public void setDeltaCoeffNeeded(boolean newValue) {
		if (newValue) {
			neededParts |= DELTA_COEFF_MASK;
		} else {
			neededParts &= ~DELTA_COEFF_MASK;
		}
	}

	/**
	 * Sets the delta coefficient presence.
	 * 
	 * @param newValue the new delta coefficient presence
	 */
	public void setDeltaCoeffPresence(boolean newValue) {
		if (newValue) {
			if (!getDeltaCoeffPresence()) {
				featureSize += getBaseSize();
			}
			presentParts |= DELTA_COEFF_MASK;
		} else {
			if (getDeltaCoeffPresence()) {
				featureSize -= getBaseSize();
			}
			presentParts &= ~DELTA_COEFF_MASK;
		}
	}

	/**
	 * Sets the delta energy needed.
	 * 
	 * @param newValue the new delta energy needed
	 */
	public void setDeltaEnergyNeeded(boolean newValue) {
		if (newValue) {
			neededParts |= DELTAENERGY_MASK;
		} else {
			neededParts &= ~DELTAENERGY_MASK;
		}
	}

	/**
	 * Sets the delta energy presence.
	 * 
	 * @param newValue the new delta energy presence
	 */
	public void setDeltaEnergyPresence(boolean newValue) {
		if (newValue) {
			if (!getDeltaEnergyPresence()) {
				featureSize += 1;
			}
			presentParts |= DELTAENERGY_MASK;
		} else {
			if (getDeltaEnergyPresence()) {
				featureSize -= 1;
			}
			presentParts &= ~DELTAENERGY_MASK;
		}
	}

	/**
	 * Sets the double delta coefficient needed.
	 * 
	 * @param newValue the new double delta coefficient needed
	 */
	public void setDoubleDeltaCoeffNeeded(boolean newValue) {
		if (newValue) {
			neededParts |= DOUBLEDELTA_COEFF_MASK;
		} else {
			neededParts &= ~DOUBLEDELTA_COEFF_MASK;
		}
	}

	/**
	 * Sets the double delta coefficient presence.
	 * 
	 * @param newValue the new double delta coefficient presence
	 */
	public void setDoubleDeltaCoeffPresence(boolean newValue) {
		if (newValue) {
			if (!getDoubleDeltaCoeffPresence()) {
				featureSize += getBaseSize();
			}
			presentParts |= DOUBLEDELTA_COEFF_MASK;
		} else {
			if (getDoubleDeltaCoeffPresence()) {
				featureSize -= getBaseSize();
			}
			presentParts &= ~DOUBLEDELTA_COEFF_MASK;
		}
	}

	/**
	 * Sets the double delta energy needed.
	 * 
	 * @param newValue the new double delta energy needed
	 */
	public void setDoubleDeltaEnergyNeeded(boolean newValue) {
		if (newValue) {
			neededParts |= DOUBLEDELTAENERGY_MASK;
		} else {
			neededParts &= ~DOUBLEDELTAENERGY_MASK;
		}
	}

	/**
	 * Sets the double delta energy presence.
	 * 
	 * @param newValue the new double delta energy presence
	 */
	public void setDoubleDeltaEnergyPresence(boolean newValue) {
		if (newValue) {
			if (!getDoubleDeltaEnergyPresence()) {
				featureSize += 1;
			}
			presentParts |= DOUBLEDELTAENERGY_MASK;
		} else {
			if (getDoubleDeltaEnergyPresence()) {
				featureSize -= 1;
			}
			presentParts &= ~DOUBLEDELTAENERGY_MASK;
		}
	}

	/**
	 * Sets the energy needed.
	 * 
	 * @param newValue the new energy needed
	 */
	public void setEnergyNeeded(boolean newValue) {
		if (newValue) {
			neededParts |= ENERGY_MASK;
		} else {
			neededParts &= ~ENERGY_MASK;
		}
	}

	/**
	 * Sets the energy presence.
	 * 
	 * @param newValue the new energy presence
	 */
	public void setEnergyPresence(boolean newValue) {
		if (newValue) {
			if (!getEnergyPresence()) {
				featureSize += 1;
			}
			presentParts |= ENERGY_MASK;
		} else {
			if (getEnergyPresence()) {
				featureSize -= 1;
			}
			presentParts &= ~ENERGY_MASK;
		}
	}

	/**
	 * Sets the normalization method.
	 * 
	 * @param normBy the new normalization method
	 */
	public void setNormalizationMethod(int normBy) {
		normalizationMethod = normBy;
	}

	/**
	 * Sets the normalization window size.
	 * 
	 * @param newSize the new normalization window size
	 */
	public void setNormalizationWindowSize(int newSize) {
		windowSize = newSize;
	}

	/**
	 * Sets the reduced.
	 * 
	 * @param newValue the new reduced
	 */
	public void setReduced(boolean newValue) {
		reduced = newValue;
	}

	/**
	 * Sets the static coefficient needed.
	 * 
	 * @param newValue the new static coefficient needed
	 */
	public void setStaticCoeffNeeded(boolean newValue) {
		if (newValue) {
			neededParts |= STATIC_COEFF_MASK;
		} else {
			neededParts &= ~STATIC_COEFF_MASK;
		}
	}

	/**
	 * Sets the static coefficient presence.
	 * 
	 * @param newValue the new static coefficient presence
	 */
	public void setStaticCoeffPresence(boolean newValue) {
		if (newValue) {
			if (!getStaticCoeffPresence()) {
				featureSize += getBaseSize();
			}
			presentParts |= STATIC_COEFF_MASK;
		} else {
			if (getStaticCoeffPresence()) {
				featureSize -= getBaseSize();
			}
			presentParts &= ~STATIC_COEFF_MASK;
		}
	}

	/**
	 * Sets the vector size.
	 * 
	 * @param newSize the new vector size
	 */
	public void setFeatureSize(int newSize) {
		featureSize = newSize;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "";
		result = result + "Vector size: " + getFeatureSize() + "\n";
		result = result + "Base size: " + getBaseSize() + "\n";
		result = result + "Static: " + (getStaticCoeffPresence() ? "present" : "not present") + ", "
				+ (getStaticCoeffNeeded() ? "needed" : "not needed") + " => "
				+ (getStaticCoeffMustBeDeleted() ? "deleted" : "not deleted") + "\n";
		result = result + "Energy: " + (getEnergyPresence() ? "present" : "not present") + ", "
				+ (getEnergyNeeded() ? "needed" : "not needed") + " => "
				+ (getEnergyMustBeDeleted() ? "deleted" : "not deleted") + "\n";
		result = result + "Delta Coeff: " + (getDeltaCoeffPresence() ? "present" : "not present") + ", "
				+ (getDeltaCoeffNeeded() ? "needed" : "not needed") + " => "
				+ (getDeltaCoeffMustBeComputed() ? "computed" : "not computed") + ", "
				+ (getDeltaCoeffMustBeDeleted() ? "deleted" : "not deleted") + "\n";
		result = result + "Delta Energy: " + (getDeltaEnergyPresence() ? "present" : "not present") + ", "
				+ (getDeltaEnergyNeeded() ? "needed" : "not needed") + " => "
				+ (getDeltaEnergyMustBeComputed() ? "computed" : "not computed") + ", "
				+ (getDeltaEnergyMustBeDeleted() ? "deleted" : "not deleted") + "\n";
		result = result + "Double Delta Coeff: " + (getDoubleDeltaCoeffPresence() ? "present" : "not present") + ", "
				+ (getDoubleDeltaCoeffNeeded() ? "needed" : "not needed") + " => "
				+ (getDoubleDeltaCoeffMustBeComputed() ? "computed" : "not computed") + ", "
				+ (getDoubleDeltaCoeffMustBeDeleted() ? "deleted" : "not deleted") + "\n";
		result = result + "Double Delta Energy: " + (getDoubleDeltaEnergyPresence() ? "present" : "not present") + ", "
				+ (getDoubleDeltaEnergyNeeded() ? "needed" : "not needed") + " => "
				+ (getDoubleDeltaEnergyMustBeComputed() ? "computed" : "not computed") + ", "
				+ (getDoubleDeltaEnergyMustBeDeleted() ? "deleted" : "not deleted") + "\n";
		result = result + "Centered: " + (getCentered() ? "yes" : "no") + "\n";
		result = result + "Reduced: " + (getReduced() ? "yes" : "no") + "\n";
		result = result + "Window Size: " + getNormalizationWindowSize() + "\n";
		result = result + "Normalization method: " + getNormalizationMethod() + "\n";
		return result;
	}

	/**
	 * Sets the features format.
	 * 
	 * @param featuresFormat the new features format
	 */
	public void setFeaturesFormat(int featuresFormat) {
		this.featuresFormat = featuresFormat;
	}

	/**
	 * Gets the features format.
	 * 
	 * @return the features format
	 */
	public int getFeaturesFormat() {
		return featuresFormat;
	}

	/**
	 * Gets the features format as string.
	 * 
	 * @return the features format as string
	 */
	public String getFeaturesFormatAsString() {
		return ParameterAudioFeature.AudioFeaturesTypeString[featuresFormat];
	}

	/**
	 * Gets the delta format.
	 * 
	 * @return the delta format
	 */
	public int getDeltaFormat() {
		return this.deltaFormat;
	}

	/**
	 * Sets the delta format.
	 * 
	 * @param deltaFormat the new delta format
	 */
	public void setDeltaFormat(int deltaFormat) {
		this.deltaFormat = deltaFormat;
	}

}
