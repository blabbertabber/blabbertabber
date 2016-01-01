/**
 * 
 * <p>
 * XMLInputOutput
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
 *          Find silence and split a segmentation
 * 
 */
package fr.lium.experimental.EPAC.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Entity;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.EntitySet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Path;
import fr.lium.experimental.spkDiarization.libSCTree.SCTProbabilities;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;

// TODO: Auto-generated Javadoc
/**
 * The Class XMLInputOutput.
 */
public class XmlEPACInputOutput {
	// private final static Logger logger = Logger.getLogger(XmlEPACInputOutput.class.getName());

	/** The list of tools. */
	LinkedList<Tool> tools;

	/** The segment id. */
	int segmentId;

	/** The XML document. */
	Document document;

	/**
	 * Instantiates a new xML input output.
	 */
	public XmlEPACInputOutput() {
		tools = new LinkedList<Tool>();
	}

	/**
	 * Read tool.
	 * 
	 * @param owner the owner
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void readTool(Element owner) throws DiarizationException {
		// read tool
		NodeList toolList = owner.getElementsByTagName("tool");
		for (int i = 0; i < toolList.getLength(); i++) {
			Node node = toolList.item(i);
			if (node instanceof Element) {
				tools.add(new Tool((Element) node));
			}
		}
	}

	/**
	 * Write tool.
	 * 
	 * @param owner the owner
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void writeTool(Element owner) throws DiarizationException {
		// read tool
		Element toolsElement = document.createElement("tools");
		owner.appendChild(toolsElement);
		for (Tool tool : tools) {

			Element toolElement = document.createElement("tool");
			toolsElement.appendChild(toolElement);
			tool.write(toolElement);
		}
	}

	/**
	 * Read speaker information.
	 * 
	 * @param speaker the speaker xml node
	 * @param clusterSet the cluster set
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void readSpeaker(Element speaker, ClusterSet clusterSet) throws DiarizationException {
		String name = speaker.getAttribute("name");
		String identity = speaker.getAttribute("identity");
		Cluster cluster = clusterSet.createANewCluster(name);
		cluster.setInformation("XMLSpeakerIdentity", identity);
		cluster.setGender(speaker.getAttribute("gender"));
	}

	/**
	 * Write speaker.
	 * 
	 * @param speakers xml root node for speakers
	 * @param cluster the cluster
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void writeSpeaker(Element speakers, Cluster cluster) throws DiarizationException {
		Element speaker = document.createElement("speaker");
		speakers.appendChild(speaker);
		speaker.setAttribute("type", "generic label");
		speaker.setAttribute("name", cluster.getName());
		String identity = cluster.getInformation("XMLSpeakerIdentity");
		if (identity == null) {
			identity = "";
		}
		speaker.setAttribute("identity", identity);
		speaker.setAttribute("gender", cluster.getGender());
		speaker.setAttribute("generator", "auto");
	}

	/**
	 * Write a entity.
	 * 
	 * @param entity the entity
	 * @param self the father xml node
	 */
	public void writeEntity(Entity entity, Element self) {
		String entityType = entity.getType().replace("entity.", "");
		self.setAttribute("type", entityType);
		Integer id = entity.getId();
		self.setAttribute("id", id.toString());
		for (Path path : entity) {
			Element pathElement = document.createElement("path");
			pathElement.setAttribute("graph", String.valueOf(path.getIdOfLinkSet()));
			pathElement.setAttribute("link", String.valueOf(path.getIdLink()));
			self.appendChild(pathElement);
		}

		for (String name : entity.getScores().keySet()) {
			Element scoresElement = document.createElement("scores");
			scoresElement.setAttribute("name", name);
			self.appendChild(scoresElement);
			SCTProbabilities score = entity.getScore(name);
			for (String key : score.keySet()) {
				Element scoreElement = document.createElement("score");
				scoreElement.setAttribute("key", key);
				scoreElement.setAttribute("value", String.valueOf(score.get(key)));
				scoresElement.appendChild(scoreElement);
			}
		}
	}

	/**
	 * Read an entity.
	 * 
	 * @param father the father xml node
	 * @param linkSet the set of links
	 * @param entitySet the set of entities
	 * 
	 * @return the entity
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public Entity readEntity(Element father, LinkSet linkSet, EntitySet entitySet) throws DiarizationException {
		String type = father.getAttribute("type");
		int id = Integer.parseInt(father.getAttribute("id"));

		type = "entity." + type;

		// TODO: a supprimer d'ici
		if (type.equals(EntitySet.TypeEster2Amount)) {
			return null;
		}
		if (type.equals(EntitySet.TypeEster2Time)) {
			return null;
		}
		// if (type.equals(EntitySet.TypeEster2Fonction)) {
		// return null;
		// }
		if (type.equals(EntitySet.TypeUnknown)) {
			return null;
		}
		if (type.equals(EntitySet.TypeEster2Production)) {
			return null;
		}

		father.setAttribute("generator", "auto");
		Entity entity = new Entity(linkSet, type, id);

		NodeList pathList = father.getElementsByTagName("path");
		for (int k = 0; k < pathList.getLength(); k++) {
			Node node = pathList.item(k);
			if (node instanceof Element) {
				int graph = Integer.parseInt(((Element) node).getAttribute("graph"));
				int link = Integer.parseInt(((Element) node).getAttribute("link"));
				entity.addPath(graph, link);
			}
		}

		NodeList scoresList = father.getElementsByTagName("scores");
		for (int k = 0; k < scoresList.getLength(); k++) {
			Node nodeScores = scoresList.item(k);
			if (nodeScores instanceof Element) {
				NodeList scoreList = ((Element) nodeScores).getElementsByTagName("score");
				String name = ((Element) nodeScores).getAttribute("name");
				SCTProbabilities probabilities = entity.getScore(name);
				for (int j = 0; j < scoreList.getLength(); j++) {
					Node nodeScore = scoreList.item(j);
					if (nodeScore instanceof Element) {
						String key = ((Element) nodeScore).getAttribute("key");
						String v = ((Element) nodeScore).getAttribute("value");
						double value = Double.parseDouble(v);
// logger.info(name+" = "+key+" --> "+value);
						probabilities.put(key, value);
					}
				}

			}
		}

		entitySet.add(entity);
		//
		return entity;
	}

	/**
	 * Read the entities.
	 * 
	 * @param linkSet the link set
	 * @param segment the father xml node
	 * 
	 * @return the entity set
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public EntitySet readEntities(LinkSet linkSet, Element segment) throws DiarizationException {
		EntitySet entitySet = new EntitySet();
		NodeList neList = segment.getElementsByTagName("entity");
		for (int k = 0; k < neList.getLength(); k++) {
			Node node = neList.item(k);
			if (node instanceof Element) {
				readEntity((Element) node, linkSet, entitySet);
			}
		}
		return entitySet;
	}

	/**
	 * Read a graph.
	 * 
	 * @param segment the father xml node
	 * 
	 * @return the link set corresponding to a graph
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public LinkSet readGraph(Element segment) throws DiarizationException {
		LinkSet linkSet = new LinkSet(0);
		NodeList graphs = segment.getElementsByTagName("graph");
		if (graphs.getLength() > 0) {
			if (graphs.item(0) instanceof Element) {
				Element graph = (Element) graphs.item(0);
				int id = Integer.parseInt(graph.getAttribute("id"));
				if (graph.getAttribute("type").equals("1-best") == true) {
// int linkCount = Integer.parseInt(graph.getAttribute("linkCount"));
					linkSet = new LinkSet(id);
					NodeList links = graph.getElementsByTagName("link");
					for (int k = 0; k < links.getLength(); k++) {
						Node node = links.item(k);
						if (node instanceof Element) {
							Element link = (Element) node;
							int idLink = Integer.parseInt(link.getAttribute("id"));
							int linkStart = Integer.parseInt(link.getAttribute("start"));
							int linkEnd = Integer.parseInt(link.getAttribute("end"));
							double linkProba = Double.parseDouble(link.getAttribute("probability"));
							String linkType = link.getAttribute("type");
							String word = link.getTextContent();
							linkSet.newLink(idLink, linkStart, linkEnd, linkType, linkProba, word);
						}
					}
				}
			}
		}

		return linkSet;
	}

	/**
	 * Write graph.
	 * 
	 * @param segmentElement the father xml node
	 * @param segment the segment containing the graph
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void writeGraph(Element segmentElement, Segment segment) throws DiarizationException {
		LinkSet sausageSet = segment.getTranscription().getLinkSet();
		if (sausageSet.size() > 0) {
			Element graphs = document.createElement("graph");
			segmentElement.appendChild(graphs);
			graphs.setAttribute("id", String.valueOf(sausageSet.getId()));
			graphs.setAttribute("type", "1-best");
			graphs.setAttribute("generator", "auto");
			String txt = "";
			for (int i = 0; i < sausageSet.size(); i++) {
				Link link = sausageSet.getLink(i);
				Element linkElement = document.createElement("link");
				graphs.appendChild(linkElement);
				linkElement.setAttribute("id", String.valueOf(link.getId()));
				linkElement.setAttribute("start", String.valueOf(link.getStart()));
				linkElement.setAttribute("end", String.valueOf(link.getEnd()));
				linkElement.setAttribute("probability", String.valueOf(link.getProbability()));
				linkElement.setAttribute("type", link.getType());
				linkElement.setTextContent(link.getWord());
				txt += " " + link.getWord();
			}
			Element text = document.createElement("text");
			segmentElement.appendChild(text);
			text.setTextContent(txt);
		}
	}

	/**
	 * Write the entities.
	 * 
	 * @param segmentElement the father xml node
	 * @param segment the segment containing the graph
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void writeEntities(Element segmentElement, Segment segment) throws DiarizationException {
		EntitySet entities = segment.getTranscription().getEntitySet();
		if (entities.size() > 0) {
			Element entitiesElement = document.createElement("entities");
			segmentElement.appendChild(entitiesElement);
			entitiesElement.setAttribute("generator", "auto");
			for (Entity entity : entities) {
				Element entityElement = document.createElement("entity");
				writeEntity(entity, entityElement);
				entitiesElement.appendChild(entityElement);
			}
		}
	}

	/**
	 * Read a segment.
	 * 
	 * @param showName the name of the show
	 * @param segmentElement the segment xml element node
	 * @param clusterSet the cluster set
	 * @param rate the rate
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void readSegment(String showName, Element segmentElement, ClusterSet clusterSet, float rate) throws DiarizationException {
		// read segment
		String speaker = segmentElement.getAttribute("speaker");
		String bandwidth = segmentElement.getAttribute("bandwidth");
		double dStart = Double.valueOf(segmentElement.getAttribute("start"));
		double dEnd = Double.valueOf(segmentElement.getAttribute("end"));
		int start = (int) Math.round(dStart * 100.0);
		int end = (int) Math.round(dEnd * 100.0);
		int len = end - start;
		Cluster cluster = clusterSet.getCluster(speaker);
		Segment segment = new Segment(showName, start, len, cluster, rate);
		segment.setBandwidth(bandwidth);
		cluster.addSegment(segment);
		LinkSet linkSet = readGraph(segmentElement);
		segment.getTranscription().setLinkSet(linkSet);
		EntitySet entitiesSet = readEntities(linkSet, segmentElement);
		segment.getTranscription().setEntitySet(entitiesSet);
		for (Entity entity : entitiesSet) {
			for (Path path : entity) {
				linkSet.getLink(path.getIdLink()).setEntity(entity);
			}
		}
	}

	/**
	 * Write a segment.
	 * 
	 * @param segmentsElement the father xml node
	 * @param segment the segment
	 * @param showName the name of the show
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void writeSegment(Element segmentsElement, Segment segment, String showName) throws DiarizationException {
		// read segment
		if (segment.getShowName().equals(showName)) {
			Element segmentElement = document.createElement("segment");
			segmentsElement.appendChild(segmentElement);
			segmentElement.setAttribute("start", String.valueOf(segment.getStartInSecond()));
			segmentElement.setAttribute("end", String.valueOf(segment.getEndInSecond()));
			segmentElement.setAttribute("speaker", segment.getCluster().getName());
			segmentElement.setAttribute("bandwidth", segment.getBandwidth());
			segmentElement.setAttribute("generator", "auto");
			writeGraph(segmentElement, segment);
			writeEntities(segmentElement, segment);
		}
	}

	/**
	 * Read xml segmentation file.
	 * 
	 * @param clusterSet the cluster set
	 * @param f the file
	 * @param encoding the encoding type
	 * @param rate the rate
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void readXML(ClusterSet clusterSet, File f, Charset encoding, float rate) throws ParserConfigurationException, SAXException, IOException, DiarizationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		FileInputStream fis = new FileInputStream(f);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis, encoding));

		InputSource is = new InputSource(in);
		is.setEncoding(encoding.displayName());
		document = builder.parse(f);

		Element root = document.getDocumentElement();

		readTool(root);

		NodeList shows = root.getElementsByTagName("audiofile");

		for (int j = 0; j < shows.getLength(); j++) {
			Node nodeShow = shows.item(j);
			if (nodeShow instanceof Element) {
				Element show = (Element) nodeShow;
				String showName = show.getAttribute("name");
				// speakers
				NodeList speakers = show.getElementsByTagName("speaker");
				for (int i = 0; i < speakers.getLength(); i++) {
					Node nodeSpeaker = speakers.item(i);
					if (nodeSpeaker instanceof Element) {
						readSpeaker((Element) nodeSpeaker, clusterSet);
					}
				}
				// segments
				NodeList segments = show.getElementsByTagName("segment");
				for (int i = 0; i < segments.getLength(); i++) {
					Node nodeSegment = segments.item(i);
					if (nodeSegment instanceof Element) {
						readSegment(showName, (Element) nodeSegment, clusterSet, rate);
					}
				}
			}
		}
		document = null;
	}

	/**
	 * Write xml segmentation file.
	 * 
	 * @param clusterSet the cluster set
	 * @param f the file
	 * @param encoding the encoding type
	 * 
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the sphinx clust exception
	 * @throws TransformerException the transformer exception
	 */
	public void writeXML(ClusterSet clusterSet, File f, Charset encoding) throws ParserConfigurationException, SAXException, IOException, DiarizationException, TransformerException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		document = builder.newDocument();
		Element root = document.createElement("epac");
		document.appendChild(root);
		writeTool(root);

		segmentId = 0;
		for (String showName : clusterSet.getShowNames()) {
			Element audiofileElement = document.createElement("audiofile");
			audiofileElement.setAttribute("name", showName);
			root.appendChild(audiofileElement);
			// speakers
			Element speakersElement = document.createElement("speakers");
			audiofileElement.appendChild(speakersElement);
			for (String clusterName : clusterSet) {
				writeSpeaker(speakersElement, clusterSet.getCluster(clusterName));
			}
			// segments
			Element segementsElement = document.createElement("segments");
			audiofileElement.appendChild(segementsElement);
			for (Segment segment : clusterSet.getSegments()) {
				writeSegment(segementsElement, segment, showName);
			}
		}
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(f);

		// Output to console for testing
		// StreamResult result = new StreamResult(System.out);

		transformer.transform(source, result);
		// save source
		/*
		 * OutputFormat format = new OutputFormat(document); format.setIndenting(true); format.setIndent(2); format.setEncoding(encoding.displayName()); format.setLineWidth(0); Writer output = new BufferedWriter(new OutputStreamWriter(new
		 * FileOutputStream(f), encoding)); XMLSerializer serializer = new XMLSerializer(output, format); serializer.serialize(document); document = null;
		 */
	}

}
