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

import org.jsoup.nodes.Document;

import android.util.Log;
import cn.bliss.grabber.Item;
import cn.bliss.grabber.PageItem;

/**
 * 分页搜索器
 * 
 * @author dragon
 * 
 */
public class PagingSearcher extends HttpSearcher {
	private static final String tag = PagingSearcher.class.getName();
	private String pagingUrl;// 分页网址的url模式
	private String pagingSelector;// 获取分页信息的选择器
	private String pagingRegx;// 从分页信息解析出总页数的正则表达式
	private boolean reversed;// 是否逆向分业抓取，即从第一页开始抓还是从最后页开始抓

	public boolean isReversed() {
		return reversed;
	}

	public void setReversed(boolean reversed) {
		this.reversed = reversed;
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
	public List<Item> list() throws IOException {
		List<Item> items, pageItems = new ArrayList<Item>();
		PageItem pageItem;

		// 获取当前页
		Document doc = find(items = new ArrayList<Item>(), getUrl(),
				getSelector());

		// 获取分页数
		String pagingInfo = doc.select(this.getPagingSelector()).text();
		int pageCount = buildPageCount(pagingInfo, this.getPagingRegx());
		Log.d(tag, "pageCount=" + pageCount);

		File to = new File(sdCardDir, this.getPath());
		pageItem = new PageItem();
		pageItem.setPid(getUid());
		pageItem.setFrom(getUrl());
		pageItem.setTo(to);
		pageItem.add(items);
		pageItems.add(pageItem);

		// 没有分页信息直接返回
		if (pageCount < 2) {
			pageItem.setIndex(0);// 第一页
			return pageItems;
		}
		if (isReversed()) {
			pageItem.setIndex(pageCount - 1);// 最后页
		}

		// 循环每一页
		String pagingUrl;
		if (isReversed()) {// 按页码从大到小抓取
			for (int pageIndex = pageCount - 2; pageIndex >= 0; pageIndex--) {
				pagingUrl = buildPagingUrl(this.getPagingUrl(), this.getUrl(),
						pageIndex);
				find(items = new ArrayList<Item>(), pagingUrl, getSelector());

				pageItem = new PageItem();
				pageItem.setFrom(pagingUrl);
				pageItem.setIndex(pageIndex);
				pageItem.setTo(to);
				pageItem.add(items);
				pageItems.add(pageItem);
			}
		} else {// 按页码从小到大抓取
			for (int pageIndex = 1; pageIndex <= pageCount - 1; pageIndex++) {
				pagingUrl = buildPagingUrl(this.getPagingUrl(), this.getUrl(),
						pageIndex);
				find(items = new ArrayList<Item>(), pagingUrl, getSelector());

				pageItem = new PageItem();
				pageItem.setFrom(pagingUrl);
				pageItem.setTo(to);
				pageItem.setIndex(pageIndex);
				pageItem.add(items);
				pageItems.add(pageItem);
			}
		}

		return pageItems;
	}

	/**
	 * 解析出总页数
	 * 
	 * @param text
	 *            包含总页数的文本信息
	 * @param pattern
	 *            用于解析总页数的正则表达式
	 * @return
	 */
	public static int buildPageCount(String text, String pattern) {
		int i = pattern.indexOf("{page}");
		if (i != -1) {
			pattern = "(?<=" + pattern.substring(0, i) + ")(\\d)+(?="
					+ pattern.substring(i + 6) + ")";
		}
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		String t;
		if (m.find())
			t = m.group();
		else
			t = null;
		return t != null ? Integer.parseInt(t) : 0;
	}

	/**
	 * 构建实际的分页请求url
	 * 
	 * @param pattern
	 *            抽象的分页url
	 * @param url
	 *            原始的url
	 * @param pageIndex
	 *            页索引号，从0开始
	 * @return
	 */
	public static String buildPagingUrl(String pattern, String url,
			int pageIndex) {
		if (pattern.indexOf("{url}") != -1)
			pattern = pattern.replaceAll("\\{url\\}", url);
		if (pattern.indexOf("{page}") != -1)
			pattern = pattern.replaceAll("\\{page\\}",
					String.valueOf(pageIndex + 1));
		if (pattern.indexOf("{urlName}") != -1) {
			int sepIndex = url.lastIndexOf(".");
			pattern = pattern.replaceAll("\\{urlName\\}",
					url.substring(0, sepIndex));
		}

		return pattern;
	}
}
