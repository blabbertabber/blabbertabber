package fr.lium.experimental.spkDiarization.libNamedSpeaker;

/**
 * The Class AudioInfo.
 */
public class AudioInfo {

	/** The nb head. */
	protected int nbHead;

	/** The central head. */
	protected String centralHead;

	/**
	 * Instantiates a new audio info.
	 * 
	 * @param nbHead the nb head
	 * @param centralHead the central head
	 */
	public AudioInfo(int nbHead, String centralHead) {
		super();
		this.nbHead = nbHead;
		this.centralHead = centralHead;
	}

	/**
	 * Gets the nb head.
	 * 
	 * @return the nbHead
	 */
	public int getNbHead() {
		return nbHead;
	}

	/**
	 * Sets the nb head.
	 * 
	 * @param nbHead the nbHead to set
	 */
	public void setNbHead(int nbHead) {
		this.nbHead = nbHead;
	}

	/**
	 * Gets the central head.
	 * 
	 * @return the centralHead
	 */
	public String getCentralHead() {
		return centralHead;
	}

	/**
	 * Sets the central head.
	 * 
	 * @param centralHead the centralHead to set
	 */
	public void setCentralHead(String centralHead) {
		this.centralHead = centralHead;
	}

}
