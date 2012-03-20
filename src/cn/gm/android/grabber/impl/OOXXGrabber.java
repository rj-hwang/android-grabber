package cn.gm.android.grabber.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.os.Environment;
import android.util.Log;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Grab;
import cn.gm.android.grabber.util.GrabberUtils;

/**
 * 妹子图网图片抓取器
 * 
 * @author dragon
 * 
 */
public class OOXXGrabber implements Grab<Map<String, String>> {
	private static final String tag = OOXXGrabber.class.getName();

	public void excute(Object context, Callback<Map<String, String>> callback) {
		// 抓取的网址
		String url = "http://jandan.net/ooxx";

		// 图片选择器
		String selector = "div.acv_comment>img,ol.commentlist>li>p>img";

		// 图片保存到sd卡下的子目录路径
		String subDir = "grabber/ooxx/";

		// 抓取网页内容
		Document doc;
		try {
			if (callback != null) {
				Map<String, String> result = new HashMap<String, String>();
				result.put("type", "ready");
				result.put("msg", "开始抓取:" + url);
				callback.call(result);
			}
			Connection connection = Jsoup.connect(url);
			// connection
			// .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.79 Safari/535.11");
			doc = connection.get();
		} catch (IOException e) {
			Log.e(tag, "抓取网页失败:url=" + url + ",e=" + e.getMessage());

			if (callback != null) {
				Map<String, String> result = new HashMap<String, String>();
				result.put("type", "error");
				result.put("msg", "抓取失败:" + url + ",e=" + e.getMessage());
				callback.call(result);
			}
			return;
		}
		Elements imgs = doc.select(selector);
		Log.i(tag, "count＝" + imgs.size());
		File sdCardDir = Environment.getExternalStorageDirectory();// sd卡的目录路径
		File saveToFile;
		if (callback != null) {
			Map<String, String> result = new HashMap<String, String>();
			result.put("type", "beforeStart");
			result.put("count", String.valueOf(imgs.size()));
			result.put("url", url);
			result.put("msg", "找到" + imgs.size() + "张图片,开抓中...");
			callback.call(result);
		}

		// 循环每一个图片进行抓取并保存到sd卡指定的目录
		String imgUrl;
		int index = 1;
		for (Element img : imgs) {
			imgUrl = img.attr("src");
			Log.d(tag, "img＝" + imgUrl);

			// 图片保存到的路径
			saveToFile = new File(sdCardDir, subDir
					+ GrabberUtils.getFilename(imgUrl));

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
				Map<String, String> result = new HashMap<String, String>();
				result.put("type", "one");
				result.put("count", String.valueOf(imgs.size()));
				result.put("index", String.valueOf(index));
				result.put("success", String.valueOf(success));
				result.put("url", imgUrl);
				result.put("saveTo", saveToFile.getAbsolutePath());
				callback.call(result);
			}

			index++;
		}
	}
}