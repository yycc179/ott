package com.ott.webtv.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ott.webtv.core.DataNode.*;

public class CategoryManager {
	public static enum SOURCE_TYPE {
		VOD, LIVE
	}

	private static CategoryManager categoryMgr;
	private SOURCE_TYPE curSourceType = SOURCE_TYPE.VOD;

	private Map<SOURCE_TYPE, List<Category>> list;
	private Map<SOURCE_TYPE, String[]> title;

	public static CategoryManager getInstace() {
		if(categoryMgr == null){
			categoryMgr = new CategoryManager();
		}
		return categoryMgr;
	}

	private CategoryManager() {
		list = new HashMap<SOURCE_TYPE, List<Category>>();
		title = new HashMap<SOURCE_TYPE, String[]>();
		list.put(SOURCE_TYPE.VOD, new ArrayList<Category>());
		title.put(SOURCE_TYPE.VOD, null);
	}

	void setLiveEnable() {
		list.put(SOURCE_TYPE.LIVE, new ArrayList<Category>());
		title.put(SOURCE_TYPE.LIVE, null);
	}

	public Boolean getLiveEnable() {
		return list.containsKey(SOURCE_TYPE.LIVE);
	}

	void addNode(SOURCE_TYPE type, Category node) {
		list.get(type).add(node);
	}

	public Category getNode(int index) {
		return list.get(curSourceType).get(index);
	}

	Category getNode(SOURCE_TYPE type, int index) {
		return list.get(type).get(index);
	}

	public String[] getTitles(SOURCE_TYPE type) {
		String[] titles = title.get(type);

		if (titles == null) {
			List<Category> data = list.get(type);
			titles = new String[data.size()];
			int i = 0;
			for (Category node : data) {
				titles[i++] = node.getTitle();
			}
			title.put(type, titles);
		}

		curSourceType = type;
		return titles;
	}

	static void release(){
		categoryMgr = null;
	}
	
}
