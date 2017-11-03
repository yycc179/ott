package com.ott.webtv.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DataNode {
	public static enum DATA_TYPE {
		CATEGORY, CONTAINER, VIDEO, SERIAL
	}

	public static enum RESLOUTION {
		SD, HD720, HD1080
	}

	@SuppressWarnings("serial")
	public static abstract class BaseNode implements Serializable {
		private String title;
		private String url;
		private transient DATA_TYPE childType;

		public void setTitle(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}

		public void setURL(String url) {
			this.url = url;
		}

		public String getURL() {
			return url;
		}

		void setChildType(DATA_TYPE type) {
			this.childType = type;
		}

		public DATA_TYPE getChildType() {
			return childType;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "/" + getTitle();
		}
	}

	@SuppressWarnings("serial")
	public static class Category extends BaseNode {
		private List<Category> subList;

		@Override
		public void setChildType(DATA_TYPE type) {
			// TODO Auto-generated method stub
			if (type == DATA_TYPE.CATEGORY)
				subList = new ArrayList<Category>();
			super.setChildType(type);
		}

		public void addSubNode(Category node) {
			subList.add(node);
		}

		public Category getSubNode(int index) {
			return subList.get(index);
		}

		public Boolean hasSubCategory() {
			return subList != null && subList.size() > 0;
		}

		public String[] getSubTitles() {
			if (subList == null || subList.size() == 0)
				return null;

			String name[] = new String[subList.size()];
			int i = 0;
			for (Category node : subList) {
				name[i++] = node.getTitle();
			}

			return name;
		}
	}

	@SuppressWarnings("serial")
	public static abstract class Content extends BaseNode {
		private String picURL;
		private String largePicURL;
		private String description;
		private DATA_TYPE contentType;
		private Boolean bDataComplete = false;

		private RESLOUTION lastResloution = RESLOUTION.SD;

		public void setPicURL(String url) {
			picURL = url;
		}

		String getPicURL() {
			return picURL;
		}

		public void setLargePicURL(String url) {
			largePicURL = url;
		}

		public String getLargePicURL() {
			return largePicURL;
		}

		public void setDescription(String desp) {
			description = desp;
		}

		public String getDescription() {
			return description;
		}		
		
		void setDataComplete() {
			bDataComplete = true;
		}

		Boolean isDataComplete() {
			return bDataComplete;
		}
//
//		public Boolean hasLargePic() {
//			return largePicURL != null;
//		}

		void setContentType(DATA_TYPE type) {
			this.contentType = type;
		}

		public DATA_TYPE getContentType() {
			return contentType;
		}

		void setLastResloution(RESLOUTION type) {
			lastResloution = type;
		}

		public RESLOUTION getLastResloution() {
			return lastResloution;
		}

		@Override
		public boolean equals(Object o) {
			// TODO Auto-generated method stub
			return o instanceof Content
					&& ((Content) o).getTitle().equals(this.getTitle())
					&& ((Content) o).picURL.equals(this.picURL);
		}

		@Override
		public int hashCode() {
			// TODO Auto-generated method stub
			return getTitle().hashCode() + picURL.hashCode();
		}

	}

	@SuppressWarnings("serial")
	public static class Container extends Content {
		public Container() {
			setContentType(DATA_TYPE.CONTAINER);
		}

		@Override
		public void setLargePicURL(String url) {
			// TODO Auto-generated method stub
		}

		@Override
		public void setDescription(String desp) {
			// TODO Auto-generated method stub
		}

		@Override
		public RESLOUTION getLastResloution() {
			return null;
		}
	}

	public static class Video extends Content {
		private static final long serialVersionUID = 5376117619091907347L;
		private Map<RESLOUTION, String> playURL;
//		public transient String date;
//		public transient String author;
//		public transient String duration;

		private int lastPlayTime;

		public Video() {
			setContentType(DATA_TYPE.VIDEO);
			playURL = new HashMap<RESLOUTION, String>();
		}

		public void addPlayURL(RESLOUTION type, String url) {
			playURL.put(type, url);
		}

		public Boolean isSupport(RESLOUTION type) {
			return playURL.containsKey(type);
		}

		public String getPlayURL(RESLOUTION type) {
			setLastResloution(type);
			return playURL.get(type);
		}

		public void setLastPlayTime(int sec) {
			lastPlayTime = sec;
		}

		public int getLastPlayTime() {
			return lastPlayTime;
		}

		@Override
		public void setChildType(DATA_TYPE type) {
			// TODO Auto-generated method stub
		}
	}

	public static class Serial extends Content {
		private static final long serialVersionUID = 7330441012721143062L;
		private List<Map<RESLOUTION, String>> list;

		private int[] lastPlayTime;
		private int lastPlayIndex;

		public Serial() {
			setContentType(DATA_TYPE.SERIAL);
			list = new ArrayList<Map<RESLOUTION, String>>();
		}

		public void addOne(Map<RESLOUTION, String> url) {
			list.add(url);
		}

		public Boolean isSupport(RESLOUTION type) {
			return list.get(0).containsKey(type);
		}

		public String getPlayURL(int index, RESLOUTION type) {
			setLastResloution(type);
			return list.get(index).get(type);
		}

		public int getTotalCount() {
			return list.size();
		}

		public void setLastPlayTime(int index, int sec) {
			if (lastPlayTime == null) {
				lastPlayTime = new int[getTotalCount()];
			}

			lastPlayTime[index] = sec;
			lastPlayIndex = index;
		}

		public int getLastPlayTime(int index) {
			if (lastPlayTime == null) {
				lastPlayTime = new int[getTotalCount()];
			}
			return lastPlayTime[index];
		}

		public int getLastPlayIndex() {
			return lastPlayIndex;
		}

		@Override
		public void setChildType(DATA_TYPE type) {
			// TODO Auto-generated method stub
		}
	}

}
