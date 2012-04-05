package cn.gm.android.grabber;

/**
 * 抓取事件的类型
 * 
 * @author dragon
 * 
 */
public enum EventType {
	/** 开始抓取网页前 */
	BeforeConnect,
	/** 抓取网页成功后 */
	AfterConnect,
	/** 开始抓取其中一个项目前 */
	BeforeGrabOne,
	/** 抓取其中一个项目成功后 */
	AfterGrabOne,
	/** 全部条目抓取成功后 */
	Finished,
	/** 终止 */
	Stop,
	/** 抓取异常 */
	Error,
	/** 已经抓取过 */
	Grabbed
}