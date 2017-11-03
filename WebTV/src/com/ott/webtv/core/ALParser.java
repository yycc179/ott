package com.ott.webtv.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ott.webtv.core.CategoryManager.SOURCE_TYPE;
import com.ott.webtv.core.CoreHandler.Callback;
import com.ott.webtv.core.DataNode.BaseNode;
import com.ott.webtv.core.DataNode.Category;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.DATA_TYPE;
import com.ott.webtv.core.HttpUtils.CBKHandler;

public abstract class ALParser implements ILoader {
	static int pageSize;
	private static String curSearchVal;
	private Boolean bInSearchMode;

	private DATA_TYPE curSearchType;
	private int total_count;

	private static final int PARSE_CANCEL = 2;
	public static final int PARSE_NORESULT = 1;
	public static final int PARSE_SUCCESS = 0;
	public static final int PARSE_ERROR = -1;
	public static final int PARSE_IOERROR = -2;

	protected ALParser(int size) {
		pageSize = size;
	}

	protected void setPageSize(int size) {
		pageSize = size;
	}

	protected static SOURCE_TYPE getCurSourceType() {
		return CategoryManager.curSourceType;
	}

	protected Boolean getSearchFlag() {
		return bInSearchMode;
	}

	protected DATA_TYPE getSearchType() {
		return curSearchType;
	}

	protected BaseNode getCurData() {
		ContentManager mgr = ContentManager.getCurrent();
		return mgr != null ? mgr.getParrentNode() : null;
	}

	protected void setTotalCount(int count) {
		total_count = count;
	}

	protected void setCountByPage(int page) {
		int p = page > 1 ? page - 1 : page;
		total_count = p * pageSize;
	}

	protected int getTotalCount() {
		return total_count;
	}

	protected static String getSearchVal() {
		return curSearchVal;
	}

	@Override
	public void loadCategory(String src) {
		// TODO Auto-generated method stub
		Callback c_type = Callback.CBK_GET_CATEGORY_DONE;

		try {
			JSONObject jobj = new JSONObject(src);

			loadCategory(null, SOURCE_TYPE.VOD, jobj.optJSONArray("VOD"));

			loadCategory(null, SOURCE_TYPE.LIVE, jobj.optJSONArray("Live"));

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			c_type = Callback.CBK_GET_CATEGORY_FAIL;
		}

		CoreHandler.getInstace().sendCallback(c_type, 0, 0, null);
	}

	private void loadCategory(Category parrent, SOURCE_TYPE type, JSONArray ja)
			throws JSONException {

		if (ja == null) {
			return;
		}

		CategoryManager mgr = CategoryManager.getCurrent(type);

		int len = ja.length();
		JSONObject peerObj = null;

		for (int i = 0; i < len; i++) {
			final Category node = new Category();
			peerObj = ja.getJSONObject(i);

			node.setTitle(peerObj.getString("title"));

			int c_type = peerObj.getInt("childType");
			node.setChildType(DATA_TYPE.values()[c_type]);

			if (!peerObj.isNull("url")) {
				node.setURL(peerObj.getString("url"));

				if (node.getChildType() == DATA_TYPE.CATEGORY) {
					new HttpUtils(node.getURL(), new CBKHandler() {
						@Override
						public void handle(byte[] src, Object... attr) {
							// TODO Auto-generated method
							// stub
							if (src != null) {
								parseSubCategory(new String(src), node);
							}
						}

					}).execute();
				}
			}

			if (!peerObj.isNull("child")) {
				loadCategory(node, null, peerObj.getJSONArray("child"));
			}

			if (parrent == null) {
				mgr.addNode(node);
			} else {
				parrent.addSubNode(node);
			}
		}
	}

	private HttpUtils doContentRequest(int page) {
		return bInSearchMode ? makeSearchRequest(page, curSearchVal)
				: makeContentRequest(page, getCurData());
	}

	protected String getContentResult(HttpUtils request) {
		return request.getResult();
	}

	private int getCurrentPage(int page) {
		int ret = PARSE_SUCCESS;
		Callback c_type = Callback.CBK_GET_CONTENT_FAIL;
		ContentManager mgr = ContentManager.getCurrent();
		List<Content> tmplist = null;

		String data = getContentResult(doContentRequest(mgr.getSitePage()));

		if (HttpUtils.getCanceled()) {
			ret = PARSE_CANCEL;
		} else if (data == null) {
			ret = PARSE_IOERROR;
		} else {
			tmplist = new ArrayList<Content>();
			ret = parseContentList(data, bInSearchMode, tmplist);
		}

		switch (ret) {
		case PARSE_SUCCESS:
			if (page == 1) {
				mgr.setPageByCount(total_count);
			}
			mgr.addList(tmplist);

			if (pageSize < ContentManager.contentPageSize) {
				preLoadOtherPage();
			}

			mgr.prepareData(page);
			c_type = Callback.CBK_GET_CONTENT_DONE;
			break;

		case PARSE_CANCEL:
			c_type = Callback.CBK_CANCEL_CMOMAND;
			HttpUtils.setCancel(false);
			break;

		}

		CoreHandler.getInstace().sendCallback(c_type, page, ret, null);

		if (ret == PARSE_SUCCESS) {
			preLoadOtherPage();

		} else {
			mgr.reset();
		}

		return ret;
	}

	private void preLoadOtherPage() {
		ContentManager mgr = ContentManager.getCurrent();
		List<Content> tmplist = null;
		int page = mgr.getCachePageNum();

		if (page != -1) {
			String data = getContentResult(doContentRequest(page));

			if (data != null) {
				tmplist = new ArrayList<Content>();
				if (parseContentList(data, bInSearchMode, tmplist) == PARSE_SUCCESS) {
					mgr.addList(tmplist);
				}
			}
		}
	}

	private void initData(BaseNode parrent) {
		total_count = 0;
		bInSearchMode = ContentManager.isSearchMode();

		ContentManager.newManager(parrent);
	}

	@Override
	public void loadContentPage(SOURCE_TYPE type, BaseNode parrent) {
		// TODO Auto-generated method stub
		initData(parrent);

		if (getCurrentPage(1) != PARSE_SUCCESS) {
			ContentManager.getLast();
		}
	}

	@Override
	public void loadSearchPage(DATA_TYPE searchType, String value) {
		// TODO Auto-generated method stub
		initData(null);
		curSearchType = searchType;

		try {
			curSearchVal = URLEncoder.encode(value, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (getCurrentPage(1) != PARSE_SUCCESS) {
			ContentManager.getLast();
		}
	}

	@Override
	public void loadPageList(int page) {
		ContentManager mgr = ContentManager.getCurrent();

		synchronized (this) {
			if (mgr.prepareData(page)) {
				CoreHandler.getInstace().sendCallback(
						Callback.CBK_GET_CONTENT_DONE, page, 0, null);
				preLoadOtherPage();

			} else {
				System.out.println("Reload page_____ " + page);
				getCurrentPage(page);
			}
		}
	}

	@Override
	public void loadExtraInfo(Content node) {
		// TODO Auto-generated method stub
		Callback c_type = Callback.CBK_GET_EXTRA_DONE;
		int ret = 0;
		do {
			if (node.isDataComplete()) {
				break;
			}

			ret = parseExtraInfo(node);
			if (ret != PARSE_SUCCESS) {
				c_type = Callback.CBK_GET_EXTRA_FAIL;
				break;
			}
			node.setDataComplete();
		} while (false);

		CoreHandler.getInstace().sendCallback(c_type, ret, 0, node);
	}

	@Override
	public void loadPlayUrl(Content node, int index) {
		// TODO Auto-generated method stub
		Callback c_type = Callback.CBK_GET_URL_DONE;
		int ret = parsePlayUrl(node, index);

		if (ret != PARSE_SUCCESS) {
			c_type = Callback.CBK_GET_URL_FAIL;
		}

		CoreHandler.getInstace().sendCallback(c_type, ret, index, node);
	}

	@Override
	public void loadCustomerData(int arg1, int arg2, Object obj) {
		// TODO Auto-generated method stub
		Callback c_type = Callback.CBK_GET_CUSTOMER_DONE;
		int ret = parseCustomerInfo(arg1, arg2, obj);
		if (ret != PARSE_SUCCESS) {
			c_type = Callback.CBK_GET_CUSTOMER_FAIL;
		}

		CoreHandler.getInstace().sendCallback(c_type, arg1, ret, obj);
	}

	protected abstract int parseSubCategory(final String net_data,
			Category parrent);

	protected abstract HttpUtils makeContentRequest(int page, BaseNode node);

	protected abstract HttpUtils makeSearchRequest(int page, String value);

	protected abstract int parseContentList(final String net_data,
			Boolean bInSearch, List<Content> out);

	protected abstract int parseExtraInfo(Content node);

	protected abstract int parsePlayUrl(Content node, int index);

	protected abstract int parseCustomerInfo(int arg1, int arg2, Object obj);
}
