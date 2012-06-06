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
import org.jsoup.select.Elements;

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
	private String pagingCountSelector;// 获取分页信息的选择器
	private String pagingCountRegx;// 从分页信息解析出总页数的正则表达式
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

	public String getPagingCountSelector() {
		return pagingCountSelector;
	}

	public void setPagingCountSelector(String pagingCountSelector) {
		this.pagingCountSelector = pagingCountSelector;
	}

	public String getPagingCountRegx() {
		return pagingCountRegx;
	}

	public void setPagingCountRegx(String pagingCountRegx) {
		this.pagingCountRegx = pagingCountRegx;
	}

	/**
	 * 获取指定页码的请求url
	 * 
	 * @param pageNo
	 *            页码
	 * @return
	 */
	public String getPagingUrl(int pageNo) {
		// url - 主url
		// urlName - 主url去除扩展名后的部分，如“http://.../xx.html”的“http://.../xx”
		// pageNo - 当前页码
		String pattern = getPagingUrl();
		String url = getUrl();
		if (pagingUrl.indexOf("{url}") != -1)
			pattern = pattern.replaceAll("\\{url\\}", url);
		if (pattern.indexOf("{pageNo}") != -1)
			pattern = pattern
					.replaceAll("\\{pageNo\\}", String.valueOf(pageNo));
		if (pattern.indexOf("{urlName}") != -1) {
			int sepIndex = url.lastIndexOf(".");
			pattern = pattern.replaceAll("\\{urlName\\}",
					url.substring(0, sepIndex));
		}

		return pattern;
	}

	@Override
	public List<Item> list() throws IOException {
		List<Item> pageItems = new ArrayList<Item>();
		PageItem pageItem;

		// 获取当前页
		Document doc = getDocument(getUrl(), this.getUserAgent());

		// 获取分页数
		Elements els = doc.select(this.getPagingCountSelector());
		if (els == null || els.isEmpty())
			throw new IOException("can't find pagingCount info.");
		String pagingInfo = els.text();
		int pageCount = getPageCount(pagingInfo);
		Log.d(tag, "pageCount=" + pageCount);

		File to = new File(sdCardDir, this.getPath());
		pageItem = new PageItem();
		pageItem.setPid(getUid());
		pageItem.setSelector(getSelector());
		pageItem.setFrom(getUrl());
		pageItem.setTo(to);
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
		if (isReversed()) {// 按页码从大到小抓取
			for (int pageIndex = pageCount - 2; pageIndex >= 0; pageIndex--) {
				pageItem = new PageItem();
				pageItem.setPid(getUid());
				pageItem.setSelector(getSelector());
				pageItem.setTo(to);

				pageItem.setFrom(getPagingUrl(pageIndex + 1));
				pageItem.setIndex(pageIndex);
				
				pageItems.add(pageItem);
			}
		} else {// 按页码从小到大抓取
			for (int pageIndex = 1; pageIndex <= pageCount - 1; pageIndex++) {
				pageItem = new PageItem();
				pageItem.setPid(getUid());
				pageItem.setSelector(getSelector());
				pageItem.setTo(to);

				pageItem.setFrom(getPagingUrl(pageIndex + 1));
				pageItem.setIndex(pageIndex);
				
				pageItems.add(pageItem);
			}
		}

		return pageItems;
	}

	/**
	 * 解析出总页数
	 * 
	 * @param pageCountInfo
	 *            包含总页数的文本信息
	 * @return
	 */
	public int getPageCount(String pageCountInfo) {
		String pattern = this.getPagingCountRegx();// 用于解析总页数的正则表达式
		int i = pattern.indexOf("{pageCount}");
		if (i != -1) {
			pattern = "(?<=" + pattern.substring(0, i) + ")(\\d)+(?="
					+ pattern.substring(i + 11) + ")";
		}
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(pageCountInfo);
		String t;
		if (m.find())
			t = m.group();
		else
			t = null;
		return t != null ? Integer.parseInt(t) : 0;
	}
}
