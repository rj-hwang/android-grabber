package cn.bliss.grabber.event;

/**
 * 抓取事件的类型
 * 
 * @author dragon
 * 
 */
public enum EventType {
	/** 已搜索完毕 */
	Finded,
	/** 已抓取一项 */
	GrabOneItem,
	/** 已抓取一页 */
	GrabOnePage,
	/** 被停止 */
	Stoped,
	/** 异常 */
	Error,
	/** 忽略 */
	Skip,
	/** 全部完毕 */
	Finished
}
