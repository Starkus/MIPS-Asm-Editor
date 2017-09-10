package net.starkus.mipseditor.savedata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class JSONUtils {
	
	private static final File file = new File(filePath());
	
	
	private static String filePath()
	{
		if (System.getProperty("os.name").toLowerCase().contains("win"))
			return System.getenv("APPDATA") + "/MIPSEditor/save.xml";
		else
			return System.getProperty("user.home") + "/MIPSEditor/save.xml";
	}
	
	
	private static class SaveBundle {
		
		private String[] recentFiles;
		
		SaveBundle() {
			List<File> fileList = RecentFileHistory.getRecentFiles();
			this.recentFiles = new String[fileList.size()];
			
			for (int i=0; i < recentFiles.length; ++i)
			{
				recentFiles[i] = fileList.get(i).getAbsolutePath();
			}
		}
	}
	
	
	public static void Save()
	{
		try {
			
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			
			
			writer.write(gson.toJson(new SaveBundle()));
			
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void Load()
	{
		try {
			
			BufferedReader reader = new BufferedReader(new FileReader(file));
			
			String json = "";
			String currentLine;
			while ((currentLine = reader.readLine()) != null)
			{
				json += currentLine;
			}
			
			reader.close();
			
			
			Gson gson = new Gson();
			SaveBundle bundle = gson.fromJson(json, SaveBundle.class);
			
			RecentFileHistory.clear();
			for (String filepath : bundle.recentFiles)
				RecentFileHistory.addLast(new File(filepath));
		}
		
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
