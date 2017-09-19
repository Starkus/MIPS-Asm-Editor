package net.starkus.mipseditor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import insidefx.undecorator.UndecoratorScene;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.starkus.mipseditor.assistant.Syntax;
import net.starkus.mipseditor.model.FileManager;
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
		
		Syntax.initialize();
		
		try {
			FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("view/MainWindow.fxml"));
			AnchorPane pane = (AnchorPane) loader.load();
			
			UndecoratorScene scene = new UndecoratorScene(primaryStage, pane);
			scene.setFill(Color.TRANSPARENT);
			scene.addStylesheet(getResourcePath() + "Window.css");			
			scene.getStylesheets().add(getResourcePath() + "Flatus.css");	
			scene.getStylesheets().add(getResourcePath() + "CodeArea.css");
			scene.getStylesheets().add(getResourcePath() + "GeneralStyling.css");
			
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			primaryStage.setTitle(appName);
			primaryStage.setMinWidth(700);
			primaryStage.setMinHeight(450);
			primaryStage.setScene(scene);
			
			primaryStage.getIcons().setAll(new Image(getResourceAsStream("icon.png")));
			
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
						FileManager.openFile(file);
					}
				}
				e.setDropCompleted(success);
				e.consume();
			});

			window = primaryStage;
			primaryStage.show();
			
			
			if (!getParameters().getRaw().isEmpty())
				Platform.runLater(() -> {
					mainController.handleOpenFile(new File(getParameters().getUnnamed().get(0)));
				});
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static String getResourcePath()
	{
		return "/net/starkus/mipseditor/resources/";
	}
	
	public static InputStream getResourceAsStream(String name)
	{
		return MainApp.class.getResourceAsStream(getResourcePath() + name);
	}
	
	public static String getResourceAsString(String name) throws IOException
	{
		InputStream is = getResourceAsStream(name);
		
		byte[] b = new byte[is.available()];
		is.read(b);
		
		String s = new String(b);
		
		return s;
	}
	
	
	@Override
	public void stop() throws Exception {
		
		Syntax.stop();
	}
	
	public static void main(String[] args) {
		
		System.out.println("Launching app...");
		
		launch(args);
	}


	public static Stage getWindow() {
		return window;
	}
}
