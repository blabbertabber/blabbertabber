/**
 *
 * <p>
 * GMMFactory
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
 * Factory of model
 *
 */
package fr.lium.spkDiarization.libModel.gaussian;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.parameter.ParameterEM;
import fr.lium.spkDiarization.parameter.ParameterInitializationEM.ModelInitializeMethod;
import fr.lium.spkDiarization.parameter.ParameterMAP;
import fr.lium.spkDiarization.parameter.ParameterVarianceControl;

/**
 * A factory for creating GMM objects.
 */
public class GMMFactory {

    /**
     * The Constant logger.
     */
    private final static Logger logger = Logger.getLogger(GMMFactory.class.getName());

    /**
     * Check covariance.
     *
     * @param gmm the gmm
     * @param gaussianCovarianceControl the g cov ctrl
     * @param parameterVarianceControl the variance control
     *
     * @throws DiarizationException the diarization exception
     */
    protected static void checkCovaraiance(GMM gmm, Gaussian gaussianCovarianceControl, ParameterVarianceControl parameterVarianceControl) throws DiarizationException {
        int dim = gmm.getDimension();
        for (int i = 0; i < gmm.getNbOfComponents(); i++) {
            Gaussian gaussian = gmm.getComponent(i);
            boolean floringDone = false;
            boolean cellingDone = false;
            for (int j = 0; j < dim; j++) {
                double ctrl = gaussianCovarianceControl.getCovariance(j, j);
                double covarianceJJ = gaussian.getCovariance(j, j);
                double controlFloring = ctrl * parameterVarianceControl.getFlooring();
                double controlCelling = ctrl * parameterVarianceControl.getCeilling();
                if (covarianceJJ < controlFloring) {
                    gaussian.setCovariance(j, j, controlFloring);

                    if (SpkDiarizationLogger.DEBUG) {
                        logger.finest("variance of idx=" + i + " flooring " + covarianceJJ + " " + controlFloring);
                    }
                    floringDone = true;
                }
                /*
                 * if(v < 0.1){ g.setCovariance(j, j, 0.1); flooringDone = true; }
                 */
                if (covarianceJJ > controlCelling) {
                    gaussian.setCovariance(j, j, controlCelling);
                    if (SpkDiarizationLogger.DEBUG) {
                        logger.finest("variance of idx=" + i + " ceilling " + covarianceJJ + " " + controlCelling);
                    }
                    cellingDone = true;
                }
            }
            if (cellingDone || floringDone) {
                gaussian.computeInvertCovariance();
                gaussian.setGLR();
                gaussian.computeLikelihoodConstant();
                if (floringDone) {
                    logger.warning("variance of gaussian idx=" + i + " flooring ");
                }
                if (cellingDone) {
                    logger.warning("variance of gaussian idx=" + i + " ceilling ");
                }
            }

        }
    }

    /**
     * Training: get a model learned by EM-ML.
     *
     * @param cluster the cluster
     * @param featureSet the features
     * @param initializationGmm the initialization model
     * @param nbComponent the number of component
     * @param parameterEM the EM control parameter
     * @param parameterVarianceControl the variance control parameter
     * @param useSpeechDetection the use speech detection
     * @return the EM
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static GMM getEM(Cluster cluster, AudioFeatureSet featureSet, GMM initializationGmm, int nbComponent, ParameterEM parameterEM, ParameterVarianceControl parameterVarianceControl, boolean useSpeechDetection) throws DiarizationException, IOException {
        Gaussian gaussian = null;
        if (initializationGmm.getGaussianKind() == Gaussian.FULL) {
            gaussian = new FullGaussian(initializationGmm.getDimension());
        } else {
            gaussian = new DiagGaussian(initializationGmm.getDimension());
        }
        GMMFactory.initializeGaussian(featureSet, gaussian, cluster);
        GMM gmmResult = new GMM();
        GMM gmmCurrent = (initializationGmm.clone());
        double oldScore = GMMFactory.iterationEM(cluster, featureSet, initializationGmm, gmmCurrent, gaussian, parameterVarianceControl, useSpeechDetection);
        logger.finer("NbComp=" + gmmCurrent.getNbOfComponents() + " first llh=" + oldScore);

        for (int i = 0; i < parameterEM.getMaximumIteration(); i++) {
            double score = iterationEM(cluster, featureSet, gmmCurrent, gmmResult, gaussian, parameterVarianceControl, useSpeechDetection);
            double dg = score - oldScore;
            logger.finer("\t i=" + i + " llh=" + score + " delta=" + dg);

            oldScore = score;
            if ((i >= parameterEM.getMinimumIteration()) && (dg < parameterEM.getMinimumGain())) {
                break;
            }
            gmmCurrent = (gmmResult.clone());
        }
        gmmCurrent.setName(cluster.getName());
        gmmCurrent.setGender(cluster.getGender());
        return gmmCurrent;
    }

    /**
     * Training: get a model learned by MAP-ML.
     *
     * @param cluster the cluster
     * @param featureSet the features
     * @param initializationGmm the initialization model
     * @param ubm the universal background model
     * @param parameterEM the EM control parameter
     * @param parameterMAP the map control
     * @param parameterVarianceControl the variance control parameter
     * @param useSpeechDetection if true read speech detector information in
     * segment of the cluster
     * @param resetAccumulator the reset accumulator
     * @return the MAP
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static GMM getMAP(Cluster cluster, AudioFeatureSet featureSet, GMM initializationGmm, GMM ubm, ParameterEM parameterEM, ParameterMAP parameterMAP, ParameterVarianceControl parameterVarianceControl, boolean useSpeechDetection, boolean resetAccumulator) throws DiarizationException, IOException {
        Gaussian gaussian = null;
        if (initializationGmm.getGaussianKind() == Gaussian.FULL) {
            gaussian = new FullGaussian(initializationGmm.getDimension());
        } else {
            gaussian = new DiagGaussian(initializationGmm.getDimension());
        }
        GMMFactory.initializeGaussian(featureSet, gaussian, cluster);

        double oldScore = -5000.0;
        GMM gmmCurrent = (initializationGmm.clone());
        GMM gmmResult = new GMM();
        int nbFeature = cluster.getLength();
        GMM ubmTmp = ubm;

        for (long i = 0; i < parameterEM.getMaximumIteration(); i++) {
            double score = GMMFactory.iterationMAP(cluster, featureSet, gmmCurrent, ubmTmp, gmmResult, gaussian, parameterMAP, parameterVarianceControl, useSpeechDetection, resetAccumulator);
            double deltaScore = score - oldScore;
            String message = "i=" + i + " llh=" + score;
            if (oldScore != -5000.0) {
                message += " gain=" + deltaScore;
            }
            message += " Cluster name=" + cluster.getName() + " cluster length=" + nbFeature;
            logger.finer(message);

            oldScore = score;
            if ((i >= parameterEM.getMinimumIteration()) && (deltaScore < parameterEM.getMinimumGain())) {
                break;
            }
            gmmCurrent = (gmmResult.clone());
            if (parameterMAP.getMethod() == ParameterMAP.MAPMethod.VPMAP) {
                logger.finest("clone current for VPMAP ");
                ubmTmp = (gmmCurrent.clone());
            }
        }
        gmmResult.setName(cluster.getName());
        gmmResult.setGender(cluster.getGender());
        logger.finer("");
        return gmmResult;
    }

    /**
     * Gets the map.
     *
     * @param cluster the cluster
     * @param featureSet the feature set
     * @param initializationGmm the initialization gmm
     * @param ubm the ubm
     * @param parameterEM the parameter em
     * @param parameterMAP the parameter map
     * @param parameterVarianceControl the parameter variance control
     * @param useSpeechDetection the use speech detection
     * @return the map
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static GMM getMAP(Cluster cluster, AudioFeatureSet featureSet, GMM initializationGmm, GMM ubm, ParameterEM parameterEM, ParameterMAP parameterMAP, ParameterVarianceControl parameterVarianceControl, boolean useSpeechDetection) throws DiarizationException, IOException {
        return getMAP(cluster, featureSet, initializationGmm, ubm, parameterEM, parameterMAP, parameterVarianceControl, useSpeechDetection, true);
    }

    /**
     * Initialization: user initialization method.
     *
     * @param name the name
     * @param cluster the cluster
     * @param featureSet the features
     * @param gaussianKind the kind
     * @param nbComponent the number of component
     * @param modelToInitializeMethod model to initialize the training
     * @param parameterEM the EM control parameter
     * @param parameterVarianceControl the variance control parameter
     * @param useSpeechDetection the use speech detection
     * @return the gMM
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static GMM initializeGMM(String name, Cluster cluster, AudioFeatureSet featureSet, int gaussianKind, int nbComponent, ModelInitializeMethod modelToInitializeMethod, ParameterEM parameterEM, ParameterVarianceControl parameterVarianceControl, boolean useSpeechDetection) throws DiarizationException, IOException {
        int dim = featureSet.getFeatureSize();
        GMM gmm = new GMM(1, dim, gaussianKind);

        gmm.setName(name);

        Gaussian gaussian = gmm.getComponent(0);
        gaussian.statistic_initialize();
        gaussian.statistic_addFeatures(cluster, featureSet);
        gaussian.setModel();

        if (modelToInitializeMethod.equals(ModelInitializeMethod.TRAININIT_UNIFORM)) {
// gmm = uniformInit(cluster.iterator(), gmm, features, nbComp);
            gmm = globalAndUniformInit(cluster, gmm, featureSet, nbComponent);
        } else {
            int oldNbComponent = gmm.getNbOfComponents();
            while (gmm.getNbOfComponents() < nbComponent) {
                GMM gmmSplit;
                if (modelToInitializeMethod.equals(ModelInitializeMethod.TRAININIT_SPLIT)) {
                    if (SpkDiarizationLogger.DEBUG) {
                        logger.fine("$$$ init spilt on");
                    }
                    gmmSplit = GMMFactory.splitSup(gmm, nbComponent);
                } else {
                    if (SpkDiarizationLogger.DEBUG) {
                        logger.fine("$$$ init spilt all");
                    }
                    gmmSplit = GMMFactory.splitAll(gmm, nbComponent);
                }
                if (oldNbComponent >= gmmSplit.getNbOfComponents()) {
                    throw new DiarizationException("GMMFactory::init: number of components is not increased");
                }
                // gmmSplit.debug(4);

                gmm = getEM(cluster, featureSet, gmmSplit, nbComponent, parameterEM, parameterVarianceControl, useSpeechDetection);
            }
        }
        return gmm;
    }

    /**
     * Initialize a Gaussian from #start to #start + #len.
     *
     * @param featureSet a feature container
     * @param gaussian the Gaussian
     * @param start of the feature / segment
     * @param length length of the segment
     *
     * @return the likelihood
     *
     * @throws DiarizationException the diarization exception
     */
    public static int initializeGaussian(AudioFeatureSet featureSet, Gaussian gaussian, int start, int length) throws DiarizationException {
        gaussian.statistic_initialize();
        for (int i = start; i < (start + length); i++) {
            gaussian.statistic_addFeature(featureSet, i);
        }
        return gaussian.setModel();
    }

    /**
     * Compute global Gaussian.
     *
     * @param featureSet the features
     * @param gaussian the gaussian
     * @param cluster the cluster
     * @return the likelihood
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static int initializeGaussian(AudioFeatureSet featureSet, Gaussian gaussian, Cluster cluster) throws DiarizationException, IOException {
        gaussian.statistic_initialize();
        gaussian.statistic_addFeatures(cluster, featureSet);
        // for (Segment segment :cluster) {
        // Segment segment = iteratorSegment.next();
        // int start = segment.getStart();
        // int end = start + segment.getLength();
        // featureSet.setCurrentShow(segment.getShowName());
        // for(int i: segment) {
        // for (int i = start; i < end; i++) { // for all features
        // gaussian.statistic_addFeature(featureSet, i);
        // }
        // }

        return gaussian.setModel();
    }

    /**
     * Training: accumulation iteration for EM or MAP.
     *
     * @param cluster the cluster
     * @param featureSet the features
     * @param gmmInitialization the initialization model
     * @param gmmResult the result
     * @param useSpeechDetection the use speech detection
     * @return the double
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static double iterationAccumulation(Cluster cluster, AudioFeatureSet featureSet, GMM gmmInitialization, GMM gmmResult, boolean useSpeechDetection) throws DiarizationException, IOException {
        boolean ok = false;
        Gaussian gaussian = null;
        if (gmmInitialization.getGaussianKind() == Gaussian.FULL) {
            gaussian = new FullGaussian(gmmInitialization.getDimension());
        } else {
            gaussian = new DiagGaussian(gmmInitialization.getDimension());
        }
        // int nbComponent = gmmInitialization.getNbOfComponents();
        int NbFeatureUsed = 0;
        gmmResult.replaceWithGMM(gmmInitialization);
        // logger.info("comp="+nbComponent+" // dim="+gmmInitialization.getDimension());
        gmmResult.statistic_initialize();
        gmmResult.score_initialize();
        
		
        for (Segment segment : cluster) {
            // Segment segment = iteratorSegment.next();
            // int start = segment.getStart();
            // int end = start + segment.getLength();
            featureSet.setCurrentShow(segment.getShowName());
            // for (int i = start; i < end; i++) { // for all features
            for (int i : segment) {
                if ((useSpeechDetection == true) && (Distance.useThisFeature(segment, i, useSpeechDetection) == false)) {
                    continue;
                }
                if (SpkDiarizationLogger.DEBUG) {
                    NbFeatureUsed++;
                }
                // compute the Likelihood of the feature given the GMM
                float[] feature = featureSet.getFeatureUnsafe(i);
                double likelihoodGMM = gmmResult.score_getAndAccumulate(feature);
                gaussian.statistic_initialize();
                gaussian.statistic_addFeature(feature);
                for (Gaussian gaussianResult : gmmResult) {
                    // for (int j = 0; j < nbComponent; j++) {
                    // Compute the contribution of the feature for a gaussian, get the Likelihood of the gaussian computed above
                    double likelihoodGaussian = gaussianResult.score_getScore();
                    double weight = likelihoodGaussian / likelihoodGMM;
                    gaussianResult.statistic_add(gaussian, weight);
                }
            }
        }
        if (SpkDiarizationLogger.DEBUG) {
            logger.finest("itEM nbFeature use:" + NbFeatureUsed);
        }

        double meanLogLh = gmmResult.score_getMeanLog();
        return meanLogLh;
    }

    /**
     * Training: EM iteration.
     *
     * @param cluster the cluster
     * @param featureSet the features
     * @param gmmInitialization the initialization model
     * @param gmmResult the res
     * @param gaussianControlCovariance the gaussian control covariance
     * @param parameterVarianceControl the variance control
     * @param useSpeechDetection the use speech detection
     * @return the double
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected static double iterationEM(Cluster cluster, AudioFeatureSet featureSet, GMM gmmInitialization, GMM gmmResult, Gaussian gaussianControlCovariance, ParameterVarianceControl parameterVarianceControl, boolean useSpeechDetection) throws DiarizationException, IOException {
        double meanLogLikelihood = GMMFactory.iterationAccumulation(cluster, featureSet, gmmInitialization, gmmResult, useSpeechDetection);
        gmmResult.setModel();
        checkCovaraiance(gmmResult, gaussianControlCovariance, parameterVarianceControl);
        // normalizeGMM(gmmResult);
        gmmResult.statistic_reset();
        // res.resetScore();
        return meanLogLikelihood;
    }

    /**
     * Training: MAP iteration.
     *
     * @param cluster the cluster
     * @param featureSet the features
     * @param gmmInitialization the initialization model
     * @param ubm the universal background model
     * @param gmmResult the res
     * @param gaussianControlCovariance the gaussian control covariance
     * @param parameterMAP the map control
     * @param parameterVarianceControl the variance control
     * @param useSpeechDetection the use speech detection
     * @param resetAccumulator the reset accumulator
     * @return the double
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected static double iterationMAP(Cluster cluster, AudioFeatureSet featureSet, GMM gmmInitialization, GMM ubm, GMM gmmResult, Gaussian gaussianControlCovariance, ParameterMAP parameterMAP, ParameterVarianceControl parameterVarianceControl, boolean useSpeechDetection, boolean resetAccumulator) throws DiarizationException, IOException {
        double meanLogLikelihood = 0;
        meanLogLikelihood = iterationAccumulation(cluster, featureSet, gmmInitialization, gmmResult, useSpeechDetection);
        gmmResult.setAdaptedModel(ubm, parameterMAP);
        GMMFactory.checkCovaraiance(gmmResult, gaussianControlCovariance, parameterVarianceControl);
        // normalizeGMM(gmmResult);
        if (resetAccumulator == true) {
            gmmResult.statistic_reset();
        }
        return meanLogLikelihood;
    }

    /**
     * Initialization: split all Gaussians.
     *
     * @param gmmInitialization the initialization model
     * @param maxNbComponent the maximum number of components
     *
     * @return the GMM
     *
     * @throws DiarizationException the diarization exception
     */
    protected static GMM splitAll(GMM gmmInitialization, int maxNbComponent) throws DiarizationException {
        GMM gmm = (gmmInitialization.clone());
        GaussianList gaussianList = gmm.getComponents();
        Collections.sort(gaussianList);
        int nbComp = gmm.getNbOfComponents();
        double epsilon = 0.1;

        for (int i = 0; i < nbComp; i++) {
            splitOne(gmm, i, epsilon);
            if (gmm.getNbOfComponents() >= maxNbComponent) {
                break;
            }
        }
        return gmm;
    }

    /**
     * Initialization: split a Gaussian.
     *
     * @param gmm the model
     * @param index the index
     *
     * @throws DiarizationException the diarization exception
     */
    protected static void splitOne(final GMM gmm, final int index) throws DiarizationException {
        GMMFactory.splitOne(gmm, index, 0.01);
    }

    /**
     * Initialization: split a Gaussian.
     *
     * @param gmm the model
     * @param index the index
     * @param epsilon the perturbation factor of the covariance matrix
     *
     * @throws DiarizationException the diarization exception
     */
    protected static void splitOne(GMM gmm, int index, double epsilon) throws DiarizationException {
        int dim = gmm.getDimension();
        int nbComponent = gmm.getNbOfComponents();

        if (index > nbComponent) {
            throw new DiarizationException("GMMSplit: splitOne() 1 error (idx > nbComp)");
        }

        Gaussian gaussian1 = gmm.getComponent(index);
        gaussian1.setWeight(gaussian1.getWeight() * 0.5);
        Gaussian gaussian2 = gmm.addComponent(gaussian1);
        for (int i = 0; i < dim; i++) {
            double factor = epsilon * Math.sqrt(gaussian1.getCovariance(i, i));
            double v = gaussian1.getMean(i);
            gaussian1.setMean(i, v - factor);
            gaussian2.setMean(i, v + factor);
        }
    }

    /**
     * Initialization: split the Gaussians whose weight is greater than 1/number
     * of components.
     *
     * @param gmmInitialization the initialization model
     * @param maxNbComponent the maximum number of components
     *
     * @return the GMM
     *
     * @throws DiarizationException the diarization exception
     */
    protected static GMM splitSup(GMM gmmInitialization, int maxNbComponent) throws DiarizationException {
        GMM gmm = (gmmInitialization.clone());
        GaussianList gaussianList = gmm.getComponents();
        int nbComponent = gmm.getNbOfComponents();
        Collections.sort(gaussianList);
        double threshold = (1.0 / nbComponent);
        // double epsi = 0.1;
        for (int i = 0; i < nbComponent; i++) {
            if (gaussianList.get(i).getWeight() >= threshold) {
                splitOne(gmm, i);
            } else {
                break;
            }
            if (gmm.getNbOfComponents() >= maxNbComponent) {
                break;
            }
        }

        return gmm;
    }

    /**
     * Initialization: uniform initialization, ie random initialization of the
     * means.
     *
     * @param cluster the cluster
     * @param gmmInitialization the initialization model
     * @param featureSet the features
     * @param maxNbComponent the maximum number of components
     * @return the GMM
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected static GMM uniformInit(Cluster cluster, GMM gmmInitialization, AudioFeatureSet featureSet, int maxNbComponent) throws DiarizationException, IOException {
        GMM gmm = (gmmInitialization.clone());
        int nbComponent = maxNbComponent;
        int length = 5;
        double weight = 1.0 / nbComponent;
        for (int i = 1; i < nbComponent; i++) {
            splitOne(gmm, 0, 0.0);
        }
        int nb = 0;
        ArrayList<Integer> freatureIndexList = new ArrayList<Integer>();
        ArrayList<String> showIndexList = new ArrayList<String>();
        for (Segment segment : cluster) {
            nb += segment.getLength();
            int start = segment.getStart();
            int end = start + segment.getLength();
            // int idx = seg.getShowIndex();
            for (int i = start; i < end; i++) {
                freatureIndexList.add(i);
                showIndexList.add(segment.getShowName());
            }
        }
        int step = (nb - 1) / nbComponent;
        // int step = nb / nbComp;
        if (SpkDiarizationLogger.DEBUG) {
            logger.finest("step:" + step);
        }
        int dim = featureSet.getFeatureSize();
        for (int i = 0; i < nbComponent; i++) {
            for (int k = 0; k < dim; k++) {
                double sum = 0.0;
                for (int j = 0; j < length; j++) {
                    int index = freatureIndexList.get((i * step) + j);
                    featureSet.setCurrentShow(showIndexList.get((i * step) + j));
                    sum += featureSet.getFeatureUnsafe(index)[k];
                }
                double mean = sum / length;
                gmm.getComponent(i).setMean(k, mean);
            }
            gmm.getComponent(i).setWeight(weight);
            gmm.getComponent(i).setGLR();
        }
        return gmm;
    }

    /**
     * Initialization: uniform initialization, ie random initialization of the
     * means.
     *
     * @param cluster the cluster
     * @param gmmInitialization the initialization model
     * @param featureSet the features
     * @param maxNbComponent the maximum number of components
     * @return the GMM
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected static GMM globalAndUniformInit(Cluster cluster, GMM gmmInitialization, AudioFeatureSet featureSet, int maxNbComponent) throws DiarizationException, IOException {
        GMM gmm = (gmmInitialization.clone());
        Gaussian gaussian = gmm.getComponent(0);
        int nbComponent = maxNbComponent;
        int length = 1;
        double weight = 1.0 / nbComponent;

        for (int i = 1; i < nbComponent; i++) {
            gmm.addComponent(gaussian);
        }

        int nb = 0;
        ArrayList<Integer> freatureIndexList = new ArrayList<Integer>();
        ArrayList<String> showIndexList = new ArrayList<String>();
        for (Segment segment : cluster) {
            nb += segment.getLength();
            int start = segment.getStart();
            int end = start + segment.getLength();
            // int idx = seg.getShowIndex();
            for (int i = start; i < end; i++) {
                freatureIndexList.add(i);
                showIndexList.add(segment.getShowName());
            }
        }
        int step = nb / nbComponent;
        // int step = nb / nbComp;
        if (SpkDiarizationLogger.DEBUG) {
            logger.finest("step:" + step);
        }
        int dim = featureSet.getFeatureSize();
        gmm.getComponent(0).setWeight(weight);
        gmm.getComponent(0).setGLR();
        for (int i = 1; i < nbComponent; i++) {
            for (int k = 0; k < dim; k++) {
                double sum = 0.0;
                for (int j = 0; j < length; j++) {
                    int idx = freatureIndexList.get((i * step) + j);
                    featureSet.setCurrentShow(showIndexList.get((i * step) + j));
                    sum += featureSet.getFeatureUnsafe(idx)[k];
                }
                double mean = sum / length;
                gmm.getComponent(i).setMean(k, mean);
            }
            gmm.getComponent(i).setWeight(weight);
            gmm.getComponent(i).setGLR();
        }
        return gmm;
    }

    // From ALIZE
    /**
     * Gaussian fusion.
     *
     * @param gaussian1 the gaussian1
     * @param gaussian2 the gaussian2
     * @return the diag gaussian
     * @throws DiarizationException the diarization exception
     */
    static DiagGaussian gaussianFusion(DiagGaussian gaussian1, DiagGaussian gaussian2) throws DiarizationException {
        DiagGaussian result = gaussian1.clone();
        int dimension = gaussian1.getDimension();
        double weight1 = gaussian1.getWeight();
        double weight2 = gaussian2.getWeight();

        double a1 = weight1 / (weight1 + weight2);
        double a2 = 1.0 - a1;
        for (int k = 0; k < dimension; k++) {
            double d = gaussian1.getMean(k) - gaussian2.getMean(k);
            double v = (a1 * gaussian1.getCovariance(k, k)) + (a2 * gaussian2.getCovariance(k, k)) + (a1 * a2 * d * d);
            result.setCovariance(k, k, v);
            result.setMean(k, (a1 * gaussian1.getMean(k)) + (a2 * gaussian2.getMean(k)));
        }
        result.setWeight(weight1 + weight2);
        result.computeInvertCovariance();
        result.setGLR();
        result.computeLikelihoodConstant();
        return result;
    }

    // From ALIZE
    /**
     * Gmm fusion.
     *
     * @param gmm the gmm
     * @return the diag gaussian
     * @throws DiarizationException the diarization exception
     */
    static DiagGaussian gmmFusion(GMM gmm) throws DiarizationException {
        int distribCount = gmm.getNbOfComponents();
        DiagGaussian g = (DiagGaussian) gmm.getComponent(0);
        for (int i = 1; i < distribCount; i++) {
            g = gaussianFusion((DiagGaussian) gmm.getComponent(i), g);
        }
        return g;
    }

    // From ALIZE
    /**
     * Normalize gmm.
     *
     * @param gmm the gmm
     * @throws DiarizationException the diarization exception
     */
    static void normalizeGMM(GMM gmm) throws DiarizationException {
        DiagGaussian fusioned = gmmFusion(gmm);
        for (int i = 0; i < gmm.getNbOfComponents(); i++) {
            DiagGaussian gaussian = (DiagGaussian) gmm.getComponent(i);
            for (int j = 0; j < gaussian.getDimension(); j++) {
                gaussian.setMean(j, (gaussian.getMean(j) - fusioned.getMean(j)) / fusioned.getCovariance(j, j));
                gaussian.setCovariance(j, j, gaussian.getCovariance(j, j) / fusioned.getCovariance(j, j));
            }
            gaussian.computeInvertCovariance();
            gaussian.setGLR();
            gaussian.computeLikelihoodConstant();
        }
    }
}
