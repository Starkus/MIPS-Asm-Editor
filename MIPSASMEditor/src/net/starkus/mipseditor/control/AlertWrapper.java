package net.starkus.mipseditor.control;

import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.starkus.mipseditor.MainApp;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

public class AlertWrapper {
	
	private Alert actualAlert;
	
	public AlertWrapper(AlertType type) {
		
		actualAlert = new Alert(type);
		
		DialogPane pane = actualAlert.getDialogPane();
		
		pane.getStylesheets().add(MainApp.getResourcePath() + "Flatus.css");
		pane.getStylesheets().add(MainApp.getResourcePath() + "Alert.css");
	}
	
	
	
	public AlertWrapper setTitle(String s) {
		actualAlert.setTitle(s);
		return this;
	}
	
	public AlertWrapper setHeaderText(String s) {
		actualAlert.setHeaderText(s);
		return this;
	}
	
	public AlertWrapper setContentText(String s) {
		actualAlert.setContentText(s);
		return this;
	}
	
	
	
	public void show() {
		actualAlert.show();
	}
	
	public Optional<ButtonType> showAndWait() {
		return actualAlert.showAndWait();
	}
	
	public ButtonType getResult() {
		return actualAlert.getResult();
	}
	
	public ObservableList<ButtonType> getButtonTypes() {
		return actualAlert.getButtonTypes();
	}

}
