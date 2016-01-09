/**
 * <p>
 * FeaturesSet
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
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.zip.GZIPOutputStream;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.StringListFileIO;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.parameter.ParameterFeature;

// TODO: Auto-generated Javadoc

/**
 * The Class FeatureSet.
 */
public class FeatureSet implements Cloneable {

    /** Constants for file types. */
    public static final int SPRO4 = 0;

    /** The Constant HTK. */
    public static final int HTK = 1;

    /** The Constant SPHINX. */
    public static final int SPHINX = 2;

    /** The Constant GZTXT. */
    public static final int GZTXT = 3;

    /** The Constant AUDIO16Khz2SPHINX. */
    public static final int AUDIO16Khz2SPHINXMFCC = 4;

    /** The Constant AUDIO8Khz2SPHINX. */
    public static final int AUDIO8kHz2SPHINXMFCC = 6;

    /** The Constant FEATURESETTRANSFORMATION. */
    public static final int FEATURESETTRANSFORMATION = 5;

    /** Constant to use when the current show is not set. */
    public static final String UNKNOWN_SHOW = "UNKNOWN_SHOW";

    /** Constant for delta computation. */
    public static final int FOLLOW_INPUT_FILE_TYPE = -1;

    /** Constants for Sphinx-style delta computation. */
    private static final int SPHINX_DELTA_WINDOW_SIZE = 2;

    /** The Constant SPHINX_DOUBLE_DELTA_WINDOW_SIZE. */
    private static final int SPHINX_DOUBLE_DELTA_WINDOW_SIZE = 1;


    /** The fontend config url. */
    protected URL fontend16kHzConfigURL;
    protected URL fontend8kHzConfigURL;

    /** The initial desc. */
    protected FeatureDescription initialDesc; // description of the features

    /** The current file desc. */
    protected FeatureDescription currentFileDesc; // description of the features in the
    // current file, after a read
    /** The data. */
    protected FeatureData currentData;// features
    protected TreeMap<String, FeatureData> dataMap;
    /** The filename mask. */
    protected String filenameMask; // filename mask, some think like %s.cep

    /** The current filename. */
    protected String currentFilename; // the current loaded file

    /** The current show. */
    protected String currentShow; // index of the current show
// protected int inputType; // !< type of features
// protected int outputType; // !< type of features

    protected long sizeInMemory = 0;
    /**
     * algorithm to use to compute delta and double delta; it can either be decided by the input file type, or be set to Spro or Sphinx (using the file type
     * constants).
     */
    protected int deltaType;

    /** The cluster set. */
    protected ClusterSet clusterSet;

    /** The UBMs. */
    protected ArrayList<GMM> UBMs;

    /** used when a FeatureSet is created by copying the data of an other FeatureSet. */
    protected FeatureSet source;

    /** The swap. */
    protected boolean swap;

    /** The trace. */
    protected boolean trace;

    protected boolean changePositionOfEnergy = true;

    protected double memoryOccupationRate = 0.5;
    /**
     * **********************************************************************************.
     *
     * @param clusters the clusters
     * @param parameterInputFeature the parameter input feature
     * @param trace the trace
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    /*************************************************************************************/
    public FeatureSet(ClusterSet clusters, ParameterFeature parameterInputFeature, boolean trace) throws IOException, DiarizationException {
        fontend16kHzConfigURL = getClass().getResource("frontend.config.16kHz.xml");
        fontend8kHzConfigURL = getClass().getResource("frontend.config.8kHz.xml");

        clusterSet = clusters;
        currentData = null;
        dataMap = new TreeMap<String, FeatureData>();
// inputType = inputFileType;
// outputType = outputFileType;
        filenameMask = parameterInputFeature.getFeatureMask();
        currentFilename = "";
        currentShow = UNKNOWN_SHOW;
        initialDesc = parameterInputFeature.getFeaturesDescription();
        currentFileDesc = null;
        deltaType = FOLLOW_INPUT_FILE_TYPE;// !!!:Teva:20071206 Temporary, until
        // an argument is added to the
        // method
        if (deltaType == FOLLOW_INPUT_FILE_TYPE) {
            deltaType = initialDesc.getFeaturesFormat();
        }
        memoryOccupationRate = parameterInputFeature.getMemoryOccupationRate();
    }

    /**
     * Instantiates a new feature set.
     *
     * @param features the features
     * @param clusters the clusters
     * @param parameterInputFeature the parameter input feature
     * @param trace the trace
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public FeatureSet(FeatureSet features, ClusterSet clusters, ParameterFeature parameterInputFeature, boolean trace) throws IOException,
            DiarizationException {
        fontend16kHzConfigURL = getClass().getResource("frontend.config.xml");
        fontend8kHzConfigURL = getClass().getResource("frontend.config.8kHz.xml");
        source = features;
        clusterSet = clusters;
        currentData = null;
        dataMap = new TreeMap<String, FeatureData>();
        filenameMask = parameterInputFeature.getFeatureMask();
        currentFilename = "";
        currentShow = UNKNOWN_SHOW;
        initialDesc = parameterInputFeature.getFeaturesDescription();
        currentFileDesc = null;
        deltaType = FOLLOW_INPUT_FILE_TYPE;// !!!:Teva:20071206 Temporary, until
        // an argument is added to the
        // method

        if (deltaType == FOLLOW_INPUT_FILE_TYPE) {
            deltaType = features.deltaType;
        }
        memoryOccupationRate = parameterInputFeature.getMemoryOccupationRate();
    }

    /**
     * Constructor.
     *
     * @param initialCapacity specifies path and extension for the shows
     * @param description description of the features
     */
    public FeatureSet(int initialCapacity, FeatureDescription description) {
        fontend16kHzConfigURL = getClass().getResource("frontend.config.xml");
        initialDesc = description;
        currentFileDesc = description.getTrimmedFeatureDesc();
        currentFilename = "";
        currentShow = UNKNOWN_SHOW;
        currentData = new FeatureData(initialCapacity);
        dataMap = new TreeMap<String, FeatureData>();
        dataMap.put(currentShow, currentData);
// inputType = outputFileType;
// outputType = outputFileType;
        deltaType = FOLLOW_INPUT_FILE_TYPE;// !!!:Teva:20071206 Temporary, until
        // an argument is added to the
        // method
        if (deltaType == FOLLOW_INPUT_FILE_TYPE) {
            deltaType = description.getFeaturesFormat();
        }
    }

    /**
     * Add a frame at the end of the feature set.
     *
     * @param values the values
     *
     * @throws DiarizationException the diarization exception
     */
    public void addFrame(float[] values) throws DiarizationException {
        if (values.length != getDim()) {
            throw new DiarizationException("Features: addFrame() error: dim=" + getDim() + ", new frame dim=" + values.length);
        }
        currentData.add(values);
    }

    /**
     * Perform a shallow copy of the feature set. The data currently in memory are shared with the original. However, the container of the data is not shared,
     * and frames suppressed from or added to the copy won't be suppressed from/added to the original.
     *
     * @return the object
     */
    public Object clone() {
        FeatureSet result = null;
        try {
            result = (FeatureSet) (super.clone());
        } catch (CloneNotSupportedException e) {
        }
        if (currentData != null) {
            result.currentData = (FeatureData) (currentData.clone());
        }
        if (initialDesc != null) {
            result.initialDesc = (FeatureDescription) (initialDesc.clone());
        }
        if (currentFileDesc != null) {
            result.currentFileDesc = (FeatureDescription) (currentFileDesc.clone());
        }
        result.dataMap = new TreeMap<String, FeatureData>();
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
    public boolean compareFrames(int i, int j) {
        for (int k = 0; k < getDim(); k++) {
            if (currentData.get(i)[k] != currentData.get(j)[k]) {
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
                && (!currentFileDesc.getDeltaEnergyMustBeComputed()) && (!currentFileDesc.getDoubleDeltaEnergyMustBeComputed())) {
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
        if (currentFileDesc.getDeltaCoeffPresence() || currentFileDesc.getDeltaCoeffMustBeComputed() || currentFileDesc.getDoubleDeltaCoeffMustBeComputed()) {
            newDim += baseSize;
        }
        if (currentFileDesc.getDeltaEnergyPresence() || currentFileDesc.getDeltaEnergyMustBeComputed() || currentFileDesc.getDoubleDeltaEnergyMustBeComputed()) {
            newDim += 1;
        }
        if (currentFileDesc.getDoubleDeltaCoeffPresence() || currentFileDesc.getDoubleDeltaCoeffMustBeComputed()) {
            newDim += baseSize;
        }
        if (currentFileDesc.getDoubleDeltaEnergyPresence() || currentFileDesc.getDoubleDeltaEnergyMustBeComputed()) {
            newDim += 1;
        }
        // Allocate the new vectors, copying the existing values
        int nbFrames = currentData.size();
        for (int i = 0; i < nbFrames; i++) {
            float[] tmpFrame = new float[newDim];
            float[] frame = currentData.get(i);
            for (int j = 0; j < getStaticDimension(); j++) {
                tmpFrame[j] = frame[j];
            }
            currentData.set(i, tmpFrame);
        }

        if (currentFileDesc.getDeltaCoeffMustBeComputed()
                || (currentFileDesc.getDoubleDeltaCoeffMustBeComputed() && (currentFileDesc.getDeltaCoeffPresence() == false))) {
            /* compute Delta */
            currentFileDesc.setDeltaCoeffPresence(true);
            if (trace) {
                System.out.println("trace[features] \tCompute delta coeff");
            }
            if (deltaType == SPHINX) {
                computeSphinxStyleDerivedCoefficients(currentFileDesc.getIndexOfFirstStaticCoeff(), currentFileDesc.getIndexOfFirstDeltaCoeff(), baseSize,
                        SPHINX_DELTA_WINDOW_SIZE);
            } else {
                computeSPROStyleDerivedCoefficients(currentFileDesc.getIndexOfFirstStaticCoeff(), currentFileDesc.getIndexOfFirstDeltaCoeff(), baseSize);
            }
        }
        if (currentFileDesc.getDeltaEnergyMustBeComputed()
                || (currentFileDesc.getDoubleDeltaEnergyMustBeComputed() && (currentFileDesc.getDeltaEnergyPresence() == false))) {
			/* compute Delta Energy */
            currentFileDesc.setDeltaEnergyPresence(true);
            if (trace) {
                System.out.println("trace[features] \tCompute delta energy");
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
            if (trace) {
                System.out.println("trace[features] \tCompute double delta coeff");
            }
            if (deltaType == SPHINX) {
                computeSphinxStyleDerivedCoefficients(currentFileDesc.getIndexOfFirstDeltaCoeff(), currentFileDesc.getIndexOfFirstDoubleDeltaCoeff(), baseSize,
                        SPHINX_DOUBLE_DELTA_WINDOW_SIZE);
            } else {
                computeSPROStyleDerivedCoefficients(currentFileDesc.getIndexOfFirstDeltaCoeff(), currentFileDesc.getIndexOfFirstDoubleDeltaCoeff(), baseSize);
            }
        }
        if (currentFileDesc.getDoubleDeltaEnergyMustBeComputed()) {
			/* compute DoubleDeltaEnergy from DeltaEnergy */
            currentFileDesc.setDoubleDeltaEnergyPresence(true);
            if (trace) {
                System.out.println("trace[features] \tCompute double delta energy");
            }
            if (deltaType == SPHINX) {
                computeSphinxStyleDerivedCoefficients(currentFileDesc.getIndexOfDeltaEnergy(), currentFileDesc.getIndexOfDoubleDeltaEnergy(), 1,
                        SPHINX_DOUBLE_DELTA_WINDOW_SIZE);
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
// System.err.println("**** computeSphinxStyleDerivedCoefficients");
		/*
		 * Sphinx way for delta coefficients: d(t) = c(t+2) - c(t-2) => windowSize == 2
		 */
		/*
		 * Sphinx way for double delta coefficients: dd(t) = d(t+1) - d(t-1) => windowSize == 1
		 */
        float prev[];
        float current[];
        float next[];
        int nbFrames = currentData.size();

		/* Take care of the beginning of the signal */
        prev = currentData.get(0);
        for (int frameIndex = 0; frameIndex < windowSize; frameIndex++) {
            current = currentData.get(frameIndex);
            next = currentData.get(frameIndex + windowSize);
            for (int i = 0; i < coeffNb; i++) {
                current[firstDestCoeff + i] = next[firstSrcCoeff + i] - prev[firstSrcCoeff + i];
            }
        }

		/* Central part */
        for (int frameIndex = windowSize; frameIndex < nbFrames - windowSize; frameIndex++) {
            prev = currentData.get(frameIndex - windowSize);
            current = currentData.get(frameIndex);
            next = currentData.get(frameIndex + windowSize);
            for (int i = 0; i < coeffNb; i++) {
                current[firstDestCoeff + i] = next[firstSrcCoeff + i] - prev[firstSrcCoeff + i];
            }
        }

		/* Take care of the end of the signal */
        next = currentData.get(nbFrames - 1);
        for (int frameIndex = nbFrames - windowSize; frameIndex < nbFrames; frameIndex++) {
            prev = currentData.get(frameIndex - windowSize);
            current = currentData.get(frameIndex);
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
        int nbFrames = currentData.size();

// System.err.println("**** computeSPROStyleDerivedCoefficients");

		/* Take care of the beginning of the signal */
        prev2 = prev = current = next = currentData.get(0);
        next2 = currentData.get(1);
        for (int frameIndex = 0; frameIndex < 2; frameIndex++) {
            prev2 = prev;
            prev = current;
            current = next;
            next = next2;
            next2 = currentData.get(frameIndex + 2);
            for (int i = 0; i < coeffNb; i++) {
                current[firstDestCoeff + i] = 0.1f * (next[firstSrcCoeff + i] - prev[firstSrcCoeff + i]) + 0.2f
                        * (next2[firstSrcCoeff + i] - prev2[firstSrcCoeff + i]);
            }
        }

		/* Central part */
        for (int frameIndex = 2; frameIndex < nbFrames - 2; frameIndex++) {
            prev2 = prev;
            prev = current;
            current = next;
            next = next2;
            next2 = currentData.get(frameIndex + 2);
            for (int i = 0; i < coeffNb; i++) {
                current[firstDestCoeff + i] = 0.1f * (next[firstSrcCoeff + i] - prev[firstSrcCoeff + i]) + 0.2f
                        * (next2[firstSrcCoeff + i] - prev2[firstSrcCoeff + i]);
            }
        }

		/* Take care of the end of the signal */
        for (int frameIndex = nbFrames - 2; frameIndex < nbFrames; frameIndex++) {
            prev2 = prev;
            prev = current;
            current = next;
            next = next2;
            for (int i = 0; i < coeffNb; i++) {
                current[firstDestCoeff + i] = 0.1f * (next[firstSrcCoeff + i] - prev[firstSrcCoeff + i]) + 0.2f
                        * (next2[firstSrcCoeff + i] - prev2[firstSrcCoeff + i]);
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
        //System.out.println("debug[features] \t level = " + level);

        System.err.print("debug[features] \t filename = " + currentFilename + " inputType = " + initialDesc.getFeaturesFormat());
        System.err.print(" memory Dim = " + getDim() + " static Dim = " + getStaticDimension());
        System.err.println(" file Dim = " + initialDesc.getVectorSize());

        if (level > 1) {
            String ch = initialDesc.toString();
            ch.replaceAll("\\n", "\ndebug[features] \t");
            ch = "debug[features] \t" + ch;
            System.err.println(ch);
/*			System.out.println("debug[features] \t Desc E: " + (initialDesc.getEnergyPresence() ? "present" : "not present") + ", "
				+ (initialDesc.getEnergyNeeded() ? "needed" : "not needed"));
		System.out.println("debug[features] \t Desc S = " + (initialDesc.getStaticCoeffPresence() ? "present" : "not present") + ", "
				+ (initialDesc.getStaticCoeffNeeded() ? "needed" : "not needed"));
		System.out.println("debug[features] \t Desc DE = " + (initialDesc.getDeltaEnergyPresence() ? "present" : "not present") + ", "
				+ (initialDesc.getDeltaEnergyNeeded() ? "needed" : "not needed") + ", "
				+ (initialDesc.getDeltaEnergyMustBeComputed() ? "computed" : "not computed"));
		System.out.println("debug[features] \t Desc DS = " + (initialDesc.getDeltaCoeffPresence() ? "present" : "not present") + ", "
				+ (initialDesc.getDeltaCoeffNeeded() ? "needed" : "not needed") + ", "
				+ (initialDesc.getDeltaCoeffMustBeComputed() ? "computed" : "not computed"));
		System.out.println("debug[features] \t Desc DDE = " + (initialDesc.getDoubleDeltaEnergyPresence() ? "present" : "not present") + ", "
				+ (initialDesc.getDoubleDeltaEnergyNeeded() ? "needed" : "not needed") + ", "
				+ (initialDesc.getDoubleDeltaEnergyMustBeComputed() ? "computed" : "not computed"));
		System.out.println("debug[features] \t Desc DDS = " + (initialDesc.getDoubleDeltaCoeffPresence() ? "present" : "not present") + ", "
				+ (initialDesc.getDoubleDeltaCoeffNeeded() ? "needed" : "not needed") + ", "
				+ (initialDesc.getDoubleDeltaCoeffMustBeComputed() ? "computed" : "not computed"));*/
            if (currentData != null) {
                System.err.println("debug[features] \t nb = " + currentData.size() + " size = " + currentData.size() * getDim());
                if (level > 2) {
                    for (int f = 0; f < currentData.size(); f++) {
                        System.err.print("debug[features] \t frame = " + f + " values =");
                        float[] frame = getFeature(f);
                        for (int d = 0; d < getDim(); d++) {
                            System.err.print(" " + frame[d]);
                        }
                        System.err.println();
                    }
                }
            } else {
                System.err.println("debug[features] \t no data");
            }
        }
    }

    /**
     * Free data.
     */
    protected void freeData() {
        if (currentData != null) {
            //currentData.clear();
            currentData = null;
            //System.gc();
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
    public int getDim() {
        if (currentFileDesc == null) {
            //System.err.println("*** initialDesc");
            return initialDesc.getTrimmedFeatureDesc().getVectorSize();
        } else {
            //System.err.println("*** currentFileDesc");
            return currentFileDesc.getVectorSize();
        }
    }

    /**
     * Get the feature number \e index of the current show.
     * Not safety method to get a feature, the show should be change by the program
     *
     * @param index the frame index
     *
     * @return the frame, as a float array; it is a reference to the actual storage of the frame, not a copy
     */
    public float[] getFeature(int index) {
        try {
            return currentData.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Features: get() with wrong frame index (" + index + " -- nb of frames:" + currentData.size() + ")");
        }
    }

    /**
     * Get feature number \e index after having check if the current show is the show.
     * Safety to get a feature, the show is validated. The validation costs time.
     *
     * @param index the frame index
     *
     * @return the frame, as a float array; it is a reference to the actual storage of the frame, not a copy
     * @throws IOException
     * @throws DiarizationException
     */
    public float[] getFeature(String show, int index) throws DiarizationException, IOException {
        try {
            setCurrentShow(show);
            return currentData.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Features: get() with wrong frame index (" + index + " -- nb of frames:" + currentData.size() + ")");
        }
    }

    /**
     * Get the number of frames.
     *
     * @return the number of frames
     */
    public int getNumberOfFeatures() {
        if (currentData != null) {
            return currentData.size();
        }
        return 0;
    }

    /**
     * Get the static dimension of the features.
     *
     * @return the static dimension of the features: sizeof(static + E)
     */
    public int getStaticDimension() {
        FeatureDescription tmpFeatureDesc = currentFileDesc;
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
    public ArrayList<GMM> getUBMs() {
        return UBMs;
    }

    /**
     * Sets the UBM.
     *
     * @param ubm the new UBM
     */
    public void setUBMs(ArrayList<GMM> ubm) {
        UBMs = ubm;
    }

    /**
     * Normalize.
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void normalize() throws DiarizationException, IOException {
        if (initialDesc.getCentered()) {
            FeatureNormalization featureNorm = new FeatureNormalization(this, initialDesc.getReduced(), initialDesc.getNormalizationWindowSize());
            if (initialDesc.getNormalizationMethod() == FeatureDescription.NORM_BY_SEGMENT) {
//				if (trace) System.out.println("feature: NORM_BY_SEGMENT ");
                featureNorm.normalizeClusterSetBySegment(clusterSet);
            } else if (initialDesc.getNormalizationMethod() == FeatureDescription.NORM_BY_SLIDING) {
//				if (trace) System.out.println("feature: NORM_BY_SLIDING ");
                featureNorm.normalizeClusterSetByWindow(clusterSet);
            } else if (initialDesc.getNormalizationMethod() == FeatureDescription.NORM_BY_CLUSTER) {
//				if (trace) System.out.println("feature: NORM_BY_CLUSTER ");
                featureNorm.normalizeClusterSetByCluster(clusterSet);
            } else if (initialDesc.getNormalizationMethod() == FeatureDescription.NORM_BY_WARPING) {
//				if (trace) System.out.println("feature: NORM_BY_WARPING ");
                featureNorm.warpFile();
            } else if (initialDesc.getNormalizationMethod() == FeatureDescription.NORM_BY_WARPING_AND_CR) {
//				if (trace) System.out.println("feature: NORM_BY_WARPING_AND_CR ");
                featureNorm.warpFile();
                featureNorm.normalizeClusterSetBySegment(clusterSet);
            } else if (initialDesc.getNormalizationMethod() == FeatureDescription.NORM_BY_CR_AND_MAPPING) {
//				if (trace) System.out.println("feature: NORM_BY_CR_AND_MAPPING ");
                featureNorm.normalizeClusterSetBySegment(clusterSet);
                featureNorm.mapFeatureClusterSet(clusterSet, UBMs);
            } else if (initialDesc.getNormalizationMethod() == FeatureDescription.NORM_BY_WARPING_AND_CRByCluster) {
//				if (trace) System.out.println("feature: NORM_BY_WARPING_AND_CRByCluster ");
                featureNorm.warpFile();
                featureNorm.normalizeClusterSetByCluster(clusterSet);
            } else {
                throw new DiarizationException("Features (" + currentFilename + "): normalization method don't exist");
            }
        }
    }

    protected long memoryUsed() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    protected double memoryRate(long before, long after) {
        sizeInMemory += (after - before);
        if (sizeInMemory < 0) {
            sizeInMemory = 0;
        }
        long max = Runtime.getRuntime().maxMemory();
        double rate = (double) (sizeInMemory) / (double) max;

        System.err.println(" // % mem free=" + rate +
                " used=" + (sizeInMemory) / (1024 * 1024) + " max=" + max / (1024 * 1024));
        return rate;
    }

    /**
     * Read.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    protected void read() throws IOException, DiarizationException {
        if (dataMap.containsKey(currentShow)) {
            System.err.println("[FeatureSet] read : get data from map " + currentShow);
            currentData = dataMap.get(currentShow);
        } else {
            Runtime.getRuntime().gc();
            System.err.print("[FeatureSet] read : compute data " + currentShow);
            long memBefore = memoryUsed();
            currentFileDesc = (FeatureDescription) (initialDesc.clone());
            int inputType = currentFileDesc.getFeaturesFormat();
            freeData();
            if (inputType == SPRO4) {
                readSPRO4();
            } else if (inputType == SPHINX) {
                readSphinx();
            } else if (inputType == HTK) {
                readHTK();
            } else if (inputType == GZTXT) {
                readGZTxt();
            } else if (inputType == AUDIO16Khz2SPHINXMFCC) {
                if (trace) System.err.printf("AUDIO16Khz2SPHINXMFCC");
                currentData = FeatureFactory.MakeMFCCFeature(fontend16kHzConfigURL, currentFilename, currentFileDesc);
                deltaType = SPHINX;
                makeChangePositionOfEnergy();
            } else if (inputType == AUDIO8kHz2SPHINXMFCC) {
                if (trace) System.err.printf("AUDIO8kHz2SPHINXMFCC");
                currentData = FeatureFactory.MakeMFCCFeature(fontend8kHzConfigURL, currentFilename, currentFileDesc);
                deltaType = SPHINX;
                makeChangePositionOfEnergy();
            } else if (inputType == FEATURESETTRANSFORMATION) {
                if (trace) System.err.printf("FEATURESETTRANSFORMATION");
                source.setCurrentShow(currentShow);
                copyData();
            } else {
                throw new DiarizationException("Features (" + ParameterFeature.FeaturesTypeString[inputType] + "): unknown input feature type " + currentFilename);
            }
            //debug();
            computeMissingCoefficients();
            removeUnneededCoefficients();
            normalize();
            currentFileDesc = initialDesc.getTrimmedFeatureDesc();
            long memAfter = memoryUsed();

            if (trace) debug();
            double rate = memoryRate(memBefore, memAfter);
            if (rate > memoryOccupationRate) {
                while (rate > 0.75 * memoryOccupationRate) {
                    if (dataMap.isEmpty()) {
                        break;
                    }
                    if (dataMap.size() < 25) {
                        //String key = dataMap.pollFirstEntry().getKey();
                        // method pollFirstEntry() is undefined ... ?
                        String key = dataMap.firstKey();
                        dataMap.remove(key);
                        System.err.print("[FeatureSet] read : remove data from " + key);
                    } else {
                        dataMap.clear();
                        sizeInMemory = 0;
                        System.err.print("[FeatureSet] read : clear data ");
                    }
                    Runtime.getRuntime().gc();
                    long memRemove = memoryUsed();
                    rate = memoryRate(memAfter, memRemove);
                    memAfter = memRemove;
                }
            }
            dataMap.put(currentShow, currentData);
        }
    }

    /**
     * Copy data.
     */
    private void copyData() {
        currentData = new FeatureData(source.getNumberOfFeatures());
        currentData.addAll(source.currentData);
    }

    /**
     * Read gz txt.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void readGZTxt() throws IOException {
        currentData = new FeatureData();
        ArrayList<String> lst = StringListFileIO.read(currentFilename, true);
        for (int i = 0; i < lst.size(); i++) {
            StringTokenizer stok = new StringTokenizer(lst.get(i), " ");
            float[] tmpframe = new float[getDim()];
            stok.nextToken();
            for (int j = 0; j < getDim(); j++) {
                tmpframe[j] = Float.parseFloat(stok.nextToken());
            }
            currentData.add(tmpframe);
        }
    }

    /**
     * Read htk.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    private void readHTK() throws IOException, DiarizationException {
        int WITHE = 0100; // _E Data is with log energy
        //int HASNULLE = 0200;  // _N absolute energy suppressed
        int WITHD = 0400; // _D Data contains delta coefficients
        int WITHA = 01000; // _A Data contains delta-delta coefficients
        //int HASZEROM = 04000; // _Z zero meaned
        //int HASCOMPX = 02000; // _C is compressed
        //int HASCRCC = 010000; // _K has CRC check
        //int HASZEROC = 020000; // _0 0'th Cepstra included
        //int HASVQ  =  040000; // _V has VQ index attached
        //int HASTHIRD = 0100000; // _T has Delta-Delta-Delta index attached

        String mode = "rb";
        IOFile file = new IOFile(currentFilename, mode, swap);
        file.open();

        int nb = file.readInt();
        //System.err.println("***** NB:"+nb);

        int samplePeriod = file.readInt();
        if (samplePeriod != 100000) {
            throw new DiarizationException("Features (" + currentFilename
                    + "): the feature sample Periode doesn't match the standart one -- file: " + samplePeriod / 1000 + ", user: 100");

        }

        short vectSize = (short) (file.readShort() / 4);
        if (currentFileDesc.getVectorSize() != vectSize) {
            if (Short.reverseBytes(vectSize) == currentFileDesc.getVectorSize()) {
                vectSize = Short.reverseBytes(vectSize);
                file.setSwap(true);
            } else {
                throw new DiarizationException("Features (" + currentFilename
                        + "): the feature vector dimension in the file doesn't match what the user specified -- file: " + vectSize + ", user: "
                        + currentFileDesc.getVectorSize());
            }
        }

        int flag = file.readShort();
        checkFlag(currentFilename, flag, WITHE, WITHD, WITHA);

        currentData = new FeatureData(nb);

        for (int i = 0; i < nb; i++) {
            float[] tmpFrame = new float[getDim()];
            currentData.add(tmpFrame);
            file.readFloatArray(currentData.get(i), getDim());
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

        int nb = (int) (file.len() - headerSize) / (getDim() * 4); // 4: size of
        // float
        if ((file.len() - headerSize) % (getDim() * 4) != 0) {
            throw new DiarizationException("Features (" + currentFilename
                    + "): the feature vector dimension in the file doesn't seem to match what the user specified -- file: N/A (Sphinx), user: "
                    + currentFileDesc.getVectorSize());
        }

        currentData = new FeatureData(nb);
        for (int i = 0; i < nb; i++) {
            float[] tmpFrame = new float[getDim()];
            currentData.add(tmpFrame);
            file.readFloatArray(currentData.get(i), getDim());
        }
        file.close();
        makeChangePositionOfEnergy();
    }

    /**
     * Change position of energy.
     */
    private void makeChangePositionOfEnergy() {
        if (changePositionOfEnergy) {
            for (int i = 0; i < currentData.size(); i++) {
                float[] frame = currentData.get(i);
                float old = frame[0];
                for (int j = 0; j < getDim() - 1; j++) {
                    frame[j] = frame[j + 1];
                }
                frame[getDim() - 1] = old;
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
        if (currentFileDesc.getVectorSize() != vectSize) {
            if (Short.reverseBytes(vectSize) == currentFileDesc.getVectorSize()) {
                vectSize = Short.reverseBytes(vectSize);
                file.setSwap(true);
            } else {
                throw new DiarizationException("Features (" + currentFilename
                        + "): the feature vector dimension in the file doesn't match what the user specified -- file: " + vectSize + ", user: "
                        + currentFileDesc.getVectorSize());
            }
        }

        int flag = file.readInt();
        checkFlag(currentFilename, flag, WITHE, WITHD, WITHA);

        file.readInt(); // rate

        long headerSize = 2 + 4 + 4; // sizeof(vectSize) + sizeof(flag) +
        // sizeof(rate)

        int nb = (int) (file.len() - headerSize) / (getDim() * 4); // 4 : size
        // of float
        if ((file.len() - headerSize) % (getDim() * 4) != 0) {
            throw new DiarizationException("Features (" + currentFilename + "): file appears to be corrupted");
        }
        currentData = new FeatureData(nb);
        for (int i = 0; i < nb; i++) {
            float[] tmpFrame = new float[getDim()];
            currentData.add(tmpFrame);
            file.readFloatArray(currentData.get(i), getDim());
        }
        file.close();
    }

    private void checkFlag(String currentFilename, int flag, int WITHE, int WITHD, int WITHA) throws DiarizationException {
        if (currentFileDesc.getEnergyPresence() && ((flag & WITHE) == 0)) {
            throw new DiarizationException("Features (" + currentFilename + "): energy should be present in file but is not (1)");
        }
        if ((!currentFileDesc.getEnergyPresence()) && ((flag & WITHE) == 1)) {
            throw new DiarizationException("Features (" + currentFilename + "): energy should not be present but is present in file (2)");
        }
        if (currentFileDesc.getDeltaCoeffPresence() && ((flag & WITHD) == 0)) {
            throw new DiarizationException("Features (" + currentFilename + "): delta should be present in file but are not (3)");
        }
        if ((!currentFileDesc.getDeltaCoeffPresence()) && ((flag & WITHD) == 1)) {
            throw new DiarizationException("Features (" + currentFilename + "): delta should not be present but are present in file (4)");
        }
        if (currentFileDesc.getDeltaEnergyPresence() && (((flag & WITHE) == 0) || ((flag & WITHD) == 0))) {
            throw new DiarizationException("Features (" + currentFilename + "): delta energy should be present in file but is not (5)");
        }
        if ((!currentFileDesc.getDeltaEnergyPresence()) && (((flag & WITHE) == 1) && ((flag & WITHD) == 1))) {
            throw new DiarizationException("Features (" + currentFilename + "): delta energy should not be present but is present in file (6)");
        }
        if (currentFileDesc.getDoubleDeltaCoeffPresence() && ((flag & WITHA) == 0)) {
            throw new DiarizationException("Features (" + currentFilename + "): delta delta should be present in file but are not (7)");
        }
        if ((!currentFileDesc.getDoubleDeltaCoeffPresence()) && ((flag & WITHA) == 1)) {
            throw new DiarizationException("Features (" + currentFilename + "): delta delta should not be present but are present in file (8)");
        }
        if (currentFileDesc.getDoubleDeltaEnergyPresence() && (((flag & WITHE) == 0) || ((flag & WITHA) == 0))) {
            throw new DiarizationException("Features (" + currentFilename + "): double delta energy should be present in file but is not (9)");
        }
        if ((!currentFileDesc.getDoubleDeltaEnergyPresence()) && (((flag & WITHE) == 1) && ((flag & WITHA) == 1))) {
            throw new DiarizationException("Features (" + currentFilename + "): double delta energy should not be present but is present in file (10)");
        }

    }

    /**
     * Removes the coefficients.
     *
     * @param indexOfFirstCoeff the index of first coeff
     * @param nbOfCoeffs the nb of coeffs
     */
    private void removeCoefficients(int indexOfFirstCoeff, int nbOfCoeffs) {
        int nbOfFrames = currentData.size();
        int currentDimension = currentFileDesc.getVectorSize();
        for (int i = 0; i < nbOfFrames; i++) {
            float[] currentFrame = currentData.get(i);
            float[] newFrame = new float[currentDimension - nbOfCoeffs];
            int j, k;
            for (j = 0, k = 0; j < indexOfFirstCoeff; j++, k++) {
                newFrame[k] = currentFrame[j];
            }
            for (j = indexOfFirstCoeff + nbOfCoeffs; j < currentDimension; j++, k++) {
                newFrame[k] = currentFrame[j];
            }
            currentData.set(i, newFrame);
        }
    }

    /**
     * Removes the unneeded coefficients.
     */
    private void removeUnneededCoefficients() {
        if (currentFileDesc.getEnergyMustBeDeleted()) {
            if (trace) {
                System.out.println("trace[features] \tDelete energy " + currentFileDesc.getIndexOfEnergy());
            }
            removeCoefficients(currentFileDesc.getIndexOfEnergy(), 1);
            currentFileDesc.setEnergyPresence(false);
        }
        if (currentFileDesc.getDeltaEnergyMustBeDeleted()) {
            if (trace) {
                System.out.println("trace[features] \tDelete delta energy " + currentFileDesc.getIndexOfDeltaEnergy());
            }
            removeCoefficients(currentFileDesc.getIndexOfDeltaEnergy(), 1);
            currentFileDesc.setDeltaEnergyPresence(false);
        }
        if (currentFileDesc.getDoubleDeltaEnergyMustBeDeleted()) {
            if (trace) {
                System.out.println("trace[features] \tDelete double delta energy " + currentFileDesc.getIndexOfDoubleDeltaEnergy());
            }
            removeCoefficients(currentFileDesc.getIndexOfDoubleDeltaEnergy(), 1);
            currentFileDesc.setDoubleDeltaEnergyPresence(false);
        }
        if (currentFileDesc.getDeltaCoeffMustBeDeleted()) {
            if (trace) {
                System.out.println("trace[features] \tDelete delta coeff " + currentFileDesc.getIndexOfFirstDeltaCoeff() + " (" + currentFileDesc.getBaseSize()
                        + " coeff)");
            }
            removeCoefficients(currentFileDesc.getIndexOfFirstDeltaCoeff(), currentFileDesc.getBaseSize());
            currentFileDesc.setDeltaCoeffPresence(false);
        }
        if (currentFileDesc.getDoubleDeltaCoeffMustBeDeleted()) {
            if (trace) {
                System.out.println("trace[features] \tDelete double delta coeff " + currentFileDesc.getIndexOfFirstDoubleDeltaCoeff() + " ("
                        + currentFileDesc.getBaseSize() + " coeff)");
            }
            removeCoefficients(currentFileDesc.getIndexOfFirstDoubleDeltaCoeff(), currentFileDesc.getBaseSize());
            currentFileDesc.setDoubleDeltaCoeffPresence(false);
        }
    }

    /**
     * Check if the current show is the one with index \e showIndex; if not, the show with this index is read.
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
            if (trace) {
                System.out.println("trace[features] \t in setCurrentShow(), will read file: " + currentFilename);
            }
            read();
        }
    }

    public boolean getChangePositionOfEnergy() {
        return changePositionOfEnergy;
    }

    public void setChangePositionOfEnergy(boolean v) {
        changePositionOfEnergy = v;
    }

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
    public void write(String show, String outputFeatureMask, FeatureDescription featureOutputDescription) throws DiarizationException, IOException {
        String filename = IOFile.getFilename(outputFeatureMask, show);

        int outputType = featureOutputDescription.getFeaturesFormat();
        if (outputType == SPRO4) {
            writeSPRO4(filename);
        } else if (outputType == SPHINX) {
            writeSphinx(filename);
        } else if (outputType == GZTXT) {
            writeGZTxt(filename);
        } else {
            throw new DiarizationException("Features (" + ParameterFeature.FeaturesTypeString[outputType] + "): unknown output feature type " + currentFilename);
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
            for (int i = 0; i < currentData.size(); i++) {
                float[] frame = currentData.get(i);
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
        if (trace) {
            System.out.println("trace[features] \t sphinx dim = " + getDim() + " staticDim = " + getStaticDimension());
        }
        String mode = "wb";
        IOFile file = new IOFile(filename, mode, swap);
        file.open();
        int dim = getDim();
        int baseDim = currentFileDesc.getBaseSize();
        file.writeInt(currentData.size() * dim);
        for (int i = 0; i < currentData.size(); i++) {
            float frame[] = currentData.get(i);
            if (currentFileDesc.getEnergyPresence()) {
                file.writeFloat(frame[currentFileDesc.getIndexOfEnergy()]);
            }
            if (currentFileDesc.getStaticCoeffPresence()) {
                for (int j = currentFileDesc.getIndexOfFirstStaticCoeff(); j < currentFileDesc.getIndexOfFirstStaticCoeff() + baseDim; j++) {
                    file.writeFloat(frame[j]);
                }
            }
            if (currentFileDesc.getDeltaEnergyPresence()) {
                file.writeFloat(frame[currentFileDesc.getIndexOfDeltaEnergy()]);
            }
            if (currentFileDesc.getDeltaCoeffPresence()) {
                for (int j = currentFileDesc.getIndexOfFirstDeltaCoeff(); j < currentFileDesc.getIndexOfFirstDeltaCoeff() + baseDim; j++) {
                    file.writeFloat(frame[j]);
                }
            }
            if (currentFileDesc.getDoubleDeltaEnergyPresence()) {
                file.writeFloat(frame[currentFileDesc.getIndexOfDoubleDeltaEnergy()]);
            }
            if (currentFileDesc.getDoubleDeltaCoeffPresence()) {
                for (int j = currentFileDesc.getIndexOfFirstDoubleDeltaCoeff(); j < currentFileDesc.getIndexOfFirstDoubleDeltaCoeff() + baseDim; j++) {
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
        file.writeShort((short) getDim());
        if (trace) {
            System.out.println("trace[features] \t spro 4 dim " + getDim());
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
        if (trace) {
            System.out.println("trace[features] \t flag=" + flag);
        }
        file.writeInt(0);
        if (trace) {
            System.out.println("trace[features] \t rate=0 (unsupported)");
        }
        for (int i = 0; i < currentData.size(); i++) {
            file.writeFloatArray(currentData.get(i), getDim());
        }
        file.close();
    }
}
