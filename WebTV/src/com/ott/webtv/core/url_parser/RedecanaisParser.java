package com.ott.webtv.core.url_parser;

import java.util.HashMap;
import java.util.Map;

import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.DataNode.RESLOUTION;

public class RedecanaisParser extends URLParser{
	
	private static final String REDECANAIS_HOST = "http://www.redecanais.net/";
	
	@Override
	protected Boolean isURLCorrect(String url) {
		// TODO Auto-generated method stub
		return url.startsWith(REDECANAIS_HOST);
	}

	@Override
	protected Map<RESLOUTION, String> parseURL(String url) {
		String playUrlLow = null;
//		String playUrlHD = null;
		String tempUrl   = null;
		String rcUrl     = null;
		int start = -1;
		int lowIdx = -1;
//		int hdIdx = -1;
		
		Map<RESLOUTION, String> urlMap = new HashMap<RESLOUTION, String>();
		
		String net_data = new HttpUtils(url).execute().getResult();
		
		if (net_data == null || "".equals(net_data.trim())) {
			return null;
		}
		
		if((start = net_data.indexOf("<iframe name")) != -1){
			net_data = net_data.substring(start + "<iframe name".length());
			
			if((start = net_data.indexOf("src=\"")) != -1){
				net_data = net_data.substring(start + "src=\"".length());
				tempUrl  = net_data.substring(0, net_data.indexOf("\""));
				tempUrl  = tempUrl.replace("redecanais.tv", "www.redecanais.net");
			}
		}
//		System.out.println("RedecanaisParser  tempUrl-->" + tempUrl);
		if(tempUrl == null || "".equals(tempUrl.trim())){
			return null;
		}
		
		String temp_data = new HttpUtils(tempUrl).execute().getResult();
		if (temp_data == null || "".equals(temp_data.trim())) {
			return null;
		}
		
		if((start = temp_data.indexOf("<div class=\"player\">")) != -1){
			temp_data = temp_data.substring(start + "<div class=\"player\">".length());
			
			if((start = temp_data.indexOf("<a href=\"")) != -1){
				temp_data = temp_data.substring(start + "<a href=\"".length());
				rcUrl = temp_data.substring(0, net_data.indexOf("\""));
				rcUrl  = rcUrl.replace("redecanais.tv", "www.redecanais.net");
			}
		}
//		System.out.println("RedecanaisParser  rcUrl-->" + rcUrl);
		if(rcUrl == null || "".equals(rcUrl.trim())){
			return null;
		}
		
		String rc_data = new HttpUtils(rcUrl).execute().getResult();
		if (rc_data == null || "".equals(rc_data.trim())) {
			return null;
		}
		
		if ((lowIdx = rc_data.indexOf("file: \"")) != -1) {
			rc_data = rc_data.substring(lowIdx + "file: \"".length());
			playUrlLow = rc_data.substring(0, rc_data.indexOf("\""));
//			System.out.println("RedecanaisParser  playUrlLow-->" + playUrlLow);
		}
		
		if (playUrlLow != null && !"".equals(playUrlLow.trim())) {
			urlMap.put(RESLOUTION.LOW, playUrlLow);
		}

		return urlMap;
	}

}
