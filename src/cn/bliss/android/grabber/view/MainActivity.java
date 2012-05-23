/**
 * 
 */
package cn.bliss.android.grabber.view;

import cn.bliss.android.grabber.R;
import android.app.Activity;
import android.os.Bundle;

/**
 * @author dragon
 * 
 */
public class MainActivity extends Activity {
	public int add(int a) {
		return a++;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}
}
