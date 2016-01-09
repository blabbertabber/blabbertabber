package fr.lium.spkDiarization.tools;

/**
 * <p>
 * SIterativeSegmentation
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
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
 * Iterative train speakers and segmentation the signal by Viterbi decoding
 */

import java.util.ArrayList;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterInitializationEM;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.programs.MTrainMAP;

public class SIterativeSegmentation {
    public static ClusterSet make(FeatureSet features, ClusterSet clusters, ClusterSet fltClusters, ArrayList<GMM> ubm, Parameter param) throws Exception {
        ClusterSet currentClusters = clusters;
        ClusterSet oldClusters = null;
        ArrayList<GMM> speakers = new ArrayList<GMM>(currentClusters.clusterGetSize());
        ArrayList<GMM> oldSpeakers = new ArrayList<GMM>(currentClusters.clusterGetSize());

        param.parameterModel.setKind("DIAG");
        param.parameterInitializationEM.setModelInitMethod(ParameterInitializationEM.TrainInitMethodString[3]);

        int i = 0;
        while ((oldClusters == null) || (oldClusters.equals(currentClusters) == false)) {
            if (param.trace) {
                System.err.println("-----------------------------------------------");
                System.err.println("iteration idx=" + i);
                System.err.println("-----------------------------------------------");

            }
            for (String name : currentClusters) {
                boolean compute = true;
                Cluster cluster = currentClusters.getCluster(name);
                if (cluster.getLength() > 50) {
                    if (oldClusters != null) {
                        Cluster oldCluster = oldClusters.getCluster(name);
                        if (oldCluster != null) {
                            if (oldCluster.equals(cluster)) {
                                compute = false;
                            }
                        }
                    }
                    if (compute == false) {
                        System.err.println("-----------------------------------------------");
                        System.err.println(" copy gmm :" + name);
                        for (GMM gmm : oldSpeakers) {
                            if (gmm.getName() == name) {
                                speakers.add(gmm);
                                break;
                            }
                        }
                    } else {
                        ArrayList<GMM> gmmInitVect = new ArrayList<GMM>(1);
                        ArrayList<GMM> speaker = new ArrayList<GMM>(1);
                        ClusterSet local = new ClusterSet();
                        local.putCluster(name, cluster);
                        MTrainInit.make(features, local, gmmInitVect, param);
                        MTrainMAP.make(features, local, gmmInitVect, speaker, param);
                        speakers.add(speaker.get(0));
                    }
                }
            }

            ClusterSet newClusters = MDecode.make(features, fltClusters, speakers, param);

            oldClusters = currentClusters;
            oldSpeakers = speakers;
            currentClusters = newClusters;
            i++;
        }

        return currentClusters;
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "SIterativeSegmentation");
            if (param.nbShow > 0) {
                // Clusters
                ClusterSet clusters = MainTools.readClusterSet(param);
// clusters.debug();
                ArrayList<String> toRemove = new ArrayList<String>();

                for (String name : clusters) {
                    Cluster cluster = clusters.getCluster(name);
                    int len = cluster.getLength();
                    if (len < 50) {
                        System.err.println("remove cluster : " + name + " len = " + len);
                        toRemove.add(name);
                    }
                }
                for (String name : toRemove) {
                    clusters.removeCluster(name);
                }

                ClusterSet fltClusters = new ClusterSet();
                fltClusters.read(param.showLst, param.parameterSegmentationFilterFile);

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);

                // Models
                ArrayList<GMM> gmmVect = MainTools.readGMMContainer(param);

                ClusterSet clustersRes = make(features, clusters, fltClusters, gmmVect, param);
                // Seg outPut
                MainTools.writeClusterSet(param, clustersRes, false);
            }
        } catch (DiarizationException e) {
            System.out.println("error \t exception " + e.getMessage());
        }
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
            param.parameterSegmentationFilterFile.print(); // sInFltMask
            param.parameterSegmentationOutputFile.print();
            param.printSeparator();
            param.parameterModelSetInputFile.printMask(); // tInMask
            param.parameterTopGaussian.printTopGaussian(); // sTop
            param.printSeparator();
            param.parameterEM.print(); // emCtl
            param.parameterMAP.print(); // mapCtrl
            param.parameterVarianceControl.printVarianceControl(); // varCtrl
            param.printSeparator();
            param.parameterDecoder.print();
        }
    }
}
