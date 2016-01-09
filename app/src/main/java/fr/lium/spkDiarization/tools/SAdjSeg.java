/**
 * <p>
 * SAdjSeg
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
 * move the segmentation boundary don't touch the last boundary
 */

package fr.lium.spkDiarization.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.parameter.Parameter;

public class SAdjSeg {
    static boolean trace = false;

    public static double getE(FeatureSet features, int indexOfEnergy, int i) throws DiarizationException {
        return features.getFeature(i)[indexOfEnergy];
    }

    public static void listSeg(FeatureSet features, Segment seg, int indexOfEnergy) throws DiarizationException {
        int start = seg.getStart();
        int len = seg.getLength();
        int end = start + len;

        int silStart = SAdjSeg.posMaxSil(features, start, 25, 5, indexOfEnergy);
        seg.setStart(silStart);
        seg.setLength(end - silStart);

        start = seg.getStart();
        len = seg.getLength();
        end = start + len;
        int silEnd = SAdjSeg.posMaxSil(features, end, 25, 5, indexOfEnergy);
        if (silEnd - silStart < 0) {
            seg.setLength(0);
        } else {
            seg.setLength(silEnd - silStart);
        }
    }

    public static ClusterSet make(FeatureSet features, ClusterSet clusters, Parameter param) throws DiarizationException, IOException {
        trace = param.trace;
        ArrayList<Segment> vSeg = clusters.getSegmentVectorRepresentation();
        int indexOfEnergy = param.parameterInputFeature.getFeaturesDescription().getIndexOfEnergy();
        // adjust segment boundaries
        if (indexOfEnergy < 0) {
            throw new DiarizationException("SAdjSeg: main() error: energy not present");
        }
        int size = vSeg.size();
        int cpt = 0;
        Iterator<Segment> itSeg = vSeg.iterator();
        while (itSeg.hasNext() && (cpt < (size - 1))) {
            Segment seg = itSeg.next();
            features.setCurrentShow(seg.getShowName());
            listSeg(features, seg, indexOfEnergy);
            cpt++;
        }

        ClusterSet res = new ClusterSet();
        res.addVector(vSeg);
        return res;
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "SAdjSeg");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);

                ClusterSet res = make(features, clusters, param);

                MainTools.writeClusterSet(param, res, false);
            }
        } catch (DiarizationException e) {
            System.err.println("error \t exception " + e.getMessage());
        }
    }

    public static int posMaxSil(FeatureSet features, int pos, int seachDecay, int sizeWinE, int indexOfEnergy) throws DiarizationException {
        int idxMin;
        int nbF = features.getNumberOfFeatures();

        int idxMeanL = Math.max(pos - seachDecay - sizeWinE, 0);
        int idxMeanR = Math.min(pos + seachDecay, nbF - 1);

        for (int i = idxMeanL; i < idxMeanR; i++) {
            if (features.compareFrames(i, i + 1)) {
                if (trace)
                    System.out.println("WARNING[sAdjSeg] \t two consecutive features are the same (" + i + "," + i + "+1) with pos = " + pos);
                return pos;
            }
            // System.out.println("test[sAdjSeg] \t i "+i);
        }

        idxMeanL = Math.max(pos - seachDecay - sizeWinE, 0);
        idxMeanR = Math.min(pos - seachDecay + sizeWinE, nbF);
        int nb = 0;
        double s = 0.0;
        for (int i = idxMeanL; i <= idxMeanR; i++) {
            s += SAdjSeg.getE(features, indexOfEnergy, i);
            nb++;
        }
        idxMeanL = Math.max(pos - seachDecay, 0);
        idxMeanR = Math.min(pos + seachDecay, nbF);
        double min = s / nb;
        idxMin = idxMeanL;
        for (int i = idxMeanL + 1; i < idxMeanR; i++) {
            if (i - sizeWinE - 1 >= 0) {
                s -= SAdjSeg.getE(features, indexOfEnergy, i - sizeWinE - 1);
                nb--;
            }
            if (i + sizeWinE < nbF) {
                s += SAdjSeg.getE(features, indexOfEnergy, i + sizeWinE);
                nb++;
            }
            double tmp = s / nb;
            if (tmp < min) {
                min = tmp;
                idxMin = i;
            }
        }
        return idxMin;
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
            param.parameterAdjustSegmentation.printSeachDecay();
            param.parameterAdjustSegmentation.printHalfWindowSizeForEnergie();
        }
    }

}