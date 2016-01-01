/**
 * 
 * <p>
 * Tool
 * </p>
 * 
 * @author <a href="mailto:sylvain.meignier@lium.univ-lemans.fr">Sylvain Meignier</a>
 * @version v2.0
 * 
 *          Copyright (c) 2007-2009 Universite du Maine All Rights Reserved. Use is subject to license terms.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE "UNIVERSITE DU MAINE" AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *          DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *          USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *          ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 *          Find silence and split a segmentation
 * 
 */
package fr.lium.experimental.EPAC.xml;

import org.w3c.dom.Element;

import fr.lium.spkDiarization.lib.DiarizationException;

/**
 * The Class Tool.
 * 
 * @author meignier
 */
public class Tool {
	// <tool type="speaker diarization" name="LIUM segmentation toolkit (BIC only)" version="prev Ester2" date="Sun Mar 22 15:42:48 2009"/>
	/** The type. */
	protected String type;

	/** The name. */
	protected String name;

	/** The version. */
	protected String version;

	/** The date. */
	protected String date;

	/**
	 * Instantiates a new tool.
	 */
	public Tool() {
		type = "speaker diarization";
		name = "LIUM segmentation toolkit (BIC only)";
		version = "prev Ester2";
		date = "Sun Mar 22 15:42:48 2009";
	}

	/**
	 * Instantiates a new tool.
	 * 
	 * @param self the self
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public Tool(Element self) throws DiarizationException {
		read(self);
	}

	/**
	 * Read.
	 * 
	 * @param self the self
	 * 
	 * @throws DiarizationException the sphinx clust exception
	 */
	public void read(Element self) throws DiarizationException {
		if (self.getNodeName().equals("tool")) {
			type = self.getAttribute("type");
			name = self.getAttribute("name");
			version = self.getAttribute("version");
			date = self.getAttribute("date");
		} else {
			throw new DiarizationException("EPAC tool: read() bad node name ");
		}
	}

	/**
	 * Write.
	 * 
	 * @param self the self
	 */
	public void write(Element self) {
		// Element self = new Element("tool");
		self.setAttribute("type", type);
		self.setAttribute("name", name);
		self.setAttribute("version", version);
		self.setAttribute("date", date);
		// return self;
	}
}
