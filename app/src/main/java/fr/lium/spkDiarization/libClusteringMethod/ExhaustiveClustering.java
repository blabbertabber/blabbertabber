package fr.lium.spkDiarization.libClusteringMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Logger;

import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;

/**
 * The Class ExhaustiveClustering.
 */
public class ExhaustiveClustering {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(ExhaustiveClustering.class.getName());

	/** The distance. */
	MatrixSymmetric distance;

	/** The size. */
	int size;

	/** The max sum of distance. */
	double maxSumOfDistance;

	/** The current partition. */
	int[] currentPartition;

	/** The current value. */
	double currentValue;

	/** The best partition. */
	int[] bestPartition;

	/** The best value. */
	double bestValue;

	/** The center list. */
	LinkedList<Integer> centerList;

	/** The nb center. */
	int nbCenter;

	/** The threshold. */
	double threshold;

	/** The nodes. */
	ArrayList<Integer> nodes;

	/**
	 * Instantiates a new exhaustive clustering.
	 * 
	 * @param distance the distance
	 * @param threshold the threshold
	 * @param nodes the nodes
	 */
	public ExhaustiveClustering(MatrixSymmetric distance, double threshold, ArrayList<Integer> nodes) {
		super();
		this.distance = distance;
		this.threshold = threshold;
		this.nodes = nodes;
		size = distance.getSize();
		currentPartition = new int[size];
		Arrays.fill(currentPartition, -1);
		bestPartition = Arrays.copyOf(currentPartition, currentPartition.length);
//		bestPartition = currentPartition.clone();
		bestValue = Double.MAX_VALUE;
		centerList = new LinkedList<Integer>();
		nbCenter = 0;
		maxSumOfDistance();
	}

	/**
	 * Max sum of distance.
	 */
	protected void maxSumOfDistance() {
		maxSumOfDistance = 0.0;
		for (int i : nodes) {
			double sum = 0.0;
			for (int j : nodes) {
				sum += distance.unsafe_get(i, j);
			}
			if (sum > maxSumOfDistance) {
				maxSumOfDistance = sum;
			}
		}
		if (maxSumOfDistance == 0.0) {
			maxSumOfDistance = 1.0;
		}
	}

	/**
	 * Backtrack.
	 * 
	 * @return the int[]
	 */
	public int[] backtrack() {
		backTrackCenter(0);
		return bestPartition;
	}

	/**
	 * Back track center.
	 * 
	 * @param level the level
	 */
	@SuppressWarnings("unchecked")
	/*protected void backTrackCenter(int level) {
		LinkedList<LinkedList<Integer>> next = new LinkedList<LinkedList<Integer>>();
		
		boolean haveASolution = false;
		
		for (int i : nodes) {
			centerList.add(i);
			// debugCenterList("nbCenter: "+nbCenter+" // ");

			boolean valide = assignElement();
			if (haveASolution == false) {
				if (valide == false) {
					next.add((LinkedList<Integer>) centerList.clone());
				} else {
					logger.info("--> find a solution for center at level "+level);
					haveASolution = true;
					next.clear();
				}
			}
			centerList.removeLast();
		}
		for(LinkedList<Integer> list: next) {
			logger.info("--> next level");
			centerList = list;
			nbCenter++;
			backTrackCenter(level + 1);
			nbCenter--;
		}
		
	}*/
	protected void backTrackCenter(int level) {
		
		for (int i : nodes) {
			centerList.add(i);
			nbCenter++;
			// debugCenterList("nbCenter: "+nbCenter+" // ");

			boolean valide = assignElement();
			if (valide == false) {
				backTrackCenter(level + 1);
			} else {
				logger.info("--> old: find a solution for center at level "+level);
			}
			centerList.removeLast();
			nbCenter--;
		}
	}

	/**
	 * Debug center list.
	 * 
	 * @param msg the msg
	 */
	protected void debugCenterList(String msg) {
		String ch = "";
		for (Integer c : centerList) {
			ch += c + " ";
		}
		logger.info(msg + " " + ch);
	}

	/**
	 * Assign element.
	 * 
	 * @return true, if successful
	 */
	protected boolean assignElement() {
		for (int i : nodes) {
			if (centerList.contains(i) == true) {
				currentPartition[i] = i;
			} else {
				double minDist = Double.MAX_VALUE;
				int minCenter = -1;
				for (Integer c : centerList) {
					Double dist = distance.get(c, i);
					if (dist < minDist) {
						minCenter = c;
						minDist = dist;
					}
				}
				currentPartition[i] = minCenter;
			}
		}
		boolean res = evaluate();
		return res;

	}

	/**
	 * Evaluate.
	 * 
	 * @return true, if successful
	 */
	protected boolean evaluate() {
		// validation of the solution
		for (int i : nodes) {
			if (currentPartition[i] < 0) {
				return false;
			}
		}

		currentValue = 0.0;
		for (int i : nodes) {
			currentValue += distance.unsafe_get(i, currentPartition[i]);
		}
		currentValue /= maxSumOfDistance;
		currentValue += centerList.size();

		if (currentValue < bestValue) {
			bestValue = currentValue;
			bestPartition = Arrays.copyOf(currentPartition, currentPartition.length);
			//bestPartition = currentPartition.clone();
		}
		debugPartition("partition: nbCentre: " + centerList.size() + " currentValue: " + currentValue + " bestValue: "
				+ bestValue + " // ", currentPartition);

		return true;
	}

	/**
	 * Debug partition.
	 * 
	 * @param msg the msg
	 * @param solution the solution
	 */
	protected void debugPartition(String msg, int[] solution) {
		String partition = "";
		for (int i : nodes) {
			if (i == solution[i]) {
				partition += "*";
			}

			partition += solution[i] + " ";
		}
		logger.info(msg + " " + partition);
	}
}
