package cn.bliss.grabber.cfg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import android.util.Log;

/**
 * 抓取分记录
 * 
 * @author dragon
 * 
 */
public class Record {
	private static final String tag = Record.class.getName();
	private String id;
	private String date;// 最后抓取时间:yyyy-MM-dd HH:mm:ss
	private int count;// 已抓取的数量
	private String path;// 抓取保存到的路径
	private Map<String, History> hs = new LinkedHashMap<String, History>();
	private File historyFile;// 历史记录文件

	public void setHistoryFile(File historyFile) {
		this.historyFile = historyFile;
	}

	/**
	 * 判断指定的源是否已经抓取过
	 * 
	 * @param from
	 *            抓取项的涞源地址
	 * @return
	 */
	public boolean has(String from) {
		return hs.containsKey(from);
	}
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void add(History history, boolean isNew) {
		hs.put(history.getFrom(), history);

		// 如果是新纪录直接写入文件
		if (isNew) {
			try {
				// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
				// --id,抓取时间,源url,保存为的文件
				Log.i(tag, "写入新的抓取项");
				FileWriter writer = new FileWriter(historyFile, true);
				if(!historyFile.exists()){
					writer.write("# " + df.format(new Date()) + " by dragon");
					writer.write("\n# id,抓取时间,源url,保存为的文件");
				}
				writer.write("\n" + this.getId() + "," + history.getDate()
						+ "," + history.getFrom() + "," + history.getTo());
				writer.close();
			} catch (IOException e) {
				Log.e(tag, "写入抓取项记录失败", e);
			}
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}