package cn.bliss.grabber.event;

import java.util.EventObject;

import cn.bliss.grabber.Item;


/**
 * 抓取项事件对象
 * 
 * @author dragon
 * 
 */
public class GrabEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private int index;// 当前抓取项的索引号
	private int count;// 总抓取数
	private EventType type;// 抓取类型
	private Exception error;// 记录整个异常对象（当抓取出现异常时）

	public GrabEvent(Item source, int index, EventType type) {
		super(source);
		this.index = index;
		this.type = type;
	}

	public GrabEvent(Item source, int index, EventType type, int count) {
		super(source);
		this.index = index;
		this.type = type;
		this.count = count;
	}

	public GrabEvent(Item source, int index, EventType type, Exception error) {
		this(source,index,type);
		this.error = error;
	}

	public int getCount() {
		return count;
	}

	public int getIndex() {
		return index;
	}

	public EventType getType() {
		return type;
	}

	public Exception getError() {
		return error;
	}
}
