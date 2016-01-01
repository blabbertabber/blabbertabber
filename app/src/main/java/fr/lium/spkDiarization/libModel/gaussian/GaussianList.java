/**
 * 
 */
package fr.lium.spkDiarization.libModel.gaussian;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The Class GaussianList.
 * 
 * @author meignier
 */
public class GaussianList extends ArrayList<Gaussian> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new gaussian list.
	 */
	public GaussianList() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gaussian list.
	 * 
	 * @param c the c
	 */
	public GaussianList(Collection<? extends Gaussian> c) {
		super(c);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Instantiates a new gaussian list.
	 * 
	 * @param initialCapacity the initial capacity
	 */
	public GaussianList(int initialCapacity) {
		super(initialCapacity);
		// TODO Auto-generated constructor stub
	}

}
