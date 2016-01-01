/**
 * 
 * <p>
 * ParameterInterface
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine. All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          not more use
 */

package fr.lium.spkDiarization.parameter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * The Class ParameterBase.
 */
public abstract class ParameterBase {

	/** The logger. */
	protected Logger logger = Logger.getLogger(this.getClass().getName());

	/** The local option list. */
	protected ArrayList<LongOptWithAction> localOptionList = new ArrayList<LongOptWithAction>();

	/** The global option list. */
	protected ArrayList<LongOptWithAction> globalOptionList;

	/**
	 * Instantiates a new parameter base.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterBase(Parameter parameter) {
		super();
		globalOptionList = parameter.getOptionList();
	}

	/**
	 * Read parameter.
	 * 
	 * @param index the index
	 * @param optarg the optarg
	 * @return true, if successful
	 */
	public boolean readParameter(int index, String optarg) {
		for (LongOptWithAction option : localOptionList) {
			if (index == option.getVal()) {
				option.execute(optarg);
				return true;
			}
		}
		return false;
	}

	/**
	 * Adds the option.
	 * 
	 * @param option the option
	 */
	public void addOption(LongOptWithAction option) {
		localOptionList.add(option);
		globalOptionList.add(option);
	}

	/**
	 * Log all.
	 * 
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public void logAll() throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		for (LongOptWithAction option : localOptionList) {
			option.log(logger);
		}
	}

	/**
	 * Log.
	 * 
	 * @param key the key
	 */
	public void log(String key) {
		for (LongOptWithAction option : localOptionList) {
			if (option.getName().equals(key)) {
				option.log(logger);
			}
		}
	}

	/**
	 * Array to sting.
	 * 
	 * @param strings the strings
	 * @return the string
	 */
	public static String ArrayToSting(String[] strings) {
		String msg = "[";
		for (String s : strings) {
			msg += ", " + s;
		}
		msg.replaceFirst(",", "");
		msg += "]";
		return msg;
	}

}
