/**
 * 
 * <p>
 * SMergeModel
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

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
 * The Class to merge all gaussians of n GMM in one GMM. Useful to make a UBM from gender- or channel- dependent GMM.
 */
public class MergeModel {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(MergeModel.class.getName());

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		try {
			SpkDiarizationLogger.setup();
			Parameter parameter = MainTools.getParameters(args);
			info(parameter, "SMergeModel");
			GMMArrayList gmmList = new GMMArrayList();
			try {
				ArrayList<String> nameList = StringListFileIO.read(parameter.getParameterModelSetInputFile().getMask(), false);
				for (String string : nameList) {
					logger.fine("add " + string);
					IOFile fi = new IOFile(IOFile.getFilename(string, ""), "rb");
					fi.open();
					GMMArrayList tmpGmmList = new GMMArrayList();
					ModelIO.readerGMMContainer(fi, tmpGmmList);
					for (GMM gmm : tmpGmmList) {
						logger.finer("add gmm " + gmm.getName());
						gmmList.add(gmm);
					}
					tmpGmmList.clear();
					fi.close();
				}
			} catch (Exception e) {
				try {
					gmmList = MainTools.readGMMContainer(parameter);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SAXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ParserConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			logger.info("gmm loaded:" + gmmList.size());
			GMMArrayList outGmmList = new GMMArrayList();
			outGmmList.add(gmmList.get(0));
			logger.info("add gmm " + gmmList.get(0).getName());
			GMM gmmOut = outGmmList.get(0);
			String nameOut = gmmOut.getName();
			int nbFrames = gmmOut.getComponent(0).getCount();
			for (int i = 1; i < gmmList.size(); i++) {
				GMM add = gmmList.get(i);
				logger.info("add gmm " + add.getName());
				nameOut += add.getName();
				nbFrames += add.getComponent(0).getCount();
				for (int k = 0; k < add.getNbOfComponents(); k++) {
					gmmOut.addComponent(add.getComponent(k));
				}
			}
			for (long i = 0; i < gmmOut.getNbOfComponents(); i++) {
				gmmOut.updateCount(nbFrames);
			}
			gmmOut.setName(nameOut);
			gmmOut.normalizeWeights();
			gmmOut.sortComponents();
			MainTools.writeGMMContainer(parameter, outGmmList);
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "", e);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IllegalAccessException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InvocationTargetException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
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
			logger.config("Program name = " + program);
			logger.config(parameter.getSeparator());
			parameter.logShow();

			parameter.getParameterModelSetInputFile().logAll(); // tInMask
			parameter.getParameterModelSetOutputFile().logAll(); // tOutMask
		}
	}

}