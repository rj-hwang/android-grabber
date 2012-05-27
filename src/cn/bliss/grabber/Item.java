package cn.bliss.grabber;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * 抓取项
 * 
 * @author dragon|rongjihuang@gmail.com
 * 
 */
public class Item implements Command {
	private String from;// 抓取项的来源地址
	private String to;// 要抓取到的地方
	private String type;// 抓取项的类型，如图片、音乐、视频

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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	private boolean excuted;

	@Override
	public void excute() throws IOException {
		if (excuted)
			return;
		// 抓取数据
		URL url = new URL(this.from);
		URLConnection con = url.openConnection();
		con.setConnectTimeout(30000);// 设置连接主机超时（单位：毫秒）
		con.setReadTimeout(30000);// 设置从主机读取数据超时（单位：毫秒）
		InputStream is = con.getInputStream();// 文件流

		// 创建要保存到的路径
		File saveToFile = new File(this.to);
		if (!saveToFile.getParentFile().exists()) {
			saveToFile.getParentFile().mkdirs();
		}

		// 保存到文件
		byte[] bs = new byte[1024];// 1K的数据缓冲
		int len;// 读取到的数据长度
		OutputStream os = new FileOutputStream(saveToFile);
		while ((len = is.read(bs)) != -1) {
			os.write(bs, 0, len);
		}
		os.close();
		excuted = true;
	}
}
