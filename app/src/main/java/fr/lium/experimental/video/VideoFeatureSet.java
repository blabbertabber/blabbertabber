package fr.lium.experimental.video;

import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;

import java.io.File;
import java.io.FilenameFilter;
import java.util.logging.Logger;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect;

import fr.lium.spkDiarization.lib.IOFile;
import fr.lium.spkDiarization.parameter.Parameter;
import fr.lium.spkDiarization.parameter.ParameterVideoInputFeature.VideoFeatureFormat;

/**
 * The Class VideoFeatureSet.
 */
public class VideoFeatureSet {

	/** The Constant logger. */
	private final static Logger logger = Logger.getLogger(VideoFeatureSet.class.getName());

	/** The count. */
	protected int count;

	/** The image. */
	protected IplImage image;

	/** The parameter. */
	protected Parameter parameter;

	/** The format. */
	protected VideoFeatureFormat format;

	/** The capture. */
	protected FFmpegFrameGrabber capture;

	/** The image index. */
	protected int imageIndex;

	/** The image directory. */
	protected String imageDirectory;

	/** The image name list. */
	protected String[] imageNameList;

	/**
	 * Instantiates a new video feature set.
	 * 
	 * @param _parameter the _parameter
	 */
	public VideoFeatureSet(Parameter _parameter) {
		super();
		Loader.load(opencv_objdetect.class);
		parameter = _parameter;
		format = parameter.getParameterVideoInputFeature().getVideoFormat();

	}

	/**
	 * Open.
	 * 
	 * @throws Exception the exception
	 */
	public void open() throws Exception {
		if (format == VideoFeatureFormat.VIDEO) {
			openVideo();
		} else {
			openImage();
		}
		imageIndex = 0;
	}

	/**
	 * Open image.
	 */
	private void openImage() {
		imageDirectory = parameter.getParameterVideoInputFeature().getVideoMask();
		imageNameList = new File(imageDirectory).list(new FilenameFilter() {
			@Override
			public boolean accept(File directory, String fileName) {
				return (fileName.endsWith(".png") || fileName.endsWith(".jpg"));
			}
		});
	}

	/**
	 * Open video.
	 * 
	 * @throws Exception the exception
	 */
	private void openVideo() throws Exception {
		String fileName = IOFile.getFilename(parameter.getParameterVideoInputFeature().getVideoMask(), parameter.show);
		capture = new FFmpegFrameGrabber(fileName);
		capture.start();
	}

	/**
	 * Grab.
	 * 
	 * @return the image
	 * @throws Exception the exception
	 */

	public IplImage grab() throws Exception {
		if (format == VideoFeatureFormat.VIDEO) {
			grabFromVideo();
		} else {
			grabFromImage();
		}
		imageIndex++;
		return image;
	}

	/**
	 * Grab from image.
	 */
	private void grabFromImage() {
		if (imageIndex >= imageNameList.length) {
			image = null;
		} else {
			String fileName = imageDirectory + File.separatorChar + imageNameList[imageIndex];
			logger.info("image: " + imageIndex + " " + fileName);
			image = cvLoadImage(fileName);
		}
	}

	/**
	 * Grab from video.
	 * 
	 * @throws Exception the exception
	 */
	private void grabFromVideo() throws Exception {
		image = capture.grab();
	}

	/**
	 * Seek.
	 * 
	 * @param index the index
	 * @throws Exception the exception
	 */
	public void seek(int index) throws Exception {
		imageIndex = index;
		if (format == VideoFeatureFormat.VIDEO) {
			capture.setFrameNumber(index);
		}
	}

	/**
	 * Close.
	 * 
	 * @throws Exception the exception
	 */
	public void close() throws Exception {
		if (format == VideoFeatureFormat.VIDEO) {
			capture.stop();
		}

	}

	/**
	 * Gets the index.
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return imageIndex;
	}

	/**
	 * Gets the lenght.
	 * 
	 * @return the lenght
	 */
	public int getLenght() {
		int v;
		if (format == VideoFeatureFormat.VIDEO) {
			v = capture.getFrameNumber();
		} else {
			v = imageNameList.length;
		}
		return v;
	}
}
