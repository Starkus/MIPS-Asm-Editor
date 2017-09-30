package net.starkus.mipseditor.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class FileIOHelper {
	
	/*
	 * Okay this needs to be the best, most robust and reliable file
	 * reading function ever.
	 */
	public static String readFile(File file)
	{
		String source = "";
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			
			String currentLine;
			
			while ((currentLine = br.readLine()) != null)
			{
				if (!source.isEmpty())
					source += "\n";
				
				source += currentLine;
			}
			
			br.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return source;
	}

	
	public static boolean saveFile(File file, String content)
	{
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
			
			bw.write(content);
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
}
