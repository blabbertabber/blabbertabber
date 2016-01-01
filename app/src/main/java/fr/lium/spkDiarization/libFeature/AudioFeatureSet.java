/**
 *
 * <p>
 * FeaturesSet
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain
 * Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 *
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is
 * subject to license terms.
 *
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package fr.lium.spkDiarization.libFeature;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.lib.StringListFileIO;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.ParameterAudioFeature;
import fr.lium.spkDiarization.parameter.ParameterAudioFeature.SpeechDetectorMethod;

// TODO: Auto-generated Javadoc
/**
 * The Class FeatureSet.
 */
public class AudioFeatureSet implements Cloneable {

	/**
	 * The Constant logger.
	 */
	private final static Logger logger = Logger.getLogger(AudioFeatureSet.class.getName());
	/**
	 * Constants for file types.
	 */
	public static final int SPRO4 = 0;
	/**
	 * The Constant HTK.
	 */
	public static final int HTK = 1;
	/**
	 * The Constant SPHINX.
	 */
	public static final int SPHINX = 2;
	/**
	 * The Constant GZTXT.
	 */
	public static final int GZTXT = 3;
	/**
	 * The Constant FEATURESETTRANSFORMATION.
	 */
	public static final int FEATURESETTRANSFORMATION = 4;
	/**
	 * The Constant AUDIO8Khz2SPHINX.
	 */
	public static final int AUDIO8kHz2SPHINXMFCC = 5;
	/**
	 * The Constant AUDIO16Khz2SPHINX.
	 */
	public static final int AUDIO16Khz2SPHINXMFCC = 6;
	/**
	 * The Constant AUDIO8Khz2SPHINX.
	 */
	public static final int AUDIO22kHz2SPHINXMFCC = 7;
	/**
	 * The Constant AUDIO8Khz2SPHINX.
	 */
	public static final int AUDIO44kHz2SPHINXMFCC = 8;
	/**
	 * The Constant AUDIO8Khz2SPHINX.
	 */
	public static final int AUDIO48kHz2SPHINXMFCC = 9;
	/**
	 * Constant to use when the current show is not set.
	 */
	public static final String UNKNOWN_SHOW = "UNKNOWN_SHOW";
	/**
	 * Constant for delta computation.
	 */
	public static final int FOLLOW_INPUT_FILE_TYPE = -1;
	/**
	 * Constants for Sphinx-style delta computation.
	 */
	private static final int SPHINX_DELTA_WINDOW_SIZE = 2;
	/**
	 * The Constant SPHINX_DOUBLE_DELTA_WINDOW_SIZE.
	 */
	private static final int SPHINX_DOUBLE_DELTA_WINDOW_SIZE = 1;
	/**
	 * The fontend config url.
	 */
	protected URL fontend48kHzConfigURL;
	/**
	 * The fontend44k hz config url.
	 */
	protected URL fontend44kHzConfigURL;
	/**
	 * The fontend22k hz config url.
	 */
	protected URL fontend22kHzConfigURL;
	/**
	 * The fontend16k hz config url.
	 */
	protected URL fontend16kHzConfigURL;
	/**
	 * The fontend8k hz config url.
	 */
	protected URL fontend8kHzConfigURL;
	/**
	 * The initial desc.
	 */
	protected AudioFeatureDescription initialDesc; // description of the features
	/**
	 * The current file desc.
	 */
	protected AudioFeatureDescription currentFileDesc; // description of the features in the
	// current file, after a read
	/**
	 * The data.
	 */
	protected AudioFeatureList currentFeatureList;// features
	/**
	 * The feature list map.
	 */
	protected TreeMap<String, AudioFeatureList> featureListMap;
	/**
	 * The filename mask.
	 */
	protected String filenameMask; // filename mask, some think like %s.cep
	/**
	 * The current filename.
	 */
	protected String currentFilename; // the current loaded file
	/**
	 * The current show.
	 */
	protected String currentShow; // index of the current show
// protected int inputType; // !< type of features
// protected int outputType; // !< type of features
	/**
	 * The size in memory.
	 */
	protected long sizeInMemory = 0;
	/**
	 * algorithm to use to compute delta and double delta; it can either be
	 * decided by the input file type, or be set to Spro or Sphinx (using the
	 * file type constants).
	 */
	protected int deltaType;
	/**
	 * The cluster set.
	 */
	protected ClusterSet clusterSet;
	/**
	 * The UBMs.
	 */
	protected GMMArrayList ubmList;
	/**
	 * used when a FeatureSet is created by copying the data of an other
	 * FeatureSet.
	 */
	protected AudioFeatureSet source;
	/**
	 * The swap.
	 */
	protected boolean swap;
	/**
	 * The change position of energy.
	 */
	protected boolean changePositionOfEnergy = true;
	/**
	 * The memory occupation rate.
	 */
	protected double memoryOccupationRate = 0.5;
	/**
	 * The speech threshold.
	 */
	protected double speechThreshold; // Segmentation threshold.
	/**
	 * The speech method.
	 */
	protected SpeechDetectorMethod speechMethod; // Segmentation method.

	
	
	/**
	 * **********************************************************************************.
	 *
	 * @param clusterSet the cluster set
	 * @param parameterInputFeature the parameter input feature
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	/**
	 * **********************************************************************************
	 */
	public AudioFeatureSet(ClusterSet clusterSet, ParameterAudioFeature parameterInputFeature) throws IOException, DiarizationException {
		fontend48kHzConfigURL = getClass().getResource("frontend.config.48kHz.xml");
		fontend44kHzConfigURL = getClass().getResource("frontend.config.44kHz.xml");
		fontend22kHzConfigURL = getClass().getResource("frontend.config.22kHz.xml");
		fontend16kHzConfigURL = getClass().getResource("frontend.config.16kHz.xml");
		fontend8kHzConfigURL = getClass().getResource("frontend.config.8kHz.xml");

		this.clusterSet = clusterSet;
		currentFeatureList = null;
		featureListMap = new TreeMap<String, AudioFeatureList>();
// inputType = inputFileType;
// outputType = outputFileType;
		filenameMask = new String(parameterInputFeature.getFeatureMask());
		currentFilename = "";
		currentShow = UNKNOWN_SHOW;
		initialDesc = (AudioFeatureDescription) parameterInputFeature.getFeaturesDescription().clone();
		currentFileDesc = null;
		/*
		 * deltaType = FOLLOW_INPUT_FILE_TYPE;// !!!:Teva:20071206 Temporary, until // an argument is added to the // method if (deltaType == FOLLOW_INPUT_FILE_TYPE) { deltaType = initialDesc.getFeaturesFormat(); }
		 */
		deltaType = initialDesc.getDeltaFormat();
		memoryOccupationRate = parameterInputFeature.getMemoryOccupationRate();
		speechMethod = parameterInputFeature.getSpeechMethod();
		speechThreshold = parameterInputFeature.getSpeechThreshold();
		
		clusterSet.getShowNames().size();
	}

	/**
	 * Instantiates a new feature set.
	 *
	 * @param features the features
	 * @param clusterSet the clusters
	 * @param parameterInputFeature the parameter input feature
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public AudioFeatureSet(AudioFeatureSet features, ClusterSet clusterSet, ParameterAudioFeature parameterInputFeature) throws IOException, DiarizationException {
		fontend48kHzConfigURL = getClass().getResource("frontend.config.48kHz.xml");
		fontend44kHzConfigURL = getClass().getResource("frontend.config.44kHz.xml");
		fontend22kHzConfigURL = getClass().getResource("frontend.config.22kHz.xml");
		fontend16kHzConfigURL = getClass().getResource("frontend.config.16kHz.xml");
		fontend8kHzConfigURL = getClass().getResource("frontend.config.8kHz.xml");
		source = features;
		this.clusterSet = clusterSet;
		currentFeatureList = null;
		featureListMap = new TreeMap<String, AudioFeatureList>();
		filenameMask = new String(parameterInputFeature.getFeatureMask());
		currentFilename = "";
		currentShow = UNKNOWN_SHOW;
		initialDesc = (AudioFeatureDescription) parameterInputFeature.getFeaturesDescription().clone();
		currentFileDesc = null;
		/*
		 * deltaType = FOLLOW_INPUT_FILE_TYPE;// !!!:Teva:20071206 Temporary, until // an argument is added to the // method if (deltaType == FOLLOW_INPUT_FILE_TYPE) { deltaType = features.deltaType; }
		 */
		deltaType = initialDesc.getDeltaFormat();

		memoryOccupationRate = parameterInputFeature.getMemoryOccupationRate();
		speechMethod = parameterInputFeature.getSpeechMethod();
		speechThreshold = parameterInputFeature.getSpeechThreshold();
	}

	/**
	 * Constructor.
	 *
	 * @param initialCapacity specifies path and extension for the shows
	 * @param description description of the features
	 */
	public AudioFeatureSet(int initialCapacity, AudioFeatureDescription description) {
		fontend48kHzConfigURL = getClass().getResource("frontend.config.48kHz.xml");
		fontend44kHzConfigURL = getClass().getResource("frontend.config.44kHz.xml");
		fontend22kHzConfigURL = getClass().getResource("frontend.config.22kHz.xml");
		fontend16kHzConfigURL = getClass().getResource("frontend.config.16kHz.xml");
		fontend8kHzConfigURL = getClass().getResource("frontend.config.8kHz.xml");
		initialDesc = (AudioFeatureDescription) description.clone();
		currentFileDesc = description.getTrimmedFeatureDesc();
		currentFilename = "";
		currentShow = UNKNOWN_SHOW;
		currentFeatureList = new AudioFeatureList(initialCapacity);
		featureListMap = new TreeMap<String, AudioFeatureList>();
		featureListMap.put(currentShow, currentFeatureList);
// inputType = outputFileType;
// outputType = outputFileType;
/*
		 * deltaType = FOLLOW_INPUT_FILE_TYPE;// !!!:Teva:20071206 Temporary, until // an argument is added to the // method if (deltaType == FOLLOW_INPUT_FILE_TYPE) { deltaType = description.getFeaturesFormat(); }
		 */
		deltaType = initialDesc.getDeltaFormat();

		speechMethod = SpeechDetectorMethod.SPEECH_ON_ENERGY;
		speechThreshold = 0;
	}

	/**
	 * Add a frame at the end of the feature set.
	 *
	 * @param feature the values
	 *
	 * @throws DiarizationException the diarization exception
	 */
	public void addFeature(float[] feature) throws DiarizationException {
		if (feature.length != getFeatureSize()) {
			debug(2);
			throw new DiarizationException("Features: addFrame() error: dim=" + getFeatureSize() + ", new frame dim="
					+ feature.length);
		}
		currentFeatureList.add(feature);
	}

	/**
	 * Perform a shallow copy of the feature set. The data currently in memory
	 * are shared with the original. However, the container of the data is not
	 * shared, and frames suppressed from or added to the copy won't be
	 * suppressed from/added to the original.
	 *
	 * @return the object
	 */
	@Override
	public Object clone() {
		AudioFeatureSet result = null;
		try {
			result = (AudioFeatureSet) (super.clone());
		} catch (CloneNotSupportedException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
		if (currentFeatureList != null) {
			result.currentFeatureList = (AudioFeatureList) (currentFeatureList.clone());
		}
		if (initialDesc != null) {
			result.initialDesc = (AudioFeatureDescription) (initialDesc.clone());
		}
		if (currentFileDesc != null) {
			result.currentFileDesc = (AudioFeatureDescription) (currentFileDesc.clone());
		}
		result.featureListMap = new TreeMap<String, AudioFeatureList>();
		return result;
	}

	/**
	 * Comparison of two frames.
	 *
	 * @param i index of first frame
	 * @param j index of second frame
	 *
	 * @return true, if frames are equal
	 */
	public boolean compareFreatures(int i, int j) {
		for (int k = 0; k < getFeatureSize(); k++) {
			if (currentFeatureList.get(i)[k] != currentFeatureList.get(j)[k]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Compute missing coefficients.
	 */
	private void computeMissingCoefficients() {
		if ((!currentFileDesc.getDeltaCoeffMustBeComputed()) && (!currentFileDesc.getDoubleDeltaCoeffMustBeComputed())
				&& (!currentFileDesc.getDeltaEnergyMustBeComputed())
				&& (!currentFileDesc.getDoubleDeltaEnergyMustBeComputed())) {
			return;
		}
		// Compute the new vector size, depending on what is present and what
		// needs to be computed
		int baseSize = currentFileDesc.getBaseSize();
		int newDim = baseSize; // Of course, this works only if static
		// coefficients are present; but we hope it's
		// always the case
		if (currentFileDesc.getEnergyPresence()) {
			newDim += 1;
		}
		if (currentFileDesc.getDeltaCoeffPresence() || currentFileDesc.getDeltaCoeffMustBeComputed()
				|| currentFileDesc.getDoubleDeltaCoeffMustBeComputed()) {
			newDim += baseSize;
		}
		if (currentFileDesc.getDeltaEnergyPresence() || currentFileDesc.getDeltaEnergyMustBeComputed()
				|| currentFileDesc.getDoubleDeltaEnergyMustBeComputed()) {
			newDim += 1;
		}
		if (currentFileDesc.getDoubleDeltaCoeffPresence() || currentFileDesc.getDoubleDeltaCoeffMustBeComputed()) {
			newDim += baseSize;
		}
		if (currentFileDesc.getDoubleDeltaEnergyPresence() || currentFileDesc.getDoubleDeltaEnergyMustBeComputed()) {
			newDim += 1;
		}
		// Allocate the new vectors, copying the existing values
		int nbFrames = currentFeatureList.size();
		for (int i = 0; i < nbFrames; i++) {
			float[] tmpFrame = new float[newDim];
			float[] frame = currentFeatureList.get(i);
			for (int j = 0; j < getStaticFeatureSize(); j++) {
				tmpFrame[j] = frame[j];
			}
			currentFeatureList.set(i, tmpFrame);
		}

		if (currentFileDesc.getDeltaCoeffMustBeComputed()
				|| (currentFileDesc.getDoubleDeltaCoeffMustBeComputed() && (currentFileDesc.getDeltaCoeffPresence() == false))) {
			/* compute Delta */
			currentFileDesc.setDeltaCoeffPresence(true);
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("Compute delta coeff");
			}

			if (deltaType == SPHINX) {
				computeSphinxStyleDerivedCoefficients(currentFileDesc.getIndexOfFirstStaticCoeff(), currentFileDesc.getIndexOfFirstDeltaCoeff(), baseSize, SPHINX_DELTA_WINDOW_SIZE);
			} else {
				computeSPROStyleDerivedCoefficients(currentFileDesc.getIndexOfFirstStaticCoeff(), currentFileDesc.getIndexOfFirstDeltaCoeff(), baseSize);
			}
		}
		if (currentFileDesc.getDeltaEnergyMustBeComputed()
				|| (currentFileDesc.getDoubleDeltaEnergyMustBeComputed() && (currentFileDesc.getDeltaEnergyPresence() == false))) {
			/* compute Delta Energy */
			currentFileDesc.setDeltaEnergyPresence(true);
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("Compute delta energy");
			}

			if (deltaType == SPHINX) {
				computeSphinxStyleDerivedCoefficients(currentFileDesc.getIndexOfEnergy(), currentFileDesc.getIndexOfDeltaEnergy(), 1, SPHINX_DELTA_WINDOW_SIZE);
			} else {
				computeSPROStyleDerivedCoefficients(currentFileDesc.getIndexOfEnergy(), currentFileDesc.getIndexOfDeltaEnergy(), 1);
			}
		}

		if (currentFileDesc.getDoubleDeltaCoeffMustBeComputed()) {
			/* compute DoubleDelta from Delta */
			currentFileDesc.setDoubleDeltaCoeffPresence(true);
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("Compute double delta coeff");
			}

			if (deltaType == SPHINX) {
				computeSphinxStyleDerivedCoefficients(currentFileDesc.getIndexOfFirstDeltaCoeff(), currentFileDesc.getIndexOfFirstDoubleDeltaCoeff(), baseSize, SPHINX_DOUBLE_DELTA_WINDOW_SIZE);
			} else {
				computeSPROStyleDerivedCoefficients(currentFileDesc.getIndexOfFirstDeltaCoeff(), currentFileDesc.getIndexOfFirstDoubleDeltaCoeff(), baseSize);
			}
		}
		if (currentFileDesc.getDoubleDeltaEnergyMustBeComputed()) {
			/* compute DoubleDeltaEnergy from DeltaEnergy */
			currentFileDesc.setDoubleDeltaEnergyPresence(true);
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("Compute double delta energy");
			}

			if (deltaType == SPHINX) {
				computeSphinxStyleDerivedCoefficients(currentFileDesc.getIndexOfDeltaEnergy(), currentFileDesc.getIndexOfDoubleDeltaEnergy(), 1, SPHINX_DOUBLE_DELTA_WINDOW_SIZE);
			} else {
				computeSPROStyleDerivedCoefficients(currentFileDesc.getIndexOfDeltaEnergy(), currentFileDesc.getIndexOfDoubleDeltaEnergy(), 1);
			}
		}
	}

	/**
	 * Compute sphinx style derived coefficients.
	 *
	 * @param firstSrcCoeff the first src coeff
	 * @param firstDestCoeff the first dest coeff
	 * @param coeffNb the coeff nb
	 * @param windowSize the window size
	 */
	private void computeSphinxStyleDerivedCoefficients(int firstSrcCoeff, int firstDestCoeff, int coeffNb, int windowSize) {
		if (SpkDiarizationLogger.DEBUG) {
			logger.fine("**** computeSphinxStyleDerivedCoefficients");
		}
		/*
		 * Sphinx way for delta coefficients: d(t) = c(t+2) - c(t-2) => windowSize == 2
		 */
		/*
		 * Sphinx way for double delta coefficients: dd(t) = d(t+1) - d(t-1) => windowSize == 1
		 */
		float prev[];
		float current[];
		float next[];
		int nbFrames = currentFeatureList.size();

		/* Take care of the beginning of the signal */
		prev = currentFeatureList.get(0);
		for (int frameIndex = 0; frameIndex < windowSize; frameIndex++) {
			current = currentFeatureList.get(frameIndex);
			next = currentFeatureList.get(frameIndex + windowSize);
			for (int i = 0; i < coeffNb; i++) {
				current[firstDestCoeff + i] = next[firstSrcCoeff + i] - prev[firstSrcCoeff + i];
			}
		}

		/* Central part */
		for (int frameIndex = windowSize; frameIndex < (nbFrames - windowSize); frameIndex++) {
			prev = currentFeatureList.get(frameIndex - windowSize);
			current = currentFeatureList.get(frameIndex);
			next = currentFeatureList.get(frameIndex + windowSize);
			for (int i = 0; i < coeffNb; i++) {
				current[firstDestCoeff + i] = next[firstSrcCoeff + i] - prev[firstSrcCoeff + i];
			}
		}

		/* Take care of the end of the signal */
		next = currentFeatureList.get(nbFrames - 1);
		for (int frameIndex = nbFrames - windowSize; frameIndex < nbFrames; frameIndex++) {
			prev = currentFeatureList.get(frameIndex - windowSize);
			current = currentFeatureList.get(frameIndex);
			for (int i = 0; i < coeffNb; i++) {
				current[firstDestCoeff + i] = next[firstSrcCoeff + i] - prev[firstSrcCoeff + i];
			}
		}
	}

	/**
	 * Compute spro style derived coefficients.
	 *
	 * @param firstSrcCoeff the first src coeff
	 * @param firstDestCoeff the first dest coeff
	 * @param coeffNb the coeff nb
	 */
	private void computeSPROStyleDerivedCoefficients(int firstSrcCoeff, int firstDestCoeff, int coeffNb) {
		float prev2[];
		float prev[];
		float current[];
		float next[];
		float next2[];
		int nbFrames = currentFeatureList.size();

		if (SpkDiarizationLogger.DEBUG) {
			logger.fine("**** computeSPROStyleDerivedCoefficients");
		}

		/* Take care of the beginning of the signal */
		prev2 = prev = current = next = currentFeatureList.get(0);
		next2 = currentFeatureList.get(1);
		for (int frameIndex = 0; frameIndex < 2; frameIndex++) {
			prev2 = prev;
			prev = current;
			current = next;
			next = next2;
			next2 = currentFeatureList.get(frameIndex + 2);
			for (int i = 0; i < coeffNb; i++) {
				current[firstDestCoeff + i] = (0.1f * (next[firstSrcCoeff + i] - prev[firstSrcCoeff + i]))
						+ (0.2f * (next2[firstSrcCoeff + i] - prev2[firstSrcCoeff + i]));
			}
		}

		/* Central part */
		for (int frameIndex = 2; frameIndex < (nbFrames - 2); frameIndex++) {
			prev2 = prev;
			prev = current;
			current = next;
			next = next2;
			next2 = currentFeatureList.get(frameIndex + 2);
			for (int i = 0; i < coeffNb; i++) {
				current[firstDestCoeff + i] = (0.1f * (next[firstSrcCoeff + i] - prev[firstSrcCoeff + i]))
						+ (0.2f * (next2[firstSrcCoeff + i] - prev2[firstSrcCoeff + i]));
			}
		}

		/* Take care of the end of the signal */
		for (int frameIndex = nbFrames - 2; frameIndex < nbFrames; frameIndex++) {
			prev2 = prev;
			prev = current;
			current = next;
			next = next2;
			for (int i = 0; i < coeffNb; i++) {
				current[firstDestCoeff + i] = (0.1f * (next[firstSrcCoeff + i] - prev[firstSrcCoeff + i]))
						+ (0.2f * (next2[firstSrcCoeff + i] - prev2[firstSrcCoeff + i]));
			}
		}
	}

	/**
	 * Debug.
	 *
	 * @throws DiarizationException the diarization exception
	 */
	public void debug() throws DiarizationException {
		debug(0);
	}

	/**
	 * Debug.
	 *
	 * @param level the level
	 *
	 * @throws DiarizationException the diarization exception
	 */
	public void debug(int level) throws DiarizationException {
		logger.finest("level = " + level);

		logger.finer("filename = " + currentFilename + " inputType = " + initialDesc.getFeaturesFormat()
				+ " memory Dim = " + getFeatureSize() + " static Dim = " + getStaticFeatureSize() + " file Dim = "
				+ initialDesc.getFeatureSize());

		if (level > 1) {
			String ch = initialDesc.toString();
			ch.replaceAll("\\n", "\ndebug[features] \t");
			ch = "debug[features] \t" + ch;
			logger.finer(ch);
			if (currentFeatureList != null) {
				logger.finer("nb = " + currentFeatureList.size() + " size = "
						+ (currentFeatureList.size() * getFeatureSize()));
				if (level > 2) {
					for (int f = 0; f < currentFeatureList.size(); f++) {
						String message = "frame = " + f + " values =";
						float[] frame = getFeatureUnsafe(f);
						for (int d = 0; d < getFeatureSize(); d++) {
							message += " " + frame[d];
						}
						logger.finer(message);
					}
				}
			} else {
				logger.finer("no data");
			}
		}
	}

	/**
	 * Free data.
	 */
	protected void freeFeatureList() {
		if (currentFeatureList != null) {
			// currentData.clear();
			currentFeatureList = null;
			// System.gc();
		}
	}

	/**
	 * Gets the current show name.
	 *
	 * @return the current show name
	 */
	public String getCurrentShowName() {
		return currentShow;
	}

	/**
	 * Get the dimension of the feature vectors.
	 *
	 * @return dimension of the feature vectors
	 */
	public int getFeatureSize() {
		if (currentFileDesc == null) {
			if (SpkDiarizationLogger.DEBUG) {
				logger.finest("*** initialDesc");
			}
			return initialDesc.getTrimmedFeatureDesc().getFeatureSize();
		} else {
			if (SpkDiarizationLogger.DEBUG) {
				logger.finest("*** currentFileDesc");
			}
			return currentFileDesc.getFeatureSize();
		}
	}

	/**
	 * Gets the current feature list size.
	 *
	 * @return the current feature list size
	 */
	public int getCurrentFeatureListSize() {
		return currentFeatureList.size();
	}

	/**
	 * Get the feature number \e index of the current show. Not safety method to
	 * get a feature, the show should be change by the program
	 *
	 * @param index the frame index
	 *
	 * @return the frame, as a float array; it is a reference to the actual
	 * storage of the frame, not a copy
	 */
	public float[] getFeatureUnsafe(int index) {
		try {
			return currentFeatureList.get(index);
		} catch (IndexOutOfBoundsException e) {
			throw new IndexOutOfBoundsException("Features: get() with wrong frame index (" + index
					+ " -- nb of frames:" + currentFeatureList.size() + ")");
		}
	}

	/**
	 * Get feature number \e index after having check if the current show is the
	 * show. Safety to get a feature, the show is validated. The validation
	 * costs time.
	 *
	 * @param show the show
	 * @param index the frame index
	 * @return the frame, as a float array; it is a reference to the actual
	 * storage of the frame, not a copy
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public float[] getFeature(String show, int index) throws DiarizationException, IOException {
		setCurrentShow(show);
		return currentFeatureList.get(index);
	}

	/**
	 * Get the number of frames.
	 *
	 * @return the number of frames
	 */
	public int getNumberOfFeatures() {
		if (currentFeatureList != null) {
			return currentFeatureList.size();
		}
		return 0;
	}

	/**
	 * Get the static dimension of the features.
	 *
	 * @return the static dimension of the features: sizeof(static + E)
	 */
	public int getStaticFeatureSize() {
		AudioFeatureDescription tmpFeatureDesc = currentFileDesc;
		if (tmpFeatureDesc == null) {
			tmpFeatureDesc = initialDesc.getTrimmedFeatureDesc();
		}
		if (tmpFeatureDesc.getEnergyPresence()) {
			return tmpFeatureDesc.getBaseSize() + 1;
		} else {
			return tmpFeatureDesc.getBaseSize();
		}
	}

	/**
	 * Gets the uB ms.
	 *
	 * @return the uB ms
	 */
	public GMMArrayList getUBMs() {
		return ubmList;
	}

	/**
	 * Normalize.
	 *
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void normalize() throws DiarizationException, IOException {
		if (currentFileDesc.getCentered()) {
			AudioFeatureNormalization featureNorm = new AudioFeatureNormalization(this, currentFileDesc.getReduced(), currentFileDesc.getNormalizationWindowSize());
			if (currentFileDesc.getNormalizationMethod() == AudioFeatureDescription.NORM_BY_SEGMENT) {
				if (SpkDiarizationLogger.DEBUG) {
					logger.finest("feature: NORM_BY_SEGMENT ");
				}
				featureNorm.normalizeClusterSetBySegment(clusterSet);
			} else if (currentFileDesc.getNormalizationMethod() == AudioFeatureDescription.NORM_BY_SLIDING) {
				if (SpkDiarizationLogger.DEBUG) {
					logger.finest("feature: NORM_BY_SLIDING ");
				}
				featureNorm.normalizeClusterSetByWindow(clusterSet);
			} else if (currentFileDesc.getNormalizationMethod() == AudioFeatureDescription.NORM_BY_CLUSTER) {
				if (SpkDiarizationLogger.DEBUG) {
					logger.finest("feature: NORM_BY_CLUSTER ");
				}
				featureNorm.normalizeClusterSetByCluster(clusterSet);
			} else if (currentFileDesc.getNormalizationMethod() == AudioFeatureDescription.NORM_BY_WARPING) {
				if (SpkDiarizationLogger.DEBUG) {
					logger.finest("feature: NORM_BY_WARPING ");
				}
				featureNorm.warpFile();
			} else if (currentFileDesc.getNormalizationMethod() == AudioFeatureDescription.NORM_BY_WARPING_AND_CR) {
				if (SpkDiarizationLogger.DEBUG) {
					logger.finest("feature: NORM_BY_WARPING_AND_CR ");
				}
				featureNorm.warpFile();
				featureNorm.normalizeClusterSetBySegment(clusterSet);
			} else if (currentFileDesc.getNormalizationMethod() == AudioFeatureDescription.NORM_BY_CR_AND_MAPPING) {
				if (SpkDiarizationLogger.DEBUG) {
					logger.finest("feature: NORM_BY_CR_AND_MAPPING ");
				}
				featureNorm.normalizeClusterSetBySegment(clusterSet);
				featureNorm.mapFeatureClusterSet(clusterSet, ubmList);
			} else if (currentFileDesc.getNormalizationMethod() == AudioFeatureDescription.NORM_BY_WARPING_AND_CRByCluster) {
				if (SpkDiarizationLogger.DEBUG) {
					logger.finest("feature: NORM_BY_WARPING_AND_CRByCluster ");
				}
				featureNorm.warpFile();
				featureNorm.normalizeClusterSetByCluster(clusterSet);
			} else {
				throw new DiarizationException("Features (" + currentFilename + "): normalization method don't exist");
			}
		}
	}

	/**
	 * Memory used.
	 *
	 * @return the long
	 */
	protected long memoryUsed() {
		return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
	}

	/**
	 * Memory rate.
	 *
	 * @param before the before
	 * @param after the after
	 * @return the double
	 */
	protected double memoryRate(long before, long after) {
		sizeInMemory += (after - before);
		if (sizeInMemory < 0) {
			sizeInMemory = 0;
		}
		long max = Runtime.getRuntime().maxMemory();
		double rate = (double) (sizeInMemory) / (double) max;

		if (SpkDiarizationLogger.DEBUG) {
			logger.finer(" // % mem free=" + rate + " used=" + ((sizeInMemory) / (1024 * 1024)) + " max="
					+ (max / (1024 * 1024)));
		}
		return rate;
	}

	/**
	 * Check end of segmentation.
	 */
	protected void checkEndOfSegmentation() {
		for (Cluster cluster : clusterSet.clusterSetValue()) {
			for (Segment segment : cluster) {
				if (segment.getShowName().contentEquals(currentShow)) {
					int endSegment = segment.getStart() + segment.getLength();
					if (endSegment > getNumberOfFeatures()) {
						logger.warning("segment is out of featureSet, correct end of segment :" + endSegment + "-->"
								+ getNumberOfFeatures());
						segment.setStartAndLast(segment.getStart(), getNumberOfFeatures() - 1);
					}
				}
			}
		}
	}

	/**
	 * Read.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	protected void read() throws IOException, DiarizationException {
		if (featureListMap.containsKey(currentShow)) {
			if (SpkDiarizationLogger.DEBUG) {
				logger.info("get data from map " + currentShow);
			}
			currentFeatureList = featureListMap.get(currentShow);
		} else {
			currentFileDesc = (AudioFeatureDescription) (initialDesc.clone());
			int inputType = currentFileDesc.getFeaturesFormat();
			if (SpkDiarizationLogger.DEBUG) {
				logger.info("compute data " + currentShow + " " + currentFileDesc.getFeaturesFormatAsString());
			}
			freeFeatureList();
			if (inputType == SPRO4) {
				readSPRO4();
			} else if (inputType == SPHINX) {
				readSphinx();
			} else if (inputType == HTK) {
				readHTK();
			} else if (inputType == GZTXT) {
				readGZTxt();
			} else if (inputType == AUDIO16Khz2SPHINXMFCC) {
				currentFeatureList = AudioFeatureSetFactory.MakeMFCCFeature(fontend16kHzConfigURL, currentFilename, currentFileDesc);
				deltaType = SPHINX;
				makeChangePositionOfEnergy();
			} else if (inputType == AUDIO8kHz2SPHINXMFCC) {
				currentFeatureList = AudioFeatureSetFactory.MakeMFCCFeature(fontend8kHzConfigURL, currentFilename, currentFileDesc);
				deltaType = SPHINX;
				makeChangePositionOfEnergy();
			} else if (inputType == AUDIO22kHz2SPHINXMFCC) {
				currentFeatureList = AudioFeatureSetFactory.MakeMFCCFeature(fontend22kHzConfigURL, currentFilename, currentFileDesc);
				deltaType = SPHINX;
				makeChangePositionOfEnergy();
			} else if (inputType == AUDIO44kHz2SPHINXMFCC) {
				currentFeatureList = AudioFeatureSetFactory.MakeMFCCFeature(fontend44kHzConfigURL, currentFilename, currentFileDesc);
				deltaType = SPHINX;
				makeChangePositionOfEnergy();
			} else if (inputType == AUDIO48kHz2SPHINXMFCC) {
				currentFeatureList = AudioFeatureSetFactory.MakeMFCCFeature(fontend48kHzConfigURL, currentFilename, currentFileDesc);
				deltaType = SPHINX;
				makeChangePositionOfEnergy();
			} else if (inputType == FEATURESETTRANSFORMATION) {
				source.setCurrentShow(currentShow);
				copyFeatureList();
			} else {
				throw new DiarizationException("Features (" + ParameterAudioFeature.AudioFeaturesTypeString[inputType]
						+ "): unknown input feature type " + currentFilename);
			}
			// debug();
			checkEndOfSegmentation();
			speechDetection();
			computeMissingCoefficients();
			removeUnneededCoefficients();
			normalize();
			currentFileDesc = initialDesc.getTrimmedFeatureDesc();

			if (SpkDiarizationLogger.DEBUG) {
				debug();
			}
			float rate = MainTools.calculateMemoryUsage(true, memoryOccupationRate);
			if (rate > memoryOccupationRate) {
				while (rate > (0.75 * memoryOccupationRate)) {
					if (featureListMap.isEmpty()) {
						break;
					}
					/*
					 * if (featureListMap.size() < 25) { String key = featureListMap.pollFirstEntry().getKey(); logger.finer("remove data from " + key); } else {
					 */
					featureListMap = new TreeMap<String, AudioFeatureList>();
					sizeInMemory = 0;
					if (SpkDiarizationLogger.DEBUG) {
						logger.fine("!!! clear data !!!");
					}
					// }
					Runtime.getRuntime().gc();
					MainTools.calculateMemoryUsage(true, memoryOccupationRate);
				}
			}
			featureListMap.put(currentShow, currentFeatureList);
		}
	}

	/**
	 * Copy data.
	 */
	private void copyFeatureList() {
		currentFeatureList = new AudioFeatureList(source.getNumberOfFeatures());
		currentFeatureList.addAll(source.currentFeatureList);
	}

	/**
	 * Read gz txt.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void readGZTxt() throws IOException {
		currentFeatureList = new AudioFeatureList();
		ArrayList<String> lst = StringListFileIO.read(currentFilename, true);
		for (int i = 0; i < lst.size(); i++) {
			String tokens[] = lst.get(i).split(" ");
			float[] tmpframe = new float[getFeatureSize()];
			// logger.warning(getFeatureSize() + " / " + tokens.length + " / " + lst.get(i));
			for (int j = 0; j < getFeatureSize(); j++) {
				tmpframe[j] = Float.parseFloat(tokens[j]);
			}
			/*
			 * StringTokenizer stok = new StringTokenizer(lst.get(i), " "); float[] tmpframe = new float[getFeatureSize()]; stok.nextToken(); for (int j = 0; j < getFeatureSize(); j++) { tmpframe[j] = Float.parseFloat(stok.nextToken()); }
			 */
			currentFeatureList.add(tmpframe);
		}
	}

	/**
	 * Read htk.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	private void readHTK() throws IOException, DiarizationException {
		int WITHE = 0x0040; // _E Data is with log energy
		int WITHD = 0x0100; // _D Data contains delta coefficients
		int WITHA = 0x0200; // _A Data contains delta-delta coefficients

		// int WITHE = 0100; // _E Data is with log energy
		// int HASNULLE = 0200; // _N absolute energy suppressed
		// int WITHD = 0400; // _D Data contains delta coefficients
		// int WITHA = 01000; // _A Data contains delta-delta coefficients
		// int HASZEROM = 04000; // _Z zero meaned
		// int HASCOMPX = 02000; // _C is compressed
		// int HASCRCC = 010000; // _K has CRC check
		// int HASZEROC = 020000; // _0 0'th Cepstra included
		// int HASVQ = 040000; // _V has VQ index attached
		// int HASTHIRD = 0100000; // _T has Delta-Delta-Delta index attached

		String mode = "rb";
		IOFile file = new IOFile(currentFilename, mode, swap);
		file.open();
		// logger.info("currentFilename:"+currentFilename);

		int nb = file.readInt();
		// logger.info("nb:"+nb+" / "+Integer.reverseBytes(nb));
		int samplePeriod = file.readInt();
		// logger.info("samplePeriod:"+samplePeriod+" / "+Integer.reverseBytes(samplePeriod));
		short vectSize = file.readShort();
		if (SpkDiarizationLogger.DEBUG) {
			logger.info("vectSize:" + vectSize + " / " + Integer.reverseBytes(vectSize));
		}

		short flag = file.readShort();
		if (currentFileDesc.getFeatureSize() != (vectSize * 4)) {
			if (Short.reverseBytes(vectSize) == (currentFileDesc.getFeatureSize() * 4)) {
				vectSize = Short.reverseBytes(vectSize);
				flag = Short.reverseBytes(flag);
				nb = Integer.reverseBytes(nb);
				samplePeriod = Integer.reverseBytes(samplePeriod);
				file.setSwap(true);
			} else {
				throw new DiarizationException("Features (" + currentFilename
						+ "): the feature vector dimension in the file doesn't match what the user specified -- file: "
						+ vectSize + "/" + Short.reverseBytes(vectSize) + ", user: " + currentFileDesc.getFeatureSize());
			}
		}
		vectSize /= 4;
		if ((samplePeriod / 100) != 100) {
			logger.warning("Features (" + currentFilename
					+ "): the feature sample Periode doesn't match the standart one -- file: " + (samplePeriod / 100)
					+ ", user: 100");
		}

		checkFlag(currentFilename, flag, WITHE, WITHD, WITHA);

		currentFeatureList = new AudioFeatureList(nb);

		for (int i = 0; i < nb; i++) {
			float[] tmpFrame = new float[getFeatureSize()];
			currentFeatureList.add(tmpFrame);
			file.readFloatArray(currentFeatureList.get(i), getFeatureSize());
		}
		file.close();
	}

	/**
	 * Read sphinx.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	private void readSphinx() throws IOException, DiarizationException {
		String mode = "rb";
		IOFile file = new IOFile(currentFilename, mode, swap);
		file.open();

		file.readInt();
		long headerSize = 4; // sizeof(int)
		// TODO: check the contents of the header

		long nb = (file.len() - headerSize) / (getFeatureSize() * 4L); // 4: size of
		// float
		if (((file.len() - headerSize) % (getFeatureSize() * 4)) != 0) {
			throw new DiarizationException("Features ("
					+ currentFilename
					+ "): the feature vector dimension in the file doesn't seem to match what the user specified -- file: N/A (Sphinx), user: "
					+ currentFileDesc.getFeatureSize());
		}

		currentFeatureList = new AudioFeatureList((int) nb);
		for (int i = 0; i < nb; i++) {
			float[] tmpFrame = new float[getFeatureSize()];
			currentFeatureList.add(tmpFrame);
			file.readFloatArray(currentFeatureList.get(i), getFeatureSize());
		}
		file.close();
		makeChangePositionOfEnergy();
	}

	/**
	 * Change position of energy.
	 */
	private void makeChangePositionOfEnergy() {
		if (changePositionOfEnergy) {
			for (int i = 0; i < currentFeatureList.size(); i++) {
				float[] frame = currentFeatureList.get(i);
				float old = frame[0];
				for (int j = 0; j < (getFeatureSize() - 1); j++) {
					frame[j] = frame[j + 1];
				}
				frame[getFeatureSize() - 1] = old;
			}
		}
	}

	/**
	 * Read spr o4.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	private void readSPRO4() throws IOException, DiarizationException {
		// spro4: flag information
		int WITHE = 0x01; // Data is with log energy
		int WITHD = 0x08; // Data contains delta coefficients
		int WITHA = 0xa0; // Data contains delta-delta coefficients

		String mode = "rb";
		IOFile file = new IOFile(currentFilename, mode, swap);
		file.open();
		short vectSize = file.readShort();
		if (currentFileDesc.getFeatureSize() != vectSize) {
			if (Short.reverseBytes(vectSize) == currentFileDesc.getFeatureSize()) {
				vectSize = Short.reverseBytes(vectSize);
				file.setSwap(true);
			} else {
				throw new DiarizationException("Features (" + currentFilename
						+ "): the feature vector dimension in the file doesn't match what the user specified -- file: "
						+ vectSize + ", user: " + currentFileDesc.getFeatureSize());
			}
		}

		int flag = file.readInt();
		checkFlag(currentFilename, flag, WITHE, WITHD, WITHA);

		file.readInt(); // rate

		long headerSize = 2 + 4 + 4; // sizeof(vectSize) + sizeof(flag) +
		// sizeof(rate)

		int nb = (int) (file.len() - headerSize) / (getFeatureSize() * 4); // 4 : size
		// of float
		if (((file.len() - headerSize) % (getFeatureSize() * 4)) != 0) {
			throw new DiarizationException("Features (" + currentFilename + "): file appears to be corrupted");
		}
		currentFeatureList = new AudioFeatureList(nb);
		for (int i = 0; i < nb; i++) {
			float[] tmpFrame = new float[getFeatureSize()];
			currentFeatureList.add(tmpFrame);
			file.readFloatArray(currentFeatureList.get(i), getFeatureSize());
		}
		file.close();
	}

	/**
	 * Check flag.
	 *
	 * @param currentFilename the current filename
	 * @param flag the flag
	 * @param WITHE the withe
	 * @param WITHD the withd
	 * @param WITHA the witha
	 * @throws DiarizationException the diarization exception
	 */
	private void checkFlag(String currentFilename, int flag, int WITHE, int WITHD, int WITHA) throws DiarizationException {
		if (currentFileDesc.getEnergyPresence() && ((flag & WITHE) == 0)) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): energy should be present in file but is not (1)");
		}
		if ((!currentFileDesc.getEnergyPresence()) && ((flag & WITHE) == 1)) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): energy should not be present but is present in file (2)");
		}
		if (currentFileDesc.getDeltaCoeffPresence() && ((flag & WITHD) == 0)) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): delta should be present in file but are not (3)");
		}
		if ((!currentFileDesc.getDeltaCoeffPresence()) && ((flag & WITHD) == 1)) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): delta should not be present but are present in file (4)");
		}
		if (currentFileDesc.getDeltaEnergyPresence() && (((flag & WITHE) == 0) || ((flag & WITHD) == 0))) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): delta energy should be present in file but is not (5)");
		}
		if ((!currentFileDesc.getDeltaEnergyPresence()) && (((flag & WITHE) == 1) && ((flag & WITHD) == 1))) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): delta energy should not be present but is present in file (6)");
		}
		if (currentFileDesc.getDoubleDeltaCoeffPresence() && ((flag & WITHA) == 0)) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): delta delta should be present in file but are not (7)");
		}
		if ((!currentFileDesc.getDoubleDeltaCoeffPresence()) && ((flag & WITHA) == 1)) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): delta delta should not be present but are present in file (8)");
		}
		if (currentFileDesc.getDoubleDeltaEnergyPresence() && (((flag & WITHE) == 0) || ((flag & WITHA) == 0))) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): double delta energy should be present in file but is not (9)");
		}
		if ((!currentFileDesc.getDoubleDeltaEnergyPresence()) && (((flag & WITHE) == 1) && ((flag & WITHA) == 1))) {
			throw new DiarizationException("Features (" + currentFilename
					+ "): double delta energy should not be present but is present in file (10)");
		}

	}

	/**
	 * Speech detection.
	 *
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void speechDetection() throws DiarizationException, IOException {
		if (speechThreshold > 0) {
			if (currentFileDesc.getIndexOfEnergy() >= 0) {
				AudioSpeechDetection.speechDetection(this);
			} else {
				throw new DiarizationException("Features (" + currentFilename
						+ "): energy not available, need for speech detection (11)");
			}
		}
	}

	/**
	 * Removes the coefficients.
	 *
	 * @param indexOfFirstCoeff the index of first coeff
	 * @param nbOfCoeffs the nb of coeffs
	 */
	private void removeCoefficients(int indexOfFirstCoeff, int nbOfCoeffs) {
		int nbOfFrames = currentFeatureList.size();
		int currentDimension = currentFileDesc.getFeatureSize();
		for (int i = 0; i < nbOfFrames; i++) {
			float[] currentFrame = currentFeatureList.get(i);
			float[] newFrame = new float[currentDimension - nbOfCoeffs];
			int j, k;
			for (j = 0, k = 0; j < indexOfFirstCoeff; j++, k++) {
				newFrame[k] = currentFrame[j];
			}
			for (j = indexOfFirstCoeff + nbOfCoeffs; j < currentDimension; j++, k++) {
				newFrame[k] = currentFrame[j];
			}
			currentFeatureList.set(i, newFrame);
		}
	}

	/**
	 * Removes the unneeded coefficients.
	 */
	private void removeUnneededCoefficients() {
		// logger.info(initialDesc.toString());
		// logger.fine(currentFileDesc.toString());

		if (currentFileDesc.getEnergyMustBeDeleted()) {
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("Delete energy " + currentFileDesc.getIndexOfEnergy());
			}
			removeCoefficients(currentFileDesc.getIndexOfEnergy(), 1);
			currentFileDesc.setEnergyPresence(false);
		}
		if (currentFileDesc.getDeltaEnergyMustBeDeleted()) {
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("Delete delta energy " + currentFileDesc.getIndexOfDeltaEnergy());
			}

			removeCoefficients(currentFileDesc.getIndexOfDeltaEnergy(), 1);
			currentFileDesc.setDeltaEnergyPresence(false);
		}
		if (currentFileDesc.getDoubleDeltaEnergyMustBeDeleted()) {
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("Delete double delta energy " + currentFileDesc.getIndexOfDoubleDeltaEnergy());
			}
			removeCoefficients(currentFileDesc.getIndexOfDoubleDeltaEnergy(), 1);
			currentFileDesc.setDoubleDeltaEnergyPresence(false);
		}
		if (currentFileDesc.getStaticCoeffMustBeDeleted()) {
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("static coeff " + currentFileDesc.getIndexOfFirstStaticCoeff() + " ("
						+ currentFileDesc.getBaseSize() + " coeff)");
			}
			removeCoefficients(currentFileDesc.getIndexOfFirstStaticCoeff(), currentFileDesc.getBaseSize());
			currentFileDesc.setStaticCoeffPresence(false);
		}
		if (currentFileDesc.getDeltaCoeffMustBeDeleted()) {
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("Delete delta coeff " + currentFileDesc.getIndexOfFirstDeltaCoeff() + " ("
						+ currentFileDesc.getBaseSize() + " coeff)");
			}
			removeCoefficients(currentFileDesc.getIndexOfFirstDeltaCoeff(), currentFileDesc.getBaseSize());
			currentFileDesc.setDeltaCoeffPresence(false);
		}
		if (currentFileDesc.getDoubleDeltaCoeffMustBeDeleted()) {
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("Delete double delta coeff " + currentFileDesc.getIndexOfFirstDoubleDeltaCoeff() + " ("
						+ currentFileDesc.getBaseSize() + " coeff)");
			}
			removeCoefficients(currentFileDesc.getIndexOfFirstDoubleDeltaCoeff(), currentFileDesc.getBaseSize());
			currentFileDesc.setDoubleDeltaCoeffPresence(false);
		}
	}

	/**
	 * Check if the current show is the one with index \e showIndex; if not, the
	 * show with this index is read.
	 *
	 * @param show the show name to check
	 *
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void setCurrentShow(String show) throws DiarizationException, IOException {
		if (show.compareTo(currentShow) != 0) {
			currentShow = show;
			currentFilename = IOFile.getFilename(filenameMask, currentShow);
			if (SpkDiarizationLogger.DEBUG) {
				logger.finer("setCurrentShow(), will read file: " + currentFilename);
			}

			read();
		}
	}

	/**
	 * Creates the current show.
	 *
	 * @param show the show
	 */
	public void CreateCurrentShow(String show) {
		currentShow = show;
		if (SpkDiarizationLogger.DEBUG) {
			logger.finer("CreateCurrentShow(), assign show name: " + currentFilename);
		}
	}

	/**
	 * Sets the UBM.
	 *
	 * @param ubmList the new UBM
	 */
	public void setUbmList(GMMArrayList ubmList) {
		this.ubmList = ubmList;
	}

	/**
	 * Sets the change position of energy.
	 *
	 * @param v the new change position of energy
	 */
	public void setChangePositionOfEnergy(boolean v) {
		changePositionOfEnergy = v;
	}

	/**
	 * Gets the change position of energy.
	 *
	 * @return the change position of energy
	 */
	public boolean getChangePositionOfEnergy() {
		return changePositionOfEnergy;
	}

	/**
	 * Gets the index of energy.
	 *
	 * @return the index of energy
	 */
	public int getIndexOfEnergy() {
		return currentFileDesc.getIndexOfEnergy();
	}

	/**
	 * Write.
	 *
	 * @param show the name of the show
	 * @param outputFeatureMask the output feature mask
	 * @param featureOutputDescription the feature output description
	 *
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void write(String show, String outputFeatureMask, AudioFeatureDescription featureOutputDescription) throws DiarizationException, IOException {
		String filename = IOFile.getFilename(outputFeatureMask, show);

		if (SpkDiarizationLogger.DEBUG) {
			logger.fine("filename:" + filename);
		}
		if (SpkDiarizationLogger.DEBUG) {
			logger.fine("initial:" + initialDesc.toString());
		}

		currentFileDesc = (AudioFeatureDescription) (featureOutputDescription.clone());
		if (SpkDiarizationLogger.DEBUG) {
			logger.fine("currentFileDesc:" + currentFileDesc.toString());
		}
		computeMissingCoefficients();
		removeUnneededCoefficients();
		normalize();
		currentFileDesc = featureOutputDescription.getTrimmedFeatureDesc();
		if (SpkDiarizationLogger.DEBUG) {
			logger.fine("currentFileDesc after:" + currentFileDesc.toString());
		}
		if (SpkDiarizationLogger.DEBUG) {
			logger.fine("feature size:" + currentFileDesc.getFeatureSize() + " base size:" + currentFileDesc.getBaseSize());
		}

		int outputType = featureOutputDescription.getFeaturesFormat();
		if (outputType == SPRO4) {
			writeSPRO4(filename);
		} else if (outputType == SPHINX) {
			writeSphinx(filename);
		} else if (outputType == GZTXT) {
			writeGZTxt(filename);
		} else {
			throw new DiarizationException("Features (" + ParameterAudioFeature.AudioFeaturesTypeString[outputType]
					+ "): unknown output feature type " + currentFilename);
		}
	}

	/**
	 * Write gziped text format.
	 *
	 * @param filename the filename
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void writeGZTxt(String filename) throws IOException {
		File f_test = new File(filename);
		if (!f_test.isDirectory()) {
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filename))));
			String line = "";
			for (int i = 0; i < currentFeatureList.size(); i++) {
				float[] frame = currentFeatureList.get(i);
				for (float element : frame) {
					line += String.format(Locale.US, "%8.6f", element) + " ";

				}
				bufferedWriter.write(line);
				bufferedWriter.newLine();
				line = "";
			}
			bufferedWriter.close();
		}
	}

	/**
	 * Write sphinx format.
	 *
	 * @param filename the filename
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void writeSphinx(String filename) throws IOException {
		if (SpkDiarizationLogger.DEBUG) {
			logger.finer("sphinx dim = " + getFeatureSize() + " staticDim = " + getStaticFeatureSize());
		}

		String mode = "wb";
		IOFile file = new IOFile(filename, mode, swap);
		file.open();
		int dim = currentFileDesc.getFeatureSize();
		// int baseDim = currentFileDesc.getBaseSize();
		file.writeInt(currentFeatureList.size() * dim);
		for (int i = 0; i < currentFeatureList.size(); i++) {
			// file.writeFloatArray(currentFeatureList.get(i), getFeatureSize());
			float frame[] = currentFeatureList.get(i);
			if (currentFileDesc.getEnergyPresence()) {
				file.writeFloat(frame[currentFileDesc.getIndexOfEnergy()]);
			}
			if (currentFileDesc.getStaticCoeffPresence()) {
				for (int j = currentFileDesc.getIndexOfFirstStaticCoeff(); j < (currentFileDesc.getIndexOfFirstStaticCoeff() + currentFileDesc.getBaseSize()); j++) {
					file.writeFloat(frame[j]);
				}
			}
			if (currentFileDesc.getDeltaEnergyPresence()) {
				file.writeFloat(frame[currentFileDesc.getIndexOfDeltaEnergy()]);
			}
			if (currentFileDesc.getDeltaCoeffPresence()) {
				for (int j = currentFileDesc.getIndexOfFirstDeltaCoeff(); j < (currentFileDesc.getIndexOfFirstDeltaCoeff() + currentFileDesc.getBaseSize()); j++) {
					file.writeFloat(frame[j]);
				}
			}
			if (currentFileDesc.getDoubleDeltaEnergyPresence()) {
				file.writeFloat(frame[currentFileDesc.getIndexOfDoubleDeltaEnergy()]);
			}
			if (currentFileDesc.getDoubleDeltaCoeffPresence()) {
				for (int j = currentFileDesc.getIndexOfFirstDoubleDeltaCoeff(); j < (currentFileDesc.getIndexOfFirstDoubleDeltaCoeff() + currentFileDesc.getBaseSize()); j++) {
					file.writeFloat(frame[j]);
				}
			}
		}
		file.close();
	}

	/**
	 * Write SPRO4 file.
	 *
	 * @param filename the filename
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected void writeSPRO4(String filename) throws IOException {
		// spro4 : flag information
		int WITHE = 0x01; // Data is with log energy
		int WITHD = 0x08; // Data as delta coefficients
		int WITHA = 0xa0; // Data contains delta-delta coefficients

		String mode = "wb";
		IOFile file = new IOFile(filename, mode, swap);
		file.open();
		file.writeShort((short) getFeatureSize());
		if (SpkDiarizationLogger.DEBUG) {
			logger.finer("spro 4 dim " + getFeatureSize());
		}

		int flag = 0;
		if (currentFileDesc.getEnergyPresence()) {
			flag |= WITHE;
		}
		if (currentFileDesc.getDeltaCoeffPresence()) {
			flag |= WITHD;
		}
		if (currentFileDesc.getDoubleDeltaCoeffPresence()) {
			flag |= WITHA;
		}
		file.writeInt(flag);
		if (SpkDiarizationLogger.DEBUG) {
			logger.finer("flag=" + flag);
		}

		file.writeInt(0);
		if (SpkDiarizationLogger.DEBUG) {
			logger.finer("rate=0 (unsupported)");
		}

		for (int i = 0; i < currentFeatureList.size(); i++) {
			file.writeFloatArray(currentFeatureList.get(i), getFeatureSize());
		}
		file.close();
	}

	/**
	 * Gets the cluster set.
	 *
	 * @return the clusterSet
	 */
	public ClusterSet getClusterSet() {
		return clusterSet;
	}

	/**
	 * Sets the cluster set.
	 *
	 * @param clusterSet the clusterSet to set
	 */
	public void setClusterSet(ClusterSet clusterSet) {
		this.clusterSet = clusterSet;
	}
}
