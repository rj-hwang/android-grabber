package cn.gm.android.grabber.activity;

import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Grab;
import cn.gm.android.grabber.R;
import cn.gm.android.grabber.Result;
import cn.gm.android.grabber.cfg.Config;
import cn.gm.android.grabber.impl.MeiNvMenGrabber;
import cn.gm.android.grabber.impl.OOXXGrabber;
import cn.gm.android.grabber.util.GrabberUtils;

public class MainActivity extends Activity {
	private static final String tag = MainActivity.class.getSimpleName();

	private TextView info;
	private boolean doing = false;
	private Grab<Result> grab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		info = (TextView) findViewById(R.id.textView4GrabbingInfo);

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

		info.setText(GrabberUtils.init());

		// 加载配置文件
		config = new Config();
		InputStream inputStream = this.getClass().getClassLoader()
				.getResourceAsStream("assets/config.xml");
		try {
			config.load(inputStream);
			System.out.println(config.toString());
		} catch (Throwable e) {
			Log.e(tag, e.getMessage(), e);
		}
	}

	private Config config;
	private Handler handler;

	private void startGrab() {
		Log.i(tag, "startGrab...");

		// 妹子图的配置参数
		final boolean doMeiZiTu = ((CheckBox) this.findViewById(R.id.doMeiZiTu))
				.isChecked();
		final boolean doMeiZiTuDeepGrab = ((CheckBox) this
				.findViewById(R.id.doMeiZiTuDeepGrab)).isChecked();
		String _deepGrabFrom = ((EditText) this
				.findViewById(R.id.doMeiZiTuDeepGrabFrom)).getText().toString();
		final int doMeiZiTuDeepGrabFrom;
		if (_deepGrabFrom != null && _deepGrabFrom.length() > 0) {
			doMeiZiTuDeepGrabFrom = Integer.parseInt(_deepGrabFrom);
		} else {
			doMeiZiTuDeepGrabFrom = 0;
		}

		// 美女门的配置参数
		final boolean doMeiNvMen = ((CheckBox) this
				.findViewById(R.id.doMeiNvMen)).isChecked();
		final boolean doMeiNvMenDeepGrab = ((CheckBox) this
				.findViewById(R.id.doMeiNvMenDeepGrab)).isChecked();

		Log.i(tag, "doMeiZiTu=" + doMeiZiTu);
		Log.i(tag, "doMeiNvMen=" + doMeiNvMen);

		// 开启一个线程进行抓取
		Thread thread = new Thread(new Runnable() {
			public void run() {
				// 抓取妹子图
				if (doMeiZiTu) {
					OOXXGrabber grab = new OOXXGrabber();
					grab.setDeepGrab(doMeiZiTuDeepGrab);
					grab.setDeepGrabFrom(doMeiZiTuDeepGrabFrom);
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
				}

				// 抓取美女门
				if (doMeiNvMen) {
					MeiNvMenGrabber grab = new MeiNvMenGrabber();
					grab.setDeepGrab(doMeiNvMenDeepGrab);
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
				}

				MainActivity.this.grab = null;
				doing = false;
			}
		});
		thread.start();
	}
}