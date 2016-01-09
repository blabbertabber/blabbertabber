/**
 * <p>
 * MDecode
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
 * Viterbi decoding program
 */

package fr.lium.spkDiarization.programs;

import java.util.ArrayList;
import java.util.TreeSet;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libDecoder.DecoderWithDuration;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.parameter.Parameter;

public class MDecode {
    public static ClusterSet make(FeatureSet features, ClusterSet clusters, ArrayList<GMM> gmmVect, Parameter param) throws Exception {
        DecoderWithDuration decoder = null;
        if (param.parameterTopGaussian.getScoreNTop() > 0) {
            ArrayList<GMM> wld = MainTools.readGMMForTopGaussian(param, features);
            decoder = new DecoderWithDuration(param.parameterTopGaussian.getScoreNTop(), wld.get(0), param.parameterDecoder.isComputeLLhR(), param.parameterDecoder.getShift());
        } else {
            decoder = new DecoderWithDuration(param.parameterDecoder.getShift());
        }
        decoder.setupHMM(gmmVect, param);
        ClusterSet clustersToDecode = new ClusterSet();
        Cluster clusterToDecode = clustersToDecode.getOrCreateANewCluster("Init");
        TreeSet<Segment> segLstDecode = clusters.getSegments();

        if (param.trace) {
            System.out.println("trace ----------------------------------------------------");
            System.out.println("trace[mDecode] \t decoder.init");
            System.out.println("trace[mDecode] \t decoder.init");
        }

        for (Segment segment : segLstDecode) {
            clusterToDecode.addSegment(segment);
        }
        clustersToDecode.collapse();
        segLstDecode = clustersToDecode.getSegments();

        for (Segment segment : segLstDecode) {
            if (param.trace) {
                System.out.println("trace[mDecode] \t decoder.acc start=" + segment.getStart());
            }
// decoder.accumulate(features, segment, initSet);
            decoder.accumulate(features, segment);
        }
        if (param.trace) {
            System.out.println("trace[mDecode] \t decoder.get");
            // decoder.debug();
        }
        return decoder.getClusters(clusters);
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MDecode");
            if (param.nbShow > 0) {
                // Clusters
                ClusterSet clusters = MainTools.readClusterSet(param);
                // clusters.debug();

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);

                // Models
                ArrayList<GMM> gmmVect = MainTools.readGMMContainer(param);

                // Create the decoder
                ClusterSet clustersRes = make(features, clusters, gmmVect, param);
                // Seg outPut
                MainTools.writeClusterSet(param, clustersRes, false);
            }
        } catch (DiarizationException e) {
            System.out.println("error \t exception " + e.getMessage());
        }
    }

    /**
     * Get a Viterbi decoding result
     */
/*	public static TreeSet<Integer> setupViterbiDecoder(Decoder decoder, ArrayList<GMM> gmmVect, Parameter param) {
        // add model
		TreeSet<Integer> initSet = new TreeSet<Integer>();
		int sizeGmm = gmmVect.size();
		int sizeP = param.parameterDecoder.getDecodePenalty().size();
		int len = 1;
		int start = len - 1;
		for (int i = 0; i < sizeGmm; i++) {
			GMM res = gmmVect.get(i);
			double p = param.parameterDecoder.getDecodePenalty().get(sizeP - 1);
			if (i < sizeP) {
				p = param.parameterDecoder.getDecodePenalty().get(i);
			}
			decoder.add(res, len, p);
			initSet.add(start);
			if (param.trace) {
				System.out.println("trace[mDecode] \t penalty=" + p + " init=" + start);
			}
			start += len;
		}
		decoder.makePenalty();
		return initSet;
	}
*/

    /**
     * Initialize the decoder
     */

    public static void info(Parameter param, String prog) {
        if (param.help) {
            param.printSeparator2();
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterInputFeature.print();
            param.printSeparator();
            param.parameterSegmentationInputFile.print();
            param.parameterSegmentationOutputFile.print();
            param.printSeparator();
            param.parameterModelSetInputFile.printMask(); // tInMask
            param.parameterTopGaussian.printTopGaussian(); // sTop
            param.printSeparator();
            param.parameterDecoder.print();
        }
    }

}
