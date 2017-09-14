package net.starkus.mipseditor.syntax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.google.gson.Gson;

public class KeywordBank {

	private final HashMap<String, Syntax.Keyword> assemblerInstructions = new HashMap<>();
	private final HashMap<String, Syntax.Keyword> opcodes = new HashMap<>();
	private final HashMap<String, Syntax.Keyword> registers = new HashMap<>();
	
	
	public KeywordBank()
	{
		
	}
	public KeywordBank(File file)
	{
		try {
			buildFromFile(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void buildFromFile(File file) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		String text = "";
		String currentLine;
		while ((currentLine = reader.readLine()) != null)
		{
			text += currentLine;
		}
		
		reader.close();
		
		
		Gson gson = new Gson();
		
		SyntaxFile syntaxFile = gson.fromJson(text, SyntaxFile.class);
		
		for (AssemblerInstructionEntry entry : syntaxFile.assemblerInstructions)
		{
			Syntax.Keyword keyword = new Syntax.Keyword(Syntax.KeywordType.OPCODE, 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			assemblerInstructions.put(keyword.keyword, keyword);
		}
		
		for (OpCodeEntry entry : syntaxFile.arithmeticOpcodes)
		{
			Syntax.Keyword keyword = new Syntax.Keyword(Syntax.KeywordType.OPCODE, 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);
			
			opcodes.put(keyword.keyword, keyword);
		}
		
		for (OpCodeEntry entry : syntaxFile.moveOpcodes)
		{
			Syntax.Keyword keyword = new Syntax.Keyword(Syntax.KeywordType.OPCODE, 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			opcodes.put(keyword.keyword, keyword);
		}
		
		for (OpCodeEntry entry : syntaxFile.branchOpcodes)
		{
			Syntax.Keyword keyword = new Syntax.Keyword(Syntax.KeywordType.OPCODE, 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			opcodes.put(keyword.keyword, keyword);
		}
		
		for (OpCodeEntry entry : syntaxFile.logicOpcodes)
		{
			Syntax.Keyword keyword = new Syntax.Keyword(Syntax.KeywordType.OPCODE, 
					entry.keyword, 
					entry.name, 
					entry.description + "\nSyntax: " + entry.syntax);

			opcodes.put(keyword.keyword, keyword);
		}
		
		for (RegisterEntry entry : syntaxFile.registerNames)
		{
			Syntax.Keyword namedReg = new Syntax.Keyword(Syntax.KeywordType.REGISTER, 
					entry.name, 
					entry.name, 
					entry.description);
			
			Syntax.Keyword reg = new Syntax.Keyword(Syntax.KeywordType.REGISTER, 
					entry.register, 
					entry.register, 
					"Also referenced by: " + entry.name + "\n" + entry.description);
			
			registers.put(reg.keyword, reg);
			registers.put(namedReg.keyword, namedReg);
		}
	}
	
	public Syntax.Keyword getKeyword(String s)
	{
		if (assemblerInstructions.containsKey(s))
			return assemblerInstructions.get(s);
		
		if (opcodes.containsKey(s))
			return opcodes.get(s);
		
		if (registers.containsKey(s))
			return registers.get(s);
		
		return null;
	}
	
	public HashMap<String, Syntax.Keyword> getAssemblerInstructions() {
		return assemblerInstructions;
	}
	public HashMap<String, Syntax.Keyword> getOpcodes() {
		return opcodes;
	}
	public HashMap<String, Syntax.Keyword> getRegisters() {
		return registers;
	}
	

	private static class AssemblerInstructionEntry
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
		public AssemblerInstructionEntry[] assemblerInstructions;
		
		public OpCodeEntry[] arithmeticOpcodes;
		public OpCodeEntry[] moveOpcodes;
		public OpCodeEntry[] branchOpcodes;
		public OpCodeEntry[] logicOpcodes;
		
		public RegisterEntry[] registerNames;
	}
}
