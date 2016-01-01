/**
 * 
 * <p>
 * SSplitSeg
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
 *          Find silence and split a segmentation
 * 
 */

package fr.lium.spkDiarization.tools;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class SSplitSeg.
 */
public class SSplitSeg {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SSplitSeg.class.getName());

	/**
	 * The Class FilterSort.
	 */
	private class FilterSort implements Comparator<Segment> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Segment segment1, Segment segment2) {
			int length1 = segment1.getLength();
			int length2 = segment2.getLength();
			if (length1 == length2) {
				int start1 = Math.abs(segment1.getStart() - Integer.parseInt(segment1.getInformation(keyMid)));
				int start2 = Math.abs(segment2.getStart() - Integer.parseInt(segment2.getInformation(keyMid)));
				return new Integer(start1).compareTo(new Integer(start2));
			}
			return new Integer(length2).compareTo(new Integer(length1));
		}
	}

	/**
	 * The Class FilterSort2.
	 */
	private class FilterSort2 implements Comparator<Segment> {

		/*
		 * (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(Segment segment1, Segment segment2) {
			double score1 = segment1.getScore();
			double score2 = segment2.getScore();
			return Double.compare(score2, score1);
		}
	}

	/** The Constant keyMid. */
	private static final String keyMid = "mid";

	/**
	 * Find silence.
	 * 
	 * @param features the features
	 * @param gmmList the gmm list
	 * @param start the start
	 * @param end the end
	 * @param sizeWindow the size window
	 * @param fliterSegmentList the fliter segment list
	 * @param rate the rate
	 * @throws DiarizationException the diarization exception
	 */
	public static void findSilence(AudioFeatureSet features, GMMArrayList gmmList, int start, int end, int sizeWindow, ArrayList<Segment> fliterSegmentList, float rate) throws DiarizationException {

		fliterSegmentList.clear();
		int nbFeature = features.getNumberOfFeatures();
		String showName = features.getCurrentShowName();

		int nbGmm = gmmList.size();
		ArrayList<Double> likelihoodList = new ArrayList<Double>(nbGmm);
		for (int i = 0; i < nbGmm; i++) {
			likelihoodList.add(0.0);
			gmmList.get(i).score_initialize();
		}

		int indexMeanLeft = Math.max(start - sizeWindow, 0);
		int indexMeanRight = Math.min(start + sizeWindow, nbFeature);
		for (int i = indexMeanLeft; i <= indexMeanRight; i++) {
			for (int j = 0; j < nbGmm; j++) {
				likelihoodList.set(j, likelihoodList.get(j) + gmmList.get(j).score_getAndAccumulate(features, i));
			}
		}
		double maxLikelihood = likelihoodList.get(0);
		for (int j = 1; j < nbGmm; j++) {
			if (likelihoodList.get(j) > maxLikelihood) {
				maxLikelihood = likelihoodList.get(j);
			}
		}

		indexMeanLeft = Math.max(start, 0);
		indexMeanRight = Math.min(end, nbFeature);
		Cluster clusterSilence = new Cluster("SILENCE");
		Segment segment = new Segment(showName, indexMeanLeft, 1, clusterSilence, rate);
		segment.setScore(maxLikelihood);
		fliterSegmentList.add(segment);

		for (int i = indexMeanLeft + 1; i < indexMeanRight; i++) {
			if ((i - sizeWindow - 1) >= 0) {
				for (int j = 0; j < nbGmm; j++) {
					likelihoodList.set(j, likelihoodList.get(j)
							- gmmList.get(j).score_getAndAccumulate(features, i - sizeWindow - 1));
				}
			}
			if ((i + sizeWindow) < nbFeature) {
				for (int j = 0; j < nbGmm; j++) {
					likelihoodList.set(j, likelihoodList.get(j)
							+ gmmList.get(j).score_getAndAccumulate(features, i + sizeWindow));
				}
			}
			maxLikelihood = likelihoodList.get(0);
			for (int j = 1; j < nbGmm; j++) {
				if (likelihoodList.get(j) > maxLikelihood) {
					maxLikelihood = likelihoodList.get(j);
				}
			}
			Segment segmentI = new Segment(showName, i, 1, clusterSilence, rate);
			segmentI.setScore(maxLikelihood);
			fliterSegmentList.add(segmentI);
		}
		Collections.sort(fliterSegmentList, (new SSplitSeg()).new FilterSort2());
	}

	/**
	 * Gets the silence.
	 * 
	 * @param filterSegmentSet the filter segment set
	 * @param start the start
	 * @param length the length
	 * @param minLength the min length
	 * @param filterSegmentList the filter segment list
	 * @return the silence
	 */
	public static int getSilence(TreeSet<Segment> filterSegmentSet, int start, int length, int minLength, ArrayList<Segment> filterSegmentList) {
		int end = length + start;
		int middle = start + (length / 2);
		start += minLength;
		end -= minLength;
		Iterator<Segment> itSeg = filterSegmentSet.iterator();
		filterSegmentList.clear();
		while (itSeg.hasNext()) {
			Segment segmentFromSet = itSeg.next();
			int startFromSet = segmentFromSet.getStart();
			int lengthFromSet = segmentFromSet.getLength();
			int endFromSet = startFromSet + lengthFromSet;
			if ((startFromSet > start) && (endFromSet < end)) {
				Segment tmpSegment = (segmentFromSet.clone());
				tmpSegment.setInformation(keyMid, middle);
				filterSegmentList.add(tmpSegment);
			}
		}
		Collections.sort(filterSegmentList, (new SSplitSeg()).new FilterSort());
		return filterSegmentList.size();
	}

	/**
	 * Make.
	 * 
	 * @param featureSet the feature set
	 * @param clusterSet the cluster set
	 * @param gmmList the gmm list
	 * @param filterClusterSet the filter cluster set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws DiarizationException the diarization exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static ClusterSet make(AudioFeatureSet featureSet, ClusterSet clusterSet, GMMArrayList gmmList, ClusterSet filterClusterSet, Parameter parameter) throws DiarizationException, IOException {
		int maxLength = parameter.getParameterSegmentationSplit().getSegmentMaximumLength();
		int minLen = parameter.getParameterSegmentationSplit().getSegmentMinimumLength();
		ArrayList<Segment> segmentList = clusterSet.getSegmentVectorRepresentation();
		TreeSet<Segment> filterSegmentSet = new TreeSet<Segment>();
		ArrayList<String> tokens = new ArrayList<String>();
		String sep = ",";

		StringTokenizer stok = new StringTokenizer(parameter.getParameterSegmentationFilterFile().getClusterFilterName(), sep);
		while (stok.hasMoreTokens()) {
			tokens.add(stok.nextToken());
		}

		for (int i = 0; i < tokens.size(); i++) {
			// String token = tokens.get(i);
			// int idxCluster = fltClusters.getNameIndex(token);
			String idxCluster = tokens.get(i);
			if (filterClusterSet.containsCluster(idxCluster)) {
				Cluster filterCluster = filterClusterSet.getCluster(idxCluster);
				Iterator<Segment> itSeg = filterCluster.iterator();
				while (itSeg.hasNext()) {
					filterSegmentSet.add(itSeg.next());
				}
			}
		}

		// --- Split segment based upon silence segment ---
		int i = 0;
		int size = segmentList.size();

		ArrayList<Segment> segmentListCopy = new ArrayList<Segment>();
		for (i = 0; i < size; i++) {
			Segment seg = segmentList.get(i);
			int l = seg.getLength();
			int s = seg.getStart();
			if (l > maxLength) {
				ArrayList<Segment> tmpFilterSegmentList = new ArrayList<Segment>();
				if (SSplitSeg.getSilence(filterSegmentSet, s, l, minLen, tmpFilterSegmentList) >= 0) {
					segmentListCopy.clear();
					for (int cpt = 0; cpt < tmpFilterSegmentList.size(); cpt++) {
						segmentListCopy.add(tmpFilterSegmentList.get(cpt));
					}
					SSplitSeg.splitSeg(segmentList, i, maxLength, minLen, segmentListCopy, 0);
				} else {
					logger.warning("no split segment, start=" + seg.getStart() + " " + seg.getLength());
				}
			}
		}

		// --- Check segment need to be split using gmm ---
		int wsize = 10;
		i = 0;
		while (i < segmentList.size()) {
			Segment segment = segmentList.get(i);
			featureSet.setCurrentShow(segment.getShowName());
			int length = segment.getLength();
			int start = segment.getStart();
			int end = length + start;
			if (length > maxLength) {
				logger.finer("split segment using gmm, start=" + segment.getStart() + " " + segment.getLength());
				ArrayList<Segment> tmpFilterSegmentList = new ArrayList<Segment>();
				SSplitSeg.findSilence(featureSet, gmmList, start + minLen, end - minLen, wsize, tmpFilterSegmentList, segment.getRate());
				SSplitSeg.splitSeg(segmentList, i, maxLength, minLen, tmpFilterSegmentList, 0);
			}
			i++;
		}

		// --- set the final segmentation ---
		ClusterSet clusterSetResult = new ClusterSet();
		clusterSetResult.addVector(segmentList);
		return clusterSetResult;
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
			info(parameter, "SSplitSeg");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);

				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
				// Compute Model
				GMMArrayList gmmList = MainTools.readGMMContainer(parameter);

				ClusterSet filterClusterSet = new ClusterSet();
				filterClusterSet.read(parameter.show, parameter.getParameterSegmentationFilterFile());

				ClusterSet clusterSetResult = make(featureSet, clusterSet, gmmList, filterClusterSet, parameter);

				MainTools.writeClusterSet(parameter, clusterSetResult, false);

			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		}
	}

	/**
	 * Split seg.
	 * 
	 * @param segmentList the segment list
	 * @param currentIndex the current index
	 * @param maxLength the max length
	 * @param minLength the min length
	 * @param filterSegmentList the filter segment list
	 * @param dec the dec
	 */
	public static void splitSeg(ArrayList<Segment> segmentList, int currentIndex, int maxLength, int minLength, ArrayList<Segment> filterSegmentList, int dec) {
		int length = segmentList.get(currentIndex).getLength();
		if (length > maxLength) {
			int start = segmentList.get(currentIndex).getStart();
			int end = start + length;
			if (filterSegmentList.size() > 0) {
				int index = filterSegmentList.get(0).getStart() + (filterSegmentList.get(0).getLength() / 2);
				Segment segment = (segmentList.get(currentIndex).clone());
				segment.setStart(index);
				segment.setLength(end - index);
				segmentList.get(currentIndex).setLength(index - start - dec);
				segmentList.add(segment);
				ArrayList<Segment> tmpSegmentList = new ArrayList<Segment>();
				filterSegmentList.remove(0);
				int idx_l = index - minLength;
				int idx_r = index + minLength;

				int cpt = 0;
				while (cpt < filterSegmentList.size()) {
					int curtmp = filterSegmentList.get(cpt).getStart();
					if ((curtmp > idx_l) && (curtmp < idx_r)) {
						filterSegmentList.remove(cpt);
					} else {
						cpt++;
					}
				}
				cpt = 0;
				while (cpt < filterSegmentList.size()) {
					if (filterSegmentList.get(cpt).getStart() > index) {
						tmpSegmentList.add(filterSegmentList.get(cpt));
						filterSegmentList.remove(cpt);
					} else {
						cpt++;
					}
				}
				int n = segmentList.size() - 1;
				ArrayList<Segment> filterSegmentListCopy = new ArrayList<Segment>();
				for (int cptf = 0; cptf < tmpSegmentList.size(); cptf++) {
					filterSegmentListCopy.add(tmpSegmentList.get(cptf));
				}
				SSplitSeg.splitSeg(segmentList, n, maxLength, minLength, filterSegmentListCopy, dec);
				filterSegmentListCopy.clear();
				for (int cptf = 0; cptf < filterSegmentList.size(); cptf++) {
					filterSegmentListCopy.add(filterSegmentList.get(cptf));
				}
				SSplitSeg.splitSeg(segmentList, currentIndex, maxLength, minLength, filterSegmentListCopy, dec);
			} else {
				logger.warning("no more split segment, len=" + length);
			}
		}
	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param program the program
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("Programe name = " + program);
			logger.config(parameter.getSeparator2());
			parameter.logShow();

			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationFilterFile().logAll(); // sInFltMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator2());
			parameter.getParameterSegmentationFilterFile().logAll();
			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			logger.config(parameter.getSeparator2());
			parameter.getParameterSegmentationSplit().logAll();
		}
	}

}