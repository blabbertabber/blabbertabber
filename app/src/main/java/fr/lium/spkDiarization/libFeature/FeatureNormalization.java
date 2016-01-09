/**
 * <p>
 * FeatureNormalization
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
 * Method for normalize feature. This class provides methods to normalize the features. The normalization is computed by segment, cluster or clusters.
 * It can be applied over a segment, a cluster or a clusters. It can be also applied over a sliding windows.
 * <p/>
 * Warning : The features are directly modified.
 */

package fr.lium.spkDiarization.libFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.Distribution;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libModel.DiagGaussian;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.Gaussian;

// TODO: Auto-generated Javadoc

/**
 * The Class FeatureNormalization.
 */
public class FeatureNormalization {

    /** The features. */
    protected FeatureSet features;
    /** The local distribution. to store mean and cov for the
     // segment or window*/
    protected DiagGaussian localDistribution;
    /** The global distribution, to store mean and cov for the cluster. */
    protected DiagGaussian globalDistribution;
    /** The local std dev. */
    protected double localStdDev[];
    /** The reduce. */
    protected boolean reduce;
    /** The window size. */
    protected int windowSize;
    /** The current show name. */
    protected String currentShowName;
    /** The warping table. */
    protected Float[] warpingTable;

    /**
     * Instantiates a new feature normalization.
     *
     * @param _features the _features
     * @param _reduce the _reduce
     */
    public FeatureNormalization(FeatureSet _features, boolean _reduce) {
        this(_features, _reduce, 0);
    }

    /**
     * Instantiates a new feature normalization.
     *
     * @param _features the _features
     * @param _reduce the _reduce
     * @param _windowSize the _window size
     */
    public FeatureNormalization(FeatureSet _features, boolean _reduce, int _windowSize) {
        features = _features;
        localDistribution = new DiagGaussian(_features.getDim());
        globalDistribution = new DiagGaussian(_features.getDim());
        localStdDev = new double[_features.getDim()];
        reduce = _reduce;
        windowSize = _windowSize;
        currentShowName = features.getCurrentShowName();
    }

    /**
     * Apply pre-computed normalization to a segment.
     *
     * @param segment the segment
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void applyNormToSegment(Segment segment) throws DiarizationException, IOException {
        if (currentShowName.compareTo(segment.getShowName()) == 0) {
            // features.setCurrentShow(seg.getShowIndex());
            if (reduce) {
                for (int i = segment.getStart(); i < segment.getStart() + segment.getLength(); i++) {
                    centerAndReduce(i);
                }
            } else {
                for (int i = segment.getStart(); i < segment.getStart() + segment.getLength(); i++) {
                    center(i);
                }
            }
        }
    }

    /**
     * Center the feature at position #frameIndex.
     *
     * @param frameIndex the frame index
     *
     * @throws DiarizationException the diarization exception
     */
    protected void center(int frameIndex) throws DiarizationException {
        float[] frame = features.getFeature(frameIndex);
        int dim = features.getDim();
        for (int i = 0; i < dim; i++) {
            frame[i] = (float) (frame[i] - localDistribution.getMean(i));
        }
    }

    /**
     * Center and reduce the feature at position #frameIndex.
     *
     * @param frameIndex the frame index
     *
     * @throws DiarizationException the diarization exception
     */
    protected void centerAndReduce(int frameIndex) throws DiarizationException {
        float[] frame = features.getFeature(frameIndex);
        int dim = features.getDim();
        for (int i = 0; i < dim; i++) {
            frame[i] = (float) ((frame[i] - localDistribution.getMean(i)) / localStdDev[i]);
        }
    }

    /**
     * Compute local standard deviation.
     *
     * @throws DiarizationException the diarization exception
     */
    protected void computeLocalStdDev() throws DiarizationException {
        for (int i = 0; i < features.getDim(); i++) {
            localStdDev[i] = Math.sqrt(localDistribution.getCovariance(i, i));
        }
    }

    /**
     * Compute normalization over a cluster.
     *
     * @param cluster the cluster
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    protected void computeNormOnCluster(Cluster cluster) throws DiarizationException, IOException {
        localDistribution.initStatisticAccumulator();
        for (Segment segment : cluster) {
            /*
			 * Iterator<Segment> itSegment = cluster.iterator(); while (itSegment.hasNext()) { Segment segment = itSegment.next();
			 */
            if (currentShowName.compareTo(segment.getShowName()) == 0) {
                for (int i = segment.getStart(); i < segment.getStart() + segment.getLength(); i++) {
                    localDistribution.addFeature(features, i);
                }
            }
        }
        if (localDistribution.setModelFromAccululator() != 0) {
            System.out.print("WARNING: Features normalized using global information for cluster " + cluster.getName());
            localDistribution = (DiagGaussian) (globalDistribution.clone());
        }
        localDistribution.resetStatisticAccumulator();
        if (reduce) {
            computeLocalStdDev();
        }
    }

    /**
     * Compute normalization over a cluster.
     *
     * @param clusters the clusters
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    protected void computeNormOnClusterSet(ClusterSet clusters) throws DiarizationException, IOException {
        globalDistribution.initStatisticAccumulator();
        for (Cluster cluster : clusters.clusterSetValue()) {
			/*
			 * Iterator<Cluster> itCluster = clusters.clusterSetValueIterator(); while (itCluster.hasNext()) { Cluster cluster = itCluster.next();
			 */
            for (Segment segment : cluster) {
				/*
				 * Iterator<Segment> itSegment = cluster.iterator(); while (itSegment.hasNext()) { Segment segment = itSegment.next();
				 */
                if (currentShowName.compareTo(segment.getShowName()) == 0) {
                    // features.setCurrentShow(seg.getShowIndex()) ;
                    for (int i = segment.getStart(); i < segment.getStart() + segment.getLength(); i++) {
                        globalDistribution.addFeature(features, i);
                    }
                }
            }
        }
        globalDistribution.setModelFromAccululator();
        globalDistribution.resetStatisticAccumulator();
    }

    /**
     * Compute normalization over a segment.
     *
     * @param segment the segment
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    protected void computeNormOnSegment(Segment segment) throws DiarizationException, IOException {
        localDistribution.initStatisticAccumulator();
        if (currentShowName.compareTo(segment.getShowName()) == 0) {
            for (int i = segment.getStart(); i < segment.getStart() + segment.getLength(); i++) {
                localDistribution.addFeature(features, i);
            }
            if (localDistribution.setModelFromAccululator() != 0) {
                System.out.print("WARNING: Features (start=" + segment.getStart() + " len=");
                System.out.println(segment.getLength() + ") normalized using global information ");
                localDistribution = (DiagGaussian) (globalDistribution.clone());
            }
            localDistribution.resetStatisticAccumulator();
            if (reduce) {
                computeLocalStdDev();
            }
        }
    }

    /**
     * Initialized warping.
     *
     * @throws DiarizationException the diarization exception
     */
    protected void initializedWarping() throws DiarizationException {
        if (windowSize <= 0) {
            throw new DiarizationException("FeatureNorm : initializedWarping, windowsSize <= 0");
        }
        warpingTable = new Float[windowSize];
        for (int i = 0; i < windowSize; i++) {
            warpingTable[i] = (float) Distribution.normalInvert((i + 0.5) / windowSize);
        }
    }

    /**
     * Map feature cluster.
     *
     * @param cluster the cluster
     * @param UBMs the uB ms
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void mapFeatureCluster(Cluster cluster, ArrayList<GMM> UBMs) throws DiarizationException, IOException {
        for (Segment segment : cluster) {
			/*
			 * Iterator<Segment> itSegment = cluster.iterator(); while (itSegment.hasNext()) { Segment segment = itSegment.next();
			 */
            mapFeatureSegment(segment, UBMs);
        }
    }

    /**
     * Map feature cluster set.
     *
     * @param clusters the clusters
     * @param UBMs the uB ms
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void mapFeatureClusterSet(ClusterSet clusters, ArrayList<GMM> UBMs) throws DiarizationException, IOException {
        //System.out.println("trace[featureNormalization] \t mappingClusterSet");

        Iterator<Cluster> itCluster = clusters.clusterSetValueIterator();

        while (itCluster.hasNext()) {
            Cluster cluster = itCluster.next();
            mapFeatureCluster(cluster, UBMs);
        }

    }

    /**
     * Map feature segment.
     *
     * @param segment the segment
     * @param UBMs the uB ms
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void mapFeatureSegment(Segment segment, ArrayList<GMM> UBMs) throws DiarizationException, IOException {
        if (currentShowName.compareTo(segment.getShowName()) == 0) {
            int start = segment.getStart();
            int length = segment.getLength();
            GMM UBM = UBMs.get(0);
            int dim = UBM.getDim();
            int[] top = UBM.getTopGaussians();
            for (int i = 0; i < length; i++) {
                UBM.initScoreAccumulator();
                UBM.getAndAccumulateLikelihoodAndFindTopComponents(features, start + i, 1);
                double max = Double.NEGATIVE_INFINITY;
                int idxMax = 0;
                for (int j = 1; j < UBMs.size(); j++) {
                    GMM gmm = UBMs.get(j);
                    gmm.initScoreAccumulator();
                    gmm.getAndAccumulateLikelihoodForComponentSubset(features, j, top);
                    double v = gmm.getLogLikelihood();
                    if (v > max) {
                        max = v;
                        idxMax = j;
                    }
                    gmm.resetScoreAccumulator();
                }
                float[] frame = features.getFeature(start + i);
                Gaussian componentUBM = UBM.getComponent(idxMax);
                Gaussian componentGMM = UBMs.get(idxMax).getComponent(idxMax);

                for (int j = 0; j < dim; j++) {
                    double std = componentUBM.getCovariance(j, j) / componentGMM.getCovariance(j, j);
                    double diff = frame[j] - componentGMM.getMean(j);
                    frame[j] = (float) (diff * std + componentUBM.getMean(j));
                }
            }
            UBM.resetScoreAccumulator();
        }
    }

    /**
     * Normalize a cluster using a sliding window. Normalization is done on each segment using the local distribution of features within the window.
     *
     * @param cluster the cluster
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void normalizeClusterByWindow(Cluster cluster) throws DiarizationException, IOException {
        for (Segment segment : cluster) {
            normalizeSegmentByWindow(segment);
        }
		/*
		 * Iterator<Segment> itSegment = cluster.iterator(); while (itSegment.hasNext()) { normalizeSegmentByWindow(itSegment.next()); }
		 */
    }

    /**
     * Normalize a set of clusters, segment by segment. Each segment is normalized using the global distribution of features within the segment.
     *
     * @param clusters the clusters
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public void normalizeClusterSetByCluster(ClusterSet clusters) throws DiarizationException, IOException {
        //System.out.println("trace[featureNormalization] \t normalizeClusterSetByCluster");
        computeNormOnClusterSet(clusters);
        for (Cluster cluster : clusters.clusterSetValue()) {
			/*
			 * Iterator<Cluster> itCluster = clusters.clusterSetValueIterator(); while (itCluster.hasNext()) { Cluster cluster = itCluster.next();
			 */
            computeNormOnCluster(cluster);
            for (Segment segment : cluster) {
				/*
				 * Iterator<Segment> itSegment = cluster.iterator(); while (itSegment.hasNext()) { Segment segment = itSegment.next();
				 */
                applyNormToSegment(segment);
            }
        }
    }

    /**
     * Normalize a set of clusters, segment by segment. Each segment is normalized using the global distribution of features within the segment.
     *
     * @param clusters the clusters
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public void normalizeClusterSetBySegment(ClusterSet clusters) throws DiarizationException, IOException {
        //System.out.println("trace[featureNormalization] \t normalizeClusterSetBySegment");
        computeNormOnClusterSet(clusters);

        for (Cluster cluster : clusters.clusterSetValue()) {
			/*
			 * Iterator<Cluster> itCluster = clusters.clusterSetValueIterator(); while (itCluster.hasNext()) { Cluster cluster = itCluster.next();
			 */
            for (Segment segment : cluster) {
				/*
				 * Iterator<Segment> itSegment = cluster.iterator(); while (itSegment.hasNext()) { Segment segment = itSegment.next();
				 */
                computeNormOnSegment(segment);
                applyNormToSegment(segment);
            }
        }
    }

    /**
     * Normalize a set of clusters using a sliding window. Normalization is done on each segment using the local distribution of features within the window.
     *
     * @param clusters the clusters
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void normalizeClusterSetByWindow(ClusterSet clusters) throws DiarizationException, IOException {
        //System.out.println("trace[featureNormalization] \t normalizeClusterSetByWindow");
        for (Cluster cluster : clusters.clusterSetValue()) {
			/*
			 * Iterator<Cluster> enumClusters = clusters.clusterSetValueIterator(); while (enumClusters.hasNext()) { Cluster cluster = enumClusters.next();
			 */
            normalizeClusterByWindow(cluster);
        }
    }

    /**
     * Normalize a file using a sliding window.
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void normalizeFileByWindow() throws DiarizationException, IOException {
        Segment segment = new Segment(currentShowName, 0, features.getNumberOfFeatures(), new Cluster("UNKNOWN"));
        normalizeSegmentByWindow(segment);
    }

    /**
     * Normalize a segment using a sliding window. Normalization uses the local distribution of features within the sliding window.
     *
     * @param segment the segment
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void normalizeSegmentByWindow(Segment segment) throws DiarizationException, IOException {
        if (currentShowName.compareTo(segment.getShowName()) == 0) {
            int start = segment.getStart();
            int length = segment.getLength();
            int size = windowSize;
            if (length < windowSize) {
                size = length;
            }
            int dsize = size / 2;
            localDistribution.initStatisticAccumulator();

            // features.setCurrentShow(seg.getShowIndex());

            int i = segment.getStart();
            for (; i < start + size; i++) {
                localDistribution.addFeature(features, i);
            }
            localDistribution.setModelFromAccululator();
            if (reduce) {
                for (i = start; i < start + dsize; i++) {
                    center(i);
                }
            } else {
                computeLocalStdDev();
                for (i = start; i < start + dsize; i++) {
                    centerAndReduce(i);
                }
            }

            for (i = start + dsize; i < start + length - dsize + 1; i++) {
                localDistribution.initStatisticAccumulator();
                for (int j = 0; j < size; j++) {
                    localDistribution.addFeature(features, i - dsize + j);
                }
                localDistribution.setModelFromAccululator();
                if (reduce) {
                    center(i);
                } else {
                    computeLocalStdDev();
                    centerAndReduce(i);
                }
            }

            if (reduce) {
                for (i = start + length - dsize + 1; i < start + length; i++) {
                    center(i);
                }
            } else {
                for (i = start + length - dsize + 1; i < start + length; i++) {
                    centerAndReduce(i);
                }
            }
        }
    }

    /**
     * Warp cluster.
     *
     * @param cluster the cluster
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void warpCluster(Cluster cluster) throws DiarizationException, IOException {
        for (Segment segment : cluster) {
			/*
			 * Iterator<Segment> itSegment = cluster.iterator(); while (itSegment.hasNext()) { Segment segment = itSegment.next();
			 */
            warpSegment(segment);
        }
    }

    /**
     * Warp cluster set.
     *
     * @param clusters the clusters
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void warpClusterSet(ClusterSet clusters) throws DiarizationException, IOException {
        //System.out.println("trace[featureNormalization] \t warpingClusterSet");
        initializedWarping();

        for (Cluster cluster : clusters.clusterSetValue()) {
			/*
			 * Iterator<Cluster> itCluster = clusters.clusterSetValueIterator(); while (itCluster.hasNext()) { Cluster cluster = itCluster.next();
			 */
            warpCluster(cluster);
        }

    }

    /**
     * Warp file.
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void warpFile() throws DiarizationException, IOException {
        //System.out.println("trace[featureNormalization] \t warpingFile");
        initializedWarping();

        Segment segment = new Segment(currentShowName, 0, features.getNumberOfFeatures(), new Cluster("UNKNOWN"));
        warpSegment(segment);

    }

    /**
     * Warp segment.
     *
     * @param segment the segment
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void warpSegment(Segment segment) throws DiarizationException, IOException {
        // attention features.setCurrentShow(segment.getShowIndex());
        if (currentShowName.compareTo(segment.getShowName()) == 0) {
            int start = segment.getStart();
            int length = segment.getLength();
            int wSize = (windowSize < length ? windowSize : length);
            int dim = features.getDim();
            int halfWSize = wSize / 2;
            // System.err.println("*** warpingSegment wSise = "+wSize+" halfWSize = "+halfWSize+" start = "+start+" lenght = "+length+" dim="+dim);

            IndexValueList[] data = new IndexValueList[dim];
            for (int j = 0; j < dim; j++) {
                data[j] = new IndexValueList();
            }
            // --- from 0 to wSize ---
            // prepare
            for (int i = 0; i < wSize; i++) {
                float[] frame = features.getFeature(start + i);
                for (int j = 0; j < dim; j++) {
                    data[j].add(new IndexValue(start + i, frame[j]));
                }
            }
            // sort
            for (int j = 0; j < dim; j++) {
                Collections.sort(data[j]);
            }
            // normalize que 1/2
            for (int j = 0; j < dim; j++) {
                int idxWarpingTable = 0;
                for (IndexValue indexValue : data[j]) {
					/*
					 * Iterator<IndexValue> it = data[j].iterator(); while (it.hasNext()) { IndexValue indexValue = it.next();
					 */
                    int idx = indexValue.getIndex();
                    if (idx < start + halfWSize) {
                        float[] frame = features.getFeature(idx);
                        frame[j] = warpingTable[idxWarpingTable];
                    }
                    idxWarpingTable++;
                }
            }

            // --- from wSize to length - wSize ---
            for (int i = halfWSize; i < length - halfWSize; i++) {
                int removeIndex = start + i - halfWSize;
                int addIndex = start + i + halfWSize;
                float[] addFrame = features.getFeature(addIndex);
                int frameIndex = start + i;
                float[] frame = features.getFeature(frameIndex);
                for (int j = 0; j < dim; j++) {
                    java.util.ListIterator<IndexValue> it = data[j].listIterator();
                    boolean notAdded = true;
                    int wSizeIndex = -1;
                    int idx = 0;
                    int job = 0;
                    while (it.hasNext()) {
                        IndexValue indexValue = it.next();
                        double value = indexValue.getValue();
                        int index = indexValue.getIndex();
                        if (index == removeIndex) {
                            it.remove();
                            job++;
                        }
                        if ((value > addFrame[j]) && (notAdded)) {
                            it.add(new IndexValue(addIndex, addFrame[j]));
                            job++;
                            notAdded = false;
                        }
                        if (index == frameIndex) {
                            wSizeIndex = idx;
                            job++;
                        }
                        if (job == 3) { // = 1 remove, 1 add, 1 find
                            break;
                        }
                        idx++;
                    }
                    if (notAdded) {
                        data[j].add(new IndexValue(addIndex, addFrame[j]));
                        job++;
                    }
                    if (job < 3) {
                        throw new DiarizationException("FeatureNorm : job problem");
                    }
                    frame[j] = warpingTable[wSizeIndex];
                }
            }

            // --- from length -wSize to length, normalize using stored data ---
            for (int j = 0; j < dim; j++) {
                int idxWarpingTable = 0;
                for (IndexValue indexValue : data[j]) {
					/*
					 * Iterator<IndexValue> it = data[j].iterator(); while (it.hasNext()) { IndexValue indexValue = it.next();
					 */
                    int idx = indexValue.getIndex();
                    if (idx >= length - halfWSize) {
                        float[] frame = features.getFeature(idx);
                        frame[j] = warpingTable[idxWarpingTable];
                    }
                    idxWarpingTable++;
                }
            }
        }
    }

    /**
     * The Class IndexValue.
     */
    class IndexValue implements Comparable<IndexValue> {

        /** The index. */
        protected int index;

        /** The value. */
        protected float value;

        /**
         * Instantiates a new index value.
         *
         * @param index the index
         * @param value the value
         */
        public IndexValue(int index, float value) {
            super();
            this.index = index;
            this.value = value;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(IndexValue arg0) {
            // TODO Auto-generated method stub
            if (value > arg0.getValue()) {
                return 1;
            }
            if (value < arg0.getValue()) {
                return -1;
            }
            return 0;
        }

        /**
         * Gets the index.
         *
         * @return the index
         */
        public int getIndex() {
            return index;
        }

        /**
         * Sets the index.
         *
         * @param index the new index
         */
        public void setIndex(int index) {
            this.index = index;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public float getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param value the new value
         */
        public void setValue(float value) {
            this.value = value;
        }
    }

    /**
     * The Class IndexValueList.
     */
    class IndexValueList extends LinkedList<IndexValue> {

        /** The Constant serialVersionUID. */
        private static final long serialVersionUID = 1L;
    }

}
