package fr.lium.spkDiarization.libFeature;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Class AudioFeatureList.
 */
public class AudioFeatureList extends ArrayList<float[]> {

	/**
	 * Instantiates a new audio feature list.
	 */
	public AudioFeatureList() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new audio feature list.
	 * 
	 * @param c the c
	 */
	public AudioFeatureList(Collection<? extends float[]> c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new audio feature list.
	 * 
	 * @param initialCapacity the initial capacity
	 */
	public AudioFeatureList(int initialCapacity) {
		super(initialCapacity);
		// TODO Auto-generated constructor stub
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

}
