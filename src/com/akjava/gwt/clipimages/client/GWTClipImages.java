package com.akjava.gwt.clipimages.client;

import static com.google.common.base.Preconditions.checkState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.DoneDeleteListener;
import com.akjava.gwt.clipimages.client.IndexBasedAsyncFileSystemList.ReadListener;
import com.akjava.gwt.clipimages.client.custom.SimpleCellListResources;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileHandler;
import com.akjava.gwt.html5.client.file.FileReader;
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
import com.akjava.gwt.lib.client.experimental.FileEntryOrdering;
import com.akjava.gwt.lib.client.experimental.ImageBuilder;
import com.akjava.gwt.lib.client.experimental.ProgressCanvas;
import com.akjava.gwt.lib.client.experimental.ImageBuilder.WebPBuilder;
import com.akjava.gwt.lib.client.experimental.PreviewHtmlPanelControler;
import com.akjava.gwt.lib.client.experimental.RectCanvasUtils;
import com.akjava.gwt.lib.client.experimental.RectListEditor;
import com.akjava.gwt.lib.client.widget.PanelUtils;
import com.akjava.gwt.lib.client.widget.cell.SimpleContextMenu;
import com.akjava.lib.common.graphics.Rect;
import com.google.common.base.Converter;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ForwardingList;
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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
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

	
	
	
	private Canvas sharedCanvas;
	private Map<String,String> imageMap=new HashMap<String,String>();
	
	//private AreaSelectionControler areaSelectionControler;

	private SingleSelectionModel<ImageClipData> selectionModel;


	
	private PreviewHtmlPanelControler<ImageClipData> previewControler;


	private List<ImageClipData> rawList=new ArrayList<ImageClipData>();
	
	private FileEntry currentDroppingFileEntry;
	private AsyncMultiCaller<FileEntry> dropAnddAddCaller;

	private Button updateAndNextBt;

	private Button removeAndNextBt;
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
	
	public ClipImageList getClipImageList(){
		return clipImageList;
	}
	
	Stopwatch generateWatch=Stopwatch.createUnstarted();
	public void onModuleLoad() {
		
		String token=History.getToken();
		if(token.indexOf("debug")!=-1){
			debugSetting=true;
		}
		
		previewControler=new PreviewHtmlPanelControler<ImageClipData>();
		Button editBt=new Button("Edit",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				edit(getSelection());
			}
		});
		editBt.setWidth("100%");
		previewControler.show();//for generate container
		previewControler.hide();
		previewControler.getContainer().add(editBt);
		
		HorizontalPanel bts=new HorizontalPanel();
		bts.setSpacing(4);
		bts.setWidth("100%");
		
		previewControler.getContainer().add(bts);
		
		HorizontalPanel h2=new HorizontalPanel();
		h2.setWidth("100%");
		
		h2.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
		final CheckBox check=new CheckBox("Without confirm");
		bts.add(check);
		bts.add(h2);
		Button removeSelectionBt=new Button("Remove",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(!check.getValue()){
					boolean confirm=Window.confirm("Remove selection?");
					if(!confirm){
						return;
					}
				}
				
				doRemove(getSelection());
			}
		});
		h2.add(removeSelectionBt);
		
		
		Button hideBt=new Button("Hide",new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				previewControler.hide();
			}
		});
		previewControler.getContainer().add(hideBt);
		hideBt.setWidth("100%");
		
		rootDeck = new DeckLayoutPanel();
		
		RootLayoutPanel.get().add(rootDeck);
		
		
		
		
		//areaSelectionControler=new AreaSelectionControler();
		
		
		 //title,rect,image
		
		DockLayoutPanel mainRoot=new DropDockDataUrlRootPanel() {
			
			@Override
			public void loadFile(String pareht, Optional<File> optional, String dataUrl) {
				//never called
			}
			
			@Override
			public  void onDropFiles(List<FileEntry> files){
				
				Collections.sort(files,FileEntryOrdering.getOrderByPath());
				
					
				
				
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
											addImageDataOnly(WebPBuilder.from(dataUrl).toDataUrl());
											
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
							currentDroppingFileEntry=null;
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
		
		

		
		HorizontalPanel inputPanel=new HorizontalPanel();
		inputPanel.setSpacing(8);
		
		fileUpload = FileUtils.createSingleFileUploadForm(new DataURLListener() {
			@Override
			public void uploaded(File file, String text) {
				text=WebPBuilder.from(text).toDataUrl();
				//areaSelectionControler.getSelectionRect().clear();
				editor.updateImageclipData(text);
				//setCanvasImage(text);//convert from PNG to WEBP here
				
				//areaSelectionControler.updateRect();
				rootDeck.showWidget(1);
			}
		}, true);
		inputPanel.add(fileUpload);
		fileUpload.getFileUpload().setEnabled(false);
		

		
	
		
		
		editor = new ImageClipDataEditor();
		driver.initialize(editor);
		
		//VerticalPanel editorPanel=new VerticalPanel();
		driver.edit(new ImageClipData());
		rootDeck.add(editor);


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
				//editor.updateRect(areaSelectionControler.getSelectionRect());
				ImageClipData addData=driver.flush();
				
				
				
				add(addData);
			}
		});
	    	
	    	updateBt = new Button("Update",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					
					
					//editor.updateRect(areaSelectionControler.getSelectionRect());
					
					ImageClipData data=driver.flush();
					
					clearImageCashes(data);//remove old data
					
					generateImages(data);
					
					
					
					clipImageList.updateAsync(data);//update no need update index
					//listUpdate(); //unselect call update
					unselect();
				}
			});
	    	updateBt.setEnabled(false);
	    	
	    	updateAndNextBt = new Button("Update&Next",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					doUpdateAndNext();
					
					
					//listUpdate(); //unselect call update
					
				}
			});
	    	
	    	
	    	removeBt = new Button("Remove",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final ImageClipData targetData=driver.flush();//Update & Next ,change editor
					doRemove(targetData);
					
				}
			});
	    	removeBt.setEnabled(false);
	    	
	    	removeAndNextBt = new Button("Remove & Next",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final ImageClipData targetData=driver.flush();//Update & Next ,change editor
					clearImageCashes(targetData);//remove old data
					
					clipImageList.read(targetData.getId(), new ReadListener<ImageClipData>() {

						@Override
						public void onError(String message) {
							LogUtils.log("maybe invalid json:"+message);
							
							clipImageList.remove(targetData);//call inside
							
							driver.edit(new ImageClipData());//clear
							unselect();
							listUpdate();
							
							
							rootDeck.showWidget(0);//add case,need this
						}

						@Override
						public void onRead(ImageClipData data) {
							settingPanel.addTrashBox(data);
							
							Optional<ImageClipData> hasNext=getNextData(data);
							clipImageList.remove(targetData);//call inside
							
							//driver.edit(new ImageClipData());//clear
							//unselect();
							listUpdate();
							boolean edited=false;
							for(ImageClipData nextData:hasNext.asSet()){
								edit(nextData);
								edited=true;
							}
							
							if(!edited){
								driver.edit(new ImageClipData());//clear
								unselect();//reach last
							}
							
						}
						
					});
				}
			});
	    	
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
				showSelectionImage(getSelection());
				
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
					updateAndNextBt.setEnabled(false);
					removeBt.setEnabled(false);
					removeAndNextBt.setEnabled(false);
					newBt.setEnabled(false);
					rootDeck.showWidget(0);//TODO future only double click
				}
			}
		});
		
		cellList.setSelectionModel(selectionModel);
		

		
		onAddMakeImageList = new ForwardingList<ImageClipData>(){
			@Override
			public void add(int index, ImageClipData element) {
				
				//clear image after async-write-finished
				super.add(index, element);
				listUpdate();//for cell-update on initialize
			}

			@Override
			public boolean add(ImageClipData element) {
				generateWatch.start();
				//add is only called when initial read,so clear image on here
				generateImages(element);
				generateWatch.stop();
				clearLargeImageDataFromMemory(element);
				boolean b= super.add(element);
				
				
				//listUpdate();//for cell-update on initialize
				
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
	    	buttons.add(updateAndNextBt);
	    	buttons.add(removeBt);
	    	buttons.add(removeAndNextBt);
	    	buttons.add(cancelBt);
	    	
		
		//TODO initial load form system
		
		//TODO implement initial read
		
		
		
		HorizontalPanel buttons2=new HorizontalPanel();
		//root.add(buttons2);
		/*
		Button clearRect=new Button("Clear Rect",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				areaSelectionControler.getSelectionRect().clear();
				areaSelectionControler.updateRect();
			}
		});
		buttons2.add(clearRect);
		*/
		
	
		
		
		
		
		
		 
		 
		
		
		 
		 imageContainer = new VerticalPanel();
			topPanel.add(imageContainer);
			
			
			fullDock=new DockLayoutPanel(Unit.PX);
			rootDeck.add(fullDock);
			
			settingPanel = new ClipImageSettingPanel(this);
			rootDeck.add(settingPanel);
			
	}
	
	
	protected void doRemove(final ImageClipData targetData) {
		
		clearImageCashes(targetData);//remove old data
		
		clipImageList.read(targetData.getId(), new ReadListener<ImageClipData>() {

			@Override
			public void onError(String message) {
				LogUtils.log("maybe invalid json:"+message);
				
				clipImageList.remove(targetData);//call inside
				
				driver.edit(new ImageClipData());//clear
				unselect();
				listUpdate();
				rootDeck.showWidget(0);//add case,need this
			}

			@Override
			public void onRead(ImageClipData data) {
				settingPanel.addTrashBox(data);
				
				clipImageList.remove(targetData);//call inside
				
				driver.edit(new ImageClipData());//clear
				unselect();
				listUpdate();
				
				rootDeck.showWidget(0);//add case,need this
			}
			
		});
	}

	protected void doUpdateAndNext() {
		//no need
		//editor.updateRect(areaSelectionControler.getSelectionRect());
		
		ImageClipData data=driver.flush();
		
		clearImageCashes(data);//remove old data
		
		generateImages(data);
		
		
		
		clipImageList.updateAsync(data);//update no need update index
		//unselect();
		
		for(ImageClipData nextData:getNextData(data).asSet()){
			edit(nextData);
		}
	}

	protected Optional<ImageClipData> getNextData(ImageClipData data) {
		int index=clipImageList.indexOf(data)+1;
		if(index<clipImageList.size()){
			return Optional.of(clipImageList.get(index));
		}else{
			return Optional.absent();
		}
	}
	
	protected Optional<ImageClipData> getPrevData(ImageClipData data) {
		int index=clipImageList.indexOf(data)-1;
		if(index>0){
			return Optional.of(clipImageList.get(index));
		}else{
			return Optional.absent();
		}
	}

	DockLayoutPanel fullDock;
	 void showMainWidget(){
		rootDeck.showWidget(0);
		if(getSelection()!=null){
			previewControler.show();
		}
	}
	
	private void showSettingWidget(){
		previewControler.hide();
		settingPanel.updateStorageInfo();
		rootDeck.showWidget(3);
	}
	private void edit(ImageClipData data){
		previewControler.hide();
		driver.edit(data);
		addBt.setEnabled(false);
		updateBt.setEnabled(true);
		updateAndNextBt.setEnabled(true);
		removeBt.setEnabled(true);
		removeAndNextBt.setEnabled(true);
		newBt.setEnabled(true);
		rootDeck.showWidget(1);//TODO future only double click
	}
	protected void showSelectionImage(final ImageClipData imageData) {
		clipImageList.read(imageData.getId(), new ReadListener<ImageClipData>() {

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
				
				img.addMouseWheelHandler(new MouseWheelHandler() {
					
					@Override
					public void onMouseWheel(MouseWheelEvent event) {
						if(event.getDeltaY()>0){
							for(ImageClipData next:getNextData(imageData).asSet()){
								showSelectionImage(next);
							}
						}else{
							
							for(ImageClipData prev:getPrevData(imageData).asSet()){
								showSelectionImage(prev);
							}
						}
					}
				});
			
				
			}
			
		});
	}
	
	private ImageElement dummyImage;
	
	
	 void generateImages(ImageClipData object) {
		checkState(object.getImageData()!=null,"image is null & faild on generate image");
		
		/*
		 * inserting HD image take so much time;
		imageMap.put(object.getId()+"clip", object.getImageData());
		imageMap.put(object.getId()+"cell", object.getImageData());
		
		if(true){
			return;
		}
		*/
		/*
		if(dummyImage==null){
			Canvas canvas=CanvasUtils.createCanvas(100, 100);
			CanvasUtils.fillRect(canvas, "#888");
			dummyImage=ImageElementUtils.create(canvas.toDataUrl());
		}
		
		ImageElement imageElement =dummyImage;
		*/
		
		ImageElement imageElement = ImageElementUtils.create(object.getImageData());
		
		//make thumb
		Canvas generateImageCanvas=CanvasUtils.createCanvas(getSharedCanvas(), 230,128);//making field take slow
		CanvasUtils.drawFitCenter(generateImageCanvas, imageElement);
		
		//from webp to png,jpeg not so effect.but jpeg 10% fast
		//String thumbImage=WebPBuilder.from(generateImageCanvas).toDataUrl();
		String thumbImage=ImageBuilder.from(generateImageCanvas).onJpeg().toDataUrl();
		imageMap.put(object.getId(), thumbImage);//Webp is slow but cut down memory usage
		
		
		
		
		if(object.getRects().size()>0){
			
			List<ImageElement> thumbs=new ArrayList<ImageElement>();
			
			
			int canvasWidth=320;
			int canvasHeight=180;
			
			
			for(Rect rec:object.getRects()){
			//crate cell around image
			
			//TODO make method
			
			//in this here try to draw around
			double[] wh=CanvasUtils.calculateFitSize(canvasWidth, canvasHeight, rec.getWidth(), rec.getHeight());
			double ratio=wh[0]/rec.getWidth();
			int offX=(int)((canvasWidth-wh[0])/ratio);
			int offY=(int)((canvasHeight-wh[1])/ratio);
			generateImageCanvas=CanvasUtils.createCanvas(getSharedCanvas(), canvasWidth,canvasHeight);
			CanvasUtils.fillRect(generateImageCanvas, "#000");
			generateImageCanvas.getContext2d().drawImage(imageElement,rec.getX()-offX/2, rec.getY()-offY/2,rec.getWidth()+offX,rec.getHeight()+offY, 0	, 0,canvasWidth,canvasHeight);
			//String url=WebPBuilder.from(canvas).toDataUrl();
			
			
			
			//TODO support fillRect
			//canvas.getContext2d().fillRect(baseRect.getX(), baseRect.getY(),baseRect.getWidth(),baseRect.getHeight());
			
			//CanvasUtils.drawFitImage(canvas, imageElement, baseRect, CanvasUtils.ALIGN_RIGHT, CanvasUtils.VALIGN_BOTTOM);
			
			
			
			RectCanvasUtils.crop(imageElement, rec, sharedCanvas);
			String url=WebPBuilder.from(sharedCanvas).toDataUrl();//use exactly crop;
			
			if(imageMap.get(object.getId()+"clip")==null){//only do first time
			//create clip selected-image
				generateImageCanvas=CanvasUtils.createCanvas(getSharedCanvas(),rec.getWidth(), rec.getHeight());
				generateImageCanvas.getContext2d().drawImage(imageElement, -rec.getX(), -rec.getY());
				
				//ImageData clipData=canvas.getContext2d().getImageData(0, 0, rec.getWidth(), rec.getHeight());
				
				ImageElement clipImage=ImageElementUtils.create(generateImageCanvas.toDataUrl());
				generateImageCanvas=CanvasUtils.createCanvas(getSharedCanvas(),230,230);
				CanvasUtils.clear(generateImageCanvas);
				CanvasUtils.drawExpandCenter(generateImageCanvas, clipImage);
				
				
				String selectionImage=ImageBuilder.from(generateImageCanvas).onJpeg().toDataUrl();
				imageMap.put(object.getId()+"clip", selectionImage);
				
				}
			
			if(object.getRects().size()==1){
				imageMap.put(object.getId()+"cell", url);
				}
			else{
				thumbs.add(ImageElementUtils.create(url));
			}
			
			}
			//only this case join it.
			if(object.getRects().size()>1){
				joinHorizontal(thumbs,generateImageCanvas);
				imageMap.put(object.getId()+"cell", ImageBuilder.from(generateImageCanvas).onJpeg().toDataUrl());//JPEG to speed up
			}
			
		}else{
			imageMap.put(object.getId()+"cell", thumbImage);//at least need image
			
			
		}
		
	}
	
	public void joinHorizontal(List<ImageElement> images,Canvas target){
		int w=0;
		int h=0;
		int dx=0;
		for(ImageElement img:images){
			w+=img.getWidth();
			if(h<img.getHeight()){
				h=img.getHeight();
			}
		}
		CanvasUtils.createCanvas(target, w, h);
		for(ImageElement img:images){
			target.getContext2d().drawImage(img, dx, 0);
			dx+=img.getWidth();
		}
	}
	
	 void clearLargeImageDataFromMemory(ImageClipData element){
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
		
		String clipImage=getClipImage(object);
		
		
		
		
		//make thumb
		
		//thumb first
		if(cssName.equals("preview")){
			if(thumbImage!=null && !imageUrl.equals(thumbImage)){
				sb.appendHtmlConstant("<img src='"+thumbImage+"' >");
				}
			
		}
		
		//clip-second
		if(imageUrl!=null ){
		sb.appendHtmlConstant("<img src='"+imageUrl+"' >");
		}
		
		if(cssName.equals("preview")){
			
			
			if(clipImage!=null){//not show useless same-image(when not clipped)
				sb.appendHtmlConstant("<img src='"+clipImage+"' >");
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
	

	/*
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
	*/
	
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
			
			
			if(imageElement!=null){
				
				object.getElement().getStyle().setBackgroundImage(imageElement.getSrc());
				
			}
		}
	}
	
	
	

	

	
	


	private Set<String> faildList=Sets.newLinkedHashSet();
	public Set<String> getReadFaildFileNameSet() {
		return faildList;
	}
	
	private boolean debugSetting=false;

	private long readAllStart;
	public final class ClipImageList extends AbstractFileSystemList<ImageClipData>{
		private ProgressCanvas progressCanvas;

		//Stopwatch watch;
		public ClipImageList(String rootDir, List<ImageClipData> list, Converter<ImageClipData, String> converter) {
			super(rootDir, list, converter);
		}
		
		public void onReadAllStart(int size){
			progressCanvas = new ProgressCanvas("Loading", size);
			progressCanvas.show();
			
		}
		public void onReadAllProgress(String fileName){
			progressCanvas.progress(1);
		}
		
		@Override
		public void readAll(){
			readAllStart=System.currentTimeMillis();
			//for test
			if(debugSetting){
				debug=true;
			showSettingWidget();
			}
			else{
				//watch.createStarted();
				super.readAll();
			}
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
			if(progressCanvas!=null){
				progressCanvas.hide();
			}
			LogUtils.log("total-generate-image:"+generateWatch.elapsed(TimeUnit.MILLISECONDS)+"ms");
			LogUtils.log("read:"+size()+",time="+((System.currentTimeMillis()-readAllStart)/1000)+" sec");
			settingPanel.onReadAll();
			fileUpload.getFileUpload().setEnabled(true);
			
			listUpdate();//only update on last.
		}
		
		
		@Override
		public void onAddComplete(String fileName) {
			//LogUtils.log("onAddComplete:"+fileName);
			
			for(ImageClipData d:rawList){
				if(d.getId().equals(fileName)){
					//LogUtils.log("generate-image-after has id");
					generateImages(d);
					break;
				}else{
					//LogUtils.log("id:"+d.getId()+",but "+fileName);
				}
			}
			
			checkState(clipImageList.size()!=0,"maybe you forget call updateList on add.");
			
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
		//LogUtils.log("listUpdate:"+clipImageList.size());
		cellList.setRowData(clipImageList);
	
	}
	
	

	
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
	private FileUploadForm fileUpload;

	//private CellListControlList<ImageClipData> updateCellListList;


	

	
	
	public class ImageClipDataEditor extends DockLayoutPanel implements Editor<ImageClipData>,ValueAwareEditor<ImageClipData>{
		Label idEditor;
		TextBox titleEditor;
		TextArea descriptionEditor;
		SimpleEditor<String> imageDataEditor;
		
		
		//SimpleEditor<Rect> rectEditor;
		
		RectListEditor rectsEditor;
		
		private VerticalPanel controler;
		
		
		
		public VerticalPanel getControler() {
			return controler;
		}
		public ImageClipDataEditor(){
			super(Unit.PX);
			HorizontalPanel top=new HorizontalPanel();
			
			VerticalPanel left=new VerticalPanel();
			top.add(left);
			VerticalPanel right=new VerticalPanel();
			top.add(right);
			
			idEditor=new Label();
			VerticalPanel main=new  VerticalPanel();
			left.add(main);
			addNorth(top,60);
			main.add(idEditor);
			
			HorizontalPanel first=new HorizontalPanel();
			first.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
			first.setSpacing(8);
			main.add(first);
			
			
			
			first.add(new Label("Title"));
			titleEditor=new TextBox();
			first.add(titleEditor);
			right.add(new Label("Description"));
			descriptionEditor=new TextArea();
			descriptionEditor.setWidth("400px");
			right.add(descriptionEditor);
			
			first.add(new Label("Image"));
			FileUploadForm fileUpload=FileUtils.createSingleFileUploadForm(new DataURLListener() {
				
				

				@Override
				public void uploaded(File file, String text) {
					text=WebPBuilder.from(text).toDataUrl();
					imageDataEditor.setValue(text);
					rectsEditor.setBackgroundImage(ImageElementUtils.create(text));
					rectsEditor.getCanvas().setFocus(true);
				}
			}, true);
			first.add(fileUpload);
			
			
			imageDataEditor=SimpleEditor.of();
			rectsEditor=new RectListEditor();
			
			rectsEditor.getCanvas().addKeyUpHandler(new KeyUpHandler() {
				
				@Override
				public void onKeyUp(KeyUpEvent event) {
					if(event.getNativeKeyCode()==KeyCodes.KEY_ENTER){
						doUpdateAndNext();
					}else if(event.getNativeKeyCode()==' '){
						rectsEditor.doPlus();
					}
				}
			});
			//rectEditor=SimpleEditor.of();
			
			controler=new VerticalPanel();
			first.add(controler);
			
			
			ScrollPanel scroll=new ScrollPanel();
			add(scroll);
			scroll.add(rectsEditor);
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
			//value.getRect().copyTo(areaSelectionControler.getSelectionRect());
			
			if(value.getImageData()!=null){
				checkState(false,"image must be null");
				
				rectsEditor.setBackgroundImage(ImageElementUtils.create(value.getImageData()));
				
			}else{
				rectsEditor.clearBackgroundImage();
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
							rectsEditor.setBackgroundImage(ImageElementUtils.create(data.getImageData()));
						}
					}
				});
				}
				
			}
			
			rectsEditor.getCanvas().setFocus(true);
		}
		
		public void updateImageclipData(String value){
			imageDataEditor.setValue(value);
			rectsEditor.setBackgroundImage(ImageElementUtils.create(value));
			rectsEditor.setValue(new ArrayList<Rect>());//for clear
			//need reset rects?
		}
		
		/*
		public void updateRect(Rect rect){
			rectEditor.setValue(rect.copy());
		}
		*/
		
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
		//LogUtils.log("add( called");
		clipImageList.add(0,addData);//call direct,not effect when add(data)
		
		
		showMainWidget();
		
		driver.edit(new ImageClipData());//for new data
	}

	public void deleteAllFiles() {
		deleteAllFiles(null);
	}
	public void deleteAllFiles(final DoneDeleteListener listener) {
		
		final ProgressCanvas progress=new ProgressCanvas("Delete all", 1);
		progress.show();
		clipImageList.deleteAllFiles(new DoneDeleteListener() {
			@Override
			public void done() {
				rawList.clear();
				listUpdate();
				showMainWidget();
				progress.hide();
				if(listener!=null){
					listener.done();
				}
			}
		});
	}

	public Optional<ImageClipData> findDataById(String fileName) {
		for(ImageClipData data:clipImageList){
			if(data.getId().equals(fileName)){
				return Optional.of(data);
			}
		}
		return Optional.absent();
	}
	
	
}
