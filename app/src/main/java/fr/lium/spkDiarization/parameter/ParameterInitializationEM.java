/**
 * <p>
 * ParameterInitializationEM
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 UniversitE du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 * <p/>
 * not more use
 */

package fr.lium.spkDiarization.parameter;

import java.util.ArrayList;

import gnu.getopt.LongOpt;

public class ParameterInitializationEM extends ParameterEM {
    public static String[] TrainInitMethodString = {"split_all", "split", "uniform", "copy"};
    public static int ReferenceModelInitMethod = -1;
    private ModelInitMethod modelInitMethod; // Initialization method of GMM.

    ;

    public ParameterInitializationEM(ArrayList<LongOpt> list, Parameter param) {
        super();
        ReferenceModelInitMethod = param.getNextOptionIndex();
        ReferenceEMControl = param.getNextOptionIndex();
        setMinimumIteration(1);
        setMaximumIteration(5);
        setMinimumGain(0.01);
        setEMControl("1,5,0.01");
        modelInitMethod = ModelInitMethod.TRAININIT_UNIFORM;
        addOptions(list);
    }

    @Override
    public boolean readParam(int option, String optarg) {
        if (option == ReferenceEMControl) {
            setEMControl(optarg);
            return true;
        } else if (option == ReferenceModelInitMethod) {
            setModelInitMethod(optarg);
            return true;
        }
        return false;
    }

    @Override
    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("emCtrl", 1, null, ReferenceEMControl));
        list.add(new LongOpt("emInitMethod", 1, null, ReferenceModelInitMethod));
    }

    public ModelInitMethod getModelInitMethod() {
        return modelInitMethod;
    }

    public void setModelInitMethod(String ch) {
        if (ch.equals(TrainInitMethodString[ModelInitMethod.TRAININIT_SPLIT_ALL.ordinal()])) {
            modelInitMethod = ModelInitMethod.TRAININIT_SPLIT_ALL;
        } else if (ch.equals(TrainInitMethodString[ModelInitMethod.TRAININIT_SPLIT.ordinal()])) {
            modelInitMethod = ModelInitMethod.TRAININIT_SPLIT;
        } else if (ch.equals(TrainInitMethodString[ModelInitMethod.TRAININIT_UNIFORM.ordinal()])) {
            modelInitMethod = ModelInitMethod.TRAININIT_UNIFORM;
        } else if (ch.equals(TrainInitMethodString[ModelInitMethod.TRAININIT_COPY.ordinal()])) {
            modelInitMethod = ModelInitMethod.TRAININIT_COPY;
        }
    }

    public void printModelInitMethod() {
        super.print();
        System.out.print("info[ParameterInitializationEM]  --emInitMethod\tem initialization method ");
        System.out.print("[" + TrainInitMethodString[ModelInitMethod.TRAININIT_SPLIT_ALL.ordinal()] + ", ");
        System.out.print(TrainInitMethodString[ModelInitMethod.TRAININIT_SPLIT.ordinal()] + ", ");
        System.out.print(TrainInitMethodString[ModelInitMethod.TRAININIT_UNIFORM.ordinal()] + ", ");
        System.out.print(TrainInitMethodString[ModelInitMethod.TRAININIT_COPY.ordinal()] + "] = ");
        System.out.print(TrainInitMethodString[getModelInitMethod().ordinal()] + "(" + getModelInitMethod().ordinal());
        System.out.println(")");
    }

    // Type of initialization method of GMM.
    public enum ModelInitMethod {
        TRAININIT_SPLIT_ALL, TRAININIT_SPLIT, TRAININIT_UNIFORM, TRAININIT_COPY
    }
}
