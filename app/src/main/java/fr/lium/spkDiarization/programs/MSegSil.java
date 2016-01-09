/**
 * <p>
 * MSegSil
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
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
 * Silence segmentation
 */

package fr.lium.spkDiarization.programs;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.parameter.Parameter;

public class MSegSil {

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MSegSil");
            if (!param.parameterInputFeature.getFeaturesDescription().getEnergyPresence()) {
                System.out.println("trace[mSegsil] \t Energy is not available in features");
                System.exit(-1);
            }
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);

                int staticDimension = features.getStaticDimension();

                // clusters result
                ClusterSet clustersResult = (ClusterSet) (clusters.clone());

                // compute mean and std
                for (Cluster cluster : clusters.clusterSetValue()) {
                    double thr = Distance.getEnergyThreshold(cluster, features, param.parameterSegmentation.getSilenceThreshold());
                    Cluster clusterResult = clustersResult.getCluster(cluster.getName());
                    clusterResult.clearSegments();

                    if ((param.trace) && (param.parameterSegmentation.getSilenceThreshold() > 0.0)) {
                        System.out.println("trace[mSegSil] \t cluster : " + cluster.getName() + " thr = " + thr);
                    }
                    // remove frames that have energy under the thershold
                    for (Segment segTmp : cluster) {
                        int start = segTmp.getStart();
                        int endSegment = start + segTmp.getLength();
                        int end = Math.min(endSegment, features.getNumberOfFeatures());
                        features.setCurrentShow(segTmp.getShowName());
                        for (int i = start; i < end; i++) {
                            double value = features.getFeature(i)[staticDimension - 1];
                            if (value > thr) {
                                Segment seg = (Segment) (segTmp.clone());
                                seg.setStart(i);
                                seg.setLength(1);
                                clusterResult.addSegment(seg);
                            }
                        }
                    }
                }
                MainTools.writeClusterSet(param, clustersResult, true);
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

            param.parameterInputFeature.printMask(); // fInMask
            param.parameterInputFeature.printDescription(); // fDesc
            param.printSeparator();
            param.parameterSegmentationInputFile.printMask(); // sInMask
            param.parameterSegmentationInputFile.printEncodingFormat();
            param.parameterSegmentationOutputFile.printMask(); // sOutMask
            param.parameterSegmentationOutputFile.printEncodingFormat();
            param.parameterSegmentation.printSilenceThreshold(); // ssThr
            param.printSeparator();
        }
    }

}
