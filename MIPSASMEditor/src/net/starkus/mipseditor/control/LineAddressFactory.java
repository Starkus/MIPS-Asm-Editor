package net.starkus.mipseditor.control;

import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.IntFunction;

import org.fxmisc.richtext.GenericStyledArea;
import org.reactfx.collection.LiveList;
import org.reactfx.value.Val;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

public class LineAddressFactory implements IntFunction<Node>
{
	private static final Insets DEFAULT_INSETS = new Insets(0.0, 5.0, 0.0, 5.0);
    private static final Paint DEFAULT_TEXT_FILL = Color.web("#666");
    private static final Font DEFAULT_FONT =
            Font.font("monospace", FontPosture.ITALIC, 13);
    private static final Background DEFAULT_BACKGROUND =
            new Background(new BackgroundFill(Color.web("#ddd"), null, null));
    

    private final Val<Integer> nParagraphs;
    private final Callable<String> format;
    
    
    
    
    public static IntFunction<Node> get(GenericStyledArea<?, ?, ?> area) {
        return new LineAddressFactory(area, () -> "%08X");
    }
    
    
    private LineAddressFactory(
    		GenericStyledArea<?, ?, ?> area,
    		Callable<String> format)
	{
		nParagraphs = LiveList.sizeOf(area.getParagraphs());
		this.format = format;
	}
    
	
	@Override
	public Node apply(int value)
	{
		Val<String> formatted = nParagraphs.map(n -> format(value * 4));
		
		Label lineAddress = new Label();
		lineAddress.setFont(DEFAULT_FONT);
		lineAddress.setBackground(DEFAULT_BACKGROUND);
		lineAddress.setTextFill(DEFAULT_TEXT_FILL);
		lineAddress.setPadding(DEFAULT_INSETS);
		lineAddress.setAlignment(Pos.TOP_RIGHT);
		lineAddress.getStyleClass().add("lineno");
		
		lineAddress.textProperty().bind(formatted.conditionOnShowing(lineAddress));
		
		return lineAddress;
	}
	
	private String format(int x)
	{
		try
		{
			return String.format(format.call(), x);
		} catch (Exception e)
		{
			e.printStackTrace();
			return "!";
		}
	}
}