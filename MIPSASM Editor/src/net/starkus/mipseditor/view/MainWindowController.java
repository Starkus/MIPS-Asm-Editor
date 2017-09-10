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
		
		openCmd.setOnAction(event -> handleOpenFile());
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
		
		
		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
				
				FileTab fileTab = (FileTab) newValue;
				
				String title = "";
				
				if (fileTab == null || fileTab.getFile() == null)
					title = MainApp.appName;
					
				else
				{
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
				openFile(f);
			});
			
			fileMenu.getItems().add(menuItem);
		});
		
		fileMenu.getItems().add(new SeparatorMenuItem());
		fileMenu.getItems().add(exitCmd);
	}
	
	
	public void openFile(File file)
	{
		/* Check if file is open */
		for (Tab t : tabPane.getTabs())
		{
			FileTab ft = (FileTab) t;
			if (ft.getFile().equals(file))
			{
				tabPane.getSelectionModel().select(t);
				return;
			}
		}
		
		FileTab newTab = new FileTab(file);
		
		tabPane.getTabs().add(newTab);
		tabPane.getSelectionModel().select(newTab);
	}
	
	
	public void handleNewFile()
	{
		FileTab newTab = new FileTab(null);
		
		tabPane.getTabs().add(newTab);
		tabPane.getSelectionModel().select(newTab);
	}
	
	public void handleOpenFile()
	{
		File file = FileManager.BrowseFile(MainApp.getWindow(), false, null);
		
		if (file != null)
		{
			/* Check if the choosen file is already open. */
			for (Tab tab : tabPane.getTabs())
			{
				FileTab ft = (FileTab) tab;
				if (ft.getFile().equals(file))
				{
					tabPane.getSelectionModel().select(tab);
					return;
				}
			}
			
			openFile(file);
		}
	}
	
	public void handleSaveFile()
	{
		FileTab currentTab = getCurrentTab();
		
		File file = currentTab.getFile();
		
		if (file == null)
		{
			handleSaveAs();
			return;
		}

		currentTab.saveFile();
	}
	
	public void handleSaveAs()
	{
		FileTab currentTab = getCurrentTab();
		
		String initialName = currentTab.getFile() == null ? 
				RecentFileHistory.getRecentFiles().get(0).getAbsolutePath() : 
					currentTab.getFile().getName();
				
		File file = FileManager.BrowseFile(MainApp.getWindow(), true, initialName);
		
		if (file != null)
		{
			currentTab.setFile(file);
			currentTab.saveFile();
		}
	}
	
	public void handleSaveCopy()
	{
		File file = FileManager.BrowseFile(MainApp.getWindow(), true, getCurrentTab().getFile().getName());
		
		String text = ((FileTab) tabPane.getSelectionModel().getSelectedItem())
				.getCodeArea().getText();
		
		if (file != null)
		{
			FileManager.WriteFile(file, text);
		}
	}
	
	public void handleSaveAll()
	{
		for (Tab tab : tabPane.getTabs())
		{
			FileTab fileTab = (FileTab) tab;
			
			File file = fileTab.getFile();
			if (file == null)
			{
				tabPane.getSelectionModel().select(tab);
				handleSaveAs();
			}
			else
			{
				fileTab.saveFile();
			}
		}
	}
	
	public void handleCloseFile()
	{
		tabPane.getTabs().remove(getCurrentTab());
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
