package fr.lium.spkDiarization.programs.ivector;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.gaussian.FullGaussian;
import fr.lium.spkDiarization.libModel.ivector.IVector;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class to compute the covariance Matrix for Mahanalobis distance.
 */
public class ComputeMahanalobisCovariance {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ComputeMahanalobisCovariance.class.getName());

	/*
	 * public static SymmetricMatrix makeIVectorCovariance(IVectorArrayList iVectorList) throws DiarizationException { if(iVectorList.size() == 0) { return null; } return TotalVariability.covariance(iVectorList); }
	 */

	/**
	 * Make mean of speaker covariance.
	 * 
	 * @param iVectorList the i vector list
	 * @return the matrix symmetric
	 * @throws DiarizationException the diarization exception
	 * @throws FileNotFoundException the file not found exception
	 */
	public static MatrixSymmetric makeMeanOfSpeakerCovariance(IVectorArrayList iVectorList) throws DiarizationException, FileNotFoundException {
		if (iVectorList.size() == 0) {
			return null;
		}

		int dimmension = iVectorList.get(0).getDimension();
		MatrixSymmetric meanCov = new MatrixSymmetric(dimmension);
		meanCov.fill(0.0);

		Set<String> speakerIDList = iVectorList.getSpeakerIDList();
		HashMap<String, FullGaussian> map = new HashMap<String, FullGaussian>();
		logger.info("number of speaker: " + speakerIDList.size());
		for (String speakerID : speakerIDList) {
			FullGaussian fg = new FullGaussian(dimmension);
			fg.statistic_initialize();
			fg.setName(speakerID);
			map.put(speakerID, fg);
		}

		for (IVector iVector : iVectorList) {
			FullGaussian fg = map.get(iVector.getSpeakerID());
			logger.info("--> "+iVector.getName()+" "+iVector.getSpeakerID()+" count:"+fg.statistic_getCount());
			fg.statistic_addFeature(iVector.getData(), 1.0);
		}

		int count = 0;
		for (FullGaussian fg : map.values()) {
			fg.setModel();
			if (fg.statistic_getCount() >= 3) {
				count++;
				fg.statistic_setMeanAndCovariance();
				for (int i = 0; i < dimmension; i++) {
					for (int j = i; j < dimmension; j++) {
						meanCov.set(i, j, meanCov.get(i, j) + fg.getCovariance(i, j));
					}
				}
				logger.info("add" + fg.getName() + ", to few data, count:" + fg.getCount());
			} else {
				logger.info("reject " + fg.getName() + ", to few data, count:" + fg.getCount());
			}
			fg.statistic_reset();
		}
		logger.info("make mean of covariance");
		for (int i = 0; i < dimmension; i++) {
			for (int j = i; j < dimmension; j++) {
				meanCov.set(i, j, meanCov.get(i, j) / count);
			}
		}

		return meanCov;
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
			info(parameter, "ComputeMeanOfSpeakerInterSessionCovariance");
			if (parameter.show.isEmpty() == false) {

				for (int nb = 1; nb <= 1; nb++) {
					IVectorArrayList trainIVectorList = MainTools.readIVectorArrayList(parameter);
					MatrixSymmetric meanCov = makeMeanOfSpeakerCovariance(trainIVectorList);
					MainTools.writeWMahanalonisCovarianceMatrix(meanCov, parameter);
				}
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
			// parameter.getParameterModelSetOutputFile().logAll();
			// parameter.getParameterNormlization().logAll();
			parameter.getParameterNormlization().logMahanalobisCovarianceMask();
		}
	}
}
