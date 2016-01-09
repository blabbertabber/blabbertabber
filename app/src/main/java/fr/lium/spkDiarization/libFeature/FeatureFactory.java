/**
 * <p>
 * FeatureFactory
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
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
 * <p/>
 * load audio file (java supported format and uncompressed sphere format) and compute MFCC.
 * This class is employed CMU Sphinx 4 classes.
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
import edu.thesis.sound.sampled.AudioFormat;
import edu.thesis.sound.sampled.AudioFormat.Encoding;
import edu.thesis.sound.sampled.AudioInputStream;
import edu.thesis.sound.sampled.AudioSystem;
import edu.thesis.sound.sampled.UnsupportedAudioFileException;
import fr.lium.spkDiarization.lib.DiarizationException;

/**
 * A factory for creating Feature objects.
 */
public class FeatureFactory {
    static final String DATA_SOURCE = "streamDataSource";
    static final String DCT = "dct";
    static final String PLPCepProd = "plpCepstrumProducer";

    // ------------------------------------------------------------------------------------

    /**
     * Read the audio file.
     *
     * @param audioFilename the audio filename
     *
     * @return the an audio stream
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     * @throws UnsupportedAudioFileException the unsupported audio file exception
     */
    public static AudioInputStream getAudio(String audioFilename, float sampleRate) throws IOException, DiarizationException, UnsupportedAudioFileException {
        AudioInputStream ais = null;
        int channels = 1; // channels 1 ou 2 ou ...
        int frameSize = 16; // nb bits Èchantillon 8 ou 16 ou ...
        boolean bigEndian = true;
        boolean signed = true;
        AudioFormat targetAudioFormat = new AudioFormat(sampleRate, frameSize, channels, signed, bigEndian);
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
            int nbEchantillonsSphere = -1;
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
                    nbEchantillonsSphere = Integer.valueOf(st.nextToken()).intValue();
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
                    //System.err.println("*** bigendian");
                    bigEndianSphere = false;
                }
            }

            f.close();
            if ((tailleEntete < 0) || (channelsSphere < 0) || (nbEchantillonsSphere < 0) || (sampleRateSphere < 0) || (encodingSphere == null)
                    || (frameSizeSphere < 0)) {
                throw new DiarizationException("Error in file");
            }

            FileInputStream in = new FileInputStream(file);
            in.skip(tailleEntete); // on saute l'entete

            sourceAudioFormat = new AudioFormat(encodingSphere, sampleRateSphere, frameSizeSphere, channelsSphere, frameSizeSphere * channelsSphere / 8,
                    sampleRateSphere, bigEndianSphere);
            ais = new AudioInputStream(in, sourceAudioFormat, nbEchantillonsSphere * channelsSphere);
        } else {
            sourceAudioFormat = AudioSystem.getAudioFileFormat(file).getFormat();

            //System.err.println("***"+sourceAudioFormat.toString()+" ***");

            ais = AudioSystem.getAudioInputStream(file);
        }
        return AudioSystem.getAudioInputStream(targetAudioFormat, ais);
    }


    public static Data getData(FrontEnd frontEnd) {
        return frontEnd.getData();
    }

    /**
     * Make feature.
     *
     * @param configurationURL the xml configuration file url
     * @param frontEndName the front end name
     * @param inputAudioFilename the input audio filename
     *
     * @return the array list<float[]>
     */
    public static FeatureData MakeFeature(URL configurationURL, String frontEndName, String inputAudioFilename, FeatureDescription featureDescription) {
        ConfigurationManager configurationManager = new ConfigurationManager(configurationURL);

        FrontEnd frontEnd = (FrontEnd) configurationManager.lookup(frontEndName);
        StreamDataSource audioSource = (StreamDataSource) configurationManager.lookup(DATA_SOURCE);
        PropertySheet psDataSource = configurationManager.getPropertySheet(DATA_SOURCE);
        PropertySheet psDCT = configurationManager.getPropertySheet(DCT);
        PropertySheet psPLPCepstrumProducer = configurationManager.getPropertySheet(PLPCepProd);

        psDCT.setInt(DiscreteCosineTransform.PROP_CEPSTRUM_LENGTH, featureDescription.getVectorSize());
        psPLPCepstrumProducer.setInt(PLPCepstrumProducer.PROP_CEPSTRUM_LENGTH, featureDescription.getVectorSize());
        //configurationManager.
        try {
            audioSource.setInputStream(getAudio(inputAudioFilename, psDataSource.getInt(StreamDataSource.PROP_SAMPLE_RATE)), "audio");
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DiarizationException e) {
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        }
        FeatureData allFeatures = new FeatureData();
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

        return allFeatures;
    }

    /**
     * Make MFCC feature.
     *
     * @param configurationURL the configuration URL
     * @param inputAudioFilename the input audio filename
     *
     * @return the array list<float[]>
     */
    public static FeatureData MakeMFCCFeature(URL configurationURL, String inputAudioFilename, FeatureDescription featureDescription) {
        return MakeFeature(configurationURL, "cepstraFrontEnd", inputAudioFilename, featureDescription);

    }

}
