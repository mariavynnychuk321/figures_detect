package src;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class MyImageUtils {
	
	public static Image matToImage(Mat frame){
		try{
			return SwingFXUtils.toFXImage(matToBufferedImage(frame), null);
		}catch (Exception e){
			System.err.println("Can't convert the Mat to Image: " + e);
			return null;
		}
	}
	
	static Mat arrays2Mat(int[][] dump){
		BufferedImage returns = new BufferedImage(dump.length, dump[0].length, BufferedImage.TYPE_4BYTE_ABGR);
		for(int x= 0; x < dump.length; x++){
			for(int y = 0; y < dump[0].length; y++){
				returns.setRGB(x, y, dump[x][y]);
			}	
		}
		
		Mat mat = new Mat(returns.getHeight(), returns.getWidth(), 24);
		mat.put(0, 0, ((DataBufferByte) returns.getRaster().getDataBuffer()).getData());
		return mat;
	}
	
	public static BufferedImage matToBufferedImage(Mat original){
		BufferedImage image = null;
		int width = original.width(), height = original.height(), channels = original.channels();
		byte[] sourcePixels = new byte[width * height * channels];
		original.get(0, 0, sourcePixels);
		
		if (original.channels() > 1){
			image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		}else{
			image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		}
		final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
		
		return image;
	}
	
	static void getBinaryMatrix(Mat frame, int threshold, int[][] frameArray) {
		Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.threshold(frame, frame, threshold, 255, Imgproc.THRESH_BINARY);	// creates array w/ 0 or 255 (not 1, due to displaying)
		getMatrix(frame, frameArray);
	}
	
	static void getMatrix(Mat frame, int[][] frameArray) {
		BufferedImage image = matToBufferedImage(frame);
		int w = image.getWidth();
		int h = image.getHeight();

		frameArray = new int[w][h];
		
		Raster raster =  image.getData();
		for (int j = 0; j < w; j++) {
		    for (int k = 0; k < h; k++) {
		    	frameArray[j][k] = raster.getSample(j, k, 0);
		    }
		}		
	}
}
