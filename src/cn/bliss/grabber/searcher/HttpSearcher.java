/**
 * 
 */
package cn.bliss.grabber.searcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;
import cn.bliss.grabber.Item;
import cn.bliss.grabber.Searcher;

/**
 * 基于Http的搜索器
 * 
 * @author dragon
 * 
 */
public class HttpSearcher extends AbstractSearcher implements Searcher {
	private static final String tag = HttpSearcher.class.getName();
	private String url;// 地址
	private String selector;// 抓取项的选择器
	private String userAgent;// 请求的用户代理

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSelector() {
		return selector;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String agent) {
		this.userAgent = agent;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	@Override
	public List<Item> list() throws IOException {
		List<Item> container = new ArrayList<Item>();
		find(container, getUrl(), getSelector());
		return container;
	}

	protected Document find(List<Item> container, String from, String selector)
			throws IOException {
		Log.d(tag, "----url=" + url);
		Log.d(tag, "----userAgent=" + userAgent);
		Log.d(tag, "----selector=" + selector);

		// 获取请求页面
		Document doc = getDocument(from, userAgent);

		// 获取匹配的元素
		Elements els = doc.select(selector);
		Log.d(tag, "----size=" + els.size());

		// 生成抓取项列表
		Item item;
		int index = 0;
		File to = new File(sdCardDir, this.getPath());
		String domain = getDomain(this.getUrl());
		for (Element el : els) {
			item = new Item();
			item.setIndex(index);
			item.setPid(getUid());
			item.setFrom(getItemFrom(el,domain));
			item.setTo(to);
			container.add(item);

			index++;
		}

		return doc;
	}

	/**
	 * 获取指定url的html文档
	 * 
	 * @param url
	 *            请求的url
	 * @param userAgent
	 *            使用的用户代理
	 * @return
	 * @throws IOException
	 */
	public static Document getDocument(String url, String userAgent)
			throws IOException {
		Log.d(tag, "connecting..." + url);
		Connection connection = Jsoup.connect(url);
		if (userAgent != null)
			connection.userAgent(userAgent);// 使用用户代理
		connection.timeout(1000);
		return connection.get();
	}

	public static String getItemFrom(Element el,String domain) {
		String src = el.attr("src");
		if (!src.startsWith("http")) {
			src = domain + src;
		}
		return src;
	}

	/**
	 * 获取域名
	 * 
	 * @return
	 */
	public static String getDomain(String url) {
		Pattern p = Pattern.compile("http(s)?://([\\w-]+\\.)+[\\w-]+(?<=/?)",
				Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(url);
		String t;
		if (m.find())
			t = m.group();
		else
			t = null;
		return t;
	}
}
