/**
 * 
 */
package cn.bliss.grabber.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bliss.android.grabber.R;

/**
 * 配置项UI
 * 
 * @author dragon
 * 
 */
public class SearcherUI extends RelativeLayout {
	private ImageView logo;
	private TextView name;
	private TextView path;
	private TextView date;
	private TextView count;
	private TextView progress;
	private Button optRun;
	private CheckBox optSelect;
	private boolean running = false;

	public SearcherUI(Context context) {
		super(context);
		init();
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

		// 绑定处理事件
		bindButtonEvent();
	}

	private void bindButtonEvent() {
		// 执行按钮
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
		optRun.setBackgroundResource(android.R.drawable.ic_media_pause);
		running = false;
	}

	public void start() {
		optRun.setBackgroundResource(android.R.drawable.ic_media_play);
		running = true;
	}
}
