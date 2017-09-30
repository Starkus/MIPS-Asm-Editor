package net.starkus.mipseditor.file;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.reactfx.EventStreams;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.starkus.mipseditor.MainApp;
import net.starkus.mipseditor.savedata.RecentFileHistory;

public class FileManager {
	
	private static final ObservableList<LoadedFile> loadedFiles = FXCollections.observableArrayList(f -> 
			new Observable[] { f.removeFlagProperty() });

	private static final ObjectProperty<EventHandler<FileEvents.LoadedFileEvent>> onFileOpened = new SimpleObjectProperty<>();
	private static final ObjectProperty<EventHandler<FileEvents.LoadedFileEvent>> onFileClosed = new SimpleObjectProperty<>();
	private static final ObjectProperty<EventHandler<FileEvents.LoadedFileEvent>> onFileSaved = new SimpleObjectProperty<>();
	private static final ObjectProperty<EventHandler<FileEvents.LoadedFilePathChangedEvent>> onFilePathChanged = new SimpleObjectProperty<>();
	
	private static FilteredList<LoadedFile> flaggedFiles;
	
	
	static {
		flaggedFiles = loadedFiles.filtered(f -> f.getRemoveFlag());
		
		loadedFiles.addListener(new ListChangeListener<LoadedFile>() {

			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends LoadedFile> c)
			{
				System.out.print("Files in memory: [");
				for (LoadedFile loadedFile : loadedFiles)
				{
					System.out.print(loadedFile.getFile() == null ? "null" : loadedFile.getFile().getName());
					System.out.print(", ");
				}
				System.out.println("]");
			}
		});
		
		EventStreams.changesOf(flaggedFiles)
				.filter(ch -> {
					while (ch.next())
					{
						return (ch.wasAdded() && !ch.getRemoved().equals(ch.getAddedSubList()));
					}
					return false;
				})
				.successionEnds(Duration.ofMillis(1200))
				.subscribe(c -> removeFlaggedLoadedFiles());
	}
	
	
	public static LoadedFileOpen newFile()
	{
		/* Make a new file with unique name, in last recent directory. */
		File recentDirectory = RecentFileHistory.getRecentFiles().get(0);
		
		File file = new File(recentDirectory, "Untitled");
		
		/* Ensure unique name */
		int c = 0;
		while (FileManager.getLoadedfiles().contains(file))
		{
			c++;
			file = new File(recentDirectory, "Untitled_" + c);
		}
		
		LoadedFileOpen loadedFile = new LoadedFileOpen();
		FileEvents.LoadedFileEvent e = new FileEvents.LoadedFileEvent(loadedFile);
		
		loadedFile.onLoad(e);
		
		if (e.isConsumed())
			return null;
		
		loadedFiles.add(loadedFile);
		
		return loadedFile;
	}
	
	
	public static LoadedFileOpen openFile(File file)
	{
		LoadedFile alreadyOpen = getLoadedFileFromFile(file);
		if (alreadyOpen != null)
		{
			if (alreadyOpen.getClass().equals(LoadedFileReference.class))
			{
				loadedFiles.remove(alreadyOpen);
			}
			else if (alreadyOpen.getClass().equals(LoadedFileOpen.class))
			{
				return (LoadedFileOpen) alreadyOpen;
			}
		}
		
		LoadedFileOpen loadedFile = new LoadedFileOpen(file);
		FileEvents.LoadedFileEvent e = new FileEvents.LoadedFileEvent(loadedFile);
		
		loadedFile.onLoad(e);
		
		if (e.isConsumed())
			return null;
		
		loadedFiles.add(loadedFile);
		
		//MainApp.getFileChangeWatcher().registerFile(file.getParentFile().getAbsolutePath());
		
		RecentFileHistory.addFirst(loadedFile.getFile());

		if (onFileOpened.get() != null)
			onFileOpened.get().handle(new FileEvents.LoadedFileEvent(loadedFile));
		
		return loadedFile;
	}
	
	public static boolean closeFile(LoadedFile loadedFile)
	{
		FileEvents.LoadedFileEvent e = new FileEvents.LoadedFileEvent(loadedFile);
		
		loadedFile.onUnload(e);

		if (!e.isConsumed())
		{
			//loadedFiles.remove(loadedFile);
			loadedFile.flagForRemoval();

			if (onFileClosed.get() != null)
				onFileClosed.get().handle(new FileEvents.LoadedFileEvent(loadedFile));
		}
		return !e.isConsumed();
	}
	
	public static void saveFile(LoadedFileOpen loadedFile)
	{		
		assert loadedFile.getFile().exists();
		
		loadedFile.save();
		
		RecentFileHistory.addFirst(loadedFile.getFile());

		if (onFileSaved.get() != null)
			onFileSaved.get().handle(new FileEvents.LoadedFileEvent(loadedFile));
	}
	
	public static void saveFileAs(LoadedFileOpen loadedFile)
	{
		File initialName = loadedFile.getFile() == null ? 
				RecentFileHistory.getRecentFiles().get(0).getParentFile() : 
				loadedFile.getFile();
		
		File newFile = browseFile(MainApp.getWindow(), true, initialName);
		
		if (newFile == null) // Cancelled
			return;
		
		loadedFile.setFile(newFile);
		loadedFile.save();
		
		RecentFileHistory.addFirst(newFile);
		
		if (onFileSaved.get() != null)
			onFileSaved.get().handle(new FileEvents.LoadedFileEvent(loadedFile));
	}
	
	public static void saveFileCopy(LoadedFileOpen loadedFile)
	{
		File file = loadedFile.getFile();
		
		File initialName = file == null ? 
				RecentFileHistory.getRecentFiles().get(0) : 
				new File(file.getParentFile(), 
						file.getName().substring(0, file.getName().lastIndexOf('.')) + " - Copy");
		
		File copy = browseFile(MainApp.getWindow(), true, initialName);
		
		if (copy != null) // Not cancelled
			loadedFile.saveCopy(copy);
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
	
	public static LoadedFile getLoadedFileFromFile(File file)
	{
		FilteredList<LoadedFile> matches = loadedFiles.filtered(lf -> lf.getFile() != null && lf.getFile().equals(file)); 
		
		if (matches.isEmpty())
			return null;
		else
		{
			assert matches.size() == 1;
			return matches.get(0);
		}
	}
	
	public static boolean isFileLoaded(File file)
	{
		return getLoadedFileFromFile(file) == null;
	}
	
	
	private static void removeFlaggedLoadedFiles()
	{
		List<LoadedFile> flagged = new ArrayList<>(flaggedFiles);
		
		for (LoadedFile flaggedFile : flagged)
		{
			loadedFiles.remove(flaggedFile);
		}
	}
	
	
	public static ObservableList<LoadedFile> getLoadedfiles() {
		return loadedFiles;
	}

	public static EventHandler<FileEvents.LoadedFileEvent> getOnFileOpened() {
		return onFileOpened.get();
	}
	public static void setOnFileOpened(EventHandler<FileEvents.LoadedFileEvent> handler) {
		onFileOpened.set(handler);
	}
	public static ObjectProperty<EventHandler<FileEvents.LoadedFileEvent>> onFileOpenedProperty() {
		return onFileOpened;
	}

	public static EventHandler<FileEvents.LoadedFileEvent> getOnFileClosed() {
		return onFileClosed.get();
	}
	public static void setOnFileClosed(EventHandler<FileEvents.LoadedFileEvent> handler) {
		onFileClosed.set(handler);
	}
	public static ObjectProperty<EventHandler<FileEvents.LoadedFileEvent>> onFileClosedProperty() {
		return onFileClosed;
	}

	public static EventHandler<FileEvents.LoadedFileEvent> getOnFileSaved() {
		return onFileSaved.get();
	}
	public static void setOnFileSaved(EventHandler<FileEvents.LoadedFileEvent> handler) {
		onFileSaved.set(handler);
	}
	public static ObjectProperty<EventHandler<FileEvents.LoadedFileEvent>> onFileSavedProperty() {
		return onFileSaved;
	}

	public static EventHandler<FileEvents.LoadedFilePathChangedEvent> getOnFilePathChanged() {
		return onFilePathChanged.get();
	}
	public static void setOnFilePathChanged(EventHandler<FileEvents.LoadedFilePathChangedEvent> handler) {
		onFilePathChanged.set(handler);
	}
	public static ObjectProperty<EventHandler<FileEvents.LoadedFilePathChangedEvent>> onFilePathChangedProperty() {
		return onFilePathChanged;
	}
}
