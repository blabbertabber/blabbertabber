package fr.lium.experimental.spkDiarization.programs;

import java.io.IOException;
import java.util.ArrayList;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterEHMM;
import fr.lium.spkDiarization.programs.MDecode;
import fr.lium.spkDiarization.programs.MTrainInit;
import fr.lium.spkDiarization.programs.MTrainMAP;

public class MEHMM {
    static final int likelihoodWindowSize = 300;
    static Segment maximumSegment;
    static int maximumFeatureIndex;

    public static void trainMAPSpeakers(FeatureSet features, ClusterSet clusters, ArrayList<GMM> speakersList, Parameter param) throws Exception {
        ArrayList<GMM> initVect = new ArrayList<GMM>();
        speakersList.clear();
        MTrainInit.make(features, clusters, initVect, param);
        MTrainMAP.make(features, clusters, initVect, speakersList, param);
    }

    public static ClusterSet makeNSpk(FeatureSet features, ClusterSet clusters, GMM ubm, Parameter param) throws Exception {
        /*Integer nbOfSpeaker = 0;
		ArrayList<GMM> speakersList = new ArrayList<GMM>();
		ClusterSet current = makeInitialClustering(clusters, nbOfSpeaker);

		trainSpeakers(features, current, speakersList, param);
		ClusterSet previous = new ClusterSet();

		while(){
			getMaximumWindowzedLikelihood(features, clusters.getCluster("S0"), ubm);
			setNewSpeaker(features, current, ubm, "S"+nbOfSpeaker);
			nbOfSpeaker++;

			while(current.equals(previous) == false) {
				previous = current;
				trainSpeakers(features, previous, speakersList, param);
				current = 	MDecode.make(features, previous, speakersList, param);
			}
		}
		
		return current;*/
        return clusters;
    }

    public static ClusterSet make2Spk(FeatureSet features, ClusterSet clusters, GMM ubm, Parameter param) throws Exception {
        Integer nbOfSpeaker = 0;
        ArrayList<GMM> speakersList = new ArrayList<GMM>();
        ClusterSet current = makeInitialClustering(clusters, nbOfSpeaker);

        trainMAPSpeakers(features, current, speakersList, param);

        GMM gmmS0 = speakersList.get(0);

        ClusterSet previous = new ClusterSet();

        getMaximumWindowzedLikelihood(features, current.getCluster("S" + nbOfSpeaker), ubm, gmmS0);
        nbOfSpeaker++;
        setNewSpeaker(features, current, ubm, "S" + nbOfSpeaker);

        while (current.equals(previous) == false) {
            previous = current;
            trainMAPSpeakers(features, previous, speakersList, param);
            current = MDecode.make(features, previous, speakersList, param);
        }

        return current;
    }

    public static ClusterSet makeReSeg(FeatureSet features, ClusterSet clusters, GMM ubm, Parameter param) throws Exception {
        ArrayList<GMM> speakersList = new ArrayList<GMM>();
        ClusterSet current = clusters;

        trainMAPSpeakers(features, current, speakersList, param);

        ClusterSet previous = new ClusterSet();

        while (current.equals(previous) == false) {
            previous = current;
            trainMAPSpeakers(features, previous, speakersList, param);
            current = MDecode.make(features, previous, speakersList, param);
        }

        return current;
    }

    public static ClusterSet makeInitialClustering(ClusterSet clusters, Integer nbOfSpeaker) {
        ClusterSet initialClusters = new ClusterSet();
        Cluster initalCluster = initialClusters.createANewCluster("S" + nbOfSpeaker);
        nbOfSpeaker++;
        for (Cluster cluster : clusters.clusterSetValue()) {
            for (Segment segment : cluster) {
                Segment initalSegment = (Segment) segment.clone();
                initalCluster.addSegment(initalSegment);
            }
        }
        return initialClusters;
    }

    public static int getMaximumWindowzedLikelihood(FeatureSet features, Cluster cluster, GMM ubm, GMM gmmS0) throws DiarizationException, IOException {
        Double maximumLogLikelihood = -1.0 * Double.MAX_VALUE;
        maximumFeatureIndex = -1;
        maximumSegment = null;
        for (Segment segment : cluster) {
            features.setCurrentShow(segment.getShowName());
            int start = segment.getStart();
            int last = segment.getLast() - likelihoodWindowSize + 1;
            for (int i = start; i <= last; i++) {
                ubm.initScoreAccumulator();
                gmmS0.initScoreAccumulator();
                for (int j = 0; j < likelihoodWindowSize; j++) {
                    ubm.getAndAccumulateLikelihood(features, i + j);
                    gmmS0.getAndAccumulateLikelihood(features, i + j);

                }
                double ubmLlh = ubm.getMeanLogLikelihood();
                double gmmS0Llh = gmmS0.getMeanLogLikelihood();
                double llr = gmmS0Llh - ubmLlh;
                ubm.resetScoreAccumulator();
                gmmS0.resetScoreAccumulator();
                if (llr > maximumLogLikelihood) {
                    System.err.println("index = " + i + " llr = " + llr);
                    maximumLogLikelihood = llr;
                    maximumFeatureIndex = i;
                    maximumSegment = segment;
                }
            }
        }
        return maximumFeatureIndex;
    }

    public static void setNewSpeaker(FeatureSet features, ClusterSet clusters, GMM ubm, String name) throws DiarizationException, IOException {
        Cluster clusterS0 = clusters.getCluster(maximumSegment.getClusterName());
        clusterS0.removeSegment(maximumSegment);

        Cluster clusterS1 = clusters.createANewCluster(name);

        if (maximumFeatureIndex != maximumSegment.getStart()) {
            Segment begin = (Segment) maximumSegment.clone();
            begin.setLength(maximumFeatureIndex - begin.getStart());
            clusterS0.addSegment(begin);
        }

        Segment mid = (Segment) maximumSegment.clone();
        mid.setStart(maximumFeatureIndex);
        mid.setLength(likelihoodWindowSize);
        clusterS1.addSegment(mid);

        if (maximumFeatureIndex + likelihoodWindowSize < maximumSegment.getLast()) {
            Segment end = (Segment) maximumSegment.clone();
            end.setStart(maximumFeatureIndex + likelihoodWindowSize);
            end.setLength(maximumSegment.getLast() - end.getStart());
            clusterS0.addSegment(end);
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            Parameter param = MainTools.getParameters(args);
            info(param, "MEHMM");
            if (param.nbShow > 0) {
                // Clusters
                ClusterSet clusters = MainTools.readClusterSet(param);
                // clusters.debug();

                // Features
                FeatureSet features = MainTools.readFeatureSet(param, clusters);

                // Compute Model
                ArrayList<GMM> initVect = MainTools.readGMMContainer(param);

                ClusterSet clustersRes = null;
                if (param.parameterEHMM.getTypeEHMM() == ParameterEHMM.TypeEHMMList.ReSeg.ordinal()) {
                    clustersRes = makeReSeg(features, clusters, initVect.get(0), param);
                } else if (param.parameterEHMM.getTypeEHMM() == ParameterEHMM.TypeEHMMList.twoSpk.ordinal()) {
                    clustersRes = make2Spk(features, clusters, initVect.get(0), param);
                } else {
                    throw new DiarizationException("ERROR: not implemented");
                }

                // Seg outPut
                MainTools.writeClusterSet(param, clustersRes, false);
            }
        } catch (DiarizationException e) {
            System.out.println("error \t exception " + e.getMessage());
        }
    }

    public static void info(Parameter param, String prog) {
        if (param.help) {
            param.printSeparator2();
            System.out.println("info[program] \t name = " + prog);
            param.printSeparator();
            param.printShow();

            param.parameterInputFeature.print();
            param.printSeparator();
            param.parameterSegmentationInputFile.print();
            param.parameterSegmentationOutputFile.print();
            param.printSeparator();
            param.parameterModelSetInputFile.printMask(); // tInMask
            param.parameterTopGaussian.printTopGaussian(); // sTop
            param.printSeparator();
            param.parameterDecoder.print();
            param.printSeparator();
            param.parameterEM.print(); // emCtl
            param.parameterMAP.print(); // mapCtrl
            param.parameterVarianceControl.printVarianceControl(); // varCtrl
            param.printSeparator();
            param.parameterEHMM.print();
        }
    }

}
