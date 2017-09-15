package net.starkus.mipseditor.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StringUtils {

	
	private static final String EXPR = "\\s|\\(|\\)|,";
	
	
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
		
		startIndex = startOfWord(str, index);
		
		endIndex = endOfWord(str, index);
		
		if (endIndex - startIndex < 0)
			return null;
		
		return str.substring(startIndex, endIndex);
	}
	
	public static String getWordBeingWritten(String str, int caretPosition)
	{
		int startIndex = 0;
		int endIndex = caretPosition;
		
		if (caretPosition == 0)
			return null;
		
		for (int i = caretPosition - 1; i > 0; i--)
		{
			String c = str.substring(i, i+1);
			
			if (c.matches(EXPR))
			{
				startIndex = i + 1;
				break;
			}
		}
		
		if (endIndex - startIndex < 0)
			return null;
		
		return str.substring(startIndex, endIndex);
	}
	
	
	public static int startOfWord(String str, int index)
	{
		if (index > str.length())
			return -1;
		
		int startIndex = 0;
		
		for (int i = index - 1; i > 0; i--)
		{
			String c = str.substring(i, i+1);
			
			if (c.matches(EXPR))
			{
				startIndex = i + 1;
				break;
			}
		}
		
		return startIndex;
	}
	
	public static int endOfWord(String str, int index)
	{
		int endIndex = 0;
		
		for (int i = index; i < str.length() - 1; i++)
		{
			String c = str.substring(i, i+1);
			
			if (c.matches(EXPR))
			{
				endIndex = i;
				break;
			}
		}
		
		return endIndex;
	}
	
	
	public static interface StringExtractor<T>
	{
		public abstract String extract(T thing);
	}
	
	public static class StringListBuilder<T>
	{
		public List<String> build(StringExtractor<T> extractor, Collection<T> items)
		{
			List<String> result = new ArrayList<>();
			
			for (T t : items)
			{
				result.add(extractor.extract(t));
			}
			
			return result;
		}
	}
}
