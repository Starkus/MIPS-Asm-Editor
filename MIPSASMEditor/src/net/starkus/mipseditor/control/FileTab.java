package net.starkus.mipseditor.control;

import java.io.File;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import net.starkus.mipseditor.file.FileIOHelper;
import net.starkus.mipseditor.file.LoadedFileOpen;

public class FileTab extends Tab {
	
	private final LoadedFileOpen owner;
	private ReadOnlyObjectProperty<File> fileProperty;
	
	private VirtualizedScrollPane<CodeArea> scrollPane;
	private MyCodeArea codeArea;
	
	
	public FileTab(LoadedFileOpen owner, ReadOnlyObjectProperty<File> fileProperty)
	{
		this.owner = owner;
		this.fileProperty = fileProperty;

		
		codeArea = new MyCodeArea(this);
		codeArea.setAutoScrollOnDragDesired(true);

		fileProperty.addListener((obs, oldv, newv) -> {
			setText(newv == null ? "Untitled" : newv.getName());
		});
		
		
		// Init value
		File file = fileProperty.get();
		
		if (file != null)
		{
			setText(file.getName());
			
			if (fileProperty.get() != null)
			{
				codeArea.replaceText(FileIOHelper.readFile(file));
				codeArea.getUndoManager().forgetHistory();
			}
		}
		else
		{
			setText("Untitled");
		}

		scrollPane = new VirtualizedScrollPane<CodeArea>(codeArea);
		
		final double margin = 10;
		AnchorPane.setTopAnchor(scrollPane, margin);
		AnchorPane.setRightAnchor(scrollPane, margin);
		AnchorPane.setBottomAnchor(scrollPane, margin);
		AnchorPane.setLeftAnchor(scrollPane, margin);
		
		AnchorPane anchorPane = new AnchorPane(scrollPane);
		this.setContent(anchorPane);
		
		
		Platform.runLater(() -> {
			codeArea.requestFocus();
			codeArea.requestFollowCaret();
		});
	}
	
	
	public MyCodeArea getCodeArea() {
		return codeArea;
	}
	
	public File getFile()
	{
		return fileProperty.get();
	}
	
	public ReadOnlyObjectProperty<File> fileProperty()
	{
		return fileProperty;
	}
	
	
	public LoadedFileOpen getOwner()
	{
		return owner;
	}
}
