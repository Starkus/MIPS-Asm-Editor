package net.starkus.mipseditor.control;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Popup;
import net.starkus.mipseditor.assistant.keyword.Keyword;

public class SuggestionContextMenu extends Popup
{
	private final VBox vbox = new VBox();
	private final Pane pane = new Pane();
	
	private Text selectedTitle;
	private Text selectedDescription;
	
	private final int maxEntries = 10;
	
	private final SelectionModel<Keyword> selectionModel;
	private final IntegerProperty hoverIndex = new SimpleIntegerProperty(1);
	
	private final ObservableList<Keyword> entries = FXCollections.observableArrayList();
	private final List<Button> buttonList = new ArrayList<>();
	
	private final ObjectProperty<EventHandler<Event>> onAction = new SimpleObjectProperty<>();
	
	
	public SuggestionContextMenu()
	{
		buildLayout();
		
		selectionModel = new SingleSelectionModel<Keyword>() {
			
			@Override
			public void selectNext() {
				select((getSelectedIndex() + 1) % entries.size());
			}
			
			@Override
			public void selectPrevious() {
				select((getSelectedIndex() + entries.size() - 1) % entries.size());
			}
			
			@Override
			public void selectLast() {
				select(entries.size() - 1);
			}
			
			@Override
			public void selectFirst() {
				select(0);
			}
			
			@Override
			public void select(Keyword s) {
				if (!entries.contains(s))
					new NullPointerException("Trying to select an item that's not"
							+ " in the entries!").printStackTrace();
				
				int i = entries.indexOf(s);
				setSelectedIndex(i);
				setSelectedItem(s);
			}
			
			@Override
			public void select(int index) {
				if (index < 0 || index >= entries.size())
					new ArrayIndexOutOfBoundsException().printStackTrace();
				
				setSelectedIndex(index);
				setSelectedItem(getModelItem(index));
			}
			
			@Override
			public boolean isSelected(int index) {
				return getSelectedIndex() == index;
			}
	
			@Override
			protected Keyword getModelItem(int index) {
				return entries.get(index);
			}
	
			@Override
			protected int getItemCount() {
				return entries.size();
			}
			
		};
		
		eventStuffs();
	}
	
	
	private void buildLayout()
	{
		pane.getStylesheets().add(SuggestionContextMenu.class
				.getResource("SuggestionContextMenu.css").toExternalForm());
		pane.getStyleClass().add("background");
		
		getContent().add(pane);
		
		
		selectedTitle = new Text("Title\n");
		selectedTitle.setFill(Color.WHITE);
		selectedTitle.setStyle("-fx-font-weight: bold;");
		
		selectedDescription = new Text("Description");
		selectedDescription.setFill(Color.WHITE);
		
		TextFlow textFlow = new TextFlow(selectedTitle, selectedDescription);
		textFlow.setStyle("-fx-padding: 8px;");
		
		ScrollPane itemsScrollPane = new ScrollPane(vbox);
		itemsScrollPane.setFitToWidth(true);
		itemsScrollPane.onKeyPressedProperty().bind(pane.onKeyPressedProperty());
		
		ScrollPane infoScrollPane = new ScrollPane(textFlow);
		infoScrollPane.setFitToWidth(true);
		
		HBox hbox = new HBox(itemsScrollPane, new ScrollPane(textFlow));
		
		pane.getChildren().add(hbox);
		
		for (int i=0; i < maxEntries; ++i)
		{
			Button b = new Button("Entry");
			b.getStyleClass().add("item");
			b.setMaxWidth(Double.MAX_VALUE);
			
			final int _i = i;
			b.setOnMouseEntered(e -> {
				hoverIndex.set(_i);
			});
			
			b.setOnMouseClicked(e -> {
				onAction.get().handle(e);
				hide();
			});
			
			b.setOnKeyPressed(e -> {
				if (e.getCode() == KeyCode.ENTER)
					onAction.get().handle(e);
			});
			
			buttonList.add(b);
		}
	}
	
	
	private void eventStuffs()
	{
		hoverIndex.addListener((obs, oldv, newv) -> {
			selectionModel.select(newv.intValue());
		});
		
		entries.addListener(new ListChangeListener<Keyword>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Keyword> c) 
			{
				vbox.getChildren().clear();
				
				int entriesSize = Math.min(maxEntries, entries.size());
				
				if (entriesSize == 0)
					hide();
				
				for (int i=0; i < entriesSize; ++i)
				{
					buttonList.get(i).setText(entries.get(i).getKeyword());
					vbox.getChildren().add(buttonList.get(i));
				}
				
				if (!entries.isEmpty())
					selectionModel.selectFirst();
			}
		});
		
		selectionModel.selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				
				Keyword entry = entries.get(newValue.intValue());
				
				selectedTitle.setText(entry.getKeyword() + "\n");
				selectedDescription.setText(entry.getDescription());
				
				for (int i=0; i < maxEntries; ++i) {
					
					if (i == newValue.intValue()) {
						buttonList.get(i).getStyleClass().add("item-selected");
					}
					else {
						buttonList.get(i).getStyleClass().remove("item-selected");
					}
				}
			}
		});
		
		hoverIndex.addListener((obs, oldValue, newValue) -> {
			selectionModel.select(newValue.intValue());
		});
		
		pane.setOnKeyPressed(e -> {
			
			if (!isShowing())
				return;
			
			switch (e.getCode()) {
			
			case ENTER:
				if (onAction.get() != null)
				{
					onAction.get().handle(new ActionEvent());
					
					hide();
					e.consume();
				}
				break;
				
			case SPACE:
				entries.clear();
				hide();
				
				e.consume();
				break;
				
			case UP:
				selectionModel.selectPrevious();
				e.consume();
				break;
				
			case DOWN:
				selectionModel.selectNext();
				e.consume();
				break;
				
			case ESCAPE:
				hide();

			default:
				break;
			}
		});
	}
	
	
	public void setOnMouseClicked(EventHandler<? super MouseEvent> event)
	{
		buttonList.forEach(b -> b.setOnMouseClicked(event));
	}
	
	public EventHandler<Event> getOnAction()
	{
		return onAction.get();
	}
	public void setOnAction(EventHandler<Event> handler)
	{
		onAction.set(handler);
	}
	public ObjectProperty<EventHandler<Event>> onActionProperty()
	{
		return onAction;
	}
	
	public SelectionModel<Keyword> getSelectionModel() {
		return selectionModel;
	}
	
	public ObservableList<Keyword> getEntries() {
		return entries;
	}
	
	public void show(Node owner, Point2D position)
	{
		if (!isShowing() && !entries.isEmpty())
		{
			Keyword entry = entries.get(0);
			
			selectedTitle.setText(entry.getKeyword() + "\n");
			selectedDescription.setText(entry.getDescription());
		}
		
		super.show(owner, position.getX(), position.getY());
	}
}
