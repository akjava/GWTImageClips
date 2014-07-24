package com.akjava.gwt.clipimages.client.custom;

import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.CellList.Style;


import com.google.gwt.resources.client.ImageResource;

public interface SimpleCellListResources extends CellList.Resources{
	 @Source({"clear.gif"})
	public ImageResource clearGif();
	 
     @Source({"simpleCellList.css"})
     @Override
     public Style cellListStyle();
}