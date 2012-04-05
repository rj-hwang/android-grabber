package cn.gm.android.grabber.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Event;
import cn.gm.android.grabber.EventType;
import cn.gm.android.grabber.Grab;
import cn.gm.android.grabber.util.GrabberUtils;

/**
 * 简易单页面抓取器：只抓取指定页的相关信息
 * 
 * @author dragon
 * 
 */
public class SimpleGrabber implements Grab {
	private static final String tag = SimpleGrabber.class.getName();
	private String name;// 简要描述
	private String url;// 抓取的网址
	private String selector;// 选择器
	private String dir;// 保存到sd卡下的子目录路径
	private String userAgent;// 抓取时使用的User-Agent，为空则不使用

	public String getName() {
		return name;
	}

	public SimpleGrabber setName(String name) {
		this.name = name;
		return this;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public SimpleGrabber setUserAgent(String userAgent) {
		this.userAgent = userAgent;
		return this;
	}

	public String getUrl() {
		return url;
	}

	public SimpleGrabber setUrl(String url) {
		this.url = url;
		return this;
	}

	public String getSelector() {
		return selector;
	}

	public SimpleGrabber setSelector(String selector) {
		this.selector = selector;
		return this;
	}

	public String getDir() {
		return dir;
	}

	public SimpleGrabber setDir(String dir) {
		this.dir = dir;
		return this;
	}

	// 抓取网站
	public void start(Callback<Event> callback) {
		Event result;
		Document doc;
		try {
			if (callback != null) {
				result = new Event();
				result.setType(EventType.BeforeConnect);
				result.setSrc(this.getUrl());
				result.setName(this.getName());
				callback.call(result);
			}
			Connection connection = Jsoup.connect(this.getUrl());
			if (this.getUserAgent() != null)
				connection.userAgent(this.getUserAgent());
			doc = connection.get();
		} catch (IOException e) {
			// Log.e(tag, "抓取失败:url=" + this.getUrl() + ",e=" + e.getMessage(),
			// e);
			if (callback != null) {
				result = new Event();
				result.setType(EventType.Error);
				result.setSrc(this.getUrl());
				result.setData(e);
				callback.call(result);
			}
			return;
		}
		Elements els = doc.select(this.getSelector());
		if (callback != null) {
			result = new Event();
			result.setType(EventType.AfterConnect);
			result.setTotal(els.size());
			result.setSrc(this.getUrl());
			result.setData(doc);
			callback.call(result);
		}

		// 循环每一个图片进行抓取并保存到sd卡指定的目录
		String elSrc;
		int index = 0;
		for (Element el : els) {
			index++;
			elSrc = this.rebuildSrc(el.attr("src"));
			Log.d(tag, "index=" + index + ",src＝" + elSrc);
			this.grabOne(callback, elSrc, index, els.size());

			if (this.isForceStop()) {
				result = new Event();
				result.setType(EventType.Stop);
				result.setSrc(elSrc);
				result.setIndex(index);
				result.setTotal(els.size());
				result.setData(el);
				callback.call(result);
				return;
			}
		}

		// 本页抓取完成后的处理
		result = new Event();
		result.setType(EventType.Finished);
		result.setSrc(this.getUrl());
		result.setTotal(els.size());
		result.setData(doc);
		doFinished(doc, callback, result);
	}

	protected void doFinished(Document doc, Callback<Event> callback,
			Event result) {
		if (callback != null) {
			callback.call(result);
		}
	}

	protected String rebuildSrc(String src) {
		if (!src.startsWith("http")) {
			src = this.getDomainUrl() + src;
			System.out.println("newSrc=" + src);
		}
		return src;
	}

	/**
	 * 获取域名url
	 * 
	 * @return
	 */
	protected String getDomainUrl() {
		Pattern p = Pattern.compile("http(s)?://([\\w-]+\\.)+[\\w-]+(?<=/?)",
				Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(this.getUrl());
		String t;
		if (m.find())
			t = m.group();
		else
			t = null;
		return t;
	}

	// 抓取图片
	protected void grabOne(Callback<Event> callback, String src, int index,
			int total) {
		Event event;
		if (!src.startsWith("http")) {
			if (callback != null) {
				event = new Event();
				event.setType(EventType.Error);
				event.setSrc(src);
				event.setIndex(index);
				event.setTotal(total);
				event.setData(new Exception("src 格式不对"));
				callback.call(event);
			}
			return;
		}

		// 判断是否已经抓取过
		if (GrabberUtils.isGrabbed(src)) {
			if (callback != null) {
				event = new Event();
				event.setType(EventType.Grabbed);
				event.setSrc(src);
				event.setIndex(index);
				event.setTotal(total);
				event.setData(new Exception("已经抓取过"));
				callback.call(event);
			}
			return;
		}

		// 下载
		try {
			URL _url = new URL(src);
			URLConnection con = _url.openConnection();
			con.setConnectTimeout(30000);// 设置连接主机超时（单位：毫秒）
			con.setReadTimeout(30000);// 设置从主机读取数据超时（单位：毫秒）
			InputStream data = con.getInputStream();// 文件流

			if (callback != null) {
				event = new Event();
				event.setType(EventType.AfterGrabOne);
				event.setSrc(src);
				event.setIndex(index);
				event.setTotal(total);
				event.setData(data);
				callback.call(event);
			}
			data.close();
		} catch (Exception e) {
			if (callback != null) {
				event = new Event();
				event.setType(EventType.Error);
				event.setSrc(src);
				event.setIndex(index);
				event.setTotal(total);
				event.setData(e);
				callback.call(event);
			}
		}
	}

	private boolean forceStop;

	public boolean isForceStop() {
		return forceStop;
	}

	public void stop() {
		Log.d(tag, "stop");
		forceStop = true;
	}
}