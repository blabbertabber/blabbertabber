/**
 * <p>
 * MTrainEM
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
 * EM trainer program for the GMMs
 */

package fr.lium.spkDiarization.programs;

import java.io.IOException;
import java.util.ArrayList;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.GMMFactory;
import fr.lium.spkDiarization.parameter.Parameter;

public class MTrainEM {
    public static GMM compute(FeatureSet features, Cluster cluster, GMM initGMM, Parameter param) throws DiarizationException, IOException {
        if (param.trace) {
            System.out.println("trace ------------------------------------");
            System.out.println("trace[mTrainEM] \t cluster=" + cluster.getName());
        }
        return GMMFactory.getEM(cluster, features, initGMM, param.parameterModel.getNumberOfComponents(), param.parameterEM,
                param.parameterVarianceControl, param.trace);
    }

    public static void make(FeatureSet features, ClusterSet clusters, ArrayList<GMM> initVect, ArrayList<GMM> GMMList, Parameter param) throws Exception {
        if (initVect.size() != clusters.clusterGetSize()) {
            throw new DiarizationException("error[MTrainEM] \t initial model number is not good :" + initVect.size() + "!=" + clusters.clusterGetSize());
        }
        int nGmm = 0;
        for (int i = 0; i < initVect.size(); i++) {
            GMM initGMM = initVect.get(i);
            String name = initGMM.getName();
            Cluster cluster = clusters.getCluster(name);
            if (cluster == null) {
                throw new DiarizationException("error[MTrainEM] \t can't find cluster for model " + name);
            }
            GMM gmm = compute(features, cluster, initGMM, param);
            GMMList.add(gmm);
            nGmm++;
        }
    }

/*	public static void make2(FeatureSet features, ClusterSet clusters, ArrayList<GMM> initVect, ArrayList<ClusterAndGMM> clusterAndGMM, Parameter param) throws Exception {
		if (initVect.size() != clusters.clusterGetSize()) {
			throw new DiarizationException("error \t initial model number is not good ");
		}
		int nGmm = 0;
		for (int i = 0; i < initVect.size(); i++) {
			GMM initGMM = initVect.get(i);
			String name = initGMM.getName();
			Cluster cluster = clusters.getCluster(name);
			if (cluster == null) {
				throw new DiarizationException("error \t can't find cluster for model " + name);
			}
			GMM gmm = compute(features, cluster, initGMM, param);
			clusterAndGMM.add(new ClusterAndGMM(cluster, gmm));
			nGmm++;
		}
	}*/

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MTrainEM");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);

                // Compute Model
                ArrayList<GMM> initVect = MainTools.readGMMContainer(param);
                // Compute Model
                ArrayList<GMM> gmmVect = new ArrayList<GMM>(clusters.clusterGetSize());

                make(features, clusters, initVect, gmmVect, param);

                MainTools.writeGMMContainer(param, gmmVect);
            }
        } catch (DiarizationException e) {
            System.out.println("error \t Exception : " + e.getMessage());
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
            param.printSeparator();
            param.parameterModelSetInputFile.printMask(); // tInMask
            param.parameterModelSetOutputFile.printMask(); // tOutMask
            param.printSeparator();
            param.parameterEM.print(); // emCtl
            param.parameterVarianceControl.printVarianceControl(); // varCtrl
            param.printSeparator();
        }
    }

}