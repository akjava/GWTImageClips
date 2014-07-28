package com.akjava.gwt.clipimages.client;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PreviewHtmlPanelControler {

	private PopupPanel popup;
	private HTML previewHTML;
	
	
	int w;
	int h;
	private int marginTop=30;
	private VerticalPanel container;
	public PreviewHtmlPanelControler(int w,int h){
		this.w=w;
		this.h=h;
	}
	public PreviewHtmlPanelControler(){
		this(230,800);
	}
	
	public void show(){
		if(popup==null){
			createPreviewPanel();
		}
		popup.show();
	}
	public void hide(){
		if(popup!=null){
			popup.hide();
		}
	}
	
	public void setPreviewHtml(SafeHtml html){
		show();
		previewHTML.setHTML(html);
		
	}
	
	private void createPreviewPanel() {
		popup = new PopupPanel();
		container = new VerticalPanel();
		popup.add(container);
		previewHTML = new HTML();
		previewHTML.setSize(w+"px", h+"px");
		container.add(previewHTML);
		
		popup.show();
		moveToAroundRightTop(popup);
		
		
	}
	
	public VerticalPanel getContainer() {
		return container;
	}
	private void moveToAroundRightTop(PopupPanel dialog){
		int w=Window.getClientWidth();
		int h=Window.getScrollTop();
		int dw=dialog.getOffsetWidth();
		
		dialog.setPopupPosition(w-dw, h+marginTop);
	}
	
}
