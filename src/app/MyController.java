package app;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.imgproc.Imgproc;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.videoio.VideoCapture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;

import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class MyController {
	@FXML
	private ImageView iv_video;
	@FXML
	private ImageView iv_frame;
	@FXML
	private Button btn_shot;
	@FXML
	private LineChart<?, ?> lc_abs;
	@FXML 
	private NumberAxis lc_abs_x;
	@FXML 
	private CategoryAxis lc_abs_y;
	@FXML
	private TextArea ta_details;
	
	private VideoCapture capture;
	private static int cameraId = -1;
	private int currentThreshold = 210;	//	TODO: automatic calculation
	private Mat currentFrame;
	private Mat currentFrameCrop;
	private Mat currentFrameCropPaper;
	private int[][] currentFrameArray;
	private boolean isInited = false;
	private double k = 1.0;

	private ScheduledExecutorService timer;

	public void cameraInit(){
		this.capture = new VideoCapture();
		this.capture.open(cameraId);
		startCamera();
		btn_shot.setText("Take a shot");
		isInited = true;
	}
	
	protected void startCamera(){
		if (this.capture.isOpened()){
			Runnable frameGrabber = new Runnable(){
				@Override
				public void run(){							
					updateImageView(iv_video, MyImageUtils.matToImage(grabFrame()));
				}
			};
			
			this.timer = Executors.newSingleThreadScheduledExecutor();
			this.timer.scheduleAtFixedRate(frameGrabber, 0, 330, TimeUnit.MILLISECONDS);
		}
	}
	
	ArrayList<JObject> objects = new ArrayList<>();
	@FXML
	protected void takeShot(ActionEvent event){
		if(!isInited){
			objects.add(new JSquare(false));
			objects.add(new JTriangle(false));
			cameraInit();
		}
		else{			
			MyImageUtils.getBinaryMatrix(getFrame(true), currentThreshold, currentFrameArray);
			displayDetails((byte)0, null);
			if(findA4AndCalculateK((byte) 0, getFrame(false))){
				
				for(JObject jObj : objects){
					jObj.search(currentFrameCropPaper);
				}
				
				for(byte i = 1; i <= objects.size(); i++){
					System.out.println(objects.get(i-1).isFinded);
					if(objects.get(i-1).isFinded){
						displayDetails(i, objects.get(i-1));
						System.out.println("FIG!" + i);
						break;
					}
				}
			
			}
			updateImageView(iv_frame, MyImageUtils.matToImage(currentFrameCrop));
			System.gc();
		}
	}
	
	private static int shotNumber = 0;

	static void updateImageView(ImageView view, Image image){
		Platform.runLater(() -> {
			view.imageProperty().set(image);
		});
	}

	private void displayDetails(byte id, JObject... params){	//	id: 0 - none, 1 - square, 2 - circle
		String toDisplay = "";
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		double error = Double.POSITIVE_INFINITY;
		if(!a4Corners.isEmpty() && k != 0){
			error = 210 - (a4Corners.get(1).y - a4Corners.get(0).y) / k;
		}
		toDisplay += "Похибка ~"+df.format(error) +" мм\n\n";
		System.out.println(id);
		switch(id){
			case 0:{
				toDisplay += "Об'єкт : Не знайдено\n";
			}
			break;
			case 1:{
				double width = params[0].boundaryWidth / k;
				double height = params[0].boundaryHeight / k;
				double perimeter = (width + height)*2;
				double area = width * height;
				toDisplay += "Об'єкт : "+params[0].name+"\n\n";
				 
				toDisplay += "\tдовжина: \n\t\t" + df.format(width) +" мм\n";
				toDisplay += "\tвисота: \n\t\t" + df.format(height) +" мм\n";
				toDisplay += "\tпериметр: \n\t\t" + df.format(perimeter) +" мм\n";
				toDisplay += "\tплоща: \n\t\t" + df.format(area) +" мм кв.";
			}
			break;
			case 2:{
				double width = params[0].boundaryWidth / k;
				double height = params[0].boundaryHeight / k;
				double perimeter1 = width*3;
				double perimeter2 = height*3;
				double perimeterX = (perimeter1+perimeter2)/2;
				toDisplay += "Об'єкт : "+params[0].name+"\n\n";
				 
				toDisplay += "\tдовжина: \n\t\t" + df.format(width) +" мм\n";
				toDisplay += "\tпериметр: \n\t\t" + df.format(perimeter1) +" мм\n";
			}
		}
		
		ta_details.setText(toDisplay);
	}
	
	ArrayList<Point> a4Corners = new ArrayList<Point>();
	private boolean findA4AndCalculateK(byte corner, Mat frame) {	//corner 0 - center, 1 LT, 2 RT, 3 LL, 4 RL
		if(frame != null && frame.width() > 0 && frame.height() > 0){
			switch(corner){
				case 0:{ // center
					Rect roi = new Rect(0, 0, frame.width(), frame.height());
					currentFrameCrop = new Mat(frame, roi);
					MatOfPoint dots = new MatOfPoint();
					Imgproc.goodFeaturesToTrack(currentFrameCrop, dots, 50, 0.7, 130);
					a4Corners.clear();
					int xmax = 0;
					int ymax = 0;
					int xmin = frame.width();
					int ymin = frame.height();
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
	
					a4Corners.add(new Point(xmin,ymin));
					a4Corners.add(new Point(xmax,ymax));
	
					int padding = 10;
					Rect roi2 = new Rect((int)a4Corners.get(0).x + padding, 
							(int)a4Corners.get(0).y + padding,
							(int)(a4Corners.get(1).x - a4Corners.get(0).x) - padding*2,
							(int)(a4Corners.get(1).y - a4Corners.get(0).y) - padding*2);
					
					if( 0 < roi2.width && 0 < roi2.height){
						currentFrameCropPaper = new Mat(currentFrameCrop, roi2);
	
						Imgproc.circle(currentFrameCrop, a4Corners.get(0), 12, new Scalar(128, 128, 128), 2);
						Imgproc.circle(currentFrameCrop, a4Corners.get(1), 12, new Scalar(128, 128, 128), 2);
						
						
						double k1 = (a4Corners.get(1).x - a4Corners.get(0).x) / 297 ;
						double k2 = (a4Corners.get(1).y - a4Corners.get(0).y) / 210 ;
						k = (k1 + k2) / 2.0;
						
						System.out.println("k = " + k);
						System.out.println((a4Corners.get(1).y - a4Corners.get(0).y) / k);	//	 k test (210 ideal)	
						return true;
					}else{
						k = 0;
					}
				}
			}
		}

		return false;
	}

	private Mat grabFrame(){
		Mat frame = new Mat();
		if (this.capture.isOpened()){
			try{
				this.capture.read(frame);
			}catch (Exception e){
				System.err.println("Exception during the image grabing: " + e);
			}
		}
		return frame;
	}
	
	public Mat getFrame(boolean isUpdate){
		if(isUpdate)
			currentFrame = grabFrame();//MyImageUtils.arrays2Mat(readDump());//grabFrame();
		return currentFrame;
	}
	
	int[][] readDump(){
		int[][] board = new int[1920][1080];
		try{
			BufferedReader reader = new BufferedReader(new FileReader("./object_2.mm"));
			
			String line = "";
			int row = 0;
			while((line = reader.readLine()) != null){
			   String[] cols = line.split(","); //note that if you have used space as separator you have to split on " "
			   int col = 0;
			   for(String  c : cols){
			      board[row][col] = Integer.parseInt(c);
			      col++;
			   }
			   row++;
			}	
			reader.close();
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		return board;
	}
}