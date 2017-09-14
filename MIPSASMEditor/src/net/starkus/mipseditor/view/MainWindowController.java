package net.starkus.mipseditor.view;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.Stream;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import net.starkus.mipseditor.MainApp;
import net.starkus.mipseditor.control.FileTab;
import net.starkus.mipseditor.model.FileManager;
import net.starkus.mipseditor.savedata.RecentFileHistory;;


public class MainWindowController {

	@FXML
	private TabPane tabPane;

	@FXML
	private Menu fileMenu;
	
	@FXML
	private MenuItem newCmd;
	@FXML
	private MenuItem openCmd;
	@FXML
	private MenuItem saveCmd;
	@FXML
	private MenuItem saveAsCmd;
	@FXML
	private MenuItem saveCopyCmd;
	@FXML
	private MenuItem saveAllCmd;
	@FXML
	private MenuItem closeCmd;
	
	private MenuItem exitCmd;
	

	@FXML
	private MenuItem undoCmd;
	@FXML
	private MenuItem redoCmd;
	
	
	private MenuItem[] initialFileMenu;	
	
	
	@FXML
	void initialize()
	{
		/* Menu bar actions */
		newCmd.setOnAction(event -> handleNewFile());
		newCmd.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		
		openCmd.setOnAction(event -> handleOpenFile(FileManager.browseFile(MainApp.getWindow(), false, null)));
		openCmd.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		
		saveCmd.setOnAction(event -> handleSaveFile());
		saveCmd.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		
		saveAsCmd.setOnAction(event -> handleSaveAs());
		saveAsCmd.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN));
		
		saveCopyCmd.setOnAction(event -> handleSaveCopy());
		
		saveAllCmd.setOnAction(e -> handleSaveAll());
		saveAllCmd.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		
		closeCmd.setOnAction(event -> handleCloseFile());
		closeCmd.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
		
		exitCmd = new MenuItem("Exit");
		exitCmd.setOnAction(event -> handleExit());
		

		undoCmd.setOnAction(e -> getCurrentTab().getCodeArea().undo());
		//undoCmd.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
		
		redoCmd.setOnAction(e -> getCurrentTab().getCodeArea().redo());
		//redoCmd.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
		
		initialFileMenu = fileMenu.getItems().toArray(new MenuItem[0]);
		makeFileMenu();
		
		
		connectTabsWithFileManager();
		
		
		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
				
				FileTab fileTab = (FileTab) newValue;
				
				String title = "";
				
				if (fileTab == null || fileTab.getFile() == null)
					title = MainApp.appName;
					
				else
				{
					fileTab.getCodeArea().requestFocus();
					
					title = fileTab.getFile().getName();
					
					if (fileTab.isDirty())
						title += "*";
					
					title += " - " + MainApp.appName;
				}
				MainApp.getWindow().setTitle(title);
				
				/* Bindings */
				if (fileTab != null)
				{
					saveCmd.disableProperty().bind(Bindings.not(fileTab.dirtyProperty()));
					undoCmd.disableProperty().bind(Bindings.not(fileTab.getCodeArea().undoAvailableProperty()));
					redoCmd.disableProperty().bind(Bindings.not(fileTab.getCodeArea().redoAvailableProperty()));
				}
				else
				{
					saveCmd.disableProperty().unbind();
					saveCmd.setDisable(true);
					undoCmd.disableProperty().unbind();
					undoCmd.setDisable(true);
					redoCmd.disableProperty().unbind();
					redoCmd.setDisable(true);
				}
			}
		});
	}
	
	
	public void makeFileMenu()
	{
		fileMenu.getItems().clear();
		fileMenu.getItems().addAll(initialFileMenu);
		
		fileMenu.getItems().add(new SeparatorMenuItem());
		
		RecentFileHistory.getRecentFiles().forEach(f -> {
			MenuItem menuItem = new MenuItem(f.getAbsolutePath());
			menuItem.setMnemonicParsing(false);
			
			menuItem.setOnAction(e -> {
				handleOpenFile(f);
			});
			
			fileMenu.getItems().add(menuItem);
		});
		
		fileMenu.getItems().add(new SeparatorMenuItem());
		fileMenu.getItems().add(exitCmd);
	}
	
	
	private void connectTabsWithFileManager()
	{
		FileManager.setOnFileOpened(e -> {
			
			FileTab newTab = new FileTab(e.getFile());
			
			tabPane.getTabs().add(newTab);
			tabPane.getSelectionModel().select(newTab);
		});
		
		FileManager.setOnFileClosed(e -> {
			
			if (!tabPane.getTabs().remove(tabFromFile(e.getFile())))
				e.consume();
		});
		
		FileManager.setOnFileSaved(e -> {
			
			FileTab tab = tabFromFile(e.getFile());
			
			tab.setDirty(false);
		});
		
		FileManager.setOnFilePathChanged(e -> {
			
			FileTab tab = tabFromFile(e.getOldFile());
			
			tab.setFile(e.getNewFile());
		});
	}
	
	
	private FileTab tabFromFile(File file)
	{
		return (FileTab) tabPane.getTabs().filtered(t -> 
				((FileTab)t).getFile().equals(file)
				).get(0);
	}
	
	
	public void handleNewFile()
	{
		File recentDirectory = RecentFileHistory.getRecentFiles().get(0);
		
		File file = new File(recentDirectory, "Untitled");
		
		/* Ensure unique name */
		int c = 0;
		while (FileManager.getOpenfiles().keySet().contains(file))
		{
			c++;
			file = new File(recentDirectory, "Untitled_" + c);
		}
		
		FileManager.openFile(file);
	}
	
	public void handleOpenFile(File file)
	{
		if (file != null)
		{
			if (FileManager.getOpenfiles().keySet().contains(file))
			{
				tabPane.getSelectionModel().select(tabFromFile(file));
				return;
			}
			
			FileManager.openFile(file);
		}
	}
	
	public void handleSaveFile()
	{
		File file = getCurrentTab().getFile();
		
		FileManager.saveFile(file);
	}
	
	public void handleSaveAs()
	{
		File file = getCurrentTab().getFile();
		
		FileManager.saveFileAs(file);
	}
	
	public void handleSaveCopy()
	{
		File file = getCurrentTab().getFile();
		
		FileManager.saveFileCopy(file);
	}
	
	public void handleSaveAll()
	{
		for (File file : FileManager.getOpenfiles().keySet())
		{
			FileManager.saveFile(file);
		}
	}
	
	public void handleCloseFile()
	{
		FileManager.closeFile(getCurrentTab().getFile());
	}
	
	public void handleExit()
	{
		System.exit(0);
	}
	
	
	public boolean requestClose()
	{
		for (Tab tab : new ArrayList<>(tabPane.getTabs()))
		{
			if (((FileTab)tab).requestClose())
				tabPane.getTabs().remove(tab);
			else
				return false;
		}
		
		return true;
	}
	
	
	public FileTab getCurrentTab() {
		return (FileTab) tabPane.getSelectionModel().getSelectedItem();
	}
	
	public ReadOnlyObjectProperty<Tab> currentTabProperty() {
		return tabPane.getSelectionModel().selectedItemProperty();
	}
	
	public Stream<Tab> getDirtyTabs() {
		return tabPane.getTabs().stream().filter(t -> ((FileTab)t).isDirty());
	}
}
