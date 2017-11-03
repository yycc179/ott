package com.ott.webtv.core;

import java.util.ArrayList;
import java.util.List;

import com.ott.webtv.core.DataNode.*;

public class ContentManager {
	static int contentPageSize;
	private static final int MAX_CACHE_PAGE = 5;
	private static int maxCacheCount;
	private static int maxPreLoadCount;
	private static ContentManager curManager;
	private static ContentManager lastManager;

	private int totalPage;
	private int currentPage;

	private String[] titles;
	private String[] urls;

	private List<Content> cacheList;

	private int pageStartIndex;
	private int cacheStartIndex;
	private int cacheEndIndex;

	private Boolean bNextDirection;
	private int totalContent;

	private ContentManager() {
		bNextDirection = true;

		cacheList = new ArrayList<Content>();
	}

	static void initialize(int pageSize) {
		contentPageSize = pageSize;
		maxCacheCount = contentPageSize * MAX_CACHE_PAGE;
		maxPreLoadCount = contentPageSize * (MAX_CACHE_PAGE / 2);
	}

	static ContentManager newManager() {
		lastManager = curManager;
		curManager = new ContentManager();

		return curManager;
	}

	public static ContentManager getCurrent() {
		return curManager;
	}

	public static ContentManager getLast() {
		curManager = lastManager;
		return lastManager;
	}

	void setPageByCount(int count) {
		if (count > 0) {
			int a = count % contentPageSize;
			int page = count / contentPageSize;
			totalPage = a > 0 ? page + 1 : page;
		}
		totalContent = count;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	Boolean prepareData(int page) {
		bNextDirection = currentPage <= page;

		currentPage = page;
		pageStartIndex = (page - 1) * contentPageSize;

		if (cacheEndIndex < totalContent
				&& cacheEndIndex < (pageStartIndex + contentPageSize)) {
			return false;

		} else {
			int dataIndex = pageStartIndex - cacheStartIndex;

			int end = dataIndex + contentPageSize;

			if (cacheList.size() < end) {
				end = cacheList.size();
			}

			List<Content> cur = cacheList.size() < contentPageSize ? cacheList
					: cacheList.subList(dataIndex, end);

			int size = cur.size();
			titles = new String[size];
			urls = new String[size];

			int i = 0;
			for (Content node : cur) {
				titles[i] = node.getTitle();
				urls[i] = node.getPicURL();
				i++;
			}

			return true;
		}

	}

	int getCachePageNum() {
		int page = -1;
		
		if (bNextDirection) {
			if ((cacheEndIndex < totalContent || totalPage == 0)
					&& cacheEndIndex <= (pageStartIndex + maxPreLoadCount)) {
				page = cacheEndIndex / ALParser.pageSize + 1;
			}
		} else if ((cacheStartIndex > 0)
				&& (pageStartIndex < (cacheStartIndex + contentPageSize))) {
			page = cacheStartIndex / ALParser.pageSize;
		}

		if (page != -1) {
			System.out.println("__________Prepare page_____" + page);
		} else {
			System.out.println("__________Data ready______");
		}

		return page;
	}

	// List<Integer> getCachePageIndex() {
	// int start = -1;
	// cacheIndex.clear();
	//
	// if (cacheEndIndex < cacheStartIndex + maxCacheCount) {
	//
	// if (bNextDirection) {
	// if ((cacheEndIndex < totalContent) || (totalPage == 0)) {
	// start = cacheEndIndex / ALParser.pageSize + 1;
	// }
	// } else if (cacheStartIndex > 0) {
	// start = cacheStartIndex / ALParser.pageSize;
	// }
	// }
	//
	// if (start != -1) {
	// System.out.println("Cache webpage : " + page);
	// } else {
	// System.out.println("No need cache");
	// }
	// return cacheIndex;
	// }
	//

	void addList(List<Content> list) {
		if (bNextDirection) {
			addTail(list);
		} else {
			addFirst(list);
		}
	}

	private void addTail(List<Content> list) {
		if (list.size() == 0) {
			return;
		}

		cacheList.addAll(list);
		cacheEndIndex += ALParser.pageSize;

		int tmpIndex = cacheStartIndex + ALParser.pageSize;

		if (cacheEndIndex - tmpIndex >= maxCacheCount) {
			cacheStartIndex = tmpIndex;
			cacheList = cacheList.subList(ALParser.pageSize, cacheList.size());
		}

	}

	private void addFirst(List<Content> list) {
		if (list.size() == 0) {
			return;
		}

		cacheList.addAll(0, list);
		cacheStartIndex -= ALParser.pageSize;

		int tmpIndex = cacheEndIndex - ALParser.pageSize;
		if (tmpIndex - cacheStartIndex >= maxCacheCount) {
			cacheEndIndex = tmpIndex;
			cacheList = cacheList.subList(0, cacheList.size()
					- ALParser.pageSize);
		}
	}

	static void release() {
		curManager = null;
		lastManager = null;
	}

	public Content getNode(int index) {
		return cacheList.get(index + pageStartIndex - cacheStartIndex);
	}

	public String[] getTitles() {
		return titles;
	}

	public String[] getPicURLs() {
		return urls;
	}

}
