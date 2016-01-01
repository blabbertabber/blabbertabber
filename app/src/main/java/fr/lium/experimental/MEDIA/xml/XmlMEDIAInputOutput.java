package fr.lium.experimental.MEDIA.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;

/**
 * The Class XmlMEDIAInputOutput.
 */
public class XmlMEDIAInputOutput {
	/** The XML document. */
	Document document;

	/**
	 * Read xml.
	 * 
	 * @param clusterSet the cluster set
	 * @param f the f
	 * @param encoding the encoding
	 * @param rate the rate
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the sAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void readXML(ClusterSet clusterSet, File f, Charset encoding, float rate) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		FileInputStream fis = new FileInputStream(f);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis, encoding));

		InputSource is = new InputSource(in);
		is.setEncoding(encoding.displayName());
		document = builder.parse(f);

		Element root = document.getDocumentElement();

		readDialogs(root, clusterSet, rate);

		document = null;
	}

	/**
	 * Read dialogs.
	 * 
	 * @param root the root
	 * @param clusterSet the cluster set
	 * @param rate the rate
	 */
	public void readDialogs(Element root, ClusterSet clusterSet, float rate) {
		NodeList dialogs = root.getElementsByTagName("dialogue");
		for (int j = 0; j < dialogs.getLength(); j++) {
			Node nodeDialog = dialogs.item(j);
			if (nodeDialog instanceof Element) {
				Element dialog = (Element) nodeDialog;
				// <dialogue id="1004" audioFilename="08730_1004.wav" channelLeft="woz" channelRight="spk" nameSpk="speaker#1_08730_1004" nameWoz="comp?re2"
				// startTime="0" endTime="132.370">

				String id = dialog.getAttribute("id");
				String show = dialog.getAttribute("audioFilename").split("[._]")[1];
				String channelLeft = dialog.getAttribute("channelLeft");
				String nameSpk = dialog.getAttribute("nameSpk");
				String nameWoz = dialog.getAttribute("nameWoz");

				nameSpk = show + "_" + nameSpk;
				nameWoz = show + "_" + nameWoz;
				if (channelLeft.equals("woz")) {
					nameSpk += "_R";
					nameWoz += "_L";
				} else {
					nameSpk += "_L";
					nameWoz += "_R";
				}

				clusterSet.setInformation("id", id);
				Cluster speaker = clusterSet.createANewCluster(nameSpk);
				Cluster woz = clusterSet.createANewCluster(nameWoz);

				readTurn(dialog, speaker, woz, show, rate);
			}
		}
	}

	/**
	 * Read turn.
	 * 
	 * @param dialog the dialog
	 * @param speaker the speaker
	 * @param woz the woz
	 * @param show the show
	 * @param rate the rate
	 */
	public void readTurn(Element dialog, Cluster speaker, Cluster woz, String show, float rate) {
		NodeList turns = dialog.getElementsByTagName("turn");
		for (int j = 0; j < turns.getLength(); j++) {
			Node nodeTurn = turns.item(j);
			if (nodeTurn instanceof Element) {
				Element turn = (Element) nodeTurn;
				// <turn id="1004_1_woz" startTime="0" endTime="4.529" speaker="woz" audioFilename="./audio/1004/1004.l.0_452.wav">
				// String id = turn.getAttribute("id");
				int start = Math.round(Float.valueOf(turn.getAttribute("startTime")) * 100);
				int end = Math.round(Float.valueOf(turn.getAttribute("endTime")) * 100);
				int lenght = end - start;
				String speakerName = turn.getAttribute("speaker");
				Cluster current = speaker;
				if (speakerName.equals("woz")) {
					current = woz;
				}
				Segment segment = new Segment(show, start, lenght, current, rate);
				// segment.setInformation("id", id);
				String text = "";
				NodeList tanscriptions = turn.getElementsByTagName("transcription");
				for (int i = 0; i < tanscriptions.getLength(); i++) {
					Node nodeTranscription = tanscriptions.item(i);

					if (nodeTranscription instanceof Element) {
						Element transcription = (Element) nodeTranscription;
						if (nodeTranscription.getParentNode().getNodeName().equals("turn")
								&& transcription.getAttribute("origin").equals("ELDA")
								&& transcription.getAttribute("manual").equals("true")) {
							text += transcription.getTextContent() + " ";
						}
					}
				}
				if (text.isEmpty() == false) {
					segment.setInformation("trans", text.replaceAll("\\s+", " "));
				}
				current.addSegment(segment);

			}
		}
	}

	/**
	 * Write xml.
	 * 
	 * @param clusterSet the cluster set
	 * @param f the f
	 * @param encoding the encoding
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void writeXML(ClusterSet clusterSet, File f, Charset encoding) throws IOException {
		// TODO Auto-generated method stub
		throw new IOException("writeXML not developed");
	}

}