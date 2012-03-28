package cn.gm.android.grabber.cfg;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 抓取配置项
 * 
 * @author dragon
 * 
 */
public class Item {
	/**
	 * 配置类型
	 * 
	 * @author dragon
	 * 
	 */
	public enum Type {
		/** 单页抓取 */
		Single,
		/** 分页抓取 */
		Multi,
		/** 导航页 */
		Nav
	};

	private String id;// ID
	private String name;// 名称
	private Type type;// 类型
	private String dir;// 抓取后保存到sdcard/grabber下的相对目录，开头和结尾不带"/"
	private String url;// 地址
	private String selector;// 选择器
	private boolean deep;// 深抓

	private boolean nested;// 是否为内嵌的配置
	private String itemId;// 导航项使用的抓取配置的id，仅在type=Nav时有效

	/**
	 * 分页抓取的url模式
	 * <p>
	 * 如妹子图的"{u}-{p}"
	 * </p>
	 * <ul>
	 * <li>{url} -- 代表url属性的值</li>
	 * <li>{urlName} -- 代表url属性小数点前面部分的值</li>
	 * <li>{page} -- 代表要抓取的页码</li>
	 * </ul>
	 * 
	 */
	private String pagingUrl;
	private String pagingSelector;// 总页数字符串的选择器
	private String pagingRegx;// 获取总页数的数字的正则表达式，不配置代表pagingSelector获取的就是数字格式

	/**
	 * 解析总页码数
	 * 
	 * @param pagingString
	 *            包含总页码数的字符串
	 * @param pagingRegx
	 *            解析出数字的正则表达式
	 * @return 总页码数，如果解析异常，返回-1
	 */
	public static int getPageCount(String pagingString, String pagingRegx) {
		String page;
		if (pagingRegx == null) {
			page = pagingString;
		} else {
			Pattern p = Pattern.compile(pagingRegx);
			Matcher m = p.matcher(pagingString);
			if (m.find()) {
				page = m.group();
			} else {
				return -1;
			}
		}
		try {
			return Integer.parseInt(page);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * 解析出分页URL
	 * 
	 * @param pattern
	 *            分页url的配置模版
	 * @param url
	 *            用于代替{url}、{urlName}占位符的值
	 * @param page
	 *            用于代替{page}占位符的值
	 * @return
	 */
	public static String getPagingUrl(String pattern, String url, int page) {
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
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

	public boolean isDeep() {
		return deep;
	}

	public void setDeep(boolean deep) {
		this.deep = deep;
	}

	public boolean isNested() {
		return nested;
	}

	public void setNested(boolean nested) {
		this.nested = nested;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getPagingUrl() {
		return pagingUrl;
	}

	public void setPagingUrl(String pagingUrl) {
		this.pagingUrl = pagingUrl;
	}

	public String getPagingSelector() {
		return pagingSelector;
	}

	public void setPagingSelector(String pagingSelector) {
		this.pagingSelector = pagingSelector;
	}

	public String getPagingRegx() {
		return pagingRegx;
	}

	public void setPagingRegx(String pagingRegx) {
		this.pagingRegx = pagingRegx;
	}

	@Override
	public String toString() {
		JSONObject json = new JSONObject();
		try {
			json.put("id", this.getId());
			json.put("name", this.getName());
			json.put("type", this.getType());
			json.put("dir", this.getDir());
			json.put("url", this.getUrl());
			json.put("selector", this.getSelector());
			json.put("deep", this.isDeep());
			json.put("pagingUrl", this.getPagingUrl());
			json.put("pagingSelector", this.getPagingSelector());
			json.put("pagingRegx", this.getPagingRegx());
			json.put("nested", this.isNested());
			json.put("itemId", this.getItemId());
			// System.out.println("--" + this.getPagingRegx());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
}
