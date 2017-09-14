package net.starkus.mipseditor.assistant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;

import org.reactfx.EventStreams;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import net.starkus.mipseditor.model.FileManager;
import net.starkus.mipseditor.syntax.StringUtils;

public class CodeProcessor {
	
	private final ObservableSet<File> relevantFiles;
	private final ObservableMap<String, Define> defines;
	
	public CodeProcessor()
	{
		relevantFiles = FXCollections.observableSet();
		relevantFiles.addAll(FileManager.getOpenfiles().keySet());
		
		defines = FXCollections.observableHashMap();
		
		EventStreams.changesOf(FileManager.getOpenfiles())
				.filter(ch -> !ch.getValueAdded().equals(ch.getValueRemoved()))
				.successionEnds(Duration.ofMillis(1000))
				.supplyTask(this::process)
				.subscribe(e -> {});
		
		FileManager.getOpenfiles().addListener(new MapChangeListener<File, String>() {
			@Override
			public void onChanged(
					MapChangeListener.Change<? extends File, ? extends String> change) {
				
				if (change.wasAdded())
					relevantFiles.add(change.getKey());
				
				else if (change.wasRemoved())
					relevantFiles.remove(change.getKey());
				
				process();
			}
		});
		
		relevantFiles.addListener(new SetChangeListener<File>() {
			@Override
			public void onChanged(Change<? extends File> change) {
				process();
			}
		});
	}
	
	private Task<Void> process()
	{
		checkDependancies();
		makeDefineList();
		
		return null;
	}
	
	private void checkDependancies()
	{
		for (File file : FileManager.getOpenfiles().keySet())
		{
			String source = FileManager.getOpenfiles().get(file);
			
			int nowOccurrence, lastOccurrence = 0;
			while ((nowOccurrence = source.indexOf(".include", lastOccurrence)) != -1)
			{
				nowOccurrence += 10; // ".include" is 8 characters long, plus 2.
				
				String filename = StringUtils.getWordFromIndex(source, nowOccurrence);
				filename = filename.replaceAll("\"", "");
				
				File dependencyFile = new File(file.getParentFile(), filename);
				
				lastOccurrence = nowOccurrence;
				
				if (dependencyFile.exists())
					relevantFiles.add(dependencyFile);
			}
		}
	}
	
	private void makeDefineList()
	{
		String descBuilding = null;
		String description = null;
		
		for (File f : relevantFiles)
		{
			BufferedReader reader;
			try {
				reader = new BufferedReader(new FileReader(f));
				String line;
				
				while ((line = reader.readLine()) != null)
				{
					/* Description (comment above) */
					if (line.contains("/*") && descBuilding == null)
					{
						descBuilding = line.substring(line.indexOf("/*") + 2);
					}
					else if (line.contains("*/") && descBuilding != null)
					{
						description = descBuilding + "\n" + line.substring(0, line.indexOf("*/"));
						descBuilding = null;
					}
					else if (descBuilding != null)
					{
						descBuilding += "\n" + line;
					}
					
					if (line.matches("\\[[^\n]*\\]:[^\n]*"))
					{
						Define d = new Define(
								line.substring(line.indexOf('[') + 1, line.indexOf(']')),
								line.substring(line.indexOf("]: ") + 3, line.length()),
								description
							);
						
						defines.put(d.name, d);
					}
				}
				
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public ObservableMap<String, Define> getDefines() {
		return defines;
	}
	
	
	public class Define
	{
		public final String name;
		public final String description;
		public final String value;
		
		public Define(String name, String value, String description)
		{
			this.name = name;
			this.value = value;
			this.description = description;
		}
	}
}
