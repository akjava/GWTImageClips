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
import com.akjava.gwt.lib.client.experimental.RectCanvasUtils;
import com.akjava.gwt.lib.client.widget.cell.EasyCellTableObjects;
import com.akjava.gwt.lib.client.widget.cell.HtmlColumn;
import com.akjava.gwt.lib.client.widget.cell.SimpleCellTable;
import com.akjava.lib.common.graphics.Rect;
import com.akjava.lib.common.io.FileType;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d.Composite;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
		FileIOUtils.getFileQuataAndUsage(false, new FileQuataAndUsageListener() {

			@Override
			public void storageInfoUsageCallback(double currentUsageInBytes, double currentQuotaInBytes) {
				LogUtils.log("temporaly:"+currentUsageInBytes+"/"+currentQuotaInBytes);
			}
			
		});
		
		mainPanel.add(new Label("Storage"));
		Button givemeMore=new Button("give me more 10MB",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				FileIOUtils.getFileQuataAndUsage(true, new FileQuataAndUsageListener() {
					@Override
					public void storageInfoUsageCallback(double currentUsageInBytes, double currentQuotaInBytes) {
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
							}
						});
					}
				});
				
			}
		});
		mainPanel.add(givemeMore);
		
		
		
		
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
		
		dumpButtons.add(new Label("Restore from Zip(warning not delete old data right now)"));
		dumpButtons.add(restoreFileUpload);
		
		
		
		Label openCvLabel=new Label("OpenCV tools");
		mainPanel.add(openCvLabel);
		
		HorizontalPanel openCvButtons=new HorizontalPanel();
		openCvButtons.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		mainPanel.add(openCvButtons);
		HorizontalPanel openCvButtons2=new HorizontalPanel();
		openCvButtons2.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
		mainPanel.add(openCvButtons2);
		
		
		
		final VerticalPanel openCvLinks=new VerticalPanel();
		mainPanel.add(openCvLinks);
		
		
		openCvButtons.add(new Label("export-image-type"));
		final ToStringValueListBox<String> imageType=new ToStringValueListBox<String>(Lists.newArrayList("jpg","png","webp"));
		openCvButtons.add(imageType);
		
		final CheckBox extractAll=new CheckBox("extract all(full image or clipped)");
		openCvButtons.add(extractAll);
		
		
		
		ExecuteButton openCvPosImages=new ExecuteButton("Extract Pos Datas",false){
			@Override
			public void executeOnClick() {
				final FileType fileType=FileType.getFileTypeByExtension(imageType.getValue());
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
						if(extractAll.getValue()){
						
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
							
							for(Rect r:clipdata.getRects()){
								String rinfo=r.toKanmaString().replace(",", "_");
								String fileName=clipdata.getId()+"_"+rinfo+"."+fileType.getExtension();
								
								//crop here
								zip.base64UrlFile(fileName, RectCanvasUtils.crop(image, r, sharedCanvas).toDataUrl(fileType.getMimeType()));
								String line=fileName+" 1 0 0 "+r.getWidth()+" "+r.getHeight();
								lines.add(line);
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
						
						dumpLinks.clear();
						Blob blob=zip.generateBlob(null);
						Anchor a=new HTML5Download().generateDownloadLink(blob,"application/zip","opencv-pos-images.zip","Download OpenCv pos-images",true);
						openCvLinks.add(a);
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
		 
		openCvButtons.add(openCvPosImages);
		
		ExecuteButton openCvPosImages2=new ExecuteButton("Export Both Datas as info-format",false){
			@Override
			public void executeOnClick() {
				final FileType fileType=FileType.getFileTypeByExtension(imageType.getValue());
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
						
						dumpLinks.clear();
						Blob blob=zip.generateBlob(null);
						Anchor a=new HTML5Download().generateDownloadLink(blob,"application/zip","opencv-pos-images.zip","Download OpenCv pos-images",true);
						openCvLinks.add(a);
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
		 
		openCvButtons2.add(openCvPosImages2);
		
		//only work not check extract all
		ExecuteButton openCvHPosImages=new ExecuteButton("Extract Pos-H-Flip Datas",false){
			@Override
			public void executeOnClick() {
				final FileType fileType=FileType.getFileTypeByExtension(imageType.getValue());
				
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
						if(extractAll.getValue()){
							//throw new RuntimeException("not support extract all mode");
						
						String fileName=clipdata.getId()+"."+fileType.getExtension();
						
						
						
						if(fileType!=FileType.WEBP){//stored image url is webp no need to convert
							imageUrl=ImageBuilder.from(imageUrl).on(fileType).toDataUrl();
						}
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
							
							for(Rect r:clipdata.getRects()){
								String rinfo=r.toKanmaString().replace(",", "_");
								String fileName=clipdata.getId()+"_"+rinfo+"h."+fileType.getExtension();
								
								//crop here
								String cropUrl= RectCanvasUtils.crop(image, r, sharedCanvas).toDataUrl();
								
								copyHorizontal(ImageElementUtils.create(cropUrl),sharedCanvas);
								
								zip.base64UrlFile(fileName,sharedCanvas.toDataUrl(fileType.getMimeType()));
								String line=fileName+" 1 0 0 "+r.getWidth()+" "+r.getHeight();
								lines.add(line);
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
						
						dumpLinks.clear();
						Blob blob=zip.generateBlob(null);
						Anchor a=new HTML5Download().generateDownloadLink(blob,"application/zip","opencv-pos-images.zip","Download OpenCv pos-images",true);
						openCvLinks.add(a);
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
		 
		openCvButtons.add(openCvHPosImages);
		
		final IntegerBox stepBox=new IntegerBox();
		stepBox.setValue(15);
		stepBox.setWidth("40px");
		
		openCvButtons.add(stepBox);
		
		ExecuteButton openCvPosTurnedImages=new ExecuteButton("Extract Turned-Pos Datas",false){
			boolean readed;
			int total;
			int stepAngle=5;
			@Override
			public void executeOnClick() {
				final FileType fileType=FileType.getFileTypeByExtension(imageType.getValue());
				
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
						
						double scale=0.9;
						//int stepAngle=5;
						int startAngle=-30;
						int endAngle=30;
						
						ImageElement image=ImageElementUtils.create(imageUrl);
							
							for(Rect r:clipdata.getRects()){
								CanvasUtils.createCanvas(sharedCanvas2, r.getWidth(), r.getHeight());
								for(int i=0;i<=(endAngle-startAngle)/stepAngle;i++){
									int angle=startAngle+stepAngle*i;
									if(angle==0){
										continue;
									}
									
									String rinfo=r.toKanmaString().replace(",", "_");
									String fileName=clipdata.getId()+"_"+rinfo+"_"+i+"."+fileType.getExtension();
									
									Canvas clip=RectCanvasUtils.crop(image, r.copy().expand(r.getWidth(), r.getHeight()), sharedCanvas);
									CanvasUtils.clear(sharedCanvas2);
									CanvasUtils.drawCenter(sharedCanvas2, clip.getCanvasElement(), 0, 0, scale, scale, angle, 1);
									
									
									zip.base64UrlFile(fileName, sharedCanvas2.toDataUrl(fileType.getMimeType()));
									String line=fileName+" 1 0 0 "+r.getWidth()+" "+r.getHeight();
									lines.add(line);
									
									
								}
								
							}
							
							
							
							readed=true;//for debug
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
						
						dumpLinks.clear();
						Blob blob=zip.generateBlob(null);
						Anchor a=new HTML5Download().generateDownloadLink(blob,"application/zip","opencv-pos-turned-images_step"+stepAngle+".zip","Download OpenCv pos-turned-images",true);
						openCvLinks.add(a);
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
		 
		openCvButtons.add(openCvPosTurnedImages);
		
		
		ExecuteButton openCvBgImages=new ExecuteButton("Extract Bg Datas",false){
			int imageIndex=0;
			int maxImage=20000;
			@Override
			public void executeOnClick() {
				final FileType fileType=FileType.getFileTypeByExtension(imageType.getValue());
				
				final Canvas imageCanvas=Canvas.createIfSupported();
				final int size=480;
				final Canvas clipCanvas=CanvasUtils.createCanvas(size, size);
				final JSZip zip=JSZip.newJSZip();
				
				final List<String> lines=new ArrayList<String>();
				
				doGetAllFile(new ReadAllFileListener() {
					@Override
					public void read(ImageClipData clipdata) {
						if(clipdata.getRects().size()==0 || imageIndex>=maxImage){
							return;
						}
						String imageUrl=clipdata.getImageData();
						
						List<Rect> expandRect=new ArrayList<Rect>();
						for(Rect r:clipdata.getRects()){
							expandRect.add(r.copy().expand(16, 16));//to avoid close area.how ever this remove important area too
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
						
						dumpLinks.clear();
						Blob blob=zip.generateBlob(null);
						Anchor a=new HTML5Download().generateDownloadLink(blob,"application/zip","opencv-bg-images.zip","Download OpenCv bg-images",true);
						openCvLinks.add(a);
						setEnabled(true);
					}

					@Override
					public boolean isCancelld() {
						return imageIndex>=maxImage;
					}
				});
				
			}
			
		};
		openCvButtons.add(openCvBgImages);
		
		final TextBox paintGgColorBox=new TextBox();
		paintGgColorBox.setText("#000");
		final CheckBox transparentCheck=new CheckBox("Transparent(save as png)");
		//final CheckBox exportAsPngCheck=new CheckBox("save as png");
		
		ExecuteButton openCvBgPaintImages=new ExecuteButton("Extract Paint Bg Datas",false){
			
			@Override
			public void executeOnClick() {

				final FileType fileType=transparentCheck.getValue()?FileType.PNG:FileType.getFileTypeByExtension(imageType.getValue());
				
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
						
						dumpLinks.clear();
						Blob blob=zip.generateBlob(null);
						Anchor a=new HTML5Download().generateDownloadLink(blob,"application/zip","opencv-paint-bg-images.zip","Download OpenCv paint-bg-images",true);
						openCvLinks.add(a);
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
		openCvButtons.add(openCvBgPaintImages);
		
		openCvButtons.add(paintGgColorBox);
		openCvButtons.add(transparentCheck);
		
		openCvButtons.add(new Label());
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
