/**
 * <p>
 * ParameterDecoder
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
 */

package fr.lium.spkDiarization.parameter;

import java.util.ArrayList;
import java.util.StringTokenizer;

import gnu.getopt.LongOpt;

public class ParameterDecoder implements ParameterInterface {
    public static String[] ViterbiDurationConstraintString = {"none", "minimal", "periodic", "fixed"};
    public static int ReferencePenality = -1;

    ;
    public static int ReferenceDurationConstraints = -1;
    public static int ReferenceComputeLLhR = -1;
    public static int ReferenceShift = -1;
    private ArrayList<Double> exitDecoderPenalty;
    private ArrayList<Double> loopDecoderPenalty;
    private ArrayList<ViterbiDurationConstraint> viterbiDurationConstraints;
    private ArrayList<Integer> viterbiDurationConstraintValues;
    private boolean computeLLhR;
    private int shift;
    public ParameterDecoder(ArrayList<LongOpt> list, Parameter param) {
        setComputeLLhR(false);
        shift = 1;
        ReferenceComputeLLhR = param.getNextOptionIndex();
        ReferenceShift = param.getNextOptionIndex();
        exitDecoderPenalty = new ArrayList<Double>();
        loopDecoderPenalty = new ArrayList<Double>();
        getExitDecoderPenalty().add(0.0);
        viterbiDurationConstraints = new ArrayList<ViterbiDurationConstraint>();
        viterbiDurationConstraints.add(ViterbiDurationConstraint.VITERBI_NO_CONSTRAINT);
        viterbiDurationConstraintValues = new ArrayList<Integer>();
        getViterbiDurationConstraintValues().add(1);
        ReferencePenality = param.getNextOptionIndex();
        ReferenceDurationConstraints = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferencePenality) {
            setDecoderPenalty(optarg);
            return true;
        } else if (option == ReferenceDurationConstraints) {
            setViterbiDurationConstraints(optarg);
            return true;
        } else if (option == ReferenceComputeLLhR) {
            setComputeLLhR(true);
            return true;
        } else if (option == ReferenceShift) {
            setShift(Integer.parseInt(optarg));
            return true;
        }
        return false;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int parseInt) {
        // TODO Auto-generated method stub
        shift = parseInt;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("dPenality", 1, null, ReferencePenality));
        list.add(new LongOpt("dDurationConstraints", 1, null, ReferenceDurationConstraints));
        list.add(new LongOpt("dComputeLLhR", 0, null, ReferenceComputeLLhR));
        list.add(new LongOpt("dShift", 1, null, ReferenceShift));
    }

    public ArrayList<Double> getExitDecoderPenalty() {
        return exitDecoderPenalty;
    }

    public ArrayList<Double> getLoopDecoderPenalty() {
        return loopDecoderPenalty;
    }

    public void setDecoderPenalty(String ch) {
        //System.err.println("*** setDecoderPenalty:"+ch);
        exitDecoderPenalty.clear();
        loopDecoderPenalty.clear();
        String[] listKey = ch.split(",");
        for (String key : listKey) {
            //System.err.println("*** setDecoderPenalty: key="+key);
            String[] listKey2 = key.split(":");
            getExitDecoderPenalty().add(Double.parseDouble(listKey2[0]));
            //System.err.println("*** setDecoderPenalty: key[0]="+listKey2[0]);
            if (listKey2.length > 1) {
                getLoopDecoderPenalty().add(Double.parseDouble(listKey2[1]));
                //System.err.println("*** setDecoderPenalty: key[1]="+listKey2[1]);
            } else {
                getLoopDecoderPenalty().add(0.0);
            }
        }

/*		ArrayList<String> tmpCh = new ArrayList<String>();
		String limite = ",";
		StringTokenizer stok = new StringTokenizer(ch, limite);
		while (stok.hasMoreTokens()) {
			tmpCh.add(stok.nextToken());
		}
		if (tmpCh.size() > 0) {
			getDecodePenalty().clear();
			for (int i = 0; i < tmpCh.size(); i++) {
				getDecodePenalty().add(Double.parseDouble((tmpCh.get(i))));
			}
		}
*/
    }

    public ArrayList<ViterbiDurationConstraint> getViterbiDurationConstraints() {
        return viterbiDurationConstraints;
    }

    public void setViterbiDurationConstraints(String ch) {
        ArrayList<String> tmpCh = new ArrayList<String>();
        String limite = ",";
        StringTokenizer stok = new StringTokenizer(ch, limite);
        while (stok.hasMoreTokens()) {
            tmpCh.add(stok.nextToken());
        }
        if (tmpCh.size() > 0) {
            viterbiDurationConstraints.clear();
            setViterbiDurationConstraintValues(new ArrayList<Integer>());
            int durValue;
            for (int i = 0; i < tmpCh.size(); i++) {
                if (tmpCh.get(i).equals(ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_MINIMAL_DURATION.ordinal()])) {
                    getViterbiDurationConstraints().add(ViterbiDurationConstraint.VITERBI_MINIMAL_DURATION);
                    durValue = Integer.parseInt(tmpCh.get(++i));
                } else if (tmpCh.get(i).equals(ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_PERIODIC_DURATION.ordinal()])) {
                    getViterbiDurationConstraints().add(ViterbiDurationConstraint.VITERBI_PERIODIC_DURATION);
                    durValue = Integer.parseInt(tmpCh.get(++i));
                } else if (tmpCh.get(i).equals(ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_FIXED_DURATION.ordinal()])) {
                    getViterbiDurationConstraints().add(ViterbiDurationConstraint.VITERBI_FIXED_DURATION);
                    durValue = Integer.parseInt(tmpCh.get(++i));
                } else {
                    getViterbiDurationConstraints().add(ViterbiDurationConstraint.VITERBI_NO_CONSTRAINT);
                    durValue = 1;
                }
                getViterbiDurationConstraintValues().add(durValue);
            }
        }
    }

    public ArrayList<Integer> getViterbiDurationConstraintValues() {
        return viterbiDurationConstraintValues;
    }

    public void setViterbiDurationConstraintValues(ArrayList<Integer> viterbiDurationConstraintValues) {
        this.viterbiDurationConstraintValues = viterbiDurationConstraintValues;
    }

    public void printPenality() {
        System.out.print("info[ParameterDecoder] \t --dPenalty \t model penalties = ");
        for (int i = 0; i < getExitDecoderPenalty().size() - 1; i++) {
            System.out.print(getExitDecoderPenalty().get(i) + ":");
            System.out.print(getLoopDecoderPenalty().get(i) + ", ");
        }
        System.out.println(getExitDecoderPenalty().get(getExitDecoderPenalty().size() - 1));

    }

    public void printDurationConstraints() {
        System.out.print("info[ParameterDecoder] \t --dDurationConstraints \t duration constraints during decoding");
        System.out.print(" [" + ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_NO_CONSTRAINT.ordinal()] + "|");
        System.out.print("(" + ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_MINIMAL_DURATION.ordinal()] + "|");
        System.out.print(ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_PERIODIC_DURATION.ordinal()] + "|");
        System.out.print(ViterbiDurationConstraintString[ViterbiDurationConstraint.VITERBI_FIXED_DURATION.ordinal()] + ",value)] = ");
        for (int i = 0; i < getViterbiDurationConstraints().size() - 1; i++) {
            System.out.print(ViterbiDurationConstraintString[getViterbiDurationConstraints().get(i).ordinal()] + ",");
            if (getViterbiDurationConstraints().get(i) != ViterbiDurationConstraint.VITERBI_NO_CONSTRAINT) {
                System.out.print(getViterbiDurationConstraintValues().get(i) + ",");
            }
        }
        System.out.print(ViterbiDurationConstraintString[getViterbiDurationConstraints().get(getViterbiDurationConstraints().size() - 1).ordinal()]);
        if (getViterbiDurationConstraints().get(getViterbiDurationConstraints().size() - 1) != ViterbiDurationConstraint.VITERBI_NO_CONSTRAINT) {
            System.out.print("," + getViterbiDurationConstraintValues().get(getViterbiDurationConstraints().size() - 1));
        }
        System.out.println();

    }

    public void printComputeLLhR() {
        System.out.println("info[ParameterDecoder] \t --dComputeLLhR \t score is Log Likelihood Ratio = " + isComputeLLhR());
    }

    public void printShift() {
        System.out.println("info[ParameterDecoder] \t --dShift \t size of a step = " + getShift());
    }

    public void print() {
        printPenality();
        printDurationConstraints();
        printComputeLLhR();
        printShift();
    }

    public boolean isComputeLLhR() {
        return computeLLhR;
    }

    public void setComputeLLhR(boolean computeLLhR) {
        this.computeLLhR = computeLLhR;
    }

    // Type of duration constraint for Viterbi.
    public enum ViterbiDurationConstraint {
        VITERBI_NO_CONSTRAINT, VITERBI_MINIMAL_DURATION, VITERBI_PERIODIC_DURATION, VITERBI_FIXED_DURATION
    }
}