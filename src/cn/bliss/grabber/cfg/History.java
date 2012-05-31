package cn.bliss.grabber.cfg;


/**
 * 抓取历史
 * 
 * @author dragon
 * 
 */
public class History {
	private String date;// 抓取时间:yyyy-MM-dd HH:mm:ss
	private String from;// 原始抓取路径
	private String to;// 保存到的路径

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}
}