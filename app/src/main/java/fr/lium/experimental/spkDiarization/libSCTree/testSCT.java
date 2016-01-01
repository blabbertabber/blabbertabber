/**
 * 
 * <p>
 * testSCT
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
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

package fr.lium.experimental.spkDiarization.libSCTree;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class testSCT.
 */
public class testSCT {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(testSCT.class.getName());

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

			parameter.getParameterSegmentationInputFile().logAll(); // sInMask
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());
// param.getParameterNamedSpeaker().printWordDictonaryMask();
			parameter.getParameterNamedSpeaker().logAll();
		}
	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		try {
			Parameter parameter = MainTools.getParameters(args);
			if (parameter.show.isEmpty() == false) {
				info(parameter, "sNamedSpeaker");
				int nbOfLabel = 4;

				// On charge le dico passé en parametres
// WordDictonary wordDictonary = new WordDictonary();
// wordDictonary.read(param.show, param.getParameterNamedSpeaker().getWordDictonaryMask());

				// clusters
				String filename = IOFile.getFilename(parameter.getParameterSegmentationInputFile().getMask(), parameter.show);
				File file = new File(filename);

				// On lit le .seg fournit en entree et on charge les saucisses
				ClusterSet clusters = new ClusterSet();
// clusters.readSausage(file, param.getParameterSegmentationInputFile().getEncoding(), wordDictonary);
				clusters.readXmlEPAC(file, parameter.getParameterSegmentationInputFile().getEncoding(), parameter.getParameterSegmentationInputFile().getRate());

				// Initialisation du SCT avec le dico et le nombre d'etiquettes possibles
// SCT sct = new SCT(wordDictonary, nbOfLabel);
				SCT sct = new SCT(nbOfLabel);
				// On lit le fichier .tree (SCT Mask)
				sct.read(parameter.show, parameter.getParameterNamedSpeaker().getSCTMask());

				TreeSet<Segment> segmentSet = clusters.getSegments();
				Iterator<Segment> iteratorSegment = segmentSet.iterator();

				// On parcourt tous les segments
				while (iteratorSegment.hasNext()) {
					// Get the sausage set/graph of the segment
					LinkSet sausageSet = iteratorSegment.next().getTranscription().getLinkSet();
					if (SpkDiarizationLogger.DEBUG) {
                                            logger.info(parameter.getSeparator());

                                            sausageSet.debug();

                                            logger.info(parameter.getSeparator());

                                            // Display the SCT
                                            sct.debug();
                                            logger.info(parameter.getSeparator());

                                            // Get the solutions
                                            SCTSolution solution = sct.test(sausageSet);
                                            logger.info(parameter.getSeparator());
                                            solution.debug();
                                        }

				}

			}
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "exception ", e);
			e.printStackTrace();
		}
	}

}
