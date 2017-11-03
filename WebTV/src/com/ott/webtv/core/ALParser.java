package com.ott.webtv.core;

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

public abstract class ALParser implements ILoader {
	static int pageSize;

	private Boolean bInSearchMode;

	private SOURCE_TYPE curSourceType;
	private BaseNode curDataNode;

	private DATA_TYPE curSearchType;
	private String curSearchVal;

	private int total_count;

	private static final int PARSE_CANCEL = 2;
	protected static final int PARSE_NORESULT = 1;
	protected static final int PARSE_SUCCESS = 0;
	protected static final int PARSE_ERROR = -1;
	protected static final int PARSE_IOERROR = -2;

	private static final String STR_NORESULT = "No Result!";
	private static final String STR_ERROR = "Parse Data Fail!";
	private static final String STR_IOERROR = "Load Data Fail!";

	// private static final int MAX_RETRY_TIMES = 3;

	// private CoreHandler core = CoreHandler.getInstace();

	protected ALParser(int size) {
		pageSize = size;
	}

	protected void setPageSize(int size) {
		pageSize = size;
	}

	protected SOURCE_TYPE getCurSourceType() {
		return curSourceType;
	}

	protected DATA_TYPE getSearchType() {
		return curSearchType;
	}

	protected BaseNode getCurData() {
		return curDataNode;
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

	@Override
	public void loadCategory(String src) {
		// TODO Auto-generated method stub
		CategoryManager mgr = CategoryManager.getInstace();
		Callback c_type = Callback.CBK_GET_CATEGORY_DONE;

		do {
			JSONObject demoJson = null;

			try {
				demoJson = new JSONObject(src);

				loadCategory(null, SOURCE_TYPE.VOD,
						demoJson.getJSONArray("VOD"));

				if (!demoJson.isNull("Live")) {
					mgr.setLiveEnable();
					loadCategory(null, SOURCE_TYPE.LIVE,
							demoJson.getJSONArray("Live"));
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				c_type = Callback.CBK_GET_CATEGORY_FAIL;
				break;
			}
		} while (false);

		CoreHandler.getInstace().sendCallback(c_type, 0, 0, null);
	}

	private int loadCategory(Category parrent, SOURCE_TYPE type, JSONArray ja) {
		int len = ja.length();
		int ret = 0;

		JSONObject peerObj = null;
		for (int i = 0; i < len; i++) {
			Category node = new Category();
			try {
				peerObj = ja.getJSONObject(i);

				node.setTitle(peerObj.getString("title"));

				int c_type = peerObj.getInt("childType");
				node.setChildType(DATA_TYPE.values()[c_type]);

				if (!peerObj.isNull("url")) {
					node.setURL(peerObj.getString("url"));

					final Category cate = node;
					if (cate.getChildType() == DATA_TYPE.CATEGORY) {
						new HttpUtils(cate.getURL(), new IParser() {
							@Override
							public void parse(String src) {
								// TODO Auto-generated method
								// stub
								if (parseSubCategory(src, cate) != PARSE_SUCCESS) {
									System.err.println("parseSubCategory");
								}
							}

						}).execute();
					}
				}

				if (!peerObj.isNull("child")) {
					loadCategory(node, null, peerObj.getJSONArray("child"));
				}

				if (parrent == null) {
					CategoryManager.getInstace().addNode(type, node);
				} else {
					parrent.addSubNode(node);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				ret = -1;
			}
		}

		return ret;
	}

	private HttpUtils makeContentRequest(int page) {
		String url = null;

		if (bInSearchMode) {
			url = makeSearchURL(page, curSearchVal);
		} else {
			url = makeContentURL(page, curDataNode);
		}

		System.out.println("Request URL: " + url);

		return new HttpUtils(url).execute();
	}

	private int getCurrentPage(int page) {
		ContentManager mgr = ContentManager.getCurrent();
		int ret = PARSE_SUCCESS;
		Callback c_type = Callback.CBK_GET_CONTENT_FAIL;
		List<Content> tmplist = null;
		String err_msg = null;
		
		int p = page;

		if (p != 1) {
			int pageStart = page * ContentManager.contentPageSize;
			p = pageStart / pageSize;
			int b = pageStart % pageSize;
			p = b > 0 ? p + 1 : p;
		}

		String data = makeContentRequest(p).getResult();

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
			System.out.println("_________Get data done__________");
			if (page == 1) {
				mgr = ContentManager.newManager();
				mgr.setPageByCount(total_count);
			}

			mgr.addList(tmplist);
			mgr.prepareData(page);
			c_type = Callback.CBK_GET_CONTENT_DONE;

			break;

		case PARSE_CANCEL:
			c_type = Callback.CBK_CANCEL_CMOMAND;
			HttpUtils.setCancel(false);
			break;

		case PARSE_NORESULT:
			err_msg = STR_NORESULT;
			break;

		case PARSE_ERROR:
			err_msg = STR_ERROR;
			break;

		case PARSE_IOERROR:
			err_msg = STR_IOERROR;
			break;
		}

		CoreHandler.getInstace().sendCallback(c_type, page, 0, err_msg);

		if (ret == PARSE_SUCCESS) {
			preLoadOtherPage();
		}

		return ret;
	}

	private void preLoadOtherPage() {
		ContentManager mgr = ContentManager.getCurrent();
		List<Content> tmplist = null;
		int page = mgr.getCachePageNum();

		if (page != -1) {
			String data = makeContentRequest(page).getResult();

			if (data != null) {
				tmplist = new ArrayList<Content>();
				parseContentList(data, bInSearchMode, tmplist);
				mgr.addList(tmplist);
			}

		}
	}

	@Override
	public void loadContentPage(SOURCE_TYPE type, BaseNode node) {
		// TODO Auto-generated method stub
		Boolean b_search = bInSearchMode;
		SOURCE_TYPE cur_type = curSourceType;
		BaseNode cur_node = curDataNode;

		bInSearchMode = false;
		curSourceType = type;
		curDataNode = node;

		if (getCurrentPage(1) == PARSE_CANCEL) {
			bInSearchMode = b_search;
			curSourceType = cur_type;
			curDataNode = cur_node;
		}
	}

	@Override
	public void loadSearchPage(DATA_TYPE searchType, String value) {
		// TODO Auto-generated method stub

		Boolean b_search = bInSearchMode;
		DATA_TYPE cur_type = curSearchType;
		String cur_val = curSearchVal;

		bInSearchMode = true;
		curSearchType = searchType;
		curSearchVal = value;

		if (getCurrentPage(1) == PARSE_CANCEL) {
			bInSearchMode = b_search;
			curSearchType = cur_type;
			curSearchVal = cur_val;
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
		DATA_TYPE d_type = node.getContentType();
		do {
			if (node.isDataComplete()) {
				break;
			}

			if (d_type == DATA_TYPE.CATEGORY || d_type == DATA_TYPE.CONTAINER
					|| parseExtraInfo(node) != PARSE_SUCCESS) {
				c_type = Callback.CBK_GET_EXTRA_FAIL;
				break;
			}

			node.setDataComplete();
		} while (false);

		CoreHandler.getInstace().sendCallback(c_type, 0, 0, node);
	}

	protected abstract int parseSubCategory(final String net_data,
			Category parrent);

	protected abstract String makeContentURL(int page, BaseNode node);

	protected abstract String makeSearchURL(int page, String value);

	protected abstract int parseContentList(final String net_data,
			Boolean bInSearch, List<Content> out);

	protected abstract int parseExtraInfo(Content node);
}

interface IParser {
	public void parse(String src);
}