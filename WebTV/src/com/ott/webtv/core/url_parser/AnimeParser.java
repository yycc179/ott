package com.ott.webtv.core.url_parser;

import java.util.HashMap;
import java.util.Map;

import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.DataNode.RESLOUTION;

public class AnimeParser extends URLParser{
	
	private static final String ANIME_HOST = "anime.php";

	private static final String ANIME_URL_HEAD = "http://mais.uol.com.br/apiuol/player/media.js?p=undefined&mediaId=";

	private static final String ANIME_URL_TAIL = "&action=showPlayer&types=V";
	
	@Override
	protected Boolean isURLCorrect(String url) {
		boolean bRet = false;
		if ((url != null) && (url.indexOf(ANIME_HOST) != -1)) {
			bRet = true;
		}

		return bRet;
	}

	@Override
	protected Map<RESLOUTION, String> parseURL(String url) {
		String mediaId = null;
		String playUrlLow = null;
		// String playUrlHD = null;
		int lowIdx = -1;
		// int hdIdx = -1;

		Map<RESLOUTION, String> urlMap = new HashMap<RESLOUTION, String>();

		String net_data = new HttpUtils(url).execute().getResult();

		if (net_data == null || "".equals(net_data.trim())) {
			return null;
		}

		int mediaIdx = -1;
		if ((mediaIdx = net_data.indexOf("mediaId=")) != -1) {
			mediaIdx += "mediaId=".length();
			net_data = net_data.substring(mediaIdx);
			mediaId = net_data.substring(0, net_data.indexOf("\""));
			System.out.println("AnimeParser mediaId-->" + mediaId);
		} else {
			return null;
		}

		String animeUrl = ANIME_URL_HEAD + mediaId + ANIME_URL_TAIL;
		String url_data = new HttpUtils(animeUrl).execute().getResult();

		if (url_data == null || "".equals(url_data.trim())) {
			return null;
		}

		if ((lowIdx = url_data.indexOf("formats")) != -1) {
			url_data = url_data.substring(lowIdx + "formats".length());
			if ((lowIdx = url_data.indexOf("\"url\":\"")) != -1) {
				lowIdx += "\"url\":\"".length();
				url_data = url_data.substring(lowIdx);
				String url1 = url_data.substring(0, url_data.indexOf("\""));
				
				url_data = url_data.substring(
						url_data.indexOf("\"embedUrl\":\"")
								+ "\"embedUrl\":\"".length());
				
				String url2 = url_data.substring(0, url_data.indexOf("\""));

				playUrlLow = url1 + "?ver=0&start=0&r=" + url2;
				//System.out.println("AnimeParser playurl-->"+playUrlLow);
			}
		}

		if (playUrlLow != null && !"".equals(playUrlLow.trim())) {
			urlMap.put(RESLOUTION.LOW, playUrlLow);
		}

		return urlMap;
	}

}
