/**
 * @author y.yang
 * @version v1.0.0 
 * @date 4/18/2014
 * 
 */

package com.webtv.aljazeera;

import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ott.webtv.core.ALParser;
import com.ott.webtv.core.CategoryManager.SOURCE_TYPE;
import com.ott.webtv.core.DataNode.BaseNode;
import com.ott.webtv.core.DataNode.Category;
import com.ott.webtv.core.DataNode.Content;
import com.ott.webtv.core.DataNode.RESLOUTION;
import com.ott.webtv.core.DataNode.Video;

public class Parser extends ALParser {
	private static final int pageSize = 20;

	private static final String sAddr = "http://api.brightcove.com/services/library?command=search_videos&all=";
	private static final String sTag = "tag:";
	private static final String sPageSize = "&page_size=" + pageSize;
	private static final String sPageNum = "&token=7kITGpNoLcepAAN-8u7b--bguf8F_S5pxbVFh7l8ihCoiMkqltT0JA..&page_number=";
	private static final String suffix = "&get_item_count=true&sort_by=modified_date:desc";

	public Parser() {
		// TODO Auto-generated constructor stub
		super(pageSize);
	}

	@Override
	protected int parseSubCategory(final String net_data, Category parrent) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected String makeContentURL(int page, BaseNode node) {
		// TODO Auto-generated method stub
		if (getCurSourceType() == SOURCE_TYPE.LIVE) {
			return node.getURL();
		}
		return sAddr + sTag + node.getURL() + sPageSize + sPageNum + (page - 1)
				+ suffix;
	}

	@Override
	protected String makeSearchURL(int page, String value) {
		// TODO Auto-generated method stub
		return sAddr + value + sPageSize + sPageNum + (page - 1) + suffix;
	}

	@Override
	protected int parseContentList(final String net_data, Boolean bInSearch,
			List<Content> out) {
		// TODO Auto-generated method stub
		if (getCurSourceType() == SOURCE_TYPE.LIVE && !bInSearch) {
			return parseLivePage(net_data, out);
		}

		try {
			JSONObject jsObject = new JSONObject(net_data);
			JSONArray numberList = jsObject.getJSONArray("items");

			int tot = jsObject.getInt("total_count");
			super.setTotalCount(tot);

			int len = numberList.length();
			if (len == 0) {
				return PARSE_NORESULT;
			}

			JSONObject peerObj = null;
			Video video = null;

			for (int i = 0; i < len; i++) {
				peerObj = numberList.getJSONObject(i);
				video = new Video();
				video.setTitle(peerObj.getString("name"));
				String small = peerObj.getString("thumbnailURL");
				String big = peerObj.getString("videoStillURL");
				if(small.endsWith("null")){
					small = big;
				}
				video.setPicURL(small);
				video.setLargePicURL(big);

				String url = peerObj.getString("FLVURL");
				video.addPlayURL(RESLOUTION.SD, url.replaceAll("&mp4:", ""));

				parseExtraInfo(peerObj.getLong("publishedDate"),
						peerObj.getInt("length"),
						peerObj.getString("shortDescription"), video);

				out.add(video);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return PARSE_ERROR;
		}

		return PARSE_SUCCESS;
	}

	private int parseLivePage(String net_data, List<Content> out) {
		Video video = new Video();
		int start = -1;
		int end = -1;

		video.setTitle("Al Jazeera English Live News Streaming");
		start = net_data.indexOf("image\" content=\"");
		end = net_data.indexOf("\">", start);

		if (start == -1 || end == -1) {
			return PARSE_ERROR;
		}
		video.setPicURL(new String(net_data.substring(start
				+ "image\" content=\"".length(), end)));

		start = net_data.indexOf("383px\" src=\"");
		end = net_data.indexOf("\" controls", start);

		if (start == -1 || end == -1) {
			return PARSE_ERROR;
		}
		video.addPlayURL(
				RESLOUTION.SD,
				new String(net_data.substring(
						start + "383px\" src=\"".length(), end)));

		out.add(video);

		setTotalCount(1);
		return PARSE_SUCCESS;

	}

	private String convertTime(int ms) {
		int h = 0, d = 0, s = 0;
		int sec = ms / 1000;
		s = sec % 60;
		
		if(ms % 1000 > 500){
			s += 1;
		}
		
		sec = sec / 60;
		d = sec % 60;
		h = sec / 60;
		return String.format("%02d:%02d:%02d", h, d, s);
	}
	
	private int parseExtraInfo(long time, int len, String desp, Content node) {
		StringBuffer s = new StringBuffer();

		s.append("length: " + convertTime(len));
		s.append("\n\n");
		s.append("pub date: " + new Date(time).toString());
		s.append("\n\n");
		s.append(desp);

		node.setDescription(s.toString());
		return PARSE_SUCCESS;
	}

	@Override
	protected int parseExtraInfo(Content node) {
		// TODO Auto-generated method stub

		return 0;
	}

}
