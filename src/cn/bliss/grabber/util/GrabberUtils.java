package cn.bliss.grabber.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.os.Environment;
import android.util.Log;

/**
 * 抓取器工具类
 * 
 * @author dragon
 * 
 */
public class GrabberUtils {
	private static final String tag = GrabberUtils.class.getName();
	private static DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSSS");
	private static Map<String, String> records = new HashMap<String, String>();

	static {
		// init();
	}

	public static String init() {
		// 加载以往的抓取记录
		try {
			File sdCardDir = Environment.getExternalStorageDirectory();// sd卡的目录路径
			File cfg = new File(sdCardDir, "grabber/records.log");
			if (!cfg.exists()) {
				// 创建记录文件
				if (!cfg.getParentFile().exists()) {
					cfg.getParentFile().mkdirs();
				}
				BufferedWriter out = new BufferedWriter(new FileWriter(cfg));
				out.write("# " + df.format(new Date()) + " by dragon");
				out.close();
				return "没有抓取记录";
			} else {
				Log.d(tag, "load config file:" + cfg.getAbsolutePath());

				InputStream instream = new FileInputStream(cfg);
				InputStreamReader inputreader = new InputStreamReader(instream);
				BufferedReader buffreader = new BufferedReader(inputreader);
				String line;
				String ts, url;
				int index, c = 0;
				while ((line = buffreader.readLine()) != null) {
					if (line == null || line.length() == 0
							|| line.startsWith("#"))
						continue;
					index = line.indexOf(" ");
					if (index == -1)
						continue;
					ts = line.substring(0, index);
					url = line.substring(index + 1);
					records.put(url, ts);
					c++;
				}
				instream.close();
				String msg = "已抓取过" + c + "张";
				Log.i(tag, msg);
				return msg;
			}
		} catch (Exception e) {
			Log.e(tag, "加载抓取记录失败！", e);
			return "加载抓取记录失败";
		}
	}

	/**
	 * 获取文件名,并附加日期前缀，如"/a/b/c/test.txt"返回"yyyyMMdd_test.txt"
	 * 
	 * @param path
	 * @return
	 */
	public static String getFilename(String path) {
		// if (path == null) {
		// return null;
		// }
		// int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR);
		// String name = (separatorIndex != -1 ? path
		// .substring(separatorIndex + 1) : path);

		return df.format(new Date()) + "." + getFilenameExtension(path);
	}

	/**
	 * 获取文件扩展名,如"/a/b/c/test.txt"返回"txt"
	 * 
	 * @param path
	 *            the file path (may be <code>null</code>)
	 * @return 文件扩展名, or <code>null</code>
	 */
	public static String getFilenameExtension(String path) {
		if (path == null) {
			return null;
		}
		int sepIndex = path.lastIndexOf(".");
		return (sepIndex != -1 ? path.substring(sepIndex + 1) : null);
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

			// 添加一个成功抓取记录
			addGrabRecord(url);

			return true;
		} catch (Exception e) {
			Log.e(tag, "下载文件失败:url=" + url + ",e=" + e.getMessage());
			return false;
		}
	}

	public static void addGrabRecord(String url) throws FileNotFoundException {
		String ts = df.format(new Date());
		records.put(url, ts);

		try {
			File sdCardDir = Environment.getExternalStorageDirectory();// sd卡的目录路径
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(new File(sdCardDir,
					"grabber/records.log"), true);
			writer.write("\n" + records.size() + "-" + ts + " " + url);
			writer.close();
		} catch (IOException e) {
			Log.e(tag, "读写配置文件失败", e);
		}
	}

	/**
	 * 计算指定时间到当前时间之间的耗时描述信息
	 * 
	 * @param fromDate
	 *            开始时间
	 * @return
	 */
	public static String getWasteTime(Date fromDate) {
		return getWasteTime(fromDate, new Date());
	}

	/**
	 * 计算指定时间范围内的耗时描述信息
	 * 
	 * @param startDate
	 *            开始时间
	 * @param endDate
	 *            结束时间
	 * @return
	 */
	public static String getWasteTime(Date startDate, Date endDate) {
		long wt = endDate.getTime() - startDate.getTime();
		if (wt < 1000) {
			return wt + "ms";
		} else if (wt < 60000) {
			long ms = wt % 1000;
			return ((wt - ms) / 1000) + "s " + ms + "ms";
		} else {
			long ms = wt % 1000;
			long s = (wt - ms) % 60;
			return ((wt - s - ms) / 60000) + "m " + s + "s " + ms + "ms";
		}
	}

	/**
	 * 判断指定的url是否曾经成功抓取过
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isGrabbed(String url) {
		return records.containsKey(url);
	}

	/**
	 * 保存文件流到指定文件
	 * 
	 * @param inputStream
	 * @param root
	 *            文件所在根目录
	 * @param dir
	 *            子目录
	 * @param src
	 *            文件抓取的原始路径
	 */
	public static File storeFile(InputStream inputStream, File root,
			String dir, String src) {
		// 保存到的文件
		File saveToFile = new File(root, dir + "/"
				+ GrabberUtils.getFilename(src));

		// 创建要保存到的路径
		if (!saveToFile.getParentFile().exists()) {
			saveToFile.getParentFile().mkdirs();
		}

		byte[] bs = new byte[1024];// 1K的数据缓冲
		int len;// 读取到的数据长度
		try {
			// 输出的文件流
			OutputStream os = new FileOutputStream(saveToFile);

			// 开始读取
			while ((len = inputStream.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			os.close();

			// 添加一个成功抓取记录
			GrabberUtils.addGrabRecord(src);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return saveToFile;
	}
}
