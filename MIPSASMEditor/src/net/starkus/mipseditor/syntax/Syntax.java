package net.starkus.mipseditor.syntax;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import net.starkus.mipseditor.MainApp;


public class Syntax {
	
	private static final KeywordBank keywordBank = new KeywordBank();
	
	private static ExecutorService highlightingExecutor;

	private static String ASSEMBLERINSTRUCTION_PATTERN;
	private static String OPCODE_PATTERN;
	private static String REGISTERNAME_PATTERN;
	
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String LITERAL_PATTERN = "0[xX][0-9a-fA-F]+";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String DEFINE_PATTERN = "\\[[^\n]*\\]:";
    private static final String LABEL_PATTERN = "[^\n]+:";
	
	private static Pattern PATTERN;
	
	
	public static void initialize()
	{
		File syntaxFile = new File(MainApp.class.getResource("syntax.json").getFile());
		
		try {
			keywordBank.buildFromFile(syntaxFile);
			buildPatterns();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		highlightingExecutor = Executors.newSingleThreadExecutor();
	}
	
	public static void stop()
	{
		highlightingExecutor.shutdown();
	}
	
	
	private static void buildPatterns()
	{
		ASSEMBLERINSTRUCTION_PATTERN = "(\\" + String.join("|\\", keywordBank.getAssemblerInstructions().keySet()) + ")\\b";
		OPCODE_PATTERN = "\\b(" + String.join("|", keywordBank.getOpcodes().keySet()) + ")\\b";
		REGISTERNAME_PATTERN = "\\b(" + String.join("|", keywordBank.getRegisters().keySet()) + ")\\b";
		
		PATTERN = Pattern.compile("(?<OPCODE>" + OPCODE_PATTERN + ")"
						+ "|(?<ASSEMBLERINSTRUCTION>" + ASSEMBLERINSTRUCTION_PATTERN + ")"
						+ "|(?<REGISTERNAME>" + REGISTERNAME_PATTERN + ")"
						+ "|(?<COMMENT>" + COMMENT_PATTERN + ")"
						+ "|(?<STRING>" + STRING_PATTERN + ")"
						+ "|(?<LITERAL>" + LITERAL_PATTERN + ")"
						+ "|(?<DEFINE>" + DEFINE_PATTERN + ")"
						+ "|(?<LABEL>" + LABEL_PATTERN + ")"
				);
	}
	
	
	public static StyleSpans<Collection<String>> computeHighlighting(String code)
	{
		Matcher matcher = PATTERN.matcher(code);
		int lastKwEnd = 0; // ?
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		
		while (matcher.find())
		{
			String styleClass = matcher.group("OPCODE") != null ? "opcode" :
					matcher.group("ASSEMBLERINSTRUCTION") != null ? "asm-instruction" :
					matcher.group("REGISTERNAME") != null ? "register-name" :
					matcher.group("COMMENT") != null ? "comment" :
					matcher.group("STRING") != null ? "string" :
					matcher.group("LITERAL") != null ? "literal" :
					matcher.group("DEFINE") != null ? "define" :
					matcher.group("LABEL") != null ? "label-s" :
					null;
			
			spansBuilder.add(Collections.singleton("normal-code"), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		
		spansBuilder.add(Collections.singleton("normal-code"), code.length() - lastKwEnd);
		return spansBuilder.create();
	}
	
	
	public static KeywordBank getKeywordBank() {
		return keywordBank;
	}
	
	public static Pattern getPattern() {
		return PATTERN;
	}
	
	public static ExecutorService getHighlightingExecutor() {
		return highlightingExecutor;
	}
	
	
	public static enum KeywordType
	{
		OPCODE, REGISTER;
	}
	
	public static class Keyword
	{
		protected KeywordType type;
		protected String keyword;
		protected String name;
		protected String description;
		
		public Keyword(KeywordType type, String keyword, String name, String description)
		{
			this.type = type;
			this.keyword = keyword;
			this.name = name;
			this.description = description;
		}
		
		public String getTooltipTitle() {
			return name;
		}
		public String getTooltipBody() {
			return description;
		}
		
		public KeywordType getType() {
			return type;
		}
		public String getKeyword() {
			return keyword;
		}
		public String getName() {
			return name;
		}
		public String getDescription() {
			return description;
		}
	}
}
