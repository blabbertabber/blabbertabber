package fr.lium.spkDiarization.programs.ivector;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialList;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialNormalizationFactory;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class TrainEigenFactorRadialNormalisation.
 */
public class TrainEigenFactorRadialNormalisation {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(TrainEigenFactorRadialNormalisation.class.getName());

	/**
	 * Make.
	 * 
	 * @param iVectorList the i vector list
	 * @param normalization the normalization
	 * @param nb the nb
	 * @return the i vector array list
	 * @throws DiarizationException the diarization exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public static IVectorArrayList make(IVectorArrayList iVectorList, EigenFactorRadialList normalization, int nb) throws DiarizationException, FileNotFoundException {
		IVectorArrayList newIVectorList = EigenFactorRadialNormalizationFactory.train(iVectorList, normalization, nb);
		return newIVectorList;
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "TrainEigenFactorRadialNormalisation");
			if (parameter.show.isEmpty() == false) {
				int nb = parameter.getParameterNormlization().getNumberOfInteration();
				IVectorArrayList trainIVectorList = MainTools.readIVectorArrayList(parameter);
				logger.info("size ivList:" + trainIVectorList.size());
				logger.info("number of iteration:" + nb);
				EigenFactorRadialList normalization = new EigenFactorRadialList(nb);
				IVectorArrayList newIVectorList = make(trainIVectorList, normalization, nb);
				MainTools.writeEigenFactorRadialNormalization(normalization, parameter);
				MainTools.writeIVectorArrayList(newIVectorList, parameter);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "error \t exception ", e);
			e.printStackTrace();
		}
	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param program the program
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("info[program] \t name = " + program);
			parameter.getSeparator();
			parameter.logShow();

			parameter.getParameterModelSetInputFile().logAll();
			parameter.getParameterModelSetOutputFile().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterNormlization().logAll();
			parameter.getParameterTotalVariability().logAll();
			logger.config(parameter.getSeparator());
		}
	}
}
