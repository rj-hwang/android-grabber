package cn.gm.android.grabber.test;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;
import android.os.Environment;
import cn.bliss.grabber.Item;

public class ItemTest extends TestCase {
	public void testExcute() throws IOException {
		Item item = new Item();
		item.setFrom("http://ww4.sinaimg.cn/bmiddle/9b8dcd88gw1dss5sp9tb8j.jpg");
		File to = new File(Environment.getExternalStorageDirectory(),
				"grabber/t.j");
		assertFalse(to.exists());
		item.setTo(to.getAbsolutePath());
		item.excute();
		assertTrue(to.exists());
		to.delete();
		assertFalse(to.exists());
	}
}
