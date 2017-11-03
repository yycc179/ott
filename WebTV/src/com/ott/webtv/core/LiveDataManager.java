package com.ott.webtv.core;

import java.util.ArrayList;
import java.util.List;

public class LiveDataManager{
	
	private static LiveDataManager liveData = new LiveDataManager();
	
	private ArrayList<LiveBaseNode> chnList = new ArrayList<LiveBaseNode>();
	private ArrayList<LiveBaseNode> favList = new ArrayList<LiveBaseNode>();
	private ArrayList<EventInfo> curEventList = new ArrayList<EventInfo>();
	private ArrayList<EventInfo> epgList = new ArrayList<EventInfo>();
	
	public void clearData(){
		chnList.clear();
		favList.clear();
		curEventList.clear();
		epgList.clear();
	}
	
	public static LiveDataManager getInstance(){
		return liveData;
	}
	
	public ArrayList<LiveBaseNode> getChnList(){
		return chnList;
	}

	public ArrayList<LiveBaseNode> getFavList(){
		return favList;
	}
	
	public ArrayList<EventInfo> getCurEventList() {
		return curEventList;
	}
	
	public ArrayList<EventInfo> getEpgList() {
		return epgList;
	}
	
	public void setChnList(ArrayList<LiveBaseNode> list){
		chnList = list;
	}
	
	public void setFavList(ArrayList<LiveBaseNode> list){
		favList = list;
	}

	public void setCurEventList(ArrayList<EventInfo> EventList) {
		this.curEventList = EventList;
	}

	public void setEpgList(ArrayList<EventInfo> epgList) {
		this.epgList = epgList;
	}

	public static class LiveBaseNode{
		private int cid=0;
		private boolean isProtected = false;
		private String chnName;

		public void setChnName(String chnName) {
			this.chnName = chnName;
		}
		
		public void setProtected(boolean isProtected) {
			this.isProtected = isProtected;
		}
		
		public void setCid(int cid) {
			this.cid = cid;
		}
		
		public boolean isProtected() {
			return isProtected;
		}
		
		public String getChnName() {
			return chnName;
		}
		
		public int getCid() {
			return cid;
		}
		
	}
	
	public static class EventInfo extends LiveBaseNode{
		private long startTime;
		private long endTime;
		private String proName;
		private long curDaily;
		
		public void setStartTime(long startTime) {
			this.startTime = startTime;
		}
		
		public void setEndTime(long endTime) {
			this.endTime = endTime;
		}
		
		public void setCurProName(String ProName) {
			this.proName = ProName;
		}

		public long getStartTime() {
			return startTime;
		}

		public long getEndTime() {
			return endTime;
		}

		public String getCurProName() {
			return proName;
		}
		
		public void setCurDaily(long curDaily) {
			this.curDaily = curDaily;
		}
		
		public long getCurDaily() {
			return curDaily;
		}
	}
	
	public static class CodeManager extends LiveBaseNode{
		private String parentCode;
		private String newCode;
		private String playUrl;
		
		public String getPlayUrl() {
			return playUrl;
		}
		
		public void setPlayUrl(String playUrl) {
			this.playUrl = playUrl;
		}
		
		public String getParentCode() {
			return parentCode;
		}
		
		public void setParentCode(String parentCode) {
			this.parentCode = parentCode;
		}
		
		public String getNewCode() {
			return newCode;
		}
		
		public void setNewCode(String newCode) {
			this.newCode = newCode;
		}
	}
	
}