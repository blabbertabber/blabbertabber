/**
 * 
 * <p>
 * ParameterNamedSpeaker
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

package fr.lium.deprecated.spkDiarization;

import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterBase;

/**
 * The Class ParameterNamedSpeaker.
 * 
 * @deprecated
 */
@Deprecated
public class ParameterNamedSpeaker extends ParameterBase {

	/** The threshold. */
	private double threshold;

	/** The firstname list. */
	private String firstnameList;

	/** The word dictonary mask. */
	private String wordDictonaryMask;

	/** The SCT mask. */
	private String SCTMask;

	/** The target list. */
	private String targetList;

	/** The remove check gender. */
	private boolean removeCheckGender;

	/**
	 * Instantiates a new parameter named speaker.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterNamedSpeaker(Parameter parameter) {
		super(parameter);
		setThreshold(0.0);
		setFirstnameList("%s.lst");
		setWordDictonaryMask("%s.dic");
		setSCTMask("%s.tree");
		setTargetList("%s");
		removeCheckGender = false;
	}

/*
 * @Override public boolean readParameter(int option, String optarg) { if (option == ReferenceThreshold) { setThreshold(Double.parseDouble(optarg)); return true; } else if (option == ReferenceFirstnameList) { setFirstnameList(optarg); return true; }
 * else if (option == ReferenceSCTMask) { setSCTMask(optarg); return true; } else if (option == ReferenceWordDictonaryMask) { setWordDictonaryMask(optarg); return true; } else if (option == ReferenceTargetList) { setTargetList(optarg); return true; }
 * return false; }
 * @Override public void addOptions(ArrayList<LongOptWithAction> list) { list.add(new LongOptWithAction("nThr", 1, null, ReferenceThreshold, null)); list.add(new LongOptWithAction("nFirstNameList", 1, null, ReferenceFirstnameList, null));
 * list.add(new LongOptWithAction("nSCTMask", 1, null, ReferenceSCTMask, null)); list.add(new LongOptWithAction("nTargetList", 1, null, ReferenceTargetList, null)); list.add(new LongOptWithAction("nRemoveCheckGender", 0, null,
 * ReferenceRemoveCheckGender, null)); }
 */

	/**
	 * Gets the threshold.
	 * 
	 * @return the threshold
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * Sets the threshold.
	 * 
	 * @param namedNEThr the new threshold
	 */
	public void setThreshold(double namedNEThr) {
		threshold = namedNEThr;
	}

	/**
	 * Gets the firstname list.
	 * 
	 * @return the firstname list
	 */
	public String getFirstnameList() {
		return firstnameList;
	}

	/**
	 * Sets the firstname list.
	 * 
	 * @param firstnameList the new firstname list
	 */
	public void setFirstnameList(String firstnameList) {
		this.firstnameList = firstnameList;
	}

	/**
	 * Gets the word dictonary mask.
	 * 
	 * @return the word dictonary mask
	 */
	public String getWordDictonaryMask() {
		return wordDictonaryMask;
	}

	/**
	 * Sets the word dictonary mask.
	 * 
	 * @param wordDictonaryMask the new word dictonary mask
	 */
	public void setWordDictonaryMask(String wordDictonaryMask) {
		this.wordDictonaryMask = wordDictonaryMask;
	}

	/**
	 * Gets the sCT mask.
	 * 
	 * @return the sCT mask
	 */
	public String getSCTMask() {
		return SCTMask;
	}

	/**
	 * Sets the sCT mask.
	 * 
	 * @param mask the new sCT mask
	 */
	public void setSCTMask(String mask) {
		SCTMask = mask;
	}

	/**
	 * Gets the target list.
	 * 
	 * @return the target list
	 */
	public String getTargetList() {
		return targetList;
	}

	/**
	 * Sets the target list.
	 * 
	 * @param targetList the new target list
	 */
	public void setTargetList(String targetList) {
		this.targetList = targetList;
	}

	/**
	 * Gets the removes the check gender.
	 * 
	 * @return the removes the check gender
	 */
	public boolean getRemoveCheckGender() {
		return removeCheckGender;
	}

	/**
	 * Sets the removes the check gender.
	 * 
	 * @param removeCheckGender the new removes the check gender
	 */
	public void setRemoveCheckGender(boolean removeCheckGender) {
		this.removeCheckGender = removeCheckGender;
	}

	/**
	 * Prints the threshold.
	 */
	public void printThreshold() {
		System.out.print("info[ParameterNamedSpeaker] \t --nThr \t named speaker threshold = ");
		System.out.println(getThreshold());
	}

	/**
	 * Prints the firstname list.
	 */
	public void printFirstnameList() {
		System.out.print("info[ParameterNamedSpeaker] \t --nFirstnameList \t list of firstname with gender information = ");
		System.out.println(getFirstnameList());
	}

	/**
	 * Prints the word dictonary mask.
	 */
	public void printWordDictonaryMask() {
		System.out.print("info[ParameterNamedSpeaker] \t --nWordDictonaryMask \t dictonary, decoding word list ");
		System.out.println(getWordDictonaryMask());
	}

	/**
	 * Prints the sct mask.
	 */
	public void printSCTMask() {
		System.out.print("info[ParameterNamedSpeaker] \t --nSCTMask \t Semantic Classification Tree Mask ");
		System.out.println(getSCTMask());
	}

	/**
	 * Prints the target list.
	 */
	public void printTargetList() {
		System.out.print("info[ParameterNamedSpeaker] \t --nTargetList \t list of target speakers to identify = ");
		System.out.println(getTargetList());
	}

	/**
	 * Prints the remove check gender.
	 */
	public void printRemoveCheckGender() {
		System.out.print("info[ParameterNamedSpeaker] \t --nRemoveCheckGender \t remove the gender check = ");
		System.out.println(getTargetList());
	}
}