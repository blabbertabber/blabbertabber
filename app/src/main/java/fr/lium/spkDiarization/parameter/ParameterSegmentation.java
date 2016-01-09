/**
 * <p>
 * ParameterSegmentation
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

public class ParameterSegmentation implements ParameterInterface {
    public static String[] SegMethodString = {"BIC", "GLR", "KL2", "GD", "H2"};

    ;
    // Type of distance for silence segmentation.
    /*
	 * public enum SegSilMethod { SEGSIL_ETHR, SEGSIL_GAUSS };
	 */
    public static int ReferenceModelWindowSize = -1;
    public static int ReferenceMinimimWindowSize = -1;
    public static int ReferenceThreshold = -1;
    public static int ReferenceMethod = -1;
    public static int ReferenceSilenceThreshold = -1;
    public static int ReferenceRecursion = -1;
    private int modelWindowSize; // Size of a window in segmentation, ie number
    // of feature for the learning of a model.
    private int minimimWindowSize; // Minimum size of segment.
    private double threshold; // Segmentation threshold.
    private SegmentationMethod method; // Segmentation method.
    // private SegSilMethod segSilMethod; // Silence segmentation method.
    private double silenceThreshold; // Silence segmentation threshold.
    private boolean recursion;
    //
    public ParameterSegmentation(ArrayList<LongOpt> list, Parameter param) {
        setModelWindowSize(250);
        setMinimimWindowSize(250);
        setThreshold(Integer.MIN_VALUE);
        method = SegmentationMethod.SEG_GLR;
        // setSegSilMethod(SegSilMethod.SEGSIL_ETHR);
        setSilenceThreshold(0.0);
        setRecursion(false);
        ReferenceModelWindowSize = param.getNextOptionIndex();
        ReferenceMinimimWindowSize = param.getNextOptionIndex();
        ReferenceThreshold = param.getNextOptionIndex();
        ReferenceMethod = param.getNextOptionIndex();
        ReferenceSilenceThreshold = param.getNextOptionIndex();
        ReferenceRecursion = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceModelWindowSize) {
            setModelWindowSize(Integer.parseInt(optarg));
            return true;
        } else if (option == ReferenceMinimimWindowSize) {
            setMinimimWindowSize(Integer.parseInt(optarg));
            return true;
        } else if (option == ReferenceThreshold) {
            setThreshold(Long.parseLong(optarg));
            return true;
        } else if (option == ReferenceMethod) {
            setMethod(optarg);
            return true;
        } else if (option == ReferenceRecursion) {
            setRecursion(true);
            return true;
        } else if (option == ReferenceSilenceThreshold) {
            setSilenceThreshold(Double.parseDouble(optarg));
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("sModelWindowSize", 1, null, ReferenceModelWindowSize));
        list.add(new LongOpt("sMinimumWindowSize", 1, null, ReferenceMinimimWindowSize));
        list.add(new LongOpt("sThr", 1, null, ReferenceThreshold));
        list.add(new LongOpt("sMethod", 1, null, ReferenceMethod));
        list.add(new LongOpt("sRecursion", 0, null, ReferenceRecursion));
        list.add(new LongOpt("sSilenceThr", 1, null, ReferenceSilenceThreshold));
    }

    public int getModelWindowSize() {

        return modelWindowSize;
    }

    public void setModelWindowSize(int segWSize) {
        this.modelWindowSize = segWSize;
    }

    public int getMinimimWindowSize() {
        return minimimWindowSize;
    }

    public void setMinimimWindowSize(int segMinWSize) {
        this.minimimWindowSize = segMinWSize;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double segThr) {
        this.threshold = segThr;
    }

    public SegmentationMethod getMethod() {
        return method;
    }

    public void setMethod(String ch) {
        if (ch.equals(SegMethodString[SegmentationMethod.SEG_BIC.ordinal()])) {
            method = SegmentationMethod.SEG_BIC;
        } else if (ch.equals(SegMethodString[SegmentationMethod.SEG_GLR.ordinal()])) {
            method = SegmentationMethod.SEG_GLR;
        } else if (ch.equals(SegMethodString[SegmentationMethod.SEG_KL2.ordinal()])) {
            method = SegmentationMethod.SEG_KL2;
        } else if (ch.equals(SegMethodString[SegmentationMethod.SEG_GD.ordinal()])) {
            method = SegmentationMethod.SEG_GD;
        } else if (ch.equals(SegMethodString[SegmentationMethod.SEG_H2.ordinal()])) {
            method = SegmentationMethod.SEG_H2;
        }
    }

    public double getSilenceThreshold() {
        return silenceThreshold;
    }

    public void setSilenceThreshold(double segSilThr) {
        this.silenceThreshold = segSilThr;
    }

    public boolean isRecursion() {
        return recursion;
    }

    public void setRecursion(boolean segRecursion) {
        this.recursion = segRecursion;
    }

    public void printMethod() {
        System.out.print("info[ParameterSegmentation] \t --sMethod \t seg similarity [");
        System.out.print(SegMethodString[SegmentationMethod.SEG_BIC.ordinal()] + "," + SegMethodString[SegmentationMethod.SEG_GLR.ordinal()] + ",");
        System.out.print(SegMethodString[SegmentationMethod.SEG_KL2.ordinal()] + "," + SegMethodString[SegmentationMethod.SEG_GD.ordinal()] + ",");
        System.out.print(SegMethodString[SegmentationMethod.SEG_H2.ordinal()] + "] = ");
        System.out.println(SegMethodString[getMethod().ordinal()] + "(" + getMethod().ordinal() + ")");

    }

    public void printRecursion() {
        System.out.print("info[ParameterSegmentation] \t --sRecursion \t segmentation make by a recursion fonction = ");
        System.out.println(isRecursion());

    }

    public void printModelWindowSize() {
        System.out.print("info[ParameterSegmentation] \t --sModelWindowSize \t seg 1/2 window size (in features) = ");
        System.out.println(getModelWindowSize());

    }

    public void printMinimimWindowSize() {
        System.out.print("info[ParameterSegmentation] \t --sMinimumWindowSize \t seg min size segment (in features) = ");
        System.out.println(getMinimimWindowSize());

    }

    public void printThreshold() {
        System.out.print("info[ParameterSegmentation] \t --sThr \t seg threshold = ");
        System.out.println(getThreshold());

    }

    public void printSilenceThreshold() {
        System.out.println("info[ParameterSegmentation] \t --sSilenceThr threshold = " + getSilenceThreshold());

    }

    // Type of distance for segmentation.
    public enum SegmentationMethod {
        SEG_BIC, SEG_GLR, SEG_KL2, SEG_GD, SEG_H2
    }
}
