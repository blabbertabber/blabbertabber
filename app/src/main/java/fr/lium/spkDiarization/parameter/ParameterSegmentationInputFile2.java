package fr.lium.spkDiarization.parameter;

import java.util.ArrayList;

import gnu.getopt.LongOpt;

public class ParameterSegmentationInputFile2 extends ParameterSegmentationFile {
    public static int ReferenceMask = -1;
    public static int ReferenceEncodingFormat = -1;

    public ParameterSegmentationInputFile2(ArrayList<LongOpt> list, Parameter param) {
        super();
        type = "Input2";
        ReferenceMask = param.getNextOptionIndex();
        ReferenceEncodingFormat = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceMask) {
            setMask(optarg);
            return true;
        } else if (option == ReferenceEncodingFormat) {
            setFormatEncoding(optarg);
            return true;
        }
        return false;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("s" + type + "Mask", 1, null, ReferenceMask));
        list.add(new LongOpt("s" + type + "Format", 1, null, ReferenceEncodingFormat));
    }

}
