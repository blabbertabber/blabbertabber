package fr.lium.spkDiarization.libModel.ivector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.StringListFileIO;

/**
 * The Class IVectorArrayList.
 */
public class IVectorArrayList extends ArrayList<IVector> {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new i vector array list.
	 */
	public IVectorArrayList() {
	}

	/**
	 * Instantiates a new i vector array list.
	 * 
	 * @param arg0 the arg0
	 */
	public IVectorArrayList(int arg0) {
		super(arg0);
	}

	/**
	 * Instantiates a new i vector array list.
	 * 
	 * @param arg0 the arg0
	 */
	public IVectorArrayList(Collection<? extends IVector> arg0) {
		super(arg0);
	}

	/**
	 * Load i vector.
	 * 
	 * @param fileName the file name
	 * @return the i vector array list
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DiarizationException the diarization exception
	 */
	public static IVectorArrayList loadIVector(String fileName) throws IOException, DiarizationException {
		ArrayList<String> list = StringListFileIO.read(fileName, false);
		IVectorArrayList IVectorList = new IVectorArrayList(list.size());
		int len = -1;

		for (String line : list) {
			String[] words = line.split(" +");
			int i = -1;
			IVector iv = new IVector(words.length - 1);
			if (len == -1) {
				len = words.length;
			} else {
				if (len != words.length) {
					throw new DiarizationException("length problem: " + words[0]);
				}
			}
			for (String w : words) {
				if (i == -1) {
					iv.setName(w);
					String[] sp = w.split("#");
					if (sp.length > 1) {
						iv.setSession(sp[0]);
						iv.setSpeakerID(sp[1]);
					} else {
						iv.setSpeakerID(sp[0]);
					}
					i++;
				} else {
					iv.set(i, Double.parseDouble(w));
					i++;
				}
			}
			IVectorList.add(iv);
		}
		return IVectorList;
	}

	/**
	 * Write i vector.
	 * 
	 * @param fileName the file name
	 * @param list the list
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void writeIVector(String fileName, IVectorArrayList list) throws IOException {
		ArrayList<String> stringList = new ArrayList<String>();
		for (IVector iVector : list) {
			String ch = iVector.getName();
			for (int i = 0; i < iVector.getDimension(); i++) {
				ch += " " + String.format("%10.8f", iVector.get(i));
			}
			stringList.add(ch);
		}
		StringListFileIO.write(fileName, false, stringList);
	}

	/**
	 * Gets the speaker id list.
	 * 
	 * @return the speaker id list
	 */
	public TreeSet<String> getSpeakerIDList() {
		TreeSet<String> set = new TreeSet<String>();
		for (IVector iv : this) {
			set.add(iv.getSpeakerID());
		}
		return set;
	}

}
