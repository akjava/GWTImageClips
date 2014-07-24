package com.akjava.gwt.clipimages.client;

import javax.annotation.Nullable;

import com.akjava.lib.common.graphics.Rect;

public class ImageClipData{
		private String id;
		public ImageClipData() {
			this(null,null,new Rect(),null,null);
		}
		public ImageClipData(@Nullable String id,@Nullable String title,Rect rect,@Nullable String imageData,@Nullable String description) {
			super();
			this.id = id;
			this.title = title;
			this.rect=rect;
			this.imageData=imageData;
			this.description=description;
		}
		public String getId() {
			return id;
		}
		public void setId(@Nullable String id) {
			this.id = id;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(@Nullable String title) {
			this.title = title;
		}
		private String title;
		private Rect rect;
		public Rect getRect() {
			return rect;
		}
		public void setRect(Rect rect) {
			this.rect = rect;
		}
		public String getImageData() {
			return imageData;
		}
		public void setImageData(String imageData) {
			this.imageData = imageData;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		private String imageData;
		private String description;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((description == null) ? 0 : description.hashCode());
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			result = prime * result + ((imageData == null) ? 0 : imageData.hashCode());
			result = prime * result + ((rect == null) ? 0 : rect.hashCode());
			result = prime * result + ((title == null) ? 0 : title.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ImageClipData other = (ImageClipData) obj;
			if (description == null) {
				if (other.description != null)
					return false;
			} else if (!description.equals(other.description))
				return false;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (imageData == null) {
				if (other.imageData != null)
					return false;
			} else if (!imageData.equals(other.imageData))
				return false;
			if (rect == null) {
				if (other.rect != null)
					return false;
			} else if (!rect.equals(other.rect))
				return false;
			if (title == null) {
				if (other.title != null)
					return false;
			} else if (!title.equals(other.title))
				return false;
			return true;
		}
		
		public String toString(){
			return ""+getId()+",hash="+hashCode();
		}
		
		
		
	}