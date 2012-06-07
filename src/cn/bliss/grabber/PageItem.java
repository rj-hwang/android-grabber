package cn.bliss.grabber;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;
import cn.bliss.grabber.cfg.Records;
import cn.bliss.grabber.event.EventType;
import cn.bliss.grabber.event.GrabEvent;
import cn.bliss.grabber.searcher.HttpSearcher;

/**
 * 抓取项
 * 
 * @author dragon
 * 
 */
public class PageItem extends Item {
	private static final String tag = PageItem.class.getName();
	private String selector;// 该页中抓取项的选择表达式
	private int count;// 本页搜索到的抓取项的数量

	//
	// public void add(List<Item> items) {
	// for(Item item:items){
	// item.setPid(getPid());
	// }
	// items.addAll(items);
	// }

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	public int getCount() {
		return count;
	}

	@Override
	public void excute() {
		try {
			// 获取匹配的元素
			Document doc = HttpSearcher.getDocument(getFrom(), null);// TODO add
																		// Agent
			Elements els = doc.select(getSelector());
			if (els == null || els.isEmpty()) {
				throw new IOException("find empty items from:" + getFrom());
			}
			count = els.size();
			Log.d(tag, "count=" + count);

			// 循环每个元素抓取
			String url;
			String domain = HttpSearcher.getDomain(getFrom());
			int index = 0;
			for (Element el : els) {
				url = HttpSearcher.getItemFrom(el, domain);

				// 避免重复抓取
				if (Records.getInstance().has(url)) {
					// 发布抓取被忽略事件
					for (OnProcessListener l : processEventListeners) {
						l.onProcess(new GrabEvent(this, index, EventType.Skip,
								count));
					}
				} else {
					// 执行抓取
					grabOne(getPid(), url, getTo());

					// 发布抓完一项事件
					for (OnProcessListener l : processEventListeners) {
						l.onProcess(new GrabEvent(this, index,
								EventType.GrabOneItem, count));
					}
				}
				index++;
			}

			// 发布抓完一页事件
			for (OnProcessListener l : processEventListeners) {
				l.onProcess(new GrabEvent(this, getIndex(),
						EventType.GrabOnePage, count));
			}
		} catch (IOException e) {
			Log.e(tag, e.getMessage(), e);
			// 发布异常事件
			for (OnProcessListener l : processEventListeners) {
				l.onProcess(new GrabEvent(this, getIndex(), EventType.Error, e));
			}
		}
	}
}
