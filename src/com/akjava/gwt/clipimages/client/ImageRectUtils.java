package com.akjava.gwt.clipimages.client;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.annotation.Nullable;

import com.akjava.gwt.lib.client.CanvasUtils;
import com.akjava.gwt.lib.client.ImageElementUtils;
import com.akjava.lib.common.graphics.Rect;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.ImageElement;

public class ImageRectUtils {
private static Canvas sharedCanvas=Canvas.createIfSupported();//TODO delay
	public static Canvas crop(String dataUrl,Rect rect,@Nullable Canvas retCanvas){
		checkNotNull(dataUrl);
		checkNotNull(rect);
		Canvas imageCanvas=ImageElementUtils.copytoCanvas(dataUrl, sharedCanvas);
		if(retCanvas==null){
			retCanvas=Canvas.createIfSupported();
		}
		CanvasUtils.createCanvas(retCanvas, rect.getWidth(),rect.getHeight());
		retCanvas.getContext2d().drawImage(imageCanvas.getCanvasElement(), -rect.getX(), -rect.getY());
		return retCanvas;
	}
	
	public static Canvas crop(ImageElement image,Rect rect,@Nullable Canvas retCanvas){
		checkNotNull(image);
		checkNotNull(rect);
		
		if(retCanvas==null){
			retCanvas=Canvas.createIfSupported();
		}
		//use imagedata style can't crop over pos
		CanvasUtils.createCanvas(retCanvas, rect.getWidth(),rect.getHeight());
		retCanvas.getContext2d().drawImage(image, -rect.getX(), -rect.getY());
		return retCanvas;
	}
}
