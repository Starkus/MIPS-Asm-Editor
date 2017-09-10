package net.starkus.mipseditor;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import net.starkus.mipseditor.savedata.JSONUtils;
import net.starkus.mipseditor.view.MainWindowController;

public class MainApp extends Application {
	
	private static Stage window;
	private static MainWindowController mainController;
	
	public static final String appName = "MIPS Assembler Editor";
	
	
	public static MainWindowController getMainController()
	{
		return mainController;
	}
	
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		try {
			FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/MainWindow.fxml"));
			AnchorPane pane = (AnchorPane) loader.load();
			
			Scene scene = new Scene(pane);
			scene.getStylesheets().add(MainApp.class.getResource("Flatus.css").toExternalForm());
			scene.getStylesheets().add(MainApp.class.getResource("GeneralStyling.css").toExternalForm());
			
			primaryStage.setScene(scene);
			primaryStage.setTitle(appName);
			primaryStage.setMinWidth(700);
			primaryStage.setMinHeight(450);
			
			mainController = loader.getController();
			primaryStage.setOnCloseRequest(e -> {
				
				if (!mainController.requestClose())
					e.consume();
			});
			
			JSONUtils.Load();
			
			/* Drag and drop files */
			scene.setOnDragOver(e -> {
				Dragboard dragboard = e.getDragboard();
				if (dragboard.hasFiles())
					e.acceptTransferModes(TransferMode.COPY);
				else
					e.consume();
			});
			
			scene.setOnDragDropped(e -> {
				Dragboard dragboard = e.getDragboard();
				boolean success = false;
				if (dragboard.hasFiles())
				{
					success = true;
					for (File file : dragboard.getFiles())
					{
						mainController.openFile(file);
					}
				}
				e.setDropCompleted(success);
				e.consume();
			});

			window = primaryStage;
			primaryStage.show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		System.out.println("Launching app...");
		
		launch(args);
	}


	public static Stage getWindow() {
		return window;
	}
}
