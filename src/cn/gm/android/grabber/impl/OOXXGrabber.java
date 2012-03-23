package cn.gm.android.grabber.impl;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.util.Log;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Result;

/**
 * 妹子图网图片抓取器
 * 
 * @author dragon
 * 
 */
public class OOXXGrabber extends AbstractGrabber {
	private static final String tag = OOXXGrabber.class.getName();
	private int deepGrabFrom;

	public int getDeepGrabFrom() {
		return deepGrabFrom;
	}

	public void setDeepGrabFrom(int deepGrabFrom) {
		this.deepGrabFrom = deepGrabFrom;
	}

	public OOXXGrabber() {
		// 默认深度抓取
		super.setDeepGrab(true);

		super.setUrl("http://jandan.net/ooxx");
		// super.setUrl("http://www.baidu.com/");
		super.setSubDir("grabber/ooxx/");
		super.setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.79 Safari/535.11");

		// 右边栏的图片：div.acv_comment>img
		// 主栏的图片：ol.commentlist>li>p>img
		super.setSelector("ol.commentlist>li>p>img,div.acv_comment>img");
	}

	private String getPageCountSelector() {
		// 获取总页数的选择器:<span class="current-comment-page">[1]</span>
		return "span.current-comment-page";
	}

	@Override
	protected void excuteDeepGrab(Object context, Callback<Result> callback,
			Document doc) {
		int fromPage;
		if (this.getDeepGrabFrom() > 0) {
			fromPage = this.getDeepGrabFrom();
		} else {
			int curPage = 0;
			String t = null;
			// 获取分页信息
			Elements els = doc.select(this.getPageCountSelector());
			if (els.size() > 0)
				t = els.get(0).text();
			Log.d(tag, "curPage=" + t);
			if (t != null && t.startsWith("[")) {
				try {
					curPage = Integer.parseInt(t.substring(1, t.length() - 1));
				} catch (NumberFormatException e) {
				}
			}
			if (curPage < 2) {
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
				result.setMsg("当前为第" + curPage + "页，继续抓取其它页...");
				result.setSuccess(true);
				result.setFrom(this.getUrl());
				callback.call(result);
			}

			fromPage = curPage - 1;

		}

		// 逐页抓取
		String srcUrl = this.getUrl();
		for (int p = fromPage; p > 0; p--) {
			// 重新设置要抓取页的url
			this.setUrl(srcUrl + "/page-" + p);

			// 分页抓取时不抓取右边栏的图片
			super.setSelector("ol.commentlist>li>p>img");

			if (callback != null) {
				Result result = new Result();
				result.setType(Result.TYPE_READY);
				result.setMsg("开始抓取第" + p + "页...");
				result.setSuccess(true);
				result.setFrom(this.getUrl());
				callback.call(result);
			}

			// 避免循环抓取
			this.setDeepGrab(false);

			// 开始抓取
			// try {
			// Log.d(tag, "sleep...");
			// Thread.sleep(500);
			// Log.d(tag, "sleep");
			// } catch (InterruptedException e) {
			// Log.e(tag, e.getMessage());
			// }
			this.excute(context, callback);

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