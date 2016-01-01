/**
 * 
 * <p>
 * FeatureFactory
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          load audio file (java supported format and uncompressed sphere format) and compute MFCC. This class is employed CMU Sphinx 4 classes.
 */

package fr.lium.spkDiarization.libFeature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.media.AudioFormat;
/*
import android.media.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
*/

import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataEndSignal;
import edu.cmu.sphinx.frontend.DoubleData;
import edu.cmu.sphinx.frontend.FloatData;
import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.frequencywarp.PLPCepstrumProducer;
import edu.cmu.sphinx.frontend.transform.DiscreteCosineTransform;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertySheet;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;

/**
 * A factory for creating Feature objects.
 */
public class AudioFeatureSetFactory {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(AudioFeatureSetFactory.class.getName());

	/** The Constant DATA_SOURCE. */
	static final String DATA_SOURCE = "streamDataSource";

	/** The Constant DCT. */
	static final String DCT = "dct";

	/** The Constant PLPCepProd. */
	static final String PLPCepProd = "plpCepstrumProducer";

	// ------------------------------------------------------------------------------------
	/**
	 * Read the audio file.
	 * 
	 * @param audioFilename the audio filename
	 * @param sampleRate the sample rate
	 * @return the an audio stream
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 * @throws UnsupportedAudioFileException the unsupported audio file exception
	 */
	public static AudioInputStream getAudio(String audioFilename, float sampleRate) throws IOException, DiarizationException, UnsupportedAudioFileException {
		AudioInputStream ais = null;
		int channels = 1; // channels 1 ou 2 ou ...
		int featureSize = 16; // nb bits Èchantillon 8 ou 16 ou ...
		boolean bigEndian = true;
		boolean signed = true;
		AudioFormat targetAudioFormat = new AudioFormat(sampleRate, featureSize, channels, signed, bigEndian);
		AudioFormat sourceAudioFormat = null;

		File file = new File(audioFilename);
		if (audioFilename.toLowerCase().endsWith(".sph")) {
			BufferedReader f = null;
			f = new BufferedReader(new FileReader(file));
			String chaine = "";
			int noLigne = 0;
			int tailleEntete = -1;
			float sampleRateSphere = -1; // sample rate (indÈpendant de nbCanaux)
			int channelsSphere = -1; // channels 1 ou 2 ou ...
			int frameSizeSphere = -1; // nb bits Èchantillon 8 ou 16 ou ...
			long nbEchantillonsSphere = -1;
			boolean bigEndianSphere = true;
			Encoding encodingSphere = Encoding.PCM_SIGNED;
			while (true) {
				chaine = f.readLine();
				// on sort si fin de l'entÍte
				if (chaine.startsWith("end_head")) {
					break;
				}
				noLigne++;
				// on saute la 1Ëre ligne
				if (noLigne == 1) {
					continue;
				}
				// on rÈcupËre la longueur de l'entÍte sur la 2Ëme ligne
				if (noLigne == 2) {
					tailleEntete = Integer.valueOf(chaine.trim()).intValue();
					continue;
				}
				// on saute les lignes de commentaires
				if (chaine.startsWith(";")) {
					continue;
				}
				if (chaine.startsWith("channel_count -i ")) {
					StringTokenizer st = new StringTokenizer(chaine.substring(17));
					channelsSphere = Integer.valueOf(st.nextToken()).intValue();
				} else if (chaine.startsWith("sample_count -i ")) {
					StringTokenizer st = new StringTokenizer(chaine.substring(16));
					nbEchantillonsSphere = Long.valueOf(st.nextToken()).longValue();
				} else if (chaine.startsWith("sample_rate -i ")) {
					StringTokenizer st = new StringTokenizer(chaine.substring(15));
					sampleRateSphere = (Integer.valueOf(st.nextToken()).intValue());
				} else if (chaine.startsWith("sample_n_bytes -i ")) {
					StringTokenizer st = new StringTokenizer(chaine.substring(18));
					frameSizeSphere = Integer.valueOf(st.nextToken()).intValue() * 8;
				} else if (chaine.startsWith("sample_coding -s")) {
					String lg = new StringTokenizer(chaine.substring(16)).nextToken();
					int p = 16 + lg.length() + 1; // +1 pour l'espace
					String cod = chaine.substring(p, p + Integer.valueOf(lg).intValue());
					if (cod.equals("ulaw")) {
						encodingSphere = AudioFormat.Encoding.ULAW;
					}
					if (cod.equals("pcm")) {
						encodingSphere = AudioFormat.Encoding.PCM_SIGNED;
					}
				} else if (chaine.startsWith("sample_byte_format -s2 01")) {
					if (SpkDiarizationLogger.DEBUG) logger.finest("*** bigendian");
					bigEndianSphere = false;
				}
			}

			f.close();
			if ((tailleEntete < 0) || (channelsSphere < 0) || (nbEchantillonsSphere < 0) || (sampleRateSphere < 0)
					|| (encodingSphere == null) || (frameSizeSphere < 0)) {
				throw new DiarizationException("Error in file");
			}

			FileInputStream in = new FileInputStream(file);
			in.skip(tailleEntete); // on saute l'entete

			sourceAudioFormat = new AudioFormat(encodingSphere, sampleRateSphere, frameSizeSphere, channelsSphere, (frameSizeSphere * channelsSphere) / 8, sampleRateSphere, bigEndianSphere);
			ais = new AudioInputStream(in, sourceAudioFormat, nbEchantillonsSphere * channelsSphere);
		} else {
			sourceAudioFormat = AudioSystem.getAudioFileFormat(file).getFormat();

			if (SpkDiarizationLogger.DEBUG) logger.finest("***" + sourceAudioFormat.toString() + " ***");

			ais = AudioSystem.getAudioInputStream(file);
		}
		return AudioSystem.getAudioInputStream(targetAudioFormat, ais);
	}

	/**
	 * Gets the data.
	 * 
	 * @param frontEnd the front end
	 * @return the data
	 */
	public static Data getData(FrontEnd frontEnd) {
		return frontEnd.getData();
	}

	/**
	 * Make feature.
	 * 
	 * @param configurationURL the xml configuration file url
	 * @param frontEndName the front end name
	 * @param inputAudioFilename the input audio filename
	 * @param featureDescription the feature description
	 * @return the array list<float[]>
	 */
	public synchronized static AudioFeatureList MakeFeature(URL configurationURL, String frontEndName, String inputAudioFilename, AudioFeatureDescription featureDescription) {
		// System.err.print("-----------");
		// System.err.print(configurationURL.toString());
		// System.err.print("-----------");
		ConfigurationManager configurationManager = new ConfigurationManager(configurationURL);

		FrontEnd frontEnd = (FrontEnd) configurationManager.lookup(frontEndName);
		StreamDataSource audioSource = (StreamDataSource) configurationManager.lookup(DATA_SOURCE);
		PropertySheet psDataSource = configurationManager.getPropertySheet(DATA_SOURCE);
		PropertySheet psDCT = configurationManager.getPropertySheet(DCT);
		PropertySheet psPLPCepstrumProducer = configurationManager.getPropertySheet(PLPCepProd);

		psDCT.setInt(DiscreteCosineTransform.PROP_CEPSTRUM_LENGTH, featureDescription.getFeatureSize());
		psPLPCepstrumProducer.setInt(PLPCepstrumProducer.PROP_CEPSTRUM_LENGTH, featureDescription.getFeatureSize());

		// configurationManager.
		try {
			audioSource.setInputStream(getAudio(inputAudioFilename, psDataSource.getInt(StreamDataSource.PROP_SAMPLE_RATE)), "audio");
		} catch (FileNotFoundException e) {
			logger.log(Level.SEVERE, "FileNotFoundException", e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "IOException", e);
			e.printStackTrace();
		} catch (DiarizationException e) {
			logger.log(Level.SEVERE, "DiarizationException", e);
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			logger.log(Level.SEVERE, "UnsupportedAudioFileException", e);
			e.printStackTrace();
		}
		AudioFeatureList allFeatures = new AudioFeatureList();
		int featureLength = -1;
		Data feature = getData(frontEnd);
		while (!(feature instanceof DataEndSignal)) {
			if (feature instanceof DoubleData) {
				double[] featureDataTmp = ((DoubleData) feature).getValues();
				float[] featureData = new float[featureDataTmp.length];
				for (int i = 0; i < featureData.length; i++) {
					featureData[i] = (float) featureDataTmp[i];
				}
				allFeatures.add(featureData);
			} else if (feature instanceof FloatData) {
				float[] featureData = ((FloatData) feature).getValues();
				if (featureLength < 0) {
					featureLength = featureData.length;
				}
				allFeatures.add(featureData);
			}
			feature = getData(frontEnd);
		}
		// Hack because Sphinx change the logger
		SpkDiarizationLogger.setup();
		return allFeatures;
	}

	/**
	 * Make MFCC feature.
	 * 
	 * @param configurationURL the configuration URL
	 * @param inputAudioFilename the input audio filename
	 * @param featureDescription the feature description
	 * @return the array list<float[]>
	 */
	public static AudioFeatureList MakeMFCCFeature(URL configurationURL, String inputAudioFilename, AudioFeatureDescription featureDescription) {
		return MakeFeature(configurationURL, "cepstraFrontEnd", inputAudioFilename, featureDescription);

	}

}
