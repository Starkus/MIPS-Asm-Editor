package net.starkus.mipseditor.control;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.MouseOverTextEvent;
import org.fxmisc.richtext.model.StyleSpans;
import org.reactfx.EventStreams;

import javafx.concurrent.Task;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import net.starkus.mipseditor.assistant.Assistant;
import net.starkus.mipseditor.assistant.Syntax;
import net.starkus.mipseditor.assistant.Tooltips;
import net.starkus.mipseditor.model.FileManager;
import net.starkus.mipseditor.util.StringUtils;

public class MyCodeArea extends CodeArea {

	private final FileTab fileTab;

	private Assistant assistant = new Assistant(this);
	private Popup popup = new Popup();
	private TextFlow popupFlow = new TextFlow();

	public MyCodeArea(FileTab fileTab) {
		super();
		this.fileTab = fileTab;

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
				.supplyTask(this::updateSourceAsync).subscribe(e -> {
		});

		addEventFilter(KeyEvent.KEY_RELEASED, e -> {
			this.assistant.processKeyPress(e);
		});
		
		/*plainTextChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).addObserver(c -> {
			
			int caret = getCaretPosition();
			int start = StringUtils.startOfLine(getText(), Math.max(0, caret - 50));
			int end = StringUtils.endOfWord(getText(), Math.min(caret + 50, getText().length()));
			
			setStyleSpans(start, Syntax.computeHighlighting(start, end, getText(), caret));
		});*/
		
		caretPositionProperty().addListener((obs, oldv, newv) -> {
			
			int caret = newv;
			int start = StringUtils.startOfLine(getText(), Math.max(0, caret - 50));
			int end = StringUtils.endOfWord(getText(), Math.min(caret + 50, getText().length()));
			
			StyleSpans<Collection<String>> nearbySpans = Syntax.computeHighlighting(start, end, getText(), caret);
			
			nearbySpans.overlay(getStyleSpans(0, getText().length()), (a, b) -> a);
			
			setStyleSpans(start, nearbySpans);
		});
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

	@Override
	public void replaceText(String replacement)
	{
		/* When replacing whole text, highlight it right away */
		super.replaceText(replacement);

		applyHighlighting(Syntax.computeHighlighting(getText(), getCaretPosition()));
	}

	private Task<StyleSpans<Collection<String>>> computeHighlightingAsync()
	{
		String text = getText();
		Task<StyleSpans<Collection<String>>> task = new Task<StyleSpans<Collection<String>>>() {
			protected StyleSpans<java.util.Collection<String>> call() throws Exception {
				return Syntax.computeHighlighting(text, getCaretPosition());
			}
		};

		Syntax.getHighlightingExecutor().execute(task);
		return task;
	}

	private Task<Void> updateSourceAsync()
	{
		FileManager.getOpenfiles().replace(fileTab.getFile(), getText());

		return null;
	}

	private void applyHighlighting(StyleSpans<Collection<String>> highlighting)
	{
		setStyleSpans(0, highlighting);
	}
}
