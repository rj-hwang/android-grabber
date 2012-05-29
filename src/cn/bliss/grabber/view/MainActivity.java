/**
 * 
 */
package cn.bliss.grabber.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import cn.bliss.grabber.Config;
import cn.bliss.grabber.R;
import cn.bliss.grabber.Searcher;
import cn.bliss.grabber.view.SearcherUI.OnStartListener;

/**
 * @author dragon
 * 
 */
public class MainActivity extends Activity {
	private LinearLayout items;
	private Button totalOptlRun;
	private CheckBox totalOptlSelect;
	private boolean running;
	private Map<String, Integer> logos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// 图标列表
		logos = new HashMap<String, Integer>();
		logos.put("meizitu_home", R.drawable.ic_launcher);

		// 界面元素
		items = (LinearLayout) this.findViewById(R.id.items_container);
		totalOptlRun = (Button) this.findViewById(R.id.totalOpt_run);
		totalOptlSelect = (CheckBox) this.findViewById(R.id.totalOpt_select);

		// 加载配置列表
		List<Searcher> searchers = new Config(this.getApplicationContext(),
				R.raw.config).list();
		SearcherUI searcherUI;
		Integer resid;
		for (Searcher searcher : searchers) {
			searcherUI = new SearcherUI(this);
			searcherUI.setTag(searcher);
			searcherUI.setName(searcher.getName());
			searcherUI.setPath(searcher.getPath());
			resid = getLogoResid(searcher.getId());
			searcherUI.setLogoResource(resid != null ? resid
					: R.drawable.ic_launcher);
			searcherUI.setCount(0);// TODO 从抓取历史中获取

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

	private Integer getLogoResid(String id) {
		return logos.get(id);
	}
}