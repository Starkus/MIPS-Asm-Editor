package net.starkus.mipseditor.view;

import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import net.starkus.mipseditor.MainApp;

public class AlertWrapper {
	
	private Alert actualAlert;
	
	public AlertWrapper(AlertType type) {
		
		actualAlert = new Alert(type);
		
		DialogPane pane = actualAlert.getDialogPane();
		pane.getStylesheets().add(MainApp.class.getResource("Alert.css").toExternalForm());
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
