package cn.bliss.grabber;

import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import cn.bliss.grabber.event.EventType;
import cn.bliss.grabber.event.GrabEvent;
import cn.bliss.grabber.view.SearcherView;
import cn.bliss.grabber.view.SearcherView.OnStartListener;
import cn.bliss.grabber.view.SearcherView.OnStopListener;

/**
 * 抓取器
 * 
 * @author dragon
 * 
 */
public class Grabber {
	private static final String tag = Grabber.class.getName();
	private Searcher searcher;
	private SearcherView ui;
	private boolean running;
	private Handler handler;
	private Thread grabThread;

	public Grabber(Searcher searcher, SearcherView ui) {
		this.searcher = searcher;
		this.ui = ui;
		initHandler();

		// 事件侦听
		bindUIEvent();
	}

	private void bindUIEvent() {
		// 开始事件
		ui.setOnStartListener(new OnStartListener() {
			@Override
			public void onStart(SearcherView view) {
				Log.d(tag, "on searcher start");
				running = true;

				// 抛出启动事件

				// 启动抓取线程
				startGrabThread();
			}
		});

		// 停止事件
		ui.setOnStopListener(new OnStopListener() {
			@Override
			public void onStop(SearcherView view) {
				Log.d(tag, "on searcher stop");
				running = false;

				// 抛出停止事件

				// 停止线抓取程
				if (grabThread != null)
					grabThread.interrupt();
			}
		});
	}

	public void start() {
		running = true;
		ui.start();
	}

	public void stop() {
		running = false;
		ui.stop();
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isChecked() {
		return ui.isChecked();
	}

	public void setChecked(boolean checked) {
		ui.setChecked(checked);
	}

	/**
	 * 启动抓取线程
	 */
	protected void startGrabThread() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				// 将回调信息传给界面的UI线程显示
				try {
					Message message = new Message();

					// 搜索抓取项
					List<Item> items = searcher.list();
					message = new Message();
					message.what = EventType.Finded.ordinal();// 搜素完毕
					Bundle data = new Bundle();
					message.setData(data);
					data.putString("uid", searcher.getUid());
					data.putInt("count", items.size());
					data.putString("type", items.isEmpty() ? "" : items.get(0)
							.getClass().getSimpleName());
					handler.sendMessage(message);

					// 侦听抓取事件，转发为线程消息
					final int count = items.size();
					for (Item item : items) {
						item.setOnProcessListener(new Item.OnProcessListener() {
							@Override
							public void onProcess(GrabEvent event) {
								Log.i(tag, "process=" + event.getIndex());
								Message message = new Message();
								Bundle data = new Bundle();
								message.setData(data);
								data.putString("uid", searcher.getUid());
								data.putInt("count", count);
								data.putInt("index", event.getIndex());
								if (event.getError() != null)
									data.putString("msg", event.getError()
											.getMessage());
								message.what = event.getType().ordinal();
								handler.sendMessage(message);
							}
						});
					}

					// 逐项抓取
					int index = 0;
					for (Item item : items) {
						if (running) {// 执行抓取
							Log.i(tag, "from=" + item.getFrom());
							item.excute();
						} else {// 被中途终止了
							message = new Message();
							message.what = EventType.Stoped.ordinal();// 停抓
							data = new Bundle();
							message.setData(data);
							data.putString("uid", searcher.getUid());
							data.putInt("index", index);
							data.putInt("count", count);
							handler.sendMessage(message);
							break;
						}
						index++;
					}

					// 完成抓取的处理
					message = new Message();
					message.what = EventType.Finished.ordinal();
					data = new Bundle();
					message.setData(data);
					handler.sendMessage(message);
				} catch (Exception e) {
					Message message = new Message();
					message.what = EventType.Error.ordinal();
					Bundle data = new Bundle();
					message.setData(data);
					data.putString("msg", e.getMessage());
					handler.sendMessage(message);
				}
			}
		});
		thread.start();
	}

	private void initHandler() {
		this.handler = new Handler() {
			int successCount, pageIndex, itemIndex;
			String type;

			@Override
			public void handleMessage(Message m) {
				String uid = searcher.getUid();
				Log.d(tag, "uid=" + uid);
				Log.d(tag, "what=" + m.what);
				Log.d(tag, "count=" + m.getData().getInt("count"));
				Log.d(tag, "msg=" + m.getData().getString("msg"));
				if (m.what == EventType.Finded.ordinal()) {
					successCount = 0;
					pageIndex = -1;
					type = m.getData().getString("type");
					Log.d(tag, "type=" + type);
					// 显示要抓取的总数量
					ui.setProgress("..." + m.getData().getInt("count"));
				} else if (m.what == EventType.GrabOneItem.ordinal()) {
					successCount += 1;
					itemIndex = m.getData().getInt("index");
					Log.d(tag, "itemIndex=" + itemIndex);
					// 显示抓取进度
					if (pageIndex != -1) {
						ui.setProgress("..." + (itemIndex + 1) + "/? "
								+ (pageIndex + 2) + "/"
								+ m.getData().getInt("count"));
					} else {
						ui.setProgress("..." + (itemIndex + 1) + "/"
								+ m.getData().getInt("count"));
					}
				} else if (m.what == EventType.GrabOnePage.ordinal()) {
					pageIndex = m.getData().getInt("index");
					Log.d(tag, "pageIndex=" + pageIndex);
					// 显示抓取进度
					ui.setProgress("..." + (pageIndex + 2) + "/"
							+ m.getData().getInt("count"));
				} else if (m.what == EventType.Skip.ordinal()) {
					// 显示抓取进度
					ui.setProgress("..." + m.getData().getInt("index") + "/"
							+ m.getData().getInt("count"));
				} else if (m.what == EventType.Finished.ordinal()) {
					Log.e(tag, "successCount=" + successCount);
					if (successCount > 0) {
						ui.addCount(successCount);// 累计成功抓取的总数
					}
					running = false;
					ui.setProgress(getText(R.string.info_grabbed).toString()
							+ " " + successCount);
					ui.finish();
				} else if (m.what == EventType.Error.ordinal()) {
					Log.e(tag, "error:" + m.getData().getString("msg"));
					running = false;
					// 强制显示异常信息
					Toast.makeText(
							ui.getContext(),
							getText(R.string.info_grab) + "'" + ui.getName()
									+ "'" + getText(R.string.info_error) + ":"
									+ m.getData().getString("msg"),
							Toast.LENGTH_LONG).show();

					// 处理界面显示
					ui.setProgress(getText(R.string.info_fireEeror).toString());
					ui.error();
				}
				super.handleMessage(m);
			}
		};
	}

	private CharSequence getText(int resId) {
		return ui.getContext().getText(resId);
	}
}
