package net.starkus.mipseditor.file;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class LoadedFile {

	private final ReadOnlyObjectWrapper<File> file;
	private final BooleanProperty removeFlag;
	
	
	public LoadedFile()
	{
		file = new ReadOnlyObjectWrapper<>();
		removeFlag = new SimpleBooleanProperty(false);
	}
	
	
	public abstract String getSource();
	public abstract String read();
	
	protected void onLoad(FileEvents.LoadedFileEvent e) {}
	protected void onUnload(FileEvents.LoadedFileEvent e) {}

	
	public final File getFile()
	{
		return file.get();
	}

	public final void setFile(File file)
	{
		this.file.set(file);
	}
	
	public ReadOnlyObjectProperty<File> fileProperty()
	{
		return file.getReadOnlyProperty();
	}
	
	public void flagForRemoval()
	{
		removeFlag.set(true);
	}
	
	public boolean getRemoveFlag()
	{
		return removeFlag.get();
	}
	
	public BooleanProperty removeFlagProperty()
	{
		return removeFlag;
	}
}
