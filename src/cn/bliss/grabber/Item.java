package cn.bliss.grabber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.List;

import android.util.Log;
import cn.bliss.grabber.cfg.History;
import cn.bliss.grabber.cfg.Records;
import cn.bliss.grabber.event.EventType;
import cn.bliss.grabber.event.GrabEvent;

/**
 * 抓取项
 * 
 * @author dragon
 * 
 */
public class Item implements Command {
	private static final String tag = Item.class.getName();
	private String pid;// 所隶属的配置项的id
	private String from;// 抓取项的来源地址
	private File to;// 要抓取到的地方
	private int index;// 索引号
	protected static DateFormat df4fileName = new SimpleDateFormat(
			"yyyyMMddHHmmssSSSS");
	protected static DateFormat df4datetime = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public File getTo() {
		return to;
	}

	public void setTo(File to) {
		this.to = to;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}


	@Override
	public void excute() {
		// 避免重复抓取
		if (Records.getInstance().has(getFrom())) {
			// 发布抓取被忽略事件
			for (OnProcessListener l : processEventListeners) {
				l.onProcess(new GrabEvent(this, getIndex(),
						EventType.Skip));
			}
			return;
		}

		// 抓取数据
		try {
			URL url = new URL(this.from);
			URLConnection con = url.openConnection();
			con.setConnectTimeout(10000);// 设置连接主机超时（单位：毫秒）
			con.setReadTimeout(10000);// 设置从主机读取数据超时（单位：毫秒）
			InputStream is = con.getInputStream();// 文件流

			// 创建要保存到的路径
			if (!to.exists()) {
				to.mkdirs();
			}

			// 保存到文件
			byte[] bs = new byte[1024];// 1K的数据缓冲
			int len;// 读取到的数据长度
			File wto = new File(to,getToFilename(getFrom()));
			OutputStream os = new FileOutputStream(wto);
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			os.close();
			
			// 添加抓取记录
			History history = new History();
			history.setDate(df4datetime.format(new Date()));
			history.setFrom(getFrom());
			history.setTo(wto.getAbsolutePath());
			Log.d(tag, "---------");
			Records.getInstance().addHistory(pid, history, true);

			// 发布抓完事件
			for (OnProcessListener l : processEventListeners) {
				l.onProcess(new GrabEvent(this, getIndex(),
						EventType.GrabOneItem));
			}
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
			// 发布异常事件
			for (OnProcessListener l : processEventListeners) {
				l.onProcess(new GrabEvent(this, getIndex(), EventType.Error, e));
			}
		}
	}

	/**
	 * 获取文件名,并附加日期前缀，如"/a/b/c/test.txt"返回"yyyyMMdd_test.txt"
	 * 
	 * @param path
	 * @return
	 */
	public static String getToFilename(String src) {
		return df4fileName.format(new Date()) + "." + getFilenameExtension(src);
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
		int lIndex = path.lastIndexOf("/");
		if(lIndex > sepIndex)
			return ".png";// 没有扩展名就默认给一个
		return (sepIndex != -1 ? path.substring(sepIndex + 1) : null);
	}

	// == 事件相关
	protected List<OnProcessListener> processEventListeners = new ArrayList<OnProcessListener>();

	/**
	 * 添加侦听器
	 * 
	 * @param event
	 */
	public void setOnProcessListener(OnProcessListener event) {
		processEventListeners.add(event);
	}

	/**
	 * 抓取过程事件
	 * 
	 * @author dragon
	 * 
	 */
	public interface OnProcessListener extends EventListener {
		/**
		 * @param event
		 *            事件对象
		 */
		void onProcess(GrabEvent event);
	}
}
