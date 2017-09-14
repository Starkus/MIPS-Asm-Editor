package net.starkus.mipseditor.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.starkus.mipseditor.MainApp;
import net.starkus.mipseditor.savedata.RecentFileHistory;

public class FileManager {
	
	private static final ObservableMap<File, String> openFiles = FXCollections.observableHashMap();

	private static final ObjectProperty<EventHandler<FileEvent>> onFileOpened = new SimpleObjectProperty<>();
	private static final ObjectProperty<EventHandler<FileEvent>> onFileClosed = new SimpleObjectProperty<>();
	private static final ObjectProperty<EventHandler<FileEvent>> onFileSaved = new SimpleObjectProperty<>();
	private static final ObjectProperty<EventHandler<FilePathChangedEvent>> onFilePathChanged = new SimpleObjectProperty<>();
	
	
	public static void openFile(File file)
	{
		String source = file.exists() ? readFile(file) : "";
		openFiles.put(file, source);
		
		onFileOpened.get().handle(new FileEvent(file));
	}
	
	public static void closeFile(File file)
	{
		FileEvent closeEvent = new FileEvent(file);
		onFileClosed.get().handle(closeEvent);

		if (!closeEvent.isConsumed())
			openFiles.remove(file);
	}
	
	public static void saveFile(File file)
	{
		if (!file.exists())
		{
			File old = file;
			file = browseFile(MainApp.getWindow(), true, file);
			
			if (file == null) // If canceled on file chooser
				return;
			
			onFilePathChanged.get().handle(new FilePathChangedEvent(old, file));
		}
		
		writeFile(file, openFiles.get(file));
	
		onFileSaved.get().handle(new FileEvent(file));
	}
	
	public static void saveFileAs(File file)
	{
		File initialName = file == null ? 
				RecentFileHistory.getRecentFiles().get(0).getParentFile() : 
				file;
		
		File oldf = file;
		File newf = browseFile(MainApp.getWindow(), true, initialName);
		
		if (newf == null)
			return;
		
		file = newf;
		
		onFilePathChanged.get().handle(new FilePathChangedEvent(oldf, file));
		
		writeFile(file, openFiles.get(file));
		
		onFileSaved.get().handle(new FileEvent(file));
	}
	
	public static void saveFileCopy(File file)
	{
		File initialName = file == null ? 
				RecentFileHistory.getRecentFiles().get(0) : 
				new File(file.getParentFile(), 
						file.getName().substring(0, file.getName().lastIndexOf('.')) + " - Copy");
		
		File copy = browseFile(MainApp.getWindow(), true, initialName);
		
		if (copy != null)
			writeFile(copy, openFiles.get(file));
		
		//onFileSaved.get().handle(new FileEvent(copy)); This file is not open... or may it?
	}
	

	private static String readFile(File file)
	{
		String text = "";
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String currentLine;
			
			while ((currentLine = br.readLine()) != null)
			{
				if (!text.isEmpty())
					text += "\n";
				
				text += currentLine;
			}
			
			br.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		/* Make entry to file history */
		if (file != null && file.exists())
		{
			RecentFileHistory.addFirst(file);
		}
		
		return text;
	}
	
	private static void writeFile(File file, String text)
	{
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
			
			bw.write(text);
			
			bw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		/* Make entry to file history */
		if (file != null)
		{
			RecentFileHistory.addFirst(file);
		}
	}
	
	
	public static File browseFile(Stage ownerWindow, boolean save, File initFileName)
	{
		File file = null;
		
		if (initFileName != null && initFileName.getParentFile().exists() == false)
			initFileName = null;
		
		FileChooser fileChooser = new FileChooser();
		if (initFileName != null)
		{
			if (!initFileName.isDirectory())
				fileChooser.setInitialFileName(initFileName.getName());
			
			fileChooser.setInitialDirectory(initFileName.getParentFile());
		}
		
		if (!RecentFileHistory.getRecentFiles().isEmpty())
		{
			File mostRecentFile = RecentFileHistory.getRecentFiles().get(0);
			
			fileChooser.setInitialDirectory(mostRecentFile.getParentFile());
		}
		
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
				"Assembly source files (*.asm, *.txt)", "*.asm", "*.txt");
		FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter(
				"All files (*.*)", "*.*");
		
		fileChooser.getExtensionFilters().addAll(extFilter, allFilter);
		
		if (save)
			file = fileChooser.showSaveDialog(ownerWindow);
		else
			file = fileChooser.showOpenDialog(ownerWindow);
		
			
		return file;
	}
	
	
	public static ObservableMap<File, String> getOpenfiles() {
		return openFiles;
	}

	public static EventHandler<FileEvent> getOnFileOpened() {
		return onFileOpened.get();
	}
	public static void setOnFileOpened(EventHandler<FileEvent> handler) {
		onFileOpened.set(handler);
	}
	public static ObjectProperty<EventHandler<FileEvent>> onFileOpenedProperty() {
		return onFileOpened;
	}

	public static EventHandler<FileEvent> getOnFileClosed() {
		return onFileClosed.get();
	}
	public static void setOnFileClosed(EventHandler<FileEvent> handler) {
		onFileClosed.set(handler);
	}
	public static ObjectProperty<EventHandler<FileEvent>> onFileClosedProperty() {
		return onFileClosed;
	}

	public static EventHandler<FileEvent> getOnFileSaved() {
		return onFileSaved.get();
	}
	public static void setOnFileSaved(EventHandler<FileEvent> handler) {
		onFileSaved.set(handler);
	}
	public static ObjectProperty<EventHandler<FileEvent>> onFileSavedProperty() {
		return onFileSaved;
	}

	public static EventHandler<FilePathChangedEvent> getOnFilePathChanged() {
		return onFilePathChanged.get();
	}
	public static void setOnFilePathChanged(EventHandler<FilePathChangedEvent> handler) {
		onFilePathChanged.set(handler);
	}
	public static ObjectProperty<EventHandler<FilePathChangedEvent>> onFilePathChangedProperty() {
		return onFilePathChanged;
	}
	
	
	
	public static class FileEvent extends Event
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private final File file;
		
		public FileEvent(File file) {
			super(EventType.ROOT);
			this.file = file;
		}
		
		public File getFile() {
			return file;
		}
	}
	
	public static class FilePathChangedEvent extends Event
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private final File oldFile;
		private final File newFile;
		
		public FilePathChangedEvent(File oldPath, File newPath)
		{
			super(EventType.ROOT);
			oldFile = oldPath;
			newFile = newPath;
		}
		
		public File getOldFile() {
			return oldFile;
		}
		public File getNewFile() {
			return newFile;
		}
	}
}
