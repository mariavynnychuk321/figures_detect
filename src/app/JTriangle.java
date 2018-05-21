package app;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class JTriangle extends JObject{
	
	boolean isTriangle = false;
	boolean isFilled = false;
	Point[] corners = new Point[3];
	double sideLenght = 0.0;
	
	public JTriangle(boolean isFilled){
		name = "Трикутник";
		this.isFilled = isFilled;
	}
	
	@Override
	public boolean search(Mat frame) {
		System.out.println("Looking for Triangle!");
		isFinded = false;
		isTriangle = false;
		MatOfPoint dots = new MatOfPoint();
		Mat workingframe = frame.clone();
		
		Imgproc.blur(workingframe, workingframe, new Size(1.2, 1.2));		
		Imgproc.goodFeaturesToTrack(workingframe, dots, 50, 0.01, 10);
		
		corners = new Point[4];
		int xmax = 0;
		int ymax = 0;
		int xmin = workingframe.width();
		int ymin = workingframe.height();
		for(Point dot : dots.toArray()){
			if(dot.x > xmax){
				xmax = (int) dot.x;
			}
			if(dot.y > ymax){
				ymax = (int) dot.y;
			}
			if(dot.x < xmin){
				xmin = (int) dot.x;
			}
			if(dot.y < ymin){
				ymin = (int) dot.y;
			}
		}
		dots.release();
		int xc = (xmax + xmin)/2;
		
		corners[0] = new Point(xmin, ymin);
		corners[1] = new Point(xmax, ymin);
		corners[2] = new Point(xc, ymax);
		corners[3] = new Point(xc, ymin);
		
		double lengthX = this.boundaryWidth = getDistance(corners[0], corners[1]);
		double lengthY = this.boundaryHeight = getDistance(corners[2], corners[3]);
		
		double sideLenght = Math.sqrt(Math.pow((Math.abs(lengthX)/2), 2) + Math.pow(lengthY, 2));
		System.out.println("sl = " + sideLenght);
		System.out.println("lx = " + lengthX);
		System.out.println("ly = " + lengthY);
		double error = 4;
		isTriangle = false;
		
		if (Math.abs(sideLenght - lengthX) < error*10){
			isTriangle = true;
			System.out.println("isTriangle");
			
			isFinded = true;
			if (Math.abs(sideLenght) - Math.abs(lengthX) <= error){
				return true;
			}else{
				return false;
				}
			}else{
				System.out.println("Not a Triangle!");
			}
		return false;
	}
	private double getDistance(Point p1, Point p2){
		return Math.sqrt(Math.pow((Math.abs(p1.x - p2.x)),2) + Math.pow((Math.abs(p1.y - p2.y)),2));
	}
}
