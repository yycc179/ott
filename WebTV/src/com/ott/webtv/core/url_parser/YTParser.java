package com.ott.webtv.core.url_parser;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ott.webtv.core.DataNode.RESLOUTION;
import com.ott.webtv.core.HttpUtils;
import com.ott.webtv.core.StoreManager;
import com.ott.webtv.core.js_eval.JsEvaluator;
import com.ott.webtv.core.js_eval.JsCallback;

class YTParser extends URLParser {
	private static final String sYTTag = "http://www.youtube";

	private static final String sURLHead = "http://www.youtube.com/watch?v=";
	private static final String sURLTail = "&gl=US&hl=en&has_verified=1";

	private static final String sAgeGateURLHead = "http://www.youtube.com/get_video_info?video_id=";
	private static final String sAgeGateURLBody = "&el=embedded&gl=US&hl=en&eurl=http://youtube.googleapis.com/v/";
	private static final String sAgeGateURLTail = "&asv=3&sts=1588";
	private static final String sAgeGateStr = "player-age-gate-content";

	public static final int PARSE_WAY_WEB_PAGE = 0;
	public static final int PARSE_WAY_AGE_GATE = 1;

	private static final String URL_MAP_DELIMITER_WEBPAGE = ",";
	private static final String URL_MAP_DELIMITER_RAWLIST = "%2C";

	private static final String sVideoIdRegEx = "[-A-Z0-9a-z]{11}(?=$|\\?)";
	private static final String sUrlMapRegEx = "\"url_encoded_fmt_stream_map\":\\s*\"(.+?)\"";
	private static final String sJsFileRegEx = "\"assets\":.+?\"js\":\\s*\"(.+?)\"";

	@Override
	protected Boolean isURLCorrect(String url) {
		return url.startsWith(sYTTag);
	}

	@Override
	protected Map<RESLOUTION, String> parseURL(String url) {
		Map<RESLOUTION, String> ret = null;
		do {

			int parseWay = PARSE_WAY_WEB_PAGE;

			String id = getMatchRes(url, sVideoIdRegEx, 0);
			if (id == null) {
				System.out.println("Get id fail: " + url);
				break;
			}

			String src = getWebPage(parseWay, id);

			if (src != null && src.indexOf(sAgeGateStr) != -1) {
				src = getWebPage(PARSE_WAY_AGE_GATE, id);
				parseWay = PARSE_WAY_AGE_GATE;
			}

			String videoMap = getMatchRes(src, sUrlMapRegEx, 1);
			if (videoMap == null) {
				System.out.println("videoMap null");
				break;
			}

			String[] items = videoMap
					.split(parseWay == PARSE_WAY_WEB_PAGE ? URL_MAP_DELIMITER_WEBPAGE
							: URL_MAP_DELIMITER_RAWLIST);

			if (items.length < 1) {
				break;
			}

			VideoItem.init(getMatchRes(src, sJsFileRegEx, 1), parseWay);

			List<VideoItem> list = new ArrayList<VideoItem>();

			for (String item : items) {
				VideoItem v = new VideoItem();
				if (v.parseItem(item) == 0) {
					list.add(v);
				}
			}

			if (list.size() == 0) {
				System.out.println("videoItem null");
				break;
			}

			Collections.sort(list);

			ret = new HashMap<RESLOUTION, String>();

			for (VideoItem vi : list) {
				String qt = vi.getQuality();
				if (VideoItem.QUALITY_SMALL.equals(qt)) {
					ret.put(RESLOUTION.LOW, vi.getRealUrl());

				} else if (VideoItem.QUALITY_MEDIUM.equals(qt)) {
					ret.put(RESLOUTION.MEDIUM, vi.getRealUrl());

				} else {
					ret.put(RESLOUTION.HIGH, vi.getRealUrl());
				}
			}

			VideoItem.waitSigDone();

		} while (false);

		return ret;

	}

	private String getWebPage(int way, String id) {
		String url = null;

		switch (way) {
		case PARSE_WAY_WEB_PAGE:
			url = sURLHead + id + sURLTail;
			break;

		case PARSE_WAY_AGE_GATE:
			url = sAgeGateURLHead + id + sAgeGateURLBody + id + sAgeGateURLTail;
			break;
		}

		return new HttpUtils(url).execute().getResult();
	}

	public static String getMatchRes(String src, String regEx, int group) {
		String res = null;

		do {
			if (src == null) {
				break;
			}
			Matcher mat = Pattern.compile(regEx).matcher(src);
			if (mat.find()) {
				return mat.group(group);
			}
		} while (false);

		return res;
	}

}

class VideoItem implements Comparable<VideoItem> {
	private static final String DELIMITER_WEBPAGE = "\\\\u0026";
	private static final String DELIMITER_RAWLIST = "%26";

	private static final String KEY_ADDITION_WEBPAGE = "=";
	private static final String KEY_ADDITION_RAWLIST = "%3D";

	public static final String QUALITY_SMALL = "small";
	public static final String QUALITY_MEDIUM = "medium";

	private static final String sSigFuncNameRegEx = "signature=([a-zA-Z]+)";
	private static final String sSigFuncBodyRegEx = "function\\s%s.+?return.+?\\};";
	private static final String sSigClassNameRegEx = "(\\w{2,})\\.\\w{2,}\\(.+?\\)";
	private static final String sSigClassBodyRegEx = "var\\s%s=\\{.+?\\};";
	private static final String sItemKeyRegEx = "%s%s(.+?)(?=$|%s)";

	private static final String sTypeMp4 = "mp4";
	private static final String sTypeFlv = "flv";

	private static final String sCacheJsName = "/sig.js";

	public static enum ITAG {
		MP4_1080P("37"), MP4_720P("22"), FLV_480P("35"), MP4_360P("18"), FLV_360P(
				"34"), FLV_240P("5"), FLV_160P("36"), MP4_144P("17");

		private static final Map<String, ITAG> EnumStr = new HashMap<String, ITAG>();
		static {
			for (ITAG tag : values()) {
				EnumStr.put(tag.toString(), tag);
			}
		}

		private final String tag;

		ITAG(String tag) {
			this.tag = tag;
		}

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return tag;
		}

		public static ITAG fromString(String tag) {
			return EnumStr.get(tag);
		}
	};

	private static int parseWay;
	private static String jsUrl;
	private static String jsFuncName;
	private static JsEvaluator jsEvaluator;

	private static int sigCount;
	private static int sigGetted;

	private String url;
	private ITAG itag;
	private String quality;

	public static void init(String url, int way) {
		jsUrl = "http:" + url.replace("\\", "");
		parseWay = way;
		sigCount = 0;
		sigGetted = 0;
	}

	private String getItemData(String item, String key) {
		String key_addition = null;
		String item_delimiter = null;

		if (parseWay == YTParser.PARSE_WAY_WEB_PAGE) {
			item_delimiter = DELIMITER_WEBPAGE;
			key_addition = KEY_ADDITION_WEBPAGE;
		} else {
			item_delimiter = DELIMITER_RAWLIST;
			key_addition = KEY_ADDITION_RAWLIST;
		}

		String regx = String.format(sItemKeyRegEx, key, key_addition,
				item_delimiter);

		return YTParser.getMatchRes(item, regx, 1);
	}

	@SuppressWarnings("deprecation")
	public int parseItem(String item) {
		do {
			String type = getItemData(item, "type");
			if (type.indexOf(sTypeFlv) == -1 && type.indexOf(sTypeMp4) == -1) {
				break;
			}

			itag = ITAG.fromString(getItemData(item, "itag"));
			if (itag == null) {
				break;
			}

			quality = getItemData(item, "quality");

			url = URLDecoder.decode(getItemData(item, "url"));
			String sig = getItemData(item, "sig");
			String s = null;

			if (sig != null) {
				setSignature(sig);

			} else if ((s = getItemData(item, "s")) != null) {
				sigCount++;
				getJsEvaluator().callFunction(new JsCallback() {

					@Override
					public void onResult(String value) {
						// TODO Auto-generated method stub
						setSignature(value);
						sigGetted++;
					}
				}, jsFuncName, s);
			}

			return 0;

		} while (false);

		return -1;
	}

	private void setSignature(String val) {
		url = url + "&signature=" + val;
	}

	public String getRealUrl() {
		return url;
	}

	public String getQuality() {
		return quality;
	}

	private static JsEvaluator getJsEvaluator() {
		if (jsEvaluator == null) {
			jsEvaluator = JsEvaluator.getEvaluator();
			jsEvaluator.evaluate(getJavaScript());
		}

		return jsEvaluator;
	}

	private static String getSigFunction(String js) {
		StringBuffer sb = new StringBuffer();
		jsFuncName = YTParser.getMatchRes(js, sSigFuncNameRegEx, 1);

		String funcBody = YTParser.getMatchRes(js,
				String.format(sSigFuncBodyRegEx, jsFuncName), 0);
		sb.append(funcBody);

		Matcher mat = Pattern.compile(sSigClassNameRegEx).matcher(funcBody);
		Set<String> set = new HashSet<String>();
		while (mat.find()) {
			set.add(mat.group(1));
		}

		for (Iterator<String> it = set.iterator(); it.hasNext();) {
			String clsbody = YTParser.getMatchRes(js,
					String.format(sSigClassBodyRegEx, it.next().toString()), 0);
			sb.append(clsbody);
		}

		return sb.toString();
	}

	private static String getJavaScript() {
		String jsCode = null;
		File file = new File(StoreManager.getCacheDir() + sCacheJsName);

		String date = StoreManager.getLastModified(file);

		HttpUtils req = new HttpUtils(jsUrl).setParam(HttpUtils.sModifySince,
				date).execute();
		byte[] buf = req.getByteArray();

		if (req.isNotModified()) {
			jsCode = new String(StoreManager.readFile(file));
		} else {
			StoreManager.writeFile(file, buf);
			jsCode = getSigFunction(new String(buf));
		}

		return jsCode;
	}

	public static void waitSigDone() {
		final int max = 50;
		int i = 0;
		while (sigCount < sigGetted && (i++) < max) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}
	}

	@Override
	public int compareTo(VideoItem another) {
		// TODO Auto-generated method stub
		if (this.itag.ordinal() < another.itag.ordinal()) {
			return 1;
		} else {
			return -1;
		}
	}

}
