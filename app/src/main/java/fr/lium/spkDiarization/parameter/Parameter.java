/**
 * 
 * <p>
 * Param
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package fr.lium.spkDiarization.parameter;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cmu.sphinx.util.Utilities;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import gnu.getopt.Getopt;

/**
 * The Class Parameter.
 */
public class Parameter implements Cloneable {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(Parameter.class.getName());

	/** The Default charset. */
	public static Charset DefaultCharset = Charset.forName("ISO-8859-1");

	/** The show. */
	public String show; // Current show name.

	/** The help. */
	public Boolean help;

	/**
	 * The Class ActionHelp.
	 */
	private class ActionHelp extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String arg) {
			help = true;
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return help.toString();
		}

	}

	/** The logger level. */
	public Level loggerLevel = Level.INFO;

	/**
	 * The Class ActionLoggerLevel.
	 */
	private class ActionLoggerLevel extends LongOptAction {

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#execute(java.lang.String)
		 */
		@Override
		public void execute(String optarg) {
			SpkDiarizationLogger.setLevel(optarg);
		}

		/*
		 * (non-Javadoc)
		 * @see fr.lium.spkDiarization.parameter.LongOptAction#getValue()
		 */
		@Override
		public String getValue() {
			return loggerLevel.toString();
		}
	}

	// Video Parameters
	/** The parameter video input feature. */
	private ParameterVideoInputFeature parameterVideoInputFeature;
	// Audio Parameters
	/** The parameter input feature. */
	private ParameterAudioInputFeature parameterInputFeature;

	/** The parameter input feature2. */
	private ParameterInputFeature2 parameterInputFeature2;

	/** The parameter output feature. */
	private ParameterAudioOutputFeature parameterOutputFeature;

	/** The parameter segmentation. */
	private ParameterSegmentation parameterSegmentation;

	/** The parameter clustering. */
	private ParameterClustering parameterClustering;

	/** The parameter em. */
	private ParameterEM parameterEM;

	/** The parameter ehmm. */
	private ParameterEHMM parameterEHMM;

	/** The parameter map. */
	private ParameterMAP parameterMAP;

	/** The parameter variance control. */
	private ParameterVarianceControl parameterVarianceControl;

	/** The parameter score. */
	private ParameterScore parameterScore;

	/** The parameter decoder. */
	private ParameterDecoder parameterDecoder;

	/** The parameter filter. */
	private ParameterFilter parameterFilter;

	/** The parameter adjust segmentation. */
	private ParameterAdjustSegmentation parameterAdjustSegmentation;

	/** The parameter top gaussian. */
	private ParameterTopGaussian parameterTopGaussian;

	/** The parameter named speaker. */
	private ParameterNamedSpeaker parameterNamedSpeaker;

	/** The parameter segmentation split. */
	private ParameterSegmentationSplit parameterSegmentationSplit;

	/** The parameter model. */
	private ParameterModel parameterModel;

	/** The parameter initialization em. */
	private ParameterInitializationEM parameterInitializationEM;

	/** The parameter segmentation input file. */
	private ParameterSegmentationInputFile parameterSegmentationInputFile;

	/** The parameter segmentation input file2. */
	private ParameterSegmentationInputFile2 parameterSegmentationInputFile2;

	/** The parameter segmentation input file3. */
	private ParameterSegmentationInputFile3 parameterSegmentationInputFile3;

	/** The parameter segmentation output file. */
	private ParameterSegmentationOutputFile parameterSegmentationOutputFile;

	/** The parameter segmentation filter file. */
	private ParameterSegmentationFilterFile parameterSegmentationFilterFile;

	/** The parameter model set output file. */
	private ParameterModelSetOutputFile parameterModelSetOutputFile;

	/** The parameter model set input file. */
	private ParameterModelSetInputFile parameterModelSetInputFile;

	/** The parameter model set input file2. */
	private ParameterModelSetInputFile2 parameterModelSetInputFile2;

	/** The parameter diarization. */
	private ParameterBNDiarization parameterDiarization;

	/** The parameter normlization. */
	private ParameterIVectorNormalization parameterNormlization;

	/** The parameter ilp. */
	private ParameterILP parameterILP;

	/** The parameter total variability. */
	private ParameterTotalVariability parameterTotalVariability;
	// GetOption
	/** The option list. */
	private ArrayList<LongOptWithAction> optionList = new ArrayList<LongOptWithAction>();

	/** The local option list. */
	private ArrayList<LongOptWithAction> localOptionList = new ArrayList<LongOptWithAction>();

	/** The nb options. */
	private static int nbOptions = 100;

	/** The list of parameter object. */
	private ArrayList<ParameterBase> listOfParameterObject = new ArrayList<ParameterBase>();

	/**
	 * Gets the next option index.
	 * 
	 * @return the next option index
	 */
	public static int getNextOptionIndex() {
		return nbOptions++;
	}

	/**
	 * Instantiates a new parameter.
	 */
	public Parameter() {
		parameterSegmentationInputFile = new ParameterSegmentationInputFile(this);
		parameterSegmentationInputFile2 = new ParameterSegmentationInputFile2(this);
		parameterSegmentationInputFile3 = new ParameterSegmentationInputFile3(this);
		parameterSegmentationOutputFile = new ParameterSegmentationOutputFile(this);
		parameterSegmentationFilterFile = new ParameterSegmentationFilterFile(this);
		parameterEM = new ParameterEM(this);
		parameterEHMM = new ParameterEHMM(this);
		parameterVarianceControl = new ParameterVarianceControl(this);
		parameterMAP = new ParameterMAP(this);
		parameterClustering = new ParameterClustering(this);
		parameterSegmentation = new ParameterSegmentation(this);
		// parameterSpeechDetector = new ParameterSpeechDetector(this);
		parameterScore = new ParameterScore(this);
		parameterDecoder = new ParameterDecoder(this);
		parameterFilter = new ParameterFilter(this);
		parameterAdjustSegmentation = new ParameterAdjustSegmentation(this);
		parameterTopGaussian = new ParameterTopGaussian(this);
		parameterNamedSpeaker = new ParameterNamedSpeaker(this);
		parameterSegmentationSplit = new ParameterSegmentationSplit(this);
		parameterModel = new ParameterModel(this);
		parameterInitializationEM = new ParameterInitializationEM(this);
		parameterInputFeature = new ParameterAudioInputFeature(this);
		parameterInputFeature2 = new ParameterInputFeature2(this);
		parameterOutputFeature = new ParameterAudioOutputFeature(this);
		parameterModelSetOutputFile = new ParameterModelSetOutputFile(this);
		parameterModelSetInputFile = new ParameterModelSetInputFile(this);
		parameterModelSetInputFile2 = new ParameterModelSetInputFile2(this);
		parameterDiarization = new ParameterBNDiarization(this);
		parameterVideoInputFeature = new ParameterVideoInputFeature(this);
		parameterNormlization = new ParameterIVectorNormalization(this);
		parameterTotalVariability = new ParameterTotalVariability(this);
		parameterILP = new ParameterILP(this);

		for (Field field : Parameter.class.getDeclaredFields()) {
			// field.setAccessible(true);
			if (field.getName().startsWith("parameter") == true) {
				ParameterBase p;
				try {
					p = (ParameterBase) field.get(this);
					listOfParameterObject.add(p);
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		addOptions(new LongOptWithAction("logger", new ActionLoggerLevel(), "logger level"));
		addOptions(new LongOptWithAction("help", 0, new ActionHelp(), "help wanted"));

		show = "";
		help = false;
	}

	/**
	 * Adds the sub parameter.
	 * 
	 * @param parameterBase the parameter base
	 * @return the parameter base
	 */
	protected ParameterBase addSubParameter(ParameterBase parameterBase) {
		listOfParameterObject.add(parameterBase);
		return parameterBase;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Parameter clone() throws CloneNotSupportedException {
		Parameter parameter = (Parameter) super.clone();
		parameter.parameterInputFeature = parameterInputFeature.clone();
		parameter.parameterInputFeature2 = parameterInputFeature2.clone();
		parameter.parameterOutputFeature = parameterOutputFeature.clone();
		parameter.parameterSegmentation = parameterSegmentation.clone();
		parameter.parameterClustering = parameterClustering.clone();
		parameter.parameterEM = parameterEM.clone();
		parameter.parameterEHMM = parameterEHMM.clone();
		parameter.parameterMAP = parameterMAP.clone();

		parameter.parameterVarianceControl = parameterVarianceControl.clone();
		parameter.parameterScore = parameterScore.clone();
		parameter.parameterDecoder = parameterDecoder.clone();
		parameter.parameterFilter = parameterFilter.clone();
		parameter.parameterAdjustSegmentation = parameterAdjustSegmentation.clone();
		parameter.parameterTopGaussian = parameterTopGaussian.clone();
		parameter.parameterNamedSpeaker = parameterNamedSpeaker.clone();
		parameter.parameterSegmentationSplit = parameterSegmentationSplit.clone();

		parameter.parameterModel = parameterModel.clone();
		parameter.parameterInitializationEM = parameterInitializationEM.clone();
		parameter.parameterSegmentationInputFile = parameterSegmentationInputFile.clone();
		parameter.parameterSegmentationInputFile2 = parameterSegmentationInputFile2.clone();
		parameter.parameterSegmentationInputFile3 = parameterSegmentationInputFile3.clone();
		parameter.parameterSegmentationOutputFile = parameterSegmentationOutputFile.clone();
		parameter.parameterSegmentationFilterFile = parameterSegmentationFilterFile.clone();
		parameter.parameterModelSetOutputFile = parameterModelSetOutputFile.clone();
		parameter.parameterModelSetInputFile = parameterModelSetInputFile.clone();
		parameter.parameterModelSetInputFile2 = parameterModelSetInputFile2.clone();
		parameter.parameterDiarization = parameterDiarization.clone();
		parameter.parameterVideoInputFeature = parameterVideoInputFeature.clone();
		parameter.parameterNormlization = parameterNormlization.clone();
		parameter.parameterILP = parameterILP.clone();
		parameter.parameterTotalVariability = parameterTotalVariability.clone();
		return parameter;
	}

	/**
	 * Adds the options.
	 * 
	 * @param option the option
	 */
	public void addOptions(LongOptWithAction option) {
		localOptionList.add(option);
		optionList.add(option);
	}

	/**
	 * Log cmd line.
	 * 
	 * @param args the args
	 */
	public void logCmdLine(String[] args) {
		getSeparator2();
		String message = "cmdLine:";
		for (String arg : args) {
			message += " " + arg;
		}
		logger.config(message);
		getSeparator2();
	}

	/**
	 * Read parameters.
	 * 
	 * @param args the args
	 */
	public void readParameters(String args[]) {
		int c;

		if (SpkDiarizationLogger.DEBUG) for (int i = 0; i < optionList.size(); i++) {
			logger.finest("name:" + optionList.get(i).getName() + " idx=" + optionList.get(i).getVal());
		}
		LongOptWithAction[] LongOptWithActions = new LongOptWithAction[optionList.size()];
		optionList.toArray(LongOptWithActions);
		String message = new SimpleDateFormat("hh:mm.SSS").format(System.currentTimeMillis());
		message += Utilities.pad(" Parameter ", 15);
		message += Utilities.pad("WARNING ", 6);
		message += "| ";

		Getopt getOpt = new Getopt(message, args, "-", LongOptWithActions);

		String optarg;
		while ((c = getOpt.getopt()) != -1) {
			optarg = getOpt.getOptarg();
			for (ParameterBase p : listOfParameterObject) {
				p.readParameter(c, optarg);
				continue;
			}
			for (LongOptWithAction option : localOptionList) {
				if (c == option.getVal()) {
					option.execute(optarg);
					continue;
				}
			}
			if (c == 1) {
				show = optarg;
			}
		}
	}

	/**
	 * Gets the separator2.
	 * 
	 * @return the separator2
	 */
	public String getSeparator2() {
		return " ====================================================== ";
	}

	/**
	 * Gets the separator.
	 * 
	 * @return the separator
	 */
	public String getSeparator() {
		return " ------------------------------------------------------ ";
	}

	/**
	 * Log show.
	 */
	public void logShow() {
		logger.config("[options] show:" + show);
	}

	/**
	 * Gets the default charset.
	 * 
	 * @return the defaultCharset
	 */
	protected static Charset getDefaultCharset() {
		return DefaultCharset;
	}

	/**
	 * Gets the show.
	 * 
	 * @return the show
	 */
	public String getShow() {
		return show;
	}

	/**
	 * Checks if is help.
	 * 
	 * @return the help
	 */
	public boolean isHelp() {
		return help;
	}

	/**
	 * Gets the parameter video input feature.
	 * 
	 * @return the parameter video input feature
	 */
	public ParameterVideoInputFeature getParameterVideoInputFeature() {
		return parameterVideoInputFeature;
	}

	/**
	 * Gets the parameter input feature.
	 * 
	 * @return the parameter input feature
	 */
	public ParameterAudioInputFeature getParameterInputFeature() {
		return parameterInputFeature;
	}

	/**
	 * Gets the parameter input feature2.
	 * 
	 * @return the parameter input feature2
	 */
	public ParameterInputFeature2 getParameterInputFeature2() {
		return parameterInputFeature2;
	}

	/**
	 * Gets the parameter output feature.
	 * 
	 * @return the parameter output feature
	 */
	public ParameterAudioOutputFeature getParameterOutputFeature() {
		return parameterOutputFeature;
	}

	/**
	 * Gets the parameter segmentation.
	 * 
	 * @return the parameter segmentation
	 */
	public ParameterSegmentation getParameterSegmentation() {
		return parameterSegmentation;
	}

	/**
	 * Gets the parameter clustering.
	 * 
	 * @return the parameter clustering
	 */
	public ParameterClustering getParameterClustering() {
		return parameterClustering;
	}

	/**
	 * Gets the parameter em.
	 * 
	 * @return the parameter em
	 */
	public ParameterEM getParameterEM() {
		return parameterEM;
	}

	/**
	 * Gets the parameter ehmm.
	 * 
	 * @return the parameter ehmm
	 */
	public ParameterEHMM getParameterEHMM() {
		return parameterEHMM;
	}

	/**
	 * Gets the parameter map.
	 * 
	 * @return the parameter map
	 */
	public ParameterMAP getParameterMAP() {
		return parameterMAP;
	}

	/**
	 * Gets the parameter variance control.
	 * 
	 * @return the parameter variance control
	 */
	public ParameterVarianceControl getParameterVarianceControl() {
		return parameterVarianceControl;
	}

	/**
	 * Gets the parameter score.
	 * 
	 * @return the parameter score
	 */
	public ParameterScore getParameterScore() {
		return parameterScore;
	}

	/**
	 * Gets the parameter decoder.
	 * 
	 * @return the parameter decoder
	 */
	public ParameterDecoder getParameterDecoder() {
		return parameterDecoder;
	}

	/**
	 * Gets the parameter filter.
	 * 
	 * @return the parameter filter
	 */
	public ParameterFilter getParameterFilter() {
		return parameterFilter;
	}

	/**
	 * Gets the parameter adjust segmentation.
	 * 
	 * @return the parameter adjust segmentation
	 */
	public ParameterAdjustSegmentation getParameterAdjustSegmentation() {
		return parameterAdjustSegmentation;
	}

	/**
	 * Gets the parameter top gaussian.
	 * 
	 * @return the parameter top gaussian
	 */
	public ParameterTopGaussian getParameterTopGaussian() {
		return parameterTopGaussian;
	}

	/**
	 * Gets the parameter named speaker.
	 * 
	 * @return the parameter named speaker
	 */
	public ParameterNamedSpeaker getParameterNamedSpeaker() {
		return parameterNamedSpeaker;
	}

	/**
	 * Gets the parameter segmentation split.
	 * 
	 * @return the parameter segmentation split
	 */
	public ParameterSegmentationSplit getParameterSegmentationSplit() {
		return parameterSegmentationSplit;
	}

	/**
	 * Gets the parameter model.
	 * 
	 * @return the parameter model
	 */
	public ParameterModel getParameterModel() {
		return parameterModel;
	}

	/**
	 * Gets the parameter initialization em.
	 * 
	 * @return the parameter initialization em
	 */
	public ParameterInitializationEM getParameterInitializationEM() {
		return parameterInitializationEM;
	}

	/**
	 * Gets the parameter segmentation input file2.
	 * 
	 * @return the parameter segmentation input file2
	 */
	public ParameterSegmentationInputFile2 getParameterSegmentationInputFile2() {
		return parameterSegmentationInputFile2;
	}

	/**
	 * Gets the parameter segmentation input file3.
	 * 
	 * @return the parameter segmentation input file3
	 */
	public ParameterSegmentationInputFile3 getParameterSegmentationInputFile3() {
		return parameterSegmentationInputFile3;
	}

	/**
	 * Gets the parameter segmentation output file.
	 * 
	 * @return the parameter segmentation output file
	 */
	public ParameterSegmentationOutputFile getParameterSegmentationOutputFile() {
		return parameterSegmentationOutputFile;
	}

	/**
	 * Gets the parameter segmentation filter file.
	 * 
	 * @return the parameter segmentation filter file
	 */
	public ParameterSegmentationFilterFile getParameterSegmentationFilterFile() {
		return parameterSegmentationFilterFile;
	}

	/**
	 * Gets the parameter model set output file.
	 * 
	 * @return the parameter model set output file
	 */
	public ParameterModelSetOutputFile getParameterModelSetOutputFile() {
		return parameterModelSetOutputFile;
	}

	/**
	 * Gets the parameter model set input file.
	 * 
	 * @return the parameter model set input file
	 */
	public ParameterModelSetInputFile getParameterModelSetInputFile() {
		return parameterModelSetInputFile;
	}

	/**
	 * Gets the parameter diarization.
	 * 
	 * @return the parameter diarization
	 */
	public ParameterBNDiarization getParameterDiarization() {
		return parameterDiarization;
	}

	/**
	 * Gets the parameter segmentation input file.
	 * 
	 * @return the parameter segmentation input file
	 */
	public ParameterSegmentationInputFile getParameterSegmentationInputFile() {
		return parameterSegmentationInputFile;
	}

	/**
	 * Gets the option list.
	 * 
	 * @return the option list
	 */
	public ArrayList<LongOptWithAction> getOptionList() {
		return optionList;
	}

	/**
	 * Gets the parameter normlization.
	 * 
	 * @return the parameter normlization
	 */
	public ParameterIVectorNormalization getParameterNormlization() {
		return parameterNormlization;
	}

	/**
	 * Gets the parameter model set input file2.
	 * 
	 * @return the parameter model set input file2
	 */
	public ParameterModelSetInputFile2 getParameterModelSetInputFile2() {
		return parameterModelSetInputFile2;
	}

	/**
	 * Gets the parameter ilp.
	 * 
	 * @return the parameterILP
	 */
	public ParameterILP getParameterILP() {
		return parameterILP;
	}

	/**
	 * Gets the parameter total variability.
	 * 
	 * @return the parameterIVector
	 */
	public ParameterTotalVariability getParameterTotalVariability() {
		return parameterTotalVariability;
	}

}