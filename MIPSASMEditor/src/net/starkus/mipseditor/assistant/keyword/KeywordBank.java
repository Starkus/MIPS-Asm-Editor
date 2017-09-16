package net.starkus.mipseditor.assistant.keyword;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

public class KeywordBank {

	private final HashMap<String, Keyword> keywords = new HashMap<>();
	
	
	public KeywordBank()
	{
		
	}
	
	public KeywordBank(String text)
	{
		buildFromString(text);
	}
	
	
	public void buildFromString(String text)
	{
		Gson gson = new Gson();
		
		SyntaxFile syntaxFile = gson.fromJson(text, SyntaxFile.class);
		
		for (DirectiveEntry entry : syntaxFile.directives)
		{
			Keyword keyword = new KeywordDirective( 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			keywords.put(keyword.getKeyword(), keyword);
		}
		
		for (OpCodeEntry entry : syntaxFile.arithmeticOpcodes)
		{
			Keyword keyword = new KeywordOpcode( 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			keywords.put(keyword.getKeyword(), keyword);
		}
		
		for (OpCodeEntry entry : syntaxFile.moveOpcodes)
		{
			Keyword keyword = new KeywordOpcode( 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			keywords.put(keyword.getKeyword(), keyword);
		}
		
		for (OpCodeEntry entry : syntaxFile.branchOpcodes)
		{
			Keyword keyword = new KeywordOpcode( 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			keywords.put(keyword.getKeyword(), keyword);
		}
		
		for (OpCodeEntry entry : syntaxFile.logicOpcodes)
		{
			Keyword keyword = new KeywordOpcode( 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			keywords.put(keyword.getKeyword(), keyword);
		}
		
		for (OpCodeEntry entry : syntaxFile.fpuOpcodes)
		{
			Keyword keyword = new KeywordOpcode( 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			keywords.put(keyword.getKeyword(), keyword);
		}
		
		for (RegisterEntry entry : syntaxFile.registerNames)
		{
			KeywordRegisterName namedReg = new KeywordRegisterName(
					entry.name, 
					entry.name, 
					entry.description);
			
			KeywordRegisterName reg = new KeywordRegisterName(
					entry.register, 
					entry.register, 
					"Also referenced by: " + entry.name + "\n" + entry.description);
			
			keywords.put(reg.getKeyword(), reg);
			keywords.put(namedReg.getKeyword(), namedReg);
		}
	}
	
	public HashMap<String, Keyword> getKeywords() {
		return keywords;
	}
	
	public List<Keyword> getKeywordsByType(Class<? extends Keyword> type)
	{
		List<Keyword> result = new ArrayList<Keyword>();
		keywords.values().stream()
				.filter(k -> k.getClass()==(type))
				.forEach(k -> result.add(k));
		
		return result;
	}
	

	private static class DirectiveEntry
	{
		public String keyword;
		public String name;
		public String description;
		public String syntax;
	}
	
	private static class OpCodeEntry
	{
		public String keyword;
		public String name;
		public String description;
		public String syntax;
	}
	
	private static class RegisterEntry
	{
		public String name;
		public String register;
		public String description;
	}
	
	private static class SyntaxFile
	{
		public DirectiveEntry[] directives;
		
		public OpCodeEntry[] arithmeticOpcodes;
		public OpCodeEntry[] moveOpcodes;
		public OpCodeEntry[] branchOpcodes;
		public OpCodeEntry[] logicOpcodes;
		
		public OpCodeEntry[] fpuOpcodes;
		
		public RegisterEntry[] registerNames;
	}
}
