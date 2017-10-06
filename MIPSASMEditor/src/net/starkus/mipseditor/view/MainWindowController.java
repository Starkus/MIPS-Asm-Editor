package net.starkus.mipseditor.view;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import net.starkus.mipseditor.MainApp;
import net.starkus.mipseditor.control.AlertWrapper;
import net.starkus.mipseditor.control.FileTab;
import net.starkus.mipseditor.control.MyCodeArea;
import net.starkus.mipseditor.file.FileManager;
import net.starkus.mipseditor.file.LoadedFile;
import net.starkus.mipseditor.file.LoadedFileOpen;
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
	

	@FXML
	private RadioMenuItem lineNothingCmd;
	@FXML
	private RadioMenuItem lineNumbersCmd;
	@FXML
	private RadioMenuItem lineAddressesCmd;
	@FXML
	private ToggleGroup lineDecorators;
	

	@FXML
	private Label filePathLabel;
	@FXML
	private Label fileInfoLabel;
	@FXML
	private Label caretInfoLabel;
	
	
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
		
		
		makeTabSelectionListeners();
		
		/* Status bar info */
		makeStatusBarBindings();
	}


	private void makeTabSelectionListeners()
	{
		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldv, newv) -> 
		{
			FileTab fileTab = (FileTab) newv;
			
			String title = "";
			
			if (fileTab == null)
				title = MainApp.appName;
				
			else
			{
				fileTab.getCodeArea().requestFocus();
				
				fileTab.textProperty().addListener((obs1, oldv1, newv1) ->
				{			
					MainApp.getWindow().setTitle(newv.getText() + " - " + MainApp.appName);					
				});
				
				// Immediate update
				title = fileTab.getText() + " - " + MainApp.appName;
			}
			MainApp.getWindow().setTitle(title);
			
			
			if (lineNumbersCmd.isSelected())
				fileTab.getCodeArea().showNumberLines();
			else if (lineAddressesCmd.isSelected())
				fileTab.getCodeArea().showAddresses();
			else
				fileTab.getCodeArea().showNoLineDecorations();
			
			
			lineDecorators.selectedToggleProperty().addListener((obs1, oldv1, newv1) -> {
				if (newv1 == null || newv1.equals(lineNothingCmd))
					fileTab.getCodeArea().showNoLineDecorations();
				
				else if (newv1.equals(lineNumbersCmd))
					fileTab.getCodeArea().showNumberLines();
				
				else if (newv1.equals(lineAddressesCmd))
					fileTab.getCodeArea().showAddresses();
			});
			
			
			LoadedFileOpen loadedFile = getCurrentFile();
			
			/* Bindings */
			
			// Reset
			saveCmd.disableProperty().unbind();
			saveCmd.setDisable(true);
			undoCmd.disableProperty().unbind();
			undoCmd.setDisable(true);
			redoCmd.disableProperty().unbind();
			redoCmd.setDisable(true);
			
			if (loadedFile != null)
			{
				saveCmd.disableProperty().bind(Bindings.not(loadedFile.dirtyProperty()));
			}
			if (fileTab != null)
			{
				undoCmd.disableProperty().bind(Bindings.not(fileTab.getCodeArea().undoAvailableProperty()));
				redoCmd.disableProperty().bind(Bindings.not(fileTab.getCodeArea().redoAvailableProperty()));
			}
		});
	}


	private void makeStatusBarBindings()
	{
		currentTabProperty().addListener((obs, oldv, newv) -> {
			
			if (newv == null)
			{
				filePathLabel.setText("");
				
				fileInfoLabel.textProperty().unbind();
				fileInfoLabel.setText("");
				
				caretInfoLabel.textProperty().unbind();
				caretInfoLabel.setText("");
				
				return;
			}
			
			FileTab fileTab = (FileTab) newv;
			LoadedFileOpen loadedFile = getCurrentFile();
			
			filePathLabel.setText(loadedFile == null ? "" : loadedFile.getFile().getAbsolutePath());
			
			
			/* File info */
			fileInfoLabel.textProperty().bind(Bindings.createObjectBinding(() -> {
				
				String code = fileTab.getCodeArea().getText();
				int lines = code.split("\n", -1).length;
				
				return "Lenght: " + code.length() + " - Lines: " + lines;
				
			}, fileTab.getCodeArea().textProperty()));
			
			
			/* Caret */
			caretInfoLabel.textProperty().bind(Bindings.createObjectBinding(() -> {
				
				MyCodeArea codeArea = fileTab.getCodeArea();
				
				int pos = codeArea.getCaretPosition();
				int line = codeArea.getText().substring(0, pos).split("\n", -1).length;
				
				int column = codeArea.getCaretColumn();
				
				return "Pos: " + pos + " - Line: " + line + " - Col: " + column;
				
			}, fileTab.getCodeArea().caretPositionProperty()));
		});
	}
	
	
	/*
	 * Make a file menu with all the recent files entries.
	 */
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
	
	
	public void selectTab(FileTab tab)
	{
		tabPane.getSelectionModel().select(tab);
	}
	
	
	/*private FileTab tabFromFile(File file)
	{
		FilteredList<Tab> tabs = tabPane.getTabs().filtered(t -> 
				((FileTab)t).getFile().equals(file)
				);
		
		if (tabs.isEmpty())
			return null;
		
		return (FileTab) tabs.get(0);
	}*/
	
	
	public void handleNewFile()
	{
		FileManager.newFile();
	}
	
	public void handleOpenFile(File file)
	{
		if (file != null)
		{
			if (!file.exists() || file.isDirectory())
			{
				AlertWrapper alert = new AlertWrapper(AlertType.ERROR)
						.setTitle("File not found!")
						.setHeaderText("The file " + file.getName() + " doesn't exist!")
						.setContentText(file.getAbsolutePath());
				
				alert.showAndWait();
				return;
			}
			
			
			
			FileManager.openFile(file);
		}
	}
	
	public void handleSaveFile()
	{
		LoadedFileOpen loadedFile = getCurrentTab().getOwner();
		FileManager.saveFile(loadedFile);
	}
	
	public void handleSaveAs()
	{
		LoadedFileOpen loadedFile = getCurrentTab().getOwner();
		FileManager.saveFileAs(loadedFile);
	}
	
	public void handleSaveCopy()
	{
		LoadedFileOpen loadedFile = getCurrentTab().getOwner();
		FileManager.saveFileCopy(loadedFile);
	}
	
	public void handleSaveAll()
	{
		for (Tab tab : tabPane.getTabs())
		{
			LoadedFileOpen loadedFile = ((FileTab) tab).getOwner();
			FileManager.saveFile(loadedFile);
		}
	}
	
	public void handleCloseFile()
	{
		LoadedFileOpen loadedFile = getCurrentTab().getOwner();
		FileManager.closeFile(loadedFile);
	}
	
	public void handleExit()
	{
		System.exit(0);
	}
	
	
	public boolean requestClose()
	{
		List<LoadedFile> loadedFiles = new ArrayList<>(FileManager.getLoadedfiles());
		
		for (LoadedFile loadedFile : loadedFiles)
		{
			if (loadedFile.getClass().equals(LoadedFileOpen.class) == false)
				continue;
			
			boolean success = FileManager.closeFile(loadedFile);
			
			if (!success)
				return false;
		}
		
		return true;
	}
	
	
	public boolean requestAddTab(FileTab tab)
	{
		return tabPane.getTabs().add(tab);
	}
	public boolean requestCloseTab(FileTab tab)
	{
		return tabPane.getTabs().remove(tab);
	}
	
	
	public FileTab getCurrentTab() {
		return (FileTab) tabPane.getSelectionModel().getSelectedItem();
	}
	
	public LoadedFileOpen getCurrentFile()
	{
		if (getCurrentTab() == null)
			return null;
		
		return getCurrentTab().getOwner();
	}
	
	public ReadOnlyObjectProperty<Tab> currentTabProperty() {
		return tabPane.getSelectionModel().selectedItemProperty();
	}
	
	/*public Stream<Tab> getDirtyTabs() {
		return tabPane.getTabs().stream().filter(t -> ((FileTab)t).isDirty());
	}*/
}
