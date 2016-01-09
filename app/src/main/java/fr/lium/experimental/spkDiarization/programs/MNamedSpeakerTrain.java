package fr.lium.experimental.spkDiarization.programs;

import java.util.ArrayList;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.Turn;
import fr.lium.experimental.spkDiarization.libClusteringData.turnRepresentation.TurnSet;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.TargetNameMap;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.parameter.Parameter;

public class MNamedSpeakerTrain {
    static boolean useSpeakerList;
    static Parameter param;
    static TargetNameMap nameAndGenderMap;
    static boolean trace = true;

    /**
     * Print the available options.
     *
     * @param param is all the parameters
     * @param prog  name of this program
     */
    public static void info(Parameter param, String prog) {
        if (param.help) {
            System.out.println("info[info] \t ------------------------------------------------------ ");
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterSegmentationInputFile.print(); // sInMask
            param.parameterSegmentationOutputFile.print(); // sOutMask
            param.printSeparator();
            param.parameterNamedSpeaker.print();
        }
    }

    /**
     * Test the SCT over each segment containing a linkSet and an entity. The result of the SCT (speaker name and probability) are stored in the clusters of the
     * previous, current or next turn.
     *
     * @param clusters
     * @throws CloneNotSupportedException
     * @throws DiarizationException       TODO manager open and close speaker list
     */
    public static ArrayList<String> prepareCorpus(ClusterSet clusters, TargetNameMap targetSpeakerNameMap) throws CloneNotSupportedException, DiarizationException {
        TurnSet turns = clusters.getTurns();
        ArrayList<String> list = new ArrayList<String>();
        boolean isCloseListCheck = param.parameterNamedSpeaker.isCloseListCheck();
        for (int i = 0; i < turns.size(); i++) {
            Turn currentTurn = turns.get(i);
            LinkSet linkSet = currentTurn.getCollapsedLinkSet();
            boolean startTurn = true;
            boolean endTurn = true;
            SpeakerNameUtils.makeLinkSetForSCT(linkSet, startTurn, endTurn);
            for (int index = 0; index < linkSet.size(); index++) {
                Link link = linkSet.getLink(index);
                if (link.haveEntity(EntitySet.TypePersonne) == true) {
                    LinkSet linkSetForTest = SpeakerNameUtils.reduceLinkSetForSCT(linkSet, index, 5, startTurn, endTurn);
                    String speakerName = link.getWord();
                    if (SpeakerNameUtils.checkSpeakerName(speakerName, isCloseListCheck, nameAndGenderMap) == true) {
                        String label = findLabel(turns, i, speakerName);
                        String value = linkSetForTest.SCTTrainInformation(currentTurn.first().getShowName(), currentTurn.first().getStart(), index);
                        list.add(value + "\n@ " + label);
                    }
                }
            }

        }
        return list;
    }

    public static String findLabel(TurnSet turns, int index, String speakerName) {
        String result = SpeakerNameUtils.OTHER + " 1";
        String next = "empty";
        if (index + 1 < turns.size()) {
            next = SpeakerNameUtils.normalizeSpeakerName(turns.get(index + 1).getCluster().getName());
        }
        String current = SpeakerNameUtils.normalizeSpeakerName(turns.get(index).getCluster().getName());

        String previous = "empty";
        if (index - 1 >= 0) {
            previous = SpeakerNameUtils.normalizeSpeakerName(turns.get(index - 1).getCluster().getName());
        }

        String spk = SpeakerNameUtils.normalizeSpeakerName(speakerName);
        if (spk.equals(current) == true) {
            result = SpeakerNameUtils.CURRENT + " 1";
        } else if (spk.equals(next) == true) {
            result = SpeakerNameUtils.NEXT + " 1";
        } else if (spk.equals(previous) == true) {
            result = SpeakerNameUtils.PREVIOUS + " 1";
        }

        int distance = Distance.levenshteinDistance(previous, spk);
        String ch = previous;
        int tmp = Distance.levenshteinDistance(current, spk);
        if (tmp < distance) {
            distance = tmp;
            ch = current;
        }
        tmp = Distance.levenshteinDistance(next, spk);
        if (tmp < distance) {
            distance = tmp;
            ch = next;
        }
        System.err.println("*** " + spk + " ~ " + ch + " / " + distance + " / " + result);

        return result;
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        try {
            param = MainTools.getParameters(args);
            info(param, "SNamedSpeaker2");
            if (param.nbShow > 0) {
                ClusterSet clusters = MainTools.readClusterSet(param);
                clusters.collapse();
                nameAndGenderMap = null;
                nameAndGenderMap = SpeakerNameUtils.loadList(param.parameterNamedSpeaker.getNameAndGenderList());

				/*nameAndGenderMap = new TargetNameMap();
				for(String name: clusters){
					Cluster cluster = clusters.getCluster(name);
					name = SpeakerNameUtils.normalizeSpeakerName(name);
					nameAndGenderMap.put(name, cluster.getGender());
				}*/

                useSpeakerList = true;
                ArrayList<String> list = prepareCorpus(clusters, nameAndGenderMap);
                MainTools.writeStringList(param, list);
            }
        } catch (DiarizationException e) {
            System.out.println("error \t exception " + e.getMessage());
        }
    }


}
