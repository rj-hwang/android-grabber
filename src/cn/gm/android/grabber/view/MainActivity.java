package cn.gm.android.grabber.view;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import cn.gm.android.grabber.Callback;
import cn.gm.android.grabber.Event;
import cn.gm.android.grabber.Grab;
import cn.gm.android.grabber.R;
import cn.gm.android.grabber.cfg.Config;
import cn.gm.android.grabber.cfg.Item;
import cn.gm.android.grabber.impl.PagingGrabber;
import cn.gm.android.grabber.impl.SimpleGrabber;
import cn.gm.android.grabber.util.GrabberUtils;

public class MainActivity extends Activity {
	private static final String tag = MainActivity.class.getSimpleName();
	// 菜单项
	final private int MENU_SETTING = 1;
	final private int MENU_ABOUT = 2;
	final private int MENU_START = 3;
	final private int MENU_STOP = 4;
	final private int MENU_CLEAN = 5;
	private static final int REQ_SETTING = 1;

	private ImageView imageViewer;
	private TextView info;
	private Handler handler;
	private boolean forceStop;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		this.initStoreConfig();
		info = (TextView) findViewById(R.id.textView4GrabbingInfo);
		imageViewer = (ImageView) findViewById(R.id.imageView1);

		// 加载抓取记录
		info.setText(GrabberUtils.init() + "\n" + info.getText());

		// 处理回调信息
		this.handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String _msg = msg.getData().getString("msg");
				if (_msg != null) {
					info.setText(_msg + "\n" + info.getText());
				} else {
					String file = msg.getData().getString("file");
					if (file != null) {
						File _file = new File(file);
						if (_file.exists() && _file.canRead()) {
							Bitmap b = BitmapFactory.decodeFile(file);
							if (b != null) {
								imageViewer.setImageBitmap(b);
							}
						}
					}
				}
				super.handleMessage(msg);
			}
		};
	}

	// 创建菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_START, Menu.NONE, R.string.main_menu_start);
		menu.add(Menu.NONE, MENU_STOP, Menu.NONE, R.string.main_menu_stop);
		menu.add(Menu.NONE, MENU_CLEAN, Menu.NONE, R.string.main_menu_clean);
		menu.add(Menu.NONE, MENU_SETTING, Menu.NONE, R.string.main_menu_setting);
		menu.add(Menu.NONE, MENU_ABOUT, Menu.NONE, R.string.main_menu_about);
		return super.onCreateOptionsMenu(menu);
	}

	// 菜单选择事件处理
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTING:
			// 转到设置界面
			startActivityForResult(new Intent(this, SettingActivity.class),
					REQ_SETTING);
			break;
		case MENU_ABOUT:
			// 转到关于界面
			startActivityForResult(new Intent(this, AboutActivity.class), 0);
			break;
		case MENU_START:
			// 开始抓取
			this.startGrab();
			break;
		case MENU_STOP:
			// 终止抓取
			this.stopGrab();

			break;
		case MENU_CLEAN:
			// 清屏
			info.setText("");

			break;
		default:
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void stopGrab() {
		Log.w(tag,"--stop--");
		this.forceStop = true;
		if (this.grab != null) {
			grab.stop();
		}
	}

	private boolean grabbing = false;
	protected Grab grab;

	private void startGrab() {
		if (grabbing) {
			Log.w(tag, "正在抓取");
			info.setText("正在抓取\n" + info.getText());
			return;
		}
		this.forceStop = false;
		grabbing = true;

		// 开启一个线程进行抓取
		Thread thread = new Thread(new Runnable() {
			public void run() {
				Date startTime = new Date();
				Grab grab;
				Date oneStartTime;
				for (final Item item : Config.getInstance().getItems()) {
					Log.d(tag, "iname=" + item.getName());
					Log.d(tag, "itype=" + item.getType());
					if (forceStop
							|| !item.isSelected()
							|| (item.getType() != Item.Type.Single && item
									.getType() != Item.Type.Multi))
						continue;

					if (item.getType() == Item.Type.Single) {
						grab = new SimpleGrabber()
								.setDir("/grabber/" + item.getDir())
								.setUrl(item.getUrl())
								.setUserAgent(
										Config.getInstance().getUserAgent())
								.setSelector(item.getSelector())
								.setName(item.getName());
					} else if (item.getType() == Item.Type.Multi) {
						grab = new PagingGrabber()
								.setPagingUrl(item.getPagingUrl())
								.setPagingSelector(item.getPagingSelector())
								.setPagingRegx(item.getPagingRegx())
								.setDir("/grabber/" + item.getDir())
								.setUrl(item.getUrl())
								.setUserAgent(
										Config.getInstance().getUserAgent())
								.setSelector(item.getSelector())
								.setName(item.getName());
					} else {
						grab = null;
					}
					MainActivity.this.grab = grab;
					oneStartTime = new Date();
					grab.start(new Callback<Event>() {
						public void call(Event event) {
							String msg = null;
							switch (event.getType()) {
							case BeforeConnect:
								msg = "连接 " + event.getName() + " "
										+ event.getSrc();
								break;
							case AfterConnect:
								msg = "找到" + event.getTotal() + "张";
								break;
							case BeforeGrabOne:
								break;
							case AfterGrabOne:
								msg = event.getTotal() + "-" + event.getIndex()
										+ "	OK";
								File saveToFile = GrabberUtils.storeFile(
										(InputStream) event.getData(),
										sdCardDir, "grabber/" + item.getDir(),
										event.getSrc());
								// 将回调信息传给界面的UI线程显示图片
								Message message = new Message();
								Bundle data = new Bundle();
								data.putString("file",
										saveToFile.getAbsolutePath());
								message.setData(data);
								MainActivity.this.handler.sendMessage(message);

								break;
							case Finished:
								// msg = "抓取完毕";
								break;
							case Error:
								if (event.getTotal() > 0)
									msg = event.getTotal()
											+ "-"
											+ event.getIndex()
											+ "	"
											+ ((Exception) event.getData())
													.getMessage();
								else
									msg = ((Exception) event.getData())
											.getMessage();

								break;
							case Grabbed:
								msg = event.getTotal() + "-" + event.getIndex()
										+ "	已抓";
								break;
							case Stop:
								msg = "终止";
								break;
							default:
								break;
							}

							if (msg != null) {
								// 将回调信息传给界面的UI线程显示
								Message message = new Message();
								Bundle data = new Bundle();
								data.putString("msg", msg);
								message.setData(data);
								MainActivity.this.handler.sendMessage(message);
							}
						}
					});

					// 显示抓取耗时信息
					Message message = new Message();
					Bundle data = new Bundle();
					message.setData(data);
					data.putString("msg", "抓取 " + item.getName() + " 耗时 "
							+ GrabberUtils.getWasteTime(oneStartTime));
					MainActivity.this.handler.sendMessage(message);
				}

				MainActivity.this.grab = null;
				grabbing = false;

				// 显示抓取总耗时信息
				Message message = new Message();
				Bundle data = new Bundle();
				message.setData(data);
				data.putString("msg",
						"此次抓取总耗时 " + GrabberUtils.getWasteTime(startTime));
				MainActivity.this.handler.sendMessage(message);
			}
		});
		thread.start();
	}

	// sd卡的目录路径
	private File sdCardDir = Environment.getExternalStorageDirectory();

	// 加载曾经的配置
	private void initStoreConfig() {
		// 取得属于整个应用程序的SharedPreferences
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);

		// 获取配置的值
		Item item;
		Config cfg = Config.getInstance();
		for (Entry<String, ?> e : settings.getAll().entrySet()) {
			item = cfg.getItem(e.getKey());
			if (item != null) {
				item.setSelected((Boolean) e.getValue());
			}
		}
		Log.d(tag, "cfg=" + cfg);
	}

	// 设置界面返回的结果
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_SETTING) {// 从设置界面返回的处理
			Log.d(tag, "requestCode=" + requestCode);
		} else {
			// 其他Intent返回的结果
			Log.d(tag, "requestCode?=" + requestCode);
		}
	}
}