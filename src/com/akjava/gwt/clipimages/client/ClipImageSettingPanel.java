package com.akjava.gwt.clipimages.client;

import java.util.List;

import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.FileListListener;
import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.ReadListener;
import com.akjava.gwt.html5.client.download.HTML5Download;
import com.akjava.gwt.html5.client.file.Blob;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileIOUtils.ReadStringCallback;
import com.akjava.gwt.html5.client.file.FileIOUtils.RemoveCallback;
import com.akjava.gwt.html5.client.file.FileIOUtils.WriteCallback;
import com.akjava.gwt.html5.client.file.FileUploadForm;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.html5.client.file.FileUtils.DataArrayListener;
import com.akjava.gwt.html5.client.file.Uint8Array;
import com.akjava.gwt.html5.client.file.webkit.FileEntry;
import com.akjava.gwt.jszip.client.JSZip;
import com.akjava.gwt.lib.client.JavaScriptUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.AsyncMultiCaller;
import com.akjava.gwt.lib.client.widget.cell.EasyCellTableObjects;
import com.akjava.gwt.lib.client.widget.cell.HtmlColumn;
import com.akjava.gwt.lib.client.widget.cell.SimpleCellTable;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ClipImageSettingPanel extends DockLayoutPanel {
private GWTClipImages gwtClipImages;
private EasyCellTableObjects<ImageClipData> tableObjects;
private Button deleteUnusedDatasBt;
private Button dumpAllBt;
private Button cleanToTrashBt;
private FileUploadForm restoreFileUpload;
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
		mainPanel.setSpacing(8);
		
		Button closeBt=new Button("Close");
		
		mainPanel.add(closeBt);
		
		closeBt.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
				gwtClipImages.showMainWidget();
			}
		});
		
		Label trashbox=new Label("Trashbox");
		mainPanel.add(trashbox);
		
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
		
		Label delateAllLabel=new Label("Delte all data");
		mainPanel.add(delateAllLabel);
		
		Button clearAllButton=new Button("Delete all files on FileSystem(can't recover it.Do dump first)",new ClickHandler() {
			
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
		cleanToTrashBt = new Button("unused data to trashbox",new ClickHandler() {
			
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
							@Override
							public void doFinally(boolean cancelled) {
								if(cancelled)
									LogUtils.log("async multi-caller finally:cancelled="+cancelled);
							}
							
						};
						loader.startCall();
					}
				});
			}
		});
		clPanel.add(cleanToTrashBt);
		cleanToTrashBt.setEnabled(false);
		
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
					@Override
					public void doFinally(boolean cancelled) {
						if(cancelled)
							LogUtils.log("async multi-caller finally:cancelled="+cancelled);
					}
					
				};
				caller.startCall();
			}
		});
		clPanel.add(deleteBrokenDataBt);
		
		Label dumpLabel=new Label("Dump & Restore");
		mainPanel.add(dumpLabel);
		HorizontalPanel dumpButtons=new HorizontalPanel();
		dumpButtons.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		mainPanel.add(dumpButtons);
		final VerticalPanel dumpLinks=new VerticalPanel();
		mainPanel.add(dumpLinks);
		Button dumpImages=new Button("Extract Image only",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				final JSZip zip=JSZip.newJSZip();
				
				doGetAllFile(new ReadAllFileListener() {
					@Override
					public void read(ImageClipData clipdata) {
						String imageUrl=clipdata.getImageData();
						zip.base64UrlFile(clipdata.getId()+".webp", imageUrl);
					}
					
					@Override
					public void error(String message) {
						LogUtils.log(message);
					}
					
					@Override
					public void end() {
						dumpLinks.clear();
						Blob blob=zip.generateBlob(null);
						Anchor a=new HTML5Download().generateDownloadLink(blob,"application/zip","clip-raw-images.zip","Download clip-raw-images",true);
						dumpLinks.add(a);
					}
				});
				
			}
		});
		dumpButtons.add(dumpImages);
		
		dumpAllBt = new Button("Dump Alls",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				dumpAllBt.setEnabled(false);
				new Timer(){

					@Override
					public void run() {
						final JSZip zip=JSZip.newJSZip();
						gwtClipImages.getClipImageList().getAllFiles(new FileListListener() {
							@Override
							public void files(List<String> fileNames) {
								AsyncMultiCaller<String> caller=new AsyncMultiCaller<String>(fileNames) {
									@Override
									public void execAsync(final String data) {
										gwtClipImages.getClipImageList().getFileSystem().readText(data, new ReadStringCallback() {
											
											@Override
											public void onError(String message, Object option) {
												LogUtils.log("read-faild:"+message+","+option);
												done(data,false);
											}
											
											@Override
											public void onReadString(String text, FileEntry file) {
												zip.file(file.getName(), text);
												done(data,true);
											}
										});
									}
									public void doFinally(boolean cancelled){
										dumpLinks.clear();
										Blob blob=zip.generateBlob(null);
										Anchor a=new HTML5Download().generateDownloadLink(blob,"application/zip","clip-images.zip","Download clip-images",true);
										dumpLinks.add(a);
										dumpAllBt.setEnabled(true);
									}
								};
								caller.startCall();
								
							}
						}, null);
					}
					
				}.schedule(50);
				
				
					}
		});
				
		dumpButtons.add(dumpAllBt);
		
		
		
	
restoreFileUpload = FileUtils.createSingleFileUploadForm(new DataArrayListener() {
		@Override
		public void uploaded(File file, final Uint8Array array) {
			
			restoreFileUpload.getFileUpload().setEnabled(false);
			
			new Timer(){
				public void run(){
					
					final JSZip zip=JSZip.loadFromArray(array);
					
					JsArrayString files=zip.getFiles();
					List<String> fileNameList=JavaScriptUtils.toList(files);
					
					AsyncMultiCaller<String> writeDatas=new AsyncMultiCaller<String>(fileNameList) {

						@Override
						public void doFinally(boolean cancelled) {
							restoreFileUpload.getFileUpload().setEnabled(true);
							gwtClipImages.getClipImageList().reReadAll();
						}

						@Override
						public void execAsync(final String data) {
							String text=zip.getFile(data).asText();
							gwtClipImages.getClipImageList().getFileSystem().updateData(data, text, new WriteCallback() {
								
								@Override
								public void onError(String message, Object option) {
									LogUtils.log("write-faild:"+data+","+message+","+option);
									done(data,false);
								}
								
								@Override
								public void onWriteEnd(FileEntry file) {
									done(data,true);
								}
							});
						}
						
					};
					writeDatas.startCall();
					
				}
			}.schedule(50);
			
		
			
		}
			
		}, true,false);
restoreFileUpload.setAccept("*.zip");
		
		dumpButtons.add(new Label("Restore from Zip"));
		dumpButtons.add(restoreFileUpload);
		
	}
	
	private void doGetAllFile(final ReadAllFileListener allFileListener){
		gwtClipImages.getClipImageList().getAllFiles(new FileListListener() {
			@Override
			public void files(List<String> fileNames) {
				AsyncMultiCaller<String> getDataCaller=new AsyncMultiCaller<String>(fileNames) {
					@Override
					public void execAsync(final String data) {
						gwtClipImages.getClipImageList().read(data, new ReadListener<ImageClipData>() {

							@Override
							public void onError(String message) {
								allFileListener.error(message);
								done(data,false);
							}

							@Override
							public void onRead(ImageClipData clipdata) {
								done(data,true);
								allFileListener.read(clipdata);
								
							}
							
						});
					}
					
					public void doFinally(boolean cancelled){
						allFileListener.end();
					}
					
				};
				getDataCaller.startCall();
			}
			
		},null);
	}
	
	public static interface  ReadAllFileListener {
		public void error(String message);
		public void read(ImageClipData clipdata);
		public void end();
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
		cleanToTrashBt.setEnabled(true);
		deleteUnusedDatasBt.setEnabled(true);
	}
}
