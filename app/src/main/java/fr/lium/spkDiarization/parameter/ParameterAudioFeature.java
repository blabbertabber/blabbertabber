/**
 * 
 * <p>
 * ParameterFeature
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
 *          not more use
 */

package fr.lium.spkDiarization.parameter;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import fr.lium.spkDiarization.libFeature.AudioFeatureDescription;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;

/**
 * The Class ParameterAudioFeature.
 */
public abstract class ParameterAudioFeature extends ParameterBase implements Cloneable {

	/** The Constant AudioFeaturesTypeString. */
	public final static String[] AudioFeaturesTypeString = { "spro4", "htk", "sphinx", "gztxt",
			"featureSetTransformation", "audio8kHz2sphinx", "audio16kHz2sphinx", "audio22kHz2sphinx",
			"audio44kHz2sphinx", "audio48kHz2sphinx" };

	/** The Constant DeltaTypeString. */
	public final static String[] DeltaTypeString = { "spro4", "htk", "sphinx" };

	/** The feature description. */
	private AudioFeatureDescription featureDescription;

	/** The features desc string. */
	protected String featuresDescString; // Feature description in a string

	/**
	 * The Class ActionFeaturesDescString.
	 */
	protected class ActionFeaturesDescString extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setFeaturesDescription(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String message = "--" + longOpt.getName() + " \t " + type
					+ " featureSet: description (type[:deltatype][,s:e:ds:de:dds:dde,dim,c:r:wSize:method]) = ";
			message += AudioFeaturesTypeString[featureDescription.getFeaturesFormat()] + ":"
					+ DeltaTypeString[featureDescription.getDeltaFormat()] + ",";
			message += paramValueFromComponentPresenceAndNeed(featureDescription.getStaticCoeffPresence(), featureDescription.getStaticCoeffNeeded())
					+ ":";
			message += paramValueFromComponentPresenceAndNeed(featureDescription.getEnergyPresence(), featureDescription.getEnergyNeeded())
					+ ":";
			message += paramValueFromComponentPresenceAndNeed(featureDescription.getDeltaCoeffPresence(), featureDescription.getDeltaCoeffNeeded())
					+ ":";
			message += paramValueFromComponentPresenceAndNeed(featureDescription.getDeltaEnergyPresence(), featureDescription.getDeltaEnergyNeeded())
					+ ":";
			message += paramValueFromComponentPresenceAndNeed(featureDescription.getDoubleDeltaCoeffPresence(), featureDescription.getDoubleDeltaCoeffNeeded())
					+ ":";
			message += paramValueFromComponentPresenceAndNeed(featureDescription.getDoubleDeltaEnergyPresence(), featureDescription.getDoubleDeltaEnergyNeeded())
					+ ",";
			message += featureDescription.getFeatureSize() + ",";
			message += (featureDescription.getCentered() ? 1 : 0) + ":";
			message += (featureDescription.getReduced() ? 1 : 0) + ":";
			message += featureDescription.getNormalizationWindowSize() + ":";
			message += featureDescription.getNormalizationMethod();
			logger.config(message + " [" + logger.getName() + "]");

			message = "\t \t type ";
			message += formatStrigArray(AudioFeaturesTypeString) + " = ";
			message += AudioFeaturesTypeString[featureDescription.getFeaturesFormat()];
			message += " (" + featureDescription.getFeaturesFormat() + ")";
			logger.config(message);

			message = "\t \t deltaType ";
			message += formatStrigArray(DeltaTypeString) + " = ";
			message += DeltaTypeString[featureDescription.getDeltaFormat()];
			message += " (" + featureDescription.getDeltaFormat() + ")";
			logger.config(message);

			logger.config("\t \t static [0=not present,1=present ,3=to be removed] = "
					+ paramValueFromComponentPresenceAndNeed(featureDescription.getStaticCoeffPresence(), featureDescription.getStaticCoeffNeeded()));
			logger.config("\t \t energy [0,1,3] = "
					+ paramValueFromComponentPresenceAndNeed(featureDescription.getEnergyPresence(), featureDescription.getEnergyNeeded()));
			logger.config("\t \t delta [0,1,2=computed on the fly,3] = "
					+ paramValueFromComponentPresenceAndNeed(featureDescription.getDeltaCoeffPresence(), featureDescription.getDeltaCoeffNeeded()));
			logger.config("\t \t delta energy [0,1,2=computed on the fly,3] = "
					+ paramValueFromComponentPresenceAndNeed(featureDescription.getDeltaEnergyPresence(), featureDescription.getDeltaEnergyNeeded()));
			logger.config("\t \t delta delta [0,1,2,3] = "
					+ paramValueFromComponentPresenceAndNeed(featureDescription.getDoubleDeltaCoeffPresence(), featureDescription.getDoubleDeltaCoeffNeeded()));
			logger.config("\t \t delta delta energy [0,1,2,3] = "
					+ paramValueFromComponentPresenceAndNeed(featureDescription.getDoubleDeltaEnergyPresence(), featureDescription.getDoubleDeltaEnergyNeeded()));
			logger.config(" \t \t file dim = " + featureDescription.getFeatureSize());
			logger.config(" \t \t normalization, center [0,1] = " + (featureDescription.getCentered() ? 1 : 0));
			logger.config(" \t \t normalization, reduce [0,1] = " + (featureDescription.getReduced() ? 1 : 0));
			logger.config(" \t \t normalization, window size = " + featureDescription.getNormalizationWindowSize());
			message = "\t \t normalization, method [";
			message += AudioFeatureDescription.NORM_BY_SEGMENT + " ("
					+ AudioFeatureDescription.NORMALIZE_METHOD_STR[AudioFeatureDescription.NORM_BY_SEGMENT] + "), ";
			message += AudioFeatureDescription.NORM_BY_CLUSTER + " ("
					+ AudioFeatureDescription.NORMALIZE_METHOD_STR[AudioFeatureDescription.NORM_BY_CLUSTER] + "), ";
			message += AudioFeatureDescription.NORM_BY_SLIDING + " ("
					+ AudioFeatureDescription.NORMALIZE_METHOD_STR[AudioFeatureDescription.NORM_BY_SLIDING] + "), ";
			message += AudioFeatureDescription.NORM_BY_WARPING + " ("
					+ AudioFeatureDescription.NORMALIZE_METHOD_STR[AudioFeatureDescription.NORM_BY_WARPING] + ")] =";
			message += featureDescription.getNormalizationMethod();
			logger.config(message);
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

	/** The memory occupation rate. */
	protected double memoryOccupationRate;

	/**
	 * The Class ActionMemoryOccupationRate.
	 */
	protected class ActionMemoryOccupationRate extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setMemoryOccupationRate(Double.valueOf(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t " + type
					+ " featureSet: memory occupation rate of the feature in the java virtual machine = "
					+ getFeatureMask() + " [" + logger.getName() + "]");
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

	/** The feature mask. */
	protected String featureMask;

	/**
	 * The Class ActionFeatureMask.
	 */
	protected class ActionFeatureMask extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setFeatureMask(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t " + type + " featureSet: mask = " + getFeatureMask() + " ["
					+ logger.getName() + "]");
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

	// Type of distance for segmentation.
	/**
	 * The Enum SpeechDetectorMethod.
	 */
	public enum SpeechDetectorMethod {

		/** The speech on energy. */
		SPEECH_ON_ENERGY,
		/** The speech on bigaussian. */
		SPEECH_ON_BIGAUSSIAN,
		/** The speech none. */
		SPEECH_NONE
	};

	/** The speech detector method string. */
	public final String[] speechDetectorMethodString = { "E", "BIGAUSSIAN", "None" };

	/** The speech method. */
	protected SpeechDetectorMethod speechMethod; // Segmentation method.

	/** The speech method string. */
	protected String speechMethodString; // Segmentation method.

	/**
	 * The Class ActionSpeechMethod.
	 */
	protected class ActionSpeechMethod extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setSpeechMethod(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			String msg = "--" + longOpt.getName() + " \t " + type + " featureSet: silence segmentation method ";
			msg += formatStrigArray(speechDetectorMethodString) + " = ";
			msg += speechDetectorMethodString[getSpeechMethod().ordinal()] + "(" + getSpeechMethod().ordinal() + ")";
			logger.config(msg + " [" + logger.getName() + "]");
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

	/** The speech threshold. */
	protected double speechThreshold; // Segmentation threshold.

	/**
	 * The Class ActionSpeechThreshold.
	 */
	protected class ActionSpeechThreshold extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			setSpeechThreshold(Double.parseDouble(optarg));
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#log(java.util.logging.Logger, fr.lium.spkDiarization.parameter.LongOptWithAction)
		 */
		@Override
		public void log(Logger logger, LongOptWithAction longOpt) {
			logger.config("--" + longOpt.getName() + " \t " + type + " featureSet: speech detector threshold = "
					+ getFeatureMask() + " [" + logger.getName() + "]");
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

	/**
	 * Instantiates a new parameter audio feature.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterAudioFeature(Parameter parameter) {
		super(parameter);
		featureMask = "";
		featureDescription = new AudioFeatureDescription();
		featuresDescString = AudioFeaturesTypeString[AudioFeatureSet.AUDIO16Khz2SPHINXMFCC] + ":"
				+ DeltaTypeString[AudioFeatureSet.SPHINX] + ",1:1:0:0:0:0,13,0:0:0:0";
		setFeaturesDescription(featuresDescString);
		setMemoryOccupationRate(0.8);
		setSpeechMethod(speechDetectorMethodString[2]);
		setSpeechThreshold(0);
	}

	/**
	 * Gets the memory occupation rate.
	 * 
	 * @return the memoryOccupationRate
	 */
	public double getMemoryOccupationRate() {
		return memoryOccupationRate;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected ParameterAudioFeature clone() throws CloneNotSupportedException {
		ParameterAudioFeature result = (ParameterAudioFeature) super.clone();
		result.featureDescription = (AudioFeatureDescription) featureDescription.clone();
		return result;
	}

	/**
	 * Sets the memory occupation rate.
	 * 
	 * @param memoryOccupationRate the memoryOccupationRate to set
	 */
	public void setMemoryOccupationRate(double memoryOccupationRate) {
		this.memoryOccupationRate = memoryOccupationRate;
	}

	/** The type. */
	private String type;

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	protected String getType() {
		return type;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type the new type
	 */
	protected void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the feature mask.
	 * 
	 * @return the feature mask
	 */
	public String getFeatureMask() {
		return featureMask;
	}

	/**
	 * Sets the feature mask.
	 * 
	 * @param featuresInputMask the new feature mask
	 */
	public void setFeatureMask(String featuresInputMask) {
		this.featureMask = featuresInputMask;
	}

	/**
	 * Gets the features description.
	 * 
	 * @return the features description
	 */
	public AudioFeatureDescription getFeaturesDescription() {
		return featureDescription;
	}

	/**
	 * Sets the features description.
	 * 
	 * @param optarg the new features description
	 */
	public void setFeaturesDescription(String optarg) {
		featuresDescString = optarg;
		String inputType = "";
		String deltaType = new String(DeltaTypeString[AudioFeatureSet.SPHINX]);
		int i;
		int s;
		int e;
		int ds;
		int dds;
		int de;
		int dde;
		int fDim;
		int nb;
		int fc;
		int fr;
		int fwsize;
		int normMethod = 0;
		i = s = e = ds = dds = de = dde = fDim = nb = fc = fr = fwsize = 0;

		StringTokenizer strTok = new StringTokenizer(optarg, ",");
		int cpt = 0;
		nb = 0;
		while (strTok.hasMoreTokens()) {
			String token = strTok.nextToken();
			if (cpt == 0) {
				StringTokenizer stok2 = new StringTokenizer(token, ":");
				int cpt2 = 0;
				while (stok2.hasMoreTokens()) {
					if (cpt2 == 0) {
						inputType = new String(stok2.nextToken());
						nb++;
					} else if (cpt2 == 1) {
						deltaType = new String(stok2.nextToken());
						nb++;
					}
					cpt2++;
				}
			} else if (cpt == 1) {
				StringTokenizer stok2 = new StringTokenizer(token, ":");
				int cpt2 = 0;
				while (stok2.hasMoreTokens()) {
					if (cpt2 == 0) {
						s = Integer.parseInt(stok2.nextToken());
						nb++;
					} else if (cpt2 == 1) {
						e = Integer.parseInt(stok2.nextToken());
						nb++;
					} else if (cpt2 == 2) {
						ds = Integer.parseInt(stok2.nextToken());
						nb++;
					} else if (cpt2 == 3) {
						de = Integer.parseInt(stok2.nextToken());
						nb++;
					} else if (cpt2 == 4) {
						dds = Integer.parseInt(stok2.nextToken());
						nb++;
					} else if (cpt2 == 5) {
						dde = Integer.parseInt(stok2.nextToken());
						nb++;
					}
					cpt2++;
				}
			} else if (cpt == 2) {
				fDim = Integer.parseInt(token);
				nb++;
			} else if (cpt == 3) {
				StringTokenizer strTok2 = new StringTokenizer(token, ":");
				int cpt2 = 0;
				while (strTok2.hasMoreTokens()) {
					if (cpt2 == 0) {
						fc = Integer.parseInt(strTok2.nextToken());
						nb++;
					} else if (cpt2 == 1) {
						fr = Integer.parseInt(strTok2.nextToken());
						nb++;
					} else if (cpt2 == 2) {
						fwsize = Integer.parseInt(strTok2.nextToken());
						nb++;
					} else if (cpt2 == 3) {
						normMethod = Integer.parseInt(strTok2.nextToken());
						nb++;
					}
					cpt2++;
				}
			}
			cpt++;
		}
		if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.SPRO4])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.SPRO4);
		} else if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.HTK])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.HTK);
		} else if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.SPHINX])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.SPHINX);
		} else if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.GZTXT])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.GZTXT);
		} else if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.AUDIO8kHz2SPHINXMFCC])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.AUDIO8kHz2SPHINXMFCC);
		} else if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.AUDIO16Khz2SPHINXMFCC])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.AUDIO16Khz2SPHINXMFCC);
		} else if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.AUDIO22kHz2SPHINXMFCC])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.AUDIO22kHz2SPHINXMFCC);
		} else if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.AUDIO44kHz2SPHINXMFCC])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.AUDIO44kHz2SPHINXMFCC);
		} else if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.AUDIO48kHz2SPHINXMFCC])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.AUDIO48kHz2SPHINXMFCC);
		} else if (inputType.equals(AudioFeaturesTypeString[AudioFeatureSet.FEATURESETTRANSFORMATION])) {
			featureDescription.setFeaturesFormat(AudioFeatureSet.FEATURESETTRANSFORMATION);
		}

		if (deltaType.equals(DeltaTypeString[AudioFeatureSet.SPRO4])) {
			featureDescription.setDeltaFormat(AudioFeatureSet.SPRO4);
		} else if (deltaType.equals(DeltaTypeString[AudioFeatureSet.HTK])) {
			featureDescription.setDeltaFormat(AudioFeatureSet.HTK);
		} else if (deltaType.equals(DeltaTypeString[AudioFeatureSet.SPHINX])) {
			featureDescription.setDeltaFormat(AudioFeatureSet.SPHINX);
		}

		i = 1;
		if (nb > i++) {
			featureDescription.setStaticCoeffPresence((s == 1) || (s == 3));
			featureDescription.setStaticCoeffNeeded((s == 1) || (s == 2));
		}
		if (nb > i++) {
			featureDescription.setEnergyPresence((e == 1) || (e == 3));
			featureDescription.setEnergyNeeded((e == 1) || (e == 2));
		}
		if (nb > i++) {
			featureDescription.setDeltaCoeffPresence((ds == 1) || (ds == 3));
			featureDescription.setDeltaCoeffNeeded((ds == 1) || (ds == 2));
		}
		if (nb > i++) {
			featureDescription.setDeltaEnergyPresence((de == 1) || (de == 3));
			featureDescription.setDeltaEnergyNeeded((de == 1) || (de == 2));
		}
		if (nb > i++) {
			featureDescription.setDoubleDeltaCoeffPresence((dds == 1) || (dds == 3));
			featureDescription.setDoubleDeltaCoeffNeeded((dds == 1) || (dds == 2));
		}
		if (nb > i++) {
			featureDescription.setDoubleDeltaEnergyPresence((dde == 1) || (dde == 3));
			featureDescription.setDoubleDeltaEnergyNeeded((dde == 1) || (dde == 2));
		}
		if (nb > i++) {
			featureDescription.setFeatureSize(fDim);
		}
		if (nb > i++) {
			featureDescription.setCentered(fc == 1);
		}
		if (nb > i++) {
			featureDescription.setReduced(fr == 1);
		}
		if (nb > i++) {
			featureDescription.setNormalizationWindowSize(fwsize);
		}
		if (nb > i++) {
			featureDescription.setNormalizationMethod(normMethod);
		}
	}

	/**
	 * Param value from component presence and need.
	 * 
	 * @param isComponentPresent the is component present
	 * @param isComponentNeeded the is component needed
	 * @return the int
	 */
	private int paramValueFromComponentPresenceAndNeed(boolean isComponentPresent, boolean isComponentNeeded) {
		if ((!isComponentPresent) && (!isComponentNeeded)) {
			return 0;
		}
		if ((isComponentPresent) && (isComponentNeeded)) {
			return 1;
		}
		if ((!isComponentPresent) && (isComponentNeeded)) {
			return 2;
		}
		if ((isComponentPresent) && (!isComponentNeeded)) {
			return 3;
		}
		return -1; // Never reached, this return is there just to prevent the
		// compiler from telling us "missing return statement"
	}

	/**
	 * Gets the features descriptor as string.
	 * 
	 * @return the features descriptor as string
	 */
	public String getFeaturesDescriptorAsString() {
		return featuresDescString;
	}

	/**
	 * Sets the features descriptor string.
	 * 
	 * @param featuresDescString the new features descriptor string
	 */
	public void setFeaturesDescriptorString(String featuresDescString) {
		this.featuresDescString = featuresDescString;
	}

	/**
	 * Sets the speech threshold.
	 * 
	 * @param segThr the new speech threshold
	 */
	public void setSpeechThreshold(double segThr) {
		this.speechThreshold = segThr;
	}

	/**
	 * Gets the speech threshold.
	 * 
	 * @return the speech threshold
	 */
	public double getSpeechThreshold() {
		return speechThreshold;
	}

	/**
	 * Gets the speech method.
	 * 
	 * @return the speech method
	 */
	public SpeechDetectorMethod getSpeechMethod() {
		return speechMethod;
	}

	/**
	 * Sets the speech method.
	 * 
	 * @param ch the new speech method
	 */
	public void setSpeechMethod(String ch) {
		speechMethodString = ch;

		if (ch.equals(speechDetectorMethodString[SpeechDetectorMethod.SPEECH_ON_ENERGY.ordinal()])) {
			speechMethod = SpeechDetectorMethod.SPEECH_ON_ENERGY;
		} else if (ch.equals(speechDetectorMethodString[SpeechDetectorMethod.SPEECH_ON_BIGAUSSIAN.ordinal()])) {
			speechMethod = SpeechDetectorMethod.SPEECH_ON_BIGAUSSIAN;
		} else if (ch.equals(speechDetectorMethodString[SpeechDetectorMethod.SPEECH_NONE.ordinal()])) {
			speechMethod = SpeechDetectorMethod.SPEECH_NONE;
		}
	}

	/**
	 * Use speech detection.
	 * 
	 * @return true, if successful
	 */
	public boolean useSpeechDetection() {
		return (getSpeechThreshold() > 0);
	}

	/**
	 * Gets the speech method as string.
	 * 
	 * @return the speech method as string
	 */
	public String getSpeechMethodAsString() {
		// TODO Auto-generated method stub
		return speechDetectorMethodString[getSpeechMethod().ordinal()];
	}
}