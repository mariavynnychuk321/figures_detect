package app;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class JSquare extends JObject {

	boolean isSquarish = false;
	boolean isFilled = false;
	Point[] corners = new Point[4];
	
	public JSquare(boolean isFilled) {
		name = "Прямокутник";
		this.isFilled = isFilled;
	}
	
	@Override
	public boolean search(Mat frame) {
		System.out.println("Looking for Square!");
		isFinded = false;
		isSquarish = false;
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
		
		corners[0] = new Point(xmin, ymin);
		corners[1] = new Point(xmax, ymax);
		corners[2] = new Point(xmin, ymax);
		corners[3] = new Point(xmax, ymin);

		double lengthX = this.boundaryWidth = getDistance(corners[0], corners[2]);
		double lengthY = this.boundaryHeight = getDistance(corners[1], corners[2]);
		
		double error = 2;
		isSquarish = false;
		
		if(Math.abs(lengthX - lengthY) > error){

			isSquarish = true;
			System.out.println("isSquarish \\0/");
			
			double[] checkLT = workingframe.get(		// LeftTop
					(int)(corners[0].y + lengthY*0.02),
					(int)(corners[0].x + lengthX*0.02)
				);
			double[] checkCT = workingframe.get(		// CenterTop
					(int)(corners[0].y + lengthY*0.02),
					(int)(corners[0].x + lengthX*0.5)
				);
			double[] checkCP = workingframe.get(		// Center
					(int)(corners[0].y + lengthY*0.5),
					(int)(corners[0].x + lengthX*0.5)
				);

			if(checkLT != null && checkCT != null && checkCP != null){
				if(checkLT[0] - checkCT[0] <= 15.0){
					if(isFilled){
						if(Math.abs(checkLT[0] - checkCP[0]) <= 15.0){     
							System.out.println("Not a weird filled structure \\0/");
							isFinded = true;
							return true;
						}
					}else{
						if(Math.abs(checkLT[0] - checkCP[0]) >= 15.0){     
							System.out.println("Not a weird bordered structure \\0/");
							isFinded = true;
							return true;
						}
						else{
							System.out.println("Weird bordered structure -_-");
						}
					}
				}
			}
		}else{
			System.out.println("NOT SQUARISH");
		}
		return false;
	}
	
	private double getDistance(Point p1, Point p2){
		return Math.sqrt(Math.pow((Math.abs(p1.x - p2.x)),2) + Math.pow((Math.abs(p1.y - p2.y)),2));
	}

}
