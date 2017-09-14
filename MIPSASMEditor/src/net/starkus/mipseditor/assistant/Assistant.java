package net.starkus.mipseditor.assistant;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import net.starkus.mipseditor.control.MyCodeArea;
import net.starkus.mipseditor.control.SuggestionContextMenu;
import net.starkus.mipseditor.syntax.StringUtils;

public class Assistant {
	
	private static final SuggestionContextMenu suggestionMenu = new SuggestionContextMenu();
	private static final CodeProcessor codeProcessor = new CodeProcessor();
	
	private final MyCodeArea codeArea;
	
	
	public Assistant(MyCodeArea codeArea)
	{
		this.codeArea = codeArea;
		
		codeArea.focusedProperty().addListener((obs, oldv, newv) -> {
			if (!newv)
				suggestionMenu.hide();
		});
		
		suggestionMenu.setOnAction(e -> {
			String selected = suggestionMenu.getSelectionModel().getSelectedItem();
			
			codeArea.selectWord();
			codeArea.replaceSelection(selected);
		});
	}
	
	
	public void processKeyPress(KeyEvent e)
	{
		KeyCode k = e.getCode();
		
		if (!(k.isDigitKey() || k.isLetterKey() || k.isKeypadKey() || k == KeyCode.BACK_SPACE))
			return;
		
		//suggestionMenu.hide();
		
		String code = codeArea.getText();
		String word = StringUtils.getWordBeingWritten(code, codeArea.getCaretPosition());
		
		computeSuggestions(word);
		
		Bounds caretBounds = codeArea.getCaretBounds().get();
		Point2D pos = new Point2D(caretBounds.getMinX(), caretBounds.getMaxY());
		
		suggestionMenu.hide();
		suggestionMenu.show(codeArea, pos);
	}

	// Branch depending on the type of suggestion to do
	private void computeSuggestions(String writing)
	{
		suggestionMenu.getEntries().clear();
		
		if (writing == null)
			return;
		
		if (writing.startsWith("@"))
		{
			variableSuggestions(writing);
			return;
		}
	}
	
	private void variableSuggestions(String writing)
	{
		// Get rid of the @ for comparing
		final String filter = writing.substring(1);
		
		codeProcessor.getDefines().keySet().forEach(name -> {
			if (name.startsWith(filter))
				suggestionMenu.getEntries().add(name);
		});
	}
	
	
	public static CodeProcessor getCodeProcessor() {
		return codeProcessor;
	}
}
