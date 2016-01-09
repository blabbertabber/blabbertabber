/**
 * <p>
 * Gaussian
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
 * Abstract Gaussian model
 */

package fr.lium.spkDiarization.libModel;

import java.io.IOException;
import java.util.Iterator;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.DoubleVector;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.ParameterMAP;

/**
 * The Class Gaussian.
 */
public abstract class Gaussian extends Model implements Comparable<Gaussian>, Cloneable {

    /** The Constant FULL. */
    public static final int FULL = 0;

    /** The Constant DIAG. */
    public static final int DIAG = 1;

    /** The ok model, true if model allocated (true dimension). */
    protected int okModel; //

    /** The ok var: all variances are > 0. */
    protected boolean okVar; //

    /** The mean: mean vector <i>mu</i>. */
    protected DoubleVector mean; //

    /** The weight. of the Gaussian, set to 1 for mono Gaussian*/
    protected double weight;
    /** The count: number of features used to compute the Gaussian. */
    protected int count;

    /** The likelihood constant: preset of constant for likelihood. */
    protected double likelihoodConstant; //

    /**
     * The Constructor.
     *
     * @param _dim gaussian dimension
     * @param _kind gaussian kind (FULL or DIAG)
     */
    public Gaussian(int _dim, int _kind) {
        super(_dim, _kind);
        mean = new DoubleVector();
    }

    /**
     * Accumulator: add the statistic accumulator \e m1.
     *
     * @param m1 the gaussian
     *
     * @throws DiarizationException the diarization exception
     */
    public void add(Gaussian m1) throws DiarizationException {
        add(m1, 1.0);
    }

    /**
     * Accumulator: adding the statistic accumulator \e m1 with weight.
     *
     * @param m1 the gaussian
     * @param weight the weight
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract void add(Gaussian m1, double weight) throws DiarizationException;

    /**
     * Accumulator: add feature of index \e i.
     *
     * @param features the features
     * @param frameIndex the frame index
     *
     * @throws DiarizationException the diarization exception
     */
    public void addFeature(FeatureSet features, int frameIndex) throws DiarizationException {
        addFeature(features, frameIndex, 1.0);
    }

    /**
     * Adds the feature.
     *
     * @param frame the frame
     * @param weight the weight
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract void addFeature(float[] frame, double weight) throws DiarizationException;

    /**
     * Adds the feature.
     *
     * @param frame the frame
     *
     * @throws DiarizationException the diarization exception
     */
    public void addFeature(float[] frame) throws DiarizationException {
        addFeature(frame, weight);
    }

    /**
     * Accumulator: add the weighted feature of index \e i.
     *
     * @param features the features
     * @param frameIndex the feature index
     * @param weight the weight
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract void addFeature(FeatureSet features, int frameIndex, double weight) throws DiarizationException;

    /**
     * Adds the features from segments.
     *
     * @param itSeg the segment iterator
     * @param features the features
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void addFeaturesFromSegments(Iterator<Segment> itSeg, FeatureSet features) throws DiarizationException, IOException {
        addFeaturesFromSegments(itSeg, features, 1.0);
    }

    /**
     * Accumulator: add the weighted feature contained in the segment set.
     *
     * @param features the features
     * @param weight the weight
     * @param itSeg the segment iterator
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void addFeaturesFromSegments(Iterator<Segment> itSeg, FeatureSet features, double weight) throws DiarizationException, IOException {
        while (itSeg.hasNext()) {
            Segment seg = itSeg.next();
            int s = seg.getStart();
            int e = s + seg.getLength();
            features.setCurrentShow(seg.getShowName());
            for (int i = s; i < e; i++) {
                addFeature(features, i, weight);
            }
        }
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#clone()
     */
    @Override
    public Object clone() {
        Gaussian result = (Gaussian) (super.clone());
        result.mean = (DoubleVector) (mean.clone());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Gaussian g) {
        if (weight > g.getWeight()) {
            return -1;
        } else {
            return (weight < g.getWeight()) ? 1 : 0;
        }
    }

    /**
     * Model: set the invert of the covariance matrix.
     *
     * @return true, if compute invert covariance matrix
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract boolean computeInvertCovariance() throws DiarizationException;

    /**
     * Compute the preset of constant for likelihood computation.
     */
    public abstract void computeLikelihoodConstant();

    /**
     * Accumulator: get the number of features in the accumulator.
     *
     * @return the accumulator count
     */
    public abstract int getAccumulatorCount();

    /**
     * Accumulator : set the number of features in the accumulator.
     *
     * @param c the c
     */
    protected void setAccumulatorCount(int c) {
        count = c;
    }

    /* (non-Javadoc)
     * @see fr.lium.spkDiarization.lib.Model#getAndAccumulateLikelihoodForComponentSubset(fr.lium.spkDiarization.lib.FeatureSet, int, int[])
     */
    @Override
    public double getAndAccumulateLikelihoodForComponentSubset(FeatureSet features, int i, int[] vTop) throws DiarizationException {
        return getAndAccumulateLikelihood(features, i);
    }

    /**
     * Model: get the number of features in the accumulator.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Model: get the \e i, \e j covariance value.
     *
     * @param i the i index
     * @param j the j index
     *
     * @return the covariance
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract double getCovariance(int i, int j) throws DiarizationException;

    /**
     * Model: get the \e i, \e j invert value of the covariance.
     *
     * @param i the i index
     * @param j the j index
     *
     * @return the invert covariance
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract double getInvertCovariance(int i, int j) throws DiarizationException;

    /**
     * Gets the mAP.
     *
     * @param ubm the universal background model
     * @param mapControl the map control
     *
     * @return the mAP
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract int setAdaptedModelFromAccumulator(Gaussian ubm, ParameterMAP mapControl) throws DiarizationException;

    /**
     * Accumulator : get the model from the accumulator, MAP mode.
     *
     * @param world the world
     * @param mapControl the map control
     *
     * @return the value of the ??
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract int setLinearAdaptedModelFromAccumulator(Gaussian world, ParameterMAP mapControl) throws DiarizationException;

    /**
     * Model: get the \e i mean value.
     *
     * @param i the i index
     *
     * @return the mean value at position i
     */
    public double getMean(int i) {
        return mean.get(i);
    }

    /**
     * Model: get the weight of features in the accumulator.
     *
     * @return the weight
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Model/Acc : set the weight of features in the accumulator.
     *
     * @param w the w
     */
    protected void setWeight(double w) {
        weight = w;
    }

    /**
     * Accumulator: Merge the statistics accumulators \e m1 and \e m2 Useful to compute the cluster \f$xy\f$ resulting of the merge of \f$x\f$ and \f$y\f$
     * clusters Warning The accumulator statistics are initialized before the merge.
     *
     * @param m1 the gaussian 1
     * @param m2 the gaussian 2
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract void merge(Gaussian m1, Gaussian m2) throws DiarizationException;

    /**
     * Accumulator: subtract from the accumulator the \e i feature.
     *
     * @param features the features
     * @param frameIndex index of the feature
     *
     * @throws DiarizationException the diarization exception
     */
    public void removeFeatureFromAccumulator(FeatureSet features, int frameIndex) throws DiarizationException {
        removeFeatureFromAccumulator(features, frameIndex, 1.0);
    }

    /**
     * Accumulator: subtract the weighted feature of index \e i.
     *
     * @param features the features
     * @param weight the weight
     * @param frameIndex the frame index
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract void removeFeatureFromAccumulator(FeatureSet features, int frameIndex, double weight) throws DiarizationException;

    /**
     * Model: set \e v to the \e i, \e j invert value of the covariance matrix.
     *
     * @param i the i
     * @param j the j
     * @param v the v
     *
     * @throws DiarizationException the diarization exception
     */
    public abstract void setCovariance(int i, int j, double v) throws DiarizationException;

    /**
     * Compute the #GLR score.
     */
    public abstract void setGLR();

    /**
     * Model: set \e v to the \e i mean value.
     *
     * @param i the i
     * @param v the v
     */
    public void setMean(int i, double v) {
        mean.set(i, v);
    }

    /**
     * update the count.
     *
     * @param c the c
     */

    public void updateCount(int c) {
        count = c;
        setGLR();
    }

}
