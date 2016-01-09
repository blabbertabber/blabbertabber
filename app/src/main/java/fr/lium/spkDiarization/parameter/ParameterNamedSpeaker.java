package fr.lium.spkDiarization.parameter;

import java.util.ArrayList;

import gnu.getopt.LongOpt;

public class ParameterNamedSpeaker implements ParameterInterface {
    public static int ReferenceThreshold = -1;
    public static int ReferenceNameAndGenderList = -1;
    public static int ReferenceLIASCTMask = -1;
    public static int ReferenceDontCheckGender = -1;
    public static int ReferencCloseListCheck = -1;
    public static int ReferenceBeliefFunctions = -1;

    private double threshold;
    private String nameAndGenderList;
    private String SCTMask;
    private boolean dontCheckGender;
    private boolean closeListCheck;
    private boolean beliefFunctions;

    public ParameterNamedSpeaker(ArrayList<LongOpt> list, Parameter parameter) {
        setThreshold(0.0);
        setNameAndGenderList("%s.lst");
        setSCTMask("%s.tree");
        dontCheckGender = false;
        closeListCheck = false;
        beliefFunctions = false;
        ReferenceThreshold = parameter.getNextOptionIndex();
        ReferenceNameAndGenderList = parameter.getNextOptionIndex();
        ReferenceLIASCTMask = parameter.getNextOptionIndex();
        ReferenceDontCheckGender = parameter.getNextOptionIndex();
        ReferencCloseListCheck = parameter.getNextOptionIndex();
        ReferenceBeliefFunctions = parameter.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceThreshold) {
            setThreshold(Double.parseDouble(optarg));
            return true;
        } else if (option == ReferenceNameAndGenderList) {
            setNameAndGenderList(optarg);
            return true;
        } else if (option == ReferenceDontCheckGender) {
            setDontCheckGender(true);
            return true;
        } else if (option == ReferencCloseListCheck) {
            setCloseListCheck(true);
            return true;
        } else if (option == ReferenceLIASCTMask) {
            setSCTMask(optarg);
            return true;
        } else if (option == ReferenceBeliefFunctions) {
            setBeliefFunctions(true);
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("nThr", 1, null, ReferenceThreshold));
        list.add(new LongOpt("nNameAndGenderList", 1, null, ReferenceNameAndGenderList));
        list.add(new LongOpt("nLIA_SCTMask", 1, null, ReferenceLIASCTMask));
        list.add(new LongOpt("nDontCheckGender", 0, null, ReferenceDontCheckGender));
        list.add(new LongOpt("nCloseListCheck", 0, null, ReferencCloseListCheck));
        list.add(new LongOpt("nBeliefFunctions", 0, null, ReferenceBeliefFunctions));
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double namedNEThr) {
        threshold = namedNEThr;
    }

    public String getNameAndGenderList() {
        return nameAndGenderList;
    }

    public void setNameAndGenderList(String ch) {
        this.nameAndGenderList = ch;
    }

    public String getSCTMask() {
        return SCTMask;
    }

    public void setSCTMask(String mask) {
        SCTMask = mask;
    }

    public boolean isBeliefFunctions() {
        return beliefFunctions;
    }

    public void setBeliefFunctions(boolean beliefFunctions) {
        this.beliefFunctions = beliefFunctions;
    }

    public boolean isDontCheckGender() {
        return dontCheckGender;
    }

    public void setDontCheckGender(boolean removeCheckGender) {
        this.dontCheckGender = removeCheckGender;
    }

    public void printThreshold() {
        System.out.print("info[ParameterNamedSpeaker] \t --nThr \t named speaker threshold = ");
        System.out.println(getThreshold());
    }

    public void printNameAndGenderList() {
        System.out.print("info[ParameterNamedSpeaker] \t --nNameAndGenderList \t list of name (full name or firstname) with gender information = ");
        System.out.println(getNameAndGenderList());
    }

    public void printSCTMask() {
        System.out.print("info[ParameterNamedSpeaker] \t --nLIA_SCTMask \t Semantic Classification Tree Mask ");
        System.out.println(getSCTMask());
    }

    public void printCloseListCheck() {
        System.out.print("info[ParameterNamedSpeaker] \t --nCloseListCheck \t check the present of the speaker in --nNameAndGenderList list = ");
        System.out.println(isCloseListCheck());
    }

    public void printDontCheckGender() {
        System.out.print("info[ParameterNamedSpeaker] \t --nDontCheckGender \t remove the gender check = ");
        System.out.println(isDontCheckGender());
    }

    public void print() {
        printThreshold();
        printNameAndGenderList();
        printSCTMask();
        printCloseListCheck();
        printDontCheckGender();
    }

    /**
     * @return the closeListCheck
     */
    public boolean isCloseListCheck() {
        return closeListCheck;
    }

    /**
     * @param closeListCheck the closeListCheck to set
     */
    public void setCloseListCheck(boolean closeListCheck) {
        this.closeListCheck = closeListCheck;
    }
}
