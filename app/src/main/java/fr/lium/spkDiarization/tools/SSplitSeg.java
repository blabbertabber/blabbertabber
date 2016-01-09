/**
 * <p>
 * SSplitSeg
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
 * Find silence and split a segmentation
 */

package fr.lium.spkDiarization.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.parameter.Parameter;

public class SSplitSeg {
    private static final String keyMid = "mid";
    static boolean trace = false;

    public static void findSil(FeatureSet features, ArrayList<GMM> gmmVect, int start, int end, int sizeWinE, ArrayList<Segment> vFlt)
            throws DiarizationException {

        vFlt.clear();
        int nbF = features.getNumberOfFeatures();
        String showName = features.getCurrentShowName();

        int nbGMM = gmmVect.size();
        ArrayList<Double> llk = new ArrayList<Double>(nbGMM);
        for (int i = 0; i < nbGMM; i++) {
            llk.add(0.0);
            gmmVect.get(i).initScoreAccumulator();
        }

        int idxMeanL = Math.max(start - sizeWinE, 0);
        int idxMeanR = Math.min(start + sizeWinE, nbF);
        int nb = 0;
        for (int i = idxMeanL; i <= idxMeanR; i++) {
            for (int j = 0; j < nbGMM; j++) {
                llk.set(j, llk.get(j) + gmmVect.get(j).getAndAccumulateLikelihood(features, i));
            }
            nb++;
        }
        double s = llk.get(0);
        for (int j = 1; j < nbGMM; j++) {
            if (llk.get(j) > s) {
                s = llk.get(j);
            }
        }

        idxMeanL = Math.max(start, 0);
        idxMeanR = Math.min(end, nbF);
        Cluster clusterSilence = new Cluster("SILENCE");
        Segment segment = new Segment(showName, idxMeanL, 1, clusterSilence);
        segment.setScore(s);
        vFlt.add(segment);

        for (int i = idxMeanL + 1; i < idxMeanR; i++) {
            if (i - sizeWinE - 1 >= 0) {
                for (int j = 0; j < nbGMM; j++) {
                    llk.set(j, llk.get(j) - gmmVect.get(j).getAndAccumulateLikelihood(features, i - sizeWinE - 1));
                }
                nb--;
            }
            if (i + sizeWinE < nbF) {
                for (int j = 0; j < nbGMM; j++) {
                    llk.set(j, llk.get(j) + gmmVect.get(j).getAndAccumulateLikelihood(features, i + sizeWinE));
                }
                nb++;
            }
            s = llk.get(0);
            for (int j = 1; j < nbGMM; j++) {
                if (llk.get(j) > s) {
                    s = llk.get(j);
                }
            }
            Segment segmentI = new Segment(showName, i, 1, clusterSilence);
            segmentI.setScore(s);
            vFlt.add(segmentI);
        }
        Collections.sort(vFlt, (new SSplitSeg()).new FltSort2());
    }

    public static int getSil(TreeSet<Segment> fltLst, int s, int l, int minLen, ArrayList<Segment> vFlt) {
        int e = l + s;
        int mid = s + (l / 2);
        s += minLen;
        e -= minLen;
        Iterator<Segment> itSeg = fltLst.iterator();
        vFlt.clear();
        while (itSeg.hasNext()) {
            Segment seg = itSeg.next();
            int fs = seg.getStart();
            int fl = seg.getLength();
            int fe = fs + fl;
            if ((fs > s) && (fe < e)) {
                Segment tmp = (Segment) (seg.clone());
                tmp.setInformation(keyMid, mid);
                vFlt.add(tmp);
            }
        }
        Collections.sort(vFlt, (new SSplitSeg()).new FltSort());
        return vFlt.size();
    }

    public static ClusterSet make(FeatureSet features, ClusterSet clusters, ArrayList<GMM> gmmVect, ClusterSet fltClusters, Parameter param)
            throws DiarizationException, IOException {
        trace = param.trace;
        int maxLen = param.parameterSegmentationSplit.getSegmentMaximumLength();
        int minLen = param.parameterSegmentationSplit.getSegmentMinimumLength();
        ArrayList<Segment> vSeg = clusters.getSegmentVectorRepresentation();
        TreeSet<Segment> fltLst = new TreeSet<Segment>();
        ArrayList<String> tokens = new ArrayList<String>();
        String sep = ",";

        StringTokenizer stok = new StringTokenizer(param.parameterSegmentationFilterFile.getClusterFilterName(), sep);
        while (stok.hasMoreTokens()) {
            tokens.add(stok.nextToken());
        }

        for (int i = 0; i < tokens.size(); i++) {
            // String token = tokens.get(i);
            // int idxCluster = fltClusters.getNameIndex(token);
            String idxCluster = tokens.get(i);
            if (fltClusters.containsCluster(idxCluster)) {
                Cluster fltCluster = fltClusters.getCluster(idxCluster);
                Iterator<Segment> itSeg = fltCluster.iterator();
                while (itSeg.hasNext()) {
                    fltLst.add(itSeg.next());
                }
            }
        }

        // --- Split segment based upon silence segment ---
        int i = 0;
        int size = vSeg.size();

        ArrayList<Segment> copy_vFlt = new ArrayList<Segment>();
        for (i = 0; i < size; i++) {
            Segment seg = vSeg.get(i);
            int l = seg.getLength();
            int s = seg.getStart();
            if (l > maxLen) {
                ArrayList<Segment> vFlt = new ArrayList<Segment>();
                if (SSplitSeg.getSil(fltLst, s, l, minLen, vFlt) >= 0) {
                    copy_vFlt.clear();
                    for (int cpt = 0; cpt < vFlt.size(); cpt++) {
                        copy_vFlt.add(vFlt.get(cpt));
                    }
                    SSplitSeg.splitSeg(vSeg, i, maxLen, minLen, copy_vFlt, 0);
                } else {
                    if (param.trace)
                        System.out.println("warning[sSplitSeg] \t no split segment, start=" + seg.getStart() + " " + seg.getLength());
                }
            }
        }

        // --- Check segment need to be split using gmm ---
        int wsize = 10;
        i = 0;
        while (i < vSeg.size()) {
            Segment seg = vSeg.get(i);
            features.setCurrentShow(seg.getShowName());
            int l = seg.getLength();
            int s = seg.getStart();
            int e = l + s;
            if (l > maxLen) {
                if (param.trace)
                    System.out.println("info[sSplitSeg] \t split segment using gmm, start=" + seg.getStart() + " " + seg.getLength());
                ArrayList<Segment> vFlt = new ArrayList<Segment>();
                SSplitSeg.findSil(features, gmmVect, s + minLen, e - minLen, wsize, vFlt);
                SSplitSeg.splitSeg(vSeg, i, maxLen, minLen, vFlt, 0);
            }
            i++;
        }

        // --- set the final segmentation ---
        ClusterSet res = new ClusterSet();
        res.addVector(vSeg);
        return res;
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "SSplitSeg");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);
                // Compute Model
                ArrayList<GMM> gmmVect = MainTools.readGMMContainer(param);

                ClusterSet fltClusters = new ClusterSet();
                fltClusters.read(param.showLst, param.parameterSegmentationFilterFile);

                ClusterSet res = make(features, clusters, gmmVect, fltClusters, param);

                MainTools.writeClusterSet(param, res, false);

            }
        } catch (DiarizationException e) {
            System.out.println("error \t exception " + e.getMessage());
        }
    }

    public static void splitSeg(ArrayList<Segment> vSeg, int cur, int maxLen, int minLen, ArrayList<Segment> fSeg, int dec) {
        int l = vSeg.get(cur).getLength();
        if (l > maxLen) {
            int s = vSeg.get(cur).getStart();
            int e = s + l;
            if (fSeg.size() > 0) {
                int idx = fSeg.get(0).getStart() + (fSeg.get(0).getLength() / 2);
                Segment seg = (Segment) (vSeg.get(cur).clone());
                seg.setStart(idx);
                seg.setLength(e - idx);
                vSeg.get(cur).setLength(idx - s - dec);
                vSeg.add(seg);
                ArrayList<Segment> fSeg1 = new ArrayList<Segment>();
                fSeg.remove(0);
                int idx_l = idx - minLen;
                int idx_r = idx + minLen;

                int cpt = 0;
                while (cpt < fSeg.size()) {
                    int curtmp = fSeg.get(cpt).getStart();
                    if ((curtmp > idx_l) && (curtmp < idx_r)) {
                        fSeg.remove(cpt);
                    } else {
                        cpt++;
                    }
                }
                cpt = 0;
                while (cpt < fSeg.size()) {
                    if (fSeg.get(cpt).getStart() > idx) {
                        fSeg1.add(fSeg.get(cpt));
                        fSeg.remove(cpt);
                    } else {
                        cpt++;
                    }
                }
                int n = vSeg.size() - 1;
                ArrayList<Segment> copyfSeg = new ArrayList<Segment>();
                for (int cptf = 0; cptf < fSeg1.size(); cptf++) {
                    copyfSeg.add(fSeg1.get(cptf));
                }
                SSplitSeg.splitSeg(vSeg, n, maxLen, minLen, copyfSeg, dec);
                copyfSeg.clear();
                for (int cptf = 0; cptf < fSeg.size(); cptf++) {
                    copyfSeg.add(fSeg.get(cptf));
                }
                SSplitSeg.splitSeg(vSeg, cur, maxLen, minLen, copyfSeg, dec);
            } else {
                if (trace)
                    System.out.println("warning[sSplitSeg] \t no more split segment, len=" + l);
            }
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
            param.parameterSegmentationFilterFile.printMask(); // sInFltMask
            param.parameterSegmentationFilterFile.printEncodingFormat();
            param.parameterSegmentationOutputFile.printMask(); // sOutMask
            param.parameterSegmentationOutputFile.printEncodingFormat();
            param.printSeparator();
            param.parameterSegmentationFilterFile.printFilterClusterName();
            param.parameterModelSetInputFile.printMask(); // tInMask
            param.printSeparator();
            param.parameterSegmentationSplit.printSegmentMaximumLength();
            param.parameterSegmentationSplit.printSegmentMinimumLength();
        }
    }

    private class FltSort implements Comparator<Segment> {
        public int compare(Segment seg1, Segment seg2) {
            int l1 = seg1.getLength();
            int l2 = seg2.getLength();
            if (l1 == l2) {
                int s1 = Math.abs(seg1.getStart() - Integer.parseInt(seg1.getInformation(keyMid)));
                int s2 = Math.abs(seg2.getStart() - Integer.parseInt(seg2.getInformation(keyMid)));
                return new Integer(s1).compareTo(new Integer(s2));
            }
            return new Integer(l2).compareTo(new Integer(l1));
        }
    }

    private class FltSort2 implements Comparator<Segment> {
        public int compare(Segment seg1, Segment seg2) {
            double l1 = seg1.getScore();
            double l2 = seg2.getScore();
            return Double.compare(l2, l1);
        }
    }

}