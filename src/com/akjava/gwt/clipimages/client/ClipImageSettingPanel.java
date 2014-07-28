package com.akjava.gwt.clipimages.client;

import com.akjava.gwt.lib.client.widget.cell.EasyCellTableObjects;
import com.akjava.gwt.lib.client.widget.cell.HtmlColumn;
import com.akjava.gwt.lib.client.widget.cell.SimpleCellTable;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ClipImageSettingPanel extends DockLayoutPanel {
private GWTClipImages gwtClipImages;
private EasyCellTableObjects<ImageClipData> tableObjects;
	public ClipImageSettingPanel(GWTClipImages app) {
		
		super(Unit.PX);
		this.gwtClipImages=app;
		// TODO Auto-generated constructor stub
		VerticalPanel topPanel=new VerticalPanel();
		topPanel.setSpacing(16);
		this.addNorth(topPanel, 48);
		topPanel.setSize("100%", "100%");
		topPanel.getElement().getStyle().setBackgroundColor("#607d8b");
		
		Label appLabel=new Label("ImageClip >> Settings");
		topPanel.add(appLabel);
		
		
		VerticalPanel mainPanel=new VerticalPanel();
		add(mainPanel);
		
		Button closeBt=new Button("Close");
		
		mainPanel.add(closeBt);
		
		closeBt.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
				gwtClipImages.showMainWidget();
			}
		});
		
		SimpleCellTable<ImageClipData> table=new SimpleCellTable<ImageClipData>() {
			@Override
			public void addColumns(CellTable<ImageClipData> table) {
				TextColumn<ImageClipData> titleColumn=new TextColumn<ImageClipData>() {
					@Override
					public String getValue(ImageClipData object) {
						return object.getTitle();
					}
				};
				table.addColumn(titleColumn,"title");
				
				TextColumn<ImageClipData> descriptionColumn=new TextColumn<ImageClipData>() {
					@Override
					public String getValue(ImageClipData object) {
						return object.getDescription();
					}
				};
				table.addColumn(descriptionColumn,"title");
				HtmlColumn<ImageClipData> htmlColumn=new HtmlColumn<ImageClipData>() {

					@Override
					public String toHtml(ImageClipData object) {
						// TODO Auto-generated method stub
						return "<img src='"+object.getImageData()+"' style='max-width:200px;max-height:200px'>";
					}
				};
				table.addColumn(htmlColumn,"image");
			}
		};
		
		final Button recoverButton=new Button("Recover",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doRecover();
			}
		});
		recoverButton.setEnabled(false);
		
		tableObjects = new EasyCellTableObjects<ImageClipData>(table) {
			@Override
			public void onSelect(ImageClipData selection) {
				recoverButton.setEnabled(true);
			}
		};
		
		mainPanel.add(table);
		mainPanel.add(recoverButton);
		
		
		Button clearAllButton=new Button("Clear all",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				boolean confirm=Window.confirm("really clear all datas?\nthis takes few times");
				if(!confirm){
					return;
				}
				gwtClipImages.deleteAllFiles();
				gwtClipImages.listUpdate();
				gwtClipImages.showMainWidget();
				
				
			}
		});
		mainPanel.add(clearAllButton);
	}
	
	public void addTrashBox(ImageClipData data){
		tableObjects.addItem(data);
	}
	
	protected void doRecover() {
		if(tableObjects.getSelection()!=null){
			gwtClipImages.add(tableObjects.getSelection());
		}
	}

	protected void onClose() {
		// TODO Auto-generated method stub
		
	}
	public void onReadAll(){
		//enable clean-up button
	}
}
