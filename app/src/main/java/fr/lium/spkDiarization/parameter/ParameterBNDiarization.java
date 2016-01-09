package fr.lium.spkDiarization.parameter;

import java.util.ArrayList;

import gnu.getopt.LongOpt;

public class ParameterBNDiarization implements ParameterInterface {
    public static String[] SystemString = {"baseline", "10s"};
    public int ReferenceDiarization = -1;
    public int ReferenceAudioFileMask = -1;
    public int ReferenceCEClustering = -1;
    public int ReferenceTuning = -1;
    public int ReferenceSaveAllStep = -1;
    public int ReferenceLoadInputSegmentation = -1;
    public int ReferenceThresholds = -1;
    private boolean CEClustering;
    private boolean tuning;
    private boolean saveAllStep;
    private boolean loadInputSegmentation;
    private String system;
    private String[] thresholdsKey = {"l", "h", "d", "c"};
    private double[] thresholds = {2.0, 3.0, 250.0, 1.7};
    private double[] thresholdsMax = {2.0, 3.0, 250.0, 5};

    public ParameterBNDiarization(ArrayList<LongOpt> list, Parameter param) {

        CEClustering = false;
        saveAllStep = false;
        loadInputSegmentation = false;
        tuning = false;
        system = SystemString[0];
        ReferenceDiarization = param.getNextOptionIndex();
        ReferenceAudioFileMask = param.getNextOptionIndex();
        ReferenceCEClustering = param.getNextOptionIndex();
        ReferenceTuning = param.getNextOptionIndex();
        ReferenceSaveAllStep = param.getNextOptionIndex();
        ReferenceLoadInputSegmentation = param.getNextOptionIndex();
        ReferenceThresholds = param.getNextOptionIndex();
        addOptions(list);
    }

    public boolean readParam(int option, String optarg) {
        if (option == ReferenceCEClustering) {
            setCEClustering(true);
            return true;
        } else if (option == ReferenceDiarization) {
            setSystem(optarg);
            return true;
        } else if (option == ReferenceTuning) {
            setTuning(true);
            return true;
        } else if (option == ReferenceSaveAllStep) {
            setSaveAllStep(true);
            return true;
        } else if (option == ReferenceThresholds) {
            setThresholds(optarg);
            return true;
        } else if (option == ReferenceLoadInputSegmentation) {
            setLoadInputSegmentation(true);
            return true;
        }
        return false;
    }

    public double getThreshold(String key) {
        for (int i = 0; i < thresholdsKey.length; i++) {
            if (key.equals(thresholdsKey[i])) {
                return thresholds[i];
            }
        }
        return Double.NaN;
    }

    public double getMaxThreshold(String key) {
        for (int i = 0; i < thresholdsKey.length; i++) {
            if (key.equals(thresholdsKey[i])) {
                return thresholdsMax[i];
            }
        }
        return Double.NaN;
    }

    private void setThresholds(String optarg) {
        String[] tab = optarg.split(",");
        for (int i = 0; i < thresholds.length; i++) {
            if (i < tab.length) {
                double min = Double.NaN;
                double max = Double.NaN;
                if (tab[i].contains(":")) {
                    String minmax[] = tab[i].split(":");
                    min = Double.valueOf(minmax[0]);
                    max = Double.valueOf(minmax[1]);
                } else {
                    min = Double.valueOf(tab[i]);
                    max = min;
                }
                thresholds[i] = min;
                thresholdsMax[i] = max;
            }
        }
    }

    public boolean isTuning() {
        return tuning;
    }

    private void setTuning(boolean value) {
        tuning = value;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("system", 1, null, ReferenceDiarization));
        list.add(new LongOpt("audioFile", 1, null, ReferenceAudioFileMask));
        list.add(new LongOpt("thresholds", 1, null, ReferenceThresholds));
        list.add(new LongOpt("doCEClustering", 0, null, ReferenceCEClustering));
        list.add(new LongOpt("doTuning", 0, null, ReferenceTuning));
        list.add(new LongOpt("saveAllStep", 0, null, ReferenceSaveAllStep));
        list.add(new LongOpt("loadInputSegmentation", 0, null, ReferenceLoadInputSegmentation));
    }

    public boolean isCEClustering() {
        return CEClustering;
    }

    public void setCEClustering(boolean cEClustering) {
        CEClustering = cEClustering;
    }

    public boolean isLoadInputSegmentation() {
        return loadInputSegmentation;
    }

    private void setLoadInputSegmentation(boolean b) {
        loadInputSegmentation = b;
    }

    public void printCEClustering() {
        System.out.println("info[ParameterSystem] \t --doCEClustering = " + isCEClustering());
    }

    public void printTresholds() {
        String ch = "";
        int i = 0;
        if (thresholds[i] == thresholdsMax[i]) {
            ch = thresholdsKey[i] + "=" + Double.toString(thresholds[i]);
        } else {
            ch = thresholdsKey[i] + "=" + Double.toString(thresholds[i]) + ":" + Double.toString(thresholdsMax[i]);
        }
        for (i = 1; i < thresholds.length; i++) {
            if (thresholds[i] == thresholdsMax[i]) {
                ch = ch + ", " + thresholdsKey[i] + "=" + Double.toString(thresholds[i]);
            } else {
                ch = ch + ", " + thresholdsKey[i] + "=" + Double.toString(thresholds[i]) + ":" + Double.toString(thresholdsMax[i]);
            }
        }
        System.out.println("info[ParameterSystem] \t --thresholds = " + ch);
    }

    public void printTuning() {
        System.out.println("info[ParameterSystem] \t --doTuning = " + isTuning());
    }

    public void printSystem() {
        System.out.println("info[ParameterSystem] \t --system [" + SystemString[0] + "," + SystemString[1] + "]= " + getSystem());
    }

    public void printSaveAllStep() {
        System.out.println("info[ParameterSystem] \t --saveAllStep = " + isSaveAllStep());
    }

    public void printLoadInputSegmentation() {
        System.out.println("info[ParameterSystem] \t --loadInputSegmentation = " + isLoadInputSegmentation());
    }

    public void print() {
        printSystem();
        printCEClustering();
        printTuning();
        printTresholds();
        printSaveAllStep();
        printLoadInputSegmentation();
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = SystemString[0];
        if (system.equals(SystemString[1])) {
            this.system = system;
        }
    }

    public boolean isSaveAllStep() {
        return saveAllStep;
    }

    public void setSaveAllStep(boolean saveAllStep) {
        this.saveAllStep = saveAllStep;
    }
}
