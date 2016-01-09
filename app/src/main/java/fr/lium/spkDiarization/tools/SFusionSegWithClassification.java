/**
 * <p>
 * SFusionSegWithClassification
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
 * Concat GMM model file in a GMM Vector and save it
 */
package fr.lium.spkDiarization.tools;

import java.util.Iterator;
import java.util.TreeMap;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.Parameter;

public class SFusionSegWithClassification {

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "SFilter");
            if (param.nbShow > 0) {
                System.out.println("[SFusionSegWithClassification] \t info : use segFltInputMask for classification");
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);

                ClusterSet classificationClusters = new ClusterSet();
                classificationClusters.read(param.showLst, param.parameterSegmentationFilterFile);
                TreeMap<Integer, Segment> classificationMap = classificationClusters.toFrames();
                System.err.println("***********************");

                ClusterSet resultClusterSet = (ClusterSet) classificationClusters.clone();
                Iterator<Cluster> itResultCluster = resultClusterSet.clusterSetValueIterator();
                while (itResultCluster.hasNext()) {
                    Cluster cluster = itResultCluster.next();
                    cluster.clearSegments();
                }

                String nameUnk = new String("UNK");
                Iterator<Cluster> itCluster = clusters.clusterSetValueIterator();
                while (itCluster.hasNext()) {
                    Cluster cluster = itCluster.next();
                    Iterator<Segment> itSegment = cluster.iterator();
                    while (itSegment.hasNext()) {
                        Segment segment = itSegment.next();
                        TreeMap<String, Integer> count = new TreeMap<String, Integer>();
                        Iterator<Cluster> itCluster2 = classificationClusters.clusterSetValueIterator();
                        while (itCluster2.hasNext()) {
                            count.put(itCluster2.next().getName(), 0);
                        }

                        int unknown = 0;
                        for (int i = segment.getStart(); i <= segment.getLast(); i++) {
                            if (classificationMap.containsKey(i)) {
                                String name = classificationMap.get(i).getClusterName();
                                int val = count.get(name) + 1;
                                count.put(name, val);
                            } else {
                                unknown++;
                            }
                        }
                        String idxMax = "UNK";
                        Iterator<String> itCount = count.keySet().iterator();
                        while (itCount.hasNext()) {
                            if (count.get(itCount.next()) > count.get(idxMax)) {
                                idxMax = itCount.next();
                            }
                        }
                        if (count.get(idxMax) < unknown) {
                            System.out.println("[SFusionSegWithClassification] \t more unknow, get UNK ");
                            resultClusterSet.getOrCreateANewCluster(nameUnk);
                        }
                        // ajouter les segments
                        Segment newSegment = (Segment) segment.clone();
                        resultClusterSet.getCluster(idxMax).addSegment(newSegment);
                    }
                }

                // --- remove non speech segment

                MainTools.writeClusterSet(param, resultClusterSet, true);
            }
        } catch (DiarizationException e) {
            System.out.println("Error \t exception " + e.getMessage());
        }
    }

    public static void info(Parameter param, String prog) {
        if (param.help) {
            param.printSeparator2();
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterSegmentationInputFile.printMask(); // sInMask
            param.parameterSegmentationInputFile.printEncodingFormat();
            param.parameterSegmentationOutputFile.printMask(); // sOutMask
            param.parameterSegmentationOutputFile.printEncodingFormat();
        }
    }
}
