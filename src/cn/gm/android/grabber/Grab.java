package cn.gm.android.grabber;

/**
 * 抓取接口
 * 
 * @author dragon
 * 
 */
public interface Grab {
	/**
	 * @param callback
	 *            每个文件成功抓取后的回调处理
	 */
	void start(Callback<Event> callback);

	/**
	 * 停止抓取
	 */
	void stop();
}
