/**
 * 
 */
package cn.bliss.android.grabber.searcher;

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

import cn.bliss.android.grabber.Item;
import cn.bliss.android.grabber.Searcher;

/**
 * 基于Http的搜索器
 * 
 * @author dragon
 * 
 */
public class HttpSearcher extends AbstractSearcher implements Searcher {
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
		// 获取请求页面
		Connection connection = Jsoup.connect(this.getUrl());
		if (this.getUserAgent() != null)
			connection.userAgent(this.getUserAgent());// 使用用户代理
		Document doc = connection.get();

		// 获取匹配的元素
		Elements els = doc.select(this.getSelector());

		// 生成抓取项列表
		List<Item> list = new ArrayList<Item>();
		Item item;
		for (Element el : els) {
			item = new Item();
			item.setFrom(this.getItemFrom(el));
			list.add(item);
		}

		return list;
	}

	private String getItemFrom(Element el) {
		String src = el.attr("src");
		if (!src.startsWith("http")) {
			src = getDomain(this.getUrl()) + src;
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
