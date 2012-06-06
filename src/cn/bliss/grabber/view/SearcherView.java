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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bliss.grabber.R;
import cn.bliss.grabber.cfg.Records;
import cn.bliss.grabber.util.GrabberUtils;

/**
 * 搜索器界面UI
 * 
 * @author dragon
 * 
 */
public class SearcherView extends RelativeLayout {
	private String uid;
	private ImageView logo;
	private TextView name;
	private TextView path;
	private TextView date;
	private TextView count;
	private TextView progress;
	private Button optRun;
	private CheckBox optSelect;
	private boolean running = false;

	private Date startTime;
	private static DateFormat df4datetime = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private List<OnStartListener> startEventListeners = new ArrayList<OnStartListener>();
	private List<OnStopListener> stopEventListeners = new ArrayList<OnStopListener>();

	public SearcherView(Context context) {
		super(context);

		// 加载自定义的XML布局
		LayoutInflater li = (LayoutInflater) getContext().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		li.inflate(R.layout.item, this, true);

		// 记录控件的引用
		logo = (ImageView) findViewById(R.id.item_logo);
		name = (TextView) findViewById(R.id.item_name);
		path = (TextView) findViewById(R.id.item_path);
		date = (TextView) findViewById(R.id.item_date);
		count = (TextView) findViewById(R.id.item_count);
		progress = (TextView) findViewById(R.id.item_progress);
		optRun = (Button) findViewById(R.id.item_opt_run);
		optSelect = (CheckBox) findViewById(R.id.item_opt_select);

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

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	private String getText(int resId) {
		return getContext().getText(resId).toString();
	}

	public void setName(String name) {
		this.name.setText(name);
	}

	public String getName() {
		return name.getText().toString();
	}

	public void setLogo(Drawable drawable) {
		this.logo.setImageDrawable(drawable);
	}

	public void setPath(String path) {
		this.path.setText(path);
	}

	public void setDate(String date) {
		this.date.setText(date);
	}

	public int getCount() {
		return Integer.parseInt(count.getText().toString());
	}

	public void setCount(int count) {
		this.count.setText(String.valueOf(count));
	}

	public void addCount(int count) {
		this.count.setText(String.valueOf(Integer.parseInt(this.count.getText()
				.toString()) + count));
	}

	public void setProgress(String progress) {
		this.progress.setText(progress);
	}

	public void setChecked(boolean checked) {
		this.optSelect.setChecked(checked);
	}

	public boolean isChecked() {
		return optSelect.isChecked();
	}

	public boolean isRunning() {
		return running;
	}

	public void start() {
		running = true;

		// 处理界面显示
		optRun.setBackgroundResource(android.R.drawable.ic_media_pause);
		startTime = new Date();
		date.setText(df4datetime.format(startTime));
		progress.setText("...");

		// 记录抓取时间
		Records.getInstance().get(getUid())
				.setDate(df4datetime.format(new Date()));

		// 发布开始事件
		for (OnStartListener l : startEventListeners) {
			l.onStart(this);
		}
	}

	public void stop() {
		running = false;

		// 处理界面显示
		optRun.setBackgroundResource(android.R.drawable.ic_media_play);
		date.setText(date.getText() + " "
				+ GrabberUtils.getWasteTime(startTime));
		progress.setText(getText(R.string.info_stoped));

		// 记录抓取时间
		Records.getInstance().get(getUid())
				.setDate(df4datetime.format(new Date()));

		// 发布事件
		for (OnStopListener l : stopEventListeners) {
			l.onStop(this);
		}
	}

	public void setOnStartListener(OnStartListener event) {
		startEventListeners.add(event);
	}

	public void setOnStopListener(OnStopListener event) {
		stopEventListeners.add(event);
	}

	/**
	 * 开始抓取事件
	 * 
	 * @author dragon
	 * 
	 */
	public interface OnStartListener {
		void onStart(SearcherView view);
	}

	/**
	 * 停止抓取事
	 * 
	 * @author dragon
	 * 
	 */
	public interface OnStopListener {
		void onStop(SearcherView view);
	}

	public void finish() {
		running = false;

		// 处理界面显示
		optRun.setBackgroundResource(android.R.drawable.ic_media_play);
		date.setText(date.getText() + " "
				+ GrabberUtils.getWasteTime(startTime));
		progress.setText(getText(R.string.info_grabbed));

		// 记录抓取时间
		Records.getInstance().get(getUid())
				.setDate(df4datetime.format(new Date()));
	}

	public void error() {
		running = false;

		// 处理界面显示
		optRun.setBackgroundResource(android.R.drawable.ic_media_play);
		date.setText(date.getText() + " "
				+ GrabberUtils.getWasteTime(startTime));
		progress.setText(getText(R.string.info_error));

		// 记录抓取时间
		Records.getInstance().get(getUid())
				.setDate(df4datetime.format(new Date()));
	}
}