/**
 * 
 * <p>
 * ParameterSegmentationOutputFile
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

/**
 * The Class ParameterSegmentationOutputFile.
 */
public class ParameterSegmentationOutputFile extends ParameterSegmentationFile implements Cloneable {

	/**
	 * Instantiates a new parameter segmentation output file.
	 * 
	 * @param parameter the parameter
	 */
	public ParameterSegmentationOutputFile(Parameter parameter) {
		super(parameter);
		type = "Output";
		setMask("%s.out.seg");
		addOption(new LongOptWithAction("s" + type + "Mask", new ActionMask(), ""));
		addOption(new LongOptWithAction("s" + type + "Format", new ActionFormatEncoding(), ""));
		addOption(new LongOptWithAction("s" + type + "Rate", new ActionRate(), ""));
	}

	/*
	 * (non-Javadoc)
	 * @see fr.lium.spkDiarization.parameter.ParameterSegmentationFile#clone()
	 */
	@Override
	protected ParameterSegmentationOutputFile clone() throws CloneNotSupportedException {
		return (ParameterSegmentationOutputFile) super.clone();
	}

}
