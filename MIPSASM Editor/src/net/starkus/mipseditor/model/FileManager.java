package net.starkus.mipseditor.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.starkus.mipseditor.savedata.RecentFileHistory;

public class FileManager {

	public static String ReadFile(File file)
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
		if (file != null)
		{
			RecentFileHistory.addFirst(file);
		}
		
		return text;
	}
	
	public static void WriteFile(File file, String text)
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
	
	
	public static File BrowseFile(Stage ownerWindow, boolean save, String initFileName)
	{
		File file = null;
		
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialFileName(initFileName);
		
		if (!RecentFileHistory.getRecentFiles().isEmpty())
		{
			File mostRecentFile = RecentFileHistory.getRecentFiles().get(0);
			
			fileChooser.setInitialDirectory(mostRecentFile.getParentFile());
		}
		
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
				"Assembly source files (*.asm, *.txt", "*.asm", "*.txt");
		
		fileChooser.getExtensionFilters().add(extFilter);
		
		if (save)
			file = fileChooser.showSaveDialog(ownerWindow);
		else
			file = fileChooser.showOpenDialog(ownerWindow);
		
			
		return file;
	}
}
