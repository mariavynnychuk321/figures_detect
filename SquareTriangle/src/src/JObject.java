package src;

import org.opencv.core.Mat;

public abstract class JObject {
	public String name = "JObject";
	public boolean isFinded = false;
	public double boundaryWidth = 0.0;
	public double boundaryHeight = 0.0;
	
	public abstract boolean search(Mat frame);
}
