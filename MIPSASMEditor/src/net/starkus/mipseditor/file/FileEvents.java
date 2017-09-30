package net.starkus.mipseditor.file;

import java.io.File;

import javafx.event.Event;
import javafx.event.EventType;

public class FileEvents {

	public static class LoadedFileEvent extends Event
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		
		private final LoadedFile loadedFile;
		
		public LoadedFileEvent(LoadedFile loadedFile)
		{
			super(EventType.ROOT);
			
			this.loadedFile = loadedFile;
		}
		
		public LoadedFile getLoadedFile()
		{
			return loadedFile;
		}
	}
	
	public static class LoadedFilePathChangedEvent extends Event
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		private final File oldFile;
		private final File newFile;
		
		public LoadedFilePathChangedEvent(LoadedFile loadedFile, File oldPath, File newPath)
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
