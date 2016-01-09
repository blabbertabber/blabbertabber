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
 * Merge the labels of two segmentation at the frame level
 */

import java.util.TreeMap;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.Parameter;

public class SMergeSeg {
    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "sMerge");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);
                ClusterSet clusters2 = MainTools.readTheSecondClusterSet(param);

                TreeMap<Integer, Segment> mapSeg = clusters.toFrames();
                TreeMap<Integer, Segment> mapSeg2 = clusters2.toFrames();
                ClusterSet res = new ClusterSet();

                int size = Math.max(clusters.getLength(), clusters2.getLength());

                for (int i = 0; i < size; i++) {

                    Segment newSeg = null;
                    String newName = "empty";

                    if (mapSeg.containsKey(i) && mapSeg2.containsKey(i)) {
                        Segment segMap = mapSeg.get(i);
                        Segment segMap2 = mapSeg2.get(i);
                        newName = segMap.getCluster().getName();
                        newName += ":";
                        newName += segMap2.getCluster().getName();
                        newSeg = (Segment) segMap.clone();
                    } else {
                        if (mapSeg.containsKey(i)) {
                            Segment segMap = mapSeg.get(i);
                            newName = segMap.getCluster().getName();
                            newName += ":UNK";
                            newSeg = (Segment) segMap.clone();
                        } else {
                            if (mapSeg2.containsKey(i)) {
                                Segment segMap2 = mapSeg2.get(i);
                                newName = "UNK:";
                                newName += segMap2.getCluster().getName();
                                newSeg = (Segment) segMap2.clone();
                            }
                        }
                    }
// System.err.println(i+"="+newName);

                    Cluster cluster = null;
                    if (res.containsCluster(newName)) {
                        cluster = res.getCluster(newName);
                    } else {
                        cluster = res.createANewCluster(newName);
                    }
                    if (newSeg != null) {
                        cluster.addSegment(newSeg);
                    }
                }

                MainTools.writeClusterSet(param, res, true);
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
