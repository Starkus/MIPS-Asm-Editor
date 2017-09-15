package net.starkus.mipseditor.assistant;

import java.util.HashMap;
import java.util.regex.Matcher;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.starkus.mipseditor.assistant.keyword.Keyword;
import net.starkus.mipseditor.util.StringUtils;

public class Tooltips {
	
	public static boolean makeTooltipFromCodeIndex(TextFlow flow, String code, int index)
	{
		Matcher matcher = Syntax.getPattern().matcher(code);
		Text title = new Text();
		Text desc = new Text();

		title.getStyleClass().add("tooltip-title");
		desc.getStyleClass().add("tooltip-content");
		
		if (matcher.find(StringUtils.startOfWord(code, index)) && (matcher.group("OPCODE") != null || 
						matcher.group("DIRECTIVE") != null ||
						matcher.group("REGISTERNAME") != null) || 
						matcher.group("DEFINECALL") != null)
		{
			
			String word = code.substring(matcher.start(), matcher.end());
			if (word.startsWith("@"))
				word = word.substring(1);
			
			HashMap<String, Keyword> keywords = Assistant.getKeywordBank().getKeywords(); 
			Keyword k = keywords.get(word);
			
			if (k == null)
				return false;
			
			title.setText(k.getTooltipTitle());
			desc.setText(k.getTooltipBody());
			
			title.setText(title.getText() + "\n");
			flow.getChildren().setAll(title, desc);
			
			return true;
		}
		
		return false;
	}

}
