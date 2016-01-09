/**
 * <p>
 * testSCT
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
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

package fr.lium.experimental.spkDiarization.libSCTree;

//import java.io.File;
//import java.util.Iterator;
//import java.util.TreeSet;
//
//import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
//import fr.lium.spkDiarization.lib.IOFile;
//import fr.lium.spkDiarization.lib.MainTools;
//import fr.lium.spkDiarization.lib.DiarizationException;
//import fr.lium.spkDiarization.libClusteringData.ClusterSet;
//import fr.lium.spkDiarization.libClusteringData.Segment;
//import fr.lium.spkDiarization.parameter.Parameter;
//
//public class testSCT {
//	public static void info(Parameter param, String prog) {
//		if (param.help) {
//			System.out.println("info[info] \t ------------------------------------------------------ ");
//			System.out.println("info[program] \t name = " + prog);
//			param.printSeparator();
//			param.printShow();
//
//			param.parameterSegmentationInputFile.printMask(); // sInMask
//			param.parameterSegmentationInputFile.printEncodingFormat();
//			param.parameterSegmentationOutputFile.printMask(); // sOutMask
//			param.parameterSegmentationOutputFile.printEncodingFormat();
//			param.printSeparator();
////			param.parameterNamedSpeaker.printWordDictonaryMask();
//			param.parameterNamedSpeaker.printSCTMask();
//		}
//	}
//
//	public static void main(String[] args) throws Exception {
//		try {
//			Parameter param = MainTools.getParameters(args);
//			if (param.nbShow > 0) {
//				info(param, "sNamedSpeaker");
//				int nbOfLabel = 4;
//
//				// On charge le dico passé en parametres
//// WordDictonary wordDictonary = new WordDictonary();
//// wordDictonary.read(param.show, param.parameterNamedSpeaker.getWordDictonaryMask());
//
//				// clusters
//				String filename = IOFile.getFilename(param.parameterSegmentationInputFile.getMask(), param.show);
//				File file = new File(filename);
//
//				// On lit le .seg fournit en entree et on charge les saucisses
//				ClusterSet clusters = new ClusterSet();
//// clusters.readSausage(file, param.parameterSegmentationInputFile.getEncoding(), wordDictonary);
//				clusters.readXmlEPAC(file, param.parameterSegmentationInputFile.getEncoding());
//
//				// Initialisation du SCT avec le dico et le nombre d'etiquettes possibles
//// SCT sct = new SCT(wordDictonary, nbOfLabel);
//				SCT sct = new SCT(nbOfLabel, param.trace);
//				// On lit le fichier .tree (SCT Mask)
//				sct.read(param.show, param.parameterNamedSpeaker.getSCTMask());
//
//				TreeSet<Segment> segLst = clusters.getSegments();
//				Iterator<Segment> itSeg = segLst.iterator();
//
//				// On parcourt tous les segments
//				while (itSeg.hasNext()) {
//					// Get the sausage set/graph of the segment
//					LinkSet sausageSet = itSeg.next().getTranscription().getLinkSet();
//					System.err.println("[debug] ---------------------------------------------");
//					sausageSet.debug();
//
//					System.err.println("[debug] ---------------------------------------------");
//
//					// Display the SCT
//					sct.debug();
//					System.err.println("[debug] ---------------------------------------------");
//
//					// Get the solutions
//					SCTSolution solution = sct.test(sausageSet);
//					System.err.println("[debug] ---------------------------------------------");
//					solution.debug();
//
//				}
//
//			}
//		} catch (DiarizationException e) {
//			System.out.println("error \t exception " + e.getMessage());
//		}
//	}
//
//}
