package net.starkus.mipseditor.assistant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import net.starkus.mipseditor.MainApp;
import net.starkus.mipseditor.assistant.keyword.Keyword;
import net.starkus.mipseditor.assistant.keyword.KeywordBank;
import net.starkus.mipseditor.assistant.keyword.KeywordDirective;
import net.starkus.mipseditor.assistant.keyword.KeywordLabel;
import net.starkus.mipseditor.assistant.keyword.KeywordOpcode;
import net.starkus.mipseditor.assistant.keyword.KeywordRegisterName;


public class SyntaxHighlights {
	
	private static ExecutorService highlightingExecutor;

	private static String DIRECTIVE_PATTERN;
	private static String OPCODE_PATTERN;
	private static String REGISTERNAME_PATTERN;
    private static String LABELCALL_PATTERN;
    
    //private static String selectedWord;
	
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String LITERAL_PATTERN = "0[xX][0-9a-fA-F]+";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String DEFINE_PATTERN = "\\[[^\n]*\\]:";
    private static final String DEFINECALL_PATTERN = "@(\\w*)\\b";
    private static final String LABEL_PATTERN = "[^\n]+:";
	
	private static Pattern PATTERN;
	
	
	public static void initialize()
	{
		try
		{
			Assistant.loadKeywords(MainApp.getResourceAsString("syntax.json"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		buildPatterns();
		
		highlightingExecutor = Executors.newSingleThreadExecutor();
	}
	
	public static void stop()
	{
		highlightingExecutor.shutdown();
	}
	
	
	public static void buildPatterns()
	{
		KeywordBank keywordBank = Assistant.getKeywordBank();

		DIRECTIVE_PATTERN = "(";
		REGISTERNAME_PATTERN = "\\b(";
		LABELCALL_PATTERN = "";
		/* Put opcode with '.'s first, then the rest */
		String op1 = "(?i)\\b(";
		String op2 = "";
		
		/* Iterate through all keywords and put them on the according strings. */
		for (Keyword k : keywordBank.getKeywords().values())
		{
			if (k.getClass().equals(KeywordOpcode.class))
			{
				if (k.getKeyword().contains("."))
					op1 += k.getKeyword().replaceAll("\\.", "\\\\.") + "|";
				else
					op2 += k.getKeyword() + "|";
			}
			else if (k.getClass().equals(KeywordDirective.class))
			{
				DIRECTIVE_PATTERN += "\\" + k.getKeyword() + "|";
			}
			else if (k.getClass().equals(KeywordRegisterName.class))
			{
				REGISTERNAME_PATTERN += k.getKeyword() + "|";
			}
			else if (k.getClass().equals(KeywordLabel.class))
			{
				LABELCALL_PATTERN += k.getKeyword() + "|";
			}
		}
		
		OPCODE_PATTERN = op1 + op2.substring(0, op2.length()-1) + ")\\b";
		
		DIRECTIVE_PATTERN = DIRECTIVE_PATTERN.substring(0, 
				DIRECTIVE_PATTERN.length()-1) + ")\\b";
		
		REGISTERNAME_PATTERN = REGISTERNAME_PATTERN.substring(0, 
				REGISTERNAME_PATTERN.length()-1) + ")\\b";
		
		if (!LABELCALL_PATTERN.isEmpty())
		LABELCALL_PATTERN = LABELCALL_PATTERN.substring(0, 
				LABELCALL_PATTERN.length()-1);
		
		PATTERN = Pattern.compile("(?<OPCODE>" + OPCODE_PATTERN + ")"
						+ "|(?<DIRECTIVE>" + DIRECTIVE_PATTERN + ")"
						+ "|(?<REGISTERNAME>" + REGISTERNAME_PATTERN + ")"
						+ "|(?<COMMENT>" + COMMENT_PATTERN + ")"
						+ "|(?<STRING>" + STRING_PATTERN + ")"
						+ "|(?<LITERAL>" + LITERAL_PATTERN + ")"
						+ "|(?<DEFINE>" + DEFINE_PATTERN + ")"
						+ "|(?<DEFINECALL>" + DEFINECALL_PATTERN + ")"
						+ "|(?<LABEL>" + LABEL_PATTERN + ")"
						+ "|(?<LABELCALL>" + LABELCALL_PATTERN + ")"
				);
	}
	
	
	public static StyleSpans<Collection<String>> computeHighlighting(String code, int caret)
	{
		return computeHighlighting(0, -1, code, caret);
	}
	
	public static StyleSpans<Collection<String>> computeHighlighting(int start, int end, String code, int caret)
	{
		code = code.substring(start);
		caret -= start;
		
		Matcher matcher = PATTERN.matcher(code);
		int lastKeywordEnd = 0;
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

		if (end != -1)
		{
			matcher.region(0, end - start);
		}
		
		while (matcher.find())
		{
			String styleClass = matcher.group("OPCODE") != null ? "opcode" :
					matcher.group("DIRECTIVE") != null ? "directive" :
					matcher.group("REGISTERNAME") != null ? "register-name" :
					matcher.group("COMMENT") != null ? "comment" :
					matcher.group("STRING") != null ? "string" :
					matcher.group("LITERAL") != null ? "literal" :
					matcher.group("DEFINE") != null ? "define" :
					matcher.group("DEFINECALL") != null ? "define-call" :
					matcher.group("LABEL") != null ? "label-s" :
					matcher.group("LABELCALL") != null ? "label-s" :
					null;
			

			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKeywordEnd);
			
			
			if (matcher.start() < caret && caret <= matcher.end())
			{
				List<String> styleClasses = new ArrayList<>();
				styleClasses.add(styleClass);
				styleClasses.add("backlight");
				spansBuilder.add(styleClasses, caret - matcher.start());
				
				spansBuilder.add(Collections.singleton(styleClass), matcher.end() - caret);
			}
			else
			{
				int length = matcher.end() - matcher.start();
				spansBuilder.add(Collections.singleton(styleClass), length);
			}
			
			lastKeywordEnd = matcher.end();
		}
		
		// rest is empty
		if (lastKeywordEnd < end - start)
			spansBuilder.add(Collections.emptyList(), end - start - lastKeywordEnd);
		
		return spansBuilder.create();
	}
	
	
	public static Pattern getPattern() {
		return PATTERN;
	}
	
	public static ExecutorService getHighlightingExecutor() {
		return highlightingExecutor;
	}
}
