package fr.lium.spkDiarization.lib;

/**
 * The Class CityBlockPair.
 */
public class CityBlockPair extends Pair<Integer, Double> implements Comparable<CityBlockPair> {

	/**
	 * Instantiates a new city block pair.
	 * 
	 * @param first the first
	 * @param second the second
	 */
	public CityBlockPair(Integer first, Double second) {
		super(first, second);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CityBlockPair o) {
		return Double.compare(getSecond(), o.getSecond());
	}

}
