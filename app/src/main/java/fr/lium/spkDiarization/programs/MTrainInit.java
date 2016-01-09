/**
 * <p>
 * MTrainInit
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
 * Program for the initialization of the GMMs
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
import fr.lium.spkDiarization.parameter.ParameterInitializationEM.ModelInitMethod;

public class MTrainInit {
    public static void make(FeatureSet features, ClusterSet clusters, ArrayList<GMM> gmmVect, Parameter param) throws Exception {
        // wld/UBM ?
        ArrayList<GMM> wldVect = new ArrayList<GMM>();

        if (param.parameterInitializationEM.getModelInitMethod().equals(ModelInitMethod.TRAININIT_COPY)) {
            wldVect = MainTools.readGMMContainer(param);
            if (wldVect.size() > 1) {
                throw new DiarizationException("error \t UBM input model is not unique ");
            }
        }

        // training

        int nGmm = 0;
        for (String name : clusters) {
            Cluster cluster = clusters.getCluster(name);
            if (!param.parameterInitializationEM.getModelInitMethod().equals(ModelInitMethod.TRAININIT_COPY)) {
                if (param.trace) {
                    System.out.println("trace ------------------------------------");
                    System.out.println("trace[mTrainInit] \t cluster=" + cluster.getName());
                }
                gmmVect.add(GMMFactory.initializeGMM(name, cluster, features, param.parameterModel.getKind(), param.parameterModel.getNumberOfComponents(),
                        param.parameterInitializationEM.getModelInitMethod(), param.parameterEM, param.parameterVarianceControl, param.trace));
            } else {
                gmmVect.add((GMM) wldVect.get(0).clone());
                gmmVect.get(nGmm).setName(name);
            }
            nGmm++;
        }
        for (int i = 0; i < gmmVect.size(); i++) {
            if (param.trace)
                System.err.println("info : " + i + "=" + gmmVect.get(i).getName() + "/" + gmmVect.get(i).getName());
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MTrainInit");

            if (param.nbShow > 0) {
                // clusters
                ClusterSet clusters = MainTools.readClusterSet(param);

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);
                // Compute Model
                ArrayList<GMM> gmmVect = new ArrayList<GMM>(clusters.clusterGetSize());

                make(features, clusters, gmmVect, param);

                MainTools.writeGMMContainer(param, gmmVect);
            }
        } catch (DiarizationException e) {
            System.err.println("error \t Exception : " + e.getMessage());
        }
    }

    public static void info(Parameter param, String prog) {
        if (param.help) {
            param.printSeparator2();
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterInputFeature.print();
            param.printSeparator();
            param.parameterSegmentationInputFile.print();
            if (param.parameterInitializationEM.getModelInitMethod().equals(ModelInitMethod.TRAININIT_COPY)) {
                param.parameterModelSetInputFile.printMask(); // tInMask
            } else {
                param.parameterModel.printKind(); // kind
                param.parameterModel.printNumberOfComponents(); // nbComp
            }
            param.parameterModelSetOutputFile.printMask(); // tOutMask
            param.printSeparator();
            param.parameterInitializationEM.printModelInitMethod(); // emInitMethod
            if (!((param.parameterInitializationEM.getModelInitMethod().equals(ModelInitMethod.TRAININIT_COPY)) || (param.parameterInitializationEM
                    .getModelInitMethod().equals(ModelInitMethod.TRAININIT_UNIFORM)))) {
                param.printSeparator();
                param.parameterEM.print(); // emCtl
                param.parameterVarianceControl.printVarianceControl(); // varCtrl
            }
            param.printSeparator();
        }
    }

}