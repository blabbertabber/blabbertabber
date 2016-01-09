/**
 * <p>
 * MainTools
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:gael.salaun@univ-lemans.fr">Gael Salaun</a>
 * @author <a href="mailto:teva.merlin@lium.univ-lemans.fr">Teva Merlin</a>
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

package fr.lium.spkDiarization.lib;

import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import edu.thesis.xml.transform.TransformerException;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libFeature.FeatureSet;
import fr.lium.spkDiarization.libModel.GMM;
import fr.lium.spkDiarization.libModel.ModelIO;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterModel;

/**
 * The Class MainTools.
 */
public class MainTools {

    /**
     * Gets the GMM for top gaussian.
     *
     * @param param the param
     * @param features the features
     *
     * @return the GMM for top gaussian
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static ArrayList<GMM> readGMMForTopGaussian(Parameter param, FeatureSet features) throws IOException, DiarizationException {
        ArrayList<GMM> vect = new ArrayList<GMM>();
        if (param.parameterTopGaussian.getScoreNTop() >= 0) {
            String modelTopFilename = IOFile.getFilename(param.parameterTopGaussian.getScoreNTopGMMMask(), param.show);
            String mode = "rb";
            IOFile fi = new IOFile(modelTopFilename, mode);
            fi.open();
            ModelIO.readerGMMContainer(fi, vect);
            fi.close();
            vect.get(0).sortComponents();
        }
        features.setUBMs(vect);
        return vect;
    }

    /**
     * Read the clusterSet.
     *
     * @param param the parameter
     *
     * @return the input clusters
     *
     * @throws DiarizationException the diarization exception
     * @throws Exception the exception
     */
    public static ClusterSet readClusterSet(Parameter param) throws DiarizationException, Exception {
        // clusters
        ClusterSet clusters = new ClusterSet();
        clusters.read(param.showLst, param.parameterSegmentationInputFile);
        return clusters;
    }

    /**
     * Read the second clusterset.
     *
     * @param param the parameter
     *
     * @return the input clusters2
     *
     * @throws DiarizationException the diarization exception
     * @throws Exception the exception
     */
    public static ClusterSet readTheSecondClusterSet(Parameter param) throws DiarizationException, Exception {
        // clusters
        ClusterSet clusters = new ClusterSet();
        clusters.read(param.showLst, param.parameterSegmentationInputFile2);
        return clusters;
    }

    /**
     * Read the featureSet.
     *
     * @param param the parameter
     * @param clusters the clusters
     *
     * @return the input features
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static FeatureSet readFeatureSet(Parameter param, ClusterSet clusters) throws IOException, DiarizationException {
        FeatureSet features = new FeatureSet(clusters, param.parameterInputFeature, param.trace);
        if (param.trace) {
            features.debug();
        }
        return features;
    }

    /**
     * Read the featureSet.
     *
     * @param param the parameter
     * @param clusters the clusters
     *
     * @return the input features
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static FeatureSet readFeatureSet2(Parameter param, ClusterSet clusters) throws IOException, DiarizationException {
        FeatureSet features = new FeatureSet(clusters, param.parameterInputFeature2, param.trace);
        if (param.trace) {
            features.debug();
        }
        return features;
    }

    /**
     * Read the featureSet.
     *
     * @param param the parameter
     * @param clusters the clusters
     * @param featuresBase the features base
     *
     * @return the input features
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws DiarizationException the diarization exception
     */
    public static FeatureSet readFeatureSet(Parameter param, ClusterSet clusters, FeatureSet featuresBase) throws IOException, DiarizationException {
        FeatureSet features = new FeatureSet(featuresBase, clusters, param.parameterInputFeature, param.trace);
        if (param.trace) {
            features.debug();
        }
        return features;
    }

    /**
     * Read a GMM container.
     *
     * @param param the parameter
     *
     * @return the input gmm container
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ArrayList<GMM> readGMMContainer(Parameter param) throws DiarizationException, IOException {
        ArrayList<GMM> vect = new ArrayList<GMM>();
        String inputFilename = IOFile.getFilename(param.parameterModelSetInputFile.getMask(), param.show);
        if (inputFilename.equals("")) {
            throw new DiarizationException("error: input model empty " + inputFilename);
        }
        File file = new File(inputFilename);
        if (file.exists() == false) {
            // System.err.println("warring: input model don't exist " + inputFilename);
            return null;
        }
        if (inputFilename.equals("")) {
            System.err.println("warring[MainTools] \t input model empty " + inputFilename);
            return null;
        }
        IOFile fi = new IOFile(inputFilename, "rb");
        fi.open();
        ModelIO.readerGMMContainer(fi, vect);
        fi.close();
        if (vect.size() > 0) {
            param.parameterModel.setKind(vect.get(0).getKind());
            param.parameterModel.setNumberOfComponents(vect.get(0).getNbOfComponents());
        }
        for (int i = 0; i < vect.size(); i++) {
            vect.get(i).sortComponents();
        }
        return vect;
    }

    /**
     * Read a gmm container.
     *
     * @param url the url og the GMM file
     * @param parameterModel the parameter model
     *
     * @return the input gmm container
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ArrayList<GMM> readGMMContainer(URL url, ParameterModel parameterModel) throws DiarizationException, IOException {

        IOFile fi = new IOFile(url);

        if (url.getFile().compareTo("") == 0) {
            System.err.println("warring[MainTools] \t URL not found:" + url.getFile());
            return null;
        }

        ArrayList<GMM> vect = new ArrayList<GMM>();
        fi.open();
        ModelIO.readerGMMContainer(fi, vect);
        fi.close();
        if (vect.size() > 0) {
            parameterModel.setKind(vect.get(0).getKind());
            parameterModel.setNumberOfComponents(vect.get(0).getNbOfComponents());
        }
        for (int i = 0; i < vect.size(); i++) {
            vect.get(i).sortComponents();
        }
        return vect;
    }

    /**
     * Gets the parameter.
     *
     * @param args the argument of the program
     *
     * @return the param
     */
    public static Parameter getParameters(String args[]) {
        Parameter param = new Parameter();
        param.readParameters(args);
        if (args.length <= 1) {
            param.help = true;
        }
        if (param.trace) {
            param.printCmdLine(args);
        }
        return param;
    }

    /**
     * Write the output clusters.
     *
     * @param param the parameter
     * @param clusters the clusters
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws DiarizationException the diarization exception
     * @throws TransformerException the transformer exception
     */
    public static void writeClusterSet(Parameter param, ClusterSet clusters) throws IOException, ParserConfigurationException, SAXException,
            DiarizationException, TransformerException {
        writeClusterSet(param, clusters, false);
    }

    /**
     * Write the output clusters.
     *
     * @param param the parameter
     * @param clusters the clusters
     * @param collapse the segment of each cluster
     *
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException the parser configuration exception
     * @throws SAXException the SAX exception
     * @throws DiarizationException the diarization exception
     * @throws TransformerException the transformer exception
     */
    public static void writeClusterSet(Parameter param, ClusterSet clusters, boolean collapse) throws IOException, ParserConfigurationException, SAXException,
            DiarizationException, TransformerException {
        if (collapse) {
            clusters.collapse();
        }
        clusters.write(param.show, param.parameterSegmentationOutputFile);
    }

    /**
     * Write a stringList
     *
     * @param param the parameter
     * @throws IOException
     */
    public static void writeStringList(Parameter param, ArrayList<String> list) throws IOException {
        String outputFilename = IOFile.getFilename(param.parameterSegmentationOutputFile.getMask(), param.show);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilename), Parameter.DefaultCharset));

        for (String line : list) {
            writer.write(line + "\n");
        }

        writer.close();
    }

    /**
     * Write a featureSet.
     *
     * @param param the parameter
     * @param f the f
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void writeFeatureSet(Parameter param, FeatureSet f) throws DiarizationException, IOException {
        f.write(param.show, param.parameterOutputFeature.getFeatureMask(), param.parameterOutputFeature.getFeaturesDescription());
    }

    /**
     * Write a featureSet.
     *
     * @param param the parameter
     * @param f the f
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void writeFeatureSet(String showName, Parameter param, FeatureSet f) throws DiarizationException, IOException {
        f.write(showName, param.parameterOutputFeature.getFeatureMask(), param.parameterOutputFeature.getFeaturesDescription());
    }

    /**
     * Write a gmm container.
     *
     * @param param the parameter
     * @param vect the GMM vector
     *
     * @throws DiarizationException the diarization exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void writeGMMContainer(Parameter param, ArrayList<GMM> vect) throws DiarizationException, IOException {
        String modelOutputFilename = IOFile.getFilename(param.parameterModelSetOutputFile.getMask(), param.show);
        IOFile fo = new IOFile(modelOutputFilename, "wb");
        fo.open();
        ModelIO.writerGMMContainer(fo, vect);
        fo.close();
    }
}
