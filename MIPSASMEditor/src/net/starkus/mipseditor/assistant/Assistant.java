package net.starkus.mipseditor.assistant;

import java.util.Optional;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import net.starkus.mipseditor.assistant.keyword.Keyword;
import net.starkus.mipseditor.assistant.keyword.KeywordBank;
import net.starkus.mipseditor.assistant.keyword.KeywordDefine;
import net.starkus.mipseditor.assistant.keyword.KeywordOpcode;
import net.starkus.mipseditor.assistant.keyword.KeywordRegisterName;
import net.starkus.mipseditor.control.MyCodeArea;
import net.starkus.mipseditor.control.SuggestionContextMenu;
import net.starkus.mipseditor.util.StringUtils;
import net.starkus.mipseditor.util.Suggester;

public class Assistant {
	
	private static final SuggestionContextMenu suggestionMenu = new SuggestionContextMenu();
	private static final CodeProcessor codeProcessor = new CodeProcessor();
	private static final KeywordBank keywordBank = new KeywordBank();
	
	private static final ObservableMap<String, SuggestionEntry> suggestionEntries = FXCollections.observableHashMap();
	
	private final MyCodeArea codeArea;
	
	
	public Assistant(MyCodeArea codeArea)
	{
		this.codeArea = codeArea;
		
		codeArea.focusedProperty().addListener((obs, oldv, newv) -> {
			if (!newv)
				suggestionMenu.hide();
		});
		
		suggestionMenu.setOnAction(e -> {
			if (!suggestionMenu.isShowing())
				return;
			
			Keyword selected = suggestionMenu.getSelectionModel().getSelectedItem();
			
			int sow = StringUtils.startOfWord(codeArea.getText(), codeArea.getCaretPosition());
			if (codeArea.getText().charAt(sow) == '@') sow++; // FIXME
			codeArea.selectRange(sow, codeArea.getCaretPosition());
			
			//codeArea.selectWord();
			codeArea.replaceSelection(selected.getKeyword());
		});
	}
	
	
	public static void loadKeywords(String res)
	{
		keywordBank.buildFromString(res);
	}
	
	
	public void processKeyPress(KeyEvent e)
	{
		KeyCode k = e.getCode();
		
		if (!(k.isDigitKey() || k.isLetterKey() || k.isKeypadKey() || k == KeyCode.BACK_SPACE || k == KeyCode.SPACE))
			return;
		
		Platform.runLater(() -> {
			String code = codeArea.getText();
			String word = StringUtils.getWordBeingWritten(code, codeArea.getCaretPosition());
			
			if (k.equals(KeyCode.SPACE))
			{
				word = StringUtils.getWordBeingWritten(code, codeArea.getCaretPosition()-1);
				
				suggestionMenu.getEntries().clear();
				suggestionMenu.hide();
				return;
			}
			
			computeSuggestions(word);
			
			Optional<Bounds> caretBounds = codeArea.getCaretBounds();
			
			if (caretBounds.isPresent())
			{
				Point2D pos = new Point2D(caretBounds.get().getMinX(), caretBounds.get().getMaxY());
			
				suggestionMenu.hide();
				
				if (!suggestionMenu.getEntries().isEmpty())
					suggestionMenu.show(codeArea, pos);
			}
		});
	}
	
	
	public void processSourceChange()
	{
		
	}
	
	
	public void processCurrentFile()
	{
		codeProcessor.process();
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
		}
		
		else if (writing.matches("[A-Za-z]+"))
		{
			opcodeSuggestions(writing);
			registerSuggestions(writing);
		}
	}
	
	private void variableSuggestions(String writing)
	{
		if (writing.isEmpty())
			return;
		
		// Get rid of the @ for comparing
		final String filter = writing.substring(1).toLowerCase();
		
		for (Keyword k : keywordBank.getKeywordsByType(KeywordDefine.class))
		{
			if (k.getKeyword().toLowerCase().startsWith(filter))
				suggestionMenu.getEntries().add(k);
		}
	}
	
	private void opcodeSuggestions(String writing)
	{
		Suggester<Keyword> suggester = new Suggester<>(keywordBank.getKeywordsByType(KeywordOpcode.class));
		suggester.setStringExtractor(k -> k.getKeyword());
		
		suggestionMenu.getEntries().addAll(suggester.getSortedResults(writing));
	}
	
	private void registerSuggestions(String writing)
	{
		Suggester<Keyword> suggester = new Suggester<>(keywordBank.getKeywordsByType(KeywordRegisterName.class));
		suggester.setStringExtractor(k -> k.getKeyword());
		
		suggestionMenu.getEntries().addAll(suggester.getSortedResults(writing));
	}
	
	
	public static CodeProcessor getCodeProcessor() {
		return codeProcessor;
	}
	
	public static ObservableMap<String, SuggestionEntry> getSuggestionentries() {
		return suggestionEntries;
	}
	
	public static KeywordBank getKeywordBank() {
		return keywordBank;
	}
	
	
	public static class SuggestionEntry
	{
		public final String name;
		public final String description;
		
		public SuggestionEntry(String name, String description)
		{
			this.name = name;
			this.description = description;
		}
	}
	
	public static enum SuggestionType
	{
		DEFINE, OPCODE, REGISTER
	}
}
