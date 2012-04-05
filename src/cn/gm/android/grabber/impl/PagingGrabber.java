package cn.gm.android.grabber.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;

import android.util.Log;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Event;
import cn.gm.android.grabber.EventType;
import cn.gm.android.grabber.Grab;

/**
 * 分页抓取器：抓取当前页及其所有其他分页
 * 
 * @author dragon
 * 
 */
public class PagingGrabber extends SimpleGrabber {
	private static final String tag = PagingGrabber.class.getName();
	private String pagingUrl;// 分页网址的url模式
	private String pagingSelector;// 获取分页信息的选择器
	private String pagingRegx;// 从分页信息解析出总页数的正则表达式

	public String getPagingUrl() {
		return pagingUrl;
	}

	public PagingGrabber setPagingUrl(String pagingUrl) {
		this.pagingUrl = pagingUrl;
		return this;
	}

	public String getPagingSelector() {
		return pagingSelector;
	}

	public PagingGrabber setPagingSelector(String pagingSelector) {
		this.pagingSelector = pagingSelector;
		return this;
	}

	public String getPagingRegx() {
		return pagingRegx;
	}

	public PagingGrabber setPagingRegx(String pagingRegx) {
		this.pagingRegx = pagingRegx;
		return this;
	}

	/**
	 * 解析出总页数
	 * 
	 * @param text
	 *            包含总页数的文本信息
	 * @param pattern
	 *            用于解析总页数的正则表达式
	 * @return
	 */
	public static int buildPageCount(String text, String pattern) {
		int i = pattern.indexOf("{page}");
		if (i != -1) {
			pattern = "(?<=" + pattern.substring(0, i) + ")(\\d)+(?="
					+ pattern.substring(i + 6) + ")";
		}
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		String t;
		if (m.find())
			t = m.group();
		else
			t = null;
		return t != null ? Integer.parseInt(t) : 0;
	}

	/**
	 * 构建实际的分页请求url
	 * 
	 * @param pattern
	 *            抽象的分页url
	 * @param url
	 *            原始的url
	 * @param page
	 *            页码
	 * @return
	 */
	public static String buildPagingUrl(String pattern, String url, int page) {
		if (pattern.indexOf("{url}") != -1)
			pattern = pattern.replaceAll("\\{url\\}", url);
		if (pattern.indexOf("{page}") != -1)
			pattern = pattern.replaceAll("\\{page\\}", String.valueOf(page));
		if (pattern.indexOf("{urlName}") != -1) {
			int sepIndex = url.lastIndexOf(".");
			pattern = pattern.replaceAll("\\{urlName\\}",
					url.substring(0, sepIndex));
		}

		return pattern;
	}

	private Grab pagingGrabber;

	@Override
	protected void doFinished(Document doc, Callback<Event> callback,
			Event result) {
		super.doFinished(doc, callback, result);

		// 获取分页的总页数
		String pagingInfo = doc.select(this.getPagingSelector()).text();
		int pageCount = buildPageCount(pagingInfo, this.getPagingRegx());
		Log.d(tag, "pageCount=" + pageCount);

		// 没有分页信息直接返回
		if (pageCount < 2) {
			return;
		}

		// 循环每一页
		for (int page = 2; page <= pageCount; page++) {
			if (this.isForceStop()) {
				result = new Event();
				result.setType(EventType.Stop);
				result.setSrc(this.getUrl());
				result.setIndex(page);
				result.setTotal(pageCount);
				callback.call(result);
				return;
			}
			pagingGrabber = new SimpleGrabber()
					.setDir("/grabber/" + this.getDir())
					.setUrl(buildPagingUrl(this.getPagingUrl(), this.getUrl(),
							page)).setUserAgent(this.getUserAgent())
					.setSelector(this.getSelector())
					.setName(pageCount + "-" + page);
			pagingGrabber.start(callback);
		}
	}

	@Override
	public void stop() {
		super.stop();
		if (pagingGrabber != null)
			pagingGrabber.stop();
	}
}
