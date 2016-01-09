/**
 * <p>
 * MClust
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
 * Hierarchical and linear clustering program based on CLR and BIC distances
 */

package fr.lium.spkDiarization.programs;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import edu.thesis.xml.transform.TransformerException;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SquareMatrix;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringMethod.BICHClustering;
import fr.lium.spkDiarization.libClusteringMethod.BICLClustering;
import fr.lium.spkDiarization.libClusteringMethod.CLRHClustering;
import fr.lium.spkDiarization.libClusteringMethod.HClustering;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterClustering;
import fr.lium.spkDiarization.parameter.ParameterClustering.ClusteringMethod;
import fr.lium.spkDiarization.parameter.ParameterModelSetOutputFile;

public class MClust {

    /**
     * save a step of the hierarchical clustering algorithm, clustering is duplicated form prevSuffix to suffix.
     *
     * @param clustering the class of the hierarchical clustering
     * @param prevSuffix save starting
     * @param suffix save ending
     * @param param root parameter class
     * @param idxMerge the index merge
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws DiarizationException the diarization exception
     * @throws TransformerException the transformer exception
     */
    public static void saveClustering(HClustering clustering, long prevSuffix, long suffix, int idxMerge, Parameter param) throws IOException,
            ParserConfigurationException, SAXException, DiarizationException, TransformerException {
        if (param.parameterDiarization.isSaveAllStep()) {
            for (long i = prevSuffix; i < suffix; i++) {
                String segOutFilename = param.show + "." + String.format("%3d", idxMerge).replace(" ", "_") + "_" + String.valueOf(i);
                clustering.getClusterSet().write(segOutFilename, param.parameterSegmentationOutputFile);
            }
        }
    }

    /**
     * Bootum-up Hierarchical clustering based on GMMs, metric could be CE (Cross Entropy) or CLR (Cross Likelihood ratio)
     *
     * @return Clusters
     * @throws DiarizationException
     * @throws IOException
     * @throws TransformerException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static ClusterSet cclust(ClusterSet clusters, FeatureSet features, Parameter param, GMM ubm) throws IOException, DiarizationException,
            ParserConfigurationException, SAXException, TransformerException {

        CLRHClustering clustering = new CLRHClustering((ClusterSet) clusters.clone(), features, param, ubm);
        int nbCluster = clusters.clusterGetSize();

        int nbMerge = 0;
        double clustThr = param.parameterClustering.getThreshold();
        int nbMaxMerge = param.parameterClustering.getMaximumOfMerge();
        int nbMinClust = param.parameterClustering.getMinimumOfCluster();
        long suffix = -300;
        int mult = 1;
        clustering.init(0, 0); // Ci = 0; Cj = 0;
        if (param.trace) {
            printScore(clustering, param);
            saveClustering(clustering, suffix, suffix + 1, nbMerge, param);
        }
        long prevSuffix = suffix;

        double score = clustering.getScoreOfCandidatesForMerging();
        while (stopCriterion(score, nbMerge, nbCluster, clusters, clustThr, nbMaxMerge, nbMinClust, param.trace) == true) {
            nbMerge++;
            if (param.trace) {
                System.out.print("trace[cclust] \t score = " + score);
                System.out.print(" ci = " + clustering.getIndexOfFirstCandidate() + "(" + clustering.getFirstCandidate().getName() + ")");
                System.out.println(" cj = " + clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")");
                suffix = Math.round(score * mult);
                if (suffix > prevSuffix) {
                    saveClustering(clustering, prevSuffix, suffix, nbMerge, param);
                    prevSuffix = suffix;
                }
            }
            clustering.mergeCandidates();
            score = clustering.getScoreOfCandidatesForMerging();
            nbCluster = clustering.getClusterSet().clusterGetSize();
        }
        if (!param.parameterModelSetOutputFile.getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
            MainTools.writeGMMContainer(param, clustering.getModels());
        }
        if (param.trace) {
            suffix = Math.round(clustThr * mult);
            saveClustering(clustering, prevSuffix, suffix, nbMerge, param);
        }
        return clustering.getClusterSet();
    }

    /**
     * BIC Hierarchical clustering
     *
     * @return Clusters
     * @throws IOException
     * @throws DiarizationException
     */
    public static ClusterSet hclust(ClusterSet clusters, FeatureSet features, Parameter param) throws DiarizationException, IOException {
        BICHClustering clustering = new BICHClustering((ClusterSet) clusters.clone(), features, param);
        int nbMerge = 0;
        int clustMaxMerge = param.parameterClustering.getMaximumOfMerge();
        int clustMinSpk = param.parameterClustering.getMinimumOfCluster();
        int nbCluster = clusters.clusterGetSize();

        double score = 0;
        clustering.init(0, 0); // Ci = 0; Cj = 0;
        score = clustering.getScoreOfCandidatesForMerging();
        // Double GD = clustering.getGDOfScoreOfClustering();

        while (stopCriterion(score, nbMerge, nbCluster, clusters, 0.0, clustMaxMerge, clustMinSpk, param.trace) == true) {
            if (param.trace) {
                System.out.print("trace[hclust] \t merge = " + nbMerge);
                System.out.print(" score = " + score);
                System.out.print(" ci = " + clustering.getIndexOfFirstCandidate() + "(" + clustering.getFirstCandidate().getName() + ")");
                System.out.println(" cj = " + clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")");
                // System.out.println(" GD="+GD);
            }
            clustering.mergeCandidates();
            score = clustering.getScoreOfCandidatesForMerging();
            /*
			 * Double newGD = clustering.getGDOfScoreOfClustering(); if (newGD > GD) { break; } GD = newGD;
			 */
            nbMerge++;
            nbCluster = clustering.getClusterSet().clusterGetSize();
        }
        if (!param.parameterModelSetOutputFile.getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
            MainTools.writeGMMContainer(param, clustering.getModels());
        }
        return clustering.getClusterSet();
    }

    /**
     * BIC linear left to right clustering
     *
     * @return Clusters
     * @throws IOException
     * @throws DiarizationException
     */
    public static ClusterSet lclust(ClusterSet clusters, FeatureSet features, Parameter param) throws DiarizationException, IOException {
        BICLClustering clustering = new BICLClustering((ClusterSet) clusters.clone(), features, param);
        double score = 0;
        clustering.init(0, 1); // Ci = 0; Cj = 1;
        score = clustering.getScoreOfCandidatesForMerging();
        while (score < Double.MAX_VALUE) {
            if (param.trace) {
                System.out.print("trace[lclust] \t score = " + score);
                System.out.print(" ci = " + clustering.getIndexOfFirstCandidate() + "(" + clustering.getFirstCandidate().getName() + ")");
                System.out.println(" cj = " + clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")");
            }
            if (score < 0.0) {
                // System.out.println("trace[lclust] \t merge");
                clustering.mergeCandidates();
            } else {
                clustering.incrementIndexOfFirstCandidate();
                clustering.incrementIndexOfSecondCandidate();
            }
            score = clustering.getScoreOfCandidatesForMerging();
        }
        if (!param.parameterModelSetOutputFile.getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
            MainTools.writeGMMContainer(param, clustering.getModels());
        }
        // clustering.getClusterSet().debug();
        return clustering.getClusterSet();
    }

    /**
     * BIC linear right to left clustering
     *
     * @return Clusters
     * @throws IOException
     * @throws DiarizationException
     */
    public static ClusterSet rclust(ClusterSet clusters, FeatureSet features, Parameter param) throws DiarizationException, IOException {
        BICLClustering clustering = new BICLClustering((ClusterSet) clusters.clone(), features, param);
        double score = 0;
        int lastIndex = clustering.getIndexOfLastCandidate();
        System.err.println("lastIndex:" + lastIndex);
        clustering.init(lastIndex - 1, lastIndex);
        score = clustering.getScoreOfCandidatesForMerging();
        while (score < Double.MAX_VALUE) {
            if (param.trace) {
                System.out.print("trace[lclust] \t score = " + score);
                System.out.print(" ci = " + clustering.getIndexOfFirstCandidate() + "(" + clustering.getFirstCandidate().getName() + ")");
                System.out.println(" cj = " + clustering.getIndexOfSecondCandidate() + "(" + clustering.getSecondCandidate().getName() + ")");
            }
            if (score < 0.0) {
// System.out.println("trace[lclust] \t merge");
                clustering.mergeCandidates();
// } else {
// clustering.decrementIndexOfFirstCandidate();
// clustering.decrementIndexOfSecondCandidate();
            }
            clustering.decrementIndexOfFirstCandidate();
            clustering.decrementIndexOfSecondCandidate();
/*
 * System.out.print("after operation \t score = " + score); System.out.print(" ci = " + clustering.getIndexOfFirstCandidate() + "(" +
 * clustering.getFirstCandidate().getName() + ")"); System.out.println(" cj = " + clustering.getIndexOfSecondCandidate() + "(" +
 * clustering.getSecondCandidate().getName() + ")");
 */
            score = clustering.getScoreOfCandidatesForMerging();
			/*
			 * System.out.println(" score = "+score); System.out.println(" --------------------");
			 */
        }
        if (!param.parameterModelSetOutputFile.getMask().equals(ParameterModelSetOutputFile.getDefaultMask())) {
            MainTools.writeGMMContainer(param, clustering.getModels());
        }
        // clustering.getClusterSet().debug();
        return clustering.getClusterSet();
    }

    public static ClusterSet make(FeatureSet features, ClusterSet clusters, Parameter param, GMM ubm) throws Exception {
        ClusterSet clustersRes = new ClusterSet();
        if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_BIC)) {
            clustersRes = MClust.hclust(clusters, features, param);
        } else if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_ICR)) {
            clustersRes = MClust.hclust(clusters, features, param);
        } else if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_GLR)) {
            clustersRes = MClust.hclust(clusters, features, param);
        } else if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_GD)) {
            clustersRes = MClust.hclust(clusters, features, param);
        } else if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_L_BIC)) {
            clustersRes = MClust.lclust(clusters, features, param);
        } else if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_R_BIC)) {
            clustersRes = MClust.rclust(clusters, features, param);
        } else if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_TScore)) {
            clustersRes = MClust.cclust(clusters, features, param, ubm);
        } else if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_CLR)) {
            clustersRes = MClust.cclust(clusters, features, param, ubm);
        } else if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_CE)) {
            clustersRes = MClust.cclust(clusters, features, param, ubm);
        } else if (param.parameterClustering.getMethod().equals(ParameterClustering.ClusteringMethod.CLUST_H_GDGMM)) {
            clustersRes = MClust.cclust(clusters, features, param, ubm);
        } else {
            System.out.println("Error \t not implemented method");
            System.exit(-1);
        }
        return clustersRes;
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MClust");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);

                // methods
                ArrayList<GMM> vect = MainTools.readGMMContainer(param);
                GMM ubm = null;
                if (vect != null) {
                    ubm = vect.get(0);
                }
                ClusterSet clustersRes = make(features, clusters, param, ubm);

                MainTools.writeClusterSet(param, clustersRes, false);

            }
        } catch (DiarizationException e) {
            System.out.println("error \t exception " + e.getMessage());
        }
    }

    public static void printScore(CLRHClustering clustering, Parameter param) {
        SquareMatrix distances = clustering.getDistances(); // Matrix of distances.
        ArrayList<GMM> models = clustering.getModels(); // List of models
        int size = distances.getDimension();
        for (int i = 0; i < size; i++) {
            String spk1 = models.get(i).getName();
            for (int j = i + 1; j < size; j++) {
                String spk2 = models.get(j).getName();
                double score = distances.get(i, j);
                System.out.println("trace[cclust distance] \t  ( " + spk1 + " , " + spk2 + " ) = " + score);
            }
        }

    }

    public static boolean stopCriterion(double score, int nbMerge, int nbCluster, ClusterSet clusters, double clustThr, int nbMaxMerge, int nbMinCluster,
                                        boolean trace) {

        if (score == Double.MAX_VALUE) {
            return false;
        }
        boolean res = ((score < clustThr) && (nbMerge < nbMaxMerge) && (nbCluster > nbMinCluster));
        if (trace == true) {
            System.out.println("trace[clust:stopCriterion] \t result = " + res + " true=" + Boolean.TRUE);
            System.out.println("trace[clust:stopCriterion] \t Y|N thr = " + (score < clustThr));
            System.out.println("trace[clust:stopCriterion] \t Y|N nb merge = " + (nbMerge < nbMaxMerge));
            System.out.println("trace[clust:stopCriterion] \t Y|N nb cluster = " + (nbCluster > nbMinCluster));
            System.out.println("trace[clust:stopCriterion] \t score = " + score + " nbMerge=" + nbMerge + " nbCluster=" + nbCluster);
            System.out.println("trace[clust:stopCriterion] \t thr=" + clustThr + " nbMaxMerge=" + nbMaxMerge + " nbMinSpk=" + nbMinCluster);
        }
        return res;
    }

    public static void info(Parameter param, String prog) {
        if (param.help) {
            param.printSeparator2();
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterInputFeature.print();
            param.printSeparator();
            param.parameterSegmentationInputFile.print();
            param.parameterSegmentationOutputFile.print();
            param.printSeparator();
            param.parameterDiarization.printSaveAllStep();
            param.printSeparator();
            param.parameterClustering.printMethod(); // cMethod
            param.parameterClustering.printThreshold(); // cThr
            param.parameterClustering.printMaximumOfMerge(); // cMaxMerge
            param.parameterClustering.printMinmumOfCluster(); // cMinSpk
            param.parameterClustering.printMinimumOfClusterLength(); // cMinLen
            param.printSeparator();
            param.parameterModelSetOutputFile.printMask(); // model
            // output
            param.printSeparator();
            if (param.parameterClustering.getMethod().equals(ClusteringMethod.CLUST_H_CLR)
                    || param.parameterClustering.getMethod().equals(ClusteringMethod.CLUST_H_CE)) {
                param.parameterModelSetInputFile.printMask(); // tInMask
                param.parameterTopGaussian.printTopGaussian(); // sTop
                param.parameterEM.print(); // emCtrl
                param.parameterMAP.print(); // mapCtrl
                param.parameterVarianceControl.printVarianceControl(); // varCtrl
            } else {
                param.parameterModel.printKind(); // kind
                param.parameterModel.printNumberOfComponents(); // nbComp
            }
        }
    }

}