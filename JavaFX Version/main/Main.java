package main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
	
	private Mandelbrot set;
	
	public static void main(String[] args){
		launch(args);
	}
	
	@Override
	public void start(Stage window){
		set = new Mandelbrot();
		
		window.setTitle("MandelbrotFX - Multi-Threaded");
		window.setResizable(true);
		
		BorderPane root = new BorderPane();
		
		StackPane stack = new StackPane();
		
		ImageView img = new ImageView();
		
		ProgressIndicator pi = new ProgressIndicator();
		pi.setMaxSize(50, 50);
		pi.progressProperty().bind(set.progressProperty());
		pi.visibleProperty().bind(set.runningProperty());
		
		stack.getChildren().addAll(img, pi);
		
		root.setTop(new MenuControls());
		root.setCenter(stack);
		
		root.setMinSize(750, 550);
		
		window.setWidth(1280);
		window.setHeight(720);
		window.setMinWidth(800);
		window.setMinHeight(600);
		window.setScene(new Scene(root));
		window.show();
		
		set.setImage(img);
		
		Thread t = new Thread(set);
		t.setDaemon(true);
		t.start();
	}

}
