package com.ott.webtv.core.url_parser;

import java.util.HashMap;
import java.util.Map;

import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.DataNode.RESLOUTION;

public class VidtoParser extends URLParser{
	
	private static final String VIDTO_HOST = "http://vidto.me/";
	
	@Override
	protected Boolean isURLCorrect(String url) {
		// TODO Auto-generated method stub
		return url.startsWith(VIDTO_HOST);
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
		if ((start = net_data.indexOf("method=\"POST\"")) != -1) {
			net_data = net_data.substring(start + "method=\"POST\"".length());

			net_data = net_data.substring(net_data.indexOf("id\" value=\"")
					+ "id\" value=\"".length());
			String idParam = net_data.substring(0, net_data.indexOf("\""));

			net_data = net_data.substring(net_data.indexOf("fname\" value=\"")
					+ "fname\" value=\"".length());
			String fnameParam = net_data.substring(0, net_data.indexOf("\""));

			net_data = net_data.substring(net_data
					.indexOf("referer\" value=\"")
					+ "referer\" value=\"".length());
			String refererParam = net_data.substring(0, net_data.indexOf("\""));

			net_data = net_data.substring(net_data.indexOf("hash\" value=\"")
					+ "hash\" value=\"".length());
			String hashParam = net_data.substring(0, net_data.indexOf("\""));

			postData = "op=download1&usr_login=&id="
					+ idParam
					+ "&fname="
					+ fnameParam
					+ "&referer="
					+ refererParam.replace(":", "%3A").replace("/", "%2F")
							.replace("=", "%3D") + "&hash=" + hashParam
					+ "&imhuman=Proceed+to+video";
			
			System.out.println("VidtoParser postdata-->" + postData);
		}

		int dTimes = 5;
		String post_net_data = null;

		while (dTimes > 0) {
			post_net_data = new HttpUtils(url).post(postData).execute()
					.getResult();
			if (post_net_data != null && post_net_data.indexOf("file_link = \'") != -1) {
				break;
			}
			dTimes--;
		}
		
		if(post_net_data == null){
			return null;
		}
		
		if ((lowIdx = post_net_data.indexOf("file_link = \'")) != -1) {
			lowIdx += "file_link = \'".length();
			post_net_data = post_net_data.substring(lowIdx);
			playUrlLow = post_net_data.substring(0, post_net_data.indexOf("\'"));
			playUrlLow = playUrlLow.substring(0, playUrlLow.lastIndexOf("/"));
			playUrlLow = playUrlLow + "/video.mp4";
			System.out.println("VidtoParser playUrlLow-->" + postData);
		}

		if (playUrlLow != null && !"".equals(playUrlLow.trim())) {
			urlMap.put(RESLOUTION.LOW, playUrlLow);
		}

		return urlMap;
	}

}
