package cn.gm.android.grabber;

/**
 * 抓取接口
 * 
 * @author dragon
 * 
 */
public interface Grab<T> {
	/**
	 * @param context
	 *            上下文
	 * @param callback
	 *            每个文件成功抓取后的回调处理
	 */
	void excute(Object context, Callback<T> callback);

	/**
	 * 停止抓取
	 */
	void stop();
}
