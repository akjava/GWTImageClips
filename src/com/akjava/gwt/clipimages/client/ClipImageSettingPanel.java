package com.akjava.gwt.clipimages.client;

import java.util.List;

import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.FileListListener;
import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.ReadListener;
import com.akjava.gwt.html5.client.file.FileIOUtils.RemoveCallback;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.AsyncMultiCaller;
import com.akjava.gwt.lib.client.widget.cell.EasyCellTableObjects;
import com.akjava.gwt.lib.client.widget.cell.HtmlColumn;
import com.akjava.gwt.lib.client.widget.cell.SimpleCellTable;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ClipImageSettingPanel extends DockLayoutPanel {
private GWTClipImages gwtClipImages;
private EasyCellTableObjects<ImageClipData> tableObjects;
private Button deleteUnusedDatasBt;
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
		
		mainPanel.add(new Label("CleanUps"));
		HorizontalPanel clPanel=new HorizontalPanel();
		mainPanel.add(clPanel);
		Button cleanToTrashBt=new Button("unused data to trashbox",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				gwtClipImages.getClipImageList().getUnusedFiles(new FileListListener() {
					@Override
					public void files(List<String> fileNames) {
						AsyncMultiCaller<String> loader=new AsyncMultiCaller<String>(fileNames) {
							@Override
							public void execAsync(final String data) {
								gwtClipImages.getClipImageList().read(data, new ReadListener<ImageClipData>() {

									@Override
									public void onError(String message) {
										LogUtils.log("maybe invalid?:"+data+",errro="+message);
										done(data, false);
									}

									@Override
									public void onRead(ImageClipData cdata) {
										tableObjects.addItem(cdata);
										done(data, true);
									}
								});
							}
							
						};
						loader.startCall();
					}
				});
			}
		});
		clPanel.add(cleanToTrashBt);
		
		deleteUnusedDatasBt = new Button("delete unused datas",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				boolean confirm=Window.confirm("really delete unused files?");
				if(!confirm){
					return;
				}
				gwtClipImages.getClipImageList().deleteUnusedFiles();//TODO catch and clean
				
				
			}
		});
		clPanel.add(deleteUnusedDatasBt);
		deleteUnusedDatasBt.setEnabled(false);//can do after load.
		
		Button deleteBrokenDataBt=new Button("delete broken datas",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				AsyncMultiCaller<String> caller=new AsyncMultiCaller<String>(Lists.newArrayList(gwtClipImages.getReadFaildFileNameSet())){

					@Override
					public void execAsync(final String data) {
						boolean confirm=Window.confirm("really delete broken files?");
						if(!confirm){
							return;
						}
						
						gwtClipImages.getClipImageList().getFileSystem().removeData(data, new RemoveCallback() {
							
							@Override
							public void onError(String message, Object option) {
								LogUtils.log("faild-remove:"+message+","+option);
								done(data, false);
							}
							
							@Override
							public void onRemoved() {
								LogUtils.log("success-invalid-data:"+data);
								done(data, true);
							}
						});
					}
					
				};
				caller.startCall();
			}
		});
		clPanel.add(deleteBrokenDataBt);
		
		
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
		deleteUnusedDatasBt.setEnabled(true);
	}
}
