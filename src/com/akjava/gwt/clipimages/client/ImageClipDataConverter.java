package com.akjava.gwt.clipimages.client;

import com.akjava.gwt.lib.client.JsonValueUtils;
import com.akjava.gwt.lib.client.LogUtils;
import com.akjava.lib.common.graphics.Rect;
import com.google.common.base.Converter;
import com.google.common.base.Verify;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class ImageClipDataConverter extends Converter<ImageClipData, String>{
	@Override
	protected String doForward(ImageClipData data) {
		JSONObject object=new JSONObject();
		
		if(data.getTitle()!=null){
object.put("title", new JSONString(data.getTitle()));
}if(data.getDescription()!=null){
object.put("description", new JSONString(data.getDescription()));
}if(data.getImageData()!=null){
object.put("imageData", new JSONString(data.getImageData()));
}if(data.getRects()!=null){
	JSONArray array=new JSONArray();
	
	for(int i=0;i<data.getRects().size();i++){
		Rect rect=data.getRects().get(i);
		array.set(i, new JSONString(rect.toKanmaString()));
	}
	
	object.put("rects", array);
}
return object.toString();
		
		
		// TODO Auto-generated method stub
		//return Optional.fromNullable(a.getId()).or("")+","+Optional.fromNullable(a.getTitle()).or("");
	}
	
	@Override
	protected ImageClipData doBackward(String b) {
		
		JSONValue value=JSONParser.parseLenient(b);
		JSONObject object=value.isObject();
		Verify.verify(object!=null, "invalid data format");
		
		ImageClipData data=new ImageClipData();
		
		
		data.setTitle(JsonValueUtils.getString(object, "title", null));
		data.setDescription(JsonValueUtils.getString(object, "description", null));
		data.setImageData(JsonValueUtils.getString(object, "imageData", null));
		
		JSONValue rectsValue=object.get("rects");
		if(rectsValue!=null){
			JSONArray rectArray=rectsValue.isArray();
			if(rectArray!=null){
				for(int i=0;i<rectArray.size();i++){
					JSONValue v=rectArray.get(i);
					JSONString string=v.isString();
					if(string!=null){
						data.getRects().add(Rect.fromString(string.stringValue()));
					}
				}
			}
		}else{
			//compatible old version
			String rectString=JsonValueUtils.getString(object, "rect", null);
			if(rectString!=null){
				data.getRects().add(Rect.fromString(rectString));
			}
		}
		
		
		
		return data;
			
	}
}
