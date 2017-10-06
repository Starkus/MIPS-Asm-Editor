package net.starkus.mipseditor.control;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.EventStreams;

import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import net.starkus.mipseditor.assistant.Assistant;
import net.starkus.mipseditor.assistant.SyntaxHighlights;
import net.starkus.mipseditor.assistant.Tooltips;
import net.starkus.mipseditor.util.StringUtils;

public class MyCodeArea extends CodeArea {

	private Assistant assistant = new Assistant(this);
	private Popup popup = new Popup();
	private TextFlow popupFlow = new TextFlow();
	
	private Map<Long, Long> lineAddresses = new HashMap<>();

	public MyCodeArea(FileTab fileTab) {
		super();

		setParagraphGraphicFactory(LineNumberFactory.get(this));

		initInputEvents();
		initTooltipEvents();
		
		
	}

	private void initInputEvents()
	{
		richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
					.successionEnds(Duration.ofMillis(300))
					.supplyTask(this::computeHighlightingAsync)
					.awaitLatest(this.richChanges()).filterMap(t -> {
			if (t.isSuccess())
			{
				return Optional.of(t.get());
			}
			else
			{
				t.getFailure().printStackTrace();
				return Optional.empty();
			}
		}).subscribe(this::applyHighlighting);

		EventStreams.changesOf(textProperty()).successionEnds(Duration.ofMillis(600))
				.subscribe(e -> {
					this.assistant.processCurrentFile();
		});

		setOnKeyPressed(e -> {
			this.assistant.processKeyPress(e);
		});
		
		plainTextChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).addObserver(c ->
			updateNearbyHighlights());
		
		EventStreams.changesOf(caretPositionProperty())
				.successionEnds(Duration.ofMillis(400))
				.supplyTask(this::computeHighlightingAsync)
				.awaitLatest().filterMap(t -> {
					if (t.isSuccess())
					{
						return Optional.of(t.get());
					}
					else
					{
						t.getFailure().printStackTrace();
						return Optional.empty();
					}
				})
				.subscribe(this::applyHighlighting);
	}

	private void initTooltipEvents()
	{
		popup.getContent().add(popupFlow);
		popupFlow.getStyleClass().add("tooltip");

		setMouseOverTextDelay(Duration.ofMillis(400));
		addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_BEGIN, e -> {

			int charIndex = e.getCharacterIndex();
			Point2D pos = e.getScreenPosition();

			boolean tooltip = Tooltips.makeTooltipFromCodeIndex(popupFlow, getText(), charIndex);

			if (tooltip) {
				popup.show(this, pos.getX(), pos.getY() + 10);
			}
		});

		addEventHandler(MouseOverTextEvent.MOUSE_OVER_TEXT_END, e -> {
			popup.hide();
		});
	}
	
	public void showNoLineDecorations()
	{
		setParagraphGraphicFactory(null);
	}
	public void showNumberLines()
	{
		setParagraphGraphicFactory(LineNumberFactory.get(this));
	}
	public void showAddresses()
	{
		setParagraphGraphicFactory(LineAddressFactory.get(this));
	}

	@Override
	public void replaceText(String replacement)
	{
		/* When replacing whole text, highlight it right away */
		super.replaceText(replacement);

		applyHighlighting(SyntaxHighlights.computeHighlighting(getText(), getCaretPosition()));
	}

	private Task<StyleSpans<Collection<String>>> computeHighlightingAsync()
	{
		String text = getText();
		Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
			protected StyleSpans<java.util.Collection<String>> call() throws Exception {
				calculateLineAddresses();
				return SyntaxHighlights.computeHighlighting(text, getCaretPosition());
			}
		};

		SyntaxHighlights.getHighlightingExecutor().execute(task);
		return task;
	}
	
	private void updateNearbyHighlights()
	{
		if (getText().isEmpty())
			return;
		
		int caret = getCaretPosition();
		int start = StringUtils.startOfLine(getText(), Math.max(0, caret - 50));
		int end = StringUtils.endOfWord(getText(), Math.min(caret + 50, getText().length()));
		
		StyleSpans<Collection<String>> nearbySpans = SyntaxHighlights.computeHighlighting(start, end, getText(), caret);
		
		try {
			setStyleSpans(start, nearbySpans);
		} catch (IllegalStateException e) {
			//e.printStackTrace();
		}
	}

	private void applyHighlighting(StyleSpans<Collection<String>> highlighting)
	{
		if (getText().isEmpty())
			return;
		
		try {
			setStyleSpans(0, highlighting);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
	
	private void calculateLineAddresses()
	{
		long programCounter = 0;
		
		lineAddresses.clear();
		
		String[] lines = getText().split("\n");
		for (int i=0; i < lines.length; ++i)
		{
			String line = lines[i];
			String[] words = line.split("\\s");
			
			if (line.isEmpty() || words.length == 0)
				continue;
			
			if (words[0].equals(".org"))
			{
				try
				{
					long ad = Long.parseLong(line.split("\\s")[1].substring(2), 16);
					programCounter = ad;
				}
				catch (NumberFormatException e) {
					e.printStackTrace();
				}
				
				continue;
			}
			
			
			Matcher matcher = SyntaxHighlights.getPattern().matcher(words[0]);
			
			if (matcher.find() && matcher.group("OPCODE") != null)
			{
				lineAddresses.put((long) i, programCounter);
				programCounter += 4;
			}
		}
	}
	
	public Map<Long, Long> getLineAddresses()
	{
		return lineAddresses;
	}
}
