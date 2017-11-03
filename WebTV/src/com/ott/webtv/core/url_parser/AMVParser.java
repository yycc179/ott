package com.ott.webtv.core.url_parser;

import java.util.HashMap;
import java.util.Map;

import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.DataNode.RESLOUTION;

public class AMVParser extends URLParser {

	private static final String AMV_HOST = "http://allmyvideos.net/";

	@Override
	protected Boolean isURLCorrect(String url) {
		// TODO Auto-generated method stub
		return url.startsWith(AMV_HOST);
	}

	@Override
	protected Map<RESLOUTION, String> parseURL(String url) {
		// TODO Auto-generated method stub
		String playUrlLow = null;
		// String playUrlHD = null;
		int lowIdx = -1;
		// int hdIdx = -1;

		Map<RESLOUTION, String> urlMap = new HashMap<RESLOUTION, String>();

		String net_data = new HttpUtils(url).execute().getResult();

		if (net_data == null || "".equals(net_data.trim())) {
			return null;
		}

		if ((lowIdx = net_data.indexOf("stream_filesrv_id")) != -1) {
			net_data = net_data
					.substring(lowIdx + "stream_filesrv_id".length());
		}

		if ((lowIdx = net_data.indexOf("sources")) != -1) {
			net_data = net_data.substring(lowIdx + "sources".length());
			if ((lowIdx = net_data.indexOf("\"file\" : \"")) != -1) {
				lowIdx += "\"file\" : \"".length();
				net_data = net_data.substring(lowIdx);
				playUrlLow = net_data.substring(0, net_data.indexOf("\""));
			}
		}

		if (playUrlLow != null && !"".equals(playUrlLow.trim())) {
			urlMap.put(RESLOUTION.LOW, playUrlLow);
		}

		return urlMap;
	}

}
