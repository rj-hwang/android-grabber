/**
 * 
 */
package cn.bliss.grabber.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.bliss.grabber.Item;
import cn.bliss.grabber.R;
import cn.bliss.grabber.Searcher;
import cn.bliss.grabber.util.GrabberUtils;

/**
 * 配置项UI
 * 
 * @author dragon
 * 
 */
public class SearcherUI extends RelativeLayout {
	private static final String tag = SearcherUI.class.getName();
	private ImageView logo;
	private TextView name;
	private TextView path;
	private TextView date;
	private TextView count;
	private TextView progress;
	private Button optRun;
	private CheckBox optSelect;
	private boolean running = false;

	private Handler handler;
	private Thread thread;
	private static DateFormat df4date = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static DateFormat df4fileName = new SimpleDateFormat(
			"yyyyMMddHHmmssSSSS");

	public SearcherUI(Context context) {
		super(context);
		init();
	}

	private String getText(int resId) {
		return getContext().getText(resId).toString();
	}

	private void init() {
		// Inflate the view from the layout resource.
		LayoutInflater li = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		li.inflate(R.layout.item, this, true);

		// 获取子元素的引用
		logo = (ImageView) findViewById(R.id.item_logo);
		name = (TextView) findViewById(R.id.item_name);
		path = (TextView) findViewById(R.id.item_path);
		date = (TextView) findViewById(R.id.item_date);
		count = (TextView) findViewById(R.id.item_count);
		progress = (TextView) findViewById(R.id.item_progress);
		optRun = (Button) findViewById(R.id.item_opt_run);
		optSelect = (CheckBox) findViewById(R.id.item_opt_select);

		// 初始化消息处理对象
		initHandler();

		// 绑定处理事件
		bindButtonEvent();
	}

	public static final int MT_STARTED = 1;// 已启动
	public static final int MT_FINDED = 2;// 查找完毕
	public static final int MT_GRAB_ONE_ITEM = 3;// 已抓取完一项
	public static final int MT_GRAB_ONE_PAGE = 4;// 已抓取完一页
	public static final int MT_FINISHED = 5;// 全部抓取完毕
	public static final int MT_SKIP = 6;// 已抓取过忽略
	public static final int MT_STOPED = 8;// 中途停止
	public static final int MT_ERROR = 9;// 异常

	private void initHandler() {
		this.handler = new Handler() {
			private Date startDate;
			private int successCount;

			@Override
			public void handleMessage(Message m) {
				System.out.println("what=" + m.what);
				switch (m.what) {
				case MT_STARTED:
					successCount = 0;
					break;
				case MT_FINDED:
					// 显示要抓取的总数量
					progress.setText("..." + m.getData().getInt("count"));
					break;
				case MT_GRAB_ONE_ITEM:
					// 累计成功抓取的总数
					successCount += 1;

					// 显示抓取进度
					progress.setText("..." + m.getData().getInt("index") + "/"
							+ m.getData().getInt("count"));
					break;
				case MT_FINISHED:
					running = false;

					// 处理界面显示
					optRun.setBackgroundResource(android.R.drawable.ic_media_play);
					Date endDate = new Date();
					date.setText(df4date.format(endDate) + " "
							+ GrabberUtils.getWasteTime(startDate, endDate));
					progress.setText(getText(R.string.info_grabbed) + " "
							+ successCount);
					count.setText((Integer.parseInt(count.getText().toString()) + m
							.getData().getInt("count")) + "");
					break;
				case MT_ERROR:
					running = false;
					// 强制其实异常信息
					Toast.makeText(
							SearcherUI.this.getContext(),
							getText(R.string.info_grab) + "'" + name.getText()
									+ "'" + getText(R.string.info_error) + ":"
									+ m.getData().getString("msg"),
							Toast.LENGTH_LONG).show();
					Log.e(tag, "error:" + m.getData().getString("msg"));

					// 处理界面显示
					optRun.setBackgroundResource(android.R.drawable.ic_media_play);
					progress.setText(getText(R.string.info_fireEeror));
					break;
				default:
				}

				// 发布事件
				for (OnProcessListener p : processEventListeners) {
					p.onProcess(SearcherUI.this, m.what);
				}

				super.handleMessage(m);
			}
		};
	}

	private void bindButtonEvent() {
		// 执行|停止按钮
		optRun.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (running) {
					stop();
				} else {
					start();
				}
			}
		});

	}

	public void setName(String name) {
		this.name.setText(name);
	}

	public void setLogoResource(int resid) {
		this.logo.setImageResource(resid);
	}

	public void setPath(String path) {
		this.path.setText(path);
	}

	public void setDate(String date) {
		this.date.setText(date);
	}

	public void setCount(int count) {
		this.count.setText(String.valueOf(count));
	}

	public void setProgress(String progress) {
		this.progress.setText(progress);
	}

	public void setChecked(boolean checked) {
		this.optSelect.setChecked(checked);
	}

	public boolean isChecked() {
		return this.optSelect.isChecked();
	}

	public boolean isRunning() {
		return this.running;
	}

	public void stop() {
		running = false;

		// 处理界面显示
		optRun.setBackgroundResource(android.R.drawable.ic_media_play);
		progress.setText(getText(R.string.info_stoped));

		// 停止抓取线程
		if (thread != null)
			thread.interrupt();

		// 发布事件
		for (OnProcessListener p : processEventListeners) {
			p.onProcess(SearcherUI.this, MT_STOPED);
		}
	}

	public void start() {
		running = true;

		// 处理界面显示
		optRun.setBackgroundResource(android.R.drawable.ic_media_pause);
		Date startDate = new Date();
		date.setText(df4date.format(startDate));
		progress.setText("...");

		// 启动抓取线程
		startThread();

		// 发布事件
		for (OnProcessListener p : processEventListeners) {
			p.onProcess(SearcherUI.this, MT_STARTED);
		}
	}

	private void startThread() {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// 将回调信息传给界面的UI线程显示
				final Searcher searcher = (Searcher) SearcherUI.this.getTag();
				try {
					Message message = new Message();
					message.what = MT_STARTED;
					handler.sendMessage(message);

					// 搜索抓取项
					List<Item> items = searcher.list();
					message = new Message();
					message.what = MT_FINDED;
					Bundle data = new Bundle();
					message.setData(data);
					data.putInt("count", items.size());
					handler.sendMessage(message);

					// 逐项抓取
					int index = 1;
					for (Item item : items) {
						message = new Message();
						data = new Bundle();
						message.setData(data);
						data.putInt("count", items.size());
						data.putInt("index", index);
						//data.putInt("page", item.getPage());
						if (running) {
							Log.i(tag, "from=" + item.getFrom());

							// 执行抓取并保存
							item.excute();

							// 发送信息
							message.what = MT_GRAB_ONE_ITEM;
							handler.sendMessage(message);
						} else {
							message.what = MT_STOPED;
							handler.sendMessage(message);
							break;// 停抓
						}
						index++;
					}

					// 完成抓取的处理
					message = new Message();
					message.what = MT_FINISHED;
					data = new Bundle();
					message.setData(data);
					data.putInt("count", items.size());
					handler.sendMessage(message);
				} catch (Exception e) {
					Message message = new Message();
					message.what = MT_ERROR;
					Bundle data = new Bundle();
					message.setData(data);
					data.putString("msg", e.getMessage());
					handler.sendMessage(message);
				}
			}
		});
		thread.start();
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
		return (sepIndex != -1 ? path.substring(sepIndex + 1) : null);
	}

	// == 事件相关
	private List<OnProcessListener> processEventListeners = new ArrayList<OnProcessListener>();

	public void setOnProcessListener(OnProcessListener event) {
		processEventListeners.add(event);
	}

	/**
	 * 开始抓取事件
	 * 
	 * @author dragon
	 * 
	 */
	public interface OnStartListener {
		void onStart(View view);
	}

	/**
	 * 停止抓取事
	 * 
	 * @author dragon
	 * 
	 */
	public interface OnPauseListener {
		void onPause(View view);
	}

	/**
	 * 抓取过程事
	 * 
	 * @author dragon
	 * 
	 */
	public interface OnProcessListener {
		void onProcess(View view, int what);
	}
}