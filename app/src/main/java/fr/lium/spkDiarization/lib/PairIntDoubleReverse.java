package fr.lium.spkDiarization.lib;

/**
 * The Class PairIntDoubleReverse.
 */
public class PairIntDoubleReverse extends Pair<Integer, Double> implements Comparable<PairIntDoubleReverse> {

	/**
	 * Instantiates a new pair int double reverse.
	 * 
	 * @param first the first
	 * @param second the second
	 */
	public PairIntDoubleReverse(Integer first, Double second) {
		super(first, second);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(PairIntDoubleReverse o) {
		return -1 * Double.compare(getSecond(), o.getSecond());
	}

}
