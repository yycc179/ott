package com.ott.webtv.core;

/**
 * 字符串操作工具类
 * 
 * @author yang.yu
 * 
 */
public class StringUtils {

	/**
	 * 检测字串内容是否存在
	 * 
	 * @param net_data
	 *            待检测字符串
	 * @return true 字符串为空 false 字符串不为空
	 * 
	 */
	public static boolean isNull(String net_data) {
		if (net_data == null || "".equals(net_data.trim())) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断源字符串net_data中是否包含一个子字符串find
	 * 
	 * @param net_data
	 *            待查找的源字符串
	 * @param find
	 *            待查找的子字符串
	 * @return true 包含子字符串 false 不包含子字符串
	 * 
	 */
	public static boolean isContains(String net_data, String find) {
		boolean bRet = false;
		if (net_data == null || find == null) {
			return false;
		}
		if (net_data.indexOf(find) != -1) {
			bRet = true;
		}
		return bRet;
	}

	/**
	 * 从字符串开始位置向后查找子串的位置，并截取子串位置之后的字符串
	 * 
	 * @param net_data
	 *            源字符串
	 * @param sfind
	 *            查找字符串
	 * @return 截取的字符串
	 * @exception 当未查找到字串时抛出运行时异常
	 */
	public static String skip(String net_data, String sfind) {

		int findIdx = net_data.indexOf(sfind);
		if (findIdx == -1) {
			return net_data;
		}
		findIdx += sfind.length();
		return net_data.substring(findIdx);
	}

	/**
	 * 从字符串末尾位置向前查找子串的位置，并截取字串开始位置到子串位置之间的字符串
	 * 
	 * @param net_data
	 *            源字符串
	 * @param sfind
	 *            查找字符串
	 * @return 截取的字符串
	 * @exception 当未查找到字串时抛出运行时异常
	 */
	public static String lastSkip(String net_data, String sfind) {
		int findIdx = net_data.lastIndexOf(sfind);
		if (findIdx == -1) {
			throw new RuntimeException("no find the " + sfind);
		}
		return net_data.substring(0, findIdx);
	}

	/**
	 * 截取字串的start到end之间的字串
	 * 
	 * @param net_data
	 *            源字符串
	 * @param start
	 *            开始字符串
	 * @param end
	 *            结束字符串
	 * @return 截取的字符串
	 * @exception 当未查找到字串时抛出运行时异常
	 */
	public static String substr(String net_data, String start, String end) {

		int startIdx = net_data.indexOf(start);
		if (startIdx == -1) {
			throw new RuntimeException("no find the " + start);
		}
		startIdx += start.length();
		net_data = net_data.substring(startIdx);
		int endIdx = net_data.indexOf(end);
		if (endIdx == -1) {
			throw new RuntimeException("no find the " + end);
		}
		return net_data.substring(0, endIdx);
	}

	/**
	 * 从字符串开始位置向后查找子串的位置，并截取字串开始位置到子串位置之间的字符串
	 * 
	 * @param net_data
	 *            源字符串
	 * @param sfind
	 *            查找字符串
	 * @return 截取的字符串
	 * @exception 当未查找到字串时抛出运行时异常
	 */
	public static String substr(String net_data, String start) {
		int findIdx = net_data.indexOf(start);
		if (findIdx == -1) {
			throw new RuntimeException("no find the " + start);
		}
		return net_data.substring(0, findIdx);
	}

	/**
	 * 格式化网页字符，如'@#293;'格式化成utf-8字符
	 * 
	 * @param net_data
	 *            待格式化的字符串
	 * @return 格式化完的字符串
	 */
	public static String formatString(String net_data) {
		String temp = net_data;
		int start = 0;
		while (temp.indexOf("&#") != -1) {
			start = temp.indexOf("&#") + "&#".length();
			temp = temp.substring(start);
			String sCode = temp.substring(0, temp.indexOf(";"));
			String utfCode = String.valueOf((char) Integer.parseInt(sCode));
			net_data = net_data.replace("&#" + sCode + ";", utfCode);
		}
		net_data = filterString(net_data);
		return net_data;
	}

	/**
	 * 过滤掉网页字符，包括 & amp;、& quot;
	 * 
	 * @param net_data
	 *            待过滤的字符串
	 * @return 过滤完的字符串
	 */
	public static String filterString(String net_data) {

		net_data = net_data.replaceAll("&amp;", "&").replaceAll("&quot;", "\"")
				.replaceAll("&nbsp;", "").replaceAll("<br/>", "")
				.replaceAll("<b>", "").replaceAll("</b>", "")
				.replaceAll("<br />", "");

		return net_data;
	}

}
