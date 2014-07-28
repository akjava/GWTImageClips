package com.akjava.gwt.clipimages.client;

import static com.google.common.base.Preconditions.checkNotNull;

import com.akjava.gwt.lib.client.CanvasUtils;
import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.lib.common.io.FileType;
import com.google.gwt.canvas.client.Canvas;

public class ImageBuilder {
public static Canvas sharedCanvas;
	public static Canvas getSharedCanvas() {
	return sharedCanvas;
}
	
	
public static class WebPBuilder extends ImageBuilder{
	public static WebPBuilder from(String dataUrl){
		checkNotNull(dataUrl);
		return new WebPBuilder(dataUrl);
	}
	
	public static WebPBuilder from(Canvas canvas){
		checkNotNull(canvas);
		return new WebPBuilder(canvas);
	}
	
	private WebPBuilder(String dataUrl){
		super(dataUrl);
		super.onWeebp();
	}
	
	private WebPBuilder(Canvas canvas){
		super(canvas);
		super.onWeebp();
	}
	
	
	@Override
	public ImageBuilder onWeebp(){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public ImageBuilder onPng(){
		throw new UnsupportedOperationException();
	}
	@Override
	public ImageBuilder onJpeg(){
		throw new UnsupportedOperationException();
	}
	
}
	
public static void setSharedCanvas(Canvas sharedCanvas) {
	ImageBuilder.sharedCanvas = sharedCanvas;
}

private Canvas canvas;
private String dataUrl;
protected FileType fileType=FileType.PNG;
	public static ImageBuilder from(String dataUrl){
		checkNotNull(dataUrl);
		return new ImageBuilder(dataUrl);
	}
	
	public static ImageBuilder from(Canvas canvas){
		checkNotNull(canvas);
		return new ImageBuilder(canvas);
	}
	
	private ImageBuilder(String dataUrl){
		this.dataUrl=dataUrl;
	}
	
	private ImageBuilder(Canvas canvas){
		this.canvas=canvas;
	}
	

	
	public ImageBuilder onWeebp(){
		this.fileType=FileType.WEBP;
		return this;
	}
	public ImageBuilder onPng(){
		this.fileType=FileType.PNG;
		return this;
	}
	public ImageBuilder onJpeg(){
		this.fileType=FileType.JPEG;
		return this;
	}
	
	
	
	public String toDataUrl(){
		//need resize?
		Canvas shareCanvas=getSharedCanvas();
		if(shareCanvas==null){
			shareCanvas=Canvas.createIfSupported();
			setSharedCanvas(shareCanvas);
		}
		
		if(dataUrl!=null){
			ImageElementUtils.copytoCanvas(dataUrl, shareCanvas);
			return shareCanvas.toDataUrl(fileType.getMimeType());
		}
		
		if(canvas!=null){
			CanvasUtils.copyTo(canvas, shareCanvas,true);
			return shareCanvas.toDataUrl(fileType.getMimeType());
		}
		
		return null;//other type
	}
}
