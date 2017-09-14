package net.starkus.mipseditor.control;

import java.io.File;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import net.starkus.mipseditor.model.FileManager;

public class FileTab extends Tab {
	
	private ObjectProperty<File> currentFile;
	
	private VirtualizedScrollPane<CodeArea> scrollPane;
	private MyCodeArea codeArea;
	
	private final BooleanProperty dirty = new SimpleBooleanProperty(); 
	
	
	public FileTab(File file)
	{
		currentFile = new SimpleObjectProperty<File>(file);
		
		setText(file.getName());
		
		codeArea = new MyCodeArea(this);
		
		
		if (currentFile.get().exists())
			codeArea.replaceText(FileManager.getOpenfiles().get(file));
		
		
		currentFile.addListener((obs, oldv, newv) -> {
			setText(newv == null ? "Untitled" : newv.getName());
		});
		
		
		codeArea.textProperty().addListener((obs, oldv, newv) -> {
			FileManager.getOpenfiles().replace(getFile(), newv);
		});
		
		/* Dirty */
		codeArea.textProperty().addListener((obs, oldv, newv) -> dirty.set(true));
		
		dirty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue && !getText().endsWith("*"))
					setText(getText() + "*");
				else if (!newValue && getText().endsWith("*"))
					setText(getText().substring(0, getText().length()-1));
			}
		});
		
		dirty.set(!currentFile.get().exists());
		
		this.setOnCloseRequest(e -> {
			
			if (requestClose()) // requestClose() returns true if should close
				FileManager.closeFile(getFile());
			
			e.consume(); // Someone else is in charge of closing tabs!
		});

		scrollPane = new VirtualizedScrollPane<CodeArea>(codeArea);
		this.setContent(scrollPane);
		
		
		Platform.runLater(() -> {
			codeArea.requestFocus();
		});
	}
	
	
	public boolean requestClose()
	{
		if (!dirty.get())
		{
			return true;
		}
		
		String filename = currentFile == null ? "Untitled" : currentFile.getName();
		
		AlertWrapper alert = new AlertWrapper(AlertType.CONFIRMATION)
				.setTitle(filename + " has changes")
				.setHeaderText("There are unsaved changes. Do you want to save them?")
				.setContentText(getFile().getAbsolutePath() + "\nAll unsaved changes will be lost.");
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		
		alert.showAndWait();
		
		if (alert.getResult() == ButtonType.YES)
			FileManager.saveFile(getFile());
		
		if (alert.getResult() == ButtonType.CANCEL)
			return false; // A false return value is used to consume the closing event.
		
		return true;
	}
	
	
	public CodeArea getCodeArea() {
		return codeArea;
	}
	
	public void setFile(File file) {
		this.currentFile.set(file);
	}
	
	public File getFile() {
		return currentFile.get();
	}
	
	public ObjectProperty<File> fileProperty() {
		return currentFile;
	}
	
	public void setDirty(boolean f) {
		dirty.set(f);
	}
	
	public boolean isDirty() {
		return dirty.get();
	}
	
	public BooleanProperty dirtyProperty() {
		return dirty;
	}
}
