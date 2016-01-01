package fr.lium.experimental.spkDiarization.programs;

import java.io.File;
import java.nio.charset.Charset;

import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;

/**
 * The Class testXmlRepere.
 */
public class testXmlRepere {

	/** The Constant XML_ENCODING. */
	private static final Charset XML_ENCODING = Charset.forName("ISO-8859-1");

	/** The Constant XML_RATE. */
	private static final float XML_RATE = 100;

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		SpkDiarizationLogger.setup();
		SpkDiarizationLogger.setLevel("FINEST");
		File f = new File("/Users/meignier/LCP_TopQuestions_2011-05-11_213800_dev0.turn.sc.head.ocr.surf.jfa.xml");
		ClusterSet clusterSet = new ClusterSet();
		clusterSet.readXmlREPERE(f, XML_ENCODING, XML_RATE);
	}

}
