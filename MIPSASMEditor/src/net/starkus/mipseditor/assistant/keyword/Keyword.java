package net.starkus.mipseditor.assistant.keyword;

public abstract class Keyword {
	protected String keyword;
	protected String name;
	protected String description;
	
	public Keyword(String keyword, String name, String description)
	{
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
	
	public String getKeyword() {
		return keyword;
	}
	public String getName() {
		return name;
	}
	public String getDescription() {
		return description;
	}
	
	@Override
	public String toString()
	{
		return keyword;
	}
}
