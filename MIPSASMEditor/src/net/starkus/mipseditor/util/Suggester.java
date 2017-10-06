package net.starkus.mipseditor.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class Suggester<T> {
	
	List<T> stuff;
	Function<T, String> stringExtractor;

	public Suggester(List<T> stuff)
	{
		this.stuff = stuff;
	}
	
	public void setStringExtractor(Function <T, String> extractor)
	{
		this.stringExtractor = extractor;
	}
	
	public List<T> getSortedResults(String filter)
	{
		Map<T, Float> weightedResults = new HashMap<>();
		
		filter = filter.toUpperCase();
		
		for (T t : stuff)
		{
			float weight = 0;
			String thisk = stringExtractor.apply(t).toUpperCase();
			
			if (thisk.equals(filter))
				weight += 50.0;

			else
			{
				for (int i=0; i < filter.length(); ++i)
				{
					if (thisk.length() == i+1)
					{
						weight += 10.0;
						break;
					}
					
					if (thisk.charAt(i) != filter.charAt(i))
						break;
					
					weight += 2.0;
				}
				
				weight -= thisk.length() * 0.1;
			}
				
			if (!thisk.contains(filter))
				weight -= 100.0;
				
			
			if (weight > 0)
				weightedResults.put(t, weight);
		}
		
		List<Map.Entry<T, Float>> sorted = new LinkedList<>(weightedResults.entrySet());
		Collections.sort(sorted, new Comparator<Map.Entry<T, Float>>() {

			@Override
			public int compare(Entry<T, Float> o1, Entry<T, Float> o2)
			{
				return Float.compare(o2.getValue(), o1.getValue());
			}
		});
		
		List<T> result = new ArrayList<>();
		for (Map.Entry<T, Float> entry : sorted)
			result.add(entry.getKey());
		
		return result;
	}
}
