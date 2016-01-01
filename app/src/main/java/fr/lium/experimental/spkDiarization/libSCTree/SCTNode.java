/**
 * 
 * <p>
 * SCTNode
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 */

package fr.lium.experimental.spkDiarization.libSCTree;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Logger;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.spkDiarization.lib.DiarizationException;

/**
 * The Class SCTNode.
 */
public class SCTNode {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(SCTNode.class.getName());

	/** The left. */
	private SCTNode left;

	/** The right. */
	private SCTNode right;

	/** The type node. */
	private char typeNode; // Leaf or Internal node{L, I}, X : root (?)

	/** Type of question: J : <?> U : <+?+> R : <+?> L : <?+> G : Global query. */
	private char typeOfQuestion;

	/** Left or right query of the KS(?). */
	private char questionLocalisation;

	/** father node type (X: root). */
	private char fatherTypeNode;

	/** level of the question. */
	private int questionLevel;

	/** the word questioned. */
	private String word; //

	/** probability of each label. */
	private SCTProbabilities probabilities;

	/** index of the question. */
	private int index;

	/**
	 * Instantiates a new sCT node.
	 * 
	 * @param fatherTypeNode the father type node
	 * @param typeNode the type node
	 * @param index the index
	 * @param typeOfQuestion the type of question
	 * @param questionLocalisation the question localisation
	 * @param question the question
	 * @param questionLevel the question level
	 */
	public SCTNode(char fatherTypeNode, char typeNode, int index, char typeOfQuestion, char questionLocalisation, String question, int questionLevel) {
		this.fatherTypeNode = fatherTypeNode;
		this.typeNode = typeNode;
		this.index = index;
		this.typeOfQuestion = typeOfQuestion;
		this.questionLocalisation = questionLocalisation;
		word = question;
		this.questionLevel = questionLevel;
		left = right = null;
		probabilities = new SCTProbabilities();
	}

	/**
	 * Gets the left.
	 * 
	 * @return the left
	 */
	protected SCTNode getLeft() {
		return left;
	}

	/**
	 * Sets the left.
	 * 
	 * @param left the new left
	 */
	protected void setLeft(SCTNode left) {
		this.left = left;
	}

	/**
	 * Sets the right.
	 * 
	 * @param right the new right
	 */
	protected void setRight(SCTNode right) {
		this.right = right;
	}

	/**
	 * Gets the right.
	 * 
	 * @return the right
	 */
	protected SCTNode getRight() {
		return right;
	}

	/**
	 * Gets the probabilities.
	 * 
	 * @return the probabilities
	 */
	protected TreeMap<String, Double> getProbabilities() {
		return probabilities;
	}

	/**
	 * Adds the probabilities.
	 * 
	 * @param key the key
	 * @param value the value
	 */
	protected void addProbabilities(String key, Double value) {
		probabilities.put(key, value);
	}

	/**
	 * Compatible.
	 * 
	 * @param linkSet the link set
	 * @param solution the solution
	 * @param gapBefort the gap befort
	 * @param gapAfter the gap after
	 * @return the int
	 */
	public int Compatible(LinkSet linkSet, SCTSolution solution, boolean gapBefort, boolean gapAfter) {
		int index = -1;
		int fatherIndex = solution.getCurrent();
		boolean fatherGapBefore = true;
		boolean fatherGapAfter = true;
		if (fatherIndex != -1) {
			fatherGapBefore = solution.isBeforeGap(fatherIndex);
			fatherGapAfter = solution.isAfterGap(fatherIndex);
		}
		// debug();
		if (questionLocalisation == 'l') {
			/* on cherche a la gauche du KS sur la racine, ptKS est egal a NULL */
			logger.finest("\t[compatible l, 1] questionLocalisation == l");

			if (fatherGapBefore == false) {
				logger.finest("\t[compatible l, 2] father.isBeforeGap() == false, on decale vers la gauche");

				index = fatherIndex;
				for (; (index >= 0) && (solution.isUsed(index) == true); index--) {
					logger.finest("\t[compatible l, 3] father.gap_avant == false, check "
							+ linkSet.getLink(index).getWord() + " used=" + solution.isUsed(index));

				}
			} else {
				logger.finest("\t[compatible l, 6] KS.gap_avant != 0, on ne decale pas vers la gauche");

				if (fatherIndex == -1) {
					logger.finest("\t[compatible l, 7] KS.gap_avant != 0, le pere est null, prendre item");

					/* ?? */
					index = solution.getSize() - 1;
				} else {
					logger.finest("\t[compatible l, 8] KS.gap_avant != 0, le pere n'est pas null, prendre le pred du pere");

					index = fatherIndex - 1;
				}
			}

			if (index < 0) {
				logger.finest("\t[compatible l, 9] pt == NULL Sortie 1");

				return -1;
			}
			logger.finest("\t[compatible l, 10] pt != NULL on est pres pour " + linkSet.getLink(index).getWord()
					+ " used=" + solution.isUsed(index));

			if (gapAfter == false) {
				logger.finest("\t[compatible l, 11] gap_apres == 0,la question doit se trouver juste avant le KS");

				if ((solution.isUsed(index) != false) || (linkSet.getLink(index).find(word) != true)) {
					logger.finest("\t[compatible l, 12] gap_apres == 0,la question doit se trouver juste avant le KS --> FAUX, sortie 2");

					return -1;
				}
				logger.finest("\t[compatible l, 13] gap_apres == 0,la question doit se trouve juste avant le KS");

			} else {
				logger.finest("\t[compatible l, 14] gap_apres != 0, il DOIT y avoir un gap entre la question et le KS");

				index--;
				while ((index >= 0) && (solution.isUsed(index) == false)
						&& (linkSet.getLink(index).find(word) == false)) {
					logger.finest("\t[compatible l, 15] gap_apres != 0, check " + linkSet.getLink(index).getWord()
							+ " used=" + solution.isUsed(index));

					index--;
				}
				if ((index < 0) || (solution.isUsed(index) != false)) {
					logger.finest("\t[compatible l, 16] gap_apres != 0 pt == NULL pt deja utilise Sortie 3");

					return -1;
				}
				logger.finest("\t[compatible l, 17] gap_apres != 0 pt != NULL ou pt pas utilise");

			}
			/* la solution est dans 'pt', verifions le gap_avant */
			logger.finest("\t[compatible l, 18] On a trouver : " + linkSet.getLink(index).getWord() + " used="
					+ solution.isUsed(index));

			if (gapBefort == false) {

				logger.finest("\t[compatible l, 19] gap_avant == 0, il ne faut pas qu'il y est de trou avant (quoi ?)");

				int tmp = index - 1;
				for (; (tmp >= 0) && (solution.isUsed(tmp) == true); tmp--) {
					logger.finest("\t[compatible l, 20] gap_avant == 0, check " + linkSet.getLink(tmp).getWord()
							+ " used=" + solution.isUsed(tmp));

				}
				if (tmp >= 0) {
					logger.finest("\t[compatible l, 21] gap_avant == 0, tmp != NULL, il ne fallait pas un trou avant,Sortie 4");

					return -1;
				}
				logger.finest("\t[compatible l, 22] gap_avant == 0, tmp == NULL, il n'y a pas de trou avant");

			}

			logger.finest("\t[compatible l, 23] FIN, check " + linkSet.getLink(index).getWord() + " used="
					+ solution.isUsed(index));

			return index;
		} else { /* on cherche a la droite du KS */
			index = solution.getSize();
			logger.finest("\t[compatible r, 30] qloc != l --> r");

			/* test de coherence */
			/*
			 * s'il n'y a pas de GAP apres, on avance vers la droite jusqu'a en trouver un
			 */
			if (fatherGapAfter == false) {
				logger.finest("\t[compatible r, 31] KS.gap_apres == 0, on decale vers la droite");

				if (fatherIndex == -1) {
					index = 0;
				} else {
					index = fatherIndex;
				}
				for (; (index < solution.getSize()) && (solution.isUsed(index) == true); index++) {
					logger.finest("\t[compatible r, 32] ] KS.gap_apres == 0, check " + linkSet.getLink(index).getWord()
							+ " used=" + solution.isUsed(index));

				}
			} else {
				logger.finest("\t[compatible r, 35] KS.gap_apres != 0, on ne decale pas vers la droite");

				if (fatherIndex == -1) {
					logger.finest("\t[compatible r, 36] KS.gap_apres != 0, le pere est null, prendre item");

					index = 0;
				} else {
					logger.finest("\t[compatible r, 37] KS.gap_apres != 0, le pere n'est pas null, prendre le suivant du pere");

					index = fatherIndex + 1;
				}
			}

			if (index >= solution.getSize()) {
				logger.finest("\t[compatible r, 38] pt == NULL Sortie 1");
				return -1;
			}

			logger.finest("\t[compatible r, 39] ] pt != NULL , check " + linkSet.getLink(index).getWord() + " used="
					+ solution.isUsed(index));

			if (gapBefort == false) {
				logger.finest("\t[compatible r, 40] gap_avant == 0, la question doit se trouver juste apres le KS");

				if ((solution.isUsed(index) != false) || (linkSet.getLink(index).find(word) == false)) {
					logger.finest("\t[compatible r, 41] gap_avant == 0,la question doit se trouver juste apres le KS --> FAUX, sortie 2 index="
							+ index);

					return -1;
				}
				logger.finest("\t[compatible r, 42] gap_avant == 0,la question doit trouve juste apres le KS");

			} else {
				logger.finest("\t[compatible r, 43] gap_avant != 0, il DOIT y avoir un gap entre la question et le KS");

				index++;
				while ((index < solution.getSize()) && (solution.isUsed(index) == false)
						&& (linkSet.getLink(index).find(word) == false)) {
					logger.finest("\t[compatible r, 44] gap_avant != 0, check " + linkSet.getLink(index).getWord()
							+ " used=" + solution.isUsed(index));

					index++;
				}
				if ((index >= solution.getSize()) || (solution.isUsed(index) != false)) {
					logger.finest("\t[compatible r, 45] gap_avant != 0 pt == NULL ou pt deja utilise Sortie 3");

					return -1;
				}
				logger.finest("\t[compatible r, 46] gap_avant != 0 pt != NULL ou pt pas utilise");

			}
			logger.finest("\t[compatible r, 47] on a trouve " + linkSet.getLink(index).getWord() + " used="
					+ solution.isUsed(index));

			/* la solution est dans 'pt', verifions le gap_apres */
			if (gapAfter == false) {
				logger.finest("\t[compatible r, 48] gap_apres == 0, il ne faut pas qu'il y est de trou apres (quoi ?)");

				int tmp = index + 1;
				for (; (tmp < solution.getSize()) && (solution.isUsed(tmp) == true); tmp++) {
					logger.finest("\t[compatible r, 49] gap_apres == 0, check " + linkSet.getLink(tmp).getWord()
							+ " used=" + solution.isUsed(tmp));

				}
				if (tmp < solution.getSize()) {
					logger.finest("\t[compatible r, 50] gap_apres == 0, tmp != NULL, il ne fallait pas un trou apres,Sortie 4");

					return -1;
				}
				logger.finest("\t[compatible r, 51] gap_apres == 0, tmp == NULL, il n'y a pas de trou apres");

			}
			logger.finest("\t[compatible r, 52] FIN, check " + linkSet.getLink(index).getWord() + " used="
					+ solution.isUsed(index));

			return index;
		}
	}

	/**
	 * Will check the linkSet with the current node.
	 * 
	 * @param linkSet The current linkSet
	 * @param solution The current solution
	 * @return the result of the query
	 * @throws DiarizationException the diarization exception
	 */
// public boolean valide(LinkSet linkSet, SCTSolution solution, SCTSolutionSet newSolutionSet) throws DiarizationException {
	public boolean valide(LinkSet linkSet, SCTSolution solution) throws DiarizationException {
		logger.finest("[valide] SCTNode valide index:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion);

/*
 * // Current index in the solution int index = solution.getCurrent(); int start; int end; int size; // boolean result = false;
 */
		if (typeOfQuestion == 'G') { // global question
			boolean resultGlobal = checkGlobalQuestion(linkSet, solution);
			if (resultGlobal == true) {
				logger.finest("SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion
						+ " --> OUI");
			}
			return resultGlobal;
		}
		if (questionLevel > 1) {
			throw (new DiarizationException("Warring SCT question level > 1 not yet implemanted"));
		}

		if (typeOfQuestion == 'J') {// <?>
			int index = Compatible(linkSet, solution, false, false);
			if (index != -1) {
				solution.setCurrent(index, true, false, false);
				logger.finest(" SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion
						+ " --> OUI");
				return true;
			}
		} else if (typeOfQuestion == 'L') {// <?+>
			int index = Compatible(linkSet, solution, false, true);
			if (index != -1) {
				solution.setCurrent(index, true, false, true);
				logger.finest(" SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion
						+ " --> OUI");
				return true;
			}
		} else if (typeOfQuestion == 'R') {// <+?>
			int index = Compatible(linkSet, solution, true, false);
			if (index != -1) {
				solution.setCurrent(index, true, true, false);
				logger.finest("SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion
						+ " --> OUI");
				return true;
			}
		} else if (typeOfQuestion == 'U') {// <+?+>
			int index = Compatible(linkSet, solution, true, true);
			if (index != -1) {
				solution.setCurrent(index, true, true, true);
				logger.finest("SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion
						+ " --> OUI");
				return true;
			}
		} else {
			throw (new DiarizationException("Warring SCT: unknown question type"));
		}
		logger.finest("SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion + " --> NON");
		return false;
	}

	/**
	 * Check global question.
	 * 
	 * @param linkSet the link set
	 * @param solution the solution
	 * @return true, if successful
	 */
	private boolean checkGlobalQuestion(LinkSet linkSet, SCTSolution solution) {
		String message = (" G question --> " + this.word);

		if (linkSet.getInformation(this.word) != null) {
			message += " --> true";
			return true;
		}
		// Not implemented
		message += " --> false";
		logger.finest(message);
		return false;
	}

	/**
	 * Test.
	 * 
	 * @param linkSet the link set
	 * @param solution the solution
	 * @throws DiarizationException the diarization exception
	 */
	public void test(LinkSet linkSet, SCTSolution solution) throws DiarizationException {
		logger.finest("[test] ------------------------------------");
		logger.finest("[test] SCTNode test");

		if (solution.isClosed() == false) {
			// If its a leaf node with probabilities attached
			if (typeNode == 'L') {
				logger.finest("[test] SCTNode test L index=" + index);

				solution.setProbabilities(probabilities);
			} else {
				// Intermediate or Root (X) node
				logger.finest("[test] SCTNode test I/X");

				// Parameters : the current sausageSet, the current solution, the newSolutionSet
				// leftCase == true if the sausage matches the current node question
				boolean leftCase = valide(linkSet, solution);
				if (leftCase == true) {
					left.test(linkSet, solution);
				} else {
					right.test(linkSet, solution);
				}
			}
		}
	}

	/**
	 * Debug.
	 */
	public void debug() {
		logger.finer(fatherTypeNode + " " + typeNode + " " + index + " " + word + " " + questionLevel + " "
				+ typeOfQuestion + " " + questionLocalisation);
		Iterator<String> it = probabilities.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			logger.finer(" " + key + "=" + probabilities.get(key));
		}
		System.out.println("");
		if (left != null) {
			logger.finer("--- Left(" + index + ") ---");
			left.debug();
		}
		if (right != null) {
			logger.finer("--- right(" + index + ") ---");
			right.debug();
		}
	}

	/**
	 * Gets the type node.
	 * 
	 * @return the type node
	 */
	public char getTypeNode() {
		return typeNode;
	}

	/**
	 * Gets the type of question.
	 * 
	 * @return the type of question
	 */
	public char getTypeOfQuestion() {
		return typeOfQuestion;
	}

	/**
	 * Gets the question localisation.
	 * 
	 * @return the question localisation
	 */
	public char getQuestionLocalisation() {
		return questionLocalisation;
	}

	/**
	 * Gets the father type node.
	 * 
	 * @return the father type node
	 */
	public char getFatherTypeNode() {
		return fatherTypeNode;
	}

	/**
	 * Gets the question level.
	 * 
	 * @return the question level
	 */
	public int getQuestionLevel() {
		return questionLevel;
	}

	/*
	 * public int getWordIndex() { return wordIndex; }
	 */
	/**
	 * Gets the word.
	 * 
	 * @return the word
	 */
	public String getWord() {
		return word;
	}

	/**
	 * Gets the index.
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}
/*
 * public WordDictonary getWordDictonary() { return wordDictonary; }
 */

}
