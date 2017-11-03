package com.ott.webtv.core.url_parser;

import java.util.HashMap;
import java.util.Map;

import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.DataNode.RESLOUTION;
import com.ott.webtv.core.StringUtils;

public class VKParser extends URLParser{
	
	private static final String VK_HOST1 = "http://vk.com/";
	private static final String VK_HOST2 = "http://vkontakte.ru";
	
	@Override
	protected Boolean isURLCorrect(String url) {
		// TODO Auto-generated method stub
		return url.startsWith(VK_HOST1) | url.startsWith(VK_HOST2);
	}

	@Override
	protected Map<RESLOUTION, String> parseURL(String url) {
		
		String playUrlLow = null;
		String playUrlMedium = null;
		String playUrlHigh = null;
		Boolean hasVideo = false;//Jie.jia 20140902 return null when there is no any video.  
		
		Map<RESLOUTION, String> urlMap = new HashMap<RESLOUTION, String>();
		
		String vk_data = new HttpUtils(url).execute().getResult();

		if (vk_data == null || "".equals(vk_data.trim())) {
			return null;
		}
		
		if(StringUtils.isContains(vk_data, "url240=")){
			playUrlLow = StringUtils.substr(vk_data, "url240=", "&amp");
		}
		
		if(StringUtils.isContains(vk_data, "url360=")){
			playUrlLow = StringUtils.substr(vk_data, "url360=", "&amp");
		}
		
		if(StringUtils.isContains(vk_data, "url480=")){
			playUrlLow = StringUtils.substr(vk_data, "url480=", "&amp");
		}
		
		if(StringUtils.isContains(vk_data, "url720=")){
			playUrlMedium = StringUtils.substr(vk_data, "url720=", "&amp");
		}
		
		if(StringUtils.isContains(vk_data, "url1080=")){
			playUrlHigh = StringUtils.substr(vk_data, "url1080=", "&amp");
		}
		
		if (playUrlLow != null && !"".equals(playUrlLow.trim())) {
			urlMap.put(RESLOUTION.LOW, playUrlLow);
			hasVideo = true;
		}
		
		if (playUrlMedium != null && !"".equals(playUrlMedium.trim())) {
			urlMap.put(RESLOUTION.MEDIUM, playUrlMedium);
			hasVideo = true;
		}

		if (playUrlHigh != null && !"".equals(playUrlHigh.trim())) {
			urlMap.put(RESLOUTION.HIGH, playUrlHigh);
			hasVideo = true;
		}
		
		if ( !hasVideo)
		    urlMap = null;
		
		return urlMap;
	}

}
