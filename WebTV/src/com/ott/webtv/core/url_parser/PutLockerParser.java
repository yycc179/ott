package com.ott.webtv.core.url_parser;

import java.util.HashMap;
import java.util.Map;

import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.DataNode.RESLOUTION;

public class PutLockerParser extends URLParser{
	
	private static final String PUT_HOST = "http://www.firedrive.com/";
	
	@Override
	protected Boolean isURLCorrect(String url) {
		// TODO Auto-generated method stub
		return url.startsWith(PUT_HOST);
	}

	@Override
	protected Map<RESLOUTION, String> parseURL(String url) {
		String playUrlLow = null;
		int start = -1;
		int lowIdx = -1;
		String postData = null;

		Map<RESLOUTION, String> urlMap = new HashMap<RESLOUTION, String>();

		String net_data = new HttpUtils(url).execute().getResult();
		if (net_data == null || "".equals(net_data.trim())) {
			return null;
		}

		// parse postdata
		if ((start = net_data.indexOf("id=\"confirm_form\"")) != -1) {
			net_data = net_data.substring(start
					+ "id=\"confirm_form\"".length());
			net_data = net_data.substring(net_data.indexOf("value=\"")
					+ "value=\"".length());

			postData = "confirm="
					+ net_data.substring(0, net_data.indexOf("\""))
							.replace(":", "%3A").replace("/", "%2F")
							.replace("=", "%3D");
		}

		int dTimes = 5;
		String post_net_data = null;

		while (dTimes > 0) {
			post_net_data = new HttpUtils(url).post(postData).execute()
					.getResult();
			if((post_net_data != null) && (post_net_data.indexOf("file: \'") != -1)){
				break;
			}
			dTimes--;
		}
		
		if(post_net_data == null){
			return null;
		}
		
		if ((lowIdx = post_net_data.indexOf("file: \'")) != -1) {
			lowIdx += "file: \'".length();
			post_net_data = post_net_data.substring(lowIdx);
			playUrlLow = post_net_data.substring(0, post_net_data.indexOf("\'"));
		}

		if (playUrlLow != null && !"".equals(playUrlLow.trim())) {
			urlMap.put(RESLOUTION.LOW, playUrlLow);
		}

		return urlMap;
	}

}
