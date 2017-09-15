package net.starkus.mipseditor.assistant.keyword;

public class KeywordDefine extends Keyword {

	public KeywordDefine(String keyword, String name, String description) {
		super(keyword, name, description);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getTooltipTitle() {
		return keyword + " == " + name;
	}
	
	@Override
	public String getTooltipBody() {
		return description;
	}
}
