package src;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class JCircle extends JObject {
	
	public double radius = 0.0;
	public Point centerPoint = new Point();
	
	public JCircle() {
		name = "Коло";
	}

	@Override
	public boolean search(Mat frame) {		/*************** ADD CHECK FOR ACTUAL CIRCLE (LIKE COUNT PIXELS ACOSS) **************/
		isFinded = false;
		this.radius = 0.0;
		this.centerPoint = new Point();
		Mat circles = new Mat();
		Mat workingframe = frame.clone();
		
		Core.bitwise_not(workingframe, workingframe);
		Imgproc.blur( workingframe, workingframe, new Size(9.0, 9.0)); //	increases accuracy, very stable
		Imgproc.HoughCircles(workingframe, circles ,Imgproc.HOUGH_GRADIENT, 1.1, workingframe.height()/100.0);
		
		System.out.println("circles.cols : " + circles.cols());
		double radiusRes = 0, xRes = 0, yRes = 0;
		if (circles.cols() > 0){
		    for (int x = 0; x < circles.cols(); x++){
		        double vCircle[] = circles.get(0,x);
		        if (vCircle == null)break;
		        radiusRes += vCircle[2];
		        xRes += vCircle[0];
		        yRes += vCircle[1];
	        }
		    radiusRes = radiusRes / circles.cols();
		    xRes = xRes / circles.cols();
		    yRes = yRes / circles.cols();
		    centerPoint = new Point(xRes, yRes);
		}else{
			return false;
		}
		
		this.radius = radiusRes;
		this.boundaryWidth = radiusRes;
		this.boundaryHeight = radiusRes;		
		isFinded = true;
		return true;
	}
}
