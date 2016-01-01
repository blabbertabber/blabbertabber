/**
 * 
 * <p>
 * SAdjSeg
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          move the segmentation boundary don't touch the last boundary
 * 
 */

package fr.lium.spkDiarization.tools;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class to move the start and end of segments in low energy area.
 */
public class SAdjSeg {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MfccMlpConcat.class.getName());

	/**
	 * Gets the energy
	 * 
	 * @param featureSet the feature set
	 * @param indexOfEnergy the index of energy
	 * @param i the i
	 * @return the energy
	 * @throws DiarizationException the diarization exception
	 */
	public static double getE(AudioFeatureSet featureSet, int indexOfEnergy, int i) throws DiarizationException {
		return featureSet.getFeatureUnsafe(i)[indexOfEnergy];
	}

	/**
	 * Move the start and the end (length) of the segment in low energy zone.
	 * 
	 * @param featureSet the feature set
	 * @param segment the segment
	 * @param indexOfEnergy the index of energy
	 * @throws DiarizationException the diarization exception
	 */
	public static void moveStartAndEndOfSegment(AudioFeatureSet featureSet, Segment segment, int indexOfEnergy) throws DiarizationException {
		int start = segment.getStart();
		int length = segment.getLength();
		int end = start + length;

		int silenceStart = SAdjSeg.posMaxSil(featureSet, start, 25, 5, indexOfEnergy);
		segment.setStart(silenceStart);
		segment.setLength(end - silenceStart);

		start = segment.getStart();
		length = segment.getLength();
		end = start + length;
		int silenceEnd = SAdjSeg.posMaxSil(featureSet, end, 25, 5, indexOfEnergy);
		if ((silenceEnd - silenceStart) < 0) {
			segment.setLength(0);
		} else {
			segment.setLength(silenceEnd - silenceStart);
		}
	}

	/**
	 * Make: the process.
	 * 
	 * @param featureSet the feature set
	 * @param clusterset the clusterset
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterset, Parameter parameter) throws DiarizationException, IOException {
		logger.info("Adjust the bounady of segmentation");
		ArrayList<Segment> segmentList = clusterset.getSegmentVectorRepresentation();
		int indexOfEnergy = parameter.getParameterInputFeature().getFeaturesDescription().getIndexOfEnergy();
		// adjust segment boundaries
		if (indexOfEnergy < 0) {
			throw new DiarizationException("SAdjSeg: main() error: energy not present");
		}
		int size = segmentList.size();
		int nb = 0;
		Iterator<Segment> itSeg = segmentList.iterator();
		while (itSeg.hasNext() && (nb < (size - 1))) {
			Segment seg = itSeg.next();
			featureSet.setCurrentShow(seg.getShowName());
			moveStartAndEndOfSegment(featureSet, seg, indexOfEnergy);
			nb++;
		}

		ClusterSet res = new ClusterSet();
		res.addVector(segmentList);
		return res;
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "SAdjSeg");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);

				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);

				ClusterSet clusterSetResult = make(featureSet, clusterSet, parameter);

				MainTools.writeClusterSet(parameter, clusterSetResult, false);
			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
	}

	/**
	 * Position of the lowest mean energy at #index + or - #seachDecay.
	 * 
	 * @param featureSet the feature set
	 * @param index the index (start or end of a segment)
	 * @param seachDecay the seach decay on the left and right of the #index
	 * @param sizeOfWindow number of energy feature to compute the energy mean
	 * @param indexOfEnergy the index of energy in the #featureSet
	 * @return the int
	 * @throws DiarizationException the diarization exception
	 */
	public static int posMaxSil(AudioFeatureSet featureSet, int index, int seachDecay, int sizeOfWindow, int indexOfEnergy) throws DiarizationException {
		int indexMin;
		int nbFeatures = featureSet.getNumberOfFeatures();

		int indexMeanLeft = Math.max(index - seachDecay - sizeOfWindow, 0);
		int indexMeanRight = Math.min(index + seachDecay, nbFeatures - 1);

		for (int i = indexMeanLeft; i < indexMeanRight; i++) {
			if (featureSet.compareFreatures(i, i + 1)) {
				logger.warning("two consecutive features are the same (" + i + "," + i + "+1) with pos = " + index);
				return index;
			}
		}

		indexMeanLeft = Math.max(index - seachDecay - sizeOfWindow, 0);
		indexMeanRight = Math.min((index - seachDecay) + sizeOfWindow, nbFeatures);
		int nb = 0;
		double energy = 0.0;
		for (int i = indexMeanLeft; i <= indexMeanRight; i++) {
			energy += getE(featureSet, indexOfEnergy, i);
			nb++;
		}
		indexMeanLeft = Math.max(index - seachDecay, 0);
		indexMeanRight = Math.min(index + seachDecay, nbFeatures);
		double min = energy / nb;
		indexMin = indexMeanLeft;
		for (int i = indexMeanLeft + 1; i < indexMeanRight; i++) {
			if ((i - sizeOfWindow - 1) >= 0) {
				energy -= SAdjSeg.getE(featureSet, indexOfEnergy, i - sizeOfWindow - 1);
				nb--;
			}
			if ((i + sizeOfWindow) < nbFeatures) {
				energy += SAdjSeg.getE(featureSet, indexOfEnergy, i + sizeOfWindow);
				nb++;
			}
			double tmp = energy / nb;
			if (tmp < min) {
				min = tmp;
				indexMin = i;
			}
		}
		return indexMin;
	}

	/**
	 * Log the available list of parameters.
	 * 
	 * @param parameter the parameter list
	 * @param program the program name
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("Program name = " + program);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterInputFeature().logAll(); // fInMask
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
			parameter.getParameterAdjustSegmentation().logAll();
		}
	}

}