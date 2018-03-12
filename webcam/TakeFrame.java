package webcam;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;        
import org.opencv.videoio.VideoCapture;     
import org.opencv.imgproc.Imgproc;
        
public class TakeFrame {
	
    public static void main (String args[]){
    	// taking pic
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    	VideoCapture camera = new VideoCapture(0);
     
    	if(!camera.isOpened()){
    		System.out.println("Error");
    	}
    	else {
    		Mat frame = new Mat();
    		while(true){
    			if (camera.read(frame)){
    				System.out.println("captured, width " + 
    						frame.width() + ", height " + frame.height());
    				
    				Mat buf = new Mat();
    		        Imgproc.cvtColor(frame, buf, Imgproc.COLOR_RGB2GRAY);
    		         
    				Mat binary = new Mat();
    		    	Imgproc.threshold(buf, binary, 100, 255, Imgproc.THRESH_BINARY);
    		    	
    				Imgcodecs.imwrite("frame.jpg", binary);
    				System.out.println("saved");
    				break;
    			}
    		} 
    	}
    	
    	camera.release();
        
        // making matrix
    	BufferedImage image = null;
        try {
            File pic = new File("frame.jpg");
            image = ImageIO.read(pic);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
           
        double[][] matrix = new double[image.getHeight()][image.getWidth()];
        for(int i = 0; i < image.getHeight(); i++) {
        	for(int j = 0; j < image.getWidth(); j++) {
        		matrix[i][j] = image.getRGB(j, i);
        	}
        }       
        
        // writing to file
        BufferedWriter outputWriter = null;
        try {
           outputWriter = new BufferedWriter(new FileWriter("data.txt"));
           for (int i = 0; i < matrix.length; i++) {
        	   for(int j = 0; j < matrix.length; j++) {
               	   outputWriter.write(matrix[i][j] + ";");
        	   }
        	   outputWriter.newLine();
           }
           outputWriter.flush();  
           outputWriter.close();
           System.out.println("file written");
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
}
