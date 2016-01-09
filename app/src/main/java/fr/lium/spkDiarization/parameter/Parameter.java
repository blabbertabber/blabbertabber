/**
 * <p>
 * Param
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class Parameter {
    public static int ReferenceHelp = -1;
    public static int ReferenceTrace = -1;
    public static int ReferenceShowList = -1;
    // public static String[] DecodeMethodString = { "std", "align" };

    public static Charset DefaultCharset = Charset.forName("ISO-8859-1");

    public String show; // Current show name.
    public ArrayList<String> showLst; // List of shows.
    public int nbShow; // Number of shows.
    public boolean trace;
    public boolean help;

    public ParameterInputFeature parameterInputFeature;
    public ParameterInputFeature2 parameterInputFeature2;
    public ParameterOutputFeature parameterOutputFeature;
    public ParameterSegmentation parameterSegmentation;
    public ParameterClustering parameterClustering;
    public ParameterEM parameterEM;
    public ParameterEHMM parameterEHMM;
    public ParameterMAP parameterMAP;
    public ParameterVarianceControl parameterVarianceControl;
    public ParameterScore parameterScore;
    public ParameterDecoder parameterDecoder;
    public ParameterFilter parameterFilter;
    public ParameterAdjustSegmentation parameterAdjustSegmentation;
    public ParameterTopGaussian parameterTopGaussian;
    //	public ParameterNamedSpeaker parameterNamedSpeaker;
    public ParameterNamedSpeaker parameterNamedSpeaker;
    public ParameterSegmentationSplit parameterSegmentationSplit;
    public ParameterModel parameterModel;
    public ParameterInitializationEM parameterInitializationEM;
    public ParameterSegmentationInputFile parameterSegmentationInputFile;
    public ParameterSegmentationInputFile2 parameterSegmentationInputFile2;
    public ParameterSegmentationOutputFile parameterSegmentationOutputFile;
    public ParameterSegmentationFilterFile parameterSegmentationFilterFile;
    public ParameterModelSetOutputFile parameterModelSetOutputFile;
    public ParameterModelSetInputFile parameterModelSetInputFile;
    public ParameterBNDiarization parameterDiarization;
    public int nbOptions;
    ArrayList<LongOpt> optionList;

    public Parameter() {
        nbOptions = 100;
        showLst = new ArrayList<String>();
        optionList = new ArrayList<LongOpt>();
        parameterSegmentationInputFile = new ParameterSegmentationInputFile(optionList, this);
        parameterSegmentationInputFile2 = new ParameterSegmentationInputFile2(optionList, this);
        parameterSegmentationOutputFile = new ParameterSegmentationOutputFile(optionList, this);
        parameterSegmentationFilterFile = new ParameterSegmentationFilterFile(optionList, this);
        parameterEM = new ParameterEM(optionList, this);
        parameterEHMM = new ParameterEHMM(optionList, this);
        parameterVarianceControl = new ParameterVarianceControl(optionList, this);
        parameterMAP = new ParameterMAP(optionList, this);
        parameterClustering = new ParameterClustering(optionList, this);
        parameterSegmentation = new ParameterSegmentation(optionList, this);
        parameterScore = new ParameterScore(optionList, this);
        parameterDecoder = new ParameterDecoder(optionList, this);
        parameterFilter = new ParameterFilter(optionList, this);
        parameterAdjustSegmentation = new ParameterAdjustSegmentation(optionList, this);
        parameterTopGaussian = new ParameterTopGaussian(optionList, this);
//		parameterNamedSpeaker = new ParameterNamedSpeaker(optionList, this);
        parameterNamedSpeaker = new ParameterNamedSpeaker(optionList, this);
        parameterSegmentationSplit = new ParameterSegmentationSplit(optionList, this);
        parameterModel = new ParameterModel(optionList, this);
        parameterInitializationEM = new ParameterInitializationEM(optionList, this);
        parameterInputFeature = new ParameterInputFeature(optionList, this);
        parameterInputFeature2 = new ParameterInputFeature2(optionList, this);
        parameterOutputFeature = new ParameterOutputFeature(optionList, this);
        parameterModelSetOutputFile = new ParameterModelSetOutputFile(optionList, this);
        parameterModelSetInputFile = new ParameterModelSetInputFile(optionList, this);
        parameterDiarization = new ParameterBNDiarization(optionList, this);
        ReferenceHelp = getNextOptionIndex();
        ReferenceTrace = getNextOptionIndex();
        ReferenceShowList = getNextOptionIndex();
        addOptions(optionList);
        show = "";
        nbShow = 0;
        trace = false;
        help = false;
    }

    public int getNextOptionIndex() {
        return nbOptions++;
    }

    public void addOptions(ArrayList<LongOpt> list) {
        list.add(new LongOpt("showLst", 1, null, ReferenceShowList));
        list.add(new LongOpt("help", 0, null, ReferenceHelp));
        list.add(new LongOpt("trace", 0, null, ReferenceTrace));
        list.add(new LongOpt("0", 0, null, 0));
    }

    public void printCmdLine(String[] args) {
        printSeparator2();
        System.out.print("cmdLine:");
        for (String arg : args) {
            System.out.print(" " + arg);
        }
        System.out.println();
        printSeparator2();
    }

    public void readParameters(String args[]) {
        int c;

// for(int i =0; i < optionList.size(); i++){
// System.err.println("name:"+optionList.get(i).getName()+" idx="+optionList.get(i).getVal());
// }
        LongOpt[] longopts = new LongOpt[optionList.size()];
        optionList.toArray(longopts);
        Getopt g = new Getopt("testprog", args, "-", longopts);
        String optarg;
        while ((c = g.getopt()) != -1) {
            optarg = g.getOptarg();
// System.err.println("*** c:"+c+" val="+optarg);

            if (parameterInputFeature.readParam(c, optarg)) {
                continue;
            }
            if (parameterInputFeature2.readParam(c, optarg)) {
                continue;
            }
            if (parameterOutputFeature.readParam(c, optarg)) {
                continue;
            }
/*			if (parameterNamedSpeaker.readParam(c, optarg)) {
                continue;
			}*/
            if (parameterNamedSpeaker.readParam(c, optarg)) {
                continue;
            }
            if (parameterClustering.readParam(c, optarg)) {
                continue;
            }
            if (parameterSegmentation.readParam(c, optarg)) {
                continue;
            }
            if (parameterScore.readParam(c, optarg)) {
                continue;
            }
            if (parameterSegmentationFilterFile.readParam(c, optarg)) {
                continue;
            }
            if (parameterSegmentationInputFile.readParam(c, optarg)) {
                continue;
            }
            if (parameterSegmentationInputFile2.readParam(c, optarg)) {
                continue;
            }
            if (parameterSegmentationOutputFile.readParam(c, optarg)) {
                continue;
            }
            if (parameterFilter.readParam(c, optarg)) {
                continue;
            }
            if (parameterModel.readParam(c, optarg)) {
                continue;
            }
            if (parameterInitializationEM.readParam(c, optarg)) {
                continue;
            }
            if (parameterEM.readParam(c, optarg)) {
                continue;
            }
            if (parameterMAP.readParam(c, optarg)) {
                continue;
            }
            if (parameterEHMM.readParam(c, optarg)) {
                continue;
            }
            if (parameterSegmentationSplit.readParam(c, optarg)) {
                continue;
            }
            if (parameterDiarization.readParam(c, optarg)) {
                continue;
            }
            if (parameterDecoder.readParam(c, optarg)) {
                continue;
            }
            if (parameterModelSetOutputFile.readParam(c, optarg)) {
                continue;
            }
            if (parameterModelSetInputFile.readParam(c, optarg)) {
                continue;
            }
            if (parameterVarianceControl.readParam(c, optarg)) {
                continue;
            }
            if (parameterTopGaussian.readParam(c, optarg)) {
                continue;
            }
            if (parameterAdjustSegmentation.readParam(c, optarg)) {
                continue;
            }
            if (c == ReferenceShowList) {
                System.err.println("ReferenceShowList:" + show);
            }
            if (c == 1) {
                show = optarg;
                showLst.add(optarg);
                nbShow++;
                System.out.println("show = " + show);
            } else if (c == ReferenceHelp) {
                help = true;
            } else if (c == ReferenceTrace) {
                trace = true;
            }
        }
    }

    public void printSeparator2() {
        System.out.println("info[info] \t ====================================================== ");
    }

    public void printSeparator() {
        System.out.println("info[info] \t ------------------------------------------------------ ");
    }

    public void printShow() {
        Iterator<String> its = this.showLst.iterator();
        System.out.println("info[show] \t [options] show");
        while (its.hasNext()) {
            System.out.println("info[show] \t show = " + its.next());
        }
    }

}