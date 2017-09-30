package net.starkus.mipseditor.assistant;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.reactfx.EventStreams;

import javafx.concurrent.Task;
import net.starkus.mipseditor.assistant.keyword.KeywordDefine;
import net.starkus.mipseditor.assistant.keyword.KeywordLabel;
import net.starkus.mipseditor.file.FileManager;
import net.starkus.mipseditor.file.LoadedFile;
import net.starkus.mipseditor.file.LoadedFileReference;
import net.starkus.mipseditor.util.StringUtils;

public class CodeProcessor {
	
	
	private List<LoadedFile> addedLoadedFiles;
	
	
	@SuppressWarnings("unchecked")
	public CodeProcessor()
	{		
		EventStreams.changesOf(FileManager.getLoadedfiles())
				.filter(ch -> {
					while (ch.next())
						if (ch.wasAdded() && !ch.getAddedSubList().equals(ch.getRemoved()))
						{
							addedLoadedFiles = (List<LoadedFile>) ch.getAddedSubList();
							return true;
						}
					
					return false;
				})
				.successionEnds(Duration.ofMillis(1000))
				.supplyTask(this::process)
				.subscribe(e -> {});
		
		/*FileManager.getLoadedfiles().addListener(new ListChangeListener<File, String>() {
			@Override
			public void onChanged(
					MapChangeListener.Change<? extends File, ? extends String> change) {
				
				if (change.wasAdded())
					relevantFiles.add(change.getKey());
				
				else if (change.wasRemoved())
					relevantFiles.remove(change.getKey());
				
				process();
			}
		});*/
		
		/*FileManager.getLoadedfiles().addListener(new ListChangeListener<LoadedFile>() {
			@SuppressWarnings("unchecked")
			@Override
			public void onChanged(Change<? extends LoadedFile> change) {
				while (change.next())
				{
					if (change.wasAdded() && !change.getAddedSubList().equals(change.getRemoved()))
					{
						addedLoadedFiles = (List<LoadedFile>) change.getAddedSubList();
						process();
					}
				}
			}
		});*/
	}
	
	private Task<Void> process()
	{
		List<LoadedFile> foundDependencies = checkDependancies(addedLoadedFiles);
		
		if (foundDependencies.isEmpty())
			return null;
		
		
		FileManager.getLoadedfiles().addAll(foundDependencies);
		makeDefineList();
		
		SyntaxHighlights.buildPatterns();
		
		return null;
	}
	
	private List<LoadedFile> checkDependancies(List<LoadedFile> loadedFiles)
	{
		/*
		 * This function is meant to read currently open files
		 * looking for .include commands, look for those included files,
		 * and repeat the process with them, so to find every referenced
		 * file.
		 */
		
		List<LoadedFile> foundDependencies = new ArrayList<>();
		
		for (LoadedFile loadedFile : loadedFiles)
		{
			String source = loadedFile.getSource();
			
			int nowOccurrence, lastOccurrence = 0;
			while ((nowOccurrence = source.indexOf(".include", lastOccurrence)) != -1)
			{
				nowOccurrence += 10; // ".include" is 8 characters long, plus 2.
				
				String filename = StringUtils.getWordFromIndex(source, nowOccurrence);
				
				if (filename != null)
				{
					filename = filename.replaceAll("\"", "");
					File file = new File(loadedFile.getFile().getParentFile(), filename);
					
					if (file.exists())
					{
						if (FileManager.getLoadedFileFromFile(file) == null) // Not open already
						{
							LoadedFileReference dependencyLoadedFile = new LoadedFileReference(file, loadedFile);
							foundDependencies.add(dependencyLoadedFile);
						}
					}
					else
					{
						int line = source.substring(0, nowOccurrence).split("\n").length;
						
						System.err.println("Couldn't locate included file");
						System.err.println("At " + loadedFile.getFile().getName() + ", line " + line);
					}
				}
				
				lastOccurrence = nowOccurrence;
			}
		}
		
		if (!foundDependencies.isEmpty())
		{
			// Recursive
			foundDependencies.addAll(checkDependancies(foundDependencies));
		}
		
		return foundDependencies;
	}
	
	private void makeDefineList()
	{
		String descBuilding = null;
		String description = null;
		
		for (LoadedFile loadedFile : FileManager.getLoadedfiles())
		{
			for (String line : loadedFile.read().split("\n"))
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
							"Label defined in " + loadedFile.getFile().getName());
					
					Assistant.getKeywordBank().getKeywords().put(label, l);
				}
			}
		}
	}
}
