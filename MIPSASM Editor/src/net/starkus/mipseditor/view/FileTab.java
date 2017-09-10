package net.starkus.mipseditor.view;

import java.io.File;
import java.time.Duration;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.MouseOverTextEvent;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import net.starkus.mipseditor.model.FileManager;
import net.starkus.mipseditor.syntax.Syntax;

public class FileTab extends Tab {
	
	private File file;
	private CodeArea codeArea;
	
	private Popup popup = new Popup();
	private TextFlow popupFlow = new TextFlow();
	
	private final BooleanProperty dirty = new SimpleBooleanProperty(); 
	
	
	public FileTab(File file)
	{
		this.file = file;
		
		setText(file == null ? "Untitled" : file.getName());
		
		codeArea = new CodeArea();
		codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
		
		
		codeArea.richChanges()
				.filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
				.subscribe(change -> {
					
					codeArea.setStyleSpans(0, Syntax.computeHighlighting(codeArea.getText()));
					dirty.set(true);
				});
		if (file != null)
			codeArea.replaceText(FileManager.ReadFile(file));
		
		/* Tooltips */
		popup.getContent().add(popupFlow);
		popupFlow.getStyleClass().add("tooltip");
		
		codeArea.setMouseOverTextDelay(Duration.ofMillis(400));
		codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {
			
			int charIndex = e.getCharacterIndex();
			Point2D pos = e.getScreenPosition();
			
			boolean tooltip = Syntax.makeTooltipFromCodeIndex(popupFlow, codeArea.getText(), charIndex);
			
			if (tooltip)
			{
				popup.show(codeArea, pos.getX(), pos.getY() + 10);
			}
		});
		codeArea.addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
			popup.hide();
		});
		
		
		/* Dirty */
		dirty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue && !getText().endsWith("*"))
					setText(getText() + "*");
				else if (!newValue && getText().endsWith("*"))
					setText(getText().substring(0, getText().length()-1));
			}
		});
		
		dirty.set(file == null);
		
		this.setOnCloseRequest(e -> {
			
			if (!requestClose()) // requestClose() returns true if should close
				e.consume();
		});
		
		this.setContent(codeArea);
	}
	
	
	public boolean requestClose()
	{
		if (!dirty.get())
		{
			return true;
		}
		
		AlertWrapper alert = new AlertWrapper(AlertType.CONFIRMATION)
				.setTitle(getFile().getName() + " has changes")
				.setHeaderText("There are unsaved changes. Do you want to save them?")
				.setContentText(getFile().getAbsolutePath() + "\nAll unsaved changes will be lost.");
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		
		alert.showAndWait();
		
		if (alert.getResult() == ButtonType.YES)
			saveFile();
		
		if (alert.getResult() == ButtonType.CANCEL)
			return false; // A false return value is used to consume the closing event.
		
		return true;
	}
	
	
	public void saveFile()
	{
		String code = codeArea.getText();
		
		FileManager.WriteFile(file, code);
		
		dirty.set(false);
	}
	
	
	public CodeArea getCodeArea() {
		return codeArea;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
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
