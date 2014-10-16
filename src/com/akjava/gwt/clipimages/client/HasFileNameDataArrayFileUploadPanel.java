package com.akjava.gwt.clipimages.client;

import com.akjava.gwt.html5.client.file.File;
import com.akjava.gwt.html5.client.file.FileUploadForm;
import com.akjava.gwt.html5.client.file.FileUtils;
import com.akjava.gwt.html5.client.file.FileUtils.DataArrayListener;
import com.akjava.gwt.html5.client.file.Uint8Array;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

public abstract class HasFileNameDataArrayFileUploadPanel extends HorizontalPanel {
	private Label headerLabel;
	private Label fileNameLabel;
	private FileUploadForm fileUpload;
	
	public HasFileNameDataArrayFileUploadPanel(String labelName){
		super();
		this.setVerticalAlignment(ALIGN_MIDDLE);
		this.setSpacing(2);
		this.setStylePrimaryName("hasFileNameDataArrayFileUploadPanel");
		
		headerLabel=new Label(labelName);
		add(headerLabel);
		headerLabel.setStylePrimaryName("hasFileNameDataArrayFileUploadPanelHeader");
		
		fileNameLabel=new Label();
		
		fileUpload = FileUtils.createSingleFileUploadForm(new DataArrayListener() {
			@Override
			public void uploaded(File file, Uint8Array array) {
				fileNameLabel.setText(file.getFileName());
				onUploaded(file,array);
			}
		});
		add(fileUpload);
		fileUpload.setStylePrimaryName("hasFileNameDataArrayFileUploadPanelUpload");
		
		add(fileNameLabel);
		fileNameLabel.setStylePrimaryName("hasFileNameDataArrayFileUploadPanelFileName");
		
		//TODO support reset button?
	}
	public void setEnabled(boolean bool){
		fileUpload.setEnabled(bool);
	}
	
	public FileUploadForm getFileUploadForm(){
		return fileUpload;
	}
	
	public void reset(){
		fileNameLabel.setText("");
	}
	
	
	public abstract void onUploaded(File file, Uint8Array array);
	
	
}
