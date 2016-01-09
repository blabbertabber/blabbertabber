/**
 * <p>
 * MSeg
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

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.DiagGaussian;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.FullGaussian;
import fr.lium.spkDiarization.libModel.GMMFactory;
import fr.lium.spkDiarization.libModel.Gaussian;
import fr.lium.spkDiarization.libSegmentationMethod.Borders;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterSegmentation;

public class MSeg {

    public static boolean checkMax(Segment segment, int max, Parameter param, FeatureSet features) throws DiarizationException {
        int length = segment.getLength();
        int start = segment.getStart();
        int end = start + length;
        Gaussian g1;
        if (param.parameterModel.getKind() == Gaussian.FULL) {
            g1 = new FullGaussian(features.getDim());
        } else {
            g1 = new DiagGaussian(features.getDim());
        }
        Segment leftSegment = (Segment) (segment.clone());
        int leftResult = -1;
        leftSegment.setLength(max - start);
        leftResult = GMMFactory.initializeGaussian(features, g1, leftSegment.getStart(), leftSegment.getLength());

        Segment rightSegment = (Segment) (segment.clone());
        int rightResult = -1;
        rightSegment.setStart(max);
        rightSegment.setLength(end - max);
        g1.resetStatisticAccumulator();
        rightResult = GMMFactory.initializeGaussian(features, g1, rightSegment.getStart(), rightSegment.getLength());
        if ((rightResult >= 0) && (leftResult >= 0)) {
            return true;
        }
        return false;
    }

    /**
     * select the true border from the array of similarities
     *
     * @return the borders
     */
    public static Borders doBorders(double[] measures, Parameter param) {
        int size = measures.length;
        Borders borders = new Borders();
        borders.put(0, 0.0);
        int j = 0;
        borders.put(measures.length - 1, 0.0);
        double thr = param.parameterSegmentation.getThreshold();
        if (param.parameterSegmentation.getMethod() == ParameterSegmentation.SegmentationMethod.SEG_BIC) {
            thr = 0;
        }

        int i = param.parameterSegmentation.getMinimimWindowSize() - 1;

        while (i < size) {
            double curr = measures[i];
            int start = Math.max(0, i - param.parameterSegmentation.getMinimimWindowSize());
            int end = Math.min(size, i + param.parameterSegmentation.getMinimimWindowSize());
            double max = measures[start];
            for (int m = start + 1; m < end; m++) {
                double v = measures[m];
                if ((i != m) && (v > max)) {
                    max = v;
                }
            }
            if ((curr > max) && (curr > thr)) {
                if (param.trace) {
                    System.out.println("trace[mSeg] \t nb=" + j + " i=" + i + " " + curr);
                }
                borders.put(i, curr);
                i += param.parameterSegmentation.getMinimimWindowSize();
                j++;
            } else {
                i++;
            }
        }
        return borders;
    }

    /**
     * add Borders to Clusters
     */
    public static int doClusters(int idx, Borders borders, Segment inputSegment, ClusterSet clusters, Parameter param) {
        Iterator<Integer> it = borders.getSortedKeys();

        if (param.trace) {
            System.out.println("trace[mSeg] \t maxIdxName=" + idx);
        }
        String show = inputSegment.getShowName();
        // clusters.addShowAndGetIndex(show);
        int start = inputSegment.getStart();
        // int idxShow = clusters.getShowIndex(show);
        int clePrev = 0;
        it.next();
        while (it.hasNext()) {
            int cleCur = it.next();
            StringBuffer name = new StringBuffer();
            name.append("S" + idx);
            Cluster cluster = clusters.createANewCluster(name.toString());
            idx++;
            int st = start + clePrev;
            int ln = cleCur - clePrev;
            Segment segment = new Segment(show, st, ln, cluster);
            cluster.addSegment(segment);
            if (param.trace) {
                System.out.println("trace[mSeg] \t name=" + name.toString() + " start=" + st + " len=" + ln);
            }
            clePrev = cleCur;
        }
        return idx;
    }

    /**
     * Compute all the similarity
     *
     * @return a array of similarity
     * @throws DiarizationException
     * @throws IOException
     */
    public static double[] doMeasures(FeatureSet features, Segment seg, Parameter param) throws DiarizationException, IOException {
        features.setCurrentShow(seg.getShowName());
        int start = seg.getStart();
        int nb = Math.min(seg.getLength(), features.getNumberOfFeatures());

        double[] measures = new double[nb];
        int idxMeasures = 0;
        if (param.trace) {
            System.out.println("trace[mSeg] \t start=" + start + " len=" + nb);
        }

        if (nb < (2 * param.parameterSegmentation.getModelWindowSize())) {
            for (long i = 0; i < nb; i++) {
                measures[idxMeasures++] = Double.MIN_VALUE;
            }
        } else {

            int dim = features.getDim();
            Gaussian g1;
            Gaussian g2;
            if (param.parameterModel.getKind() == Gaussian.FULL) {
                g1 = new FullGaussian(dim);
                g2 = new FullGaussian(dim);
            } else {
                g1 = new DiagGaussian(dim);
                g2 = new DiagGaussian(dim);
            }
            GMMFactory.initializeGaussian(features, g1, start + 0, param.parameterSegmentation.getModelWindowSize());
            GMMFactory.initializeGaussian(features, g2, start + param.parameterSegmentation.getModelWindowSize(), param.parameterSegmentation.getModelWindowSize());
            // start
            double cst = Distance.BICConstant(param.parameterModel.getKind(), dim, param.parameterSegmentation.getThreshold());
            double s = MSeg.getSimilarity(g1, g2, param, cst);
            for (int i = 0; i < param.parameterSegmentation.getModelWindowSize(); i++) {
                measures[idxMeasures++] = s;
            }
            // compute borders
            for (int i = param.parameterSegmentation.getModelWindowSize(); i < nb - param.parameterSegmentation.getModelWindowSize(); i++) {
                g1.removeFeatureFromAccumulator(features, start + i - param.parameterSegmentation.getModelWindowSize());
                g1.addFeature(features, start + i);
                g2.removeFeatureFromAccumulator(features, start + i);
                g2.addFeature(features, start + i + param.parameterSegmentation.getModelWindowSize());

                g1.setModelFromAccululator();
                g2.setModelFromAccululator();
                s = MSeg.getSimilarity(g1, g2, param, cst);
                measures[idxMeasures++] = s;
            }
            // end
            for (int i = nb - param.parameterSegmentation.getModelWindowSize(); i < nb; i++) {
                measures[idxMeasures++] = s;
            }
        }
        return measures;
    }

    public static void doSplit(double[] measures, Segment segment, int startMeasures, int minLen, ArrayList<Segment> arraySegment) {
        int length = segment.getLength();
        if (length > (minLen + minLen + 1)) {
            int start = segment.getStart();
            int end = start + length;
            int max = -1;
            double maxValue = Double.MIN_VALUE;

            for (int i = start + minLen; i < end - minLen; i++) {
                double value = measures[i];
                if (value > maxValue) {
                    maxValue = value;
                    max = i;
                }
            }
            System.out.println("trace[mSeg] \t split max=" + max + " start=" + start + " lenght=" + length);

            Segment leftSegment = (Segment) (segment.clone());
            leftSegment.setLength(max - start);

            System.out.println("trace[mSeg] \t left =" + leftSegment.getStart() + " len=" + leftSegment.getLength());

            doSplit(measures, leftSegment, startMeasures, minLen, arraySegment);

            Segment rightSegment = (Segment) (segment.clone());
            rightSegment.setStart(max);
            rightSegment.setLength(end - max);
            System.out.println("trace[mSeg] \t right =" + rightSegment.getStart() + " len=" + rightSegment.getLength());
            doSplit(measures, rightSegment, startMeasures, minLen, arraySegment);
        } else {
            System.out.println("trace[mSeg] \t add =" + segment.getStart() + " len=" + segment.getLength());
            System.out.println("trace[mSeg] \t *****************************");

            arraySegment.add(segment);
        }
    }

    public static boolean doSplit2(double[] measures, Segment segment, int startMeasures, int minLen, ArrayList<Segment> arraySegment, Parameter param,
                                   FeatureSet features) throws DiarizationException {
        int length = segment.getLength();
        int start = segment.getStart();
        int end = start + length;
        int max = -1;
        double maxValue = Double.MIN_VALUE;

        for (int i = start + minLen; i < end - minLen; i++) {
            double value = measures[i];
            if (value > maxValue) {
                if (checkMax(segment, i, param, features)) {
                    maxValue = value;
                    max = i;
                    // System.err.println("trace[mSeg] \t  max=" + i+
                    // " **** OK");
                } else {
                    System.err.println("trace[mSeg] \t  max=" + i + " reject");
                }
            }
        }
        if (max == -1) {
            return false;
        }
        System.err.println("trace[mSeg] \t split max=" + max + " start=" + start + " lenght=" + length);

        Segment leftSegment = (Segment) (segment.clone());
        leftSegment.setLength(max - start);
        System.err.println("trace[mSeg] \t left =" + leftSegment.getStart() + " len=" + leftSegment.getLength());
        if (doSplit2(measures, leftSegment, startMeasures, minLen, arraySegment, param, features) == false) {
            arraySegment.add(leftSegment);
        }

        Segment rightSegment = (Segment) (segment.clone());
        rightSegment.setStart(max);
        rightSegment.setLength(end - max);
        System.err.println("trace[mSeg] \t right =" + rightSegment.getStart() + " len=" + rightSegment.getLength());
        if (doSplit2(measures, rightSegment, startMeasures, minLen, arraySegment, param, features) == false) {
            arraySegment.add(rightSegment);
        }

        return true;
    }

    /**
     * select and compute the similarity method
     *
     * @param g1 the first Gaussien
     * @param g2 the second Gaussien
     * @param param the parameter structure
     * @param BICCst the constant need in BIC similarity
     * @return the similarity
     * @throws DiarizationException
     */
    public static double getSimilarity(Gaussian g1, Gaussian g2, Parameter param, double BICCst) throws DiarizationException {
        if (param.parameterSegmentation.getMethod().equals(ParameterSegmentation.SegmentationMethod.SEG_GLR)) {
            return Distance.GLR(g1, g2);
        } else {
            if (param.parameterSegmentation.getMethod().equals(ParameterSegmentation.SegmentationMethod.SEG_BIC)) {
                int len = g1.getCount() + g2.getCount();
                // double cst = Distance.BICConstant(param.kind, dim,
                // param.segThr);
                return Distance.BIC(g1, g2, BICCst, len);
            } else {
                if (param.parameterSegmentation.getMethod().equals(ParameterSegmentation.SegmentationMethod.SEG_KL2)) {
                    return Distance.KL2(g1, g2);
                } else {
                    if (param.parameterSegmentation.getMethod().equals(ParameterSegmentation.SegmentationMethod.SEG_GD)) {
                        return Distance.GD(g1, g2);
                    } else {
                        if (param.parameterSegmentation.getMethod().equals(ParameterSegmentation.SegmentationMethod.SEG_H2)) {
                            return Distance.H2(g1, g2);
                        } else {
                            throw new DiarizationException("mSeg unknown similarity " + param.parameterSegmentation.getMethod());
                        }
                    }
                }
            }
        }
    }

    static public ClusterSet valide(FeatureSet features, ClusterSet clusters, Parameter param) throws DiarizationException {
        ClusterSet result = new ClusterSet();

        Iterator<Segment> itSegment = clusters.getSegments().iterator();
        Segment previous = itSegment.next();
        Segment current = itSegment.next();

        Gaussian gPrevious, gCurrent, gNext = null;
        int dim = features.getDim();
        if (param.parameterModel.getKind() == Gaussian.FULL) {
            gPrevious = new FullGaussian(dim);
            gCurrent = new FullGaussian(dim);
        } else {
            gPrevious = new DiagGaussian(dim);
            gCurrent = new DiagGaussian(dim);
        }
        GMMFactory.initializeGaussian(features, gPrevious, previous.getStart(), previous.getLength());
        GMMFactory.initializeGaussian(features, gCurrent, current.getStart(), current.getLength());
        double cst = Distance.BICConstant(param.parameterModel.getKind(), dim, 1);

        while (itSegment.hasNext()) {
            Segment next = itSegment.next();

            if (param.parameterModel.getKind() == Gaussian.FULL) {
                gNext = new FullGaussian(dim);
            } else {
                gNext = new DiagGaussian(dim);
            }
            GMMFactory.initializeGaussian(features, gNext, next.getStart(), next.getLength());

            double dpc = Distance.BICLocal(gPrevious, gCurrent, cst);
            double dcn = Distance.BICLocal(gCurrent, gNext, cst);
            double dpn = Distance.BICLocal(gPrevious, gNext, cst);

            current.setInformation("Prev/Cur", dpc);
            current.setInformation("Cur/Next", dcn);
            current.setInformation("Prev/Next", dpn);

            if ((dpn > 0) && (dpc < 0) && (dcn < 0)) {
                System.err.println("sup distance: Prev/Cur=" + dpc + " Cur/Next=" + dcn + " Prev/Next=" + dpn + " prev=" + previous.getStart() + " cur="
                        + current.getStart() + " next=" + next.getStart());
                current.setInformation("sup", "0");
            }

            // move
            previous = current;
            gPrevious = gCurrent;
            current = next;
            gCurrent = gNext;
        }

        return result;
    }

    public static void make(FeatureSet features, ClusterSet clusters, ClusterSet clustersRes, Parameter param) throws Exception {
        if (param.parameterSegmentation.isRecursion()) {
            ArrayList<Segment> arraySegment = new ArrayList<Segment>();
            for (String cle : clusters) {
                for (Segment seg : clusters.getCluster(cle)) {
                    if (param.trace) {
                        System.out.println("trace[mSeg] \t doMeasures");
                    }
                    double[] m = MSeg.doMeasures(features, seg, param);
                    if (doSplit2(m, seg, seg.getStart(), param.parameterSegmentation.getMinimimWindowSize(), arraySegment, param, features) == false) {
                        arraySegment.add(seg);
                    }
                }
            }
            for (int i = 0; i < arraySegment.size(); i++) {
                String name = new String("S" + Integer.toString(i));
                Cluster c = clustersRes.createANewCluster(name);
                c.addSegment(arraySegment.get(i));
            }
        } else {
            int idx = 0;

            for (String cle : clusters) {
                for (Segment seg : clusters.getCluster(cle)) {

                    if (param.trace) {
                        System.out.println("trace[mSeg] \t doMeasures");
                    }
                    double[] m = MSeg.doMeasures(features, seg, param);
                    dumpMeasures(m);
                    if (param.trace) {
                        System.out.println("trace[mSeg] \t doBorders");
                    }
                    Borders b = MSeg.doBorders(m, param);
                    if (param.trace) {
                        System.out.println("trace[mSeg] \t doClusters");
                    }
                    idx = MSeg.doClusters(idx, b, seg, clustersRes, param);
                }
            }

        }
    }

    public static void main(String[] args) throws Exception {

        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MSeg");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);
                clusters.collapse();
                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);
                ClusterSet clustersRes = new ClusterSet();
                make(features, clusters, clustersRes, param);
                MainTools.writeClusterSet(param, clustersRes, false);
            }
        } catch (DiarizationException e) {
            System.out.print("error \t exception " + e.getMessage());
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
            param.parameterModel.printKind(); // kind
            param.printSeparator();
            param.parameterSegmentation.printMethod(); // sMethod
            param.parameterSegmentation.printThreshold(); // sThr
            param.parameterSegmentation.printModelWindowSize(); // sWSize
            param.parameterSegmentation.printMinimimWindowSize(); // sMinWSize
            param.parameterSegmentation.printRecursion(); // sMinWSize
        }
    }


    /**
     * Dumps the linear segmentation measures to a binary file.
     */
    public static void dumpMeasures(double[] measures) {

        DataOutputStream outStream = null;

        try {
            outStream = new DataOutputStream(new FileOutputStream("/sdcard/linSegMeasures"));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        for (double val : measures) {
            try {
                outStream.writeDouble(val);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}