package cn.gm.android.grabber.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Grab;
import cn.gm.android.grabber.R;
import cn.gm.android.grabber.Result;
import cn.gm.android.grabber.impl.OOXXGrabber;

public class MainActivity extends Activity {
	private static final String tag = MainActivity.class.getSimpleName();

	private TextView info;
	ScrollView scroll;
	private boolean doing = false;
	private Grab<Result> grab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		scroll = (ScrollView) findViewById(R.id.scrollView1);
		info = (TextView) findViewById(R.id.textView1);

		// 开始事件
		Button btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(tag, "onClick:doing=" + doing);
				if (!doing) {
					doing = true;
					startGrab();
				} else {
					info.setText("\n正在抓取,别乱点！" + info.getText());
				}
			}
		});

		// 终止事件
		Button btnStop = (Button) findViewById(R.id.btnStop);
		btnStop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(tag, "btnStop");
				if (grab != null)
					grab.stop();
			}
		});

		// 退出事件
		Button btnQuit = (Button) findViewById(R.id.btnQuit);
		btnQuit.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Log.d(tag, "btnQuit");
				if (grab != null)
					grab.stop();

				// 退出程序
				// android.os.Process.killProcess(android.os.Process.myPid());
				MainActivity.this.finish();
			}
		});

		// 清屏事件
		Button btnClean = (Button) findViewById(R.id.btnClean);
		btnClean.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				info.setText("");
			}
		});

		// 处理回调信息
		this.handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Log.d(tag, "data=" + msg.getData().getString("msg"));
				info.setText(msg.getData().getString("msg") + "\n"
						+ info.getText());
				// scroll.fullScroll(View.FOCUS_DOWN);
				super.handleMessage(msg);
			}
		};
	}

	private Handler handler;

	private void startGrab() {
		Log.i(tag, "startGrab...");
		final boolean deepGrab = ((CheckBox) this.findViewById(R.id.checkBox1))
				.isChecked();

		// 开启一个线程进行抓取
		Thread thread = new Thread(new Runnable() {
			public void run() {
				OOXXGrabber grab = new OOXXGrabber();
				grab.setDeepGrab(deepGrab);
				MainActivity.this.grab = grab;
				grab.excute(null, new Callback<Result>() {
					public void call(Result result) {
						// 将回调信息传给界面的UI线程显示
						Message message = new Message();
						Bundle data = new Bundle();
						data.putString("msg", result.getMsg());
						message.setData(data);
						MainActivity.this.handler.sendMessage(message);
					}
				});

				MainActivity.this.grab = null;
				doing = false;
			}
		});
		thread.start();
	}
}