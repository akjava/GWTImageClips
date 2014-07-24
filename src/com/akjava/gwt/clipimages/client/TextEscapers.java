package com.akjava.gwt.clipimages.client;

import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

public class TextEscapers {
	
public static Escaper getInTabTextEscaper(){
	return IN_TAB_TEXT;
}

static Escaper IN_TAB_TEXT;
static{
	 Escapers.Builder builder = Escapers.builder();//need?
	 builder.addEscape('\t', "");
	 builder.addEscape('\r', "");
	 builder.addEscape('\n', "");
	 IN_TAB_TEXT=builder.build();
}

}
