/**
 * <p>
 * SConcatFeatureSet
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
 * Concatenation feature set describe in a segmentation file into a new feature set.
 */

package fr.lium.spkDiarization.tools;

import java.util.Iterator;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

public class SConcatFeatureSet {

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "SConcatFeatureSet");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = null;
                if (param.parameterSegmentationInputFile.getMask().equals("")) {
                    clusters = new ClusterSet();
                    Cluster cluster = clusters.createANewCluster("init");
                    Segment segment = new Segment(param.show, 0, Integer.MAX_VALUE, cluster);
                    cluster.addSegment(segment);

                } else {
                    // clusters
                    clusters = MainTools.readClusterSet(param);
                    clusters.collapse();
                }

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);

                FeatureSet featuresResult = new FeatureSet(0, param.parameterOutputFeature.getFeaturesDescription());
                int resIdx = 0;
                ClusterSet clustersResult = new ClusterSet();
                Iterator<Cluster> itCluster = clusters.clusterSetValueIterator();
                while (itCluster.hasNext()) {
                    Cluster cluster = itCluster.next();
                    Cluster clusterResult = clustersResult.getOrCreateANewCluster(cluster.getName());

                    Iterator<Segment> itSeg = cluster.iterator();
                    while (itSeg.hasNext()) {
                        Segment segment = itSeg.next();
                        features.setCurrentShow(segment.getShowName());
                        int start = segment.getStart();
                        int endSegment = start + segment.getLength();
                        int end = Math.min(endSegment, features.getNumberOfFeatures());
                        Segment segmentResult = new Segment(param.show, resIdx, segment.getLength(), cluster);
                        clusterResult.addSegment(segmentResult);
                        for (int i = start; i < end; i++, resIdx++) {
                            featuresResult.addFrame(features.getFeature(i));
                        }
                    }
                }

                if (param.trace) {
                    System.out.println("trace[mConcatPrm] \t save");
                    featuresResult.debug(3);
                }
                MainTools.writeFeatureSet(param, featuresResult);

                MainTools.writeClusterSet(param, clustersResult, false);
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
            param.parameterOutputFeature.print();
            param.printSeparator();
            param.parameterSegmentationInputFile.printMask(); // sInMask
            param.parameterSegmentationInputFile.printEncodingFormat();

        }
    }

}
