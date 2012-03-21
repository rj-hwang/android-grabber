package cn.gm.android.grabber;

/**
 * 抓取结果的封装
 * 
 * @author dragon
 * 
 */
public class Result {
	/** 类型:开始抓取网页前 */
	public static final int TYPE_READY = 1;
	/** 类型:成功抓取网页后 */
	public static final int TYPE_FINDED = 2;
	/** 类型:开始抓取一个图片前 */
	public static final int TYPE_BEFORE_GRAB_ONE = 3;
	/** 类型:成功抓取一个图片后 */
	public static final int TYPE_AFTER_GRAB_ONE = 4;
	/** 类型:全部抓取后 */
	public static final int TYPE_END = 99;
	/** 类型:失败 */
	public static final int TYPE_ERROR = -1;

	private boolean success;// 抓取是否成功
	private int type;// 类型
	private int index;// 当前抓取图片的索引号，从0开始
	private int total;// 总共要抓取的图片数
	private String from;// 抓取的源地址
	private String to;// 抓取到的地方
	private String msg;// 综合信息
	private Object data;// 其它数据

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

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
