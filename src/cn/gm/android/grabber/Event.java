package cn.gm.android.grabber;

/**
 * 抓取事件
 * 
 * @author dragon
 * 
 */
public class Event {
	private String name;// 摘要
	private EventType type;// 事件类型
	private String src;// 抓取的源地址
	private int index;// 当前抓取条目的索引号，从1开始
	private int total;// 总共要抓取的条目数
	private Object data;// 数据

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		this.src = src;
	}
}
