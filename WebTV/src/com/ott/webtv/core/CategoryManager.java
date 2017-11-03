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

	private static Map<SOURCE_TYPE, CategoryManager> categoryMgr = new HashMap<SOURCE_TYPE, CategoryManager>();
	static SOURCE_TYPE curSourceType;

	private List<Category> list;
	private String[] title;
	private StringBuffer currentPath;

	private CategoryManager() {
		list = new ArrayList<Category>();
	}

	public static CategoryManager getCurrent() {
		return categoryMgr.get(curSourceType);
	}

	public static CategoryManager getCurrent(SOURCE_TYPE source) {
		if (source != null) {
			curSourceType = source;
		}
		CategoryManager mgr = categoryMgr.get(curSourceType);
		if (mgr == null) {
			mgr = new CategoryManager();
			categoryMgr.put(curSourceType, mgr);
		}
		return mgr;
	}

	public static Boolean containVod() {
		return categoryMgr.containsKey(SOURCE_TYPE.VOD);
	}

	public static Boolean containLive() {
		return categoryMgr.containsKey(SOURCE_TYPE.LIVE);
	}

	void addNode(Category node) {
		list.add(node);
	}

	public Category getNode(int index) {
		return list.get(index);
	}

	public String[] getTitles() {
		if (title == null) {
			List<Category> data = list;
			title = new String[data.size()];
			int i = 0;
			for (Category node : data) {
				title[i++] = node.getTitle();
			}
		}

		return title;
	}

	public Category getFirstNode() {
		Category cate = getNode(0);
		currentPath = new StringBuffer(cate.toString());

		while (cate.getChildType() == DATA_TYPE.CATEGORY) {
			cate = cate.getSubNode(0);
			currentPath.append(cate);
		}

		return cate;
	}

	public Category getNodeByIndex(int... index) {
		Category cate = getNode(index[0]);
		currentPath = new StringBuffer(cate.toString());

		for (int i = 1; i < index.length; i++) {
			cate = cate.getSubNode(index[i]);
			currentPath.append(cate);
		}

		return cate;
	}

	String getCurrentPath() {
		return currentPath.toString();
	}

	static void release() {
		categoryMgr.clear();
	}

}
