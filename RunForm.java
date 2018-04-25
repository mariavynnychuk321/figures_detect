package application;

import java.io.FileInputStream;
import java.io.IOException;
 
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
 
public class RunForm extends Application
{
    public static void main(String[] args)
    {
        Application.launch(args);
    }
     
    @Override
    public void start(Stage stage) throws IOException
    {
        // Create the FXMLLoader
        FXMLLoader loader = new FXMLLoader();
        // Path to the FXML File
        String fxmlDocPath = "D:/projects_java/WebcamInterface/src/application/form.fxml";
        FileInputStream fxmlStream = new FileInputStream(fxmlDocPath);
 
        Pane root = (Pane) loader.load(fxmlStream);
 
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("визначення розмірів фігури");
        
        stage.show();
         
    }
}