package fr.lium.experimental.REPERE.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Transcription;
import fr.lium.experimental.spkDiarization.libNamedSpeaker.SpeakerNameUtils;
import fr.lium.experimental.spkDiarization.libSCTree.SCTProbabilities;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.libClusteringData.Cluster;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.libModel.ModelScores;

/**
 * The Class XmlREPEREInputOutput.
 */
public class XmlREPEREInputOutput {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(XmlREPEREInputOutput.class.getName());

	/** The Constant WRITING_KEY. */
	public final static String WRITING_KEY = "writing";

	/** The list of tools. */
	LinkedList<Tool> tools;

	/** The segment id. */
	int segmentId;

	/** The XML document. */
	Document document;

	/**
	 * Instantiates a new xML input output.
	 */
	public XmlREPEREInputOutput() {
		tools = new LinkedList<Tool>();
	}

	/**
	 * Read speaker information.
	 * 
	 * @param person the person
	 * @param audioClusterSet the audio cluster set
	 * @param videoClusterSet the video cluster set
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void readPerson(Element person, ClusterSet audioClusterSet, ClusterSet videoClusterSet) throws DiarizationException {
		String name = person.getAttribute("name");
		String identity = person.getAttribute("identity");
		logger.finest("Person :" + name + ", " + identity);

		Cluster audioCluster = audioClusterSet.createANewCluster(name);
		if (identity.isEmpty() == false) {
			audioCluster.setInformation("XMLSpeakerIdentity", identity);
		}
		audioCluster.setGender(person.getAttribute("gender"));

		Cluster videoCluster = videoClusterSet.createANewCluster(name);
		if (identity.isEmpty() == false) {
			videoCluster.setInformation("XMLSpeakerIdentity", identity);
		}
		videoCluster.setGender(person.getAttribute("gender"));

		NodeList scoresList = person.getElementsByTagName("scores_audio");
		for (int i = 0; i < scoresList.getLength(); i++) {
			Node nodeScores = scoresList.item(i);
			if (nodeScores instanceof Element) {
// logger.finest("scores audio ");
				ModelScores modelScores = readModelScores(((Element) nodeScores));
				audioCluster.setModelScores(modelScores);
			}
		}
		scoresList = person.getElementsByTagName("scores_video");
		for (int i = 0; i < scoresList.getLength(); i++) {
			Node nodeScores = scoresList.item(i);
			if (nodeScores instanceof Element) {
// logger.finer("scores video ");
				ModelScores modelScores = readModelScores(((Element) nodeScores));
				videoCluster.setModelScores(modelScores);
			}
		}

	}

	/**
	 * Write speaker.
	 * 
	 * @param persons the persons
	 * @param cluster the cluster
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void writePerson(Element persons, Cluster cluster) throws DiarizationException {
		Element person = document.createElement("person");
		persons.appendChild(person);
		person.setAttribute("type", "generic label");
		person.setAttribute("name", cluster.getName());
		String identity = cluster.getInformation("XMLSpeakerIdentity");
		if (identity != null) {
			person.setAttribute("identity", identity);
		}
		person.setAttribute("gender", cluster.getGender());
		person.setAttribute("generator", "auto");
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
		/*
		 * if (type.equals(EntitySet.TypeEster2Amount)) { return null; } if (type.equals(EntitySet.TypeEster2Time)) { return null; } //if (type.equals(EntitySet.TypeEster2Fonction)) { // return null; //} if (type.equals(EntitySet.TypeUnknown)) {
		 * return null; } if (type.equals(EntitySet.TypeEster2Production)) { return null; }
		 */

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
		readEntityScores(father, entity);

		entitySet.add(entity);
		//
		return entity;
	}

	/**
	 * Read entity scores.
	 * 
	 * @param father the father
	 * @param entity the entity
	 */
	public void readEntityScores(Element father, Entity entity) {
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
// logger.finest(name+" = "+key+" --> "+value);
						probabilities.put(key, value);
					}
				}

			}
		}
	}

	/**
	 * Read model scores.
	 * 
	 * @param scores the scores
	 * @return the model scores
	 * @throws DiarizationException the diarization exception
	 */
	public ModelScores readModelScores(Element scores) throws DiarizationException {
		ModelScores modelScores = new ModelScores();

		NodeList scoreList = scores.getElementsByTagName("score");
		for (int k = 0; k < scoreList.getLength(); k++) {
			Node node = scoreList.item(k);
			if (node instanceof Element) {
				String key = SpeakerNameUtils.normalizeSpeakerName(((Element) node).getAttribute("key"));
				Double value = Double.valueOf(((Element) node).getAttribute("value"));
				logger.finest("score :" + key + " = " + value);
				modelScores.put(key, value);
			}
		}
		return modelScores;
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
					// String ch = "";
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
							logger.finest("graph id: " + id + " start: " + linkStart + " end: " + linkEnd + " type: "
									+ linkType);

							String word = link.getTextContent();
							word = word.replaceAll("\\s+", " ").replaceAll("\\s+", " ").trim();
							logger.finest("graph word: " + word);

							// ch += " "+word;
							linkSet.newLink(idLink, linkStart, linkEnd, linkType, linkProba, word);
						}
					}
					// logger.finest("graph: "+ch);
				} else {
					logger.warning("graph type is not 1-best :" + graph.getAttribute("type"));
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
		String speaker = segmentElement.getAttribute("person");
		String bandwidth = segmentElement.getAttribute("bandwidth");
		double dStart = Double.valueOf(segmentElement.getAttribute("start"));
		double dEnd = Double.valueOf(segmentElement.getAttribute("end"));
		logger.finest("segment spk: " + speaker + " start:" + dStart + " end: " + dEnd);

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
		// cluster.debug(9);
	}

	/**
	 * Read a head.
	 * 
	 * @param showName the name of the show
	 * @param headElement the segment xml element node
	 * @param videoClusterSet the cluster set
	 * @param rate the rate
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void readHead(String showName, Element headElement, ClusterSet videoClusterSet, float rate) throws DiarizationException {
		// read segment
		String person = headElement.getAttribute("person");
		double dStart = Double.valueOf(headElement.getAttribute("start"));
		double dEnd = Double.valueOf(headElement.getAttribute("end"));
		int start = (int) Math.round(dStart * 100.0);
		int end = (int) Math.round(dEnd * 100.0);
		int len = end - start;
		logger.finest("person:" + person + " start:" + dStart);
		Cluster cluster = videoClusterSet.getCluster(person);
		Segment segment = new Segment(showName, start, len, cluster, rate);

		if (!headElement.getAttribute("x1").isEmpty()) {
			double x1 = Double.valueOf(headElement.getAttribute("x1"));
			double x2 = Double.valueOf(headElement.getAttribute("x2"));
			double y1 = Double.valueOf(headElement.getAttribute("y1"));
			double y2 = Double.valueOf(headElement.getAttribute("y2"));
			double dcenter = Double.valueOf(headElement.getAttribute("center"));
			if (x1 != 0.0) {
				segment.setInformation("x1", x1);
				segment.setInformation("x2", x2);
				segment.setInformation("y1", y1);
				segment.setInformation("y2", y2);
				segment.setInformation("dcenter", dcenter);
				segment.setInformation("xc", (x2 - x1) / 2);
				segment.setInformation("yc", (y2 + y1) / 2);
				segment.setInformation("surface", (x2 - x1) * (y2 + y1));
				segment.setInformation("xcenter", Math.abs(512 - ((x2 - x1) / 2)));
			}
		}

		cluster.addSegment(segment);
		// cluster.debug(8);
	}

	/**
	 * Read a writting.
	 * 
	 * @param showName the show name
	 * @param writingElement the writing element
	 * @param writing the writing
	 * @param rate the rate
	 * @throws DiarizationException the sphinx clust exception
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public void readWriting(String showName, Element writingElement, Cluster writing, float rate) throws DiarizationException, CloneNotSupportedException {
		double dStart = Double.valueOf(writingElement.getAttribute("start"));
		double dEnd = Double.valueOf(writingElement.getAttribute("end"));
		int start = (int) Math.round(dStart * 100.0);
		int end = (int) Math.round(dEnd * 100.0);
		int len = end - start;

		logger.finest("writing start:" + dStart + " end:" + dEnd);

		Segment segment = new Segment(showName, start, len, null, rate);
		LinkSet linkSet = readGraph(writingElement);

		EntitySet entitiesSet = readEntities(linkSet, writingElement);
		for (Entity entity : entitiesSet) {
			for (Path path : entity) {
				linkSet.getLink(path.getIdLink()).setEntity(entity);
			}
		}

		Transcription transcription = segment.getTranscription();
		transcription.setLinkSet(linkSet);
		transcription.setEntitySet(entitiesSet);
		transcription.setInformation("start", start);
		transcription.setInformation("len", len);
		transcription.setInformation("end", end);
		transcription.setInformation("type", WRITING_KEY);

		writing.addSegment(segment);

		// on ne place la transcription que s'il y a un et un seul visage.
		// ou on met un score égale a 1/nbTarget ?
		/*
		 * Segment target = null; int nbTraget = 0; for(String person : clusterSet) { Cluster cluster = clusterSet.getCluster(person); for(Segment segment : cluster) { if (segment.getShowName().equals(showName)) { int match =
		 * DiarizationError.match(segment, tmpSegment); if (match > 0) { target = segment; nbTraget++; String str = segment.getInformation(WRITTING_KEY); if (str.isEmpty()) { str = "1"; } else { str = String.valueOf(Integer.getInteger(str) + 1); }
		 * segment.setInformation(WRITTING_KEY, str); Transcription tmpTranscription = (Transcription) transcription.clone(); tmpTranscription.setInformation("match", match); segment.setInformation(WRITTING_KEY+"_"+str, tmpTranscription);
		 * logger.finest("writting person: "+person+" match:"+match+" WRITTING_KEY:"+str+" nbMatch:"+nbTraget); } } } }
		 */
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
	 * Removes the unused cluster.
	 * 
	 * @param clusterSet the cluster set
	 */
	public void removeUnusedCluster(ClusterSet clusterSet) {
		ArrayList<String> list = new ArrayList<String>();

		for (String name : clusterSet) {
			if (clusterSet.getCluster(name).segmentsSize() == 0) {
				list.add(name);
			}
		}
		for (String name : list) {
			logger.finest("--> REMOVE name:" + name);
			// clusterSet.getCluster(name).debug(8);
			clusterSet.removeCluster(name);
		}

	}

	/**
	 * Read xml segmentation file.
	 * 
	 * @param speakerClusterSet the cluster set
	 * @param f the file
	 * @param encoding the encoding type
	 * @param rate the rate
	 * @throws ParserConfigurationException the parser configuration exception
	 * @throws SAXException the SAX exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the sphinx clust exception
	 * @throws CloneNotSupportedException the clone not supported exception
	 */
	public void readXML(ClusterSet speakerClusterSet, File f, Charset encoding, float rate) throws ParserConfigurationException, SAXException, IOException, DiarizationException, CloneNotSupportedException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		FileInputStream fis = new FileInputStream(f);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis, encoding));

		InputSource is = new InputSource(in);
		is.setEncoding(encoding.displayName());
		document = builder.parse(f);

		Element root = document.getDocumentElement();

		ClusterSet headClusterSet = speakerClusterSet.makeHeadClusterSet();
		Cluster writing = speakerClusterSet.makeWriting();

		NodeList shows = root.getElementsByTagName("show");
		logger.finest("**show nb:" + shows.getLength());

		for (int j = 0; j < shows.getLength(); j++) {
			Node nodeShow = shows.item(j);
			if (nodeShow instanceof Element) {
				Element show = (Element) nodeShow;
				String showName = show.getAttribute("name");

				logger.finest("showname:" + showName);
				// speakers
				NodeList persons = show.getElementsByTagName("person");
				for (int i = 0; i < persons.getLength(); i++) {
					Node nodePerson = persons.item(i);
					if (nodePerson instanceof Element) {
						readPerson((Element) nodePerson, speakerClusterSet, headClusterSet);
					}
				}
				logger.finest("--> audio");

				// Audio
				NodeList audios = show.getElementsByTagName("audio");
				for (int k = 0; k < audios.getLength(); k++) {
					Node nodeAudio = audios.item(k);
					if (nodeAudio instanceof Element) {
						Element audio = (Element) nodeAudio;
						// segments
						NodeList segments = audio.getElementsByTagName("segment");
						for (int i = 0; i < segments.getLength(); i++) {
							Node nodeSegment = segments.item(i);
							if (nodeSegment instanceof Element) {
								readSegment(showName, (Element) nodeSegment, speakerClusterSet, rate);
							}
						}
					}
				}
				logger.finest("--> video");
				// video
				NodeList videos = show.getElementsByTagName("video");
				for (int k = 0; k < videos.getLength(); k++) {
					Node nodeVideo = videos.item(k);
					if (nodeVideo instanceof Element) {
						Element video = (Element) nodeVideo;
						// heads
						NodeList heads = video.getElementsByTagName("head");
						for (int i = 0; i < heads.getLength(); i++) {
							Node nodeHead = heads.item(i);
							if (nodeHead instanceof Element) {
								logger.finest("---->>>> add head");
								readHead(showName, (Element) nodeHead, headClusterSet, rate);
							}
						}

						// writings
						NodeList segments = video.getElementsByTagName("writing");
						for (int i = 0; i < segments.getLength(); i++) {
							Node nodeSegment = segments.item(i);
							if (nodeSegment instanceof Element) {
								readWriting(showName, (Element) nodeSegment, writing, rate);
							}
						}
					}
				}

			}
		}
		// headClusterSet.debug(8);
		logger.info("remove speaker without segment");
		removeUnusedCluster(speakerClusterSet);
		logger.info("remove head without segment");
		removeUnusedCluster(headClusterSet);

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
		// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// DocumentBuilder builder = factory.newDocumentBuilder();

		/*
		 * document = builder.newDocument(); Element root = document.createElement("epac"); document.appendChild(root); segmentId = 0; for (String showName : clusterSet.getShowNames()) { Element audiofileElement = document.createElement("audiofile");
		 * audiofileElement.setAttribute("name", showName); root.appendChild(audiofileElement); // speakers Element speakersElement = document.createElement("speakers"); audiofileElement.appendChild(speakersElement); for (String clusterName :
		 * clusterSet) { writeSpeaker(speakersElement, clusterSet.getCluster(clusterName)); } // segments Element segementsElement = document.createElement("segments"); audiofileElement.appendChild(segementsElement); for (Segment segment :
		 * clusterSet.getSegments()) { writeSegment(segementsElement, segment, showName); } } // save source OutputFormat format = new OutputFormat(document); format.setIndenting(true); format.setIndent(2); format.setEncoding(encoding.displayName());
		 * format.setLineWidth(0); Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), encoding)); XMLSerializer serializer = new XMLSerializer(output, format); serializer.serialize(document); document = null;
		 */
	}

}
