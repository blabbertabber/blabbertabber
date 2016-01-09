/**
 * <p>
 * SCTNode
 * </p>
 *
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @author <a href="mailto:vincent.jousse@lium.univ-lemans.fr">Vincent Jousse</a>
 * @version v2.0
 * <p/>
 * Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * <p/>
 * THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package fr.lium.experimental.spkDiarization.libSCTree;

import java.util.Iterator;
import java.util.TreeMap;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.spkDiarization.lib.DiarizationException;

public class SCTNode {

    private SCTNode left;
    private SCTNode right;
    private char typeNode; // Leaf or Internal node{L, I}, X : root (?)
    /**
     * Type of question: J : <?> U : <+?+> R : <+?> L : <?+> G : Global query
     *
     */
    private char typeOfQuestion;
    /**
     * Left or right query of the KS(?)
     *
     */
    private char questionLocalisation;
    /** father node type (X: root) */
    private char fatherTypeNode;
    /** level of the question */
    private int questionLevel;
    /** the word questioned */
    private String word; //
    /** probability of each label */
    private SCTProbabilities probabilities;
    /** index of the question */
    private int index;
    private boolean trace;

    public SCTNode(char fatherTypeNode, char typeNode, int index, char typeOfQuestion, char questionLocalisation, String question, int questionLevel,
                   boolean trace) {
        this.fatherTypeNode = fatherTypeNode;
        this.typeNode = typeNode;
        this.index = index;
        this.typeOfQuestion = typeOfQuestion;
        this.questionLocalisation = questionLocalisation;
        word = question;
        this.questionLevel = questionLevel;
        left = right = null;
        probabilities = new SCTProbabilities();
        this.trace = trace;
    }

    protected SCTNode getLeft() {
        return left;
    }

    protected void setLeft(SCTNode left) {
        this.left = left;
    }

    protected SCTNode getRight() {
        return right;
    }

    protected void setRight(SCTNode right) {
        this.right = right;
    }

    protected TreeMap<String, Double> getProbabilities() {
        return probabilities;
    }

    protected void addProbabilities(String key, Double value) {
        probabilities.put(key, value);
    }

    public int Compatible(LinkSet linkSet, SCTSolution solution, boolean gapBefort, boolean gapAfter) {
        int index = -1;
        int fatherIndex = solution.getCurrent();
        boolean fatherGapBefore = true;
        boolean fatherGapAfter = true;
        if (fatherIndex != -1) {
            fatherGapBefore = solution.isBeforeGap(fatherIndex);
            fatherGapAfter = solution.isAfterGap(fatherIndex);
        }
        //debug();
        if (questionLocalisation == 'l') {
            /* on cherche a la gauche du KS sur la racine, ptKS est egal a NULL */
            if (trace) {
                System.err.println("\t[compatible l, 1] questionLocalisation == l");
            }
            if (fatherGapBefore == false) {
                if (trace) {
                    System.err.println("\t[compatible l, 2] father.isBeforeGap() == false, on decale vers la gauche");
                }
                index = fatherIndex;
                for (; (index >= 0) && (solution.isUsed(index) == true); index--) {
                    if (trace) {
                        System.err.println("\t[compatible l, 3] father.gap_avant == false, check " + linkSet.getLink(index).getWord() + " used="
                                + solution.isUsed(index));
                    }
                }
				/*
				 * if (index < 0) { if (trace) System.err.println("\t[compatible l, 4] ERREUR : Il n'y a pas de GAP sur la gauche"); return -1; } if (trace)
				 * System.err.println("\t[compatible l, 5] ERREUR : Il y a un GAP sur la gauche");
				 */
            } else {
                if (trace) {
                    System.err.println("\t[compatible l, 6] KS.gap_avant != 0, on ne decale pas vers la gauche");
                }
                if (fatherIndex == -1) {
                    if (trace) {
                        System.err.println("\t[compatible l, 7] KS.gap_avant != 0, le pere est null, prendre item");
                    }
					/* ?? */
                    index = solution.getSize() - 1;
                } else {
                    if (trace) {
                        System.err.println("\t[compatible l, 8] KS.gap_avant != 0, le pere n'est pas null, prendre le pred du pere");
                    }
                    index = fatherIndex - 1;
                }
            }

            if (index < 0) {
                if (trace) {
                    System.err.println("\t[compatible l, 9] pt == NULL Sortie 1");
                }
                return -1;
            }
            if (trace) {
                System.err.println("\t[compatible l, 10] pt != NULL on est pres pour " + linkSet.getLink(index).getWord() + " used=" + solution.isUsed(index));
            }

            if (gapAfter == false) {
                if (trace) {
                    System.err.println("\t[compatible l, 11] gap_apres == 0,la question doit se trouver juste avant le KS");
                }
                if ((solution.isUsed(index) != false) || (linkSet.getLink(index).find(word) != true)) {
                    if (trace) {
                        System.err.println("\t[compatible l, 12] gap_apres == 0,la question doit se trouver juste avant le KS --> FAUX, sortie 2");
                    }
                    return -1;
                }
                if (trace) {
                    System.err.println("\t[compatible l, 13] gap_apres == 0,la question doit se trouve juste avant le KS");
                }
            } else {
                if (trace) {
                    System.err.println("\t[compatible l, 14] gap_apres != 0, il DOIT y avoir un gap entre la question et le KS");
                }
                index--;
                while ((index >= 0) && (solution.isUsed(index) == false) && (linkSet.getLink(index).find(word) == false)) {
                    if (trace) {
                        System.err
                                .println("\t[compatible l, 15] gap_apres != 0, check " + linkSet.getLink(index).getWord() + " used=" + solution.isUsed(index));
                    }
                    index--;
                }
                if ((index < 0) || (solution.isUsed(index) != false)) {
                    if (trace) {
                        System.err.println("\t[compatible l, 16] gap_apres != 0 pt == NULL pt deja utilise Sortie 3");
                    }
                    return -1;
                }
                if (trace) {
                    System.err.println("\t[compatible l, 17] gap_apres != 0 pt != NULL ou pt pas utilise");
                }
            }
			/* la solution est dans 'pt', verifions le gap_avant */
            if (trace) {
                System.err.println("\t[compatible l, 18] On a trouver : " + linkSet.getLink(index).getWord() + " used=" + solution.isUsed(index));
            }

            if (gapBefort == false) {

                if (trace) {
                    System.err.println("\t[compatible l, 19] gap_avant == 0, il ne faut pas qu'il y est de trou avant (quoi ?)");
                }
                int tmp = index - 1;
                for (; (tmp >= 0) && (solution.isUsed(tmp) == true); tmp--) {
                    if (trace) {
                        System.err
                                .println("\t[compatible l, 20] gap_avant == 0, check " + linkSet.getLink(tmp).getWord() + " used=" + solution.isUsed(tmp));
                    }
                }
                if (tmp >= 0) {
                    if (trace) {
                        System.err.println("\t[compatible l, 21] gap_avant == 0, tmp != NULL, il ne fallait pas un trou avant,Sortie 4");
                    }
                    return -1;
                }
                if (trace) {
                    System.err.println("\t[compatible l, 22] gap_avant == 0, tmp == NULL, il n'y a pas de trou avant");
                }
            }

            if (trace) {
                System.err.println("\t[compatible l, 23] FIN, check " + linkSet.getLink(index).getWord() + " used=" + solution.isUsed(index));
            }
            return index;
        } else { /* on cherche a la droite du KS */
            index = solution.getSize();
            if (trace) {
                System.err.println("\t[compatible r, 30] qloc != l --> r");
            }

			/* test de coherence */
			/*
			 * s'il n'y a pas de GAP apres, on avance vers la droite jusqu'a en trouver un
			 */
            if (fatherGapAfter == false) {
                if (trace) {
                    System.err.println("\t[compatible r, 31] KS.gap_apres == 0, on decale vers la droite");
                }
                if (fatherIndex == -1) {
                    index = 0;
                } else {
                    index = fatherIndex;
                }
                for (; (index < solution.getSize()) && (solution.isUsed(index) == true); index++) {
                    if (trace) {
                        System.err.println("\t[compatible r, 32] ] KS.gap_apres == 0, check " + linkSet.getLink(index).getWord() + " used="
                                + solution.isUsed(index));
                    }
                }
				/*
				 * if (index >= solution.getSize()) { if (trace) System.err.println("\t[compatible r, 33] ERREUR : Il n'y a pas de GAP sur la droite\n"); return
				 * -1; } if (trace) System.err.println("\t[compatible r, 34] ERREUR : Il y a un GAP sur la droite\n");
				 */
            } else {
                if (trace) {
                    System.err.println("\t[compatible r, 35] KS.gap_apres != 0, on ne decale pas vers la droite");
                }
                if (fatherIndex == -1) {
                    if (trace) {
                        System.err.println("\t[compatible r, 36] KS.gap_apres != 0, le pere est null, prendre item");
                    }
                    index = 0;
                } else {
                    if (trace) {
                        System.err.println("\t[compatible r, 37] KS.gap_apres != 0, le pere n'est pas null, prendre le suivant du pere");
                    }
                    index = fatherIndex + 1;
                }
            }

            if (index >= solution.getSize()) {
                if (trace) {
                    System.err.println("\t[compatible r, 38] pt == NULL Sortie 1");
                }
                return -1;
            }

            System.err.println("\t[compatible r, 39] ] pt != NULL , check " + linkSet.getLink(index).getWord() + " used="
                    + solution.isUsed(index));

            if (gapBefort == false) {
                if (trace) {
                    System.err.println("\t[compatible r, 40] gap_avant == 0, la question doit se trouver juste apres le KS");
                }
                if ((solution.isUsed(index) != false) || (linkSet.getLink(index).find(word) == false)) {
                    if (trace) {
                        System.err.println("\t[compatible r, 41] gap_avant == 0,la question doit se trouver juste apres le KS --> FAUX, sortie 2 index=" + index);
                    }
                    return -1;
                }
                if (trace) {
                    System.err.println("\t[compatible r, 42] gap_avant == 0,la question doit trouve juste apres le KS");
                }
            } else {
                if (trace) {
                    System.err.println("\t[compatible r, 43] gap_avant != 0, il DOIT y avoir un gap entre la question et le KS");
                }
                index++;
                while ((index < solution.getSize()) && (solution.isUsed(index) == false) && (linkSet.getLink(index).find(word) == false)) {
                    if (trace) {
                        System.err
                                .println("\t[compatible r, 44] gap_avant != 0, check " + linkSet.getLink(index).getWord() + " used=" + solution.isUsed(index));
                    }
                    index++;
                }
                if ((index >= solution.getSize()) || (solution.isUsed(index) != false)) {
                    if (trace) {
                        System.err.println("\t[compatible r, 45] gap_avant != 0 pt == NULL ou pt deja utilise Sortie 3");
                    }
                    return -1;
                }
                if (trace) {
                    System.err.println("\t[compatible r, 46] gap_avant != 0 pt != NULL ou pt pas utilise");
                }
            }
            System.err.println("\t[compatible r, 47] on a trouve " + linkSet.getLink(index).getWord() + " used=" + solution.isUsed(index));

			/* la solution est dans 'pt', verifions le gap_apres */
            if (gapAfter == false) {
                if (trace) {
                    System.err.println("\t[compatible r, 48] gap_apres == 0, il ne faut pas qu'il y est de trou apres (quoi ?)");
                }
                int tmp = index + 1;
                for (; (tmp < solution.getSize()) && (solution.isUsed(tmp) == true); tmp++) {
                    if (trace) {
                        System.err
                                .println("\t[compatible r, 49] gap_apres == 0, check " + linkSet.getLink(tmp).getWord() + " used=" + solution.isUsed(tmp));
                    }
                }
                if (tmp < solution.getSize()) {
                    if (trace) {
                        System.err.println("\t[compatible r, 50] gap_apres == 0, tmp != NULL, il ne fallait pas un trou apres,Sortie 4");
                    }
                    return -1;
                }
                if (trace) {
                    System.err.println("\t[compatible r, 51] gap_apres == 0, tmp == NULL, il n'y a pas de trou apres");
                }
            }
            if (trace) {
                System.err.println("\t[compatible r, 52] FIN, check " + linkSet.getLink(index).getWord() + " used=" + solution.isUsed(index));
            }
            return index;
        }
    }

    /**
     * Will check the linkSet with the current node
     *
     * @param linkSet The current linkSet
     * @param solution The current solution
     * @return the result of the query
     * @throws DiarizationException
     */
// public boolean valide(LinkSet linkSet, SCTSolution solution, SCTSolutionSet newSolutionSet) throws DiarizationException {
    public boolean valide(LinkSet linkSet, SCTSolution solution) throws DiarizationException {
        if (trace) {
            System.err.println("[valide] SCTNode valide index:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion);
        }

/*
 * // Current index in the solution int index = solution.getCurrent(); int start; int end; int size; // boolean result = false;
 */
        if (typeOfQuestion == 'G') { // global question
            boolean resultGlobal = checkGlobalQuestion(linkSet, solution);
            if (resultGlobal == true) {
                System.err.println("[valide] SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion + " --> OUI");
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
                System.err.println("[valide] SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion + " --> OUI");
                return true;
            }
        } else if (typeOfQuestion == 'L') {// <?+>
            int index = Compatible(linkSet, solution, false, true);
            if (index != -1) {
                solution.setCurrent(index, true, false, true);
                System.err.println("[valide] SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion + " --> OUI");
                return true;
            }
        } else if (typeOfQuestion == 'R') {// <+?>
            int index = Compatible(linkSet, solution, true, false);
            if (index != -1) {
                solution.setCurrent(index, true, true, false);
                System.err.println("[valide] SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion + " --> OUI");
                return true;
            }
        } else if (typeOfQuestion == 'U') {// <+?+>
            int index = Compatible(linkSet, solution, true, true);
            if (index != -1) {
                solution.setCurrent(index, true, true, true);
                System.err.println("[valide] SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion + " --> OUI");
                return true;
            }
        } else {
            throw (new DiarizationException("Warring SCT: unknown question type"));
        }
        System.err.println("[valide] SCTNode REP:" + index + " word=" + word + " typeOfQuestion=" + typeOfQuestion + " --> NON");
        return false;

/*
 * if (questionLocalisation == 'l') { // question on the left if (trace) { System.err.print("[valide] left question index:" + index); } // If the solution was
 * not already used (index ==-1) // current index is the end of the sausage (question at left) if (index < 0) { index = linkSet.size(); } // So we start from 0,
 * and end at index start = 0; end = Math.max(0, index - 1); size = end - start + 1; if (trace) { System.err.println(" / start=" + start + " end=" + end); } if
 * (typeOfQuestion == 'J') {// <?> return checkQuestion(linkSet, solution, newSolutionSet, typeOfQuestion, start, end); // ok } else if (typeOfQuestion == 'L')
 * {// <?+> return checkQuestion(linkSet, solution, newSolutionSet, typeOfQuestion, start, (size >= 2)); // ok } else if (typeOfQuestion == 'R') {// <+?> return
 * checkQuestion(linkSet, solution, newSolutionSet, typeOfQuestion, end, (size >= 1)); // ok } else if (typeOfQuestion == 'U') {// <+?+> return
 * checkUQuestion(linkSet, solution, newSolutionSet, start , end - 1, (size >= 2)); } else { throw (new
 * DiarizationException("Warring SCT: unknown question type")); } } else { // question on the right if (trace) {
 * System.err.print("[valide] right question index:" + index); } // If the solution was not already used (index ==-1) // current index is the start of the
 * sausage (question at right) // So we start from the current index and end at the end of the sausage start = Math.min(linkSet.size() - 1, index + 1); end =
 * linkSet.size() - 1; size = end - start + 1; if (trace) { System.err.println(" / start=" + start + " end=" + end); } if (typeOfQuestion == 'J') {// <?> return
 * checkQuestion(linkSet, solution, newSolutionSet, typeOfQuestion, start, end); // ok } else if (typeOfQuestion == 'L') {// <?+> return checkQuestion(linkSet,
 * solution, newSolutionSet, typeOfQuestion, start, (size >= 1)); // ok } else if (typeOfQuestion == 'R') {// <+?> return checkQuestion(linkSet, solution,
 * newSolutionSet, typeOfQuestion, end, (size >= 2)); // ok } else if (typeOfQuestion == 'U') {// <+?+> return checkUQuestion(linkSet, solution, newSolutionSet,
 * start + 1, end, (size >= 2)); } else { throw (new DiarizationException("Warring SCT: unknown question type")); } }
 */
// System.err.println("[valide] add solution");
// newSolutionSet.add(solution);
// return result;
    }

    private boolean checkGlobalQuestion(LinkSet linkSet, SCTSolution solution) {
        if (trace) {
            System.err.print("[valide] G question");
            System.err.print(" --> " + this.word);
        }
        if (linkSet.getInformation(this.word) != null) {
            if (trace) {
                System.err.println(" --> true");
            }
            return true;
        }
        // Not implemented
        if (trace) {
            System.err.println(" --> false");
        }
        return false;
    }

    /*
     * private boolean checkQuestion(LinkSet linkSet, SCTSolution solution, SCTSolutionSet newSolutionSet, char type, int position, boolean valideSize) { if (trace)
     * { System.err.print("[valide] " + type + " question position:" + position + " valideSize:" + valideSize); } if (valideSize == true) { if
     * (solution.isUsed(position) == false) { // int indexOfFind = sausageSet.getSausage(end).find(wordIndex); if (linkSet.getLink(position).find(word) == true) {
     * solution.setCurrent(position); newSolutionSet.add(solution); if (trace) { System.err.println("** / {" + word + "/" + word + "} word found --> true"); }
     * return true; } else { if (trace) { System.err.print(" / {" + word + "/" + word + "} word not found"); } } } } if (trace) { System.err.println(" --> false");
     * } newSolutionSet.add(solution); return false; } private boolean checkUQuestion(LinkSet linkSet, SCTSolution solution, SCTSolutionSet newSolutionSet, int
     * start, int end, boolean valideSize) { if (trace) { System.err.print("[valide] U question start:" + start + " end:" + end + " valideSize:" + valideSize); } if
     * (valideSize == true) { int nbFound = 0; for (int i = start; i <= end; i++) { if (trace) { System.err.print(" " + i + "=" + solution.isUsed(i)); } if
     * (solution.isUsed(i) == false) { if (linkSet.getLink(i).find(word) == true) { if (trace) { System.err.println(" find : " + linkSet.getLink(i).getWord()); }
     * SCTSolution newSolution = (SCTSolution) solution.clone(); newSolution.setCurrent(i); newSolutionSet.add(newSolution); nbFound++; break; } } } if (nbFound >
     * 0) { if (trace) { System.err.println("* / {" + word + "/" + word + "} word found (" + nbFound + ")--> true"); } return true; } else { if (trace) {
     * System.err.print(" / {" + word + "/" + word + "} word not found"); } } } if (trace) { System.err.println(" --> false"); } newSolutionSet.add(solution);
     * return false; } public SCTSolutionSet test(LinkSet linkSet, SCTSolution solution) throws DiarizationException { // what about the Epsilon link ? if (trace) {
     * System.err.println("[test] ------------------------------------"); System.err.println("[test] SCTNode test"); } if (solution.isClosed() == false) { // If its
     * a leaf node with probabilities attached if (typeNode == 'L') { if (trace) { System.err.println("[test] SCTNode test L index=" + index); }
     * solution.setProbabilities(probabilities); SCTSolutionSet solutionSet = new SCTSolutionSet(); solutionSet.add(solution); return solutionSet; } else { //
     * Intermediate or Root (X) node if (trace) { System.err.println("[test] SCTNode test I/X"); } SCTSolutionSet solutionSet = new SCTSolutionSet(); SCTSolutionSet
     * newSolutionSet = new SCTSolutionSet(); // Parameters : the current sausageSet, the current solution, the newSolutionSet // leftCase == true if the sausage
     * matches the current node question boolean leftCase = valide(linkSet, solution, newSolutionSet); for (int i = 0; i < newSolutionSet.size(); i++) { if (trace)
     * { System.err.println("[test] SCTNode test solution index i:" + i + "/" + newSolutionSet.size()); } if (leftCase == true) {
     * solutionSet.addAll(left.test(linkSet, newSolutionSet.get(i))); } else { solutionSet.addAll(right.test(linkSet, newSolutionSet.get(i))); } } return
     * solutionSet; } } return null; }
     */
    public void test(LinkSet linkSet, SCTSolution solution) throws DiarizationException {
        if (trace) {
            System.err.println("[test] ------------------------------------");
            System.err.println("[test] SCTNode test");
        }
        if (solution.isClosed() == false) {
            // If its a leaf node with probabilities attached
            if (typeNode == 'L') {
                if (trace) {
                    System.err.println("[test] SCTNode test L index=" + index);
                }
                solution.setProbabilities(probabilities);
            } else {
                // Intermediate or Root (X) node
                if (trace) {
                    System.err.println("[test] SCTNode test I/X");
                }
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

    public void debug() {
        System.out.print("debug[SCTNode] \t " + fatherTypeNode + " " + typeNode + " " + index);
        System.out.println(" " + word + " " + questionLevel + " " + typeOfQuestion + " " + questionLocalisation);
        System.out.print("debug[SCTNode] \t\t");
        Iterator<String> it = probabilities.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            System.out.print(" " + key + "=" + probabilities.get(key));
        }
        System.out.println("");
        if (left != null) {
            System.out.println("debug[SCTNode] \t--- Left(" + index + ") ---");
            left.debug();
        }
        if (right != null) {
            System.out.println("debug[SCTNode] \t--- right(" + index + ") ---");
            right.debug();
        }
    }

    public char getTypeNode() {
        return typeNode;
    }

    public char getTypeOfQuestion() {
        return typeOfQuestion;
    }

    public char getQuestionLocalisation() {
        return questionLocalisation;
    }

    public char getFatherTypeNode() {
        return fatherTypeNode;
    }

    public int getQuestionLevel() {
        return questionLevel;
    }

    /*
     * public int getWordIndex() { return wordIndex; }
     */
    public String getWord() {
        return word;
    }

    public int getIndex() {
        return index;
    }
/*
 * public WordDictonary getWordDictonary() { return wordDictonary; }
 */

}
