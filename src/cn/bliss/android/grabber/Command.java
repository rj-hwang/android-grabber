package cn.bliss.android.grabber;

import java.io.IOException;

/**
 * 抓取接口
 * 
 * @author dragon|rongjihuang@gmail.com
 * 
 */
public interface Command {
	/**
	 * 执行
	 */
	void excute() throws IOException;
}
