package com.ott.webtv.core.url_parser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.ott.webtv.core.CoreHandler;
import com.ott.webtv.core.StoreManager;
import com.ott.webtv.core.DataNode.RESLOUTION;

public abstract class URLParser {
	public static final String scPacketName = "com.ott.webtv.core.url_parser.";
	public static final String scCacheName = "url.jar";

	private static Map<String, URLParser> pList;

	public static Map<RESLOUTION, String> parse(String parser, String url) {
		URLParser pObj = getInsByName(parser);

		if (pObj != null && pObj.isURLCorrect(url)) {
			return pObj.parseURL(url);
		} else {
			return null;
		}
	}

	private static URLParser getInsByName(String name) {
		URLParser pObj = null;
		Class<?> cls = null;

		if (pList == null) {
			pList = new HashMap<String, URLParser>();
		}

		do {
			if (pList.containsKey(name)) {
				pObj = pList.get(name);
				break;
			}

			File file = new File(StoreManager.getCacheDir() + File.separator + scCacheName);
			try {
				if (!CoreHandler.isLocalEdition() && file.exists()) {
					cls = CoreHandler.loadClass(file, scPacketName + name);
				} else {
					cls = Class.forName(scPacketName + name);
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}

			try {
				pObj = (URLParser) cls.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			pList.put(name, pObj);
			
		} while (false);

		return pObj;
	}

	protected abstract Boolean isURLCorrect(String url);

	protected abstract Map<RESLOUTION, String> parseURL(String url);

}
