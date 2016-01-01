/**
 * 
 * <p>
 * ModelIO
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
 *          Reader and writer for models
 * 
 */

package fr.lium.spkDiarization.libModel.gaussian;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.libMatrix.MatrixRowVector;

/**
 * The Class Model Input Output.
 */
public class ModelIO {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ModelIO.class.getName());

	/** The key gmm container. */
	protected static String keyGMMContainer = "GMMVECT_";

	/** The key gauss container. */
	protected static String keyGaussianContainer = "GAUSSVEC";

	/** The key gmm. */
	protected static String keyGMM = "GMM_____";

	/** The key gauss. */
	protected static String keyGaussian = "GAUSS___";

	/**
	 * Read gmm container xml alize.
	 * 
	 * @param file the file
	 * @param gmmList the gmm list
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 * @throws SAXException the sAX exception
	 * @throws ParserConfigurationException the parser configuration exception
	 */
	public static void readGMMContainerXMLAlize(File file, GMMArrayList gmmList) throws IOException, DiarizationException, SAXException, ParserConfigurationException {
		Document document;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		document = builder.parse(file);
		Element root = document.getDocumentElement();
		gmmList.clear();

		if (root.getNodeName().equals("MixtureGD")) {
			Element elementMixtureGD = root;
			String name = elementMixtureGD.getAttribute("id");
			logger.finest("id:" + name);

			/*
			 * for(int i = 0; i < elementMixtureGD.getAttributes().getLength(); i++){ logger.info("attr "+i+" : "+elementMixtureGD.getAttributes().item(i).getNodeName()); } logger.info("attr :"+elementMixtureGD.getAttribute("distribCount"));
			 */
			int nbComponents = Integer.parseInt(elementMixtureGD.getAttribute("distribCount"));
			int dimension = Integer.parseInt(elementMixtureGD.getAttribute("vectSize"));

			GMM gmm = new GMM(nbComponents, dimension);
			gmm.setName(name);

			NodeList nodeListDistribGD = elementMixtureGD.getElementsByTagName("DistribGD");
			for (int d = 0; d < nodeListDistribGD.getLength(); d++) {
				Node nodeDistribGD = nodeListDistribGD.item(d);
				if (nodeDistribGD instanceof Element) {
					Element elementDistribGD = (Element) nodeDistribGD;
					double w = Double.parseDouble(elementDistribGD.getAttribute("weight"));
					DiagGaussian gaussian = (DiagGaussian) gmm.getComponent(d);
					gaussian.initialize();
					gmm.getComponent(d).setWeight(w);

					NodeList nodeListCovInv = elementDistribGD.getElementsByTagName("covInv");
					for (int i = 0; i < nodeListCovInv.getLength(); i++) {
						Node nodeCovInv = nodeListCovInv.item(i);
						if (nodeCovInv instanceof Element) {
							Element elementCovInv = (Element) nodeCovInv;
							int idx = Integer.parseInt(elementCovInv.getAttribute("i"));
							double covInv = Double.parseDouble(elementCovInv.getTextContent());
							gaussian.setCovariance(idx, idx, 1.0 / covInv);
						}
					}

					NodeList nodeListMean = elementDistribGD.getElementsByTagName("mean");
					for (int i = 0; i < nodeListMean.getLength(); i++) {
						Node nodeMean = nodeListMean.item(i);
						if (nodeMean instanceof Element) {
							Element elementMean = (Element) nodeMean;
							int idx = Integer.parseInt(elementMean.getAttribute("i"));
							double mean = Double.parseDouble(elementMean.getTextContent());
							gaussian.setMean(idx, mean);
						}
					}
					gaussian.computeInvertCovariance();
					gaussian.setGLR();
					gaussian.computeLikelihoodConstant();
				}
			}
			gmmList.add(gmm);
		}
	}

	/**
	 * Read alize gmm.
	 * 
	 * @param file the file
	 * @param name the name
	 * @return the gmm
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static GMM readAlizeGMM(IOFile file, String name) throws IOException, DiarizationException {
		int c, v;

		// number of distributions
		int distribCount = file.readInt();

		// vector size
		int vectSize = file.readInt();
		logger.info("ModelIO: readAlizeGMM() : vectSize = " + vectSize + " distribCount = " + distribCount);

		GMM gmm = new GMM(distribCount, vectSize);
		gmm.setName(name);

		// distribution weights
		for (c = 0; c < distribCount; c++) {
			DiagGaussian gaussian = (DiagGaussian) gmm.getComponent(c);
			gaussian.initialize();
		}
		for (c = 0; c < distribCount; c++) {
			double w = file.readDouble();
			gmm.getComponent(c).setWeight(w);
			// logger.info("ModelIO: readAlizeGMM() : c = " +c + " w = " +w);
		}
		double t = 0.0d;

		for (c = 0; c < distribCount; c++) {
			DiagGaussian gaussian = (DiagGaussian) gmm.getComponent(c);
			// gaussian.initializeModel();

			file.readDouble();

			file.readDouble();

			// covariance
			byte code = file.readByte();
			// logger.info("ModelIO: readAlizeGMM() : code = " +code);
			if (code == (byte) 1) {
				for (v = 0; v < vectSize; v++) {
					t = file.readDouble();
					// logger.info("ModelIO: readAlizeGMM() : v = " +v + " t = " +t);
					gaussian.setCovariance(v, v, t);
				}
			}

			// inverse covariance
			for (v = 0; v < vectSize; v++) {

				double t1;
				t1 = file.readDouble();
				t = 1.0d / t1;
				// logger.info("ModelIO: readAlizeGMM() : v = " +v + "t1 =" + t1 + " ttt = " +t);
				gaussian.setCovariance(v, v, t);
			}

			// mean
			for (v = 0; v < vectSize; v++) {
				t = file.readDouble();
				// logger.info("ModelIO: readAlizeGMM() : v = " +v + " t = " +t);
				gaussian.setMean(v, t);
			}

			gaussian.computeInvertCovariance();
			gaussian.setGLR();
			gaussian.computeLikelihoodConstant();
		}
		return gmm;

	}

	/**
	 * Reader of a GMM in AMIRAL/LIA format (old format, non public format).
	 * 
	 * @param file the file
	 * 
	 * @return the GMM
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 * @deprecated
	 */
	@Deprecated
	public static GMM readAmiral(IOFile file) throws IOException, DiarizationException {
		String lia = "LIA Modele Multidistrib.";
		String LIAIdent = file.readString(24);
		file.readChar();
		if (LIAIdent != lia) {
			throw new DiarizationException("GMM: readAmiral() error of identification ");
		}

		file.readShort(); // swap

		if (file.readChar() != 2) { // version
			throw new DiarizationException("GMM: readAmiral() error of version ");
		}
		file.readInt(); // length
		int lenInfo = file.readInt();
		if (lenInfo > 0) {
			file.readString(lenInfo - 1); // info
			file.readChar();
		}
		int nbComp = file.readInt();
		short dim = file.readShort();
		int nbfeaturesTrain = file.readInt();
		MatrixRowVector mw = new MatrixRowVector(nbComp);
		for (int i = 0; i < nbComp; i++) {
			mw.set(i, file.readDouble());
		}
		int kind = Gaussian.DIAG;
		GMM gmm = new GMM(0, dim, kind);
		for (int c = 0; c < nbComp; c++) {
			char typeDist = file.readChar();
			if (typeDist != 1) {
				throw new DiarizationException("GMM: readAmiral() error of distribution type ");
			}
			file.readDouble(); // cst
			char diag = file.readChar();
			if (diag != 1) {
				kind = Gaussian.FULL;
				gmm.setKind(kind);
			}
			Gaussian g = gmm.addNewComponent();
			g.setWeight(mw.get(c));
			g.initialize();
			g.setCount(nbfeaturesTrain);
			for (int i = 0; i < dim; i++) { // cov
				for (int j = i; j < ((kind == Gaussian.FULL) ? dim : i + 1); j++) {
					g.setCovariance(i, j, file.readDouble());
				}
			}
			for (int i = 0; i < dim; i++) { // invert cov
				for (int j = i; j < ((kind == Gaussian.FULL) ? dim : i + 1); j++) {
					file.readDouble();
				}
			}
			for (int j = 0; j < dim; j++) { // mean
				g.setMean(j, file.readDouble());
			}
			g.computeInvertCovariance();
			g.setGLR();
			g.computeLikelihoodConstant();
			file.readDouble(); // det
			file.readDouble(); // coeff appartenance des trames a cette
			// gaussienne
		}
		for (int i = 0; i < nbComp; i++) {
			gmm.getComponent(i).setWeight(mw.get(i));
		}
		return gmm;
	}

	/**
	 * Reader of a Gaussian.
	 * 
	 * @param inputFile the file
	 * 
	 * @return the gaussian
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static Gaussian readerGaussien(IOFile inputFile) throws IOException, DiarizationException {
		String key = inputFile.readString(8);
		Gaussian gaussian = null;
		if (key.equals(ModelIO.keyGaussian)) {
			inputFile.readInt(); // read id not use know
			int l = inputFile.readInt();
			String name = inputFile.readString(l);
			String gender = inputFile.readString(1);
			int gaussianKind = inputFile.readInt();
			int dim = inputFile.readInt();
			int count = inputFile.readInt();
			double weight = inputFile.readDouble();
			if (gaussianKind == Gaussian.FULL) {
				gaussian = new FullGaussian(dim);
			} else {
				gaussian = new DiagGaussian(dim);
			}
			gaussian.initialize();
			gaussian.setName(name);
			gaussian.setGender(gender);
			gaussian.setCount(count);
			gaussian.setWeight(weight);
			for (int j = 0; j < dim; j++) {
				double featureCoefficient = inputFile.readDouble();
				gaussian.setMean(j, featureCoefficient);
				for (int t = j; t < ((gaussianKind == Gaussian.FULL) ? dim : j + 1); t++) {
					featureCoefficient = inputFile.readDouble();
					gaussian.setCovariance(j, t, featureCoefficient);
				}
			}

			gaussian.computeInvertCovariance();
			gaussian.setGLR();
			gaussian.computeLikelihoodConstant();
		} else {
			throw new DiarizationException("ModelIO: readGauss() bad id");
		}
		return gaussian;
	}

	/**
	 * Reader of a Gaussian vector.
	 * 
	 * @param inputFile the file
	 * @param gaussianList the gaussian vector
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static void readerGaussianContainer(IOFile inputFile, ArrayList<Gaussian> gaussianList) throws IOException, DiarizationException {
		String key = inputFile.readString(8);
		if (key.equals(keyGaussianContainer)) {
			int size = inputFile.readInt();
			gaussianList.clear();
			for (int i = 0; i < size; i++) {
				gaussianList.add(ModelIO.readerGaussien(inputFile));
			}
		} else {
			throw new DiarizationException("ModelIO: readGaussVect() bad id");
		}
	}

	/**
	 * Reader of a GMM.
	 * 
	 * @param outputFile the file
	 * 
	 * @return the GMM
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static GMM readerGMM(IOFile outputFile) throws IOException, DiarizationException {
		String key = outputFile.readString(8);
		if (key.equals(keyGMM)) {
			outputFile.readInt(); // compatibility with old model
			int l = outputFile.readInt();
			String name = outputFile.readString(l);
			String gender = outputFile.readString(1);
			int gaussainKind = outputFile.readInt();
			int dim = outputFile.readInt();
			int nbComp = outputFile.readInt();
			GMM gaussian = new GMM(nbComp, dim, gaussainKind);
			// g.setId(id);
			gaussian.setName(name);
			gaussian.setGender(gender);
			ModelIO.readerGaussianContainer(outputFile, gaussian.getComponents());
			return gaussian;
		} else {
			logger.warning("ModelIO: readGMM() bad id");
			return new GMM();
		}
	}

	/**
	 * Reader of GMM vector.
	 * 
	 * @param outputFile the file
	 * @param gaussianList the gaussian vector
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static void readerGMMContainer(IOFile outputFile, GMMArrayList gaussianList) throws IOException, DiarizationException {

		// int distribCount = outputFile.readInt();
		String k = outputFile.readString(8);

		if (k.equals(keyGMMContainer)) {
			int size = outputFile.readInt();
			// logger.config("size " +size);
			gaussianList.clear();
			for (int i = 0; i < size; i++) {
				gaussianList.add(ModelIO.readerGMM(outputFile));
			}
		} else {
			throw new DiarizationException("ModelIO: readGMMVect() bad id ");
		}
	}

	/**
	 * Reader of GMM vector.
	 * 
	 * @param outputFile the file
	 * @param gaussianList the gaussian vector
	 * @param name the name
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static void readerGMMContainerALIZE(IOFile outputFile, GMMArrayList gaussianList, String name) throws IOException, DiarizationException {

		// logger.config("size " +size);
		gaussianList.clear();
		for (int i = 0; i < 1; i++) {
			gaussianList.add(ModelIO.readAlizeGMM(outputFile, name));
		}
		/*
		 * } else { throw new DiarizationException("ModelIO: readGMMVect() bad id " +distribCount); }
		 */
	}

	/**
	 * Writer of Gaussian.
	 * 
	 * @param outputFile the file
	 * @param gaussian the gaussian
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static void writerGaussian(IOFile outputFile, Gaussian gaussian) throws IOException, DiarizationException {
		outputFile.writeString(keyGaussian, keyGaussian.length());

		int dim = gaussian.getDimension();
		int kind = gaussian.getGaussianKind();
		outputFile.writeInt(gaussian.getName().hashCode()); // Compatibility with old models
		String name = gaussian.getName();
		// int l = name.length();

		// outputFile.writeInt(l);
		outputFile.writeStringAndLenght(name);
		outputFile.writeString(gaussian.getGender());
		outputFile.writeInt(kind);
		outputFile.writeInt(dim);
		outputFile.writeInt(gaussian.getCount());
		outputFile.writeDouble(gaussian.getWeight());
		for (int j = 0; j < dim; j++) {
			double featureCoefficient = gaussian.getMean(j);
			outputFile.writeDouble(featureCoefficient);
			for (int k = j; k < ((kind == Gaussian.FULL) ? dim : j + 1); k++) {
				featureCoefficient = gaussian.getCovariance(j, k);
				outputFile.writeDouble(featureCoefficient);
			}
		}
	}

	/**
	 * Writer of Gaussian vector.
	 * 
	 * @param outputFile the file
	 * @param gaussianList the gaussian vector
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static void writerGaussianContainer(IOFile outputFile, ArrayList<Gaussian> gaussianList) throws IOException, DiarizationException {
		outputFile.writeString(keyGaussianContainer, keyGaussianContainer.length());
		outputFile.writeInt(gaussianList.size());
		for (Gaussian gaussian : gaussianList) {
			ModelIO.writerGaussian(outputFile, gaussian);
		}
	}

	/**
	 * Writer of GMM.
	 * 
	 * @param outputFile the file
	 * @param gmmm the gaussian
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static void writerGMM(IOFile outputFile, GMM gmmm) throws IOException, DiarizationException {
		outputFile.writeString(keyGMM, keyGMM.length());
		outputFile.writeInt(gmmm.getName().hashCode());// Compatibility with old model
		String name = gmmm.getName();
		// int l = name.length();
		// outputFile.writeInt(l);
		outputFile.writeStringAndLenght(name);
		outputFile.writeString(gmmm.getGender());
		outputFile.writeInt(gmmm.getGaussianKind());
		outputFile.writeInt(gmmm.getDimension());
		outputFile.writeInt(gmmm.getNbOfComponents());
		ModelIO.writerGaussianContainer(outputFile, gmmm.getComponents());
	}

	/**
	 * Writer of GMM vector.
	 * 
	 * @param outputFile the file
	 * @param gmmList the gaussian vector
	 * 
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static void writerGMMContainer(IOFile outputFile, GMMArrayList gmmList) throws IOException, DiarizationException {
		outputFile.writeString(keyGMMContainer, keyGMMContainer.length());
		outputFile.writeInt(gmmList.size());
		for (GMM gmm : gmmList) {
			ModelIO.writerGMM(outputFile, gmm);
		}
	}

}
