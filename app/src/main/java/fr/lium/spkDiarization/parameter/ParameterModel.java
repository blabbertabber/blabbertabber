/**
 * <p>
 * ParameterModel
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
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
 * not more use
 */

package fr.lium.spkDiarization.parameter;

import java.util.ArrayList;

import fr.lium.spkDiarization.libModel.Gaussian;
import gnu.getopt.LongOpt;

public class ParameterModel implements ParameterInterface {
    public static int ReferenceKind = -1;
    public static int ReferenceNumberOfComponents = -1;

    public static String[] KindTypeString = {"FULL", "DIAG"};

    private int kind;
    private int numberOfComponents;

    public ParameterModel(ArrayList<LongOpt> list, Parameter param) {
        kind = Gaussian.FULL;
        setNumberOfComponents(1);
        ReferenceKind = param.getNextOptionIndex();
        ReferenceNumberOfComponents = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceKind) {
            setKind(optarg);
            return true;
        } else if (option == ReferenceNumberOfComponents) {
            setNumberOfComponents(Integer.parseInt(optarg));
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("kind", 1, null, ReferenceKind));
        list.add(new LongOpt("nbComp", 1, null, ReferenceNumberOfComponents));
    }

    public int getKind() {
        return kind;
    }

    public void setKind(String ch) {
        if (ch.equals(KindTypeString[Gaussian.FULL])) {
            kind = Gaussian.FULL;
        } else if (ch.equals(KindTypeString[Gaussian.DIAG])) {
            kind = Gaussian.DIAG;
        }

    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public int getNumberOfComponents() {
        return numberOfComponents;
    }

    public void setNumberOfComponents(int nbComp) {
        this.numberOfComponents = nbComp;
    }

    public void printKind() {
        System.out.print("info[ParameterModel] \t --kind \t kind of Gaussians [");
        System.out.print(KindTypeString[Gaussian.FULL] + "," + KindTypeString[Gaussian.DIAG] + "] = ");
        System.out.println(KindTypeString[getKind()] + "(" + getKind() + ")");
    }

    public void printNumberOfComponents() {
        System.out.print("info[ParameterModel] \t --nbComp \t number of Gaussians = ");
        System.out.println(getNumberOfComponents());
    }
}