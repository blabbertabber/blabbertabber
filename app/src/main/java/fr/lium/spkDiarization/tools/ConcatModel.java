/**
 * 
 * <p>
 * SConcatModel
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
 *          Concat GMM model file in a GMM Vector and save it
 * 
 */

package fr.lium.spkDiarization.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.lib.StringListFileIO;
import fr.lium.spkDiarization.libModel.gaussian.GMM;
import fr.lium.spkDiarization.libModel.gaussian.GMMArrayList;
import fr.lium.spkDiarization.libModel.gaussian.ModelIO;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class to concat a list of GMMArryLists. Works only with raw Alize models and binary self format
 */
public class ConcatModel {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ConcatModel.class.getName());

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "SConcatModel");

			ArrayList<String> list = StringListFileIO.read(parameter.getParameterModelSetInputFile().getMask(), false);
			GMMArrayList gmmList = new GMMArrayList();
			int j = 0;
			// if the model is in ALIZE RAW format
			// note that this code support only Diagonal Gaussian.
			if (parameter.getParameterModelSetInputFile().getFormat() == 1) {
				for (String string : list) {
					logger.info("read model: " + string);
					String filename = IOFile.getFilename(string, "");
					IOFile fi = new IOFile(filename, "rb", true);
					fi.open();
					GMMArrayList tmpGmmList = new GMMArrayList();
					File theFile = new File(filename);
					String basename = theFile.getName();
					// logger.info("filename = " +filename + "basename = " +basename);
					String name = basename.split("[.]")[0];
					logger.info("name = " + name);
					ModelIO.readerGMMContainerALIZE(fi, tmpGmmList, name);

					for (GMM gmm : tmpGmmList) {
						gmmList.add(gmm);
						logger.info("Model added " + j + " " + gmm.getName());
						j++;
					}
					tmpGmmList.clear();
					fi.close();
				}
			} else {
				for (String string : list) {
					logger.info("read model: " + string);
					IOFile fi = new IOFile(IOFile.getFilename(string, ""), "rb");
					fi.open();
					GMMArrayList tmpGmmList = new GMMArrayList();
					ModelIO.readerGMMContainer(fi, tmpGmmList);
					/*
					 * if (tmpGmmList.size() == 1) { File file = new File(string); String name = file.getName().split("[.]")[0]; tmpGmmList.get(0).setName(name); logger.info("new name: "+name+ " gender:"+tmpGmmList.get(0).getGender()); }
					 */
					for (GMM gmm : tmpGmmList) {
						gmmList.add(gmm);
						logger.info("Model added " + j + " " + gmm.getName());
						j++;
					}
					tmpGmmList.clear();
					fi.close();
				}

			}

			IOFile fo = new IOFile(parameter.getParameterModelSetOutputFile().getMask(), "wb");
			fo.open();
			ModelIO.writerGMMContainer(fo, gmmList);
			fo.close();
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param progam the progam
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String progam) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("Program name = " + progam);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			parameter.getParameterModelSetOutputFile().logAll(); // tOutMask
		}
	}

}
