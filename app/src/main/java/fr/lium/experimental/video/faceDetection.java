package fr.lium.experimental.video;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

import fr.lium.spkDiarization.lib.DiarizationException;
import fr.lium.spkDiarization.lib.MainTools;
import fr.lium.spkDiarization.lib.SpkDiarizationLogger;
import fr.lium.spkDiarization.libClusteringData.ClusterSet;
import fr.lium.spkDiarization.libClusteringData.Segment;
import fr.lium.spkDiarization.parameter.Parameter;

/**
 * The Class faceDetection.
 */
public class faceDetection {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(faceDetection.class.getName());

	/**
	 * The Class Face.
	 */
	public class Face {

		/** The index. */
		int index;

		/** The x. */
		int x;

		/** The y. */
		int y;

		/** The h. */
		int h;

		/** The w. */
		int w;

		/**
		 * Instantiates a new face.
		 * 
		 * @param index the index
		 * @param x the x
		 * @param y the y
		 * @param h the h
		 * @param w the w
		 */
		public Face(int index, int x, int y, int h, int w) {
			super();
			this.index = index;
			this.x = x;
			this.y = y;
			this.h = h;
			this.w = w;
		}
	}

	/**
	 * Detection.
	 * 
	 * @param parameter the parameter
	 * @throws DiarizationException the diarization exception
	 * @throws Exception the exception
	 */
	public void detection(Parameter parameter) throws DiarizationException, Exception {

		String classifierName = null;
		URL url = new URL("https://raw.github.com/Itseez/opencv/master/data/haarcascades/haarcascade_frontalface_default.xml");

		File file = Loader.extractResource(url, null, "classifier", ".xml");
		file.deleteOnExit();
		classifierName = file.getAbsolutePath();

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		// We can "cast" Pointer objects by instantiating a new object of the desired class.
		CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierName));
		if (classifier.isNull()) {
			System.err.println("Error loading classifier file \"" + classifierName + "\".");
			System.exit(1);
		}
		ClusterSet shotClusterSet = MainTools.readClusterSet(parameter);
		VideoFeatureSet videoFeatureSet = new VideoFeatureSet(parameter);
		videoFeatureSet.open();
		IplImage image = null;

		for (Segment segment : shotClusterSet.getSegments()) {
			videoFeatureSet.seek(segment.getStart());
			ArrayList<Face> faceList = new ArrayList<Face>();
			for (int shotIndex = segment.getStart(); shotIndex <= segment.getLast(); shotIndex++) {
				image = videoFeatureSet.grab();
				logger.info("process image: " + videoFeatureSet.getIndex());
				CvMemStorage storage = CvMemStorage.create();
				int width = image.width();
				int height = image.height();
				IplImage grayImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
				cvCvtColor(image, grayImage, CV_BGR2GRAY);
				CvSeq faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
				int total = faces.total();
				for (int i = 0; i < total; i++) {
					CvRect r = new CvRect(cvGetSeqElem(faces, i));
					Face face = new Face(shotIndex, r.x(), r.y(), r.width(), r.height());
					faceList.add(face);
				}
				cvClearMemStorage(storage);
			}
			segment.setUserData(faceList);
		}
		videoFeatureSet.close();

	}

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		SpkDiarizationLogger.setup();
		Parameter parameter = MainTools.getParameters(args);
		info(parameter, "faceDetection");
		if (parameter.show.isEmpty() == false) {
			new faceDetection().detection(parameter);
		}
	}

	/**
	 * Info.
	 * 
	 * @param parameter the parameter
	 * @param program the program
	 * @throws IllegalArgumentException the illegal argument exception
	 * @throws IllegalAccessException the illegal access exception
	 * @throws InvocationTargetException the invocation target exception
	 */
	public static void info(Parameter parameter, String program) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		if (parameter.help) {
			logger.config(parameter.getSeparator2());
			logger.config("info[program] \t name = " + program);
			parameter.getSeparator();
			parameter.logShow();

			parameter.getParameterSegmentationInputFile().logAll();
			parameter.getParameterSegmentationOutputFile().logAll(); // sOutMask
			logger.config(parameter.getSeparator());

			logger.config(parameter.getSeparator());
		}
	}
}
