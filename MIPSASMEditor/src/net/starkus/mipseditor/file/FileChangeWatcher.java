package net.starkus.mipseditor.file;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

public class FileChangeWatcher implements Runnable {

	private WatchService watchService;
	private Map<String, WatchKey> keys = new HashMap<>();
	private Map<WatchKey, EventHandler<ActionEvent>> handlers = new HashMap<>();
	
	private Thread thread;
	private boolean running = false;
	
	
	public FileChangeWatcher()
	{
		try
		{
			watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void registerFile(String absolutePath)
	{
		String keyName = absolutePath;
		
		Path path = Paths.get(keyName);
		
		if (keys.containsKey(keyName))
		{
			System.err.println("Can't register file " + absolutePath + ", it's already registered");
			return;
		}
		
		try
		{
			WatchKey key = path.register(watchService, ENTRY_MODIFY);
			handlers.put(key, e -> System.out.println("File change detected"));
			
			// Save key for unregistering
			keys.put(absolutePath, key);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public void unregisterFile(String absolutePath)
	{
		if (!keys.containsKey(absolutePath))
		{
			System.err.println("Can't unregister file " + absolutePath + ", it's not registered.");
			return;
		}
		
		// Retrieve key
		WatchKey key = keys.get(absolutePath);
		
		// Get rid of it
		key.cancel();
		keys.remove(absolutePath);
	}
	
	
	public void setOnUpdate(String absolutePath, EventHandler<ActionEvent> handler)
	{
		handlers.replace(keys.get(absolutePath), handler);
	}


	@Override
	public void run()
	{		
		try
		{
			WatchKey key;
			
			while(running)
			{
				key = watchService.take();
				
				for (WatchEvent<?> event : key.pollEvents())
				{
					WatchEvent.Kind<?> kind = event.kind();
					
					if (kind == OVERFLOW)
						continue;
					
					/*@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path filename = ev.context();
					*/
					// Do something
					handlers.get(key).handle(new ActionEvent());

					
					if (!key.reset())
						break;
				}
			}
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	public void start()
	{
		running = true;
		
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop()
	{
		running = false;
	}
}
