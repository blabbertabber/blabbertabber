package fr.lium.spkDiarization.libClusteringMethod;

import java.util.ArrayList;

import fr.lium.spkDiarization.libMatrix.MatrixSymmetric;

/**
 * The Class ConnectedGraph.
 */
public class ConnectedGraph {

	/** The distance. */
	private MatrixSymmetric distance;

	/** The threshold. */
	private double threshold;

	/** The size. */
	private int size;

	/** The sub graph. */
	private int[] subGraph;

	/** The nb sub graph. */
	private int nbSubGraph;

	/**
	 * The Class ListOfNode.
	 */
	protected class ListOfNode extends ArrayList<Integer> {

		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = 1L;
	}

	/** The graph. */
	protected ArrayList<ListOfNode> graph;

	/**
	 * Instantiates a new connected graph.
	 * 
	 * @param distance the distance
	 * @param threshold the threshold
	 */
	public ConnectedGraph(MatrixSymmetric distance, double threshold) {
		super();
		this.distance = distance;
		this.threshold = threshold;
		size = distance.getSize();
		graph = new ArrayList<ListOfNode>();
		distance2graph();
		subGraph();
	}

	/**
	 * Distance2graph.
	 */
	protected void distance2graph() {
		for (int c = 0; c < size; c++) {
			graph.add(new ListOfNode());
		}

		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				if (distance.get(i, j) < threshold) {
					graph.get(j).add(i);
					graph.get(i).add(j);
				}
			}
		}
	}

	/**
	 * Sub graph.
	 */
	public void subGraph() {
		subGraph = new int[size];
		for (int c = 0; c < size; c++) {
			subGraph[c] = -1;
		}
		nbSubGraph = 0;
		for (int c = 0; c < size; c++) {
			if (subGraph[c] < 0) {
				parcourir(nbSubGraph, c);
				nbSubGraph++;
			}
		}

	}

	/**
	 * Parcourir.
	 * 
	 * @param name the name
	 * @param node the node
	 * @return the int
	 */
	protected int parcourir(int name, int node) {
		int deep = 0;
		if (subGraph[node] < 0) {
			subGraph[node] = name;
			for (int s : graph.get(node)) {
				int v = parcourir(name, s) + 1;
				if (v > deep) {
					deep = v;
				}
			}
		}
		return deep;
	}

	/**
	 * Gets the sub graph.
	 * 
	 * @return the subGraph
	 */
	public int[] getSubGraph() {
		return subGraph;
	}

	/**
	 * Gets the nb sub graph.
	 * 
	 * @return the nbSubGraph
	 */
	public int getNbSubGraph() {
		return nbSubGraph;
	}

}
