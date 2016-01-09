/**
 * <p>
 * SFilter
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
 * filter a segmentation file by another one
 */

package fr.lium.spkDiarization.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.Parameter;

public class SFilter {
    static boolean trace = false;

    public static void addFrame(int start, int len, TreeMap<Integer, Segment> mapSeg, ClusterSet res) {
        for (int i = start; i < (start + len); i++) {
            if (mapSeg.containsKey(i)) {
                String name = mapSeg.get(i).getClusterName();
                Cluster c = res.getOrCreateANewCluster(name);
                // A verifier rajout du getCluster()
                c.setGender(mapSeg.get(i).getCluster().getGender());
                c.addSegment(mapSeg.get(i));
            } else {
                if (i < mapSeg.lastKey()) {
                    if (trace) System.err.println("warning[sFilter] \t frame not found=" + i);
                }
            }
        }
    }

    public static ClusterSet filter(TreeMap<Integer, Segment> mapSeg, ClusterSet clusterSetFilter, Parameter param) {
        ClusterSet clusterResult = new ClusterSet();
        String listOfFilter = "," + param.parameterSegmentationFilterFile.getClusterFilterName() + ",";
        int pad = param.parameterFilter.getSegmentPadding();
        Iterator<Cluster> itClusterFilter = clusterSetFilter.clusterSetValueIterator();
        while (itClusterFilter.hasNext()) {
            Cluster clusterFilter = itClusterFilter.next();
            String eti = clusterFilter.getName();
            Iterator<Segment> itSegmentFilter = clusterFilter.iterator();
            while (itSegmentFilter.hasNext()) {
                Segment segmentFilter = itSegmentFilter.next();
                int startFilter = segmentFilter.getStart();
                int lenFilter = segmentFilter.getLength();
                if (listOfFilter.contains("," + eti + ",")) {
                    if (lenFilter < param.parameterFilter.getSilenceMinimumLength()) {
                        addFrame(startFilter, lenFilter, mapSeg, clusterResult);
                    } else {
                        if (pad > 0) {
                            addFrame(startFilter, pad, mapSeg, clusterResult);
                            int s2 = startFilter + lenFilter - pad;
                            addFrame(s2, pad, mapSeg, clusterResult);
                        }
                    }
                } else {
                    addFrame(startFilter, lenFilter, mapSeg, clusterResult);
                }
            }
        }
        clusterResult.collapse();
        return clusterResult;
    }

    public static ClusterSet make(ClusterSet clusters, ClusterSet fltClusters, Parameter param) throws DiarizationException {
        trace = param.trace;
        // --- remove non speech segment
        TreeMap<Integer, Segment> mapSeg = clusters.toFrames();
        ClusterSet clustersResult = filter(mapSeg, fltClusters, param);

        // --- rename small speech segment ---
        ArrayList<Segment> vect = clustersResult.getSegmentVectorRepresentation();
        SFilter.removeSmall(vect, param);

        ClusterSet clustersResult2 = new ClusterSet();
        clustersResult2.addVector(vect);
        return clustersResult2;
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "SFilter");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);

                ClusterSet fltClusters = new ClusterSet();
                fltClusters.read(param.showLst, param.parameterSegmentationFilterFile);

                ClusterSet res = make(clusters, fltClusters, param);

                MainTools.writeClusterSet(param, res, true);
            }
        } catch (DiarizationException e) {
            System.err.println("Error \t exception " + e.getMessage());
        }
    }

    public static void removeSmall(ArrayList<Segment> segmentList, Parameter param) {
        int previous = -1;
        int currant = 0;
        int next = 1;
        if (trace)
            System.err.println("param.segMinLenSpeech=" + param.parameterFilter.getSpeechMinimumLength());

        int size = segmentList.size();
        while (currant < size) {
            if (segmentList.get(currant).getLength() > param.parameterFilter.getSpeechMinimumLength()) {
                previous++;
                currant++;
                next++;
            } else {
                int delayPrevious = 10;
                int delayNext = 10;
                if (previous >= 0) {
                    delayPrevious = segmentList.get(currant).getStart() - (segmentList.get(previous).getStart() + segmentList.get(previous).getLength());
                }
                if (next < size) {
                    delayNext = segmentList.get(next).getStart() - (segmentList.get(currant).getStart() + segmentList.get(currant).getLength());
                }
                if ((delayPrevious <= 0) && (delayNext <= 0)) {
                    if (segmentList.get(previous).getLength() < segmentList.get(next).getLength()) {
                        segmentList.get(previous).setLength(segmentList.get(previous).getLength() + segmentList.get(currant).getLength());
                        segmentList.remove(currant);
                    } else {
                        segmentList.get(currant).setLength(segmentList.get(next).getLength() + segmentList.get(currant).getLength());
                        segmentList.get(currant).setCluster(segmentList.get(next).getCluster());
                        segmentList.remove(next);
                    }
                } else if (delayPrevious <= 0) {
                    segmentList.get(previous).setLength(segmentList.get(previous).getLength() + segmentList.get(currant).getLength());
                    segmentList.remove(currant);
                } else if (delayNext <= 0) {
                    segmentList.get(currant).setLength(segmentList.get(next).getLength() + segmentList.get(currant).getLength());
                    segmentList.get(currant).setCluster(segmentList.get(next).getCluster());
                    segmentList.remove(next);
                } else {
                    segmentList.remove(currant);
                }
            }
            size = segmentList.size();
        }
    }

    public static void info(Parameter param, String prog) {
        if (param.help) {
            param.printSeparator2();
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterSegmentationInputFile.print(); // sInMask
            param.parameterSegmentationFilterFile.print(); // sInFltMask
            param.parameterSegmentationOutputFile.print(); // sOutMask
            param.printSeparator();
            param.parameterSegmentationFilterFile.printFilterClusterName();
            param.printSeparator();
            param.parameterFilter.printSilenceMinimumLength();
            param.parameterFilter.printSpeechMinimumLength();
            param.parameterFilter.printSegmentPadding();
        }
    }

}
