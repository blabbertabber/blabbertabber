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

import java.util.ArrayList;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.GMMFactory;
import fr.lium.spkDiarization.parameter.Parameter;

public class MTrainMAP {

    public static void make(FeatureSet features, ClusterSet clusters, ArrayList<GMM> initVect, ArrayList<GMM> gmmVect, Parameter param) throws Exception {
        if (initVect.size() != clusters.clusterGetSize()) {
            throw new DiarizationException("error \t initial model number is not good ");
        }
        for (int i = 0; i < initVect.size(); i++) {
            System.err.println("info : " + i + "=" + initVect.get(i).getName() + "/" + initVect.get(i).getName());
        }

        int nGmm = 0;
        for (int i = 0; i < initVect.size(); i++) {
            GMM initGMM = initVect.get(i);
            GMM UBMModel = initVect.get(i);
            String name = initGMM.getName();
            Cluster cluster = clusters.getCluster(name);
            if (cluster == null) {
                throw new DiarizationException("error \t can't find cluster for model " + name);
            }
            if (param.trace) {
                System.out.println("trace ------------------------------------");
                System.out.println("trace[mTrainEM] \t cluster=" + cluster.getName() + " size=" + cluster.getLength());
            }
            gmmVect.add(GMMFactory.getMAP(cluster, features, initGMM, UBMModel, param.parameterEM, param.parameterMAP, param.parameterVarianceControl,
                    param.parameterTopGaussian, param.trace));
            nGmm++;
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MTrainMAP");
            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);

                // Compute Model
                ArrayList<GMM> initVect = MainTools.readGMMContainer(param);
                ArrayList<GMM> gmmVect = new ArrayList<GMM>();

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

            param.parameterInputFeature.print(); // fInMask
            param.printSeparator();
            param.parameterSegmentationInputFile.print(); // sInMask
            param.printSeparator();
            param.parameterModelSetInputFile.printMask(); // tInMask
            param.parameterModelSetOutputFile.printMask(); // tOutMask
            param.printSeparator();
            param.parameterEM.print(); // emCtl
            param.parameterMAP.print(); // mapCtrl
            param.parameterVarianceControl.printVarianceControl(); // varCtrl
            param.printSeparator();
        }
    }

}
