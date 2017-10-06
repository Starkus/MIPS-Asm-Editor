package net.starkus.mipseditor.file;

import java.io.File;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import net.starkus.mipseditor.MainApp;
import net.starkus.mipseditor.control.AlertWrapper;
import net.starkus.mipseditor.control.FileTab;
import net.starkus.mipseditor.file.FileEvents.LoadedFileEvent;

public class LoadedFileOpen extends LoadedFile {
	
	private FileTab fileTab;
	private final ReadOnlyBooleanWrapper dirty = new ReadOnlyBooleanWrapper();
	
	
	public LoadedFileOpen()
	{
		/*
		 * Initializing a LoadedFileOpen instance automatically opens and links
		 * a file tab in the main window tab pane.
		 */
		
		super();
		fileTab = new FileTab(this, fileProperty());
		
		initFileTab();
	}
	
	public LoadedFileOpen(File file)
	{
		/*
		 * Initializing a LoadedFileOpen instance automatically opens and links
		 * a file tab in the main window tab pane.
		 * 
		 * The specified file, if exists, will be loaded onto the tab.
		 */
		
		setFile(file);
		fileTab = new FileTab(this, fileProperty());
		
		initFileTab();
	}
	
	
	private void initFileTab()
	{
		/*
		 * This code takes care of creating a FileTab instance, adding it
		 * to the tab pane and linking it to this.
		 */
		
		MainApp.getMainController().requestAddTab(fileTab);
		
		
		fileTab.getCodeArea().textProperty().addListener((obs, oldv, newv) -> dirty.set(true));
		
		dirty.addListener((obs, oldv, newv) -> {
			
			String tabTitle = fileTab.getText();
			
			if (newv && !tabTitle.endsWith("*"))
				fileTab.setText(tabTitle + "*");
			
			else if (!newv && tabTitle.endsWith("*"))
				fileTab.setText(tabTitle.substring(0, tabTitle.length()-1));
		});
		
		
		fileTab.setOnCloseRequest(e -> {
			
			boolean success = FileManager.closeFile(this);
			
			if (!success)
				e.consume();
		});
		
		MainApp.getMainController().selectTab(fileTab);
	}
	
	
	@Override
	protected void onUnload(LoadedFileEvent e)
	{
		boolean success = requestClose();
		
		if (!success)
		{
			e.consume();
			return;
		}
		
		FilteredList<LoadedFile> referencedFiles = FileManager.getLoadedfiles()
				.filtered(f -> f.getClass().equals(LoadedFileReference.class));
		
		//if (!referencedFiles.isEmpty())
		for (LoadedFile loadedFile : referencedFiles)
		{
			//if (loadedFile.getClass().equals(LoadedFileReference.class))
			{
				((LoadedFileReference) loadedFile).getReferencers().remove(this);
				if (referencedFiles.isEmpty())
					break;
			}
		}
	}
	
	
	protected boolean requestClose()
	{
		/*
		 * If dirty, a confirmation dialog is shown before closing.
		 * If the user chooses not to close, returns false.
		 */
		
		if (!dirty.get())
		{
			return true;
		}
		
		String filename = getFile() == null ? "Untitled" : getFile().getName();
		String filepath = getFile() == null ? "" : getFile().getAbsolutePath();
		
		AlertWrapper alert = new AlertWrapper(AlertType.CONFIRMATION)
				.setTitle(filename + " has changes")
				.setHeaderText("There are unsaved changes. Do you want to save them?")
				.setContentText(filepath + "\nAll unsaved changes will be lost.");
		alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
		
		alert.showAndWait();
		
		if (alert.getResult() == ButtonType.YES)
		{
			boolean success = save();
			
			if (!success)
			{
				AlertWrapper errorAlert = new AlertWrapper(AlertType.ERROR)
						.setTitle("Couldn't save file!")
						.setHeaderText("There was an error while saving!")
						.setContentText("The file was not closed.");
				
				errorAlert.showAndWait();
				
				return false;
			}
		}
		
		if (alert.getResult() == ButtonType.CANCEL)
			return false; // A false return value is used to consume the closing event.
		
		
		return true;
	}
	
	public boolean save()
	{
		boolean success = FileIOHelper.saveFile(getFile(), getSource());
		
		if (success)
			dirty.set(false);
		
		return success;
	}
	
	public boolean saveCopy(File file)
	{
		boolean success = FileIOHelper.saveFile(file, getSource());
		
		return success;
	}
	
	@Override
	public String read()
	{
		if (getFile() == null)
			return "";
		
		return FileIOHelper.readFile(getFile());
	}

	@Override
	public String getSource()
	{
		return fileTab.getCodeArea().getText();
	}


	public boolean getDirty()
	{
		return dirty.get();
	}
	
	public ReadOnlyBooleanProperty dirtyProperty()
	{
		return dirty.getReadOnlyProperty();
	}
}
