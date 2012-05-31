package cn.bliss.grabber.cfg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;
import cn.bliss.grabber.Searcher;
import cn.bliss.grabber.searcher.AbstractSearcher;
import cn.bliss.grabber.searcher.HttpSearcher;
import cn.bliss.grabber.searcher.PagingSearcher;
import cn.bliss.grabber.searcher.SimpleSearcher;

/**
 * 抓取配置
 * 
 * @author dragon
 * 
 */
public class Config {
	private InputStream from;
	private String userAgent;
	private List<Searcher> list;

	/**获取用户代理
	 * @return
	 */
	public String getUserAgent() {
		return userAgent;
	}
	
	/**设置配置文件
	 * @param from
	 * @return
	 */
	public Config setFrom(InputStream from) {
		this.from = from;
		return this;
	}

	/**
	 * 获取抓取配置列表
	 * 
	 * @return
	 */
	public List<Searcher> list() {
		if (list != null)
			return list;

		list = new ArrayList<Searcher>();
		this.loadXml(from);
		return list;
	}

	private void loadXml(InputStream in) {
		try {
			AbstractSearcher searcher = null;
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in, "utf-8");
			int event = parser.getEventType();
			String id, type, name;
			while (event != XmlPullParser.END_DOCUMENT) {
				name = parser.getName();
				switch (event) {
				case XmlPullParser.START_TAG:
					if ("userAgent".equals(name)) {
						this.userAgent = parser.nextText().toString();
					} else if ("searcher".equals(name)) {
						id = parser.getAttributeValue(0);
						type = parser.getAttributeValue(1);
						// System.out.println("id=" + id);
						// System.out.println("type=" + type);
						if ("simple".equals(type)) {
							searcher = new SimpleSearcher();
						} else if ("paging".equals(type)) {
							searcher = new PagingSearcher();
						}
						if (searcher != null)
							searcher.setId(id);

					} else if ("name".equals(name)) {
						searcher.setName(parser.nextText().toString());
					} else if ("path".equals(name)) {
						searcher.setPath(parser.nextText().toString());
					} else if ("url".equals(name)) {
						if (searcher instanceof HttpSearcher)
							((HttpSearcher) searcher).setUrl(parser.nextText()
									.toString());
					} else if ("selector".equals(name)) {
						if (searcher instanceof HttpSearcher)
							((HttpSearcher) searcher).setSelector(parser
									.nextText().toString());
					}
					break;
				case XmlPullParser.END_TAG:
					if ("searcher".equals(name)) {
						if (searcher != null) {
							if (searcher instanceof HttpSearcher)
								((HttpSearcher) searcher)
										.setUserAgent(this.userAgent);
							this.list.add(searcher);
						}
					}
					break;
				default:
					break;
				}
				event = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
