/**
 * <p>
 * GMMFactory
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
 * Factory of model
 */

package fr.lium.spkDiarization.libModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.ParameterEM;
import fr.lium.spkDiarization.parameter.ParameterInitializationEM.ModelInitMethod;
import fr.lium.spkDiarization.parameter.ParameterMAP;
import fr.lium.spkDiarization.parameter.ParameterTopGaussian;
import fr.lium.spkDiarization.parameter.ParameterVarianceControl;

/**
 * A factory for creating GMM objects.
 */
public class GMMFactory {

    /**
     * Check covariance.
     *
     * @param gmm the gmm
     * @param gCovCtrl the g cov ctrl
     * @param varianceControl the variance control
     *
     * @throws DiarizationException the diarization exception
     */
    protected static void checkCovaraiance(GMM gmm, Gaussian gCovCtrl, ParameterVarianceControl varianceControl) throws DiarizationException {
        int dim = gmm.getDim();
        for (int i = 0; i < gmm.getNbOfComponents(); i++) {
            Gaussian g = gmm.getComponent(i);
            boolean flooringDone = false;
            boolean cellingDone = false;
            for (int j = 0; j < dim; j++) {
                double ctrl = gCovCtrl.getCovariance(j, j);
                double v = g.getCovariance(j, j);
                double ctrlf = ctrl * varianceControl.getFlooring();
                double ctrlc = ctrl * varianceControl.getCeilling();
                if (v < ctrlf) {
                    g.setCovariance(j, j, ctrlf);

                    //System.out.println("Warning \t variance of idx=" + i + " flooring " + v + " " + ctrlf);
                    flooringDone = true;
                }
                /*if(v < 0.1){
					g.setCovariance(j, j, 0.1);
					flooringDone = true;
				}*/
                if (v > ctrlc) {
                    g.setCovariance(j, j, ctrlc);
                    //System.out.println("Warning \t variance of idx=" + i + " ceilling " + v + " " + ctrlc);
                    cellingDone = true;
                }
            }
            if (cellingDone || flooringDone) {
                g.computeInvertCovariance();
                g.setGLR();
                g.computeLikelihoodConstant();
                if (flooringDone) {
                    System.out.println("Warning \t variance of gaussian idx=" + i + " flooring ");
                }
                if (cellingDone) {
                    System.out.println("Warning \t variance of gaussian idx=" + i + " ceilling ");
                }
            }
        }
    }

    /**
     * Training: get a model learned by EM-ML.
     *
     * @param cluster the cluster
     * @param features the features
     * @param init the initialization model
     * @param nbComp the number of component
     * @param emControl the EM control parameter
     * @param varianceControl the variance control parameter
     *
     * @return the EM
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static GMM getEM(Cluster cluster, FeatureSet features, GMM init, int nbComp, ParameterEM emControl, ParameterVarianceControl varianceControl)
            throws DiarizationException, IOException {
        return GMMFactory.getEM(cluster, features, init, nbComp, emControl, varianceControl, true);
    }

    /**
     * Training: get a model learned by EM-ML.
     *
     * @param cluster the cluster
     * @param features the features
     * @param init the initialization model
     * @param nbComp the number of component
     * @param emControl the EM control parameter
     * @param varianceControl the variance control parameter
     * @param trace the trace
     *
     * @return the EM
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static GMM getEM(Cluster cluster, FeatureSet features, GMM init, int nbComp, ParameterEM emControl, ParameterVarianceControl varianceControl,
                            boolean trace) throws DiarizationException, IOException {
        Gaussian g = null;
        if (init.getKind() == Gaussian.FULL) {
            g = new FullGaussian(init.getDim());
        } else {
            g = new DiagGaussian(init.getDim());
        }
        GMMFactory.initializeGaussian(features, g, cluster.iterator());
        GMM res = new GMM();
        GMM cur = (GMM) (init.clone());
        double old = GMMFactory.itEM(cluster.iterator(), features, init, cur, g, varianceControl);
        if (trace) {
            System.out.println("trace[GMMFactory[EM]] \t NbComp=" + cur.getNbOfComponents() + " first llh=" + old);
        }
        for (int i = 0; i < emControl.getMaximumIteration(); i++) {
            double v = itEM(cluster.iterator(), features, cur, res, g, varianceControl);
            double dg = v - old;
            if (trace) {
                System.out.print("trace[GMMFactory[EM]] \t i=" + i + " llh=" + v);
                System.out.println(" delta=" + dg);
            }
            old = v;
            if ((i >= emControl.getMinimumIteration()) && (dg < emControl.getMinimumGain())) {
                break;
            }
            cur = (GMM) (res.clone());
        }
        cur.setName(cluster.getName());
        cur.setGender(cluster.getGender());
        return cur;
    }

    /**
     * Gets the mAP.
     *
     * @param cluster the cluster
     * @param features the features
     * @param ubm the universal background model
     * @param init the initialization model
     * @param emControl the EM control parameter
     * @param varianceControl the variance control parameter
     * @param mapControl the map control
     * @param topGaussian the top gaussian
     *
     * @return the mAP
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static GMM getMAP(Cluster cluster, FeatureSet features, GMM init, GMM ubm, ParameterEM emControl, ParameterMAP mapControl,
                             ParameterVarianceControl varianceControl, ParameterTopGaussian topGaussian) throws DiarizationException, IOException {
        // return GMMFactory.getMAP(x, features, init, wld, alpha, m, c, w,
        // minIt, maxIt, minGain, flooring, ceilling, true);
        return GMMFactory.getMAP(cluster, features, init, ubm, emControl, mapControl, varianceControl, topGaussian, true);
    }

    /**
     * Training: get a model learned by MAP-ML.
     *
     * @param cluster the cluster
     * @param features the features
     * @param ubm the universal background model
     * @param init the initialization model
     * @param emControl the EM control parameter
     * @param varianceControl the variance control parameter
     * @param mapControl the map control
     * @param topGaussian the top gaussian
     * @param trace the trace
     *
     * @return the MAP
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static GMM getMAP(Cluster cluster, FeatureSet features, GMM init, GMM ubm, ParameterEM emControl, ParameterMAP mapControl,
                             ParameterVarianceControl varianceControl, ParameterTopGaussian topGaussian, boolean trace) throws DiarizationException, IOException {
        Gaussian g = null;
        if (init.getKind() == Gaussian.FULL) {
            g = new FullGaussian(init.getDim());
        } else {
            g = new DiagGaussian(init.getDim());
        }
        GMMFactory.initializeGaussian(features, g, cluster.iterator());

        double old = -5000.0;
        GMM cur = (GMM) (init.clone());
        GMM res = new GMM();
        int nbFeature = cluster.getLength();
        GMM wldTmp = ubm;

        for (long i = 0; i < emControl.getMaximumIteration(); i++) {
            double v = GMMFactory.itMAP(cluster.iterator(), features, cur, wldTmp, res, g, mapControl, varianceControl, topGaussian);
            double g1 = v - old;
            if (trace) {
                System.out.print("trace[GMMFactory[MAP]] \t i=" + i + " llh=" + v);
                if (old != -5000.0) {
                    System.out.print(" gain=" + g1);
                }
                System.out.println(" nb features=" + nbFeature);
            }
            old = v;
            if ((i >= emControl.getMinimumIteration()) && (g1 < emControl.getMinimumGain())) {
                break;
            }
            cur = (GMM) (res.clone());
            if (mapControl.getMethod() == ParameterMAP.MAPMethod.VPMAP) {
                System.out.println("trace[GMMFactory[MAP]] \t clone current for VPMAP ");
                wldTmp = (GMM) (cur.clone());
            }
        }
        res.setName(cluster.getName());
        res.setGender(cluster.getGender());
        return res;
    }

    /**
     * Initialization: user initialization method.
     *
     * @param name the name
     * @param cluster the cluster
     * @param features the features
     * @param kind the kind of model
     * @param nbComp the number of component
     * @param modelToInitializeTraining model to initialize the training
     * @param emControl the EM control parameter
     * @param varianceControl the variance control parameter
     *
     * @return the GMM
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static GMM initilizeGMM(String name, Cluster cluster, FeatureSet features, int kind, int nbComp, ModelInitMethod modelToInitializeTraining, ParameterEM emControl,
                                   ParameterVarianceControl varianceControl) throws DiarizationException, IOException {
        return GMMFactory.initializeGMM(name, cluster, features, kind, nbComp, modelToInitializeTraining, emControl, varianceControl, false);
    }

    /**
     * Initialization: user initialization method.
     *
     * @param name the name
     * @param cluster the cluster
     * @param features the features
     * @param kind the kind
     * @param nbComp the number of component
     * @param modelToInitializeTraining model to initialize the training
     * @param emControl the EM control parameter
     * @param varianceControl the variance control parameter
     * @param trace the trace
     *
     * @return the gMM
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static GMM initializeGMM(String name, Cluster cluster, FeatureSet features, int kind, int nbComp, ModelInitMethod modelToInitializeTraining, ParameterEM emControl,
                                    ParameterVarianceControl varianceControl, boolean trace) throws DiarizationException, IOException {
        int dim = features.getDim();
        GMM gmm = new GMM(1, dim, kind);

        gmm.setName(name);

        Gaussian g = gmm.getComponent(0);
        g.initStatisticAccumulator();
        g.addFeaturesFromSegments(cluster.iterator(), features);
        g.setModelFromAccululator();

        if (modelToInitializeTraining.equals(ModelInitMethod.TRAININIT_UNIFORM)) {
            // System.err.println("$$$ init uniform");

//			gmm = uniformInit(cluster.iterator(), gmm, features, nbComp);
            gmm = globalAndUniformInit(cluster.iterator(), gmm, features, nbComp);
        } else {
            int oldNbComp = gmm.getNbOfComponents();
            while (gmm.getNbOfComponents() < nbComp) {
                GMM gmmSplit;
                if (modelToInitializeTraining.equals(ModelInitMethod.TRAININIT_SPLIT)) {
                    // System.err.println("$$$ init spilt on");
                    gmmSplit = GMMFactory.splitSup(gmm, nbComp);
                } else {
                    // System.err.println("$$$ init spilt all");
                    gmmSplit = GMMFactory.splitAll(gmm, nbComp);
                }
                if (oldNbComp >= gmmSplit.getNbOfComponents()) {
                    throw new DiarizationException("GMMFactory::init: number of components is not increased");
                }

                gmm = getEM(cluster, features, gmmSplit, nbComp, emControl, varianceControl, trace);
            }
        }
        return gmm;
    }

    /**
     * Initialize a Gaussian from #start to #start + #len.
     *
     * @param features a feature container
     * @param g the Gaussian
     * @param start of the feature / segment
     * @param len length of the segment
     *
     * @return the likelihood
     *
     * @throws DiarizationException the diarization exception
     */
    public static int initializeGaussian(FeatureSet features, Gaussian g, int start, int len) throws DiarizationException {
        g.initStatisticAccumulator();
        for (int i = start; i < start + len; i++) {
            g.addFeature(features, i);
        }
        return g.setModelFromAccululator();
    }

    /**
     * Compute global Gaussian.
     *
     * @param features the features
     * @param g the g
     * @param itSeg the segment iterator
     *
     * @return the likelihood
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static int initializeGaussian(FeatureSet features, Gaussian g, Iterator<Segment> itSeg) throws DiarizationException, IOException {
        g.initStatisticAccumulator();
        while (itSeg.hasNext()) {
            Segment segment = itSeg.next();
            int s = segment.getStart();
            int e = s + segment.getLength();
            features.setCurrentShow(segment.getShowName());
            for (int i = s; i < e; i++) { // for all features
                g.addFeature(features, i);
            }
        }

        return g.setModelFromAccululator();
    }

    /**
     * Training: accumulation iteration for EM or MAP.
     *
     * @param itSeg the segment iterator
     * @param features the features
     * @param init the initialization model
     * @param res the res
     *
     * @return the double
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    protected static double itAcc(Iterator<Segment> itSeg, FeatureSet features, GMM init, GMM res) throws DiarizationException, IOException {
        boolean ok = false;
        Gaussian g = null;
        if (init.getKind() == Gaussian.FULL) {
            g = new FullGaussian(init.getDim());
        } else {
            g = new DiagGaussian(init.getDim());
        }
        int nbComp = init.getNbOfComponents();

        res.replaceWithGMM(init);

        res.initStatisticAccumulator();
        res.initScoreAccumulator();

        while (itSeg.hasNext()) {
            Segment segment = itSeg.next();
            int s = segment.getStart();
            int e = s + segment.getLength();
            features.setCurrentShow(segment.getShowName());
            for (int i = s; i < e; i++) { // for all features
				/*double Log_lhGMM = res.getAndAccumulateLogLikelihood(features, i);
				g.initStatisticAccumulator();
				g.addFeature(features, i);
				for (int j = 0; j < nbComp; j++) {
					double log_lhGaussian = res.getComponent(j).getLogLikelihood();
					double w = Math.exp(log_lhGaussian - Log_lhGMM);
					res.getComponent(j).add(g, w);
				}*/


                // compute the Likelihood of the feature given the GMM
                double lhGMM = res.getAndAccumulateLikelihood(features, i, ok);
                g.initStatisticAccumulator();
                g.addFeature(features, i);
                for (int j = 0; j < nbComp; j++) {
                    // Compute the contribution of the feature for a gaussian, get the Likelihood of the gaussian computed above
                    double lhGaussian = res.getComponent(j).getLikelihood();
                    double w = lhGaussian / lhGMM;
                    res.getComponent(j).add(g, w);
                }
            }
        }

        double meanLogLh = res.getMeanLogLikelihood();
        return meanLogLh;
    }

    /**
     * Training: EM iteration.
     *
     * @param itSeg the segment iterator
     * @param features the features
     * @param init the initialization model
     * @param res the res
     * @param gCtrlCov the gaussian control covariance
     * @param varianceControl the variance control
     *
     * @return the double
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected static double itEM(Iterator<Segment> itSeg, FeatureSet features, GMM init, GMM res, Gaussian gCtrlCov, ParameterVarianceControl varianceControl)
            throws DiarizationException, IOException {
        double meanLogLh = GMMFactory.itAcc(itSeg, features, init, res);
        res.setModelFromAccululator();
        GMMFactory.checkCovaraiance(res, gCtrlCov, varianceControl);
        res.resetStatisticAccumulator();
        // res.resetScore();
        return meanLogLh;
    }

    /**
     * Training: MAP iteration.
     *
     * @param itSeg the it seg
     * @param features the features
     * @param init the initialization model
     * @param ubm the universal background model
     * @param res the res
     * @param gCtrlCov the gaussian control covariance
     * @param mapControl the map control
     * @param varianceControl the variance control
     * @param topGaussian the top gaussian
     *
     * @return the double
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    protected static double itMAP(Iterator<Segment> itSeg, FeatureSet features, GMM init, GMM ubm, GMM res, Gaussian gCtrlCov, ParameterMAP mapControl,
                                  ParameterVarianceControl varianceControl, ParameterTopGaussian topGaussian) throws DiarizationException, IOException {
        double meanLogLh = 0;
        meanLogLh = itAcc(itSeg, features, init, res);
        // }
        res.setAdaptedModelFromAccumulator(ubm, mapControl);
        GMMFactory.checkCovaraiance(res, gCtrlCov, varianceControl);
        res.resetStatisticAccumulator();
        // res.resetScore();
        return meanLogLh;
    }

    /**
     * Initialization: split all Gaussians.
     *
     * @param init the initialization model
     * @param maxNbComp the maximum number of components
     *
     * @return the GMM
     *
     * @throws DiarizationException the diarization exception
     */
    protected static GMM splitAll(GMM init, int maxNbComp) throws DiarizationException {
        GMM gmm = (GMM) (init.clone());
        ArrayList<Gaussian> vect = gmm.getComponents();
        Collections.sort(vect);
        int nbComp = gmm.getNbOfComponents();
        double epsi = 0.1;

        for (int i = 0; i < nbComp; i++) {
            splitOne(gmm, i, epsi);
            if (gmm.getNbOfComponents() >= maxNbComp) {
                break;
            }
        }
        return gmm;
    }

    /**
     * Initialization: split a Gaussian.
     *
     * @param gmm the model
     * @param idx the index
     *
     * @throws DiarizationException the diarization exception
     */
    protected static void splitOne(final GMM gmm, final int idx) throws DiarizationException {
        GMMFactory.splitOne(gmm, idx, 0.01);
    }

    /**
     * Initialization: split a Gaussian.
     *
     * @param gmm the model
     * @param idx the index
     * @param epsi the perturbation factor of the covariance matrix
     *
     * @throws DiarizationException the diarization exception
     */
    protected static void splitOne(GMM gmm, int idx, double epsi) throws DiarizationException {
        int dim = gmm.getDim();
        int nbComp = gmm.getNbOfComponents();

        if (idx > nbComp) {
            throw new DiarizationException("GMMSplit: splitOne() 1 error (idx > nbComp)");
        }

        Gaussian g1 = gmm.getComponent(idx);
        g1.setWeight(g1.getWeight() * 0.5);
        Gaussian g2 = gmm.addComponent(g1);
        for (int i = 0; i < dim; i++) {
            double factor = epsi * Math.sqrt(g1.getCovariance(i, i));
            double v = g1.getMean(i);
            g1.setMean(i, v - factor);
            g2.setMean(i, v + factor);
        }
    }

    /**
     * Initialization: split the Gaussians whose weight is greater than 1/number of components.
     *
     * @param init the initialization model
     * @param maxNbComp the maximum number of components
     *
     * @return the GMM
     *
     * @throws DiarizationException the diarization exception
     */
    protected static GMM splitSup(GMM init, int maxNbComp) throws DiarizationException {
        GMM gmm = (GMM) (init.clone());
        ArrayList<Gaussian> vect = gmm.getComponents();
        int nbComp = gmm.getNbOfComponents();
        Collections.sort(vect);
        double thr = (1.0 / nbComp);
        // double epsi = 0.1;
        for (int i = 0; i < nbComp; i++) {
            if (vect.get(i).getWeight() >= thr) {
                splitOne(gmm, i);
            } else {
                break;
            }
            if (gmm.getNbOfComponents() >= maxNbComp) {
                break;
            }
        }

        return gmm;
    }

    /**
     * Initialization: uniform initialization, ie random initialization of the means.
     *
     * @param itSeg the segment iterator
     * @param init the initialization model
     * @param features the features
     * @param maxNbComp the maximum number of components
     *
     * @return the GMM
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    protected static GMM uniformInit(Iterator<Segment> itSeg, GMM init, FeatureSet features, int maxNbComp) throws DiarizationException, IOException {
        GMM gmm = (GMM) (init.clone());
        int nbComp = maxNbComp;
        int len = 5;
        double weight = 1.0 / nbComp;
        for (int i = 1; i < nbComp; i++) {
            splitOne(gmm, 0, 0.0);
        }
        int nb = 0;
        ArrayList<Integer> pFreatures = new ArrayList<Integer>();
        ArrayList<String> idxShow = new ArrayList<String>();
        while (itSeg.hasNext()) {
            Segment segment = itSeg.next();
            nb += segment.getLength();
            int s = segment.getStart();
            int e = s + segment.getLength();
            // int idx = seg.getShowIndex();
            for (int i = s; i < e; i++) {
                pFreatures.add(i);
                idxShow.add(segment.getShowName());
            }
        }
        int step = (nb - 1) / nbComp;
        //int step = nb / nbComp;
        //System.err.println("step:"+step);
        int dim = features.getDim();
        for (int i = 0; i < nbComp; i++) {
            for (int k = 0; k < dim; k++) {
                double s = 0.0;
                for (int j = 0; j < len; j++) {
                    int idx = pFreatures.get(i * step + j);
                    features.setCurrentShow(idxShow.get(i * step + j));
                    s += features.getFeature(idx)[k];
                }
                double v = s / len;
                gmm.getComponent(i).setMean(k, v);
            }
            gmm.getComponent(i).setWeight(weight);
            gmm.getComponent(i).setGLR();
        }
        return gmm;
    }

    /**
     * Initialization: uniform initialization, ie random initialization of the means.
     *
     * @param itSeg the segment iterator
     * @param init the initialization model
     * @param features the features
     * @param maxNbComp the maximum number of components
     *
     * @return the GMM
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    protected static GMM globalAndUniformInit(Iterator<Segment> itSeg, GMM init, FeatureSet features, int maxNbComp) throws DiarizationException, IOException {
        GMM gmm = (GMM) (init.clone());
        Gaussian g = gmm.getComponent(0);
        int nbComp = maxNbComp;
        int len = 1;
        double weight = 1.0 / nbComp;

        for (int i = 1; i < nbComp; i++) {
            gmm.addComponent(g);
        }

        int nb = 0;
        ArrayList<Integer> pFreatures = new ArrayList<Integer>();
        ArrayList<String> idxShow = new ArrayList<String>();
        while (itSeg.hasNext()) {
            Segment segment = itSeg.next();
            nb += segment.getLength();
            int s = segment.getStart();
            int e = s + segment.getLength();
            // int idx = seg.getShowIndex();
            for (int i = s; i < e; i++) {
                pFreatures.add(i);
                idxShow.add(segment.getShowName());
            }
        }
        int step = nb / nbComp;
        //int step = nb / nbComp;
        //System.err.println("step:"+step);
        int dim = features.getDim();
        gmm.getComponent(0).setWeight(weight);
        gmm.getComponent(0).setGLR();
        for (int i = 1; i < nbComp; i++) {
            for (int k = 0; k < dim; k++) {
                double s = 0.0;
                for (int j = 0; j < len; j++) {
                    int idx = pFreatures.get(i * step + j);
                    features.setCurrentShow(idxShow.get(i * step + j));
                    s += features.getFeature(idx)[k];
                }
                double v = s / len;
                gmm.getComponent(i).setMean(k, v);
            }
            gmm.getComponent(i).setWeight(weight);
            gmm.getComponent(i).setGLR();
        }
        return gmm;
    }
}
