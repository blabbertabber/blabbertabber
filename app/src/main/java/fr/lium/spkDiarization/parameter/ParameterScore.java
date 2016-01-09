/**
 * <p>
 * ParameterScore
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

import gnu.getopt.LongOpt;

public class ParameterScore implements ParameterInterface {
    public static String[] LabelTypeString = {"none", "add", "remplace"};
    public static int ReferenceGender = -1;

    ;
    public static int ReferenceTNorm = -1;
    public static int ReferenceByCluster = -1;
    public static int ReferenceBySegment = -1;
    public static int ReferenceSetLabel = -1;
    private boolean gender;

    // manque threshold, pris dans segmentation pour MScore
    private boolean TNorm;
    private boolean byCluster;
    private boolean bySegment;
    private int labelFormat;
    public ParameterScore(ArrayList<LongOpt> list, Parameter param) {
        setLabel(LabelTypeString[LabelType.LABEL_TYPE_NONE.ordinal()]);
        setGender(false);
        setTNorm(false);
        setByCluster(false);
        setBySegment(false);
        ReferenceGender = param.getNextOptionIndex();
        ReferenceTNorm = param.getNextOptionIndex();
        ReferenceByCluster = param.getNextOptionIndex();
        ReferenceBySegment = param.getNextOptionIndex();
        ReferenceSetLabel = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceGender) {
            setGender(true);
            return true;
        } else if (option == ReferenceByCluster) {
            setByCluster(true);
            return true;
        } else if (option == ReferenceBySegment) {
            setBySegment(true);
            return true;
        } else if (option == ReferenceSetLabel) {
            setLabel(optarg);
            return true;
        } else if (option == ReferenceTNorm) {
            setTNorm(true);
            return true;
        }
        return false;
    }

    public int getLabel() {
        return labelFormat;
    }

    public void setLabel(String value) {
        for (LabelType num : LabelType.values()) {
            System.err.println("***" + value + "=" + LabelTypeString[num.ordinal()]);
            if (value.equals(LabelTypeString[num.ordinal()])) {
                labelFormat = num.ordinal();
            }
        }
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("sGender", 0, null, ReferenceGender));
        list.add(new LongOpt("sByCluster", 0, null, ReferenceByCluster));
        list.add(new LongOpt("sTNorm", 0, null, ReferenceTNorm));
        list.add(new LongOpt("sBySegment", 0, null, ReferenceBySegment));
        list.add(new LongOpt("sSetLabel", 1, null, ReferenceSetLabel));
    }

    public boolean isGender() {
        return gender;
    }

    public void setGender(boolean scoreGender) {
        this.gender = scoreGender;
    }

    public boolean isTNorm() {
        return TNorm;
    }

    public void setTNorm(boolean scoreTNorm) {
        this.TNorm = scoreTNorm;
    }

    public boolean isByCluster() {
        return byCluster;
    }

    public void setByCluster(boolean scoreByCluster) {
        this.byCluster = scoreByCluster;
    }

    public boolean isBySegment() {
        return bySegment;
    }

    public void setBySegment(boolean scoreBySegment) {
        this.bySegment = scoreBySegment;
    }

    public void printGender() {
        System.out.println("info[ParameterScore] \t --sGender = " + isGender());
    }

    public void printByCluster() {
        System.out.println("info[ParameterScore] \t --sByCluster = " + isByCluster());

    }

    public void printBySegment() {
        System.out.println("info[ParameterScore] \t --sBySegment = " + isBySegment());

    }

    public void printSetLabel() {
        String ch = "[" + LabelTypeString[0];
        for (int i = 1; i < LabelTypeString.length; i++) {
            ch += ", " + LabelTypeString[i];
        }
        ch += "]";
        System.out.println("info[ParameterScore] \t --sSetLabel " + ch + " = " + LabelTypeString[labelFormat]);
    }

    public void printTNorm() {
        System.out.println("info[ParameterScore] \t --sTNorm = " + isTNorm());

    }

    // Type of duration constraint for Viterbi.
    public enum LabelType {
        LABEL_TYPE_NONE, LABEL_TYPE_ADD, LABEL_TYPE_REPLACE
    }
}