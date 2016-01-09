/**
 * <p>
 * Distance
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
 * Distance class, ie GLR, BIC, KL2, GD, CE, CLR for Gaussian or GMM
 */

package fr.lium.spkDiarization.libModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libClusteringMethod.ClusterAndGMM;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class Distance. 
 * A set of various distances, static methodes.
 */
public class Distance {

    public static double getEnergyThreshold(Cluster cluster, FeatureSet features, double silenceThreshold) throws DiarizationException, IOException {
        ArrayList<Double> energy = new ArrayList<Double>(0);
        // get energy in a vector
        for (Segment segment : cluster) {
            features.setCurrentShow(segment.getShowName());
            int start = segment.getStart();
            int endSegment = start + segment.getLength();
            int end = Math.min(endSegment, features.getNumberOfFeatures());
            if (endSegment > end) {
                System.out.print("WARNING[msegSil] \t segment end upper to features end (");
                System.out.print("end of Seg=" + endSegment + " nb Features =" + features.getNumberOfFeatures() + ")");
                System.out.println(" starting at =" + start + " len= " + segment.getLength() + " in show = " + segment.getShowName());
            }
            for (int i = start; i < end; i++) {
                double value = features.getFeature(i)[features.getIndexOfEnergy()];
                energy.add(value);
            }
        }
        // sort the energy
        Collections.sort(energy);
        // get thershold
        int p = 0;
        if (silenceThreshold > 0.0) {
            p = (int) Math.round(silenceThreshold * energy.size());
        }
        return energy.get(p);
    }

    /**
     * Get a BIC score for GMM using ICSI method.
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException
     */
    public static double GLR(GMM gmmI, Cluster clusterI, GMM gmmJ, Cluster clusterJ, FeatureSet featureSet, Parameter param) throws DiarizationException, IOException {
        Double scoreI = getScore(gmmI, clusterI.iterator(), featureSet);
        Double scoreJ = getScore(gmmJ, clusterJ.iterator(), featureSet);

        GMM gmmIJ = (GMM) gmmI.clone();
        double cI = (double) gmmI.getNbOfComponents() / (double) (gmmI.getNbOfComponents() + gmmJ.getNbOfComponents());
        double cJ = (double) gmmJ.getNbOfComponents() / (double) (gmmI.getNbOfComponents() + gmmJ.getNbOfComponents());

        for (int k = 0; k < gmmJ.getNbOfComponents(); k++) {
            gmmIJ.addComponent(gmmJ.getComponent(k));
        }

        for (int k = 0; k < gmmIJ.getNbOfComponents(); k++) {
            Gaussian gaussian = gmmIJ.getComponent(k);
            double c = cJ;
            if (k < gmmI.getNbOfComponents()) {
                c = cI;
            }
            gaussian.setWeight(gaussian.getWeight() * c);
        }

        //gmmIJ.normWeights();
        Cluster clusterIJ = (Cluster) clusterI.clone();
        clusterIJ.addSegments(clusterJ.iterator());

        gmmIJ = GMMFactory.getEM(clusterIJ, featureSet, gmmIJ, gmmIJ.getNbOfComponents(), param.parameterEM, param.parameterVarianceControl, true);
        Double scoreIJ = getScore(gmmIJ, clusterIJ.iterator(), featureSet);

        System.err.println(gmmI.getName() + "/" + gmmJ.getName() + " " + clusterI.getName()
                + "/" + clusterJ.getName() + " " + scoreIJ / clusterIJ.getLength() +
                " - (" + scoreI / clusterI.getLength() + " + " + scoreJ / clusterJ.getLength()
                + ") " + clusterIJ.getLength() + " DIM" + gmmI.getDim() + " " + cI + " " + cJ
                + " startI=" + clusterI.firstSegment().getStart() + " startJ=" + clusterJ.firstSegment().getStart());
        return scoreIJ - (scoreI + scoreJ);
    }

    /**
     * Get a BIC score for Gaussians given a constant and the length.
     *
     * @param gi the gaussian i
     * @param gj the gaussian j
     * @param cst the BIC cst
     * @param len the number of feature
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    public static double BIC(Gaussian gi, Gaussian gj, double cst, int len) throws DiarizationException {
        double v = Math.log(len);
        return (Distance.GLR(gi, gj) - cst * v);
    }

    /**
     * Get a BIC constant.
     *
     * @param kind the kind of model
     * @param dim the dim of vector
     * @param alpha the control factor
     *
     * @return the double
     */
    public static double BICConstant(int kind, double dim, double alpha) {
        if (kind == Gaussian.FULL) {
            return 0.5 * alpha * (dim + 0.5 * ((dim + 1) * dim));
        }
        return 0.5 * alpha * (dim + dim);
    }

    /**
     * Get a BIC score for Gaussians using a length of the clusters.
     *
     * @param gi the gaussian i
     * @param gj the gaussian j
     * @param cst the BIC cst
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    public static double BICLocal(Gaussian gi, Gaussian gj, double cst) throws DiarizationException {
        double v = Math.log((gi.getCount() + gj.getCount()));
        return Distance.GLR(gi, gj) - cst * v;
    }

    /**
     * T dist stop criterion.
     *
     * @param GMMList the GMM list
     * @param ubm the ubm model
     * @param features the feature set
     * @param useTop the use top
     * @param delay the delay
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static double TDistStopCriterion(ArrayList<ClusterAndGMM> GMMList, GMM ubm, FeatureSet features, boolean useTop, int delay)
            throws DiarizationException, IOException {
        DiagGaussian accScoreInner = new DiagGaussian(1);
        DiagGaussian accScoreOuter = new DiagGaussian(1);

        accScoreInner.initStatisticAccumulator();
        accScoreOuter.initStatisticAccumulator();

        for (ClusterAndGMM clusterAndGMM : GMMList) {
            GMM gmm = clusterAndGMM.getModel();

            for (ClusterAndGMM clusterAndGMM2 : GMMList) {
                Cluster cluster = clusterAndGMM2.getCluster();
                if (clusterAndGMM2 == clusterAndGMM) {
                    getAccumulatorOfLogLikelihoodRatio(accScoreInner, gmm, ubm, cluster.iterator(), features, useTop, delay);
                } else {
                    getAccumulatorOfLogLikelihoodRatio(accScoreOuter, gmm, ubm, cluster.iterator(), features, useTop, delay);
                }
            }

        }
        accScoreInner.setModelFromAccululator();
        accScoreOuter.setModelFromAccululator();

        return -1.0 * (Math.abs(accScoreInner.getMean(0) - accScoreOuter.getMean(0)))
                / (Math.sqrt(accScoreInner.getCovariance(0, 0) / accScoreInner.getCount() + accScoreOuter.getCovariance(0, 0) / accScoreOuter.getCount()));
    }

    /**
     * T dist.
     *
     * @param gmmI the gmm i
     * @param gmmJ the gmm j
     * @param ubm the ubm model
     * @param clusterI the cluster i
     * @param clusterJ the cluster j
     * @param features the features
     * @param useTop the use top
     * @param delay the delay
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static double TDist(GMM gmmI, GMM gmmJ, GMM ubm, Cluster clusterI, Cluster clusterJ, FeatureSet features, boolean useTop, int delay)
            throws DiarizationException, IOException {
        DiagGaussian accScoreInner = new DiagGaussian(1);
        DiagGaussian accScoreOuter = new DiagGaussian(1);

        accScoreInner.initStatisticAccumulator();
        accScoreOuter.initStatisticAccumulator();

        getAccumulatorOfLogLikelihoodRatio(accScoreInner, gmmI, ubm, clusterI.iterator(), features, useTop, delay);
        getAccumulatorOfLogLikelihoodRatio(accScoreInner, gmmJ, ubm, clusterJ.iterator(), features, useTop, delay);

        getAccumulatorOfLogLikelihoodRatio(accScoreOuter, gmmJ, ubm, clusterI.iterator(), features, useTop, delay);
        getAccumulatorOfLogLikelihoodRatio(accScoreOuter, gmmI, ubm, clusterJ.iterator(), features, useTop, delay);

        accScoreInner.setModelFromAccululator();
        accScoreOuter.setModelFromAccululator();

        double count = Math.sqrt(accScoreInner.getCount() + accScoreOuter.getCount());
        return -1.0 * (Math.abs(accScoreInner.getMean(0) - accScoreOuter.getMean(0)) * count)
                / (Math.sqrt(accScoreInner.getCovariance(0, 0) + accScoreOuter.getCovariance(0, 0)));
    }

    /**
     * Cross entropy / Normalized CLR distance.
     *
     * @param gmmI the gmm i
     * @param gmmJ the gmm j
     * @param clusterI the cluster i
     * @param clusterJ the cluster j
     * @param features the features
     * @param useTop the use top
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static double CE(GMM gmmI, GMM gmmJ, Cluster clusterI, Cluster clusterJ, FeatureSet features, boolean useTop) throws DiarizationException,
            IOException {
// gmmI.resetScore();
// gmmJ.resetScore();
// System.err.println("ce");
        double gmmI_segmentI = Distance.getScore(gmmI, clusterI.iterator(), features, useTop);
        double gmmJ_segmentI = Distance.getScore(gmmJ, clusterI.iterator(), features, useTop);
        double segmentICount = gmmI.getCountLogLikelihood();
        gmmI.resetScoreAccumulator();
        gmmJ.resetScoreAccumulator();
        double gmmI_segmentJ = Distance.getScore(gmmI, clusterJ.iterator(), features, useTop);
        double gmmJ_segmentJ = Distance.getScore(gmmJ, clusterJ.iterator(), features, useTop);
        double segmentJCount = gmmJ.getCountLogLikelihood();
        return ((gmmJ_segmentJ - gmmI_segmentJ) / segmentJCount) + ((gmmI_segmentI - gmmJ_segmentI) / segmentICount);
    }

    /**
     * Cross Likelihood Ratio distance.
     *
     * @param gmmI the gmm i
     * @param gmmJ the gmm j
     * @param ubm the ubm
     * @param clusterI the cluster i
     * @param clusterJ the cluster j
     * @param scoreUBM_sI the score ub m_s i
     * @param scoreUBM_sJ the score ub m_s j
     * @param features the features
     * @param useTop the use top
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static double CLR(GMM gmmI, GMM gmmJ, GMM ubm, Cluster clusterI, Cluster clusterJ, double scoreUBM_sI, double scoreUBM_sJ, FeatureSet features,
                             boolean useTop) throws DiarizationException, IOException {
        double score_gI_sJ = Distance.getScore(gmmI, clusterJ.iterator(), features, useTop);
        double score_gJ_sI = Distance.getScore(gmmJ, clusterI.iterator(), features, useTop);
        return ((scoreUBM_sJ - score_gI_sJ) / gmmI.getCountLogLikelihood()) + ((scoreUBM_sI - score_gJ_sI) / gmmJ.getCountLogLikelihood());
    }

    /**
     * Get a Gaussian Divergence score for diagonal gaussians (see LIMSI).
     *
     * @param g1 the gaussian 1
     * @param g2 the gaussian 2
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    public static double GD(Gaussian g1, Gaussian g2) throws DiarizationException {
        double s = 0.0;
        int dim = g1.getDim();
        for (int j = 0; j < dim; j++) {
            double dmean = g1.getMean(j) - g2.getMean(j);
            double v = Math.sqrt(g1.getCovariance(j, j)) * Math.sqrt(g2.getCovariance(j, j));
            if (v < 0) {
                System.out.println("Warning[Distance] \t GD: variance problem");
                v = 1e-8;
            }
            s += (dmean * dmean) / v;
        }
        return s;
    }

    /**
     * MAP Gaussian divergence.
     * cf. Mathieux Ben thesis: MAP using KL distance
     * @param gmmI the gmm i
     * @param gmmJ the gmm j
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    public static double GDMAP(GMM gmmI, GMM gmmJ) throws DiarizationException {
        double score = 0.0;
        long nbComponents = gmmI.getNbOfComponents();
        for (int i = 0; i < nbComponents; i++) {
            DiagGaussian gaussI = (DiagGaussian) gmmI.getComponent(i);
            DiagGaussian gaussJ = (DiagGaussian) gmmJ.getComponent(i);
            score += gaussI.weight * GD(gaussI, gaussJ);
        }
        return score;
    }

    /**
     * Get the log-likelihood of a GMM over a list of segments.
     *
     * @param gmm the gmm model
     * @param itSeg the segment iterator
     * @param features the features
     *
     * @return the score
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static double getScore(GMM gmm, Iterator<Segment> itSeg, FeatureSet features) throws DiarizationException, IOException {
        return Distance.getScore(gmm, itSeg, features, false);
    }

    /**
     * Gets the score.
     *
     * @param gmm the gmm model
     * @param itSeg the segment iterator
     * @param features the features
     * @param useTop the use top
     *
     * @return the score
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static double getScore(GMM gmm, Iterator<Segment> itSeg, FeatureSet features, boolean useTop) throws DiarizationException, IOException {
        gmm.initScoreAccumulator();
        //DiagGaussian score = new DiagGaussian(1);
        //score.initStatisticAccumulator();
//		double s = 0;
        while (itSeg.hasNext()) {
            Segment segment = itSeg.next();
            int i = 0;
            int start = segment.getStart();
            int end = segment.getLength() + start;
            features.setCurrentShow(segment.getShowName());
            ArrayList<int[]> top = segment.getTop();
            for (int j = start; j < end; j++) {
                if (useTop) {
                    gmm.getAndAccumulateLikelihoodForComponentSubset(features, j, top.get(i));
                    i++;
                } else {
                    //	gmm.getAndAccumulateLogLikelihood(features, j);
                    gmm.getAndAccumulateLikelihood(features, j);
//					s += gmm.getAndAccumulateLogLikelihood(features, j);
                }
            }
        }
        //score.setModelFromAccululator();
        return gmm.getSumLogLikelihood();
        //return s;
    }

    /**
     * Gets the accumulator of log likelihood ratio.
     *
     * @param scoreAccumulator the score accumulator
     * @param gmm the gmm model
     * @param ubm the ubm model
     * @param itSeg the it seg
     * @param features the features
     * @param useTop the use top
     * @param delay the delay
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void getAccumulatorOfLogLikelihoodRatio(DiagGaussian scoreAccumulator, GMM gmm, GMM ubm, Iterator<Segment> itSeg, FeatureSet features,
                                                          boolean useTop, int delay) throws DiarizationException, IOException {
        gmm.initScoreAccumulator();
        ubm.initScoreAccumulator();
        float[] ratio = new float[1];
        while (itSeg.hasNext()) {
            Segment segment = itSeg.next();
            int i = 0;
            int start = segment.getStart();
            int end = segment.getLength() + start;
            features.setCurrentShow(segment.getShowName());
            ArrayList<int[]> top = segment.getTop();
            boolean reste = false;
            for (int j = start; j < end; j++) {
                reste = true;
                if (useTop) {
                    gmm.getAndAccumulateLikelihoodForComponentSubset(features, j, top.get(i));
                    ubm.getAndAccumulateLikelihoodForComponentSubset(features, j, top.get(i));
                } else {
                    gmm.getAndAccumulateLikelihood(features, j);
                    ubm.getAndAccumulateLikelihood(features, j);
                }
                i++;
                if ((i % delay) == 0) {
                    ratio[0] = (float) (gmm.getMeanLogLikelihood() - ubm.getMeanLogLikelihood());
                    scoreAccumulator.addFeature(ratio);
                    ubm.resetScoreAccumulator();
                    gmm.resetScoreAccumulator();
                    reste = false;
                }
            }
            if (reste == true) {
                ratio[0] = (float) (gmm.getMeanLogLikelihood() - ubm.getMeanLogLikelihood());
                scoreAccumulator.addFeature(ratio);
            }
        }
    }

    /**
     * Gets the score set top.
     *
     * @param model the model
     * @param nbTop the number of top gaussian
     * @param features the features
     * @param segmentIterator the segment iterator
     *
     * @return the score set top
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static double getScoreSetTop(GMM model, int nbTop, Iterator<Segment> segmentIterator, FeatureSet features) throws DiarizationException, IOException {
        model.initScoreAccumulator();
        while (segmentIterator.hasNext()) {
            Segment segment = segmentIterator.next();
            int s = segment.getStart();
            int e = segment.getLength() + s;
            features.setCurrentShow(segment.getShowName());
            ArrayList<int[]> top = segment.getTop();
            top.clear();
            for (int j = s; j < e; j++) {
                model.getAndAccumulateLikelihoodAndFindTopComponents(features, j, nbTop);
                top.add(model.getTopGaussians());
            }
        }
        return model.getSumLogLikelihood();
    }

    /**
     * Get a GLR score for Gaussians.
     *
     * @param gi the gaussian i
     * @param gj the gaussian j
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    public static double GLR(Gaussian gi, Gaussian gj) throws DiarizationException {
        int dim = gj.getDim();
        int kind = gj.getKind();
        Gaussian gij = null;
        if (kind == Gaussian.FULL) {
            gij = new FullGaussian(dim);
        } else {
            gij = new DiagGaussian(dim);
        }
        gij.initStatisticAccumulator();
        gij.merge(gi, gj);
        gij.setModelFromAccululator();
        double res = gij.getPartialGLR() - gi.getPartialGLR() - gj.getPartialGLR();
        return res;
    }

    /**
     * Get a Hotelling statistic score for diagonal Gaussians.
     *
     * @param gi the gaussian i
     * @param gj the gaussian j
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    public static double H2(Gaussian gi, Gaussian gj) throws DiarizationException {
        double s = 0.0;
        int dim = gj.getDim();
        int kind = gj.getKind();
        Gaussian gij = null;
        if (kind == Gaussian.FULL) {
            gij = new FullGaussian(dim);
        } else {
            gij = new DiagGaussian(dim);
        }
        gij.initStatisticAccumulator();
        gij.merge(gi, gj);
        gij.setModelFromAccululator();
        for (int j = 0; j < dim; j++) {
            double dmean = gi.getMean(j) - gj.getMean(j);
            double v = gij.getCovariance(j, j);
            if (v < 0) {
                System.out.println("Warning \t H2: variance problem");
                v = 1e-8;
            }
            s += (dmean * dmean) / v;
        }
        return s * gij.getAccumulatorCount() / (double) (gi.getAccumulatorCount() + gj.getAccumulatorCount());
    }

    /**
     * ICR.
     *
     * @param gi the gaussian i
     * @param gj the gaussian j
     * @param cst the weighting constant
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    public static double ICR(Gaussian gi, Gaussian gj, double cst) throws DiarizationException {
        double v = 1.0 / ((gi.getCount() + gj.getCount()));
        // return Distance.GLR(gi, gj) - cst * v; //correction du 30/11/2008
        return cst * v * Distance.GLR(gi, gj);
    }

    /**
     * Get a KL2 score for diagonal Gaussians.
     *
     * @param g1 the gaussian 1
     * @param g2 the gaussian 2
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     */
    public static double KL2(Gaussian g1, Gaussian g2) throws DiarizationException {
        double s = 0.0;
        int dim = g1.getDim();
        for (int j = 0; j < dim; j++) {
            double m1 = g1.getMean(j);
            double m2 = g2.getMean(j);
            double dmean = m1 - m2;
            double v1 = g1.getCovariance(j, j);
            double v2 = g2.getCovariance(j, j);
            s += 0.25 * ((v1 / v2 + v2 / v1) + dmean * dmean * (1.0 / v1 + 1.0 / v2) - 2.0);
        }
        // s /= (double) dim;
        return s;
    }

    /**
     * Compute the Levenshtein distance thanks to Chas Emerick to Michael Gilleland for this implementation.
     * <p>
     * The difference between this impl. and the current is that, rather than creating and retaining a matrix of size s.length()+1 by t.length()+1, we maintain
     * two single-dimensional arrays of length s.length()+1.
     * <p>
     * The first, d, is the 'current working' distance array that maintains the newest distance cost counts as we iterate through the characters of String s.
     * Each time we increment the index of String t we are comparing, d is copied to p, the second int[]. Doing so allows us to retain the previous cost counts
     * as required by the algorithm (taking the minimum of the cost count to the left, up one, and diagonally up and to the left of the current cost count being
     * calculated). (Note that the arrays aren't really copied anymore, just switched...this is clearly much better than cloning an array or doing a
     * System.arraycopy() each time through the outer loop.).
     * <p>
     * Effectively, the difference between the two implementations is this one does not cause an out of memory condition when calculating the LD over two very
     * large strings.
     *
     * @see <a href="http://www.merriampark.com/ldjava.htm">Levenshtein Distance Algorithm: Java Implementation, by Chas Emerick</a>
     * @param s a string
     * @param t a straing
     * @return the distance
     */
    public static int levenshteinDistance(String s, String t) {
        if ((s == null) || (t == null)) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n + 1]; // 'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; // place holder to assist in swapping p and d

        char t_j; // jth character of t
        int cost; // cost

        for (int i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (int j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;
            for (int i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }
            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }

    public static HashMap<String, Double> computeBeliefFunctions(HashMap<String, Double> hash1, HashMap<String, Double> hash2) {
        HashMap<String, Double> result = new HashMap<String, Double>();

        for (String key1 : hash1.keySet()) {
            Double score1 = hash1.get(key1);
            System.out.println(key1 + "-->" + score1);
            //foreach value of the hash1, we will compute the belief score
            for (String key2 : hash2.keySet()) {
                Double score2 = hash2.get(key2);
                //Same key ok to compute
                if (key1.equals(key2)) {
                    Double oldScore;
                    if (!result.containsKey(key1)) {
                        oldScore = new Double(0);
                    } else {
                        oldScore = result.get(key1);
                    }
                    System.out.println("1 - Key1 (" + key1 + " " + score1 + ") / key2 (" + key2 + " " + score2 + ") -->" + score1 * score2);
                    System.out.println("Adding " + (score1 * score2) + " to " + key1 + " : " + oldScore.doubleValue() + "\n");
                    result.put(key1, oldScore.doubleValue() + score1 * score2);
                } else if (key2.equals("_omega_")) {
                    Double oldScore;
                    if (!result.containsKey(key1)) {
                        oldScore = new Double(0);
                    } else {
                        oldScore = result.get(key1);
                    }
                    System.out.println("2 - Key1 (" + key1 + " " + score1 + ") / key2 (" + key2 + " " + score2 + ") -->" + score1 * score2);
                    System.out.println("Adding " + (score1 * score2) + " to " + key1 + " : " + oldScore.doubleValue() + "\n");

                    result.put(key1, oldScore.doubleValue() + score1 * score2);
                } else if (key1.equals("_omega_")) {
                    Double oldScore;

                    if (!result.containsKey(key2)) {
                        oldScore = new Double(0);
                    } else {
                        oldScore = result.get(key2);
                    }
                    System.out.println("3 - Key1 (" + key1 + " " + score1 + ") / key2 (" + key2 + " " + score2 + ") -->" + score1 * score2);
                    System.out.println("Adding " + (score1 * score2) + " to " + key2 + " : " + oldScore.doubleValue() + "\n");

                    result.put(key2, oldScore.doubleValue() + score1 * score2);
                }
            }
        }
        return result;
    }

}
