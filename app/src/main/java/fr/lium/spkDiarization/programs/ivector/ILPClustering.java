package fr.lium.spkDiarization.programs.ivector;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cmu.sphinx.tools.gui.util.SysCommandExecutor;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.lib.StringListFileIO;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.AudioFeatureSet;
import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;
import fr.lium.spkDiarization.libModel.Distance;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialList;
import fr.lium.spkDiarization.libModel.ivector.EigenFactorRadialNormalizationFactory;
import fr.lium.spkDiarization.libModel.ivector.IVectorArrayList;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class ILPClustering.
 */
public class ILPClustering {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ILPClustering.class.getName());

	/** The Constant keyILP. */
	private final static String keyILP = "elementCenter_";

	/** The start of binary. */
	private static int startOfBinary = 0;

	/**
	 * Generate ilp problem.
	 * 
	 * @param iVectorList the i vector list
	 * @param distance the distance
	 * @param threshold the threshold
	 * @return the array list
	 */
	protected static ArrayList<String> generateILPProblem(IVectorArrayList iVectorList, MatrixSymmetric distance, double threshold) {

		ArrayList<String> problem = new ArrayList<String>();

		problem.add("Minimize");
		problem.add(problemMinimize(iVectorList, distance, threshold));
		problem.add("Subject To");
		problem.addAll(problemConstraintsElement(iVectorList, distance, threshold));
		problem.addAll(problemConstraintsCenter(iVectorList, distance, threshold));
		// problem.addAll(problemDistance(iVectorList, distance, threshold));
		// problem.add("Bounds");
		// problem.addAll(problemBounds(iVectorList, distance, threshold));
		problem.add("Binary");
		startOfBinary = problem.size();
		problem.addAll(problemBinary(iVectorList, distance, threshold));
		problem.add("End");
		return problem;

	}

	/**
	 * Problem bounds.
	 * 
	 * @param iVectorList the i vector list
	 * @param distance the distance
	 * @param threshold the threshold
	 * @return the array list
	 */
	@SuppressWarnings("unused")
	private static ArrayList<String> problemBounds(IVectorArrayList iVectorList, MatrixSymmetric distance, double threshold) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < distance.getSize(); i++) {
			for (int j = 0; j < distance.getSize(); j++) {
				double d = distance.get(i, j);
				if (d <= threshold) {
					list.add("0 <= " + getName(iVectorList, i, j) + " <= 1");
				}
			}
		}
		return list;
	}

	/**
	 * Problem binary.
	 * 
	 * @param iVectorList the i vector list
	 * @param distance the distance
	 * @param threshold the threshold
	 * @return the array list
	 */
	protected static ArrayList<String> problemBinary(IVectorArrayList iVectorList, MatrixSymmetric distance, double threshold) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < distance.getSize(); i++) {
			for (int j = 0; j < distance.getSize(); j++) {
				double d = distance.get(i, j);
				if (d <= threshold) {
					list.add(getName(iVectorList, i, j));
				}
			}
		}
		return list;
	}

	/**
	 * Problem constraints distance.
	 * 
	 * @param iVectorList the i vector list
	 * @param distance the distance
	 * @param threshold the threshold
	 * @return the array list
	 */
	protected static ArrayList<String> problemConstraintsDistance(IVectorArrayList iVectorList, MatrixSymmetric distance, double threshold) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < distance.getSize(); i++) {
			for (int j = 0; j < distance.getSize(); j++) {
				if (i != j) {
					double d = distance.get(i, j);
					if (d <= threshold) {
						list.add(getName(iVectorList, i, j) + " <= 1");
						// list.add(d+" "+getName(iVectorList, i, j)+" < "+threshold);
						// } else {
						// list.add(getName(iVectorList, i, j)+" = 0");
					}
				}
			}
		}
		return list;
	}

	/**
	 * Problem constraints element.
	 * 
	 * @param iVectorList the i vector list
	 * @param distance the distance
	 * @param threshold the threshold
	 * @return the array list
	 */
	protected static ArrayList<String> problemConstraintsElement(IVectorArrayList iVectorList, MatrixSymmetric distance, double threshold) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < distance.getSize(); i++) {
			String ch = "S" + i + ": " + getName(iVectorList, i, i);
			for (int j = 0; j < distance.getSize(); j++) {
				if (i != j) {
					double d = distance.get(i, j);
					if (d <= threshold) {
						ch += " + " + getName(iVectorList, i, j);
					}
				}
			}
			ch += " = 1 ";
			list.add(ch);
		}
		return list;
	}

	/**
	 * Problem constraints center.
	 * 
	 * @param iVectorList the i vector list
	 * @param distance the distance
	 * @param threshold the threshold
	 * @return the array list
	 */
	protected static ArrayList<String> problemConstraintsCenter(IVectorArrayList iVectorList, MatrixSymmetric distance, double threshold) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < distance.getSize(); i++) {
			for (int j = 0; j < distance.getSize(); j++) {
				if (i != j) {
					double d = distance.get(i, j);
					if (d <= threshold) {
						list.add(getName(iVectorList, i, j) + " - " + getName(iVectorList, j, j) + " <= 0");
					}
				}
			}
		}
		return list;
	}

	/**
	 * Gets the name.
	 * 
	 * @param iVectorList the i vector list
	 * @param i the i
	 * @param j the j
	 * @return the name
	 */
	protected static String getName(IVectorArrayList iVectorList, int i, int j) {
		return keyILP + iVectorList.get(i).getName() + "_" + iVectorList.get(j).getName();
	}

	/**
	 * Problem minimize.
	 * 
	 * @param iVectorList the i vector list
	 * @param distance the distance
	 * @param threshold the threshold
	 * @return the string
	 */
	private static String problemMinimize(IVectorArrayList iVectorList, MatrixSymmetric distance, double threshold) {
		String ch = "problem : " + getName(iVectorList, 0, 0);

		for (int i = 1; i < distance.getSize(); i++) {
			ch += " + " + getName(iVectorList, i, i);
		}

		for (int i = 0; i < distance.getSize(); i++) {
			for (int j = 0; j < distance.getSize(); j++) {
				if (i != j) {
					double d = distance.get(i, j) / threshold;
					if ((d > 0) && (d <= 1)) {
						ch += " + " + d + " " + getName(iVectorList, i, j);
					}
				}
			}
		}
		return ch;
	}

	/**
	 * Make distance.
	 * 
	 * @param iVectorList the i vector list
	 * @param covarianceInvert the covariance invert
	 * @return the matrix symmetric
	 * @throws DiarizationException the diarization exception
	 */
	public static MatrixSymmetric makeDistance(IVectorArrayList iVectorList, MatrixSymmetric covarianceInvert) throws DiarizationException {

		MatrixSymmetric distance = new MatrixSymmetric(iVectorList.size());
		distance.fill(0.0);
		//TreeMap<Double, String> ds = new TreeMap<Double, String>();

		for (int i = 0; i < distance.getSize(); i++) {
			//String ch = iVectorList.get(i).getName() + " " + i + " [ ";
			for (int j = i; j < distance.getSize(); j++) {
				double d = Distance.iVectorMahalanobis(iVectorList.get(i), iVectorList.get(j), covarianceInvert);
				distance.set(i, j, d);
				//ch += j + ": " + String.format("%8.6f", d) + ", ";
				//ds.put(d, i + "-" + j);
			}
			//logger.info("distance " + ch + " ]");
		}
		return distance;
	}

	/**
	 * Log cmd.
	 * 
	 * @param output the output
	 * @param msg the msg
	 */
	protected static void logCmd(String output, String msg) {
		String[] list = output.split("\n");
		for (String line : list) {
			logger.info(msg + ": " + line);
		}
	}

	/**
	 * Solve ilp.
	 * 
	 * @param problem the problem
	 * @param clusterSet the cluster set
	 * @param parameter the parameter
	 * @return the cluster set
	 * @throws Exception the exception
	 */
	protected static ClusterSet solveILP(ArrayList<String> problem, ClusterSet clusterSet, Parameter parameter) throws Exception {
		ClusterSet resultClusterSet = clusterSet.clone();
		String glpsol = parameter.getParameterILP().getGlpsolProgram();
		String fileNameProblem = IOFile.getFilename(parameter.getParameterILP().getProblemMask(), parameter.show);
		String fileNameSolution = IOFile.getFilename(parameter.getParameterILP().getSolutionMask(), parameter.show);
		StringListFileIO.write(fileNameProblem, false, problem);
		String toExec = glpsol + " --lp " + fileNameProblem.toString() + " -o " + fileNameSolution;

		logger.info("Execute: " + toExec);
		SysCommandExecutor cmdExecutor = SysCommandExecutor.getInstance();
		int exitStatus = cmdExecutor.runCommand(toExec);
		String cmdError = cmdExecutor.getCommandError();
		String cmdOutput = cmdExecutor.getCommandOutput();
		logger.info("Execution exitStatus: " + exitStatus);
		logCmd(cmdError, "Execution cmdError");
		logCmd(cmdOutput, "Execution cmdOutput");

		/*
		 * ArrayList<String> solution = StringListFileIO.read(fileNameSolution, false); String[] nb = solution.get(0).split(" "); int nbConstraint = Integer.parseInt(nb[0]); int nbBinary = Integer.parseInt(nb[1]);
		 * logger.info("nbConstraint :"+nbConstraint+" nbBinary:"+nbBinary+ " startOfBinary:"+startOfBinary); for(int i = 0; i < nbBinary; i++) { int indexSolution = i + nbConstraint + 2; int value = Integer.parseInt(solution.get(indexSolution)); int
		 * indexProblem = i + startOfBinary; String binary = problem.get(indexProblem); String[] clusterName = binary.split("_"); logger.info("problem line #"+indexProblem+" corresponding to solution line #"+indexSolution+" : "+binary+" = "+value);
		 * boolean merge = ((value == 1) && (clusterName[1].equals(clusterName[2]) == false)); logger.info("solution: "+clusterName[1]+" merges in "+clusterName[2]+" ? --> "+merge); if (merge == true) { //
		 * resultClusterSet.mergeCluster(clusterName[2], clusterName[1]); } }
		 */

		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileNameSolution)));
		String line = "";
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			// logger.info(line);
			if (line.contains(" " + keyILP) == true) {
				if (line.contains("*") == false) {
					line += reader.readLine();
				}
				logger.info(line);
				String[] element = line.split(" +");

				// logger.info("solution: "+element[1]+" "+element[3]);
				String[] clusterName = element[1].split("_");
				boolean merge = ((element[3].equals("1")) && (clusterName[1].equals(clusterName[2]) == false));
				logger.info("solution: " + clusterName[1] + " merges in " + clusterName[2] + " ? --> " + merge);
				if (merge == true) {
					resultClusterSet.mergeCluster(clusterName[2], clusterName[1]);
				}
			}
		}
		reader.close();
		return resultClusterSet;
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "test");
			if (parameter.show.isEmpty() == false) {
				// clusters
				ClusterSet clusterSet = MainTools.readClusterSet(parameter);
				// Features
				AudioFeatureSet featureSet = MainTools.readFeatureSet(parameter, clusterSet);
				featureSet.setCurrentShow(clusterSet.getFirstCluster().firstSegment().getShowName());
				Date date1 = new Date();
				IVectorArrayList iVectorList = TrainIVectorOrTV.make(clusterSet, featureSet, parameter);
				EigenFactorRadialList normalization = MainTools.readEigenFactorRadialNormalization(parameter);
				logger.info("number of iteration in normalisation: " + normalization.size());
				IVectorArrayList normalizedIVectorList = EigenFactorRadialNormalizationFactory.applied(iVectorList, normalization);
				MatrixSymmetric covarianceInvert = MainTools.readMahanalonisCovarianceMatrix(parameter).invert();
				Date date2 = new Date();

				MatrixSymmetric distance = makeDistance(normalizedIVectorList, covarianceInvert);
				
				Date date3 = new Date();

				double thr = parameter.getParameterILP().getThresholdILP();
				ArrayList<String> problem = generateILPProblem(iVectorList, distance, thr);
				ClusterSet resultClusterSet = solveILP(problem, clusterSet, parameter);
				
				Date date4 = new Date();
				long d = date2.getTime() - date1.getTime();
				logger.info("##--## ivector: "+d);
				 d = date3.getTime() - date2.getTime();
				logger.info("##--##distance: "+d);
				 d = date4.getTime() - date3.getTime();
				logger.info("##--##clustering: "+d);

				
				MainTools.writeClusterSet(parameter, resultClusterSet);
			}
		} catch (DiarizationException e) {
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

			parameter.getParameterInputFeature().logAll(); // fInMask
			logger.config(parameter.getSeparator());
			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());

			parameter.getParameterModelSetInputFile().logAll(); // tInMask

			logger.config(parameter.getSeparator());
			parameter.getParameterNormlization().logAll();
			logger.config(parameter.getSeparator());
			parameter.getParameterILP().logAll();
			logger.config(parameter.getSeparator());
		}
	}
}
