package com.akjava.gwt.clipimages.client;

import java.util.List;


import com.akjava.gwt.lib.client.LogUtils;
import com.google.common.base.Converter;

public abstract class AbstractFileSystemList<T> extends  IndexBasedAsyncFileSystemList<T>{

	public AbstractFileSystemList(String rootDir, List<T> list, Converter<T, String> converter) {
		super(rootDir, list, converter);
		initialize();
	}

	protected boolean debug;
	

	public void onLog(String log){
		if(debug){
			LogUtils.log(log);
		}
	}
	public abstract void onError(String error);
	
	

	@Override
	public void onRemoveComplete(String fileName) {
		
			onLog("remove-complete:"+fileName);
	}

	@Override
	public void onRemoveFaild(String fileName, String errorMessage) {
		
		onError("remove-faild:"+fileName+",error="+errorMessage);
	}

	@Override
	public void onUpdateComplete(String fileName) {
		
		onLog("update-complete:"+fileName);
		
	}

	@Override
	public void onUpdateFaild(String fileName, String errorMessage) {
		onError("update-faild:"+fileName+",error="+errorMessage);
	}
	
	@Override
	public void onReadIndexFaild(String errorMessage) {
		onError("read-index-faild:,error="+errorMessage);
	}

	@Override
	public void onAddComplete(String fileName) {
		onLog("add-complete:"+fileName);
	}

	@Override
	public void onAddFaild(String fileName, String errorMessage) {
		onError("add-faild:"+fileName+",error="+errorMessage);
	}

	@Override
	public void onIndexUpdateComplete() {
		onLog("index-complete:");
	}

	@Override
	public void onIndexUpdateFaild(String errorMessage) {
		onLog("update-index-faild:error="+errorMessage);
	}

	
	/* shoudl i use this.
	@Override
	public String getFileName(T data) {
		//i faild do generic
		return ((HasId)data).getId();
	}

	@Override
	public void setFileName(T data, String fileName) {
		//i faild do generic
		((HasId)data).setId(fileName);
	}
	*/

	@Override
	public void onReadAllEnd() {
		onLog("read-end");
	}



	@Override
	public void onMakedireComplete() {
		onLog("makedir-complete:");
		readAll();
	}



	@Override
	public void onMakedirFaild(String errorMessage) {
		onError("makedir-faild:error="+errorMessage);
	}



	


}
