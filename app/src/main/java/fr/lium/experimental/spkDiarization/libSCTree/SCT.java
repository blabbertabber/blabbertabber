/**
 * <p>
 * SCT
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.TreeSet;

import fr.lium.experimental.spkDiarization.libClusteringData.transcription.Link;
import fr.lium.experimental.spkDiarization.libClusteringData.transcription.LinkSet;
import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.parameter.Parameter;

public class SCT {
    protected SCTNode rootNode;
    protected int numberOfLabel = 4;
    protected TreeSet<String> SCTWordUsed;
    protected boolean trace;

    public SCT(int numberOfLabel, boolean trace) {
        this.numberOfLabel = numberOfLabel;
        SCTWordUsed = new TreeSet<String>(new WordComparator());
        this.trace = trace;
    }

    public void read(String name, String mask) throws IOException, DiarizationException {
        String filename = IOFile.getFilename(mask, name);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filename), Parameter.DefaultCharset));

        rootNode = readLIA_SCT(bufferedReader, 'X');
    }

    protected char readChar(String[] tokens, int index) throws DiarizationException {
        char c = 'A';
        if (tokens[index].length() == 1) {
            c = tokens[index].charAt(0);
        } else {
            throw new DiarizationException("SCT: readLIA_SCT() tokens[" + index + "] is not a char");
        }
        return c;
    }

    protected SCTNode readLIA_SCT(BufferedReader bufferedReader, char fatherTypeNode) throws IOException, DiarizationException {
        String line = "";
        if ((line = bufferedReader.readLine()) != null) {
            // Get token
            String[] tokens = line.split("\\s+");
            if (tokens.length < 7) {
                throw new DiarizationException("SCT: readLIA_SCT() we need 7 elements by line, we find only " + tokens.length);
            }

            /** Read the first token as a Char */
            char typeNode = readChar(tokens, 0);

            /** Second token, index number of the question*/
            int index = Integer.parseInt(tokens[1]);

            /** Word of the question (only one word per question)*/
            String word = tokens[3];

            // Question level (not implemented yet)
            int questionLevel = Integer.parseInt(tokens[4]) - 1;

            /**
             * Question type J : <?> U : <+?+> R : <+?> L : <?+> G : Global question
             */
            char typeOfQuestion = readChar(tokens, 5);

            // Is the question at the right, at the left ?
            char questionLocalisation = readChar(tokens, 6);
            // New Node
            // The words are stored with an index in the dictionnary.
            // Retrieve this index to avoid string comparison
            SCTWordUsed.add(word);

            // Create a new node with the actual line
            SCTNode node = new SCTNode(fatherTypeNode, typeNode, index, typeOfQuestion, questionLocalisation, word, questionLevel, trace);

            // Leaf Node
            if (typeNode == 'L') {
                // read labels and probabilities
                if ((line = bufferedReader.readLine()) != null) {
                    String[] labelTokens = line.split("\\s+");
                    if (labelTokens.length < (numberOfLabel * 2)) {
                        throw new DiarizationException("SCT: readLIA_SCT() we need " + numberOfLabel + " label");
                    }
                    for (int j = 0; j < 2 * numberOfLabel; j += 2) {
                        String label = labelTokens[j];
                        double proba = Double.valueOf(labelTokens[j + 1]);
                        node.addProbabilities(label, proba);
                    }
                } else {
                    throw new DiarizationException("SCT: readLIA_SCT() labels not found ");
                }
            }
            if (typeNode == 'I') {
                // First set the left nodes until we meet a leaf node
                node.setLeft(readLIA_SCT(bufferedReader, typeOfQuestion));
                // Set the next question as the right one of the current node, then we go back
                node.setRight(readLIA_SCT(bufferedReader, fatherTypeNode));
            }
            return node;
        }
        return null;
    }

    public void debug() {
        rootNode.debug();
    }

    protected void linkFilter(LinkSet linkSet) {
        if (trace) {
            System.err.println("[debug] START sausageFilter()");
        }
        for (int i = 0; i < linkSet.size(); i++) {
            if (trace) {
                System.err.println("[debug] sausage number : " + i);
            }
            Link link = linkSet.getLink(i);
            String word = link.getWord();
            if (word.equals("eps") == false) {
                if (SCTWordUsed.contains(word) == false) {
                    // A suuprimer ou remplacer par UNK ?
                    if (trace) {
                        System.err.println("[debug] word=" + word + ")" + " removed");
                    }
                    linkSet.remove(i);// non mais quoi faire
                } else {
                    if (trace) {
                        System.err.println("[debug] word=" + word + ")" + " kept");
                    }
                }
            } else {
                if (trace) {
                    System.err.println("[debug] word=" + word + ")" + " kept eps (?)");
                }
            }
        }
        if (trace) {
            System.err.println("[debug] END sausageFilter()");
        }
    }

    public SCTSolution test(LinkSet linkSet) throws DiarizationException {
        // Pour plus tard
        // sausageFilter(linkSet);

        // sausageSet.size() : number of transitions (number of words for the N-best)
        SCTSolution solution = new SCTSolution(linkSet.size());
        rootNode.test(linkSet, solution);
        return solution;
    }

    private class WordComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

}
