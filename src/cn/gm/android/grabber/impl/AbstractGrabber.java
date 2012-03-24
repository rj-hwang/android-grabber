package cn.gm.android.grabber.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Environment;
import android.util.Log;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Grab;
import cn.gm.android.grabber.Result;
import cn.gm.android.grabber.util.GrabberUtils;

/**
 * 妹子图网图片抓取器
 * 
 * @author dragon
 * 
 */
public abstract class AbstractGrabber implements Grab<Result> {
	private static final String tag = AbstractGrabber.class.getName();
	private String url;// 抓取的网址
	private String selector;// 图片选择器
	private String subDir;// 图片保存到sd卡下的子目录路径
	private String userAgent;// 抓取时使用的User-Agent，为空则不使用
	private boolean deepGrab;// 是否深度抓取

	public boolean isDeepGrab() {
		return deepGrab;
	}

	public void setDeepGrab(boolean deepGrab) {
		this.deepGrab = deepGrab;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public String getSubDir() {
		return subDir;
	}

	public void setSubDir(String subDir) {
		this.subDir = subDir;
	}

	// 抓取网站
	public void excute(Object context, Callback<Result> callback) {
		Date startDate = new Date();
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
		Elements imgs = doc.select(this.getSelector());
		Log.i(tag, "total＝" + imgs.size());
		File sdCardDir = Environment.getExternalStorageDirectory();// sd卡的目录路径
		if (callback != null) {
			result = new Result();
			result.setType(Result.TYPE_FINDED);
			result.setMsg("找到" + imgs.size() + "张图片");
			result.setTotal(imgs.size());
			result.setSuccess(false);
			result.setFrom(this.getUrl());
			result.setData(doc);
			callback.call(result);
		}

		// 循环每一个图片进行抓取并保存到sd卡指定的目录
		String imgUrl;
		int index = 0;
		for (Element img : imgs) {
			imgUrl = this.rebuildImgUrl(img.attr("src"));
			Log.d(tag, "img＝" + imgUrl);
			this.excuteOne(context, callback, imgUrl, index, imgs.size(),
					sdCardDir);
			index++;

			if (forceStop) {
				result = new Result();
				result.setType(Result.TYPE_ERROR);
				result.setMsg("====强制终止====");
				result.setTotal(imgs.size());
				result.setSuccess(false);
				result.setFrom(this.getUrl());
				callback.call(result);
				break;
			}
		}

		// 执行深度抓取
		if (this.isDeepGrab()) {
			this.excuteDeepGrab(context, callback, doc);
		}

		if (callback != null) {
			result = new Result();
			result.setType(Result.TYPE_END);

			result.setMsg("====抓取完毕====" + GrabberUtils.getWasteTime(startDate));
			result.setTotal(imgs.size());
			result.setSuccess(false);
			result.setFrom(this.getUrl());
			result.setData(doc);
			callback.call(result);
		}
	}

	protected String rebuildImgUrl(String imgUrl) {
		return imgUrl;
	}

	// 执行深度抓取
	protected void excuteDeepGrab(Object context, Callback<Result> callback,
			Document doc) {
		// do nothing
	}

	// 抓取图片
	protected void excuteOne(Object context, Callback<Result> callback,
			String imgUrl, int index, int total, File sdCardDir) {
		Result result;
		File saveToFile;
		if (!imgUrl.startsWith("http")) {
			if (callback != null) {
				result = new Result();
				result.setSuccess(false);
				result.setType(Result.TYPE_ERROR);
				result.setFrom(imgUrl);
				result.setIndex(index);
				result.setTotal(total);
				result.setMsg((index + 1) + "/" + total + "	,失败! - "
						+ result.getFrom());
				callback.call(result);
			}
			return;
		}

		// 图片保存到的路径
		saveToFile = new File(sdCardDir, this.getSubDir()
				+ GrabberUtils.getFilename(imgUrl));
		if (GrabberUtils.isGrabbed(imgUrl)) {
			if (callback != null) {
				result = new Result();
				result.setSuccess(false);
				result.setType(Result.TYPE_ERROR);
				result.setFrom(imgUrl);
				result.setIndex(index);
				result.setTotal(total);
				result.setMsg((index + 1) + "/" + total + "	,忽略! - 已抓取过");
				callback.call(result);
			}
			return;
		}

		// 创建图片要保存到的路径
		if (!saveToFile.getParentFile().exists()) {
			Log.d(tag, "createDir:"
					+ saveToFile.getParentFile().getAbsolutePath());
			saveToFile.getParentFile().mkdirs();
		}
		Log.d(tag, "saveToFile:" + saveToFile.getAbsolutePath());

		// 下载并保存图片
		boolean success = GrabberUtils.download(imgUrl, saveToFile);

		if (callback != null) {
			result = new Result();
			result.setSuccess(success);
			result.setType(Result.TYPE_AFTER_GRAB_ONE);
			result.setFrom(imgUrl);
			result.setTo(saveToFile.getAbsolutePath());
			result.setIndex(index);
			result.setTotal(total);
			if (success) {
				result.setMsg((index + 1) + "/" + total + "	,成功");
			} else {
				result.setMsg((index + 1) + "/" + total + "	,失败!");
			}
			callback.call(result);
		}
	}

	private boolean forceStop;

	public boolean isForceStop() {
		return forceStop;
	}

	public synchronized void stop() {
		Log.d(tag, "stop");
		forceStop = true;
	}
}