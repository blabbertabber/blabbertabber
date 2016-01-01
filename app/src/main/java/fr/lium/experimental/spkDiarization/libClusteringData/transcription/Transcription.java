package fr.lium.experimental.spkDiarization.libClusteringData.transcription;

import java.util.TreeMap;

/**
 * The Class Transcription.
 */
public class Transcription implements Cloneable {

	/** The link set. */
	protected LinkSet linkSet;

	/** The entity set. */
	protected EntitySet entitySet;

	/** The information. */
	protected TreeMap<String, Object> information;

	/**
	 * Instantiates a new transcription.
	 */
	public Transcription() {
		super();
		linkSet = new LinkSet(-1);
		entitySet = new EntitySet();
		this.information = new TreeMap<String, Object>();
	}

	/**
	 * Gets the link set.
	 * 
	 * @return the linkSet
	 */
	public LinkSet getLinkSet() {
		return linkSet;
	}

	/**
	 * Sets the link set.
	 * 
	 * @param linkSet the linkSet to set
	 */
	public void setLinkSet(LinkSet linkSet) {
		this.linkSet = linkSet;
	}

	/**
	 * Gets the entity set.
	 * 
	 * @return the entitySet
	 */
	public EntitySet getEntitySet() {
		return entitySet;
	}

	/**
	 * Sets the entity set.
	 * 
	 * @param entitySet the entitySet to set
	 */
	public void setEntitySet(EntitySet entitySet) {
		this.entitySet = entitySet;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		Transcription result = new Transcription();
		result.linkSet = (LinkSet) linkSet.clone();
		result.entitySet = (EntitySet) entitySet.clone();

		for (String key : information.descendingKeySet()) {
			Object value = information.get(key);
			result.information.put(key, value);
		}
		return result;
	}

	/**
	 * Sets an information.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	public void setInformation(String key, Object value) {
		this.information.put(key, value);
	}

	/**
	 * Gets the information.
	 * 
	 * @return the information
	 */
	public TreeMap<String, Object> getInformation() {
		return this.information;
	}

	/**
	 * Gets the information.
	 * 
	 * @param key the key
	 * 
	 * @return the information
	 */
	public Object getInformation(Object key) {
		return this.information.get(key);
	}

	/**
	 * Gets the information.
	 * 
	 * @param key the key
	 * 
	 * @return the information
	 */
	public String getInformation(String key) {
		if (this.information.get(key) != null) {
			return this.information.get(key).toString();
		} else {
			return null;
		}
	}

}
