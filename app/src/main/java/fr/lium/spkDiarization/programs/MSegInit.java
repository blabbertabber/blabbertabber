/**
 * <p>
 * MSegInit
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
 * Initial segmentation program
 */

package fr.lium.spkDiarization.programs;

import java.io.IOException;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.Gaussian;
import fr.lium.spkDiarization.parameter.Parameter;

public class MSegInit {

    public static void detectEqualFeatures(FeatureSet features, ClusterSet clusters, ClusterSet clustersResult, Parameter param) throws DiarizationException, IOException {
        //String initialClusterName = new String("S0");
        //Cluster resultCluster = clustersResult.createANewCluster(initialClusterName);

        for (Cluster cluster : clusters.clusterSetValue()) {
            Cluster resultCluster = clustersResult.getOrCreateANewCluster(cluster.getName());
            for (Segment segment : cluster) {
                features.setCurrentShow(segment.getShowName());
                int startSegment = segment.getStart();
                int endTmp = startSegment + segment.getLength();
                int endSegment = Math.min(endTmp, features.getNumberOfFeatures());
                if (param.trace) {
                    System.out.println("trace[mSegInit] \t check segment : " + startSegment + " " + endSegment);
                }
                if (endTmp > endSegment) {
                    System.out.println("WARNING[mSegInit] \t segment end after features end");
                }
                boolean equal = false;
                for (int i = startSegment + 1; i < endSegment; i++) {
                    if (features.compareFrames(i - 1, i)) {
                        System.out.println("WARNING[mSegInit] \t two consecutive features are the same, index = " + i);
                        equal = true;
                    } else {
                        if (equal == true) {
                            equal = false;
                        } else {
                            Segment oneFrameSegment;
                            if (i == (startSegment + 1)) {
                                oneFrameSegment = new Segment(segment.getShowName(), i - 1, 1, resultCluster);
                                resultCluster.addSegment(oneFrameSegment);
                            }
                            oneFrameSegment = new Segment(segment.getShowName(), i, 1, resultCluster);
                            resultCluster.addSegment(oneFrameSegment);
                        }
                    }

                }
            }
        }
    }

    public static void detectLikelihoodProblem(FeatureSet features, ClusterSet clusters, ClusterSet clustersResult, Parameter param) throws DiarizationException, IOException {
        //String initialClusterName = new String("S0");
        //Cluster resultCluster = clustersResult.createANewCluster(initialClusterName);

        int dim = features.getDim();
        GMM gmm = new GMM(1, dim, Gaussian.DIAG);
        Gaussian g = gmm.getComponent(0);

		/*ClusterSet clusterByFile = new ClusterSet();
		for(Cluster cluster : clusters.clusterSetValue()) {
			for(Segment segment : cluster) {
				String show = segment.getShowName();
				Cluster fileCluster = clusterByFile.getCluster(show);
				if (fileCluster == null) {
					fileCluster = clusterByFile.createANewCluster(show);
				}
				fileCluster.addSegment(segment);
			}
		}*/


        for (Cluster cluster : clusters.clusterSetValue()) {
            Cluster resultCluster = clustersResult.getOrCreateANewCluster(cluster.getName());
            g.initStatisticAccumulator();
            g.addFeaturesFromSegments(cluster.iterator(), features);
            g.setModelFromAccululator();

            for (Segment segment : cluster) {
                features.setCurrentShow(segment.getShowName());
                int startSegment = segment.getStart();
                int endTmp = startSegment + segment.getLength();
                int endSegment = Math.min(endTmp, features.getNumberOfFeatures());
                if (param.trace) {
                    System.out.println("trace[mSegInit] \t check segment : " + startSegment + " " + endSegment);
                }
                if (endTmp > endSegment) {
                    System.out.println("WARNING[mSegInit] \t segment end after features end");
                }
                for (int i = startSegment; i < endSegment; i++) {
                    double score = g.getAndAccumulateLikelihood(features, i);
                    if (score == Double.MIN_VALUE) {
                        System.out.println("WARNING[mSegInit] \t freature get a tiny likelihood, remove feature index = " + i);

                    } else {
                        resultCluster.addSegment(new Segment(segment.getShowName(), i, 1, resultCluster));
                    }
                }
            }
        }
    }

    public static void make(FeatureSet features, ClusterSet clusters, ClusterSet clustersResult, Parameter param) throws DiarizationException, IOException {
        ClusterSet clustersResultTmp = new ClusterSet();
        detectEqualFeatures(features, clusters, clustersResultTmp, param);
        clustersResultTmp.collapse();
        detectLikelihoodProblem(features, clustersResultTmp, clustersResult, param);
    }

    public static void main(String[] args) throws DiarizationException, Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MSegInit");
            if (param.nbShow > 0) {
                ClusterSet clusters = null;
                Segment segment = null;
                if (param.parameterSegmentationInputFile.getMask().equals("")) {
                    clusters = new ClusterSet();
                    Cluster cluster = clusters.createANewCluster("init");
                    segment = new Segment(param.show, 0, Integer.MAX_VALUE, cluster);
                    cluster.addSegment(segment);

                } else {
                    // clusters
                    clusters = MainTools.readClusterSet(param);
                }

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);
                if (param.parameterSegmentationInputFile.getMask().equals("")) {
                    features.setCurrentShow(segment.getShowName());
                    segment.setLength(features.getNumberOfFeatures());
                }

                ClusterSet res = new ClusterSet();

                make(features, clusters, res, param);

                MainTools.writeClusterSet(param, res, true);
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
            param.printSeparator();
        }
    }

}