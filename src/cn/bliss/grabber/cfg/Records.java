package cn.bliss.grabber.cfg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

/**
 * 抓取记录
 * 
 * @author dragon
 * 
 */
public class Records {
	private static final String tag = Records.class.getName();
	private static Records instance;
	private File historyFile;// 历史记录文件
	private File recordFile;// 主记录文件
	private Map<String, Record> rs = new LinkedHashMap<String, Record>();

	private Records() {
	}

	public static Records getInstance() {
		if (instance == null)
			instance = new Records();
		return instance;
	}

	public void load() {
		// 加载主记录
		Log.i(tag, "加载主记录");
		try {
			if (recordFile.exists()) {
				InputStream in = new FileInputStream(recordFile);
				BufferedReader buffreader = new BufferedReader(
						new InputStreamReader(in));
				String line;
				String[] data;
				Record r;
				while ((line = buffreader.readLine()) != null) {
					if (line == null || line.length() == 0
							|| line.startsWith("#"))
						continue;
					// Log.d(tag,"line="+line);
					data = line.split(","); // --uid,最后抓取时间,已抓总数,保存路径
					r = rs.get(data[0]);
					if (r == null) {
						Log.w(tag, "配置项已被移除：id=" + data[0]);
						r = new Record();
						r.setHistoryFile(historyFile);
						r.setUid(data[0]);
						r.setPath(data[3]);
					}
					r.setDate(data[1]);
					// Log.d(tag,"length="+data.length);
					r.setCount(Integer.parseInt(data[2]));
					rs.put(r.getUid(), r);
				}
				in.close();
			}
		} catch (IOException e) {
			Log.e(tag, "加载主记录失败！", e);
		}

		// 加载抓取项记录
		Log.i(tag, "加载抓取项记录");
		try {
			if (historyFile.exists()) {
				InputStream in = new FileInputStream(historyFile);
				BufferedReader buffreader = new BufferedReader(
						new InputStreamReader(in));
				String line;
				String[] data;
				Record r;
				History h;
				while ((line = buffreader.readLine()) != null) {
					if (line == null || line.length() == 0
							|| line.startsWith("#"))
						continue;
					data = line.split(","); // --id,抓取时间,源url,保存为的文件
					r = rs.get(data[0]);
					h = new History();
					h.setDate(data[1]);
					h.setFrom(data[2]);
					h.setTo(data[3]);
					r.add(h, false);
				}
				in.close();
			}
		} catch (IOException e) {
			Log.e(tag, "加载抓取项记录失败！", e);
		}
	}

	public Record get(String recordId) {
		return rs.get(recordId);
	}

	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public void save() {
		// 保存主记录文件：record.log
		// --id,date,count,path
		// --id,最后抓取时间,已抓总数,保存路径
		if (!recordFile.exists()) {
			// 创建主记录文件的所在目录
			if (!recordFile.getParentFile().exists()) {
				recordFile.getParentFile().mkdirs();
			}
		}
		try {
			// 写文件
			Log.i(tag, "写入主记录");
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					recordFile));
			writer.write("# " + df.format(new Date()) + " by dragon");
			writer.write("\n# id,最后抓取时间,已抓总数,保存路径");
			Record r;
			for (String k : rs.keySet()) {
				r = rs.get(k);
				writer.write("\n" + r.getUid() + ","
						+ (r.getDate() == null ? "" : r.getDate()) + ","
						+ r.getCount() + "," + r.getPath());
			}
			writer.close();
		} catch (IOException e) {
			Log.e(tag, "写主记录文件失败", e);
		}
	}

	/**
	 * 判断指定的源是否已经抓取过
	 * 
	 * @param from
	 *            抓取项的涞源地址
	 * @return
	 */
	public boolean has(String from) {
		for (Entry<String, Record> e : rs.entrySet()) {
			if (e.getValue().has(from)) {
				return true;
			}
		}
		return false;
	}

	public File getRecordFile() {
		return recordFile;
	}

	public int getCount() {
		int c = 0;
		for (String k : rs.keySet()) {
			c += rs.get(k).getCount();
		}
		return c;
	}

	public void setRecordFile(File recordFile) {
		this.recordFile = recordFile;
	}

	public File getHistoryFile() {
		return historyFile;
	}

	public void setHistoryFile(File historyFile) {
		this.historyFile = historyFile;
	}

	public void add(Record r) {
		r.setHistoryFile(historyFile);
		rs.put(r.getUid(), r);
	}

	public void addHistory(String recordId, History history, boolean isNew) {
		Record r = rs.get(recordId);
		if (r != null) {
			r.add(history, isNew);
			r.setCount(r.getCount() + 1);
			System.out.println("rc=" + r.getCount());
		} else {
			Log.e(tag, "not exists record:uid=" + recordId);
		}
	}
}
