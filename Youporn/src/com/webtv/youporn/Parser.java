package com.webtv.youporn;

import java.util.List;

import com.ott.webtv.core.ALParser;
import com.ott.webtv.core.CategoryManager.SOURCE_TYPE;
import com.ott.webtv.core.DataNode.BaseNode;
import com.ott.webtv.core.DataNode.Category;
import com.ott.webtv.core.DataNode.Container;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.DATA_TYPE;
import com.ott.webtv.core.DataNode.RESLOUTION;
import com.ott.webtv.core.DataNode.Video;
import com.ott.webtv.core.HttpUtils;

public class Parser extends ALParser {

	private static final String sHost = "http://www.youporn.com";
	private static final String sPageNum = "?page=";
	private static final String sSearchParam = "/search/relevance/?query=";
	private static final String sSearchPage = "&page=";

	private static final int pageSize = 30;
	private static final int starPageSize = 31;
	private static final int starContentSize = 24;
	private static final int searchPageSize = 36;

	public Parser() {
		// TODO Auto-generated constructor stub
		super(pageSize);
	}

	@Override
	protected int parseSubCategory(String net_data, Category parrent) {
		// TODO Auto-generated method stub
		int start = net_data.indexOf("categoriesMenuTab");
		int end = net_data.indexOf("categoryTopCountries");

		if (start == -1 || end == -1) {
			return PARSE_ERROR;
		}
		String data = net_data.substring(start, end);

		start = data.indexOf("<ul>");

		while ((start = data.indexOf("href", start)) != -1) {
			Category cate = new Category();

			start += "href=\"".length();
			end = data.indexOf("\">", start);
			cate.setURL(data.substring(start, end));

			start = end + "\">".length();
			end = data.indexOf("</", start);
			cate.setTitle(data.substring(start, end));

			cate.setChildType(DATA_TYPE.VIDEO);

			parrent.addSubNode(cate);
		}

		return PARSE_SUCCESS;
	}

	@Override
	protected String makeContentURL(int page, BaseNode node) {
		// TODO Auto-generated method stub
		return sHost + node.getURL() + sPageNum + page;
	}

	@Override
	protected String makeSearchURL(int page, String value) {
		// TODO Auto-generated method stub
		return sHost + sSearchParam + value + sSearchPage + page;
	}

	@Override
	public void loadContentPage(SOURCE_TYPE type, BaseNode node) {
		if (node.getChildType() == DATA_TYPE.CONTAINER) {
			setPageSize(starPageSize);
		} else if (node instanceof Container) {
			setPageSize(starContentSize);
		} else {
			setPageSize(pageSize);
		}

		super.loadContentPage(type, node);
	}

	@Override
	public void loadSearchPage(DATA_TYPE searchType, String value) {
		setPageSize(searchPageSize);
		super.loadSearchPage(searchType, value);
	}

	@Override
	protected int parseContentList(String net_data, Boolean bInSearch,
			List<Content> out) {
		// TODO Auto-generated method stub
		int count = 0;

		int ret = parseTotalPage(net_data);
		if (ret != PARSE_SUCCESS) {
			return ret;
		}

		if (getCurData().getChildType() == DATA_TYPE.CONTAINER) {
			return parseStars(net_data, out);
		}

		int start = -1;
		int end = -1;

		if ((start = net_data.indexOf("data-thumbnail")) == -1) {
			return PARSE_NORESULT;
		}

		while (start != -1) {

			if (!bInSearch && ++count > pageSize) {
				break;
			}

			Video video = new Video();

			start += "data-thumbnail=\"".length();
			end = net_data.indexOf("\" ", start);
			String pic = net_data.substring(start, end);
			video.setPicURL(pic);
			video.setLargePicURL(pic);

			if ((start = net_data.indexOf("videoTitle\" href=\"", start)) == -1) {
				return PARSE_ERROR;
			}

			start += "videoTitle\" href=\"".length();
			end = net_data.indexOf("\">", start);
			video.setURL(net_data.substring(start, end));

			start = end + "\">".length();
			end = net_data.indexOf("</a>", start);
			video.setTitle(net_data.substring(start, end));

			video.setDescription(parseDescroption(net_data, end));

			out.add(video);

			start = net_data.indexOf("data-thumbnail", end);
		}

		return PARSE_SUCCESS;

	}

	private String parseDescroption(final String src, int index) {
		int start = -1;
		int end = -1;
		StringBuffer data = new StringBuffer();

		start = src.indexOf("duration", index);
		start += "duration\">".length();
		end = src.indexOf("<", start);
		data.append("length: " + src.substring(start, end));
		data.append("\n");

		start = src.indexOf("views", end);
		start += "views\">".length();
		end = src.indexOf("<", start);
		data.append("views: " + src.substring(start, end));
		data.append("\n");

		start = src.indexOf("rating up", end);
		start += "rating up\">\n".length();
		end = src.indexOf("<", start);
		data.append("rating: " + src.substring(start, end));

		return data.toString();
	}

	private int parseStars(String net_data, List<Content> out) {
		int start = -1;
		int end = -1;

		if ((start = net_data.indexOf("albumWrapper")) == -1) {
			return PARSE_NORESULT;
		}

		while (start != -1) {

			Container star = new Container();

			if ((start = net_data.indexOf("a href=\"", start)) == -1) {
				return PARSE_ERROR;
			}
			start += "a href=\"".length();
			end = net_data.indexOf("\">", start);
			star.setURL(net_data.substring(start, end));

			if ((start = net_data.indexOf("img src=\"", end)) == -1) {
				return PARSE_ERROR;
			}
			start += "img src=\"".length();
			end = net_data.indexOf("\" ", start);
			star.setPicURL(net_data.substring(start, end));

			if ((start = net_data.indexOf("alt=\"", end)) == -1) {
				return PARSE_ERROR;
			}
			start += "alt=\"".length();
			end = net_data.indexOf("\"/>", start);
			star.setTitle(net_data.substring(start, end));

			out.add(star);
			start = net_data.indexOf("albumWrapper", end);
		}

		return PARSE_SUCCESS;
	}

	private int parseTotalPage(String net_data) {
		int ret = PARSE_SUCCESS;

		do {
			if (getTotalCount() > 0) {
				break;
			}
			int start = -1;
			int end = -1;

			if ((start = net_data.indexOf("pagination")) == -1
					|| (end = net_data.indexOf("prev-next", start)) == -1) {
				ret = PARSE_ERROR;
				break;
			}

			String page = net_data.substring(start, end);
			if ((start = page.lastIndexOf("page=")) == -1) {
				ret = PARSE_ERROR;
				break;
			}

			start += "page=".length();
			end = page.indexOf("\">", start);

			setCountByPage(Integer.parseInt(page.substring(start, end)));

		} while (false);

		return ret;
	}

	@Override
	protected int parseExtraInfo(Content node) {
		// TODO Auto-generated method stub
		int start, end = -1;
		Video video = (Video) node;

		String data = new HttpUtils(sHost + node.getURL()).execute()
				.getResult();
		
		if(data == null){
			return PARSE_IOERROR;
		}
		
		if ((start = data.indexOf("$video.src")) == -1
				|| (end = data.indexOf("';", start)) == -1) {
			return PARSE_ERROR;
		}

		video.addPlayURL(RESLOUTION.SD,
				data.substring(start + "$video.src= ' ".length(), end));
		
		return PARSE_SUCCESS;
	}

}
