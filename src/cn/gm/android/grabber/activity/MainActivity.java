package cn.gm.android.grabber.activity;

import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Grab;
import cn.gm.android.grabber.R;
import cn.gm.android.grabber.impl.OOXXGrabber;

public class MainActivity extends Activity {
	private static final String tag = MainActivity.class.getSimpleName();

	private TextView info;
	ScrollView scroll;
	private boolean doing = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		scroll = (ScrollView) findViewById(R.id.scrollView1);
		info = (TextView) findViewById(R.id.textView1);
		scroll.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				Log.d(tag, "onTouch:doing=" + doing);
				if (!doing) {
					doing = true;
					startGrab();
				} else {
					info.setText(info.getText() + "\n正在抓取,别乱点！");
				}
				return false;
			}
		});
	}

	private void startGrab() {
		Log.i(tag, "startGrab...");

		// 处理回调信息
		this.handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Log.d(tag, "data=" + msg.getData().getString("msg"));
				info.setText(info.getText() + "\n"
						+ msg.getData().getString("msg"));
				scroll.fullScroll(View.FOCUS_DOWN);
				super.handleMessage(msg);
			}
		};

		// 开启一个线程进行抓取
		Thread thread = new Thread(new Runnable() {
			public void run() {
				Grab<Map<String, String>> grab = new OOXXGrabber();
				grab.excute(null, new Callback<Map<String, String>>() {
					public void call(Map<String, String> result) {
						String type = result.get("type");
						String msg = "";
						if ("error".equals(type) || "ready".equals(type)
								|| "beforeStart".equals(type)) {
							msg = result.get("msg");
						} else if ("one".equals(type)) {
							boolean success = "true".equals(result
									.get("success"));
							int index = Integer.parseInt(result.get("index"));
							int count = Integer.parseInt(result.get("count"));
							msg += "(" + index + "/" + count + ")";
							if (success) {
								msg += "抓到" + result.get("saveTo");
							} else {
								msg += "失败," + result.get("url");
							}
							if (index == count) {
								msg += "\n完了！";
							}

						}

						Message message = new Message();
						Bundle data = new Bundle();
						data.putString("msg", msg);
						message.setData(data);
						MainActivity.this.handler.sendMessage(message);
					}
				});
				doing = false;
			}
		});

		thread.start();
	}

	private Handler handler;
}