package net.starkus.mipseditor.file;

import java.io.File;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LoadedFileReference extends LoadedFile {
	
	private String source;
	private ObservableList<LoadedFile> referencers;
	
	public LoadedFileReference(File file, LoadedFile referencer)
	{
		super();
		setFile(file);
		
		LoadedFileReference this_ = this;
		referencers = FXCollections.observableArrayList(referencer);
		referencers.addListener(new InvalidationListener() {
			@Override
			public void invalidated(Observable observable)
			{
				if (referencers.isEmpty())
				{
					FileManager.closeFile(this_);
				}
			}
		});
		
		// Load source upon creation
		source = read();
	}
	
	@Override
	public String read()
	{
		return FileIOHelper.readFile(getFile());
	}

	@Override
	public String getSource()
	{
		return source;
	}

	public ObservableList<LoadedFile> getReferencers()
	{
		return referencers;
	}
}
