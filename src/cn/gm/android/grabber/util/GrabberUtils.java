package cn.gm.android.grabber.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

/**
 * 抓取器工具类
 * 
 * @author dragon
 * 
 */
public class GrabberUtils {
	private static final String tag = GrabberUtils.class.getName();
	private static final String FOLDER_SEPARATOR = "/";

	/**
	 * 获取文件名，如"/a/b/c/test.txt"返回"test.txt"
	 * 
	 * @param path
	 * @return
	 */
	public static String getFilename(String path) {
		if (path == null) {
			return null;
		}
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		return (separatorIndex != -1 ? path.substring(separatorIndex + 1)
				: path);
	}

	/**
	 * 使用URLConnection下载文件并保存到指定的地方
	 * 
	 * @param url
	 *            要下载的文件地址
	 * @param saveToFile
	 *            保存到的文件
	 * @return 下载成功返回true，否则返回false
	 */
	public static boolean download(String url, File saveToFile) {
		try {
			// 构造URL
			URL _url = new URL(url);
			// 打开连接
			URLConnection con = _url.openConnection();
			// 输入流
			InputStream is = con.getInputStream();
			// 1K的数据缓冲
			byte[] bs = new byte[1024];
			// 读取到的数据长度
			int len;
			// 输出的文件流
			OutputStream os = new FileOutputStream(saveToFile);
			// 开始读取
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			// 完毕，关闭所有链接
			os.close();
			is.close();

			return true;
		} catch (Exception e) {
			Log.e(tag, "下载文件失败:url=" + url + ",e=" + e.getMessage());
			return false;
		}
	}
}
