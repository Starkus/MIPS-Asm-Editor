package net.starkus.mipseditor.savedata;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.starkus.mipseditor.MainApp;

public class RecentFileHistory {
	
	private static final LinkedList<File> recents = new LinkedList<>();
	static int maxEntries = 10;
	
	private static void onChange() {
		MainApp.getMainController().makeFileMenu();
		JSONUtils.Save();
	}
	
	public static void addFirst(File file)
	{
		while (recents.contains(file))
		{
			recents.remove(file);
		}
		
		recents.addFirst(file);
		
		if (recents.size() > maxEntries)
			recents.removeLast();
		
		onChange();
	}
	
	public static void addLast(File file)
	{
		while (recents.contains(file))
		{
			recents.remove(file);
		}
		
		recents.addLast(file);
		
		if (recents.size() > maxEntries)
			recents.removeFirst();
		
		onChange();
	}
	
	public static void clear()
	{
		recents.clear();
		
		onChange();
	}
	
	public static void addAll(Collection<File> files)
	{
		recents.addAll(files);
		
		onChange();
	}
	
	public static List<File> getRecentFiles() {
		return Collections.unmodifiableList(recents);
	}
}
