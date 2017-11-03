package com.ott.webtv.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ott.webtv.core.CategoryManager.SOURCE_TYPE;

import android.annotation.SuppressLint;

public interface DataNode {
	public static enum DATA_TYPE {
		CATEGORY, CONTAINER, VIDEO, SERIAL
	}

	public static enum RESLOUTION {
		LOW, MEDIUM, HIGH
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

		public void setChildType(DATA_TYPE type) {
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
			if (type == DATA_TYPE.CATEGORY){
				subList = new ArrayList<Category>();
			}
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
		private Boolean bPlayFlag = true;
		private Boolean bVodNode;

		private RESLOUTION lastResloution = RESLOUTION.LOW;
		
		protected Content(){
			bVodNode = ALParser.getCurSourceType() == SOURCE_TYPE.VOD;
		}
		
		public void setPicURL(String url) {
			picURL = url;
		}

		public String getPicURL() {
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
		
		public void setPlayDisable(){
			bPlayFlag = false;
		}
		
		public Boolean getPlayFlag(){
			return bPlayFlag;
		}
		
		public Boolean isVodContent(){
			return bVodNode;
		}		
		
		Boolean isDataComplete() {
			return bDataComplete;
		}

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
		private int lastPlayTime;

		public Video() {
			setContentType(DATA_TYPE.VIDEO);
			playURL = new HashMap<RESLOUTION, String>();
		}

		public void addPlayURL(RESLOUTION type, String url) {
			playURL.put(type, url);
		}
	
		public void addPlayURL(Map<RESLOUTION, String> url) {
			playURL = url;
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
		private Map<Integer, Map<RESLOUTION, String>> list;
		private List<String> playerUrl;

		private int totalCount;
		private int[] lastPlayTime;
		private int lastPlayIndex;

		@SuppressLint("UseSparseArrays")
		public Serial() {
			setContentType(DATA_TYPE.SERIAL);
			list = new HashMap<Integer, Map<RESLOUTION, String>>();//SparseArray can not be serializable
		}

		public void setPlayerUrl(List<String> url) {
			playerUrl = url;
		}

		public List<String> getPlayerUrl() {
			return playerUrl;
		}

		public void setTotalCount(int count) {
			totalCount = count;
		}

		public void addPlayURL(int index, Map<RESLOUTION, String> url) {
			list.put(Integer.valueOf(index), url);
		}

		public Boolean isSupport(int index,RESLOUTION type) {
			if(list.get(Integer.valueOf(index)) == null){
				return false;
			}
			return list.get(Integer.valueOf(index)).containsKey(type);
		}

		public String getPlayURL(int index, RESLOUTION type) {
			setLastResloution(type);
			return list.get(Integer.valueOf(index)).get(type);
		}

		public int getTotalCount() {
			return totalCount > 0 ? totalCount : list.size();
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
