package com.akjava.gwt.clipimages.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.akjava.gwt.clipimages.client.custom.SimpleCellListResources;
import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileIOUtils;
import com.akjava.gwt.html5.client.file.FileIOUtils.FileQuataAndUsageListener;
import com.akjava.gwt.html5.client.file.FileIOUtils.RemoveCallback;
import com.akjava.gwt.html5.client.file.FileIOUtils.RequestPersitentFileQuotaListener;
import com.akjava.gwt.html5.client.file.FileSystem;
import com.akjava.gwt.html5.client.file.FileUploadForm;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.html5.client.file.FileUtils.DataURLListener;
import com.akjava.gwt.lib.client.CanvasUtils;
import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.AsyncMultiCaller;
import com.akjava.gwt.lib.client.widget.cell.SimpleContextMenu;
import com.akjava.lib.common.graphics.Rect;
import com.google.common.base.Converter;
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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DeckLayoutPanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
	public Canvas getSharedCanvas(){
		if(sharedCanvas==null){
			sharedCanvas=Canvas.createIfSupported();
		}
		return sharedCanvas;
	}
	
	public void onModuleLoad() {
		final DeckLayoutPanel rootDeck=new DeckLayoutPanel();
		RootLayoutPanel.get().add(rootDeck);
		
		
		
		
		areaSelectionControler=new AreaSelectionControler();
		
		
		 //title,rect,image
		
		VerticalPanel root=new VerticalPanel();
		rootDeck.add(root);
		rootDeck.showWidget(0);
		
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
		
		FileUploadForm fileUpload=FileUtils.createSingleFileUploadForm(new DataURLListener() {
			@Override
			public void uploaded(File file, String text) {
				areaSelectionControler.getSelectionRect().clear();
				editor.updateImageclipData(text);
				setCanvasImage(text);
				
				areaSelectionControler.updateRect();
				rootDeck.showWidget(1);
			}
		}, true);
		root.add(fileUpload);
		
		
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
		root.add(givemeMore);
		
		Button cleanUp=new Button("clean up",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				
				clipImageList.cleanUnusedFiles();//TODO catch and clean
				
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
		root.add(cleanUp);
		
		
		editor = new ImageClipDataEditor(areaSelectionControler.getCanvas());
		driver.initialize(editor);
		
		//VerticalPanel editorPanel=new VerticalPanel();
		driver.edit(new ImageClipData());
		rootDeck.add(editor);
		//editorPanel.add(editor);
		//root.add(editorPanel);
		
		

		final Button newBt=new Button("New",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				unselect();
			}
		});
		newBt.setEnabled(false);
		
		
	    	final Button addBt=new Button("Add",new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				editor.updateRect(areaSelectionControler.getSelectionRect());
				ImageClipData data=driver.flush();
				LogUtils.log("flushed-data:"+data.toString());
				
				LogUtils.log("before-add-list-raw");
				for(ImageClipData d:rawDataList){
					LogUtils.log(d.toString());	
				}
				
				LogUtils.log("before-add-list");
				for(ImageClipData d:clipImageList){
					LogUtils.log(d.toString());	
				}
				
				clipImageList.add(0,data);//call direct
				listUpdate();
				rootDeck.showWidget(0);
			}
		});
	    	
	    	final Button updateBt=new Button("Update",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					
					String key=editor.getEditKey();
					imageMap.remove(key);//no more need old version
					
					
					editor.updateRect(areaSelectionControler.getSelectionRect());
					ImageClipData data=driver.flush();
					clipImageList.updateAsync(data);//update no need update index
					//listUpdate(); //unselect call update
					unselect();
				}
			});
	    	updateBt.setEnabled(false);
	    	
	    	final Button removeBt=new Button("Remove",new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					clipImageList.remove(getSelection());//call inside
					driver.edit(new ImageClipData());//clear
					unselect();
					listUpdate();
					rootDeck.showWidget(0);//add case,need this
					
				}
			});
	    	removeBt.setEnabled(false);
	    	
	    	final Button cancelBt=new Button("Cancel",new ClickHandler() {
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
				driver.edit(getSelection());
				addBt.setEnabled(false);
				updateBt.setEnabled(true);
				removeBt.setEnabled(true);
				newBt.setEnabled(true);
				rootDeck.showWidget(1);//TODO future only double click
			}

			@Override
			public void render(com.google.gwt.cell.client.Cell.Context context, ImageClipData object, SafeHtmlBuilder sb) {
				String cssName="item";
				
				String divStart="<div class='"+cssName+"'>";
				
				sb.appendHtmlConstant(divStart);
				//sb.appendHtmlConstant("<div class='item'>");
				
				String imageUrl=null;
				
				if(object.getRect().hasWidthAndHeight() && object.getImageData()!=null){
					String key=object.getImageData()+object.getRect().toKanmaString();
					imageUrl=imageMap.get(key);
					if(imageUrl==null){
					LogUtils.log("toHtml:"+object.getId());
					ImageElement imageElement = ImageElementUtils.create(object.getImageData());
					
					int canvasWidth=320;
					int canvasHeight=180;
					//TODO make method
					double[] wh=CanvasUtils.calculateFitSize(canvasWidth, canvasHeight, object.getRect().getWidth(), object.getRect().getHeight());
					double ratio=wh[0]/object.getRect().getWidth();
					int offX=(int)((canvasWidth-wh[0])/ratio);
					int offY=(int)((canvasHeight-wh[1])/ratio);
					Canvas canvas=CanvasUtils.createCanvas(getSharedCanvas(), canvasWidth,canvasHeight);
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
					
					
					
					Rect baseRect=new Rect(0,0,320,180).expand(-20, -20).rightBottom(90);
					
					canvas.getContext2d().setFillStyle("#f00");
					//LogUtils.log(baseRect);
					
					//TODO support fillRect
					//canvas.getContext2d().fillRect(baseRect.getX(), baseRect.getY(),baseRect.getWidth(),baseRect.getHeight());
					
					//CanvasUtils.drawFitImage(canvas, imageElement, baseRect, CanvasUtils.ALIGN_RIGHT, CanvasUtils.VALIGN_BOTTOM);
					
					imageUrl=canvas.toDataUrl();
					imageMap.put(key, imageUrl);
					}
					
				}
				
				
				
				
				
				if(imageUrl!=null){
				sb.appendHtmlConstant("<img src='"+imageUrl+"' >");
				}
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
				
				
				
				sb.appendHtmlConstant("</div>");
				
			}
    		
    	};
    	

	    	
    	
    	
    	
    	cellList = new CellList<ImageClipData>(customCell,GWT.<SimpleCellListResources> create(SimpleCellListResources.class));
		root.add(cellList);
		
		
		selectionModel=new SingleSelectionModel<ImageClipData>();
		selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				ImageClipData selection=selectionModel.getSelectedObject();
				
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
		
		rawDataList = new ArrayList<ImageClipData>();
		
		clipImageList = new ClipImageList("clipimage",rawDataList,new ImageClipDataConverter());
		
		
	    	
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
			root.add(imageContainer);
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
			/*
			LogUtils.log("set-file-name:"+fileName);
			int index=indexOf(data);
			LogUtils.log("before-index:"+index);
			*/
			data.setId(fileName);
			/*
			LogUtils.log("after-index:"+indexOf(data));
			set(index, data);
			*/
		}
		
		@Override
		public void onDataUpdate() {
			//this usually called when file add & name setted.
			
			//for cell list update
			//listUpdate();
			//updateCellListList.updateCellList();//data updated
			LogUtils.log("clipImageList-data-update");
			for(ImageClipData data:clipImageList){
				LogUtils.log(data.toString());	
			}
			/*
			LogUtils.log("imageListList");
			for(ImageClipData data:updateCellListList){
				LogUtils.log(data.toString());	
				}
				*/
			
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
		
	}
	
	
	public void listUpdate(){
		LogUtils.log("before-list-update");
		for(ImageClipData data:clipImageList){
			LogUtils.log(data.toString());	
		}
		cellList.setRowData(clipImageList.delegate());
		LogUtils.log("after-list-update");
		for(ImageClipData data:clipImageList){
			LogUtils.log(data.toString());	
		}
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

	private ArrayList<ImageClipData> rawDataList;

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
		public void setValue(ImageClipData value) {
			//need update canvas
			value.getRect().copyTo(areaSelectionControler.getSelectionRect());
			
			if(value.getImageData()!=null){
				setCanvasImage(value.getImageData());
			}else{
				CanvasUtils.clearBackgroundImage(areaSelectionControler.getCanvas());
					
				
			}
			
		}
		
		public void updateImageclipData(String value){
			imageDataEditor.setValue(value);
		}
		public void updateRect(Rect rect){
			rectEditor.setValue(rect.copy());
		}
		
		public String getEditKey(){
			return imageDataEditor.getValue()+rectEditor.getValue().toKanmaString();
		}
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
	
	
}
