package net.starkus.mipseditor.syntax;

public class StringUtils {

	
	public static String getLineFromIndex(String str, int index)
	{
		int startIndex = 0;
		int endIndex = str.length() - 1;
		
		for (int i = index; i > 0; i--)
		{
			char c = str.charAt(i);
			
			if (c == '\n')
			{
				startIndex = i + 1;
				break;
			}
		}
		
		for (int i = index; i < str.length() - 1; i++)
		{
			char c = str.charAt(i);
			
			if (c == '\n')
			{
				endIndex = i;
				break;
			}
		}
		
		return str.substring(startIndex, endIndex);
	}
	
	public static String getWordFromIndex(String str, int index)
	{
		int startIndex = 0;
		int endIndex = str.length() - 1;
		
		String expr = "\\s|\\(|\\)|,";
		
		for (int i = index; i > 0; i--)
		{
			String c = str.substring(i, i+1);
			
			if (c.matches(expr))
			{
				startIndex = i + 1;
				break;
			}
		}
		
		for (int i = index; i < str.length() - 1; i++)
		{
			String c = str.substring(i, i+1);
			
			if (c.matches(expr))
			{
				endIndex = i;
				break;
			}
		}
		
		if (endIndex - startIndex <= 0)
			return null;
		
		return str.substring(startIndex, endIndex);
	}
}
