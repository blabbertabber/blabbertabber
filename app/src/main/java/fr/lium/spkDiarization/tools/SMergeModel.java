/**
 * <p>
 * SMergeModel
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
 * Concat GMM model file in a GMM Vector and save it
 */

package fr.lium.spkDiarization.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.StringListFileIO;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.ModelIO;
import fr.lium.spkDiarization.parameter.Parameter;

public class SMergeModel {

    public static void main(String[] args) throws IOException {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "SMergeModel");
            ArrayList<GMM> gmmVect = new ArrayList<GMM>();
            try {
                ArrayList<String> lst = StringListFileIO.read(param.parameterInputFeature.getFeatureMask(), false);
                Iterator<String> it = lst.iterator();
                while (it.hasNext()) {
                    String string = it.next();
                    IOFile fi = new IOFile(IOFile.getFilename(string, ""), "rb");
                    fi.open();
                    ArrayList<GMM> tmp = new ArrayList<GMM>();
                    ModelIO.readerGMMContainer(fi, tmp);
                    Iterator<GMM> itTmp = tmp.iterator();
                    while (itTmp.hasNext()) {
                        gmmVect.add(itTmp.next());
                    }
                    tmp.clear();
                    fi.close();
                }
            } catch (Exception e) {
                gmmVect = MainTools.readGMMContainer(param);
            }
            System.out.println("gmm loaded:" + gmmVect.size());
            ArrayList<GMM> outGmmVect = new ArrayList<GMM>();
            outGmmVect.add(gmmVect.get(0));
            GMM out = outGmmVect.get(0);
            String name = out.getName();
            int nbFrames = out.getComponent(0).getCount();
            for (int i = 1; i < gmmVect.size(); i++) {
                GMM add = gmmVect.get(i);
                System.out.println("add gmm " + add.getName());
                name += add.getName();
                nbFrames += add.getComponent(0).getCount();
                for (int k = 0; k < add.getNbOfComponents(); k++) {
                    out.addComponent(add.getComponent(k));
                }
            }
            for (long i = 0; i < out.getNbOfComponents(); i++) {
                out.updateCount(nbFrames);
            }
            out.setName(name);
            out.normWeights();
            out.sortComponents();
            MainTools.writeGMMContainer(param, outGmmVect);
        } catch (DiarizationException e) {
            System.out.println("error \t exception " + e.getMessage());
        }

    }

    public static void info(Parameter param, String prog) {
        if (param.help) {
            param.printSeparator2();
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterModelSetInputFile.printMask(); // tInMask
            param.parameterModelSetOutputFile.printMask(); // tOutMask
        }
    }

}