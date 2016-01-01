package fr.lium.experimental.video;

import static com.googlecode.javacv.cpp.opencv_calib3d.cvRodrigues2;
import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_core.cvFillConvexPoly;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvWarpPerspective;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.io.File;
import java.net.URL;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvPoint;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;

/**
 * The Class OpenCVDemo.
 */
public class OpenCVDemo {

	/**
	 * The main method.
	 * 
	 * @param args the arguments
	 * @throws Exception the exception
	 */
	public static void main(String[] args) throws Exception {
		String classifierName = null;
		if (args.length > 0) {
			classifierName = args[0];
		} else {
			URL url = new URL("https://raw.github.com/Itseez/opencv/master/data/haarcascades/haarcascade_frontalface_alt.xml");
			File file = Loader.extractResource(url, null, "classifier", ".xml");
			file.deleteOnExit();
			classifierName = file.getAbsolutePath();
		}

		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);

		// We can "cast" Pointer objects by instantiating a new object of the desired class.
		CvHaarClassifierCascade classifier = new CvHaarClassifierCascade(cvLoad(classifierName));
		if (classifier.isNull()) {
			System.err.println("Error loading classifier file \"" + classifierName + "\".");
			System.exit(1);
		}

		// The available FrameGrabber classes include OpenCVFrameGrabber (opencv_highgui),
		// DC1394FrameGrabber, FlyCaptureFrameGrabber, OpenKinectFrameGrabber,
		// PS3EyeFrameGrabber, VideoInputFrameGrabber, and FFmpegFrameGrabber.
		FrameGrabber grabber = FrameGrabber.createDefault(0);
		grabber.start();

		// FAQ about IplImage:
		// - For custom raw processing of data, getByteBuffer() returns an NIO direct
		// buffer wrapped around the memory pointed by imageData, and under Android we can
		// also use that Buffer with Bitmap.copyPixelsFromBuffer() and copyPixelsToBuffer().
		// - To get a BufferedImage from an IplImage, we may call getBufferedImage().
		// - The createFrom() factory method can construct an IplImage from a BufferedImage.
		// - There are also a few copy*() methods for BufferedImage<->IplImage data transfers.
		IplImage grabbedImage = grabber.grab();
		int width = grabbedImage.width();
		int height = grabbedImage.height();
		IplImage grayImage = IplImage.create(width, height, IPL_DEPTH_8U, 1);
		IplImage rotatedImage = grabbedImage.clone();

		// Objects allocated with a create*() or clone() factory method are automatically released
		// by the garbage collector, but may still be explicitly released by calling release().
		// You shall NOT call cvReleaseImage(), cvReleaseMemStorage(), etc. on objects allocated this way.
		CvMemStorage storage = CvMemStorage.create();

		// The OpenCVFrameRecorder class simply uses the CvVideoWriter of opencv_highgui,
		// but FFmpegFrameRecorder also exists as a more versatile alternative.
		// Syl; FrameRecorder recorder = FrameRecorder.createDefault("output.avi", width, height);
		// Syl; recorder.start();

		// CanvasFrame is a JFrame containing a Canvas component, which is hardware accelerated.
		// It can also switch into full-screen mode when called with a screenNumber.
		// We should also specify the relative monitor/camera response for proper gamma correction.
		CanvasFrame frame = new CanvasFrame("Some Title", CanvasFrame.getDefaultGamma() / grabber.getGamma());

		// Let's create some random 3D rotation...
		CvMat randomR = CvMat.create(3, 3), randomAxis = CvMat.create(3, 1);
		// We can easily and efficiently access the elements of CvMat objects
		// with the set of get() and put() methods.
		randomAxis.put((Math.random() - 0.5) / 4, (Math.random() - 0.5) / 4, (Math.random() - 0.5) / 4);
		cvRodrigues2(randomAxis, randomR, null);
		double f = (width + height) / 2.0;
		randomR.put(0, 2, randomR.get(0, 2) * f);
		randomR.put(1, 2, randomR.get(1, 2) * f);
		randomR.put(2, 0, randomR.get(2, 0) / f);
		randomR.put(2, 1, randomR.get(2, 1) / f);
		System.out.println(randomR);

		// We can allocate native arrays using constructors taking an integer as argument.
		CvPoint hatPoints = new CvPoint(3);

		while (frame.isVisible() && ((grabbedImage = grabber.grab()) != null)) {
			cvClearMemStorage(storage);

			// Let's try to detect some faces! but we need a grayscale image...
			cvCvtColor(grabbedImage, grayImage, CV_BGR2GRAY);
			CvSeq faces = cvHaarDetectObjects(grayImage, classifier, storage, 1.1, 3, CV_HAAR_DO_CANNY_PRUNING);
			int total = faces.total();
			for (int i = 0; i < total; i++) {
				CvRect r = new CvRect(cvGetSeqElem(faces, i));
				int x = r.x(), y = r.y(), w = r.width(), h = r.height();
				cvRectangle(grabbedImage, cvPoint(x, y), cvPoint(x + w, y + h), CvScalar.RED, 1, CV_AA, 0);

				// To access or pass as argument the elements of a native array, call position() before.
				hatPoints.position(0).x(x - (w / 10)).y(y - (h / 10));
				hatPoints.position(1).x(x + ((w * 11) / 10)).y(y - (h / 10));
				hatPoints.position(2).x(x + (w / 2)).y(y - (h / 2));
				cvFillConvexPoly(grabbedImage, hatPoints.position(0), 3, CvScalar.GREEN, CV_AA, 0);
			}

			// Let's find some contours! but first some thresholding...
			cvThreshold(grayImage, grayImage, 64, 255, CV_THRESH_BINARY);

			// To check if an output argument is null we may call either isNull() or equals(null).
			CvSeq contour = new CvSeq(null);
			cvFindContours(grayImage, storage, contour, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE);
			while ((contour != null) && !contour.isNull()) {
				if (contour.elem_size() > 0) {
					CvSeq points = cvApproxPoly(contour, Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP, cvContourPerimeter(contour) * 0.02, 0);
					cvDrawContours(grabbedImage, points, CvScalar.BLUE, CvScalar.BLUE, -1, 1, CV_AA);
				}
				contour = contour.h_next();
			}

			cvWarpPerspective(grabbedImage, rotatedImage, randomR);

			frame.showImage(rotatedImage);
			// Syl; recorder.record(rotatedImage);
		}
		frame.dispose();
		// Syl; recorder.stop();
		grabber.stop();
	}
}