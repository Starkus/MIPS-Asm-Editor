package net.starkus.mipseditor.syntax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class Syntax {
	

	public static final String OPCODE_PATTERN = "\\b(" + String.join("|", getOpCodes()) + ")\\b";
	public static final String REGISTERNAME_PATTERN = "\\b(" + String.join("|", getRegisterNames()) + ")\\b";
	
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String LITERAL_PATTERN = "0[xX][0-9a-fA-F]+";
	
	public static final Pattern PATTERN = Pattern.compile("(?<OPCODE>" + OPCODE_PATTERN + ")"
					+ "|(?<REGISTERNAME>" + REGISTERNAME_PATTERN + ")"
					+ "|(?<COMMENT>" + COMMENT_PATTERN + ")"
					+ "|(?<LITERAL>" + LITERAL_PATTERN + ")"
			);
	
	
	private static List<String> getOpCodes()
	{
		List<String> list = new ArrayList<String>();

		for (OpCode x : OpCode.values())
			list.add(x.name());
		
		return list;
	}
	private static List<String> getRegisterNames()
	{
		List<String> list = new ArrayList<String>();

		for (RegisterName x : RegisterName.values())
			list.add(x.name());
		
		return list;
	}
	
	public static boolean makeTooltipFromCodeIndex(TextFlow flow, String code, int index)
	{
		String word = StringUtils.getWordFromIndex(code, index);
		
		if (word == null)
			return false;
		
		Matcher matcher = PATTERN.matcher(word);
		Text title = new Text();
		Text desc = new Text();

		title.getStyleClass().add("tooltip-title");
		desc.getStyleClass().add("tooltip-content");
		
		while (matcher.find())
			if (matcher.group("OPCODE") != null) 
			{
				OpCode o = OpCode.valueOf(word);
				
				title.setText(o.getName());
				desc.setText(o.getDescription());
				
			} else if (matcher.group("REGISTERNAME") != null)
			{
				desc.setText(RegisterName.valueOf(word).getTooltip());
			} else
			{
				return false;
			}
		
		title.setText(title.getText() + "\n");
		flow.getChildren().setAll(title, desc);
		
		return true;
	}
	
	public static StyleSpans<Collection<String>> computeHighlighting(String code)
	{
		Matcher matcher = PATTERN.matcher(code);
		int lastKwEnd = 0; // ?
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		
		while (matcher.find())
		{
			String styleClass = matcher.group("OPCODE") != null ? "opcode" :
					matcher.group("REGISTERNAME") != null ? "register-name" :
					matcher.group("COMMENT") != null ? "comment" :
					matcher.group("LITERAL") != null ? "literal" :
					null;
			
			spansBuilder.add(Collections.singleton("normal-code"), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		
		spansBuilder.add(Collections.emptyList(), code.length() - lastKwEnd);
		return spansBuilder.create();
	}
	
	

	public static enum OpCode {
		
		ADD("Add",
				"Add two registers.",
				"ADD RD, RS, RT"),
		ADDI("Add immediate",
				"Add literal to register.",
				"ADDI RD, RS, LI"),
		ADDIU("Add immediate unsigned",
				"Add literal to register (no overflow).",
				"ADDIU RD, RS, LI"),
		ADDU("Add unsigned",
				"Add two registers (no overflow).",
				"ADDU RD, RS, RT"),
		
		SUB("Subtract",
				"Substract two registers.",
				"SUB RD, RS, RT"),
		SUBU("Subtract immediate",
				"Substract two registers (no overflow).",
				"SUBU RD, RS, RT"),
		
		DIV("Divide",
				"Divides source by target and stores quotient in LO and reminder in HI.",
				"DIV RS, RT"),
		DIVU("Divide unsigned",
				"Divides source by target and stores quotient in LO and reminder in HI.",
				"DIVU RS, RT"),
		MULT("Multiply",
				"Multiplies source by target and stores result in LO (and HI if >16 bits).",
				"MULT RS, RT"),
		MULTU("",
				"Multiplies source by target and stores result in LO (and HI if >16 bits).",
				"MULT RS, RT"),
		
		MFHI("Move from HI",
				"Move value from HI to specified register.",
				"MFHI RD"),
		MFLO("Move from LO",
				"Move value from LO to specified register.",
				"MFLO RD"),
		
		
		BEQ("Branch if equal",
				"Branch if both registers are equal.",
				"BEQ RS, RT, Offset"),
		BGEZ("Branch if greater or equal to zero",
				"Branch if the register is greater or equal to zero.",
				"BGEZ RS, Offset"),
		BGEZAL("Branch if greater or equal to zero and link",
				"Branch if the register is greater or equal to zero and returns in R31.",
				"BGEZAL RS, Offset"),
		BGTZ("Branch if greater than zero",
				"Branch if the register is greater than zero.",
				"BGTZ RS, Offset"),
		BLEZ("Branch if less or equal to zero",
				"Branch if the register is less or equal to zero.",
				"BLEZ RS, Offset"),
		BLTZ("Branch if less than zero",
				"Branch if the register is less than zero.",
				"BLTZ RS, Offset"),
		BLTZAL("Branch if less than zero and link",
				"Branch if the register is less than zero and returns in R31.",
				"BLTZAL RS, Offset"),
		BNE("Branch if not equal",
				"Branch if the registers are not equal.",
				"BNE RS, RT, Offset"),
		
		J("Jump",
				"Jump to the calculated address.",
				"J Target"),
		JAL("Jump and link",
				"Jump to the calculated address and return to R31.",
				"JAL Target"),
		JR("Jump register",
				"Jump to address contained in register.",
				"JR RS"),
		

		SLT("Set on less than",
				"If source less than target, destination is set to one, otherwise to zero.",
				"SLT RD, RS, RT"),
		SLTI("Set on less than immediate",
				"If source less than literal, destination is set to one, otherwise to zero.",
				"SLTI RT, RS, LI"),
		SLTIU("Set on less than immediate unsigned",
				"If source less than unsigned literal, destination is set to one, otherwise to zero.",
				"SLTIU RT, RS, LI"),
		SLTU("Set on less than unsigned",
				"If source less than target, destination is set to one, otherwise to zero.",
				"SLTU RT, RS, LI"),
		

		LUI("Load upper immediate",
				"Shift immediate value left 16 bits and stores it in the register. Lower 16 bits are zeroes.",
				"LUI RT, LI"),
		LB("Load byte",
				"Load byte from specified address into register.",
				"LB RT, Offset(RS)"),
		LW("Load word",
				"Load word from specified address into register.",
				"LW RT, Offset(RS)"),
		
		SB("Save byte",
				"Least significant byte from target is stored at specified address.",
				"SB RT, Offset(RS)"),
		
		SW("Save word",
				"Store value from register into specified address.",
				"SW RT, Offset(RS)"),
		
		
		AND("And",
				"Bitwise AND between two registers.",
				"AND RD, RS, RT"),
		ANDI("And immediate",
				"Bitwise AND between a register and a literal.",
				"ANDI RT, RS, i"),
		OR("Or",
				"Bitwise OR between two registers.",
				"OR RD, RS, RT"),
		ORI("Or immediate",
				"Bitwise OR between a register and a literal.",
				"ORI RT, RS, LI"),
		
		XOR("Exclusive or",
				"Bitwise XOR between two registers.",
				"XOR RD, RS, RT"),
		XORI("Exclusive or immediate",
				"Bitwise XOR between a register and a literal.",
				"XORI RT, RS, LI"),
		
		SLL("Shift left logical",
				"Shift register value left by immediate ammout and store in another register.",
				"SLL RD, RT, LI"),
		SLLV("Shift left logical variable",
				"Shift target value left by source ammount and store in destination.",
				"SLLV RD, RT, RS"),
		
		SRA("Shift right arithmetic",
				"Shift a register value right by immediate ammount and store in another register. Sign bit is shifted in.",
				"SRA RD, RT, LI"),
		SRL("Shift right logical",
				"Shift a register value right by immediate ammount and store in another register. Zeroes are shifted in.",
				"SRL RD, RT, LI"),
		SRLV("Shift right logical variable",
				"Shift target value right by source ammount and store in destination. Zeroes are shifted in.",
				"SRLV RD, RT, RS"),
		
		
		NOOP("No operation",
				"Do nothing.","NOOP"),
		NOP("No operation",
				"Do nothing.","NOP"),
		
		SYSCALL("System call",
				"Generates a software interrupt.",
				"SYSCALL");
		
		private String name;
		private String description;
		public static final String styleClass = "opcode";
		
		OpCode(String name, String description, String syntax)
		{
			syntax = syntax.replaceAll("RD", "RDestination")
				.replaceAll("RS", "RSource")
				.replaceAll("RT", "RTarget")
				.replaceAll("LI", "Literal");
			
			this.name = name;
			this.description = description + "\n\nSyntax: " + syntax;
		}
		
		public String getName() {
			return name;
		}
		public String getDescription() {
			return description;
		}
	}
	
	public static enum RegisterName {
		
		R0("R0 is a hard-wired zero register. It's value is always zero."),
		V0("Register 2, contains return values from subroutines. If return value is 64-bit wide, V1 is also used."),
		V1("Register 3, contains the higher bits from function return values."),
		A0("Register 4, meant for subroutine parameters"),
		A1("Register 5, meant for subroutine parameters"),
		A2("Register 6, meant for subroutine parameters"),
		A3("Register 7, meant for subroutine parameters"),
		T0("Register 8, used for local values inside a subroutine."),
		T1("Register 9, used for local values inside a subroutine."),
		T2("Register 10, used for local values inside a subroutine."),
		T3("Register 11, used for local values inside a subroutine."),
		T4("Register 12, used for local values inside a subroutine."),
		T5("Register 13, used for local values inside a subroutine."),
		T6("Register 14, used for local values inside a subroutine."),
		T7("Register 15, used for local values inside a subroutine."),
		S0("Register 16, is supposed not to be changed by subroutines."),
		S1("Register 17, is supposed not to be changed by subroutines."),
		S2("Register 18, is supposed not to be changed by subroutines."),
		S3("Register 19, is supposed not to be changed by subroutines."),
		S4("Register 20, is supposed not to be changed by subroutines."),
		S5("Register 21, is supposed not to be changed by subroutines."),
		S6("Register 22, is supposed not to be changed by subroutines."),
		S7("Register 23, is supposed not to be changed by subroutines."),
		T8("Register 24, used for local values inside a subroutine."),
		T9("Register 25, used for local values inside a subroutine."),
		K0("Register 26, kernel reserved."),
		K1("Register 27, kernel reserved."),
		
		GP("Register 28, Global Pointer. Points to the middle of the block memory in the static data segment."),
		SP("Register 29, Stack Pointer. Points to the top of the stack."),
		FP("Register 30, Frame Pointer. Previous top of the stack."),
		
		RA("Register 31, Return address register. Saves the address to jump back to once a subroutine is over."),
		
		F0("Register 32, used for floating-point operations."),
		F1("Register 33, used for floating-point operations."),
		F2("Register 34, used for floating-point operations."),
		F3("Register 35, used for floating-point operations."),
		F4("Register 36, used for floating-point operations."),
		F5("Register 37, used for floating-point operations."),
		F6("Register 38, used for floating-point operations."),
		F7("Register 39, used for floating-point operations."),
		F8("Register 40, used for floating-point operations."),
		F9("Register 41, used for floating-point operations."),
		F10("Register 42, used for floating-point operations."),
		F11("Register 43, used for floating-point operations."),
		F12("Register 44, used for floating-point operations."),
		F13("Register 45, used for floating-point operations."),
		F14("Register 46, used for floating-point operations."),
		F15("Register 47, used for floating-point operations."),
		F16("Register 48, used for floating-point operations."),
		F17("Register 49, used for floating-point operations."),
		F18("Register 50, used for floating-point operations."),
		F19("Register 51, used for floating-point operations."),
		F20("Register 52, used for floating-point operations."),
		F21("Register 53, used for floating-point operations."),
		F22("Register 54, used for floating-point operations."),
		F23("Register 55, used for floating-point operations."),
		F24("Register 56, used for floating-point operations."),
		F25("Register 57, used for floating-point operations."),
		F26("Register 58, used for floating-point operations."),
		F27("Register 59, used for floating-point operations."),
		F28("Register 60, used for floating-point operations."),
		F29("Register 61, used for floating-point operations."),
		F30("Register 62, used for floating-point operations."),
		F31("Register 63, used for floating-point operations."),
		F32("Register 64, used for floating-point operations."),
		;
		
		private String tooltip;
		public static final String styleClass = "register-name";
		
		RegisterName(String tooltip)
		{
			this.tooltip = tooltip;
		}
		
		public String getTooltip()
		{
			return tooltip;
		}
	}
}
