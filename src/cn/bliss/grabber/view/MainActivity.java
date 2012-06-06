/**
 * 
 */
package cn.bliss.grabber.view;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.bliss.grabber.Grabber;
import cn.bliss.grabber.R;
import cn.bliss.grabber.Searcher;
import cn.bliss.grabber.cfg.Config;
import cn.bliss.grabber.cfg.Record;
import cn.bliss.grabber.cfg.Records;

/**
 * @author dragon
 * 
 */
public class MainActivity extends Activity {
	private static final String tag = MainActivity.class.getName();
	private LinearLayout items;
	private Button totalOptRun;
	private CheckBox totalOptlSelect;
	private TextView totalCount;
	private boolean running;
	private Map<String, Integer> logos;
	private Map<String, SearcherView> searcherViews = new HashMap<String, SearcherView>();
	private List<Grabber> grabbers = new ArrayList<Grabber>();

	// sd卡的目录路径
	private File sdCardDir = Environment.getExternalStorageDirectory();
	private Records records;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(tag, "onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 图标列表
		logos = new HashMap<String, Integer>();
		logos.put("meizitu_home", R.drawable.ic_launcher);

		// 界面元素
		items = (LinearLayout) this.findViewById(R.id.items_container);
		totalOptRun = (Button) this.findViewById(R.id.totalOpt_run);
		totalOptlSelect = (CheckBox) this.findViewById(R.id.totalOpt_select);
		totalCount = (TextView) this.findViewById(R.id.total_count);
		((ImageView) this.findViewById(R.id.app_logo))
				.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// 转到关于界面
						startActivity(new Intent(MainActivity.this,
								AboutActivity.class));
					}
				});

		// 加载配置列表
		Config config = new Config().setFrom(this.getResources()
				.openRawResource(R.raw.config));
		final List<Searcher> searchers = config.list();

		// 初始化抓取记录
		records = Records.getInstance();
		File historyFile = new File(sdCardDir, "grabber/history.log");
		records.setHistoryFile(historyFile);
		records.setRecordFile(new File(sdCardDir, "grabber/record.log"));
		Record r;
		for (Searcher s : searchers) {
			r = new Record();
			r.setUid(s.getUid());
			r.setPath(s.getPath());
			records.add(r);
		}
		records.load();

		// 显示已抓取的总数量
		totalCount.setText(String.valueOf(records.getCount()));

		// 将配置添加到界面
		SearcherView searcherView;
		Drawable drawable;
		Grabber grabber;
		for (final Searcher searcher : searchers) {
			searcherView = new SearcherView(this);
			searcherView.setUid(searcher.getUid());
			searcherView.setName(searcher.getName());
			searcherView.setPath(searcher.getPath());
			drawable = getLogo(searcher.getUid());
			searcherView.setLogo(drawable);

			// 设置历史抓取记录信息
			final Record record = records.get(searcher.getUid());
			searcherView.setCount(record != null ? record.getCount() : 0);// 合计
			searcherView.setDate(record != null ? record.getDate() : "");// 最后抓取日期

			// 添加到界面
			items.addView(searcherView, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			grabbers.add(grabber = new Grabber(searcher, searcherView));
		}

		// ==总操作处理==
		// 执行/停止按钮
		totalOptRun.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (running) {
					totalOptRun
							.setBackgroundResource(android.R.drawable.ic_media_play);
					running = false;

					for (Grabber grabber : grabbers) {
						if (grabber.isChecked() && grabber.isRunning())
							grabber.stop();
					}
				} else {
					totalOptRun
							.setBackgroundResource(android.R.drawable.ic_media_pause);
					running = true;

					for (Grabber grabber : grabbers) {
						if (grabber.isChecked() && !grabber.isRunning())
							grabber.start();
					}
				}
			}
		});

		// 全选按钮
		totalOptlSelect
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton btn,
							boolean checked) {
						for (Grabber grabber : grabbers) {
							if (grabber.isChecked() != checked)
								grabber.setChecked(checked);
						}
					}
				});
	}

	@Override
	protected void onDestroy() {
		Log.d(tag, "onDestroy");

		// 停抓所有
		for (Grabber grabber : grabbers) {
			if (grabber.isRunning())
				grabber.stop();
		}

		// 保存抓取记录
		records.save();

		super.onDestroy();
	}

	private Drawable getLogo(String id) {
		Integer resId = logos.get(id);
		return resId != null ? this.getResources().getDrawable(resId)
				: getResources().getDrawable(R.drawable.ic_launcher);
	}
}