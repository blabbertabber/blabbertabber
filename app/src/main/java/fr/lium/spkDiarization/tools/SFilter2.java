/**
 * <p>
 * SFilter2
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

import java.util.Iterator;
import java.util.TreeMap;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.Parameter;

public class SFilter2 {

    public static void addFrame(int start, int len, TreeMap<Integer, Segment> mapSeg, ClusterSet res) {
        for (int i = start; i < (start + len); i++) {
            if (mapSeg.containsKey(i)) {
                String name = mapSeg.get(i).getClusterName();
                Cluster c = res.getOrCreateANewCluster(name);
                // A verifier rajout du getCluster()
                c.setGender(mapSeg.get(i).getCluster().getGender());
                c.addSegment(mapSeg.get(i));
            } else {
                System.out.println("warning[sFilter] \t frame not found=" + i);
            }
        }
    }

    public static ClusterSet filter(TreeMap<Integer, Segment> mapSeg, ClusterSet filterC, Parameter param) {
        ClusterSet res = new ClusterSet();
        int pad = param.parameterFilter.getSegmentPadding();
        String idxFliter = param.parameterSegmentationFilterFile.getClusterFilterName();
        Cluster cluster = filterC.getCluster(idxFliter);
        System.err.println("***********************");
        cluster.debug(0);
        System.err.println("***********************");
        Iterator<Segment> itSFilter = cluster.iterator();
        while (itSFilter.hasNext()) {
            Segment seg = itSFilter.next();
            int startFilter = seg.getStart();
            int lenFilter = seg.getLength();
            if (lenFilter > param.parameterFilter.getSilenceMinimumLength()) {
                System.err.println("eti = " + idxFliter + " start" + startFilter + " len" + lenFilter);
                for (int i = startFilter + pad; i < (startFilter + lenFilter - 2 * pad); i++) {
                    mapSeg.remove(i);
                }
            }
        }
        Iterator<Integer> itMap = mapSeg.keySet().iterator();
        while (itMap.hasNext()) {
            int idx = itMap.next();
            addFrame(idx, 1, mapSeg, res);
        }
        res.collapse();
        // res.debug();
        return res;
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
                fltClusters.collapse(5);
                TreeMap<Integer, Segment> mapSeg = clusters.toFrames();
                ClusterSet res = filter(mapSeg, fltClusters, param);

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

            param.parameterInputFeature.printMask(); // fInMask
            param.parameterInputFeature.printDescription(); // fDesc
            param.parameterSegmentationInputFile.printMask(); // sInMask
            param.parameterSegmentationInputFile.printEncodingFormat();
            param.parameterSegmentationFilterFile.printMask(); // sInFltMask
            param.parameterSegmentationFilterFile.printEncodingFormat();
            param.parameterSegmentationOutputFile.printMask(); // sOutMask
            param.parameterSegmentationOutputFile.printEncodingFormat();
            param.printSeparator();
            param.parameterSegmentationFilterFile.printFilterClusterName();
            param.printSeparator();
            param.parameterFilter.printSilenceMinimumLength();
            param.parameterFilter.printSpeechMinimumLength();
            param.parameterFilter.printSegmentPadding();
        }
    }
}
