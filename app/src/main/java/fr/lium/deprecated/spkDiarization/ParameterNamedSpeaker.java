/**
 * <p>
 * ParameterNamedSpeaker
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
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

package fr.lium.deprecated.spkDiarization;

import java.util.ArrayList;

import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterInterface;
import gnu.getopt.LongOpt;

/**
 * @deprecated
 */
public class ParameterNamedSpeaker implements ParameterInterface {
    public static int ReferenceThreshold = -1;
    public static int ReferenceFirstnameList = -1;
    public static int ReferenceWordDictonaryMask = -1;
    public static int ReferenceSCTMask = -1;
    public static int ReferenceTargetList = -1;
    public static int ReferenceRemoveCheckGender = -1;

    private double threshold;
    private String firstnameList;
    private String wordDictonaryMask;
    private String SCTMask;
    private String targetList;
    private boolean removeCheckGender;

    public ParameterNamedSpeaker(ArrayList<LongOpt> list, Parameter parameter) {
        setThreshold(0.0);
        setFirstnameList("%s.lst");
        setWordDictonaryMask("%s.dic");
        setSCTMask("%s.tree");
        setTargetList("%s");
        removeCheckGender = false;
        ReferenceThreshold = parameter.getNextOptionIndex();
        ReferenceFirstnameList = parameter.getNextOptionIndex();
        ReferenceWordDictonaryMask = parameter.getNextOptionIndex();
        ReferenceSCTMask = parameter.getNextOptionIndex();
        ReferenceTargetList = parameter.getNextOptionIndex();
        ReferenceRemoveCheckGender = parameter.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceThreshold) {
            setThreshold(Double.parseDouble(optarg));
            return true;
        } else if (option == ReferenceFirstnameList) {
            setFirstnameList(optarg);
            return true;
        } else if (option == ReferenceSCTMask) {
            setSCTMask(optarg);
            return true;
        } else if (option == ReferenceWordDictonaryMask) {
            setWordDictonaryMask(optarg);
            return true;
        } else if (option == ReferenceTargetList) {
            setTargetList(optarg);
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("nThr", 1, null, ReferenceThreshold));
        list.add(new LongOpt("nFirstNameList", 1, null, ReferenceFirstnameList));
        list.add(new LongOpt("nSCTMask", 1, null, ReferenceSCTMask));
        list.add(new LongOpt("nTargetList", 1, null, ReferenceTargetList));
        list.add(new LongOpt("nRemoveCheckGender", 0, null, ReferenceRemoveCheckGender));
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double namedNEThr) {
        threshold = namedNEThr;
    }

    public String getFirstnameList() {
        return firstnameList;
    }

    public void setFirstnameList(String firstnameList) {
        this.firstnameList = firstnameList;
    }

    public String getWordDictonaryMask() {
        return wordDictonaryMask;
    }

    public void setWordDictonaryMask(String wordDictonaryMask) {
        this.wordDictonaryMask = wordDictonaryMask;
    }

    public String getSCTMask() {
        return SCTMask;
    }

    public void setSCTMask(String mask) {
        SCTMask = mask;
    }

    public String getTargetList() {
        return targetList;
    }

    public void setTargetList(String targetList) {
        this.targetList = targetList;
    }

    public boolean getRemoveCheckGender() {
        return removeCheckGender;
    }

    public void setRemoveCheckGender(boolean removeCheckGender) {
        this.removeCheckGender = removeCheckGender;
    }

    public void printThreshold() {
        System.out.print("info[ParameterNamedSpeaker] \t --nThr \t named speaker threshold = ");
        System.out.println(getThreshold());
    }

    public void printFirstnameList() {
        System.out.print("info[ParameterNamedSpeaker] \t --nFirstnameList \t list of firstname with gender information = ");
        System.out.println(getFirstnameList());
    }

    public void printWordDictonaryMask() {
        System.out.print("info[ParameterNamedSpeaker] \t --nWordDictonaryMask \t dictonary, decoding word list ");
        System.out.println(getWordDictonaryMask());
    }

    public void printSCTMask() {
        System.out.print("info[ParameterNamedSpeaker] \t --nSCTMask \t Semantic Classification Tree Mask ");
        System.out.println(getSCTMask());
    }

    public void printTargetList() {
        System.out.print("info[ParameterNamedSpeaker] \t --nTargetList \t list of target speakers to identify = ");
        System.out.println(getTargetList());
    }

    public void printRemoveCheckGender() {
        System.out.print("info[ParameterNamedSpeaker] \t --nRemoveCheckGender \t remove the gender check = ");
        System.out.println(getTargetList());
    }
}