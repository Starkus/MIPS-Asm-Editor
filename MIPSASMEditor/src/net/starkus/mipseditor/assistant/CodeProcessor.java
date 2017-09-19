package net.starkus.mipseditor.assistant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;

import org.reactfx.EventStreams;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.concurrent.Task;
import net.starkus.mipseditor.assistant.keyword.KeywordDefine;
import net.starkus.mipseditor.assistant.keyword.KeywordLabel;
import net.starkus.mipseditor.model.FileManager;
import net.starkus.mipseditor.util.StringUtils;

public class CodeProcessor {
	
	private final ObservableSet<File> relevantFiles;
	
	public CodeProcessor()
	{
		relevantFiles = FXCollections.observableSet();
		relevantFiles.addAll(FileManager.getOpenfiles().keySet());
		
		EventStreams.changesOf(FileManager.getOpenfiles())
				.filter(ch -> ch.getValueAdded() == null || 
						!ch.getValueAdded().equals(ch.getValueRemoved()))
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
		
		Syntax.buildPatterns();
		
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
				
				if (filename != null)
				{
					filename = filename.replaceAll("\"", "");
					File dependencyFile = new File(file.getParentFile(), filename);
					
					if (dependencyFile.exists())
						relevantFiles.add(dependencyFile);
				}
				
				lastOccurrence = nowOccurrence;
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
				if (FileManager.getOpenfiles().containsKey(f))
					reader = new BufferedReader(new StringReader(FileManager.getOpenfiles().get(f)));
				
				else
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
						KeywordDefine d = new KeywordDefine(
								line.substring(line.indexOf('[') + 1, line.indexOf(']')),
								line.substring(line.indexOf("]: ") + 3, line.length()),
								description
							);
						
						Assistant.getKeywordBank().getKeywords().put(d.getKeyword(), d);
					}
					
					else if (line.endsWith(":"))
					{
						String label = line.substring(0, line.length()-1);
						
						KeywordLabel l = new KeywordLabel(
								label, 
								label, 
								"Label defined in " + f.getName());
						
						Assistant.getKeywordBank().getKeywords().put(label, l);
					}
				}
				
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
