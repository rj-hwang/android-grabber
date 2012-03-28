package cn.gm.android.grabber.cfg;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;
import cn.gm.android.grabber.cfg.Item.Type;

/**
 * 抓取配置项
 * 
 * @author dragon
 * 
 */
public class Config {
	private String userAgent;// 请求时使用的user-agent
	private List<Item> items;// 抓取项的配置列表

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	/**
	 * 加载配置信息
	 * 
	 * @param inputStream
	 * @throws Throwable
	 */
	public void load(InputStream inputStream) throws Throwable {
		this.items = new ArrayList<Item>();
		Item item = null;
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(inputStream, "utf-8");
		int event = parser.getEventType();
		String name, value;
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				name = parser.getName();
				if ("item".equals(name)) {
					item = new Item();
					item.setId(parser.getAttributeValue(0));
					if (parser.getAttributeCount() > 1) {
						item.setNested("true".equals(parser
								.getAttributeValue(1)));
					}
				} else if ("itemId".equals(name)) {
					item.setItemId(parser.nextText().toString());
				} else if ("userAgent".equals(name)) {
					this.setUserAgent(parser.nextText().toString());
				} else if ("name".equals(name)) {
					item.setName(parser.nextText().toString());
				} else if ("dir".equals(name)) {
					item.setDir(parser.nextText().toString());
				} else if ("type".equals(name)) {
					item.setType(Enum.valueOf(Type.class, parser.nextText()
							.toString()));
				} else if ("url".equals(name)) {
					item.setUrl(parser.nextText().toString());
				} else if ("selector".equals(name)) {
					item.setSelector(parser.nextText().toString());
				} else if ("deep".equals(name)) {
					item.setDeep("true".equals(parser.nextText().toString()));
				} else if ("pagingUrl".equals(name)) {
					item.setPagingUrl(parser.nextText().toString());
				} else if ("pagingSelector".equals(name)) {
					item.setPagingSelector(parser.nextText().toString());
				} else if ("pagingRegx".equals(name)) {
					value = parser.nextText().toString();
					int i = value.indexOf("{page}");
					if (i != -1) {
						// 简易配置的处理
						value = "(?<=" + value.substring(0, i) + ")(\\d)+(?="
								+ value.substring(i + 6) + ")";
					}
					item.setPagingRegx(value);
				}
				break;
			case XmlPullParser.END_TAG:
				if ("item".equals(parser.getName())) {
					this.items.add(item);
				}
				break;
			default:
				break;
			}
			event = parser.next();
		}
	}

	@Override
	public String toString() {
		JSONObject json = new JSONObject();
		try {
			json.put("userAgent", this.getUserAgent());
			json.put("items", this.getItems());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/**
	 * 获取指定ID的抓取配置项
	 * @param itemId
	 * @return
	 */
	public Item getItem(String itemId) {
		for (Item item : this.items) {
			if (itemId.equals(item.getId()))
				return item;
		}
		return null;
	}
}
