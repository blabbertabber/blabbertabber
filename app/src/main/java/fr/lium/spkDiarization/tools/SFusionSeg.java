/**
 * <p>
 * SFusionSeg
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

import java.util.ArrayList;
import java.util.TreeMap;

import fr.lium.spkDiarization.lib.DiarizationError;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.Parameter;

public class SFusionSeg {


    /*
     * public static void merge(ClusterSet clusters1, ClusterSet clusters2, int[][] pair, ClusterSet res2) { ArrayList<Segment> list = new ArrayList<Segment>();
     * ClusterSet res = new ClusterSet(); for (int i = 0; i < pair.length; i++) { if (pair[i][0] >= 0) { Cluster cluster1 = clusters1.getCluster(pair[i][0]);
     * Iterator<Segment> it1 = cluster1.iteratorSegments(); String nameCluster1 = cluster1.getName(); if (pair[i][1] >= 0) { Cluster cluster2 =
     * clusters2.getCluster(pair[i][1]); String nameCluster2 = cluster2.getName(); System.out.println("(" + nameCluster1 + ", " + nameCluster2 + ")");
     * TreeMap<Integer, Segment> tree2 = cluster2.clusterToFrames(); while (it1.hasNext()) { Segment seg1 = it1.next(); int start = seg1.getStart(); for (int j =
     * start; j < start + seg1.getLength(); j++) { if (tree2.containsKey(j)) { Segment seg2 = tree2.get(j); seg2.setInformation("nameCluster2", nameCluster2);
     * list.add(seg2); } else { Segment seg = (Segment) seg1.clone(); String name = "new_" + nameCluster1 + "_" + nameCluster2; // System.err.println(j + " : " +
     * name); seg.setStart(j); seg.setLength(1); seg.setInformation("nameCluster1", nameCluster1); seg.setInformation("nameCluster2", nameCluster2); list.add(seg);
     * } } } } } } ArrayList<Segment> segmentList = new ArrayList<Segment>(); //ClusterSet.vectorToClusterSet(list, res); res.collapse(); //Iterator<Cluster>
     * itClusters = res.clusterSetValueIterator(); while (itClusters.hasNext()) { Cluster cluster = itClusters.next(); Iterator<Segment> itSeg =
     * cluster.getSegments().iterator(); while (itSeg.hasNext()) { Segment seg = itSeg.next(); if (seg.getLength() < 100) {
     * seg.setClusterName(seg.getInformation("nameCluster2")); } else { seg.setClusterName(cluster.getName()); } segmentList.add(seg); } }
     * ClusterSet.vectorToClusterSet(segmentList, res2); } public static boolean findPair(int[][] pair, int idx1, int idx2) { for (int i = 0; i < pair.length; i++)
     * { if ((pair[i][0] == idx1) && (pair[i][1] == idx2)) { return true; } } return false; }
     */
    public static ClusterSet merge2(ClusterSet clusters1, ClusterSet clusters2, ArrayList<String> pairList) throws DiarizationException {
        ClusterSet res = new ClusterSet();

        TreeMap<Integer, Segment> listClusters1 = clusters1.toFrames();
        TreeMap<Integer, Segment> listClusters2 = clusters2.toFrames();

        for (int i : listClusters1.keySet()) {
            Segment segment1 = listClusters1.get(i);
            Segment segment2 = listClusters2.get(i);
            Segment segment = (Segment) listClusters1.get(i).clone();
            String name1 = segment1.getClusterName();
            String name2 = segment2.getClusterName();
            String name = name1 + ":" + name2;
            if (pairList.contains(name)) {
                name = name1;
            }
            Cluster cluster = null;
            if (res.containsCluster(name) == true) {
                cluster = res.getCluster(name);
            } else {
                cluster = res.createANewCluster(name);
            }
            cluster.addSegment(segment);
        }
        return res;
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            if (param.nbShow > 0) { // clusters ClusterSet clusters1 =
                MainTools.readClusterSet(param);

                ClusterSet clusters1 = MainTools.readClusterSet(param);
                ClusterSet clusters2 = MainTools.readTheSecondClusterSet(param);

                ArrayList<String> pairList = DiarizationError.listOfMatchedSpeakers(clusters1, clusters2);

                ClusterSet res = merge2(clusters1, clusters2, pairList);
                res.collapse();
                MainTools.writeClusterSet(param, res, false);
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

            param.parameterSegmentationInputFile.print();
            param.parameterSegmentationInputFile2.print();
            param.parameterSegmentationOutputFile.print();
        }
    }

}
