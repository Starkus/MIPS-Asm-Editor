package net.starkus.mipseditor.syntax;

import java.util.regex.Matcher;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import net.starkus.mipseditor.syntax.Syntax.Keyword;

public class Tooltips {
	
	public static boolean makeTooltipFromCodeIndex(TextFlow flow, String code, int index)
	{
		String word = StringUtils.getWordFromIndex(code, index);
		
		
		if (word == null)
			return false;
		
		Matcher matcher = Syntax.getPattern().matcher(word);
		Text title = new Text();
		Text desc = new Text();

		title.getStyleClass().add("tooltip-title");
		desc.getStyleClass().add("tooltip-content");
		
		if (matcher.find() && (matcher.group("OPCODE") != null || 
						matcher.group("ASSEMBLERINSTRUCTION") != null ||
						matcher.group("REGISTERNAME") != null))
		{
			Keyword k = Syntax.getKeywordBank().getKeyword(word);
			
			title.setText(k.getTooltipTitle());
			desc.setText(k.getTooltipBody());
			
			title.setText(title.getText() + "\n");
			flow.getChildren().setAll(title, desc);
			
			return true;
		}
		
		return false;
	}

}
