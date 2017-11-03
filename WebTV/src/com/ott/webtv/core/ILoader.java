package com.ott.webtv.core;

import com.ott.webtv.core.CategoryManager.SOURCE_TYPE;
import com.ott.webtv.core.DataNode.BaseNode;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.DATA_TYPE;

public interface ILoader {
	public void loadCategory(String src);

	public void loadContentPage(SOURCE_TYPE type, BaseNode node);

	public void loadSearchPage(DATA_TYPE searchType, String value);

	public void loadPageList(int page);

	public void loadExtraInfo(Content node);
	
	public void loadPlayUrl(Content node, int index);

	public void loadCustomerData(int arg1, int arg2, Object obj);
}
