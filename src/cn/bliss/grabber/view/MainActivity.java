/**
 * 
 */
package cn.bliss.grabber.view;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.bliss.grabber.R;
import cn.bliss.grabber.Searcher;
import cn.bliss.grabber.cfg.Config;
import cn.bliss.grabber.cfg.Record;
import cn.bliss.grabber.cfg.Records;
import cn.bliss.grabber.view.SearcherUI.OnProcessListener;
import cn.bliss.grabber.view.SearcherUI.OnStartListener;

/**
 * @author dragon
 * 
 */
public class MainActivity extends Activity {
	private LinearLayout items;
	private Button totalOptlRun;
	private CheckBox totalOptlSelect;
	private TextView totalCount;
	private boolean running;
	private Map<String, Integer> logos;

	// sd卡的目录路径
	private File sdCardDir = Environment.getExternalStorageDirectory();
	private Records records;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("onDestroy");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 图标列表
		logos = new HashMap<String, Integer>();
		logos.put("meizitu_home", R.drawable.ic_launcher);

		// 界面元素
		items = (LinearLayout) this.findViewById(R.id.items_container);
		totalOptlRun = (Button) this.findViewById(R.id.totalOpt_run);
		totalOptlSelect = (CheckBox) this.findViewById(R.id.totalOpt_select);
		totalCount = (TextView) this.findViewById(R.id.total_count);

		// 加载配置列表
		Config config = new Config().setFrom(this.getResources()
				.openRawResource(R.raw.config));
		List<Searcher> searchers = config.list();

		// 加载抓取记录
		records = new Records();
		records.setHistoryFile(new File(sdCardDir, "grabber/history.log"));
		records.setRecordFile(new File(sdCardDir, "grabber/record.log"));
		records.addSearcher(searchers);
		records.load();

		// 将配置添加到界面
		SearcherUI searcherUI;
		Integer resid;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (Searcher searcher : searchers) {
			searcherUI = new SearcherUI(this);
			searcherUI.setTag(searcher);
			searcherUI.setName(searcher.getName());
			searcherUI.setPath(searcher.getPath());
			resid = getLogoResid(searcher.getId());
			searcherUI.setLogoResource(resid != null ? resid
					: R.drawable.ic_launcher);

			// 设置历史抓取记录信息
			final Record record = records.get(searcher.getId());
			searcherUI.setRecord(record);
			searcherUI.setCount(record != null ? record.getCount() : 0);// 合计
			searcherUI.setDate(record != null ? record.getDate() : "");// 日期

			// 添加到界面
			items.addView(searcherUI, new LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			searcherUI.setOnStartListener(new OnStartListener() {
				@Override
				public void onStart(final View view) {
					// 开启一个线程执行抓取
					System.out.println("onStart-------");
				}
			});

			searcherUI.setOnProcessListener(new OnProcessListener() {
				@Override
				public void onProcess(View view, int what) {
					switch (what) {
					case SearcherUI.MT_GRAB_ONE:
						// 总数加1
						totalCount.setText((Integer.parseInt(totalCount
								.getText().toString()) + 1) + "");

						break;
					case SearcherUI.MT_SKIP:
						break;
					default:
					}
				}
			});
		}

		// ==总操作处理==
		// 执行/停止按钮
		totalOptlRun.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				SearcherUI c;
				if (running) {
					totalOptlRun
							.setBackgroundResource(android.R.drawable.ic_media_play);
					running = false;

					for (int i = 0; i < items.getChildCount(); i++) {
						c = (SearcherUI) items.getChildAt(i);
						if (c.isChecked() && c.isRunning())
							c.stop();
					}
				} else {
					totalOptlRun
							.setBackgroundResource(android.R.drawable.ic_media_pause);
					running = true;

					for (int i = 0; i < items.getChildCount(); i++) {
						c = (SearcherUI) items.getChildAt(i);
						if (c.isChecked() && !c.isRunning())
							c.start();
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
						SearcherUI c;
						for (int i = 0; i < items.getChildCount(); i++) {
							c = (SearcherUI) items.getChildAt(i);
							if (c.isChecked() != checked)
								c.setChecked(checked);
						}
					}
				});
	}

	@Override
	protected void onPause() {
		System.out.println("onPause");
		super.onPause();
	}

	@Override
	protected void onRestart() {
		System.out.println("onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		System.out.println("onResume");
		super.onResume();
	}

	@Override
	protected void onStart() {
		System.out.println("onStart");
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		System.out.println("onDestroy");

		// 保存抓取记录
		records.save();

		super.onDestroy();
	}

	@Override
	protected void onStop() {
		System.out.println("onStop");
		super.onStop();
	}

	private Integer getLogoResid(String id) {
		return logos.get(id);
	}
}