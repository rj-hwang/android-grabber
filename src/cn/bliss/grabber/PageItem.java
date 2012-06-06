package cn.bliss.grabber;

import java.util.ArrayList;
import java.util.List;

import cn.bliss.grabber.event.EventType;
import cn.bliss.grabber.event.GrabEvent;

/**
 * 抓取项
 * 
 * @author dragon
 * 
 */
public class PageItem extends Item {
	private List<Item> items = new ArrayList<Item>();// 本页包含的抓取项列表

	public void add(List<Item> items) {
		for(Item item:items){
			item.setPid(getPid());
		}
		items.addAll(items);
	}

	public int size() {
		return items.size();
	}

	@Override
	public void excute() {
		// 循环每一项执行抓取
		for (final Item item : items) {
			// 转发Item的抓取事件
			item.setOnProcessListener(new OnProcessListener() {
				@Override
				public void onProcess(GrabEvent event) {
					for (OnProcessListener l : processEventListeners) {
						l.onProcess(event);
					}
				}
			});

			// 执行抓取
			item.excute();
		}

		// 发布页抓取完毕事件
		for (OnProcessListener l : processEventListeners) {
			l.onProcess(new GrabEvent(this, getIndex(), EventType.GrabOnePage));
		}
	}
}
