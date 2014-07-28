package com.akjava.gwt.clipimages.client;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.akjava.gwt.clipimages.client.ImageBuilder.WebPBuilder;
import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.DoneDeleteListener;
import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.ReadListener;
import com.akjava.gwt.clipimages.client.custom.SimpleCellListResources;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileIOUtils;
import com.akjava.gwt.html5.client.file.FileIOUtils.FileQuataAndUsageListener;
import com.akjava.gwt.html5.client.file.FileIOUtils.RemoveCallback;
import com.akjava.gwt.html5.client.file.FileIOUtils.RequestPersitentFileQuotaListener;
import com.akjava.gwt.html5.client.file.FileHandler;
import com.akjava.gwt.html5.client.file.FileReader;
import com.akjava.gwt.html5.client.file.FileSystem;
import com.akjava.gwt.html5.client.file.FileUploadForm;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.html5.client.file.FileUtils.DataURLListener;
import com.akjava.gwt.html5.client.file.ui.DropDockDataUrlRootPanel;
import com.akjava.gwt.html5.client.file.webkit.FileEntry;
import com.akjava.gwt.html5.client.file.webkit.FilePathCallback;
import com.akjava.gwt.lib.client.CanvasUtils;
import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.AsyncMultiCaller;
import com.akjava.gwt.lib.client.widget.PanelUtils;
import com.akjava.gwt.lib.client.widget.cell.SimpleContextMenu;
import com.akjava.lib.common.graphics.Rect;
import com.google.common.base.Converter;
import com.google.common.base.Optional;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.EditorDelegate;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.editor.client.ValueAwareEditor;
import com.google.gwt.editor.client.adapters.SimpleEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;


public class GWTClipImages implements EntryPoint {

	
	
	private String imageUrl;
	
	private Canvas sharedCanvas;
	private Map<String,String> imageMap=new HashMap<String,String>();
	
	private AreaSelectionControler areaSelectionControler;

	private SingleSelectionModel<ImageClipData> selectionModel;

	//private CellList<ImageClipData> cellList;

	//private SingleSelectionModel<ImageClipData> selectionModel;
	
	
	private PreviewHtmlPanelControler previewControler;


	private List<ImageClipData> rawList=new ArrayList<ImageClipData>();
	
	private FileEntry currentDroppingFileEntry;
	private AsyncMultiCaller<FileEntry> dropAnddAddCaller;
	public Canvas getSharedCanvas(){
		if(sharedCanvas==null){
			sharedCanvas=Canvas.createIfSupported();
		}
		return sharedCanvas;
	}
	
	private void addImageDataOnly(String dataUrl) {
		ImageClipData data=new ImageClipData();
		data.setImageData(dataUrl);
		add(data);
	}
	
	public void onModuleLoad() {
		previewControler=new PreviewHtmlPanelControler();
		Button showBt=new Button("Edit",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				editSelection();
			}
		});
		previewControler.show();//for generate container
		previewControler.hide();
		previewControler.getContainer().add(showBt);
		
		
		rootDeck = new DeckLayoutPanel();
		
		RootLayoutPanel.get().add(rootDeck);
		
		
		
		
		areaSelectionControler=new AreaSelectionControler();
		
		
		 //title,rect,image
		
		DockLayoutPanel mainRoot=new DropDockDataUrlRootPanel() {
			
			@Override
			public void loadFile(String pareht, Optional<File> optional, String dataUrl) {
				//never called
			}
			/*
			long lastModified;
			List<String> dataUrlList=new ArrayList<String>();
			@Override
			public void loadFile(String pareht, Optional<File> optional, String dataUrl) {
				dataUrlList.add(dataUrl);
				lastModified=System.currentTimeMillis();
				Timer timer=new Timer(){
					@Override
					public void run() {
						if(lastModified<System.currentTimeMillis()-1000){
							LogUtils.log("maybe-last-data-added:"+dataUrlList.size());//but this consume,too much memory
						}
					}
				};
				timer.schedule(1000);
			}
			*/
			@Override
			public  void onDropFiles(List<FileEntry> files){
				
					dropAnddAddCaller = new AsyncMultiCaller<FileEntry>(files) {
						@Override
						public void execAsync(final FileEntry data) {
							data.file(new FilePathCallback() {
								
								@Override
								public void callback(File file, String parent) {
									if(file==null){
										return;
									}
									if(getFilePredicate()!=null && !getFilePredicate().apply(file)){
										return;
									}
									
								
									
									final FileReader reader = FileReader.createFileReader();
									reader.setOnLoad(new FileHandler() {
										@Override
										public void onLoad() {
											String dataUrl=reader.getResultAsString();
											currentDroppingFileEntry=data;
											addImageDataOnly(dataUrl);
											
										}
									});
									
									if(file!=null){
										reader.readAsDataURL(file);
									}
								}
							}, null);
						}
						@Override
						public void doFinally(boolean cancelled){
							dropAnddAddCaller=null;
						}
					};
					dropAnddAddCaller.startCall();
				
			}
			
		};
		rootDeck.add(mainRoot);
		
		VerticalPanel topPanel=new VerticalPanel();
		topPanel.setSpacing(16);
		mainRoot.addNorth(topPanel, 48);
		topPanel.setSize("100%", "100%");
		topPanel.getElement().getStyle().setBackgroundColor("#607d8b");
		
		rootDeck.showWidget(0);
		
		
		HorizontalPanel panel=new HorizontalPanel();
		panel.setWidth("100%");
		topPanel.add(panel);
		
		Label appLabel=new Label("ImageClip");
		appLabel.getElement().getStyle().setColor("#fff");
		panel.add(appLabel);
		
		HorizontalPanel rightPanel=new HorizontalPanel();
		panel.add(rightPanel);
		rightPanel.setWidth("100%");
		rightPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		
		Anchor setting=new Anchor("Settings");
		setting.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				showSettingWidget();
			}
		});
		rightPanel.add(setting);
		
		
		
		/*
		Button testBt=new Button("add",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				areaSelectionControler.getSelectionRect().clear();
				areaSelectionControler.getCanvas().setVisible(false);
				
				areaSelectionControler.updateRect();
				
				rootDeck.showWidget(1);
			}
		});
		root.add(testBt);
		*/
		
		HorizontalPanel inputPanel=new HorizontalPanel();
		inputPanel.setSpacing(8);
		
		FileUploadForm fileUpload=FileUtils.createSingleFileUploadForm(new DataURLListener() {
			@Override
			public void uploaded(File file, String text) {
				text=WebPBuilder.from(text).toDataUrl();
				areaSelectionControler.getSelectionRect().clear();
				editor.updateImageclipData(text);
				setCanvasImage(text);//convert from PNG to WEBP here
				
				areaSelectionControler.updateRect();
				rootDeck.showWidget(1);
			}
		}, true);
		inputPanel.add(fileUpload);
		
		
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
							}
						});
					}
				});
				
			}
		});
		inputPanel.add(givemeMore);
		
		Button cleanUp=new Button("clean up",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
				clipImageList.deleteUnusedFiles();//TODO catch and clean
				
				AsyncMultiCaller<String> caller=new AsyncMultiCaller<String>(Lists.newArrayList(faildList)){

					@Override
					public void execAsync(final String data) {
					
						
						clipImageList.getFileSystem().removeData(data, new RemoveCallback() {
							
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
		inputPanel.add(cleanUp);
		cleanUp.setEnabled(false);
		
		
		editor = new ImageClipDataEditor(areaSelectionControler.getCanvas());
		driver.initialize(editor);
		
		//VerticalPanel editorPanel=new VerticalPanel();
		driver.edit(new ImageClipData());
		rootDeck.add(editor);
		//editorPanel.add(editor);
		//root.add(editorPanel);
		
		
		
		

		newBt = new Button("New",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				unselect();
			}
		});
		newBt.setEnabled(false);
		
		
	    	addBt = new Button("Add",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				editor.updateRect(areaSelectionControler.getSelectionRect());
				ImageClipData addData=driver.flush();
				
				
				
				add(addData);
			}
		});
	    	
	    	updateBt = new Button("Update",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					
					
					editor.updateRect(areaSelectionControler.getSelectionRect());
					ImageClipData data=driver.flush();
					
					clearImageCashes(data);//remove old data
					
					generateImages(data);
					
					
					
					clipImageList.updateAsync(data);//update no need update index
					//listUpdate(); //unselect call update
					unselect();
				}
			});
	    	updateBt.setEnabled(false);
	    	
	    	removeBt = new Button("Remove",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					clearImageCashes(getSelection());//remove old data
					
					clipImageList.read(getSelection().getId(), new ReadListener<ImageClipData>() {

						@Override
						public void onError(String message) {
							LogUtils.log("maybe invalid json:"+message);
							
							clipImageList.remove(getSelection());//call inside
							
							driver.edit(new ImageClipData());//clear
							unselect();
							listUpdate();
							rootDeck.showWidget(0);//add case,need this
						}

						@Override
						public void onRead(ImageClipData data) {
							settingPanel.addTrashBox(data);
							
							clipImageList.remove(getSelection());//call inside
							
							driver.edit(new ImageClipData());//clear
							unselect();
							listUpdate();
							rootDeck.showWidget(0);//add case,need this
						}
						
					});
					
					
					
				}
			});
	    	removeBt.setEnabled(false);
	    	
	    	cancelBt = new Button("Cancel",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					unselect();
					
					rootDeck.showWidget(0);//add case,need this
				}
			});
		
		

    	AbstractMoveableCell<ImageClipData> customCell=new AbstractMoveableCell<ImageClipData>(){

			@Override
			public ImageClipData getSelectedObject() {
				return getSelection();
			}

			@Override
			public List<ImageClipData> getList() {
				// TODO Auto-generated method stub
				return clipImageList.getRawList();
			}

			@Override
			public void updateList() {
				listUpdate();
				clipImageList.updateIndexAsync();
			}

			@Override
			public void onSetCellContextMenu(SimpleContextMenu contextMenu) {
				//for add more menu
			}

			@Override
			public void onDoubleClick(int clientX, int clientY) {
				showSelectionImage();
				
			}

			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, ImageClipData object, SafeHtmlBuilder sb) {
				renderHtml(object,sb,"item");
				
			}
    		
    	};
    	

	    	
    	
    	
    	VerticalPanel mainPanel=PanelUtils.createScrolledVerticalPanel(mainRoot,99);
		mainPanel.setWidth("100%");
		
		
		
		mainPanel.getElement().getStyle().setBackgroundColor("#e8e8e8");
		mainPanel.setSpacing(16);
    	
		mainPanel.add(inputPanel);
		
    	
    	cellList = new CellList<ImageClipData>(customCell,GWT.<SimpleCellListResources> create(SimpleCellListResources.class));
    	mainPanel.add(cellList);
		
		
		selectionModel=new SingleSelectionModel<ImageClipData>();
		selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				ImageClipData selection=selectionModel.getSelectedObject();
				
				if(selection!=null){
					SafeHtmlBuilder builder=new SafeHtmlBuilder();
					renderHtml(selection, builder,"preview");
					previewControler.setPreviewHtml(builder.toSafeHtml());
				}else{
					previewControler.hide();
					
					driver.edit(new ImageClipData());
					addBt.setEnabled(true);
					updateBt.setEnabled(false);
					removeBt.setEnabled(false);
					newBt.setEnabled(false);
					rootDeck.showWidget(0);//TODO future only double click
				}
			}
		});
		
		cellList.setSelectionModel(selectionModel);
		
		/*
		updateCellListList = new CellListControlList<ImageClipData>(new ArrayList<ImageClipData>(),cellList) {
			@Override
			public void onSelect(ImageClipData selection) {
				if(selection!=null){
					//do nothing
					
				}else{
					
					driver.edit(new ImageClipData());
					addBt.setEnabled(true);
					updateBt.setEnabled(false);
					removeBt.setEnabled(false);
					newBt.setEnabled(false);
					rootDeck.showWidget(0);//TODO future only double click
				}
				
			}
		};
		*/
		
		onAddMakeImageList = new ForwardingList<ImageClipData>(){
			@Override
			public void add(int index, ImageClipData element) {
				
				//clear image after async-write-finished
				super.add(index, element);
				listUpdate();//for cell-update on initialize
			}

			@Override
			public boolean add(ImageClipData element) {
				//add is only called when initial read,so clear image on here
				generateImages(element);
				clearLargeImageDataFromMemory(element);
				boolean b= super.add(element);
				
				
				listUpdate();//for cell-update on initialize
				
				return b;
			}

			@Override
			protected List<ImageClipData> delegate() {
				return rawList;
			}};
		
		clipImageList = new ClipImageList("clipimage",onAddMakeImageList,new ImageClipDataConverter());
		
		
	    	
	    	HorizontalPanel buttons=new HorizontalPanel();
	    	
	    	editor.getControler().add(buttons);
	    	
	    	buttons.add(newBt);
	    	buttons.add(addBt);
	    	buttons.add(updateBt);
	    	buttons.add(removeBt);
	    	buttons.add(cancelBt);
	    	
	    	//buttons.getElement().getStyle().setCursor(value);//UIObjects?
	    	/*
	    	SimpleCellTable<ImageClipData> table=new SimpleCellTable<ImageClipData>() {

				@Override
				public void addColumns(CellTable<ImageClipData> table) {
					table.addColumn(new TextColumn<ImageClipData>() {

						@Override
						public String getValue(ImageClipData object) {
							// TODO Auto-generated method stub
							return object.getId();
						}
					},"id");
					table.addColumn(new TextColumn<ImageClipData>() {

						@Override
						public String getValue(ImageClipData object) {
							// TODO Auto-generated method stub
							return object.getTitle();
						}
					},"title");
					
					HtmlColumn<ImageClipData> imageColumn=new HtmlColumn<ImageClipData>() {
						@Override
						public String toHtml(ImageClipData object) {
							if(object.getRect().hasWidthAndHeight() && object.getImageData()!=null){
								String key=object.getImageData()+object.getRect().toKanmaString();
								String url=imageMap.get(key);
								if(url==null){
								LogUtils.log("toHtml:"+object.getId());
								ImageElement imageElement = ImageElementUtils.create(object.getImageData());
								
								
								Canvas canvas=CanvasUtils.createCanvas(getSharedCanvas(),object.getRect().getWidth(), object.getRect().getHeight());
								canvas.getContext2d().drawImage(imageElement, -object.getRect().getX(), -object.getRect().getY());
								
								//ImageData clipData=canvas.getContext2d().getImageData(0, 0, object.getRect().getWidth(), object.getRect().getHeight());
								
								ImageElement clipImage=ImageElementUtils.create(canvas.toDataUrl());
								canvas=CanvasUtils.createCanvas(getSharedCanvas(),320,180);
								CanvasUtils.clear(canvas);
								CanvasUtils.drawExpandCenter(canvas, clipImage);
								
								
								
								Rect baseRect=new Rect(0,0,320,180).expand(-20, -20).rightBottom(90);
								
								canvas.getContext2d().setFillStyle("#f00");
								//LogUtils.log(baseRect);
								
								//TODO support fillRect
								//canvas.getContext2d().fillRect(baseRect.getX(), baseRect.getY(),baseRect.getWidth(),baseRect.getHeight());
								
								CanvasUtils.drawFitImage(canvas, imageElement, baseRect, CanvasUtils.ALIGN_RIGHT, CanvasUtils.VALIGN_BOTTOM);
								
								url=canvas.toDataUrl();
								imageMap.put(key, url);
								}
								return "<img src='"+url+"'>";
							}else{
								return "";
							}
							
						}
					};
					table.addColumn(imageColumn,"image");//testly
				}
			};
			
	    	
	    cellObjects = new EasyCellTableObjects<ImageClipData>(table) {
			@Override
			public void onSelect(ImageClipData selection) {
				if(selection==null){
				driver.edit(new ImageClipData());
				addBt.setEnabled(true);
				updateBt.setEnabled(false);
				removeBt.setEnabled(false);
				newBt.setEnabled(false);
				rootDeck.showWidget(0);//TODO future only double click
				}else{
				driver.edit(selection);
				addBt.setEnabled(false);
				updateBt.setEnabled(true);
				removeBt.setEnabled(true);
				newBt.setEnabled(true);
				rootDeck.showWidget(1);//TODO future only double click
				}
			}
		};
		cellObjects.setDatas(testList);
		root.add(table);
		*/
	    	
		
		
		//TODO initial load form system
		
		//TODO implement initial read
		
		
		
		HorizontalPanel buttons2=new HorizontalPanel();
		//root.add(buttons2);
		
		Button clearRect=new Button("Clear Rect",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				areaSelectionControler.getSelectionRect().clear();
				areaSelectionControler.updateRect();
			}
		});
		buttons2.add(clearRect);
		
		/*
		Button clip=new Button("Clip",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(selectionRect.hasWidthAndHeight()){
					Canvas canvas=CanvasUtils.createCanvas(selectionRect.getWidth(), selectionRect.getHeight());
					canvas.getContext2d().drawImage(imageElement, -selectionRect.getX(), -selectionRect.getY());
					String url=canvas.toDataUrl();
					imageContainer.clear();
					Image image=new Image(url);
					image.setWidth("500px");
					imageContainer.add(image);
				}
			}
		});
		buttons2.add(clip);
		*/
		
		
		
		
		
		 
		 
		
		
		 
		 imageContainer = new VerticalPanel();
			topPanel.add(imageContainer);
			
			
			fullDock=new DockLayoutPanel(Unit.PX);
			rootDeck.add(fullDock);
			
			settingPanel = new ClipImageSettingPanel(this);
			rootDeck.add(settingPanel);
			
	}
	
	DockLayoutPanel fullDock;
	 void showMainWidget(){
		rootDeck.showWidget(0);
	}
	
	private void showSettingWidget(){
		rootDeck.showWidget(3);
	}
	private void editSelection(){
		driver.edit(getSelection());
		addBt.setEnabled(false);
		updateBt.setEnabled(true);
		removeBt.setEnabled(true);
		newBt.setEnabled(true);
		rootDeck.showWidget(1);//TODO future only double click
	}
	protected void showSelectionImage() {
		clipImageList.read(getSelection().getId(), new ReadListener<ImageClipData>() {

			@Override
			public void onError(String message) {
				LogUtils.log("showSelectionImage:"+message);
			}

			@Override
			public void onRead(ImageClipData data) {
				Image img=new Image(data.getImageData());
				//img.setSize("100%", "100%");
				
				img.addClickHandler(new ClickHandler() {
					
					@Override
					public void onClick(ClickEvent event) {
						showMainWidget();
						previewControler.show();
					}
				});
				
				
				fullDock.clear();
				fullDock.add(img);
				rootDeck.showWidget(2);
				previewControler.hide();
				/*
				final PopupPanel full=new PopupPanel();
				
				full.setSize("100%", "100%");
				
				DockLayoutPanel dock=new DockLayoutPanel(Unit.PX);
				//dock.setSize("100%", "100%");
				
				*/
				
			}
			
		});
	}

	private void generateImages(ImageClipData object) {
		checkState(object.getImageData()!=null,"image is null & faild on generate image");
		
		ImageElement imageElement = ImageElementUtils.create(object.getImageData());
		
		//make thumb
		Canvas canvas=CanvasUtils.createCanvas(getSharedCanvas(), 230,128);
		CanvasUtils.drawFitCenter(canvas, imageElement);
		String thumbImage=WebPBuilder.from(canvas).toDataUrl();
		imageMap.put(object.getId(), thumbImage);
		
		if(object.getRect().hasWidthAndHeight()){
			
			int canvasWidth=320;
			int canvasHeight=180;
			
			
			//crate cell around image
			
			//TODO make method
			
			double[] wh=CanvasUtils.calculateFitSize(canvasWidth, canvasHeight, object.getRect().getWidth(), object.getRect().getHeight());
			double ratio=wh[0]/object.getRect().getWidth();
			int offX=(int)((canvasWidth-wh[0])/ratio);
			int offY=(int)((canvasHeight-wh[1])/ratio);
			canvas=CanvasUtils.createCanvas(getSharedCanvas(), canvasWidth,canvasHeight);
			CanvasUtils.fillRect(canvas, "#000");
			canvas.getContext2d().drawImage(imageElement,object.getRect().getX()-offX/2, object.getRect().getY()-offY/2,object.getRect().getWidth()+offX,object.getRect().getHeight()+offY, 0	, 0,canvasWidth,canvasHeight);
			
			/*
			//Canvas canvas=CanvasUtils.createCanvas(getSharedCanvas(),object.getRect().getWidth(), object.getRect().getHeight());
			canvas.getContext2d().drawImage(imageElement, -object.getRect().getX(), -object.getRect().getY());
			
			//ImageData clipData=canvas.getContext2d().getImageData(0, 0, object.getRect().getWidth(), object.getRect().getHeight());
			
			ImageElement clipImage=ImageElementUtils.create(canvas.toDataUrl());
			canvas=CanvasUtils.createCanvas(getSharedCanvas(),canvasWidth,canvasHeight);
			CanvasUtils.clear(canvas);
			CanvasUtils.drawFitCenter(canvas, clipImage);
			*/
			
			
			/*
			Rect baseRect=new Rect(0,0,320,180).expand(-20, -20).rightBottom(90);
			canvas.getContext2d().setFillStyle("#f00");
			*/
			
			//LogUtils.log(baseRect);
			
			//TODO support fillRect
			//canvas.getContext2d().fillRect(baseRect.getX(), baseRect.getY(),baseRect.getWidth(),baseRect.getHeight());
			
			//CanvasUtils.drawFitImage(canvas, imageElement, baseRect, CanvasUtils.ALIGN_RIGHT, CanvasUtils.VALIGN_BOTTOM);
			
			imageUrl=WebPBuilder.from(canvas).toDataUrl();;
			imageMap.put(object.getId()+"cell", imageUrl);
			
			//create clip selected-image
			canvas=CanvasUtils.createCanvas(getSharedCanvas(),object.getRect().getWidth(), object.getRect().getHeight());
				canvas.getContext2d().drawImage(imageElement, -object.getRect().getX(), -object.getRect().getY());
				
				//ImageData clipData=canvas.getContext2d().getImageData(0, 0, object.getRect().getWidth(), object.getRect().getHeight());
				
				ImageElement clipImage=ImageElementUtils.create(canvas.toDataUrl());
				canvas=CanvasUtils.createCanvas(getSharedCanvas(),230,230);
				CanvasUtils.clear(canvas);
				CanvasUtils.drawExpandCenter(canvas, clipImage);
				
				
				String selectionImage=WebPBuilder.from(canvas).toDataUrl();
				imageMap.put(object.getId()+"clip", selectionImage);
			
			
			
			
		}else{
			imageMap.put(object.getId()+"cell", thumbImage);//at least need image
		}
	}
	private void clearLargeImageDataFromMemory(ImageClipData element){
		element.setImageData(null);
	}

	
	private void clearImageCashes(ImageClipData object){
		imageMap.remove(object.getId()+"clip");
		imageMap.remove(object.getId()+"cell");
		imageMap.remove(object.getId());
		
	}
	private String getThumbImage(ImageClipData object){
		return imageMap.get(object.getId());
	}
	
	private String getClipImage(ImageClipData object){
		return imageMap.get(object.getId()+"clip");
	}
	
	private String getCellImage(ImageClipData object){
		return imageMap.get(object.getId()+"cell");
	}
	
	public void renderHtml(ImageClipData object, SafeHtmlBuilder sb,String cssName){
		
		String divStart="<div class='"+cssName+"'>";
		
		sb.appendHtmlConstant(divStart);
		//sb.appendHtmlConstant("<div class='item'>");
		
		String imageUrl=getCellImage(object);
		
		String thumbImage=getThumbImage(object);
		
		String selectionImage=getClipImage(object);
		
		
		
		
		//make thumb
		
		//thumb first
		if(cssName.equals("preview")){
			if(thumbImage!=null){
				sb.appendHtmlConstant("<img src='"+thumbImage+"' >");
				}
			
		}
		
		//clip-second
		if(imageUrl!=null){
		sb.appendHtmlConstant("<img src='"+imageUrl+"' >");
		}
		
		if(cssName.equals("preview")){
			
			
			if(selectionImage!=null){
				sb.appendHtmlConstant("<img src='"+selectionImage+"' >");
				}
			
		//only preview need details
		sb.appendHtmlConstant("<h1 style='margin:4px;'>");
		if(object.getTitle()!=null){
			sb.appendEscaped(object.getTitle());
		}
		//todo support markdown
		sb.appendHtmlConstant("</h1>");
		
		
		sb.appendHtmlConstant("<pre style='margin:8px;' class='plain'>");
		if(object.getDescription()!=null){
			sb.appendEscaped(object.getDescription());
		}
		sb.appendHtmlConstant("</pre>");
		
		}
		
		
		
		sb.appendHtmlConstant("</div>");
	}
	

	private void setCanvasImage(String imageData){
		imageUrl=imageData;
		ImageElement imageElement = ImageElementUtils.create(imageUrl);
		CanvasUtils.setSize(areaSelectionControler.getCanvas(), imageElement.getWidth(), imageElement.getHeight());
		areaSelectionControler.getCanvas().setVisible(true);
		
		areaSelectionControler.setSpace(imageElement.getWidth(), imageElement.getHeight());
		
		//not work.
		//canvas.getElement().getStyle().setBackgroundImage(imageUrl);//simplly setImage
		
		areaSelectionControler.getCanvas().getElement().setAttribute("style", "background-image: url(\""+imageUrl+"\");");
		areaSelectionControler.updateRect();
	}
	
	public static class BGSetter{
		private BGSetter(){}

		private ImageElement imageElement;
		//private double scaleX=1;
		//private double scaleY=1;
		
		private String styleName="bgStyle";
		public static BGSetter fromImage(String imageUrl){
			BGSetter setter=new BGSetter();
			setter.imageElement=ImageElementUtils.create(imageUrl);
			return setter;
		}
		
		public void setStyle(UIObject object){
			//object.removeStyleName(styleName);
			
			/* i'm not sure need this
			if(injectedBgCss!=null){
				injectedBgCss.removeFromParent();
			}
			*/
			
			if(imageElement!=null){
				//int w=(int) (imageElement.getWidth()*scaleX);
				//int h=(int) (imageElement.getHeight()*scaleY);
				//String style="background-image: url(\""+imageElement.getSrc()+"\");background-size:"+w+"px "+h+"px;";
				//String css="."+styleName+"{"+"background-image: url(\""+imageElement.getSrc()+"\");background-size:"+w+"px "+h+"px;"+"}";
				//StyleElement injectedBgCss = StyleInjector.injectStylesheet(css);
				
				object.getElement().getStyle().setBackgroundImage(imageElement.getSrc());
				
			}
		}
	}
	
	
	

	

	
	


	private Set<String> faildList=Sets.newLinkedHashSet();
	public final class ClipImageList extends AbstractFileSystemList<ImageClipData>{
		
		public ClipImageList(String rootDir, List<ImageClipData> list, Converter<ImageClipData, String> converter) {
			super(rootDir, list, converter);
		}
		
		@Override
		public String getFileName(ImageClipData data) {
			return data.getId();
		}

		@Override
		public void setFileName(ImageClipData data, String fileName) {
			
			data.setId(fileName);
			
		}
		
		@Override
		public void onDataUpdate() {
			//this usually called when file add & name setted.
			
			
			listUpdate();
			
			
		}

		@Override
		public void onError(String error) {
			Window.alert(error);
			LogUtils.log(error);
		}

		@Override
		public void onReadFaild(String fileName) {
			faildList.add(fileName);
		}
		@Override
		public void onReadAllEnd() {
			settingPanel.onReadAll();
		}
		
		
		@Override
		public void onAddComplete(String fileName) {
			
			
			for(ImageClipData d:rawList){
				if(d.getId().equals(fileName)){
					LogUtils.log("generate-image-after has id");
					generateImages(d);
					break;
				}else{
					//LogUtils.log("id:"+d.getId()+",but "+fileName);
				}
			}
			
			checkState(clipImageList.size()==0,"maybe you forget call updateList on add.");
			
			cellList.redrawRow(0);//always 0 is newest data
			
			clearImageData(fileName);
			
			
			if(dropAnddAddCaller!=null){//dropping
				dropAnddAddCaller.done(currentDroppingFileEntry, true);
			}
		}

	@Override
		public void onUpdateComplete(String fileName) {
			clearImageData(fileName);
		}

	
		
	}
	
	private void clearImageData(String id){
		for(ImageClipData d:rawList){
			if(d.getId().equals(id)){
				clearLargeImageDataFromMemory(d);
				break;
			}
		}
		
	}
	
	
	public void listUpdate(){
		cellList.setRowData(clipImageList);
	
	}
	
	

	
	/*
	 * filename converter
	 * filetext converter usually json
	 * 
	 */
	
	/*
	public abstract class IndexBasedAsyncFileSystemList<T> extends ForwardingList<T> implements FileNameSetter<T>{
		private boolean loaded;
		public static final String INDEX_FILE_NAME=".index";
		private List<T> rawList;
		//private Function<T,String> fileNameFunction;
		private Converter<T,String> converter;
		private boolean loading;//check still initial data loading?
		
		//private String rootDir;
		private FileSystemTextDataControler fileSystem;
		public IndexBasedAsyncFileSystemList(String rootDir,List<T> list,Converter<T,String> converter){
			//this.rootDir=rootDir;
			rawList=list;
			fileSystem=new FileSystemTextDataControler(rootDir);
			this.converter=converter;
		}
		private boolean initialized;
		
		public void initialize(){
			fileSystem.initialize(new MakeDirectoryCallback() {
				
				@Override
				public void onError(String message, Object option) {
					onMakedirFaild(createErrorMessage(message,option));
				}
				
				@Override
				public void onMakeDirectory(FileEntry file) {
					initialized=true;
					onMakedireComplete();
				}
			});
		}
	
		@Override
		protected List<T> delegate() {
			return rawList;
		}
		
		//add & update & remove
		
		
		@Override
		public void add(int index, T element) {
			LogUtils.log("add-index:"+index);
			addAsync(element);
			//add need call update index on write-end,need new-id(filename)
			super.add(index, element);
		}
		@Override
		public boolean add(T element) {
			LogUtils.log("add:");
			addAsync(element);
			//add need call update index on write-end,need new-id(filename)
			return super.add(element);
		}
		@Override
		public boolean remove(Object object) {
			@SuppressWarnings("unchecked")
			T data=(T)object;
			removeAsync(data);
			updateIndexAsync();
			return super.remove(object);
		}
		
		public void removeAsync(T data){
			checkNotNull(data);
			final String fileName=getFileName(data);
			fileSystem.removeData(fileName, new RemoveCallback() {
				@Override
				public void onError(String message, Object option) {
					onRemoveFaild(fileName,createErrorMessage(message,option));
				}
				
				@Override
				public void onRemoved() {
					onRemoveComplete(fileName);
				}
			});
		};
		private String createErrorMessage(String message,Object option){
		return message+(option!=null?","+option:"");	
		}
		
		public abstract void onRemoveComplete(String fileName);
		public abstract void onRemoveFaild(String fileName,String errorMessage);
		public abstract void onUpdateComplete(String fileName);
		public abstract void onUpdateFaild(String fileName,String errorMessage);
		public abstract void onAddComplete(String fileName);
		public abstract void onAddFaild(String fileName,String errorMessage);
		
		public abstract void onMakedireComplete();
		public abstract void onMakedirFaild(String errorMessage);
		
		public abstract void onIndexUpdateComplete();
		public abstract void onIndexUpdateFaild(String errorMessage);
		
		public abstract void onDataUpdate();
		
		public void updateAsync(final T data){//must have data
			checkState(initialized);
			checkNotNull(data);
			final String fileName=getFileName(data);
			fileSystem.updateData(fileName, converter.convert(data),new WriteCallback(){

				@Override
				public void onError(String message, Object option) {
					onUpdateFaild(fileName,createErrorMessage(message,option));
				}

				@Override
				public void onWriteEnd(FileEntry file) {
					onUpdateComplete(fileName);
					onDataUpdate();
				}} );
		}
		
		public void addAsync(final T data){
			checkState(initialized);
			checkNotNull(data);
			final String fileName=getFileName(data);
			fileSystem.addData(converter.convert(data), new WriteCallback() {
				
				@Override
				public void onError(String message, Object option) {
					onAddFaild(fileName,createErrorMessage(message,option));
				}
				
				@Override
				public void onWriteEnd(FileEntry file) {
					setFileName(data,file.getName());//file-name first
					updateIndexAsync();
					onAddComplete(fileName);//no care index update faild or not
					onDataUpdate();
				}
			});
			
			
			
		}
		
		public void updateIndexAsync(){
			checkState(initialized);
			String indexText=craeteIndex();
			LogUtils.log("indexText:"+indexText);
			fileSystem.updateData(INDEX_FILE_NAME, indexText,new WriteCallback(){

				@Override
				public void onError(String message, Object option) {
					onIndexUpdateFaild(createErrorMessage(message,option));
				}

				@Override
				public void onWriteEnd(FileEntry file) {
					onIndexUpdateComplete();
					
				}} );
		}
		
		public String craeteIndex(){
			return Joiner.on("\n").join(FluentIterable.from(rawList).transform(new Function<T,String>(){
				@Override
				public String apply(T input) {
					return getFileName(input);
				}
				
			}));
		}
		
		public abstract String getFileName(T data);
		public abstract void setFileName(T data,String fileName);
		public abstract void onReadEnd();
	
		public void readAll(){
			checkState(initialized);
			checkState(!loaded && !loading);
			loading=true;
			
			fileSystem.readText(INDEX_FILE_NAME, new ReadStringCallback() {
				
				@Override
				public void onError(String message, Object option) {
					//TODO add indexRead faild,possible
					LogUtils.log("index - not found?"+message+","+option);
				}
				
				@Override
				public void onReadString(String text, FileEntry file) {
					LogUtils.log("index-read:"+text);
					List<String> fileNames=Lists.newArrayList(text.split("\n"));
					AsyncMultiCaller<String> caller=new AsyncMultiCaller<String>(fileNames) {
						@Override
						public void execAsync(final String fileName) {
							fileSystem.readText(fileName, new ReadStringCallback() {
								@Override
								public void onError(String message, Object option) {
									LogUtils.log("read-file-faild:"+fileName);
									done(fileName, false);
								}
								
								@Override
								public void onReadString(String text, FileEntry file) {
									T data=converter.reverse().convert(text);
									setFileName(data, fileName);//need set filename on read
									delegate().add(data);//simplly add list
									done(fileName, true);
								}
							});
						}
						@Override
						public void doFinally(boolean cancelled){
							loaded=true;//allow only call once
							loading=false;
							onReadEnd();
							onDataUpdate();
						}
						
					};
					caller.startCall();
					
				}
			});
		}
	}
	
	*/
	public interface FileNameSetter<T>{
		public void setFileName(T data,String fileName);
	}
	
	
	
	 interface Driver extends SimpleBeanEditorDriver< ImageClipData,  ImageClipDataEditor> {}
	 Driver driver = GWT.create(Driver.class);
	//private EasyCellTableObjects<ImageClipData> cellObjects;
	
	
	private VerticalPanel imageContainer;
	private ImageClipDataEditor editor;

	private ClipImageList clipImageList;

	private CellList<ImageClipData> cellList;

	private List<ImageClipData> onAddMakeImageList;

	private DeckLayoutPanel rootDeck;

	private Button addBt;

	private Button updateBt;

	private Button removeBt;

	private Button cancelBt;

	private Button newBt;

	private ClipImageSettingPanel settingPanel;

	//private CellListControlList<ImageClipData> updateCellListList;


	

	
	
	public class ImageClipDataEditor extends DockLayoutPanel implements Editor<ImageClipData>,ValueAwareEditor<ImageClipData>{
		Label idEditor;
		TextBox titleEditor;
		TextArea descriptionEditor;
		SimpleEditor<String> imageDataEditor;
		SimpleEditor<Rect> rectEditor;
		private VerticalPanel controler;
		
		
		public VerticalPanel getControler() {
			return controler;
		}
		public ImageClipDataEditor(Canvas canvas){
			super(Unit.PX);
			
			idEditor=new Label();
			VerticalPanel main=new  VerticalPanel();
			addNorth(main,120);
			main.add(idEditor);
			
			HorizontalPanel first=new HorizontalPanel();
			first.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
			first.setSpacing(8);
			main.add(first);
			
			
			
			first.add(new Label("Title"));
			titleEditor=new TextBox();
			first.add(titleEditor);
			main.add(new Label("Description"));
			descriptionEditor=new TextArea();
			descriptionEditor.setWidth("400px");
			main.add(descriptionEditor);
			
			first.add(new Label("Image"));
			FileUploadForm fileUpload=FileUtils.createSingleFileUploadForm(new DataURLListener() {
				
				

				@Override
				public void uploaded(File file, String text) {
					text=WebPBuilder.from(text).toDataUrl();
					imageDataEditor.setValue(text);
					setCanvasImage(text);
				}
			}, true);
			first.add(fileUpload);
			
			
			imageDataEditor=SimpleEditor.of();
			rectEditor=SimpleEditor.of();
			
			controler=new VerticalPanel();
			first.add(controler);
			
			
			ScrollPanel scroll=new ScrollPanel();
			add(scroll);
			scroll.add(canvas);
		}
		@Override
		public void setDelegate(EditorDelegate<ImageClipData> delegate) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void onPropertyChange(String... paths) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void setValue(final ImageClipData value) {
			//need update canvas
			value.getRect().copyTo(areaSelectionControler.getSelectionRect());
			
			if(value.getImageData()!=null){
				checkState(false,"image must be null");
				setCanvasImage(value.getImageData());
				
			}else{
				CanvasUtils.clearBackgroundImage(areaSelectionControler.getCanvas());
				if(value.getId()!=null){//null means new data
				//do read async read-background image for cut down consume memory
				clipImageList.read(value.getId(), new ReadListener<ImageClipData>() {

					@Override
					public void onError(String message) {
						Window.alert(message);
					}

					@Override
					public void onRead(ImageClipData data) {
						if(data.getImageData()!=null){
							imageDataEditor.setValue(data.getImageData());
							setCanvasImage(data.getImageData());
						}
					}
				});
				}
				
			}
			
		}
		
		public void updateImageclipData(String value){
			imageDataEditor.setValue(value);
		}
		public void updateRect(Rect rect){
			rectEditor.setValue(rect.copy());
		}
		
		/*
		public String getEditKey(){
			return imageDataEditor.getValue()+rectEditor.getValue().toKanmaString();
		}
		*/
	}
	
	public ImageClipData getSelection(){
		return selectionModel.getSelectedObject();
	}
	
	public void unselect() {
		ImageClipData selection=getSelection();
		if(selection!=null){
			selectionModel.setSelected(selection, false);
		}
	}

	public void add(ImageClipData addData) {
		LogUtils.log("add( called");
		clipImageList.add(0,addData);//call direct,not effect when add(data)
		listUpdate();
		
		showMainWidget();
		
		driver.edit(new ImageClipData());//for new data
	}

	public void deleteAllFiles() {
		clipImageList.deleteAllFiles(new DoneDeleteListener() {
			@Override
			public void done() {
				rawList.clear();
				listUpdate();
			}
		});
	}
	
	
}
