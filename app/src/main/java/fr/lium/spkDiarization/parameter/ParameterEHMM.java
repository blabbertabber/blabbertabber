package fr.lium.spkDiarization.parameter;

import java.util.ArrayList;

import gnu.getopt.LongOpt;

public class ParameterEHMM implements ParameterInterface {
    public static String[] TypeEHMMString = {"reSeg", "2Spk", "nSpk"};
    public int ReferenceTypeEHMM = -1;

    ;
    private int typeEHMM; // Minimum of iteration of EM algorithm.

    public ParameterEHMM(ArrayList<LongOpt> list, Parameter param) {
        super();
        setTypeEHMM(TypeEHMMString[TypeEHMMList.ReSeg.ordinal()]);
        ReferenceTypeEHMM = param.getNextOptionIndex();
        addOptions(list);
    }

    @Override
    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("nbOfSpeakers", 1, null, ReferenceTypeEHMM));

    }

    @Override
    public boolean readParam(int option, String optarg) {
        if (option == ReferenceTypeEHMM) {
            setTypeEHMM(optarg);
            return true;
        }
        return false;
    }

    private void setTypeEHMM(String optarg) {
        if (optarg.equals(TypeEHMMString[TypeEHMMList.ReSeg.ordinal()])) {
            typeEHMM = TypeEHMMList.ReSeg.ordinal();
        } else if (optarg.equals(TypeEHMMString[TypeEHMMList.twoSpk.ordinal()])) {
            typeEHMM = TypeEHMMList.twoSpk.ordinal();
        } else if (optarg.equals(TypeEHMMString[TypeEHMMList.nSpk.ordinal()])) {
            typeEHMM = TypeEHMMList.nSpk.ordinal();
        }
    }

    /**
     * @return the typeEHMM
     */
    public int getTypeEHMM() {
        return typeEHMM;
    }

    /**
     * @param typeEHMM the typeEHMM to set
     */
    public void setTypeEHMM(int typeEHMM) {
        this.typeEHMM = typeEHMM;
    }

    public void print() {
        System.out.print("info[ParameterEHMM] \t --nbOfSpeakers \t number of speakers (" + TypeEHMMString[TypeEHMMList.ReSeg.ordinal()] +
                ", " + TypeEHMMString[TypeEHMMList.twoSpk.ordinal()] +
                ", " + TypeEHMMString[TypeEHMMList.nSpk.ordinal()] + ") = ");
        System.out.println(TypeEHMMString[getTypeEHMM()] + " / " + getTypeEHMM());
    }

    // Type of EHMM.
    public enum TypeEHMMList {
        ReSeg, twoSpk, nSpk
    }

}
