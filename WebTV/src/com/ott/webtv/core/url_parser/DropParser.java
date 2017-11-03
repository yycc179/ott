package com.ott.webtv.core.url_parser;

import java.util.HashMap;
import java.util.Map;

import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.DataNode.RESLOUTION;

public class DropParser extends URLParser {

	private static final String DROP_HOST = "http://dropvideo.com/";

	@Override
	protected Boolean isURLCorrect(String url) {
		// TODO Auto-generated method stub
		return url.startsWith(DROP_HOST);
	}

	@Override
	protected Map<RESLOUTION, String> parseURL(String url) {
		String playUrlLow = null;
		// String playUrlHD = null;
		int lowIdx = -1;
		// int hdIdx = -1;

		Map<RESLOUTION, String> urlMap = new HashMap<RESLOUTION, String>();

		String net_data = new HttpUtils(url).execute().getResult();

		if (net_data == null || "".equals(net_data.trim())) {
			return null;
		}

		if ((lowIdx = net_data.indexOf("var vurl2 = \"")) != -1) {
			lowIdx += "var vurl2 = \"".length();
			net_data = net_data.substring(lowIdx);
			playUrlLow = net_data.substring(0, net_data.indexOf("\""));
		}

		if (playUrlLow != null && !"".equals(playUrlLow.trim())) {
			urlMap.put(RESLOUTION.LOW, playUrlLow);
		}

		return urlMap;
	}

}
