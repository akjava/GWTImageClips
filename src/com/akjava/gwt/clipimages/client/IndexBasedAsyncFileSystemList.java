package com.akjava.gwt.clipimages.client;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.akjava.gwt.clipimages.client.GWTClipImages.FileNameSetter;
import com.akjava.gwt.html5.client.file.DirectoryReader;
import com.akjava.gwt.html5.client.file.FileIOUtils;
import com.akjava.gwt.html5.client.file.FileIOUtils.FileQuataAndUsageListener;
import com.akjava.gwt.html5.client.file.FileIOUtils.MakeDirectoryCallback;
import com.akjava.gwt.html5.client.file.FileIOUtils.ReadStringCallback;
import com.akjava.gwt.html5.client.file.FileIOUtils.RemoveCallback;
import com.akjava.gwt.html5.client.file.FileIOUtils.WriteCallback;
import com.akjava.gwt.html5.client.file.webkit.DirectoryCallback;
import com.akjava.gwt.html5.client.file.webkit.FileEntry;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.gwt.lib.client.experimental.AsyncMultiCaller;
import com.akjava.gwt.lib.client.experimental.FileSystemTextDataControler;
import com.google.common.base.Converter;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Utf8;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window;

public abstract class IndexBasedAsyncFileSystemList<T> extends ForwardingList<T> implements FileNameSetter<T>{
		private boolean loaded;
		public static final String INDEX_FILE_NAME=".index";
		public static final String BACKUP_INDEX_FILE_NAME=".index.backup";
		private List<T> rawList;
		//private Function<T,String> fileNameFunction;
		private Converter<T,String> converter;
		private boolean loading;//check still initial data loading?
		
		//private String rootDir;
		private FileSystemTextDataControler fileSystem;
		public FileSystemTextDataControler getFileSystem() {
			return fileSystem;
		}
		
		public static interface FileListListener{
			public void files(List<String> fileNames);
		}
		
		public static interface DoneDeleteListener{
			public void done();
		}
		
		public void deleteAllFiles(final DoneDeleteListener listener){
			getAllFiles(new FileListListener() {
				@Override
				public void files(List<String> fileNames) {
					if(fileNames.size()==0){
						return;
					}
					AsyncMultiCaller<String> removeCaller=new AsyncMultiCaller<String>(fileNames) {
						@Override
						public void execAsync(final String data) {
							fileSystem.removeData(data, new RemoveCallback() {
								
								@Override
								public void onError(String message, Object option) {
									LogUtils.log("remove faild from cleanup:"+data);
									done(data,false);
								}
								
								@Override
								public void onRemoved() {
									LogUtils.log("remove success from cleanup:"+data);
									done(data,true);
								}
							});
						}
						public void doFinally(boolean cancelled){
							if(!cancelled){
								listener.done();
							}
						}
					};
					removeCaller.startCall();
				}
			},null);
		}
		
		public void deleteUnusedFiles(){
			getUnusedFiles(new FileListListener() {
				@Override
				public void files(List<String> fileNames) {
					if(fileNames.size()==0){
						return;
					}
					AsyncMultiCaller<String> removeCaller=new AsyncMultiCaller<String>(fileNames) {
						@Override
						public void execAsync(final String data) {
							fileSystem.removeData(data, new RemoveCallback() {
								
								@Override
								public void onError(String message, Object option) {
									LogUtils.log("remove faild from cleanup:"+data);
									done(data,false);
								}
								
								@Override
								public void onRemoved() {
									LogUtils.log("remove success from cleanup:"+data);
									done(data,true);
								}
							});
						}
					};
					removeCaller.startCall();
				}
			});
		}
		
		
		
		

		public void getAllFiles(final FileListListener listener,@Nullable List<String> existFiles){
			checkNotNull(listener);
			final List<String> ignores=new ArrayList<String>();
			
			if(existFiles!=null){
				ignores.addAll(existFiles);
			}
			
			/*
			if(!ignores.contains(INDEX_FILE_NAME)){
				ignores.add(INDEX_FILE_NAME);
			}
			
			if(!ignores.contains(BACKUP_INDEX_FILE_NAME)){
				ignores.add(BACKUP_INDEX_FILE_NAME);
			}
			*/
			
			fileSystem.initialize(new MakeDirectoryCallback() {
				
				@Override
				public void onError(String message, Object option) {
					onMakedirFaild(createErrorMessage(message,option));
				}
				
				@Override
				public void onMakeDirectory(FileEntry file) {
					DirectoryReader reader=file.getReader();
					reader.readEntries(new DirectoryCallback() {
						@Override
						public void callback(JsArray<FileEntry> entries) {
							List<String> notusedName=new ArrayList<String>();
							for(int i=0;i<entries.length();i++){
								FileEntry entry=entries.get(i);
								if(!ignores.contains(entry.getName())){
									notusedName.add(entry.getName());
								}
							}
							listener.files(notusedName);
							LogUtils.log("not-used:"+Joiner.on("\n").join(notusedName));//TODO call listener
						}
					});
				}
			});
		}
		
		public void getUnusedFiles(final FileListListener listener){
			final List<String> existFiles=Lists.newArrayList(createIndex().split("\n"));
			existFiles.add(INDEX_FILE_NAME);
			existFiles.add(BACKUP_INDEX_FILE_NAME);
			getAllFiles(listener, existFiles);
		}
		
		public IndexBasedAsyncFileSystemList(String rootDir,List<T> list,Converter<T,String> converter){
			//this.rootDir=rootDir;
			rawList=list;
			fileSystem=new FileSystemTextDataControler(rootDir);
			this.converter=converter;
		}
		public List<T> getRawList() {
			return rawList;
		}
		private boolean initialized;
		
		//TODO find better way to know
		protected void getInfo(){
			FileIOUtils.getFileQuataAndUsage(true, new FileQuataAndUsageListener() {
				@Override
				public void storageInfoUsageCallback(double currentUsageInBytes, double currentQuotaInBytes) {
					LogUtils.log("usage:"+((int)(currentUsageInBytes/1024/1204))+"Mbyte/"+((int)(currentQuotaInBytes/1024/1204))+"Mbyte");
				}
			});
		}
		
		public void initialize(){
			getInfo();
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
		public void add(int index, T indexBasedElement) {
			
			delegate().add(index, indexBasedElement);//no effect when call rawList directly
			
			
			addAsync(indexBasedElement);
			//add need call update index on write-end,need new-id(filename)
			
		}
		@Override
		public boolean add(T element) {
			
			//add need call update index on write-end,need new-id(filename)
			boolean b= delegate().add(element);
			addAsync(element);
			return b;
		}
		@Override
		public boolean remove(Object object) {
			boolean b= super.remove(object);
			@SuppressWarnings("unchecked")
			T data=(T)object;
			removeAsync(data);
			updateIndexAsync();
			return b;
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
		
		public abstract void onReadIndexFaild(String errorMessage);
		
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
			
			fileSystem.addData(converter.convert(data), new WriteCallback() {
				
				@Override
				public void onError(String message, Object option) {
					//there still not have name
					
					
					onAddFaild(null,createErrorMessage(message,option));
					
					//remove auto-matically
					delegate().remove(data);
					updateIndexAsync();
					onDataUpdate();
					
					deleteUnusedFiles();
				}
				
				@Override
				public void onWriteEnd(FileEntry file) {
					
					setFileName(data,file.getName());//file-name first
					updateIndexAsync();
					onAddComplete(file.getName());//no care index update faild or not
					onDataUpdate();
				}
			});
			
			
			
		}
		
		public void getFileQuataAndUsage(FileQuataAndUsageListener listener){
			FileIOUtils.getFileQuataAndUsage(true,listener);
		}
		
		public void updateIndexAsync(){
			checkState(initialized);
			final String indexText=createIndex();
			//LogUtils.log("indexText:"+indexText);
			//TODO check-space first
			
			getFileQuataAndUsage(new FileQuataAndUsageListener(){
				
				public void doWriteIndex(int remain,String oldIndexText){
					//check remain-size, and warn,if no space,do more space
					int needbytes=Utf8.encodedLength(oldIndexText+indexText);
					if(remain<needbytes){
						Window.alert("not enough space to write index to storage\n"+"give more space or remove unused data from settings and reindex it");
						return;
					}
					
					//so no-problem finally write-data
					//at first copy to backup //TODO
					fileSystem.updateData(BACKUP_INDEX_FILE_NAME, oldIndexText,new WriteCallback(){

						@Override
						public void onError(String message, Object option) {
							onIndexUpdateFaild(createErrorMessage("on-write-backup-index:"+message,option));
						}

						@Override
						public void onWriteEnd(FileEntry file) {
							
							fileSystem.updateData(INDEX_FILE_NAME, indexText,new WriteCallback(){

								@Override
								public void onError(String message, Object option) {
									onIndexUpdateFaild(createErrorMessage(message,option));
								}

								@Override
								public void onWriteEnd(FileEntry file) {
									onIndexUpdateComplete();
									
								}} );
							
							
						}} );
				}
				
				@Override
				public void storageInfoUsageCallback(double currentUsageInBytes, double currentQuotaInBytes) {
					final int remain=(int)(currentQuotaInBytes-currentUsageInBytes);
					
					readIndex(new ReadStringCallback() {
						
						@Override
						public void onError(String message, Object option) {
							//onReadIndexFaild(createErrorMessage(message, option));
							//possible after initialized.
							doWriteIndex(remain,"");
						}
						
						@Override
						public void onReadString(String oldIndexText, FileEntry file) {
							//compare new & old
							//alert if totally different,do more things on settings
							//TODO
							doWriteIndex(remain,oldIndexText);
							
						}
					});
					
				}});
			
			
			
		}
		
		public String createIndex(){
			return Joiner.on("\n").join(FluentIterable.from(rawList).transform(new Function<T,String>(){
				@Override
				public String apply(T input) {
					return getFileName(input);
				}
				
			}));
		}
		
		public abstract String getFileName(T data);
		public abstract void setFileName(T data,String fileName);
		public abstract void onReadAllEnd();
	
		public abstract void onReadFaild(String fileName);
		
		public void read(final String fileName,final ReadListener<T> listener) {
			fileSystem.readText(fileName, new ReadStringCallback() {
				@Override
				public void onError(String message, Object option) {
					listener.onError("File not found:"+fileName+","+message+","+option);
				}
				
				@Override
				public void onReadString(String text, FileEntry file) {
					try{
					T data=converter.reverse().convert(text);
					setFileName(data, fileName);//need set filename on read
					listener.onRead(data);
					}catch(Exception e){
						listener.onError("convert faild:"+fileName+","+e.getMessage());
					}
				}
			});
		}
		
		public interface ReadListener<T>{
			public void onError(String message);
			public void onRead(T data);
		}
		public void readIndex(ReadStringCallback callback){
			fileSystem.readText(INDEX_FILE_NAME,callback);
		}
		
		public void readAll(){
			checkState(initialized);
			checkState(!loaded && !loading);
			loading=true;
			
			fileSystem.readText(INDEX_FILE_NAME, new ReadStringCallback() {
				
				@Override
				public void onError(String message, Object option) {
					//TODO add indexRead faild,possible
					LogUtils.log("index - not found:possible after initialized."+message+","+option);
					
					loaded=true;//allow only call once
					loading=false;
					onReadAllEnd();
					onDataUpdate();
				}
				
				@Override
				public void onReadString(String text, FileEntry file) {
					//LogUtils.log("index-read:"+text);
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
									try{
									T data=converter.reverse().convert(text);
									setFileName(data, fileName);//need set filename on read
									delegate().add(data);//simplly add list
									done(fileName, true);
									}catch(Exception e){
										done(fileName,false);//TODO support faild action
									}
								}
							});
						}
						@Override
						public void onFaild(String data){
							onReadFaild(data);
						}
						
						@Override
						public void doFinally(boolean cancelled){
							loaded=true;//allow only call once
							loading=false;
							onReadAllEnd();
							onDataUpdate();
						}
						
						
					};
					caller.startCall();
					
				}
			});
		}
	}