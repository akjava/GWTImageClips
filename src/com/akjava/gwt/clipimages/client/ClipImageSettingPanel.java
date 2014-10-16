package com.akjava.gwt.clipimages.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.FileListListener;
import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.ReadListener;
import com.akjava.gwt.html5.client.download.HTML5Download;
import com.akjava.gwt.html5.client.file.Blob;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileIOUtils;
import com.akjava.gwt.html5.client.file.FileIOUtils.FileQuataAndUsageListener;
import com.akjava.gwt.html5.client.file.FileIOUtils.ReadStringCallback;
import com.akjava.gwt.html5.client.file.FileIOUtils.RemoveCallback;
import com.akjava.gwt.html5.client.file.FileIOUtils.RequestPersitentFileQuotaListener;
import com.akjava.gwt.html5.client.file.FileIOUtils.WriteCallback;
import com.akjava.gwt.html5.client.file.FileSystem;
import com.akjava.gwt.html5.client.file.FileUploadForm;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.html5.client.file.FileUtils.DataArrayListener;
import com.akjava.gwt.html5.client.file.FileUtils.DataURLListener;
import com.akjava.gwt.html5.client.file.Uint8Array;
import com.akjava.gwt.html5.client.file.webkit.FileEntry;
import com.akjava.gwt.jszip.client.JSZip;
import com.akjava.gwt.lib.client.CanvasUtils;
import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.gwt.lib.client.JavaScriptUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.ArrayTool;
import com.akjava.gwt.lib.client.experimental.AsyncMultiCaller;
import com.akjava.gwt.lib.client.experimental.ExecuteButton;
import com.akjava.gwt.lib.client.experimental.ImageBuilder;
import com.akjava.gwt.lib.client.experimental.ProgressCanvas;
import com.akjava.gwt.lib.client.experimental.RectCanvasUtils;
import com.akjava.gwt.lib.client.experimental.opencv.CVImageData;
import com.akjava.gwt.lib.client.experimental.opencv.CVImageDataConverter;
import com.akjava.gwt.lib.client.widget.cell.EasyCellTableObjects;
import com.akjava.gwt.lib.client.widget.cell.HtmlColumn;
import com.akjava.gwt.lib.client.widget.cell.SimpleCellTable;
import com.akjava.lib.common.graphics.Rect;
import com.akjava.lib.common.io.FileType;
import com.akjava.lib.common.utils.CSVUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d.Composite;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ClipImageSettingPanel extends DockLayoutPanel {
private GWTClipImages gwtClipImages;
private EasyCellTableObjects<ImageClipData> tableObjects;
private Button deleteUnusedDatasBt;
private Button dumpAllBt;
private Button cleanToTrashBt;
private FileUploadForm restoreFileUpload;

boolean extractAll=false;//TODO make checkboxs
private Button remakeIndex;
private ExecuteButton extractPosDatasBt;
private CheckBox posNormalCheck;
private CheckBox posHorizontalCheck;
private ToStringValueListBox<String> exportImageTypeBox;
private VerticalPanel dumpDownloadLinkPanel;


private String createStorageLabel(double usage,double max){
	int usageMega=(int) (usage/(1024*1024));
	int maxMega=(int) (max/1024/1024);
	return "Using "+usageMega +"Mbyte of "+maxMega+" Mbyte";
}

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
		ScrollPanel scroll=new ScrollPanel(mainPanel);
		add(scroll);
		mainPanel.setSpacing(8);
		
		Button closeBt=new Button("Close");
		closeBt.setWidth("400px");
		
		mainPanel.add(closeBt);
		
		closeBt.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onClose();
				gwtClipImages.showMainWidget();
			}
		});
		
		//test request temporaly
		/*
		FileIOUtils.getFileQuataAndUsage(false, new FileQuataAndUsageListener() {

			@Override
			public void storageInfoUsageCallback(double currentUsageInBytes, double currentQuotaInBytes) {
				LogUtils.log("temporaly-storage:"+currentUsageInBytes+"/"+currentQuotaInBytes);
			}
			
		});
		*/
		Label storage=new Label("Storage");
		mainPanel.add(storage);
		mainPanel.add(createStorageControlPanel());
		
		
		
		Label trashbox=new Label("Trashbox");
		mainPanel.add(trashbox);
		mainPanel.add(createTrashBoxPanel());
		
		
		Label delateAllLabel=new Label("Delte all data");
		mainPanel.add(delateAllLabel);
		mainPanel.add(createDeleteAllPanel());

		Label cleanUp=new Label("CleanUps");
		mainPanel.add(cleanUp);
		mainPanel.add(createCleanUpPanel());
		
		HorizontalPanel dumpRestorePanel=new HorizontalPanel();
		dumpRestorePanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		dumpRestorePanel.setSpacing(2);
		mainPanel.add(dumpRestorePanel);
		
		Label dumpLabel=new Label("Dump & Restore");
		dumpRestorePanel.add(dumpLabel);
		
		mainPanel.add(createDumpAndRestorePanel());
		
		Label rectLabel=new Label("Rects");
		mainPanel.add(rectLabel);
		
		mainPanel.add(createRectsPanel());
		
		dumpDownloadLinkPanel = new VerticalPanel();
		dumpRestorePanel.add(dumpDownloadLinkPanel);
		
		
		Label openCvLabel=new Label("OpenCV Tools");
		openCvLabel.setStylePrimaryName("large_title");
		mainPanel.add(openCvLabel);
		
		Label commonSettings=new Label("Common Settings");
		commonSettings.setStylePrimaryName("title");
		mainPanel.add(commonSettings);
		
		HorizontalPanel settings=new HorizontalPanel();
		settings.setSpacing(2);
		settings.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		mainPanel.add(settings);
		
		settings.add(new Label("export-image-type"));
		exportImageTypeBox = new ToStringValueListBox<String>(Lists.newArrayList("jpg","png","webp"));
		settings.add(exportImageTypeBox);
		
		
		
		
		HorizontalPanel exportPanel=new HorizontalPanel();
		exportPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		exportPanel.setSpacing(2);
		mainPanel.add(exportPanel);
		
		Label export=new Label("Export");
		export.setStylePrimaryName("title");
		exportPanel.add(export);
		
		opencvDownloadLinkPanel = new VerticalPanel();
		exportPanel.add(opencvDownloadLinkPanel);

		
		
		
	
		mainPanel.add(createExportPositiveDataPanel());
		mainPanel.add(createExportTurnedPositiveDataPanel());
		
		mainPanel.add(createExportSlashedNegativeDataPanel());
		mainPanel.add(createExportPaintNegativeData());
		
		mainPanel.add(createExportBothDataAsInfoFormatPanel());
		
		
	}

	private Panel createTrashBoxPanel(){
		VerticalPanel panel=new VerticalPanel();
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
		
		final Button recoverButton=new Button("Recover selection",new ClickHandler() {
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
		panel.add(table);
		panel.add(recoverButton);
		return panel;
	}
	private HorizontalPanel createDeleteAllPanel(){
		HorizontalPanel horizontalPanel=new HorizontalPanel();
		horizontalPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		horizontalPanel.setSpacing(2);
		
		Button clearAllButton=new Button("Delete all files on FileSystem",new ClickHandler() {
			
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
		horizontalPanel.add(clearAllButton);
		Label description=new Label("(can't recover it.Do dump first)");
		horizontalPanel.add(description);
		return horizontalPanel;
	}
	private HorizontalPanel createStorageControlPanel(){
		HorizontalPanel h1=new HorizontalPanel();
		h1.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		h1.setSpacing(2);
		
		
		storageLabel = new Label();
		h1.add(storageLabel);
		
		Button givemeMore=new Button("give me more 10MB",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				FileIOUtils.getFileQuataAndUsage(true, new FileQuataAndUsageListener() {
					@Override
					public void storageInfoUsageCallback(final double currentUsageInBytes, double currentQuotaInBytes) {
						FileIOUtils.requestPersitentFileQuota(currentQuotaInBytes+(1024*1024*10), new RequestPersitentFileQuotaListener() {
							
							@Override
							public void onError(String message, Object option) {
								LogUtils.log(message+","+option);
							}
							
							@Override
							public void onAccepted(FileSystem fileSystem, double acceptedSize) {
								LogUtils.log("accepted:"+","+acceptedSize);
								//updateStorageInfo();
								//TODO
								storageLabel.setText(createStorageLabel(currentUsageInBytes,acceptedSize));
							}
						});
					}
				});
				
			}
		});
		h1.add(givemeMore);
		
Button givemeMore100=new Button("give me more 100MB",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				FileIOUtils.getFileQuataAndUsage(true, new FileQuataAndUsageListener() {
					@Override
					public void storageInfoUsageCallback(final double currentUsageInBytes, double currentQuotaInBytes) {
						FileIOUtils.requestPersitentFileQuota(currentQuotaInBytes+(1024*1024*100), new RequestPersitentFileQuotaListener() {
							
							@Override
							public void onError(String message, Object option) {
								LogUtils.log(message+","+option);
							}
							
							@Override
							public void onAccepted(FileSystem fileSystem, double acceptedSize) {
								LogUtils.log("accepted:"+","+acceptedSize);
								//updateStorageInfo();
								//TODO
								storageLabel.setText(createStorageLabel(currentUsageInBytes,acceptedSize));
							}
						});
					}
				});
				
			}
		});
h1.add(givemeMore100);
return h1;
	}
	
	private HorizontalPanel createRectsPanel(){
		HorizontalPanel panel=new HorizontalPanel();
		panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		panel.setSpacing(2);
		
		
		ExecuteButton extractRectsBt = new ExecuteButton("Export Rects",false){
			@Override
			public void executeOnClick() {
				final List<String> lines=new ArrayList<String>();
				
				doGetAllFile(new ReadAllFileListener() {
					@Override
					public void read(ImageClipData clipdata) {
						String fileName=clipdata.getId();
						
						
						
						
						String line=fileName+" "+clipdata.getRects().size()+" ";
						List<String> rectTexts=new ArrayList<String>();
						for(Rect r:clipdata.getRects()){
							rectTexts.add(r.toKanmaString().replace(",", " "));
						}
						line+=Joiner.on(" ").join(rectTexts);
						lines.add(line);	
					}
					
					@Override
					public void error(String message) {
						LogUtils.log(message);
					}
					
					@Override
					public void end() {
						String text=Joiner.on("\r\n").join(lines);
						addDumpDownloadText(text, "clip-image-rects.txt","Download data-rects");
						
						setEnabled(true);
					}

					@Override
					public boolean isCancelld() {
						// TODO Auto-generated method stub
						return false;
					}
				});
				
			}
			
		};
		panel.add(extractRectsBt);
		final Label fileNameLabel=new Label();
		fileNameLabel.setStylePrimaryName("filename");
		fileNameLabel.setWidth("120px");
		FileUploadForm rectReplaceUpload=FileUtils.createSingleTextFileUploadForm(new DataURLListener() {
			@Override
			public void uploaded(File file, String text) {
				final String fileName=file.getFileName();
				List<String> lines=CSVUtils.splitLinesWithGuava(text);
				
				CVImageDataConverter converter=new CVImageDataConverter();
				List<CVImageData> datas= Lists.newArrayList(converter.convertAll(lines));
				
				final ProgressCanvas progress=new ProgressCanvas("Replacing rects", datas.size());
				progress.show();
				
				AsyncMultiCaller<CVImageData> replacer=new AsyncMultiCaller<CVImageData>(datas) {
					
					@Override
					public void execAsync(final CVImageData data) {
						progress.progress(1);
						//find
						Optional<ImageClipData> optional=gwtClipImages.findDataById(data.getFileName());
						if(optional.isPresent()){
							final ImageClipData clipData=optional.get();
							//TODO check same or not
							clipData.setRects(data.getRects());
							
							gwtClipImages.getClipImageList().read(clipData.getId(), new ReadListener<ImageClipData>() {
								@Override
								public void onError(String message) {
									LogUtils.log("update-faild on read:"+message);
									done(data,false);
								}

								@Override
								public void onRead(ImageClipData readData) {
									clipData.setImageData(readData.getImageData());//need image
									
									gwtClipImages.getClipImageList().updateAsync(clipData,new WriteCallback(){

										@Override
										public void onError(String message, Object option) {
											LogUtils.log("update-faild on update:"+message);
											done(data,false);
										}

										@Override
										public void onWriteEnd(FileEntry file) {
											gwtClipImages.generateImages(clipData);
											gwtClipImages.clearLargeImageDataFromMemory(clipData);
											done(data,true);
										}});
								}
							});
							
							
						}else{
							LogUtils.log("replace-rect not exist:"+data.getFileName());
							done(data,true);
							
						}	
					}
					
					@Override
					public void doFinally(boolean cancelled) {
						// TODO Auto-generated method stub
						progress.hide();
						gwtClipImages.listUpdate();
						
						fileNameLabel.setText(fileName);
					}
				};
				replacer.startCall();
				//do convert
				//find data and replace
				//log
			}
		}, true);
		rectReplaceUpload.setAccept(FileUploadForm.ACCEPT_TXT);
		
		HorizontalPanel replace=new HorizontalPanel();
		replace.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		replace.setSpacing(2);
		replace.setStylePrimaryName("thinBorder");
		panel.add(replace);
		
		Label replaceRectLabel=new Label("Replace Rects:");
		//replaceRectLabel.setStylePrimaryName("title");
		replace.add(replaceRectLabel);
		replace.add(rectReplaceUpload);
		replace.add(fileNameLabel);
		
		ExecuteButton clearRectBt=new ExecuteButton("Clear Rects",false) {
			
			@Override
			public void executeOnClick() {

				List<ImageClipData> datas= Lists.newArrayList(gwtClipImages.getClipImageList());
				
				final ProgressCanvas progress=new ProgressCanvas("Replacing rects", datas.size());
				progress.show();
				
				AsyncMultiCaller<ImageClipData> replacer=new AsyncMultiCaller<ImageClipData>(datas) {
					
					@Override
					public void execAsync(final ImageClipData clipData) {
						progress.progress(1);
						//find
						
							
							clipData.setRects(new ArrayList<Rect>());
							
							gwtClipImages.getClipImageList().read(clipData.getId(), new ReadListener<ImageClipData>() {
								@Override
								public void onError(String message) {
									LogUtils.log("update-faild on read:"+message);
									done(clipData,false);
								}

								@Override
								public void onRead(ImageClipData readData) {
									clipData.setImageData(readData.getImageData());//need image
									
									gwtClipImages.getClipImageList().updateAsync(clipData,new WriteCallback(){

										@Override
										public void onError(String message, Object option) {
											LogUtils.log("update-faild on update:"+message);
											done(clipData,false);
										}

										@Override
										public void onWriteEnd(FileEntry file) {
											gwtClipImages.generateImages(clipData);
											gwtClipImages.clearLargeImageDataFromMemory(clipData);
											done(clipData,true);
										}});
								}
							});
							
							
						
					}
					
					@Override
					public void doFinally(boolean cancelled) {
						// TODO Auto-generated method stub
						progress.hide();
						gwtClipImages.listUpdate();
						
						fileNameLabel.setText("");
						setEnabled(true);
					}
				};
				replacer.startCall();
			}
		};
		panel.add(clearRectBt);
		//TODO clear rect,almost same above and replace empty rects.
		
		return panel;	
	}
	
	private HorizontalPanel createDumpAndRestorePanel(){

		HorizontalPanel dumpButtons=new HorizontalPanel();
		dumpButtons.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		
		dumpButtons.setSpacing(2);
		
		dumpImages = new Button("Extract Image only",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				dumpImages.setEnabled(false);
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
						dumpDownloadLinkPanel.clear();
						Blob blob=zip.generateBlob(null);
						addDumpDownloadZip(blob,"clip-raw-images.zip","Download clip-raw-images");
						dumpImages.setEnabled(true);
					}

					@Override
					public boolean isCancelld() {
						// TODO Auto-generated method stub
						return false;
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
								
								final ProgressCanvas dump=new ProgressCanvas("Dumping", fileNames.size());
								dump.show();
								AsyncMultiCaller<String> caller=new AsyncMultiCaller<String>(fileNames) {
									@Override
									public void execAsync(final String data) {
										dump.progress(1);
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
										dumpDownloadLinkPanel.clear();
										Blob blob=zip.generateBlob(null);
										addDumpDownloadZip(blob,"clip-images.zip","Download clip-images");
										
										dumpAllBt.setEnabled(true);
										dump.hide();
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
					
					final ProgressCanvas progress=new ProgressCanvas("Writing", fileNameList.size());
					progress.show();
					
					AsyncMultiCaller<String> writeDatas=new AsyncMultiCaller<String>(fileNameList) {

						@Override
						public void doFinally(boolean cancelled) {
							restoreFileUpload.getFileUpload().setEnabled(true);
							progress.hide();
							gwtClipImages.getClipImageList().reReadAll();//this call progress too
							gwtClipImages.listUpdate();
						}

						@Override
						public void execAsync(final String data) {
							progress.progress(1);
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
		
		dumpButtons.add(new Label("Restore from Zip(warning not delete old data right now)"));
		dumpButtons.add(restoreFileUpload);
		
		return dumpButtons;
	}
	private HorizontalPanel createCleanUpPanel(){

		HorizontalPanel clPanel=new HorizontalPanel();
		clPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		clPanel.setSpacing(2);
		
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
		
		remakeIndex = new Button("remakeIndex",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				boolean confirm=Window.confirm("really remake index?");
				if(!confirm){
					return;
				}
				
				gwtClipImages.getClipImageList().remakeIndex();//TODO catch and clean
				
				
			}
		});
		clPanel.add(remakeIndex);
		
		
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
		return clPanel;
	}
	private Panel createExportSlashedNegativeDataPanel(){
		HorizontalPanel panel=new HorizontalPanel();
		panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		panel.setSpacing(2);
		
		final IntegerBox sizeBox=new IntegerBox();
		sizeBox.setWidth("60px");
		sizeBox.setValue(480);
		final IntegerBox expandBox=new IntegerBox();
		expandBox.setWidth("40px");
		expandBox.setValue(16);
		final IntegerBox maxItemBox=new IntegerBox();
		maxItemBox.setWidth("100px");
		maxItemBox.setValue(20000);

		ExecuteButton openCvBgImages=new ExecuteButton("Export Slashed Negative/Bg Datas",false){
			int imageIndex=0;
			//
			@Override
			public void executeOnClick() {
				final FileType fileType=FileType.getFileTypeByExtension(exportImageTypeBox.getValue());
				
				final Canvas imageCanvas=Canvas.createIfSupported();
				final int size=sizeBox.getValue();
				final int maxImage=maxItemBox.getValue();
				final int expand=expandBox.getValue();
				
				final Canvas clipCanvas=CanvasUtils.createCanvas(size, size);
				final JSZip zip=JSZip.newJSZip();
				
				final List<String> lines=new ArrayList<String>();
			
				
				//TODO validate
				doGetAllFile(new ReadAllFileListener() {
					@Override
					public void read(ImageClipData clipdata) {
						if(clipdata.getRects().size()==0 || imageIndex>=maxImage){
							return;
						}
						String imageUrl=clipdata.getImageData();
						
						List<Rect> expandRect=new ArrayList<Rect>();
						for(Rect r:clipdata.getRects()){
							expandRect.add(r.copy().expand(expand, expand));//to avoid close area.how ever this remove important area too
						}
						
						
						
						ImageElementUtils.copytoCanvas(imageUrl, imageCanvas);
						for(int y=0;y<imageCanvas.getCoordinateSpaceHeight()/size;y++){
							for(int x=0;x<imageCanvas.getCoordinateSpaceWidth()/size;x++){
							Rect clipRect=new Rect(x*size,y*size,size,size);
							boolean collisioned=false;
							for(Rect r:expandRect){
								if(clipRect.collision(r)){
									collisioned=true;
									break;
								}
							}
							if(!collisioned){
								CanvasUtils.clear(clipCanvas);
								//TODO generate method?
								clipCanvas.getContext2d().drawImage(imageCanvas.getCanvasElement(), -clipRect.getX(), -clipRect.getY());
								
								String fileName=clipdata.getId()+"_"+(x*size)+"_"+(y*size)+"_"+size+"_"+size+"."+fileType.getExtension();
								
								zip.base64UrlFile(fileName, clipCanvas.toDataUrl(fileType.getMimeType()));
								
								lines.add(fileName);
								
								imageIndex++;
								}
							}
						}
						
						
						
						
						
						
						
						
					}
					
					@Override
					public void error(String message) {
						LogUtils.log(message);
					}
					
					@Override
					public void end() {
						
						List<String> shuffled=new ArrayTool<String>().shuffle(lines);//make good bg
						zip.file("bg.txt", Joiner.on("\n").join(shuffled));
						
						Blob blob=zip.generateBlob(null);
						addOpenCvDownloadZip(blob, "opencv-bg-images.zip", "Download OpenCv bg-images");
						setEnabled(true);
					}

					@Override
					public boolean isCancelld() {
						return imageIndex>=maxImage;
					}
				});
				
			}
			
		};
		panel.add(openCvBgImages);
		panel.add(new Label("Size:"));
		panel.add(sizeBox);
		panel.add(new Label("Expand:"));
		panel.add(expandBox);
		panel.add(new Label("MaxItem:"));
		panel.add(maxItemBox);
		
		return panel;
		
	}

	private Panel createExportPaintNegativeData(){
		HorizontalPanel panel=new HorizontalPanel();
		panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		panel.setSpacing(2);
	

		final TextBox paintGgColorBox=new TextBox();
		paintGgColorBox.setWidth("60px");
		paintGgColorBox.setText("#000");
		final CheckBox transparentCheck=new CheckBox("Transparent(forced save as png)");
		transparentCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				paintGgColorBox.setEnabled(!event.getValue());
			}
		});
		//final CheckBox exportAsPngCheck=new CheckBox("save as png");
		
		ExecuteButton openCvBgPaintImages=new ExecuteButton("Extract Paint Bg Datas",false){
			
			@Override
			public void executeOnClick() {

				final FileType fileType=transparentCheck.getValue()?FileType.PNG:FileType.getFileTypeByExtension(exportImageTypeBox.getValue());
				
				final Canvas sharedCanvas=Canvas.createIfSupported();
				final JSZip zip=JSZip.newJSZip();
				
				final List<String> lines=new ArrayList<String>();
				
				doGetAllFile(new ReadAllFileListener() {
					@Override
					public void read(ImageClipData clipdata) {
						if(clipdata.getRects().size()==0){
							return;
						}
						
						
						
						String imageUrl=clipdata.getImageData();
						
						String fileName=clipdata.getId()+"."+fileType.getExtension();
						
						
						
						ImageElementUtils.copytoCanvas(imageUrl, sharedCanvas);
						
						
						
						sharedCanvas.getContext2d().save();
						if(transparentCheck.getValue()){
							sharedCanvas.getContext2d().setGlobalCompositeOperation(Composite.DESTINATION_OUT);
						}
						for(Rect r:clipdata.getRects()){
							RectCanvasUtils.fill(r,sharedCanvas,paintGgColorBox.getValue());
						}
						sharedCanvas.getContext2d().restore();
						zip.base64UrlFile(fileName, sharedCanvas.toDataUrl(fileType.getMimeType()));
						
						lines.add(fileName);
						
					}
					
					@Override
					public void error(String message) {
						LogUtils.log(message);
					}
					
					@Override
					public void end() {
						zip.file("bg.txt", Joiner.on("\n").join(lines));
						Blob blob=zip.generateBlob(null);
						addOpenCvDownloadZip(blob, "opencv-paint-bg-images.zip","Download OpenCv paint-bg-images");
						setEnabled(true);
					}

					@Override
					public boolean isCancelld() {
						// TODO Auto-generated method stub
						return false;
					}
				});
			}
		};
		panel.add(openCvBgPaintImages);
		
		panel.add(new Label("PaintColor"));
		
		panel.add(paintGgColorBox);
		panel.add(transparentCheck);
		
		panel.add(new Label());//?
		return panel;
	}
		
		
	private Panel createExportBothDataAsInfoFormatPanel(){
		HorizontalPanel panel=new HorizontalPanel();
		panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		panel.setSpacing(2);
		

		
		ExecuteButton openCvPosImages2=new ExecuteButton("Export Both Datas as info-format",false){
			@Override
			public void executeOnClick() {
				final FileType fileType=FileType.getFileTypeByExtension(exportImageTypeBox.getValue());
				final JSZip zip=JSZip.newJSZip();
				
				final List<String> lines=new ArrayList<String>();
				
				doGetAllFile(new ReadAllFileListener() {
					@Override
					public void read(ImageClipData clipdata) {
						if(clipdata.getRects().size()==0){
							//return;
						}
						
						
						
						String imageUrl=clipdata.getImageData();
						
						String fileName=clipdata.getId()+"."+fileType.getExtension();
						
						
						
						
						imageUrl=ImageBuilder.from(imageUrl).on(fileType).toDataUrl();//webp not support and png too big
						zip.base64UrlFile(fileName, imageUrl);
						
						String line=fileName+" "+clipdata.getRects().size();
						
						if(clipdata.getRects().size()>0){
							line+=" ";
						}
						
						List<String> rectTexts=new ArrayList<String>();
						for(Rect r:clipdata.getRects()){
							rectTexts.add(r.toKanmaString().replace(",", " "));
						}
						line+=Joiner.on(" ").join(rectTexts);
						
						lines.add(line);
						
					}
					
					@Override
					public void error(String message) {
						LogUtils.log(message);
					}
					
					@Override
					public void end() {
						zip.file("info.txt", Joiner.on("\n").join(lines));
						Blob blob=zip.generateBlob(null);
						addOpenCvDownloadZip(blob, "opencv-posneg-images.zip","Download OpenCv posneg-images");
						
						setEnabled(true);
					}

					@Override
					public boolean isCancelld() {
						// TODO Auto-generated method stub
						return false;
					}
				});
				
			}
			
		};
		 
		panel.add(openCvPosImages2);
		panel.add(new Label("add negative images as empty-rect(for test-rect)"));
		return panel;
		
	}
	private Panel createExportPositiveDataPanel(){
		HorizontalPanel openCvButtons1=new HorizontalPanel();
		openCvButtons1.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		openCvButtons1.setSpacing(2);
		

		posNormalCheck = new CheckBox("Normal");
		posNormalCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				updatePositiveExtractButton();
			}
			
		});
		posNormalCheck.setValue(true);
		
		posHorizontalCheck = new CheckBox("Flip Horizontal");
		posHorizontalCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				updatePositiveExtractButton();
			}
			
		});
		final CheckBox extractAllCheck=new CheckBox("export with uploaded image(not rect-clipped)");
		extractAllCheck.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				boolean checked=event.getValue();
				//only works clipped
				posNormalCheck.setEnabled(!checked);
				posHorizontalCheck.setEnabled(!checked);
			}
		});
		
		
		extractPosDatasBt = new ExecuteButton("Export Positive Datas",false){
			@Override
			public void executeOnClick() {
				final FileType fileType=FileType.getFileTypeByExtension(exportImageTypeBox.getValue());
				final Canvas sharedCanvas=Canvas.createIfSupported();
				final JSZip zip=JSZip.newJSZip();
				
				final List<String> lines=new ArrayList<String>();
				
				doGetAllFile(new ReadAllFileListener() {
					@Override
					public void read(ImageClipData clipdata) {
						if(clipdata.getRects().size()==0){
							return;
						}
						
						
						
						String imageUrl=clipdata.getImageData();
						if(extractAllCheck.getValue()){
						
						String fileName=clipdata.getId()+"."+fileType.getExtension();
						
						
						
						
						imageUrl=ImageBuilder.from(imageUrl).on(fileType).toDataUrl();//webp not support and png too big
						zip.base64UrlFile(fileName, imageUrl);
						
						String line=fileName+" "+clipdata.getRects().size()+" ";
						List<String> rectTexts=new ArrayList<String>();
						for(Rect r:clipdata.getRects()){
							rectTexts.add(r.toKanmaString().replace(",", " "));
						}
						line+=Joiner.on(" ").join(rectTexts);
						lines.add(line);
						}else{
							ImageElement image=ImageElementUtils.create(imageUrl);
							
							boolean useNormal=posNormalCheck.getValue();
							boolean useHorizontal=posHorizontalCheck.getValue();
							//TODO vertical
							for(Rect r:clipdata.getRects()){
								String rectInfo=r.toKanmaString().replace(",", "_");
								if(useNormal){
								
								String fileName=clipdata.getId()+"_"+rectInfo+"."+fileType.getExtension();
								
								//crop here
								zip.base64UrlFile(fileName, RectCanvasUtils.crop(image, r, sharedCanvas).toDataUrl(fileType.getMimeType()));
								String line=fileName+" 1 0 0 "+r.getWidth()+" "+r.getHeight();
								lines.add(line);
								}
								
								if(useHorizontal){
									
									String fileName=clipdata.getId()+"_"+rectInfo+"_h."+fileType.getExtension();
									
									//crop here
									String cropUrl= RectCanvasUtils.crop(image, r, sharedCanvas).toDataUrl();
									
									copyHorizontal(ImageElementUtils.create(cropUrl),sharedCanvas);
									
									zip.base64UrlFile(fileName,sharedCanvas.toDataUrl(fileType.getMimeType()));
									String line=fileName+" 1 0 0 "+r.getWidth()+" "+r.getHeight();
									lines.add(line);
								}
								
								
							}
							
							
							
							
							
						}
					}
					
					@Override
					public void error(String message) {
						LogUtils.log(message);
					}
					
					@Override
					public void end() {
						zip.file("info.txt", Joiner.on("\n").join(lines));
						Blob blob=zip.generateBlob(null);
						
						addOpenCvDownloadZip(blob, "opencv-pos-images.zip","Download OpenCv pos-images");
						
						setEnabled(true);
					}

					@Override
					public boolean isCancelld() {
						// TODO Auto-generated method stub
						return false;
					}
				});
				
			}
			
		};
		 
		openCvButtons1.add(extractPosDatasBt);
		openCvButtons1.add(extractAllCheck);
		openCvButtons1.add(posNormalCheck);
		openCvButtons1.add(posHorizontalCheck);
		
		return openCvButtons1;
	}
	private HorizontalPanel createExportTurnedPositiveDataPanel(){
		HorizontalPanel panel=new HorizontalPanel();
		panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		panel.setSpacing(2);
		
		final IntegerBox startBox=new IntegerBox();
		startBox.setValue(0);
		startBox.setWidth("40px");
		
		final IntegerBox endBox=new IntegerBox();
		endBox.setValue(360);
		endBox.setWidth("40px");
		
		final IntegerBox stepBox=new IntegerBox();
		stepBox.setValue(15);
		stepBox.setWidth("40px");
		
		
		
		ExecuteButton openCvPosTurnedImages=new ExecuteButton("Extract Turned-Positive Datas",false){
			//boolean readed;
			int total;
			int stepAngle=5;
			int startAngle=0;
			int endAngle=360;
			@Override
			public void executeOnClick() {
				final FileType fileType=FileType.getFileTypeByExtension(exportImageTypeBox.getValue());
				
				startAngle=startBox.getValue();
				endAngle=endBox.getValue();
				stepAngle=stepBox.getValue();
				
				final Canvas sharedCanvas=Canvas.createIfSupported();
				final Canvas sharedCanvas2=Canvas.createIfSupported();
				final JSZip zip=JSZip.newJSZip();
				
				final List<String> lines=new ArrayList<String>();
				
				doGetAllFile(new ReadAllFileListener() {
					@Override
					public void read(ImageClipData clipdata) {
						if(clipdata.getRects().size()==0){
							return;
						}
						String imageUrl=clipdata.getImageData();
						
						double scale=1;//need?
						//int stepAngle=5;
						
						
						ImageElement image=ImageElementUtils.create(imageUrl);
							
							for(Rect r:clipdata.getRects()){
								CanvasUtils.createCanvas(sharedCanvas2, r.getWidth(), r.getHeight());
								for(int i=0;i<(endAngle-startAngle)/stepAngle;i++){
									int angle=startAngle+stepAngle*i;
									//contain zero
									
									String rinfo=r.toKanmaString().replace(",", "_");
									String fileName=clipdata.getId()+"_"+rinfo+"_"+i+"."+fileType.getExtension();
									
									//TODO find more better way
									Canvas clip=RectCanvasUtils.crop(image, r.copy().expand(r.getWidth()*2, r.getHeight()*2), sharedCanvas);
									CanvasUtils.clear(sharedCanvas2);
									CanvasUtils.drawCenter(sharedCanvas2, clip.getCanvasElement(), 0, 0, scale, scale, angle, 1);
									
									
									zip.base64UrlFile(fileName, sharedCanvas2.toDataUrl(fileType.getMimeType()));
									String line=fileName+" 1 0 0 "+r.getWidth()+" "+r.getHeight();
									lines.add(line);
									
									//TODO support flip-horizontal
									
								}
								
							}
							
							
							
							//readed=true;//for debug
							total++;
							LogUtils.log("zipped:"+total);
						
					}
					
					@Override
					public void error(String message) {
						LogUtils.log(message);
					}
					
					@Override
					public void end() {
						LogUtils.log("generating-zip");
						zip.file("info.txt", Joiner.on("\n").join(lines));
						
						
						addOpenCvDownloadZip(zip.generateBlob(null),"opencv-pos-turned-images_step"+stepAngle+".zip","Download OpenCv pos-turned-images");
						
						setEnabled(true);
					}

					@Override
					public boolean isCancelld() {
						//return readed;
						return false;
					}
				});
				
			}
			
		};
		
		panel.add(openCvPosTurnedImages);
		panel.add(new Label("Start:"));
		panel.add(startBox);
		panel.add(new Label("End:"));
		panel.add(endBox);
		panel.add(new Label("Step:"));
		panel.add(stepBox);
		
		return panel;
	}
	
	public void updateStorageInfo(){
		FileIOUtils.getFileQuataAndUsage(true, new FileQuataAndUsageListener() {
			@Override
			public void storageInfoUsageCallback(final double currentUsageInBytes, double currentQuotaInBytes) {
				storageLabel.setText(createStorageLabel(currentUsageInBytes,currentQuotaInBytes));
			}
		});
	}
	
	private Blob keepBlob;
	private Label storageLabel;
	private VerticalPanel opencvDownloadLinkPanel;
	private Button dumpImages;
	
	
	private void addOpenCvDownloadZip(Blob blob,String fileName,String downloadLabel){
		dumpDownloadLinkPanel.clear();
		opencvDownloadLinkPanel.clear();
		keepBlob=blob;
		Anchor a=new HTML5Download().generateDownloadLink(keepBlob,"application/zip",fileName,downloadLabel,true);
		opencvDownloadLinkPanel.add(a);
	}
	
	private void addDumpDownloadText(String text,String fileName,String downloadLabel){
		dumpDownloadLinkPanel.clear();
		opencvDownloadLinkPanel.clear();
		
		keepBlob=Blob.createBlob(text);
		Anchor a=new HTML5Download().generateDownloadLink(keepBlob, "text/plain", fileName, downloadLabel);
		dumpDownloadLinkPanel.add(a);
	}
	
	private void addDumpDownloadZip(Blob blob,String fileName,String downloadLabel){
		dumpDownloadLinkPanel.clear();
		opencvDownloadLinkPanel.clear();
		keepBlob=blob;
		Anchor a=new HTML5Download().generateDownloadLink(keepBlob,"application/zip",fileName,downloadLabel,true);
		dumpDownloadLinkPanel.add(a);
	}
	
	protected void updatePositiveExtractButton() {
		if(posNormalCheck.getValue()==false && posHorizontalCheck.getValue()==false){
			extractPosDatasBt.setEnabled(false);
		}else{
			extractPosDatasBt.setEnabled(true);
		}
	}

	//TODO move
	public Canvas copyHorizontal(Canvas src,Canvas dest){
		if(dest==null){
			dest=Canvas.createIfSupported();
		}
		
		CanvasUtils.copyTo(src, dest, false);
		
		dest.getContext2d().save();
		
		dest.getContext2d().translate(src.getCoordinateSpaceWidth(), 0); //flip horizontal
		dest.getContext2d().scale(-1, 1);
		dest.getContext2d().drawImage(src.getCanvasElement(), 0, 0);
		dest.getContext2d().restore();
		return dest;
	}
	public Canvas copyHorizontal(ImageElement src,Canvas dest){
		if(dest==null){
			dest=Canvas.createIfSupported();
		}
		
		CanvasUtils.createCanvas(dest, src.getWidth(), src.getHeight());
		
		dest.getContext2d().save();
		
		dest.getContext2d().translate(src.getWidth(), 0); //flip horizontal
		dest.getContext2d().scale(-1, 1);
		dest.getContext2d().drawImage(src, 0, 0);
		dest.getContext2d().restore();
		return dest;
	}
	
	public class ToStringValueListBox<T> extends ValueListBox<T>{
		public ToStringValueListBox(List<T> values) {
			this();
			setValue(values.get(0));
			setAcceptableValues(values);
		}
		public ToStringValueListBox() {
			super(new Renderer<T>(){

				@Override
				public String render(T object) {
					if(object==null){
						return null;
					}
					return object.toString();
				}

				@Override
				public void render(T object, Appendable appendable) throws IOException {
					
				}
				
			});
			// TODO Auto-generated constructor stub
		}
		
	}
	
	
	private void doGetAllFile(final ReadAllFileListener allFileListener){
		gwtClipImages.getClipImageList().getAllFiles(new FileListListener() {
			@Override
			public void files(List<String> fileNames) {
				AsyncMultiCaller<String> getDataCaller=new AsyncMultiCaller<String>(fileNames) {
					@Override
					public void execAsync(final String data) {
						if(allFileListener.isCancelld()){
							setCancelled(true);
							done(data,false);
							return;
						}
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
		public boolean isCancelld();
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
