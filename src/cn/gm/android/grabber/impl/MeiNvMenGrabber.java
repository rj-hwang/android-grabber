package cn.gm.android.grabber.impl;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Result;

/**
 * 美女门网站图片抓取器
 * 
 * @author dragon
 * 
 */
public class MeiNvMenGrabber extends AbstractGrabber {
	private static final String tag = MeiNvMenGrabber.class.getName();
	private int deepGrabFrom;

	public int getDeepGrabFrom() {
		return deepGrabFrom;
	}

	public void setDeepGrabFrom(int deepGrabFrom) {
		this.deepGrabFrom = deepGrabFrom;
	}

	public MeiNvMenGrabber() {
		// 默认深度抓取
		super.setDeepGrab(false);

		super.setUrl("http://www.mmeinv.com/");
		super.setSubDir("grabber/美女门/");
		super.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.79 Safari/535.11");

		// 热门区的链接：div#SlidePlayer>ul>li>a>img (href属性为链接地址)
		super.setSelector("div#SlidePlayer>ul>li>a");
	}

	@Override
	public void excute(Object context, Callback<Result> callback) {
		// 还获取热门链接的地址
		Result result;
		Document doc;
		try {
			if (callback != null) {
				result = new Result();
				result.setType(Result.TYPE_READY);
				result.setMsg("开始抓取:" + this.getUrl());
				result.setFrom(this.getUrl());
				result.setSuccess(true);
				callback.call(result);
			}
			Connection connection = Jsoup.connect(this.getUrl());
			if (this.getUserAgent() != null)
				connection.userAgent(this.getUserAgent());
			doc = connection.get();
		} catch (IOException e) {
			Log.e(tag, "抓取失败:url=" + this.getUrl() + ",e=" + e.getMessage(), e);

			if (callback != null) {
				result = new Result();
				result.setType(Result.TYPE_ERROR);
				result.setMsg("抓取失败:" + this.getUrl() + ",e=" + e.getMessage());
				result.setFrom(this.getUrl());
				result.setSuccess(false);
				callback.call(result);
			}
			return;
		}
		Elements links = doc.select(this.getSelector());
		if (callback != null) {
			result = new Result();
			result.setType(Result.TYPE_READY);
			result.setMsg("找到" + links.size() + "个热门链接");
			result.setFrom(this.getUrl());
			result.setSuccess(true);
			callback.call(result);
		}
		String linkUrl;
		String subImgSelector = "div.pic img";
		boolean deepGrab = this.isDeepGrab();
		for (Element link : links) {
			linkUrl = link.attr("href");
			this.setUrl(linkUrl);
			this.setSelector(subImgSelector);
			this.setDeepGrab(deepGrab);
			super.excute(context, callback);
		}
	}

	private String getPageCountSelector() {
		// 获取总页数的选择器:<span class="current-comment-page">[1]</span>
		return "ul.pagelist>li>a";
	}
	
	@Override
	protected String rebuildImgUrl(String imgUrl) {
		if (!imgUrl.startsWith("http")) {
			return "http://www.mmeinv.com" + imgUrl;
		}else{
			return imgUrl;
		}
	}

	@Override
	protected void excuteDeepGrab(Object context, Callback<Result> callback,
			Document doc) {
		int totalPage = 0;
		String t = null;
		// 获取分页信息
		Elements els = doc.select(this.getPageCountSelector());
		if (els.size() > 0)
			t = els.get(0).text();
		Log.d(tag, "totalPageStr=" + t);
		if (t != null && t.startsWith("共")) {
			try {
				totalPage = Integer.parseInt(t.substring(1, t.indexOf("页")));
			} catch (NumberFormatException e) {
			}
		}
		Log.d(tag, "totalPage=" + totalPage);
		if (totalPage < 2) {
			if (callback != null) {
				Result result = new Result();
				result.setType(Result.TYPE_ERROR);
				result.setMsg("没有找到分页信息，终止深度抓取！");
				result.setSuccess(false);
				result.setFrom(this.getUrl());
				callback.call(result);
			}
			return;
		}

		if (callback != null) {
			Result result = new Result();
			result.setType(Result.TYPE_FINDED);
			result.setMsg("找到" + totalPage + "页");
			result.setSuccess(true);
			result.setFrom(this.getUrl());
			callback.call(result);
		}

		// 避免循环抓取
		this.setDeepGrab(false);

		// 逐页抓取
		String srcUrl = this.getUrl();
		int sepIndex = srcUrl.lastIndexOf(".");
		String srcExt = srcUrl.substring(sepIndex);
		srcUrl = srcUrl.substring(0, sepIndex);
		for (int p = 2; p <= totalPage; p++) {
			// 重新设置要抓取页的url
			this.setUrl(srcUrl + "_" + p + srcExt);

			if (callback != null) {
				Result result = new Result();
				result.setType(Result.TYPE_READY);
				result.setMsg("开始抓取第" + p + "页...");
				result.setSuccess(true);
				result.setFrom(this.getUrl());
				callback.call(result);
			}
			super.excute(context, callback);

			if (this.isForceStop()) {
				Result result = new Result();
				result.setType(Result.TYPE_ERROR);
				result.setMsg("====强制终止====");
				result.setSuccess(false);
				result.setFrom(this.getUrl());
				callback.call(result);
				break;
			}
		}
	}
}