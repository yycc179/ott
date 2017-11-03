package com.ott.webtv.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.util.SparseArray;

import com.ott.webtv.core.CategoryManager.SOURCE_TYPE;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.*;

public class ContentManager {
	static int contentPageSize;

	private static final int MAX_CACHE_PAGE = 3;
	private static final int SEARCH_MODE = SOURCE_TYPE.LIVE.ordinal() + 1;
	private static SparseArray<Stack<ContentManager>> mgrStack;
	private static int source;

	private static int maxCacheCount;
	private static int maxPreLoadCount;

	private BaseNode parrent;

	private int totalPage;
	private int lastPage;
	private int currentPage;
	private int finalPageSize;
	private int totalContent;

	private List<Content> cacheList;
	private List<Content> curList;

	private int pageStartIndex;
	private int cacheStartIndex;
	private int cacheEndIndex;

	private Boolean bNextDirection = true;
	private Boolean bSeekPage = false;

	private String currentPath;

	private ContentManager(BaseNode parrent) {
		cacheList = new ArrayList<Content>();
		this.parrent = parrent;
	}

	static void initialize(int pageSize) {
		contentPageSize = pageSize;
		maxCacheCount = contentPageSize * MAX_CACHE_PAGE;
		maxPreLoadCount = contentPageSize * (MAX_CACHE_PAGE / 2);
		mgrStack = new SparseArray<Stack<ContentManager>>();
	}

	private static Stack<ContentManager> getStackManager() {
		Stack<ContentManager> mgr = mgrStack.get(source);

		if (mgr == null) {
			mgr = new Stack<ContentManager>();
			mgrStack.put(source, mgr);
		}

		return mgr;
	}

	static ContentManager newManager(BaseNode parrent) {
		ContentManager mgr = new ContentManager(parrent);
		getStackManager().push(mgr);
		return mgr;
	}

	static Boolean isDataReady() {
		source = CategoryManager.curSourceType.ordinal();
		return getStackManager().size() > 0;
	}

	static void setSearchMode() {
		source = SEARCH_MODE;
		clear();
	}

	static Boolean isSearchMode() {
		return source == SEARCH_MODE;
	}

	public static ContentManager getCurrent() {
		Stack<ContentManager> mgr = getStackManager();

		return mgr.size() > 0 ? mgr.peek() : null;
	}

	public static ContentManager getLast() {
		Stack<ContentManager> mgr = getStackManager();
		mgr.pop();
		return mgr.size() > 0 ? mgr.peek() : null;
	}

	public static int getCurrentLevel() {
		return getStackManager().size();
	}

	static void clear() {
		Stack<ContentManager> mgr = getStackManager();
		if (!mgr.isEmpty()) {
			mgr.clear();
		}
	}

	void reset() {
		currentPage = lastPage;
	}

	BaseNode getParrentNode() {
		return parrent;
	}

	void setPageByCount(int count) {
		if (count > 0) {
			finalPageSize = count % contentPageSize;
			totalPage = count / contentPageSize;
			if (finalPageSize > 0) {
				totalPage += 1;
			} else {
				finalPageSize = ALParser.pageSize;
			}
		}
		totalContent = count;
	}

	Boolean prepareData(int page) {
		int seek = page - currentPage;
		bNextDirection = true;
		bSeekPage = false;

		if (Math.abs(seek) > 1) {
			bSeekPage = true;
			cacheEndIndex = 0;
			cacheList.clear();

		} else if (seek == -1) {
			bNextDirection = false;
		}

		lastPage = currentPage;
		currentPage = page;
		pageStartIndex = (page - 1) * contentPageSize;

		if (((cacheEndIndex < totalContent || totalContent == 0) && cacheEndIndex < (pageStartIndex + contentPageSize))
				|| (!bNextDirection && pageStartIndex < cacheStartIndex)) {
			return false;

		} else {
			int dataIndex = pageStartIndex - cacheStartIndex;
			int end = dataIndex + contentPageSize;
			int size = cacheList.size();

			if (size < end) {
				end = size;
			}

			List<Content> cur = cacheList.subList(dataIndex, end);

			System.out.println(size);
			System.out.println(dataIndex);
			curList = new ArrayList<Content>(cur);

			return true;
		}
	}

	int getCachePageNum() {
		int page = -1;

		if (bNextDirection) {
			if ((cacheEndIndex < totalContent || totalContent == 0)
					&& cacheEndIndex < (pageStartIndex + contentPageSize + maxPreLoadCount)) {
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

	int getSitePage() {
		if (pageStartIndex == 0) {
			return 1;
		}
		int page = pageStartIndex / ALParser.pageSize + 1;

		return page;
	}

	void addList(List<Content> list) {
		int size = list.size();
		size = ALParser.pageSize - size;

		while (size-- > 0) {
			list.add(null);
		}

		if (bNextDirection) {
			addTail(list);
		} else {
			addFirst(list);
		}
	}

	private void addTail(List<Content> list) {
		int size = list.size();

		if (size == 0) {
			return;
		}

		cacheList.addAll(list);

		if (bSeekPage) {
			cacheStartIndex = cacheEndIndex = (pageStartIndex / ALParser.pageSize)
					* ALParser.pageSize;
		}

		cacheEndIndex += size;
		if (totalContent != 0 && cacheEndIndex > totalContent) {
			cacheEndIndex = totalContent;
		}

		if (!bSeekPage) {
			int tmpIndex = cacheStartIndex + ALParser.pageSize;

			if (cacheEndIndex - tmpIndex >= maxCacheCount
					&& tmpIndex < pageStartIndex) {
				cacheStartIndex = tmpIndex;
				cacheList = cacheList.subList(ALParser.pageSize,
						cacheList.size());
			}
		}

	}

	private void addFirst(List<Content> list) {
		if (list.size() == 0) {
			return;
		}

		cacheList.addAll(0, list);

		cacheStartIndex -= ALParser.pageSize;

		int drop = cacheEndIndex == totalContent ? finalPageSize
				: ALParser.pageSize;

		int tmpIndex = cacheEndIndex - drop;
		if (tmpIndex - cacheStartIndex >= maxCacheCount) {
			cacheEndIndex = tmpIndex;
			cacheList = cacheList.subList(0, cacheList.size() - drop);
		}
	}

	public Content getNode(int index) {
		return curList.get(index);
	}

	public List<Content> getCurrentList() {
		return curList;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public String getCurrentPath() {
		do {
			if (currentPath != null) {
				break;
			}

			currentPath = source == SEARCH_MODE ? ALParser.getSearchVal()
					: CategoryManager.getCurrent().getCurrentPath();

			Stack<ContentManager> mgr = mgrStack.get(source);
			int size = mgr.size();

			for (int i = 1; i < size; i++) {
				currentPath += mgr.get(i).getParrentNode();
			}
		} while (false);

		return currentPath;
	}

}
